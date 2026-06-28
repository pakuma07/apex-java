# Chapter 12: Memory Management and Garbage Collection

Memory management is where Java differs most fundamentally from C++. In C++ *you* decide when and where objects are allocated and freed: `new`/`delete`, manual ownership, and the RAII idiom that ties cleanup to object lifetimes. In Java the rule is inverted — you allocate objects with `new`, but you **never free them**. A subsystem called the **garbage collector (GC)** automatically reclaims objects once they become unreachable. This eliminates the two classic C++ bugs at a stroke: there are no dangling pointers (you cannot free something the runtime still considers live) and no double-frees (you cannot free anything at all). But it introduces new concepts you must understand to write correct, performant Java: the heap regions and generations, GC algorithms (G1, ZGC, Parallel), *reachability* and GC roots, the kinds of references (strong/soft/weak/phantom), and a subtler class of "leaks" — objects that stay reachable longer than intended.

This chapter mirrors the C++ memory chapter section by section, replacing each manual mechanism with its Java automatic counterpart and noting the contrast explicitly. The guiding principle inverts RAII: instead of "tie every resource to an object's destructor," Java's rule is "the GC handles *memory*, but you must still manage *non-memory* resources (files, sockets, locks) explicitly" — and the tool for that is **try-with-resources** / `AutoCloseable`, the nearest thing Java has to RAII.

> **C++ manual memory ↔ Java managed memory — at a glance**
> - C++ `new` + `delete` ↔ Java `new` + **GC** (no `delete`, no `free`)
> - C++ stack objects with deterministic destructors ↔ Java: **all objects on the heap**, no stack objects, non-deterministic finalization
> - C++ RAII (destructor releases resource) ↔ Java try-with-resources / `AutoCloseable.close()`
> - C++ `unique_ptr`/`shared_ptr`/`weak_ptr` ↔ Java references are already "shared"; `WeakReference`/`SoftReference`/`PhantomReference` for special cases
> - C++ dangling pointer / double-free / leak-by-forgetting-delete ↔ **impossible in Java**; Java's "leak" is *unintended reachability*
> - C++ destructor ↔ Java has **no destructor**; `finalize()` is deprecated, use `Cleaner`

## 12.1 Stack vs Heap (and Metaspace)

A running Java program (the JVM) divides memory into regions. The **stack** is per-thread and holds *frames* for method calls — each frame stores local variables, including **references** (pointers) to objects and primitive locals (`int`, `double`). Allocation is a pointer bump; the frame is popped automatically when the method returns. The **heap** is the single shared region where **every object lives** — there are *no* stack-allocated objects in Java. When you write `new Foo()`, the object is created on the heap and the variable on the stack holds only a *reference* to it. A third region, **Metaspace** (native memory, replaced the old PermGen in Java 8), holds class metadata — the loaded `Class` objects, method bytecode, and runtime constant pools.

```java
// Stack holds primitives and REFERENCES; objects live on the heap.
void stackExample() {
    int x = 10;                       // primitive on the stack frame
    int[] arr = new int[100];         // 'arr' (a reference) on stack; the array on the HEAP
    List<Integer> v = new ArrayList<>(); // 'v' on stack; ArrayList + backing array on HEAP
}   // frame popped: x and the references vanish; the heap objects become unreachable
    // -> eligible for GC (reclaimed later, not immediately)

// There is no manual heap allocation/free. This:
int[] big = new int[1_000_000];       // allocate on heap
// ...use it...
// (no delete[] — when 'big' goes out of scope and nothing else points to the array,
//  the GC reclaims it)
```

| Region | Holds | C++ analogue |
|--------|-------|--------------|
| **Stack** (per thread) | frames: primitive locals + object references | C++ stack (but C++ also puts whole objects here) |
| **Heap** (shared) | **all objects and arrays** | C++ heap / free store |
| **Metaspace** (native) | class metadata, bytecode, constant pool | (no direct equivalent; vaguely like static/code segment) |

> **Difference:** In C++ you choose stack vs heap per object and get deterministic, scope-bound destruction on the stack. In Java *every object is on the heap*; only primitives and references sit on the stack, and object reclamation is deferred to the GC, not tied to scope exit.

---

## 12.2 Object Allocation (`new` without `delete`)

