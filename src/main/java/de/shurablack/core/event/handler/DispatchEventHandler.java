package de.shurablack.core.event.handler;

import de.shurablack.core.event.interaction.InteractionSet;
import de.shurablack.core.event.interaction.Type;
import de.shurablack.core.scheduling.Dispatcher;
import de.shurablack.listener.DefaultInteractionReceiver;
import de.shurablack.listener.DefaultMessageReceiver;
import de.shurablack.listener.DefaultReactionReceiver;
import de.shurablack.listener.DefaultSlashReceiver;
import de.shurablack.monitoring.ResponseRegister;

import java.util.List;

/**
 * <p>
 * EventHandler which runs the validation and the {@link de.shurablack.core.event.EventWorker} on the
 * {@link Dispatcher} thread pool and records the execution times in the {@link ResponseRegister}.
 * The JDA event thread is free again right away, so long running workers don't block further events.
 * <br><br>
 * Pass the handler to the listeners, they call the matching <b>on*Event</b> method:
 * </p>
 * <pre>{@code
 * DispatchEventHandler handler = DispatchEventHandler.createDefault()
 *         .registerEvent(InteractionSet.fromAnnotation());
 * jda.addEventListener(new DefaultSlashReceiver(handler));
 * }</pre>
 *
 * @see AbstractEventHandler
 * @see DefaultInteractionReceiver DefaultInteractionReceiver
 * @see DefaultMessageReceiver DefaultMessageReceiver
 * @see DefaultReactionReceiver DefaultReactionReceiver
 * @see DefaultSlashReceiver DefaultSlashReceiver
 *
 * @version core-1.1.0
 * @date 15.06.2023
 * @author ShuraBlack
 */
public class DispatchEventHandler extends AbstractEventHandler {

    /**
     * Creates a handler with the default settings: bot requests ignored, error feedback on, no admin bypass.
     * The prefix stays unchanged
     * @return the handler for chaining
     */
    public static DispatchEventHandler createDefault() {
        return new DispatchEventHandler();
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
    public static DispatchEventHandler create(final String prefix, final boolean ignoreBotRequest, final boolean errorFeedback, final boolean adminBypass) {
        final DispatchEventHandler handler = createDefault();
        handler.configure(prefix, ignoreBotRequest, errorFeedback, adminBypass);
        return handler;
    }

    /** {@inheritDoc} Narrows the return type for chaining. */
    @Override
    public DispatchEventHandler registerEvent(final List<InteractionSet> set) {
        super.registerEvent(set);
        return this;
    }

    /**
     * Dispatches the task and records its execution time under "TYPE#identifier" in the {@link ResponseRegister}.
     * Exceptions of the task get logged by the {@link Dispatcher}
     */
    @Override
    protected void execute(final Type type, final String identifier, final Runnable task) {
        Dispatcher.dispatch(() -> ResponseRegister.getInstance().trackRunnable(task, type.name() + "#" + identifier));
    }
}
