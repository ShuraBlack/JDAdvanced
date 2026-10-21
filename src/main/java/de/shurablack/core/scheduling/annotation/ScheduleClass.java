package de.shurablack.core.scheduling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Marks a class that contains {@link Schedule} methods, making it discoverable by
 * {@link de.shurablack.core.scheduling.Dispatcher#discoverCronTasks() Dispatcher#discoverCronTasks()}.
 * The class must be inside the packages configured for the classpath scan.
 * </p>
 *
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScheduleClass { }
