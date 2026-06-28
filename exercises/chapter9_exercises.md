# Chapter 9: Generics - Exercises (Java Edition)

> This chapter retargets C++ templates to **Java generics**. Key differences are
> called out throughout: Java has **no template specialization**, **no default
> type parameters**, **no template-template parameters**, and **no compile-time
> template metaprogramming**. Java generics also use **type erasure** — generic
> type information is not available at runtime. Numbering and difficulty are
> preserved.

## Section 1: Generic Methods 🟢

1. Create a generic "swap" routine. Java cannot swap two local variables through parameters (arguments are passed by value), so operate on a holder/array or a `List`:

   ```java
   static <T> void swap(List<T> list, int i, int j) {
       T tmp = list.get(i);
       list.set(i, list.get(j));
       list.set(j, tmp);
   }
   ```

2. Implement generic `max`/`min` methods using a bounded type parameter:

   ```java
   static <T extends Comparable<T>> T max(T a, T b) {
       return a.compareTo(b) >= 0 ? a : b;
   }
   ```

3. Create a generic method with multiple type parameters (e.g. `<K, V>`)

## Section 2: Generic Classes 🟡

4. Implement a generic `Stack<T>` class (`push`, `pop`, `peek`, `isEmpty`)

5. Create a generic `Queue<T>` class

6. Design a generic `Pair<A, B>` holding two different types — as a class, or more concisely as a `record`:

   ```java
   public record Pair<A, B>(A first, B second) {}
   ```

## Section 3: "Specialization" via Bounds & Overloading 🟡

> Java has **no template specialization**. The closest idioms are bounded type
> parameters, method overloading, and runtime checks. These exercises explore
> behavior differences without specialization.

7. Instead of full specialization, write a generic method plus an **overload** for a specific type, and observe which one the compiler chooses

8. Java cannot partially specialize for "pointer types." Reframe using a bounded type parameter `<T extends Number>` and show how behavior differs from the unbounded `<T>` version

9. Provide behavior for container types using a bound like `<T extends Collection<?>>`; note this is a constraint, not a specialization

## Section 4: Multiple Type Parameters & Tuples 🟡

10. Create a generic class with 2-3 type parameters (e.g. `Triple<A, B, C>`)

11. Implement a heterogeneous tuple-like type — most idiomatically a `record Triple<A, B, C>(A a, B b, C c)`

12. Design an adapter class parameterized by type(s) that wraps and exposes another type

## Section 5: "Default Type Parameters" 🟡

> Java has **no default type parameters**. Reframe using overloaded factory
> methods and the fact that an unbounded `<T>` is effectively bounded by
> `Object`.

13. Java has no default type parameter; provide overloaded **factory methods** (e.g. `Box.of(value)` and `Box.empty()`) to mimic defaults

14. There is no non-type template parameter either; pass a value (e.g. capacity) as a constructor argument instead, with an overload that supplies a default

15. Combine the two: a generic class whose factory methods supply both a default type behavior (via `Object` bound) and default constructor values; note the difference from C++ defaults

## Section 6: Varargs 🔴

16. Create a generic method with variable arguments using varargs:

    ```java
    @SafeVarargs
    static <T> List<T> listOf(T... args) { return List.of(args); }
    ```

17. Process a varargs parameter by iterating the implicit array (Java varargs replace C++ recursive parameter-pack unpacking — note this difference)

18. Design a type-safe `printf`-like helper using `String.format` / `java.util.Formatter` and varargs 🏆

    ```java
    static String fmt(String pattern, Object... args) {
        return String.format(pattern, args);
    }
    ```

## Section 7: Constraints (Bounded Type Parameters) 🔴

> Java has **no `enable_if` / SFINAE**. Constraints are expressed with bounded
> type parameters and multiple bounds. Sealed types + pattern matching are the
> closest analog for constraining a closed set of types.

19. Restrict a generic method to integral-like types with `<T extends Number>`; call `.intValue()` etc.

20. Use multiple bounds to require several capabilities: `<T extends Comparable<T> & Serializable>`

21. There is no SFINAE in Java. Reframe overload resolution using bounded parameters and overloading; optionally use a `sealed` interface + pattern-matching `switch` to constrain to a closed set of types

## Section 8: Wildcards Instead of Partial Specialization 🔴

> Java cannot partially specialize, but **wildcards** (`? extends`, `? super`)
> express the variance these C++ exercises were after.

22. Use an upper-bounded wildcard `List<? extends Number>` to write a method accepting any list of numbers (a producer)

23. Java arrays are covariant and reified, while generics are erased and invariant — demonstrate this contrast (e.g. why `List<Integer>` is not a `List<Number>`, but `Integer[]` is an `Object[]`)

