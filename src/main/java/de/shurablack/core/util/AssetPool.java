package de.shurablack.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static de.shurablack.core.util.FileUtil.loadProperties;

/**
 * <p>
 * Static pool of named assets: URL strings and loaded images, accessible by their names.
 * <br><br>
 * {@link #init()} loads the entries of the "assets.properties" file in the working directory. Keys starting
 * with "url" are stored as plain strings, keys starting with "img" are read as images from the given path.
 * Further assets can be added and removed at runtime; changes are not written back to the file.
 * Both stores are {@link ConcurrentHashMap}s, so the pool can be accessed from multiple threads.
 * </p>
 *
 * @version core-1.0.0
 * @date 09.06.2023
 * @author ShuraBlack
 */
public class AssetPool {

    /** Class Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(AssetPool.class);

    /** URL (string) assets by name */
    private static final Map<String, String> ASSETS = new ConcurrentHashMap<>();

    /** Image assets by name */
    private static final Map<String, BufferedImage> IMAGE_ASSETS = new ConcurrentHashMap<>();

    /** Name of the assets file, relative to the working directory */
    private static final String ASSETS_FILE = "assets.properties";

    private AssetPool() { }

    /**
     * Initializes the pool from the "assets.properties" file.
     * <br><br>
     * Entries with a key starting with "url" are stored as strings, entries starting with "img" are loaded
     * as images (failures are logged and skipped), all other keys are ignored. If the file doesn't exist,
     * it gets created with a comment header instead.
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
     * Removes an asset from the pool. URL assets are checked first; an image asset is only removed
     * if no URL asset with that name existed.
     * @param name the name of the asset
     * @return true if an asset with that name was removed
     */
    public static boolean remove(final String name) {
        return ASSETS.remove(name) != null || IMAGE_ASSETS.remove(name) != null;
    }

    /**
     * Adds an asset to the pool.
     * <br><br>
     * Names starting with "file" are loaded as image from the local path {@code source}, names starting
     * with "url" are stored as string. Note that, unlike {@link #init()}, the image prefix here is "file",
     * not "img".
     * @param name the name of the asset, its prefix determines the type
     * @param source the source of the asset (local path for images, URL otherwise)
     * @return true only if a new "url" asset was added (false if it replaced an existing one);
     *         image assets always return false, even when they were loaded successfully
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
     * Retrieves a URL (string) asset from the pool.
     * @param name the name of the asset
     * @return the asset or an empty string if it doesn't exist
     */
    public static String get(final String name) {
        return ASSETS.getOrDefault(name, "");
    }

    /**
     * Retrieves an image asset from the pool.
     * @param name the name of the asset
     * @return the image or null if it doesn't exist
     */
    public static BufferedImage getFile(final String name) {
        return IMAGE_ASSETS.get(name);
    }
}
