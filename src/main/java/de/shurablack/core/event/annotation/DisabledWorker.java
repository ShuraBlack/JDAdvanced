package de.shurablack.core.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation to disable a worker.
 * </p>
 *
 * <p>
 * This annotation is used to mark a worker as disabled, preventing it from being processed.
 * </p>
 *
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DisabledWorker {
}
