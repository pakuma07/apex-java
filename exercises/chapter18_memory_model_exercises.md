# Chapter 18: Java Memory Model & Concurrency Primitives — Exercises

Compile and run all exercises with:
```bash
javac YourFile.java
java YourFile
```

---

## Section 1: Data Races and Atomics 🟢

1. Write a program where two threads simultaneously increment a plain `int` counter 100,000 times each. Observe the incorrect final value (the data race). Then fix it with `AtomicInteger`.
2. Replace the following with a correct alternative:
   ```java
   boolean done = false;          // plain field — no happens-before, may never be seen
   // Thread A: done = true;
   // Thread B: while (!done) {}
   ```
   Use a `volatile boolean` (or `AtomicBoolean`). **Note:** unlike C++ `volatile`, Java `volatile` *does* establish happens-before and prevents reordering.
3. Write a thread-safe reference counter class `RefCount` using `AtomicInteger` with `incrementAndGet`/`decrementAndGet`. Add `isUnique()` that returns true when the count == 1.
4. Implement a thread-safe `atomicMax(AtomicInteger var, int candidate)` using a CAS loop (`compareAndSet`): atomically sets `var = max(var, candidate)`.

---

## Section 2: Ordering Basics (volatile & VarHandle) 🟢

> Java has no per-operation memory order. The JMM gives `volatile` acquire/release semantics plus a total order over all volatile accesses (≈ `seq_cst`). `VarHandle` (`java.lang.invoke.VarHandle`) provides finer control: `getAcquire`/`setRelease`, `getVolatile`/`setVolatile`, `getOpaque`/`setOpaque`, `getPlain`/`setPlain`, and `compareAndSet`.

5. Write a producer/consumer pair that publishes a `record Message(int id, double value)` from one thread to another. Use a `volatile` reference (or a `VarHandle` with `setRelease`/`getAcquire`) — this is the Java equivalent of release/acquire.
6. Modify exercise 5 to use plain `volatile` reads/writes (the Java equivalent of `seq_cst`). Reason about whether correctness differs from the `setRelease`/`getAcquire` version.
7. Write a thread-safe lazy initializer using a `volatile` flag (release/acquire via volatile):
   ```java
   private volatile boolean initialized = false;
   private Data data = null;
   void init();   // called once from any thread
   Data get();    // returns initialized data
   ```
8. Write a `SpinLock` using `AtomicBoolean.compareAndSet` (the test-and-set analog), releasing with `set(false)`. The `compareAndSet` provides acquire and the release-store provides release semantics. Test with 4 threads sharing a counter.

---

## Section 3: Acquire/Release Patterns 🟡

9. Implement a single-producer single-consumer (SPSC) queue using `AtomicInteger` head and tail indices with release/acquire ordering (`setRelease`/`getAcquire` on `AtomicInteger`, or a `VarHandle`). Capacity = 16 ints.
10. Write a **lock-free flag** that:
    - Can be set by any thread (once)
    - Can be tested by any thread without locking
    - Uses a release store on set and an acquire load on test (`volatile` or `VarHandle setRelease`/`getAcquire`)
    - Guarantees that all memory written before `set()` is visible after `test()` returns true (happens-before)
11. Design a `ReadyFlag<T>` class:
    ```java
    ReadyFlag<Integer> rf = new ReadyFlag<>();
    rf.publish(42);          // thread A
    int v = rf.wait_();      // thread B — blocks until published, returns 42
    ```
    Use acquire/release internally (`AtomicReference` / `volatile`), no mutex. Then note how `CompletableFuture` solves the same problem at a higher level — but keep the manual atomic version.

---

## Section 4: Relaxed / Opaque Ordering 🟡

> Java's closest analog to C++ `relaxed` is `VarHandle.getOpaque`/`setOpaque` (and `getPlain`/`setPlain` for no ordering at all). For plain high-contention counters, prefer `LongAdder`.

12. Write a benchmarking counter incremented by 8 threads. Implement it two ways: with `LongAdder`, and with an `AtomicLong` using opaque accesses. Verify the final count is correct and explain why ordering between increments is not needed.
13. Implement a `Statistics` holder updated by workers:
    ```java
    AtomicLong totalOps;
    AtomicLong totalTime;
    ```
    Use opaque/plain increments (`getOpaque`/`setOpaque` via `VarHandle`, or `LongAdder`) for updates and a `volatile`/`getVolatile` read for the final values. Explain the difference between opaque updates and the volatile read.
14. Write a program that demonstrates why you cannot use opaque/plain ordering for the producer/consumer pattern in exercise 5 (comment explaining the reordering / visibility hazard).

---

## Section 5: Fences (VarHandle) 🟡

> Java exposes fences as `VarHandle.fullFence()`, `acquireFence()`, `releaseFence()`, `loadLoadFence()`, `storeStoreFence()`.

15. Rewrite exercise 5 (producer/consumer) using `VarHandle.releaseFence()` / `acquireFence()` instead of per-variable ordering, with plain stores/loads on the flag itself.
16. Write `publishAll(int[] vars, int n, AtomicBoolean flag)` that uses a single `releaseFence()` to make `n` plain writes visible to a reader that does an `acquireFence()` then reads the flag.
17. Explain (in code comments) why a fence orders a *group* of accesses while a per-variable release store only orders relative to that specific variable's read.

