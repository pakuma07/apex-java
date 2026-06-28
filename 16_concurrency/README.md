# Chapter 16: Concurrency in Java

Concurrency is a first-class part of the Java platform — it has been in the language since version 1.0 and was massively expanded by `java.util.concurrent` (Java 5), `CompletableFuture` (Java 8), and **virtual threads** (Java 21). This chapter introduces threads, synchronization primitives, the high-level executor framework, and the task-based facilities you need to write correct multi-threaded code.

> **C++ vs Java at a glance.** C++ exposes threads as a thin RAII handle (`std::thread`) over an OS thread; the programmer manages lifetime. Java threads are managed objects on top of the JVM, garbage collected, and integrated with a defined memory model from the start. Where C++ uses `std::mutex` + `lock_guard`, Java offers the `synchronized` keyword *and* explicit `Lock` objects. Where C++ uses `std::async`/`std::future`, Java offers `ExecutorService` + `Future` + `CompletableFuture`.

## 16.1 Why Concurrency Matters

Use concurrency when work can progress independently:
- Handle multiple tasks at the same time
- Keep applications responsive
- Overlap computation and waiting (I/O)
- Model producer/consumer systems

Concurrency is not free. It introduces shared-state bugs, race conditions, deadlocks, and harder debugging. Java's defense is a **well-defined memory model** (Chapter 18) plus a rich library of correct, reusable primitives that you should prefer over hand-rolled synchronization.

---

## 16.2 Creating and Starting Threads

A `Thread` runs a `Runnable` concurrently with the thread that created it. Unlike a C++ `std::thread`, which begins running the moment its constructor returns, a Java `Thread` does **not** start on construction — you must call `start()`. Calling `run()` directly would just execute the body on the *current* thread, not a new one. The order in which threads' output appears is not guaranteed.

```java
public class Main {
    static void greet() {
        System.out.println("Hello from worker thread");
    }

    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(Main::greet);
        worker.start();                       // launches the new thread
        System.out.println("Hello from main thread");
        worker.join();                        // wait for it to finish
    }
}
```

### Notes
- `new Thread(...)` does **not** start execution; `start()` does (contrast with `std::thread`, which starts immediately).
- `join()` waits for the thread to finish (like C++ `join()`), and can throw `InterruptedException`.
- There is no `detach()`: a Java thread runs until its `run()` returns. The JVM stays alive until all **non-daemon** threads finish. Call `setDaemon(true)` *before* `start()` to make a thread not keep the JVM alive — the rough analog of `detach()`.

### Runnable vs subclassing Thread

```java
// Preferred: pass a Runnable (a lambda) — composition over inheritance
Thread t1 = new Thread(() -> System.out.println("via Runnable"));

// Legacy: subclass Thread and override run()
class Worker extends Thread {
    @Override public void run() { System.out.println("via subclass"); }
}
new Worker().start();
```

Prefer `Runnable` (or, better, an `ExecutorService`, Section 16.10). Subclassing `Thread` couples your task to the thread mechanism.

---

## 16.3 Passing Arguments to Threads

C++ forwards extra constructor arguments to the thread function (copied by default). Java has no such mechanism: a `Runnable` takes no arguments. You pass data by **capturing it in a lambda** or storing it in fields. Because Java passes object references (not copies), the worker sees the *same* object the caller holds — there is no `std::ref` and no copy.

```java
public class Main {
    static void multiply(int value, int factor) {
        System.out.println(value * factor);
    }

    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> multiply(21, 2));   // capture args
        worker.start();
        worker.join();
    }
}
```

### "Passing by reference" — capturing mutable state

In C++ you wrap an argument in `std::ref` so the thread mutates the caller's variable. Java lambdas can only capture **effectively final** locals, so to share a *mutable* value you use a holder object (an array, an `AtomicInteger`, or a field). The worker and caller then refer to the same heap object.

```java
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        AtomicInteger x = new AtomicInteger(10);     // shared, mutable, thread-safe
        Thread worker = new Thread(() -> x.incrementAndGet());
        worker.start();
        worker.join();
        System.out.println(x.get());                 // 11
    }
}
```

