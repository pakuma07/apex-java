# Code Examples Directory

This directory contains runnable Java code examples for each chapter of the learning resource. They are idiomatic Java 21 adaptations of the companion C++ examples (so some chapters demonstrate the *Java equivalent* of a C++-only feature — see the per-chapter notes below).

> Verified to compile and run on **JDK 17** (the examples avoid Java 21-only language features, so they run on 17 and later).

## Getting Started

Each file is a complete, standalone program that demonstrates key concepts with ~10 independent examples driven from `main`.

### Compilation

Each file's public class name matches its filename stem (e.g. `chapter1_basics.java` declares `public class chapter1_basics`). To compile and run any example file:

```bash
javac chapter<N>_<topic>.java
java chapter<N>_<topic>
```

For example:

```bash
javac chapter1_basics.java
java chapter1_basics
```

> Tip: redirect class files to a separate directory to keep the source tree clean:
> ```bash
> javac -d out chapter1_basics.java
> java -cp out chapter1_basics
> ```

### Example File Structure

Each example file includes:
- ~10 independent, runnable examples
- Clear section headers and comments
- Educational notes explaining concepts
- Output demonstrations
- Best practices

## Available Examples

### Chapter 1: Basics
**File**: `chapter1_basics.java`
- Variable declaration and initialization
- Primitive data types (int, double, char, boolean)
- `var` local-variable type inference (Java equivalent of C++ `auto`)
- `final` / `static final` constants (vs C++ `const`/`constexpr`)
- Type casting (widening, narrowing, explicit casts)
- I/O formatting with `printf` / `String.format`
- Scope and shadowing
- Literals (decimal, octal, hex, binary, underscores)
- `Integer.BYTES` etc. (Java equivalent of `sizeof`)

### Chapter 2: Control Flow & Loops
**File**: `chapter2_control_flow.java`
- If-else statements and nesting
- `switch` statements and `switch` expressions
- While / do-while loops
- For loops and enhanced for-each
- Break, continue, labeled break
- Nested loops (patterns)

### Chapter 3: Functions
**File**: `chapter3_functions.java`
- Method definition, parameters and arguments
- Pass-by-value semantics (and how object references behave)
- Method overloading
- Forwarding overloads (Java equivalent of default arguments)
- Recursion (factorial, fibonacci)
- Lambdas, functional interfaces and method references (Java equivalent of function pointers)

### Chapter 4: References & Memory (C++ Pointers adapted)
**File**: `chapter4_pointers_references.java`
- Java references vs C++ pointers
- Object aliasing and identity (`==` vs `.equals`, `System.identityHashCode`)
- "Pass reference by value" and swapping via holder objects
- `new` without `delete` (garbage collection)
- Shared references instead of smart pointers
- Immutable views / `final` (Java equivalent of `const`)

### Chapter 5: Arrays & Strings
**File**: `chapter5_arrays_strings.java`
- Array declaration, access and `.length`
- Multi-dimensional arrays (2D matrices)
- Searching and sorting (`Arrays.sort`, binary search)
- `ArrayList` (dynamic arrays, vs C++ `vector`)
- Immutable `String` and `StringBuilder` for in-place edits
- String operations, comparison and search (`.equals` / `compareTo`)

### Chapter 6: OOP Basics
**File**: `chapter6_oop.java`
- Class definition and access modifiers
- Constructors
- Static members and methods
- `this` reference
- Encapsulation and data hiding
- `AutoCloseable` + try-with-resources (Java equivalent of destructors/RAII)
- Object creation and arrays of objects

### Chapter 7: Inheritance & Polymorphism
**File**: `chapter7_inheritance_polymorphism.java`
- Inheritance and `@Override` (methods are virtual by default)
- Abstract classes and interfaces (with default methods, vs multiple inheritance)
- Polymorphism and dynamic dispatch
- Pattern-matching `instanceof` (vs C++ `dynamic_cast`)

### Chapter 8: Value Semantics & Comparison (C++ Operator Overloading adapted)
**File**: `chapter8_operator_overloading.java`
- Java has no operator overloading — adapted to:
- `equals()` / `hashCode()` contracts
- `Comparable` / `Comparator` and sorting
- Named value methods (`add`, `subtract`, ...) on a `Complex`/`Point` value type
- `toString` (the `<<` analogue)

### Chapter 9: Generics (C++ Templates adapted)
**File**: `chapter9_templates.java`
- Generic classes and methods
- Bounded type parameters (`<T extends Comparable<T>>`)
- Wildcards (PECS: `? extends` / `? super`)
- Type erasure (contrasted with C++ per-type template instantiation)

