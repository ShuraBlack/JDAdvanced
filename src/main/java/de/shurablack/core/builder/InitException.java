package de.shurablack.core.builder;

import net.dv8tion.jda.api.JDABuilder;

/**
 * <p>
 * The InitException class is a simple Java class that represents an exception that is thrown when there is an
 * error with initialization. It is a subclass of RuntimeException, which
 * means that it is an unchecked exception that is not required to be declared or caught in a try-catch block.
 * <br><br>
 * It is thrown when the {@link UtilBuilder#init()} function fails (e.g. missing config) or when
 * {@link UtilBuilder#create(JDABuilder, de.shurablack.core.event.handler.EventHandler)} is called without it.
 * </p>
 *
 * @version core-1.1.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class InitException extends RuntimeException {

    public InitException(final String message) {
        super(message);
    }
}
