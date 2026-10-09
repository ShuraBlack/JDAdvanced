package de.shurablack.core.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AssetPoolTest {

    /** Tests run in target/test-workdir, see the surefire configuration */
    private static final File ASSETS_FILE = new File("assets.properties");

    @AfterEach
    void cleanUp() {
        ASSETS_FILE.delete();
        for (String name : new String[]{"url_test", "url_loaded", "img_logo", "file_logo", "img_missing"}) {
            AssetPool.remove(name);
        }
    }

    private static String image(Path dir, String name) throws IOException {
        final File file = dir.resolve(name).toFile();
        ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "png", file);
        return file.getAbsolutePath().replace(File.separatorChar, '/');
    }

    @Test
    void initCreatesAssetsFileIfNotExists() {
        AssetPool.init();
        assertTrue(ASSETS_FILE.exists());
    }

    @Test
    void initLoadsUrlsAndImages(@TempDir Path dir) throws IOException {
        Files.writeString(ASSETS_FILE.toPath(), String.join(System.lineSeparator(),
                "url_loaded=http://example.com",
                "img_logo=" + image(dir, "logo.png"),
                "file_logo=" + image(dir, "logo2.png"),
                "img_missing=" + dir.resolve("missing.png").toString().replace(File.separatorChar, '/')));

        AssetPool.init();

        assertEquals("http://example.com", AssetPool.get("url_loaded"));
        // Both prefixes count as image, the same as in add()
        assertNotNull(AssetPool.getFile("img_logo"));
        assertNotNull(AssetPool.getFile("file_logo"));
        assertNull(AssetPool.getFile("img_missing"));
    }

    @Test
    void addLoadsImages(@TempDir Path dir) throws IOException {
        assertTrue(AssetPool.add("img_logo", image(dir, "logo.png")));
        assertTrue(AssetPool.add("file_logo", image(dir, "logo2.png")));
        assertNotNull(AssetPool.getFile("img_logo"));
        assertNotNull(AssetPool.getFile("file_logo"));
    }

    @Test
    void addReturnsFalseForNonExistentFile() {
        assertFalse(AssetPool.add("img_nonexistent", "nonexistent.png"));
        assertFalse(AssetPool.add("file_nonexistent", "nonexistent.png"));
    }

    @Test
    void addLoadsUrlAssetSuccessfully() {
        assertTrue(AssetPool.add("url_test", "http://example.com"));
        assertEquals("http://example.com", AssetPool.get("url_test"));
    }

    @Test
    void addRejectsUnknownPrefix() {
        assertFalse(AssetPool.add("other", "http://example.com"));
    }

    @Test
    void removeDeletesExistingAsset() {
        AssetPool.add("url_test", "http://example.com");
        assertTrue(AssetPool.remove("url_test"));
        assertEquals("", AssetPool.get("url_test"));
    }

    @Test
    void removeReturnsFalseForNonExistentAsset() {
        assertFalse(AssetPool.remove("nonexistent"));
    }

    @Test
    void getReturnsEmptyStringForNonExistentAsset() {
        assertEquals("", AssetPool.get("nonexistent"));
    }

    @Test
    void getFileReturnsNullForNonExistentImageAsset() {
        assertNull(AssetPool.getFile("nonexistent"));
    }
}
