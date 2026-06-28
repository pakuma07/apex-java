# Chapter 13: Exception Handling

Exceptions are Java's mechanism for separating error-handling code from the normal flow of logic. When a method detects a condition it cannot handle, it `throw`s an exception object; the runtime then unwinds the call stack — popping each stack frame in turn — until it finds a matching `catch` handler in some calling method. This lets errors propagate up to a level capable of dealing with them without every intermediate method having to check and forward return codes.

This chapter covers the syntax (`try`/`catch`/`finally`/`throw`), the `Throwable` hierarchy rooted at `Throwable` (splitting into `Error`, `Exception`, and `RuntimeException`), the distinctively Java concept of **checked vs unchecked** exceptions, how to write custom exceptions, multi-catch, exception chaining (the *cause*), try-with-resources and *suppressed* exceptions, stack traces, and best practices. A recurring theme is the contrast with C++: Java has **no `noexcept`**, **no `throw(...)` specifications** (instead it has `throws` *clauses* enforced at compile time), **no `catch (...)`** (you catch `Throwable` instead), and — most importantly — Java uses `finally` and try-with-resources for deterministic cleanup, whereas C++ relies on RAII destructors run during stack unwinding.

> **C++ exceptions vs Java exceptions — at a glance**
> - C++ can `throw` *any* type (an `int`, a `std::string`, anything). Java can only throw objects whose class extends `java.lang.Throwable`.
> - C++ has **no checked exceptions** — every function may throw anything. Java has **checked exceptions** the compiler forces you to handle or declare with `throws`.
> - C++ cleans up with **RAII** (destructors run during unwinding). Java has **no destructors**; it uses `finally` and **try-with-resources** (`AutoCloseable`).
> - C++ `catch (...)` catches anything. Java's closest equivalent is `catch (Throwable t)`.
> - C++ `noexcept` and `throw(...)` specs have no Java analogue; Java's `throws` clause is the inverse — it *declares* what may propagate and is compiler-enforced.

## 13.1 Try-Catch Blocks

The `try`/`catch` construct is the foundation of exception handling. Code that might fail goes inside a `try` block; if any statement within it throws, execution immediately jumps to a matching `catch` block, skipping the rest of the `try`. A `catch` clause names the exception type it handles; the caught reference is *final-by-convention* and lets you inspect the exception via `getMessage()`, `getCause()`, and `printStackTrace()`. If no statement throws, the `catch` blocks are skipped entirely, so there is no cost on the success path. Unlike C++ — where you catch by `const` reference to avoid slicing and copying — Java exceptions are always objects accessed through a reference, so there is no slicing concern.

```java
public class Main {
    public static void main(String[] args) {
        try {
            int x = 10;
            int y = 0;

            if (y == 0) {
                throw new ArithmeticException("Division by zero!");
            }

            int result = x / y;
        }
        catch (ArithmeticException e) {
            System.out.println("Error caught: " + e.getMessage());
        }
    }
}
```

> **Contrast with C++:** C++ requires `catch (const std::runtime_error& e)` and reads the message with `e.what()`. Java catches `ArithmeticException e` (no `const`, no `&`) and reads the message with `e.getMessage()`. Note that integer division by zero actually throws `ArithmeticException` automatically in Java — there is no need to test for it manually — whereas in C++ it is undefined behavior, not an exception.

---

## 13.2 The Throwable Hierarchy

Every exception in Java is an object whose class descends from `java.lang.Throwable`. `Throwable` has two direct subclasses with very different intent: `Error` represents serious problems a normal application should *not* try to catch (e.g. `OutOfMemoryError`, `StackOverflowError`), while `Exception` represents conditions an application might reasonably want to handle. Within `Exception`, the subtree rooted at `RuntimeException` is **unchecked**; everything else under `Exception` is **checked** (see 13.3). `Throwable` provides the key methods every exception shares: `getMessage()`, `getCause()`, `getStackTrace()`, `printStackTrace()`, and `getSuppressed()`. Reusing the standard types — rather than throwing strings — lets callers catch at whatever granularity they need, including a single `catch (Exception e)` that handles a whole family.

