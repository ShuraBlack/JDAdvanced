package de.shurablack.core.event.interaction;

import de.shurablack.core.event.EventWorker;
import de.shurablack.core.event.ImplementationException;
import de.shurablack.core.event.annotation.command.CommandOption;
import de.shurablack.core.event.annotation.command.Option;
import de.shurablack.core.event.annotation.command.SlashCommand;
import de.shurablack.core.event.annotation.command.CommandHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * <p>
 * Calls a {@link CommandHandler} method. The parameter resolvers are built and validated once on registration,
 * so a mismatch between handler and {@link SlashCommand} declaration fails on startup instead of on the first use.
 * <br><br>
 * It's an {@link EventWorker}, so the event handlers apply cooldowns, channel restrictions and permissions as usual.
 * <br><br>
 * Parameters annotated with {@link Option} get the converted option value (null if the option is missing),
 * all other parameters get injected from the event ({@link SlashCommandInteractionEvent}, {@link Member}, {@link User},
 * {@link Guild}, {@link MessageChannelUnion}, {@link DiscordLocale}).
 * </p>
 *
 * @version core-1.1.0
 * @date 07.10.2026
 * @author ShuraBlack
 */
final class SlashCommandInvoker extends EventWorker {

    /** Converts an option to a parameter type and lists the option types it accepts */
    private static final class Converter {
        final Function<OptionMapping, Object> convert;
        final Set<OptionType> accepts;

        Converter(final Function<OptionMapping, Object> convert, final OptionType first, final OptionType... rest) {
            this.convert = convert;
            this.accepts = EnumSet.of(first, rest);
        }
    }

    /** Supported {@link Option} parameter types */
    private static final Map<Class<?>, Converter> CONVERTERS = new HashMap<>();

    /** Supported parameter types without {@link Option}, resolved from the event */
    private static final Map<Class<?>, Function<SlashCommandInteractionEvent, Object>> CONTEXT = new HashMap<>();

    static {
        converter(new Converter(OptionMapping::getAsString, OptionType.STRING), String.class);
        converter(new Converter(OptionMapping::getAsInt, OptionType.INTEGER), Integer.class, int.class);
        converter(new Converter(OptionMapping::getAsLong, OptionType.INTEGER), Long.class, long.class);
        converter(new Converter(OptionMapping::getAsDouble, OptionType.NUMBER, OptionType.INTEGER), Double.class, double.class);
        converter(new Converter(OptionMapping::getAsBoolean, OptionType.BOOLEAN), Boolean.class, boolean.class);
        converter(new Converter(OptionMapping::getAsUser, OptionType.USER, OptionType.MENTIONABLE), User.class);
        converter(new Converter(OptionMapping::getAsMember, OptionType.USER, OptionType.MENTIONABLE), Member.class);
        converter(new Converter(OptionMapping::getAsRole, OptionType.ROLE, OptionType.MENTIONABLE), Role.class);
        converter(new Converter(OptionMapping::getAsMentionable, OptionType.MENTIONABLE, OptionType.USER, OptionType.ROLE), IMentionable.class);
        converter(new Converter(OptionMapping::getAsChannel, OptionType.CHANNEL), GuildChannel.class, GuildChannelUnion.class);
        converter(new Converter(OptionMapping::getAsAttachment, OptionType.ATTACHMENT), Message.Attachment.class);

        CONTEXT.put(SlashCommandInteractionEvent.class, event -> event);
        CONTEXT.put(Member.class, SlashCommandInteractionEvent::getMember);
        CONTEXT.put(User.class, SlashCommandInteractionEvent::getUser);
        CONTEXT.put(Guild.class, SlashCommandInteractionEvent::getGuild);
        CONTEXT.put(MessageChannelUnion.class, SlashCommandInteractionEvent::getChannel);
        CONTEXT.put(DiscordLocale.class, SlashCommandInteractionEvent::getUserLocale);
    }

    /** Registers the converter for all given parameter types */
    private static void converter(final Converter converter, final Class<?>... types) {
        for (Class<?> type : types) {
            CONVERTERS.put(type, converter);
        }
    }

    /** The instance which declares the method */
    private final Object target;

    /** The handler method */
    private final Method method;

    /** One resolver per method parameter */
    private final List<Function<SlashCommandInteractionEvent, Object>> resolvers;

    private SlashCommandInvoker(final Object target, final Method method, final List<Function<SlashCommandInteractionEvent, Object>> resolvers) {
        this.target = target;
        this.method = method;
        this.resolvers = resolvers;
    }

    /**
     * Builds the invoker and validates the parameters against the declaration. Makes the method accessible
     * @param target the instance which declares the method
     * @param method the {@link CommandHandler} method
     * @param declaration the {@link SlashCommand} with the same name, null if it isn't declared via annotation
     *                    (only the parameter types are checked then)
     * @return the invoker
     * @throws ImplementationException if a parameter doesn't fit the declaration or isn't supported,
     *                                 or the instance is an unsupported proxy
     */
    static SlashCommandInvoker create(final Object target, final Method method, final SlashCommand declaration) {
        final CommandHandler handler = method.getAnnotation(CommandHandler.class);
        final String where = String.format("%s#%s (/%s)", method.getDeclaringClass().getName(), method.getName(), commandName(handler));
        final CommandOption[] declared = declaration == null ? null : declaredOptions(handler, declaration, where);

        final List<Function<SlashCommandInteractionEvent, Object>> resolvers = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            final Option option = parameter.getAnnotation(Option.class);
            resolvers.add(option == null
                    ? contextResolver(parameter, where)
                    : optionResolver(parameter, option.value(), declared, where));
        }

