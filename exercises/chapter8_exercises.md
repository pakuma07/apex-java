# Chapter 8: Equality, Ordering & Operators - Exercises (Java Edition)

> Java has **no operator overloading**. This chapter retargets each classic
> operator-overloading exercise to its idiomatic Java equivalent: named methods,
> `equals`/`hashCode`, `Comparable`/`Comparator`, `toString()`, indexing methods,
> functional interfaces, and conversion methods. Numbering and difficulty are
> preserved.

## Section 1: Arithmetic Methods 🟢

> Java cannot overload `+`, `-`, or `*`. Implement named methods that return
> **new immutable instances**. Records are a natural fit.

1. Create a `Complex` class (or `record`) and implement `add` and `subtract` methods that each return a new `Complex`

2. Implement a `Vector` class with a `multiply(double scalar)` method returning a new scaled `Vector`

3. Design a `Money` class with arithmetic methods (`add`, `subtract`) returning new immutable `Money` instances

```java
public record Complex(double re, double im) {
    public Complex add(Complex o)      { return new Complex(re + o.re, im + o.im); }
    public Complex subtract(Complex o) { return new Complex(re - o.re, im - o.im); }
}
```

## Section 2: Equality & Ordering 🟡

4. Create a `Person` class and implement `equals` and `hashCode` (the Java analog of `==`), plus `Comparable<Person>` `compareTo` for the `<` analog

5. Implement a `Date` class that implements `Comparable<Date>`; `compareTo` covers all comparison operators at once (`<`, `<=`, `>`, `>=`, `==`)

6. Design a sorting system using `Collections.sort` with a `Comparator` (e.g. `Comparator.comparing(...)`, `thenComparing(...)`, `reversed()`)

```java
people.sort(Comparator.comparing(Person::lastName)
                      .thenComparing(Person::firstName));
```

## Section 3: Display & Parsing 🟡

> The C++ stream operators `<<` and `>>` map to `toString()` for output and a
> factory/`Scanner` for input.

