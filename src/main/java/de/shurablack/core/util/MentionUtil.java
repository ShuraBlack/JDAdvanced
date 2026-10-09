package de.shurablack.core.util;

/**
 * <p>
 * Builds and parses Discord mention strings from and to raw IDs.
 * <br><br>
 * No validation is done; the methods only add or strip the mention syntax.
 * </p>
 *
 * @author ShuraBlack
 */
public class MentionUtil {

    /**
     * @param id the role ID
     * @return the role mention <code>&lt;@&amp;id&gt;</code>
     */
    public static String wrapRole(String id) {
        return "<@&" + id + ">";
    }

    /**
     * @param id the user ID
     * @return the user mention <code>&lt;@id&gt;</code>
     */
    public static String wrapUser(String id) {
        return "<@" + id + ">";

    }

    /**
     * @param id the channel ID
     * @return the channel mention <code>&lt;#id&gt;</code>
     */
    public static String wrapChannel(String id) {
        return "<#" + id + ">";
    }

    /**
     * Wraps the value as <code>&lt;:id&gt;</code>. Discord renders custom emojis only in the form
     * <code>&lt;:name:id&gt;</code>, so pass "name:id" or use {@link #wrapEmoji(String, String)}.
     * @param id the emoji value placed between the brackets
     * @return the emoji string <code>&lt;:id&gt;</code>
     */
    public static String wrapEmoji(String id) {
        return "<:" + id + ">";
    }

    /**
     * @param id the custom emoji ID
     * @param name the custom emoji name
     * @return the emoji mention <code>&lt;:name:id&gt;</code> (not animated)
     */
    public static String wrapEmoji(String id, String name) {
        return "<:" + name + ":" + id + ">";
    }

    /**
     * Wraps the value as slash command mention. Discord expects <code>&lt;/name:id&gt;</code>, so pass "name:id".
     * @param id the command value placed after the slash, usually "name:id"
     * @return the command mention <code>&lt;/id&gt;</code>
     */
    public static String wrapSlashCommand(String id) {
        return "</" + id + ">";
    }

    /**
     * Strips the mention syntax and returns the ID.
     * <br><br>
     * Supports role (<code>&lt;@&amp;id&gt;</code>), user (<code>&lt;@id&gt;</code>, legacy <code>&lt;@!id&gt;</code>),
     * channel (<code>&lt;#id&gt;</code>), custom emoji (<code>&lt;:name:id&gt;</code>, animated <code>&lt;a:name:id&gt;</code>)
     * and slash command (<code>&lt;/name:id&gt;</code>) mentions.
     * @param mention the mention string
     * @return the ID or null if the input is null or no supported mention
     */
    public static String unwrap(String mention) {
        if (mention == null || !mention.startsWith("<") || !mention.endsWith(">")) {
            return null;
        }
        final String inner = mention.substring(1, mention.length() - 1);
        if (inner.startsWith("@&") || inner.startsWith("@!")) {
            return inner.substring(2);
        } else if (inner.startsWith("@") || inner.startsWith("#")) {
            return inner.substring(1);
        } else if (inner.startsWith(":") || inner.startsWith("a:") || inner.startsWith("/")) {
            // Emojis and commands carry their name before the ID
            return inner.substring(inner.lastIndexOf(':') + 1).replaceFirst("^/", "");
        }
        return null;
    }
}
