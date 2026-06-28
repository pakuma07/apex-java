# Java 21 Best Practices & Patterns Guide

> Adapted from the C++11 Best Practices guide. Reworked for modern Java (Effective Java style): immutability, `equals`/`hashCode`, generics, streams, exceptions, and concurrency.

## Code Quality & Style

### Naming Conventions
```java
// Classes, interfaces, enums, records - PascalCase
class MyClass { }
interface Drawable { }
enum Color { RED, GREEN, BLUE }
record Point(int x, int y) { }

// Variables and methods - camelCase
int myVariable = 0;
void myMethod() { }

// Constants (static final) - UPPER_SNAKE_CASE
static final int MAX_SIZE = 100;
static final double PI = 3.14159;

// Packages - all lowercase, reverse-domain
// package com.example.app.util;

// Type parameters - single uppercase letter (T, E, K, V, R)
class Box<T> { }
```

### Immutability (Prefer It)
```java
// Make fields final and the class final where possible
public final class Money {
    private final long cents;          // final field
    private final String currency;

    public Money(long cents, String currency) {
        this.cents = cents;
        this.currency = currency;
    }

    public long cents() { return cents; }
    public String currency() { return currency; }
}

// Records give you immutability + equals/hashCode/toString for free
public record MoneyR(long cents, String currency) { }

// Defensively copy mutable inputs/outputs
public final class Schedule {
    private final List<String> events;
    public Schedule(List<String> events) {
        this.events = List.copyOf(events);   // unmodifiable snapshot
    }
    public List<String> events() { return events; } // already unmodifiable
}
```

## Object Equality

### equals() and hashCode() Contract
```java
// Always override both together. Use the same fields in each.
public final class Point {
    private final int x, y;
    public Point(int x, int y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point p)) return false;   // pattern matching (Java 16+)
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Point[x=" + x + ", y=" + y + "]";
    }
}

// Or just use a record - equals/hashCode/toString are generated correctly:
public record PointR(int x, int y) { }
```

## Null Safety

### Optional Instead of null
```java
// Return Optional instead of null for "might be absent" results
public Optional<User> findUser(String id) {
    return Optional.ofNullable(userMap.get(id));
}

// Consume without unsafe get()
findUser("42").ifPresent(u -> System.out.println(u.name()));
String name = findUser("42").map(User::name).orElse("Unknown");

// Validate arguments early
public void process(String input) {
    Objects.requireNonNull(input, "input must not be null");
    // ...
}
```

> Do NOT use `Optional` for fields or method parameters — it is designed for return values.

## Generics

### Use Generics, Avoid Raw Types
```java
// GOOD - parameterized
List<String> names = new ArrayList<>();      // diamond operator infers type
Map<String, Integer> counts = new HashMap<>();

// BAD - raw type loses type safety
// List names = new ArrayList();              // avoid

// Bounded type parameters
public static <T extends Comparable<T>> T max(List<T> list) {
    T best = list.get(0);
    for (T item : list) if (item.compareTo(best) > 0) best = item;
    return best;
}

// PECS: Producer Extends, Consumer Super
void copy(List<? extends Number> src, List<? super Number> dst) {
    for (Number n : src) dst.add(n);
}
```

## Collections

### Container Selection
```java
// Prefer ArrayList by default
List<Integer> list = new ArrayList<>();

// Specific needs:
Deque<Integer> deque = new ArrayDeque<>();   // front/back ops, stack, queue
List<Integer> linked = new LinkedList<>();   // frequent middle insert/remove
Set<Integer> sorted = new TreeSet<>();       // unique, sorted
Set<Integer> fast = new HashSet<>();         // unique, fast lookup
Map<K, V> map = new HashMap<>();             // key-value, fast lookup
Map<K, V> ordered = new TreeMap<>();         // sorted by key
```