```java
// The Throwable hierarchy
// Throwable
// ├─ Error                         (unchecked — do NOT catch)
// │  ├─ OutOfMemoryError
// │  ├─ StackOverflowError
// │  └─ ...
// └─ Exception                     (checked, EXCEPT the RuntimeException subtree)
//    ├─ IOException                (checked)
//    │  └─ FileNotFoundException
//    ├─ SQLException               (checked)
//    └─ RuntimeException           (UNCHECKED)
//       ├─ NullPointerException
//       ├─ IllegalArgumentException
//       │  └─ NumberFormatException
//       ├─ IndexOutOfBoundsException
//       │  ├─ ArrayIndexOutOfBoundsException
//       │  └─ StringIndexOutOfBoundsException
//       ├─ IllegalStateException
//       ├─ ArithmeticException
//       └─ ClassCastException
```

```java
import java.util.List;

// IndexOutOfBoundsException (unchecked)
try {
    List<Integer> v = List.of(1, 2, 3);
    System.out.println(v.get(10));   // Throws IndexOutOfBoundsException
}
catch (IndexOutOfBoundsException e) {
    System.out.println(e.getMessage());  // "Index 10 out of bounds for length 3"
}

// NumberFormatException (a subclass of IllegalArgumentException)
try {
    int value = Integer.parseInt("abc");
}
catch (NumberFormatException e) {
    System.out.println("Invalid: " + e.getMessage());
}

// Catch the whole Exception family (but not Error)
try {
    throw new RuntimeException("Something failed");
}
catch (Exception e) {
    System.out.println(e.getMessage());
}
```

> **Contrast with C++:** C++ roots its hierarchy at `std::exception` with a virtual `what()` and splits into `logic_error`/`runtime_error`. Java roots at `Throwable` with `getMessage()`, and the meaningful split is `Error` vs `Exception` vs `RuntimeException`. There is no `catch (...)` in Java; the universal catch-all is `catch (Throwable t)`, but you should almost never catch `Error`.

---

## 13.3 Checked vs Unchecked Exceptions

This is the single biggest concept Java adds over C++, so it deserves a careful treatment. Java divides exceptions into two compile-time categories:

- **Checked exceptions** — subclasses of `Exception` that are *not* subclasses of `RuntimeException` (e.g. `IOException`, `SQLException`). The compiler **enforces** that any code which can throw a checked exception either catches it or declares it with a `throws` clause. This is the *catch-or-declare* rule. They model recoverable conditions the caller is expected to anticipate (a file might not exist, a network might be down).
- **Unchecked exceptions** — subclasses of `RuntimeException`, plus all `Error`s. The compiler does **not** require you to handle or declare them. They model programming bugs (`NullPointerException`, `IllegalArgumentException`, `ArrayIndexOutOfBoundsException`) or unrecoverable conditions (`OutOfMemoryError`).

```java
import java.io.*;

// CHECKED: the compiler forces you to handle or declare IOException
void readFile(String path) throws IOException {     // declared with throws
    BufferedReader r = new BufferedReader(new FileReader(path));
    r.readLine();   // may throw IOException
    r.close();
}

// Or handle it locally instead of declaring:
void readFileHandled(String path) {
    try {
        readFile(path);
    }
    catch (IOException e) {                          // handled here
        System.out.println("Could not read: " + e.getMessage());
    }
}

// UNCHECKED: no throws clause needed, compiler does not force handling
int parse(String s) {
    return Integer.parseInt(s);   // may throw NumberFormatException (unchecked) — no declaration required
}
```

> **Contrast with C++:** C++ has **no concept of checked exceptions at all** — every function may throw anything, and the compiler never forces you to handle or declare it. The deprecated C++03 `throw(SomeType)` dynamic specification was a runtime check (a violation called `std::unexpected`), and it was *removed* in C++17 precisely because it did not work well. Java's `throws` is a *compile-time* check and is the closest thing in either language to a real exception contract. The cost is some friction ("checked-exception fatigue"); the benefit is that the API documents and enforces what can go wrong.

**When to use which:** Throw a *checked* exception when the caller can reasonably be expected to recover (and you want the compiler to remind them). Throw an *unchecked* exception (typically `IllegalArgumentException`, `IllegalStateException`, or a custom `RuntimeException` subclass) for programming errors or when forcing every caller to handle the condition would be onerous. Modern Java APIs and most frameworks lean heavily toward unchecked exceptions.

---

## 13.4 throw and throws

