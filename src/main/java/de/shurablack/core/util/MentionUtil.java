package de.shurablack.core.util;

public class MentionUtil {

    public static String wrapRole(String id) {
        return "<@&" + id + ">";
    }

    public static String wrapUser(String id) {
        return "<@" + id + ">";

    }

    public static String wrapChannel(String id) {
        return "<#" + id + ">";
    }

    public static String wrapEmoji(String id) {
        return "<:" + id + ">";
    }

    public static String wrapEmoji(String id, String name) {
        return "<:" + name + ":" + id + ">";
    }

    public static String wrapSlashCommand(String id) {
        return "</" + id + ">";
    }

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