`new` in Java allocates and runs the constructor, returning a reference — just like C++. The difference is the other half: there is **no `delete`**. You never deallocate. Allocation is fast (typically a bump-the-pointer in a thread-local allocation buffer in the young generation), and the object is freed automatically once the GC proves it is unreachable. There are also no arrays-vs-single distinctions to match (`new`/`new[]` vs `delete`/`delete[]`): `new int[10]` is reclaimed exactly like any other object.

```java
// Single object — allocate, use; never free
Integer boxed = Integer.valueOf(42);   // (or just: int x = 42; for a primitive)

// Object with a constructor (C++: Person* p = new Person("Alice"); delete p;)
class Person {
    private final String name;
    Person(String n) { name = n; System.out.println("Created " + n); }
    // NO destructor. There is no ~Person().
}

Person p = new Person("Alice");    // constructor runs
p = null;                          // drop the reference -> object becomes unreachable
                                   // GC will reclaim it at some unspecified later time
                                   // (no "Destroyed" message — Java has no destructor)

// Array — same story, no delete[]
int[] arr = new int[10];
arr[0] = 5;
// arr eligible for GC when no longer reachable
```

> **Difference:** The C++ "match `new` with `delete`, `new[]` with `delete[]`, then set to `nullptr`" discipline simply does not exist in Java. Setting a reference to `null` only *hints* the object may be collectible; it does not free anything immediately.

---

## 12.3 Reachability, GC Roots, and How Collection Works

The GC reclaims an object when it is no longer **reachable** — when no chain of references leads to it from a **GC root**. The roots are the things the program can definitely still reach: local variables and parameters on every thread's stack, active method operands, static fields of loaded classes, JNI references, and live threads. Starting from the roots the collector traces the object graph; everything reached is *live*, everything else is *garbage* and its memory is reclaimed. This is the **mark-and-sweep** family of algorithms (with compaction/copying refinements). Crucially, reachability — not scope, not reference count by itself — decides liveness, which is why two objects referencing each other are *both* collected if nothing external reaches them (no cycle leak, unlike naive reference counting).

```
            GC ROOTS                         HEAP
   ┌───────────────────────┐
   │ thread stack locals    │── ref ──▶ [ Order ] ── ref ──▶ [ Customer ]   (LIVE)
   │ static fields          │── ref ──▶ [ Config ]                          (LIVE)
   │ active threads / JNI   │
   └───────────────────────┘

            [ TempA ] ◀── ref ──▶ [ TempB ]   (reference each other, but
                                               NO root reaches them)  ── GARBAGE
                                               (both collected — cycles are fine)
```

```java
// Reachability example
List<String> a = new ArrayList<>();   // reachable via local 'a' (a GC root chain)
List<String> b = new ArrayList<>();
a.add("x"); b.add("y");

a = b;        // the first ArrayList ["x"] now has NO reference -> unreachable -> garbage
b = null;     // the second list still reachable via 'a'; setting b=null is fine

// Cyclic references do NOT leak in Java (unlike reference-counting schemes):
class Node { Node other; }
Node n1 = new Node(), n2 = new Node();
n1.other = n2; n2.other = n1;     // cycle
n1 = null; n2 = null;             // no root reaches either -> BOTH collected
```

> **Difference:** C++ `shared_ptr` uses *reference counting*, so a cycle of two `shared_ptr`s never reaches count zero and leaks (the reason `weak_ptr` exists). Java's *tracing* GC reclaims unreachable cycles automatically — there is no cycle-leak problem and thus no need for a `weak_ptr` to break ownership cycles.

---

## 12.4 Generational GC: Young and Old, Minor and Major

The heap is split into **generations** based on the *weak generational hypothesis*: most objects die young. Newly allocated objects go into the **young generation** (subdivided into *Eden* and two *Survivor* spaces); objects that survive several collections are *promoted* to the **old (tenured) generation**. A **minor GC** collects only the young generation — frequent, fast, and using a *copying* collector that compacts survivors. A **major GC** (or full GC) collects the old generation — rarer but more expensive. Because young-gen collection is cheap and most garbage is young, this design keeps typical GC overhead low.

