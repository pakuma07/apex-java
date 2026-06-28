# Chapter 23: Logging -- Java

Logging is the practice of emitting a durable, structured record of what a program does — far more disciplined than scattering `System.out.println` calls. A logging framework lets you tag each message with a *severity level*, route messages to one or more destinations (console, rotating files, a network collector), control *at runtime* which messages are emitted without recompiling, and format records consistently with timestamps, the originating class, and thread. Java ships a logging framework in the JDK — **`java.util.logging`** (JUL, since Java 1.4) — and the wider ecosystem standardizes on the **SLF4J** facade backed by **Logback** or **Log4j 2**.

This chapter covers why logging beats `println`, the JUL API (`Logger`, `Level`, `Handler`, `Formatter`, `logging.properties`, parameterized and lazy `Supplier` messages, logging exceptions), the logger hierarchy and configuration, and the SLF4J/Logback/Log4j2 ecosystem (the facade pattern, dependencies, `{}` parameterized logging, and MDC). The cardinal rule of logging: log the right things at the right level, never log secrets, and never pay to build a message you are not going to emit.

> **C++ logging → Java equivalents — at a glance**
> - `std::cout`/`std::cerr` debugging → a `Logger` at `INFO`/`SEVERE` (controllable, timestamped)
> - `#ifdef DEBUG` compile-time gating → runtime `Level` configuration (no recompile)
> - spdlog / glog / Boost.Log → `java.util.logging` (built-in) or SLF4J + Logback/Log4j2
> - spdlog sinks → JUL `Handler`s (Console, File, Socket)
> - spdlog `fmt`-style `"{}"` → SLF4J `"{}"` placeholders / JUL `"{0}"` placeholders

## 23.1 Why Logging Instead of println

`System.out.println` is fine for a five-line script, but it scales badly. It has no notion of *severity*, so you cannot distinguish a routine event from a catastrophe; it has no *origin*, so you cannot tell which class emitted it; it always writes (you must delete or comment it out to silence it); it goes only to stdout, never a file; and it is unstructured, so no tool can parse or filter it. A logging framework fixes all of this. Each call carries a **level**, a **logger name** (usually the class), and the framework adds a timestamp, thread, and source automatically. You then *configure* — at deploy time, not compile time — which levels are emitted and where they go.

```java
import java.util.logging.Logger;

public class OrderService {
    // Convention: one logger per class, named after the class
    private static final Logger log = Logger.getLogger(OrderService.class.getName());

    void place(Order o) {
        log.info("Placing order " + o.id());          // routine event
        // ... vs. System.out.println, which has no level, no origin, no off switch
    }
}
```

> **Contrast with C++:** C programs lean on `printf`/`std::cerr` and `#ifdef DEBUG` to compile debug output in or out — a *compile-time* decision. Java logging is a *runtime* decision: ship one binary and turn levels up or down via a config file or a property, even on a running server. C++ gets the equivalent only from a library (spdlog, glog, Boost.Log).

---

## 23.2 Logging Levels

Every log message has a **level** indicating its severity, and every logger and handler has a level *threshold* — messages below the threshold are discarded cheaply. JUL defines seven levels (plus `ALL` and `OFF`), from most to least severe:

| JUL `Level` | Meaning | SLF4J/Logback analogue |
|-------------|---------|------------------------|
| `SEVERE`  | A serious failure | `ERROR` |
| `WARNING` | A potential problem | `WARN` |
| `INFO`    | Normal significant events | `INFO` |
| `CONFIG`  | Configuration details | (folds into `DEBUG`) |
| `FINE`    | Tracing / debug detail | `DEBUG` |
| `FINER`   | More detailed tracing | `DEBUG`/`TRACE` |
| `FINEST`  | Most detailed tracing | `TRACE` |

`Level.ALL` enables everything; `Level.OFF` disables everything. The default threshold for the root logger is `INFO`, so `FINE`/`FINER`/`FINEST` are *not* shown until you lower the threshold. Use the levels with discipline: `SEVERE`/`WARNING` for things an operator must notice, `INFO` for high-level milestones, and `FINE`–`FINEST` for developer tracing that is normally off in production.