`throw` (singular) is a statement that raises an exception; `throws` (plural) is a clause in a method signature declaring which checked exceptions the method may propagate. They are different keywords doing different jobs. You `throw` an *instance* (always `new SomeException(...)`), and the runtime begins searching for a handler. You list `throws` on a method so callers — and the compiler — know a checked exception may escape.

```java
import java.io.IOException;

class BankAccount {
    private double balance;

    // 'throws' declares the checked exception this method may propagate
    void withdraw(double amount) throws InsufficientFundsException {
        if (amount < 0) {
            // IllegalArgumentException is unchecked — needs no 'throws'
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > balance) {
            // checked — must be declared in 'throws' above
            throw new InsufficientFundsException("Balance too low");
        }
        balance -= amount;
    }
}

// A method can declare multiple checked exceptions, comma-separated
void doWork() throws IOException, InsufficientFundsException {
    // ...
}
```

A `throw` with no operand exists only inside a `catch` block as `throw e;` to rethrow — but unlike C++'s bare `throw;` (which rethrows the *currently active* exception), Java requires you to name the reference: `throw e;`.

> **Contrast with C++:** C++ has only `throw` (both to raise and, bare, to rethrow). Java splits the rethrow idiom into the named `throw e;` and adds the entirely separate `throws` clause for the checked-exception contract. C++'s nearest analogue to `throws` was the now-removed dynamic exception specification.

---

## 13.5 finally and Guaranteed Cleanup

A `finally` block attached to a `try` is **guaranteed to run** whether the `try` completes normally, throws, or even returns — making it the place for cleanup that must always happen (closing files, releasing locks, restoring state). This is Java's answer to C++'s problem of code that an exception can "fly past". Because Java has no destructors and thus no RAII, `finally` (and its modern successor, try-with-resources in 13.6) is *the* deterministic-cleanup mechanism.

```java
import java.io.*;

void processFile(String filename) {
    BufferedReader reader = null;
    try {
        reader = new BufferedReader(new FileReader(filename));
        String line = reader.readLine();
        process(line);
    }
    catch (IOException e) {
        System.out.println("Error: " + e.getMessage());
    }
    finally {
        // Always runs — even if an exception was thrown or a return executed
        if (reader != null) {
            try { reader.close(); }
            catch (IOException ignored) { /* best-effort close */ }
        }
    }
}
```

Caveats worth knowing: a `return`, `break`, or `continue` inside `finally` will *override* one from the `try` block (and swallow an in-flight exception) — so never `return` from `finally`. If both the `try` body and the `finally` throw, the `finally`'s exception wins and the original is lost (try-with-resources fixes this by *suppressing* rather than discarding — see 13.7).

> **Contrast with C++:** C++ has **no `finally`**. The idiomatic substitute is RAII: cleanup lives in a destructor that stack unwinding is guaranteed to run, so a `std::lock_guard` or `std::unique_ptr` releases automatically. Java instead writes the cleanup explicitly in `finally`, or — far better — lets try-with-resources call `close()` for it.

---

## 13.6 Try-With-Resources

Manually nesting `close()` calls in `finally` is verbose and easy to get wrong. **Try-with-resources** (Java 7+) declares one or more resources in parentheses after `try`; any resource implementing `java.lang.AutoCloseable` (or its subinterface `Closeable`) is **automatically closed** at the end of the block, in reverse order of declaration, whether the block completes normally or via an exception. This is the closest Java gets to C++ RAII — scoped, deterministic, automatic cleanup — and it is the strongly preferred idiom for any resource.

```java
import java.io.*;
import java.nio.file.*;

// Single resource — reader.close() is called automatically
void readFirstLine(String filename) throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(Path.of(filename))) {
        System.out.println(reader.readLine());
    }   // reader.close() runs here, automatically
}

// Multiple resources — closed in REVERSE order (out first, then in)
void copy(String src, String dst) throws IOException {
    try (InputStream  in  = Files.newInputStream(Path.of(src));
         OutputStream out = Files.newOutputStream(Path.of(dst))) {
        in.transferTo(out);
    }   // out.close() then in.close()
}

// You can reference an effectively-final existing resource (Java 9+)
void useExisting(BufferedReader existing) throws IOException {
    try (existing) {
        System.out.println(existing.readLine());
    }
}
```

Any class can participate by implementing `AutoCloseable`:

