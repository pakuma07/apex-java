# Chapter 17: Reflection & Annotations — Exercises

Compile and run all exercises with:
```bash
javac YourFile.java
java YourFile
```

---

## Section 1: Class Inspection with Reflection 🟢

1. Given a `Class<?>` object, print its fully qualified name, simple name, and package using `getName()`, `getSimpleName()`, and `getPackageName()`.
2. Write a method that walks the superclass chain of any `Class<?>` (`getSuperclass()`) and prints each ancestor up to `Object`.
3. Print all interfaces a class implements using `getInterfaces()` (and, recursively, the interfaces of its superclasses).
4. List every declared field of a class with its type and modifiers using `getDeclaredFields()` and the `Modifier` class (`Modifier.isPrivate`, `Modifier.isStatic`, ...).
5. List all declared methods of a class with their return types and parameter types using `getDeclaredMethods()`. Verify, for example, that `String` declares a method named `length`.

---

## Section 2: Runtime Type Inspection 🟢

6. Write `isIntegral(Class<?> c)` that returns `true` for `int.class`, `long.class`, `short.class`, `char.class`, `Integer.class`, `Long.class`, etc., and `false` for `float`/`double`/`String`.
   *Hint: combine `isPrimitive()` checks with a set of wrapper classes.*
7. Write `isArray(Object o)` and a method that, given an array's `Class<?>`, returns the element type via `getComponentType()`. Add handling for multi-dimensional arrays.
8. Write predicates `isInterface`, `isEnum`, and `isAbstract` for any `Class<?>` (using `isInterface()`, `isEnum()`, and `Modifier.isAbstract(getModifiers())`).
9. Demonstrate `Class.isAssignableFrom` vs the `instanceof` operator: show one case decided at runtime with `isAssignableFrom` that `instanceof` cannot express (because the type is only known as a `Class<?>`).
10. Write `isNumber(Class<?> c)` that returns `true` when `Number.class.isAssignableFrom(c)` (boxing primitives first). Verify it for `Integer`, `Double`, `BigDecimal`, and rejects `String`.

---

## Section 3: Dynamic Method Invocation 🟡

11. Use `Class.getMethod(name, paramTypes...)` and `Method.invoke(target, args...)` to call a public instance method dynamically (e.g. `String::substring`).
12. Invoke a `static` method reflectively (pass `null` as the target to `invoke`). Demonstrate with `Integer.parseInt`.
13. Access and invoke a `private` method using `getDeclaredMethod` + `setAccessible(true)`. Discuss when this is appropriate and the risks.
14. Write a generic `invokeByName(Object target, String method, Object... args)` helper that finds the matching method by name and argument types (handle primitive/wrapper mismatch), then invokes it.

---

## Section 4: Annotations & Annotation Processing (runtime) 🟡

15. Define a custom annotation `@Author` with `@Retention(RetentionPolicy.RUNTIME)` and `@Target(ElementType.TYPE)`, then read it off a class with `getAnnotation(Author.class)` / `isAnnotationPresent`.
16. Define `@NotNull` and `@Range(min, max)` field annotations and write a `Validator.validate(Object)` that uses reflection to enforce them, returning a list of violations.
17. Define `@Column(name)` for fields and write a serializer that turns any annotated object into a `Map<String,Object>` (or a CSV row), using the annotation's `name` as the key.
18. Define a method annotation `@Benchmark` and write a runner that finds all `@Benchmark`-annotated methods on a class, invokes each, and reports its execution time.

---

## Section 5: Generic Reflective Builders / Proxies 🔴

19. Use `java.lang.reflect.Proxy` + an `InvocationHandler` to build a **logging proxy** that wraps any interface and prints each method name and arguments before delegating to the real implementation.
20. Build a **lazy proxy**: the real target is created only on first method call. Use `Proxy` and an `InvocationHandler` that memoizes a `Supplier<T>`.
21. Build a **caching proxy** for an interface whose methods are pure functions: the `InvocationHandler` caches results keyed by `(method, args)` and returns the cached value on repeat calls.

---

## Section 6: Generic Algorithms with Bounded Types / Constants 🟡

> Java has no compile-time evaluation; these are ordinary generic/static methods. Note where C++ would use `constexpr`.

