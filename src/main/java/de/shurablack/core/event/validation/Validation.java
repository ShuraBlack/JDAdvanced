package de.shurablack.core.event.validation;

import de.shurablack.core.util.ServerUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

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
}