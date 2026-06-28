# Chapter 18: The Java Memory Model (JMM)

The **Java Memory Model** is the formal specification (JLS §17.4, originally JSR-133) of how reads and writes to shared variables interact across threads. Without understanding it, even `volatile` fields and `Atomic*` operations can silently produce incorrect concurrent programs. This chapter is the expert-level counterpart to Chapter 16's practical primitives, and it parallels the C++11 memory model — with important differences.

> **C++ vs Java memory models.** Both define a *happens-before* relation and the notion that data races are errors. The crucial difference: C++ leaves a data race as **undefined behaviour** (anything may happen), while Java guarantees **memory safety even for racy programs** — a race produces some legal *value* (no torn references, no crashes), just not necessarily the value you intended. C++ exposes fine-grained `std::memory_order` on every atomic op; Java's default is sequential consistency for `volatile`/`synchronized`, with explicit relaxed/acquire/release modes available only through `VarHandle` (Java 9+).

---

## 18.1 Why the Memory Model Matters

Modern CPUs and compilers reorder memory operations for performance. This is transparent in single-threaded code but **visible across threads**.

```
Thread A                Thread B
--------                --------
x = 1;                  if (ready) {
ready = true;             // sees x == 0 ???
                        }
```

Without explicit ordering, the JIT or CPU may reorder `x = 1` and `ready = true`. Thread B can observe `ready == true` while still reading `x == 0`. In Java this is a **data race**: the program is not *undefined* (as it would be in C++), but the value Thread B reads for `x` is not guaranteed to be the latest one.

The JMM defines:
1. What constitutes a data race.
2. How `volatile`, `synchronized`, and final fields create ordering guarantees.
3. The **happens-before** relation that makes concurrent programs correct.

```java
public class Main {
    // ❌ Data race — no guarantee Thread B sees the write (but NOT undefined behaviour)
    static int shared = 0;                 // NOT volatile

    static void writerBad() { shared = 42; }
    static void readerBad() { System.out.println(shared); }  // may see 0 or 42

    // ✅ Race eliminated — volatile establishes visibility + ordering
    static volatile int safeShared = 0;

    static void writerGood() { safeShared = 42; }
    static void readerGood() { System.out.println(safeShared); }
}
```

> Note the contrast with C++: there the racy read of `shared` is undefined behaviour and may read garbage. In Java it reads *some* `int` value (0 or 42), never garbage — but reading a stale value is still a bug.

---

## 18.2 The happens-before Relationship

**happens-before** is the JMM's fundamental ordering relation. If action A *happens-before* action B, then A's memory effects are guaranteed visible to B.

```
Operation A  happens-before  Operation B
means: B is guaranteed to see the effects of A.
```

The JMM's happens-before rules (JLS §17.4.5):

1. **Program order:** within a single thread, each action happens-before every action later in source order (≈ C++ *sequenced-before*).
2. **Monitor lock:** an `unlock` on a monitor happens-before every subsequent `lock` on that same monitor.
3. **Volatile:** a write to a `volatile` field happens-before every subsequent read of that field.
4. **Thread start:** `Thread.start()` happens-before any action in the started thread.
5. **Thread join:** any action in a thread happens-before another thread's successful return from `join()` on it.
6. **Transitivity:** if A hb B and B hb C, then A hb C.
7. **Constructor → finalizer**, **interrupt → detection of interrupt**, etc.

```java
public class Main {
    static int a = 0, b = 0;
    static volatile int sync = 0;          // the synchronizing variable

    static void threadA() {
        a = 1;                             // (1) program order before (2)
        b = 2;                             // (2) program order before (3)
        sync = 1;                          // (3) volatile write — releases (1),(2)
    }

    static void threadB() {
        while (sync != 1) { }              // (4) volatile read — acquires
        // happens-before chain: (1),(2) hb (3) hb (4)
        System.out.println(a + " " + b);   // guaranteed: 1 2
    }
}
```

