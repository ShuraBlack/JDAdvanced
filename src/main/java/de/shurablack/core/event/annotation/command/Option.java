package de.shurablack.core.event.annotation.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Injects the value of a slash command option into a {@link SubCommandHandler} parameter.
 * Options which werent provided are null, primitive parameters are only allowed for required options.
 * <br><br>
 * Supported types (option type in brackets):
 * String (STRING), Integer/int and Long/long (INTEGER), Double/double (NUMBER, INTEGER), Boolean/boolean (BOOLEAN),
 * User and Member (USER, MENTIONABLE), Role (ROLE, MENTIONABLE), IMentionable (MENTIONABLE, USER, ROLE),
 * GuildChannel and GuildChannelUnion (CHANNEL), Message.Attachment (ATTACHMENT)
 * </p>
 *
 * @version core-1.1.0
 * @date 07.10.2026
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Option {

    /**
     * @return the name of the {@link CommandOption}
     */
    String value();
}
