# The Complete Java Book 📚

A comprehensive guide covering all Java concepts from beginner to advanced levels, targeting the **Java 21 (LTS)** standard.

> Adapted from "The Complete C++ 11 Book". The chapter folder names are kept identical to the C++ edition for alignment, but every topic has been rewritten for modern Java. Where a chapter title was C++-specific (e.g. "Pointers & References", "Templates", "STL Containers", "Operator Overloading", "Template Metaprogramming"), it has been retitled to the closest Java concept and a note explains the mapping.

## 📖 Table of Contents

### [1. Basics & Fundamentals](01_basics/README.md)
- Getting Started with Java (JDK, `javac`, `java`)
- Primitive Types & Variables
- Type Conversion, Casting & Autoboxing
- Constants & Literals (`final`, text blocks)
- Basic Input/Output (`System.out`, `Scanner`)
- `var` Local Variable Inference, `assert`, Records (Java 21)

### [2. Control Flow & Loops](02_control_flow/README.md)
- If, Else If, Else Statements
- Switch Statements & Switch Expressions (`->`, `yield`)
- For Loops
- While & Do-While Loops
- Loop Control (Break, Continue, Labeled Break)
- Pattern Matching for `switch` (Java 21)

### [3. Methods](03_functions/README.md)
- Method Declaration & Definition
- Parameters & Arguments
- Return Values
- Method Overloading
- Varargs
- Recursion
- Lambdas, `Function`, Method References (functional interfaces)
- `static` Methods & Pure Functions

> Note: C++ "Functions" → Java "Methods" (Java has no free functions; everything lives in a class).

### [4. References & Memory](04_pointers_references/README.md)
- Object References vs Primitives
- Reference Semantics & Aliasing
- `null` and the `Optional` Alternative
- Pass-by-Value of References
- Identity vs Equality (`==` vs `equals`)
- Garbage Collection & Reachability

> Note: C++ "Pointers & References" → Java "References & Memory". Java has no raw pointers or pointer arithmetic; the chapter covers reference semantics and the GC model instead.

### [5. Arrays & Strings](05_arrays_strings/README.md)
- Arrays (1D, 2D, Jagged)
- Array Initialization & `Arrays` Utility
- `String` (immutability, pool)
- `StringBuilder` / `StringBuffer`
- String Operations & Methods
- Text Blocks & Unicode (Java 21)

### [6. Object-Oriented Programming Basics](06_oop_basics/README.md)
- Classes & Objects
- Constructors & Initialization
- Access Modifiers (`public`, `private`, `protected`, package-private)
- Fields & Methods
- The `this` Reference
- `static` Members
- Records (Java 21)

### [7. Inheritance & Polymorphism](07_inheritance_polymorphism/README.md)
- Inheritance (`extends`)
- Method Overriding (`@Override`)
- Dynamic Dispatch (virtual by default)
- Abstract Classes & Interfaces
- Polymorphism
- `final`, Sealed Classes & Interfaces (Java 21)
- Default & Static Interface Methods

### [8. Operators & Equality](08_operator_overloading/README.md)
- Arithmetic, Relational & Logical Operators
- `equals()` & `hashCode()` Contracts
- `Comparable` & `Comparator`
- `compareTo`, Natural Ordering
- `toString()`
- Why Java Has No Operator Overloading (and what to use instead)

> Note: C++ "Operator Overloading" → Java "Operators & Equality". Java does not allow user-defined operator overloading; the chapter covers the idiomatic substitutes (`equals`, `hashCode`, `compareTo`, `Comparator`).

### [9. Generics](09_templates/README.md)
- Generic Methods
- Generic Classes & Interfaces
- Bounded Type Parameters (`<T extends ...>`)
- Wildcards (`? extends`, `? super`)
- Type Erasure
- Generic Variance (PECS)

> Note: C++ "Templates" → Java "Generics". Java generics are erased at runtime (no template instantiation/specialization, no value parameters).

