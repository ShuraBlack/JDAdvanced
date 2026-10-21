package de.shurablack.listener;

import de.shurablack.core.event.handler.EventHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Default listener that forwards slash commands, context menu commands and auto-complete requests
 * to an {@link EventHandler}.
 * <br><br>
 * The command name is used as identifier. Slash and context menu commands are routed to the guild or global
 * handler method depending on whether the interaction happened in a guild. Auto-complete requests are
 * forwarded regardless of origin.
 * <br><br>
 * Can be used as is or serve as an example for a custom {@link ListenerAdapter}.
 * </p>
 *
 * @version core-1.0.0
 * @date 12.07.2022
 * @author ShuraBlack
 */
public class DefaultSlashReceiver extends ListenerAdapter {

    /** The {@link EventHandler} that receives all forwarded events */
    private final EventHandler handler;

    /**
     * Creates a new receiver.
     * @param handler the {@link EventHandler} to forward the events to
     */
    public DefaultSlashReceiver(EventHandler handler) {
        this.handler = handler;
    }

    /** Forwards the slash command to {@link EventHandler#onGuildSlashEvent} or {@link EventHandler#onGlobalSlashEvent}, using the command name as identifier. */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.isFromGuild()) {
            handler.onGuildSlashEvent(event.getName(),event);
            return;
        }

        handler.onGlobalSlashEvent(event.getName(), event);
    }

    /** Forwards the user context command to {@link EventHandler#onGuildUserContextEvent} or {@link EventHandler#onGlobalUserContextEvent}, using the command name as identifier. */
    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        if (event.isFromGuild()) {
            handler.onGuildUserContextEvent(event.getName(), event);
            return;
        }

        handler.onGlobalUserContextEvent(event.getName(), event);
    }

    /** Forwards the message context command to {@link EventHandler#onGuildMessageContextEvent} or {@link EventHandler#onGlobalMessageContextEvent}, using the command name as identifier. */
    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        if (event.isFromGuild()) {
            handler.onGuildMessageContextEvent(event.getName(), event);
            return;
        }

        handler.onGlobalMessageContextEvent(event.getName(), event);
    }

    /** Forwards the auto-complete request to {@link EventHandler#onAutoCompleteEvent}, using the command name as identifier. */
    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        handler.onAutoCompleteEvent(event.getName(), event);
    }
}