```
        YOUNG GENERATION                       OLD (TENURED) GENERATION
  ┌──────────┬─────────┬─────────┐         ┌──────────────────────────────┐
  │   Eden   │ Survivor│ Survivor│  ──promote──▶  long-lived objects       │
  │ (new objs)│   S0   │   S1    │         │ (survived many minor GCs)     │
  └──────────┴─────────┴─────────┘         └──────────────────────────────┘
       ▲                                              ▲
   allocation here                          full/major GC scans here
   (minor GC: fast, frequent)               (major GC: slower, rarer)
```

```java
// Allocation pattern that demonstrates generations:
for (int i = 0; i < 1_000_000; i++) {
    byte[] shortLived = new byte[1024];   // dies almost immediately -> collected by MINOR GC in Eden
    use(shortLived);
}

static final List<byte[]> cache = new ArrayList<>();  // 'cache' is a GC root (static)
void warmUp() {
    cache.add(new byte[1_000_000]);       // survives -> eventually PROMOTED to old gen
}
```

> **Difference:** C++ has no generations and no automatic reclamation at all — every allocation's lifetime is your responsibility. Java's generational split is an internal optimization you mostly observe only through GC logs and tuning flags.

---

## 12.5 GC Algorithms (G1, ZGC, Parallel)

Modern JVMs ship several collectors, selected by JVM flags. They trade throughput against pause time:

- **G1 (Garbage-First)** — the **default** since Java 9. Divides the heap into many equal-size *regions* and collects the regions with the most garbage first, aiming for soft, predictable pause-time targets. Good general-purpose, balanced choice.
- **Parallel GC** — throughput-oriented: uses multiple threads for stop-the-world young and old collections. Maximizes total work done at the cost of longer individual pauses. Good for batch jobs.
- **ZGC** (and **Shenandoah**) — **low-latency**, mostly-concurrent collectors with pause times typically **under a millisecond even on multi-terabyte heaps**. They do marking and relocation concurrently with the application. Ideal for large-heap, latency-sensitive services.
- **Serial GC** — single-threaded; for small heaps / single-core / containers.
- **Epsilon GC** — a "no-op" collector that never reclaims; for short-lived, allocation-bounded benchmarks/jobs.

```bash
# Select a collector with a JVM flag (no code changes needed):
java -XX:+UseG1GC        MyApp        # G1 (default — flag optional)
java -XX:+UseParallelGC  MyApp        # throughput
java -XX:+UseZGC         MyApp        # low-latency, large heaps
java -XX:+UseSerialGC    MyApp        # tiny heaps / single core
```

| Collector | Optimizes for | Typical use |
|-----------|---------------|-------------|
| **G1** (default) | balanced, predictable pauses | general server/desktop apps |
| **Parallel** | throughput | batch / data-processing jobs |
| **ZGC / Shenandoah** | ultra-low pause (<1 ms) | large-heap latency-sensitive services |
| **Serial** | simplicity, small footprint | tiny heaps, single core, containers |

> **Difference:** Choosing memory behavior in C++ means choosing allocators / smart-pointer strategies *in code*. In Java you swap the entire reclamation strategy with a single launch flag, with no source changes.

---

## 12.6 No Destructors: `finalize()` is Deprecated, use `Cleaner`

C++ guarantees a destructor runs deterministically when an object's lifetime ends — the cornerstone of RAII. Java has **no destructors**. The historical `Object.finalize()` method (called by the GC before reclaiming an object) is **deprecated for removal** (since Java 9) and must not be used: it runs at an unpredictable time (or never), can resurrect objects, hurts performance, and is unreliable for releasing resources. The supported replacement for "do something when an object is collected" is **`java.lang.ref.Cleaner`**, which registers a cleanup action run after the object becomes phantom-reachable — but even this is a *safety net*, not a primary cleanup mechanism. For deterministic resource release, use try-with-resources (12.8), not the GC.

```java
import java.lang.ref.Cleaner;

// ❌ DO NOT use finalize() — deprecated, non-deterministic, unsafe:
//    protected void finalize() { closeNativeHandle(); }   // never do this

// ✅ Cleaner: a safety-net cleanup action, run when the object is unreachable
class NativeResource implements AutoCloseable {
    private static final Cleaner CLEANER = Cleaner.create();

    // The cleanup state MUST NOT reference the outer object (else it stays reachable forever)
    private record State(long handle) implements Runnable {
        public void run() { /* release native handle here */ System.out.println("cleaned " + handle); }
    }

    private final State state;
    private final Cleaner.Cleanable cleanable;

    NativeResource(long handle) {
        this.state = new State(handle);
        this.cleanable = CLEANER.register(this, state);  // run state.run() when 'this' is gone
    }

    @Override public void close() { cleanable.clean(); }  // deterministic cleanup (preferred)
}
```

