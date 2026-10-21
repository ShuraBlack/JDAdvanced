package de.shurablack.core.event.handler;

import de.shurablack.core.event.interaction.InteractionSet;
import de.shurablack.core.event.interaction.Type;
import de.shurablack.core.event.validation.ValidationEvent;
import net.dv8tion.jda.api.entities.User;
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

import java.util.List;

/**
 * <p>
 * Contract between the JDA listeners (e.g. {@link de.shurablack.listener.DefaultSlashReceiver}) and the event handling.
 * A listener extracts the identifier of a JDA event and calls the matching <b>on*Event</b> method.
 * <br><br>
 * The identifier is looked up among the {@link InteractionSet registered interactions} of the same {@link Type}.
 * {@link AbstractEventHandler} implements the validation and routing, {@link NativeEventHandler} and
 * {@link DispatchEventHandler} decide on which thread the worker runs.
 * </p>
 *
 * @see AbstractEventHandler
 * @author ShuraBlack
 */
public abstract class EventHandler {

    /** The default prefix for message commands */
    public static final String DEFAULT_PREFIX = "!";

    /** The prefix for message commands, shared by all handlers */
    private static String PREFIX = DEFAULT_PREFIX;

    /**
     * @return the current prefix for message commands
     */
    public static String getPREFIX() {
        return PREFIX;
    }

    /**
     * Sets the prefix for message commands ({@link Type#PRIVATE_CHANNEL} / {@link Type#PUBLIC_CHANNEL}).
     * <br>
     * The prefix is static and gets added to the identifier when an {@link de.shurablack.core.event.interaction.Interaction Interaction}
     * is created, so it only affects interactions created afterwards (e.g. by {@link InteractionSet#fromAnnotation()}).
     *
     * @param prefix the new prefix, null or empty resets it to {@link #DEFAULT_PREFIX}
     */
    public static void setPREFIX(final String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            PREFIX = DEFAULT_PREFIX;
        } else {
            PREFIX = prefix;
        }
    }

    /**
     * Registers the interactions of the given sets, an already registered identifier of the same type gets replaced.
     * @param set the interaction sets, usually from {@link InteractionSet#fromAnnotation()}
     * @return the handler for chaining
     */
    public abstract EventHandler registerEvent(final List<InteractionSet> set);

    /**
     * Handles a button click.
     * @param identifier the custom ID of the button
     * @param event the JDA event
     */
    public abstract void onButtonEvent(final String identifier, final ButtonInteractionEvent event);

    /**
     * Handles a slash command used outside of a guild.
     * @param identifier the name of the command
     * @param event the JDA event
     */
    public abstract void onGlobalSlashEvent(final String identifier, final SlashCommandInteractionEvent event);

    /**
     * Handles a slash command used in a guild.
     * @param identifier the name of the command
     * @param event the JDA event
     */
    public abstract void onGuildSlashEvent(final String identifier, final SlashCommandInteractionEvent event);

    /**
     * Handles a modal submission.
     * @param identifier the ID of the modal
     * @param event the JDA event
     */
    public abstract void onModalEvent(final String identifier, final ModalInteractionEvent event);

    /**
     * Handles a message received in a private channel.
     * @param identifier the first word of the message (prefix and command)
     * @param event the JDA event
     */
    public abstract void onPrivateChannelEvent(final String identifier, final MessageReceivedEvent event);

    /**
     * Handles a message received in a guild channel.
     * @param identifier the first word of the message (prefix and command)
     * @param event the JDA event
     */
    public abstract void onPublicChannelEvent(final String identifier, final MessageReceivedEvent event);

    /**
     * Handles a reaction added in a private channel.
     * @param identifier the identifier the listener mapped to the reacted message
     * @param event the JDA event
     */
    public abstract void onPrivateReactionEvent(final String identifier, final MessageReactionAddEvent event);

    /**
     * Handles a reaction added in a guild channel.
     * @param identifier the identifier the listener mapped to the reacted message
     * @param event the JDA event
     */
    public abstract void onPublicReactionEvent(final String identifier, final MessageReactionAddEvent event);

    /**
     * Handles a string select menu interaction.
     * @param identifier the custom ID of the menu
     * @param event the JDA event
     */
    public abstract void onStringSelectionMenuEvent(final String identifier, final StringSelectInteractionEvent event);

    /**
     * Handles an entity select menu interaction.
     * @param identifier the custom ID of the menu
     * @param event the JDA event
     */
    public abstract void onEntitySelectionMenuEvent(final String identifier, EntitySelectInteractionEvent event);

    /**
     * Handles a user context menu command used in a guild.
     * @param identifier the name of the command
     * @param event the JDA event
     */
    public abstract void onGuildUserContextEvent(final String identifier, final UserContextInteractionEvent event);

    /**
     * Handles a user context menu command used outside of a guild.
     * @param identifier the name of the command
     * @param event the JDA event
     */
    public abstract void onGlobalUserContextEvent(final String identifier, final UserContextInteractionEvent event);

    /**
     * Handles a message context menu command used in a guild.
     * @param identifier the name of the command
     * @param event the JDA event
     */
    public abstract void onGuildMessageContextEvent(final String identifier, final MessageContextInteractionEvent event);

    /**
     * Handles a message context menu command used outside of a guild.
     * @param identifier the name of the command
     * @param event the JDA event
     */
    public abstract void onGlobalMessageContextEvent(final String identifier, final MessageContextInteractionEvent event);

    /**
     * Handles the auto complete event of a slash command option.
     * The default implementation ignores it.
     * @param identifier the name of the command
     * @param event the JDA event
     */
    public void onAutoCompleteEvent(final String identifier, final CommandAutoCompleteInteractionEvent event) { }

    /**
     * Validates a call without a guild context and returns the registered event if it passes.
     * @param identifier the identifier of the event
     * @param type the type of the event
     * @param user the user who triggered the event
     * @param channelId the ID of the channel the event happened in
     * @return the result of the validation, contains the event on success
     */
    public abstract ValidationEvent getEventWorker(final String identifier, final Type type, final User user, final String channelId);

}
