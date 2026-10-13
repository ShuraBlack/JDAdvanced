package de.shurablack.core.builder;

import de.shurablack.JDAdvanced;
import de.shurablack.core.event.handler.EventHandler;
import de.shurablack.core.event.handler.NativeEventHandler;
import de.shurablack.core.util.Config;
import net.dv8tion.jda.api.JDABuilder;

import java.util.function.Consumer;

/**
 * <p>
 * Creates and configures a {@link JDAUtil}, which wraps a single (unsharded) Discord bot.
 * For sharded bots use the {@link ShardUtilBuilder}.
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * // Load the config and initialize the library, restricting the classpath scans to your packages
 * JDAdvanced.init("de.mybot");
 *
 * // Create a JDABuilder object to build the Discord bot
 * JDABuilder builder = JDABuilder.createDefault(Config.getConfig("access_token"));
 *
 * // Create an EventHandler object to handle events in the Discord bot
 * EventHandler handler = NativeEventHandler.createDefault();
 *
 * // Use the JDAUtilBuilder to create a JDAUtil object for the Discord bot (starts the login)
 * JDAUtil util = JDAUtilBuilder.create(builder, handler)
 *     // Add some command-line actions to the JDAUtil object
 *     .setCommandLineAction(
 *         new CommandAction("say-hello", "Says 'Hello, World!'", (String args) -> System.out.println("Hello, World!")),
 *         new CommandAction("say-goodbye", "Says 'Goodbye, World!'", (String args) -> System.out.println("Goodbye, World!"))
 *     )
 *     // Return the JDAUtil object
 *     .build();
 * }</pre>
 *
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class JDAUtilBuilder {

    /** {@link JDAUtil} which gets configured */
    private final JDAUtil JDAUtil;

    /**
     * @param util the created {@link JDAUtil} object
     */
    JDAUtilBuilder(final JDAUtil util) {
        this.JDAUtil = util;
    }

    /**
     * Creates a builder with a {@link NativeEventHandler#createDefault()} handler and a
     * {@link JDABuilder#createDefault(String)} using the access_token from the {@link Config}
     * @return the builder for chaining
     * @throws InitException if {@link JDAdvanced#init()} wasn't called before
     */
    public static JDAUtilBuilder createDefault() {
        final JDABuilder builder = JDABuilder.createDefault(Config.getConfig("access_token"));
        final EventHandler handler = NativeEventHandler.createDefault();
        return create(builder, handler);
    }

    /**
     * Builds the {@link net.dv8tion.jda.api.JDA} instance (the login starts asynchronously) and creates a
     * builder for the {@link JDAUtil} wrapping it
     * @param builder the configured {@link JDABuilder}
     * @param handler the handler for the Discord events
     * @return the builder for chaining
     * @throws InitException if {@link JDAdvanced#init()} wasn't called before
     */
    public static JDAUtilBuilder create(final JDABuilder builder, final EventHandler handler) {
        if (!JDAdvanced.isInitialized()) {
            throw new InitException("You need to run JDAdvanced.init() before creating a JDAUtil object");
        }
        return new JDAUtilBuilder(new JDAUtil(builder.build(), handler));
    }

    /**
     * Sets the task executed once on application exit (exit console command or JVM shutdown).
     * Replaces a previously set task
     * @param task the task, receives the exit command input (an empty string on JVM shutdown)
     * @return the builder for chaining
     */
    public JDAUtilBuilder addOnExit(Consumer<String> task) {
        this.JDAUtil.addExitTask(task);
        return this;
    }

    /**
     * Adds {@link CommandAction}s to the command-line interface. They are checked before the
     * default console commands, so a name that prefixes a default command shadows it
     * @param commands the actions to add
     * @return the builder for chaining
     */
    public JDAUtilBuilder setCommandLineAction(final CommandAction... commands) {
        for (CommandAction ca : commands) {
            this.JDAUtil.addAction(ca);
        }
        return this;
    }

    /**
     * Returns the configured JDAUtil. The bot is already logging in since the create call, the
     * command-line interface starts once it is ready
     * @return the built {@link JDAUtil}
     */
    public JDAUtil build() {
        return this.JDAUtil;
    }
}
