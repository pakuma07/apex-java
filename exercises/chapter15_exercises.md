# Chapter 15: Modern Java Features - Exercises

## Section 1: Lambda Functions - Basics 🟢

1. Create a simple lambda with no parameters (`Runnable`)

2. Lambda with parameters and return value (`Function<T,R>` / `BinaryOperator`)

3. Use a lambda where a function pointer would be used in C++ — a functional interface field, plus a method reference (`ClassName::method`)

## Section 2: Lambda Captures 🟡

4. Capture a local variable by value — note Java captures *effectively final* variables (analog of `[=]`)

5. "Capture by reference" — Java has no by-reference capture; mutate shared state via a holder (`int[]`, a 1-field object) or `AtomicInteger` (analog of `[&]`)

6. Mixed captures — capture some values directly and use a holder/`AtomicInteger` for the value you need to mutate (analog of `[=, &x]`)

## Section 3: `var` Type Deduction 🟡

7. Use `var` with local variables (`int`, `double`, `String`)

8. Use `var` with collections (`List`, `Map`, `Set`)

9. Use `var` in enhanced for-each loops

## Section 4: Enhanced For-Each Loop 🟡

10. Iterate a `List` by value (read only)

11. Iterate and modify — note objects are references (calling a setter mutates them); for primitives in an array, use an index to modify

12. Iterate "read only" — Java has no `const` reference; reframe using an immutable/unmodifiable collection (`List.copyOf` / `List.of`)

## Section 5: Null Handling 🟡

13. Use `null` (Java has a single null literal — no `NULL`/`0` distinction)

14. Avoid null with `Optional<T>` — wrap a possibly-absent value and handle it safely

15. Method overloading with `null` — show how an explicit cast `(String) null` selects an overload

## Section 6: Runtime Type Inspection 🟡

> Java has no compile-time type traits; reframe using `instanceof`, `Class` checks, and generics bounds.

16. Check whether a value/type is integral using `instanceof Integer`/`Long`/`Short` (or `Class.isPrimitive`)

17. Dispatch on floating-point types using `instanceof Double`/`Float` (or a `Number` check)

18. Inspect a type with reflection: `Class.isPrimitive()`, `Class.isArray()`; note Java has no `is_const`

## Section 7: Constants & Recursion 🟡

> Java has no `constexpr`; reframe as `static final` constants, compile-time constant expressions, and recursive methods.

19. Compute factorial — as a recursive method, and as a `static final` constant for a fixed input. Note the result is computed at runtime, not compile time.

20. Implement Fibonacci recursively; expose a fixed value as a `static final` constant

21. Use a `static final` constant as an array dimension / fixed bound (the closest analog to a constexpr template argument)

## Section 8: Immutability & Object Reuse 🔴

> Java has no move semantics; reframe around immutability, object reuse, and returning new instances. Garbage collection makes manual move unnecessary.

22. Implement an immutable class (final fields, defensive copies) and a "with" method returning a new instance instead of mutating

23. Show object reuse / copy semantics — a copy constructor or a `record` copy; contrast returning a new instance vs mutating in place

24. Demonstrate why move semantics are unnecessary in Java: pass large objects by reference, explain GC and that copies are shallow references

## Section 9: Delegating Constructors 🟡

25. Constructor delegating to another with `this(...)`

26. Use constructor delegation to reduce duplication

27. Design multi-level constructor delegation

## Section 10: Modern Java Practices 🔴

28. Combine multiple modern Java features in a small project (`var`, lambdas, for-each, try-with-resources)

29. Design using `var`, lambdas, for-each, `record`s, sealed classes, pattern matching, and switch expressions 🏆

30. Create a comprehensive modern-Java system (records, sealed interfaces, pattern-matching `switch`, text blocks)

---

## Tips for Success

- **Lambda syntax**: `(params) -> expression` or `(params) -> { statements; }`
- **`var`**: Cleaner local code, still statically typed
- **For-each**: Works with any `Iterable` and with arrays
- **Null**: Prefer `Optional<T>` for absent values; avoid returning `null`
- **`instanceof`**: Pattern variable form `obj instanceof String s` binds and casts
- **Constants**: `static final` for compile-time constant expressions
- **Immutability**: Prefer immutable objects/records over in-place mutation
- **Delegation**: `this(...)` must be the first statement in a constructor

