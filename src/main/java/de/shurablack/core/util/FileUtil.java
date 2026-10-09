package de.shurablack.core.util;

import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Static helper methods for working with files.
 * <br><br>
 * Provides loading and saving of properties files as well as loading JSON and image files.
 * Errors are logged instead of thrown; the methods return an empty or null result instead.
 * </p>
 *
 * @version core-1.0.0
 * @date 09.06.2023
 * @author ShuraBlack
 */
public class FileUtil {

    /** Class Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() { }

    /**
     * Loads the properties from a properties file.
     * @param path the path to the properties file including its name
     * @return the loaded properties, empty if the file couldn't be read (never null)
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
     * Saves the properties to a file (UTF-8), overwriting an existing file.
     * <br><br>
     * Writes the header line followed by the entries as <code>key=value</code>. Values aren't escaped,
     * so keys and values must be strings.
     * @param path the path and name of the file
     * @param header the first line of the file, should be a comment (e.g. "# Add role with &lt;name&gt;=&lt;role_id&gt;")
     * @param properties the properties which will be saved
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
     * Loads a JSON object from a file.
     * @param path the path to the JSON file including its name
     * @return the {@link JSONObject} or null if the file couldn't be read or parsed
     */
    public static JSONObject loadJson(final String path) {
        final StringBuilder contentBuilder = new StringBuilder();
        try (final BufferedReader br = new BufferedReader(new FileReader(path)))
        {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                contentBuilder.append(sCurrentLine).append("\n");
            }

            return new JSONObject(contentBuilder.toString());
        }
        catch (IOException e) {
            final String msg = String.format("Couldnt load json file <%s>", path);
            LOGGER.error(msg);
        } catch (JSONException e) {
            LOGGER.error("File couldnt be parsed into an JSONObject");
        }

        return null;
    }

    /**
     * Converts a file to a JDA {@link FileUpload}.
     * @param file the file to upload
     * @param name the file name shown in Discord
     * @return the FileUpload or null if the file doesn't exist or couldn't be opened
     */
    public static FileUpload toFileUpload(final File file, final String name) {
        if (!file.exists()) {
            return null;
        }

        try (final InputStream target = new FileInputStream(file)) {
            return FileUpload.fromData(target, name);
        } catch (IOException e) {
            LOGGER.error("Something went wrong, while opening the file stream", e);
        }
        return null;
    }

    /**
     * Loads an image from a file.
     * @param path the path to the image file including its name
     * @return the {@link Image} or null if the file couldn't be read or has no supported image format
     */
    public static Image loadImage(final String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            final String msg = String.format("Couldnt load image file <%s>", path);
            LOGGER.error(msg);
        }
        return null;
    }
}
