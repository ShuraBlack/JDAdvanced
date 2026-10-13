package de.shurablack.core.builder;

import net.dv8tion.jda.api.JDABuilder;

/**
 * <p>
 * Unchecked exception thrown when the initialization fails.
 * <br><br>
 * It is thrown when {@link de.shurablack.JDAdvanced#init()} fails (missing config.properties, missing access_token or
 * the properties folder of {@link de.shurablack.core.util.LocalData} can't be created) or when
 * {@link JDAUtilBuilder#create(JDABuilder, de.shurablack.core.event.handler.EventHandler)} or
 * {@link ShardUtilBuilder#create(net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder, de.shurablack.core.event.handler.EventHandler)}
 * is called before it.
 * </p>
 *
 * @version core-1.1.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class InitException extends RuntimeException {

    /**
     * @param message the reason of the failure
     */
    public InitException(final String message) {
        super(message);
    }
}
