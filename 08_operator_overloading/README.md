# Chapter 8: Operator Overloading — Why Java Omits It, and What to Use Instead

In C++, *operator overloading* lets you define how built-in operators such as `+`, `==`, `<<`, and `[]` behave for objects of your own classes, so that `a + b` or `cout << obj` reads as naturally as it would for an `int`. **Java deliberately does not support operator overloading.** The only operator with type-dependent behavior is `+`, which is overloaded *by the language itself* for `String` concatenation (you cannot define your own). Every other operation on objects is expressed through ordinary **named methods**.

This chapter explains *why* Java made that choice, then maps each C++ operator-overloading use case to its idiomatic Java replacement: the `equals()`/`hashCode()` contract for `==`, the `Comparable`/`compareTo` and `Comparator` mechanisms for ordering (`<`, `>`), named methods like `add`/`multiply`/`get` for arithmetic and indexing, and `toString()` for stream output. The guiding principle is the same as the C++ "principle of least surprise" — but achieved through clearly named methods rather than symbols.

## 8.1 Why Java Has No Operator Overloading

Operator overloading was a conscious omission, not an oversight. The Java designers (James Gosling and others) argued that overloaded operators are frequently *abused* — the meaning of `a + b` or `a << b` becomes unknowable without reading the class definition, and "clever" overloads (like using `<<` for stream insertion, or `+` for non-arithmetic combination) hurt readability more than they help. C++'s own guidance ("overload only when the meaning is intuitive and consistent with built-in semantics") concedes the same risk; Java simply removes the possibility. The trade-offs:

- **Pros of omitting it:** every operation on an object is a named method, so behavior is explicit and greppable; no hidden allocations or surprising conversions; a simpler language and compiler.
- **Cons:** numeric/matrix/vector code is more verbose (`a.add(b).multiply(c)` instead of `a + b * c`), and you lose the natural notation for mathematical types.

What Java *does* provide are well-defined **method conventions** and **standard interfaces** that play the role each operator played in C++. The rest of this chapter is that mapping.

### The one built-in exception: `+` on `String`

```java
String greeting = "Hello" + " " + "World";   // language-level String concatenation
String s = "value = " + 42;                   // 42 is converted via Integer.toString
// This is the ONLY overloaded operator in Java, and it is not user-extensible.
```

> **Master mapping — C++ operator → Java approach** (each row is expanded in the sections below):
>
> | C++ operator | Purpose | Idiomatic Java |
> |---|---|---|
> | `a + b`, `a - b`, `a * b` | arithmetic | named methods: `a.add(b)`, `a.subtract(b)`, `a.multiply(b)` |
> | `a == b` | equality | `a.equals(b)` + `hashCode()` contract |
> | `a < b`, `a > b`, … | ordering | `Comparable.compareTo` / `Comparator` |
> | `a[i]` | indexing | `a.get(i)` / `a.set(i, v)` |
> | `cout << a` | output | override `toString()`; print with `System.out.println(a)` |
> | `cin >> a` | input | a parsing method / `Scanner` / factory like `valueOf` |
> | `a = b` (copy assign) | assignment | reference assignment + copy constructor/factory if needed |
> | `++a`, `-a`, `!a` | unary | named methods (`increment()`, `negate()`); `!` only on `boolean` |
> | `obj(args)` | callable | functional interface + lambda / `apply` |
> | `(T) obj` conversion op | conversion | conversion methods (`toX()`, `intValue()`, `valueOf`) |

---

## 8.2 Arithmetic: Named Methods Instead of `operator+`

C++ overloads `operator+`/`operator-` to return a brand-new object, leaving operands unchanged. The Java equivalent is an ordinary **method** that returns a new (typically immutable) instance. By convention these are named `add`, `subtract`, `multiply`, etc. — exactly as the standard library does in `BigInteger`, `BigDecimal`, and `java.time` types. Returning a new object (rather than mutating) keeps the "value semantics" feel.

```java
// C++ would write: Complex operator+(const Complex& other) const
public final class Complex {
    private final double real, imag;

    public Complex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    // Replaces operator+  →  c1.add(c2)
    public Complex add(Complex other) {
        return new Complex(real + other.real, imag + other.imag);
    }

    // Replaces operator-  →  c1.subtract(c2)
    public Complex subtract(Complex other) {
        return new Complex(real - other.real, imag - other.imag);
    }

    @Override
    public String toString() {        // replaces a display()/operator<<
        return real + " + " + imag + "i";
    }
}

// Usage
Complex c1 = new Complex(3, 4);
Complex c2 = new Complex(1, 2);
Complex c3 = c1.add(c2);              // was: c1 + c2
System.out.println(c3);               // 4.0 + 6.0i
```

