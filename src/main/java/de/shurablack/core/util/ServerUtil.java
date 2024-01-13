package de.shurablack.core.util;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.shurablack.core.scheduling.Dispatcher;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
}
