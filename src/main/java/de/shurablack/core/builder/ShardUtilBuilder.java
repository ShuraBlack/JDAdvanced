package de.shurablack.core.builder;

import de.shurablack.core.event.EventHandler;
import de.shurablack.core.scheduling.Dispatcher;
import de.shurablack.core.util.AssetPool;
import de.shurablack.core.util.Config;
import de.shurablack.core.util.ConfigException;
import de.shurablack.core.util.LocalData;
import de.shurablack.sql.ConnectionPool;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    /** Class Logger */
    private static final Logger LOGGER = LogManager.getLogger(UtilBuilder.class);

    /** Flag that indicates whether the {@link Config} has been initialized */
    private static boolean INIT = false;

    /** {@link ShardUtil} which gets build */
    private final ShardUtil shardUtil;

    /**
     * This static method initializes the ShardUtilBuilder by loading the configuration properties
     * from the "config.properties" file and initializing the {@link AssetPool} and {@link de.shurablack.core.util.LocalData} classes
     */
    public static void init() {
        Config.loadConfig();
        Dispatcher.start();
        AssetPool.init();
        LocalData.init();
        INIT = true;
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
        final EventHandler handler = EventHandler.createDefault();
        return create(builder, handler);
    }

    /**
     *  This is a static factory method that creates a new ShardUtil object for building a {@link JDAUtil}
     * @param builder the builder for the {@link DefaultShardManagerBuilder}
     * @param handler the specified {@link EventHandler}
     * @return the builder for chaining
     */
    public static ShardUtilBuilder create(final DefaultShardManagerBuilder builder, final EventHandler handler) {
        if (!INIT) {
            LOGGER.error("You need to run JDAUtil.init() before creating an JDAUtil object", new InitException());
            System.exit(1);
        }
        return new ShardUtilBuilder(new ShardUtil(builder.build(), handler));
    }

    /**
     * Adds an task which will be executed on application exit
     * @param task the specified task
     * @return the builder for chaining
     */
    public ShardUtilBuilder addOnExit(Consumer<Void> task) {
        this.shardUtil.addExitTask(task);
        return this;
    }

    /**
     * @deprecated
     * This method is deprecated and will be removed in future versions.<br>
     * The management of database connections should happen separately from the class.
     * <br><br>
     * Adds a database connection to the JDAUtil object that is being built by this UtilBuilder.
     * It uses the configuration properties in "config.properties" to connect to the database
     * and creates a ConnectionPool object to manage the connections.
     * @return the builder for chaining
     */
    public ShardUtilBuilder addDatabase() {
        final String url = Config.getConfig("db_url");
        final String username = Config.getConfig("db_username");
        final String password = Config.getConfig("db_password");
        final String size = Config.getConfig("db_poolsize");
        if (url == null || username == null || password == null) {
            LOGGER.warn("You need to declare db config properties before adding a database", new ConfigException());
        }

        try {
            LOGGER.debug("Register MySQL Driver ...");
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (
                IllegalArgumentException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            LOGGER.debug("Couldnt register MySQL Driver. Shutting down the Application ...",e);
            System.exit(1);
        }

        this.shardUtil.setConnectionPool(
                new ConnectionPool(
                        url,
                        username,
                        password,
                        size == null ? 1 : Integer.parseInt(size)
                )
        );
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

