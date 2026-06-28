# Chapter 7: Inheritance & Polymorphism

This chapter covers the two pillars of object-oriented design in Java: inheritance, which lets one class reuse and extend another, and polymorphism, which lets code call the right behavior for an object through a common supertype. You will see how method overriding enables runtime (dynamic) dispatch, how abstract classes and interfaces define contracts that subtypes must implement, and how Java deliberately avoids the complications of multiple class inheritance by using interfaces instead.

A consistent theme is safety and intent. Unlike C++, Java makes every (non-`static`, non-`private`, non-`final`) method *virtual by default*, so dynamic dispatch is the norm rather than something you opt into with a keyword. Java has no destructors and no manual `delete`, so memory is managed by the garbage collector — the C++ rule about "virtual destructors" simply does not apply. Java also has no object slicing, because variables of class type are always *references*, never values. The later sections dig into Java-specific tools — `@Override`, `final`, `sealed` classes, pattern matching for `instanceof`, and the universal `Object` superclass — and the guiding principle throughout is to prefer composition over deep inheritance hierarchies.

> **C++ vs Java at a glance**
> - C++ methods are non-virtual by default; you write `virtual`. Java methods are virtual by default; you opt *out* with `final`, `static`, or `private`.
> - C++ has `public`/`protected`/`private` *inheritance*. Java has only one form of class inheritance (always with `extends`), roughly equivalent to C++ `public` inheritance.
> - C++ supports multiple inheritance of classes. Java forbids it; a class `extends` exactly one superclass but may `implements` many interfaces.
> - C++ uses `virtual` destructors for safe polymorphic cleanup. Java uses garbage collection; there are no destructors.

## 7.1 Inheritance Basics

Inheritance models an "is-a" relationship: a subclass automatically gains the members of its superclass and can add its own. The superclass holds the shared state and behavior (here `name`, `age`, `eat()`, `sleep()`), and the subclass extends it (`Dog` adds `bark()`). The subclass constructor must initialize the superclass part by calling the superclass constructor with `super(...)` — and that call must be the **first statement** in the constructor (if you omit it, the compiler inserts an implicit `super()` to the no-arg superclass constructor). Members marked `protected` are accessible to subclasses (and to the same package), which is how a superclass shares internals with its subclasses while keeping them hidden from unrelated code.

```java
// Base class
class Animal {
    protected String name;
    protected int age;

    public Animal(String n, int a) {
        this.name = n;
        this.age = a;
    }

    public void eat()   { System.out.println(name + " is eating"); }
    public void sleep() { System.out.println(name + " is sleeping"); }
}

// Derived class (inherits from Animal)
class Dog extends Animal {
    public Dog(String n, int a) {
        super(n, a);            // must be the first statement
    }

    public void bark() { System.out.println(name + " says: Woof!"); }
}

public class Main {
    public static void main(String[] args) {
        Dog d = new Dog("Rex", 5);
        d.eat();   // From Animal
        d.bark();  // From Dog
    }
}
```

### Inheritance "Types"

In C++, the access specifier before the base class (`public`, `protected`, `private`) controls how the base's members are exposed through the derived class. **Java has no such concept** — there is exactly one kind of class inheritance, written `extends`, and it behaves like C++ `public` inheritance: a `public` member of the superclass stays `public`, a `protected` member stays `protected`. The "implemented-in-terms-of" relationships that C++ expresses with `protected`/`private` inheritance are achieved in Java the way most experts recommend even in C++: by **composition** (holding the other object as a private field).

```java
// Java: only one form of inheritance — like C++ public inheritance.
class Derived extends Base { }
// public members stay public, protected stay protected.

// There is no `extends protected Base` or `extends private Base`.
// For "implemented-in-terms-of", use composition instead:
class Stack {
    private java.util.ArrayList<Integer> store = new java.util.ArrayList<>();
    public void push(int v) { store.add(v); }
    public int  pop()       { return store.remove(store.size() - 1); }
    // Stack is implemented in terms of ArrayList, but is NOT an ArrayList.
}
```

> **Note:** Java classes implicitly extend `java.lang.Object` if no `extends` clause is given, so every class ultimately has `Object` as an ancestor (see 7.12).

---

## 7.2 Method Overriding & Polymorphism