### [10. Collections Framework](10_stl_containers/README.md)
- `ArrayList`, `LinkedList`
- `ArrayDeque` (Deque)
- `Queue`, `Stack` / `Deque` as stack
- `HashSet`, `TreeSet`, `LinkedHashSet`
- `HashMap`, `TreeMap`, `LinkedHashMap`
- Immutable Collections (`List.of`, `Map.of`)

> Note: C++ "STL Containers" → Java "Collections Framework".

### [11. Streams & Algorithms](11_stl_algorithms/README.md)
- Iterators & `Iterable`
- Stream Creation & Pipelines
- Intermediate Operations (`filter`, `map`, `sorted`)
- Terminal Operations (`collect`, `reduce`, `count`)
- `Collectors` (grouping, joining, partitioning)
- Enhanced `for` & Parallel Streams

> Note: C++ "STL Algorithms" → Java "Streams & Algorithms". The Streams API is the Java analogue of `<algorithm>`.

### [12. Memory Management](12_memory_management/README.md)
- Stack vs Heap
- Object Allocation (`new`) & Garbage Collection
- Memory Leaks in Java (lingering references, listeners)
- `try-with-resources` & `AutoCloseable`
- Weak/Soft/Phantom References
- The RAII Analogue in Java

### [13. Exception Handling](13_exception_handling/README.md)
- Try-Catch-Finally
- Checked vs Unchecked Exceptions
- Standard Exceptions
- Custom Exceptions
- `throws` Clause & Multi-Catch
- `try-with-resources` & Suppressed Exceptions

### [14. File I/O](14_file_io/README.md)
- Streams & Readers/Writers
- `java.nio.file` (`Path`, `Files`)
- Text File Operations
- Binary File Operations
- Random Access & `SeekableByteChannel`

### [15. Advanced Features](15_advanced_features/README.md)
- Enums & Enum Methods
- Annotations
- Packages & Modules (JPMS)
- `var` Type Inference
- Lambda Expressions & Functional Interfaces
- Records & Sealed Types
- Varargs

### [16. Concurrency](16_concurrency/README.md)
- Thread Creation and Joining
- Passing Data to Threads (`Runnable`, `Callable`)
- `synchronized`, `ReentrantLock`, `ReadWriteLock`
- Condition Variables & `wait/notify`
- Atomic Types & `volatile`
- `ExecutorService`, `Future`, `CompletableFuture`
- Virtual Threads (Java 21) & Deadlock Avoidance

### [17. Reflection & Annotations](17_template_metaprogramming/README.md)
- The `Class<?>` Object & Runtime Type Information
- Reflective Field/Method/Constructor Access
- Dynamic Proxies
- Defining & Processing Annotations
- Annotation Retention & `@Retention`/`@Target`
- Metaprogramming Patterns (factories, dependency injection)

> Note: C++ "Template Metaprogramming" (compile-time computation) → Java "Reflection & Annotations" (the runtime metaprogramming model that Java provides instead).

### [18. Memory Model](18_memory_model/README.md)
- Why the Java Memory Model (JMM) Matters (data races)
- `volatile` Semantics & Visibility
- happens-before Relationships
- Synchronization & Mutual Exclusion
- `final` Field Semantics
- Atomics, `VarHandle` & Compare-and-Swap (CAS)

### [19. JVM Internals](19_jvm_internals/README.md)
- JDK vs JRE vs JVM; the source → bytecode → load → JIT pipeline
- Bytecode & the `.class` format (reading it with `javap`)
- Class-Loading Subsystem (loading, linking, initialization, class loaders, parent delegation)
- Runtime Data Areas (heap, metaspace, JVM stacks, PC register, code cache)
- Execution Engine: interpreter + HotSpot JIT (C1/C2, tiered compilation, deoptimization, warmup)
- JVM tooling (`jcmd`, `jstat`, `jmap`, `jstack`, JFR) and flags; CDS & GraalVM native image

