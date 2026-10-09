package de.shurablack.core.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Declares the interactions of an {@link de.shurablack.core.event.EventWorker EventWorker} on class level,
 * each one as an {@link ExtendedEventProcess}. The worker gets discovered by
 * {@link de.shurablack.core.event.interaction.InteractionSet#fromAnnotation()}.
 * <br><br>
 * If present, {@link EventProcess} annotations on the methods of the class are ignored.
 * </p>
 * <pre>{@code
 * @RedirectedProcess({
 *      @ExtendedEventProcess(identifier = "confirm", type = Type.BUTTON),
 *      @ExtendedEventProcess(identifier = "feedback", type = Type.MODAL, userCooldown = 30)
 * })
 * public class Worker extends EventWorker { ... }
 * }</pre>
 *
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RedirectedProcess {

    /**
     * @return the interactions of the worker
     */
    ExtendedEventProcess[] value();
}
