package de.shurablack.core.event.handler;

import de.shurablack.core.event.interaction.InteractionSet;
import de.shurablack.core.event.interaction.Type;
import de.shurablack.core.event.validation.ValidationEvent;
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

import java.util.List;

public abstract class EventHandler {

    /** The default prefix for commands */
    public static final String DEFAULT_PREFIX = "!";

    /** The prefix for commands */
    private static String PREFIX = DEFAULT_PREFIX;

    /**
     * @return the set Prefix
     */
    public static String getPREFIX() {
        return PREFIX;
    }

    /**
     * Sets the prefix for commands.
     *
     * @param prefix the new prefix to set
     */
    public static void setPREFIX(final String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            PREFIX = DEFAULT_PREFIX;
        } else {
            PREFIX = prefix;
        }
    }

    public abstract EventHandler registerEvent(final InteractionSet... set);

    public abstract EventHandler registerEvent(final List<InteractionSet> set);

    public abstract void onButtonEvent(final String identifier, final ButtonInteractionEvent event);

    public abstract void onGlobalSlashEvent(final String identifier, final SlashCommandInteractionEvent event);

    public abstract void onGuildSlashEvent(final String identifier, final SlashCommandInteractionEvent event);

    public abstract void onModalEvent(final String identifier, final ModalInteractionEvent event);

    public abstract void onPrivateChannelEvent(final String identifier, final MessageReceivedEvent event);

    public abstract void onPublicChannelEvent(final String identifier, final MessageReceivedEvent event);

    public abstract void onPrivateReactionEvent(final String identifier, final MessageReactionAddEvent event);

    public abstract void onPublicReactionEvent(final String identifier, final MessageReactionAddEvent event);

    public abstract void onStringSelectionMenuEvent(final String identifier, final StringSelectInteractionEvent event);

    public abstract void onEntitySelectionMenuEvent(final String identifier, EntitySelectInteractionEvent event);

    public abstract void onGuildUserContextEvent(final String identifier, final UserContextInteractionEvent event);

    public abstract void onGlobalUserContextEvent(final String identifier, final UserContextInteractionEvent event);

    public abstract void onGuildMessageContextEvent(final String identifier, final MessageContextInteractionEvent event);

    public abstract void onGlobalMessageContextEvent(final String identifier, final MessageContextInteractionEvent event);

    public abstract ValidationEvent getEventWorker(final String identifier, final Type type, final User user, final String channelId);

}
