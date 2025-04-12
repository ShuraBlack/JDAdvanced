package de.shurablack.core.event.annotation;

import de.shurablack.core.event.interaction.Type;

import java.lang.annotation.*;

/**
 * <p>
 * Annotation to define the event process for a method.
 * </p>
 *
 * <p>
 * This annotation is used to specify the cooldowns and allowed channels for an event process.
 * </p>
 *
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventProcess {
    String identifier();
    int userCooldown() default -1;
    int globalCooldown() default -1;

    /**
     * Restricted channels should only be used in the context of regular messageReceive events.
     * @return an array of channel IDs that restrict the event to specific channels
     */
    String[] restrictedChannel() default {};
}
