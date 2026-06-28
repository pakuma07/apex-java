# Chapter 4: References & Memory

In C++ you reach for an object indirectly with two tools: a **pointer** (a variable holding a memory address) and a **reference** (an alias for an existing object). Java deliberately removes both as language-visible features. There are **no pointers** in Java — you cannot take an address, you cannot do pointer arithmetic, and there is no `*` or `&` operator. Instead, Java gives you exactly one form of indirection: the **reference**, and it is the *only* way you ever touch an object. Understanding that single fact — *every object variable is a reference to a heap object, and references are passed by value* — explains almost all of Java's behavior around assignment, parameter passing, equality, and `null`.

This chapter mirrors the C++ pointers-and-references chapter section by section, but each C++ pointer idea is mapped to its Java reality. We cover how Java lays out memory (objects on the heap, primitives inline), reference vs value semantics, `null` and `NullPointerException`, `final` references, identity vs equality (`==` vs `equals`), passing references to methods, and an introduction to garbage collection — the feature that replaces C++'s manual `new`/`delete` and smart pointers entirely.

> **Mapping at a glance.** Wherever C++ writes `T* p = &obj;` and dereferences with `*p`, Java writes `T r = obj;` and uses `r` directly. The C++ "address" is hidden; the C++ "dereference" is automatic. Java's reference is closest to a C++ **non-null-or-null pointer that you never dereference manually** — it can be `null` (like a pointer) but is used with `.` syntax (like a reference).

## 4.1 Reference Basics — There Is No Address Operator

A C++ program can ask for a variable's address with `&` and inspect its size with `sizeof`. Java exposes neither: memory addresses are managed by the JVM and are intentionally inaccessible, so that the garbage collector is free to move objects around. What Java *does* have is a clear split between **primitive types** (stored as raw values) and **reference types** (variables that refer to objects).

### Understanding Java Memory

```java
public class MemoryBasics {
    public static void main(String[] args) {
        int x = 10;                 // primitive: the variable IS the value

        System.out.println("Value: " + x);   // 10
        // System.out.println(&x);            // NO address-of operator in Java
        // sizeof(x)                          // NO sizeof in Java

        // A primitive int is always 32 bits by the Java Language Spec,
        // but you cannot query a variable's address or byte size at runtime.
        System.out.println("Bits in an int: " + Integer.SIZE);   // 32
        System.out.println("Bytes in an int: " + Integer.BYTES); // 4
    }
}
```

**C++ → Java mapping**

| C++ | Java |
|---|---|
| `&x` (address-of) | No equivalent — addresses are hidden |
| `sizeof(x)` | No equivalent for variables; `Integer.BYTES`, `Long.BYTES`, etc. give the spec size of a type |
| Pointer holds an address | A reference holds an opaque handle to a heap object |

### Reference Types vs Primitive Types

This is the central distinction in Java. There are exactly **eight primitive types** (`byte`, `short`, `int`, `long`, `float`, `double`, `char`, `boolean`); everything else — every class, array, and interface type — is a **reference type**.

```java
// Primitive: the variable directly contains the value (no indirection)
int a = 5;
int b = a;        // b is an independent COPY of the value 5
b = 99;
System.out.println(a);   // 5  -- a is untouched

// Reference: the variable contains a reference to an object on the heap
int[] arr1 = {1, 2, 3};   // arr1 refers to a heap array
int[] arr2 = arr1;        // arr2 refers to the SAME heap array (no copy)
arr2[0] = 99;
System.out.println(arr1[0]);   // 99  -- both names see the change
```

```
   PRIMITIVE (value semantics)          REFERENCE (reference semantics)

   stack                                stack            heap
  +-------+                            +--------+       +-----------+
  | a = 5 |                            | arr1 --+-----> | [99,2,3]  |
  +-------+                            +--------+    /  +-----------+
  | b =99 |  (independent copy)        | arr2 --+---/
  +-------+                            +--------+   (both refer to one object)
```