> Note: This is a Java-specific chapter with no C++ equivalent — it explains what the JVM actually does between `javac` and a running program (the C++ edition compiles straight to native code with no runtime VM).

### [20. Date & Time API (`java.time`)](20_date_time/README.md)
- `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `ZonedDateTime`
- Machine time vs human time; `Duration` vs `Period`, `ChronoUnit`
- Parsing & formatting with `DateTimeFormatter`; time zones & DST
- Immutability, `TemporalAdjusters`, legacy `Date`/`Calendar` interop

### [21. Regular Expressions](21_regular_expressions/README.md)
- `Pattern` & `Matcher`; flags; `matches`/`find`/`lookingAt`
- Capturing, named, and non-capturing groups; backreferences
- Quantifiers, anchors, lookahead/lookbehind; replacement with group refs
- `Matcher.results()` streams; `Pattern.quote`; ReDoS pitfalls

### [22. Networking](22_networking/README.md)
- TCP `ServerSocket`/`Socket` (echo server + client), multi-client handling
- UDP `DatagramSocket`/`DatagramPacket`
- `URL`/`URI` and the modern `HttpClient` (sync, async, HTTP/2)
- Blocking vs non-blocking NIO; virtual threads for I/O

### [23. Logging](23_logging/README.md)
- Why logging vs `println`; log levels
- Built-in `java.util.logging` (Logger, Handler, Formatter, config)
- Parameterized & lazy `Supplier` messages; logging exceptions
- The SLF4J + Logback/Log4j 2 ecosystem; MDC

### [24. Database Access with JDBC](24_database_jdbc/README.md)
- The `java.sql` API & driver model; `DriverManager`/`Connection`
- `Statement` vs `PreparedStatement` (and SQL injection)
- `ResultSet`, transactions, batch updates, generated keys
- Connection pooling (HikariCP), the DAO pattern, ORM (JPA) overview

### [25. Testing](25_testing/README.md)
- The testing pyramid & TDD
- JUnit 5 (`@Test`, lifecycle, assertions, parameterized, `@Nested`)
- Mockito mocking; AssertJ fluent assertions
- Running tests via Maven Surefire / Gradle

### [26. Build Tools & Project Structure](26_build_tools/README.md)
- Standard project layout; why build tools
- Maven (`pom.xml`, coordinates, lifecycle, plugins, fat JARs)
- Gradle (`build.gradle(.kts)`, tasks, the wrapper)
- Dependency management, packaging, `jlink`, native image

### [27. Internationalization, Formatting & BigDecimal](27_internationalization/README.md)
- `Locale`, `NumberFormat`/`DecimalFormat` (currency, percent, patterns)
- `MessageFormat`, `ResourceBundle`, `Charset`/UTF-8, `Collator`
- `BigDecimal` for exact money math (why `double` is wrong)

### [28. Web Frameworks & Spring](28_web_frameworks/README.md)
- The Servlet foundation; IoC / dependency injection
- Spring Boot auto-configuration; REST with Spring MVC
- Spring Data JPA (and the N+1 trap); MVC vs WebFlux vs virtual threads
- Configuration & profiles; the Quarkus/Micronaut native-image alternatives

### [29. Performance Engineering](29_performance_engineering/README.md)
- Measure-first workflow; benchmarking with **JMH** (not `nanoTime` loops)
- JVM cost model (warmup, boxing, megamorphic dispatch)
- Profiling: **JFR** & async-profiler flame graphs
- GC tuning (G1/ZGC), tail latency, virtual threads for throughput

### [30. Production & Operational Concerns](30_production_operational/README.md)
- Twelve-factor config; structured logging (SLF4J/MDC) to stdout
- Observability: Actuator + Micrometer + OpenTelemetry; SLOs; health checks
- Graceful shutdown; containerizing the JVM (cgroup-aware heap, OOMKilled)
- Resilience with Resilience4j (timeouts, retries, circuit breakers, bulkheads)

### [31. Data Validation & Serialization](31_data_validation_serialization/README.md)
- "Parse, don't validate"; Jackson JSON binding with records
- Jakarta Bean Validation (`@Valid`, custom constraints)
- Protobuf/Avro; schema evolution & the Schema Registry
- Deserialization security (the `ObjectInputStream` RCE trap)

### [32. Security & Supply Chain](32_security_supply_chain/README.md)
- Injection, secrets, password hashing (Argon2/bcrypt), cryptography (JCA/Tink)
- Unsafe deserialization (Java's signature risk) and safe alternatives
- Supply chain & **Log4Shell**: dependency scanning, pinning, SBOMs, signing
- OWASP Top 10 for Java; JWT pitfalls; TLS hardening

### [33. System Design & Distributed Systems](33_system_design_distributed/README.md)
- Fallacies & latency numbers; CAP/PACELC; consistency models
- Partitioning (consistent hashing), replication & quorums, consensus
- Time/ordering, idempotency; sync vs async; caching at scale
- Designing for failure: bulkheads, backpressure, chaos (GC pauses & fencing)

### [34. Data-Intensive Systems](34_data_intensive_systems/README.md)
- Storage engines (B-tree vs LSM); indexes; reading query plans
- Transactions, isolation levels, MVCC; optimistic (`@Version`) vs pessimistic
- SQL vs NoSQL; OLTP vs OLAP; ETL/CDC (Debezium, outbox)
- Messaging & streaming (queues vs Kafka logs); access-pattern modeling

### [35. API & Interface Design](35_api_design/README.md)
- Interface principles; designing Java library APIs (interfaces, sealed, records)
- REST, gRPC, GraphQL — and when to use each
- Versioning & backward compatibility; consumer-driven contract tests
- AuthN/AuthZ, rate limiting, OpenAPI/`.proto` docs

### [36. Engineering Practice at Scale](36_engineering_practice/README.md)
- Design docs/RFCs; code review; automated quality gates
- Testing strategy (the pyramid, Testcontainers); trunk-based dev; CI/CD
- Incident response & blameless postmortems
- Tech debt, large-scale migrations (OpenRewrite), technical leadership

### [37. Garbage Collection: Algorithms & Tuning](37_garbage_collection/README.md)
- Reachability & GC roots; tracing vs reference counting; the generational hypothesis
- TLAB bump-pointer allocation; mark/sweep/compact/evacuate; write barriers, card tables, SATB, safepoints
- The collectors: Serial, Parallel, G1, ZGC (colored pointers), Shenandoah, Epsilon — mechanics & when to use each
- Tuning by goal; heap sizing; allocation rate; `-Xlog:gc*`, JFR, container-awareness

### [38. Reactive Programming (Reactor, RxJava & Backpressure)](38_reactive_programming/README.md)
- Reactive Streams spec (`Flow`: `Publisher`/`Subscriber`/`Subscription`) & the `request(n)` backpressure protocol
- Project Reactor: `Mono`/`Flux`, operators, schedulers, hot vs cold, "nothing happens until you subscribe", `Context`
- RxJava (`Observable` vs `Flowable`); error handling; testing with `StepVerifier`
- Pitfalls (blocking the event loop, bad stack traces) and the honest reactive-vs-virtual-threads verdict

### [39. JPA & Hibernate in Depth](39_jpa_hibernate/README.md)
- The object-relational impedance mismatch; JPA (spec) vs Hibernate (impl); the persistence context & entity lifecycle
- Dirty checking, flush timing; the N+1 problem and its fixes (JOIN FETCH, `@EntityGraph`, `@BatchSize`, DTOs)
- Lazy vs eager, `LazyInitializationException` & the open-session-in-view anti-pattern; `@Version` vs pessimistic locking
- Second-level cache, bulk-operation pitfalls, projections; reading generated SQL; when to drop to jOOQ/Spring Data JDBC/plain SQL

### [40. Data Structures (Complete Module)](data_structures/README.md)
- Linear Structures (Array, `ArrayList`, Linked List, Stack, Queue, Deque)
- Hash Tables
- Trees (BST, Heap, Trie, AVL overview)
- Graphs (Adjacency List/Matrix, BFS, DFS)
- Advanced Structures (DSU, Fenwick Tree, Segment Tree)

### [41. Algorithms (Complete Module)](algorithms/README.md)
- Searching and Sorting
- Pattern-Based Techniques (Two Pointers, Sliding Window, Prefix Sum)
- Recursion and Backtracking
- Divide and Conquer, Greedy, Dynamic Programming
- Graph, String, Number Theory, Bit, and Advanced Algorithms

### [42. Design Patterns](design_patterns/README.md)
- Creational (Singleton, Factory, Builder)
- Structural (Adapter, Decorator, Proxy)
- Behavioral (Strategy, Observer, Command)
- Java-Idiomatic Patterns (enums for singletons, functional strategies)

### [43. NeetCode Patterns](neet_code/README.md)
- Arrays & Hashing
- Two Pointers, Sliding Window
- Stack, Binary Search
- Linked List, Trees, Tries
- Backtracking, Graphs, Dynamic Programming
- Interview-Style Solutions in Java

### [44. Competitive Programming](competitive_programming/README.md)
- Fast I/O (`BufferedReader`, `StreamTokenizer`) and Contest Basics
- Time and Space Complexity Analysis
- Collections & Streams Tricks for CP
- Number Theory (GCD, Sieve, Modular Arithmetic, Matrix Exponentiation)
- Graph Algorithms (Dijkstra, Floyd, MST, DSU, Bridges, SCC)
- Dynamic Programming (LCS, LIS, Knapsack, Bitmask, Digit, Tree DP)
- String Algorithms (KMP, Z-function, Hashing, Trie, Manacher)
- Bit Manipulation and Bitmask DP
- Computational Geometry (Convex Hull, Polygon Area)
- Game Theory (Nim, Sprague-Grundy)
- Contest Strategy, Debugging, Common Mistakes
- Ready-to-Use CP Template (DSU, Segment Tree, Fenwick Tree, Dijkstra)

### [45. Exercises](exercises/README.md)
- Per-chapter practice problems
- Mini-projects and challenges

### [46. Code Examples](code_examples/README.md)
- Complete, compilable `.java` programs grouped by topic

### [47. Resources](resources/README.md)
- [Best Practices & Patterns](resources/BEST_PRACTICES.md)
- [OOP Cheatsheet](resources/OOP_CHEATSHEET.md)
- [Quick Reference](resources/QUICK_REFERENCE.md)
- [Collections & Streams Cheatsheet](resources/STL_CHEATSHEET.md)

---

## 🚀 Quick Start

### Learning Path
1. **Beginner**: Chapters 1-3 (Basics, Control Flow, Methods)
2. **Intermediate**: Chapters 4-8 (References, OOP, Operators & Equality)
3. **Advanced**: Chapters 9-19 (Generics, Collections, Streams, Memory, Advanced Features, Concurrency, Reflection, Memory Model, JVM Internals)
4. **Applied / Ecosystem**: Chapters 20-27 (Date & Time, Regex, Networking, Logging, JDBC, Testing, Build Tools, Internationalization)
5. **Staff / Production**: Chapters 28-32 (Spring, Performance Engineering, Production & Operational, Validation & Serialization, Security & Supply Chain)
6. **Architecture / Staff+**: Chapters 33-36 (System Design & Distributed Systems, Data-Intensive Systems, API Design, Engineering Practice at Scale)

### Compiling & Running Examples
```bash
# Compile with the Java 21 compiler
javac --release 21 Main.java