This mirrors the C++ release/acquire pattern exactly, but Java spells it with a plain `volatile` write/read instead of `store(release)`/`load(acquire)`.

---

## 18.3 `volatile` Semantics

A `volatile` field in Java has **sequentially consistent** load/store semantics (since JSR-133). It is far stronger than C++ `volatile` (which is not a threading construct) and is closest to a C++ `std::atomic<T>` accessed with `memory_order_seq_cst` — but only for plain reads/writes, not compound operations.

```java
public class Main {
    static volatile boolean ready = false;
    static int payload = 0;                 // NOT volatile — published via `ready`

    static void producer() {
        payload = 99;                       // ordinary write
        ready = true;                       // volatile write: publishes payload
    }

    static void consumer() {
        while (!ready) { }                  // volatile read: subscribes
        // happens-before: everything before the volatile write is visible here
        System.out.println(payload);        // guaranteed to print 99
    }
}
```

`volatile` guarantees, for the annotated field:
- **Visibility:** a write is immediately visible to subsequent reads (no caching in registers).
- **Ordering:** reads/writes are not reordered across the volatile access (a write acts like release; a read like acquire), and all volatile accesses share a single total order.
- **Atomicity** of the *single* read or write — including for `long`/`double` (non-volatile 64-bit writes may otherwise tear; see 18.5).

What `volatile` does **not** give you:
- **No atomic read-modify-write.** `count++` on a volatile field is read + add + write — three steps, racy. Use `AtomicInteger` or a lock.

```java
volatile int count = 0;
count++;                       // ❌ NOT atomic — lost updates under contention
// ✅ use AtomicInteger.incrementAndGet() or synchronized
```

---

## 18.4 `synchronized` and Memory Visibility

`synchronized` provides both **mutual exclusion** and **memory visibility**. Acquiring a monitor is like an acquire load; releasing it is like a release store. By rule 2 (monitor lock), everything done before an unlock is visible to any thread that subsequently locks the *same* monitor.

```java
class SafeBox {
    private int value;                       // plain field, guarded by `this`

    synchronized void set(int v) { value = v; }      // release on exit
    synchronized int  get()      { return value; }   // acquire on entry
    // Because both methods lock the same monitor (`this`), a set() happens-before
    // a later get() — the reader always sees the latest write.
}
```

Key point: visibility is guaranteed only when **both** threads synchronize on the **same** monitor (the analog of C++ "pair release/acquire on the *same* atomic"). Locking a different lock provides no happens-before edge.

---

## 18.5 Atomicity and Word Tearing

The JMM guarantees that reads and writes of references and of all primitive types **except `long` and `double`** are atomic. A 64-bit `long`/`double` may be written as two 32-bit halves on some JVMs, so a racing reader could observe a **torn** value. Declaring the field `volatile` (or using `AtomicLong`) restores atomicity.

```java
long bad = 0;                  // ❌ non-volatile long: a racy read may tear
volatile long good = 0;        // ✅ atomic 64-bit read/write

import java.util.concurrent.atomic.AtomicLong;
AtomicLong counter = new AtomicLong();   // ✅ atomic AND supports CAS/fetch-add
```

> C++ `std::atomic<long long>` is atomic by construction. In Java you opt in with `volatile` or `Atomic*`. Atomicity (no torn value) is distinct from visibility (seeing the latest value) and from compound atomicity (`++`). You may need all three.

---

## 18.6 Data-Race-Free (DRF) Programs and Sequential Consistency

The central theorem of both the Java and C++ memory models: **if a program is correctly synchronized (data-race-free), it appears sequentially consistent.** That is, if every conflicting pair of accesses is ordered by happens-before, you may reason as though all operations executed in a single global interleaving — reordering becomes invisible.

- **As-if-serial** (single thread): the JIT/CPU may reorder freely as long as a *single thread's* result is unchanged.
- **Sequential consistency for DRF programs** (multi-thread): if there are no data races, the multi-threaded result is also as-if no reordering happened.

