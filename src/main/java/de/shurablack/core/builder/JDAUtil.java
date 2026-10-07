package de.shurablack.core.builder;

import de.shurablack.core.event.handler.EventHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
 * The JDAUtil class is a Java class that provides a way to interact with a Discord bot
 * , as well as using a command-line interface
 * <br>
 * It can only be created via the {@link UtilBuilder}.
 * <br>
 * </p>
 *
 * @see UtilBuilder UtilBuilder
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class JDAUtil extends BaseUtil {

    /** Presents the discord bot */
    private static JDA JDA;

    /**
     * This is the constructor for the JDAUtil class
     * @param jda the specified discord bot
     * @param handler the specified hadler object
     */
    JDAUtil(final JDA jda, final EventHandler handler) {
        super(handler);
        JDA = jda;
        JDA.addEventListener(new ListenerAdapter() {
            @Override
            public void onReady(ReadyEvent event) {
                JDAUtil.this.onReady();
            }
        });
    }

    @Override
    void uploadCommands(final boolean onlyChanged) {
        CommandUploadBuilder.uploadInteractionCommands(JDA, onlyChanged);
    }

    /**
     * @return the {@link JDA} object which needs be link with {@link UtilBuilder#create(JDABuilder, EventHandler)}
     */
    public static JDA getJDA() {
        return JDA;
    }
}
