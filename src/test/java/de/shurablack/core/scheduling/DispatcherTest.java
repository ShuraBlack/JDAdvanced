package de.shurablack.core.scheduling;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DispatcherTest {

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
}