### Immutable Factory Methods
```java
List<Integer> nums = List.of(1, 2, 3);                 // unmodifiable
Map<String, Integer> m = Map.of("a", 1, "b", 2);
Set<String> s = Set.of("x", "y");

// Defensive copy of an existing collection
List<Integer> copy = List.copyOf(existing);
```

### Bounds & Safe Access
```java
List<Integer> v = new ArrayList<>(List.of(1, 2, 3));
// list.get(10) throws IndexOutOfBoundsException - check size first
if (10 < v.size()) {
    int x = v.get(10);
}

// Map: avoid NPE with getOrDefault / computeIfAbsent
int count = counts.getOrDefault("key", 0);
counts.merge("key", 1, Integer::sum);                  // increment safely
```

## Streams & Functional Style

### Prefer Streams Over Manual Loops (when it reads clearly)
```java
// Filter + transform + collect
List<String> result = people.stream()
    .filter(p -> p.age() >= 18)
    .map(Person::name)
    .sorted()
    .toList();                                          // Java 16+ unmodifiable list

// Reduce / aggregate
int total = nums.stream().mapToInt(Integer::intValue).sum();
double avg = nums.stream().mapToInt(Integer::intValue).average().orElse(0);

// Grouping
Map<Integer, List<Person>> byAge = people.stream()
    .collect(Collectors.groupingBy(Person::age));
```

### Lambdas & Method References
```java
Comparator<String> byLength = Comparator.comparingInt(String::length);
Runnable task = () -> System.out.println("running");
Function<Integer, Integer> square = x -> x * x;

// Prefer method references when they read more clearly
list.forEach(System.out::println);
list.sort(Comparator.naturalOrder());
```

> Keep lambdas short. Extract a named method if the body grows beyond a couple of lines.

## Object-Oriented Design

### Encapsulation
```java
public class GoodClass {
    private int data;                        // hidden state

    public int getData() { return data; }
    public void setData(int d) {
        if (d < 0) throw new IllegalArgumentException("d must be >= 0");
        this.data = d;                       // validated
    }
}

// Avoid exposing mutable public fields:
// public class BadClass { public int data; }  // anyone can corrupt state
```

### Favor Composition Over Inheritance
```java
// Instead of extending to reuse, hold a reference and delegate
public class InstrumentedList<E> {
    private final List<E> list;              // composition
    private int addCount = 0;

    public InstrumentedList(List<E> list) { this.list = list; }

    public boolean add(E e) {
        addCount++;
        return list.add(e);
    }
    public int addCount() { return addCount; }
}
```

### Program to Interfaces
```java
// Declare with the interface type, instantiate the implementation
List<String> names = new ArrayList<>();
Map<String, Integer> ages = new HashMap<>();
// Lets you swap implementations without touching callers.
```

### Sealed Hierarchies (Java 17+)
```java
public sealed interface Shape permits Circle, Square { }
public record Circle(double r) implements Shape { }
public record Square(double side) implements Shape { }

// Exhaustive switch - compiler checks all cases are covered
static double area(Shape s) {
    return switch (s) {
        case Circle c -> Math.PI * c.r() * c.r();
        case Square sq -> sq.side() * sq.side();
    };
}
```

## Exceptions

### Use Exceptions Correctly
```java
// Use unchecked exceptions for programming errors
if (index < 0) throw new IllegalArgumentException("index < 0");

// Use checked exceptions for recoverable conditions the caller must handle
public void load(Path p) throws IOException { /* ... */ }

// Custom exception
public class ConfigException extends RuntimeException {
    public ConfigException(String message, Throwable cause) {
        super(message, cause);               // preserve the cause chain
    }
}
```

### try-with-resources (the Java RAII)
```java
// Resources are closed automatically, in reverse order, even on exception
try (var reader = Files.newBufferedReader(path)) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
}   // reader.close() called automatically

// Any class implementing AutoCloseable works
public class Connection implements AutoCloseable {
    public Connection() { /* acquire */ }
    @Override public void close() { /* release */ }
}
```

