package de.shurablack.core.scheduling;

import de.shurablack.core.scheduling.annotation.Schedule;
import de.shurablack.core.scheduling.annotation.ScheduleClass;
import de.shurablack.core.util.Config;
import de.shurablack.core.util.ClassScan;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import it.sauronsoftware.cron4j.InvalidPatternException;
import it.sauronsoftware.cron4j.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * <p>
 * Static task execution: a cron4j {@link Scheduler} for time-based (cron) tasks and a thread pool
 * ("service executor") for asynchronous tasks, e.g. the event handling.
 * <br><br>
 * {@link #start()} must be called before {@link #dispatch(Runnable)} and the other executor methods;
 * {@link de.shurablack.core.builder.UtilBuilder#init()} does that. Cron tasks are registered manually via
 * {@link #scheduleCronTask(String, String, Runnable)} or found via {@link #discoverCronTasks()}, which
 * is not called automatically.
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * Dispatcher.start();
 * Dispatcher.dispatch(() -> System.out.println("async"));
 * // every day at 08:00 (cron4j pattern)
 * Dispatcher.scheduleCronTask("0 8 * * *", "daily_report", () -> System.out.println("report"));
 * }</pre>
 *
 * @version core-1.0.0
 * @date 09.06.2023
 * @author ShuraBlack
 */
public class Dispatcher {

    /** Class Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(Dispatcher.class);

    /** Seconds {@link #shutdownService()} waits for running tasks before interrupting them */
    static final int SHUTDOWN_TIMEOUT_SECONDS = 5;

    /** The scheduler for time-based tasks, started lazily by the first scheduled cron task */
    private static final Scheduler SCHEDULER = new Scheduler();

    /** The service executor for asynchronous tasks, null until {@link #start()} is called */
    private static ThreadPoolExecutor SERVICE;

    /** The scheduled cron tasks */
    private static final ConcurrentLinkedQueue<Entry> TASKS = new ConcurrentLinkedQueue<>();

    private Dispatcher() { }

    /**
     * Starts the service executor. Does nothing if it is already running; after {@link #shutdownService()}
     * a new executor gets created.
     * <br><br>
     * The service executor is a fixed thread pool with <code>dispatcher_thread_max</code> threads from the
     * {@link Config} (default: available processors * 2). Tasks beyond that wait in an unbounded queue instead
     * of being discarded. Idle threads terminate after 10 minutes.
     * @throws NumberFormatException if <code>dispatcher_thread_max</code> isn't a valid number
     */
    public static synchronized void start() {
        // A shut down executor gets replaced, so the dispatcher can be started again
        if (SERVICE != null && !SERVICE.isShutdown()) {
            return;
        }

        // Fixed pool with a queue: the former cached pool used a SynchronousQueue, so every task beyond
        // the max thread count was rejected and silently discarded instead of waiting for a free thread.
        final int threads = Config.getConfigAsInt("dispatcher_thread_max", Runtime.getRuntime().availableProcessors() * 2);
        SERVICE = new ThreadPoolExecutor(threads, threads, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        SERVICE.allowCoreThreadTimeOut(true);

        final String msg = String.format("Started Service Executor with <\u001b[32;1m%d\u001b[0m> Threads", SERVICE.getMaximumPoolSize());
        LOGGER.info(msg);
    }

    /**
     * Executes a task asynchronously on the service executor. Exceptions thrown by the task are logged.
     * @param task the task which will be executed in a separate thread
     * @throws NullPointerException if {@link #start()} wasn't called
     * @throws java.util.concurrent.RejectedExecutionException if the executor was shut down
     */
    public static void dispatch(final Runnable task) {
        // execute instead of submit: submit stores exceptions in a Future nobody reads, hiding every failure
        SERVICE.execute(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                LOGGER.error("Uncaught exception in dispatched task", t);
            }
        });
    }

    /**
     * Shuts down the service executor and stops the scheduler if it is running.
     * <br><br>
     * Already submitted tasks get {@value #SHUTDOWN_TIMEOUT_SECONDS} seconds to finish, afterwards the remaining
     * ones get interrupted. Does nothing for the executor if {@link #start()} wasn't called.
     */
    public static synchronized void shutdownService() {
        if (SERVICE != null) {
            SERVICE.shutdown();
            try {
                if (!SERVICE.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    SERVICE.shutdownNow();
                }
            } catch (InterruptedException e) {
                SERVICE.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        if (SCHEDULER.isStarted()) {
            SCHEDULER.stop();
        }
    }

    /**
     * Schedules a cron task and starts the scheduler if necessary. Invalid patterns are logged and skipped.
     * <br><br>
     * The task runs on a thread of the cron4j scheduler, not on the service executor.
     * @param timePattern the cron4j scheduling pattern, e.g. "0 8 * * *" for every day at 08:00
     * @param name the name to find the task again; should be unique, since lookups use the first match
     * @param task the task to run
     */
    public static void scheduleCronTask(final String timePattern, final String name, final Runnable task) {
        if (!SCHEDULER.isStarted()) {
            SCHEDULER.start();
        }
        try {
            final String msg = String.format("Scheduled Task <\u001b[32;1m%s\u001b[0m> with time <\u001b[32;1m%s\u001b[0m>", name, timePattern);
            LOGGER.info(msg);
            TASKS.add(new Entry(name, SCHEDULER.schedule(timePattern, task), task));
        } catch (InvalidPatternException e) {
            LOGGER.error(String.format("Invalid pattern in Task scheduling <\u001b[31m%s\u001b[0m>", timePattern),e);
        }
    }

    /**
     * Scans the classes configured in {@link ClassScan} for {@link ScheduleClass} classes and schedules each
     * of their {@link Schedule} methods via {@link #scheduleCronTask(String, String, Runnable)}.
     * <br><br>
     * The methods must be static and parameterless, other methods are logged and skipped.
     */
    public static void discoverCronTasks() {
        try (ScanResult result = ClassScan.scan()) {
            ClassInfoList classes = result.getClassesWithAnnotation(ScheduleClass.class);

            for (ClassInfo info : classes) {
                // Superclasses outside of the scanned packages
                if (info.isExternalClass()) continue;
                Class<?> workerClass = info.loadClass();

                for (Method method : workerClass.getDeclaredMethods()) {
                    if (!method.isAnnotationPresent(Schedule.class)) {
                        continue;
                    }

                    Schedule schedule = method.getAnnotation(Schedule.class);
                    if (schedule == null) {
                        continue;
                    }
                    // The task gets invoked without an instance, so it has to be static
                    if (!Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0) {
                        LOGGER.error("Method <\u001b[31m{}\u001b[0m> in class <\u001b[31m{}\u001b[0m> must be static and without parameter", method.getName(), workerClass.getName());
                        continue;
                    }
                    method.setAccessible(true);
                    String timePattern = schedule.pattern();
                    String name = schedule.name();
                    Runnable task = () -> {
                        try {
                            method.invoke(null);
                        } catch (Exception e) {
                            LOGGER.error("Failed to execute scheduled task <\u001b[31m{}\u001b[0m> in class <\u001b[31m{}\u001b[0m>", name, workerClass.getName(), e);
                        }
                    };

                    scheduleCronTask(timePattern, name, task);
                }
            }
        }
    }

    /**
     * Deschedules the first cron task with the given name. Does nothing if no task matches.
     * @param name the name of the task
     */
    public static void descheduleCronTask(final String name) {
        final Optional<Entry> entry = TASKS.stream().filter(e -> e.getName()
                .equals(name)).findFirst();
        if (entry.isEmpty()) {
            return;
        }
        TASKS.remove(entry.get());
        SCHEDULER.deschedule(entry.get().getId());
        final String msg = String.format("Descheduled Task <\u001b[32;1m%s\u001b[0m>", name);
        LOGGER.info(msg);
    }

    /**
     * Runs the first cron task with the given name immediately and synchronously on the calling thread.
     * Logs an error if no task matches.
     * @param name the name of the task
     */
    public static void forceCronTask(final String name) {
        final Optional<Entry> entry = TASKS.stream().filter(e -> e.getName()
                .equals(name)).findFirst();

        if (entry.isEmpty()) {
            LOGGER.error("No matching task found!");
            return;
        }
        entry.get().getTask().run();
    }

    /**
     * @return the live queue of scheduled cron tasks as {@link Entry} objects (not a copy)
     */
    public static ConcurrentLinkedQueue<Entry> getTaskList() {
        return TASKS;
    }

    /**
     * @return the {@link ThreadPoolExecutor} of the service executor or null if {@link #start()} wasn't called
     */
    public static ThreadPoolExecutor getThreadPool() {
        return SERVICE;
    }

    /**
     * @return the {@link ThreadFactory} of the service executor
     * @throws NullPointerException if {@link #start()} wasn't called
     */
    public static ThreadFactory getThreadFactory() {
        return SERVICE.getThreadFactory();
    }

    /**
     * Logs the current pool size and task counters of the service executor (INFO level).
     */
    public static void logStatus() {
        LOGGER.info("\nSize [Current {}, Maximum \033[0;33m{}\033[0m, Peak \033[0;31m{}\033[0m] " +
                        "- Tasks [Total {}, Completed \033[0;32m{}\033[0m, Active \033[0;34m{}\033[0m]"
                , SERVICE.getPoolSize(), SERVICE.getMaximumPoolSize(), SERVICE.getLargestPoolSize()
        , SERVICE.getTaskCount(), SERVICE.getCompletedTaskCount(), SERVICE.getActiveCount());
    }
}
