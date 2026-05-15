package de.shurablack.monitoring;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.management.*;
import com.sun.management.OperatingSystemMXBean;

public class MBeansTracker {

    private static MBeansTracker instance;

    private final RuntimeMXBean runtime;

    private final MemoryMXBean memory;

    private final ThreadMXBean threads;

    private final OperatingSystemMXBean os;

    private final GarbageCollectorMXBean[] garbageCollectors;

    private JSONObject systemInfo;

    private MBeansTracker() {
        this.runtime = ManagementFactory.getRuntimeMXBean();
        this.memory = ManagementFactory.getMemoryMXBean();
        this.threads = ManagementFactory.getThreadMXBean();
        this.os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans().toArray(new GarbageCollectorMXBean[0]);
    }

    protected static MBeansTracker get() {
        if (instance == null) {
            instance = new MBeansTracker();
        }
        return instance;
    }

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
