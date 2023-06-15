package de.shurablack.listener;

import de.shurablack.core.event.EventHandler;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * This is a default/example class
 * <br><br>
 * The DefaultInteractionReceiver class is a listener that listens for various types of interaction events
 * in a Discord chat server.
 * <br><br>
 * It extends the ListenerAdapter class from the JDA library, which provides a simple way to create listeners for Discord events.
 * </p>
 *
 * @version core-1.0.0
 * @date 12.07.2022
 * @author ShuraBlack
 */
public class DefaultInteractionReceiver extends ListenerAdapter {

    /** The corresponding {@link EventHandler} */
    private final EventHandler handler;

    /**
     * The constructor for the class
     * @param handler the specified {@link EventHandler}
     */
    public DefaultInteractionReceiver(EventHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        handler.onModalEvent(event.getModalId(), event);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        handler.onButtonEvent(event.getId(), event);
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        handler.onStringSelectionMenuEvent(event.getId(), event);
    }

}