In Java, instance methods are **virtual by default**: when you call a method through a superclass-typed reference, Java dispatches to the override matching the object's *actual* runtime type, not the declared (static) type of the reference. This is what lets a single loop over `Animal` references call `Dog.sound()` for dogs and `Cat.sound()` for cats. There is **no `virtual` keyword** — and none is needed. Instead, you annotate the overriding method with `@Override`, which asks the compiler to verify it really overrides a supertype method, catching subtle signature typos. Because Java is garbage-collected, there are **no destructors**, so the C++ rule "always declare a virtual destructor in a polymorphic base" has no Java counterpart.

```java
class Animal {
    // Virtual by default — no keyword required
    public void sound() {
        System.out.println("Generic animal sound");
    }
}

class Dog extends Animal {
    @Override                 // verified by the compiler
    public void sound() {
        System.out.println("Woof!");
    }
}

class Cat extends Animal {
    @Override
    public void sound() {
        System.out.println("Meow!");
    }
}

public class Main {
    public static void main(String[] args) {
        // Polymorphism in action
        Animal[] animals = {
            new Dog(),
            new Cat(),
            new Animal()
        };

        for (Animal a : animals) {
            a.sound();   // Calls the appropriate override
        }
        // Output:
        // Woof!
        // Meow!
        // Generic animal sound

        // No delete needed — the garbage collector reclaims objects.
    }
}
```

### Dynamic Dispatch Mechanism

Under the hood, dynamic dispatch in the JVM is implemented much like a C++ vtable: each class has a method table, and each object header carries a pointer to its class's metadata. A virtual call resolves the method through that table at runtime, so the correct override is chosen based on the real object type — a small, well-optimized cost (the JIT compiler often *devirtualizes* and inlines such calls). An **abstract method** (declared with the `abstract` keyword and no body) is Java's equivalent of a C++ *pure virtual* function: it makes its class `abstract` and forces every concrete subclass to supply an override. This is the standard way to define a contract that subclasses must implement.

```java
// When you call an overridable method:
// 1. The JVM checks the object's actual runtime class
// 2. Finds the matching implementation in that class's method table
// 3. Invokes it

abstract class Shape {
    public abstract void draw();   // abstract method = C++ pure virtual (= 0)
}

class Circle extends Shape {
    @Override public void draw() { System.out.println("Drawing circle"); }
}

class Square extends Shape {
    @Override public void draw() { System.out.println("Drawing square"); }
}

static void renderShape(Shape shape) {
    shape.draw();   // Calls the correct override
}
```

> **Hiding vs overriding:** `static` methods are *not* overridden — they are *hidden*. A `static` method call is resolved by the reference's static type, not the object's runtime type. Only instance methods participate in dynamic dispatch. Fields are also resolved statically (never polymorphic), which is another reason to keep fields `private` and expose behavior through methods.

---

## 7.3 Abstract Classes & Interfaces

An **abstract class** has the `abstract` modifier and may contain abstract methods (with no body); it cannot be instantiated directly — you can only create objects of concrete subclasses that override every abstract method. This lets you define a contract: `Shape` declares that every shape must provide `area()` and `perimeter()` without saying how, and `Rectangle` and `Circle` fill in the details. An abstract class may also hold state (fields) and provide concrete (already-implemented) methods that subclasses inherit.

```java
// Abstract base class (cannot instantiate)
abstract class Shape {
    public abstract double area();        // abstract = C++ pure virtual
    public abstract double perimeter();

    // Abstract classes may also provide concrete behavior:
    public void describe() {
        System.out.printf("area=%.2f, perimeter=%.2f%n", area(), perimeter());
    }
}

class Rectangle extends Shape {
    private final double width, height;
    public Rectangle(double w, double h) { this.width = w; this.height = h; }

    @Override public double area()      { return width * height; }
    @Override public double perimeter() { return 2 * (width + height); }
}

class Circle extends Shape {
    private final double radius;
    public Circle(double r) { this.radius = r; }

    @Override public double area()      { return Math.PI * radius * radius; }
    @Override public double perimeter() { return 2 * Math.PI * radius; }
}

public class Main {
    public static void main(String[] args) {
        // Shape s = new Shape();   // ERROR: Shape is abstract

        Rectangle r = new Rectangle(10, 20);
        Circle c = new Circle(5);

        System.out.println("Rectangle area: " + r.area());
        System.out.println("Circle perimeter: " + c.perimeter());
    }
}
```

### Interfaces (and `default`, `static`, `private` methods)

Where a C++ class made entirely of pure virtual functions acts as a "pure interface," Java has a dedicated `interface` construct. A class **implements** an interface (it can implement many) and must supply every abstract method. Since Java 8, interfaces may also contain:

