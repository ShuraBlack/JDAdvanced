package de.shurablack.core.scheduling.sample;

import de.shurablack.core.scheduling.annotation.Schedule;
import de.shurablack.core.scheduling.annotation.ScheduleClass;

import java.util.concurrent.atomic.AtomicInteger;

@ScheduleClass
public class CronTasks {

    public static final AtomicInteger RUNS = new AtomicInteger();

    // Once a year, so it never fires during the tests
    @Schedule(pattern = "0 0 1 1 *", name = "valid")
    static void valid() {
        RUNS.incrementAndGet();
    }

    @Schedule(pattern = "0 0 1 1 *", name = "notStatic")
    void notStatic() { }

    @Schedule(pattern = "0 0 1 1 *", name = "withParameter")
    static void withParameter(String value) { }
}
