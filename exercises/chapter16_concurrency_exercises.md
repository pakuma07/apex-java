# Chapter 16: Concurrency - Exercises

## Section 1: Thread Basics 🟢

1. Launch a thread that prints a greeting (`Thread`/`Runnable`; also try `Thread.ofVirtual()`).
2. Pass an integer argument into a worker thread (capture an effectively-final variable).
3. Create two threads and `join()` both correctly (or submit to an `ExecutorService`).

## Section 2: Shared State and Locks 🟡

4. Protect a shared counter with a `synchronized` block.
5. Compare unsafe incrementing vs locked incrementing.
6. Use a lock to protect `List.add` — a `synchronized` block or `ReentrantLock` with try/finally (the `lock_guard` analog).

## Section 3: `ReentrantLock` and Lifecycle 🟡

7. Lock and unlock a `ReentrantLock` manually using `lock()`/`unlock()` in a try/finally (analog of `unique_lock`); also try `tryLock`.
8. Manage thread lifetime with an `ExecutorService` (Java threads are not movable; let the executor own them).
9. Detect whether a thread is still running with `isAlive()` / `Thread.State`.

## Section 4: Condition Variables 🟡

10. Build a one-item producer/consumer example using `Object.wait`/`notifyAll` (or a `Condition`).
11. Wait inside a `while` loop on a predicate to avoid spurious wakeups.
12. Extend the queue-based consumer to process multiple jobs (consider a `BlockingQueue`).

## Section 5: Atomics 🟡

13. Replace a lock-protected counter with `AtomicInteger`.
14. Build a stop flag using `AtomicBoolean` (or `volatile boolean`).
15. Explain when atomics are not enough for correctness (compound invariants across multiple fields).

## Section 6: Futures and Async 🔴

16. Return a computed value using `CompletableFuture.supplyAsync` (or `ExecutorService.submit` → `Future`).
17. Capture exceptions thrown inside an async task through `future.get()` (`ExecutionException`).
18. Compare eager vs lazy execution — `CompletableFuture.supplyAsync` (eager) vs a lazily-triggered `Supplier`/`CompletableFuture` completed on demand (analog of `launch::async` vs `launch::deferred`).

## Section 7: Promise and Packaged Task 🔴

19. Use a `CompletableFuture<Integer>` (complete it from another thread) to send a result between threads (`promise` analog).
20. Use a `CompletableFuture<String>` for status reporting; complete exceptionally on failure (`completeExceptionally`).
21. Wrap a sum function inside a `Callable` + `FutureTask` (the `packaged_task` analog).

## Section 8: Deadlock Awareness 🔴

22. Create a two-lock deadlock example on paper and explain why it hangs.
23. Fix the deadlock by acquiring locks in a consistent global order.
24. Acquire two `ReentrantLock`s safely using `tryLock` (back off and retry) to avoid deadlock.

## Section 9: Design and Best Practices 🔴

25. Refactor a shared-state design to reduce lock scope (the critical section as small as possible).
26. Move I/O outside the critical section in a threaded program.
27. Explain why capturing mutable shared state in thread lambdas is dangerous (capture effectively-final, avoid shared mutability).

## Section 10: Integration Challenge 🏆

28. Build a thread-safe work queue with producer and consumer threads (`BlockingQueue`).
29. Add an atomic shutdown flag to the work queue (`AtomicBoolean` / poison pill).
30. Return final statistics through a `Future` / `CompletableFuture`.

---

## Tips for Success

- Prefer high-level utilities: `ExecutorService`, `CompletableFuture`, `BlockingQueue`.
- Shut executors down with `shutdown()` + `awaitTermination(...)` instead of manual joins.
- Always release locks in a `finally` block (`ReentrantLock`).
- Protect shared mutable state consistently; prefer `synchronized` blocks by default.
- Use a `while`-loop predicate with `wait()`/`await()`.
- Consider Java 21 virtual threads (`Thread.ofVirtual()`) for large numbers of blocking tasks.

## Common Pitfalls

- Forgetting to join threads or shut down an executor.
- Capturing references to mutable shared state in a thread lambda.
- Holding locks across slow operations (I/O, blocking calls).
- Mixing atomics and lock-protected state carelessly.
- Assuming wakeups only happen when notified (always re-check the predicate).

## Learning Outcomes

After these exercises, you should be able to:
- Launch and manage threads safely (platform and virtual threads, executors)
- Protect shared state with `synchronized`/`ReentrantLock` and try/finally
- Coordinate threads with `wait`/`notifyAll` or `Condition`
- Use atomics for simple shared flags and counters
- Return results through `Future`, `CompletableFuture`, and `FutureTask`
- Recognize and avoid common deadlock patterns

## Java Exercise Example: Atomic Stop Flag

```java
import java.util.concurrent.atomic.AtomicBoolean;

public class StopFlag {
    private static final AtomicBoolean stopRequested = new AtomicBoolean(false);

    static void requestStop() {
        stopRequested.set(true);
    }

    static boolean shouldStop() {
        return stopRequested.get();
    }
}
```
