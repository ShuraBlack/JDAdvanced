package de.shurablack.core.event;

import de.shurablack.core.event.interaction.Interaction;
import de.shurablack.core.event.interaction.InteractionSet;
import de.shurablack.core.event.interaction.Type;
import de.shurablack.core.event.validation.Validation;
import de.shurablack.core.event.validation.ValidationEvent;
import de.shurablack.core.scheduling.Dispatcher;
import de.shurablack.listener.DefaultInteractionReceiver;
import de.shurablack.listener.DefaultMessageReceiver;
import de.shurablack.listener.DefaultReactionReceiver;
import de.shurablack.listener.DefaultSlashReceiver;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>
 * The EventHandler class is a utility class used to handle events that occur in a Discord bot
 * <br><br>
 * To bind the {@link net.dv8tion.jda.api.hooks.ListenerAdapter ListenerAdapter} event use the correct
 * <b>call</b> function within the Listener
 * </p>
 *
 * @see DefaultInteractionReceiver DefaultInteractionReceiver
 * @see DefaultMessageReceiver DefaultMessageReceiver
 * @see DefaultReactionReceiver DefaultReactionReceiver
 * @see DefaultSlashReceiver DefaultSlashReceiver
 *
 * @version core-1.0.0
 * @date 15.06.2023
 * @author ShuraBlack
 */
public class EventHandler {

    /** Class Logger */
    private static final Logger LOGGER = LogManager.getLogger(EventHandler.class);

    /** The default prefix for commands */
    public static final String DEFAULT_PREFIX = "!";

    /** The prefix for commands */
    private static String PREFIX = DEFAULT_PREFIX;

    /** Indicating whether requests from other bots should be ignored */
    private boolean ignoreBotRequest = true;

    /** Indicating whether error feedback should be sent */
    private boolean errorCallback = true;

    /** Indicating whether the admin bypass is enabled */
    private boolean adminBypass = false;

    /**
     * A concurrent hash map that maps event types to maps of event identifier strings to Event objects
     */
    private final ConcurrentHashMap<Type, Map<String, Event>> events = new ConcurrentHashMap<>();

    /** A map of event identifier strings to the last time the event was called */
    private final Map<String, Long> lastCallGlobal = new HashMap<>();

    /** A map of user ID strings to maps of event identifier
     * strings to the last time the event was called by that user
     */
    private final Map<String, Map<String, Long>> lastCallUser = new HashMap<>();

    /**
     * Creates a new EventHandler object
     * @return the handler for chaining
     */
    public static EventHandler createDefault() {
        final EventHandler handler = new EventHandler();
        for (Type t : Type.values()) {
            handler.events.put(t, new ConcurrentHashMap<>());
        }
        return handler;
    }

    /**
     * Creates a new EventHandler object
     * @param prefix the prefix for regular messages
     * @param ignoreBotRequest the ignore flag
     * @return the handler for chaining
     */
    public static EventHandler create(final String prefix, final boolean ignoreBotRequest, final boolean errorFeedback, final boolean adminBypass) {
        final EventHandler handler = createDefault();
        PREFIX = prefix;
        handler.ignoreBotRequest = ignoreBotRequest;
        handler.errorCallback = errorFeedback;
        handler.adminBypass = adminBypass;
        return handler;
    }

    /**
     * Registers a set of events with the EventHandler object
     * @param set of events which will be created
     * @return the handler for chaining
     */
    public EventHandler registerEvent(final InteractionSet... set) {
        for (InteractionSet os : set) {
            registerEventSubRoutine(os);
        }
        return this;
    }

    /**
     * Registers a set of events with the EventHandler object
     * @param set of events which will be created
     * @return the handler for chaining
     */
    public EventHandler registerEvent(final List<InteractionSet> set) {
        for (InteractionSet os : set) {
            registerEventSubRoutine(os);
        }
        return this;
    }

