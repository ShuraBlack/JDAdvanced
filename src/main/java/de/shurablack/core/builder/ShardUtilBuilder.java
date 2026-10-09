package de.shurablack.core.builder;

import de.shurablack.core.event.handler.EventHandler;
import de.shurablack.core.event.handler.NativeEventHandler;
import de.shurablack.core.util.AssetPool;
import de.shurablack.core.util.Config;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

import java.util.function.Consumer;

/**
 * <p>
 * Creates and configures a {@link ShardUtil}, which wraps a Discord bot running on multiple shards.
 * The initialization is shared with the {@link UtilBuilder}.
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * // Load the config and initialize the library, restricting the classpath scans to your packages
 * ShardUtilBuilder.init("de.mybot");
 *
 * // Create a ShardManagerBuilder object to build the Discord bot
 * DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(Config.getConfig("access_token"));
 *
 * // Create an EventHandler object to handle events in the Discord bot
 * EventHandler handler = NativeEventHandler.createDefault();
 *
 * // Use the ShardUtilBuilder to create a ShardUtil object for the Discord bot (starts the login)
 * ShardUtil util = ShardUtilBuilder.create(builder, handler)
 *     // Add some command-line actions to the object
 *     .setCommandLineAction(
 *         new CommandAction("say-hello", "Says 'Hello, World!'", (String args) -> System.out.println("Hello, World!")),
 *         new CommandAction("say-goodbye", "Says 'Goodbye, World!'", (String args) -> System.out.println("Goodbye, World!"))
 *     )
 *     // Return the ShardUtil object
 *     .build();
 * }</pre>
 *
 * @version core-1.1.0
 * @date 12.06.2024
 * @author ShuraBlack
 */
public class ShardUtilBuilder {

    /** {@link ShardUtil} which gets configured */
    private final ShardUtil shardUtil;

    /**
     * Same as {@link #init()}, but restricts the classpath scans to the given packages and their sub packages
     * @param basePackages the packages of your bot, e.g. "de.mybot"; none scans the whole classpath
     * @throws InitException if the initialization fails, see {@link UtilBuilder#init()}
     * @see UtilBuilder#init(String...)
     */
    public static void init(final String... basePackages) {
        UtilBuilder.init(basePackages);
    }

    /**
     * Initializes the library: loads the "config.properties" file from the working directory, starts the
     * {@link de.shurablack.core.scheduling.Dispatcher} and initializes the {@link AssetPool} and
     * {@link de.shurablack.core.util.LocalData}. Must be called before {@link #create(DefaultShardManagerBuilder, EventHandler)}.
     * The classpath scans cover the whole classpath unless restricted via {@link #init(String...)}
     * @throws InitException if the config file or its access_token is missing, or the LocalData folder can't be created
     * @see UtilBuilder#init()
     */
    public static void init() {
        UtilBuilder.init();
    }

    /**
     * @param util the created {@link ShardUtil} object
     */
    ShardUtilBuilder(final ShardUtil util) {
        this.shardUtil = util;
    }

    /**
     * Creates a builder with a {@link NativeEventHandler#createDefault()} handler and a
     * {@link DefaultShardManagerBuilder#createDefault(String)} using the access_token from the {@link Config}
     * @return the builder for chaining
     * @throws InitException if {@link #init()} wasn't called before
     */
    public static ShardUtilBuilder createDefault() {
        final DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(Config.getConfig("access_token"));
        final NativeEventHandler handler = NativeEventHandler.createDefault();
        return create(builder, handler);
    }

    /**
     * Builds the {@link net.dv8tion.jda.api.sharding.ShardManager} (the shards log in asynchronously) and
     * creates a builder for the {@link ShardUtil} wrapping it
     * @param builder the configured {@link DefaultShardManagerBuilder}
     * @param handler the handler for the Discord events
     * @return the builder for chaining
     * @throws InitException if {@link #init()} wasn't called before
     */
    public static ShardUtilBuilder create(final DefaultShardManagerBuilder builder, final EventHandler handler) {
        if (!UtilBuilder.isInitialized()) {
            throw new InitException("You need to run ShardUtilBuilder.init() before creating an ShardUtil object");
        }
        return new ShardUtilBuilder(new ShardUtil(builder.build(), handler));
    }

    /**
     * Sets the task executed once on application exit (exit console command or JVM shutdown).
     * Replaces a previously set task
     * @param task the task, receives the exit command input (an empty string on JVM shutdown)
     * @return the builder for chaining
     */
    public ShardUtilBuilder addOnExit(Consumer<String> task) {
        this.shardUtil.addExitTask(task);
        return this;
    }

    /**
     * Adds {@link CommandAction}s to the command-line interface. They are checked before the
     * default console commands, so a name that prefixes a default command shadows it
     * @param commands the actions to add
     * @return the builder for chaining
     */
    public ShardUtilBuilder setCommandLineAction(final CommandAction... commands) {
        for (CommandAction ca : commands) {
            this.shardUtil.addAction(ca);
        }
        return this;
    }

    /**
     * Returns the configured ShardUtil. The bot is already logging in since the create call, the
     * command-line interface starts once it is ready
     * @return the built {@link ShardUtil}
     */
    public ShardUtil build() {
        return this.shardUtil;
    }
}

