package de.shurablack.core.scheduling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Marks a method as a cron task with its scheduling pattern and name.
 * </p>
 *
 * <p>
 * The method must be static and parameterless, and the containing class must be annotated with
 * {@link ScheduleClass}. Such methods are scheduled by
 * {@link de.shurablack.core.scheduling.Dispatcher#discoverCronTasks() Dispatcher#discoverCronTasks()}.
 * </p>
 *
 * @version core-1.1.1
 * @date 13.04.2025
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Schedule {
    /** @return the cron4j scheduling pattern, e.g. "0 8 * * *" for every day at 08:00 */
    String pattern();
    /** @return the task name, used to deschedule or force the task via the Dispatcher */
    String name();
}