There is no Java declaration like C++'s `int* ptr` or `int& ref`. Where C++ chooses between a value, a pointer, and a reference, Java's choice is made *by the type*: primitives are always values, objects are always references.

## 4.2 No Pointer Arithmetic

C++ pointer arithmetic (`ptr + 1` advancing by `sizeof(T)` bytes, walking an array with `p++`) simply does not exist in Java. Because references are opaque handles, not numeric addresses, there is nothing to add to. Array traversal is done with **indices** or the **enhanced `for` loop**, and the JVM bounds-checks every access.

```java
int[] arr = {10, 20, 30, 40, 50};

// C++ would walk this with a pointer: for (int* p = arr; p < arr+5; p++)
// Java walks it with an index — and every access is bounds-checked:
for (int i = 0; i < arr.length; i++) {
    System.out.print(arr[i] + " ");      // 10 20 30 40 50
}

// Or, idiomatically, with the enhanced for-loop (no index needed):
for (int value : arr) {
    System.out.print(value + " ");       // 10 20 30 40 50
}

// Out-of-bounds is a checked, throwing error — NOT undefined behavior:
// int bad = arr[5];   // throws ArrayIndexOutOfBoundsException at runtime
```

**Key difference from C++:** reading past the end of a C++ array is *undefined behavior* (a silent bug or crash). In Java the same mistake throws `ArrayIndexOutOfBoundsException` deterministically. Safety is guaranteed, at the cost of the raw-pointer speed C++ offers.

## 4.3 References and Arrays

In C++ an array name "decays" into a pointer to its first element, and `arr[i]` is literally `*(arr + i)`. Java arrays are **first-class objects**: an array variable is a reference to an array object that *knows its own length* (`arr.length`) and never decays into anything.

```java
int[] arr = {10, 20, 30, 40, 50};

// arr is a reference to a heap array object.
System.out.println(arr.length);   // 5  -- the array knows its size (C++ pointer does not)
System.out.println(arr[2]);       // 30 -- normal indexing; no *(arr+2)

// "Array of references" — the Java analogue of C++'s int* ptrs[]
String[] names = {"Alice", "Bob", "Carol"};   // each element is a reference to a String
for (String n : names) {
    System.out.print(n + " ");                  // Alice Bob Carol
}

// Assigning one array variable to another copies the REFERENCE, not the data:
int[] alias = arr;
alias[0] = 999;
System.out.println(arr[0]);       // 999 -- same underlying array
```

**C++ → Java mapping**

| C++ | Java |
|---|---|
| Array name decays to `T*` | Array variable is a reference; never decays; carries `.length` |
| `arr[i]` ≡ `*(arr + i)` | `arr[i]` only; no pointer form, no `i[arr]` trick |
| `sizeof(arr)/sizeof(arr[0])` for length | `arr.length` (a field on the array object) |
| `int* ptrs[]` (array of pointers) | `String[] names` (array of references) |

Multidimensional and jagged arrays are covered in Chapter 5.

## 4.4 Heap Allocation with `new` (No `delete`)

C++ splits allocation between the **stack** (automatic, scoped) and the **heap** (`new`/`delete`, manual). Java keeps a similar split but removes the manual half entirely:

- **Primitives** declared as local variables live on the stack (or inline inside their containing object).
- **Every object** — created with `new` — lives on the **heap**.
- There is **no `delete`**. The garbage collector reclaims objects automatically once they become unreachable.

```java
class Person {
    String name;
    Person(String name) { this.name = name; }
}

public class HeapDemo {
    public static void main(String[] args) {
        // Stack: a primitive local variable
        int x = 5;

        // Heap: every object is allocated with `new`
        Person p = new Person("Alice");   // object on heap; p is a reference to it
        System.out.println(p.name);       // Alice

        // No delete. When `p` (and any other reference) stops pointing here,
        // the object becomes eligible for garbage collection automatically.
        p = null;        // we drop our reference; GC may reclaim the Person later
    }
}
```

### Why There Are No Memory Leaks of the C++ Kind (and What Can Still Leak)

