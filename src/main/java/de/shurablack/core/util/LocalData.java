package de.shurablack.core.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * <p>
 * The LocalData class is a utility class that provides methods for getting and setting
 * various Discord-related data such as category, channel, and message IDs, as well as webhook links and emoji IDs.
 * <br><br>
 * This data is stored in properties files and loaded into memory when the LocalData class is initialized.
 * <br><br>
 * The class also provides methods for adding and removing data from these properties files
 * </p>
 *
 * @version core-1.0.0
 * @date 09.06.2023
 * @author ShuraBlack
 */
public class LocalData {

    /** Class Logger */
    private static final Logger LOGGER = LogManager.getLogger(LocalData.class);

    /** Temp Properties **/
    private static final Properties TEMPS = new Properties();

    /** Map for categoryID data*/
    private static final Properties CATEGORYIDS = new Properties();

    /** Map for channelID data*/
    private static final Properties CHANNELIDS = new Properties();

    /** Map for messageID data*/
    private static final Properties MESSAGEIDS = new Properties();

    /** Map for webhook URLs data*/
    private static final Properties WEBHOOKS = new Properties();

    /** Map for emojiID data*/
    private static final Properties EMOJIIDS = new Properties();

    /** Map for roleID data*/
    private static final Properties ROLEIDS = new Properties();

    /** Name of the temp file*/
    private static final String TEMP_FILE = "properties/temp.properties";

    private LocalData() { }

    /**
     * Initializes the LocalData by checking if the necessary files exist, and creating them if they don't.
     * Then loads the files into memory.
     */
    public static void init() {
        fileCheck();
        loadFiles();
    }

    /**
     * Clears all the stored data in memory
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
     * Lets you save a temporary key-value pairs onto a local file
     * @param key the specified key
     * @param value the specified value
     */
    public static void saveTempValue(final String key, final String value) {
        TEMPS.setProperty(key, value);
        saveTmpFile();
    }

    /**
     * Lets you delete a temporary key-value pairs in the local file
     * @param key the specified key
     */
    public static void deleteTempValue(final String key) {
        TEMPS.remove(key);
        saveTmpFile();
    }

    /**
     * Saves the temp properties to a local file
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
     * Helper method that checks if the necessary files exist, and creates them if they don't.
     */
    private static void fileCheck() {
        try {
            File folder = new File("properties");
            if (!folder.exists() && !folder.mkdir()) {
                LOGGER.error("Couldnt create properties folder. Check the permissions!");
                System.exit(1);
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
     * Helper method that loads the necessary files into memory.
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
     * @param fileName the specified file name relative to the location
     * @param msg the specified message which will be put into the file
     * @throws IOException if the {@link FileOutputStream} cant write the missing file
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
     * Returns the stored temp value
     * @param key the specified key value
     * @return the stored value or null
     */
    public static String getTmp(final String key) {
        return TEMPS.getProperty(key);
    }

    /**
     * Returns the stored category ID if it exists
     * @param name the specified name for the ID
     * @return the category ID or null
     */
    public static String getCategoryID(final String name) {
        return CATEGORYIDS.getProperty(name);
    }

    /**
     * Returns the stored channel ID if it exists
     * @param name the specified name for the ID
     * @return the channel ID or null
     */
    public static String getChannelID(final String name) {
        return CHANNELIDS.getProperty(name);
    }

    /**
     * Returns the stored message ID if it exists
     * @param name the specified name for the ID
     * @return the message ID or null
     */
    public static String getMessageID(final String name) {
        return MESSAGEIDS.getProperty(name);
    }

    /**
     * Returns the stored webhook URL if it exists
     * @param name the specified name for the ID
     * @return the URL or null
     */
    public static String getWebHookLink(final String name) {
        return WEBHOOKS.getProperty(name);
    }

    /**
     * Returns the stored emoji ID or unicode if it exists
     * @param name the specified name for the ID
     * @return the ID/Unicode or null
     */
    public static String getEmojiID(final String name) {
        return EMOJIIDS.getProperty(name);
    }

    /**
     * Returns the stored role ID if it exists
     * @param name the specified name for the ID
     * @return the role ID or an empty string
     */
    public static String getRoleID(final String name) {
        return ROLEIDS.getProperty(name);
    }

    /**
     * Fetches {@link Category} from the given {@link Guild}.
     * Only missing entries will be stored and saved into the local file.
     * @param guild the specified guild which will be used
     * @return true, if the action was successful
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
        FileUtil.saveProperties("category_id.properties","# Add category with <name>=<category_id>", CATEGORYIDS);
        return true;
    }

    /**
     * Fetches {@link GuildChannel} from the given {@link Guild}.
     * This includes hidden channels, but exclude {@link net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel ThreadChannel}.
     * Only missing entries will be stored and saved into the local file.
     * @param guild the specified guild which will be used
     * @return true, if the action was successful
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
        FileUtil.saveProperties("channel_id.properties","# Add channel with <name>=<channel_id>",CHANNELIDS);
        return true;
    }

    /**
     * Fetches {@link net.dv8tion.jda.api.entities.emoji.Emoji Emoji} from the given {@link Guild}.
     * Only missing entries will be stored and saved into the local file.
     * @param guild the specified guild which will be used
     * @return true, if the action was successful
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
        FileUtil.saveProperties("emoji_id.properties","# Add emoji with <name>=<emoji_id>", EMOJIIDS);
        return true;
    }

    /**
     * Fetches {@link Role} from the given {@link Guild}.
     * Only missing entries will be stored and saved into the local file.
     * @param guild the specified guild which will be used
     * @return true, if the action was successful
     */
    public static boolean fetchRoles(final Guild guild) {
        if (guild == null) {
            return false;
        }

        for (Role role : guild.getRoles()) {
            if (EMOJIIDS.containsKey(role.getName())) {
                continue;
            }
            EMOJIIDS.setProperty(role.getName(), role.getId());
        }
        FileUtil.saveProperties("role_id.properties","# Add role with <name>=<role_id>", EMOJIIDS);
        return true;
    }

    /**
     * Logs all {@link net.dv8tion.jda.api.entities.emoji.Emoji} as mention string
     * @param guild the specified discord server, were it loads the data from
     */
    public static void getServerEmojis(final Guild guild) {
        final String emojis = guild.getEmojis().stream().map(CustomEmoji::getAsMention).collect(Collectors.joining("\n"));
        final String msg = String.format("Emojis of: %s - %s%n%n%s", guild.getName(), guild.getId(), emojis);
        LOGGER.info(msg);
    }

    /**
     * Logs all {@link net.dv8tion.jda.api.entities.Role} names and IDs as string
     * @param guild the specified discord server, were it loads the data from
     */
    public static void getServerRoles(final Guild guild) {
        final String emojis = guild.getRoles().stream().map(role -> role.getName() + " - " + role.getId()).collect(Collectors.joining("\n"));
        final String msg = String.format("Roles of: %s - %s%n%n%s", guild.getName(), guild.getId(), emojis);
        LOGGER.info(msg);
    }

}
