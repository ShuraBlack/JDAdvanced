package de.shurablack.core.event.annotation.command;

import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Marks a method as handler of a slash command, e.g. <code>/ping</code>, or of a sub command, e.g. <code>/music play</code>.
 * The class gets discovered by {@link de.shurablack.core.event.interaction.InteractionSet#fromAnnotation()}.
 * <br><br>
 * Parameters annotated with {@link Option} receive the option value, all others get injected from the event:
 * {@link net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent SlashCommandInteractionEvent},
 * {@link net.dv8tion.jda.api.entities.Member Member} (null outside of guilds),
 * {@link net.dv8tion.jda.api.entities.User User}, {@link net.dv8tion.jda.api.entities.Guild Guild},
 * {@link net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion MessageChannelUnion} and
 * {@link net.dv8tion.jda.api.interactions.DiscordLocale DiscordLocale}.
 * </p>
 * <pre>{@code
 * @CommandHandler(command = "ping")
 * void ping(SlashCommandInteractionEvent event) { ... }
 *
 * @CommandHandler(command = "music", sub = "play", userCooldown = 5)
 * void play(Member member, @Option("title") String title) { ... }
 * }</pre>
 *
 * @version core-1.1.0
 * @date 07.10.2026
 * @author ShuraBlack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandHandler {

    /**
     * @return the name of the {@link SlashCommand}
     */
    String command();

    /**
     * @return the name of the {@link SubCommand}, empty for commands without sub commands
     */
    String sub() default "";

    /**
     * @return the cooldown per user in seconds
     */
    int userCooldown() default -1;

    /**
     * @return the cooldown for everyone in seconds
     */
    int globalCooldown() default -1;

    /**
     * @return channel IDs the sub command is restricted to
     */
    String[] restrictedChannel() default {};

    /**
     * Permissions the member needs in the channel of the event. Denied outside of guilds (DMs)
     * @return the required permissions
     */
    Permission[] requiredPermissions() default {};
}
