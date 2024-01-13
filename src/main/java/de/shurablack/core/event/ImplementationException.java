package de.shurablack.core.event;

/**
 * <p>
 * The ImplementationException class is a RuntimeException that indicates that a requested operation has
 * failed due to an implementation error.
 * <br><br>
 * It has a single constructor that takes a string message as an argument, which can be used to provide
 * additional information about the error.
 * <br><br>
 * The exception is typically thrown when a method or operation is called
 * that has not been implemented or is not yet functional
 * <br><br>
 * This will inform the user that a {@link EventWorker} function got called,
 * without an implementation or stil calling the <b>super()</b> method
 * </p>
 *
 * @version core-1.0.0
 * @date 15.06.2023
 * @author ShuraBlack
 */
public class ImplementationException extends RuntimeException {

    public ImplementationException(final String message) {
        super(message);
    }

}
