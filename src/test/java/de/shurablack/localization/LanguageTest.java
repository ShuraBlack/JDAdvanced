package de.shurablack.localization;

import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LanguageTest {

    @Test
    void setDefaultLanguageReturnsFalseForNonExistentLanguage() {
        boolean result = Language.setDefaultLanguage("nonexistent");
        assertFalse(result);
    }

    @Test
    void loadLanguageFileReturnsFalseForNullPath() {
        boolean result = Language.loadLanguageFile(null);
        assertFalse(result);
    }

    @Test
    void loadLanguageFileReturnsFalseForNonExistentFile() {
        boolean result = Language.loadLanguageFile("nonexistent.properties");
        assertFalse(result);
    }

    @Test
    void loadLanguageFileReturnsFalseForInvalidFileExtension() {
        boolean result = Language.loadLanguageFile("src/test/resources/invalid.txt");
        assertFalse(result);
    }

    @Test
    void getReturnsEmptyPropertiesForNonExistentLanguage() {
        Properties properties = Language.get("nonexistent");
        assertTrue(properties.isEmpty());
    }

    @Test
    void getTextReturnsEmptyStringForNonExistentLanguage() {
        String result = Language.get("nonexistent", "code");
        assertEquals("", result);
    }

    @Test
    void getTextReturnsEmptyStringForNonExistentCode() {
        Language.loadLanguageFile("src/test/resources/en.properties");
        String result = Language.get("en", "nonexistent");
        assertEquals("", result);
    }

    @Test
    void getLanguagesReturnsEmptySetWhenNoLanguagesLoaded() {
        Set<String> languages = Language.getLanguages();
        assertTrue(languages.isEmpty());
    }
}