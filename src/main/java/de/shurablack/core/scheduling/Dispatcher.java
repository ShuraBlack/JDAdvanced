package de.shurablack.core.scheduling;

import de.shurablack.core.util.Config;
import it.sauronsoftware.cron4j.InvalidPatternException;
import it.sauronsoftware.cron4j.Scheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.*;

/**
 * <p>
 * The Dispatcher class handles scheduling and dispatching of tasks using a combination of a scheduler
 * for time-based tasks and a service executor for asynchronous tasks.
 * <br><br>
 * It provides methods for scheduling and descheduling cron tasks, dispatching tasks to the service executor,
 * and retrieving information about the scheduled tasks.
 * </p>
 *
 * @version core-1.0.0
 * @date 09.06.2023
 * @author ShuraBlack
 */
public class Dispatcher {

    private static final Logger LOGGER = LogManager.getLogger(Dispatcher.class);

    /** The scheduler for time-based tasks*/
    private static final Scheduler SCHEDULER = new Scheduler();

    /** The service executor for asynchronous tasks*/
    private static ThreadPoolExecutor SERVICE;

    /** The list of scheduled tasks */
    private static final ConcurrentLinkedQueue<Entry> TASKS = new ConcurrentLinkedQueue<>();

    private Dispatcher() { }

    /**
     * Method to start the service executor.
     * <br>
     * The service executor is a cached thread pool executor with a core pool size of the available processors
     */
    public static void start() {
        if (SERVICE != null) {
            return;
        }

        SERVICE = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        SERVICE.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        SERVICE.setMaximumPoolSize(Runtime.getRuntime().availableProcessors() * Integer.parseInt(Config.getConfig("thread_scale")));
        SERVICE.allowCoreThreadTimeOut(false);
        SERVICE.setKeepAliveTime(10, TimeUnit.MINUTES);
        SERVICE.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
    }

    /**
     * Method for dispatching a task to the service executor
     * @param task the runnable task which will be executed in a seperate thread
     */
    public static void dispatch(final Runnable task) {
        SERVICE.submit(task);
    }

    /**
     * Method to shut down the service executor and stop the scheduler
     */
    public static void shutdownService() {
        SERVICE.shutdown();
        if (!SERVICE.isShutdown()) {
            SERVICE.shutdownNow();
        }
        if (SCHEDULER.isStarted()) {
            SCHEDULER.stop();
        }
    }

    /**
     * Method to schedule a cron task
     * @param timePattern defines the frequence of the task
     * @param name defines a unique string
     * @param task defines the runnable task
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
     * Method to deschedule a cron task
     * @param name defines the unique string to find the task
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
     * Method to force a cron task
     * @param name defines the unique string to find the task
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
     * Method to get the list of scheduled tasks
     * @return a list of {@link Entry} objects
     */
    public static ConcurrentLinkedQueue<Entry> getTaskList() {
        return TASKS;
    }

    /**
     * Method to get the thread pool executor
     * @return the used {@link ThreadPoolExecutor} from {@link Executors#newFixedThreadPool(int)}
     */
    public static ThreadPoolExecutor getThreadPool() {
        return SERVICE;
    }

    /**
     * Method to get the thread factory
     * @return the used {@link ThreadFactory} for creating the threads
     */
    public static ThreadFactory getThreadFactory() {
        return SERVICE.getThreadFactory();
    }

    /**
     * Method to get information about the current status of the service executor
     */
    public static void logStatus() {
        LOGGER.info("\nSize [Current {}, Maximum \033[0;33m{}\033[0m, Peak \033[0;31m{}\033[0m] " +
                        "- Tasks [Total {}, Completed \033[0;32m{}\033[0m, Active \033[0;34m{}\033[0m]"
                , SERVICE.getPoolSize(), SERVICE.getMaximumPoolSize(), SERVICE.getLargestPoolSize()
        , SERVICE.getTaskCount(), SERVICE.getCompletedTaskCount(), SERVICE.getActiveCount());
    }
}
