# Chapter 6: Object-Oriented Programming Basics

Object-oriented programming organizes software around classes — user-defined types that bundle data (fields) together with the methods that operate on that data. A class is a blueprint; an object is a concrete instance created from it with `new`. The central idea is **encapsulation**: hide the internal representation behind a controlled public interface so the rest of the program depends on what an object does, not how it stores its state. Java is a thoroughly object-oriented language — almost all code lives inside classes — which makes these foundations essential.

This chapter mirrors the C++ OOP-basics chapter section by section, adapting each idea to Java 21: defining classes and access control, constructors and fields, `this`, static members, getters/setters, the object lifecycle, the `toString`/`equals`/`hashCode` trio, and nested/inner classes. Where C++ has features Java lacks (destructors, `friend`, `const` methods, unions, bit fields, `explicit`, `mutable`), we cover the nearest Java equivalent and note the difference. The chapter closes with **records** (Java 16+), Java's concise immutable data classes.

## 6.1 Classes & Objects

A class groups related data and behavior into a single type; an object is an instance of that type with its own copy of the fields. Member access is governed by **access modifiers** — `public` members form the interface usable from outside, while `private` members are hidden implementation details accessible only within the class. Choosing what to expose versus hide is the essence of encapsulation.

### Basic Class Definition

Unlike C++, a Java class has **no header/source split**, no trailing semicolon, and access modifiers are written **per-member** (not in `public:`/`private:` blocks). Conventionally fields are `private` and reached through `public` methods so the class controls every change to its own state.

```java
// Class definition — typically one public class per .java file
public class Person {
    // Public fields: accessible from outside (shown for parity; prefer private)
    public String name;
    public int age;

    // Private field: only accessible within this class
    private String ssn;   // Social Security Number

    // Member method
    public void display() {
        System.out.println(name + " is " + age + " years old");
    }

    public void setSsn(String s) { this.ssn = s; }
    public String getSsn() { return ssn; }
}

class Demo {
    public static void main(String[] args) {
        // Create object — ALWAYS with `new` (no stack objects in Java)
        Person p = new Person();
        p.name = "Alice";
        p.age = 25;
        p.display();

        p.setSsn("123-45-6789");
        // System.out.println(p.ssn);  // COMPILE ERROR: ssn is private
    }
}
```

> **C++ → Java differences:** every object is created with `new` and lives on the heap (no `Person p;` stack object). There is no separate `.h` declaration. Each member carries its own modifier; the default (no modifier) is *package-private*, described next.

### Access Modifiers

Java has **four** access levels (C++ has three). `public` is accessible everywhere; `private` only within the same class; `protected` within the class, its subclasses, **and the same package**; and the default (writing no modifier at all) is **package-private** — accessible to any class in the same package. There is no `friend` mechanism; package-private access is Java's coarser-grained substitute.

```java
public class MyClass {
    public    int publicData    = 10;   // accessible from anywhere
    protected int protectedData = 20;   // class + subclasses + same package
    int             packageData  = 25;   // (no modifier) = package-private
    private   int privateData    = 30;   // only within this class

    public    void publicMethod()    { }
    protected void protectedMethod() { }
    void          packageMethod()    { }
    private   void privateMethod()   { }
}

class Demo2 {
    void use() {
        MyClass obj = new MyClass();
        obj.publicMethod();        // OK
        obj.publicData = 50;       // OK
        // obj.privateMethod();    // ERROR (different class)
        // obj.privateData = 1;    // ERROR
    }
}
```

**C++ → Java mapping**

| C++ | Java |
|---|---|
| `public` / `private` / `protected` | Same names; `protected` *also* grants same-package access |
| (no package-level access) | Default (no modifier) = **package-private** |
| `friend` function/class | No equivalent — use package-private access for trusted collaborators |
| `struct` (public by default) vs `class` | Only `class`; no `struct`. Records (6.10) cover plain data aggregates |

## 6.2 Constructors and Fields

A **constructor** runs automatically when an object is created with `new`, putting its fields into a valid state. Unlike C++, Java has **no destructor** — cleanup of memory is the garbage collector's job (Chapter 4), and cleanup of other resources is done with `AutoCloseable`/`try`-with-resources (covered below in 6.7).