In C++ a forgotten `delete`, an overwritten pointer, or an exception thrown between `new` and `delete` leaks memory. Java's garbage collector eliminates all three: as long as an object is **reachable** from a live reference it survives, and the moment it becomes unreachable it is eligible for collection — even if an exception just unwound the stack.

```java
// C++ "memory leak: forgot to delete" has NO Java equivalent for this pattern:
void noLeakInJava() {
    int[] data = new int[1_000_000];   // allocated on the heap
    // ... use data ...
}   // when the method returns, `data` goes out of scope;
    // the array becomes unreachable and the GC reclaims it. No delete needed.
```

Java still has a *logical* leak hazard: if you keep a reference alive in a long-lived container (a static list, a cache, a map) the object stays reachable and is never collected. The cure is to drop the reference (remove it from the collection or set the field to `null`), not to call a `delete`.

```java
// ❌ Logical leak: objects piling up in a static collection are always reachable
static final List<byte[]> CACHE = new ArrayList<>();
void grow() { CACHE.add(new byte[1_000_000]); }   // never removed -> never collected

// ✅ Remove entries you no longer need so they become unreachable
void release(int index) { CACHE.remove(index); }
```

## 4.5 Memory Management: GC Instead of Smart Pointers

C++11 introduced smart pointers — `unique_ptr`, `shared_ptr`, `weak_ptr` — to automate `delete` via RAII and reference counting. **Java needs none of these as language types**, because the garbage collector handles object lifetime globally. It is still worth mapping the concepts, because the *ownership intent* they express has Java analogues.

### `unique_ptr` → just a plain reference

`unique_ptr` models a single owner that frees the object when it goes out of scope. In Java a plain reference already gives you this: when no reference remains, the GC reclaims the object. Java has nothing to "move," and assigning a reference does not null out the source.

```java
// C++:  unique_ptr<int[]> arr(new int[10]);   // sole owner, auto-freed
// Java: just hold a reference; GC frees it when unreachable
int[] arr = new int[10];
arr[0] = 5;
// no delete[], no move, no reset — dropping the reference is enough
```

### `shared_ptr` → ordinary shared references (no manual count)

`shared_ptr` keeps an atomic reference count and deletes the object at zero. The JVM effectively does reference *reachability* tracking for **every** object, so multiple references sharing one object is the normal, free case.

```java
class Node {
    Node next;     // a reference to another Node (like shared_ptr<Node> next)
    Node prev;     // see weak-reference note below
}

Node a = new Node();
Node b = a;        // two references to the same object — no use_count() needed
// When BOTH a and b stop referring to it, it becomes collectible.
```

### `weak_ptr` → `java.lang.ref.WeakReference`

C++'s `weak_ptr` observes an object without keeping it alive, breaking reference *cycles* that would otherwise leak under reference counting. Java's tracing GC already collects cycles correctly, so you rarely need this. When you *do* want a reference that does not prevent collection (caches, listener registries), use `java.lang.ref.WeakReference`.

```java
import java.lang.ref.WeakReference;

Object big = new byte[1_000_000];
WeakReference<Object> weak = new WeakReference<>(big);   // does not keep `big` alive

System.out.println(weak.get() != null);  // true while `big` is still strongly reachable
big = null;                               // drop the strong reference
// After a GC cycle, weak.get() may return null because nothing keeps it alive.
```

**C++ → Java mapping**

| C++ tool | Purpose | Java reality |
|---|---|---|
| `new` / `delete` | Manual allocate/free | `new` allocates; GC frees — no `delete` |
| `unique_ptr<T>` | Sole ownership, auto-free | A plain reference; GC frees when unreachable |
| `shared_ptr<T>` | Shared ownership via ref count | Multiple references; GC tracks reachability globally |
| `weak_ptr<T>` | Non-owning, breaks cycles | `WeakReference<T>`; GC already collects cycles |
| RAII destructor for memory | Free in destructor | Not needed for memory; use `try`-with-resources for *other* resources (Ch. 12/14) |

