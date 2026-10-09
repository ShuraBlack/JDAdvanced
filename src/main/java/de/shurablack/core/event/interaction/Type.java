package de.shurablack.core.event.interaction;

/**
 * <p>
 * Defines the types of events which can be handled by the {@link de.shurablack.core.event.handler.EventHandler}.
 * Identifiers are unique per type. "Global" types are used outside of guilds (e.g. DMs), "guild" types inside of guilds.
 * </p>
 *
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public enum Type {
    /** Button click, identified by the custom ID */
    BUTTON,
    /** Slash command outside of a guild, identified by the command name */
    GLOBAL_SLASH,
    /** Slash command in a guild, identified by the command name */
    GUILD_SLASH,
    /** User context menu command outside of a guild, identified by the command name */
    GLOBAL_USER_CONTEXT,
    /** User context menu command in a guild, identified by the command name */
    GUILD_USER_CONTEXT,
    /** Message context menu command outside of a guild, identified by the command name */
    GLOBAL_MSG_CONTEXT,
    /** Message context menu command in a guild, identified by the command name */
    GUILD_MSG_CONTEXT,
    /** Modal submission, identified by the modal ID */
    MODAL,
    /** Message in a private channel, identified by the prefix and command (first word) */
    PRIVATE_CHANNEL,
    /** Message in a guild channel, identified by the prefix and command (first word) */
    PUBLIC_CHANNEL,
    /** Reaction in a private channel, identified by the identifier mapped to the message */
    PRIVATE_REACTION,
    /** Reaction in a guild channel, identified by the identifier mapped to the message */
    PUBLIC_REACTION,
    /** Entity select menu, identified by the custom ID */
    ENTITY_SELECTION,
    /** String select menu, identified by the custom ID */
    STRING_SELECTION,
    /** Auto complete of a slash command option, identified by the command name */
    AUTO_COMPLETE;

    /**
     * Maps the name of an {@link de.shurablack.core.event.EventWorker EventWorker} process method to its type,
     * used for {@link de.shurablack.core.event.annotation.EventProcess EventProcess} methods
     * @param functionName the method name, e.g. "processButtonEvent"
     * @return the matching type, null if the name isn't a process method
     */
    public static Type fromFunctionName(String functionName) {
        switch (functionName) {
            case "processButtonEvent": return BUTTON;
            case "processGlobalSlashEvent": return GLOBAL_SLASH;
            case "processGuildSlashEvent": return GUILD_SLASH;
            case "processModalEvent": return MODAL;
            case "processPrivateChannelEvent": return PRIVATE_CHANNEL;
            case "processPrivateReactionEvent": return PRIVATE_REACTION;
            case "processPublicChannelEvent": return PUBLIC_CHANNEL;
            case "processPublicReactionEvent": return PUBLIC_REACTION;
            case "processStringSelectEvent": return STRING_SELECTION;
            case "processEntitySelectEvent": return ENTITY_SELECTION;
            case "processGlobalUserContextEvent": return GLOBAL_USER_CONTEXT;
            case "processGuildUserContextEvent": return GUILD_USER_CONTEXT;
            case "processGlobalMessageContextEvent": return GLOBAL_MSG_CONTEXT;
            case "processGuildMessageContextEvent": return GUILD_MSG_CONTEXT;
            case "processAutoCompleteEvent": return AUTO_COMPLETE;
        };
        return null;
    }
}