- **`default` methods** — concrete methods with a body that implementing classes inherit (and may override). This lets you add behavior to an interface without breaking existing implementors.
- **`static` methods** — utility methods called on the interface itself, e.g. `Comparator.naturalOrder()`.
- **`private` methods** (Java 9+) — helpers shared by `default`/`static` methods but not exposed to implementors.

Interface fields are implicitly `public static final` (constants). There is no C++ equivalent to `default`/`static`/`private` interface methods — in C++ you would use a base class with concrete methods or free functions.

```java
interface Drawable {
    // Implicitly public abstract — the contract every implementor must fulfill
    void draw();

    // default method (Java 8+): concrete, inherited unless overridden
    default void drawTwice() {
        draw();
        draw();
    }

    // static method (Java 8+): called as Drawable.blank()
    static Drawable blank() {
        return () -> System.out.println("(nothing)");   // lambda target
    }

    // private method (Java 9+): shared helper, not part of the public contract
    private void log() {
        System.out.println("drawing " + this);
    }
}

class Box implements Drawable {
    @Override public void draw() { System.out.println("[]"); }
}
```

> **Abstract class vs interface — when to use which:**
>
> | | Abstract class | Interface |
> |---|---|---|
> | A class can have | one superclass | many interfaces |
> | Instance fields (state) | yes | no (only constants) |
> | Constructors | yes | no |
> | Concrete methods | yes | yes (`default`/`static`) |
> | Access modifiers on methods | any | `public` (or `private` for helpers) |
> | Use when | sharing state + code among close relatives | defining a capability multiple unrelated types can offer |

---

## 7.4 No Multiple Class Inheritance — Use Interfaces

C++ allows a class to inherit from more than one base at once, which is powerful but introduces name clashes and the "diamond problem." **Java forbids multiple inheritance of classes** entirely: a class may `extends` exactly one superclass. Instead, a class may `implements` any number of **interfaces**, combining their contracts. Because interfaces traditionally carried no state and (until `default` methods) no implementation, this neatly sidesteps the diamond problem for *state*. Here `ColoredCircle` is both a `Shape` and `Colored` by implementing two interfaces.

```java
interface Shape {
    void draw();
}

interface Colored {
    String getColor();
}

// "Multiple inheritance" of interfaces (not of classes)
class ColoredCircle implements Shape, Colored {
    private final double radius;
    private final String color;

    public ColoredCircle(double r, String c) {
        this.radius = r;
        this.color = c;
    }

    @Override public void draw() {
        System.out.println("Drawing " + color + " circle");
    }

    @Override public String getColor() { return color; }
}

public class Main {
    public static void main(String[] args) {
        ColoredCircle cc = new ColoredCircle(5, "red");
        cc.draw();
        System.out.println(cc.getColor());   // "red"
    }
}
```

### The Diamond Problem in Java

In C++ the diamond problem arises when a class inherits the *same base sub-object* along two paths, duplicating its state — fixed there with `virtual` inheritance. Java cannot have this *state* diamond, because you cannot inherit from two classes. The only diamond Java has is over **`default` methods**: if a class implements two interfaces that both provide a `default` method with the same signature, the compiler reports an error and forces you to resolve it explicitly with the `Interface.super.method()` syntax.

```text
//         Greeter (interface)
//        /        \
//   English        French      (both add: default void hello())
//        \        /
//        Person  (must resolve the clash)
```

```java
interface English { default String hello() { return "Hello"; } }
interface French  { default String hello() { return "Bonjour"; } }

// Without resolution this would NOT compile — ambiguous default method.
class Person implements English, French {
    @Override
    public String hello() {
        // Explicitly pick one (or combine them) using Interface.super.method()
        return English.super.hello() + " / " + French.super.hello();
    }
}
```

> **Key difference:** C++'s diamond is about *duplicated state* (solved with `virtual` bases). Java's only diamond is about *conflicting default methods* (solved with `X.super.m()`), and there is never a state-duplication problem because there is no multiple class inheritance.

---

## 7.5 Best Practices

This section gathers the chapter's key recommendations. Always annotate overrides with `@Override` so the compiler verifies them. Use `protected` to share state with subclasses (sparingly — prefer `private` plus accessors). Favor **interfaces** for capabilities and **composition** (holding an object as a field) over deep inheritance when there is no genuine "is-a" relationship — composition gives looser coupling and more flexibility. Use `final` to forbid extension or overriding where a class/method is not designed for it, and consider `sealed` (7.10) to restrict the permitted subtypes.

