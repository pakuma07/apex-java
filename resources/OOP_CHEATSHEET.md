# Java 21 OOP & Advanced Features Cheat Sheet

> Adapted from the C++11 OOP cheat sheet. Reworked for modern Java (Java 21). C++-specific topics (operator overloading, smart pointers, templates) are mapped to their Java equivalents (equality contracts, references/GC, generics).

## Table of Contents
1. [Classes & Objects](#classes--objects)
2. [Inheritance](#inheritance)
3. [Polymorphism](#polymorphism)
4. [Interfaces](#interfaces)
5. [Operators & Equality](#operators--equality)
6. [Generics](#generics)
7. [Records & Sealed Types](#records--sealed-types)
8. [Modern Java Features](#modern-java-features)
9. [Exception Handling](#exception-handling)
10. [Resource Management](#resource-management)

---

## Classes & Objects

### Basic Class Structure
```java
public class MyClass {
    private int data;                 // private field
    private void privateMethod() { }  // private method

    protected void protectedMethod() { }  // accessible to subclasses & package

    public MyClass() { }              // constructor (no destructor in Java)

    public int getData() { return data; }     // accessor
    public void setData(int d) { data = d; }  // mutator

    public static int staticVar = 0;          // static field (class-level)
    public static void staticMethod() { }     // static method
}
```

### Constructor Types
```java
class MyClass {
    private int data1, data2;

    // Default (no-arg) constructor
    MyClass() { }

    // Parameterized constructor
    MyClass(int x) { this.data1 = x; }

    // Constructor delegation - call another constructor with this(...)
    MyClass() { this(0); }                 // (illustrative; one no-arg only)

    // Multi-field
    MyClass(int x, int y) { this.data1 = x; this.data2 = y; }
}
```

> Java has no copy constructor or move constructor. Copies are explicit (e.g. a copy factory or `clone()`), and the GC handles lifetime—there is no destructor.

### Cleanup (No Destructor)
```java
// Java reclaims memory via garbage collection.
// For external resources, implement AutoCloseable and use try-with-resources.
class Resource implements AutoCloseable {
    public Resource() { /* acquire */ }

    @Override
    public void close() { /* release - called by try-with-resources */ }
}
```

### Access Modifiers
```java
public class Example {
    public int a;       // anyone
    protected int b;    // subclasses + same package
    int c;              // package-private (default, no keyword)
    private int d;      // only this class
}
```

### Static Members
```java
class Counter {
    private static int count = 0;       // initialized inline

    Counter() { count++; }
    static int getCount() { return count; }
}

// Usage
System.out.println(Counter.getCount());  // access via class name
```

### The this Reference
```java
class Number {
    private int value;

    Number add(Number other) {
        this.value += other.value;       // explicit this
        return this;                     // return self for chaining
    }
}
```

---

## Inheritance

### Single Inheritance
```java
class Animal {                            // base class
    void eat() { System.out.println("Eating"); }
}

class Dog extends Animal {                // derived class
    @Override
    void eat() { System.out.println("Dog eating"); }   // override
    void bark() { System.out.println("Woof!"); }
}
```

> Java supports only **single class inheritance**. There is no public/protected/private inheritance—`extends` is always equivalent to C++ public inheritance. Multiple inheritance of behavior is achieved with interfaces.

### Constructor in Subclass
```java
class Base {
    Base(int x) { }
}

class Derived extends Base {
    Derived(int x, int y) {
        super(x);                         // call base constructor (must be first)
        // initialize derived-specific fields
    }
}
```

### Multiple Inheritance via Interfaces
```java
interface A { default void a() { } }
interface B { default void b() { } }
class C implements A, B { }               // inherits behavior from both
```

### final to Prevent Inheritance
```java
final class CannotExtend { }              // no subclass allowed

class Base {
    final void cannotOverride() { }       // no override allowed
}
```

---

## Polymorphism

### Overriding (Virtual by Default)
```java
abstract class Shape {
    void draw() { }                        // overridable
    abstract double area();                // abstract = pure virtual
}

class Circle extends Shape {
    private double r;
    Circle(double r) { this.r = r; }

    @Override void draw() { System.out.println("Drawing circle"); }
    @Override double area() { return Math.PI * r * r; }
}

// Usage - dynamic dispatch
Shape shape = new Circle(2.0);
shape.draw();                              // calls Circle.draw()
```

> All non-`static`, non-`private`, non-`final` methods in Java are virtual (dynamically dispatched). There is no `virtual` keyword. Use `@Override` to catch mistakes at compile time.

### Abstract Class
```java
abstract class AbstractBase {
    abstract void mustImplement();         // no body
    void shared() { }                      // can have concrete methods
}

// AbstractBase obj = new AbstractBase();  // ERROR: cannot instantiate

class Concrete extends AbstractBase {
    @Override void mustImplement() { }     // must implement
}
```

---

## Interfaces

### Interface Definition
```java
interface Drawable {
    void draw();                           // implicitly public abstract

    default void describe() {              // default method (Java 8+)
        System.out.println("A drawable");
    }

    static Drawable empty() {              // static method
        return () -> { };
    }

    int MAX = 100;                         // implicitly public static final
}

class Square implements Drawable {
    @Override public void draw() { }
}
```

### Functional Interface (single abstract method)
```java
@FunctionalInterface
interface Transformer {
    int apply(int x);
}

Transformer doubler = x -> x * 2;          // implemented with a lambda
int r = doubler.apply(5);                  // 10
```

---

## Operators & Equality

> Java does **not** support operator overloading (except the built-in `+` for `String`). Instead, you provide named methods and implement the standard contracts.

### equals() and hashCode()
```java
class Vector {
    private final int x, y;
    Vector(int x, int y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector v)) return false;   // pattern matching
        return x == v.x && y == v.y;
    }

    @Override
    public int hashCode() { return Objects.hash(x, y); }

    @Override
    public String toString() { return "(" + x + "," + y + ")"; }

    // Arithmetic as named methods (no operator+ in Java)
    Vector add(Vector o) { return new Vector(x + o.x, y + o.y); }
    Vector scale(int k)  { return new Vector(x * k, y * k); }
}
```

### Ordering: Comparable & Comparator
```java
class Person implements Comparable<Person> {
    String name; int age;

    @Override
    public int compareTo(Person other) {              // natural ordering
        return Integer.compare(this.age, other.age);
    }
}

// External ordering with Comparator
Comparator<Person> byName = Comparator.comparing(p -> p.name);
Comparator<Person> byAgeThenName =
    Comparator.comparingInt((Person p) -> p.age).thenComparing(p -> p.name);

list.sort(byAgeThenName);
```

### String Representation (analogue of operator<<)
```java
@Override
public String toString() { return "Point[x=" + x + ", y=" + y + "]"; }

System.out.println(point);   // implicitly calls toString()
```

---

## Generics

> The Java analogue of C++ templates. Generics are checked at compile time and **erased** at runtime (no specialization, no value parameters, no template metaprogramming).

### Generic Methods
```java
static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}

max(5, 3);              // Integer
max(3.5, 2.1);          // Double
max("apple", "banana"); // String
```

### Generic Classes
```java
class Stack<T> {
    private final List<T> elements = new ArrayList<>();
    void push(T value) { elements.add(value); }
    T pop() { return elements.remove(elements.size() - 1); }
    boolean isEmpty() { return elements.isEmpty(); }
}

Stack<Integer> intStack = new Stack<>();
Stack<String> stringStack = new Stack<>();
```

### Bounded Types & Wildcards
```java
// Upper bound
<T extends Number> double sum(List<T> nums) { /* ... */ }

// Wildcards (PECS: Producer Extends, Consumer Super)
void copy(List<? extends Number> src, List<? super Number> dst) {
    for (Number n : src) dst.add(n);
}
```

### Type Erasure (no specialization)
```java
// At runtime List<String> and List<Integer> are both just List.
// You cannot do: new T[10], T.class, or instanceof List<String>.
// Pass a Class<T> token if you need the type at runtime.
static <T> T newInstance(Class<T> type) throws Exception {
    return type.getDeclaredConstructor().newInstance();
}
```

---

## Records & Sealed Types

### Records (transparent immutable data carriers)
```java
public record Point(int x, int y) { }
// Auto-generates: constructor, accessors x()/y(), equals, hashCode, toString.

Point p = new Point(3, 4);
System.out.println(p.x());          // 3
System.out.println(p);              // Point[x=3, y=4]

// Compact constructor for validation
public record Range(int lo, int hi) {
    public Range {
        if (lo > hi) throw new IllegalArgumentException("lo > hi");
    }
}
```

### Sealed Classes & Interfaces (Java 17+)
```java
public sealed interface Shape permits Circle, Square, Triangle { }
public record Circle(double r) implements Shape { }
public record Square(double side) implements Shape { }
public record Triangle(double base, double height) implements Shape { }

// Exhaustive switch over a sealed type - no default needed
static double area(Shape s) {
    return switch (s) {
        case Circle c   -> Math.PI * c.r() * c.r();
        case Square sq  -> sq.side() * sq.side();
        case Triangle t -> 0.5 * t.base() * t.height();
    };
}
```

---

## Modern Java Features

### var Type Inference
```java
var x = 10;                     // int
var y = 3.14;                   // double
var name = "hello";             // String
var list = new ArrayList<String>();  // ArrayList<String>
// var is for LOCAL variables only; type must be inferable.
```

### Lambda Expressions
```java
Function<Integer, Integer> square = x -> x * x;

int factor = 3;
Function<Integer, Integer> multiply = x -> x * factor;   // captures factor (effectively final)

Runnable r = () -> System.out.println("run");

BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
```

### Enhanced for Loop
```java
List<Integer> v = List.of(1, 2, 3, 4, 5);

for (int x : v) System.out.print(x + " ");          // iterate elements

for (var entry : map.entrySet()) {                  // iterate a map
    System.out.println(entry.getKey() + ": " + entry.getValue());
}
```

### Optional (null safety)
```java
Optional<String> maybe = Optional.ofNullable(lookup());
maybe.ifPresent(System.out::println);
String value = maybe.orElse("default");
```

### Pattern Matching for switch (Java 21)
```java
static String describe(Object o) {
    return switch (o) {
        case Integer i when i > 0 -> "positive int";
        case Integer i           -> "non-positive int";
        case String s            -> "string of length " + s.length();
        case null                -> "null";
        default                  -> "something else";
    };
}
```

### Text Blocks
```java
String json = """
    {
      "name": "Java",
      "version": 21
    }
    """;
```

---

## Exception Handling

### Try-Catch-Finally
```java
try {
    if (error) throw new RuntimeException("Something failed");
} catch (IllegalArgumentException e) {
    System.out.println(e.getMessage());
} catch (RuntimeException e) {
    System.out.println("General error: " + e.getMessage());
} finally {
    // always runs
}
```

### Common Exceptions
```java
IllegalArgumentException        // invalid parameter
IndexOutOfBoundsException       // bad index
NullPointerException            // null dereference
IllegalStateException           // wrong object state
IOException                     // checked I/O failure
```

### Custom Exception
```java
class CustomException extends RuntimeException {
    public CustomException(String msg) { super(msg); }
    public CustomException(String msg, Throwable cause) { super(msg, cause); }
}

try {
    throw new CustomException("Custom error message");
} catch (CustomException e) {
    System.out.println(e.getMessage());
}
```

### Checked vs Unchecked
```java
void readFile() throws IOException { }     // checked: caller must handle/declare
void compute() { throw new IllegalStateException(); }  // unchecked: optional
```

### Multi-Catch
```java
try { risky(); }
catch (IOException | SQLException e) { handle(e); }
```

---

## Resource Management

### try-with-resources (Java's RAII)
```java
// Any AutoCloseable is closed automatically, in reverse order.
try (var in = Files.newBufferedReader(path);
     var out = Files.newBufferedWriter(other)) {
    out.write(in.readLine());
}   // out.close() then in.close(), even if an exception is thrown
```

### Custom Closeable Resource
```java
class FileHandle implements AutoCloseable {
    FileHandle(String name) { /* open */ }
    @Override public void close() { /* close - guaranteed */ }
}

try (var f = new FileHandle("data.txt")) {
    // use file
}   // automatically closed
```

---

## Quick Comparison: C++11 → Java 21

| Feature | C++11 | Java 21 |
|---------|-------|---------|
| Type deduction | `auto` | `var` (locals only) |
| Null | `nullptr` | `null` / `Optional` |
| For-each | `for (int x : v)` | `for (int x : v)` |
| Resource cleanup | RAII destructor | `AutoCloseable` + try-with-resources |
| Lambdas | `[capture](args){}` | `(args) -> body` |
| Constants | `const`/`constexpr` | `final` / `static final` |
| Generics | templates (instantiated) | generics (erased) |
| Operator overload | yes | no — use `equals`/`compareTo`/named methods |
| Smart pointers | `unique_ptr`/`shared_ptr` | references + garbage collection |
| Data class | struct + boilerplate | `record` |

---

## Best Practices Summary

1. **Make fields `private`**; expose behavior, not state
2. **Prefer immutability** (`final` fields, `record`s)
3. **Override `equals` and `hashCode` together**, or use a record
4. **Use `Optional`** for "may be absent" return values, never `null`
5. **Program to interfaces** (`List`, `Map`), not implementations
6. **Use `var`** for obvious local types
7. **Annotate overrides with `@Override`**
8. **`try-with-resources`** for any `AutoCloseable`
9. **Favor composition over inheritance**; keep hierarchies shallow
10. **Use the Collections Framework and Streams** over manual arrays/loops
