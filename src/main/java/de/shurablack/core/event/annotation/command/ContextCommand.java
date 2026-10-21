package de.shurablack.core.event.annotation.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Declares a user or message context command on a class, uploaded to Discord by
 * {@link de.shurablack.core.builder.CommandUploadBuilder CommandUploadBuilder}.
 * <br><br>
 * The command gets handled by an {@link de.shurablack.core.event.EventWorker EventWorker} with the command name as
 * identifier, e.g. via {@link de.shurablack.core.event.EventWorker#processGuildUserContextEvent processGuildUserContextEvent}.
 * </p>
 *
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContextCommand {

    /**
     * @return "GLOBAL" (default) to upload the command globally, otherwise the ID of the guild the command
     *         gets uploaded to. Guilds the bot isn't in get skipped with a warning
     */
    String guildId() default "GLOBAL";

    /**
     * @return the type of the context command, {@link Command.Type#USER} or {@link Command.Type#MESSAGE}
     */
    Command.Type type();

    /**
     * @return the name of the command, must not be blank
     */
    String name();

    /**
     * @return the localized names of the command, empty (default) for none
     */
    LocalizedText[] localizedNames() default {};

    /**
     * @return the permissions a member needs to see and use the command by default, empty (default) for everyone.
     *         Guild administrators can change it in the server settings
     */
    Permission[] defaultMemberPermissions() default {};

    /**
     * @return where the command can be used, guilds and DMs with the bot by default
     */
    InteractionContextType[] contextTypes() default {InteractionContextType.GUILD, InteractionContextType.BOT_DM};

    /**
     * @return how the app has to be installed for the command to be available, guild install by default
     */
    IntegrationType[] integrationTypes() default {IntegrationType.GUILD_INSTALL};

    /**
     * @return true if the command is age-restricted, false (default) otherwise
     */
    boolean nsfw() default false;
}
