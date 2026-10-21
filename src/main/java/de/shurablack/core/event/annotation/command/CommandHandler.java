package de.shurablack.core.event.annotation.command;

import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Marks a method as handler of a slash command, e.g. <code>/ping</code>, or of a sub command, e.g. <code>/music play</code>.
 * The declaring class gets discovered by {@link de.shurablack.core.event.interaction.InteractionSet#fromAnnotation()},
 * it doesn't have to extend {@link de.shurablack.core.event.EventWorker EventWorker}. Without an instance factory it
 * needs a no-argument constructor.
 * <br><br>
 * Parameters annotated with {@link Option} receive the option value, all others get injected from the event:
 * {@link net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent SlashCommandInteractionEvent},
 * {@link net.dv8tion.jda.api.entities.Member Member} (null outside of guilds),
 * {@link net.dv8tion.jda.api.entities.User User}, {@link net.dv8tion.jda.api.entities.Guild Guild} (null outside of guilds),
 * {@link net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion MessageChannelUnion} and
 * {@link net.dv8tion.jda.api.interactions.DiscordLocale DiscordLocale} (the locale of the user).
 * <br><br>
 * The parameters are validated against the {@link SlashCommand} declaration with the same name on discovery, a mismatch
 * throws an {@link de.shurablack.core.event.ImplementationException ImplementationException}. Without a declaration
 * only a warning gets logged. The handler gets registered for guilds and DMs with the full command name as identifier
 * (e.g. "music play"), so a sub command handler takes precedence over a worker registered for the plain command name.
 * </p>
 * <pre>{@code
 * @CommandHandler(command = "ping")
 * public void ping(SlashCommandInteractionEvent event) { ... }
 *
 * @CommandHandler(command = "music", sub = "play", userCooldown = 5)
 * public void play(Member member, @Option("title") String title) { ... }
 * }</pre>
 * Make the methods public if the instance can be a proxy (e.g. Spring beans with @Transactional).
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
     * @return the name of the {@link SubCommand}, empty (default) for commands without sub commands.
     *         Required if the declared command has sub commands
     */
    String sub() default "";

    /**
     * @return the cooldown per user in seconds, -1 (default) for none. Values below -1 throw an
     *         {@link IllegalArgumentException} on discovery
     */
    int userCooldown() default -1;

    /**
     * @return the cooldown for everyone in seconds, -1 (default) for none. Values below -1 throw an
     *         {@link IllegalArgumentException} on discovery
     */
    int globalCooldown() default -1;

    /**
     * @return the channel IDs the command or sub command is restricted to, empty (default) for every channel
     */
    String[] restrictedChannel() default {};

    /**
     * Permissions the member needs in the channel of the event (channel overrides count). Usage outside of guilds
     * (DMs) gets denied if any permission is required.
     * @return the required permissions, empty (default) for none
     */
    Permission[] requiredPermissions() default {};
}
