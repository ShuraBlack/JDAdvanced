package de.shurablack.localization;

import de.shurablack.core.util.FileUtil;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *
 * // Gets the text String in the language of the user who triggered the interaction
 * String text = Language.get(event, "code");
 * }</pre>
 * Files are named after the language ("de.properties") or a discord locale tag ("en-US.properties", "pt-BR.properties").
 *
 * @version localization-1.0.0
 * @date 23.06.2023
 * @author ShuraBlack
 */
public class Language {

    private static final Logger LOGGER = LoggerFactory.getLogger(Language.class);

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
     * Discover all languages in the given directory
     * @param dirPath of the language property directory
     * @return true if the directory was read successfully, false if its not a dir or the dir is empty
     */
    public static boolean discoverLanguages(final String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            return false;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return false;
        }

        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            LOGGER.info("Found language: " + file.getName().substring(0, file.getName().lastIndexOf('.')));

            boolean loadSuccessfully = loadLanguageFile(file.getAbsolutePath());
            if (!loadSuccessfully) {
                LOGGER.warn("Failed to load language file: " + file.getAbsolutePath());
            }
        }

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
        return resolve(code, language);
    }

    /**
     * Gets the text String for the specified discord locale and code.
     * <br><br>
     * Tries the full locale ("en-US"), then the base language ("en"), then the default language
     * @param locale the discord locale, e.g. from {@link Interaction#getUserLocale()}
     * @param code the text code
     * @return the text String or an empty String if no language contains the code
     */
    public static String get(final DiscordLocale locale, final String code) {
        final String tag = locale.getLocale();
        final int dash = tag.indexOf('-');
        return resolve(code, tag, dash == -1 ? tag : tag.substring(0, dash));
    }

    /**
     * Gets the text String in the language of the user who triggered the interaction
     * @param interaction any interaction event, e.g. a slash command or button event
     * @param code the text code
     * @return the text String or an empty String if no language contains the code
     * @see #get(DiscordLocale, String)
     */
    public static String get(final Interaction interaction, final String code) {
        return get(interaction.getUserLocale(), code);
    }

    /**
     * Returns the text of the first language containing the code, falling back to the default language
     * @param code the text code
     * @param languages the languages to try in order
     * @return the text String or an empty String
     */
    private static String resolve(final String code, final String... languages) {
        for (String language : languages) {
            final Properties properties = LANGUAGES.get(language);
            if (properties != null && properties.containsKey(code)) {
                return properties.getProperty(code);
            }
        }
        final Properties fallback = DEFAULT_LANGUAGE == null ? null : LANGUAGES.get(DEFAULT_LANGUAGE);
        return fallback == null ? "" : fallback.getProperty(code, "");
    }

    /** Removes all languages, used by tests */
    static void reset() {
        LANGUAGES.clear();
        DEFAULT_LANGUAGE = null;
    }

    /**
     * Gets all languages that have been loaded
     * @return a set of all languages
     */
    public static Set<String> getLanguages() {
        return LANGUAGES.keySet();
    }
}