### Constructors

A constructor has the class's name and no return type. A class can have several **overloaded** constructors differing by parameter list. If you declare *no* constructor, Java supplies a no-argument default; once you declare any, you get only the ones you write. Java has no C++-style *copy constructor* concept — copying is done explicitly (e.g. a copy method, `clone`, or a constructor that takes another instance).

```java
public class Student {
    private String name;
    private int    id;
    private double gpa;

    // No-argument constructor
    public Student() {
        this.name = "Unknown";
        this.id   = 0;
        this.gpa  = 0.0;
        System.out.println("No-arg constructor called");
    }

    // Parameterized constructor
    public Student(String name, int id, double gpa) {
        this.name = name;
        this.id   = id;
        this.gpa  = gpa;
        System.out.println("Parameterized constructor called");
    }

    // "Copy" constructor — explicit; nothing automatic about it in Java
    public Student(Student other) {
        this(other.name, other.id, other.gpa);   // delegate (see 6.2 below)
    }

    public void display() {
        System.out.println(name + " (" + id + "): " + gpa);
    }
}

class Demo3 {
    public static void main(String[] args) {
        Student s1 = new Student();                  // no-arg
        Student s2 = new Student("Alice", 101, 3.8); // parameterized
        Student s3 = new Student(s2);                // explicit copy
    }
}
```

### Field Initialization (No Initializer Lists)

