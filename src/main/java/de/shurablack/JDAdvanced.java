package de.shurablack;

import de.shurablack.core.builder.InitException;
import de.shurablack.core.builder.JDAUtilBuilder;
import de.shurablack.core.builder.ShardUtilBuilder;
import de.shurablack.core.scheduling.Dispatcher;
import de.shurablack.core.util.AssetPool;
import de.shurablack.core.util.ClassScan;
import de.shurablack.core.util.Config;
import de.shurablack.core.util.LocalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Entry point of the library. {@link #init()} must run once before a bot is created via the
 * {@link JDAUtilBuilder} or the {@link ShardUtilBuilder}.
 * </p>
 * <pre>{@code
 * JDAdvanced.init("de.mybot");
 * JDAUtil util = JDAUtilBuilder.createDefault().build();
 * }</pre>
 *
 * @author ShuraBlack
 */
public final class JDAdvanced {

    /** Class Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(JDAdvanced.class);

    /** Flag that indicates whether {@link #init()} has completed */
    private static volatile boolean INIT = false;

    private JDAdvanced() { }

    /**
     * Same as {@link #init()}, but restricts the classpath scans (event workers, cron tasks, commands)
     * to the given packages and their sub packages. Scanning only your own packages speeds up the startup
     * @param basePackages the packages of your bot, e.g. "de.mybot"; none scans the whole classpath
     * @throws InitException if the initialization fails, see {@link #init()}
     */
    public static void init(final String... basePackages) {
        ClassScan.setPackages(basePackages);
        init();
    }

    /**
     * Initializes the library: loads the "config.properties" file from the working directory, starts the
     * {@link Dispatcher} and initializes the {@link AssetPool} and {@link LocalData}.
     * The classpath scans cover the whole classpath unless restricted via {@link #init(String...)}
     * @throws InitException if the config file or its access_token is missing, or the LocalData folder can't be created
     */
    public static void init() {
        LOGGER.info("JDAdvanced initialization started");
        Config.loadConfig();
        Dispatcher.start();
        AssetPool.init();
        LocalData.init();
        LOGGER.info("JDAdvanced initialization completed");
        INIT = true;
    }

    /** @return true if {@link #init()} has completed successfully */
    public static boolean isInitialized() {
        return INIT;
    }
}
