package de.shurablack.core.builder;

import de.shurablack.core.event.handler.EventHandler;
import de.shurablack.core.event.handler.NativeEventHandler;
import de.shurablack.core.scheduling.Dispatcher;
import de.shurablack.core.util.AssetPool;
import de.shurablack.core.util.ClassScan;
import de.shurablack.core.util.Config;
import de.shurablack.core.util.LocalData;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * UtilBuilder.init("de.mybot");
 *
 * // Create a JDABuilder object to build the Discord bot
 * JDABuilder builder = JDABuilder.createDefault(Config.getConfig("access_token"));
 *
 * // Create an EventHandler object to handle events in the Discord bot
 * EventHandler handler = NativeEventHandler.createDefault();
 *
 * // Use the UtilBuilder to create a JDAUtil object for the Discord bot (starts the login)
 * JDAUtil util = UtilBuilder.create(builder, handler)
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
public class UtilBuilder {

    /** Class Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(UtilBuilder.class);

    /** Flag that indicates whether {@link #init()} has completed */
    private static boolean INIT = false;

    /** {@link JDAUtil} which gets configured */
    private final JDAUtil JDAUtil;

    /**
     * Same as {@link #init()}, but restricts the classpath scans (event workers, cron tasks, commands)
     * to the given packages and their sub packages. Scanning only your own packages speeds up the startup
     * @param basePackages the packages of your bot, e.g. "de.mybot"; none scans the whole classpath
     * @throws InitException if the initialization fails, see {@link #init()}
     */
    public static void init(final String... basePackages) {
        ClassScan.setPackages(basePackages);
        init();
    }

    /**
     * Initializes the library: loads the "config.properties" file from the working directory, starts the
     * {@link de.shurablack.core.scheduling.Dispatcher} and initializes the {@link AssetPool} and
     * {@link de.shurablack.core.util.LocalData}. Must be called before {@link #create(JDABuilder, EventHandler)}.
     * The classpath scans cover the whole classpath unless restricted via {@link #init(String...)}
     * @throws InitException if the config file or its access_token is missing, or the LocalData folder can't be created
     */
    public static void init() {
        LOGGER.info("UtilBuilder initialization started");
        Config.loadConfig();
        Dispatcher.start();
        AssetPool.init();
        LocalData.init();
        LOGGER.info("UtilBuilder initialization completed");
        INIT = true;
    }

    /** @return true if {@link #init()} has completed successfully */
    static boolean isInitialized() {
        return INIT;
    }

    /**
     * @param util the created {@link JDAUtil} object
     */
    UtilBuilder(final JDAUtil util) {
        this.JDAUtil = util;
    }

    /**
     * Creates a builder with a {@link NativeEventHandler#createDefault()} handler and a
     * {@link JDABuilder#createDefault(String)} using the access_token from the {@link Config}
     * @return the builder for chaining
     * @throws InitException if {@link #init()} wasn't called before
     */
    public static UtilBuilder createDefault() {
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
     * @throws InitException if {@link #init()} wasn't called before
     */
    public static UtilBuilder create(final JDABuilder builder, final EventHandler handler) {
        if (!INIT) {
            throw new InitException("You need to run UtilBuilder.init() before creating an JDAUtil object");
        }
        return new UtilBuilder(new JDAUtil(builder.build(), handler));
    }

    /**
     * Sets the task executed once on application exit (exit console command or JVM shutdown).
     * Replaces a previously set task
     * @param task the task, receives the exit command input (an empty string on JVM shutdown)
     * @return the builder for chaining
     */
    public UtilBuilder addOnExit(Consumer<String> task) {
        this.JDAUtil.addExitTask(task);
        return this;
    }

    /**
     * Adds {@link CommandAction}s to the command-line interface. They are checked before the
     * default console commands, so a name that prefixes a default command shadows it
     * @param commands the actions to add
     * @return the builder for chaining
     */
    public UtilBuilder setCommandLineAction(final CommandAction... commands) {
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
