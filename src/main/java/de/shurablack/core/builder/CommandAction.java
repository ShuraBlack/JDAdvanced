package de.shurablack.core.builder;

import java.util.function.Consumer;

/**
 * <p>
 * Represents a named action of the command-line interface. A console line is passed to the
 * first action whose name is a prefix of the line, so the full line (including the name) is the input.
 * <br><br>
 * A CommandAction can be registered via {@link UtilBuilder#setCommandLineAction(CommandAction...)} or
 * {@link ShardUtilBuilder#setCommandLineAction(CommandAction...)}
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * // Create a CommandAction object
 * CommandAction action = new CommandAction(
 *     "hello-world",
 *     "Prints 'Hello, World!' to the console",
 *     (String input) -> System.out.println("Hello, World!")
 * );
 *
 * // Print the name and description of the action
 * System.out.println("Name: " + action.getName());
 * System.out.println("Description: " + action.getDescription());
 *
 * // Invoke the action manually, passing the command line input
 * action.getConsumer().accept("");
 * }</pre>
 *
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class CommandAction {

    /**
     * Name of the action. A console line starting with this name invokes the action
     */
    private final String name;

    /**
     * Short description of the action, listed in the console banner
     */
    private final String description;

    /**
     * Code performed when the action is invoked. It receives the whole console line,
     * including the action name, and runs on the console thread
     */
    private final Consumer<String> consumer;

    /**
     * Creates a new action
     * @param name the name, used as prefix to match console lines
     * @param description the short description shown in the banner
     * @param consumer the code performed on call, receives the whole console line
     */
    public CommandAction(final String name, final String description, final Consumer<String> consumer) {
        this.name = name;
        this.description = description;
        this.consumer = consumer;
    }

    /** @return the name of the action */
    public String getName() {
        return name;
    }

    /** @return the description of the action */
    public String getDescription() {
        return description;
    }

    /** @return the code performed on call, receives the whole console line */
    public Consumer<String> getConsumer() {
        return consumer;
    }
}
