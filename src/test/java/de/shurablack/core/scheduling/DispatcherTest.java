package de.shurablack.core.scheduling;

import de.shurablack.core.scheduling.sample.CronTasks;
import de.shurablack.core.util.ClassScan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DispatcherTest {

    @AfterEach
    void cleanUp() {
        for (String name : new String[]{"valid", "notStatic", "withParameter", "manual"}) {
            Dispatcher.descheduleCronTask(name);
        }
        ClassScan.setPackages();
    }

    private static Set<String> taskNames() {
        return Dispatcher.getTaskList().stream().map(Entry::getName).collect(Collectors.toSet());
    }

    @Test
    void dispatchRunsAllTasksBeyondThreadCountAndSurvivesExceptions() throws InterruptedException {
        Dispatcher.start();
        int tasks = Runtime.getRuntime().availableProcessors() * 2 * 5;
        CountDownLatch done = new CountDownLatch(tasks);

        Dispatcher.dispatch(() -> { throw new IllegalStateException("expected test failure"); });
        for (int i = 0; i < tasks; i++) {
            Dispatcher.dispatch(() -> {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignored) { }
                done.countDown();
            });
        }

        assertTrue(done.await(10, TimeUnit.SECONDS), "tasks were discarded: " + done.getCount() + " never ran");
    }

    @Test
    void discoverCronTasksOnlySchedulesStaticParameterlessMethods() {
        ClassScan.setPackages("de.shurablack.core.scheduling.sample");
        Dispatcher.discoverCronTasks();

        assertEquals(Set.of("valid"), taskNames());

        final int before = CronTasks.RUNS.get();
        Dispatcher.forceCronTask("valid");
        assertEquals(before + 1, CronTasks.RUNS.get());
    }

    @Test
    void scheduleAndDescheduleCronTask() {
        Dispatcher.scheduleCronTask("0 0 1 1 *", "manual", () -> { });
        assertTrue(taskNames().contains("manual"));

        Dispatcher.descheduleCronTask("manual");
        assertFalse(taskNames().contains("manual"));
    }

    @Test
    void invalidPatternIsNotScheduled() {
        Dispatcher.scheduleCronTask("not a pattern", "manual", () -> { });
        assertFalse(taskNames().contains("manual"));
    }

    @Test
    void shutdownInterruptsHangingTasksAndAllowsRestart() throws InterruptedException {
        Dispatcher.start();
        final CountDownLatch started = new CountDownLatch(1);
        Dispatcher.dispatch(() -> {
            started.countDown();
            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(1));
            } catch (InterruptedException ignored) { }
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));

        Dispatcher.shutdownService();
        assertTrue(Dispatcher.getThreadPool().awaitTermination(1, TimeUnit.SECONDS), "hanging task wasnt interrupted");

        // A new executor gets created after the shutdown
        Dispatcher.start();
        final CountDownLatch ran = new CountDownLatch(1);
        Dispatcher.dispatch(ran::countDown);
        assertTrue(ran.await(5, TimeUnit.SECONDS));
    }
}