7. Override `toString()` for custom class display (Java's replacement for `operator<<`)

8. Implement the `>>` analog: a static factory method `parse(String)` (or read fields with a `Scanner`) that constructs an instance from input

9. Create a logging system: a logger that formats objects via their `toString()` (e.g. `logger.log(obj)` calls `String.valueOf(obj)`)

## Section 4: Indexing Methods 🟡

> Java cannot overload `[]`. Use methods with explicit bounds checking.

10. Implement an `Array` class with `get(int i)` / `set(int i, T value)` methods that throw `IndexOutOfBoundsException` on invalid indices

11. Create a `Matrix` class with `get(int row, int col)` / `set(int row, int col, double v)` for 2D access

12. Design a `Dictionary` class wrapping a `Map<K, V>` with `get(K key)` / `put(K key, V value)`

## Section 5: Increment/Decrement 🟡

> Java has `++`/`--` only for built-in numeric types and cannot overload them
> for objects. Reframe as `increment()` / `decrement()` methods, and discuss
> mutation vs immutability.

13. Implement `increment()` and `decrement()` methods on a mutable `Counter` class

14. Provide both `increment()` and `decrement()`; also provide an **immutable** variant that returns a new instance instead of mutating

15. Compare the two designs: in-place mutation vs returning a new immutable instance (discuss thread-safety and aliasing instead of prefix/postfix performance)

## Section 6: Copying & Assignment 🟡

> Java assignment (`=`) only rebinds references; there is no copy/move
> assignment. Reframe around copy constructors, `clone()`, defensive copying,
> and immutability.

16. Implement a deep copy for a class holding mutable members: write a copy constructor `MyClass(MyClass other)` that copies nested mutable state defensively

17. Compare a copy constructor vs implementing `Cloneable`/`clone()`; note the pitfalls of `clone()` and why copy constructors/factory methods are preferred

18. Java has **no move semantics** (the GC and references make it unnecessary). Demonstrate object reuse and immutability as the idiomatic alternative to "transferring resources"

## Section 7: Functional Interfaces 🔴

> The C++ function-call operator `()` (functors) maps to Java functional
> interfaces, lambdas, and method references.

19. Create a callable using a functional interface: implement `Function<T,R>` (or a custom `@FunctionalInterface`) with a lambda

20. Implement a `Comparator<T>` (the comparator "functor") and use it with `Collections.sort` / `Stream.sorted`

21. Design mathematical function objects using `DoubleUnaryOperator`, `DoubleBinaryOperator`, or `BiFunction`

```java
DoubleUnaryOperator square = x -> x * x;
Function<Integer, Integer> inc = n -> n + 1;
Comparator<String> byLen = Comparator.comparingInt(String::length);
```

## Section 8: Type Conversion 🔴

> Java has **no user-defined implicit conversions** (no conversion operators).
> Use explicit conversion methods and static factories. Discuss autoboxing as
> the only built-in implicit conversion.

22. Provide explicit conversion methods such as `toDouble()` / `toString()` (Java has no implicit conversion operator)

23. Provide a static `valueOf(...)` / `of(...)` factory for constructing from another type — the explicit, intent-revealing equivalent of C++'s `explicit` conversion

24. Demonstrate autoboxing/unboxing (`int` ↔ `Integer`) and the pitfalls it can hide (e.g. `NullPointerException` on unboxing `null`, identity vs value comparison)

## Section 9: Boolean Methods 🟡

> Java cannot overload `&&` / `||`, and short-circuit evaluation applies only to
> the built-in `boolean` operators. Reframe around `boolean`-returning methods.

25. Implement `boolean`-returning methods (e.g. `isValid()`, `matches(...)`) and combine them with the built-in short-circuit `&&` / `||`

26. Implement a method like `boolean asBoolean()` / `isPresent()` instead of a C++ boolean conversion operator

27. Design decision-making classes whose methods return `boolean`, then compose decisions with short-circuit operators; note you cannot change `&&`/`||` semantics

## Section 10: Iteration & Optional Access 🔴

> The C++ member-access/dereference operators (`->`, `*`) for smart pointers and
> iterators map to `Iterator`/`Iterable` and `Optional` in Java.

28. Implement `Iterable<T>` and a custom `Iterator<T>` so your container works in an enhanced `for` loop 🏆

29. Implement element access via an iterator's `next()` (the `*`/dereference analog) and demonstrate `hasNext()`

30. Create a "smart pointer-like" wrapper using `Optional<T>`: provide `get()`, `orElse(...)`, `map(...)` instead of overloaded `->`/`*`

```java
public class Box<T> implements Iterable<T> {
    private final List<T> items = new ArrayList<>();
    public Optional<T> first() { return items.stream().findFirst(); }
    @Override public Iterator<T> iterator() { return items.iterator(); }
}
```

---

## Tips for Success

- **No operator overloading**: Use clearly named methods (`add`, `multiply`, `get`)
- **Equality**: Always override `equals` **and** `hashCode` together
- **Ordering**: Implement `Comparable<T>` for natural order; use `Comparator` for alternatives
- **Display**: Override `toString()` instead of `operator<<`
- **Immutability**: Prefer returning new instances over mutation; records help
- **Functional interfaces**: Use lambdas and method references for "functors"
- **Conversions**: Be explicit (`toX()` / `valueOf`); Java has no implicit user conversions
- **Defensive copying**: Copy mutable fields in/out to preserve invariants

## Difficulty Summary

- **Easy (🟢)**: 3 exercises - Basic arithmetic methods, immutable instances
- **Medium (🟡)**: 18 exercises - Equality/ordering, display/parsing, indexing, increment
- **Hard (🔴)**: 9 exercises - Functional interfaces, conversions, iteration/Optional

## Challenge Problems 🏆

- **Challenge 1**: Custom `Iterable`/`Iterator` (member-access analog)
- **Challenge 2**: Explicit conversion methods and autoboxing pitfalls
- **Challenge 3**: Comparator composition for sorting/algorithm pipelines

## Expected Time Commitment

- Easy: 10-15 minutes per exercise
- Medium: 20-40 minutes per exercise
- Hard: 30-60 minutes per exercise
- Total: 8-15 hours for all exercises

## Common Pitfalls to Avoid

- Overriding `equals` but not `hashCode` (breaks hash-based collections)
- Comparing objects with `==` instead of `equals`
- An inconsistent `compareTo` (must agree with `equals` for sorted collections)
- Returning mutable internal collections instead of unmodifiable/defensive copies
- Unboxing a `null` `Integer` (throws `NullPointerException`)
- Relying on `clone()` instead of copy constructors/factories

## Learning Outcomes

After completing these exercises, you will:
✓ Implement arithmetic as named methods returning immutable instances
✓ Correctly implement `equals`/`hashCode`
✓ Implement `Comparable` and build `Comparator`s
✓ Use `toString()` and factory/`Scanner` parsing for display/input
✓ Provide bounds-checked indexing methods
✓ Use functional interfaces, lambdas, and method references
✓ Design explicit type-conversion methods and understand autoboxing
✓ Implement `Iterable`/`Iterator` and `Optional`-based access

## Java Closing Example: Comparable Record

```java
public record Point(int x, int y) implements Comparable<Point> {
    @Override
    public int compareTo(Point o) {
        int cmp = Integer.compare(x, o.x);
        return cmp != 0 ? cmp : Integer.compare(y, o.y);
    }
}

// records auto-generate equals(), hashCode(), and toString()
List<Point> pts = new ArrayList<>(List.of(new Point(2, 1), new Point(1, 9)));
Collections.sort(pts); // uses compareTo
```