```java
class Resource implements AutoCloseable {
    Resource()            { System.out.println("Acquired"); }
    @Override
    public void close()   { System.out.println("Freed"); }  // called automatically
}

void useResource() {
    try (Resource r = new Resource()) {   // "Acquired"
        if (someError) throw new RuntimeException("fail");
    }   // "Freed" — always, like a C++ destructor
}
```

> **Contrast with C++:** This *is* Java's RAII. A `try (Resource r = ...)` behaves like a C++ stack object whose destructor runs at scope exit — except the cleanup method is the explicit `close()` (declared by `AutoCloseable`) rather than a destructor, and it only fires for resources declared in the try-with-resources header, not for arbitrary locals.

---

## 13.7 Suppressed Exceptions

Try-with-resources introduces a subtlety C++ never had to solve. Suppose the `try` body throws, *and then* a resource's `close()` also throws while being auto-closed. The original (primary) exception is the one that propagates; the exception from `close()` is **suppressed** — attached to the primary via `addSuppressed()` and retrievable with `getSuppressed()`. This is strictly better than plain `finally`, which would discard the original and propagate the cleanup exception instead.

```java
class Faulty implements AutoCloseable {
    @Override
    public void close() { throw new RuntimeException("close failed"); }
}

void demo() {
    try {
        try (Faulty f = new Faulty()) {
            throw new RuntimeException("body failed");   // primary
        }   // close() throws "close failed" — gets SUPPRESSED
    }
    catch (RuntimeException e) {
        System.out.println("Primary: " + e.getMessage());          // "body failed"
        for (Throwable s : e.getSuppressed()) {
            System.out.println("Suppressed: " + s.getMessage());   // "close failed"
        }
    }
}
```

> **Contrast with C++:** In C++ this scenario is catastrophic — a destructor that throws *during* stack unwinding calls `std::terminate()` and ends the program, which is why C++ destructors must never let exceptions escape. Java's suppressed-exception model handles the same situation gracefully: both exceptions are preserved, the program continues, and you can inspect the cleanup failure after the fact.

---

## 13.8 Multi-Catch and Catch Ordering

A single `try` block can be followed by several `catch` clauses; the runtime tries them top to bottom and selects the first whose type matches. Because a handler for a base class also matches all subclasses, **order is critical**: list the most specific (most derived) types first and the most general (`Exception`, then `Throwable`) last. Putting a base handler before a derived one is a **compile error** in Java (not merely a warning as in C++) — the derived handler would be unreachable. Java 7 also added **multi-catch** (`catch (A | B e)`) to handle several unrelated types with one block.

```java
void process() {
    try {
        processData();
    }
    catch (NumberFormatException e) {           // most specific first
        System.out.println("Bad number: " + e.getMessage());
    }
    catch (IllegalArgumentException e) {        // NumberFormatException's parent
        System.out.println("Invalid input: " + e.getMessage());
    }
    catch (RuntimeException e) {                // broader still
        System.out.println("Runtime error: " + e.getMessage());
    }
    catch (Exception e) {                       // most general last
        System.out.println("Unknown error: " + e.getMessage());
    }
}

// Multi-catch: one block for several unrelated exception types (Java 7+)
import java.io.IOException;

void multi() {
    try {
        riskyIO();
    }
    catch (IOException | NumberFormatException e) {
        // 'e' is effectively final and typed as the common supertype
        System.out.println("Failed: " + e.getMessage());
    }
}
```

> **Contrast with C++:** C++ orders `catch` clauses the same way (specific first) but a misordering is at most a *warning*; in Java it is a hard compile error. C++ has no multi-catch syntax — you would repeat the body or catch a common base. Java's `A | B` multi-catch is purely syntactic sugar with no C++ equivalent.

---

## 13.9 Exception Chaining (Cause)

Often you catch a low-level exception and want to throw a higher-level one that is more meaningful to your caller — without losing the original diagnostic information. Java supports this directly: every `Throwable` can carry a **cause**, set either through a constructor that takes a `Throwable` or via `initCause()`. The full chain is printed in the stack trace as "Caused by: ..." lines and is retrievable with `getCause()`. This is the standard way to *wrap* a checked exception in an unchecked one, or to translate between abstraction layers.

