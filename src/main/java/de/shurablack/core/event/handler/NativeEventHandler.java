package de.shurablack.core.event.handler;

import de.shurablack.core.event.interaction.InteractionSet;
import de.shurablack.core.event.interaction.Type;

import java.util.List;

/**
 * <p>
 * EventHandler which runs the validation and the {@link de.shurablack.core.event.EventWorker} inline on the
 * JDA event thread. Exceptions of a worker propagate to JDA.
 * <br><br>
 * Long running workers block further events, use the {@link DispatchEventHandler} for those.
 * </p>
 *
 * @see AbstractEventHandler
 * @version core-1.1.0
 * @author ShuraBlack
 */
public class NativeEventHandler extends AbstractEventHandler {

    /**
     * Creates a handler with the default settings: bot requests ignored, error feedback on, no admin bypass.
     * The prefix stays unchanged
     * @return the handler for chaining
     */
    public static NativeEventHandler createDefault() {
        return new NativeEventHandler();
    }

    /**
     * Creates a handler with the given settings
     * @param prefix the prefix for message commands (static, shared by all handlers), null or empty for
     *               {@link EventHandler#DEFAULT_PREFIX}. Set it before the interactions get created
     * @param ignoreBotRequest whether events triggered by bots get rejected
     * @param errorFeedback whether failed validations get an ephemeral error reply
     * @param adminBypass whether administrators bypass the channel restrictions and cooldowns of guild events
     * @return the handler for chaining
     */
    public static NativeEventHandler create(final String prefix, final boolean ignoreBotRequest, final boolean errorFeedback, final boolean adminBypass) {
        final NativeEventHandler handler = createDefault();
        handler.configure(prefix, ignoreBotRequest, errorFeedback, adminBypass);
        return handler;
    }

    /** {@inheritDoc} Narrows the return type for chaining. */
    @Override
    public NativeEventHandler registerEvent(final List<InteractionSet> set) {
        super.registerEvent(set);
        return this;
    }

    /** Runs the task on the calling (JDA event) thread */
    @Override
    protected void execute(final Type type, final String identifier, final Runnable task) {
        task.run();
    }
}
