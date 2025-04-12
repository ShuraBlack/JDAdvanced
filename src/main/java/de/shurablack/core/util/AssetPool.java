package de.shurablack.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static de.shurablack.core.util.FileUtil.loadProperties;

/**
 * <p>
 * The AssetPool class is a utility class that provides a pool of assets, such as images,
 * that can be accessed by their names.
 * <br><br>
 * It provides methods for initializing the pool, adding and removing assets, and retrieving assets from the pool.
 * </p>
 *
 * @version core-1.0.0
 * @date 09.06.2023
 * @author ShuraBlack
 */
public class AssetPool {

    /** Class Logger */
    private static final Logger LOGGER = LogManager.getLogger(AssetPool.class);

    /** Map object for storing the data */
    private static final Map<String, String> ASSETS = new ConcurrentHashMap<>();

    /** Map object for storing images */
    private static final Map<String, BufferedImage> IMAGE_ASSETS = new ConcurrentHashMap<>();

    /** Name of assets file*/
    private static final String ASSETS_FILE = "assets.properties";

    private AssetPool() { }

    /**
     * This is a static method that initializes the asset pool by loading the assets from a properties file
     */
    public static void init() {
        final File file = new File(ASSETS_FILE);
        if (file.exists()) {
            Properties properties = loadProperties(ASSETS_FILE);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                if (((String) entry.getKey()).startsWith("url")) {
                    ASSETS.put((String) entry.getKey(), (String) entry.getValue());
                } else if (((String) entry.getKey()).startsWith("img")) {
                    final File reference = new File((String) entry.getValue());
                    if (!file.exists()) {
                        LOGGER.info(String.format("Couldnt find file <%s>", entry.getValue()));
                        continue;
                    }
                    try {
                        final BufferedImage image = ImageIO.read(reference);
                        IMAGE_ASSETS.put((String) entry.getKey(), image);
                    } catch (IOException e) {
                        LOGGER.error(String.format("Couldnt load image <%s>", entry.getValue()));
                    }
                }
            }
            return;
        }
        try (final OutputStream out = new FileOutputStream(file)) {
            final String msg = "# Add channel with <url_name/file_name>=<source>\n" +
                    "# The source can be a path or link to an image";
            out.write(msg.getBytes());
            out.flush();
        } catch (FileNotFoundException e) {
            LOGGER.error("Missing file <assets.properties>");
        } catch (IOException e) {
            LOGGER.error("Missing permissions to read the assets.properties");
        }

        Properties properties = loadProperties(ASSETS_FILE);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            ASSETS.put((String) entry.getKey(), (String) entry.getValue());
        }
    }

    /**
     * This is a static method that removes an asset from the pool
     * @param name the name of the asset
     * @return true if the asset got successfully removed
     */
    public static boolean remove(final String name) {
        return ASSETS.remove(name) != null || IMAGE_ASSETS.remove(name) != null;
    }

    /**
     * This is a static method that adds an asset to the pool.
     * Local images need to be specified with an "img" and links with an "url"
     * @param name the name of the asset
     * @param source the source of the asset (URL or path)
     * @return true if the asset got successfully loaded
     */
    public static boolean add(final String name, final String source) {
        if (name.startsWith("file")) {
            final File file = new File(source);
            if (!file.exists()) {
                return false;
            }
            try {
                final BufferedImage image = ImageIO.read(file);
                IMAGE_ASSETS.put(name, image);
            } catch (IOException e) {
                LOGGER.error(String.format("Couldnt load image <%s>", source));
                return false;
            }
        }
        if (name.startsWith("url")) {
            return ASSETS.put(name, source) == null;
        }
        return false;
    }

    /**
     * This is a static method that retrieves an asset from the pool
     * @param name the name of the asset
     * @return the asset
     */
    public static String get(final String name) {
        return ASSETS.getOrDefault(name, "");
    }

    /**
     * This is a static method that retrieves an asset from the pool
     * @param name the name of the asset
     * @return the asset image
     */
    public static BufferedImage getFile(final String name) {
        return IMAGE_ASSETS.get(name);
    }
}
