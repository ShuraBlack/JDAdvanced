package de.shurablack.listener;

import de.shurablack.core.event.handler.EventHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Default listener that forwards every {@link MessageReceivedEvent} to an {@link EventHandler}.
 * <br><br>
 * The identifier is the first space-separated token of the raw message content (e.g. {@code "!help"} for
 * {@code "!help me"}). Guild messages go to {@link EventHandler#onPublicChannelEvent}, direct messages to
 * {@link EventHandler#onPrivateChannelEvent}. Messages from bots, including this one, are forwarded as well.
 * Reading message content requires the {@code MESSAGE_CONTENT} gateway intent.
 * <br><br>
 * Can be used as is or serve as an example for a custom {@link ListenerAdapter}.
 * </p>
 *
 * @version core-1.0.0
 * @date 12.07.2022
 * @author ShuraBlack
 */
public class DefaultMessageReceiver extends ListenerAdapter {

    /** The {@link EventHandler} that receives all forwarded events */
    private final EventHandler handler;

    /** Whether incoming {@link net.dv8tion.jda.api.entities.Guild Guild} messages are deleted before they are forwarded */
    private boolean autoDelete = false;

    /**
     * Creates a new receiver.
     * @param handler the {@link EventHandler} to forward the events to
     * @param autoDelete {@code true} to delete every guild message (requires the {@code MESSAGE_MANAGE} permission);
     *                   direct messages are never deleted
     */
    public DefaultMessageReceiver(final EventHandler handler, final boolean autoDelete) {
        this.handler = handler;
        this.autoDelete = autoDelete;
    }

    /** Optionally deletes guild messages, then forwards the message to the public or private channel handler method. */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ");
        if (event.isFromGuild()) {
            if (autoDelete) {
                event.getMessage().delete().queue();
            }
            handler.onPublicChannelEvent(args[0], event);
            return;
        }

        handler.onPrivateChannelEvent(args[0], event);
    }
}