```java
// Use an interface (or abstract class) to express a contract
interface Processor {
    void process();
}

// Always annotate overrides with @Override
class JsonProcessor implements Processor {
    @Override public void process() { /* ... */ }
}

// No virtual destructors — the GC handles cleanup. For external resources,
// implement AutoCloseable and use try-with-resources instead:
class FileHandle implements AutoCloseable {
    @Override public void close() { /* release the OS resource */ }
}

// Prefer composition over inheritance
class Engine { }
class Car {
    private final Engine engine = new Engine();   // composition, not "extends Engine"
}

// Mark classes/methods final when they are not designed for extension
final class ImmutablePoint { /* ... */ }
```

---

## Summary

| Concept | Details |
|---------|---------|
| **Inheritance** | `extends` one superclass (single inheritance) |
| **Virtual by default** | Instance methods are dynamically dispatched; no `virtual` keyword |
| **`@Override`** | Compiler-checked override annotation |
| **Abstract** | `abstract` method = C++ pure virtual; class cannot be instantiated |
| **Interface** | Multiple inheritance of *type/behavior*; `default`/`static`/`private` methods |
| **Polymorphism** | Same supertype reference, different runtime implementations |
| **No destructors** | Garbage collection; use `AutoCloseable` for resources |

---

## 7.6 No Object Slicing in Java

In C++, **object slicing** happens when a derived object is copied by value into a base-class variable, silently discarding the derived data and breaking virtual dispatch. **Java cannot slice**, because a variable of class type is always a *reference* to an object on the heap, never an inline value. Assigning a `Dog` to an `Animal` variable just copies the reference; the object remains a full `Dog`, and dynamic dispatch still finds `Dog.sound()`. This eliminates one of the most insidious C++ OOP bugs entirely.

```java
class Animal {
    String name;
    Animal(String n) { this.name = n; }
    String sound() { return "..."; }
}

class Dog extends Animal {
    String breed;
    Dog(String n, String b) { super(n); this.breed = b; }
    @Override String sound() { return "Woof"; }
}

public class Main {
    // Passing "by value" in Java means passing the REFERENCE by value —
    // the object itself is never copied or sliced.
    static void makeNoise(Animal a) {
        System.out.println(a.sound());   // dynamic dispatch still works
    }

    public static void main(String[] args) {
        Animal a = new Dog("Rex", "Labrador");
        System.out.println(a.sound());   // "Woof"  — NOT sliced
        System.out.println(a.name);      // "Rex"
        // a.breed is not visible through the Animal type, but the object
        // is still a full Dog at runtime (see downcasting in 7.7).

        makeNoise(new Dog("Buddy", "Poodle"));   // "Woof" — no slicing

        // Storing polymorphic objects in a collection: always correct,
        // because the list holds references, never copies.
        java.util.List<Animal> zoo = new java.util.ArrayList<>();
        zoo.add(new Dog("Max", "Husky"));
        for (Animal x : zoo) System.out.println(x.sound());   // "Woof"
    }
}
```

| C++ scenario | Slicing in C++? | Java equivalent | Slicing in Java? |
|---|---|---|---|
| `Animal a = dog;` | Yes | `Animal a = dog;` (reference copy) | **No** |
| `void f(Animal a)` | Yes | `void f(Animal a)` (reference param) | **No** |
| `vector<Animal>` | Yes | `List<Animal>` | **No** |
| `Animal& a = dog;` | No | (references are the only option) | No |

> **Takeaway:** The C++ "never store polymorphic objects by value; use references/pointers/smart pointers" advice is automatic in Java — *all* objects are accessed through references. A C++ virtual `clone()` corresponds to overriding `Object.clone()` or, more idiomatically, a copy constructor / factory method.

---

## 7.7 Upcasting, Downcasting, `instanceof` & Pattern Matching

**Upcasting** — treating a subclass reference as its superclass type — is always safe and implicit (`Animal a = new Dog(...)`). **Downcasting** — going back from a supertype to a subtype — must be explicit (`(Dog) a`) and is checked at runtime: if the object is not actually that type, a `ClassCastException` is thrown. To check first, use `instanceof`. Since Java 16, **pattern matching for `instanceof`** lets you test and bind in one step, eliminating the redundant cast. This is the rough Java analogue of C++'s `dynamic_cast<Derived*>` (which yields `nullptr` on failure) — but Java throws rather than returning null on a bad cast.

