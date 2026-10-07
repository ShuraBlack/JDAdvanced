package de.shurablack.core.event.handler;

import de.shurablack.core.event.interaction.InteractionSet;
import de.shurablack.core.event.interaction.Type;

import java.util.List;

/**
 * <p>
 * EventHandler which runs the {@link de.shurablack.core.event.EventWorker} directly on the JDA event thread.
 * Long running workers block further events, use the {@link DispatchEventHandler} for those.
 * </p>
 *
 * @see AbstractEventHandler
 * @version core-1.1.0
 * @author ShuraBlack
 */
public class NativeEventHandler extends AbstractEventHandler {

    /**
     * Creates a new EventHandler object
     * @return the handler for chaining
     */
    public static NativeEventHandler createDefault() {
        return new NativeEventHandler();
    }

    /**
     * Creates a new EventHandler object
     * @param prefix the prefix for regular messages
     * @param ignoreBotRequest the ignore flag
     * @param errorFeedback whether failed validations get an ephemeral error reply
     * @param adminBypass whether administrators bypass channel restrictions and cooldowns
     * @return the handler for chaining
     */
    public static NativeEventHandler create(final String prefix, final boolean ignoreBotRequest, final boolean errorFeedback, final boolean adminBypass) {
        final NativeEventHandler handler = createDefault();
        handler.configure(prefix, ignoreBotRequest, errorFeedback, adminBypass);
        return handler;
    }

    @Override
    public NativeEventHandler registerEvent(final List<InteractionSet> set) {
        super.registerEvent(set);
        return this;
    }

    @Override
    protected void execute(final Type type, final String identifier, final Runnable task) {
        task.run();
    }
}
