package de.shurablack.core.scheduling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation to mark a method as a scheduled task.
 * </p>
 *
 * <p>
 * This annotation is used to specify the scheduling pattern and name for a method that should be executed on a schedule.
 * </p>
 *
 * <p>
 * The containing class needs to annotate {@link ScheduleClass} to be discoverable.
 * </p>
 *
 * @version core-1.1.1
 * @date 13.04.2025
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Schedule {
    String pattern();
    String name();
}