> **Real-world precedent:** `BigInteger`/`BigDecimal` use exactly this style: `a.add(b).multiply(c)`. Because there is no operator overloading, even arbitrary-precision math reads as method chains rather than `a + b * c`.

---

## 8.3 Unary Operations and Increment/Decrement

C++ overloads unary `operator-`, `operator!`, and the prefix/postfix `operator++`/`operator--` (distinguished by a dummy `int` parameter). Java has **none** of this for user types:

- The built-in `++`, `--`, unary `-`, and `!` work *only* on primitives (`!` only on `boolean`). You cannot make them apply to your class.
- The Java replacement is named methods that **return a new value** (for immutable types) — there is no prefix/postfix distinction to model because there is no operator to overload.

```java
public final class Counter {
    private final int value;
    public Counter(int value) { this.value = value; }

    // Replaces ++n / n++  (no prefix/postfix split — just a clear name)
    public Counter increment() { return new Counter(value + 1); }

    // Replaces unary  -n
    public Counter negate()    { return new Counter(-value); }

    // Replaces  !n  (C++ used bool operator!())  — return a boolean
    public boolean isZero()    { return value == 0; }

    public int value() { return value; }
}

Counter n = new Counter(5);
Counter m = n.increment();    // was: ++n  /  n++
System.out.println(m.value()); // 6
System.out.println(n.negate().value()); // -5
```

> **If you need in-place mutation** (like C++ `++n` modifying the object), make the field non-`final` and have the method update `this` and return `void` (or `this` for chaining). Immutable-and-return-new is generally preferred for value types.

---

## 8.4 Equality: `equals()` / `hashCode()` Instead of `operator==`

This is the single most important mapping. In C++ you overload `operator==` (and derive `!=` from it). In Java:

- `==` on reference types tests **reference identity** (are these the *same object*?), never logical equality. You almost never want this for value types.
- **Logical equality** is `obj.equals(other)`, inherited from `Object` and meant to be overridden.
- `equals()` must be overridden **together with `hashCode()`**, because hash-based collections (`HashMap`, `HashSet`) rely on the contract: *equal objects must have equal hash codes.*

The `equals` contract requires it to be **reflexive, symmetric, transitive, consistent**, and `x.equals(null)` must be `false`. There is no separate `!=` to write — `!a.equals(b)` covers it.

```java
import java.util.Objects;

public final class Vector2D {
    private final double x, y;
    public Vector2D(double x, double y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(Object o) {            // replaces operator==
        if (this == o) return true;              // fast path: same reference
        if (!(o instanceof Vector2D v)) return false;  // type check + bind
        return Double.compare(x, v.x) == 0
            && Double.compare(y, v.y) == 0;      // field-by-field equality
    }

    @Override
    public int hashCode() {                      // MUST be consistent with equals
        return Objects.hash(x, y);
    }
}

Vector2D a = new Vector2D(1, 2);
Vector2D b = new Vector2D(1, 2);
System.out.println(a == b);        // false — different objects (reference identity)
System.out.println(a.equals(b));   // true  — logical equality (was: a == b in C++)
System.out.println(!a.equals(b));  // false — this is your "!="
```

> **`record` shortcut (Java 16+):** declaring `record Vector2D(double x, double y) {}` auto-generates a correct `equals()`, `hashCode()`, and `toString()` from the components — the most concise way to get value semantics. Override them only if you need custom behavior.

> **C++ pitfall avoided:** in C++ `a == b` calls *your* `operator==`; the equivalent mistake in Java is using `==` (identity) where you meant `.equals()` (logical equality) — especially for `String`. Always use `.equals()` for content comparison.

---

## 8.5 Ordering: `Comparable`/`compareTo` and `Comparator` Instead of `operator<`

C++ overloads `operator<` (and friends) so objects can be sorted and used as keys in `std::map`/`std::set`. Java splits this into two mechanisms:

