package de.shurablack.localization;

import de.shurablack.core.util.FileUtil;
import net.dv8tion.jda.api.entities.Member;

import java.io.File;
import java.util.*;

/**
 * <p>
 * The Language class is a Java class that provides a way to load and access language files
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * // Load the language files
 * Language.loadLanguageFile("path/to/language/en.properties");
 * Language.loadLanguageFile("path/to/another/language/de.properties");
 *
 * // Set the default language
 * Language.setDefaultLanguage("en");
 *
 * // Get the language properties
 * Properties properties = Language.get("en");
 *
 * // Gets the text String for the specified language and code
 * String text = Language.get("en", "code");
 * }</pre>
 *
 * @version localization-1.0.0
 * @date 23.06.2023
 * @author ShuraBlack
 */
public class Language {

    /** Map of all languages */
    private static final Map<String, Properties> LANGUAGES = new HashMap<>();

    /** The default language */
    private static String DEFAULT_LANGUAGE;

    private Language() { }

    /**
     * Sets the default language
     * @param language the language to set
     * @return true if the language was set successfully
     */
    public static boolean setDefaultLanguage(final String language) {
        if (!LANGUAGES.containsKey(language)) {
            return false;
        }
        DEFAULT_LANGUAGE = language;
        return true;
    }

    /**
     * Loads a language file
     * @param path the path to the language file
     * @return true if the language file was loaded successfully
     */
    public static boolean loadLanguageFile(final String path) {
        if (path == null) {
            return false;
        }
        final File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        if (!file.getName().endsWith(".properties")) {
            return false;
        }

        final Properties properties = FileUtil.loadProperties(path);
        LANGUAGES.put(file.getName().substring(0, file.getName().length() - 11), properties);
        return true;
    }

    /**
     * Gets the language with the specified name or the default language if the specified language does not exist
     * @return the language properties or an empty properties object if the language does not exist
     */
    public static Properties get(final String language) {
        Properties properties = LANGUAGES.get(language);
        if (properties != null) {
            return properties;
        }
        if (DEFAULT_LANGUAGE != null) {
            properties = LANGUAGES.get(DEFAULT_LANGUAGE);
            if (properties != null) {
                return properties;
            }
        }
        return new Properties();
    }

    /**
     * Gets the text String for the specified language and code (default language if the specified language does not exist)
     * @param language the language
     * @param code the text code
     * @return the text String or an empty String if the language or code does not exist
     */
    public static String get(final String language, final String code) {
        final Properties properties = Language.get(language);
        if (properties.isEmpty()) {
            return "";
        }
        if (!properties.containsKey(code)) {
            return "";
        }
        return properties.getProperty(code);
    }

    /**
     * Gets all languages that have been loaded
     * @return a set of all languages
     */
    public static Set<String> getLanguages() {
        return LANGUAGES.keySet();
    }
}
