package de.shurablack.core.util;

import java.io.File;
import java.util.Properties;

/**
 * <p>
 * The Config class is a simple Java class that provides a way to read configuration properties from a file.
 * <br><br>
 * Config will automatically fetch an config.properties file on {@link UtilBuilder#init()} and will provide
 * properties for the {@link net.dv8tion.jda.api.JDA}
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
     * A Properties object that stores the configuration properties that are loaded from the "config.properties" file.
     * <br><br>
     * This field is initially an empty Properties object, and it
     * is populated with the properties from the file when the loadConfig() method is called
     */
    private static Properties CONFIGS = new Properties();

    /**
     * This method takes a string representing the name of a configuration property,
     * and it returns the value of that property.
     * <br><br>
     * If the property does not exist, this method will return 'null'
     * @param name the specified property which will be checked
     * @return the corresponding property or null
     */
    public static String getConfig(final String name) {
        return CONFIGS.getProperty(name);
    }

    /**
     * Loads the configuration properties from a file named "config.properties" in the current directory.
     * <br><br>
     * If this file does not exist, the method will log an error message and terminate the program
     */
    public static void loadConfig() {
        final File file = new File("config.properties");
        if (!file.exists()) {
            ServerUtil.GLOBAL_LOGGER.error("JDAUtils misses the <\u001b[32;1mconfig.properties\u001b[0m> file and will be terminated");
            System.exit(1);
        }

        CONFIGS = FileUtil.loadProperties("config.properties");

        if (!CONFIGS.containsKey("access_token")) {
            ServerUtil.GLOBAL_LOGGER.error("The <\u001b[32;1mconfig.properties\u001b[0m> misses an discord access_token and will be terminated");
            System.exit(1);
        }
    }
}