24. Use a lower-bounded wildcard `List<? super Integer>` (a consumer) following the **PECS** rule (Producer Extends, Consumer Super)

## Section 9: Generic Factories Instead of Template-Template Params 🔴

> Java has **no template-template parameters**. Reframe using wildcards over
> nested generics and `Supplier` factories.

25. Create a generic container adapter that accepts a factory: `Supplier<Collection<T>>` to build the backing collection 🏆

    ```java
    static <T> Collection<T> build(Supplier<Collection<T>> factory, T... items) {
        Collection<T> c = factory.get();
        Collections.addAll(c, items);
        return c;
    }
    // build(ArrayList::new, 1, 2, 3);
    ```

26. Implement an algorithm wrapper that works across container types via `Collection<? extends T>` parameters

27. Design a flexible data structure parameterized by both element type and a `Supplier` for its backing store; note Java lacks template-template parameters

## Section 10: Generic Computation 🔴

> Java has **no compile-time template metaprogramming**. Reframe as ordinary
> generic/recursive methods and `static final` constants.

28. Java has no template metaprogramming. Implement the equivalent logic as a normal (possibly recursive) generic method evaluated at runtime; note the difference from C++ TMP

29. Implement factorial as a generic/recursive method, and also as a `static final` constant computed once — contrast with C++ compile-time `constexpr`/template factorial

    ```java
    static long factorial(int n) {
        return n <= 1 ? 1 : n * factorial(n - 1);
    }
    static final long FACT_10 = factorial(10); // computed at class init, not compile time
    ```

30. Design type selection at runtime using generics + bounded types (and optionally `instanceof` pattern matching / a `sealed` hierarchy), since Java has no compile-time type selection

> **Type erasure note**: because Java erases generic type parameters at runtime,
> you cannot write `new T()`, `T.class`, `arr instanceof List<String>`, or
> `new T[n]` directly. Pass a `Class<T>` token or a `Supplier<T>` factory when
> you need runtime type information. This affects exercises 25-30 especially.

---

## Tips for Success

- **Syntax**: Declare type parameters with `<T>` before the return type for methods, after the class name for classes
- **Bounds**: Use `<T extends Bound>` and multiple bounds `<T extends A & B>` for constraints
- **Wildcards**: Apply PECS — `? extends` for producers, `? super` for consumers
- **No specialization/defaults/TT-params**: Use overloading, factories, and wildcards instead
- **Type erasure**: No runtime generic type info; pass `Class<T>` or `Supplier<T>` tokens
- **Varargs**: Annotate safe generic varargs with `@SafeVarargs`
- **Records**: Use generic records for tuples/pairs
- **Comparable**: Bound by `Comparable<T>` for ordering-based generics

## Difficulty Summary

- **Easy (🟢)**: 3 exercises - Generic methods, basic syntax
- **Medium (🟡)**: 12 exercises - Generic classes, multiple parameters, bounds-as-specialization
- **Hard (🔴)**: 15 exercises - Wildcards, constraints, factories, generic computation

## Challenge Problems 🏆

- **Challenge 1**: Type-safe varargs formatter
- **Challenge 2**: Generic factory adapter (template-template-parameter analog) with `Supplier`
- **Challenge 3**: Wildcard/variance patterns following PECS

## Expected Time Commitment

- Easy: 10-15 minutes per exercise
- Medium: 20-40 minutes per exercise
- Hard: 40-90 minutes per exercise
- Total: 12-20 hours for all exercises

## Common Pitfalls to Avoid

- Forgetting that generics are **erased** (no `new T()`, `T.class`, or `new T[]`)
- Mixing covariant arrays with invariant generics (heap pollution warnings)
- Using raw types (e.g. `List` instead of `List<String>`)
- Misapplying wildcards (violating PECS, then unable to add/read)
- Unchecked-cast warnings from erasure
- Expecting specialization/default type parameters that Java does not have
- Overcomplicating with nested wildcards that hurt readability

## Learning Outcomes

After completing these exercises, you will:
✓ Write generic methods and classes
✓ Use bounded type parameters and multiple bounds as constraints
✓ Apply wildcards and the PECS rule
✓ Use varargs and `@SafeVarargs`
✓ Understand type erasure and its consequences
✓ Replace specialization/defaults/TT-params with idiomatic Java
✓ Use generic records for tuples and pairs
✓ Reason about generics vs reified arrays
✓ Avoid common generics pitfalls

## Java Exercise Example: Generic Method

```java
static <T extends Comparable<T>> T maximum(T a, T b) {
    return a.compareTo(b) < 0 ? b : a;
}
```
