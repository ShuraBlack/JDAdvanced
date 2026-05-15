package de.shurablack.core.event.annotation.command;

import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * <p>
 * Annotation to set localized texts in SlashCommands.
 * </p>
 *
 *
 * @version core-1.2.0
 * @date 12.04.2026
 * @author ShuraBlack
 */
public @interface LocalizedText {
    DiscordLocale locale() default DiscordLocale.ENGLISH_US;
    String value();
}
