package de.shurablack.core.util;

import net.dv8tion.jda.api.utils.FileUpload;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.awt.Image;
import java.io.File;
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

}