```java
class ConfigException extends RuntimeException {
    ConfigException(String message, Throwable cause) {
        super(message, cause);          // pass the cause to Throwable's constructor
    }
}

void loadConfig() {
    try {
        readFromDisk();                 // throws IOException (low-level cause)
    }
    catch (IOException e) {
        // Wrap the low-level cause inside a higher-level, unchecked exception
        throw new ConfigException("Startup failed", e);
    }
}

// Inspecting the chain
try {
    loadConfig();
}
catch (ConfigException e) {
    System.out.println(e.getMessage());            // "Startup failed"
    System.out.println("Caused by: " + e.getCause().getMessage());  // "disk read error"
}
```

A printed stack trace shows the whole chain automatically:

```
ConfigException: Startup failed
    at App.loadConfig(App.java:12)
    ...
Caused by: java.io.IOException: disk read error
    at App.readFromDisk(App.java:20)
    ...
```

> **Contrast with C++:** This is the direct analogue of C++11's `std::throw_with_nested` / `std::rethrow_if_nested`, but it is built into *every* Java `Throwable` from the start (no separate `nested_exception` mix-in) and is far more commonly used. C++'s nested exceptions are a niche feature; Java's cause chain is everyday practice.

---

## 13.10 Custom Exceptions

When the standard types do not convey enough domain meaning, define your own by extending the most appropriate base class: extend `RuntimeException` for an **unchecked** custom exception (the common modern choice) or `Exception` for a **checked** one (when you want the compiler to force handling). Provide the conventional set of constructors — at minimum `(String message)` and `(String message, Throwable cause)` — so your exception integrates with chaining and tooling. You can also add domain-specific fields and accessors.

```java
// Checked custom exception — extends Exception
public class InsufficientFundsException extends Exception {
    private final double shortfall;

    public InsufficientFundsException(String message, double shortfall) {
        super(message);
        this.shortfall = shortfall;
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
        this.shortfall = 0;
    }

    public double getShortfall() { return shortfall; }   // extra domain data
}

// Unchecked custom exception — extends RuntimeException
public class ValidationException extends RuntimeException {
    public ValidationException(String message)               { super(message); }
    public ValidationException(String message, Throwable cause) { super(message, cause); }
}

// Usage
void validate(int value) {
    if (value < 0) {
        throw new ValidationException("Value must be non-negative: " + value);
    }
}
```

> **Contrast with C++:** C++ custom exceptions derive from `std::runtime_error`/`std::invalid_argument` (which take a message) and must keep copy/move non-throwing because an in-flight exception is *copied* during propagation. Java exceptions are never copied — they propagate by reference — so there is no such constraint, but you do choose deliberately between extending `Exception` (checked) and `RuntimeException` (unchecked), a decision C++ does not have.

---

## 13.11 Stack Traces

When an exception is created, the JVM captures a **stack trace**: a snapshot of the call stack at the point of construction (not the point it is thrown). It records each `StackTraceElement` — class, method, file, and line number — and is what you see printed by `printStackTrace()` or in an uncaught-exception report. You can also retrieve it programmatically with `getStackTrace()`. Stack traces are invaluable for diagnosis; the chief mistake is *swallowing* them with an empty `catch` block.

```java
void demonstrate() {
    try {
        level1();
    }
    catch (RuntimeException e) {
        e.printStackTrace();                 // full trace to System.err

        // Or inspect programmatically:
        for (StackTraceElement frame : e.getStackTrace()) {
            System.out.println(frame.getClassName() + "." + frame.getMethodName()
                               + " (" + frame.getFileName() + ":" + frame.getLineNumber() + ")");
        }
    }
}

void level1() { level2(); }
void level2() { level3(); }
void level3() { throw new RuntimeException("Error in level 3"); }

// printStackTrace output:
// java.lang.RuntimeException: Error in level 3
//     at App.level3(App.java:24)
//     at App.level2(App.java:23)
//     at App.level1(App.java:22)
//     at App.demonstrate(App.java:5)
```

This is also a clean illustration of **stack unwinding**: the throw in `level3` unwinds through `level2` and `level1` (their remaining statements never run) until `demonstrate`'s `catch` handles it.

> **Contrast with C++:** Standard C++ has no built-in, portable stack-trace facility (C++23 finally added `<stacktrace>`; before that you needed platform-specific libraries). In Java, every exception captures its trace automatically and for free — a significant debugging advantage. Capturing the trace does cost time at construction, which is why hot-path control flow should not be built on exceptions.

---

## 13.12 Best Practices

This section condenses the chapter's lessons into actionable rules.

