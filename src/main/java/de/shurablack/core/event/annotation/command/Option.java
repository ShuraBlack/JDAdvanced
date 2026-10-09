package de.shurablack.core.event.annotation.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Injects the value of a slash command option into a {@link CommandHandler} parameter.
 * Options which weren't provided are null, so primitive parameters are only allowed for required options.
 * <br><br>
 * Supported types (accepted option types in brackets):
 * String (STRING), Integer/int and Long/long (INTEGER), Double/double (NUMBER, INTEGER), Boolean/boolean (BOOLEAN),
 * User and Member (USER, MENTIONABLE), Role (ROLE, MENTIONABLE), IMentionable (MENTIONABLE, USER, ROLE),
 * GuildChannel and GuildChannelUnion (CHANNEL), Message.Attachment (ATTACHMENT).
 * <br><br>
 * Unsupported parameter types throw an {@link de.shurablack.core.event.ImplementationException ImplementationException}
 * on discovery. If the command is declared via {@link SlashCommand}, the option name, the option type and the
 * primitive rule are checked as well.
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
