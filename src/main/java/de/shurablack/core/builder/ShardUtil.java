package de.shurablack.core.builder;

import de.shurablack.core.event.handler.EventHandler;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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
 * Wraps a {@link ShardManager} and provides the command-line interface of {@link BaseUtil}.
 * The console starts once all shards fired their {@link ReadyEvent}.
 * <br><br>
 * It can only be created via the {@link ShardUtilBuilder}. The ShardManager is stored statically,
 * so only one ShardUtil per application is supported.
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

    /** The Discord bot, null until a ShardUtil was created */
    private static ShardManager SHARD_MANAGER;

    /**
     * Stores the manager and registers a listener that calls {@link #onReady()} once the ready events
     * of all shards were received
     * @param manager the Discord bot
     * @param handler the handler for the Discord events
     */
    ShardUtil(final ShardManager manager, final EventHandler handler) {
        super(handler);
        SHARD_MANAGER = manager;
        SHARD_MANAGER.addEventListener(new ListenerAdapter() {
            /** Counts the ready events, atomic because the shards fire them concurrently */
            private final AtomicInteger readyShards = new AtomicInteger();
            @Override
            public void onReady(@NotNull ReadyEvent event) {
                final int ready = readyShards.incrementAndGet();
                LOGGER.info("Shard [{}/{}] is ready", event.getJDA().getShardInfo().getShardId()+1, SHARD_MANAGER.getShardsTotal());
                if (ready == SHARD_MANAGER.getShardsTotal()) {
                    ShardUtil.this.onReady();
                }
            }
        });
    }

    @Override
    void uploadCommands(final boolean onlyChanged) {
        CommandUploadBuilder.uploadInteractionCommands(SHARD_MANAGER, onlyChanged);
    }

    /**
     * @return the {@link ShardManager} created by
     * {@link ShardUtilBuilder#create(net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder, EventHandler)},
     * null if no ShardUtil was created yet
     */
    public static ShardManager getShardManager() {
        return SHARD_MANAGER;
    }
}