> Unlike C++, there is no dangling-reference hazard: the captured object is reachable and kept alive by the GC for as long as the thread can touch it.

---

## 16.4 Data Races and `synchronized`

A data race happens when multiple threads access the same memory and at least one access is a write, with no synchronization. Java's most basic tool is the `synchronized` keyword, which acquires the **intrinsic lock (monitor)** of an object. This is the closest analog to a C++ `std::mutex` + `lock_guard` — but the lock lives *inside every Java object* and is released automatically when the block exits (even via exception).

```java
public class Main {
    static int counter = 0;
    static final Object counterLock = new Object();

    static void safeIncrement() {
        for (int i = 0; i < 1000; i++) {
            synchronized (counterLock) {     // acquire monitor of counterLock
                counter++;
            }                                // monitor released here, always
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(Main::safeIncrement);
        Thread t2 = new Thread(Main::safeIncrement);
        t1.start(); t2.start();
        t1.join();  t2.join();
        System.out.println(counter);          // 2000
    }
}
```

### Why `synchronized` matters (vs C++ `lock_guard`)
- Acquires the monitor on entry, releases it on exit — automatically, even on exception (RAII-like, but built into the language rather than a stack object).
- A `synchronized` **method** locks `this` (or the `Class` object for `static` methods).
- It also establishes the **happens-before** relation (Chapter 18): the unlock by one thread happens-before the next lock, making writes visible.

```java
// Method-level synchronization: locks `this`
class Counter {
    private int value;
    public synchronized void increment() { value++; }
    public synchronized int get()        { return value; }
}
```

---

## 16.5 `volatile` and Visibility

C++ has no portable threading meaning for `volatile`. **Java's `volatile` is different and is a real concurrency tool**: it guarantees that reads and writes of the variable go to/from main memory (no caching in a register) and establishes happens-before ordering. It does **not** make compound operations like `x++` atomic.

```java
public class Main {
    static volatile boolean running = true;     // visible across threads

    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            long n = 0;
            while (running) { n++; }            // sees the write below, eventually
            System.out.println("stopped after " + n + " iterations");
        });
        worker.start();
        Thread.sleep(50);
        running = false;                        // publish stop signal
        worker.join();
    }
}
```

| Need | Use |
|---|---|
| Visibility of a single flag/reference | `volatile` |
| Atomic compound update (`++`, CAS) | `AtomicInteger`/`AtomicReference` (16.8) |
| Mutual exclusion over several fields | `synchronized` or a `Lock` (16.7) |

> `volatile` ≈ C++ `atomic<T>` with `memory_order_seq_cst` for **load/store only** — but `volatile` gives you no atomic read-modify-write. For that, use the atomic classes.

---

## 16.6 Producer / Consumer with `wait` / `notify`

Java's intrinsic monitor includes the low-level coordination methods `wait()`, `notify()`, and `notifyAll()` — the analog of a C++ `condition_variable`. They may **only** be called while holding the object's monitor (inside `synchronized` on that object). Like C++ condition variables, they are subject to *spurious wakeups*, so you must **always wait in a loop testing a predicate**.

```java
import java.util.ArrayDeque;
import java.util.Queue;

public class Main {
    static final Object lock = new Object();
    static final Queue<Integer> jobs = new ArrayDeque<>();
    static boolean finished = false;

    static void producer() {
        synchronized (lock) {
            jobs.add(5);
            jobs.add(8);
            finished = true;
            lock.notifyAll();                 // wake waiting consumers
        }
    }

    static void consumer() {
        synchronized (lock) {
            // wait with a predicate — guards against spurious wakeups
            while (jobs.isEmpty() && !finished) {
                try { lock.wait(); }          // releases the monitor while waiting
                catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            }
            while (!jobs.isEmpty()) {
                System.out.println("Consumed: " + jobs.poll());
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(Main::producer);
        Thread t2 = new Thread(Main::consumer);
        t2.start(); t1.start();
        t1.join();  t2.join();
    }
}
```

