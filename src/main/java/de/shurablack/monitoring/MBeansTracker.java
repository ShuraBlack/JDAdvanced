package de.shurablack.monitoring;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.management.*;
import com.sun.management.OperatingSystemMXBean;

/**
 * <p>
 * Collects JVM and operating system metrics from the platform MXBeans as {@link JSONObject}s.
 * <br><br>
 * Used by {@link VMServer} to send static system information once per client and live data periodically.
 * </p>
 *
 * @author ShuraBlack
 */
public class MBeansTracker {

    /** Lazily created singleton */
    private static MBeansTracker instance;

    private final RuntimeMXBean runtime;

    private final MemoryMXBean memory;

    private final ThreadMXBean threads;

    private final OperatingSystemMXBean os;

    private final GarbageCollectorMXBean[] garbageCollectors;

    /** Cached result of {@link #getSystemInfo()}, {@code null} until the first call */
    private JSONObject systemInfo;

    private MBeansTracker() {
        this.runtime = ManagementFactory.getRuntimeMXBean();
        this.memory = ManagementFactory.getMemoryMXBean();
        this.threads = ManagementFactory.getThreadMXBean();
        this.os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans().toArray(new GarbageCollectorMXBean[0]);
    }

    /**
     * Returns the singleton, creating it on the first call. The lazy creation isn't synchronized.
     * @return the tracker instance
     */
    protected static MBeansTracker get() {
        if (instance == null) {
            instance = new MBeansTracker();
        }
        return instance;
    }

    /**
     * Returns JVM, operating system and class loading information.
     * <br><br>
     * The object is built on the first call and cached afterwards, so the class loading counts are a snapshot
     * of that moment. {@code jvmStartTime} is in epoch milliseconds.
     * @return the cached system information with the sections {@code jvm}, {@code os} and {@code classLoading}
     */
    public JSONObject getSystemInfo() {
        if (this.systemInfo != null) {
            return this.systemInfo;
        }

        JSONObject systemInfo = new JSONObject();

        JSONObject jvm = new JSONObject();
        jvm.put("jvmName", this.runtime.getVmName());
        jvm.put("jvmVersion", this.runtime.getVmVersion());
        jvm.put("jvmVendor", this.runtime.getVmVendor());
        jvm.put("jvmStartTime", this.runtime.getStartTime());
        jvm.put("jvmPid", this.runtime.getPid());
        systemInfo.put("jvm", jvm);

        JSONObject os = new JSONObject();
        os.put("osName", this.os.getName());
        os.put("osVersion", this.os.getVersion());
        os.put("osArch", this.os.getArch());
        os.put("availableProcessors", this.os.getAvailableProcessors());
        systemInfo.put("os", os);

        ClassLoadingMXBean clBean = ManagementFactory.getClassLoadingMXBean();
        JSONObject classLoading = new JSONObject();
        classLoading.put("loadedClassCount", clBean.getLoadedClassCount());
        classLoading.put("totalLoadedClassCount", clBean.getTotalLoadedClassCount());
        classLoading.put("unloadedClassCount", clBean.getUnloadedClassCount());
        systemInfo.put("classLoading", classLoading);

        this.systemInfo = systemInfo;

        return systemInfo;
    }

    /**
     * Returns a fresh snapshot of uptime, process CPU load, memory, thread and garbage collector metrics.
     * <br><br>
     * Memory values are in bytes ({@code -1} if undefined), times in milliseconds. {@code processCpuLoad} is in
     * the range 0.0 to 1.0 across all CPUs, or negative if unavailable.
     * @return a new object with the keys {@code uptimeMs}, {@code os}, {@code memory}, {@code threads}
     *         and {@code garbageCollectors}
     */
    public JSONObject getLiveInfo() {
        JSONObject liveInfo = new JSONObject();
        liveInfo.put("uptimeMs", this.runtime.getUptime());

        JSONObject osInfo = new JSONObject();
        osInfo.put("processCpuLoad", this.os.getProcessCpuLoad());
        liveInfo.put("os", osInfo);

        JSONObject memory = new JSONObject();
        MemoryUsage heapMemoryUsage = this.memory.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = this.memory.getNonHeapMemoryUsage();
        memory.put("heapInit", heapMemoryUsage.getInit());
        memory.put("heapUsed", heapMemoryUsage.getUsed());
        memory.put("heapCommitted", heapMemoryUsage.getCommitted());
        memory.put("heapMax", heapMemoryUsage.getMax());
        memory.put("nonHeapInit", nonHeapMemoryUsage.getInit());
        memory.put("nonHeapUsed", nonHeapMemoryUsage.getUsed());
        memory.put("nonHeapCommitted", nonHeapMemoryUsage.getCommitted());
        memory.put("nonHeapMax", nonHeapMemoryUsage.getMax());
        memory.put("objectPendingFinalizationCount", this.memory.getObjectPendingFinalizationCount());
        liveInfo.put("memory", memory);

        JSONObject threads = new JSONObject();
        threads.put("threadCount", this.threads.getThreadCount());
        threads.put("peakThreadCount", this.threads.getPeakThreadCount());
        threads.put("daemonThreadCount", this.threads.getDaemonThreadCount());
        threads.put("totalStartedThreadCount", this.threads.getTotalStartedThreadCount());
        liveInfo.put("threads", threads);

        JSONArray gcArray = new JSONArray();
        for (GarbageCollectorMXBean gc : this.garbageCollectors) {
            JSONObject gcInfo = new JSONObject();
            gcInfo.put("name", gc.getName());
            gcInfo.put("collectionCount", gc.getCollectionCount());
            gcInfo.put("collectionTimeMs", gc.getCollectionTime());
            gcArray.put(gcInfo);
        }
        liveInfo.put("garbageCollectors", gcArray);

        return liveInfo;
    }
}
