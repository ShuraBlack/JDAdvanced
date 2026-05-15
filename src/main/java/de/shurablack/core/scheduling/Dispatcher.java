package de.shurablack.core.scheduling;

import de.shurablack.core.scheduling.annotation.Schedule;
import de.shurablack.core.scheduling.annotation.ScheduleClass;
import de.shurablack.core.util.Config;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import it.sauronsoftware.cron4j.InvalidPatternException;
import it.sauronsoftware.cron4j.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(Dispatcher.class);

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
     * and a maximum pool size of the available processors multiplied by the thread scale from the config.
     */
    public static void start() {
        if (SERVICE != null) {
            return;
        }

        SERVICE = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        SERVICE.setCorePoolSize(Config.getConfigAsInt("dispatcher_thread_core", Runtime.getRuntime().availableProcessors()));
        SERVICE.setMaximumPoolSize(Config.getConfigAsInt("dispatcher_thread_max", Runtime.getRuntime().availableProcessors() * 2));
        SERVICE.allowCoreThreadTimeOut(false);
        SERVICE.setKeepAliveTime(10, TimeUnit.MINUTES);
        SERVICE.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());

        final String msg = String.format("Started Service Executor with <\u001b[32;1m%d\u001b[0m> Threads", SERVICE.getMaximumPoolSize());
        LOGGER.info(msg);
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
     * @param timePattern defines the frequency of the task
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

    public static void discoverCronTasks() {
        try (ScanResult result = new ClassGraph().enableAllInfo().acceptPackages("").scan()) {
            ClassInfoList classes = result.getClassesWithAnnotation(ScheduleClass.class);

            for (ClassInfo info : classes) {
                Class<?> workerClass = info.loadClass();

                for (Method method : workerClass.getDeclaredMethods()) {
                    if (!method.isAnnotationPresent(Schedule.class)) {
                        continue;
                    }

                    Schedule schedule = method.getAnnotation(Schedule.class);
                    if (schedule == null) {
                        continue;
                    }
                    // method must be static and without parameter
                    if (!Arrays.stream(method.getParameterTypes()).allMatch(p -> p.equals(Void.TYPE))) {
                        LOGGER.error("Method <\u001b[31m%s\u001b[0m> in class <\u001b[31m%s\u001b[0m> must be static and without parameter", method.getName(), workerClass.getName());
                        continue;
                    }
                    String timePattern = schedule.pattern();
                    String name = schedule.name();
                    Runnable task = () -> {
                        try {
                            method.invoke(null);
                        } catch (Exception e) {
                            LOGGER.error("Failed to execute scheduled task <\u001b[31m%s\u001b[0m> in class <\u001b[31m%s\u001b[0m>", name, workerClass.getName(), e);
                        }
                    };

                    scheduleCronTask(timePattern, name, task);
                }
            }
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
