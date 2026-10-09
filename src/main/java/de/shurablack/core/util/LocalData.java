package de.shurablack.core.util;

import de.shurablack.core.builder.InitException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * <p>
 * Static store for named Discord data such as category, channel, message, emoji and role IDs,
 * webhook URLs and free temporary key-value pairs.
 * <br><br>
 * The data lives in properties files inside the "properties" folder of the working directory.
 * {@link #init()} creates missing files and loads them into memory; the getters only read the memory copy.
 * <br><br>
 * Temporary values are written back to their file immediately. The fetch methods add missing guild
 * entries and save the result.
 * </p>
 *
 * @version core-1.0.0
 * @date 09.06.2023
 * @author ShuraBlack
 */
public class LocalData {

    /** Class Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalData.class);

    /** Temporary key-value pairs */
    private static final Properties TEMPS = new Properties();

    /** Category IDs by name */
    private static final Properties CATEGORYIDS = new Properties();

    /** Channel IDs by name */
    private static final Properties CHANNELIDS = new Properties();

    /** Message IDs by name */
    private static final Properties MESSAGEIDS = new Properties();

    /** Webhook URLs by name */
    private static final Properties WEBHOOKS = new Properties();

    /** Emoji IDs, mentions or unicode by name */
    private static final Properties EMOJIIDS = new Properties();

    /** Role IDs by name */
    private static final Properties ROLEIDS = new Properties();

    /** Path of the temp file, relative to the working directory */
    private static final String TEMP_FILE = "properties/temp.properties";

    private LocalData() { }

    /**
     * Creates the "properties" folder and any missing properties file, then loads all files into memory.
     * Loaded entries are added to the existing in-memory data.
     * @throws InitException if the "properties" folder can't be created
     */
    public static void init() {
        fileCheck();
        loadFiles();
    }

    /**
     * Clears all data in memory. The files stay untouched.
     */
    public static void clear() {
        CATEGORYIDS.clear();
        CHANNELIDS.clear();
        MESSAGEIDS.clear();
        WEBHOOKS.clear();
        EMOJIIDS.clear();
        ROLEIDS.clear();
        TEMPS.clear();
    }

    /**
     * Stores a temporary key-value pair and rewrites the temp file.
     * @param key the key
     * @param value the value
     */
    public static void saveTempValue(final String key, final String value) {
        TEMPS.setProperty(key, value);
        saveTmpFile();
    }

    /**
     * Removes a temporary key-value pair and rewrites the temp file.
     * @param key the key
     */
    public static void deleteTempValue(final String key) {
        TEMPS.remove(key);
        saveTmpFile();
    }

    /**
     * Rewrites the temp file with the current temp properties. Errors are only logged.
     */
    private static void saveTmpFile() {
        StringBuilder s = new StringBuilder();
        s.append("# Add local properties with <key>=<value>\n\n");
        TEMPS.forEach((key1, value1) -> s.append(key1).append("=").append(value1).append("\n"));

        final File file = new File(TEMP_FILE);
        try (final OutputStream out = new FileOutputStream(file)) {
            out.write(s.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            LOGGER.error("Missing permissions to save the temp properties file");
        }
    }

    /**
     * Creates the "properties" folder and every missing properties file with a comment header.
     */
    private static void fileCheck() {
        try {
            File folder = new File("properties");
            if (!folder.exists() && !folder.mkdir()) {
                throw new InitException("Couldnt create properties folder. Check the permissions!");
            }
            singleFile("properties/category_id.properties", "# Add category with <name>=<category_id>");
            singleFile("properties/channel_id.properties", "# Add channel with <name>=<channel_id>");
            singleFile("properties/message_id.properties", "# Add message with <name>=<message_id>");
            singleFile("properties/webhook_link.properties", "# Add webhook with <name>=<webhook_link>");
            singleFile("properties/emoji_id.properties", "# Add emoji with <name>=<emoji_id>");
            singleFile("properties/role_id.properties", "# Add role with <name>=<role_id>");
            singleFile(TEMP_FILE,"# Add local properties with <key>=<value>");
        } catch (FileNotFoundException e) {
            LOGGER.warn("Couldnt create properties files", e);
        } catch (IOException e) {
            LOGGER.warn("Couldnt write to properties files", e);
        }
    }

    /**
     * Loads all properties files into memory.
     */
    private static void loadFiles() {
        CATEGORYIDS.putAll(FileUtil.loadProperties("properties/category_id.properties"));
        CHANNELIDS.putAll(FileUtil.loadProperties("properties/channel_id.properties"));
        MESSAGEIDS.putAll(FileUtil.loadProperties("properties/message_id.properties"));
        WEBHOOKS.putAll(FileUtil.loadProperties("properties/webhook_link.properties"));
        EMOJIIDS.putAll(FileUtil.loadProperties("properties/emoji_id.properties"));
        ROLEIDS.putAll(FileUtil.loadProperties("properties/role_id.properties"));
        TEMPS.putAll(FileUtil.loadProperties(TEMP_FILE));
    }

    /**
     * Creates the file with the given content if it doesn't exist yet. Write errors are caught and logged.
     * @param fileName the file path, relative to the working directory
     * @param msg the initial content of the file
     * @throws IOException declared, but never thrown since write errors are caught internally
     */
    private static void singleFile(final String fileName, final String msg) throws IOException {
        final File file = new File(fileName);

        if (file.exists()) {
            return;
        }

        try (final OutputStream out = new FileOutputStream(file)) {
            final String outputMsg = String.format("Missing Properties detected <\u001b[32;1m%s\u001b[0m>. [Auto-Generate]", fileName);
            LOGGER.info(outputMsg);
            out.write(msg.getBytes());
            out.flush();
        } catch (IOException e) {
            LOGGER.error(String.format("Missing permissions to auto generate the requested file <%s>", fileName));
        }
    }

    /**
     * Returns a stored temporary value.
     * @param key the key
     * @return the value or null if it doesn't exist
     */
    public static String getTmp(final String key) {
        return TEMPS.getProperty(key);
    }

    /**
     * Returns the stored category ID.
     * @param name the name of the entry
     * @return the category ID or null
     */
    public static String getCategoryID(final String name) {
        return CATEGORYIDS.getProperty(name);
    }

    /**
     * Returns the stored channel ID.
     * @param name the name of the entry
     * @return the channel ID or null
     */
    public static String getChannelID(final String name) {
        return CHANNELIDS.getProperty(name);
    }

    /**
     * Returns the stored message ID.
     * @param name the name of the entry
     * @return the message ID or null
     */
    public static String getMessageID(final String name) {
        return MESSAGEIDS.getProperty(name);
    }

    /**
     * Returns the stored webhook URL.
     * @param name the name of the entry
     * @return the URL or null
     */
    public static String getWebHookLink(final String name) {
        return WEBHOOKS.getProperty(name);
    }

    /**
     * Returns the stored emoji entry (ID, mention string or unicode, as stored in the file).
     * @param name the name of the entry
     * @return the stored value or null
     */
    public static String getEmojiID(final String name) {
        return EMOJIIDS.getProperty(name);
    }

    /**
     * Returns the stored role ID.
     * @param name the name of the entry
     * @return the role ID or null
     */
    public static String getRoleID(final String name) {
        return ROLEIDS.getProperty(name);
    }

    /**
     * Adds every {@link Category} of the given {@link Guild} whose ID isn't stored yet (name as key)
     * and saves the category data to "properties/category_id.properties".
     * @param guild the guild to read from
     * @return false if the guild is null, true otherwise
     */
    public static boolean fetchCategories(final Guild guild) {
        if (guild == null) {
            return false;
        }

        for (Category category : guild.getCategories()) {
            if (CATEGORYIDS.containsValue(category.getId())) {
                continue;
            }
            CATEGORYIDS.setProperty(category.getName(), category.getId());
        }
        FileUtil.saveProperties("properties/category_id.properties","# Add category with <name>=<category_id>", CATEGORYIDS);
        return true;
    }

    /**
     * Adds every {@link GuildChannel} of the given {@link Guild} whose ID isn't stored yet (name as key)
     * and saves the channel data to "properties/channel_id.properties".
     * This includes hidden channels, but excludes {@link net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel ThreadChannel}s.
     * Channels with the same name overwrite each other.
     * @param guild the guild to read from
     * @return false if the guild is null, true otherwise
     */
    public static boolean fetchChannels(final Guild guild) {
        if (guild == null) {
            return false;
        }

        for (GuildChannel channel : guild.getChannels()) {
            if (CHANNELIDS.containsValue(channel.getId())) {
                continue;
            }
            CHANNELIDS.setProperty(channel.getName(), channel.getId());
        }
        FileUtil.saveProperties("properties/channel_id.properties","# Add channel with <name>=<channel_id>",CHANNELIDS);
        return true;
    }

    /**
     * Adds every custom emoji of the given {@link Guild} whose name isn't stored yet, with its mention
     * string as value, and saves the emoji data to "properties/emoji_id.properties".
     * @param guild the guild to read from
     * @return false if the guild is null, true otherwise
     */
    public static boolean fetchEmojis(final Guild guild) {
        if (guild == null) {
            return false;
        }

        for (RichCustomEmoji emoji : guild.getEmojis()) {
            if (EMOJIIDS.containsKey(emoji.getName())) {
                continue;
            }
            EMOJIIDS.setProperty(emoji.getName(), emoji.getAsMention());
        }
        FileUtil.saveProperties("properties/emoji_id.properties","# Add emoji with <name>=<emoji_id>", EMOJIIDS);
        return true;
    }

    /**
     * Adds every {@link Role} of the given {@link Guild} whose name isn't stored yet (name as key, ID as value)
     * and saves the role data to "properties/role_id.properties".
     * @param guild the guild to read from
     * @return false if the guild is null, true otherwise
     */
    public static boolean fetchRoles(final Guild guild) {
        if (guild == null) {
            return false;
        }

        for (Role role : guild.getRoles()) {
            if (ROLEIDS.containsKey(role.getName())) {
                continue;
            }
            ROLEIDS.setProperty(role.getName(), role.getId());
        }
        FileUtil.saveProperties("properties/role_id.properties","# Add role with <name>=<role_id>", ROLEIDS);
        return true;
    }

    /**
     * Logs all custom emojis of the guild as mention strings (INFO level). Nothing gets stored.
     * @param guild the guild to read from, must not be null
     */
    public static void getServerEmojis(final Guild guild) {
        final String emojis = guild.getEmojis().stream().map(CustomEmoji::getAsMention).collect(Collectors.joining("\n"));
        final String msg = String.format("Emojis of: %s - %s%n%n%s", guild.getName(), guild.getId(), emojis);
        LOGGER.info(msg);
    }

    /**
     * Logs the names and IDs of all roles of the guild (INFO level). Nothing gets stored.
     * @param guild the guild to read from, must not be null
     */
    public static void getServerRoles(final Guild guild) {
        final String emojis = guild.getRoles().stream().map(role -> role.getName() + " - " + role.getId()).collect(Collectors.joining("\n"));
        final String msg = String.format("Roles of: %s - %s%n%n%s", guild.getName(), guild.getId(), emojis);
        LOGGER.info(msg);
    }

}