### Don't Swallow Exceptions
```java
// BAD - silent failure
// try { risky(); } catch (Exception e) { }

// GOOD - at minimum, log or rethrow with context
try {
    risky();
} catch (IOException e) {
    throw new UncheckedIOException("failed to process file", e);
}
```

## Concurrency

### Prefer High-Level Utilities
```java
// Use ExecutorService instead of raw Threads
ExecutorService pool = Executors.newFixedThreadPool(4);
Future<Integer> f = pool.submit(() -> compute());
int result = f.get();
pool.shutdown();

// Virtual threads (Java 21) for massive I/O concurrency
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> handleRequest());
}
```

### Visibility & Atomicity
```java
// volatile for visibility of a single flag
private volatile boolean running = true;

// Atomics for lock-free counters
private final AtomicInteger counter = new AtomicInteger();
counter.incrementAndGet();

// Concurrent collections instead of synchronizing manually
Map<String, Integer> map = new ConcurrentHashMap<>();
map.merge("key", 1, Integer::sum);
```

### Synchronization
```java
public class Counter {
    private int count = 0;
    public synchronized void increment() { count++; }   // mutual exclusion
    public synchronized int get() { return count; }
}

// Or an explicit lock for finer control
private final Lock lock = new ReentrantLock();
void update() {
    lock.lock();
    try { /* critical section */ }
    finally { lock.unlock(); }                           // always unlock in finally
}
```

> Prefer immutable objects and confinement; they need no synchronization at all.

## Strings & Performance

### Avoid String Concatenation in Loops
```java
// BAD - creates a new String each iteration (O(n^2))
// String s = "";
// for (int i = 0; i < 1000; i++) s = s + "x";

// GOOD - StringBuilder
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) sb.append("x");
String s = sb.toString();

// Or join a stream
String joined = nums.stream().map(String::valueOf)
                    .collect(Collectors.joining(", "));
```

### Pre-Size Collections When Capacity Is Known
```java
List<Integer> list = new ArrayList<>(1000);     // avoids regrowth
Map<String, Integer> map = new HashMap<>(256);
```

## Testing & Debugging

### Assertions (development checks)
```java
void process(int b) {
    assert b != 0 : "b must not be zero";       // run with java -ea
    // ...
}
```

### Logging Over System.out
```java
import java.util.logging.Logger;
private static final Logger log = Logger.getLogger(MyClass.class.getName());
log.info(() -> "value = " + value);             // lazy message construction
```

### Compiler Warnings
```bash
# Enable all lint warnings
javac --release 21 -Xlint:all File.java
```

## Checklist for Good Java

- [ ] Fields are `private`; mutable state is encapsulated
- [ ] Prefer immutability (`final` fields, records) where practical
- [ ] `equals` and `hashCode` overridden together (or use a record)
- [ ] `Optional` used for "may be absent" return values, never `null`
- [ ] Parameterized collections; no raw types
- [ ] Declared with interface types (`List`, `Map`), not implementations
- [ ] `try-with-resources` for anything `AutoCloseable`
- [ ] Exceptions carry context and never swallowed silently
- [ ] Streams used where they improve clarity, loops where they don't
- [ ] Concurrency via `java.util.concurrent`, not hand-rolled locking
- [ ] Compiler lint warnings enabled and fixed
- [ ] Comments explain "why", not "what"
- [ ] Consistent naming and style
- [ ] Performance verified, not assumed

---

## References

- **Platform**: Java SE 21 (LTS), [JLS 21](https://docs.oracle.com/javase/specs/)
- **API Docs**: [Java SE 21 API](https://docs.oracle.com/en/java/javase/21/docs/api/)
- **Effective Java** by Joshua Bloch
- **Java Concurrency in Practice** by Brian Goetz
- **The Java™ Tutorials**: https://docs.oracle.com/javase/tutorial/
