package de.shurablack.core.builder;

import de.shurablack.core.event.handler.EventHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * This project is licensed under the Apache 2.0 license <br>
 * Copyright (c) 2023 ShuraBlack<br>
 * For more information about the license, see <a href="https://www.apache.org/licenses/LICENSE-2.0">apache.org</a>
 * or check the <b>LICENSE</b> file in the project.
 * </p>
 * <br>
 *
 * <p>
 * Wraps a single {@link JDA} instance and provides the command-line interface of {@link BaseUtil}.
 * The console starts with the first {@link ReadyEvent}.
 * <br><br>
 * It can only be created via the {@link JDAUtilBuilder}. The JDA instance is stored statically,
 * so only one JDAUtil per application is supported.
 * </p>
 *
 * @see JDAUtilBuilder JDAUtilBuilder
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class JDAUtil extends BaseUtil {

    /** The Discord bot, null until a JDAUtil was created */
    private static JDA JDA;

    /**
     * Stores the bot and registers a listener that calls {@link #onReady()} on its ready event
     * @param jda the Discord bot
     * @param handler the handler for the Discord events
     */
    JDAUtil(final JDA jda, final EventHandler handler) {
        super(handler);
        JDA = jda;
        JDA.addEventListener(new ListenerAdapter() {
            @Override
            public void onReady(@NotNull ReadyEvent event) {
                JDAUtil.this.onReady();
            }
        });
    }

    @Override
    void uploadCommands(final boolean onlyChanged) {
        CommandUploadBuilder.uploadInteractionCommands(JDA, onlyChanged);
    }

    /**
     * @return the {@link JDA} instance created by {@link JDAUtilBuilder#create(JDABuilder, EventHandler)},
     * null if no JDAUtil was created yet
     */
    public static JDA getJDA() {
        return JDA;
    }
}
