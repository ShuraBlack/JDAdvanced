package de.shurablack.core.util;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.shurablack.core.scheduling.Dispatcher;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * <p>
 * The ServerUtil class provides utility methods for working with Discord servers.
 * <br><br>
 * It contains constants for various colors and functions to work with the {@link net.dv8tion.jda.api.JDA}
 * </p>
 *
 * @version core-1.0.0
 * @date 09.06.2023
 * @author ShuraBlack
 */
public class ServerUtil {

    public static final Logger GLOBAL_LOGGER = LogManager.getLogger("Global_Logger");

    /**
     * Color code for default use (grey)
     */
    public static final Integer DEFAULT_COLOR = 3289650;

    /**
     * Color code for positive use (green)
     */
    public static final Integer GREEN = 4437377;

    /**
     * Color code for negative use (red)
     */
    public static final Integer RED = 16729871;

    /**
     * Color code for neutral use (blue)
     */
    public static final Integer BLUE = 3375061;

    /**
     * Color code for special use (gold)
     */
    public static final Integer GOLD = 16766720;

    /**
     * Color code for boost use (pink)
     */
    public static final Integer BOOST = 16019183;

    private ServerUtil() { }

    /**
     * Checks if the {@link Member#getRoles()} include the given roleID
     * @param member the specified server member which will be checked
     * @param roleID the specified {@link net.dv8tion.jda.api.entities.Guild Guild} {@link Role#getId()}
     * @return true if he owns the given role
     */
    public static boolean hasRole(final Member member, final String roleID) {
        return member.getRoles().stream().anyMatch(role -> role.getId().equals(roleID));
    }

    /**
     * Sends a embeded message via the discrd webook URL
     * @param webHookLink the specified URL generated in the discord channel settings
     * @param builder the specified embed builder
     */
    public static void sendWebHookMessage(final String webHookLink, final WebhookEmbedBuilder builder) {

        final WebhookClientBuilder clientBuilder = new WebhookClientBuilder(webHookLink);

        clientBuilder.setThreadFactory(Dispatcher.getThreadFactory());

        clientBuilder.setWait(true);
        final WebhookClient client = clientBuilder.build();

        client.send(builder.build());
        client.close();
    }

    /**
     * Copies the permissions from one role to another in all channels
     * @param guild the specified {@link Guild}
     * @param sourceId the {@link Role#getId()} of the source role
     * @param targetId the {@link Role#getId()} of the target role
     */
    public static void copyPermissionsToAllChannel(Guild guild, String sourceId, String targetId) {
        Role source = guild.getRoleById(sourceId);
        Role target = guild.getRoleById(targetId);
        List<GuildChannel> channels = guild.getChannels();

        if (source == null || target == null) {
            GLOBAL_LOGGER.error("Role/s not found!");
            return;
        }

        GLOBAL_LOGGER.info("Copying permissions from {} to {}", source.getName(), target.getName());
        GLOBAL_LOGGER.info("Found {} channels", channels.size());

        int count = 1;

        for (GuildChannel channel : channels) {
            GLOBAL_LOGGER.info("[{}/{}] Channel: {}", count++, channels.size(), channel.getName());
            PermissionOverride override = channel.getPermissionContainer().getPermissionOverride(source);
            if (override != null) {

                try {
                    PermissionOverrideAction action = channel.getPermissionContainer().upsertPermissionOverride(target);
                    action = action.setAllowed(override.getAllowed());
                    action = action.setDenied(override.getDenied());
                    action.queue();

                    Thread.sleep(1000);
                } catch (Exception e) {
                    GLOBAL_LOGGER.error("Failed to copy permissions for channel {}", channel.getName());
                }
            }
        }
    }

    /**
     * Copies the permissions from one role to another in a specific channel
     * @param channel the specified {@link GuildChannel}
     * @param source the source {@link Role}
     * @param target the target {@link Role}
     */
    public static void copyPermissionsTo(GuildChannel channel, Role source, Role target) {
        PermissionOverride override = channel.getPermissionContainer().getPermissionOverride(source);
        if (override != null) {
            PermissionOverrideAction action = channel.getPermissionContainer().upsertPermissionOverride(target);
            action = action.setAllowed(override.getAllowed());
            action = action.setDenied(override.getDenied());
            action.queue();
            GLOBAL_LOGGER.info("Copied permissions for channel {}", channel.getName());
        } else {
            GLOBAL_LOGGER.error("Failed to copy permissions for channel {}", channel.getName());
        }
    }
}
