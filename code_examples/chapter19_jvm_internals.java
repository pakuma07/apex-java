// ============================================================================
// Chapter 19: JVM Internals -- runnable demonstrations
//
// Compile & run:
//     javac chapter19_jvm_internals.java
//     java  chapter19_jvm_internals
//
// This program introspects the *running* JVM using the java.lang.management
// MXBeans and java.lang.* APIs to make the chapter's concepts concrete:
//   1. JVM / runtime identification
//   2. The class-loader hierarchy and parent delegation
//   3. Runtime data areas: heap vs non-heap (metaspace/code cache) memory
//   4. Class-loading statistics (loaded/unloaded counts)
//   5. Garbage collectors actually in use
//   6. Thread / stack information
//   7. Lazy, thread-safe static initialization (when a class is initialized)
//   8. Observing JIT warmup (a hot loop speeds up after compilation)
//
// Everything here uses only the standard library and runs on Java 17+.
// ============================================================================

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;

public class chapter19_jvm_internals {

    public static void main(String[] args) {
        section("1. JVM / Runtime identification");
        identifyRuntime();

        section("2. Class-loader hierarchy (parent delegation)");
        classLoaderHierarchy();

        section("3. Runtime data areas: heap vs non-heap memory");
        memoryAreas();

        section("4. Class-loading statistics");
        classLoadingStats();

        section("5. Garbage collectors in use");
        garbageCollectors();

        section("6. Threads and stacks");
        threadInfo();

        section("7. Lazy, thread-safe static initialization");
        lazyInitializationDemo();

        section("8. JIT warmup (the same loop gets faster once compiled)");
        jitWarmupDemo();
    }

    // ---- 1. Runtime identification ----------------------------------------
    private static void identifyRuntime() {
        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
        System.out.println("JVM name      : " + rt.getVmName());
        System.out.println("JVM vendor    : " + rt.getVmVendor());
        System.out.println("JVM version   : " + rt.getVmVersion());
        System.out.println("Java version  : " + System.getProperty("java.version"));
        System.out.println("Spec version  : " + System.getProperty("java.specification.version"));
        System.out.println("Uptime (ms)   : " + rt.getUptime());
        List<String> jvmArgs = rt.getInputArguments();
        System.out.println("JVM flags     : " + (jvmArgs.isEmpty() ? "(none passed)" : jvmArgs));
    }

    // ---- 2. Class-loader hierarchy ----------------------------------------
    private static void classLoaderHierarchy() {
        // Walk up from this application class's loader to the bootstrap loader.
        ClassLoader cl = chapter19_jvm_internals.class.getClassLoader();
        int level = 0;
        while (cl != null) {
            System.out.println("  ".repeat(level) + "-> " + cl);
            cl = cl.getParent();
            level++;
        }
        System.out.println("  ".repeat(level) + "-> null (bootstrap class loader, native)");

        // Core JDK classes are loaded by the bootstrap loader, so their loader is null.
        System.out.println("Loader of String.class : " + String.class.getClassLoader()
                + "  (null == bootstrap)");
        System.out.println("Loader of this class    : "
                + chapter19_jvm_internals.class.getClassLoader());
    }

    // ---- 3. Memory areas ---------------------------------------------------
    private static void memoryAreas() {
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = mem.getHeapMemoryUsage();
        MemoryUsage nonHeap = mem.getNonHeapMemoryUsage();   // metaspace, code cache, ...
        System.out.println("Heap     used/committed/max : "
                + mb(heap.getUsed()) + " / " + mb(heap.getCommitted()) + " / " + mb(heap.getMax()));
        System.out.println("Non-heap used/committed/max : "
                + mb(nonHeap.getUsed()) + " / " + mb(nonHeap.getCommitted()) + " / " + mb(nonHeap.getMax()));
        System.out.println("Available processors        : " + Runtime.getRuntime().availableProcessors());
    }

    // ---- 4. Class-loading statistics --------------------------------------
    private static void classLoadingStats() {
        ClassLoadingMXBean cls = ManagementFactory.getClassLoadingMXBean();
        System.out.println("Total classes loaded   : " + cls.getTotalLoadedClassCount());
        System.out.println("Currently loaded       : " + cls.getLoadedClassCount());
        System.out.println("Classes unloaded       : " + cls.getUnloadedClassCount());
    }

    // ---- 5. Garbage collectors --------------------------------------------
    private static void garbageCollectors() {
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.println("GC '" + gc.getName() + "' : collections=" + gc.getCollectionCount()
                    + ", totalTime=" + gc.getCollectionTime() + "ms, pools=" + List.of(gc.getMemoryPoolNames()));
        }
    }

    // ---- 6. Threads --------------------------------------------------------
    private static void threadInfo() {
        ThreadMXBean th = ManagementFactory.getThreadMXBean();
        System.out.println("Live threads   : " + th.getThreadCount());
        System.out.println("Peak threads   : " + th.getPeakThreadCount());
        System.out.println("Daemon threads : " + th.getDaemonThreadCount());
    }

    // ---- 7. Lazy static initialization ------------------------------------
    // The static initializer of Heavy runs ONCE, lazily, the first time the
    // class is actively used (here: first access to its static method).
    static class Heavy {
        static { System.out.println("    >> Heavy class is being INITIALIZED now"); }
        static int value() { return 42; }
    }

    private static void lazyInitializationDemo() {
        System.out.println("Before touching Heavy (note: not initialized yet)");
        System.out.println("Calling Heavy.value() ...");
        int v = Heavy.value();              // triggers initialization exactly here
        System.out.println("Got value = " + v);
        System.out.println("Calling again (no re-initialization) ...");
        Heavy.value();                      // static block does NOT run again
    }

    // ---- 8. JIT warmup -----------------------------------------------------
    private static void jitWarmupDemo() {
        long cold = time(() -> busyLoop(2_000_000));   // mostly interpreted
        long warm1 = time(() -> busyLoop(2_000_000));
        long warm2 = time(() -> busyLoop(2_000_000));  // likely JIT-compiled by now
        System.out.println("cold run  : " + cold / 1000 + " us");
        System.out.println("warm run 1: " + warm1 / 1000 + " us");
        System.out.println("warm run 2: " + warm2 / 1000 + " us");
        System.out.println("(The warm runs are usually faster once the JIT compiles the loop.)");
        System.out.println("(Run with -XX:+PrintCompilation to watch methods being compiled.)");
    }

    // A trivial CPU-bound loop. 'volatile' sink prevents the JIT from deleting it.
    private static volatile long sink;
    private static void busyLoop(int n) {
        long acc = 0;
        for (int i = 0; i < n; i++) acc += (i * 31L) ^ (i >> 1);
        sink = acc;
    }

    // ---- helpers -----------------------------------------------------------
    private static long time(Runnable r) {
        long t0 = System.nanoTime();
        r.run();
        return System.nanoTime() - t0;
    }

    private static String mb(long bytes) {
        if (bytes < 0) return "n/a";        // max can be undefined (-1)
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }
}