The practical rule: **make your program DRF** (guard every shared mutable access with `volatile`, `synchronized`, a `Lock`, or an `Atomic*`), and you never have to reason about reordering. This is identical in intent to C++'s "DRF ⇒ SC" guarantee for `seq_cst` programs.

```java
// Classic SC demonstration (independent reads of independent writes)
public class Main {
    static volatile boolean x = false, y = false;
    static volatile int z = 0;

    static void writeX() { x = true; }
    static void writeY() { y = true; }
    static void readXthenY() { while (!x) {} if (y) z++; }
    static void readYthenX() { while (!y) {} if (x) z++; }
    // Because x, y, z are volatile, all accesses share a single total order:
    // at least one reader sees both flags set, so z >= 1 always (z is 1 or 2).
}
```

---

## 18.7 Final Field Semantics

`final` fields have **special JMM guarantees** that ordinary fields lack and that have no direct C++ analog. If an object is properly constructed (its reference does not "escape" during construction), any thread that sees the reference is **guaranteed to see the correctly initialized `final` fields**, with no synchronization — even via a data race on the reference itself.

```java
final class ImmutablePoint {
    final int x, y;                          // final fields
    ImmutablePoint(int x, int y) { this.x = x; this.y = y; }
}

// Even if `shared` is published without synchronization, any thread that reads
// a non-null reference is guaranteed to see x and y fully initialized.
static ImmutablePoint shared;                // not even volatile
static void publisher() { shared = new ImmutablePoint(3, 4); }
static void reader()    { var p = shared; if (p != null) System.out.println(p.x + "," + p.y); }
// p.x/p.y are guaranteed 3/4 — never 0 — thanks to final-field semantics.
```

