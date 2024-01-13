package de.shurablack.listener;

import de.shurablack.core.event.EventHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * This is a default/example class
 * <br><br>
 * The DefaultMessageReceiver class is a listener that listens for {@link MessageReceivedEvent} in a Discord chat server.
 * <br><br>
 * It extends the ListenerAdapter class from the JDA library, which provides a simple way to create listeners for Discord events.
 * </p>
 *
 * @version core-1.0.0
 * @date 12.07.2022
 * @author ShuraBlack
 */
public class DefaultMessageReceiver extends ListenerAdapter {

    /** The corresponding {@link EventHandler} */
    private final EventHandler handler;

    /** Automatic delete incoming {@link net.dv8tion.jda.api.entities.Guild Guild} messages */
    private boolean autoDelete = false;

    /**
     * The constructor for the class
     * @param handler the specified {@link EventHandler}
     * @param autoDelete the flag for auto deletion
     */
    public DefaultMessageReceiver(final EventHandler handler, final boolean autoDelete) {
        this.handler = handler;
        this.autoDelete = autoDelete;
    }

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