    /**
     * Registers a set of events with the EventHandler object.
     * <br><br>
     * This method is used to register a single event.
     * @param os the event which will be created
     */
    private void registerEventSubRoutine(InteractionSet os) {
        for (Interaction o : os.getInteractions()) {
            final Event event = new Event(
                    os.getWorker(),
                    o.getGlobalCooldown(),
                    o.getUserCooldown(),
                    o.getChannelRestriction()
            );
            this.events.get(o.getType()).put(o.getIdentifier(),event);

        }
        LOGGER.info(String.format("Register - Type/s: %s, Identifier/s: %s"
                , os.getInteractions().stream().map(interaction -> "\u001B[33m" + interaction.getType().name()
                        + "\u001B[0m").collect(Collectors.joining(", "))
                , os.getInteractions().stream().map(interaction -> "\u001B[33m" + interaction.getIdentifier()
                        + "\u001B[0m").collect(Collectors.joining(", "))));
    }

    /**
     * Checks whether the event is on cooldown for the given user
     * @param identifier the unique string of the event
     * @param userID the unique ID of an discord user
     * @param event the event object
     * @return true if there is a global or user cooldown still active
     */
    private boolean onCooldown(final String identifier, final String userID, final Event event) {
        long time = System.currentTimeMillis();
        if (event.getGlobalCooldown() != Interaction.NO_COOLDOWN) {
            if (this.lastCallGlobal.containsKey(identifier)) {
                long diff = time - this.lastCallGlobal.get(identifier);
                if (diff > event.getGlobalCooldown()) {
                    this.lastCallGlobal.replace(identifier, time);
                    return false;
                } else {
                    return true;
                }
            } else {
                this.lastCallGlobal.put(identifier,time);
            }
        }

        if (this.lastCallUser.containsKey(userID)) {
            Map<String, Long> data = this.lastCallUser.get(userID);
            if (data.containsKey(identifier)) {
                long diff = time - data.get(identifier);
                if (diff > event.getUserCooldown()) {
                    data.replace(identifier, time);
                    return false;
                } else {
                    return true;
                }
            } else {
                data.put(identifier, time);
                return false;
            }
        } else {
            Map<String,Long> data = new HashMap<>();
            data.put(identifier, time);
            this.lastCallUser.put(userID,data);
            return false;
        }
    }