```java
// ✅ Catch the most specific type you can actually handle
try { parse(input); }
catch (NumberFormatException e) { useDefault(); }

// ❌ Don't swallow exceptions — at minimum log the cause
// catch (Exception e) { }          // silent failure — a debugging nightmare

// ✅ Preserve the cause when wrapping (chaining)
try { readFromDisk(); }
catch (IOException e) { throw new ConfigException("load failed", e); }   // keep 'e'

// ✅ Use try-with-resources instead of manual finally for any AutoCloseable
try (var in = Files.newBufferedReader(path)) { /* ... */ }

// ✅ Throw IllegalArgumentException / IllegalStateException for bad inputs/state
if (value < 0) throw new IllegalArgumentException("value must be >= 0");

// ✅ Prefer unchecked exceptions for programming errors; checked for recoverable conditions
// ✅ Never catch Error (OutOfMemoryError, etc.) — you can't meaningfully recover
// ✅ Don't use exceptions for ordinary control flow — they are for exceptional cases

// ✅ Document thrown exceptions with @throws in Javadoc
/**
 * @throws IllegalArgumentException if value < 0
 * @throws IOException              if the file cannot be read
 */
void process(int value) throws IOException { /* ... */ }
```

Key habits: catch only what you can handle, never silently swallow, always preserve the cause when re-throwing, prefer try-with-resources over `finally`, choose checked vs unchecked deliberately, and reserve exceptions for genuinely exceptional conditions.

---

## 13.13 What Java Does *Not* Have (vs C++)

Because this chapter parallels the C++ exception chapter, it is worth naming the C++ features that have **no Java equivalent**, so the mapping is complete:

| C++ feature | Java situation |
|---|---|
| `noexcept` specifier | No equivalent. Java cannot mark a method "never throws"; the JVM does not optimize on such a promise. |
| `throw(TypeList)` dynamic spec | Replaced (and improved) by the compiler-enforced `throws` clause. |
| `catch (...)` | Use `catch (Throwable t)` (or `catch (Exception e)` to exclude `Error`). |
| RAII / destructors for cleanup | Use try-with-resources (`AutoCloseable`) or `finally`. |
| Throwing arbitrary types (`throw 42;`) | Illegal — you can only throw `Throwable` subclasses. |
| `std::exception_ptr` / transporting an exception across threads | Not needed the same way; an exception thrown in a task is captured by `Future`/`CompletableFuture` and rethrown (wrapped in `ExecutionException`) when you call `get()`. |
| Exception-safety *levels* (no-throw / strong / basic) | Same *concepts* apply to writing correct code, but there is no language machinery (`noexcept`) and no need to worry about move/copy of the exception object during unwinding. |

> **Exception safety still matters.** The C++ notions of *basic* (no leaks, invariants hold) and *strong* (commit-or-rollback) guarantees are good design goals in Java too — e.g. mutate a copy then swap references, or validate before mutating shared state. Java just achieves them with `finally`/try-with-resources and immutability rather than with `noexcept` and copy-and-swap.

---

## Summary

| Concept | Details |
|---------|---------|
| **try** | Protected code block |
| **catch** | Exception handler; order most-specific → most-general (misorder = compile error) |
| **finally** | Always runs — cleanup that must happen |
| **try-with-resources** | Auto-closes `AutoCloseable` resources (Java's RAII) |
| **throw** | Raise an exception (must extend `Throwable`) |
| **throws** | Declare checked exceptions a method may propagate |
| **Throwable** | Root: splits into `Error`, `Exception`, `RuntimeException` |
| **Checked vs unchecked** | Compiler enforces catch-or-declare for checked (`Exception`, not `RuntimeException`) |
| **Multi-catch** | `catch (A \| B e)` handles several types (Java 7+) |
| **Chaining (cause)** | `new X(msg, cause)` / `getCause()` — preserves the underlying error |
| **Suppressed** | `getSuppressed()` — close() failures during try-with-resources |
| **Stack trace** | Captured automatically; `printStackTrace()` / `getStackTrace()` |
| **vs C++** | No `noexcept`, no `catch(...)`, no RAII destructors; `throws` replaces `throw(...)` |

---

## Next Steps
- Use try/catch/finally and try-with-resources for error handling
- Decide deliberately between checked and unchecked custom exceptions
- Chain causes and inspect suppressed exceptions
- Move to [Chapter 14: File I/O](../14_file_io/README.md)