1. **`Comparable<T>`** — the type's *natural ordering*. Implement `int compareTo(T other)`, returning negative / zero / positive when `this` is less / equal / greater than `other`. This is what `Collections.sort`, `TreeMap`, `TreeSet`, and `Arrays.sort` use by default. It is the direct analogue of giving your class an `operator<` (plus the rest of the comparisons in one method).
2. **`Comparator<T>`** — an *external* ordering you pass in, allowing many different orderings without modifying the class. This is like passing a custom comparison functor to `std::sort`.

You do **not** write separate `<`, `>`, `<=`, `>=` methods — a single `compareTo` (or `Comparator`) expresses all four. Keep `compareTo` *consistent with* `equals` (`a.compareTo(b) == 0` should usually imply `a.equals(b)`), exactly as C++ requires a strict-weak-ordering for `operator<`.

```java
import java.util.*;

public final class Person implements Comparable<Person> {
    private final String name;
    private final int age;
    public Person(String name, int age) { this.name = name; this.age = age; }
    public String name() { return name; }
    public int age()     { return age; }

    // Natural ordering by age — replaces operator< (and >, <=, >= all at once)
    @Override
    public int compareTo(Person other) {
        return Integer.compare(this.age, other.age);
    }

    @Override public String toString() { return name + "(" + age + ")"; }
}

List<Person> people = new ArrayList<>(List.of(
        new Person("Bob", 30), new Person("Alice", 25)));

// Sort by natural order (uses compareTo) — like std::sort with operator<
Collections.sort(people);
System.out.println(people);   // [Alice(25), Bob(30)]

// "p1 < p2" becomes a sign test on compareTo:
Person p1 = people.get(0), p2 = people.get(1);
if (p1.compareTo(p2) < 0) System.out.println("p1 is younger");

// Comparator: a DIFFERENT ordering without touching the class
// (like passing a custom comparison functor to std::sort)
people.sort(Comparator.comparing(Person::name));            // by name
people.sort(Comparator.comparingInt(Person::age).reversed()); // by age, descending

// TreeSet uses the natural ordering (or a supplied Comparator) as the key order
TreeSet<Person> byAge = new TreeSet<>(people);              // ordered like std::set
```

| C++ | Java |
|---|---|
| `bool operator<(const T&) const` | `int compareTo(T)` via `Comparable<T>` |
| `operator>` / `<=` / `>=` | sign of `compareTo` (no separate methods) |
| custom comparison functor for `std::sort` | `Comparator<T>` (e.g. `Comparator.comparing(...)`) |
| `std::map`/`std::set` key ordering | `TreeMap`/`TreeSet` using `Comparable`/`Comparator` |

---

## 8.6 Indexing: `get`/`set` Instead of `operator[]`

C++'s `operator[]` gives array-like syntax `m[i]` and, by returning a reference, supports both reading and writing (`m[0][0] = 1`). **Java has no overloadable subscript operator** — `[]` works only on built-in arrays. For your own collection types, you expose **`get`** and **`set`** methods. This is exactly what the standard collections do: `list.get(i)`, `list.set(i, v)`, `map.get(key)`, `map.put(key, value)`.

```java
import java.util.*;

public final class Matrix {
    private final int[][] data;
    public Matrix(int rows, int cols) { data = new int[rows][cols]; }

    // Replaces  int operator[](int,int) for reading  →  m.get(r, c)
    public int get(int row, int col)            { return data[row][col]; }

    // Replaces the assignable  m[r][c] = v       →  m.set(r, c, v)
    public void set(int row, int col, int value) { data[row][col] = value; }
}

Matrix m = new Matrix(3, 3);
m.set(0, 0, 1);            // was: m[0][0] = 1;
m.set(1, 2, 5);            // was: m[1][2] = 5;
System.out.println(m.get(1, 2));   // 5

// Standard-library precedent:
List<String> list = new ArrayList<>(List.of("a", "b"));
list.set(0, "z");          // was: list[0] = "z";
System.out.println(list.get(0));   // z
```

> **Why no `[]` overload?** Returning a mutable reference (as C++ `T& operator[]` does) has no clean Java analogue — Java cannot return an assignable lvalue. Splitting into `get`/`set` makes the read vs write intent explicit, and naturally accommodates bounds checking (which the methods above delegate to the backing array, throwing `ArrayIndexOutOfBoundsException`).

---

## 8.7 Output/Input: `toString()` Instead of `operator<<`/`operator>>`

C++ overloads the stream operators `<<` and `>>` (as non-member `friend` functions returning the stream, to allow chaining) so objects work with `cout`/`cin`. Java's approach is completely different:

