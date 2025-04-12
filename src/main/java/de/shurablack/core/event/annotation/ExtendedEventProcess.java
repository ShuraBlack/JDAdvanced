package de.shurablack.core.event.annotation;

import de.shurablack.core.event.interaction.Type;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation to define the extended event process for a method.
 * </p>
 *
 * <p>
 * This annotation is used to specify the cooldowns, allowed channels, and type for an event process.
 * </p>
 *
 * <p>
 * It can only be used in conjunction with the {@link RedirectedProcess} annotation.
 * </p>
 *
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface ExtendedEventProcess {
    String identifier();
    Type type();
    int userCooldown() default -1;
    int globalCooldown() default -1;
    String[] restrictedChannel() default {};
}
