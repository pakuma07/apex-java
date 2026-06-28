# Chapter 15: Advanced Features

This chapter gathers the language features that define modern, idiomatic Java — the tools introduced across Java 8 through Java 21 that make code safer, more concise, and more expressive than the verbose Java of old. Where the C++ companion chapter showcased C++11's headline additions (`auto`, lambdas, move semantics, `constexpr`, scoped enums, and so on), this chapter showcases Java's equivalent leap forward: lambdas and functional interfaces, method references, `Optional`, rich enums, annotations, **records**, **sealed classes**, **pattern matching**, **text blocks**, `var`, the Streams API, a generics recap, the module system (JPMS), and **switch expressions**.

The unifying lesson is that modern Java has shifted from imperative, boilerplate-heavy code toward a more declarative, type-safe, and functional style — much as modern C++ moved away from C-style code. Every feature shown here targets **Java 21** (the current LTS); where a feature arrived earlier, the version is noted, and where two recent features combine (records + sealed + pattern matching) the result is something neither language had a decade ago. Many of these features have direct C++ analogues we will call out; a few (records, sealed types, exhaustive pattern matching) go beyond what standard C++ offers today.

> **C++11 feature → Java modern equivalent — at a glance**
> - C++ lambdas → Java lambdas + functional interfaces (`Function`, `Predicate`, ...)
> - C++ `auto` → Java `var` (local-variable type inference, Java 10)
> - C++ `enum class` → Java `enum` (which is far richer — can have fields and methods)
> - C++ `decltype`/type traits → no direct analogue (type erasure; use generics + bounds)
> - C++ move semantics → no analogue needed (GC + references; pass-by-reference always)
> - C++ `constexpr` → no general analogue (`static final` constants; no compile-time function eval)
> - C++ `std::optional` → Java `Optional<T>`
> - C++ namespaces → Java packages + modules (JPMS)
> - C++ template metaprogramming → not available (covered in Chapter 17's contrast)

## 15.1 Lambda Expressions and Functional Interfaces

A lambda is an anonymous function you can define inline and pass around as a value — the foundation of functional-style Java since Java 8. Syntactically it is `(params) -> body`: a parameter list, the arrow `->`, and either a single expression or a `{ ... }` block. Crucially, a Java lambda is **not** a free-floating function object; it is an instance of a **functional interface** — any interface with exactly one abstract method (a *SAM* type). The compiler infers which interface from the context. The `java.util.function` package supplies the standard ones: `Function<T,R>`, `Predicate<T>`, `Consumer<T>`, `Supplier<T>`, `BiFunction<T,U,R>`, and primitive-specialized variants. Lambdas capture *effectively final* local variables by value (you cannot reassign a captured local).

```java
import java.util.function.*;

// Basic lambda assigned to a functional interface
BinaryOperator<Integer> add = (a, b) -> a + b;
System.out.println(add.apply(5, 3));        // 8

// The core functional interfaces
Function<Integer, Integer> square = x -> x * x;        // T -> R
Predicate<Integer>         isEven = x -> x % 2 == 0;   // T -> boolean
Consumer<String>           print  = s -> System.out.println(s);  // T -> void
Supplier<String>           greet  = () -> "Hello";     // () -> T

System.out.println(square.apply(6));   // 36
System.out.println(isEven.test(4));    // true
print.accept("hi");                    // hi
System.out.println(greet.get());       // Hello

// Capturing local state — the captured variable must be effectively final
int factor = 2;
Function<Integer, Integer> multiply = x -> x * factor;
System.out.println(multiply.apply(5)); // 10
// factor = 3;   // ERROR: would make 'factor' not effectively final

// Defining your own functional interface
@FunctionalInterface
interface Transformer { int apply(int value); }

Transformer triple = v -> v * 3;
System.out.println(triple.apply(4));   // 12
```

> **Contrast with C++:** A C++ lambda has an explicit *capture list* (`[x]` by value, `[&x]` by reference, `[=]`/`[&]` capture-all) and produces a unique closure type. Java has **no capture list** — it implicitly captures effectively-final variables *by value* (there is no capture-by-reference, sidestepping C++'s dangling-reference pitfall), and the lambda's type is whatever functional interface the context demands. Java also has no `mutable` lambdas; captured values cannot be modified.

---

## 15.2 Method References

When a lambda does nothing but call an existing method, a **method reference** (`::`) expresses it more concisely and readably. There are four forms: a static method (`Integer::parseInt`), an instance method of a particular object (`System.out::println`), an instance method of an arbitrary object of a type (`String::toUpperCase` — the receiver becomes the first parameter), and a constructor (`ArrayList::new`). Each is just sugar for the equivalent lambda and resolves to the same functional interface.

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

// 1. Static method reference:  x -> Integer.parseInt(x)
Function<String, Integer> parse = Integer::parseInt;

// 2. Instance method of a specific object:  s -> System.out.println(s)
Consumer<String> printer = System.out::println;

// 3. Instance method of an arbitrary receiver:  s -> s.toUpperCase()
Function<String, String> upper = String::toUpperCase;   // receiver is the argument

// 4. Constructor reference:  () -> new ArrayList<>()
Supplier<List<String>> listMaker = ArrayList::new;

// In a stream pipeline — reads almost like prose
List<String> names = List.of("alice", "bob", "carol");
List<String> shouted = names.stream()
                            .map(String::toUpperCase)   // method ref
                            .collect(Collectors.toList());
System.out.println(shouted);   // [ALICE, BOB, CAROL]
```

> **Contrast with C++:** C++ has function pointers and `std::function`, and you can pass `&Class::method` member-function pointers, but there is no unified `::` shorthand that adapts a method into a callable of the expected signature. Java's four method-reference forms are a notational convenience with no single C++ equivalent.

---

## 15.3 Optional

`Optional<T>` is a container that holds either a value or nothing, designed to make the *absence* of a value explicit in an API's type — instead of returning `null` and inviting `NullPointerException`. Create one with `Optional.of(x)`, `Optional.ofNullable(maybeNull)`, or `Optional.empty()`. Consume it functionally with `map`, `filter`, `flatMap`, `orElse`, `orElseGet`, `orElseThrow`, and `ifPresent` — avoiding the anti-pattern of `isPresent()` + `get()`, which is just `null` checking in disguise. `Optional` is best as a *return type*; do not use it for fields or method parameters.

```java
import java.util.*;

Optional<String> found   = Optional.of("Alice");
Optional<String> missing = Optional.empty();
Optional<String> maybe   = Optional.ofNullable(lookup());  // null -> empty

// Provide a default
String name = missing.orElse("Unknown");                 // "Unknown"
String lazy = missing.orElseGet(() -> expensiveDefault());

// Transform without unwrapping
Optional<Integer> length = found.map(String::length);    // Optional[5]

// Run code only if present
found.ifPresent(n -> System.out.println("Hi " + n));     // Hi Alice

// Throw if absent
String required = found.orElseThrow(() -> new NoSuchElementException("missing"));

// A method that returns Optional instead of null
Optional<User> findUser(String id) {
    return id.equals("1") ? Optional.of(new User("Alice")) : Optional.empty();
}
```

> **Contrast with C++:** This is the direct analogue of C++17's `std::optional<T>`, but Java's is richer functionally (`map`/`flatMap`/`filter`) and is the idiomatic remedy for `null`, whereas in C++ `std::optional` competes with raw/smart pointers and sentinel values. Java's `Optional` is a heap object (it does not avoid allocation the way `std::optional` avoids it by storing the value inline).

---

## 15.4 Enums with Fields and Methods

A Java `enum` is far more than a list of named integer constants: each constant is a full-fledged *object*, and the enum type can declare fields, a constructor, and methods — even per-constant method overrides. Every enum implicitly provides `name()`, `ordinal()`, `values()`, and `valueOf(String)`, can be used in `switch`, and is a natural fit for `EnumSet`/`EnumMap`. This makes enums the idiomatic way to model a fixed set of related values that carry data and behavior.

```java
// Enum with fields, a constructor, and a method
enum Planet {
    MERCURY(3.303e+23, 2.4397e6),
    EARTH  (5.976e+24, 6.37814e6),
    JUPITER(1.9e+27,   7.1492e7);          // each constant calls the constructor

    private final double mass;             // fields
    private final double radius;

    Planet(double mass, double radius) {   // constructor (implicitly private)
        this.mass = mass;
        this.radius = radius;
    }

    double surfaceGravity() {              // method shared by all constants
        return 6.67300E-11 * mass / (radius * radius);
    }
}

System.out.println(Planet.EARTH.surfaceGravity());   // ~9.8
System.out.println(Planet.values().length);          // 3
System.out.println(Planet.valueOf("EARTH"));         // EARTH

// Per-constant behavior (constant-specific method bodies)
enum Operation {
    PLUS  { public int apply(int a, int b) { return a + b; } },
    TIMES { public int apply(int a, int b) { return a * b; } };
    public abstract int apply(int a, int b);
}
System.out.println(Operation.TIMES.apply(3, 4));     // 12
```

> **Contrast with C++:** C++'s `enum class` is *only* a scoped, type-safe set of named integer values — it cannot hold fields or methods. Java enums are full objects, so a Java enum subsumes both C++'s `enum class` *and* the pattern of "a small class with a fixed set of instances." Java enums do not implicitly convert to `int` (use `ordinal()` explicitly), matching `enum class`'s type safety.

---

## 15.5 Annotations

Annotations attach metadata to code — classes, methods, fields, parameters — that tools, frameworks, and the compiler can read. Built-in ones include `@Override` (compiler-checked), `@Deprecated`, `@SuppressWarnings`, and `@FunctionalInterface`. You can define your own with `@interface`, controlling where it applies (`@Target`) and how long it survives (`@Retention` — `SOURCE`, `CLASS`, or `RUNTIME`); `RUNTIME` annotations are readable via reflection, which is how frameworks like JUnit and Spring drive behavior from annotations.

```java
import java.lang.annotation.*;

// Built-in annotations
class Base { void greet() {} }
class Derived extends Base {
    @Override                       // compile error if it doesn't actually override
    void greet() {}

    @Deprecated                     // warns callers
    void oldApi() {}

    @SuppressWarnings("unchecked")  // silences a specific warning
    void legacy() {}
}

// Defining a custom annotation, readable at runtime via reflection
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Test {
    String name() default "";       // elements look like methods
}

class CalculatorTests {
    @Test(name = "addition")
    void testAdd() { /* ... */ }
}

// A framework reads the annotation reflectively:
for (var m : CalculatorTests.class.getDeclaredMethods()) {
    if (m.isAnnotationPresent(Test.class)) {
        System.out.println("Found test: " + m.getAnnotation(Test.class).name());
    }
}
```

> **Contrast with C++:** C++ has `[[attributes]]` (`[[nodiscard]]`, `[[deprecated]]`, `[[noreturn]]`) — standardized hints to the *compiler* only. Java annotations are far more general: they are first-class metadata readable at **runtime** via reflection, which is what enables entire frameworks (dependency injection, ORM, test runners) to be configured declaratively. C++ has no runtime-reflectable attribute equivalent.

---

## 15.6 Records

A **record** (Java 16+) is a concise declaration of an immutable *data carrier*. Writing `record Point(int x, int y) {}` generates, automatically: `private final` fields, a canonical constructor, accessor methods (`x()`, `y()`), and value-based `equals()`, `hashCode()`, and `toString()`. Records eliminate the boilerplate of a plain data class. You can add a *compact constructor* for validation, define extra methods, and implement interfaces — but a record cannot extend a class (it implicitly extends `java.lang.Record`) and its fields are always final.

```java
// One line replaces ~40 lines of boilerplate
record Point(int x, int y) {}

Point p = new Point(3, 4);
System.out.println(p.x());        // 3 (generated accessor)
System.out.println(p);            // Point[x=3, y=4]  (generated toString)
System.out.println(p.equals(new Point(3, 4)));   // true (value equality)

// Compact constructor for validation
record Range(int low, int high) {
    Range {                       // no parameter list, no field assignments needed
        if (low > high) throw new IllegalArgumentException("low > high");
    }
}

// Records can have extra methods, static factories, and implement interfaces
record Money(long cents) implements Comparable<Money> {
    static Money ofDollars(double d) { return new Money(Math.round(d * 100)); }
    String formatted() { return "$" + cents / 100.0; }
    @Override public int compareTo(Money o) { return Long.compare(cents, o.cents); }
}
System.out.println(Money.ofDollars(4.99).formatted());   // $4.99
```

> **Contrast with C++:** A record is roughly a C++ aggregate `struct` with auto-generated value semantics — but Java goes further by generating `equals`/`hashCode`/`toString` and enforcing immutability. C++ gets value equality via the C++20 *defaulted* `operator==` (`bool operator==(const Point&) const = default;`) and you write your own constructor; there is no single keyword that produces the whole immutable value type as Java's `record` does. Records also power pattern matching (15.8).

---

## 15.7 Sealed Classes

A **sealed** class or interface (Java 17+) restricts *which* classes may extend or implement it, using a `permits` clause. This lets you model a closed set of subtypes — an *algebraic sum type* — so the compiler knows the complete hierarchy. Each permitted subtype must itself be `final`, `sealed`, or `non-sealed`. The big payoff arrives with pattern-matching `switch` (15.8): switching over a sealed type can be **exhaustive**, with no `default` needed, and the compiler errors if you forget a case.

```java
// A closed hierarchy: a Shape is exactly one of these three
sealed interface Shape permits Circle, Rectangle, Triangle {}

record Circle(double radius)            implements Shape {}
record Rectangle(double w, double h)    implements Shape {}
record Triangle(double base, double h)  implements Shape {}

// Because Shape is sealed, the compiler knows the full set of subtypes:
double area(Shape s) {
    return switch (s) {                 // exhaustive — no 'default' required
        case Circle c    -> Math.PI * c.radius() * c.radius();
        case Rectangle r -> r.w() * r.h();
        case Triangle t  -> 0.5 * t.base() * t.h();
    };
}
```

> **Contrast with C++:** C++ has no sealed types; any class can be subclassed unless you mark it `final`, and there is no `permits` list nor exhaustiveness checking. To model a closed sum type, C++ programmers reach for `std::variant` (with `std::visit`). Java's `sealed` + records is the language-level equivalent of a tagged union, and the compiler-checked exhaustiveness is something `std::variant` only approximates.

---

## 15.8 Pattern Matching

**Pattern matching** lets you test an object's shape and bind its parts in one step. It comes in three escalating forms. *Pattern matching for `instanceof`* (Java 16) binds a typed variable when the test succeeds, eliminating the cast. *Pattern matching for `switch`* (Java 21) allows `case` labels that are type patterns, with optional `when` guards. *Record patterns* (Java 21) destructure a record's components directly in the pattern, nesting arbitrarily deep. Combined with sealed types, this yields concise, exhaustive, type-safe dispatch.

```java
// 1. instanceof pattern — test and bind in one step (no separate cast)
Object obj = "hello";
if (obj instanceof String s && s.length() > 3) {   // 's' is in scope here
    System.out.println(s.toUpperCase());           // HELLO
}

// 2. switch pattern with type patterns and 'when' guards
String describe(Object o) {
    return switch (o) {
        case Integer i when i < 0 -> "negative int";
        case Integer i            -> "int " + i;
        case String s             -> "string of length " + s.length();
        case null                 -> "null";        // can match null explicitly
        default                   -> "something else";
    };
}

// 3. Record patterns — destructure components, nestable
sealed interface Shape permits Circle, Rectangle {}
record Point(int x, int y) {}
record Circle(Point center, double r)            implements Shape {}
record Rectangle(Point topLeft, Point bottomRight) implements Shape {}

String location(Shape s) {
    return switch (s) {
        // destructure Circle, and nested Point, in the pattern itself
        case Circle(Point(var x, var y), var r) -> "circle at (" + x + "," + y + ") r=" + r;
        case Rectangle(Point tl, Point br)      -> "rect from " + tl + " to " + br;
    };
}
```

> **Contrast with C++:** C++ has no language-level pattern matching (a pattern-matching proposal has long been discussed but is not in the standard). The closest C++ idiom is `std::visit` over a `std::variant` with an overloaded visitor, or chains of `dynamic_cast`. Java's record patterns + sealed types + switch deliver destructuring and exhaustiveness that C++ currently lacks entirely.

---

## 15.9 Text Blocks

A **text block** (Java 15+) is a multi-line string literal delimited by triple quotes `"""`, which preserves newlines and lets you write embedded JSON, SQL, HTML, or any multi-line text without escaping every quote or concatenating with `\n`. The compiler intelligently strips incidental leading whitespace based on the least-indented line, so the block stays readable in source while producing clean output.

```java
// Without text blocks — noisy escaping and concatenation
String jsonOld = "{\n" +
                 "  \"name\": \"Alice\",\n" +
                 "  \"age\": 25\n" +
                 "}";

// With a text block — what you see is what you get
String json = """
        {
          "name": "Alice",
          "age": 25
        }
        """;                    // closing delimiter sets the indentation baseline

// Handy for SQL / HTML
String sql = """
        SELECT id, name
        FROM   users
        WHERE  active = true
        """;
```

> **Contrast with C++:** The closest C++ feature is the *raw string literal* `R"(...)"`, which similarly avoids escaping. But C++ raw strings do **not** strip incidental indentation — every space inside the delimiters is literal — so Java text blocks are friendlier for indented, embedded content.

---

## 15.10 Local Variable Type Inference: var

`var` (Java 10) lets the compiler infer the type of a *local variable* from its initializer, reducing redundancy with verbose generic types. It is purely a local convenience — `var` cannot be used for fields, method parameters, or return types, and the variable must be initialized at declaration. The variable is still **statically and strongly typed**; `var` is not a dynamic `Object`.

```java
var x = 10;                              // int
var name = "Alice";                      // String
var list = new ArrayList<String>();      // ArrayList<String> — no repetition
var map = new HashMap<String, List<Integer>>();   // verbose type stated once

// Especially nice in loops
for (var entry : map.entrySet()) {       // Map.Entry<String, List<Integer>>
    System.out.println(entry.getKey());
}

// var must be initialized and cannot be a field/parameter:
// var y;                  // ERROR — no initializer to infer from
// void f(var p) {}        // ERROR — not allowed for parameters
```

> **Contrast with C++:** `var` is Java's analogue of C++ `auto`, with the same spirit — let the compiler do type bookkeeping for verbose types. The differences: C++ `auto` works for many more positions (return types via `auto`, parameters in generic lambdas, etc.) and strips `const`/references, whereas Java `var` is restricted to *local variables only* and there is no `const`/reference stripping to worry about (Java has neither concept the same way).

---

## 15.11 Streams Recap

The **Streams API** (Java 8) processes sequences of elements through a declarative pipeline of operations, replacing many explicit loops. A pipeline has a *source* (a collection, array, or generator), zero or more *intermediate* operations (`filter`, `map`, `sorted`, `distinct` — all lazy, returning a new stream), and one *terminal* operation (`collect`, `forEach`, `reduce`, `count`, `findFirst`) that triggers execution. Streams pair naturally with lambdas and method references, and `parallelStream()` can parallelize a pipeline across cores.

```java
import java.util.*;
import java.util.stream.*;

List<String> names = List.of("Alice", "Bob", "Carol", "Dave");

// Filter, transform, collect
List<String> result = names.stream()
    .filter(n -> n.length() > 3)        // intermediate (lazy)
    .map(String::toUpperCase)           // intermediate (lazy)
    .sorted()                           // intermediate (lazy)
    .collect(Collectors.toList());      // terminal (runs the pipeline)
System.out.println(result);             // [ALICE, CAROL]

// Reduce to a single value
int total = Stream.of(1, 2, 3, 4).reduce(0, Integer::sum);   // 10

// Numeric streams and statistics
double avg = IntStream.rangeClosed(1, 100).average().orElse(0);  // 50.5

// Grouping
Map<Integer, List<String>> byLength =
    names.stream().collect(Collectors.groupingBy(String::length));
```

> **Contrast with C++:** Streams are roughly C++20 *ranges* (`std::ranges` views with `|` pipe composition) plus the STL algorithms (`std::transform`, `std::accumulate`). Pre-C++20, the closest equivalent was chaining `<algorithm>` calls explicitly. Java streams are single-use (a stream is consumed by its terminal op), lazy in their intermediate stages, and offer easy parallelism via `parallelStream()`.

---

## 15.12 Generics Recap

Generics (covered in depth in Chapter 9) parameterize types and methods over type variables, giving compile-time type safety without casts. The advanced points worth recapping here are **bounded type parameters** (`<T extends Comparable<T>>`) and **wildcards** with the *PECS* rule — *Producer Extends, Consumer Super*: use `? extends T` for a source you read from and `? super T` for a sink you write to. Remember Java generics use **type erasure**: the type arguments exist only at compile time, so there are no `new T[]`, no `T.class`, and no specialization (the recurring contrast with C++ templates).

```java
import java.util.*;

// Bounded type parameter
static <T extends Comparable<T>> T max(List<T> list) {
    T best = list.get(0);
    for (T x : list) if (x.compareTo(best) > 0) best = x;
    return best;
}

// PECS: 'extends' for a producer you read from
double sumOf(List<? extends Number> nums) {     // accepts List<Integer>, List<Double>, ...
    double s = 0;
    for (Number n : nums) s += n.doubleValue();
    return s;
}

// PECS: 'super' for a consumer you write into
void addNumbers(List<? super Integer> sink) {   // accepts List<Integer>, List<Number>, List<Object>
    sink.add(1);
    sink.add(2);
}
```

> **Contrast with C++:** C++ templates are *monomorphized* — separate code is generated per type, any operation that compiles is allowed, and there is full/partial specialization and metaprogramming. Java erases types to a single compiled class, restricts arguments to reference types (no `int`, use `Integer`), and uses *bounds* and *wildcards* (with no C++ counterpart) to constrain and vary generics. See Chapter 9 for the full treatment.

---

## 15.13 Switch Expressions

Java's `switch` was modernized (a *statement* since forever, an *expression* since Java 14) with arrow labels and the ability to **yield a value**. Arrow form (`case X -> ...`) has no fall-through (no `break` needed) and a single label can list several constants (`case A, B ->`). As an expression, `switch` returns a value directly, must be exhaustive (often enforced via sealed types or enums), and uses `yield` to return from a multi-statement block.

```java
// Old statement form — fall-through, break, mutation
int numLettersOld;
switch (day) {
    case MONDAY: case FRIDAY: case SUNDAY: numLettersOld = 6; break;
    case TUESDAY:                          numLettersOld = 7; break;
    default:                               numLettersOld = 0;
}

// Modern switch EXPRESSION — arrow labels, no fall-through, returns a value
enum Day { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }

int numLetters = switch (day) {
    case MONDAY, FRIDAY, SUNDAY -> 6;        // multiple labels, no break
    case TUESDAY                -> 7;
    case THURSDAY, SATURDAY     -> 8;
    case WEDNESDAY              -> 9;
};                                            // exhaustive over the enum

// yield from a block when you need multiple statements
int score = switch (grade) {
    case "A" -> 4;
    case "B" -> 3;
    default  -> {
        System.out.println("unknown grade");
        yield 0;                              // 'yield' produces the block's value
    }
};
```

> **Contrast with C++:** C++ `switch` remains a statement with C-style fall-through (you must write `break`), works only on integral/enum values, and cannot itself produce a value. Java's switch expression with arrow syntax, value-yielding, multi-label cases, and (with patterns, see 15.8) type matching is considerably more powerful than C++'s `switch`.

---

## 15.14 Modules (JPMS Intro)

The **Java Platform Module System** (JPMS, Java 9) adds a layer above packages for organizing large applications: a *module* is a named, self-describing group of packages declared in a `module-info.java` file. It states what the module `requires` (its dependencies) and `exports` (the packages it makes public); everything not exported is *strongly encapsulated* and inaccessible from outside, even via reflection. Modules give reliable configuration (missing dependencies are detected at startup) and a smaller footprint (with `jlink` you can build a runtime image containing only the modules you use).

```java
// module-info.java — placed at the root of the module's source
module com.example.app {
    requires java.sql;                  // depend on another module
    requires transitive com.example.util;   // re-export this dependency to my consumers

    exports com.example.app.api;        // make this package public
    exports com.example.app.spi to com.example.plugin;   // qualified export

    uses   com.example.app.Service;     // consume a service (ServiceLoader)
    provides com.example.app.Service with com.example.app.impl.DefaultService;
}
```

> **Contrast with C++:** C++ historically organized code with namespaces (Chapter 15.2 of the C++ book) and headers; C++20 introduced its *own* module system (`export module foo;`, `import foo;`) to replace textual `#include`. Both languages now have real modules, but they solve different specific problems: Java's JPMS is about *strong encapsulation and reliable dependency configuration* at the package level, while C++20 modules primarily target *compilation speed and avoiding preprocessor textual inclusion*. Java packages (not modules) are the everyday analogue of C++ namespaces.

---

## 15.15 Best Practices Summary

This section distills modern Java style into a checklist.

```java
// ✅ Use var for obvious local types — but keep names meaningful
var users = new ArrayList<User>();

// ✅ Prefer records for immutable data carriers
record Coordinate(double lat, double lon) {}

// ✅ Model closed hierarchies with sealed + records + exhaustive switch
sealed interface Event permits Click, Scroll {}

// ✅ Use Optional for "maybe absent" return values — never for fields/params
Optional<User> findById(String id) { /* ... */ return Optional.empty(); }

// ✅ Express transformations with streams + method references
names.stream().map(String::trim).filter(s -> !s.isBlank()).toList();

// ✅ Use switch expressions (arrow form) — no fall-through bugs
int code = switch (status) { case OK -> 200; case ERROR -> 500; };

// ✅ Use text blocks for embedded JSON/SQL/HTML
String q = """
    SELECT * FROM t WHERE id = ?
    """;

// ✅ Use enums (with fields/methods) for fixed sets of typed constants
// ✅ Annotate overrides with @Override; define runtime annotations for frameworks
// ✅ Organize large apps with packages and (optionally) modules
```

The recurring themes: let the compiler infer types (`var`), model data with records and closed hierarchies with sealed types, prefer immutability and `Optional` over `null`, express logic declaratively with streams and switch expressions, and use the right scoping mechanism (packages/modules) to manage large codebases.

---

## Summary

| Feature | Since | Benefit |
|---------|-------|---------|
| **Lambdas** | Java 8 | Inline anonymous functions (functional interfaces) |
| **Method references** | Java 8 | `::` shorthand for calling an existing method |
| **Optional** | Java 8 | Explicit "maybe absent" — the `null` antidote |
| **Streams** | Java 8 | Declarative, lazy data pipelines |
| **Rich enums** | Java 5 | Constants with fields, methods, behavior |
| **Annotations** | Java 5 | Runtime-readable metadata that drives frameworks |
| **`var`** | Java 10 | Local-variable type inference (≈ C++ `auto`) |
| **Text blocks** | Java 15 | Multi-line string literals |
| **Records** | Java 16 | Concise immutable data carriers |
| **Sealed classes** | Java 17 | Closed hierarchies (algebraic sum types) |
| **Switch expressions** | Java 14 | Value-yielding, no fall-through |
| **Pattern matching** | Java 16–21 | `instanceof`, `switch`, record destructuring |
| **Modules (JPMS)** | Java 9 | Strong encapsulation + reliable dependencies |

---

## Conclusion

You've now toured the features that define modern, idiomatic Java 21. Key takeaways:

1. **Object-Oriented + Functional**: classes and inheritance, plus lambdas, streams, and method references
2. **Data Modeling**: records for immutable values, sealed types for closed hierarchies
3. **Safety**: `Optional` over `null`, exhaustive pattern matching, strong static typing
4. **Expressiveness**: switch expressions, text blocks, `var`, pattern matching
5. **Scale**: packages and the module system for large codebases

Continue with:
- Concurrency (virtual threads, structured concurrency) — Chapter 16
- The Collections framework and Streams in depth
- Design patterns adapted to records, sealed types, and lambdas
- Framework study (Spring, Jakarta EE)

---

## Happy Coding!

You now have the foundation to write professional, modern, idiomatic Java code!