### Chapter 10: Collections (C++ STL Containers adapted)
**File**: `chapter10_stl_containers.java`
- `ArrayList`, `LinkedList`, `ArrayDeque`
- `HashMap` / `TreeMap`, `HashSet` / `TreeSet`
- `PriorityQueue`, stack/queue via `ArrayDeque`
- Emulating multiset/multimap

### Chapter 11: Streams (C++ STL Algorithms adapted)
**File**: `chapter11_stl_algorithms.java`
- `filter` / `map` / `reduce` / `sorted` / `collect`
- `Collections.sort/reverse/rotate/binarySearch/fill`
- `IntStream` summary statistics, min/max

### Chapter 12: Memory Management
**File**: `chapter12_memory_management.java`
- Garbage collection (vs `new`/`delete`)
- Owning vs shared references (vs `unique_ptr`/`shared_ptr`)
- `WeakReference` / `SoftReference`
- try-with-resources / `AutoCloseable` (RAII analogue)

### Chapter 13: Exception Handling
**File**: `chapter13_exceptions.java`
- try / catch / finally
- Custom exception hierarchies
- Checked vs unchecked exceptions
- Multi-catch, re-throw, exception chaining (`getCause()`)
- try-with-resources

### Chapter 14: File I/O
**File**: `chapter14_file_io.java`
- `java.nio.file.Files` / `Path`
- `BufferedReader` / `BufferedWriter`
- Binary I/O (`DataOutputStream` / `DataInputStream`)
- `RandomAccessFile` (seek)
- Self-contained: writes to the system temp dir and cleans up afterward

### Chapter 15: Advanced Features
**File**: `chapter15_advanced_features.java`
- Lambdas, functional interfaces, closures
- `Optional`, streams, `var`
- Records, sealed types, enums with methods
- Method references, text blocks, pattern matching

### Chapter 16: Concurrency
**File**: `chapter16_concurrency.java`
- `Thread` / `Runnable`
- `synchronized`, `ReentrantLock`, `Condition`
- `AtomicInteger`
- `ExecutorService` + `Future`, `CompletableFuture`
- All threads joined / executors shut down cleanly

### Chapter 17: Reflection & Annotations (C++ Template Metaprogramming adapted)
**File**: `chapter17_tmp.java`
- Custom `@Retention(RUNTIME)` annotations
- Inspecting classes, methods and fields via `java.lang.reflect`
- Dynamic invocation
- Generics introspection (`ParameterizedType`)
- Contrasts compile-time C++ TMP with runtime Java reflection

### Chapter 18: Java Memory Model
**File**: `chapter18_memory_model.java`
- `volatile` and happens-before
- `synchronized` ordering
- Final-field safe publication
- `AtomicInteger` / `AtomicLong`, `compareAndSet`
- `ThreadLocal`; threads joined cleanly

### Chapter 19: JVM Internals
**File**: `chapter19_jvm_internals.java`
- Runtime identification via `RuntimeMXBean`
- Class-loader hierarchy and parent delegation
- Heap vs non-heap (metaspace/code cache) memory via `MemoryMXBean`
- Class-loading statistics, garbage collectors in use, thread/stack info
- Lazy, thread-safe `static` initialization
- Observable JIT warmup (a hot loop speeds up after compilation)

### Chapter 20: Date & Time API
**File**: `chapter20_date_time.java`
- `LocalDate`/`LocalDateTime`/`Instant`, `Duration`/`Period`/`ChronoUnit`
- Parsing & formatting with `DateTimeFormatter`, zone/DST conversion, legacy interop

### Chapter 21: Regular Expressions
**File**: `chapter21_regular_expressions.java`
- `Pattern`/`Matcher`, numbered & named groups, `replaceAll` with refs, `split`
- `find` loop with `start`/`end`, `Matcher.results()` stream, practical examples

### Chapter 22: Networking
**File**: `chapter22_networking.java`
- Local loopback TCP echo (ephemeral-port `ServerSocket` + client), clean shutdown
- `HttpClient` reference snippet (offline)

### Chapter 23: Logging
**File**: `chapter23_logging.java`
- `java.util.logging` Logger/Handler/Formatter, levels, parameterized & lazy messages
- Logging an exception with the throwable

### Chapter 25: Testing
**File**: `chapter25_testing.java`
- A dependency-free hand-rolled test harness (assert helpers + pass/fail summary)
- Equivalent JUnit 5 test shown in comments

