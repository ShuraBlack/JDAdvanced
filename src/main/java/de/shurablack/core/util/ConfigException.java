package de.shurablack.core.util;

/**
 * <p>
 * The ConfigException class is a simple Java class that represents an exception that is thrown when there is an error
 * with a configuration. It is a subclass of RuntimeException, which means that it is
 * an unchecked exception that is not required to be declared or caught in a try-catch block.
 * <br><br>
 * This will prevent the user to add a Database to the {@link UtilBuilder} will the needed properties are absent
 * </p>
 *
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class ConfigException extends RuntimeException {
}
