package de.shurablack.core.builder;

import de.shurablack.core.event.handler.EventHandler;
import de.shurablack.core.scheduling.Dispatcher;
import de.shurablack.core.util.Config;
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
 * <br><br>
 * Once the bot is ready ({@link #onReady()}), the commands are optionally uploaded, the default
 * console commands ({@code force cron}, {@code reload local}, {@code upload}, {@code performance},
 * {@code dispatcher}, {@code exit}) are registered and a console thread starts reading {@code System.in}.
 * A line is passed to the first {@link CommandAction} whose name is a prefix of it.
 * <br><br>
 * The constructor registers a JVM shutdown hook which stops the {@link Dispatcher} and runs the exit task.
 * </p>
 *
 * @version core-1.1.0
 * @date 07.10.2026
 * @author ShuraBlack
 */
public abstract class BaseUtil {

    /** Class Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseUtil.class);

    /** Handler that dispatches the Discord events of the bot */
    private final EventHandler handler;

    /**
     * The {@link CommandAction}s available on the command-line interface, user actions first,
     * followed by the defaults added in {@link #onReady()}
     */
    private final List<CommandAction> commandActions = new ArrayList<>();

    /** Task executed on shutdown, null if none was set */
    private volatile Consumer<String> onExit;

    /** Guards the shutdown, so the exit command and the shutdown hook only run it once */
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    /** Guards {@link #onReady()}, so a repeated ready event doesn't start a second console */
    private final AtomicBoolean commandLineStarted = new AtomicBoolean(false);

    /**
     * Stores the handler and registers a JVM shutdown hook that runs the shutdown once
     * @param handler the handler for the Discord events
     */
    BaseUtil(final EventHandler handler) {
        this.handler = handler;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(""), "Shutdown_Hook"));
    }

    /**
     * Uploads all annotated interaction commands to Discord, asynchronously
     * @param onlyChanged true to skip targets (global/guild) whose commands are already registered unchanged
     * @see CommandUploadBuilder
     */
    abstract void uploadCommands(boolean onlyChanged);

    /**
     * Makes a {@link CommandAction} available on the command-line interface.
     * Not synchronized, intended to be called before the bot is ready
     * @param action the action to add
     */
    void addAction(final CommandAction action) {
        this.commandActions.add(action);
    }

    /**
     * Sets the shutdown task, executed once on the exit command or on JVM shutdown.
     * Replaces a previously set task
     * @param task the task, receives the exit command input (an empty string on JVM shutdown)
     */
    void addExitTask(final Consumer<String> task) {
        this.onExit = task;
    }

    /**
     * Stops the dispatcher and runs the exit task. Only the first call has an effect
     * @param input the exit command input, empty when called by the shutdown hook
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
     * Finds the action for a command line. An action matches if the line starts with its name,
     * the first match in registration order wins
     * @param input the command line input
     * @return the matching {@link CommandAction}, or an empty Optional if none matches
     */
    private Optional<CommandAction> findCommand(final String input) {
        return commandActions.stream().filter(command -> input.startsWith(command.getName())).findAny();
    }

    /**
     * Called when the bot is ready, only the first call has an effect. Uploads the changed commands if
     * "commands.auto-upload" is true in the config, registers the default console commands, prints the
     * banner and starts the "Console_Thread" that reads {@code System.in} and invokes the matching
     * {@link CommandAction}s. Exceptions thrown by an action are logged and don't stop the console.
     */
    void onReady() {
        if (!commandLineStarted.compareAndSet(false, true)) {
            return;
        }
        if (Boolean.parseBoolean(Config.getConfig("commands.auto-upload"))) {
            LOGGER.info("Auto upload of changed commands ...");
            uploadCommands(true);
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
                "[force] Uploads changed interaction commands, force uploads all",
                input -> {
                    LOGGER.info("Uploading commands ...");
                    uploadCommands(!input.endsWith(" force"));
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

    /** @return the {@link EventHandler} of the bot */
    public EventHandler getHandler() {
        return handler;
    }
}