### Rule
Always wait inside a `while` loop that re-checks the condition. `wait()` atomically releases the monitor and suspends; on wakeup it re-acquires the monitor before returning — exactly like `condition_variable::wait(lock, pred)`.

> In modern code, prefer a `BlockingQueue` (16.9) or a `Condition` on an explicit `Lock` (16.7) over raw `wait`/`notify`.

---

## 16.7 Explicit Locks: `ReentrantLock` and `ReadWriteLock`

`java.util.concurrent.locks` offers explicit lock objects that are more flexible than `synchronized` — the analog of choosing C++ `std::unique_lock` over `lock_guard`. You get manual lock/unlock, `tryLock`, timed locking, interruptible locking, fairness, and multiple `Condition`s per lock. **You must release the lock in a `finally` block** (Java has no destructor to do it for you).

```java
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    static final ReentrantLock lock = new ReentrantLock();
    static int counter = 0;

    static void work() {
        lock.lock();
        try {
            counter++;                 // critical section
        } finally {
            lock.unlock();             // ALWAYS in finally — no RAII in Java
        }
    }

    public static void main(String[] args) {
        // tryLock with timeout — not possible with `synchronized`
        if (lock.tryLock()) {
            try { /* got it */ } finally { lock.unlock(); }
        }
    }
}
```

### Prefer an explicit `Lock` when you need
- Manual unlock/relock or non-block-structured locking
- `tryLock()` / timed `tryLock(timeout, unit)` / `lockInterruptibly()`
- Fairness policy (`new ReentrantLock(true)`)
- Multiple `Condition` objects on one lock

```java
import java.util.concurrent.locks.*;

ReentrantLock lock = new ReentrantLock();
Condition notFull  = lock.newCondition();   // like C++ condition_variable, but
Condition notEmpty = lock.newCondition();   // multiple per lock
// notEmpty.await();  notEmpty.signal();     // await/signal must hold the lock
```

### `ReadWriteLock` — many readers, one writer

C++11 added `std::shared_mutex` (C++17) for the same purpose; Java's is `ReentrantReadWriteLock`.

```java
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Cache {
    private final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    private int data;

    int read() {
        rw.readLock().lock();          // many readers may hold this concurrently
        try { return data; } finally { rw.readLock().unlock(); }
    }
    void write(int v) {
        rw.writeLock().lock();         // exclusive
        try { data = v; } finally { rw.writeLock().unlock(); }
    }
}
```

---

## 16.8 Atomic Types

When a single variable needs lock-free synchronized access, the `java.util.concurrent.atomic` classes are simpler and faster than a lock — the analog of C++ `std::atomic<T>`. They wrap compare-and-swap (CAS) hardware instructions.

```java
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static final AtomicInteger counter = new AtomicInteger(0);

    static void work() {
        for (int i = 0; i < 5000; i++) {
            counter.incrementAndGet();     // atomic ++counter
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(Main::work);
        Thread t2 = new Thread(Main::work);
        t1.start(); t2.start();
        t1.join();  t2.join();
        System.out.println(counter.get());  // 10000
    }
}
```

### The atomic family
- `AtomicInteger`, `AtomicLong`, `AtomicBoolean` — scalars.
- `AtomicReference<V>` — atomic object reference (≈ C++ `atomic<T*>`).
- `compareAndSet(expected, new)` — the CAS primitive (≈ `compare_exchange`).
- `LongAdder` / `LongAccumulator` — high-contention counters that beat `AtomicLong` under heavy write load (no C++ standard equivalent).

```java
AtomicReference<String> ref = new AtomicReference<>("init");
ref.compareAndSet("init", "ready");          // CAS

import java.util.concurrent.atomic.LongAdder;
LongAdder hits = new LongAdder();
hits.increment();                            // striped, low-contention
long total = hits.sum();
```

Use atomics for counters, flags, and simple state transitions. Use a lock (or `synchronized`) when **several values must change together** consistently.

---

## 16.9 The Executor Framework: `ExecutorService`, `Callable`, `Future`

