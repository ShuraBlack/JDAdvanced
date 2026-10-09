package de.shurablack.monitoring;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * <p>
 * Builds human-readable reports of the memory usage and CPU load of the running JVM, e.g. for logging.
 * </p>
 *
 * @author ShuraBlack
 */
public class VM {

    /**
     * Builds a multi-line report of the {@link Runtime} memory and the heap/non-heap usage of the memory MXBean.
     * <br><br>
     * Values are in whole megabytes (truncated); an undefined maximum is reported as {@code 0 MB}.
     * @return the memory report
     */
    public static String getMemoryUsage() {
        final StringBuilder memoryUsage = new StringBuilder();
        Runtime runtime = Runtime.getRuntime();
        memoryUsage.append("=== Runtime Memory ===\n");
        memoryUsage.append("\nMax Memory (JVM Limit): ").append(runtime.maxMemory() / (1024 * 1024)).append(" MB");
        memoryUsage.append("\nTotal Memory (Allocated): ").append(runtime.totalMemory() / (1024 * 1024)).append(" MB");
        memoryUsage.append("\nFree Memory (Unused): ").append(runtime.freeMemory() / (1024 * 1024)).append(" MB");
        memoryUsage.append("\nUsed Memory: ").append((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)).append(" MB");

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        memoryUsage.append("\n=== Memory MXBean ===");
        memoryUsage.append("\nHeap Memory:");
        memoryUsage.append("\n  Init: ").append(heapMemoryUsage.getInit() / (1024 * 1024)).append(" MB");
        memoryUsage.append("\n  Used: ").append(heapMemoryUsage.getUsed() / (1024 * 1024)).append(" MB");
        memoryUsage.append("\n  Committed: ").append(heapMemoryUsage.getCommitted() / (1024 * 1024)).append(" MB");
        memoryUsage.append("\n  Max: ").append(heapMemoryUsage.getMax() / (1024 * 1024)).append(" MB");

        memoryUsage.append("\nNon-Heap Memory:");
        memoryUsage.append("\n  Init: ").append(nonHeapMemoryUsage.getInit() / (1024 * 1024)).append(" MB");
        memoryUsage.append("\n  Used: ").append(nonHeapMemoryUsage.getUsed() / (1024 * 1024)).append(" MB");
        memoryUsage.append("\n  Committed: ").append(nonHeapMemoryUsage.getCommitted() / (1024 * 1024)).append(" MB");
        memoryUsage.append("\n  Max: ").append(nonHeapMemoryUsage.getMax() / (1024 * 1024)).append(" MB");

        return memoryUsage.toString();
    }

    /**
     * Describes the recent CPU usage of the JVM process as a percentage of all CPUs (unrounded).
     * @return the CPU load text, or a notice if the value isn't available
     */
    public static String getProcessLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

        double processCpuLoad = osBean.getProcessCpuLoad();

        if (processCpuLoad >= 0) {
            return "CPU-load of the application: " + (processCpuLoad * 100) + "%";
        }
        return "CPU-load of the application is not available.";
    }
}
