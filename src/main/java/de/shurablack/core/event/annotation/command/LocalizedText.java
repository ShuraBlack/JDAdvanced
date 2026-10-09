package de.shurablack.core.event.annotation.command;

import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * <p>
 * A text in a specific language, used for the localized names and descriptions of
 * {@link SlashCommand}, {@link SubCommand}, {@link CommandOption} and {@link ContextCommand}.
 * </p>
 *
 * @version core-1.2.0
 * @date 12.04.2026
 * @author ShuraBlack
 */
public @interface LocalizedText {

    /**
     * @return the language of the text, {@link DiscordLocale#ENGLISH_US} by default
     */
    DiscordLocale locale() default DiscordLocale.ENGLISH_US;

    /**
     * @return the localized text
     */
    String value();
}
