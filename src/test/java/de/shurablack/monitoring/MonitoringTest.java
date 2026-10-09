package de.shurablack.monitoring;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests, the values depend on the running JVM
 */
class MonitoringTest {

    @Test
    void responseTimesAreRecordedAndCapped() {
        final ResponseRegister register = ResponseRegister.getInstance();
        register.trackRunnable(() -> { }, "test#tracked");
        assertEquals(1, register.getResponseTimes("test#tracked").size());

        // Failing tasks are recorded too
        assertThrows(IllegalStateException.class,
                () -> register.trackRunnable(() -> { throw new IllegalStateException(); }, "test#tracked"));
        assertEquals(2, register.getResponseTimes("test#tracked").size());

        for (int i = 0; i < 1_100; i++) {
            register.addResponseTime("test#capped", i);
        }
        final List<Double> capped = register.getResponseTimes("test#capped");
        assertEquals(1_000, capped.size());
        assertEquals(100.0, capped.get(0), "the oldest entries get dropped");

        assertTrue(register.getResponseTimes("test#unknown").isEmpty());
        assertDoesNotThrow(register::getResponseSummary);
    }

    @Test
    void trackerProvidesSystemAndLiveInfo() {
        final MBeansTracker tracker = MBeansTracker.get();
        final JSONObject system = tracker.getSystemInfo();
        final JSONObject live = tracker.getLiveInfo();

        assertFalse(system.isEmpty());
        assertFalse(live.isEmpty());
    }

    @Test
    void vmReports() {
        assertTrue(VM.getMemoryUsage().contains("Heap Memory"));
        assertTrue(VM.getProcessLoad().startsWith("CPU-load"));
    }
}
