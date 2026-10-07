package de.shurablack.core.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

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
        return new ClassGraph().enableAllInfo().acceptPackages(packages).scan();
    }
}
