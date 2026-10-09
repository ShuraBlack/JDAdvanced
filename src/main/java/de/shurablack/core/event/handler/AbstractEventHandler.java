package de.shurablack.core.event.handler;

import de.shurablack.core.event.Event;
import de.shurablack.core.event.interaction.Interaction;
import de.shurablack.core.event.interaction.InteractionSet;
import de.shurablack.core.event.interaction.Type;
import de.shurablack.core.event.validation.Validation;
import de.shurablack.core.event.validation.ValidationEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * <p>
 * Shared foundation of {@link NativeEventHandler} and {@link DispatchEventHandler}: registration,
 * validation (bots, permissions, channel restriction, cooldowns, admin bypass) and the routing to the
 * {@link de.shurablack.core.event.EventWorker}.
 * <br><br>
 * Subclasses only decide where the validation and the worker call run, see {@link #execute(Type, String, Runnable)}.
 * The event calls are thread-safe, the registry is concurrent and the cooldown check is synchronized.
 * </p>
 *
 * @version core-1.1.0
 * @date 07.10.2026
 * @author ShuraBlack
 */
public abstract class AbstractEventHandler extends EventHandler {

    /** Above this amount of cooldown entries, expired ones get removed */
    private static final int COOLDOWN_CLEANUP_THRESHOLD = 10_000;

    /** Class Logger */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Indicating whether requests from other bots should be ignored */
    private boolean ignoreBotRequest = true;

    /** Indicating whether error feedback should be sent */
    private boolean errorCallback = true;

    /** Indicating whether the admin bypass is enabled */
    private boolean adminBypass = false;

    /** Maps event types to maps of event identifier strings to Event objects */
    private final Map<Type, Map<String, Event>> events = new EnumMap<>(Type.class);

    /** Maps a cooldown key (global: type+identifier, user: userId:type+identifier) to its expiry time in epoch milliseconds */
    private final Map<String, Long> cooldowns = new HashMap<>();

    /**
     * Creates a handler with the default settings and an empty registry for every {@link Type}
     */
    protected AbstractEventHandler() {
        for (Type t : Type.values()) {
            events.put(t, new ConcurrentHashMap<>());
        }
    }

    /**
     * Runs the task, the subclass decides on which thread
     * @param type the interaction type
     * @param identifier the unique string of the event
     * @param task validates the event and calls the worker
     */
    protected abstract void execute(Type type, String identifier, Runnable task);

    /**
     * Applies the settings of the create factory methods
     * @param prefix the prefix for message commands, null or empty for {@link EventHandler#DEFAULT_PREFIX}
     * @param ignoreBotRequest whether events triggered by bots get rejected
     * @param errorFeedback whether failed validations get an ephemeral error reply
     * @param adminBypass whether administrators skip the channel restriction and cooldowns of guild events
     */
    void configure(final String prefix, final boolean ignoreBotRequest, final boolean errorFeedback, final boolean adminBypass) {
        EventHandler.setPREFIX(prefix);
        this.ignoreBotRequest = ignoreBotRequest;
        this.errorCallback = errorFeedback;
        this.adminBypass = adminBypass;
    }

    /**
     * Registers the interactions of the given sets, an already registered identifier of the same type gets replaced
     * @param set the interaction sets, usually from {@link InteractionSet#fromAnnotation()}
     * @return the handler for chaining
     */
    @Override
    public AbstractEventHandler registerEvent(final List<InteractionSet> set) {
        for (InteractionSet os : set) {
            registerEventSubRoutine(os);
        }
        return this;
    }

    /**
     * Creates an {@link Event} for every interaction of the set and logs the registered worker
     * @param os the interactions and their worker
     */
    private void registerEventSubRoutine(final InteractionSet os) {
        for (Interaction o : os.getInteractions()) {
            final Event event = new Event(
                    os.getWorker(),
                    o.getGlobalCooldown(),
                    o.getUserCooldown(),
                    o.getChannelRestriction(),
                    o.isBypassGuildValidity(),
                    o.getRequiredPermissions()
            );
            this.events.get(o.getType()).put(o.getIdentifier(), event);
        }
        logger.info(String.format("Register worker: \u001B[33m%s\u001B[0m -> %s"
                , os.getWorker().getClass().getName()
                , os.getInteractions().stream().map(interaction -> "\u001B[33m" + interaction.getType().name()
                        + "\u001B[0m" + (interaction.isBypassGuildValidity() ? "(BP)" : ""))
                        .collect(Collectors.joining(", "))
        ));
    }

    /**
     * Checks both cooldowns before starting them, so a blocked call doesn't consume the other cooldown.
     * If the call isn't blocked, the cooldowns of the event that are greater than 0 get started
     * @param key the type and identifier of the event
     * @param userID the unique ID of the discord user
     * @param event the event object
     * @return true if there is a global or user cooldown still active
     */
    // ponytail: one global lock, cheap compared to the discord round trip. Per-key locks if it ever shows up in profiling
    private synchronized boolean onCooldown(final String key, final String userID, final Event event) {
        final long now = System.currentTimeMillis();
        if (cooldowns.size() > COOLDOWN_CLEANUP_THRESHOLD) {
            cooldowns.values().removeIf(expires -> expires <= now);
        }

        final String userKey = userID + ":" + key;
        if (cooldowns.getOrDefault(key, 0L) > now || cooldowns.getOrDefault(userKey, 0L) > now) {
            return true;
        }
        if (event.getGlobalCooldown() > 0) {
            cooldowns.put(key, now + event.getGlobalCooldown());
        }
        if (event.getUserCooldown() > 0) {
            cooldowns.put(userKey, now + event.getUserCooldown());
        }
        return false;
    }

    /**
     * Validates the call and runs the worker, otherwise sends the error feedback (if enabled and there is a callback).
     * Both happen inside {@link #execute(Type, String, Runnable)}
     * @param identifier the unique {@link String} for the event
     * @param type the Type of the event
     * @param user the discord user who triggered the event
     * @param member the guild member who triggered the event, null outside of guilds (selects the non-guild validation)
     * @param channelID the ID of the channel
     * @param callback the reply callback for the error feedback, null for messages and reactions
     * @param worker calls the worker of the validated {@link Event}
     */
    private void handle(final String identifier, final Type type, final User user, final Member member,
                        final String channelID, final IReplyCallback callback, final Consumer<Event> worker) {
        execute(type, identifier, () -> {
            final ValidationEvent e = member != null
                    ? isValidGuild(identifier, type, member, channelID)
                    : isValid(identifier, type, user, channelID);

            if (e.isSuccess()) {
                worker.accept(e.getEvent());
            } else if (callback != null && errorCallback) {
                callback.replyEmbeds(e.getValidation().getEmbed(callback.getUserLocale())).setEphemeral(true).queue();
            }
        });
    }

    @Override
    public void onButtonEvent(final String identifier, final ButtonInteractionEvent event) {
        handle(identifier, Type.BUTTON, event.getUser(), event.getMember(), event.getChannel().getId(), event,
                e -> e.getWorker().processButtonEvent(event.getMember(), event.getChannel(), event.getButton().getCustomId(), event));
    }

    /**
     * Prefers a registration of the full command name ("music play", e.g. by a
     * {@link de.shurablack.core.event.annotation.command.CommandHandler}) over the registration of the command itself ("music")
     * @return the identifier to look up
     */
    private String slashIdentifier(final Type type, final String identifier, final SlashCommandInteractionEvent event) {
        final String fullName = event.getFullCommandName();
        return this.events.get(type).containsKey(fullName) ? fullName : identifier;
    }

    @Override
    public void onGlobalSlashEvent(final String identifier, final SlashCommandInteractionEvent event) {
        handle(slashIdentifier(Type.GLOBAL_SLASH, identifier, event), Type.GLOBAL_SLASH, event.getUser(), null, event.getChannel().getId(), event,
                e -> e.getWorker().processGlobalSlashEvent(event.getUser(), event.getChannel().asPrivateChannel(), event.getName(), event));
    }

    @Override
    public void onGuildSlashEvent(final String identifier, final SlashCommandInteractionEvent event) {
        handle(slashIdentifier(Type.GUILD_SLASH, identifier, event), Type.GUILD_SLASH, event.getUser(), event.getMember(), event.getChannel().getId(), event,
                e -> e.getWorker().processGuildSlashEvent(event.getMember(), event.getChannel(), event.getSubcommandName(), event));
    }

    @Override
    public void onModalEvent(final String identifier, final ModalInteractionEvent event) {
        handle(identifier, Type.MODAL, event.getUser(), event.getMember(), event.getChannel().getId(), event,
                e -> e.getWorker().processModalEvent(event.getMember(), event.getChannel(), event.getModalId(), event));
    }

    @Override
    public void onPrivateChannelEvent(final String identifier, final MessageReceivedEvent event) {
        handle(identifier, Type.PRIVATE_CHANNEL, event.getAuthor(), null, event.getChannel().getId(), null,
                e -> e.getWorker().processPrivateChannelEvent(event.getAuthor(), event.getChannel().asPrivateChannel()
                        , event.getMessage().getContentRaw().replace(identifier, ""), event));
    }

    @Override
    public void onPublicChannelEvent(final String identifier, final MessageReceivedEvent event) {
        // member is null for webhook messages, those get the non-guild validation
        handle(identifier, Type.PUBLIC_CHANNEL, event.getAuthor(), event.getMember(), event.getChannel().getId(), null,
                e -> e.getWorker().processPublicChannelEvent(event.getMember(), event.getChannel()
                        , event.getMessage().getContentRaw().replace(identifier, ""), event));
    }

    @Override
    public void onPrivateReactionEvent(final String identifier, final MessageReactionAddEvent event) {
        handle(identifier, Type.PRIVATE_REACTION, event.getUser(), null, event.getChannel().getId(), null,
                e -> e.getWorker().processPrivateReactionEvent(event.getUser(), event.getChannel().asPrivateChannel()
                        , event.getEmoji().getName(), event));
    }

    @Override
    public void onPublicReactionEvent(final String identifier, final MessageReactionAddEvent event) {
        handle(identifier, Type.PUBLIC_REACTION, event.getUser(), event.getMember(), event.getChannel().getId(), null,
                e -> e.getWorker().processPublicReactionEvent(event.getMember(), event.getChannel()
                        , event.getEmoji().getName(), event));
    }

    @Override
    public void onStringSelectionMenuEvent(final String identifier, final StringSelectInteractionEvent event) {
        handle(identifier, Type.STRING_SELECTION, event.getUser(), event.getMember(), event.getChannel().getId(), event,
                e -> e.getWorker().processStringSelectEvent(event.getMember(), event.getChannel()
                        , event.getInteraction().getComponentId(), event));
    }

    @Override
    public void onEntitySelectionMenuEvent(final String identifier, final EntitySelectInteractionEvent event) {
        handle(identifier, Type.ENTITY_SELECTION, event.getUser(), event.getMember(), event.getChannel().getId(), event,
                e -> e.getWorker().processEntitySelectEvent(event.getMember(), event.getChannel()
                        , event.getInteraction().getComponentId(), event));
    }

    @Override
    public void onGuildUserContextEvent(final String identifier, final UserContextInteractionEvent event) {
        handle(identifier, Type.GUILD_USER_CONTEXT, event.getUser(), event.getMember(), event.getChannel().getId(), event,
                e -> e.getWorker().processGuildUserContextEvent(event.getMember(), event.getTargetMember(), event.getName(), event));
    }

    @Override
    public void onGlobalUserContextEvent(final String identifier, final UserContextInteractionEvent event) {
        handle(identifier, Type.GLOBAL_USER_CONTEXT, event.getUser(), null, event.getChannel().getId(), event,
                e -> e.getWorker().processGlobalUserContextEvent(event.getUser(), event.getTarget(), event.getName(), event));
    }

    @Override
    public void onGuildMessageContextEvent(final String identifier, final MessageContextInteractionEvent event) {
        handle(identifier, Type.GUILD_MSG_CONTEXT, event.getUser(), event.getMember(), event.getChannel().getId(), event,
                e -> e.getWorker().processGuildMessageContextEvent(event.getMember(), event.getChannel(), event.getName(), event));
    }

    @Override
    public void onGlobalMessageContextEvent(final String identifier, final MessageContextInteractionEvent event) {
        handle(identifier, Type.GLOBAL_MSG_CONTEXT, event.getUser(), null, event.getChannel().getId(), event,
                e -> e.getWorker().processGlobalMessageContextEvent(event.getUser(), event.getChannel(), event.getName(), event));
    }

    /**
     * Handles the auto complete event. Only the registration and the channel restriction are checked
     * (no bot check, permissions, cooldowns or error embeds), because discord fires it on every keystroke
     * and only accepts choices as reply. A rejected call gets an empty choice list
     * @param identifier the name of the command
     * @param event the JDA event
     */
    @Override
    public void onAutoCompleteEvent(final String identifier, final CommandAutoCompleteInteractionEvent event) {
        execute(Type.AUTO_COMPLETE, identifier, () -> {
            final Event e = this.events.get(Type.AUTO_COMPLETE).get(identifier);
            if (e == null || !e.isAllowedChannel(event.getChannel().getId())) {
                event.replyChoices().queue();
                return;
            }
            e.getWorker().processAutoCompleteEvent(event.getUser(), event.getChannel(), event.getFocusedOption().getName(), event);
        });
    }

    /**
     * Runs the validation of a call without a guild member (bot check, registration, no required permissions,
     * channel restriction, cooldowns). A successful call starts the cooldowns, like a real event would
     * @param identifier the unique {@link String} for the event
     * @param type the Type of the event
     * @param user the discord user who triggered the event
     * @param channelId the ID of the channel which should be checked
     * @return the {@link ValidationEvent}, contains the {@link Event} if it's a valid request
     */
    @Override
    public ValidationEvent getEventWorker(final String identifier, final Type type, final User user, final String channelId) {
        return isValid(identifier, type, user, channelId);
    }

    /**
     * Checks if a call without a guild member is valid, in this order:<br>
     * <ul>
     *     <li>Not from a bot (if bot requests are ignored)</li>
     *     <li>Existing Event</li>
     *     <li>No required permissions, they can't be checked without a member</li>
     *     <li>Allowed Channel</li>
     *     <li>Isn't on cooldown (starts the cooldowns otherwise)</li>
     * </ul>
     * @param identifier the unique {@link String} for the event
     * @param type the Type of the event
     * @param user the discord user who triggered the event
     * @param channelID the ID of the channel which should be checked
     * @return the {@link ValidationEvent}, contains the {@link Event} if it's a valid request
     */
    private ValidationEvent isValid(final String identifier, final Type type, final User user, final String channelID) {
        if (this.ignoreBotRequest && user.isBot()) {
            return ValidationEvent.fail(Validation.IS_BOT);
        }

        final Event e = this.events.get(type).get(identifier);
        if (e == null) {
            return ValidationEvent.fail(Validation.NO_EVENT);
        }

        // Outside of guilds there are no permissions to check
        if (!e.getRequiredPermissions().isEmpty()) {
            return ValidationEvent.fail(Validation.NO_PERMISSION);
        }

        if (!e.isAllowedChannel(channelID)) {
            return ValidationEvent.fail(Validation.WRONG_CHANNEL);
        }

        if (onCooldown(type + identifier, user.getId(), e)) {
            return ValidationEvent.fail(Validation.ON_COOLDOWN);
        }
        return ValidationEvent.success(e);
    }

    /**
     * Checks if a call of a guild member is valid, in this order:<br>
     * <ul>
     *     <li>Not from a bot (if bot requests are ignored)</li>
     *     <li>Existing Event</li>
     *     <li>Required permissions</li>
     *     <li>Admin bypass (succeeds without the cooldown check) or allowed channel, both skipped
     *     for {@link de.shurablack.core.event.annotation.BypassGuildValidity}</li>
     *     <li>Isn't on cooldown (starts the cooldowns otherwise)</li>
     * </ul>
     * @param identifier the unique {@link String} for the event
     * @param type the Type of the event
     * @param member the guild member who triggered the event
     * @param channelID the ID of the channel which should be checked
     * @return the {@link ValidationEvent}, contains the {@link Event} if it's a valid request
     */
    private ValidationEvent isValidGuild(final String identifier, final Type type, final Member member, final String channelID) {
        if (this.ignoreBotRequest && member.getUser().isBot()) {
            return ValidationEvent.fail(Validation.IS_BOT);
        }

        final Event e = this.events.get(type).get(identifier);
        if (e == null) {
            return ValidationEvent.fail(Validation.NO_EVENT);
        }

        // Not affected by the bypasses, administrators have every permission anyway
        if (!hasPermissions(member, channelID, e.getRequiredPermissions())) {
            return ValidationEvent.fail(Validation.NO_PERMISSION);
        }

        if (!e.isBypassGuildValidity()) {
            if (adminBypass && member.hasPermission(Permission.ADMINISTRATOR)) {
                return ValidationEvent.success(e);
            }

            if (!e.isAllowedChannel(channelID)) {
                return ValidationEvent.fail(Validation.WRONG_CHANNEL);
            }
        }

        if (onCooldown(type + identifier, member.getId(), e)) {
            return ValidationEvent.fail(Validation.ON_COOLDOWN);
        }
        return ValidationEvent.success(e);
    }

    /**
     * Checks the permissions in the channel of the event (including channel overrides),
     * falls back to the guild permissions if the channel isn't found in the guild of the member
     * @return true if the member has all permissions (always true for an empty set)
     */
    private static boolean hasPermissions(final Member member, final String channelID, final Set<Permission> permissions) {
        if (permissions.isEmpty()) {
            return true;
        }
        final GuildChannel channel = member.getGuild().getGuildChannelById(channelID);
        return channel != null ? member.hasPermission(channel, permissions) : member.hasPermission(permissions);
    }
}