Task-based concurrency is far easier than managing `Thread` objects by hand — this is Java's answer to C++ `std::async`. An `ExecutorService` owns a pool of worker threads and runs the tasks you submit. A `Runnable` returns nothing; a **`Callable<V>`** returns a value and may throw a checked exception. `submit()` returns a **`Future<V>`** whose `get()` blocks until the result is ready (≈ C++ `future::get()`), re-throwing any task exception wrapped in `ExecutionException`.

```java
import java.util.concurrent.*;

public class Main {
    static int slowSquare(int x) { return x * x; }

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            Future<Integer> result = pool.submit(() -> slowSquare(9));  // Callable
            System.out.println(result.get());          // 81 — blocks until ready
        } finally {
            pool.shutdown();                            // always shut the pool down
        }
    }
}
```

### `Future` operations (vs C++ `std::future`)
| Java | C++ | Meaning |
|---|---|---|
| `future.get()` | `future.get()` | Block for result; rethrows task exception |
| `future.get(t, unit)` | `future.wait_for(t)` then `get()` | Timed wait |
| `future.cancel(true)` | (no direct analog) | Attempt to cancel/interrupt |
| `future.isDone()` | `future.valid()`-ish | Completion check |

### Picking a pool / `Executors` factory

```java
Executors.newFixedThreadPool(n);     // n reusable threads
Executors.newCachedThreadPool();     // grows/shrinks on demand
Executors.newSingleThreadExecutor(); // serial execution
Executors.newScheduledThreadPool(n); // delayed / periodic tasks
Executors.newVirtualThreadPerTaskExecutor();  // Java 21 — one virtual thread per task
```

> **Try-with-resources (Java 19+):** `ExecutorService` implements `AutoCloseable`, so `try (var pool = Executors.newFixedThreadPool(4)) { ... }` shuts down and awaits termination automatically — the closest Java gets to C++ RAII for thread pools.

### `ThreadPoolExecutor` — full control

The factory methods wrap a `ThreadPoolExecutor`. Construct it directly to tune core/max size, the work queue, and the rejection policy.

```java
import java.util.concurrent.*;

ThreadPoolExecutor exec = new ThreadPoolExecutor(
    2,                                  // core pool size
    8,                                  // maximum pool size
    60L, TimeUnit.SECONDS,              // idle keep-alive for non-core threads
    new ArrayBlockingQueue<>(100),      // bounded work queue
    new ThreadPoolExecutor.CallerRunsPolicy()  // backpressure when saturated
);
```

---

## 16.10 `invokeAll`, `invokeAny`, and shutdown semantics

```java
import java.util.*;
import java.util.concurrent.*;

ExecutorService pool = Executors.newFixedThreadPool(4);
List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2, () -> 3);

List<Future<Integer>> all = pool.invokeAll(tasks);   // run all, wait for all
int any = pool.invokeAny(tasks);                     // first successful result

pool.shutdown();                                     // no new tasks; finish running ones
boolean done = pool.awaitTermination(10, TimeUnit.SECONDS);
if (!done) pool.shutdownNow();                       // interrupt remaining tasks
```

- `shutdown()` — graceful: refuses new tasks, lets running/queued ones finish.
- `shutdownNow()` — interrupts active tasks and returns the queued ones.
- Forgetting to shut down a pool keeps non-daemon threads alive and the JVM running — the Java analog of "forgetting to `join()`".

---

## 16.11 `CompletableFuture` — Composable Async Pipelines

`CompletableFuture<T>` is Java's most powerful task abstraction: a `Future` you can **complete manually** *and* **chain** with callbacks, without blocking on `get()`. It is roughly C++ `std::promise`/`std::future` plus a fluent combinator API (which C++ standard futures lack until `std::future::then`, still not standardized).

```java
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        CompletableFuture<Integer> pipeline =
            CompletableFuture
                .supplyAsync(() -> 9)                 // run async, produce a value
                .thenApply(x -> x * x)                // transform (map)        -> 81
                .thenApply(x -> x + 1)                //                        -> 82
                .exceptionally(ex -> -1);             // recover from failures

        System.out.println(pipeline.join());          // 82 (join = get, unchecked)
    }
}
```