# Run the resulting class
java Main

# Run a single source file directly (no explicit compile step, Java 11+)
java Main.java
```

---

## 📝 Code Examples

Each chapter includes:
- **Detailed explanations** with code comments
- **Complete working examples** in `/code_examples/`
- **Best practices** and common mistakes
- **Performance considerations**

### Example Structure
```
Chapter X
├── README.md          (Full explanation)
├── Examples.java      (Code examples)
└── best_practices.md  (Tips & tricks)
```

---

## 🎯 Learning Objectives

By completing this book, you will understand:

✅ Java fundamentals and syntax  
✅ Object-oriented programming principles  
✅ Reference semantics and the garbage-collected memory model  
✅ The Collections Framework and Streams API  
✅ Generics and type-safe programming  
✅ Modern Java features (records, sealed types, pattern matching, virtual threads)  
✅ Core concurrency primitives and task-based execution  
✅ Best practices and design patterns  
✅ How to write efficient, maintainable code  

---

## 📊 Modern Java Features Covered

This book specifically highlights modern Java enhancements up to Java 21:

- **`var`** for local-variable type inference
- **Lambda expressions & method references** for functional programming
- **Streams API** for declarative data processing
- **Records** for transparent, immutable data carriers
- **Sealed classes/interfaces** for closed type hierarchies
- **Pattern matching** for `instanceof` and `switch`
- **Text blocks** for multi-line strings
- **`Optional`** for null safety
- **Virtual threads** for scalable concurrency
- **`java.util.concurrent`** for portable multi-threading

---

## 💡 Key Concepts Matrix

| Concept | Level | Chapter | Keywords |
|---------|-------|---------|----------|
| Variables | Beginner | 1 | primitives, `var`, initialization |
| Methods | Beginner | 3 | parameters, return, overloading |
| References | Intermediate | 4 | aliasing, `null`, GC |
| Classes | Intermediate | 6 | encapsulation, records |
| Inheritance | Advanced | 7 | `@Override`, sealed, abstract |
| Generics | Advanced | 9 | type parameters, erasure |
| Collections & Streams | Advanced | 10-11 | `List`, `Map`, pipelines |
| Memory | Advanced | 12 | heap, GC, `AutoCloseable` |
| Concurrency | Advanced | 16 | thread, lock, `Future`, virtual threads |
| Reflection | Advanced | 17 | `Class<?>`, annotations, proxies |
| JVM Internals | Advanced | 19 | bytecode, class loading, JIT, runtime areas |

---

## 📚 Resources

- **Platform**: Java SE 21 (LTS)
- **Toolchain**: JDK 21 (`javac`, `java`, `jshell`)
- **Build Tools**: Maven, Gradle
- **Online Editors**: JDoodle, OneCompiler, Replit
- **References**: [Java SE 21 API Docs](https://docs.oracle.com/en/java/javase/21/docs/api/), [The Java™ Tutorials](https://docs.oracle.com/javase/tutorial/), [JLS 21](https://docs.oracle.com/javase/specs/)

---

## 🔗 Repository Structure

```
java_book/
├── 01_basics/
├── 02_control_flow/
├── 03_functions/
├── 04_pointers_references/
├── 05_arrays_strings/
├── 06_oop_basics/
├── 07_inheritance_polymorphism/
├── 08_operator_overloading/
├── 09_templates/
├── 10_stl_containers/
├── 11_stl_algorithms/
├── 12_memory_management/
├── 13_exception_handling/
├── 14_file_io/
├── 15_advanced_features/
├── 16_concurrency/
├── 17_template_metaprogramming/
├── 18_memory_model/
├── 19_jvm_internals/
├── 20_date_time/
├── 21_regular_expressions/
├── 22_networking/
├── 23_logging/
├── 24_database_jdbc/
├── 25_testing/
├── 26_build_tools/
├── 27_internationalization/
├── 28_web_frameworks/
├── 29_performance_engineering/
├── 30_production_operational/
├── 31_data_validation_serialization/
├── 32_security_supply_chain/
├── 33_system_design_distributed/
├── 34_data_intensive_systems/
├── 35_api_design/
├── 36_engineering_practice/
├── data_structures/
├── algorithms/
├── design_patterns/
├── neet_code/
├── competitive_programming/
├── code_examples/
├── exercises/
├── resources/
└── README.md (this file)
```

> Folder names mirror the C++ edition for alignment. Their Java contents are described in the table of contents above.

---

## 🎓 How to Use This Book

1. **Read the Chapter**: Start with the README.md in each chapter folder
2. **Study Examples**: Look at the detailed code examples
3. **Practice**: Try modifying the examples
4. **Solve Exercises**: Complete practice problems
5. **Build Projects**: Combine concepts to build real programs

---

## 💻 Compilation & Run Flags

### Recommended Commands
```bash
# Target Java 21 explicitly
javac --release 21 File.java