### Chapter 27: Internationalization, Formatting & BigDecimal
**File**: `chapter27_internationalization.java`
- `NumberFormat` currency/percent across locales, `DecimalFormat` patterns
- `BigDecimal` money math (the `double` pitfall), `compareTo` vs `equals`, `MessageFormat`

> Chapters 24 (JDBC) and 26 (Build Tools) have no standalone `.java` example — their
> code requires external setup (a JDBC driver / a Maven/Gradle project), so the
> complete, copy-pasteable snippets live inline in those chapters' READMEs.

## Running the Examples

### Single Example
```bash
javac chapter1_basics.java
java chapter1_basics
```

### Compile All
```bash
for file in chapter*.java; do
    javac "$file"
done
```

### Run All
```bash
for file in chapter*.java; do
    stem="${file%.java}"
    echo "=== Running $stem ==="
    java "$stem"
done
```

> Note: each file is an independent program with its own `main`. Because some chapters define helper classes with the same simple names, prefer compiling each file into its own output directory (`javac -d out_$stem "$file"`) if you compile everything at once.

## Learning Tips

1. **Read the code**: Understand what each example does
2. **Run the program**: See the output
3. **Modify examples**: Change values and parameters
4. **Combine concepts**: Merge multiple examples
5. **Compare variations**: See different approaches
6. **Read the notes**: Learn why code works that way

## Compilation Flags

For better diagnostics:
```bash
# All recommended warnings
javac -Xlint:all chapter1_basics.java

# Show deprecation details
javac -Xlint:deprecation chapter1_basics.java

# Target/Source release explicitly
javac --release 17 chapter1_basics.java
```

## Next Steps

1. **Run each example**: Understand the output
2. **Study the code**: Read comments and learn concepts
3. **Modify examples**: Change parameters and see results
4. **Create variations**: Write your own similar examples
5. **Use in projects**: Apply these patterns in real code

## Progress Tracker

- [x] Chapter 1 - Basics
- [x] Chapter 2 - Control Flow
- [x] Chapter 3 - Functions
- [x] Chapter 4 - References & Memory (C++ Pointers adapted)
- [x] Chapter 5 - Arrays & Strings
- [x] Chapter 6 - OOP Basics
- [x] Chapter 7 - Inheritance & Polymorphism
- [x] Chapter 8 - Value Semantics & Comparison (C++ Operator Overloading adapted)
- [x] Chapter 9 - Generics (C++ Templates adapted)
- [x] Chapter 10 - Collections (C++ STL Containers adapted)
- [x] Chapter 11 - Streams (C++ STL Algorithms adapted)
- [x] Chapter 12 - Memory Management
- [x] Chapter 13 - Exception Handling
- [x] Chapter 14 - File I/O
- [x] Chapter 15 - Advanced Features
- [x] Chapter 16 - Concurrency
- [x] Chapter 17 - Reflection & Annotations (C++ Template Metaprogramming adapted)
- [x] Chapter 18 - Java Memory Model
- [x] Chapter 19 - JVM Internals
- [x] Chapter 20 - Date & Time API
- [x] Chapter 21 - Regular Expressions
- [x] Chapter 22 - Networking
- [x] Chapter 23 - Logging
- [x] Chapter 25 - Testing (hand-rolled harness; JUnit shown in README)
- [x] Chapter 27 - Internationalization, Formatting & BigDecimal

**STATUS: code examples for chapters 1-23, 25, 27 (chapters 24 JDBC & 26 Build Tools are README-only — they need external setup).**

## Questions & Debugging

If you encounter issues:

1. **Compilation errors**: Check that the public class name matches the filename
2. **`NoClassDefFoundError`**: Make sure your classpath (`-cp`) points at the output directory
3. **Runtime exceptions**: Read the stack trace top-down
4. **Logic errors**: Trace through the code step by step

## Standards

All examples follow these conventions:
- **Language level**: Java (verified on JDK 17; idiomatic Java 21 style)
- **Style**: Clear, readable code with comments
- **Output**: Demonstrates what the code does
- **Learning**: Educational value over optimization

## Java Code-Examples Entry Snippet

```java
public class Entry {
    static void runChapterExample(String chapter) {
        System.out.println("Run chapter sample: " + chapter);
        // Each real sample is in this folder as a standalone .java file.
    }

    public static void main(String[] args) {
        runChapterExample("stl_algorithms");
    }
}
```