## 4.6 `null` and `NullPointerException`

A C++ pointer can be null (`nullptr`); a C++ reference cannot. A Java reference is closer to a *pointer that you never dereference manually*: it **can** be `null`, meaning "refers to no object." Calling a method or accessing a field through a `null` reference throws `NullPointerException` (NPE) — Java's checked, deterministic equivalent of C++'s undefined behavior when you dereference a null pointer.

```java
String s = null;                 // s refers to no object (like C++ T* p = nullptr;)
// System.out.println(s.length());  // throws NullPointerException at runtime

// Guard before use — the Java analogue of `if (ptr != nullptr)`
if (s != null) {
    System.out.println(s.length());
}

// Helpful NPE messages (Java 14+) tell you exactly which reference was null:
// "Cannot invoke "String.length()" because "s" is null"
```

Modern Java offers tools to make the *absence of a value* explicit instead of relying on bare `null`:

```java
import java.util.Objects;
import java.util.Optional;

// 1. Fail fast / supply defaults with java.util.Objects
String name = Objects.requireNonNull(maybeName, "name must not be null");
String safe = Objects.requireNonNullElse(maybeName, "(unknown)");

// 2. Model "might be absent" in the type system with Optional<T>
Optional<String> found = lookup(id);          // method returns Optional, not null
String result = found.orElse("default");       // no NPE possible
found.ifPresent(v -> System.out.println(v));
```

**C++ → Java mapping**

| C++ | Java |
|---|---|
| `nullptr` | `null` |
| Dereferencing a null pointer → **undefined behavior** | Using a null reference → **`NullPointerException`** (deterministic) |
| References cannot be null | References *can* be null (so check, or use `Optional`) |
| `if (p != nullptr)` | `if (r != null)` |

## 4.7 `final` References

C++ has three independent `const` flavors for pointers: pointer-to-const (`const int*`), const-pointer (`int* const`), and const-pointer-to-const (`const int* const`). Java's `final` corresponds to **only one** of these: the *const pointer* (`int* const`). `final` makes the **reference** unchangeable — it can never be reseated to another object — but it says **nothing** about whether the referred-to object can be mutated.

```java
final int[] arr = {1, 2, 3};
arr[0] = 99;            // OK — the OBJECT is mutable; final only locks the reference
System.out.println(arr[0]);   // 99
// arr = new int[5];    // COMPILE ERROR — cannot reassign a final reference

final int CONSTANT = 42;   // for a primitive, final makes the value itself constant
// CONSTANT = 43;          // COMPILE ERROR
```

To get C++'s `const int*` behavior (read-only *contents*), you do not use `final` — you use an **immutable type** instead (e.g. `String`, `record`, `List.copyOf(...)`, or your own class with no setters).

```java
import java.util.List;

final List<Integer> nums = List.copyOf(List.of(1, 2, 3));  // immutable list
// nums.add(4);   // throws UnsupportedOperationException — contents are read-only
// nums = ...     // also illegal: final reference
```

**C++ → Java mapping**

| C++ | Meaning | Java equivalent |
|---|---|---|
| `int* const p` | Reference fixed, object mutable | `final` reference |
| `const int* p` | Object read-only, reference reassignable | Use an immutable type (no `final` keyword for this) |
| `const int* const p` | Both fixed | `final` reference **to** an immutable type |

## 4.8 Identity vs Equality: `==` vs `equals`

This is one of the most consequential differences from C++. In Java:

- `==` on **reference types** compares **identity** — do the two references point to the *same* object? (This is exactly comparing the two C++ pointer values.)
- `equals(...)` compares **logical equality** — do the two objects have the *same content*?
- `==` on **primitives** compares values directly (no objects involved).

```java
String a = new String("hello");
String b = new String("hello");

System.out.println(a == b);        // false — two distinct objects (different identity)
System.out.println(a.equals(b));   // true  — same characters (logical equality)

// Primitives: == is the only comparison, and it compares values
int x = 5, y = 5;
System.out.println(x == y);        // true
```