        checkInstance(target, method, where);
        method.setAccessible(true);
        return new SlashCommandInvoker(target, method, resolvers);
    }

    /**
     * Instances from a dependency injection container can be proxies. Interface based proxies aren't instances of the class,
     * and subclass proxies (e.g. Spring CGLIB) don't intercept non-public methods, so those would run on the empty proxy fields
     * @throws ImplementationException if the instance isn't of the declaring class, or is a subclass and the method isn't public
     */
    private static void checkInstance(final Object target, final Method method, final String where) {
        final Class<?> declaring = method.getDeclaringClass();
        if (!declaring.isInstance(target)) {
            throw new ImplementationException(String.format("%s: the instance is a %s, not a %s. Interface based proxies arent supported, use class based proxies",
                    where, target.getClass().getName(), declaring.getName()));
        }
        if (target.getClass() != declaring && !Modifier.isPublic(method.getModifiers())) {
            throw new ImplementationException(String.format("%s: the instance is a proxy (%s), make the method public so the proxy can delegate it",
                    where, target.getClass().getName()));
        }
    }

    /**
     * @param handler the annotation of the handler method
     * @return the full command name, e.g. "ping" or "music play"
     */
    static String commandName(final CommandHandler handler) {
        return handler.sub().isEmpty() ? handler.command() : handler.command() + " " + handler.sub();
    }

    /**
     * @return the options of the command or of the subcommand the handler belongs to
     * @throws ImplementationException if the command has subcommands but the handler sets none,
     *                                 or the subcommand isn't declared
     */
    private static CommandOption[] declaredOptions(final CommandHandler handler, final SlashCommand declaration, final String where) {
        if (handler.sub().isEmpty()) {
            if (declaration.subCommands().length > 0) {
                throw new ImplementationException(where + ": the command has sub commands, set sub in @CommandHandler");
            }
            return declaration.options();
        }
        return Arrays.stream(declaration.subCommands())
                .filter(s -> s.name().equals(handler.sub())).findFirst()
                .orElseThrow(() -> new ImplementationException(where + ": sub command isnt declared in @SlashCommand"))
                .options();
    }

    /**
     * @return the resolver of a parameter without {@link Option}, taken from the event
     * @throws ImplementationException if the parameter type can't be injected
     */
    private static Function<SlashCommandInteractionEvent, Object> contextResolver(final Parameter parameter, final String where) {
        final Function<SlashCommandInteractionEvent, Object> resolver = CONTEXT.get(parameter.getType());
        if (resolver == null) {
            throw new ImplementationException(String.format("%s: parameter %s of type %s is neither injectable nor annotated with @Option",
                    where, parameter.getName(), parameter.getType().getSimpleName()));
        }
        return resolver;
    }

    /**
     * Creates the resolver of an {@link Option} parameter, which returns null if the option is missing.
     * With a declaration, the option must exist, its type must be convertible and optional options need a wrapper type
     * @param declared the declared options, null to skip the checks against the declaration
     * @return the resolver
     * @throws ImplementationException if the parameter type isn't supported or doesn't fit the declaration
     */
    private static Function<SlashCommandInteractionEvent, Object> optionResolver(final Parameter parameter, final String name,
                                                                               final CommandOption[] declared, final String where) {
        final Class<?> type = parameter.getType();
        final Converter converter = CONVERTERS.get(type);
        if (converter == null) {
            throw new ImplementationException(String.format("%s: option '%s' has the unsupported type %s", where, name, type.getSimpleName()));
        }

        if (declared != null) {
            final CommandOption option = Arrays.stream(declared).filter(o -> o.name().equals(name)).findFirst()
                    .orElseThrow(() -> new ImplementationException(String.format("%s: option '%s' isnt declared", where, name)));
            if (!converter.accepts.contains(option.type())) {
                throw new ImplementationException(String.format("%s: option '%s' is %s, which cant be converted to %s",
                        where, name, option.type(), type.getSimpleName()));
            }
            if (type.isPrimitive() && !option.required()) {
                throw new ImplementationException(String.format("%s: option '%s' is optional and can be null, use a wrapper type (e.g. Integer) instead of %s",
                        where, name, type.getSimpleName()));
            }
        }

        return event -> {
            final OptionMapping mapping = event.getOption(name);
            return mapping == null ? null : converter.convert.apply(mapping);
        };
    }

    /**
     * Resolves the parameters and calls the handler method
     * @param event the slash command event
     * @throws RuntimeException the exception of the handler itself (checked ones wrapped in an {@link IllegalStateException})
     */
    void invoke(final SlashCommandInteractionEvent event) {
        final Object[] args = new Object[resolvers.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = resolvers.get(i).apply(event);
        }
        try {
            method.invoke(target, args);
        } catch (InvocationTargetException e) {
            // Rethrow the exception of the handler itself, so the logs show the real cause
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException(cause);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Calls the handler method, the other arguments are resolved from the event */
    @Override
    public void processGuildSlashEvent(final Member member, final MessageChannelUnion channel, final String name, final SlashCommandInteractionEvent event) {
        invoke(event);
    }

    /** Calls the handler method, the other arguments are resolved from the event */
    @Override
    public void processGlobalSlashEvent(final User user, final PrivateChannel channel, final String name, final SlashCommandInteractionEvent event) {
        invoke(event);
    }
}
