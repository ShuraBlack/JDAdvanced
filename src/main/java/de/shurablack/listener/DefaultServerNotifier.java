package de.shurablack.listener;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.shurablack.core.util.ServerUtil;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostCountEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 * This is a default/example class
 * <br><br>
 * The DefaultServerNotifier class is a listener that listens for various types of guild events
 * in a Discord chat server.
 * <br><br>
 * It extends the ListenerAdapter class from the JDA library, which provides a simple way to create listeners for Discord events.
 * </p>
 *
 * @version core-1.0.0
 * @date 12.07.2022
 * @author ShuraBlack
 */
public class DefaultServerNotifier extends ListenerAdapter {

    /** Class Logger */
    private static final Logger LOGGER = LogManager.getLogger(DefaultServerNotifier.class);

    /** Discord Server webhook URL */
    private final String webHookLink;

    /** Join messages */
    private List<String> joinMessage;

    /** Leave messages */
    private List<String> leaveMessage;

    /** Flags for in-/activ events */
    private final boolean join, leave, ban, unban, boost;

    /**
     * Standard Contructor.
     * <br><br>
     * Specify what events should be send
     */
    public DefaultServerNotifier(
            String webHookLink,
            List<String> joinMessage,
            List<String> leaveMessage,
            boolean join,
            boolean leave,
            boolean ban,
            boolean unban,
            boolean boost
    ) {
        this.webHookLink = webHookLink;
        this.joinMessage = joinMessage;
        this.leaveMessage = leaveMessage;
        this.join = join;
        this.leave = leave;
        this.ban = ban;
        this.unban = unban;
        this.boost = boost;
    }

    /**
     * Standard Contructor.
     * <br><br>
     * Specify what events should be send
     */
    public DefaultServerNotifier(
            String webHookLink,
            boolean join,
            boolean leave,
            boolean ban,
            boolean unban,
            boolean boost
    ) {
        this.webHookLink = webHookLink;
        this.join = join;
        this.leave = leave;
        this.ban = ban;
        this.unban = unban;
        this.boost = boost;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (!join) {
            return;
        }
        final WebhookEmbedBuilder eb = createMessageTemplate(
                event.getUser(),
                (this.joinMessage == null ? "welcome to the server"
                        : this.joinMessage.get(ThreadLocalRandom.current().nextInt(0, this.joinMessage.size()))),
                ServerUtil.GREEN
        );
        ServerUtil.sendWebHookMessage(this.webHookLink, eb);
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (!leave) {
            return;
        }
        final WebhookEmbedBuilder eb = createMessageTemplate(
                event.getUser(),
                (this.joinMessage == null ? "see you later"
                        : this.leaveMessage.get(ThreadLocalRandom.current().nextInt(0, this.leaveMessage.size()))),
                ServerUtil.RED
        );
        ServerUtil.sendWebHookMessage(this.webHookLink, eb);
    }

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        if (!ban) {
            return;
        }

        final WebhookEmbedBuilder eb = createMessageTemplate(
                event.getUser(),
                "got banned",
                ServerUtil.BLUE
        );
        ServerUtil.sendWebHookMessage(this.webHookLink, eb);
    }

    @Override
    public void onGuildUnban(@NotNull GuildUnbanEvent event) {
        if (!unban) {
            return;
        }

        final WebhookEmbedBuilder eb = createMessageTemplate(
                event.getUser(),
                "got unbanned",
                ServerUtil.BLUE
        );
        ServerUtil.sendWebHookMessage(this.webHookLink, eb);
    }

    @Override
    public void onGuildUpdateBoostCount(@NotNull GuildUpdateBoostCountEvent event) {
        if (!boost) {
            return;
        }

        String msg = (event.getOldBoostCount() < event.getNewBoostCount()) ?
                String.format("Server boosts increased from %d to %d. Great Job", event.getOldBoostCount(), event.getNewBoostCount())
                : String.format("Server boosts decreased from %d to %d", event.getOldBoostCount(), event.getNewBoostCount());

        final WebhookEmbedBuilder eb = new WebhookEmbedBuilder()
                .setColor(ServerUtil.BOOST)
                .setDescription(msg);
        ServerUtil.sendWebHookMessage(this.webHookLink, eb);
    }

    /**
     * Creates a template message for webook
     * @param user the independent discord user
     * @param msg the description of the embed message
     * @param colorCode the color of the message
     * @return the builder
     */
    private WebhookEmbedBuilder createMessageTemplate(User user, String msg, int colorCode) {
        return new WebhookEmbedBuilder()
                .setAuthor(new WebhookEmbed.EmbedAuthor(user.getName(),user.getEffectiveAvatarUrl(),user.getEffectiveAvatarUrl()))
                .setColor(colorCode)
                .setDescription(user.getAsMention() + ", " + msg)
                .setTimestamp(OffsetDateTime.now());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        LOGGER.debug("JDA announced to be ready");
    }
}
