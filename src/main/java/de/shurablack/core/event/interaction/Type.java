package de.shurablack.core.event.interaction;

import de.shurablack.core.event.EventHandler;

/**
 * <p>
 * Defines available types of events, which can be handled by the {@link EventHandler}
 * </p>
 *
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public enum Type {
    BUTTON,
    GLOBAL_SLASH,
    GUILD_SLASH,
    GLOBAL_USER_CONTEXT,
    GUILD_USER_CONTEXT,
    GLOBAL_MSG_CONTEXT,
    GUILD_MSG_CONTEXT,
    MODAL,
    PRIVATE_CHANNEL,
    PUBLIC_CHANNEL,
    PRIVATE_REACTION,
    PUBLIC_REACTION,
    ENTITY_SELECTION,
    STRING_SELECTION;

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
            case "processGlobalMsgContextEvent": return GLOBAL_MSG_CONTEXT;
            case "processGuildMsgContextEvent": return GUILD_MSG_CONTEXT;
        };
        return null;
    }
}