22. Write `boolean isPalindrome(String s)` and expose a fixed known palindrome as a `static final` constant. Note the check happens at runtime, unlike a C++ `constexpr`/`static_assert`.
23. Write `static boolean isPrime(int n)` and use it to build a `static final int[]` of the first 10 primes (computed in a static initializer).
24. Write `static int log2Floor(int n)` (`floor(log2(n))`). Verify `log2Floor(8) == 3`, `log2Floor(9) == 3`. Consider `Integer.numberOfLeadingZeros`.
25. Write a string hasher `static long fnv1a(String s)` using the FNV-1a algorithm. Verify two strings get different hashes. Compare with `String.hashCode()`.

---

## Section 7: Generic Self-Referential Types (F-bounded generics) 🔴

26. Define `interface Printable<T extends Printable<T>>` with `default void print()` calling `T toString()`-style logic via a self-referential bound. Apply it to `Circle` and `Rectangle`.
27. Implement a generic enum-based `Singleton` registry, or a self-bounded base `abstract class Singleton<T extends Singleton<T>>` exposing `instance()`. Apply it to two classes; discuss why F-bounded generics replace CRTP here.
28. Implement `abstract class Counted<T extends Counted<T>>` that counts how many instances of each concrete subtype exist (keep a `Map<Class<?>, Integer>` or per-subclass count via reflection). Verify counts separately for two subtypes.
29. Use F-bounded generics to implement a fluent `Builder<T extends Builder<T>>` whose setter methods return the self type `T`, so subclass builders chain without casts.

---

## Section 8: Generics with Wildcards & Reflection on Generic Types 🔴

30. Write `void copy(List<? super T> dest, List<? extends T> src)` (PECS) and a `printAll(Collection<?> c)`. Explain the wildcard choices.
31. Use reflection on generic types: given a class that extends `ArrayList<String>`, recover `String` via `getGenericSuperclass()` cast to `ParameterizedType` and `getActualTypeArguments()`.
32. Build a generic factory `<T> T create(Class<T> type)` that instantiates a type via its no-arg constructor (`getDeclaredConstructor().newInstance()`), and a variant taking a `Supplier<T>`. Discuss type erasure and the `Class<T>` token workaround.

---

## Integration Challenges 🏆

**Challenge 1:** Build a runtime `TypeId` registry mapping types to integer IDs using a `Map<Class<?>, Integer>` that assigns IDs on first registration:

```java
TypeId.register(Integer.class);  // -> 0
TypeId.register(Double.class);   // -> 1
TypeId.register(String.class);   // -> 2

assert TypeId.idOf(Integer.class) == 0;
assert TypeId.idOf(Double.class)  == 1;
assert TypeId.idOf(String.class)  == 2;
```

**Challenge 2:** Write an annotation-driven object validator (or a simple dependency injector / serializer):
- Custom field annotations (`@NotNull`, `@Range`, `@Inject`) with `@Retention(RUNTIME)`
- A reflective engine that scans declared fields and applies the rules / injects dependencies
- Return a structured report of violations, or a fully-wired object

**Challenge 3:** Implement a small reflective ORM-like or tuple-like structure using generics + reflection:
- A `Tuple` holding heterogeneous values with a typed `get(int index, Class<T> type)` accessor, or
- A `Table<T>` that maps annotated `record`/class fields to columns and converts rows to/from objects via reflection.

---

## Key Concepts Checklist

```
✓ Class<?> as the runtime type token; getName/getSuperclass/getInterfaces
✓ Inspecting fields/methods/modifiers (getDeclaredFields, Modifier)
✓ Runtime type checks: isPrimitive/isArray/isEnum/isAssignableFrom/instanceof
✓ Dynamic invocation: Method.invoke, getMethod vs getDeclaredMethod
✓ setAccessible for private members (and when to avoid it)
✓ Custom annotations with @Retention(RUNTIME) + @Target; reading via reflection
✓ Dynamic proxies: java.lang.reflect.Proxy + InvocationHandler
✓ F-bounded generics (T extends Base<T>) as the Java replacement for CRTP
✓ Wildcards & PECS (? extends / ? super)
✓ Type erasure & reified-type workarounds via Class<T> tokens
✓ Reflecting generic types: ParameterizedType, getActualTypeArguments
```

---

## Expected Difficulty

- **Easy (🟢)**: 15-20 min each — 5 exercises
- **Medium (🟡)**: 25-45 min each — 11 exercises  
- **Hard (🔴)**: 45-90 min each — 11 exercises
- **Challenges (🏆)**: 60-120+ min — 3 exercises