Caveats:
- The guarantee holds only if `this` does **not escape** the constructor (don't publish `this` before the constructor finishes).
- It covers objects transitively reachable through `final` fields at the end of construction.

This is *why* immutable objects (all-`final`, no escape) are inherently thread-safe to share — a cornerstone of Java concurrency. Java `record`s (Chapter 15) get this for free since their components are `final`.

---

## 18.8 Instruction Reordering and the `volatile` Fix for Double-Checked Locking

**Double-checked locking (DCL)** is the canonical example of why the memory model matters. The naive version is broken because `instance = new Lazy()` is not atomic: it (1) allocates, (2) runs the constructor, (3) assigns the reference. The JIT may reorder (3) before (2), so another thread can observe a **non-null but not-yet-constructed** object.

```java
class Lazy {
    private int data = 42;

    // ❌ BROKEN before Java 5 / without volatile: reader may see a half-built object
    private static Lazy brokenInstance;
    static Lazy getBroken() {
        if (brokenInstance == null) {
            synchronized (Lazy.class) {
                if (brokenInstance == null)
                    brokenInstance = new Lazy();   // store may be reordered before init
            }
        }
        return brokenInstance;
    }

    // ✅ CORRECT: `volatile` forbids the reordering and adds the happens-before edge
    private static volatile Lazy instance;
    static Lazy get() {
        Lazy local = instance;                 // read volatile once (perf)
        if (local == null) {
            synchronized (Lazy.class) {
                local = instance;
                if (local == null)
                    instance = local = new Lazy();  // volatile write: fully published
            }
        }
        return local;
    }
}
```

This is exactly parallel to the C++ DCL fix, where the pointer must be `std::atomic` with proper release/acquire ordering (or you use `std::call_once`). In Java, prefer the **static holder idiom** (Chapter 16.17), which sidesteps DCL entirely using the JVM's lazy, thread-safe class initialization.

---

## 18.9 `java.util.concurrent.atomic` and `VarHandle` Memory Ordering

The `Atomic*` classes provide lock-free atomic read-modify-write (CAS, fetch-add) — the analog of C++ atomics, but historically with only sequentially-consistent semantics. Since Java 9, **`VarHandle`** exposes the full spectrum of memory-ordering access modes, finally matching C++'s `std::memory_order`.

```java
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.invoke.*;

public class Main {
    int value = 0;

    static void atomics() {
        AtomicInteger a = new AtomicInteger(0);
        a.incrementAndGet();                       // seq_cst RMW (≈ fetch_add seq_cst)
        a.compareAndSet(1, 2);                      // CAS (≈ compare_exchange)
    }

    public static void main(String[] args) throws Throwable {
        VarHandle V = MethodHandles.lookup()
            .findVarHandle(Main.class, "value", int.class);
        Main m = new Main();

        V.set(m, 1);                  // plain      ≈ memory_order_relaxed (no ordering)
        V.setOpaque(m, 2);            // opaque     ≈ relaxed + progress/coherence
        V.setRelease(m, 3);           // release    ≈ memory_order_release
        int r = (int) V.getAcquire(m);     // acquire ≈ memory_order_acquire
        V.setVolatile(m, 4);          // volatile   ≈ memory_order_seq_cst
        int v = (int) V.getVolatile(m);
        V.compareAndSet(m, 4, 5);     // CAS        ≈ compare_exchange (acq_rel)
        V.getAndAdd(m, 10);           // atomic fetch-add
    }
}
```

### VarHandle access modes ↔ C++ `memory_order`
| VarHandle mode | C++ `std::memory_order` | Guarantees |
|---|---|---|
| `getPlain` / `setPlain` | (none / non-atomic) | No ordering, no atomicity guarantee |
| `getOpaque` / `setOpaque` | `relaxed` | Atomic + coherence, no cross-variable ordering |
| `getAcquire` / `setRelease` | `acquire` / `release` | Pairwise release→acquire ordering |
| `getVolatile` / `setVolatile` | `seq_cst` | Total order across all such accesses |
| `compareAndSet` / `getAndAdd` | `acq_rel` RMW | Atomic read-modify-write |

> Most code should stick to `Atomic*` and `volatile` (sequentially consistent, easy to reason about). Reach for relaxed/opaque/acquire-release `VarHandle` modes only after profiling, exactly as you would reach for weaker `std::memory_order` in C++.

---

## 18.10 Lock-Free Programming and CAS

Lock-free means **at least one thread always makes progress** even if others stall. The primitive is **compare-and-swap (CAS)**, exposed by `Atomic*.compareAndSet` and `VarHandle.compareAndSet` — the analog of C++ `compare_exchange`.

```java
import java.util.concurrent.atomic.AtomicReference;

class LockFreeStack<T> {
    private static final class Node<T> {
        final T value; final Node<T> next;
        Node(T v, Node<T> n) { value = v; next = n; }
    }
    private final AtomicReference<Node<T>> head = new AtomicReference<>();

    void push(T val) {
        Node<T> oldHead, newNode;
        do {
            oldHead = head.get();
            newNode = new Node<>(val, oldHead);
        } while (!head.compareAndSet(oldHead, newNode));   // CAS retry loop
    }

    T pop() {
        Node<T> oldHead, newHead;
        do {
            oldHead = head.get();
            if (oldHead == null) return null;
            newHead = oldHead.next;
        } while (!head.compareAndSet(oldHead, newHead));   // CAS retry loop
        return oldHead.value;
    }
}
```

- The CAS loop is **mandatory**: `compareAndSet` can fail under contention, so you must retry.
- Java's `compareAndSet` corresponds to C++ `compare_exchange_weak/strong`. There is no spurious-failure split in the basic API; `weakCompareAndSet*` variants exist on `VarHandle` for the weak form used on ARM/POWER.

### The ABA problem
Like C++, a naive CAS suffers from **ABA**: a value changes A→B→A and CAS wrongly succeeds. Java's solution mirrors C++ tagged pointers: `AtomicStampedReference` (value + version stamp) or `AtomicMarkableReference`. Garbage collection, however, removes Java's *memory-reclamation* ABA hazard that C++ solves with hazard pointers/epochs — a notable Java advantage.

---

## 18.11 False Sharing

**False sharing** is a hardware-level performance pathology (not a correctness bug) shared by C++ and Java: two independent variables land on the same CPU **cache line**, so a write to one invalidates the other in every core's cache, causing cache-coherence traffic.

```java
// ❌ Two hot counters likely on the same 64-byte cache line — they "ping-pong"
class Counters {
    volatile long a;     // thread 1 writes a
    volatile long b;     // thread 2 writes b — invalidates thread 1's cache line
}
```

Mitigations:
- **`@jdk.internal.vm.annotation.Contended`** (JDK-internal; `-XX:-RestrictContended` to enable for user code) pads a field onto its own cache line — the JMM-aware analog of C++ `alignas(std::hardware_destructive_interference_size)`.
- Manual padding (extra `long` fields) — the historical trick, also used in C++.
- `LongAdder` (Chapter 16.8) sidesteps it by striping counters across cache lines automatically.

```java
import java.util.concurrent.atomic.LongAdder;
LongAdder hits = new LongAdder();   // striped — avoids false sharing under contention
hits.increment();
long total = hits.sum();
```

---

## 18.12 Common Pitfalls

```java
// ❌ Pitfall 1: assuming `volatile` makes compound ops atomic
volatile int n = 0;
n++;                          // read-modify-write: NOT atomic
// ✅ AtomicInteger.incrementAndGet() or synchronized

// ❌ Pitfall 2: publishing through one field, reading through another
//    (the Java analog of C++ "wrong atomic variable" pairing)
//    Writer: data = 1; flagA = true;   Reader: while(!flagB){}  -> no happens-before

// ❌ Pitfall 3: relying on visibility without any synchronization
//    A plain field written by one thread may never be observed by another (no hb edge).
//    Make it volatile, or guard it with the same lock on both sides.

// ❌ Pitfall 4: letting `this` escape during construction
//    Breaks final-field guarantees and can publish a half-built object.

// ❌ Pitfall 5: broken double-checked locking without `volatile` (Section 18.8)

// ❌ Pitfall 6: treating Java `volatile` like C++ `volatile`
//    Java volatile IS a threading primitive (visibility + ordering);
//    C++ volatile is NOT (it only inhibits some compiler optimizations).
```

---

## Summary

| Concept | Key points |
|---|---|
| **Data race** | Unsynchronized conflicting access; in Java safe-but-wrong, in C++ undefined |
| **happens-before** | Formal ordering: B sees A's effects iff A hb B |
| **`volatile`** | Sequentially consistent load/store + visibility; NOT atomic RMW |
| **`synchronized`** | Mutual exclusion + visibility; unlock hb subsequent lock on same monitor |
| **Atomicity** | All types atomic except non-volatile `long`/`double` (word tearing) |
| **DRF ⇒ SC** | Correctly synchronized programs appear sequentially consistent |
| **final fields** | Safe publication of immutable objects without synchronization |
| **Double-checked locking** | Requires `volatile`; prefer the static holder idiom |
| **`VarHandle` modes** | plain/opaque/acquire-release/volatile ≈ C++ relaxed/acquire-release/seq_cst |
| **CAS** | Foundation of lock-free structures; needs a retry loop; ABA via stamped refs |
| **False sharing** | Cache-line contention; `@Contended`, padding, or `LongAdder` |

---

## Next Steps

- Read JLS §17.4 (the JMM) and JSR-133 ("Java Memory Model and Thread Specification").
- Study `java.lang.invoke.VarHandle` access modes and compare with C++ `std::memory_order`.
- Use immutable (all-`final`) objects and the static holder idiom to avoid most ordering pitfalls.
- Validate concurrent code with the **jcstress** harness (the JMM equivalent of stress-testing).
- Return to [Chapter 16: Concurrency](../16_concurrency/README.md) for the high-level primitives.
- Return to [Chapter 17: Reflection & Metaprogramming](../17_template_metaprogramming/README.md) for `VarHandle`'s introduction.
