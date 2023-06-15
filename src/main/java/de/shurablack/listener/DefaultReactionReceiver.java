package de.shurablack.listener;

import de.shurablack.core.event.EventHandler;
import de.shurablack.mapping.MultiKeyMap;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * <p>
 * This is a default/example class
 * <br><br>
 * The DefaultReactionReceiver class is a listener that listens for {@link MessageReactionAddEvent}
 * in a Discord chat server.
 * <br><br>
 * It extends the ListenerAdapter class from the JDA library, which provides a simple way to create listeners for Discord events.
 * </p>
 *
 * @version core-1.0.0
 * @date 12.07.2022
 * @author ShuraBlack
 */
public class DefaultReactionReceiver extends ListenerAdapter {

    /** The corresponding {@link EventHandler} */
    private final EventHandler handler;

    /** MultiKeyMap for grouping */
    private final MultiKeyMap<String,String> map = new MultiKeyMap<>();

    /**
     * The constructor for the class
     * @param handler the specified {@link EventHandler}
     */
    public DefaultReactionReceiver(final EventHandler handler) {
        this.handler = handler;
    }

    /**
     * @return the MultiKeyMap
     */
    public MultiKeyMap<String, String> getMap() {
        return map;
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        Optional<String> identifier = Optional.ofNullable(map.get(event.getChannel().getId() + "/" + event.getMessageId()));

        if (!identifier.isPresent()) {
            return;
        }

        if (event.isFromGuild()) {
            handler.onPublicReactionEvent(identifier.get(), event);
            return;
        }

        handler.onPrivateReactionEvent(identifier.get(), event);
    }
}
