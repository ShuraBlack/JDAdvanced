package de.shurablack.core.util;

import de.shurablack.core.builder.InitException;
import de.shurablack.JDAdvanced;

import java.io.File;
import java.util.Properties;
import java.util.function.Function;

/**
 * <p>
 * Read access to the configuration properties of the "config.properties" file in the working directory.
 * <br><br>
 * {@link JDAdvanced#init()} loads the file automatically. Besides the mandatory "access_token" for the
 * {@link net.dv8tion.jda.api.JDA} login, it holds optional settings like "dispatcher_thread_max".
 * <br>
 * Example:
 * </p>
 * <pre>{@code
 * // Load the configuration properties from the "config.properties" file
 * Config.loadConfig();
 *
 * // Get the value of the "access_token" property
 * String accessToken = Config.getConfig("access_token");
 *
 * // Use the access token to connect to a service
 * connectToService(accessToken);
 * }</pre>
 *
 * @version core-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class Config {

    /**
     * The properties loaded from the "config.properties" file.
     * <br><br>
     * Empty until {@link #loadConfig()} is called, which replaces it with the content of the file.
     */
    private static Properties CONFIGS = new Properties();

    /**
     * Returns the value of a configuration property.
     * @param name the name of the property
     * @return the value or null if the property doesn't exist
     */
    public static String getConfig(final String name) {
        return CONFIGS.getProperty(name);
    }

    /**
     * Returns the value of a configuration property parsed as {@code long}.
     * @param name the name of the property
     * @param defaultValue the value returned if the property doesn't exist
     * @return the parsed value or {@code defaultValue}
     * @throws NumberFormatException if the value isn't a valid {@code long}
     */
    public static long getConfigAsLong(final String name, final long defaultValue) {
        final String value = CONFIGS.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        return Long.parseLong(value);
    }

    /**
     * Returns the value of a configuration property parsed as {@code int}.
     * @param name the name of the property
     * @param defaultValue the value returned if the property doesn't exist
     * @return the parsed value or {@code defaultValue}
     * @throws NumberFormatException if the value isn't a valid {@code int}
     */
    public static int getConfigAsInt(final String name, final int defaultValue) {
        final String value = CONFIGS.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    /**
     * Loads the configuration properties from the file "config.properties" in the working directory,
     * replacing any previously loaded properties.
     * @throws InitException if the file or the "access_token" property is missing
     */
    public static void loadConfig() {
        final File file = new File("config.properties");
        if (!file.exists()) {
            throw new InitException("JDAdvanced misses the config.properties file");
        }

        CONFIGS = FileUtil.loadProperties("config.properties");

        if (!CONFIGS.containsKey("access_token")) {
            throw new InitException("The config.properties misses a discord access_token");
        }
    }
}
