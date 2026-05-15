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
 * Annotation to mark a class as a slash command.
 * </p>
 *
 * <p>
 * This annotation is used to upload slash commands to the Discord API.
 * </p>
 *
 * @version core-1.2.0
 * @date 12.04.2026
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SlashCommand {
    String guildId() default "GLOBAL";
    String name();
    String description();
    LocalizedText[] localizedNames() default {};
    LocalizedText[] localizedDescriptions() default {};
    SubCommand[] subCommands() default {};
    Permission[] defaultMemberPermissions() default {};
    InteractionContextType[] contextTypes() default {InteractionContextType.GUILD, InteractionContextType.BOT_DM};
    IntegrationType[] integrationTypes() default {IntegrationType.GUILD_INSTALL};
    boolean nsfw() default false;
}
