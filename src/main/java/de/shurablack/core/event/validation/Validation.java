package de.shurablack.core.event.validation;

import de.shurablack.core.util.ServerUtil;
import de.shurablack.localization.Language;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Locale;

/**
 * Enum representing various validation states for interactions.
 * Each validation state has an associated message and can generate
 * a Discord embed message for user feedback.
 *
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
public enum Validation {
    /**
     * Validation state indicating the interaction is not available for bots.
     */
    IS_BOT("This interaction is not available for bots"),

    /**
     * Validation state indicating the interaction request might be malformed or outdated.
     */
    NO_EVENT("The interaction request might be malformed or outdated"),

    /**
     * Validation state indicating the command is not available in the current channel.
     */
    WRONG_CHANNEL("This command is not available in this channel"),

    /**
     * Validation state indicating the interaction is on cooldown.
     */
    ON_COOLDOWN("This interaction is on cooldown"),

    /**
     * Validation state indicating the user lacks the required permissions.
     */
    NO_PERMISSION("You don't have the permission to execute this interaction"),

    /**
     * Validation state indicating the interaction was successful.
     */
    SUCCESS(null);

    /**
     * The message associated with the validation state.
     */
    private final String message;

    /**
     * Constructor for the Validation enum.
     *
     * @param message The message associated with the validation state.
     */
    Validation(String message) {
        this.message = message;
    }

    /**
     * Retrieves the message associated with the validation state.
     *
     * @return The validation message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Generates a Discord embed message based on the validation state.
     *
     * @return A {@link MessageEmbed} containing the validation message.
     */
    public MessageEmbed getEmbed() {
        return new EmbedBuilder()
                .setDescription(message)
                .setColor(ServerUtil.RED)
                .build();
    }

    /**
     * Retrieves the message in the given language. Translations are looked up via {@link Language}
     * with the key "validation.&lt;name&gt;" (e.g. "validation.on_cooldown"), the english message is the fallback.
     *
     * @param locale The discord locale of the user.
     * @return The translated or the english validation message.
     */
    public String getMessage(final DiscordLocale locale) {
        final String translated = Language.get(locale, "validation." + name().toLowerCase(Locale.ROOT));
        return translated.isEmpty() ? message : translated;
    }

    /**
     * Generates a Discord embed message in the given language.
     *
     * @param locale The discord locale of the user.
     * @return A {@link MessageEmbed} containing the translated validation message.
     * @see #getMessage(DiscordLocale)
     */
    public MessageEmbed getEmbed(final DiscordLocale locale) {
        return new EmbedBuilder()
                .setDescription(getMessage(locale))
                .setColor(ServerUtil.RED)
                .build();
    }
}