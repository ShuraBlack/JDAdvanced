package de.shurablack.core.util;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssetPoolTest {

    @Test
    void initCreatesAssetsFileIfNotExists() {
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(false);
        AssetPool.init();
        assertTrue(new File("assets.properties").exists());
        new File("assets.properties").delete();
    }

    @Test
    void initLoadsAssetsFromExistingFile() {
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        AssetPool.init();
        assertNotNull(AssetPool.get("url_example"));
    }

    @Test
    void addReturnsFalseForNonExistentFile() {
        boolean result = AssetPool.add("img_nonexistent", "nonexistent.png");
        assertFalse(result);
    }

    @Test
    void addLoadsUrlAssetSuccessfully() {
        boolean result = AssetPool.add("url_test", "http://example.com");
        assertTrue(result);
        assertEquals("http://example.com", AssetPool.get("url_test"));
    }

    @Test
    void removeDeletesExistingAsset() {
        AssetPool.add("url_test", "http://example.com");
        boolean result = AssetPool.remove("url_test");
        assertTrue(result);
        assertEquals("", AssetPool.get("url_test"));
    }

    @Test
    void removeReturnsFalseForNonExistentAsset() {
        boolean result = AssetPool.remove("nonexistent");
        assertFalse(result);
    }

    @Test
    void getReturnsEmptyStringForNonExistentAsset() {
        String result = AssetPool.get("nonexistent");
        assertEquals("", result);
    }

    @Test
    void getFileReturnsNullForNonExistentImageAsset() {
        BufferedImage result = AssetPool.getFile("nonexistent");
        assertNull(result);
    }
}