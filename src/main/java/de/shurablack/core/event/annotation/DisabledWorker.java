package de.shurablack.core.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Disables a class for {@link de.shurablack.core.event.interaction.InteractionSet#fromAnnotation()}.
 * <br><br>
 * The class doesn't get instantiated and none of its interactions get registered, neither its
 * {@link EventProcess} / {@link RedirectedProcess} interactions nor its
 * {@link de.shurablack.core.event.annotation.command.CommandHandler CommandHandler} methods.
 * Command declarations ({@link de.shurablack.core.event.annotation.command.SlashCommand SlashCommand},
 * {@link de.shurablack.core.event.annotation.command.ContextCommand ContextCommand}) on the class are still uploaded.
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
