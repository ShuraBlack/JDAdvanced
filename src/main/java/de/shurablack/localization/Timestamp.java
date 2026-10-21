package de.shurablack.localization;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * <p>
 * Utility for building Discord timestamp markdown ({@code <t:seconds:style>}).
 * <br><br>
 * Discord renders these tags in the time zone and locale of each viewer.
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * // Format the time to a long date time
 * String longDateTime = Timestamp.format(Timestamp.Format.LONG_DATE_TIME, 1614556800);
 * // Output: <t:1614556800:F>
 * // Discord text (UTC, en-US): Monday, March 1, 2021 12:00 AM
 * }</pre>
 *
 * @version localization-1.0.1
 * @date 05.03.2024
 * @author ShuraBlack
 */
public class Timestamp {

    /**
     * The Discord timestamp styles. The examples show the en-US rendering.
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
         * The style letter used in the timestamp tag
         */
        private final String identifier;

        /**
         * Constructs a new format
         * @param identifier the style letter used in the timestamp tag
         */
        Format(String identifier) {
            this.identifier = identifier;
        }

        /** @return the style letter used in the timestamp tag, e.g. {@code "R"} */
        public String getIdentifier() {
            return identifier;
        }
    }

    /**
     * Builds a timestamp tag for a Unix time.
     * @param format the display style
     * @param unixSeconds the time in seconds since the Unix epoch
     * @return the timestamp tag, e.g. {@code "<t:1614556800:F>"}
     */
    public static String format(Format format, long unixSeconds) {
        return build(format, unixSeconds);
    }

    /**
     * Builds a timestamp tag for a local date-time, interpreted in the system default time zone.
     * @param format the display style
     * @param time the local date-time
     * @return the timestamp tag, e.g. {@code "<t:1614556800:F>"}
     */
    public static String format(Format format, LocalDateTime time) {
        ZoneOffset current = ZoneId.systemDefault().getRules().getOffset(time);
        return build(format, time.toEpochSecond(current));
    }

    /**
     * Builds the timestamp tag.
     * @param format the display style
     * @param time the time in seconds since the Unix epoch
     * @return the timestamp tag
     */
    private static String build(Format format, long time) {
        return String.format("<t:%d:%s>", time, format.getIdentifier());
    }
}
