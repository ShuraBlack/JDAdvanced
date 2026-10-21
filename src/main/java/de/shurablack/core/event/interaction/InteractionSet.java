package de.shurablack.core.event.interaction;

import de.shurablack.core.event.EventWorker;
import de.shurablack.core.event.annotation.*;
import de.shurablack.core.event.annotation.command.SlashCommand;
import de.shurablack.core.event.annotation.command.CommandHandler;
import de.shurablack.core.util.ClassScan;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * <p>
 * Stores the {@link Interaction Interactions} of one {@link EventWorker}.
 * <br><br>
 * Sets are discovered from the annotations by {@link #fromAnnotation()} and passed to
 * {@link de.shurablack.core.event.handler.EventHandler#registerEvent(List)}:
 * </p>
 * <pre>{@code
 * NativeEventHandler handler = NativeEventHandler.createDefault()
 *         .registerEvent(InteractionSet.fromAnnotation());
 * }</pre>
 *
 * @see Interaction Interaction
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class InteractionSet {


    /** The EventWorker object associated with all {@link Interaction Interactions} */
    private EventWorker worker;

    /** The list of Interaction objects stored in the InteractionSet */
    private final List<Interaction> interactions = new ArrayList<>();

    private InteractionSet() { }

    /**
     * Creates an InteractionSet, used by the annotation scan
     * @param worker the associated {@link EventWorker}
     * @param interactions the interactions handled by the worker
     * @return the built InteractionSet
     */
    static InteractionSet create(final EventWorker worker, final Interaction... interactions) {
        InteractionSet interactionSet = new InteractionSet();
        interactionSet.worker = worker;
        interactionSet.interactions.addAll(Arrays.asList(interactions));
        return interactionSet;
    }

    /**
     * @return the interactions stored in the set (mutable internal list)
     */
    public List<Interaction> getInteractions() {
        return interactions;
    }

    /**
     * @return the associated {@link EventWorker}
     */
    public EventWorker getWorker() {
        return worker;
    }

    /**
     * Scans the packages configured in {@link ClassScan} for {@link EventWorker} subclasses and classes declaring
     * {@link CommandHandler} methods, creates one instance per concrete class and builds their InteractionSets.
     * Anonymous, abstract and {@link DisabledWorker} classes are skipped.
     * @param instanceFactory creates the instance of a discovered class, null skips the class
     * @return the list of InteractionSets
     */
    private static List<InteractionSet> internalFromAnnotation(final Function<Class<?>, ?> instanceFactory) {
        final Logger logger = LoggerFactory.getLogger(InteractionSet.class);
        final List<InteractionSet> interactionSets = new ArrayList<>();

        try (ScanResult result = ClassScan.scan()) {
            final Map<String, SlashCommand> declarations = new HashMap<>();
            for (ClassInfo info : result.getClassesWithAnnotation(SlashCommand.class)) {
                final SlashCommand command = info.loadClass().getAnnotation(SlashCommand.class);
                declarations.put(command.name(), command);
            }

            // One instance per class, even if it has worker methods and subcommand handlers
            final Map<String, ClassInfo> classes = new LinkedHashMap<>();
            result.getSubclasses(EventWorker.class).forEach(info -> classes.put(info.getName(), info));
            // Internal worker, gets created per handler method in fromCommandHandlers
            classes.remove(SlashCommandInvoker.class.getName());
            // Only classes declaring handlers themselves, subclasses inheriting them would register a second instance
            result.getClassesWithMethodAnnotation(CommandHandler.class).stream()
                    .filter(info -> info.hasDeclaredMethodAnnotation(CommandHandler.class))
                    .forEach(info -> classes.putIfAbsent(info.getName(), info));

            for (ClassInfo info : classes.values()) {
                // Anonymous classes can't be created by a factory or default constructor. External classes are
                // superclasses outside of the scanned packages, ClassGraph reports them too
                if (info.isAnonymousInnerClass() || info.isExternalClass()) {
                    continue;
                }
                final Class<?> workerClass = info.loadClass();
                if (Modifier.isAbstract(workerClass.getModifiers())) {
                    continue;
                }
                if (workerClass.isAnnotationPresent(DisabledWorker.class)) {
                    logger.debug("Worker {} is disabled", workerClass.getName());
                    continue;
                }
                final Object instance = instanceFactory.apply(workerClass);
                if (instance == null) {
                    // e.g. a dependency injection container without a bean for this class
                    logger.debug("No instance for {}, skipped", workerClass.getName());
                    continue;
                }

                if (instance instanceof EventWorker) {
                    final InteractionSet set = fromWorker(workerClass, (EventWorker) instance, logger);
                    if (set != null) {
                        interactionSets.add(set);
                    }
                }
                interactionSets.addAll(fromCommandHandlers(workerClass, instance, declarations, logger));
            }
            logger.info("Found {} worker classes", classes.size());
        }

        return interactionSets;
    }

    /**
     * Creates the interactions of a worker. A class level {@link RedirectedProcess} takes precedence,
     * otherwise every declared {@link EventProcess} method is used (its type is derived from the method name,
     * see {@link Type#fromFunctionName(String)}). {@link BypassGuildValidity} only works on {@link EventProcess} methods
     * @return the InteractionSet or null if the worker has no interactions
     */
    private static InteractionSet fromWorker(final Class<?> workerClass, final EventWorker worker, final Logger logger) {
        final List<Interaction> interactionsList = new ArrayList<>();

        if (workerClass.isAnnotationPresent(RedirectedProcess.class)) {
            for (ExtendedEventProcess extended : workerClass.getAnnotation(RedirectedProcess.class).value()) {
                interactionsList.add(Interaction.create(extended.type(), extended.identifier())
                        .setGlobalCD(extended.globalCooldown())
                        .setUserCD(extended.userCooldown())
                        .setChannelRestriction(Arrays.asList(extended.restrictedChannel()))
                        .setRequiredPermissions(extended.requiredPermissions()));
            }
        } else {
            for (Method method : workerClass.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(EventProcess.class)) {
                    continue;
                }
                final Type type = Type.fromFunctionName(method.getName());
                if (type == null) {
                    logger.warn("Method {} in {} has no valid type", method.getName(), workerClass.getName());
                    continue;
                }

                EventProcess eventProcess = method.getAnnotation(EventProcess.class);

                if (method.isAnnotationPresent(BypassGuildValidity.class)) {
                    logger.warn("Method {} in {} is annotated with @BypassGuildValidity!", method.getName(), workerClass.getName());
                }

                interactionsList.add(Interaction.create(type, eventProcess.identifier(), method.isAnnotationPresent(BypassGuildValidity.class))
                        .setGlobalCD(eventProcess.globalCooldown())
                        .setUserCD(eventProcess.userCooldown())
                        .setChannelRestriction(Arrays.asList(eventProcess.restrictedChannel()))
                        .setRequiredPermissions(eventProcess.requiredPermissions()));
            }
        }

        return interactionsList.isEmpty() ? null : InteractionSet.create(worker, interactionsList.toArray(new Interaction[0]));
    }

    /**
     * Creates one InteractionSet per declared {@link CommandHandler} method, registered as guild and global slash event
     * with the full command name as identifier, e.g. "ping" or "music play"
     * @throws de.shurablack.core.event.ImplementationException if a handler doesn't fit its declaration
     */
    private static List<InteractionSet> fromCommandHandlers(final Class<?> workerClass, final Object instance,
                                                               final Map<String, SlashCommand> declarations, final Logger logger) {
        final List<InteractionSet> sets = new ArrayList<>();
        for (Method method : workerClass.getDeclaredMethods()) {
            final CommandHandler handler = method.getAnnotation(CommandHandler.class);
            if (handler == null) {
                continue;
            }
            final SlashCommand declaration = declarations.get(handler.command());
            if (declaration == null) {
                logger.warn("/{} isnt declared via @SlashCommand, the options of {}#{} cant be validated",
                        handler.command(), workerClass.getName(), method.getName());
            }

            final SlashCommandInvoker invoker = SlashCommandInvoker.create(instance, method, declaration);
            final String identifier = SlashCommandInvoker.commandName(handler);
            final List<Interaction> interactions = new ArrayList<>();
            for (Type type : new Type[]{Type.GUILD_SLASH, Type.GLOBAL_SLASH}) {
                interactions.add(Interaction.create(type, identifier)
                        .setGlobalCD(handler.globalCooldown())
                        .setUserCD(handler.userCooldown())
                        .setChannelRestriction(Arrays.asList(handler.restrictedChannel()))
                        .setRequiredPermissions(handler.requiredPermissions()));
            }
            sets.add(InteractionSet.create(invoker, interactions.toArray(new Interaction[0])));
        }
        return sets;
    }

    /**
     * Discovers the interactions declared via annotations and creates the instances with their no-arg constructor.
     * <br><br>
     * The scan covers all {@link EventWorker} subclasses ({@link EventProcess} methods or a {@link RedirectedProcess}
     * class annotation) and all classes declaring {@link CommandHandler} methods, see {@link #fromAnnotation(Function)}.
     * Message command identifiers get the {@link de.shurablack.core.event.handler.EventHandler#getPREFIX() prefix}
     * that is set at the time of the call.
     *
     * @see EventProcess
     * @see RedirectedProcess
     * @see ExtendedEventProcess
     * @see CommandHandler
     * @see DisabledWorker
     * @return the list of InteractionSets
     * @throws RuntimeException if a discovered class has no accessible no-arg constructor or it throws
     * @throws de.shurablack.core.event.ImplementationException if a {@link CommandHandler} doesn't fit its declaration
     */
    public static List<InteractionSet> fromAnnotation() {
        return internalFromAnnotation(cls -> {
            try {
                return cls.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Discovers the interactions declared via annotations, like {@link #fromAnnotation()}, but lets the caller create
     * the instances (e.g. a dependency injection container).
     * <br><br>
     * Abstract, anonymous and {@link DisabledWorker} classes are skipped. A {@link CommandHandler} class doesn't have to
     * extend {@link EventWorker}, a class can have both worker methods and {@link CommandHandler} methods (one instance).
     * <br><br>
     * Spring example, classes without a bean get skipped:
     * <pre>{@code
     * InteractionSet.fromAnnotation(cls -> context.getBeanProvider(cls).getIfAvailable());
     * }</pre>
     * Proxied beans (e.g. @Transactional) need public {@link CommandHandler} methods, see {@link CommandHandler}.
     *
     * @param instanceFactory creates the instance of a discovered class (worker or class with {@link CommandHandler} methods)
     *                        instead of the default constructor. Returning null skips the class
     * @return the list of InteractionSets
     * @throws de.shurablack.core.event.ImplementationException if a {@link CommandHandler} doesn't fit its declaration
     *                                                          or the instance is an unsupported proxy
     * @see EventProcess
     * @see RedirectedProcess
     * @see ExtendedEventProcess
     */
    public static List<InteractionSet> fromAnnotation(final Function<Class<?>, ?> instanceFactory) {
        return internalFromAnnotation(instanceFactory);
    }

}
