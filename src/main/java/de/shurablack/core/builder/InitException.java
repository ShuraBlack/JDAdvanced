package de.shurablack.core.builder;

import de.shurablack.core.event.EventHandler;
import net.dv8tion.jda.api.JDABuilder;

/**
 * <p>
 * The InitException class is a simple Java class that represents an exception that is thrown when there is an
 * error with initialization. It is a subclass of RuntimeException, which
 * means that it is an unchecked exception that is not required to be declared or caught in a try-catch block.
 * <br><br>
 * This will prevent the user to create an {@link JDAUtil} object via the {@link UtilBuilder#create(JDABuilder, EventHandler)},
 * without calling the {@link UtilBuilder#init()} function.
 * </p>
 *
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class InitException extends RuntimeException {
}