C++ uses a constructor *initializer list* (`: name(n), age(a)`) and worries about initialization order. Java has no initializer-list syntax — you assign fields in the constructor body. Fields also receive **default values** automatically (`0`, `0.0`, `false`, `null`) before the constructor runs, so there is no "uninitialized member" hazard. You can also use **field initializers** and **`final` fields** (which must be assigned exactly once, by an initializer or every constructor — Java's analogue of a `const` member that must be set in the init list).

```java
public class Person {
    private final String name;     // final: must be set exactly once (like a const member)
    private int age = 0;           // field initializer (runs before constructor body)

    public Person(String name, int age) {
        this.name = name;          // final field assigned here
        this.age  = age;
        System.out.println("Initialized: " + name);
    }
    // A second constructor would ALSO have to assign `name`, or it won't compile.
}
```

### Delegating Constructors

A constructor can call another constructor of the **same class** using `this(...)` as its **first statement** — Java's equivalent of C++11 delegating constructors. This keeps validation in one place. (To call a *superclass* constructor you use `super(...)`, covered in Chapter 7.)

```java
public class Config {
    private final String host;
    private final int    port;
    private final boolean secure;

    // Primary constructor — all real work and validation
    public Config(String host, int port, boolean secure) {
        if (port < 1 || port > 65535)
            throw new IllegalArgumentException("Invalid port");
        this.host = host;
        this.port = port;
        this.secure = secure;
    }

    public Config(String host, int port) { this(host, port, false); }  // delegates
    public Config(String host)           { this(host, 80); }           // chains
    public Config()                      { this("localhost"); }        // full chain

    public String info() { return host + ":" + port; }
}
// Config c1 = new Config();                       // localhost:80
// Config c3 = new Config("db.example.com", 5432, false);
```

> **Difference from C++:** the delegation call is the statement `this(...)`, not a member-init-list entry, and it must be the *first* statement in the constructor body.

### Object Cleanup — No Destructor

C++ pairs a constructor with a `~ClassName` destructor that frees resources at scope end. Java has **no destructor**. For memory, the GC reclaims unreachable objects automatically. For *other* resources (files, sockets, locks), implement `AutoCloseable` and use **`try`-with-resources**, which calls `close()` deterministically — this is Java's RAII analogue.

```java
// Java's deterministic-cleanup pattern (replaces the C++ destructor for resources)
class FileResource implements AutoCloseable {
    FileResource(String path) { System.out.println("Resource opened: " + path); }

    @Override public void close() {            // called automatically at end of try
        System.out.println("Resource closed");
    }
}

class Demo4 {
    public static void main(String[] args) {
        try (FileResource r = new FileResource("data.txt")) {
            // use the resource
        }   // r.close() runs here automatically, even on exception
    }
}
```

> `Object.finalize()` once played the destructor role but is **deprecated for removal** — never use it. Use `try`-with-resources (or `java.lang.ref.Cleaner` for advanced cases).

## 6.3 Static Members and Immutability (No `const` Methods)

Beyond per-object members, classes support members with special semantics. A `static` member belongs to the **class itself** rather than any instance — exactly like C++ `static` members, but with no separate out-of-class definition required.

### Static Members

A `static` field has exactly one copy shared by all instances (an instance counter, a shared constant). A `static` method has no `this` and is called on the class: `Counter.getCount()`. Unlike C++, you do **not** define the static field separately outside the class — you initialize it inline.

```java
public class Counter {
    private static int count = 0;   // shared by all instances (no out-of-class definition!)
    private final int id;

    public Counter() { this.id = ++count; }

    public int getId() { return id; }
    public static int getCount() { return count; }   // static: no `this`, touches only static
}

class Demo5 {
    public static void main(String[] args) {
        Counter c1 = new Counter();
        Counter c2 = new Counter();
        Counter c3 = new Counter();

        System.out.println("Total created: " + Counter.getCount()); // 3
        System.out.println("c1 ID: " + c1.getId());                 // 1
        System.out.println("c3 ID: " + c3.getId());                 // 3
    }
}
```

A `static final` field is the Java idiom for a class-wide constant: `public static final double PI = 3.14159;` (conventionally named in UPPER_SNAKE_CASE).

### No `const` Member Functions — Use Immutability Instead

C++ marks query methods `const` so they can be called on `const` objects and cannot mutate state. **Java has no `const` methods and no `const` objects.** The Java way to guarantee an object cannot change is to make the *class itself* immutable: declare fields `private final`, set them only in the constructor, and provide no setters. (This also replaces C++'s `mutable`, which Java has no keyword for — an immutable class simply has no mutable state to exempt.)

```java
// Immutable class = Java's substitute for `const`-correctness
public final class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) { this.x = x; this.y = y; }

    public int getX() { return x; }   // no `const` keyword; there are no setters,
    public int getY() { return y; }   // so the object can never change after construction

    // "Mutators" return a NEW object instead of changing this one
    public Point movedBy(int dx, int dy) { return new Point(x + dx, y + dy); }

    @Override public String toString() { return "(" + x + "," + y + ")"; }
}
```

**C++ → Java mapping**

| C++ | Java |
|---|---|
| `static int count;` + out-of-class `int C::count = 0;` | `static int count = 0;` (initialized inline) |
| `static T getCount()` called `C::getCount()` | `static T getCount()` called `C.getCount()` |
| `const` member function | No equivalent — design the class to be immutable |
| `const Point p;` (const object) | No const objects — use an immutable class |
| `mutable` field exemption | No equivalent — immutable classes have nothing to exempt |

## 6.4 The `this` Reference

Inside an instance method or constructor, `this` is a **reference** to the object the method was called on, so `this.field` refers to that object's data. (C++ calls it a *pointer*; in Java it is a reference, used with `.`.) It is mostly implicit, but it disambiguates a field from a parameter of the same name and lets a method return the current object to enable **method chaining**.

```java
public class NumberBox {
    private int value;

    public NumberBox(int value) {
        this.value = value;    // `this.value` (field) vs `value` (parameter)
    }

    // Return `this` to allow chaining (the Java analogue of returning *this)
    public NumberBox add(int n) {
        this.value += n;
        return this;
    }

    public int getValue() { return value; }
}

class Demo6 {
    public static void main(String[] args) {
        NumberBox n = new NumberBox(10);
        n.add(5).add(5);                       // chain calls
        System.out.println(n.getValue());      // 20
    }
}
```

> **Difference from C++:** C++ uses `this->member` (pointer) and returns `*this`. Java uses `this.member` (reference) and returns `this`. Java also has no user-defined `operator=`; self-assignment checks like `if (this != &other)` have no Java counterpart.

## 6.5 Getters & Setters

Getters and setters are public methods that provide controlled access to private fields. A getter returns a field (or a computed view), and a setter writes a field after **validating** the input, so the object can reject illegal states. This is the practical payoff of encapsulation: callers cannot corrupt invariants, and you can change the internal representation later without touching the public interface.

```java
public class Temperature {
    private double celsius = 0;

    public double getCelsius() { return celsius; }

    // Computed view — no stored fahrenheit field
    public double getFahrenheit() { return celsius * 9 / 5 + 32; }

    // Setter with validation
    public void setCelsius(double c) {
        if (c >= -273.15) {           // absolute zero
            this.celsius = c;
        } else {
            throw new IllegalArgumentException("Below absolute zero");
        }
    }

    public void setFahrenheit(double f) { this.celsius = (f - 32) * 5 / 9; }
}

class Demo7 {
    public static void main(String[] args) {
        Temperature t = new Temperature();
        t.setCelsius(25);
        System.out.println(t.getCelsius() + "°C");    // 25.0°C
        System.out.println(t.getFahrenheit() + "°F"); // 77.0°F
    }
}
```

The JavaBeans convention (`getX`/`setX`, `isX` for booleans) is widely used by frameworks and tooling. For pure immutable data, prefer a **record** (6.10), which generates accessors for you.

## 6.6 Encapsulation Example

This pulls the chapter together into a realistic class. `BankAccount` keeps its balance `private`, exposes only getters for reading, and routes every change through `deposit`/`withdraw`, which validate before touching the balance. Because outside code cannot modify `balance` directly, the class guarantees its own invariants — no negative deposits, no overdrafts.

```java
public class BankAccount {
    private final String accountNumber;
    private final String ownerName;
    private double balance;

    public BankAccount(String accountNumber, String ownerName, double initial) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = initial;
    }

    // Private helper (encapsulated validation)
    private boolean isValidAmount(double amount) {
        return amount > 0 && amount <= 1e9;
    }

    // Read-only getters
    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName()     { return ownerName; }
    public double getBalance()       { return balance; }

    public boolean deposit(double amount) {
        if (isValidAmount(amount)) { balance += amount; return true; }
        return false;
    }

    public boolean withdraw(double amount) {
        if (isValidAmount(amount) && amount <= balance) { balance -= amount; return true; }
        return false;
    }
}

class Demo8 {
    public static void main(String[] args) {
        BankAccount acc = new BankAccount("12345", "Alice", 1000);
        acc.deposit(500);
        System.out.println("Balance: $" + acc.getBalance());
        if (acc.withdraw(200)) System.out.println("Withdrawal successful");
    }
}
```

## 6.7 Object Lifecycle

Where a C++ object's lifetime depends on stack vs heap, **every Java object lives on the heap** and its lifetime is governed by **reachability** (Chapter 4). An object is created at `new`, lives as long as some reference can reach it, and becomes eligible for garbage collection once unreachable. There is no destructor and no deterministic "destroyed here" point for memory — but `AutoCloseable` gives deterministic cleanup for *resources*.

```java
class Logger implements AutoCloseable {
    private final String name;

    public Logger(String name) {
        this.name = name;
        System.out.println("Logger " + name + " created");
    }

    @Override public void close() {               // deterministic, not a destructor
        System.out.println("Logger " + name + " closed");
    }
}

class Demo9 {
    public static void main(String[] args) {
        Logger l1 = new Logger("Heap");           // created; lives until unreachable + GC'd

        try (Logger l2 = new Logger("Scoped")) {  // created
            // use l2
        }   // l2.close() runs here — deterministic, like a C++ destructor

        l1 = null;   // drop the reference; the object becomes collectible (timing not guaranteed)
    }
}
/* Output:
Logger Heap created
Logger Scoped created
Logger Scoped closed
*/
```

**C++ → Java mapping**

| C++ | Java |
|---|---|
| Stack object destroyed at scope end (deterministic) | Use `try`-with-resources + `AutoCloseable.close()` for deterministic cleanup |
| `delete` for heap object | None — GC reclaims when unreachable |
| Destructor `~T()` for memory | None — GC handles memory |
| Destructor `~T()` for files/locks (RAII) | `AutoCloseable.close()` via `try`-with-resources |

## 6.8 `toString`, `equals`, and `hashCode`

Every Java class implicitly extends `java.lang.Object`, which supplies `toString()`, `equals(Object)`, and `hashCode()`. Overriding them is the Java counterpart to C++'s overloaded `operator<<`, `operator==`, and providing a hash for use in containers — except Java does it via method overrides, since **Java has no operator overloading**.

- **`toString()`** — a human-readable string (the analogue of a `friend operator<<`). Used automatically in `"" + obj` and `System.out.println(obj)`.
- **`equals(Object)`** — logical equality (the analogue of `operator==`). Default is identity (`==`); override for value semantics.
- **`hashCode()`** — must be **consistent with `equals`**: equal objects must return equal hash codes, or they break `HashMap`/`HashSet`.

```java
import java.util.Objects;

public class Point {
    private final int x, y;
    public Point(int x, int y) { this.x = x; this.y = y; }

    @Override public String toString() {       // ≈ operator<< in C++
        return "Point(" + x + ", " + y + ")";
    }

    @Override public boolean equals(Object o) { // ≈ operator== in C++
        if (this == o) return true;             // same reference
        if (!(o instanceof Point p)) return false;  // pattern match (Java 16+)
        return x == p.x && y == p.y;            // compare contents
    }

    @Override public int hashCode() {           // MUST be consistent with equals
        return Objects.hash(x, y);
    }
}

class Demo10 {
    public static void main(String[] args) {
        Point a = new Point(1, 2);
        Point b = new Point(1, 2);
        System.out.println(a);            // Point(1, 2)  (toString)
        System.out.println(a == b);       // false  -- different objects (identity)
        System.out.println(a.equals(b));  // true   -- same content
    }
}
```

> **The rule:** always override `equals` and `hashCode` together. Use `java.util.Objects.hash(...)` and `Objects.equals(...)` to write them safely. Records (next section) generate all three for you.

## 6.9 Nested and Inner Classes

A class declared **inside another class** is a nested class — Java's counterpart to C++ nested classes, with one important addition: Java distinguishes **`static` nested classes** from **(non-static) inner classes**.

- A **`static` nested class** behaves like a C++ nested class: it has no link to an enclosing instance and is referenced as `Outer.Nested`.
- A **(non-static) inner class** is tied to an *instance* of the outer class and can directly access the outer object's fields — there is no C++ equivalent, since C++ nested classes never get an implicit enclosing-object link.
- A **local class** is declared inside a method; an **anonymous class** is a one-off unnamed implementation (often replaced by a lambda).

```java
public class Outer {
    private int x = 10;

    // 1. static nested class — no enclosing instance (like a C++ nested class)
    public static class Nested {
        public int square(int n) { return n * n; }   // cannot see Outer.this.x
    }

    // 2. inner class — bound to an Outer instance; sees outer fields directly
    public class Inner {
        public void show() { System.out.println(x); }  // accesses Outer's x — no reference needed
    }

    public void demo() {
        Inner in = this.new Inner();   // inner needs an enclosing instance
        in.show();                     // prints 10

        // 3. local class — visible only inside this method
        class Helper { void run() { System.out.println("running"); } }
        new Helper().run();

        // 4. anonymous class implementing an interface (one-off)
        Runnable r = new Runnable() {
            @Override public void run() { System.out.println("anon"); }
        };
        r.run();
    }
}

class Demo11 {
    public static void main(String[] args) {
        Outer.Nested n = new Outer.Nested();      // static nested: no Outer instance needed
        System.out.println(n.square(5));          // 25

        Outer outer = new Outer();
        Outer.Inner i = outer.new Inner();         // inner: needs an Outer instance
        i.show();                                  // 10
    }
}
```

**C++ → Java mapping**

| C++ | Java |
|---|---|
| Nested class (no enclosing-object link) | `static` nested class |
| (no equivalent) | non-static **inner** class (bound to an enclosing instance) |
| Local class in a function | Local class in a method (can capture *effectively final* locals) |
| Lambda / functor | Anonymous class, or (preferably) a lambda |
| `union` / anonymous union | No equivalent — use a sealed-interface hierarchy or a tagged class |
| Bit fields | No equivalent — use `int` flags with bitwise ops or an `EnumSet` |

## 6.10 Records — Concise Immutable Data Classes (Java 16+)

A C++ `struct` (or a small class with public data) is often just an aggregate of fields. Java's **record** (Java 16+) is the modern, concise way to write such an immutable data carrier. From a one-line declaration, the compiler generates a `private final` field per component, a canonical constructor, an accessor per component, and consistent `equals`, `hashCode`, and `toString`. Records are **implicitly `final`** and **immutable**.

```java
// One line replaces a whole boilerplate class:
public record PointR(int x, int y) {}

// The compiler generates:
//   - private final int x, y;
//   - PointR(int x, int y) { this.x = x; this.y = y; }
//   - int x(), int y()                 (accessors — note: no "get" prefix)
//   - equals(), hashCode(), toString()

class Demo12 {
    public static void main(String[] args) {
        PointR a = new PointR(1, 2);
        PointR b = new PointR(1, 2);

        System.out.println(a);            // PointR[x=1, y=2]   (generated toString)
        System.out.println(a.x());        // 1                  (generated accessor)
        System.out.println(a.equals(b));  // true               (value equality, generated)
        System.out.println(a == b);       // false              (still distinct objects)
    }
}
```

Records can add validation via a **compact canonical constructor**, plus extra methods and static factories:

```java
public record Temperature(double celsius) {
    // Compact canonical constructor — validate before fields are assigned
    public Temperature {
        if (celsius < -273.15)
            throw new IllegalArgumentException("Below absolute zero");
    }

    // Derived value as a method (records have no extra mutable state)
    public double fahrenheit() { return celsius * 9 / 5 + 32; }

    // Static factory
    public static Temperature fromFahrenheit(double f) {
        return new Temperature((f - 32) * 5 / 9);
    }
}
```

**When to use what:** reach for a **record** for immutable value/data types (DTOs, coordinates, results); use a regular **class** when you need mutable state, a non-trivial identity, or inheritance from a class. Records cannot extend another class (they already extend `java.lang.Record`) but can implement interfaces.

## 6.11 Best Practices

```java
// ✅ Private fields, public methods (encapsulation)
public class Good {
    private int value;
    public int getValue() { return value; }
    public void setValue(int v) { this.value = v; }
}

// ✅ Prefer immutable types: final fields, no setters, mutators return new objects
public final class Money {
    private final long cents;
    public Money(long cents) { this.cents = cents; }
    public Money plus(Money o) { return new Money(cents + o.cents); }
}

// ✅ Always override equals AND hashCode together (use java.util.Objects)
@Override public boolean equals(Object o) { /* compare contents */ return false; }
@Override public int hashCode() { return java.util.Objects.hash(/* fields */); }

// ✅ Use records for plain immutable data instead of hand-written boilerplate
public record User(String name, int age) {}

// ✅ Use try-with-resources for cleanup (Java's RAII), never finalize()
// try (var r = openResource()) { ... }  // close() runs automatically

// ✅ Validate in constructors / setters to protect invariants
// throw new IllegalArgumentException(...) on bad input

// ❌ Don't expose mutable fields directly (breaks encapsulation)
// public int value;   // anyone can corrupt it
```

## Summary

| Concept | Details |
|---------|---------|
| **Class** | Blueprint for objects; one public class per file; no header/source split |
| **Constructor** | Initializes; overloadable; `this(...)` delegates; no copy ctor by default |
| **No destructor** | GC frees memory; `AutoCloseable` + try-with-resources frees other resources |
| **Access** | `public`, `protected`, *package-private* (default), `private`; no `friend` |
| **Static** | Class-wide members; initialized inline (no out-of-class definition) |
| **No `const`/`mutable`** | Achieve immutability via `private final` fields and no setters |
| **`this`** | Reference (not pointer) to the current object; enables chaining |
| **`toString`/`equals`/`hashCode`** | Override the `Object` trio (no operator overloading); keep equals/hashCode consistent |
| **Nested/inner classes** | `static` nested (≈ C++ nested) vs inner (bound to an instance); local & anonymous |
| **Records** | Concise, immutable data classes (Java 16+); auto-generated members |

## Next Steps
- Design classes with `private` fields and a deliberate public interface
- Override `equals`/`hashCode`/`toString`; reach for records for data types
- Use `try`-with-resources instead of destructors for cleanup
- Move to [Chapter 7: Inheritance & Polymorphism](../07_inheritance_polymorphism/README.md)
