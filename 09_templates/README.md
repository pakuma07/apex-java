# Chapter 9: Generics

Generics are Java's mechanism for **generic programming**: writing code once that works with many different types, with type safety checked by the compiler. Instead of duplicating a class or method for `Integer`, `Double`, `String`, and every other type — or falling back to `Object` and unsafe casts — you write a single *generic* type parameterized over a type variable, and the compiler verifies that every use is type-correct. This is Java's analogue of C++ templates, but the two work very differently under the hood, and a recurring theme of this chapter is contrasting them.

The biggest difference is **how the genericity is implemented**. C++ templates are a *compile-time code-generation* facility: the compiler stamps out a separate, fully specialized copy of the code for each set of type arguments (`vector<int>` and `vector<string>` are genuinely different machine code). Java generics use **type erasure**: there is a *single* compiled class, the type parameters exist only at compile time for checking, and at runtime the type information is "erased" to `Object` (or the bound). This makes Java generics lighter on code size but imposes restrictions C++ programmers find surprising — no `new T[]`, no `T.class`, no primitive type arguments, and no template specialization or metaprogramming. We will call out each contrast explicitly.

> **C++ template vs Java generic — at a glance**
> - C++: one template, many *instantiations* (separate code per type) — *monomorphization*.
> - Java: one class, *type erasure* — types checked at compile time, gone at runtime.
> - C++ `template <typename T>` ↔ Java `<T>` type parameter.
> - C++ has full + partial specialization and template metaprogramming. Java has **none** of these.
> - C++ accepts any type incl. primitives and non-type (value) parameters. Java accepts only **reference types** (no `int`, use `Integer`) and only **types** (no value parameters).

## 9.1 Generic Methods

A generic method declares one or more type parameters in angle brackets *before the return type*, e.g. `<T> T identity(T x)`. The compiler performs **type inference**, deducing `T` from the argument types, so `max(5, 3)` infers `T = Integer` automatically (autoboxing turns the `int` literals into `Integer`). You can also specify the type explicitly with the slightly unusual `this.<Double>max(...)` syntax when inference is impossible. The body must compile for whatever the type parameter could be — by itself a bare `T` only supports `Object`'s methods, so to call `+` or compare you need a *bound* (see 9.4).

```java
// Generic method that works with any reference type
public class Utils {

    // <T> introduces the type parameter; it is inferred from the arguments
    static <T> T firstOf(T a, T b) {
        return a;
    }

    // A bounded generic method so we can actually compare the values
    static <T extends Comparable<T>> T max(T a, T b) {
        return (a.compareTo(b) >= 0) ? a : b;
    }

    public static void main(String[] args) {
        System.out.println(max(5, 3));            // 5      (T = Integer)
        System.out.println(max(3.5, 2.1));        // 3.5    (T = Double)
        System.out.println(max("Hello", "World"));// World  (T = String)

        // Explicit type argument (rarely needed):
        String s = Utils.<String>firstOf("a", "b");
    }
}
```

> **Contrast with C++:** C++'s `template <typename T> T add(T a, T b){ return a + b; }` compiles `a + b` directly, working for any type with `operator+`. In Java a bare `<T>` only sees `Object`, so there is **no way to write a generic `add` that uses `+`** — Java has no operator overloading (Chapter 8) and no "duck-typed" templates. You must bound `T` to an interface that provides the operation (e.g. `Comparable`), or accept the concrete numeric type.

### Multiple Type Parameters

A generic method (or class) may declare several type parameters, each inferred or specified independently. This is useful when a method relates two distinct types — here `convert` takes a value of type `U` and a converter and produces a `T`.

```java
import java.util.function.Function;

static <T, U> T convert(U value, Function<U, T> converter) {
    return converter.apply(value);
}

int len = convert("hello", String::length);   // U = String, T = Integer  → 5
```

> Unlike the C++ example where `T` appears only in the return type and must be given explicitly, Java idiomatically passes a `Function<U,T>` so both parameters are inferable — Java has no `static_cast<T>` to drop into a generic body.

---

## 9.2 Generic Classes

