package de.shurablack.core.builder;

import de.shurablack.core.event.EventHandler;
import de.shurablack.core.scheduling.Dispatcher;
import de.shurablack.sql.ConnectionPool;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * <p>
 * This project is licensed under the Apache 2.0 license <br>
 * Copyright (c) 2023 ShuraBlack<br>
 * For more information about the license, see <a href="https://www.apache.org/licenses/LICENSE-2.0">apache.org</a>
 * or check the <b>LICENSE</b> file in the project.
 * </p>
 * <br>
 *
 * <p>
 * The JDAUtil class is a Java class that provides a way to interact with a Discord bot
 * , aswell as using a command-line interface
 * <br>
 * It can only be created via the {@link UtilBuilder}.
 * <br>
 * </p>
 *
 * @see UtilBuilder UtilBuilder
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class JDAUtil {

    /** Class Logger */
    private static final Logger LOGGER = LogManager.getLogger(JDAUtil.class);

    /** Presents the discord bot */
    private static JDA JDA;

    /** Class to handle events of the discord bot */
    private final EventHandler handler;

    /** Class to manage connections to a database */
    private ConnectionPool connectionPool;

    /** Task which will be executed on exit */
    private Consumer<String> onExit;

    /**
     * List of {@link CommandAction} which represeting the available commands
     */
    private final List<CommandAction> commandActions = new ArrayList<>();

    /**
     * This is the constructor for the JDAUtil class
     * @param jda the specified discord bot
     * @param handler the specified hadler object
     */
    JDAUtil(final JDA jda, final EventHandler handler) {
        JDA = jda;
        this.handler = handler;
    }

    /**
     * Sets the connectionPool field to the given ConnectionPool object
     * @param pool the specified connection pool
     */
    void setConnectionPool(final ConnectionPool pool) {
        this.connectionPool = pool;
    }

    /**
     * Adds a {@link CommandAction} object to the commandActions list and make it available
     * @param action that should be added
     */
    void addAction(final CommandAction action) {
        this.commandActions.add(action);
    }

    /**
     * Adds an shutdown task, which will be executed on calling the exit command in the terminal
     * @param task the specified task
     */
    void addExitTask(final Consumer<String> task) {
        this.onExit = task;
    }

    /**
     * Creates a string containing a list of the available commands and their descriptions
     * @return the generated string
     */
    private String createOverview() {
        final StringBuilder s = new StringBuilder();

        for (CommandAction action : commandActions) {
            s.append(action.getName()).append(" -> ").append(action.getDescription()).append("\n");
        }

        return s.toString();
    }

    /**
     * Takes a string representing a command, and it returns an Optional<CommandAction> object
     * containing the CommandAction object for the given command if it exists,
     * or an empty Optional if the command does not exist.
     * @param input the command line input
     * @return Optional with {@link CommandAction} or an empty Optional
     */
    private Optional<CommandAction> findCommand(final String input) {
        return commandActions.stream().filter(command -> input.startsWith(command.getName())).findAny();
    }

    /**
     * Starts a new thread that reads commands from the
     * command-line interface and invokes the corresponding CommandAction objects.
     */
    void startCommandLine() {
        addAction(new CommandAction(
                "exit",
                "Closes the Application",
                input -> {
                    LOGGER.info("Application gets terminated ...");
                    Dispatcher.shutdownService();
                    if (this.onExit != null) {
                        onExit.accept(input);
                    }
                    System.exit(1);
                }
        ));
        new Thread(() -> {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            try {
                System.out.println("\n====================================================\n" +
                        "> Welcome to JDAUtil command line.\n" +
                        "Discord Bot fully started and is ready to use.\n" +
                        "Made by ShuraBlack" +
                        "\n====================================================\n" +
                        "Available Commands:" +
                        "\n====================================================\n" +
                        createOverview() + "\n");

                while ((line = reader.readLine()) != null) {
                    Optional<CommandAction> action = findCommand(line);
                    final String input = line;
                    action.ifPresent(entry -> entry.getConsumer().accept(input));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "Console_Thread").start();
    }

    /**
     * @return the {@link JDA} object which needs be link with {@link UtilBuilder#create(JDABuilder, EventHandler)}
     */
    public static JDA getJDA() {
        return JDA;
    }

    /**
     * @return the EventHandler object
     */
    public EventHandler getHandler() {
        return handler;
    }

    /**
     * @return the ConnectionPool object
     */
    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

}
