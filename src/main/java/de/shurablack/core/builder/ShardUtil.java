package de.shurablack.core.builder;

import de.shurablack.core.event.handler.EventHandler;
import de.shurablack.core.util.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * This project is licensed under the Apache 2.0 license <br>
 * Copyright (c) 2023-2025 ShuraBlack<br>
 * For more information about the license, see <a href="https://www.apache.org/licenses/LICENSE-2.0">apache.org</a>
 * or check the <b>LICENSE</b> file in the project.
 * </p>
 * <br>
 *
 * <p>
 * The ShardUtil class is a Java class that provides a way to interact with a Discord bot
 * , as well as using a command-line interface
 * <br>
 * It can only be created via the {@link ShardUtilBuilder}.
 * <br>
 * </p>
 *
 * @see ShardUtilBuilder ShardUtilBuilder
 * @version core-1.1.0
 * @date 12.04.2025
 * @author ShuraBlack
 */
public class ShardUtil extends BaseUtil {

    /** Class Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(ShardUtil.class);

    /** ShardManager which presents the discord bot */
    private static ShardManager SHARD_MANAGER;

    /**
     * This is the constructor for the ShardUtil class
     * @param manager the specified discord bot
     * @param handler the specified hadler object
     */
    ShardUtil(final ShardManager manager, final EventHandler handler) {
        super(handler);
        SHARD_MANAGER = manager;
        SHARD_MANAGER.addEventListener(new ListenerAdapter() {
            /** Shards fire their ready events concurrently */
            private final AtomicInteger readyShards = new AtomicInteger();
            @Override
            public void onReady(@NotNull ReadyEvent event) {
                final int ready = readyShards.incrementAndGet();
                LOGGER.info("Shard [{}/{}] is ready", event.getJDA().getShardInfo().getShardId()+1, SHARD_MANAGER.getShardsTotal());
                if (ready == SHARD_MANAGER.getShardsTotal()) {
                    startCommandLine();
                }
            }
        });
    }

    @Override
    void uploadCommands() {
        JDA jda = JDABuilder.createLight(Config.getConfig("access_token"))
                .enableIntents(GatewayIntent.GUILD_MESSAGES)
                .disableIntents(
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_WEBHOOKS,
                        GatewayIntent.GUILD_INVITES,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_MESSAGE_TYPING,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGE_TYPING
                )
                .build();

        CommandUploadBuilder.uploadInteractionCommands(jda);
        jda.shutdown();
    }

    /**
     * @return the {@link ShardManager} object which needs be link with {@link ShardUtilBuilder#create(net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder, EventHandler)}
     */
    public static ShardManager getShardManager() {
        return SHARD_MANAGER;
    }
}
