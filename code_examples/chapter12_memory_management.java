// Chapter 12: Memory Management - Runnable Java Examples
// Compile/run on Java 17:
//   javac chapter12_memory_management.java
//   java  chapter12_memory_management
//
// BIG PICTURE DIFFERENCE: C++ manual memory vs Java garbage collection
//   - C++: you new/delete, use unique_ptr/shared_ptr/weak_ptr, and rely on
//     deterministic destructors (RAII) for cleanup of memory AND other resources.
//   - Java: ALL objects live on a garbage-collected heap. You never delete;
//     the GC reclaims objects once they become UNREACHABLE. There are no raw
//     pointers and no manual frees, so memory leaks are mostly "lingering
//     references" rather than forgotten deletes.
//   Mapping of concepts:
//     new/delete         -> new + automatic GC (no delete)
//     unique_ptr         -> a plain reference with single, clear ownership
//     shared_ptr         -> ordinary shared references (GC counts reachability)
//     weak_ptr           -> java.lang.ref.WeakReference / SoftReference
//     RAII / destructor  -> try-with-resources + AutoCloseable.close()
//     move semantics     -> not needed; passing a reference is already cheap
//   finalize() exists but is DEPRECATED and unreliable - never use it for cleanup.

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class chapter12_memory_management {

    // ============================================================
    // EXAMPLE 1: No raw pointers - allocation is just 'new', GC frees
    // ============================================================
    static void example1_allocation() {
        System.out.println("\n=== EXAMPLE 1: Allocation (no raw pointers / delete) ===");
        Integer boxed = 42; // heap object, reference held by 'boxed'
        System.out.println("Allocated Integer with value: " + boxed);

        int[] arr = new int[5]; // arrays are objects too
        for (int i = 0; i < 5; i++) arr[i] = i * 10;
        System.out.print("Array: ");
        for (int x : arr) System.out.print(x + " ");
        System.out.println();
        // No delete / delete[]: when 'boxed' and 'arr' go out of scope and
        // become unreachable, the garbage collector reclaims them.
        System.out.println("No manual free needed - GC reclaims unreachable objects");
    }

    // ============================================================
    // EXAMPLE 2: "unique_ptr" -> a single owning reference
    // Java has no ownership transfer keyword; reassigning the old variable
    // to null is the closest analogue to std::move leaving a null behind.
    // ============================================================
    static class Resource implements AutoCloseable {
        private final String id;
        Resource(String id) { this.id = id; System.out.println("Resource " + id + " acquired"); }
        void use() { System.out.println("Using resource " + id); }
        @Override public void close() { System.out.println("Resource " + id + " released"); }
    }

    static void example2_singleOwnership() {
        System.out.println("\n=== EXAMPLE 2: Single Ownership (unique_ptr analogue) ===");
        Resource owner1 = new Resource("A");
        owner1.use();
        Resource owner2 = owner1; // both reference SAME object (no copy)
        owner1 = null;            // "transfer": only owner2 keeps it reachable
        System.out.println("owner1 set to null after transfer");
        owner2.use();
        // GC will reclaim the Resource once owner2 is also unreachable.
        // (For deterministic release use try-with-resources, see Example 6.)
    }

    // ============================================================
    // EXAMPLE 3: "shared_ptr" -> ordinary shared references
    // Java tracks reachability, not an explicit reference count, but the
    // effect is the same: the object lives while any reference reaches it.
    // ============================================================
    static void example3_sharedReferences() {
        System.out.println("\n=== EXAMPLE 3: Shared References (shared_ptr analogue) ===");
        Resource r = new Resource("Shared");
        List<Resource> holders = new ArrayList<>();
        holders.add(r);  // reference 2
        holders.add(r);  // reference 3 (same object)
        System.out.println("References reaching the object: 1 local + " + holders.size() + " in list");
        r.use();
        holders.clear();
        r = null; // now unreachable -> eligible for GC
        System.out.println("All references dropped -> object becomes GC-eligible");
    }

    // ============================================================
    // EXAMPLE 4: Object creation idioms (make_unique/make_shared analogue)
    // In Java you simply call 'new'; there is no separate factory needed for
    // exception safety because there are no raw owning pointers to leak.
    // ============================================================
    static void example4_creation() {
        System.out.println("\n=== EXAMPLE 4: Object Creation (just use 'new') ===");
        Integer value = 100; // == Integer.valueOf(100), may be cached
        System.out.println("Created Integer: " + value);
        Resource res = new Resource("Made");
        res.use();
        System.out.println("'new' is exception-safe in Java; no make_* wrappers needed");
    }

    // ============================================================
    // EXAMPLE 5: WeakReference -> weak_ptr (does not keep object alive)
    // ============================================================
    static void example5_weakReference() {
        System.out.println("\n=== EXAMPLE 5: WeakReference (weak_ptr analogue) ===");
        Object strong = new Object();
        WeakReference<Object> weak = new WeakReference<>(strong);
        System.out.println("Before clearing strong ref, weak.get() != null? " + (weak.get() != null));

        strong = null;     // drop the only strong reference
        System.gc();       // request (not guarantee) a collection
        // After GC the weakly-referenced object may be cleared.
        System.out.println("After GC, weak.get() == null? " + (weak.get() == null)
                + "  (weak refs don't prevent collection)");
        System.out.println("Use WeakReference to break would-be 'circular' ownership / caches");
    }

    // ============================================================
    // EXAMPLE 6: RAII -> try-with-resources + AutoCloseable
    // This is Java's deterministic cleanup mechanism; close() always runs,
    // even on exception, mirroring a C++ destructor at scope exit.
    // ============================================================
    static class FileHandle implements AutoCloseable {
        private final String name;
        FileHandle(String name) { this.name = name; System.out.println("File '" + name + "' opened"); }
        @Override public void close() { System.out.println("File '" + name + "' closed"); }
    }

    static void example6_raii() {
        System.out.println("\n=== EXAMPLE 6: RAII via try-with-resources ===");
        try (FileHandle file = new FileHandle("data.txt")) {
            System.out.println("Working with the file");
        } // close() called automatically here (deterministic, like a destructor)
    }

    // ============================================================
    // EXAMPLE 7: "Move semantics" -> not needed in Java
    // Passing/returning an object only copies a reference (cheap), so there
    // is nothing to move. We show that two references share the same data.
    // ============================================================
    static class BigData {
        final int[] data;
        BigData(int size) { data = new int[size]; System.out.println("BigData allocated (" + size + " ints)"); }
    }

    static void example7_moveSemantics() {
        System.out.println("\n=== EXAMPLE 7: 'Move' Semantics (unnecessary in Java) ===");
        List<BigData> vec = new ArrayList<>();
        BigData temp = new BigData(100);
        vec.add(temp); // stores the reference - no copy of the 100 ints
        System.out.println("Same object in list and 'temp'? " + (vec.get(0) == temp));
        System.out.println("Adding to a collection copies only the reference (already cheap)");
    }

    // ============================================================
    // EXAMPLE 8: Arrays are first-class GC objects (smart_ptr<T[]> analogue)
    // ============================================================
    static void example8_arrays() {
        System.out.println("\n=== EXAMPLE 8: Arrays (managed by GC) ===");
        int[] arr = new int[5];
        for (int i = 0; i < 5; i++) arr[i] = i * 2;
        System.out.print("Array: ");
        for (int x : arr) System.out.print(x + " ");
        System.out.println();
        System.out.println("Array freed automatically when unreachable (no delete[])");
    }

    // ============================================================
    // EXAMPLE 9: Collections of resources + bulk deterministic cleanup
    // ============================================================
    static void example9_resourceContainer() {
        System.out.println("\n=== EXAMPLE 9: Container of Resources ===");
        List<Resource> resources = new ArrayList<>();
        resources.add(new Resource("R1"));
        resources.add(new Resource("R2"));
        resources.add(new Resource("R3"));
        for (Resource res : resources) res.use();
        // For non-memory resources, close them explicitly (GC won't call close()).
        for (Resource res : resources) res.close();
        resources.clear();
        System.out.println("Memory reclaimed by GC; close() handled other resources");
    }

    // ============================================================
    // EXAMPLE 10: SoftReference (memory-sensitive cache) + best practices
    // ============================================================
    static void example10_softRefAndBestPractices() {
        System.out.println("\n=== EXAMPLE 10: SoftReference & Best Practices ===");
        SoftReference<byte[]> cache = new SoftReference<>(new byte[1024]);
        System.out.println("SoftReference cache present? " + (cache.get() != null)
                + "  (kept until memory pressure, good for caches)");

        System.out.println("\nJava memory best practices:");
        System.out.println("  - Let the GC manage memory; never try to 'delete'");
        System.out.println("  - Null out long-lived references you no longer need");
        System.out.println("  - Implement AutoCloseable + use try-with-resources for I/O/handles");
        System.out.println("  - Use WeakReference/SoftReference for caches and listeners");
        System.out.println("  - Do NOT rely on finalize() (deprecated, non-deterministic)");
    }

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("   CHAPTER 12: MEMORY MANAGEMENT (Java 17 / GC)");
        System.out.println("======================================================");

        example1_allocation();
        example2_singleOwnership();
        example3_sharedReferences();
        example4_creation();
        example5_weakReference();
        example6_raii();
        example7_moveSemantics();
        example8_arrays();
        example9_resourceContainer();
        example10_softRefAndBestPractices();

        System.out.println("\n======================================================");
        System.out.println("All examples completed!");
        System.out.println("======================================================");
    }
}
