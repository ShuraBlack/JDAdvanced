package de.shurablack.core.event.annotation.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;

public @interface ContextCommand {
    String guildId() default "GLOBAL";
    Command.Type type();
    String name();
    LocalizedText[] localizedNames() default {};
    Permission[] defaultMemberPermissions() default {};
    InteractionContextType[] contextTypes() default {InteractionContextType.GUILD, InteractionContextType.BOT_DM};
    IntegrationType[] integrationTypes() default {IntegrationType.GUILD_INSTALL};
    boolean nsfw() default false;
}
