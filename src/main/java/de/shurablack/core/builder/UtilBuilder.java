package de.shurablack.core.builder;

import de.shurablack.core.event.EventHandler;
import de.shurablack.core.util.AssetPool;
import de.shurablack.core.util.Config;
import de.shurablack.core.util.ConfigException;
import de.shurablack.core.util.LocalData;
import de.shurablack.sql.ConnectionPool;
import net.dv8tion.jda.api.JDABuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

/**
 * <p>
 * The UtilBuilder class is a Java class that provides a way to create
 * and configure a {@link JDAUtil} object, which can be used to interact with a Discord bot
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * // Initialize the UtilBuilder
 * UtilBuilder.init();
 *
 * // Create a JDABuilder object to build the Discord bot
 * JDABuilder builder = JDABuilder.createDefault(Config.getConfig("access_token"));
 *
 * // Create an EventHandler object to handle events in the Discord bot
 * EventHandler handler = createEventHandler();
 *
 * // Use the UtilBuilder to create a JDAUtil object for the Discord bot
 * JDAUtil util = UtilBuilder.create(builder, handler)
 *     // Add a database connection to the JDAUtil object
 *     .addDataBase()
 *     // Add some command-line actions to the JDAUtil object
 *     .setCommandLineAction(
 *         new CommandAction("say-hello", "Says 'Hello, World!'", (String args) -> System.out.println("Hello, World!")),
 *         new CommandAction("say-goodbye", "Says 'Goodbye, World!'", (String args) -> System.out.println("Goodbye, World!"))
 *     )
 *     // Build and return the JDAUtil object
 *     .build();
 * }</pre>
 *
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class UtilBuilder {

    /** Class Logger */
    private static final Logger LOGGER = LogManager.getLogger(UtilBuilder.class);

    /** Flag that indicates whether the {@link Config} has been initialized */
    private static boolean INIT = false;

    /** {@link JDAUtil} which gets build */
    private final JDAUtil JDAUtil;

    /**
     * This static method initializes the UtilBuilder by loading the configuration properties
     * from the "config.properties" file and initializing the {@link AssetPool} and {@link de.shurablack.core.util.LocalData} classes
     */
    public static void init() {
        Config.loadConfig();
        AssetPool.init();
        LocalData.init();
        INIT = true;
    }

    /**
     * This is the constructor for the UtilBuilder class
     * @param util the created {@link JDAUtil} object
     */
    UtilBuilder(final JDAUtil util) {
        this.JDAUtil = util;
    }

    /**
     *  This is a static factory method that creates a new UtilBuilder object for building a {@link JDAUtil}.
     *  It uses the default EventHandler and a default JDABuilder with the token from {@link Config}
     * @return the builder for chaining
     */
    public static UtilBuilder createDefault() {
        final JDABuilder builder = JDABuilder.createDefault(Config.getConfig("access_token"));
        final EventHandler handler = EventHandler.createDefault();
        return create(builder, handler);
    }

    /**
     *  This is a static factory method that creates a new UtilBuilder object for building a {@link JDAUtil}
     * @param builder the builder for the {@link net.dv8tion.jda.api.JDA}
     * @param handler the specified {@link EventHandler}
     * @return the builder for chaining
     */
    public static UtilBuilder create(final JDABuilder builder, final EventHandler handler) {
        if (!INIT) {
            LOGGER.error("You need to run JDAUtil.init() before creating an JDAUtil object", new InitException());
            System.exit(1);
        }
        return new UtilBuilder(new JDAUtil(builder.build(), handler));
    }

    /**
     * Adds an task which will be executed on application exit
     * @param task the specified task
     * @return the builder for chaining
     */
    public UtilBuilder addOnExit(Consumer<String> task) {
        this.JDAUtil.addExitTask(task);
        return this;
    }

    /**
     * Adds a database connection to the JDAUtil object that is being built by this UtilBuilder.
     * It uses the configuration properties in "config.properties" to connect to the database
     * and creates a ConnectionPool object to manage the connections.
     * @return the builder for chaining
     */
    public UtilBuilder addDatabase() {
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

        this.JDAUtil.setConnectionPool(
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
     * Adds one or more {@link CommandAction} objects to the JDAUtil object that is being built by this UtilBuilder.
     * <br><br>
     * These CommandAction objects define actions that can be performed using the command-line interface
     * @param commands the specified {@link CommandAction}
     * @return the builder for chaining
     */
    public UtilBuilder setCommandLineAction(final CommandAction... commands) {
        for (CommandAction ca : commands) {
            this.JDAUtil.addAction(ca);
        }
        return this;
    }

    /**
     * This method builds and returns the JDAUtil object that is being constructed by this UtilBuilder.
     * <br><br>
     * It also starts the command-line interface for the Discord bot.
     * @return the build {@link JDAUtil}
     */
    public JDAUtil build() {
        this.JDAUtil.startCommandLine();
        return this.JDAUtil;
    }
}
