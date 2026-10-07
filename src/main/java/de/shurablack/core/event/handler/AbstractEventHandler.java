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
 * validation (bots, channel restriction, cooldowns, admin bypass) and the routing to the {@link de.shurablack.core.event.EventWorker}.
 * <br><br>
 * Subclasses only decide where the validated worker call runs, see {@link #execute(Type, String, Runnable)}.
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

    /** Maps a cooldown key (global: type+identifier, user: userId:type+identifier) to the time it expires */
    private final Map<String, Long> cooldowns = new HashMap<>();

    protected AbstractEventHandler() {
        for (Type t : Type.values()) {
            events.put(t, new ConcurrentHashMap<>());
        }
    }

    /**
     * Runs the validated worker call
     * @param type the interaction type
     * @param identifier the unique string of the event
     * @param task validates the event and calls the worker
     */
    protected abstract void execute(Type type, String identifier, Runnable task);

    /**
     * Applies the settings of the create factory methods
     */
    void configure(final String prefix, final boolean ignoreBotRequest, final boolean errorFeedback, final boolean adminBypass) {
        EventHandler.setPREFIX(prefix);
        this.ignoreBotRequest = ignoreBotRequest;
        this.errorCallback = errorFeedback;
        this.adminBypass = adminBypass;
    }

    /**
     * Registers a set of events with the EventHandler object
     * @param set of events which will be created
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
     * Registers a single set of events
     * @param os the events which will be created
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
     * Checks both cooldowns before starting them, so a blocked call doesnt consume the other cooldown
     * @param key the type and identifier of the event
     * @param userID the unique ID of an discord user
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
     * Validates the call and runs the worker, otherwise sends the error feedback (if there is a callback)
     * @param identifier is the unique {@link String} for the event
     * @param type is the Type of the event
     * @param user the discord user who triggered the event
     * @param member the guild member who triggered the event, null outside of guilds
     * @param channelID is the ID of the channel
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
     * Prefers a {@link de.shurablack.core.event.annotation.command.CommandHandler} registration ("music play")
     * over the registration of the whole command ("music")
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
        // member is null for webhook messages
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
     * Handles the auto complete event. There are no cooldowns or error embeds, because discord fires it
     * on every keystroke and only accepts choices as reply
     * @param identifier is the name of the command
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
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
     * Gets the event worker for the given identifier, type, user and channel ID
     * @param identifier is the unique {@link String} for the event
     * @param type is the Type of the event
     * @param user is a independent discord user object
     * @param channelId is the ID of the channel which should be checked
     * @return the {@link ValidationEvent} contains the {@link Event} if its a valid request
     */
    @Override
    public ValidationEvent getEventWorker(final String identifier, final Type type, final User user, final String channelId) {
        return isValid(identifier, type, user, channelId);
    }

    /**
     * Checks if the call is valid or not.<br>
     * <ul>
     *     <li>Existing Event</li>
     *     <li>Allowed Channel</li>
     *     <li>Isnt on cooldown</li>
     * </ul>
     * @param identifier is the unique {@link String} for the event
     * @param type is the Type of the event
     * @param user is a independent discord user object
     * @param channelID is the ID of the channel which should be checked
     * @return the {@link ValidationEvent} contains the {@link Event} if its a valid request
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
     * Checks if the call is valid or not.<br>
     * <ul>
     *     <li>Existing Event</li>
     *     <li>Required permissions</li>
     *     <li>Admin bypass or allowed channel (skipped for {@link de.shurablack.core.event.annotation.BypassGuildValidity})</li>
     *     <li>Isnt on cooldown</li>
     * </ul>
     * @param identifier is the unique {@link String} for the event
     * @param type is the Type of the event
     * @param member is a guild discord user object
     * @param channelID is the ID of the channel which should be checked
     * @return the {@link ValidationEvent} contains the {@link Event} if its a valid request
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
     * falls back to the guild permissions if the channel isnt a guild channel
     */
    private static boolean hasPermissions(final Member member, final String channelID, final Set<Permission> permissions) {
        if (permissions.isEmpty()) {
            return true;
        }
        final GuildChannel channel = member.getGuild().getGuildChannelById(channelID);
        return channel != null ? member.hasPermission(channel, permissions) : member.hasPermission(permissions);
    }
}