---

## Section 6: Compare-and-Swap 🔴

18. Implement `atomicMin(AtomicInteger a, int b)` using a `compareAndSet` (or `weakCompareAndSet`) loop. Verify it works with 8 threads each contributing a random value.
19. Write a lock-free `AtomicQueue<Integer>` (MPMC — multiple producer, multiple consumer) using a CAS loop on an internal linked list of `AtomicReference` nodes. Support `push` and `pop`. (Compare with `ConcurrentLinkedQueue`.)
20. Implement a `TreiberStack<T>` (the classic lock-free stack) using `AtomicReference` to the head node. Test with 4 pusher and 4 popper threads, each doing 1000 operations. Assert all pushes are eventually popped.
21. Write a `HazardPointer` stub that mitigates the ABA problem: before reading a node's `next`, the thread registers the node as "in use" to prevent reclamation. Outline the design (full implementation is a research topic). Compare with using `AtomicStampedReference`.

---

## Section 7: ThreadLocal and Once Initialization 🟡

22. Write a program where each of 4 threads accumulates a running sum in a `ThreadLocal<Long>`. At the end, collect all sums into a synchronized global list and print the total.
23. Implement thread-safe `Logger.instance()` using the holder-class idiom (or double-checked locking with a `volatile` field). Verify the constructor runs exactly once even when 8 threads call `instance()` simultaneously.
24. Write a per-thread random engine:
    ```java
    Random getEngine();   // returns this thread's Random, seeded once
    ```
    Use `ThreadLocal<Random>` (or `ThreadLocalRandom`) so each thread gets an independent, properly seeded engine.
25. Implement `runOnce(Runnable task)` — guarantees the task runs exactly once across all calls from any thread, using `AtomicBoolean.compareAndSet` (or a memoizing `Supplier`). Compare with a `static` initializer.

---

## Section 8: Lock-Free Data Structures 🔴

26. Implement a lock-free `AtomicCounter` supporting `increment()`, `decrement()`, `get()` using only `AtomicInteger` and its add operations.
27. Write a lock-free single-linked stack guarded against ABA using `AtomicStampedReference` (the Java counted-pointer analog), combining a node reference with a stamp/counter.
28. Design and implement a lock-free 64-bit bitset using `AtomicLong`. Provide thread-safe `set(bit)`, `clear(bit)`, `test(bit)`, and `flip(bit)` via CAS loops.
29. Write a `SpinBarrier` for N threads: all must call `arrive()` before any proceeds. Use `AtomicInteger` and a spin-wait loop. (Compare with `CyclicBarrier` / `Phaser`.)
30. Build a thread-safe `EventBus` where publishers call `publish(event)` and subscribers register handlers. Use an `AtomicInteger` for the count of registered handlers (no lock for the count) and a concurrent collection for the handlers.

---

## Integration Challenges 🏆

**Challenge 1 — Sequence Lock (seqlock):**  
Implement a `SeqLock<T>` allowing many concurrent readers and one writer:
- Writer increments a sequence counter (odd = writing), updates data, increments again (even = done)
- Reader reads the sequence, copies the data, reads the sequence again — if unchanged and even, the data is valid
- Use a `volatile`/`VarHandle` sequence counter with appropriate ordering
- Verify correctness with 1 writer and 4 readers

**Challenge 2 — Lock-Free Ring Buffer (SPSC):**  
Write a single-producer single-consumer ring buffer of capacity 256. Use `AtomicLong` head and tail with `setRelease`/`getAcquire`. Ensure zero blocking under non-full/non-empty conditions.

**Challenge 3 — Memory Model Quiz:**  
Without running the code, predict the possible outcomes of each snippet, then run and verify:
```java
// Snippet A: two plain (or VarHandle plain) fields, two threads
// Snippet B: message passing with the wrong pairing (write to a, read from b)
// Snippet C: volatile store, volatile load, different threads
```
Discuss how the JMM's happens-before rules explain the allowed outcomes.

---

## Key Concepts Checklist

```
✓ Data races on plain fields = unspecified results; use volatile/Atomic* for shared state
✓ Java volatile establishes happens-before AND prevents reordering (UNLIKE C++ volatile)
✓ JMM provides a total order over all volatile accesses (≈ seq_cst)
✓ VarHandle gives finer control: getAcquire/setRelease, getOpaque/setOpaque, getPlain/setPlain
✓ CAS via Atomic*/VarHandle compareAndSet/weakCompareAndSet — foundation of lock-free code
✓ ThreadLocal: per-thread independent storage
✓ Once-init: holder-class idiom or double-checked locking with a volatile field
✓ ABA mitigation: AtomicStampedReference (counted pointer) / hazard pointers
✓ happens-before: the JMM's formal correctness relation
✓ final-field freeze: safely-published final fields are visible after construction
```

---

## Expected Difficulty

- **Easy (🟢)**: 15-25 min each — 7 exercises
- **Medium (🟡)**: 30-50 min each — 12 exercises
- **Hard (🔴)**: 60-120 min each — 8 exercises
- **Challenges (🏆)**: 90-180 min — 3 exercises