- For **output**, override **`toString()`** (from `Object`). `System.out.println(obj)`, string concatenation, `String.format`/`printf`, loggers, and debuggers all call it automatically. Chaining is irrelevant because `println` takes one argument and `+` builds the whole string first.
- For **input**, there is no `operator>>`. You parse explicitly with a `Scanner`, `BufferedReader`, or a static factory/parse method (e.g. `Integer.parseInt`, `LocalDate.parse`, your own `Point.parse`).

```java
public final class Point {
    private final int x, y;
    public Point(int x, int y) { this.x = x; this.y = y; }
    public int x() { return x; }
    public int y() { return y; }

    // Replaces  friend ostream& operator<<(ostream&, const Point&)
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    // Replaces  friend istream& operator>>(istream&, Point&)
    // Idiomatic Java uses a static factory that parses input text.
    public static Point parse(String token) {       // e.g. "3 4"
        String[] parts = token.trim().split("\\s+");
        return new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}

Point p = new Point(3, 4);
System.out.println(p);                 // (3,4)   — toString() called automatically
System.out.println("point = " + p);    // point = (3,4)  — used in concatenation

// Reading (replaces  cin >> p2):
java.util.Scanner sc = new java.util.Scanner(System.in);
Point p2 = new Point(sc.nextInt(), sc.nextInt());   // or Point.parse(sc.nextLine())
```

| C++ stream idiom | Java equivalent |
|---|---|
| `ostream& operator<<(ostream&, const T&)` | override `toString()` |
| `cout << obj << endl;` | `System.out.println(obj);` |
| chaining `cout << a << b;` | `System.out.println("" + a + b);` or `printf` |
| `istream& operator>>(istream&, T&)` | `Scanner`/`BufferedReader` + a parse/factory method |

---

## 8.8 The Function-Call Operator: Functional Interfaces Instead of `operator()`

C++ overloads `operator()` to make objects *callable* (functors), which can carry state and be passed to algorithms like `std::transform`. Java has no callable-object operator, but achieves the same goal with **functional interfaces** — interfaces with a single abstract method — implemented by **lambdas** or method references. The standard `java.util.function` package supplies `Function<T,R>`, `Predicate<T>`, `Supplier<T>`, `Consumer<T>`, `UnaryOperator<T>`, etc. The lambda's captured variables play the role of the functor's state.

```java
import java.util.*;
import java.util.function.*;

// C++ functor:  struct Multiplier { int operator()(int x){ return x*factor; } };
// Java: a lambda capturing 'factor' (the state) implementing a functional interface.
int factor = 3;
IntUnaryOperator times3 = x -> x * factor;     // the "operator()" is apply/applyAsInt

System.out.println(times3.applyAsInt(5));      // 15   (was: times3(5))
System.out.println(times3.applyAsInt(10));     // 30

// Used with the Streams API — the Java analogue of std::transform:
List<Integer> v = new ArrayList<>(List.of(1, 2, 3, 4));
v.replaceAll(x -> x * factor);                 // in place: [3, 6, 9, 12]

// A stateful "functor" as a class implementing a functional interface:
class Multiplier implements IntUnaryOperator {
    private final int factor;
    Multiplier(int factor) { this.factor = factor; }
    @Override public int applyAsInt(int x) { return x * factor; }   // ≈ operator()
}
IntUnaryOperator m = new Multiplier(4);
System.out.println(m.applyAsInt(5));           // 20
```

---

## 8.9 Assignment and Conversion

### `operator=` (copy assignment)

C++ overloads `operator=` to deep-copy resources (the Rule of Three/Five). Java has **no assignment operator to overload**: `a = b` simply rebinds the reference `a` to the same object as `b` — no copy occurs, and there is nothing to customize. Because the garbage collector manages memory, there are no self-assignment checks, no manual `delete` of old data, and no double-delete hazards. When you genuinely need a *copy*, you do it explicitly:

```java
// 'b = a' copies the REFERENCE, not the object — both point to one object.
StringBuilder a = new StringBuilder("hello");
StringBuilder b = a;             // b and a are the SAME object
b.append("!");
System.out.println(a);           // hello!  — mutation visible through both

// Want an independent copy? Be explicit (copy constructor / factory / clone):
StringBuilder c = new StringBuilder(a);    // copy constructor → separate object
List<Integer> copy = new ArrayList<>(original);   // defensive copy
```

> Immutable value types (like `String`, `Integer`, or your own `record`) make this a non-issue: sharing a reference is safe because the object can never change.

