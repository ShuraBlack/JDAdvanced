package de.shurablack.core.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * Shared entry point for the classpath scans (event workers, cron tasks, interaction commands).
 * <br><br>
 * By default the whole classpath gets scanned, including all dependencies. Restrict it to the
 * packages of your bot via {@link de.shurablack.core.builder.UtilBuilder#init(String...)} to speed up the startup.
 * </p>
 *
 * @version core-1.1.0
 * @date 07.10.2026
 * @author ShuraBlack
 */
public final class ClassScan {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassScan.class);

    /** The hint about the whole classpath scan gets logged once */
    private static final AtomicBoolean HINTED = new AtomicBoolean(false);

    /** Packages to scan, empty string means the whole classpath */
    private static volatile String[] packages = {""};

    private ClassScan() { }

    /**
     * Restricts the scans to the given packages (including sub packages)
     * @param basePackages the packages to scan, none for the whole classpath
     */
    public static void setPackages(final String... basePackages) {
        packages = basePackages.length == 0 ? new String[]{""} : basePackages.clone();
    }

    /**
     * Scans the configured packages. The result needs to be closed
     * @return the {@link ScanResult}
     */
    public static ScanResult scan() {
        final String[] scanned = packages;
        if (scanned.length == 1 && scanned[0].isEmpty() && !HINTED.getAndSet(true)) {
            LOGGER.info("Scanning the whole classpath including all dependencies. "
                    + "Restrict it to your packages via UtilBuilder.init(\"de.mybot\") for a faster startup");
        }
        return new ClassGraph().enableAllInfo().acceptPackages(scanned).scan();
    }
}
