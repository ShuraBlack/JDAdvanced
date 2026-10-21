package de.shurablack.localization;

import de.shurablack.core.event.validation.Validation;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LanguageTest {

    @AfterEach
    void reset() {
        Language.reset();
    }

    private static void write(Path dir, String name, String content) throws IOException {
        final Path file = dir.resolve(name);
        Files.writeString(file, content);
        assertTrue(Language.loadLanguageFile(file.toString()));
    }

    @Test
    void discoverLanguagesLoadsOnlyPropertiesFiles(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("de.properties"), "greet=Hallo");
        Files.writeString(dir.resolve("en-US.properties"), "greet=Hello");
        Files.writeString(dir.resolve("README"), "no dot in the name");
        Files.writeString(dir.resolve("notes.txt"), "other extension");
        Files.createDirectory(dir.resolve("sub.properties"));

        assertTrue(Language.discoverLanguages(dir.toString()));
        assertEquals(Set.of("de", "en-US"), Language.getLanguages());
        assertEquals("Hallo", Language.get("de", "greet"));
    }

    @Test
    void discoverLanguagesReturnsFalseForMissingDirectory() {
        assertFalse(Language.discoverLanguages("does/not/exist"));
    }

    @Test
    void localeFallsBackToBaseLanguageThenDefaultPerKey(@TempDir Path dir) throws IOException {
        write(dir, "en.properties", "greet=Hello\nbye=Bye\n");
        write(dir, "de.properties", "greet=Hallo\nvalidation.on_cooldown=Bitte warte kurz\n");
        Language.setDefaultLanguage("en");

        assertEquals("Hallo", Language.get(DiscordLocale.GERMAN, "greet"));
        // Missing in de -> default language instead of an empty text
        assertEquals("Bye", Language.get(DiscordLocale.GERMAN, "bye"));
        // en-GB file doesnt exist -> base language "en"
        assertEquals("Hello", Language.get(DiscordLocale.ENGLISH_UK, "greet"));
        assertEquals("Hello", Language.get(DiscordLocale.FRENCH, "greet"));
        assertEquals("", Language.get(DiscordLocale.GERMAN, "missing"));

        assertEquals("Bitte warte kurz", Validation.ON_COOLDOWN.getMessage(DiscordLocale.GERMAN));
        assertEquals(Validation.ON_COOLDOWN.getMessage(), Validation.ON_COOLDOWN.getMessage(DiscordLocale.FRENCH));
    }

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