```java
log.severe("Database connection lost");      // SEVERE
log.warning("Cache miss rate is high");      // WARNING
log.info("Server started on port 8080");     // INFO
log.config("Loaded config from app.yml");    // CONFIG
log.fine("Entering computeTotals()");        // FINE  (hidden at default INFO threshold)
log.finest("loop i=" + i);                   // FINEST

// The general form: log.log(Level, message)
log.log(java.util.logging.Level.WARNING, "explicit-level message");
```

---

## 23.3 java.util.logging — Logger, Handler, Formatter

JUL has three core abstractions. A **`Logger`** is what your code calls; you obtain one by name with `Logger.getLogger(name)`. A **`Handler`** is a destination — `ConsoleHandler` (writes to `System.err`), `FileHandler` (writes to a file, with optional rotation), `SocketHandler`. A **`Formatter`** turns a log record into text — `SimpleFormatter` (one human-readable line) or `XMLFormatter` (machine-readable XML). A logger can have several handlers, and each handler has its own level and formatter. Crucially, **a message is emitted only if it passes both the logger's level and the handler's level**, so to see `FINE` messages you must lower *both*.

```java
import java.util.logging.*;

Logger log = Logger.getLogger("com.example.app");

// 1. Let the logger pass FINE and above
log.setLevel(Level.FINE);

// 2. Don't also send records to the parent's (root) default console handler
log.setUseParentHandlers(false);

// 3. Attach a console handler that also passes FINE, with a simple text format
ConsoleHandler console = new ConsoleHandler();
console.setLevel(Level.FINE);
console.setFormatter(new SimpleFormatter());
log.addHandler(console);

// 4. Also write SEVERE+ to a rotating file (5 files, ~1 MB each, append)
FileHandler file = new FileHandler("app-%g.log", 1_000_000, 5, true);
file.setLevel(Level.SEVERE);
file.setFormatter(new XMLFormatter());   // or SimpleFormatter for plain text
log.addHandler(file);

log.fine("now visible on the console");
log.severe("also written to app-0.log");
```

The `SimpleFormatter`'s layout is itself configurable via the `java.util.logging.SimpleFormatter.format` system property (a `String.format` pattern), e.g. `"%1$tF %1$tT %4$s %3$s - %5$s%6$s%n"` for a `date time LEVEL logger - message` line.

---

## 23.4 Parameterized and Lazy Messages

Two techniques keep logging cheap. First, **parameterized messages** use `{0}`, `{1}` placeholders filled from an argument array — the framework substitutes them *only if the record is actually published*, avoiding wasteful string work and keeping the template clean. Second, and more importantly, the `log(Level, Supplier<String>)` overloads (Java 8+) take a **lazy `Supplier`**: the lambda that builds the message runs *only if the level is enabled*. This matters when constructing the message is expensive (serializing an object, joining a collection) — at the default `INFO` threshold, a `log.fine(() -> expensive())` call costs nothing because the supplier is never invoked.

```java
import java.util.logging.*;

Logger log = Logger.getLogger("com.example.app");

// Parameterized: placeholders substituted only when the record is published
log.log(Level.INFO, "User {0} placed order {1}", new Object[]{ userId, orderId });
log.log(Level.INFO, "Single arg: {0}", userId);    // single-arg convenience overload

// Lazy Supplier: the lambda runs ONLY if FINE is enabled (huge win for hot paths)
log.fine(() -> "expensive dump: " + buildBigReport());     // not called at INFO threshold

// The general lazy form
log.log(Level.FINEST, () -> "state = " + serialize(state));

// ❌ Anti-pattern: this concatenation runs even when FINE is disabled
log.fine("expensive dump: " + buildBigReport());           // buildBigReport() always called!
```

> **Contrast with C++:** spdlog achieves the same "don't pay for disabled logs" goal with `SPDLOG_DEBUG` macros that compile out, and with its `fmt`-based lazy formatting. Java's `Supplier` overloads give the same deferral at runtime without macros, and the `{0}` placeholder style mirrors `fmt`'s `{}`.

---

## 23.5 Logging Exceptions

