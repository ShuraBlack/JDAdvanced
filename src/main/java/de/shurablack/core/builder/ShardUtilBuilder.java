package de.shurablack.core.builder;

import de.shurablack.core.event.handler.EventHandler;
import de.shurablack.core.event.handler.NativeEventHandler;
import de.shurablack.core.util.AssetPool;
import de.shurablack.core.util.Config;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

import java.util.function.Consumer;

/**
 * <p>
 * The ShardUtilBuilder class is a Java class that provides a way to create
 * and configure a {@link ShardUtil} object, which can be used to interact with a Discord bot
 * on multiple shards.
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * // Initialize the ShardUtilBuilder
 * UtilBuilder.init();
 *
 * // Create a ShardManagerBuilder object to build the Discord bot
 * DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(Config.getConfig("access_token"));
 *
 * // Create an EventHandler object to handle events in the Discord bot
 * EventHandler handler = createEventHandler();
 *
 * // Use the ShardUtilBuilder to create a ShardUtil object for the Discord bot
 * ShardUtil util = ShardUtilBuilder.create(builder, handler)
 *     // Add some command-line actions to the object
 *     .setCommandLineAction(
 *         new CommandAction("say-hello", "Says 'Hello, World!'", (String args) -> System.out.println("Hello, World!")),
 *         new CommandAction("say-goodbye", "Says 'Goodbye, World!'", (String args) -> System.out.println("Goodbye, World!"))
 *     )
 *     // Build and finish the object
 *     .build();
 * }</pre>
 *
 * @version core-1.1.0
 * @date 12.06.2024
 * @author ShuraBlack
 */
public class ShardUtilBuilder {

    /** {@link ShardUtil} which gets build */
    private final ShardUtil shardUtil;

    /**
     * This static method initializes the ShardUtilBuilder by loading the configuration properties
     * from the "config.properties" file and initializing the {@link AssetPool} and {@link de.shurablack.core.util.LocalData} classes
     */
    public static void init() {
        UtilBuilder.init();
    }

    /**
     * This is the constructor for the ShardUtilBuilder class
     * @param util the created {@link ShardUtil} object
     */
    ShardUtilBuilder(final ShardUtil util) {
        this.shardUtil = util;
    }

    /**
     *  This is a static factory method that creates a new ShardUtil object for building a {@link net.dv8tion.jda.api.sharding.ShardManager}.
     *  It uses the default EventHandler and a default DefaultShardManagerBuilder with the token from {@link Config}
     * @return the builder for chaining
     */
    public static ShardUtilBuilder createDefault() {
        final DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(Config.getConfig("access_token"));
        final NativeEventHandler handler = NativeEventHandler.createDefault();
        return create(builder, handler);
    }

    /**
     *  This is a static factory method that creates a new ShardUtil object for building a {@link JDAUtil}
     * @param builder the builder for the {@link DefaultShardManagerBuilder}
     * @param handler the specified {@link de.shurablack.core.event.handler.EventHandler}
     * @return the builder for chaining
     */
    public static ShardUtilBuilder create(final DefaultShardManagerBuilder builder, final EventHandler handler) {
        if (!UtilBuilder.isInitialized()) {
            throw new InitException("You need to run ShardUtilBuilder.init() before creating an ShardUtil object");
        }
        return new ShardUtilBuilder(new ShardUtil(builder.build(), handler));
    }

    /**
     * Adds an task which will be executed on application exit
     * @param task the specified task
     * @return the builder for chaining
     */
    public ShardUtilBuilder addOnExit(Consumer<String> task) {
        this.shardUtil.addExitTask(task);
        return this;
    }

    /**
     * Adds one or more {@link CommandAction} objects to the ShardUtil object that is being built by this ShardUtilBuilder.
     * <br><br>
     * These CommandAction objects define actions that can be performed using the command-line interface
     * @param commands the specified {@link CommandAction}
     * @return the builder for chaining
     */
    public ShardUtilBuilder setCommandLineAction(final CommandAction... commands) {
        for (CommandAction ca : commands) {
            this.shardUtil.addAction(ca);
        }
        return this;
    }

    /**
     * This method builds and returns the ShardUtil object that is being constructed by this ShardUtilBuilder.
     * <br><br>
     * The command line interface will be started on a successful connection to the Discord bot API.
     * @return the build {@link ShardUtil}
     */
    public ShardUtil build() {
        return this.shardUtil;
    }
}

