package de.shurablack.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Thread-safe singleton that records execution times in milliseconds per command key.
 * <br><br>
 * Only the latest 1000 times per key are kept. The event dispatching uses keys of the form
 * {@code "<event type>#<identifier>"}.
 * </p>
 *
 * @author ShuraBlack
 */
public class ResponseRegister {

    /** The singleton instance */
    private static final ResponseRegister INSTANCE = new ResponseRegister();

    /** Recorded times in milliseconds per key; each list is synchronized and ordered from oldest to newest */
    private final Map<String, List<Double>> responseTimes = new ConcurrentHashMap<>();

    private ResponseRegister() {
        // Private constructor to prevent instantiation
    }

    /** @return the singleton instance */
    public static ResponseRegister getInstance() {
        return INSTANCE;
    }

    /**
     * Runs the runnable on the calling thread and records its execution time, also if it throws.
     * @param runnable the task to run
     * @param command the key to record the time under
     */
    public void trackRunnable(Runnable runnable, String command) {
        long startTime = System.nanoTime();
        try {
            runnable.run();
        } finally {
            addResponseTime(command, (System.nanoTime() - startTime) / 1.0e6);
        }
    }

    /**
     * Records a time for the key, dropping the oldest entry once more than 1000 are stored.
     * @param command the key to record the time under
     * @param time the execution time in milliseconds
     */
    public void addResponseTime(String command, double time) {
        List<Double> times = responseTimes.computeIfAbsent(command, k -> Collections.synchronizedList(new ArrayList<>()));
        synchronized (times) {
            times.add(time);
            if (times.size() > 1000) {
                times.remove(0);
            }
        }
    }

    /**
     * @param command the key
     * @return a copy of the recorded times in milliseconds, oldest first; empty if the key is unknown
     */
    public List<Double> getResponseTimes(String command) {
        List<Double> times = responseTimes.getOrDefault(command, Collections.emptyList());
        synchronized (times) {
            return new ArrayList<>(times);
        }
    }

    /**
     * Returns the internal map, not a copy. The lists are synchronized; hold their lock while iterating.
     * @return the live map from key to recorded times in milliseconds
     */
    public Map<String, List<Double>> getAllResponseTimes() {
        return responseTimes;
    }

    /**
     * Logs on info level the average time of every key followed by its first (oldest retained) ten times.
     * Despite its name, this method returns nothing.
     */
    public void getResponseSummary() {
        final Logger logger = LoggerFactory.getLogger(ResponseRegister.class);

        Map<String, Double> averageTimes = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : responseTimes.entrySet()) {
            String command = entry.getKey();
            List<Double> times = getResponseTimes(command);
            double average = times.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            averageTimes.put(command, average);
        }

        StringBuilder builder = new StringBuilder("Response Summary:\n");
        for (Map.Entry<String, Double> entry : averageTimes.entrySet()) {
            String command = entry.getKey();
            double averageTime = entry.getValue();
            builder.append(String.format("%s -> %.2f ms [%s]%n", command, averageTime, getResponseTimes(command).stream()
                    .map(time -> String.format("%.2f", time))
                    .limit(10)
                    .reduce((first, second) -> first + ", " + second)
                    .orElse("No data")));
        }

        logger.info(builder.toString());
    }
}