When you catch an exception, log it *with its stack trace*, not just its message. JUL's `log(Level, String, Throwable)` overload takes the throwable as a separate argument so the formatter prints the full stack trace. Never swallow an exception silently, and never log only `e.getMessage()` (which loses the stack and the cause chain).

```java
import java.util.logging.*;

try {
    riskyOperation();
} catch (IOException e) {
    // message + throwable: the formatter prints the full stack trace and cause chain
    log.log(Level.SEVERE, "Failed to process file " + path, e);
}

// ❌ Don't do this — loses the stack trace and cause:
// log.severe("Failed: " + e.getMessage());
// ❌ And never swallow it silently:
// catch (IOException e) { }
```

---

## 23.6 The Logger Hierarchy and Configuration

JUL loggers form a **hierarchy** based on dotted names, like packages: `com.example.app.OrderService` is a child of `com.example.app`, which is a child of `com.example`, up to the unnamed **root logger** (`""`). A logger without its own level *inherits* its effective level from the nearest ancestor that has one, and by default a record flows up to every ancestor's handlers too (controlled by `setUseParentHandlers`). This lets you set one threshold for a whole subsystem (`com.example.app = FINE`) while keeping the rest at `INFO`.

Rather than configuring in code, JUL reads a **`logging.properties`** file at startup. The JDK ships a default at `$JAVA_HOME/conf/logging.properties`; point JUL at your own with the system property `-Djava.util.logging.config.file=logging.properties`.

```properties
# logging.properties — configure handlers, formats, and per-logger levels

# Root logger: which handlers, and the default level
handlers = java.util.logging.ConsoleHandler
.level   = INFO

# Console handler settings
java.util.logging.ConsoleHandler.level     = ALL
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter

# SimpleFormatter line layout: 2026-06-23 10:15:00 INFO com.example - message
java.util.logging.SimpleFormatter.format = %1$tF %1$tT %4$s %3$s - %5$s%6$s%n

# Per-logger (and per-subtree) overrides — verbose tracing for one package only
com.example.app.level = FINE
org.noisy.library.level = WARNING
```

```bash
# Apply it at launch
java -Djava.util.logging.config.file=logging.properties -jar app.jar
```

---

## 23.7 The Ecosystem — SLF4J, Logback, Log4j 2