### Common combinators
| Method | Purpose |
|---|---|
| `supplyAsync(supplier)` / `runAsync(runnable)` | Start async work |
| `thenApply(fn)` / `thenApplyAsync(fn)` | Transform the result |
| `thenCompose(fn)` | Flat-map (chain another future) |
| `thenCombine(other, fn)` | Combine two independent futures |
| `thenAccept` / `thenRun` | Consume result / run after |
| `exceptionally` / `handle` / `whenComplete` | Error handling |
| `allOf(...)` / `anyOf(...)` | Wait for all / any |

```java
// manual completion — like std::promise::set_value
CompletableFuture<String> promise = new CompletableFuture<>();
new Thread(() -> promise.complete("work completed")).start();
System.out.println(promise.join());            // "work completed"

// combine two async results
var a = CompletableFuture.supplyAsync(() -> 20);
var b = CompletableFuture.supplyAsync(() -> 22);
System.out.println(a.thenCombine(b, Integer::sum).join());   // 42
```

> **C++ mapping.** `std::promise`/`std::future` ↔ `new CompletableFuture<>()` + `complete()`. `std::async` ↔ `CompletableFuture.supplyAsync`. `std::packaged_task` (wrap a callable, hand out a future) ↔ `FutureTask` (below) or `supplyAsync`.

### `FutureTask` — the `packaged_task` analog

```java
import java.util.concurrent.FutureTask;
import java.util.List;

FutureTask<Integer> task = new FutureTask<>(
    () -> List.of(1, 2, 3, 4).stream().mapToInt(Integer::intValue).sum());
new Thread(task).start();
System.out.println(task.get());                // 10
```

---

## 16.12 The Fork/Join Framework

The fork/join framework (`java.util.concurrent`) targets **divide-and-conquer** parallelism using a *work-stealing* pool. It is the engine behind parallel streams. There is no direct C++ standard equivalent (C++17 parallel algorithms are the closest idea).

```java
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

class SumTask extends RecursiveTask<Long> {
    private static final int THRESHOLD = 1000;
    private final long[] a; private final int lo, hi;

    SumTask(long[] a, int lo, int hi) { this.a = a; this.lo = lo; this.hi = hi; }

    @Override protected Long compute() {
        if (hi - lo <= THRESHOLD) {            // small enough: solve directly
            long sum = 0;
            for (int i = lo; i < hi; i++) sum += a[i];
            return sum;
        }
        int mid = (lo + hi) >>> 1;             // split
        SumTask left  = new SumTask(a, lo, mid);
        SumTask right = new SumTask(a, mid, hi);
        left.fork();                           // schedule left asynchronously
        long rightResult = right.compute();    // compute right on this thread
        long leftResult  = left.join();        // wait for left
        return leftResult + rightResult;
    }
}

public class Main {
    public static void main(String[] args) {
        long[] data = new long[10_000];
        for (int i = 0; i < data.length; i++) data[i] = i;
        long total = ForkJoinPool.commonPool().invoke(new SumTask(data, 0, data.length));
        System.out.println(total);             // 49995000
    }
}
```

- `RecursiveTask<V>` returns a value; `RecursiveAction` returns nothing.
- `fork()` schedules a subtask; `join()` waits for it. Always `fork` one branch and `compute` the other on the current thread to keep a worker busy.
- The **common pool** is shared JVM-wide and also powers `parallelStream()`.

---

## 16.13 Concurrent Collections

The `java.util.concurrent` collections are thread-safe and high-throughput, replacing both unsynchronized collections and the legacy `Collections.synchronizedXxx` wrappers. C++ has no thread-safe standard containers, so these are a real Java advantage.

### `ConcurrentHashMap`

```java
import java.util.concurrent.ConcurrentHashMap;

ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("a", 1);
map.merge("a", 1, Integer::sum);            // atomic read-modify-write -> 2
map.computeIfAbsent("b", k -> 0);           // atomic
map.putIfAbsent("c", 3);                    // atomic
```

