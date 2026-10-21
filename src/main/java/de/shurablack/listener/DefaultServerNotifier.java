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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 * Default listener that announces guild events as embeds through a Discord webhook.
 * <br><br>
 * Supported events are member join, member leave, ban, unban and boost count changes; each one can be
 * switched on or off in the constructor. Join and leave announcements use a random entry of the configured
 * message lists, or a built-in default text if no list is given. The messages are sent with
 * {@link ServerUtil#sendWebHookMessage(String, WebhookEmbedBuilder)}.
 * <br><br>
 * Can be used as is or serve as an example for a custom {@link ListenerAdapter}.
 * </p>
 *
 * @version core-1.0.0
 * @date 12.07.2022
 * @author ShuraBlack
 */
public class DefaultServerNotifier extends ListenerAdapter {

    /** Class Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerNotifier.class);

    /** Webhook URL of the channel the announcements are posted to */
    private final String webHookLink;

    /** Texts for join announcements, one is picked at random; {@code null} or empty uses a default text */
    private List<String> joinMessage;

    /** Texts for leave announcements, one is picked at random; {@code null} or empty uses a default text */
    private List<String> leaveMessage;

    /** Flags that enable the announcement of the respective event */
    private final boolean join, leave, ban, unban, boost;

    /**
     * Creates a notifier with custom join and leave texts.
     * <br><br>
     * The texts are appended to the mention of the user, e.g. {@code "@User, welcome to the server"}.
     * @param webHookLink the webhook URL of the target channel
     * @param joinMessage texts for join announcements; {@code null} or empty uses a default text
     * @param leaveMessage texts for leave announcements; {@code null} or empty uses a default text
     * @param join whether member joins are announced
     * @param leave whether members leaving (or being kicked) are announced
     * @param ban whether bans are announced
     * @param unban whether unbans are announced
     * @param boost whether boost count changes are announced
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
     * Creates a notifier that uses the default join and leave texts.
     * @param webHookLink the webhook URL of the target channel
     * @param join whether member joins are announced
     * @param leave whether members leaving (or being kicked) are announced
     * @param ban whether bans are announced
     * @param unban whether unbans are announced
     * @param boost whether boost count changes are announced
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

    /** Announces the new member with a random join text in green, if enabled. */
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (!join) {
            return;
        }
        final WebhookEmbedBuilder eb = createMessageTemplate(
                event.getUser(),
                pick(this.joinMessage, "welcome to the server"),
                ServerUtil.GREEN
        );
        ServerUtil.sendWebHookMessage(this.webHookLink, eb);
    }

    /** Announces the leaving member with a random leave text in red, if enabled. */
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (!leave) {
            return;
        }
        final WebhookEmbedBuilder eb = createMessageTemplate(
                event.getUser(),
                pick(this.leaveMessage, "see you later"),
                ServerUtil.RED
        );
        ServerUtil.sendWebHookMessage(this.webHookLink, eb);
    }

    /** Announces the banned user in blue, if enabled. */
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

    /** Announces the unbanned user in blue, if enabled. */
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

    /** Announces the old and new boost count, if enabled. */
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
     * Picks a random message
     * @param messages the configured messages, can be null or empty
     * @param fallback the message used if there are none
     * @return a random message or the fallback
     */
    private static String pick(final List<String> messages, final String fallback) {
        if (messages == null || messages.isEmpty()) {
            return fallback;
        }
        return messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
    }

    /**
     * Creates an embed with the user as author, the current time as timestamp and
     * {@code "<mention>, <msg>"} as description.
     * @param user the user the announcement is about
     * @param msg the text appended to the user mention
     * @param colorCode the RGB color of the embed
     * @return the prepared builder
     */
    private WebhookEmbedBuilder createMessageTemplate(User user, String msg, int colorCode) {
        return new WebhookEmbedBuilder()
                .setAuthor(new WebhookEmbed.EmbedAuthor(user.getName(),user.getEffectiveAvatarUrl(),user.getEffectiveAvatarUrl()))
                .setColor(colorCode)
                .setDescription(user.getAsMention() + ", " + msg)
                .setTimestamp(OffsetDateTime.now());
    }

    /** Logs on debug level that JDA is ready. */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        LOGGER.debug("JDA announced to be ready");
    }
}