### Conversion operators

C++ provides `operator T()` (optionally `explicit`) for converting objects to other types. Java has **no user-defined conversions** (no implicit coercion of object types). The idiomatic replacement is **named conversion methods**:

- *To* another type: instance methods like `intValue()`, `doubleValue()`, `toString()`, `toList()`.
- *From* another type: static factory methods like `Integer.valueOf(...)`, `LocalDate.parse(...)`.

```java
public final class Fraction {
    private final int num, den;
    public Fraction(int num, int den) { this.num = num; this.den = den; }

    // Replaces  explicit operator double()
    public double toDouble() { return (double) num / den; }

    // Replaces  explicit operator string()  /  also serves operator<<
    @Override public String toString() { return num + "/" + den; }

    // Replaces a "from" conversion / constructor; like a C++ converting ctor,
    // but always explicit because Java never converts object types implicitly.
    public static Fraction valueOf(String s) {       // "3/4"
        String[] p = s.split("/");
        return new Fraction(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
    }
}

Fraction f = new Fraction(3, 4);
double d = f.toDouble();               // was: static_cast<double>(f)
System.out.println(f);                 // 3/4
Fraction g = Fraction.valueOf("5/8");  // was: a converting constructor
```

> **No `explicit` keyword needed:** since Java performs no implicit object conversions in the first place, the entire C++ concern about implicit-vs-`explicit` conversion operators evaporates. Every conversion is an explicit method call.

---

## 8.10 What You Cannot Do (and the Java Mindset)

To be unambiguous: in Java you **cannot**
- define or redefine `+`, `-`, `*`, `/`, `%`, `==`, `<`, `>`, `[]`, `()`, `<<`, `>>`, `=`, `++`, `--`, `!`, `~`, `&`, `|`, `^` for your own types;
- change the meaning, arity, precedence, or associativity of any operator;
- add a conversion that the compiler applies implicitly.

The only operator with object-aware behavior baked into the language is `+` for `String`. Everything else is a method. This is a feature, not a limitation: reading Java code, you always know that `a.add(b)`, `a.equals(b)`, and `a.compareTo(b)` are method calls with discoverable definitions, with none of the "what does `+` mean for this type?" ambiguity that motivates C++'s "principle of least surprise" warnings.

```java
// ❌ Not possible in Java:
// Vector operator+(Vector a, Vector b)   // no free-function operators
// MyType a = b;  with custom copy logic  // no operator=
// implicit  double d = myFraction;        // no implicit conversion operators

// ✅ The Java way — explicit, named, discoverable:
Vector2D sum   = a.add(b);          // arithmetic        (was a + b)
boolean  same  = a.equals(b);       // equality          (was a == b)
int      order = x.compareTo(y);    // ordering          (was x < y)
String   text  = obj.toString();    // output            (was cout << obj)
int      v     = list.get(i);       // indexing read     (was list[i])
list.set(i, v);                     // indexing write    (was list[i] = v)
```

---

## Summary

| C++ operator overload | Purpose | Idiomatic Java replacement |
|---|---|---|
| `operator+`, `operator-`, `operator*` | arithmetic | named methods: `add`, `subtract`, `multiply` (return new object) |
| `operator==` (+ `!=`) | equality | override `equals(Object)` **and** `hashCode()` |
| `operator<` (+ `>`, `<=`, `>=`) | ordering | `Comparable.compareTo` and/or `Comparator` |
| `operator[]` | indexing | `get(i)` / `set(i, v)` methods |
| `operator<<` / `operator>>` | stream I/O | override `toString()`; parse via `Scanner`/factory |
| `operator()` | callable functor | functional interface + lambda / method reference |
| `operator=` | copy assignment | reference assignment; explicit copy constructor/factory |
| `operator T()` (conversion) | type conversion | conversion methods (`toX`, `valueOf`) — always explicit |
| `++`, `--`, unary `-`, `!` | unary ops | named methods; built-in unary ops apply only to primitives |
| built-in `+` on `String` | concatenation | the **only** type-aware operator (not user-extensible) |

---

## Next Steps
- Override `equals`/`hashCode`/`toString` (or use `record`) for value types
- Implement `Comparable` and use `Comparator` for ordering and sorting
- Express arithmetic/indexing/callables as named methods, lambdas, and `get`/`set`
- Move to [Chapter 9: Generics (Java's answer to C++ templates)](../09_templates/README.md)
