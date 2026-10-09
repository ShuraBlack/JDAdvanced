package de.shurablack.core.util;

import net.dv8tion.jda.api.utils.FileUpload;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilTest {

    @Test
    void loadPropertiesReturnsEmptyPropertiesForNonExistentFile() {
        Properties properties = FileUtil.loadProperties("nonexistent.properties");
        assertTrue(properties.isEmpty());
    }

    @Test
    void loadJsonReturnsNullForNonExistentFile() {
        JSONObject json = FileUtil.loadJson("nonexistent.json");
        assertNull(json);
    }

    @Test
    void toFileUploadReturnsNullForNonExistentFile() {
        FileUpload fileUpload = FileUtil.toFileUpload(new File("nonexistent.file"), "test");
        assertNull(fileUpload);
    }


    @Test
    void loadImageReturnsNullForNonExistentFile() {
        Image image = FileUtil.loadImage("nonexistent.png");
        assertNull(image);
    }

    @Test
    void savedPropertiesCanBeLoadedAgain(@TempDir Path dir) {
        final Properties properties = new Properties();
        properties.setProperty("first", "1");
        properties.setProperty("second", "2");
        final String path = dir.resolve("data.properties").toString();

        FileUtil.saveProperties(path, "# Header", properties);

        assertEquals(properties, FileUtil.loadProperties(path));
    }

    @Test
    void fileUploadCanStillBeRead(@TempDir Path dir) throws IOException {
        final Path file = dir.resolve("image.png");
        Files.writeString(file, "content");

        try (FileUpload upload = FileUtil.toFileUpload(file.toFile(), "image.png")) {
            assertEquals("image.png", upload.getName());
            // JDA reads the data later when sending, so the stream must still be open
            assertArrayEquals("content".getBytes(), upload.getData().readAllBytes());
        }
    }

    @Test
    void loadJson(@TempDir Path dir) throws IOException {
        final Path valid = dir.resolve("valid.json");
        Files.writeString(valid, "{\"key\": 1}");
        final Path invalid = dir.resolve("invalid.json");
        Files.writeString(invalid, "no json");

        assertEquals(1, FileUtil.loadJson(valid.toString()).getInt("key"));
        assertNull(FileUtil.loadJson(invalid.toString()));
    }
}