A classic trap is the **String pool**: identical string *literals* are interned to the same object, so `==` may *accidentally* return `true`. Never rely on it — always use `equals` for content.

```java
String p = "hello";   // literal -> interned in the string pool
String q = "hello";   // same pooled object
System.out.println(p == q);        // true  (same interned object — DO NOT rely on this)
System.out.println(p.equals(q));   // true  (the correct comparison)
```

When you write your own class, override `equals` and `hashCode` together so logical equality works in collections (covered in Chapter 6). `records` (Chapter 6) generate both for you automatically.

**C++ → Java mapping**

| C++ | Java |
|---|---|
| `p1 == p2` (pointer compare) | `r1 == r2` (identity compare) |
| `*p1 == *p2` / `obj1 == obj2` with overloaded `operator==` | `r1.equals(r2)` (logical equality) |
| Operator overloading defines `==` for a type | No operator overloading — you override `equals` instead |

## 4.9 Passing References to Methods

C++ lets you pass by value, by pointer, or by reference, and pass-by-reference (or pointer) lets a function modify the caller's variable. **Java is always pass-by-value** — but the subtlety is *what* the value is:

- For a **primitive**, the value is the data itself, so the method gets an independent copy and cannot affect the caller's variable.
- For an **object**, the value is the **reference** (the handle). The method gets a *copy of the reference*, which points at the *same* object — so it **can mutate that shared object**, but it **cannot** make the caller's variable point somewhere else.

```java
static void tryToChangePrimitive(int n) {
    n = 99;                     // changes only the local copy
}

static void mutateObject(int[] arr) {
    arr[0] = 99;                // mutates the SHARED object — visible to caller
}

static void tryToReseat(int[] arr) {
    arr = new int[]{7, 8, 9};   // reassigns only the local copy of the reference
}                               // caller's variable still points to the old array

public static void main(String[] args) {
    int num = 5;
    tryToChangePrimitive(num);
    System.out.println(num);          // 5  -- unchanged (pass-by-value of the int)

    int[] data = {1, 2, 3};
    mutateObject(data);
    System.out.println(data[0]);      // 99 -- the shared object was mutated

    tryToReseat(data);
    System.out.println(data[0]);      // 99 -- still the old array; reseat didn't escape
}
```

```
   mutateObject(data)                      tryToReseat(data)

   caller.data --+                         caller.data --+--> [99,2,3]   (unchanged)
                 +--> [99,2,3]  (mutated)  param arr  ---+--> [7,8,9]    (local only)
   param arr  ---+                                       (new object, discarded on return)
```

**C++ → Java mapping**

