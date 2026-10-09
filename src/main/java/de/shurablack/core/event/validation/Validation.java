package de.shurablack.core.event.validation;

import de.shurablack.core.util.ServerUtil;
import de.shurablack.localization.Language;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Locale;

/**
 * <p>
 * Result of the validation of an incoming event by the event handler.
 * <br><br>
 * Every failure state has an English message and can generate a red Discord embed, which the handler
 * sends as ephemeral error feedback for interactions. The messages can be translated via
 * {@link Language}, see {@link #getMessage(DiscordLocale)}.
 * </p>
 *
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
public enum Validation {
    /**
     * The event got triggered by a bot and bot requests are ignored.
     */
    IS_BOT("This interaction is not available for bots"),

    /**
     * No event is registered for the type and identifier.
     */
    NO_EVENT("The interaction request might be malformed or outdated"),

    /**
     * The channel isn't part of the channel restriction of the event.
     */
    WRONG_CHANNEL("This command is not available in this channel"),

    /**
     * The global cooldown or the cooldown of the user is still active.
     */
    ON_COOLDOWN("This interaction is on cooldown"),

    /**
     * The member lacks a required permission, or the event requires permissions and has no guild member (e.g. DMs).
     */
    NO_PERMISSION("You don't have the permission to execute this interaction"),

    /**
     * The event passed the validation. Has no message (null).
     */
    SUCCESS(null);

    /**
     * The English message of the validation state, null for {@link #SUCCESS}.
     */
    private final String message;

    /**
     * Constructor for the Validation enum.
     *
     * @param message the English message of the validation state, null for {@link #SUCCESS}
     */
    Validation(String message) {
        this.message = message;
    }

    /**
     * Retrieves the English message of the validation state.
     *
     * @return the English message, null for {@link #SUCCESS}
     */
    public String getMessage() {
        return message;
    }

    /**
     * Generates a red Discord embed with the English message as description.
     *
     * @return a {@link MessageEmbed} containing the validation message
     */
    public MessageEmbed getEmbed() {
        return new EmbedBuilder()
                .setDescription(message)
                .setColor(ServerUtil.RED)
                .build();
    }

    /**
     * Retrieves the message in the given language. Translations are looked up via {@link Language}
     * with the key "validation.&lt;name&gt;" (e.g. "validation.on_cooldown"), the English message is the fallback.
     *
     * @param locale the Discord locale of the user
     * @return the translated or the English validation message
     */
    public String getMessage(final DiscordLocale locale) {
        final String translated = Language.get(locale, "validation." + name().toLowerCase(Locale.ROOT));
        return translated.isEmpty() ? message : translated;
    }

    /**
     * Generates a red Discord embed with the message in the given language as description.
     *
     * @param locale the Discord locale of the user
     * @return a {@link MessageEmbed} containing the translated validation message
     * @see #getMessage(DiscordLocale)
     */
    public MessageEmbed getEmbed(final DiscordLocale locale) {
        return new EmbedBuilder()
                .setDescription(getMessage(locale))
                .setColor(ServerUtil.RED)
                .build();
    }
}