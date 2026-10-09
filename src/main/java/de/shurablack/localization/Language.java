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
 * Static registry of language files ({@code .properties}) used to translate bot texts.
 * <br><br>
 * Each file is registered under its file name without the {@code .properties} extension, so files are named
 * after the language ({@code "de.properties"}) or a Discord locale tag ({@code "en-US.properties"},
 * {@code "pt-BR.properties"}). Texts are resolved per key with the fallback
 * full locale &rarr; base language &rarr; default language &rarr; empty String.
 * <br><br>
 * The registry is not thread-safe; load all languages at startup before reading from it concurrently.
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * // Load the language files (or all files of a directory with discoverLanguages(...))
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
 * String userText = Language.get(event, "code");
 * }</pre>
 *
 * @version localization-1.0.0
 * @date 23.06.2023
 * @author ShuraBlack
 */
public class Language {

    private static final Logger LOGGER = LoggerFactory.getLogger(Language.class);

    /** Loaded languages, keyed by file name without extension */
    private static final Map<String, Properties> LANGUAGES = new HashMap<>();

    /** Key of the fallback language, or {@code null} if none is set */
    private static String DEFAULT_LANGUAGE;

    private Language() { }

    /**
     * Sets the fallback language used when a text is missing in the requested language.
     * @param language the key of an already loaded language, e.g. {@code "en"}
     * @return true if the language was set, false if it isn't loaded
     */
    public static boolean setDefaultLanguage(final String language) {
        if (!LANGUAGES.containsKey(language)) {
            return false;
        }
        DEFAULT_LANGUAGE = language;
        return true;
    }

    /**
     * Loads every {@code .properties} file in the given directory (not recursive) with {@link #loadLanguageFile(String)}.
     * <br><br>
     * Other files, e.g. a README, are skipped silently.
     * @param dirPath path of the directory containing the language files
     * @return true if the directory was listed (even if it is empty), false if it isn't a directory
     *         or can't be read
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
            // Other files in the folder (e.g. a README) are no languages
            if (!file.isFile() || !file.getName().endsWith(".properties")) {
                continue;
            }
            LOGGER.info("Found language: " + file.getName().substring(0, file.getName().length() - ".properties".length()));

            boolean loadSuccessfully = loadLanguageFile(file.getAbsolutePath());
            if (!loadSuccessfully) {
                LOGGER.warn("Failed to load language file: " + file.getAbsolutePath());
            }
        }

        return true;
    }

    /**
     * Loads a language file and registers it under its file name without the {@code .properties} extension.
     * <br><br>
     * An already loaded language with the same name is replaced. If the file can't be read, the error is logged
     * and the language is registered without any texts.
     * @param path the path to the language file, may be {@code null}
     * @return true if the language was registered, false if the path is {@code null}, the file doesn't exist
     *         or doesn't end with {@code .properties}
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
     * Gets the properties of the language with the specified name, or of the default language if it isn't loaded.
     * <br><br>
     * The returned object is the registered instance, so changes to it affect later lookups.
     * @param language the language key, e.g. {@code "en"} or {@code "en-US"}
     * @return the language properties, or a new empty {@link Properties} if neither the language
     *         nor a default language is loaded
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
     * Gets the text String for the specified language and code, falling back to the default language
     * if the language isn't loaded or doesn't contain the code.
     * @param language the language key, e.g. {@code "en"} or {@code "en-US"}
     * @param code the text code
     * @return the text String or an empty String if neither the language nor the default language contains the code
     */
    public static String get(final String language, final String code) {
        return resolve(code, language);
    }

    /**
     * Gets the text String for the specified discord locale and code.
     * <br><br>
     * Tries the full locale ("en-US"), then the base language ("en"), then the default language.
     * @param locale the discord locale, e.g. from {@link Interaction#getUserLocale()}; must not be {@code null}
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
     * Returns the text of the first language containing the code, falling back to the default language.
     * @param code the text code
     * @param languages the language keys to try in order
     * @return the text String or an empty String if no candidate contains the code
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
     * Gets the keys of all loaded languages.
     * @return a live view of the language keys; removing an element unloads that language
     */
    public static Set<String> getLanguages() {
        return LANGUAGES.keySet();
    }
}