| C++ | Java |
|---|---|
| `void f(int x)` (by value) | Same: primitive copied; caller unaffected |
| `void f(int& x)` / `void f(int* x)` to mutate caller's variable | **No equivalent** — Java cannot reseat a caller's variable |
| `void f(SomeObj& o)` to mutate the object | Pass the reference; the method mutates the shared object (but cannot reseat the caller's variable) |
| Output parameters via reference/pointer | Return a value, return a record/array of results, or mutate a passed object |

There is **no** way in Java to write C++'s `void increment(int& x)` so that a plain `int` argument changes in the caller. You either return the new value (`num = increment(num);`) or wrap the value in an object/array and mutate that.

## 4.10 Garbage Collection: An Introduction

Java's garbage collector (GC) is the runtime subsystem that automatically reclaims heap memory occupied by objects that are no longer reachable. It is the feature that lets Java drop `delete`, `unique_ptr`, and `shared_ptr` altogether.

**Core model — reachability.** An object is *live* if it can be reached by following references starting from a set of **GC roots** (local variables on the stack of running threads, static fields, JNI references, etc.). Anything not reachable from a root is *garbage* and may be collected. Because the GC traces references, it correctly reclaims **cycles** (objects that refer to each other) — the very situation that defeats C++ `shared_ptr` reference counting.

```
GC roots ──> objects ──> objects        ⟲ cycle (a <-> b) with no root path in:
   |                                        a ──> b
   v                                        ^     |
 [reachable: kept]                          +-----+   (unreachable -> collected anyway)

 [unreachable subgraph: collected]
```

A few practical points:

- **Non-deterministic timing.** You do not control *when* collection happens. `System.gc()` is only a *hint* and is usually best left unused.
- **`finalize()` is dead.** The old `Object.finalize()` hook is deprecated for removal and must not be used. For cleanup of *non-memory* resources (files, sockets, locks) use `try`-with-resources / `AutoCloseable` (Chapters 12 and 14), which is Java's RAII analogue.
- **Generational, often concurrent.** Modern JVMs (G1, ZGC, Shenandoah) split the heap into generations and do most work concurrently with the application to keep pauses short. You don't manage any of this manually.
- **Reachability-based leaks remain.** The GC cannot reclaim something you still reference. Unbounded caches, static collections, and forgotten listeners are the Java equivalent of leaks — fixed by removing references, not by freeing memory.

**C++ → Java mapping**

| C++ | Java |
|---|---|
| `delete` / `delete[]` | Automatic GC — none needed |
| RAII destructor frees memory at scope end | GC frees memory at some later point after unreachability |
| RAII destructor frees *files/locks* deterministically | `try`-with-resources + `AutoCloseable.close()` |
| Reference-counting cycles leak (`shared_ptr`) | Tracing GC reclaims cycles correctly |
| Dangling pointer after `delete` | Cannot happen — a live reference keeps the object alive |

## 4.11 Best Practices

The guiding principle in Java is the opposite of C++'s: you do **not** manage memory, so spend your attention on *nullability*, *identity vs equality*, and *immutability* instead.

```java
// ✅ Prefer Optional or null-checks over assuming non-null
Optional<User> findUser(int id) { /* ... */ return Optional.empty(); }

// ✅ Compare object content with equals, identity only when you truly mean "same object"
if (a.equals(b)) { /* logical equality */ }
if (a == b)      { /* the SAME object — rarely what you want for value types */ }

// ✅ Use final for references that should never be reseated (intent + safety)
final List<Integer> ids = new ArrayList<>();
ids.add(1);                   // mutating contents is fine
// ids = new ArrayList<>();   // reseating is forbidden

// ✅ Prefer immutable types to get C++ "const object" semantics
record Point(int x, int y) {}             // fully immutable value type
List<Integer> readOnly = List.copyOf(ids); // unmodifiable view/copy

// ✅ Don't reach for WeakReference unless you have a real cache/listener cycle concern
// ✅ Don't call System.gc(); let the JVM decide
// ✅ Release references you no longer need (remove from collections, null out fields)

// ❌ Don't rely on == for Strings or boxed values
Integer m = 1000, n = 1000;
// if (m == n) ...   // may be false! identity, not value — use m.equals(n) or m.intValue()
```

## Summary

| Topic | Key Points |
|-------|-----------|
| **Pointers** | Do not exist in Java — no `&`, no `*`, no pointer arithmetic |
| **References** | The only form of indirection; opaque handle to a heap object; can be `null` |
| **Primitives vs references** | 8 primitives hold values directly; all other types are references |
| **`null`** | Like a null pointer; misuse throws `NullPointerException` (not UB) |
| **`final`** | Locks the reference (≈ `int* const`), not the object; use immutable types for `const int*` |
| **`==` vs `equals`** | `==` = identity (same object); `equals` = logical equality |
| **Passing to methods** | Always pass-by-value: a copy of the reference; can mutate the object, cannot reseat the caller's variable |
| **Memory management** | Garbage collection replaces `new`/`delete` and all smart pointers; collects cycles |

## Next Steps
- Internalize "everything is a reference; references are passed by value"
- Use `equals`/`hashCode` for content comparison; reserve `==` for identity
- Model absence with `Optional`, and guard `null` defensively
- Move to [Chapter 5: Arrays & Strings](../05_arrays_strings/README.md)