A generic class parameterizes an entire class over one or more types, which is how the collections framework (`List<T>`, `Map<K,V>`, `Optional<T>`) is built. Type arguments are written in angle brackets: `Stack<Integer>`, `Stack<String>`. Since Java 7 you can use the **diamond operator** `<>` on the right-hand side and let the compiler infer the arguments: `new Stack<Integer>()` → `new Stack<>()`. Each parameterization is *checked* as a distinct type at compile time, but — crucially, and unlike C++ — there is only **one** compiled `Stack` class shared by all of them (see 9.6 on erasure).

```java
import java.util.ArrayList;
import java.util.List;

public class Stack<T> {
    private final List<T> elements = new ArrayList<>();

    public void push(T value) {
        elements.add(value);
    }

    public T pop() {
        if (elements.isEmpty()) throw new java.util.NoSuchElementException();
        return elements.remove(elements.size() - 1);
    }

    public T top() {
        return elements.get(elements.size() - 1);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public static void main(String[] args) {
        Stack<Integer> intStack = new Stack<>();   // diamond operator
        intStack.push(10);
        intStack.push(20);
        System.out.println(intStack.top());        // 20

        Stack<String> stringStack = new Stack<>();
        stringStack.push("hello");
        System.out.println(stringStack.top());     // hello
    }
}
```

> **Contrast with C++:** In C++, `Stack<int>` and `Stack<string>` are *unrelated, separately generated* types and `Stack<int>` stores `int`s inline. In Java, `Stack<Integer>` and `Stack<String>` are the *same* runtime class (`Stack`), they store `Object` references internally, and `Integer` is a boxed object — there is no `Stack<int>` (primitives are not allowed as type arguments; see 9.6).

---

## 9.3 Generic Interfaces

Interfaces can be generic too — the foundation of the standard library's `Comparable<T>`, `Iterable<T>`, `Comparator<T>`, `Function<T,R>`, and the `Collection<E>` hierarchy. A class implementing a generic interface either fixes the type argument (`implements Comparable<Money>`) or stays generic itself.

```java
// A generic interface (mirrors java.lang.Comparable<T>)
interface Container<T> {
    void add(T item);
    T get(int index);
    int size();
}

// Implementation that fixes T = String
class StringBag implements Container<String> {
    private final java.util.List<String> items = new java.util.ArrayList<>();
    @Override public void add(String item) { items.add(item); }
    @Override public String get(int i)     { return items.get(i); }
    @Override public int size()            { return items.size(); }
}

// Implementation that stays generic
class ListContainer<T> implements Container<T> {
    private final java.util.List<T> items = new java.util.ArrayList<>();
    @Override public void add(T item) { items.add(item); }
    @Override public T get(int i)     { return items.get(i); }
    @Override public int size()       { return items.size(); }
}
```

---

## 9.4 Bounded Type Parameters (`extends` / `super`)

By default a type parameter `T` is treated as `Object`, so you can only call `Object` methods on it. A **bound** restricts `T` to subtypes of a given type, which simultaneously (a) limits which arguments are legal and (b) lets you call that type's methods inside the body. The keyword is always **`extends`** (for both classes and interfaces, and even when "implementing" an interface), e.g. `<T extends Comparable<T>>`. You can require multiple bounds with `&`: `<T extends Number & Comparable<T>>`.

This is Java's substitute for C++ template *constraints*. C++ (before C++20 concepts) had no language-level way to state requirements — a template just failed to compile deep inside the body if a type lacked an operation. Java bounds state the requirement **up front** in the signature, giving clear errors at the call site.

```java
import java.util.List;

public class Bounds {

    // Upper bound: T must be a Number (so we can call doubleValue())
    static <T extends Number> double sum(List<T> nums) {
        double total = 0;
        for (T n : nums) total += n.doubleValue();   // Number method available
        return total;
    }

    // Multiple bounds: T must be both Comparable and a Number
    static <T extends Number & Comparable<T>> T max(List<T> xs) {
        T best = xs.get(0);
        for (T x : xs) if (x.compareTo(best) > 0) best = x;
        return best;
    }

    public static void main(String[] args) {
        System.out.println(sum(List.of(1, 2, 3)));        // 6.0
        System.out.println(max(List.of(3, 1, 4, 1, 5)));  // 5
        // sum(List.of("a","b"));   // compile error: String is not a Number
    }
}
```