```java
Animal a = new Dog("Rex", "Lab");

// Upcasting — implicit and always safe
Animal up = new Dog("Buddy", "Poodle");

// Classic downcast with an instanceof guard
if (a instanceof Dog) {
    Dog d = (Dog) a;            // explicit cast
    System.out.println(d.breed);
}

// Pattern matching for instanceof (Java 16+): test + bind in one step
if (a instanceof Dog d) {       // 'd' is in scope and already cast
    System.out.println(d.breed);
}

// Combined with && — 'd' is usable on the right-hand side
if (a instanceof Dog d && d.breed.equals("Lab")) {
    System.out.println("A Labrador named " + d.name);
}
```

Java 21 extends pattern matching to `switch`, including a sealed-type hierarchy where the compiler can verify all cases are covered:

```java
// Pattern matching in switch (Java 21)
static String describe(Animal a) {
    return switch (a) {
        case Dog d  -> "Dog of breed " + d.breed;
        case Cat c  -> "A cat";
        default     -> "Some animal: " + a.name;
    };
}
```

> **C++ contrast:** `dynamic_cast<Dog*>(p)` returns `nullptr` on failure (no exception) and requires a polymorphic type; `dynamic_cast<Dog&>(ref)` throws `std::bad_cast`. In Java, a failed `(Dog) a` always throws `ClassCastException`; use `instanceof` (or pattern matching) to test safely first.

---

## 7.8 `final` Classes and Methods

`final` is Java's tool for preventing extension and overriding — the direct analogue of C++11's `final`. A `final` **class** cannot be subclassed (e.g. `java.lang.String`). A `final` **method** cannot be overridden by subclasses, even though the class itself may be extended. Marking things `final` documents intent, prevents accidental or malicious subclassing, and can help the JIT compiler devirtualize calls. (`final` on a *variable* means it cannot be reassigned — a separate use of the same keyword.)

```java
final class Leaf {              // no class may extend Leaf
    void draw() { }
}
// class Bad extends Leaf {}    // ERROR: cannot inherit from final Leaf

class Base {
    final void critical() { }   // subclasses may NOT override this
    void normal() { }           // overridable
}

class Derived extends Base {
    // @Override void critical() { }  // ERROR: critical() is final in Base
    @Override void normal() { }       // OK
}
```

**Rules:**
- `final` on a class forbids `extends`; on a method forbids overriding (the two parallel C++ `final`).
- Unlike C++, `final` is a *reserved keyword* in Java (it cannot be used as an identifier).
- Java has no `override final` placement subtlety — you simply write `final` in the method's modifier list, optionally alongside `@Override`.

---

## 7.9 Covariant Return Types

Just like C++, an overriding method in Java may **narrow its return type** to a subtype of the supertype method's return type — this is a *covariant return type*. It is the idiomatic way to write `clone()`/factory methods that return the precise subtype, sparing callers a cast. Java's covariant returns work for any reference type (C++ restricts covariance to pointers/references; Java has no value types for objects, so the restriction is moot).

```java
class Animal {
    Animal copy() { return new Animal(); }
}

class Dog extends Animal {
    @Override
    Dog copy() {                 // covariant: Dog is a subtype of Animal
        return new Dog();
    }
}

Animal a = new Dog();
Dog d = a.copy() instanceof Dog dog ? dog : null; // copy() dynamically returns a Dog
```

---

## 7.10 Sealed Classes & Interfaces — `permits` (Java 17+)

A **sealed** type restricts *which* classes or interfaces may extend/implement it, using a `permits` clause. This sits between fully open inheritance and a `final` class: you keep a closed, known set of subtypes, which lets the compiler reason exhaustively about them (e.g. in `switch` pattern matching, no `default` branch is needed). Every permitted subclass must itself be declared `final`, `sealed`, or `non-sealed`:

- `final` — closes that branch entirely.
- `sealed` — further restricts its own subtypes with another `permits`.
- `non-sealed` — reopens that branch to arbitrary extension.

C++ has no direct equivalent (the closest is a `final` class plus convention). Sealed hierarchies model *algebraic data types* and pair naturally with records and pattern matching.

```java
// The shape hierarchy is closed to exactly these three subtypes.
sealed interface Shape permits Circle, Rectangle, Triangle { }

record Circle(double radius)              implements Shape { }
record Rectangle(double w, double h)      implements Shape { }
final class Triangle implements Shape {   // 'final' is also allowed
    final double base, height;
    Triangle(double b, double h) { this.base = b; this.height = h; }
}

public class Areas {
    static double area(Shape s) {
        // Exhaustive: the compiler knows the only subtypes, so NO default is needed.
        return switch (s) {
            case Circle c    -> Math.PI * c.radius() * c.radius();
            case Rectangle r -> r.w() * r.h();
            case Triangle t  -> 0.5 * t.base * t.height;
        };
    }
}
```