In real-world Java, most projects do not call JUL directly. The community standard is **SLF4J** (Simple Logging Facade for Java): an *API only* — a thin set of interfaces your code logs against — with **no logging logic of its own**. At runtime you add exactly one **implementation (binding)** on the classpath, typically **Logback** (SLF4J's native companion) or **Log4j 2**. Your code depends only on the facade; swapping the backend is a dependency change, not a code change.

```java
// Application code depends ONLY on the SLF4J facade
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    void place(Order o) {
        // SLF4J uses "{}" placeholders — substituted lazily, only if the level is enabled
        log.info("Placing order {} for user {}", o.id(), o.userId());
        log.debug("order detail: {}", o);                 // toString() called only if DEBUG on

        try {
            o.submit();
        } catch (Exception e) {
            log.error("Failed to place order {}", o.id(), e);  // last arg = the Throwable
        }
    }
}
```

The `{}` placeholders give the same "no concatenation unless emitted" benefit as JUL's `Supplier`, but with cleaner syntax — and SLF4J detects a trailing `Throwable` argument automatically, so `log.error("msg {}", arg, exception)` logs the value *and* the stack trace.

### Why a facade?

A library that logs against SLF4J imposes no logging backend on the application that uses it; the application picks the implementation. This avoids the classic problem of two libraries demanding two incompatible logging frameworks. SLF4J also bridges legacy APIs (`jul-to-slf4j`, `log4j-over-slf4j`) so everything funnels through one backend.

### Dependencies (brief)

```xml
<!-- Maven: the facade + Logback implementation (Logback pulls in slf4j-api) -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.6</version>
</dependency>
```

```groovy
// Gradle equivalent
implementation 'ch.qos.logback:logback-classic:1.5.6'
// (Log4j 2 alternative: org.apache.logging.log4j:log4j-slf4j2-impl + log4j-core)
```

Logback is configured with `logback.xml`; Log4j 2 with `log4j2.xml`. Both support console and rolling-file appenders, pattern layouts, and per-logger levels analogous to JUL's `logging.properties`.

### MDC — Mapped Diagnostic Context

For server applications, **MDC** attaches per-thread key/value context (a request ID, user ID, tenant) to *every* log line on that thread, without threading the value through every method. You `put` keys at the start of a request and `clear` at the end (or use try-with-resources via `MDC.putCloseable`), and reference them in the layout pattern (e.g. `%X{requestId}`).

```java
import org.slf4j.MDC;

// At the start of handling a request
MDC.put("requestId", requestId);
MDC.put("userId", userId);
try {
    log.info("processing");            // layout %X{requestId} stamps the id onto this line
} finally {
    MDC.clear();                       // ALWAYS clear — threads are reused in pools
}
```

> **Contrast with C++:** spdlog and Boost.Log are concrete libraries with no facade tier, so a C++ application typically commits to one logging library across all its code. Java's SLF4J facade decouples *logging calls* from the *logging implementation*, a layering that has no direct C++ standard analogue.

---

## 23.8 Best Practices

The following idioms summarize how to log well in modern Java.

```java
// ✅ One logger per class, named after the class
private static final Logger log = Logger.getLogger(MyClass.class.getName());

// ✅ Parameterized / lazy messages — never string-concatenate in the call
log.log(Level.INFO, "user {0} did {1}", new Object[]{ user, action });   // JUL
log.fine(() -> "expensive: " + dump());                                  // lazy Supplier
// slf4jLog.debug("expensive: {}", obj);                                  // SLF4J

// ✅ Log exceptions WITH the throwable (full stack trace), don't swallow
log.log(Level.SEVERE, "operation failed", e);

// ✅ Use levels correctly: SEVERE/WARNING for operators, INFO milestones, FINE+ tracing

// ✅ Configure via file (logging.properties / logback.xml), not hard-coded in code

// ✅ Log an event ONCE — at the layer that has the most context. Don't log-and-rethrow
//    at every level, which produces duplicate stack traces.

// ❌ NEVER log secrets: passwords, tokens, API keys, full card/PII data
// log.info("login user=" + user + " password=" + password);   // catastrophic

// ✅ Prefer structured logging (key/value or JSON) so logs are machine-queryable
```

The central themes: one logger per class, parameterized/lazy messages (never eager concatenation), log exceptions with their throwable, choose levels deliberately, configure externally, log each event exactly once, and never, ever log secrets.

---

## Summary

| Concept | Java API |
|---------|----------|
| **Get a logger** | `Logger.getLogger(MyClass.class.getName())` |
| **Levels (severe→fine)** | `SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST` (+ `ALL`/`OFF`) |
| **Log at a level** | `log.info(...)`, `log.severe(...)`, `log.log(Level, msg)` |
| **Destinations** | `Handler` — `ConsoleHandler`, `FileHandler`, `SocketHandler` |
| **Format** | `Formatter` — `SimpleFormatter`, `XMLFormatter` |
| **Parameterized** | `log.log(Level, "{0} {1}", new Object[]{a, b})` |
| **Lazy message** | `log.fine(() -> expensive())` (`Supplier`, runs only if enabled) |
| **Log an exception** | `log.log(Level.SEVERE, msg, throwable)` |
| **Hierarchy & config** | dotted logger names; `logging.properties` (`-Djava.util.logging.config.file`) |
| **Facade (ecosystem)** | SLF4J (`org.slf4j.Logger`, `LoggerFactory`) |
| **Implementations** | Logback (`logback-classic`), Log4j 2 |
| **SLF4J placeholders** | `log.info("{} {}", a, b)`; trailing `Throwable` auto-detected |
| **Per-thread context** | MDC (`MDC.put` / `MDC.clear`, `%X{key}` in layout) |

---

## Next Steps
- Replace `println` debugging with a per-class `Logger` and proper levels
- Configure JUL with a `logging.properties` file and a `FileHandler`
- Use parameterized and lazy `Supplier` messages; log exceptions with the throwable
- For real projects, log against SLF4J and add Logback or Log4j 2
- Move to [Chapter 24: Database Access with JDBC](../24_database_jdbc/README.md)
