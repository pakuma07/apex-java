// Chapter 18: The Java Memory Model (analogue of the C++ memory model chapter)
// Compile: javac chapter18_memory_model.java
// Run:     java chapter18_memory_model
//
// The C++ chapter explored std::atomic and std::memory_order (relaxed,
// acquire/release, seq_cst, fences). Java does NOT expose per-operation
// memory orders the way C++ does. Instead the JAVA MEMORY MODEL (JMM, JSR-133)
// gives you a small set of tools with fixed, well-defined happens-before
// semantics:
//   - volatile          : every read/write is like a seq_cst atomic;
//                          a write happens-before every subsequent read.
//   - synchronized      : unlock happens-before the next lock on the same monitor.
//   - final fields      : safely published after construction (no tearing).
//   - java.util.concurrent.atomic.* : lock-free atomics with CAS.
//
// There is no "relaxed" or "acquire/release" knob on a plain Java field;
// the JMM picks sensible defaults so portable code stays correct. All
// examples below keep threads short and join cleanly.

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class chapter18_memory_model {

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError("CHECK FAILED: " + msg);
    }

    // ============================================================
    // Example 1: Atomic Counter (AtomicInteger == seq_cst counter)
    // ============================================================
    static void example1_atomicCounter() throws InterruptedException {
        System.out.println("=== Example 1: Atomic Counter (AtomicInteger) ===");
        AtomicInteger safeCounter = new AtomicInteger(0);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            threads.add(new Thread(() -> {
                for (int j = 0; j < 250; j++) safeCounter.incrementAndGet();
            }));
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        System.out.println("Expected: 1000, Got: " + safeCounter.get());
        check(safeCounter.get() == 1000, "atomic counter == 1000");
    }

    // ============================================================
    // Example 2: volatile = happens-before message passing
    // (Java's answer to C++ acquire/release.)
    // ============================================================
    static volatile boolean msgReady = false;
    static int payload = 0;   // plain field, published via the volatile write

    static void example2_happensBefore() throws InterruptedException {
        System.out.println("\n=== Example 2: volatile happens-before (message passing) ===");
        msgReady = false;
        payload = 0;

        Thread producer = new Thread(() -> {
            payload = 42;          // write payload first
            msgReady = true;       // volatile write "publishes" payload
        });
        Thread consumer = new Thread(() -> {
            while (!msgReady) { Thread.onSpinWait(); }   // volatile read "subscribes"
            // happens-before: payload write is now visible
            System.out.println("Consumer received payload = " + payload);
            check(payload == 42, "payload visible after volatile read");
        });
        producer.start(); consumer.start();
        producer.join();  consumer.join();
    }

    // ============================================================
    // Example 3: AtomicLong (no "relaxed" knob; still correct & fast)
    // ============================================================
    static void example3_atomicLong() throws InterruptedException {
        System.out.println("\n=== Example 3: AtomicLong Counter ===");
        AtomicLong counter = new AtomicLong(0);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            threads.add(new Thread(() -> {
                for (int j = 0; j < 1000; j++) counter.incrementAndGet();
            }));
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        System.out.println("AtomicLong counter: " + counter.get() + " (expected 8000)");
        check(counter.get() == 8000, "atomic long == 8000");
        System.out.println("Note: Java has no per-op memory_order_relaxed; the JMM picks safe defaults.");
    }

    // ============================================================
    // Example 4: synchronized total ordering (monitor happens-before)
    // ============================================================
    static int sharedX = 0, sharedY = 0, witnessZ = 0;
    static final Object monitor = new Object();

    static void example4_synchronizedOrdering() throws InterruptedException {
        System.out.println("\n=== Example 4: synchronized Happens-Before ===");
        sharedX = sharedY = witnessZ = 0;

        Thread writer = new Thread(() -> {
            synchronized (monitor) { sharedX = 1; sharedY = 2; }
        });
        Thread reader = new Thread(() -> {
            synchronized (monitor) {
                // unlock-by-writer happens-before this lock if writer ran first;
                // either way the synchronized block sees a consistent pair.
                if (sharedX == 1 && sharedY == 2) witnessZ = 1;
            }
        });
        writer.start(); writer.join();   // deterministic order for the demo
        reader.start(); reader.join();
        System.out.println("witnessZ = " + witnessZ + " (1 means both writes seen atomically)");
        check(witnessZ == 1, "synchronized published both writes");
    }

    // ============================================================
    // Example 5: final-field safe publication
    // (final fields are guaranteed visible after the constructor returns,
    //  the JMM analogue of "grouped publish" via a release fence.)
    // ============================================================
    static final class Config {
        final int a;
        final int b;
        Config(int a, int b) { this.a = a; this.b = b; }
    }
    static AtomicReference<Config> publishedConfig = new AtomicReference<>(null);

    static void example5_finalFieldPublication() throws InterruptedException {
        System.out.println("\n=== Example 5: final-field Safe Publication ===");
        publishedConfig.set(null);

        Thread writer = new Thread(() -> publishedConfig.set(new Config(10, 20)));
        Thread reader = new Thread(() -> {
            Config c;
            while ((c = publishedConfig.get()) == null) { Thread.onSpinWait(); }
            // final fields a,b are guaranteed fully constructed & visible here.
            System.out.println("config.a = " + c.a + " (expected 10)");
            System.out.println("config.b = " + c.b + " (expected 20)");
            check(c.a == 10 && c.b == 20, "final fields safely published");
        });
        writer.start(); reader.start();
        writer.join();  reader.join();
    }

    // ============================================================
    // Example 6: compareAndSet (CAS) - one-shot init + contended loop
    // ============================================================
    static void example6_cas() throws InterruptedException {
        System.out.println("\n=== Example 6: compareAndSet (CAS) ===");
        AtomicInteger val = new AtomicInteger(0);

        if (val.compareAndSet(0, 42)) {
            System.out.println("CAS succeeded: val = " + val.get());
        }
        // Second attempt must fail: current value is 42, not 0.
        boolean second = val.compareAndSet(0, 99);
        System.out.println("Second CAS " + (second ? "succeeded" : "failed as expected")
                + "; val = " + val.get());
        check(!second && val.get() == 42, "second CAS fails, val stays 42");

        // Contended CAS loop across 4 threads.
        AtomicInteger counter = new AtomicInteger(0);
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < 4; t++) {
            threads.add(new Thread(() -> {
                for (int i = 0; i < 250; i++) {
                    int old;
                    do { old = counter.get(); }
                    while (!counter.compareAndSet(old, old + 1));
                }
            }));
        }
        for (Thread th : threads) th.start();
        for (Thread th : threads) th.join();
        System.out.println("CAS loop counter: " + counter.get() + " (expected 1000)");
        check(counter.get() == 1000, "CAS loop == 1000");
    }

    // ============================================================
    // Example 7: Lock-free stack via AtomicReference CAS (Treiber stack)
    // (Java analogue of the C++ lock-free stack.)
    // ============================================================
    static final class LockFreeStack<T> {
        private static final class Node<T> {
            final T value; final Node<T> next;
            Node(T value, Node<T> next) { this.value = value; this.next = next; }
        }
        private final AtomicReference<Node<T>> head = new AtomicReference<>(null);

        void push(T val) {
            Node<T> oldHead, newHead;
            do {
                oldHead = head.get();
                newHead = new Node<>(val, oldHead);
            } while (!head.compareAndSet(oldHead, newHead));
        }
        T pop() {
            Node<T> oldHead, newHead;
            do {
                oldHead = head.get();
                if (oldHead == null) return null;
                newHead = oldHead.next;
            } while (!head.compareAndSet(oldHead, newHead));
            return oldHead.value;
        }
    }

    static void example7_lockFreeStack() throws InterruptedException {
        System.out.println("\n=== Example 7: Lock-Free Stack (Treiber) ===");
        LockFreeStack<Integer> stack = new LockFreeStack<>();
        for (int i = 1; i <= 5; i++) stack.push(i);
        System.out.print("Pop order (LIFO): ");
        Integer v;
        while ((v = stack.pop()) != null) System.out.print(v + " ");
        System.out.println();

        LockFreeStack<Integer> shared = new LockFreeStack<>();
        AtomicInteger popped = new AtomicInteger(0);
        List<Thread> pushers = new ArrayList<>(), poppers = new ArrayList<>();
        for (int t = 0; t < 4; t++) {
            pushers.add(new Thread(() -> { for (int i = 0; i < 100; i++) shared.push(i); }));
            poppers.add(new Thread(() -> {
                for (int i = 0; i < 100; i++) { while (shared.pop() == null) { /* retry */ } }
                popped.addAndGet(100);
            }));
        }
        for (Thread t : pushers) t.start();
        for (Thread t : poppers) t.start();
        for (Thread t : pushers) t.join();
        for (Thread t : poppers) t.join();
        System.out.println("Total popped by 4 threads: " + popped.get() + " (expected 400)");
        check(popped.get() == 400, "lock-free stack popped 400");
    }

    // ============================================================
    // Example 8: ThreadLocal storage (C++ thread_local analogue)
    // ============================================================
    static final ThreadLocal<Integer> tls = ThreadLocal.withInitial(() -> 0);

    static void example8_threadLocal() throws InterruptedException {
        System.out.println("\n=== Example 8: ThreadLocal Storage ===");
        Runnable worker = () -> {
            for (int i = 0; i < 5; i++) tls.set(tls.get() + 1);
            System.out.println(Thread.currentThread().getName() + " tls = " + tls.get());
            check(tls.get() == 5, "each thread sees its own 5");
            tls.remove();   // good hygiene
        };
        Thread t1 = new Thread(worker, "T1"), t2 = new Thread(worker, "T2"), t3 = new Thread(worker, "T3");
        t1.start(); t2.start(); t3.start();
        t1.join();  t2.join();  t3.join();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("======================================================");
        System.out.println("   CHAPTER 18: THE JAVA MEMORY MODEL");
        System.out.println("======================================================");

        example1_atomicCounter();
        example2_happensBefore();
        example3_atomicLong();
        example4_synchronizedOrdering();
        example5_finalFieldPublication();
        example6_cas();
        example7_lockFreeStack();
        example8_threadLocal();

        System.out.println("\nAll memory model examples completed successfully.");
    }
}
