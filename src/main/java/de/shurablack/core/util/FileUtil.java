package de.shurablack.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * The FileUtil class is a utility class that provides helper methods for working with files.
 * <br><br>
 * It provides methods for loading properties, JSON, and image files
 * </p>
 *
 * @version core-1.0.0
 * @date 09.06.2023
 * @author ShuraBlack
 */
public class FileUtil {

    /** Class Logger */
    private static final Logger LOGGER = LogManager.getLogger(FileUtil.class);

    private FileUtil() { }

    /**
     * Loads the properties from a properties file
     * @param path the path to the properties file including its name
     * @return the Properties
     */
    public static Properties loadProperties(final String path) {
        final Properties properties = new Properties();

        final File propertiesFile = new File(path);
        try (final InputStream inputStream = new FileInputStream(propertiesFile)) {
            properties.load(inputStream);
            final String msg = String.format("Loaded properties <\u001b[32;1m%s\u001b[0m>", path);
            LOGGER.debug(msg);
        } catch (IOException e) {
            final String msg = String.format("Couldnt load properties file <%s>", propertiesFile.getName());
            LOGGER.error(msg);
        }
        return properties;
    }

    /**
     * Saves the properties object to a file
     * @param path the path and name where the file should be created
     * @param header the headline of the properties file (e.g. informations about the file)
     * @param properties the properties object which will be saved
     */
    public static void saveProperties(final String path, final String header, final Properties properties) {
        final StringBuilder data = new StringBuilder();
        data.append(header).append("\n");

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            data.append((String)entry.getKey()).append("=").append((String)entry.getValue());
        }

        final File file = new File(path);
        try (final OutputStream out = new FileOutputStream(file)) {
            out.write(data.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            final String msg = String.format("Missing permissions to save the properties file <%s>", path);
            LOGGER.error(msg);
        }
    }

    /**
     * This is a static method that loads the JSON from a JSON file
     * @param filePath the path to the properties file including its name
     * @return the {@link JSONObject} or null
     */
    public static JSONObject loadJson(final String filePath) {
        final StringBuilder contentBuilder = new StringBuilder();
        try (final BufferedReader br = new BufferedReader(new FileReader(filePath)))
        {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                contentBuilder.append(sCurrentLine).append("\n");
            }

            return new JSONObject(contentBuilder.toString());
        }
        catch (IOException e) {
            final String msg = String.format("Couldnt load json file <%s>", filePath);
            LOGGER.error(msg);
        } catch (JSONException e) {
            LOGGER.error("File couldnt be parsed into an JSONObject");
        }

        return null;
    }

    /**
     * This is a static method that loads an image from an image file
     * @param filePath the path to the properties file including its name
     * @return the {@link Image} or null
     */
    public static Image loadImage(final String filePath) {
        try {
            return ImageIO.read(new File(filePath));
        } catch (IOException e) {
            final String msg = String.format("Couldnt load image file <%s>", filePath);
            LOGGER.error(msg);
        }
        return null;
    }
}