# Enable all recommended warnings
javac --release 21 -Xlint:all File.java

# Run with assertions enabled (for the assert examples)
java -ea Main

# Single-file launch (compile + run in one step)
java File.java

# Explore interactively
jshell
```

---

## 🤔 Common Questions

### Q: Should I learn an older Java version instead?
A: Java 21 is the current LTS and a great baseline. Most pre-21 code still works; this book points out which features are newer.

### Q: How long to learn?
A: Beginner level (Chapters 1-5): 2-4 weeks  
Intermediate (Chapters 6-8): 3-4 weeks  
Advanced (Chapters 9-18): 4-6 weeks

### Q: Do I need prior programming experience?
A: Not required, but helpful. Chapter 1 starts from the basics.

### Q: Is Java still relevant?
A: Yes! Java powers Enterprise Backends, Android, Big Data (Spark, Hadoop), Cloud Services, Financial Systems, and more.

---

## 📈 Progress Tracker

Track your learning progress:

- [ ] Chapter 1: Basics
- [ ] Chapter 2: Control Flow
- [ ] Chapter 3: Methods
- [ ] Chapter 4: References & Memory
- [ ] Chapter 5: Arrays & Strings
- [ ] Chapter 6: OOP Basics
- [ ] Chapter 7: Inheritance & Polymorphism
- [ ] Chapter 8: Operators & Equality
- [ ] Chapter 9: Generics
- [ ] Chapter 10: Collections Framework
- [ ] Chapter 11: Streams & Algorithms
- [ ] Chapter 12: Memory Management
- [ ] Chapter 13: Exception Handling
- [ ] Chapter 14: File I/O
- [ ] Chapter 15: Advanced Features
- [ ] Chapter 16: Concurrency
- [ ] Chapter 17: Reflection & Annotations
- [ ] Chapter 18: Memory Model
- [ ] Chapter 19: JVM Internals
- [ ] Chapter 20: Date & Time API
- [ ] Chapter 21: Regular Expressions
- [ ] Chapter 22: Networking
- [ ] Chapter 23: Logging
- [ ] Chapter 24: Database Access with JDBC
- [ ] Chapter 25: Testing
- [ ] Chapter 26: Build Tools & Project Structure
- [ ] Chapter 27: Internationalization, Formatting & BigDecimal
- [ ] Chapter 28: Web Frameworks & Spring
- [ ] Chapter 29: Performance Engineering
- [ ] Chapter 30: Production & Operational Concerns
- [ ] Chapter 31: Data Validation & Serialization
- [ ] Chapter 32: Security & Supply Chain
- [ ] Chapter 33: System Design & Distributed Systems
- [ ] Chapter 34: Data-Intensive Systems
- [ ] Chapter 35: API & Interface Design
- [ ] Chapter 36: Engineering Practice at Scale

---

## 🤝 Contributing

Found an error or want to add content? Contributions welcome!

---

## 📄 License

This learning material is provided as-is for educational purposes.

---

## 🎉 Let's Get Started!

Begin with [Chapter 1: Basics](01_basics/README.md) and embark on your Java journey!

**Happy Learning! 🚀**

## Java 21 Project-Wide Example

```java
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Integer> practice = List.of(3, 1, 4, 1, 5);
        int total = practice.stream().mapToInt(Integer::intValue).sum();
        System.out.println("java_book practice sum = " + total);
    }
}
```
