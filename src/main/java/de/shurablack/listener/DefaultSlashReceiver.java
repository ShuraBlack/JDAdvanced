package de.shurablack.listener;

import de.shurablack.core.event.EventHandler;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * This is a default/example class
 * <br><br>
 * The DefaultSlashReceiver class is a listener that listens for various types of {@link SlashCommandInteractionEvent}
 * in a Discord chat server.
 * <br><br>
 * It extends the ListenerAdapter class from the JDA library, which provides a simple way to create listeners for Discord events.
 * </p>
 *
 * @version core-1.0.0
 * @date 12.07.2022
 * @author ShuraBlack
 */
public class DefaultSlashReceiver extends ListenerAdapter {

    /** The corresponding {@link EventHandler} */
    private final EventHandler handler;

    /**
     * The constructor for the class
     * @param handler the specified {@link EventHandler}
     */
    public DefaultSlashReceiver(EventHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.isFromGuild()) {
            handler.onGuildSlashEvent(event.getName(),event);
            return;
        }

        handler.onGlobalSlashEvent(event.getName(), event);
    }

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        if (event.isFromGuild()) {
            handler.onGuildUserContextEvent(event.getName(), event);
            return;
        }

        handler.onGlobalUserContextEvent(event.getName(), event);
    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        if (event.isFromGuild()) {
            handler.onGuildMessageContextEvent(event.getName(), event);
        }

        handler.onGlobalMessageContextEvent(event.getName(), event);
    }

    @Override
    public void onEntitySelectInteraction(EntitySelectInteractionEvent event) {
        handler.onEntitySelectionMenuEvent(event.getId(), event);
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        handler.onStringSelectionMenuEvent(event.getId(), event);
    }
}
