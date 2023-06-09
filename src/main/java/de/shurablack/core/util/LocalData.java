package de.shurablack.core.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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
    public static final Logger LOGGER = LogManager.getLogger(LocalData.class);

    /** Temp Properties **/
    private static final Properties TEMPS = new Properties();

    /** Map for categoryID data*/
    private static final Map<String, String> CATEGORYIDS = new HashMap<>();

    /** Map for channelID data*/
    private static final Map<String,String> CHANNELIDS = new HashMap<>();

    /** Map for messageID data*/
    private static final Map<String, String> MESSAGEIDS = new HashMap<>();

    /** Map for webhook URLs data*/
    private static final Map<String,String> WEBHOOKS = new HashMap<>();

    /** Map for emojiID data*/
    private static final Map<String, String> EMOJIID = new HashMap<>();

    /** Map for roleID data*/
    private static final Map<String, String> ROLEID = new HashMap<>();

    /** Name of the temp file*/
    private static final String TEMP_FILE = "temp.properties";

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
            singleFile("category_id.properties", "# Add channel with <name>=<category_id>");
            singleFile("channel_id.properties", "# Add channel with <name>=<channel_id>");
            singleFile("message_id.properties", "# Add message with <name>=<message_id>");
            singleFile("webhook_link.properties", "# Add webhook with <name>=<webhook_link>");
            singleFile("emoji_id.properties", "# Add webhook with <name>=<emoji_id>");
            singleFile("role_id.properties", "# Add webhook with <name>=<role_id>");
            singleFile("tmp.properties","# Add local properties with <key>=<value>");
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
        FileUtil.fillProperties(FileUtil.loadProperties("category_id.properties"), CATEGORYIDS);
        FileUtil.fillProperties(FileUtil.loadProperties("channel_id.properties"), CHANNELIDS);
        FileUtil.fillProperties(FileUtil.loadProperties("message_id.properties"), MESSAGEIDS);
        FileUtil.fillProperties(FileUtil.loadProperties("webhook_link.properties"), WEBHOOKS);
        FileUtil.fillProperties(FileUtil.loadProperties("emoji_id.properties"), EMOJIID);
        FileUtil.fillProperties(FileUtil.loadProperties("role_id.properties"), ROLEID);
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
     * @return the stored value
     */
    public static String getTmp(final String key) {
        return TEMPS.getProperty(key);
    }

    /**
     * Returns the stored category ID if it exists
     * @param name the specified name for the ID
     * @return the category ID or an empty string
     */
    public static String getCategoryID(final String name) {
        return CATEGORYIDS.getOrDefault(name,"");
    }

    /**
     * Returns the stored channel ID if it exists
     * @param name the specified name for the ID
     * @return the channel ID or an empty string
     */
    public static String getChannelID(final String name) {
        return CHANNELIDS.getOrDefault(name,"");
    }

    /**
     * Returns the stored message ID if it exists
     * @param name the specified name for the ID
     * @return the message ID or an empty string
     */
    public static String getMessageID(final String name) {
        return MESSAGEIDS.getOrDefault(name,"");
    }

    /**
     * Returns the stored webhook URL if it exists
     * @param name the specified name for the ID
     * @return the URL or an empty string
     */
    public static String getWebHookLink(final String name) {
        return WEBHOOKS.getOrDefault(name,"");
    }

    /**
     * Returns the stored emoji ID or unicode if it exists
     * @param name the specified name for the ID
     * @return the ID/Unicode or an empty string
     */
    public static String getEmojiID(final String name) {
        return EMOJIID.getOrDefault(name,"");
    }

    /**
     * Returns the stored role ID if it exists
     * @param name the specified name for the ID
     * @return the role ID or an empty string
     */
    public static String getRoleID(final String name) {
        return ROLEID.getOrDefault(name, "");
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
