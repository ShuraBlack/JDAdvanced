package de.shurablack.core.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation to define the redirected process for a class.
 * </p>
 *
 * <p>
 * This annotation is used to specify the extended event processes for a class.
 * </p>
 *
 * <p>
 * It can only be used in conjunction with the {@link ExtendedEventProcess} annotation.
 * </p>
 *
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RedirectedProcess {
    ExtendedEventProcess[] value();
}
