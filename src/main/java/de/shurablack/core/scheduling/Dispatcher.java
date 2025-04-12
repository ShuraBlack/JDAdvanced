package de.shurablack.core.scheduling;

import it.sauronsoftware.cron4j.InvalidPatternException;
import it.sauronsoftware.cron4j.Scheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

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
    private static final ThreadPoolExecutor SERVICE = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /** A map for tracking scheduled tasks*/
    private static final Map<String, String> TASKS = new ConcurrentHashMap<>();

    private Dispatcher() { }

    /**
     * Method for dispatching a task to the service executor
     * @param task the runnable task which will be executed in a seperate thread
     */
    public static void dispatch(final Runnable task) {
        SERVICE.submit(task);
    }

    /**
     * Method to shutdown the service executor and stop the scheduler
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
            TASKS.put(name,SCHEDULER.schedule(timePattern, task));
        } catch (InvalidPatternException e) {
            LOGGER.error(String.format("Invalid pattern in Task scheduling <\u001b[31m%s\u001b[0m>", timePattern),e);
        }
    }

    /**
     * Method to deschedule a cron task
     * @param name defines the unique string to find the task
     */
    public static void descheduleCronTask(final String name) {
        final String taskID = TASKS.remove(name);
        if (taskID == null) {
            return;
        }
        SCHEDULER.deschedule(taskID);
        final String msg = String.format("Descheduled Task <\u001b[32;1m%s\u001b[0m>", name);
        LOGGER.info(msg);
    }

    /**
     * Method to get the map of scheduled tasks
     * @return a map with unique string as key and schedular id as value
     */
    public static Map<String, String> getTasks() {
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
}
