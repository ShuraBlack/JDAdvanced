package de.shurablack.listener;


import de.shurablack.core.event.handler.EventHandler;
import de.shurablack.mapping.MultiKeyMap;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * <p>
 * Default listener that forwards {@link MessageReactionAddEvent}s on registered messages to an {@link EventHandler}.
 * <br><br>
 * Only reactions on messages registered in {@link #getMap()} are forwarded. The map key is
 * {@code "<channelId>/<messageId>"} and the value is the identifier passed to the handler. Guild reactions go to
 * {@link EventHandler#onPublicReactionEvent}, direct message reactions to {@link EventHandler#onPrivateReactionEvent}.
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * DefaultReactionReceiver receiver = new DefaultReactionReceiver(handler);
 * receiver.getMap().put(message.getChannel().getId() + "/" + message.getId(), "poll");
 * }</pre>
 * <p>
 * Can be used as is or serve as an example for a custom {@link ListenerAdapter}.
 * </p>
 *
 * @version core-1.0.0
 * @date 12.07.2022
 * @author ShuraBlack
 */
public class DefaultReactionReceiver extends ListenerAdapter {

    /** The {@link EventHandler} that receives all forwarded events */
    private final EventHandler handler;

    /** Maps {@code "<channelId>/<messageId>"} to the identifier; several messages can share one identifier */
    private final MultiKeyMap<String,String> map = new MultiKeyMap<>();

    /**
     * Creates a new receiver with an empty message registry.
     * @param handler the {@link EventHandler} to forward the events to
     */
    public DefaultReactionReceiver(final EventHandler handler) {
        this.handler = handler;
    }

    /**
     * Returns the live registry of watched messages. It is not thread-safe.
     * @return the map from {@code "<channelId>/<messageId>"} to identifier
     */
    public MultiKeyMap<String, String> getMap() {
        return map;
    }

    /** Forwards reactions on registered messages to the public or private reaction handler method; others are ignored. */
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
