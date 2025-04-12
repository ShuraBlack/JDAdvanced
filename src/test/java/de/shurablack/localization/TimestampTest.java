package de.shurablack.localization;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimestampTest {

    @Test
    void formatWithUnixSecondsReturnsCorrectRelativeTime() {
        String result = Timestamp.format(Timestamp.Format.RELATIVE, 1614556800);
        assertEquals("<t:1614556800:R>", result);
    }

    @Test
    void formatWithUnixSecondsReturnsCorrectLongDateTime() {
        String result = Timestamp.format(Timestamp.Format.LONG_DATE_TIME, 1614556800);
        assertEquals("<t:1614556800:F>", result);
    }

    @Test
    void formatWithLocalDateTimeReturnsCorrectShortDate() {
        LocalDateTime time = LocalDateTime.of(2021, 3, 1, 0, 0);
        String result = Timestamp.format(Timestamp.Format.SHORT_DATE, time);
        ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(time);
        assertEquals(String.format("<t:%d:d>", time.toEpochSecond(offset)), result);
    }

    @Test
    void formatWithUnixSecondsHandlesZeroTimestamp() {
        String result = Timestamp.format(Timestamp.Format.LONG_DATE, 0);
        assertEquals("<t:0:D>", result);
    }

    @Test
    void formatWithUnixSecondsHandlesNegativeTimestamp() {
        String result = Timestamp.format(Timestamp.Format.SHORT_TIME, -1614556800);
        assertEquals("<t:-1614556800:t>", result);
    }
}