**Rules:**
- A `sealed` class/interface must list its direct subtypes in `permits` (the clause can be omitted if all subtypes live in the same source file).
- Permitted subtypes must be in the same module (or same package, for the unnamed module).
- Each permitted subtype must be exactly one of `final`, `sealed`, or `non-sealed`.

| Modifier | Meaning |
|---|---|
| `final` | No subtypes allowed |
| `sealed` + `permits` | Only the listed subtypes allowed |
| `non-sealed` | Reopens a sealed branch to any subtype |
| (none) | Open to any subtype (the traditional default) |

---

## 7.11 Calling Superclass Behavior with `super`

`super` references the superclass part of the current object. It is used to (a) call the superclass constructor (`super(args)` — must be first in the constructor), and (b) invoke the superclass version of an overridden method (`super.method()`), which is handy when an override *augments* rather than replaces the inherited behavior. This is Java's counterpart to C++'s `Base::method()` qualified call and the member-initializer base-constructor call.

```java
class Base {
    Base(int x) { System.out.println("Base(" + x + ")"); }
    void greet() { System.out.println("Hello from Base"); }
}

class Derived extends Base {
    Derived(int x) {
        super(x);                  // call superclass constructor (must be first)
        System.out.println("Derived(" + x + ")");
    }

    @Override
    void greet() {
        super.greet();             // run Base's version first...
        System.out.println("...and Derived adds more");   // ...then extend it
    }
}
```

> **No "inheriting constructors":** C++11's `using Base::Base;` inherits all base constructors. Java has **no** equivalent — a subclass must declare its own constructors and forward to `super(...)` explicitly. (Records and some frameworks reduce the boilerplate, but the language itself does not inherit constructors.)

---

## 7.12 The `Object` Superclass

Every Java class ultimately extends `java.lang.Object` (directly or transitively). This gives every object a common set of methods, the most important of which are designed to be overridden:

- **`toString()`** — a string representation (used by `System.out.println`, string concatenation, debuggers). The default is an unhelpful `ClassName@hexHash`, so override it.
- **`equals(Object)`** and **`hashCode()`** — logical equality and the matching hash (covered in depth in Chapter 8). They must be overridden together and obey a strict contract.
- **`getClass()`** — the runtime `Class` object (reflection / type identity).
- **`clone()`** — shallow copy (requires implementing `Cloneable`; widely considered error-prone — prefer copy constructors/factories).

There is no single universal base class in C++; `Object` is roughly what C# `object` provides. Because `Object` defines these methods as overridable, collections, printing, and equality "just work" for any type that overrides them correctly.

```java
class Point {
    private final int x, y;
    public Point(int x, int y) { this.x = x; this.y = y; }

    @Override
    public String toString() {                 // override Object.toString
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {           // override Object.equals
        if (this == o) return true;
        if (!(o instanceof Point p)) return false;   // pattern matching
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() {                     // must match equals
        return java.util.Objects.hash(x, y);
    }
}

Point p = new Point(3, 4);
System.out.println(p);          // (3, 4)  — uses toString()
System.out.println(p.getClass().getSimpleName());   // Point
```

---

## Summary

| Concept | Details |
|---------|---------|
| **Inheritance** | `extends` one superclass |
| **Virtual by default** | Dynamic dispatch without a `virtual` keyword |
| **`@Override`** | Compiler-checked override annotation |
| **Abstract** | `abstract` class/method; cannot instantiate |
| **Interface** | Multiple type inheritance; `default`/`static`/`private` methods |
| **Polymorphism** | Same supertype, different runtime behavior |
| **No object slicing** | Variables are references; objects never copied implicitly |
| **`instanceof` + pattern** | Test-and-bind; `switch` patterns (Java 21) |
| **`final`** | No subclass / no override |
| **`sealed` + `permits`** | Closed set of subtypes (Java 17+) |
| **`Object`** | Universal superclass: `toString`/`equals`/`hashCode`/`getClass` |

---

## Next Steps
- Design class hierarchies with `extends` and `implements`
- Use abstract classes and interfaces to define contracts
- Use `sealed` types + pattern matching for closed hierarchies
- Move to [Chapter 8: "Operator Overloading" — and Java's Idiomatic Replacements](../08_operator_overloading/README.md)
