package de.shurablack.localization;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * <p>
 * The TimeFormatter class is a Java class that provides a way to format time in Discord.
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * // Format the time to a long date time
 * String relativeTime = TimeFormatter.format(TimeFormatter.Format.LONG_DATE_TIME, 1614556800);
 * // Output: <t:1614556800:F>
 * // Discord text: Sunday, January 3, 2021 00:00:00 AM
 *
 * @version localization-1.0.1
 * @date 05.03.2024
 * @author ShuraBlack
 */
public class Timestamp {

    /**
     * The format of the time
     */
    public enum Format {
        /** Example: 3 years ago */
        RELATIVE("R"),
        /** Example: January 3, 2021 */
        LONG_DATE("D"),
        /** Example: 01/03/2021 */
        SHORT_DATE("d"),
        /** Example: 6:35:00 PM */
        LONG_TIME("T"),
        /** Example: 6:35 PM */
        SHORT_TIME("t"),
        /** Example: Sunday, January 3, 2021 6:35 PM */
        LONG_DATE_TIME("F"),
        /** Example: January 3, 2021 6:35 PM */
        SHORT_DATE_TIME("f");

        /**
         * The identifier of the format
         */
        private final String identifier;

        /**
         * Constructs a new format
         * @param identifier the identifier of the format
         */
        Format(String identifier) {
            this.identifier = identifier;
        }

        /**
         * Gets the identifier of the format
         * @return the identifier of the format
         */
        public String getIdentifier() {
            return identifier;
        }
    }

    /**
     * Formats the time
     * @param format the format of the time {@link Format}
     * @param unixSeconds the time in seconds
     * @return the formatted time
     */
    public static String format(Format format, long unixSeconds) {
        return build(format, unixSeconds);
    }

    /**
     * Formats the time
     * @param format the format of the time {@link Format}
     * @param time the time
     * @return the formatted time
     */
    public static String format(Format format, LocalDateTime time) {
        ZoneOffset current = ZoneId.systemDefault().getRules().getOffset(time);
        return build(format, time.toEpochSecond(current));
    }

    /**
     * Builds the time string
     * @param format the format of the time {@link Format}
     * @param time the time in seconds
     * @return the time string
     */
    private static String build(Format format, long time) {
        return String.format("<t:%d:%s>", time, format.getIdentifier());
    }
}