> **Difference:** A C++ destructor runs *deterministically* at scope end. A Java `Cleaner` action runs at an *unpredictable* future time (or not at all before exit). Never rely on it for correctness — always provide an explicit `close()` and use try-with-resources.

---

## 12.7 Reference Types: Strong, Soft, Weak, Phantom

Ordinary Java references are **strong** — as long as a strong reference exists, the object cannot be collected. The `java.lang.ref` package adds three weaker reference types that let the GC reclaim an object under specified conditions. These are Java's specialized counterparts to C++ `weak_ptr` (non-owning observation) and cache-eviction patterns.

- **`SoftReference`** — the referent is kept *until the JVM is low on memory*, then collected. Ideal for **memory-sensitive caches**.
- **`WeakReference`** — the referent is collected at the **next GC** if only weakly reachable. Used for canonicalizing maps and to avoid keeping keys alive (`WeakHashMap`). This is the closest analogue to `weak_ptr`'s "observe without owning."
- **`PhantomReference`** — never returns the referent (`.get()` is always `null`); used only to schedule post-mortem cleanup via a `ReferenceQueue` (the mechanism `Cleaner` is built on).

```java
import java.lang.ref.*;
import java.util.*;

// SoftReference: a cache the GC may evict only under memory pressure
SoftReference<byte[]> cache = new SoftReference<>(new byte[10_000_000]);
byte[] data = cache.get();         // non-null until the JVM needs the memory
if (data == null) data = recompute();   // evicted -> rebuild

// WeakReference: observe an object without keeping it alive (like weak_ptr.lock())
Object target = new Object();
WeakReference<Object> weak = new WeakReference<>(target);
Object got = weak.get();           // the object if still alive, else null
target = null;                     // drop the strong ref -> weak.get() may now return null

// WeakHashMap: entries vanish automatically when keys become unreachable
Map<Object, String> meta = new WeakHashMap<>();
// keys not referenced elsewhere are GC'd, removing their entries

// PhantomReference: post-mortem cleanup notification via a ReferenceQueue
ReferenceQueue<Object> queue = new ReferenceQueue<>();
PhantomReference<Object> phantom = new PhantomReference<>(new Object(), queue);
// phantom.get() is ALWAYS null; poll 'queue' to learn the referent was collected
```

