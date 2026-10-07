package de.shurablack.core.builder;

import de.shurablack.core.event.handler.EventHandler;
import de.shurablack.core.scheduling.Dispatcher;
import de.shurablack.core.util.FileBanner;
import de.shurablack.core.util.LocalData;
import de.shurablack.monitoring.VM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * <p>
 * Shared foundation of {@link JDAUtil} and {@link ShardUtil}: the command-line interface,
 * the default console commands and the shutdown handling.
 * </p>
 *
 * @version core-1.1.0
 * @date 07.10.2026
 * @author ShuraBlack
 */
public abstract class BaseUtil {

    /** Class Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseUtil.class);

    /** Class to handle events of the discord bot */
    private final EventHandler handler;

    /**
     * List of {@link CommandAction} which represeting the available commands
     */
    private final List<CommandAction> commandActions = new ArrayList<>();

    /** Task which will be executed on exit */
    private volatile Consumer<String> onExit;

    /** Guards the shutdown, so the exit command and the shutdown hook only run it once */
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    /** Guards the command line, so a repeated ready event doesnt start a second console */
    private final AtomicBoolean commandLineStarted = new AtomicBoolean(false);

    /**
     * @param handler the specified handler object
     */
    BaseUtil(final EventHandler handler) {
        this.handler = handler;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(""), "Shutdown_Hook"));
    }

    /**
     * Uploads all interaction commands to discord
     */
    abstract void uploadCommands();

    /**
     * Adds a {@link CommandAction} object to the commandActions list and make it available
     * @param action that should be added
     */
    void addAction(final CommandAction action) {
        this.commandActions.add(action);
    }

    /**
     * Adds an shutdown task, which will be executed on calling the exit command or on JVM shutdown
     * @param task the specified task, receives the exit command input (empty on JVM shutdown)
     */
    void addExitTask(final Consumer<String> task) {
        this.onExit = task;
    }

    /**
     * Stops the dispatcher and runs the exit task. Only the first call has an effect
     * @param input the exit command input
     */
    private void shutdown(final String input) {
        if (!shutdown.compareAndSet(false, true)) {
            return;
        }
        LOGGER.info("Application gets terminated ...");
        Dispatcher.shutdownService();
        final Consumer<String> task = this.onExit;
        if (task != null) {
            task.accept(input);
        }
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
        if (!commandLineStarted.compareAndSet(false, true)) {
            return;
        }
        addAction(new CommandAction(
                "force cron",
                "<name> Forces a cron task with given name",
                input -> {
                    final String[] split = input.split(" ");
                    if (split.length < 3) {
                        LOGGER.error("Invalid input: force cron <name>");
                        return;
                    }
                    Dispatcher.forceCronTask(split[2]);
                }
        ));
        addAction(new CommandAction(
                "reload local",
                "Reloads the LocalData",
                input -> {
                    LOGGER.info("Reloading LocalData ...");
                    LocalData.clear();
                    LocalData.init();
                    LOGGER.info("LocalData reloaded");
                }
        ));
        addAction(new CommandAction(
                "upload",
                "Uploads all interaction commands",
                input -> {
                    LOGGER.info("Uploading commands ...");
                    uploadCommands();
                    LOGGER.info("Commands uploaded");
                }
        ));
        addAction(new CommandAction(
                "performance",
                "Shows performance debug (<mem/load>)",
                input -> {
                    final String[] args = input.split(" ");
                    if (args.length < 2) {
                        LOGGER.error("Invalid input: performance <mem/load>");
                        return;
                    }
                    if (args[1].equals("mem")) {
                        LOGGER.info("\n{}", VM.getMemoryUsage());
                    } else if (args[1].equals("load")) {
                        LOGGER.info("\n{}", VM.getProcessLoad());
                    } else {
                        LOGGER.error("Invalid input: performance <mem/load>");
                    }
                }
        ));
        addAction(new CommandAction(
                "dispatcher",
                "Shows dispatcher debug",
                input -> Dispatcher.logStatus()
        ));
        addAction(new CommandAction(
                "exit",
                "Closes the Application",
                input -> {
                    shutdown(input);
                    System.exit(0);
                }
        ));
        new Thread(() -> {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            try {
                new FileBanner("/internal/banner.txt", commandActions).print(System.out);

                while ((line = reader.readLine()) != null) {
                    final String input = line;
                    findCommand(input).ifPresent(entry -> {
                        // A failing command must not kill the console thread
                        try {
                            entry.getConsumer().accept(input);
                        } catch (RuntimeException e) {
                            LOGGER.error("Command <{}> failed", entry.getName(), e);
                        }
                    });
                }
            } catch (IOException e) {
                LOGGER.error("Command line stopped", e);
            }
        }, "Console_Thread").start();
    }

    /**
     * @return the EventHandler object
     */
    public EventHandler getHandler() {
        return handler;
    }
}