    /**
     * Handles the button event
     * @param identifier is the unique {@link String} for the event
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void onButtonEvent(final String identifier, final ButtonInteractionEvent event) {
        Dispatcher.dispatch(() -> {
            final ValidationEvent e = isValidGuild(identifier, Type.BUTTON, event.getMember(), event.getChannel().getId());

            if (e.isSuccess()) {
                e.getEvent().getWorker().processButtonEvent(event.getMember(),event.getChannel(), event.getButton().getId(), event);
                return;
            }

            sendErrorCallback(e, event);
        });
    }

    /**
     * Handles the global slash event
     * @param identifier is the unique {@link String} for the event
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void onGlobalSlashEvent(final String identifier, final SlashCommandInteractionEvent event) {
        Dispatcher.dispatch(() -> {
            final ValidationEvent e = isValid(identifier, Type.GLOBAL_SLASH, event.getUser(), event.getChannel().getId());

            if (e.isSuccess()) {
                e.getEvent().getWorker().processGlobalSlashEvent(event.getUser(),event.getChannel().asPrivateChannel(), event.getName(), event);
                return;
            }

            sendErrorCallback(e, event);
        });
    }

    /**
     * Handles the guild slash event
     * @param identifier is the unique {@link String} for the event
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void onGuildSlashEvent(final String identifier, final SlashCommandInteractionEvent event) {
        Dispatcher.dispatch(() -> {
            final ValidationEvent e = isValidGuild(identifier, Type.GUILD_SLASH, event.getMember(), event.getChannel().getId());

            if (e.isSuccess()) {
                e.getEvent().getWorker().processGuildSlashEvent(event.getMember(),event.getChannel(), event.getSubcommandName(), event);
                return;
            }

            sendErrorCallback(e, event);
        });
    }

    /**
     * Handles the modal event
     * @param identifier is the unique {@link String} for the event
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void onModalEvent(final String identifier, final ModalInteractionEvent event) {
        Dispatcher.dispatch(() -> {
            final ValidationEvent e = isValidGuild(identifier, Type.MODAL, event.getMember(), event.getChannel().getId());

            if (e.isSuccess()) {
                e.getEvent().getWorker().processModalEvent(event.getMember(),event.getChannel(), event.getModalId(), event);
                return;
            }

            sendErrorCallback(e, event);
        });
    }

    /**
     * Handles the private message received event
     * @param identifier is the unique {@link String} for the event
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void onPrivateChannelEvent(final String identifier, final MessageReceivedEvent event) {
        Dispatcher.dispatch(() -> {
            final ValidationEvent e = isValid(identifier, Type.PRIVATE_CHANNEL, event.getAuthor(), event.getChannel().getId());

            if (e.isSuccess()) {
                e.getEvent().getWorker().processPrivateChannelEvent(event.getAuthor(),event.getChannel().asPrivateChannel()
                        , event.getMessage().getContentRaw().replace(identifier,""), event);
            }
        });
    }

    /**
     * Handles the guild message received event
     * @param identifier is the unique {@link String} for the event
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void onPublicChannelEvent(final String identifier, final MessageReceivedEvent event) {
        Dispatcher.dispatch(() -> {
            final ValidationEvent e = isValidGuild(identifier, Type.PUBLIC_CHANNEL, event.getMember(), event.getChannel().getId());

            if (e.isSuccess()) {
                e.getEvent().getWorker().processPublicChannelEvent(event.getMember(),event.getChannel()
                        , event.getMessage().getContentRaw().replace(identifier,""), event);
            }
        });
    }

    /**
     * Handles the private reaction add event
     * @param identifier is the unique {@link String} for the event
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void onPrivateReactionEvent(final String identifier, final MessageReactionAddEvent event) {
        Dispatcher.dispatch(() -> {
            final ValidationEvent e = isValid(identifier, Type.PRIVATE_REACTION, event.getUser(), event.getChannel().getId());

            if (e.isSuccess()) {
                e.getEvent().getWorker().processPrivateReactionEvent(event.getUser(),event.getChannel().asPrivateChannel()
                        , event.getEmoji().getName(), event);
            }
        });
    }

    /**
     * Handles the guild reaction add event
     * @param identifier is the unique {@link String} for the event
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void onPublicReactionEvent(final String identifier, final MessageReactionAddEvent event) {
        Dispatcher.dispatch(() -> {
            final ValidationEvent e = isValidGuild(identifier, Type.PUBLIC_REACTION, event.getMember(), event.getChannel().getId());

            if (e.isSuccess()) {
                e.getEvent().getWorker().processPublicReactionEvent(event.getMember(),event.getChannel()
                        , event.getEmoji().getName(), event);
            }
        });
    }

    /**
     * Handles the string selection menu event
     * @param identifier is the unique {@link String} for the event
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void onStringSelectionMenuEvent(final String identifier, final StringSelectInteractionEvent event) {
        Dispatcher.dispatch(() -> {
            final ValidationEvent e = isValid(identifier, Type.STRING_SELECTION, event.getUser(), event.getChannel().getId());

            if (e.isSuccess()) {
                e.getEvent().getWorker().processStringSelectEvent(event.getMember(),event.getChannel()
                        , event.getInteraction().getComponentId(), event);
                return;
            }

            sendErrorCallback(e, event);
        });
    }

    /**
     * Handles the entity selection menu event
     * @param identifier is the unique {@link String} for the event
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void onEntitySelectionMenuEvent(final String identifier, EntitySelectInteractionEvent event) {
        Dispatcher.dispatch(() -> {
            final ValidationEvent e = isValid(identifier, Type.STRING_SELECTION, event.getUser(), event.getChannel().getId());

            if (e.isSuccess()) {
                e.getEvent().getWorker().processEntitySelectEvent(event.getMember(),event.getChannel()
                        , event.getInteraction().getComponentId(), event);
                return;
            }

            sendErrorCallback(e, event);
        });
    }

    /**
     * Handles the guild user context event
     * @param identifier is the unique {@link String} for the event
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void onGuildUserContextEvent(final String identifier, final UserContextInteractionEvent event) {
        Dispatcher.dispatch(() -> {
            final ValidationEvent e = isValidGuild(identifier, Type.GUILD_USER_CONTEXT, event.getMember(), event.getChannel().getId());

            if (e.isSuccess()) {
                e.getEvent().getWorker().processGuildUserContextEvent(event.getMember()
                        ,event.getTargetMember(),event.getName(),event);
                return;
            }

            sendErrorCallback(e, event);
        });
    }

    /**
     * Handles the user context event
     * @param identifier is the unique {@link String} for the event
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void onGlobalUserContextEvent(final String identifier, final UserContextInteractionEvent event) {
        Dispatcher.dispatch(() -> {
            final ValidationEvent e = isValid(identifier, Type.GLOBAL_USER_CONTEXT, event.getUser(), event.getChannel().getId());

            if (e.isSuccess()) {
                e.getEvent().getWorker().processGlobalUserContextEvent(event.getUser()
                        ,event.getTarget(),event.getName(),event);
                return;
            }

            sendErrorCallback(e, event);
        });
    }

    /**
     * Handles the guild message context event
     * @param identifier is the unique {@link String} for the event
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void onGuildMessageContextEvent(final String identifier, final MessageContextInteractionEvent event) {
        Dispatcher.dispatch(() -> {
            final ValidationEvent e = isValidGuild(identifier, Type.GUILD_MSG_CONTEXT, event.getMember(), event.getChannel().getId());

            if (e.isSuccess()) {
                e.getEvent().getWorker().processGuildMessageContextEvent(event.getMember(), event.getChannel()
                        , event.getName(), event);
                return;
            }

            sendErrorCallback(e, event);
        });
    }

    /**
     * Handles the message context event
     * @param identifier is the unique {@link String} for the event
     * @param event is the original {@link net.dv8tion.jda.api.interactions.Interaction Interaction}
     *              of the {@link net.dv8tion.jda.api.JDA JDA}
     */
    public void onGlobalMessageContextEvent(final String identifier, final MessageContextInteractionEvent event) {
        Dispatcher.dispatch(() -> {
            final ValidationEvent e = isValid(identifier, Type.GLOBAL_MSG_CONTEXT, event.getUser(), event.getChannel().getId());
            if (e.isSuccess()) {
                e.getEvent().getWorker().processGlobalMessageContextEvent(event.getUser(), event.getChannel()
                        , event.getName(), event);
            }
        });
    }