> **Note on `super` for type parameters:** A *declared type parameter* may only use `extends` (an upper bound). The keyword `super` appears with **wildcards** (next section), not in a `<T super X>` declaration — Java has no lower-bounded type *parameters*, only lower-bounded *wildcards*.

---

## 9.5 Wildcards (`? extends` / `? super`) and PECS

A **wildcard** `?` is an *anonymous* type argument used at the point of *use* (in a variable/parameter type), not at declaration. It expresses "some unknown type" and comes in three forms:

- **Unbounded `<?>`** — "a list of some unknown type"; you can read elements as `Object` and call type-independent methods, but cannot add (except `null`).
- **Upper-bounded `<? extends T>`** — "some subtype of `T`." You can **read** `T` out of it, but cannot **write** into it (the compiler doesn't know the exact subtype). This is a *producer*.
- **Lower-bounded `<? super T>`** — "some supertype of `T`." You can **write** `T` into it, but reads only come back as `Object`. This is a *consumer*.

The mnemonic is **PECS — "Producer `extends`, Consumer `super`."** If a parameter *produces* values you read, use `extends`; if it *consumes* values you put in, use `super`. Wildcards exist because Java generics are **invariant**: `List<Integer>` is **not** a subtype of `List<Number>` (even though `Integer` is a `Number`). Wildcards reintroduce controlled covariance/contravariance.

```java
import java.util.List;
import java.util.ArrayList;

public class Pecs {

    // PRODUCER: we only READ Numbers out of src → use ? extends
    // Accepts List<Integer>, List<Double>, List<Number>, ...
    static double sumAll(List<? extends Number> src) {
        double total = 0;
        for (Number n : src) total += n.doubleValue();   // read as Number — OK
        // src.add(1);   // COMPILE ERROR: can't write through ? extends
        return total;
    }

    // CONSUMER: we only WRITE Integers into dst → use ? super
    // Accepts List<Integer>, List<Number>, List<Object>, ...
    static void fillWithInts(List<? super Integer> dst, int count) {
        for (int i = 0; i < count; i++) dst.add(i);      // write Integer — OK
        // Integer x = dst.get(0);   // reads come back as Object only
    }

    public static void main(String[] args) {
        List<Integer> ints = new ArrayList<>(List.of(1, 2, 3));
        System.out.println(sumAll(ints));   // 6.0   (List<Integer> as producer)

        List<Number> nums = new ArrayList<>();
        fillWithInts(nums, 3);               // List<Number> as consumer → [0,1,2]
        System.out.println(nums);
    }
}
```

| Wildcard | Meaning | Can read? | Can write? | Role (PECS) |
|---|---|---|---|---|
| `<?>` | unknown type | as `Object` | only `null` | — |
| `<? extends T>` | some subtype of `T` | as `T` | no | **Producer** |
| `<? super T>` | some supertype of `T` | as `Object` | `T` (and subtypes) | **Consumer** |

> **Contrast with C++:** C++ templates have *no* wildcards and *no* variance machinery — `vector<int>` and `vector<Number>` simply have no subtype relationship and the question never arises, because each instantiation is an independent type and you would write another template parameter instead. Java needs wildcards precisely because generics share one erased class and references are subtype-polymorphic. The C++ analogue of `<? extends Number>` is just another template parameter `template <typename T> ... requires std::derived_from<T, Number>` (with C++20 concepts).

---

## 9.6 Type Erasure — and How It Differs from C++ Instantiation

This is the defining characteristic of Java generics. The compiler uses the type parameters to **type-check** your code, then **erases** them:

- Unbounded `T` is replaced by `Object`; bounded `<T extends Number>` is replaced by its bound, `Number`.
- The compiler inserts **casts** automatically where you read generic values, so it stays type-safe at the source level.
- At runtime there is **one** class. `List<String>` and `List<Integer>` share the same `Class` object: `new ArrayList<String>().getClass() == new ArrayList<Integer>().getClass()` is `true`.

This is the polar opposite of C++ *monomorphization*, where each `vector<T>` is a distinct, fully typed, separately compiled entity. Erasure keeps Java binaries small and was chosen for backward compatibility (generics were retrofitted in Java 5 onto a pre-generics runtime), but it imposes real **consequences**:

```java
import java.util.*;

public class Erasure {
    static <T> void demo(List<T> list) {
        // 1. No reified type: you CANNOT do  T.class,  new T(),  (T) checks at runtime
        // T value = new T();          // ❌ compile error — T is not known at runtime
        // if (x instanceof T) { }     // ❌ illegal — generic type not reifiable

        // 2. No generic array creation
        // T[] arr = new T[10];        // ❌ "generic array creation"
        @SuppressWarnings("unchecked")
        T[] arr = (T[]) new Object[10];   // workaround: create Object[] and cast

        // 3. Same runtime class regardless of type argument
        List<String> a = new ArrayList<>();
        List<Integer> b = new ArrayList<>();
        System.out.println(a.getClass() == b.getClass());   // true — both ArrayList
    }

    // 4. You cannot overload on erased signatures — these "differ" only by <T>:
    // void f(List<String> s) {}
    // void f(List<Integer> i) {}   // ❌ name clash: both erase to f(List)
}
```

**Key consequences of erasure (none of which exist in C++):**

| Restriction | Why | C++ has it? |
|---|---|---|
| No `new T()` / `T.class` / `T[]` | `T` is erased; no runtime type info | C++ knows `T` exactly — all allowed |
| No primitives as type args (`List<int>`) | erased `Object` can't hold primitives | C++ `vector<int>` is fine |
| `instanceof List<String>` illegal | type arg not retained | C++ checks the concrete type |
| Can't overload methods differing only by type arg | same erased signature | C++ each instantiation distinct |
| `static` fields shared across all parameterizations | one class exists | C++ each instantiation has its own |
| Unchecked-cast warnings at raw/legacy boundaries | runtime can't verify | C++ verifies at compile time |

> **Reifiable workaround — the `Class<T>` token:** because the type is erased, APIs that *need* the runtime type pass an explicit `Class<T>` (a "type token"), e.g. `<T> T parse(String s, Class<T> type)`. This is the idiomatic Java way to recover what a C++ template would know for free.

> **Bridge methods (FYI):** to make overriding work after erasure (e.g. a `Comparable<T>.compareTo(T)` vs the erased `compareTo(Object)`), the compiler synthesizes hidden *bridge methods*. You rarely see them, but they explain occasional surprises in stack traces and reflection.

---

## 9.7 Generic Constraints in Practice

Putting bounds, wildcards, and erasure together, the canonical generic-library signature looks like this — and it is worth dissecting because it shows all three working at once. The standard `Collections.copy` is a perfect PECS example:

```java
public static <T> void copy(
        List<? super T> dest,        // CONSUMER of T  → super
        List<? extends T> src) {     // PRODUCER of T  → extends
    for (int i = 0; i < src.size(); i++) {
        dest.set(i, src.get(i));     // read from src (as T), write into dest (as T)
    }
}
```

Documenting requirements: since the bound is part of the signature (`<T extends Comparable<T>>`), the constraint is self-documenting and enforced — there is no need for the C++ habit of writing comments like `// Requires: T comparable with <`, and no risk of a cryptic deep-instantiation error. Where C++20 finally added **concepts** to formalize constraints, Java has expressed them with bounded type parameters since Java 5.

```java
// "Requires T to be orderable" — enforced by the compiler, not a comment:
static <T extends Comparable<? super T>> void sort(List<T> list) {
    list.sort(null);   // uses natural ordering
}
```

---

## 9.8 Best Practices

Effective Java generics is mostly about eliminating warnings, preferring lists to arrays (which interact badly with erasure), and applying PECS:

- **Don't use raw types** (`List` instead of `List<String>`); they exist only for pre-Java-5 compatibility and disable type checking.
- **Eliminate unchecked warnings**; when one is provably safe, suppress it narrowly with `@SuppressWarnings("unchecked")` on the smallest scope and a comment justifying it.
- **Prefer `List<T>` to `T[]`** — arrays are *covariant and reified* while generics are *invariant and erased*, so they clash (`new T[]` is forbidden; `Object[]` covariance can throw `ArrayStoreException`).
- **Use bounded wildcards for API flexibility** (PECS) on method parameters; keep return types as concrete parameterized types (don't return wildcards).
- **Favor generic methods** and let inference do the work; use the diamond operator.

```java
// ✅ Always parameterize — never use raw types
List<String> names = new ArrayList<>();      // not: List names = ...

// ✅ PECS on parameters
static <E> void addAll(Collection<? super E> dst, Collection<? extends E> src) {
    for (E e : src) dst.add(e);
}

// ✅ Prefer List to array in generic code
class Stack<E> {
    private final List<E> items = new ArrayList<>();   // not E[] items
}

// ✅ Narrow, justified suppression when a cast is provably safe
@SuppressWarnings("unchecked")
static <T> T[] toArray(Object[] src) {
    return (T[]) src;   // safe: caller guarantees the element type
}
```

---

## 9.9 What Java Generics Deliberately Lack (vs C++ Templates)

To close the C++ contrast, here is what C++ templates can do that **Java generics cannot** — by design, because generics are an *erasure-based type system*, not a *code-generation* facility:

- **No template specialization.** C++ lets you give `Printer<bool>` or `Printer<vector<T>>` a custom implementation (full and partial specialization). Java has **no specialization at all** — a generic class has exactly one implementation for every type argument. To vary behavior by type you must dispatch at runtime (e.g. `instanceof`, a `Class<T>` token, or polymorphism) rather than at compile time.
- **No template metaprogramming.** C++ templates are Turing-complete and compute types/values at compile time (traits, `if constexpr`, recursion over packs). Java generics do no compile-time computation; they only check assignability. Compile-time logic in Java is done with ordinary code, reflection, or annotation processors — not the type system.
- **No non-type (value) parameters.** C++ `template <typename T, int SIZE>` parameterizes over a *value*. Java type parameters can only be **types**; a "size" is just a constructor argument (`new Buffer(50)`), resolved at runtime.
- **No primitive type arguments.** `List<int>` is illegal; use `List<Integer>` (boxed) or primitive-specialized streams (`IntStream`) and arrays (`int[]`). C++ `vector<int>` stores raw `int`s.
- **No variadic *type* packs.** C++ `template <typename... Args>` expands a pack of types. Java's `varargs` (`T... args`) is a runtime array of one element type, not a heterogeneous compile-time type pack.
- **No reification.** As covered in 9.6, the type argument is not available at runtime (`new T()`, `T.class`, `T[]`, `instanceof T` are all impossible).

The flip side: Java generics give **smaller binaries** (one class, not N), **faster compiles**, **no template-bloat**, and far more readable error messages — and bounded type parameters/wildcards cover the vast majority of real generic-programming needs without any of the template machinery.

| C++ template feature | Java generics? | Java alternative |
|---|---|---|
| Full / partial specialization | ❌ | runtime dispatch (`instanceof`, `Class<T>`, polymorphism) |
| Template metaprogramming | ❌ | reflection / annotation processors / plain code |
| Non-type (value) parameters | ❌ | constructor arguments / `enum` |
| Primitive type arguments | ❌ | boxed wrappers / `IntStream` / `int[]` |
| Variadic type packs | ❌ | `T...` varargs (single type), heterogeneous via `Object...`/records |
| Runtime type info for `T` | ❌ (erased) | `Class<T>` type token |
| Compile-time monomorphization | ❌ | single erased class + auto-inserted casts |

---

## Summary

| Concept | Details |
|---------|---------|
| **Generic method** | `<T>` before return type; type inferred from arguments |
| **Generic class/interface** | `class Box<T>`, `interface Comparable<T>`; one erased runtime class |
| **Diamond `<>`** | Compiler infers type arguments on the right-hand side |
| **Bounded type** | `<T extends Number>`, multiple bounds with `&` |
| **Wildcards** | `<?>`, `<? extends T>`, `<? super T>` |
| **PECS** | Producer `extends`, Consumer `super` |
| **Invariance** | `List<Integer>` is **not** a `List<Number>` |
| **Type erasure** | Type params checked then removed; one class at runtime |
| **No `new T()`/`T[]`/`T.class`** | Consequence of erasure; use `Class<T>` tokens |
| **No specialization / metaprogramming / value params** | Use runtime dispatch / constructor args; no compile-time computation |

---

## Next Steps
- Write generic methods and classes; let inference and the diamond operator help
- Apply PECS with bounded wildcards in your APIs
- Remember erasure's limits — reach for `Class<T>` tokens when you need the runtime type
- Move to [Chapter 10: Collections (the Java Collections Framework)](../10_stl_containers/README.md)
