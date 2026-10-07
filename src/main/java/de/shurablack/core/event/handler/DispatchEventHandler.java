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
 * EventHandler which runs the {@link de.shurablack.core.event.EventWorker} on the {@link Dispatcher} thread pool
 * and records the response times in the {@link ResponseRegister}
 * <br><br>
 * To bind the {@link net.dv8tion.jda.api.hooks.ListenerAdapter ListenerAdapter} event use the correct
 * <b>call</b> function within the Listener
 * </p>
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
     * Creates a new EventHandler object
     * @return the handler for chaining
     */
    public static DispatchEventHandler createDefault() {
        return new DispatchEventHandler();
    }

    /**
     * Creates a new EventHandler object
     * @param prefix the prefix for regular messages
     * @param ignoreBotRequest the ignore flag
     * @param errorFeedback whether failed validations get an ephemeral error reply
     * @param adminBypass whether administrators bypass channel restrictions and cooldowns
     * @return the handler for chaining
     */
    public static DispatchEventHandler create(final String prefix, final boolean ignoreBotRequest, final boolean errorFeedback, final boolean adminBypass) {
        final DispatchEventHandler handler = createDefault();
        handler.configure(prefix, ignoreBotRequest, errorFeedback, adminBypass);
        return handler;
    }

    @Override
    public DispatchEventHandler registerEvent(final List<InteractionSet> set) {
        super.registerEvent(set);
        return this;
    }

    /**
     * Dispatches the task and records its execution time in the {@link ResponseRegister}
     */
    @Override
    protected void execute(final Type type, final String identifier, final Runnable task) {
        Dispatcher.dispatch(() -> ResponseRegister.getInstance().trackRunnable(task, type.name() + "#" + identifier));
    }
}