    /**
     * Sends an error callback to the user
     * @param event the validation event
     * @param callback the reply callback
     */
    private void sendErrorCallback(ValidationEvent event, IReplyCallback callback) {
        if (!errorCallback) {
            return;
        }

        callback.replyEmbeds(event.getValidation().getEmbed()).setEphemeral(true).queue();
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
     *     <li>Allowed Channel</li>
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

        if (adminBypass && member.hasPermission(Permission.ADMINISTRATOR)) {
            return ValidationEvent.success(e);
        }

        if (!e.isAllowedChannel(channelID)) {
            return ValidationEvent.fail(Validation.WRONG_CHANNEL);
        }

        if (onCooldown(type + identifier, member.getId(), e)) {
            return ValidationEvent.fail(Validation.ON_COOLDOWN);
        }
        return ValidationEvent.success(e);
    }

    /**
     * @return the map of last global calls
     */
    public Map<String, Long> getLastCallGlobal() {
        return lastCallGlobal;
    }

    /**
     * @return the map of last user calls
     */
    public Map<String, Map<String, Long>> getLastCallUser() {
        return lastCallUser;
    }

    /**
     * @return the set Prefix
     */
    public static String getPREFIX() {
        return PREFIX;
    }

    /**
     * Clears the cooldown of the last call maps
     */
    public void clearCooldowns() {
        lastCallGlobal.clear();
        lastCallUser.clear();
        LOGGER.info("Cleaned up cooldown´s");
    }
}
