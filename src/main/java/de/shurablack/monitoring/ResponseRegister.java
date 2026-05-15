package de.shurablack.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseRegister {

    private static ResponseRegister instance;

    private final Map<String, List<Double>> responseTimes = new HashMap<>();

    private ResponseRegister() {
        // Private constructor to prevent instantiation
    }

    public static ResponseRegister getInstance() {
        if (instance == null) {
            instance = new ResponseRegister();
        }
        return instance;
    }

    public void trackRunnable(Runnable runnable, String command) {
        long startTime = System.nanoTime();
        runnable.run();
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1.0e6;
        addResponseTime(command, duration);
    }

    public void addResponseTime(String command, double time) {
        responseTimes.computeIfAbsent(command, k -> new java.util.ArrayList<>()).add(time);
        if (responseTimes.get(command).size() > 1000) {
            responseTimes.get(command).remove(0);
        }
    }

    public List<Double> getResponseTimes(String command) {
        return responseTimes.getOrDefault(command, java.util.Collections.emptyList());
    }

    public Map<String, List<Double>> getAllResponseTimes() {
        return responseTimes;
    }

    public void getResponseSummary() {
        final Logger logger = LoggerFactory.getLogger(ResponseRegister.class);

        // get average response time of all keys
        Map<String, Double> averageTimes = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : responseTimes.entrySet()) {
            String command = entry.getKey();
            List<Double> times = entry.getValue();
            double average = times.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            averageTimes.put(command, average);
        }

        StringBuilder builder = new StringBuilder("Response Summary:\n");
        for (Map.Entry<String, Double> entry : averageTimes.entrySet()) {
            String command = entry.getKey();
            double averageTime = entry.getValue();
            builder.append(String.format("%s -> %.2f ms [%s]%n", command, averageTime, responseTimes.get(command).stream()
                    .map(time -> String.format("%.2f", time))
                    .limit(10)
                    .reduce((first, second) -> first + ", " + second)
                    .orElse("No data")));
        }

        logger.info(builder.toString());
    }
}