`ConcurrentHashMap` allows concurrent reads and high-concurrency writes without locking the whole map. Its compound methods (`merge`, `compute`, `computeIfAbsent`) are atomic — use them instead of get-then-put races.

### `BlockingQueue` — the producer/consumer workhorse

A `BlockingQueue` blocks `put()` when full and `take()` when empty, making producer/consumer code trivial and correct. This is the high-level replacement for the `wait`/`notify` example in 16.6.

```java
import java.util.concurrent.*;

BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(/* capacity */ 100);

Runnable producer = () -> {
    try { for (int i = 0; i < 5; i++) queue.put(i); }   // blocks if full
    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
};
Runnable consumer = () -> {
    try { while (true) System.out.println("Consumed: " + queue.take()); } // blocks if empty
    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
};
```

| Implementation | Characteristics |
|---|---|
| `ArrayBlockingQueue` | Bounded, array-backed, optional fairness |
| `LinkedBlockingQueue` | Optionally bounded, linked nodes |
| `ConcurrentLinkedQueue` | Non-blocking, unbounded, lock-free |
| `PriorityBlockingQueue` | Unbounded priority-ordered |
| `SynchronousQueue` | Zero capacity — hand-off between threads |

Other concurrent collections: `CopyOnWriteArrayList` (read-mostly), `ConcurrentSkipListMap` (sorted, concurrent).

---

## 16.14 Coordination Tools: `CountDownLatch`, `Semaphore`, `CyclicBarrier`

These `java.util.concurrent` synchronizers handle common coordination patterns that would otherwise need hand-written `wait`/`notify`.

### `CountDownLatch` — wait for N events (one-shot)

```java
import java.util.concurrent.CountDownLatch;

CountDownLatch ready = new CountDownLatch(3);     // wait for 3 workers
for (int i = 0; i < 3; i++) {
    new Thread(() -> {
        // ... do startup work ...
        ready.countDown();                        // signal "I'm ready"
    }).start();
}
ready.await();                                    // blocks until count reaches 0
System.out.println("All workers ready");
```

### `Semaphore` — limit concurrent access to N permits

```java
import java.util.concurrent.Semaphore;

Semaphore permits = new Semaphore(3);             // at most 3 concurrent users
permits.acquire();                                // blocks if none free
try { /* use limited resource */ }
finally { permits.release(); }
```

### `CyclicBarrier` — N threads rendezvous, then proceed (reusable)

```java
import java.util.concurrent.CyclicBarrier;

CyclicBarrier barrier = new CyclicBarrier(3, () -> System.out.println("phase done"));
// each of 3 threads calls barrier.await(); the last one triggers the action,
// then all are released. Unlike CountDownLatch, the barrier resets and can be reused.
```

| Tool | One-shot? | Purpose |
|---|---|---|
| `CountDownLatch` | Yes | Wait for a fixed number of events |
| `CyclicBarrier` | No (reusable) | Threads wait for each other at a barrier |
| `Semaphore` | Reusable | Limit number of concurrent accessors |
| `Phaser` | Reusable | Flexible multi-phase barrier |

---

## 16.15 Virtual Threads (Java 21)

**Virtual threads** are the headline feature of Java 21 (Project Loom, JEP 444). A virtual thread is a lightweight thread scheduled by the JVM onto a small pool of OS *carrier* threads. You can run **millions** of them, and blocking a virtual thread (on I/O, locks, `sleep`) does *not* block an OS thread. This is a fundamentally different model from C++, which has no managed lightweight threads in the standard library.

```java
public class Main {
    public static void main(String[] args) throws InterruptedException {
        // Start a single virtual thread
        Thread vt = Thread.ofVirtual().start(() ->
            System.out.println("Hello from a virtual thread: " + Thread.currentThread()));
        vt.join();

        // One virtual thread per task — scales to huge numbers of blocking tasks
        try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10_000; i++) {
                int id = i;
                executor.submit(() -> {
                    Thread.sleep(100);            // blocking is cheap here
                    return id;
                });
            }
        }   // close() waits for all tasks to finish
    }
}
```

