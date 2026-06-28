// Chapter 16: Concurrency - Runnable Java Examples
// Compile: javac chapter16_concurrency.java
// Run:     java chapter16_concurrency
//
// Java adaptation of the C++11 concurrency chapter. The C++ examples used
// std::thread, mutex, lock_guard, unique_lock + condition_variable, atomic,
// std::async/future, std::promise and std::packaged_task. Here we use the
// java.util.concurrent toolkit:
//   - Thread / Runnable
//   - synchronized + ReentrantLock
//   - Condition (await/signal) = condition_variable
//   - AtomicInteger / AtomicLong
//   - ExecutorService + Future        = std::async + future
//   - CompletableFuture                = composable futures
//   - FutureTask                       = std::packaged_task
//
// All examples keep runtime short, join all threads, and shut down executors
// so the program exits cleanly.

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class chapter16_concurrency {

    static void printHeader(String title) {
        System.out.println("\n=== " + title + " ===");
    }

    // ============================================================
    // EXAMPLE 1: Thread Basics (Thread + Runnable)
    // ============================================================
    static void example1ThreadBasics() throws InterruptedException {
        printHeader("Example 1: Thread Basics");
        Thread worker = new Thread(() ->
                System.out.println("Worker thread name: " + Thread.currentThread().getName()));
        worker.start();
        worker.join();
    }

    // ============================================================
    // EXAMPLE 2: Passing Arguments (via closure capture)
    // ============================================================
    static void example2PassArguments() throws InterruptedException {
        printHeader("Example 2: Passing Arguments");
        final int value = 21;
        final int factor = 2;
        Thread worker = new Thread(() ->
                System.out.println("Computed value: " + (value * factor)));
        worker.start();
        worker.join();
    }

    // ============================================================
    // EXAMPLE 3: synchronized Protection (mutex analogue)
    // ============================================================
    static class Counter {
        private int value = 0;
        synchronized void increment() { value++; }     // synchronized = lock_guard
        synchronized int get() { return value; }
    }

    static void example3Synchronized() throws InterruptedException {
        printHeader("Example 3: synchronized Protection");
        Counter counter = new Counter();
        Runnable inc = () -> { for (int i = 0; i < 1000; i++) counter.increment(); };
        Thread t1 = new Thread(inc), t2 = new Thread(inc);
        t1.start(); t2.start();
        t1.join();  t2.join();
        System.out.println("Final protected counter: " + counter.get());
    }

    // ============================================================
    // EXAMPLE 4: ReentrantLock RAII-style (try/finally unlock)
    // ============================================================
    static void example4ReentrantLock() throws InterruptedException {
        printHeader("Example 4: ReentrantLock");
        ReentrantLock lock = new ReentrantLock();
        List<Integer> values = new ArrayList<>();
        Runnable pushValue = () -> {
            // Java has no RAII; the idiom is lock(); try { ... } finally { unlock(); }
            lock.lock();
            try { values.add(Thread.currentThread().getName().endsWith("0") ? 10 : 20); }
            finally { lock.unlock(); }
        };
        Thread t1 = new Thread(pushValue, "t-0"), t2 = new Thread(pushValue, "t-1");
        t1.start(); t2.start();
        t1.join();  t2.join();
        System.out.println("Values inserted: " + values);
    }

    // ============================================================
    // EXAMPLE 5: Condition variable (await/signal)
    // ============================================================
    static void example5Condition() throws InterruptedException {
        printHeader("Example 5: Lock + Condition (condition_variable)");
        ReentrantLock lock = new ReentrantLock();
        Condition ready = lock.newCondition();
        List<Integer> jobs = new ArrayList<>();
        final boolean[] done = {false};

        Thread producer = new Thread(() -> {
            lock.lock();
            try {
                jobs.add(7);
                jobs.add(11);
                done[0] = true;
                ready.signal();
            } finally { lock.unlock(); }
        });

        Thread consumer = new Thread(() -> {
            lock.lock();
            try {
                while (jobs.isEmpty() && !done[0]) {
                    ready.awaitUninterruptibly();
                }
                while (!jobs.isEmpty()) {
                    int job = jobs.remove(0);
                    System.out.println("Consumed job: " + job);
                }
            } finally { lock.unlock(); }
        });

        producer.start();
        producer.join();        // ensure jobs are published first (deterministic)
        consumer.start();
        consumer.join();
    }

    // ============================================================
    // EXAMPLE 6: Atomic Counter (AtomicInteger)
    // ============================================================
    static void example6Atomic() throws InterruptedException {
        printHeader("Example 6: Atomic Counter");
        AtomicInteger counter = new AtomicInteger(0);
        Runnable work = () -> { for (int i = 0; i < 5000; i++) counter.incrementAndGet(); };
        Thread t1 = new Thread(work), t2 = new Thread(work);
        t1.start(); t2.start();
        t1.join();  t2.join();
        System.out.println("Atomic counter: " + counter.get());
    }

    static int slowSquare(int x) {
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        return x * x;
    }

    // ============================================================
    // EXAMPLE 7: ExecutorService + Future (std::async analogue)
    // ============================================================
    static void example7ExecutorFuture() throws Exception {
        printHeader("Example 7: ExecutorService + Future");
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> answer = pool.submit(() -> slowSquare(9));
            System.out.println("Square result: " + answer.get());
        } finally {
            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    // ============================================================
    // EXAMPLE 8: CompletableFuture (promise/future analogue)
    // ============================================================
    static void example8CompletableFuture() throws Exception {
        printHeader("Example 8: CompletableFuture (promise + future)");
        CompletableFuture<String> message = new CompletableFuture<>();
        Thread worker = new Thread(() -> message.complete("promise delivered a value"));
        worker.start();
        System.out.println(message.get());   // blocks until completed
        worker.join();

        // Bonus: composition (thenApply) - no direct C++ analogue.
        String chained = CompletableFuture
                .supplyAsync(() -> "hello")
                .thenApply(String::toUpperCase)
                .get();
        System.out.println("Composed: " + chained);
    }

    // ============================================================
    // EXAMPLE 9: FutureTask (std::packaged_task analogue)
    // ============================================================
    static void example9FutureTask() throws Exception {
        printHeader("Example 9: FutureTask (packaged_task)");
        List<Integer> data = List.of(1, 2, 3, 4, 5);
        FutureTask<Integer> task = new FutureTask<>(() ->
                data.stream().mapToInt(Integer::intValue).sum());
        Thread worker = new Thread(task);
        worker.start();
        worker.join();
        System.out.println("Sum from FutureTask: " + task.get());
    }

    // ============================================================
    // EXAMPLE 10: Thread State / Lifecycle
    // (C++ showed ownership transfer; Java Threads aren't movable, so we
    //  demonstrate the equivalent concept: lifecycle/state inspection.)
    // ============================================================
    static void example10ThreadLifecycle() throws InterruptedException {
        printHeader("Example 10: Thread Lifecycle / State");
        Thread t = new Thread(() ->
                System.out.println("Thread running: " + Thread.currentThread().getName()));
        System.out.println("State before start: " + t.getState());   // NEW
        t.start();
        t.join();
        System.out.println("State after join:  " + t.getState());    // TERMINATED
        System.out.println("isAlive: " + t.isAlive());
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Chapter 16: Concurrency in Java");
        System.out.println("This file demonstrates core concurrency features safely.");

        example1ThreadBasics();
        example2PassArguments();
        example3Synchronized();
        example4ReentrantLock();
        example5Condition();
        example6Atomic();
        example7ExecutorFuture();
        example8CompletableFuture();
        example9FutureTask();
        example10ThreadLifecycle();

        System.out.println("\nAll concurrency examples completed successfully.");
    }
}
