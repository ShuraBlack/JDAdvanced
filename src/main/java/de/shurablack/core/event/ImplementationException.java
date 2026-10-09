package de.shurablack.core.event;

/**
 * <p>
 * Unchecked exception that indicates a wrong implementation by the user of the library.
 * <br><br>
 * It gets thrown when a process method of an {@link EventWorker} gets called without being overridden
 * or while still calling the <b>super</b> method.
 * <br><br>
 * It also gets thrown by {@link de.shurablack.core.event.interaction.InteractionSet#fromAnnotation()} when a
 * {@link de.shurablack.core.event.annotation.command.CommandHandler @CommandHandler} method doesn't fit its
 * {@link de.shurablack.core.event.annotation.command.SlashCommand @SlashCommand} declaration or can't be invoked.
 * </p>
 *
 * @version core-1.0.0
 * @date 15.06.2023
 * @author ShuraBlack
 */
public class ImplementationException extends RuntimeException {

    /**
     * Creates the exception.
     * @param message the description of the wrong implementation
     */
    public ImplementationException(final String message) {
        super(message);
    }

}