### Platform vs virtual threads
| | Platform thread | Virtual thread |
|---|---|---|
| Backed by | One OS thread | Multiplexed onto carrier threads |
| Cost | ~1 MB stack, expensive | A few hundred bytes, cheap |
| Count | Thousands | Millions |
| Best for | CPU-bound work | I/O-bound / blocking tasks |
| Create | `Thread.ofPlatform()` / `new Thread()` | `Thread.ofVirtual()` |

> **Guidance.** Use virtual threads for high-concurrency, blocking workloads (servers handling many requests). Do **not** pool them (create one per task). Keep using platform-thread pools (or fork/join) for CPU-bound parallelism. Avoid pinning a virtual thread by running long work inside a `synchronized` block holding it to a carrier — prefer a `ReentrantLock` in those spots.

---

## 16.16 Thread-Local Storage: `ThreadLocal`

`ThreadLocal<T>` gives each thread its own independent copy of a variable — the direct analog of C++ `thread_local`. It is lazily initialized per thread via the supplier.

```java
public class Main {
    // One independent counter per thread — no lock needed
    static final ThreadLocal<Integer> threadCounter = ThreadLocal.withInitial(() -> 0);

    static void worker(int id) {
        for (int i = 0; i < 5; i++) {
            threadCounter.set(threadCounter.get() + 1);   // modifies this thread's copy
        }
        System.out.println("Thread " + id + " counter = " + threadCounter.get());
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> worker(1));
        Thread t2 = new Thread(() -> worker(2));
        t1.start(); t2.start();
        t1.join();  t2.join();
        // Both print "counter = 5" — they never see each other's value
    }
}
```

| Mechanism | One copy per | Notes |
|---|---|---|
| local variable | Method invocation | Stack, like C++ `auto` |
| `static` field | Whole program | Shared across threads |
| `ThreadLocal<T>` | Thread | ≈ C++ `thread_local` |

**Use cases:** per-thread `SimpleDateFormat`/`Random`, per-thread buffers, per-request context. **Caution:** with pooled (and especially virtual) threads, always `remove()` thread-locals after use to avoid leaks and stale data. (Java 21 also previews `ScopedValue` as a safer, immutable alternative.)

---

## 16.17 One-Time Initialization

C++ uses `std::call_once` + `std::once_flag` for thread-safe lazy init. Java provides several idioms instead — there is no single keyword, but the language guarantees make them safe.

```java
// 1) Static holder idiom — JVM guarantees class init runs exactly once, lazily
class Config {
    private final int maxConnections = 100;
    private Config() {}
    private static class Holder { static final Config INSTANCE = new Config(); }
    public static Config instance() { return Holder.INSTANCE; }   // safe, lazy
    public int getMax() { return maxConnections; }
}

// 2) ConcurrentHashMap.computeIfAbsent — run a factory at most once per key
import java.util.concurrent.ConcurrentHashMap;
ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
Object resource = cache.computeIfAbsent("db", k -> expensiveInit());

// 3) Double-checked locking with volatile (see Chapter 18 for why volatile is required)
class Lazy {
    private static volatile Lazy instance;          // volatile is essential
    public static Lazy get() {
        Lazy local = instance;
        if (local == null) {
            synchronized (Lazy.class) {
                local = instance;
                if (local == null) instance = local = new Lazy();
            }
        }
        return local;
    }
}
```

| C++ | Java idiom |
|---|---|
| `static once_flag` + `call_once` | Static holder class (preferred), or `volatile` DCL |
| Meyers singleton (`static` local) | Static holder class (init-on-demand) |

---

## 16.18 Avoiding Deadlocks and Locking Multiple Locks

Deadlock happens when threads wait forever for one another's locks. The classic case: thread A locks `m1` then `m2`; thread B locks `m2` then `m1`.

### Better patterns
- Acquire locks in **one consistent global order** (e.g., by object identity / `System.identityHashCode`).
- Keep critical sections small; don't do I/O while holding a lock.
- Prefer `tryLock` with a timeout and back-off when ordering is impossible.
- Prefer message passing (`BlockingQueue`) or immutable data.

