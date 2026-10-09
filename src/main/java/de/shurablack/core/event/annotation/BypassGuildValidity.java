package de.shurablack.core.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Marks an {@link EventProcess} method whose interaction skips the guild validity checks.
 * <br><br>
 * For events with a guild member the channel restriction and the admin bypass are skipped, so the event can be
 * triggered in every channel and administrators are still subject to the cooldowns. Required permissions are
 * still checked. A warning gets logged on discovery for every annotated method.
 * <br><br>
 * Only evaluated together with {@link EventProcess}, it has no effect on {@link ExtendedEventProcess} or
 * {@link de.shurablack.core.event.annotation.command.CommandHandler CommandHandler} interactions.
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
