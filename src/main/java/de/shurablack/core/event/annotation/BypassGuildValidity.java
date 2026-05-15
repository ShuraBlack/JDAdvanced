package de.shurablack.core.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation to mark a method to bypass guild validity checks.
 * </p>
 *
 * <p>
 * This annotation is used to indicate that the annotated method should bypass any guild validity checks
 * that would normally be applied. This can be useful for methods that need to operate regardless of guild context.
 * </p>
 *
 * @version core-1.1.0
 * @date 09.10.2025
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BypassGuildValidity {
}