C++ offers `std::lock(m1, m2)` to acquire several mutexes atomically with deadlock avoidance. Java has **no built-in multi-lock primitive**, so you impose an order or use `tryLock`:

```java
import java.util.concurrent.locks.ReentrantLock;

class Account {
    final ReentrantLock lock = new ReentrantLock();
    int balance; final int id;
    Account(int id, int balance) { this.id = id; this.balance = balance; }
}

// Order-based: always lock the lower id first — prevents the A/B deadlock
void transfer(Account from, Account to, int amount) {
    Account first  = from.id < to.id ? from : to;
    Account second = from.id < to.id ? to   : from;
    first.lock.lock();
    try {
        second.lock.lock();
        try {
            from.balance -= amount;
            to.balance   += amount;
        } finally { second.lock.unlock(); }
    } finally { first.lock.unlock(); }
}

// tryLock alternative: acquire both or back off and retry
boolean transferTry(Account from, Account to, int amount) {
    if (from.lock.tryLock()) {
        try {
            if (to.lock.tryLock()) {
                try { from.balance -= amount; to.balance += amount; return true; }
                finally { to.lock.unlock(); }
            }
        } finally { from.lock.unlock(); }
    }
    return false;   // caller retries
}
```

| Pattern | Safe? | Notes |
|---|---|---|
| Sequential lock in inconsistent order | Deadlock risk | Different threads use different orders |
| Consistent global lock ordering | Safe | The standard Java fix for `std::lock` |
| `tryLock` + back-off | Safe | Livelock possible; add randomized retry |

---

## 16.19 Best Practices

- Prefer the **executor framework** and `CompletableFuture` over raw `Thread` objects.
- Prefer **virtual threads** (Java 21) for high-concurrency blocking workloads; one per task, don't pool them.
- Prefer `synchronized` for simple scoped locking; use an explicit `Lock` only when you need its flexibility (analogous to choosing `lock_guard` vs `unique_lock`).
- Protect every shared mutable object consistently; document the locking policy.
- Prefer `volatile` for single flags and the **atomic classes** for counters/CAS.
- Prefer **concurrent collections** (`ConcurrentHashMap`, `BlockingQueue`) over manual synchronization.
- Always wait on conditions (`wait`/`await`) inside a predicate loop.
- Keep critical sections small; never block on I/O while holding a lock.
- Always `unlock()` in a `finally` block and always `shutdown()` your executors.
- Design for ownership and lifetime first, then optimize for performance.

---

## 16.20 Common Mistakes

- Accessing shared mutable data without synchronization (a data race — see Chapter 18).
- Calling `run()` instead of `start()` (runs on the current thread, no concurrency).
- Forgetting to `shutdown()` an `ExecutorService` (keeps the JVM alive).
- Waiting on `wait()`/`await()` without a predicate loop (spurious wakeups).
- Using `volatile` for compound updates like `count++` (not atomic).
- Acquiring multiple locks in inconsistent order (deadlock).
- Calling `wait`/`notify` outside the owning `synchronized` block (`IllegalMonitorStateException`).
- Swallowing `InterruptedException` instead of restoring the interrupt flag.
- Pinning virtual threads inside long `synchronized` blocks.

---

## 16.21 Compile and Run

Java concurrency needs no special compiler flag (unlike C++'s `-pthread`):

```bash
javac Main.java
java Main
```

Virtual threads and try-with-resources on `ExecutorService` require **Java 21** (or 19/20 with preview flags).

---

## 16.22 Related Chapters

- [12_memory_management](../12_memory_management/README.md) for object lifetime and garbage collection
- [13_exception_handling](../13_exception_handling/README.md) for try-with-resources and exception-safe cleanup
- [15_advanced_features](../15_advanced_features/README.md) for lambdas, streams, and functional interfaces
- [18_memory_model](../18_memory_model/README.md) for the Java Memory Model that makes all of this correct
