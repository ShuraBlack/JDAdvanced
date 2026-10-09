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
     * Strips the mention syntax and returns the inner value.
     * <br><br>
     * Role, user, channel and slash command mentions return everything between the prefix and the closing
     * bracket (e.g. "name:id" for a slash command, "!id" for a legacy nickname mention). Custom emojis
     * (<code>&lt;:name:id&gt;</code>) return the part from the second colon on, including that colon (":id").
     * Animated emojis (<code>&lt;a:...&gt;</code>) aren't supported.
     * @param mention the mention string
     * @return the inner value or null if the input is null, empty or no supported mention
     */
    public static String unwrap(String mention) {
        if (mention == null || mention.isEmpty()) {
            return null;
        }
        if (mention.startsWith("<@&")) {
            return mention.substring(3, mention.length() - 1);
        } else if (mention.startsWith("<@")) {
            return mention.substring(2, mention.length() - 1);
        } else if (mention.startsWith("<#")) {
            return mention.substring(2, mention.length() - 1);
        } else if (mention.startsWith("<:")) {
            return mention.substring(mention.indexOf(':', 2), mention.length() - 1);
        } else if (mention.startsWith("</")) {
            return mention.substring(2, mention.length() - 1);
        }
        return null;
    }
}
