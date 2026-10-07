package de.shurablack.core.event.interaction;

import de.shurablack.core.event.EventWorker;
import de.shurablack.core.event.annotation.*;
import de.shurablack.core.event.annotation.command.SlashCommand;
import de.shurablack.core.event.annotation.command.CommandHandler;
import de.shurablack.core.util.ClassScan;
import de.shurablack.core.util.FileUtil;
import de.shurablack.core.util.LocalData;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.dv8tion.jda.api.Permission;
import org.json.JSONArray;
import org.json.JSONObject;

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
 * The InteractionSet class is a utility class that stores a list of {@link Interaction} objects and an associated {@link EventWorker} object.
 * <br><br>
 * It provides methods for creating and setting the interactions and for accessing the stored interactions and worker
 * </p>
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
     * This is a static factory method that creates a new InteractionSet object.
     * @param worker the associated {@link EventWorker}
     * @param interactions the set of {@link Interaction Options}
     * @return the built InteractionSet
     */
    public static InteractionSet create(final EventWorker worker, final Interaction... interactions) {
        InteractionSet interactionSet = new InteractionSet();
        interactionSet.worker = worker;
        interactionSet.interactions.addAll(Arrays.asList(interactions));
        return interactionSet;
    }

    /**
     * @return the Options stored in the Set
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
     * This static method creates a list of InteractionSets from a JSON file.
     * <br><br>
     * Check out the GitHub Wiki for more information's, on how to structure the JSON file.
     *
     * @param path the path to the JSON file
     * @return the list of InteractionSets
     */
    public static List<InteractionSet> fromJson(final String path) {
        JSONObject jsonObject = FileUtil.loadJson(path);
        if (jsonObject == null) {
            return new ArrayList<>();
        }
        List<InteractionSet> interactionSets = new ArrayList<>();

        JSONArray jsonArray = jsonObject.getJSONArray("interactionSets");
        try {
            for (int idx = 0; idx < jsonArray.length(); idx++) {
                JSONObject interactionSet = jsonArray.getJSONObject(idx);
                Class<?> cls = Class.forName(interactionSet.getString("worker"));
                Object worker = cls.getDeclaredConstructor().newInstance();

                List<Interaction> interactionsList = new ArrayList<>();
                JSONArray interactions = interactionSet.getJSONArray("interactions");
                for (int subidx = 0 ; subidx < interactions.length(); subidx++) {
                    JSONObject interaction = interactions.getJSONObject(subidx);
                    Type type = Type.valueOf(interaction.getString("type"));
                    String identifier = interaction.getString("identifier");
                    long globalCooldown = interaction.getLong("globalCooldown");
                    long userCooldown = interaction.getLong("userCooldown");
                    JSONArray channelRestriction = interaction.getJSONArray("channelRestriction");
                    List<String> channelRestrictionList = new ArrayList<>();
                    for (int i = 0; i < channelRestriction.length(); i++) {
                        channelRestrictionList.add(LocalData.getChannelID(channelRestriction.getString(i)));
                    }
                    Interaction interactionObject = Interaction.create(type, identifier)
                            .setGlobalCD(globalCooldown)
                            .setUserCD(userCooldown)
                            .setChannelRestriction(channelRestrictionList);
                    // Optional, permission names like "MESSAGE_MANAGE"
                    JSONArray permissions = interaction.optJSONArray("requiredPermissions");
                    for (int i = 0; permissions != null && i < permissions.length(); i++) {
                        interactionObject.setRequiredPermissions(Permission.valueOf(permissions.getString(i)));
                    }
                    interactionsList.add(interactionObject);
                }
                InteractionSet interactionSetObject = InteractionSet.create(
                        (EventWorker) worker,
                        interactionsList.toArray(new Interaction[0])
                );
                interactionSets.add(interactionSetObject);
            }
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException
                 | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return interactionSets;
    }

    /**
     * This static method creates a list of InteractionSets from the {@link EventWorker} classes in the classpath.
     * <br><br>
     * It scans the classpath for all classes that extend {@link EventWorker} and creates an InteractionSet for each
     * class that has the {@link EventProcess} or {@link RedirectedProcess} annotation.
     *
     * @see EventProcess
     * @see RedirectedProcess
     * @see ExtendedEventProcess
     * @see DisabledWorker
     * @return the list of InteractionSets
     */
    private static List<InteractionSet> internalFromAnnotation(final Function<Class<?>, ?> workerFactory) {
        final Logger logger = LoggerFactory.getLogger(InteractionSet.class);
        final List<InteractionSet> interactionSets = new ArrayList<>();

        try (ScanResult result = ClassScan.scan()) {
            final Map<String, SlashCommand> declarations = new HashMap<>();
            for (ClassInfo info : result.getClassesWithAnnotation(SlashCommand.class)) {
                final SlashCommand command = info.loadClass().getAnnotation(SlashCommand.class);
                declarations.put(command.name(), command);
            }

            // One instance per class, even if it has worker methods and sub command handlers
            final Map<String, ClassInfo> classes = new LinkedHashMap<>();
            result.getSubclasses(EventWorker.class).forEach(info -> classes.put(info.getName(), info));
            // Internal worker, gets created per handler method
            classes.remove(SlashCommandInvoker.class.getName());
            result.getClassesWithMethodAnnotation(CommandHandler.class).forEach(info -> classes.putIfAbsent(info.getName(), info));

            for (ClassInfo info : classes.values()) {
                final Class<?> workerClass = info.loadClass();
                if (Modifier.isAbstract(workerClass.getModifiers())) {
                    continue;
                }
                if (workerClass.isAnnotationPresent(DisabledWorker.class)) {
                    logger.debug("Worker {} is disabled", workerClass.getName());
                    continue;
                }
                final Object instance = workerFactory.apply(workerClass);

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
     * Creates the interactions of the {@link EventProcess} / {@link RedirectedProcess} annotations
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
     * Creates one InteractionSet per {@link CommandHandler} method, registered as guild and global slash event
     * with the full command name as identifier, e.g. "ping" or "music play"
     * @throws de.shurablack.core.event.ImplementationException if a handler doesnt fit its declaration
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
     * This static method creates a list of InteractionSets from the {@link EventWorker} classes in the classpath.
     * <br><br>
     * It scans the classpath for all classes that extend {@link EventWorker} and creates an InteractionSet for each
     * class that has the {@link EventProcess} or {@link RedirectedProcess} annotation.
     *
     * @see EventProcess
     * @see RedirectedProcess
     * @see ExtendedEventProcess
     * @see DisabledWorker
     * @return the list of InteractionSets
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
     * This static method creates a list of InteractionSets from the {@link EventWorker} classes in the classpath.
     * <br><br>
     * It scans the classpath for all classes that extend {@link EventWorker} and creates an InteractionSet for each
     * class that has the {@link EventProcess} or {@link RedirectedProcess} annotation.
     *
     * @see EventProcess
     * @see RedirectedProcess
     * @see ExtendedEventProcess
     * @see DisabledWorker
     * @param workerFactory a function that takes a Class object and returns an instance of that class. This is used to create the EventWorker instances instead of using the default constructor.
     * @return the list of InteractionSets
     */
    public static List<InteractionSet> fromAnnotation(final Function<Class<?>, ?> workerFactory) {
        return internalFromAnnotation(workerFactory);
    }

}
