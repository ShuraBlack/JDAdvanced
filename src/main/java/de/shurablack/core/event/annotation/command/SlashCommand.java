package de.shurablack.core.event.annotation.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Declares a slash command on a class, uploaded to Discord by
 * {@link de.shurablack.core.builder.CommandUploadBuilder CommandUploadBuilder}.
 * <br><br>
 * The command gets handled by {@link CommandHandler} methods, which are validated against this declaration,
 * or by an {@link de.shurablack.core.event.EventWorker EventWorker} with the command name as identifier.
 * If a declaration is invalid (e.g. options and sub commands on the same command), no command gets uploaded.
 * </p>
 * <pre>{@code
 * @SlashCommand(name = "music", description = "Music player", subCommands = {
 *      @SubCommand(name = "play", description = "Plays a song", options = {
 *          @CommandOption(type = OptionType.STRING, name = "title", description = "The song", required = true)
 *      }),
 *      @SubCommand(name = "stop", description = "Stops the music")
 * })
 * public class MusicCommand { ... }
 * }</pre>
 *
 * @version core-1.2.0
 * @date 12.04.2026
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SlashCommand {

    /**
     * @return "GLOBAL" (default) to upload the command globally, otherwise the ID of the guild the command
     *         gets uploaded to. Guilds the bot isn't in get skipped with a warning
     */
    String guildId() default "GLOBAL";

    /**
     * @return the name of the command, must not be blank
     */
    String name();

    /**
     * @return the description of the command, must not be blank
     */
    String description();

    /**
     * @return the localized names of the command, empty (default) for none
     */
    LocalizedText[] localizedNames() default {};

    /**
     * @return the localized descriptions of the command, empty (default) for none
     */
    LocalizedText[] localizedDescriptions() default {};

    /**
     * Sub commands of the command. Discord doesn't allow sub commands and {@link #options()} on the same command.
     * @return the sub commands, empty (default) for none
     */
    SubCommand[] subCommands() default {};

    /**
     * Options of a command without sub commands. Discord doesn't allow options and {@link #subCommands()} on the same command.
     * @return the options, empty (default) for none
     */
    CommandOption[] options() default {};

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
