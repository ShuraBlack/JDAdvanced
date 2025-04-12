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
    STRING_SELECTION
}