| Reference | Collected when | C++ analogue |
|-----------|----------------|--------------|
| **Strong** (normal) | never while reachable | `shared_ptr` / raw owning ptr |
| **SoftReference** | JVM low on memory | (no direct equiv; cache pattern) |
| **WeakReference** | next GC if only weakly reachable | `weak_ptr` (observe, don't own) |
| **PhantomReference** | after finalization, pre-reclaim | (no direct equiv; cleanup hook) |

> **Difference:** C++ uses `weak_ptr` mainly to *break ownership cycles* (which Java's tracing GC already handles). Java's weak/soft references instead solve *cache eviction* and *avoid-keeping-keys-alive* problems — different motivations for superficially similar tools.

---

## 12.8 try-with-resources and `AutoCloseable` (Java's RAII)

RAII — acquire a resource in a constructor, release it in a destructor — is the central C++ idiom and works because C++ destructors are deterministic. Java's GC is *not* deterministic, so it cannot release files, sockets, or locks promptly. The Java answer is **try-with-resources**: any object implementing `AutoCloseable` declared in the `try (...)` header has its `close()` called automatically when the block exits — normally *or* via exception — exactly like a C++ destructor unwinding the stack. This is Java's closest equivalent to RAII for *non-memory* resources (memory itself is the GC's job).

```java
import java.io.*;
import java.util.concurrent.locks.*;

// ✅ try-with-resources: close() runs automatically on ANY exit (Java's RAII)
void processFile(String path) throws IOException {
    try (BufferedReader r = new BufferedReader(new FileReader(path))) {
        String line = r.readLine();
        // ... use the file ...
    }   // r.close() called here — even if readLine() threw (cf. C++ ~File())
}

// Multiple resources close in REVERSE order of declaration (like C++ stack unwinding)
try (FileInputStream in = new FileInputStream("a");
     FileOutputStream out = new FileOutputStream("b")) {
    in.transferTo(out);
}   // out.close() then in.close()

// Implement AutoCloseable for your own resources (the RAII pattern in Java)
class File implements AutoCloseable {
    private final long handle;
    File(String name) {
        handle = open(name);                       // acquire (like C++ constructor)
        if (handle == 0) throw new RuntimeException("Cannot open file");
    }
    @Override public void close() { release(handle); }   // release (like C++ destructor)
    private static long open(String n) { return 1; }
    private static void release(long h) { /* close native handle */ }
}

// Locks: try-with-resources doesn't fit lock/unlock directly; the idiom is try/finally
// (the analogue of C++ std::lock_guard)
Lock lock = new ReentrantLock();
lock.lock();
try {
    // critical section
} finally {
    lock.unlock();      // guaranteed release, like lock_guard's destructor
}
```

| C++ RAII | Java equivalent |
|----------|-----------------|
| Destructor releases on scope exit | `AutoCloseable.close()` via try-with-resources |
| `std::lock_guard` / `std::unique_lock` | `lock.lock()` + `finally { lock.unlock(); }` |
| `std::fstream` (closes in dtor) | `try (var r = new FileReader(...))` |
| Smart pointer frees memory | (not needed — GC frees memory) |

> **Difference:** C++ RAII covers *both* memory and other resources with one mechanism (the destructor). Java splits them: the GC owns *memory*, while try-with-resources / `AutoCloseable` owns *files, sockets, locks, and native handles*. Forgetting to `close()` a resource is the Java analogue of forgetting `delete` — but it leaks an OS handle, not heap memory.

---

## 12.9 Memory Leaks in Java (Unintended Reachability)

Java cannot leak memory by "forgetting to free" — but it *can* leak by **keeping objects reachable longer than intended**. As long as a GC root chain reaches an object, the GC must keep it, even if your program will never use it again. The classic culprits: objects accumulated in a long-lived `static` collection (a cache that never evicts), listeners/callbacks registered but never unregistered, and `ThreadLocal` values on long-lived threads. The fix is to *drop references* you no longer need (or use a `WeakHashMap`/`SoftReference` cache that the GC can prune).

```java
import java.util.*;

// ❌ Leak: a static collection grows forever — every entry stays reachable
class Registry {
    private static final List<Object> ALL = new ArrayList<>();  // static = GC root
    static void register(Object o) { ALL.add(o); }              // never removed -> grows unbounded
}

// ❌ Leak: listener registered but never removed keeps the listener (and its captured
//    references) alive for the lifetime of the event source.

// ✅ Fix 1: remove when done
class Registry2 {
    private static final List<Object> ALL = new ArrayList<>();
    static void register(Object o) { ALL.add(o); }
    static void unregister(Object o) { ALL.remove(o); }   // drop the reference
}

// ✅ Fix 2: a cache the GC can prune automatically
Map<Key, Value> cache = new WeakHashMap<>();   // entries vanish when keys unreachable
// or SoftReference values for memory-sensitive caches
```

Leaks are diagnosed with heap-analysis tools (`jmap`, VisualVM, Eclipse MAT) rather than Valgrind/AddressSanitizer — you look for the *retained set* and the *GC root path* keeping an object alive, the mirror image of C++'s "who forgot to free this?"

> **Difference:** A C++ leak is *unreachable* memory that was never freed. A Java leak is *reachable* memory that should have been let go. The C++ tool answers "what wasn't freed?"; the Java tool answers "what is still rooted, and why?"

---

## 12.10 Escape Analysis (and why there are no stack objects to choose)

C++ lets you decide stack vs heap allocation per object. Java has no such choice in source — everything is `new`-ed on the heap. However, the JIT compiler performs **escape analysis**: at runtime it proves whether an object *escapes* the method that created it (is stored in a field, returned, or passed somewhere it could be retained). If an object provably does **not** escape, the JIT may optimize it — *scalar-replace* it (break it into its fields held in registers/stack slots) or eliminate the allocation and associated synchronization entirely. This gives some of the performance of C++ stack allocation automatically, without you specifying it.

```java
// This Point never escapes computeDistance(); the JIT may eliminate the heap
// allocation entirely (scalar replacement), keeping x,y in registers.
record Point(double x, double y) {}

double computeDistance(double x1, double y1, double x2, double y2) {
    Point a = new Point(x1, y1);    // 'new' in source...
    Point b = new Point(x2, y2);    // ...but may be allocated NOWHERE at runtime
    double dx = a.x() - b.x();
    double dy = a.y() - b.y();
    return Math.sqrt(dx * dx + dy * dy);
}   // a, b do not escape -> JIT can scalar-replace them (no heap allocation, no GC pressure)
```

| | C++ | Java |
|---|---|---|
| Stack vs heap | you choose per object | always heap *in source* |
| Avoiding heap cost | write a stack/`std::array` object | JIT escape analysis may do it for you |
| Determinism | guaranteed by language | best-effort JIT optimization |

> **Difference:** In C++ stack allocation is a guarantee you control. In Java it is an *optimization the JIT may apply* when it can prove the object doesn't escape — you cannot force it, only write escape-friendly code (don't leak references unnecessarily).

---

## 12.11 GC Tuning Flags

Because reclamation is the JVM's job, you steer it with launch flags rather than code. The most common:

```bash
# Heap sizing
java -Xms512m -Xmx4g MyApp          # initial (Xms) and maximum (Xmx) heap size
java -XX:MaxMetaspaceSize=256m MyApp # cap class-metadata (Metaspace) growth

# Collector selection (see 12.5)
java -XX:+UseG1GC MyApp
java -XX:+UseZGC  MyApp

# Pause-time / behavior goals (G1)
java -XX:MaxGCPauseMillis=200 MyApp  # soft target for max pause

# Observability — log GC activity (unified logging, Java 9+)
java -Xlog:gc* MyApp                 # detailed GC log to stdout
java -Xlog:gc:gc.log MyApp           # GC log to a file

# Heap dump on OutOfMemoryError (for leak analysis)
java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp MyApp
```

| Flag | Effect | C++ analogue |
|------|--------|--------------|
| `-Xms` / `-Xmx` | initial / max heap | OS limits / custom allocator sizing |
| `-XX:+UseG1GC` etc. | choose collector | choose allocator strategy (in code) |
| `-XX:MaxGCPauseMillis` | pause-time target | (none) |
| `-Xlog:gc*` | GC logging | Valgrind/profiler instrumentation |
| `-XX:+HeapDumpOnOutOfMemoryError` | dump heap on OOM | core dump on crash |

> **Note:** Avoid calling `System.gc()` in code — it is only a *hint* and modern collectors usually ignore or are harmed by it. Tune with flags, not explicit GC requests.

---

## Summary

| Topic | Key Points | vs C++ |
|-------|------------|--------|
| **Stack** | primitives + references only | C++ also puts whole objects here |
| **Heap** | **all** objects; GC-managed | C++ heap is manually freed |
| **Metaspace** | class metadata (native) | (no equivalent) |
| **Allocation** | `new`, never `delete` | C++ matches `new`/`delete` |
| **Reachability** | live = reachable from a root | C++ liveness = you decide |
| **Generational GC** | young/old, minor/major | (none in C++) |
| **Collectors** | G1 (default), ZGC, Parallel, Serial | allocator strategies |
| **Destructors** | none; `finalize` deprecated → `Cleaner` | deterministic destructors |
| **References** | strong/soft/weak/phantom | `weak_ptr` (cycle-breaking) |
| **try-with-resources** | `AutoCloseable.close()` — Java's RAII | RAII via destructor |
| **Leaks** | unintended *reachability* | forgetting `delete` (unreachable) |
| **Escape analysis** | JIT may skip heap alloc | manual stack allocation |

---

## Next Steps
- Let the GC manage memory — allocate with `new`, never try to free.
- Use try-with-resources / `AutoCloseable` for files, sockets, and locks (Java's RAII).
- Reach for `WeakReference`/`SoftReference`/`WeakHashMap` for caches and avoid-retention patterns; never use `finalize()`.
- Diagnose "leaks" as unintended reachability with a heap profiler; tune the GC with launch flags.
- Move to [Chapter 13: Exception Handling](../13_exception_handling/README.md)
```