## Difficulty Summary

- **Easy (🟢)**: 3 exercises - Lambda basics, `var` introduction
- **Medium (🟡)**: 18 exercises - Captures, type deduction, for-each
- **Hard (🔴)**: 9 exercises - Immutability, advanced patterns

## Challenge Problems 🏆

- **Challenge 1**: Complex modern Java system design
- **Challenge 2**: Immutability / object-reuse optimization
- **Challenge 3**: Advanced lambda usage with captured state

## Expected Time Commitment

- Easy: 10-15 minutes per exercise
- Medium: 20-40 minutes per exercise
- Hard: 30-60 minutes per exercise
- Total: 8-15 hours for all exercises

## Common Pitfalls to Avoid

- Trying to mutate a captured local variable directly (must be effectively final)
- Expecting `var` to infer non-local types (fields/parameters/return)
- Returning `null` instead of `Optional` and causing `NullPointerException`
- Iterating a collection while structurally modifying it (`ConcurrentModificationException`)
- Overusing reflection where polymorphism would do
- Expecting `static final` to evaluate arbitrary code at compile time
- Forgetting `this(...)` must be the first statement (delegation)
- Creating constructor delegation cycles

## Learning Outcomes

After completing these exercises, you will:
✓ Write clean lambda functions and method references
✓ Use `var` for readability while keeping static typing
✓ Implement enhanced for-each loops effectively
✓ Avoid null bugs with `Optional`
✓ Inspect types at runtime with `instanceof`/reflection
✓ Use `static final` constants and recursion appropriately
✓ Favor immutability and object reuse over manual memory tricks
✓ Use constructor delegation effectively
✓ Write modern, idiomatic Java 21 code
✓ Combine multiple features naturally

## Modern Java Feature Checklist

After all exercises, verify you can:

**Basics:**
- [ ] Use `var` for local type deduction
- [ ] Write lambda functions and method references
- [ ] Use enhanced for-each loops
- [ ] Use `Optional<T>` instead of returning `null`

**Resources & Safety:**
- [ ] Use try-with-resources and `AutoCloseable`
- [ ] Write immutable classes / records
- [ ] Apply defensive copying
- [ ] Write exception-safe code

**Containers & Algorithms:**
- [ ] Choose appropriate collections
- [ ] Use the Streams API with lambdas
- [ ] Work with iterators correctly
- [ ] Combine collections and streams

**Advanced Techniques:**
- [ ] Write generic methods and classes
- [ ] Use bounded type parameters and wildcards
- [ ] Apply functional interfaces
- [ ] Understand type checks via `instanceof`/`Class`

**Design Patterns:**
- [ ] Use sealed classes/interfaces
- [ ] Use polymorphic collections
- [ ] Use `@Override` correctly
- [ ] Implement `equals`/`hashCode` (or use records)

**Quality:**
- [ ] Write exception-safe code
- [ ] Prefer immutability
- [ ] Use try-with-resources for resource management
- [ ] Follow Java 21 best practices

## Integration Projects

After mastering individual chapters:

1. **Small Project (10 hours)**
   - Student management system
   - File I/O, collections, streams
   - Exception handling, basic OOP

2. **Medium Project (30 hours)**
   - Banking system with polymorphism
   - Advanced OOP, generics, records/sealed types
   - Comprehensive exception handling

3. **Large Project (60+ hours)**
   - Game engine or framework
   - All modern Java features combined
   - Professional-quality architecture

## Java Exercise Example: Strategy Pattern with a Functional Interface

```java
import java.util.function.IntBinaryOperator;

public class Strategy {
    // The strategy is just a functional interface — lambdas implement it.
    public static int compute(int a, int b, IntBinaryOperator op) {
        return op.applyAsInt(a, b);
    }

    public static void main(String[] args) {
        IntBinaryOperator add = (a, b) -> a + b;
        IntBinaryOperator mul = Math::multiplyExact;
        System.out.println(compute(2, 3, add)); // 5
        System.out.println(compute(2, 3, mul)); // 6
    }
}
```
