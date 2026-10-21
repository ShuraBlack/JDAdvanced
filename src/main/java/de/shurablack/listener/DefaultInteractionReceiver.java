package de.shurablack.listener;

import de.shurablack.core.event.handler.EventHandler;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Default listener that forwards component and modal interactions to an {@link EventHandler}.
 * <br><br>
 * Buttons, string select menus and entity select menus are forwarded with their component ID as identifier,
 * modals with their modal ID. This is the only default listener that handles select menus.
 * <br><br>
 * Can be used as is or serve as an example for a custom {@link ListenerAdapter}.
 * </p>
 *
 * @version core-1.0.0
 * @date 12.07.2022
 * @author ShuraBlack
 */
public class DefaultInteractionReceiver extends ListenerAdapter {

    /** The {@link EventHandler} that receives all forwarded events */
    private final EventHandler handler;

    /**
     * Creates a new receiver.
     * @param handler the {@link EventHandler} to forward the events to
     */
    public DefaultInteractionReceiver(EventHandler handler) {
        this.handler = handler;
    }

    /** Forwards the modal submission to {@link EventHandler#onModalEvent}, using the modal ID as identifier. */
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        handler.onModalEvent(event.getModalId(), event);
    }

    /** Forwards the button click to {@link EventHandler#onButtonEvent}, using the component ID as identifier. */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        handler.onButtonEvent(event.getComponentId(), event);
    }

    /** Forwards the string selection to {@link EventHandler#onStringSelectionMenuEvent}, using the component ID as identifier. */
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        handler.onStringSelectionMenuEvent(event.getComponentId(), event);
    }

    /** Forwards the entity selection to {@link EventHandler#onEntitySelectionMenuEvent}, using the component ID as identifier. */
    @Override
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        handler.onEntitySelectionMenuEvent(event.getComponentId(), event);
    }

}
