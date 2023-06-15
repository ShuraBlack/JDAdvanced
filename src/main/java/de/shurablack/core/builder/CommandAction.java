package de.shurablack.core.builder;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * <p>
 * The CommandAction class is a simple Java class that represents an
 * action that can be performed by a command-line interface
 * <br><br>
 * A CommandAction can be registered on {@link UtilBuilder#setCommandLineAction(CommandAction...)} or
 * {@link JDAUtil#addAction(CommandAction)}
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * // Create a CommandAction object
 * CommandAction action = new CommandAction(
 *     "hello-world",
 *     "Prints 'Hello, World!' to the console",
 *     (String args) -> System.log.println("Hello, World!")
 * );
 *
 * // Print the name and description of the action
 * System.out.println("Name: " + action.getName());
 * System.out.println("Description: " + action.getDescription());
 *
 * // Invoke the action by calling the getConsumer() method and passing in any necessary arguments
 * action.getConsumer().accept("");
 * }</pre>
 *
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class CommandAction implements Serializable {

    private static final long serialVersionUID = 7730366511601722413L;

    /**
     * A string representing the name of the action.
     * This is the name that will be used to invoke the action from the command-line interface
     */
    private final String name;

    /**
     * A string providing a description of the action.
     * This description can be displayed to the user to help them understand what the action does
     */
    private final String description;

    /**
     * Object representing the action to be performed when the command is invoked.
     * This object should take a single string argument representing any additional
     * arguments that are passed to the command, and it should perform the desired action
     */
    private final Consumer<String> consumer;

    /**
     * This is the constructor for the CommandAction class.
     * It takes three arguments: the name and description of the action,
     * and a consumer object representing the action to be performed.
     * @param name the specified name
     * @param description the specified short description
     * @param consumer the code which will be performed on call
     */
    public CommandAction(final String name, final String description, final Consumer<String> consumer) {
        this.name = name;
        this.description = description;
        this.consumer = consumer;
    }

    /**
     * @return  the 'name' of the action
     */
    public String getName() {
        return name;
    }

    /**
     * @return the 'description' of the action
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the 'consumer' object representing the action to be performed
     */
    public Consumer<String> getConsumer() {
        return consumer;
    }
}
