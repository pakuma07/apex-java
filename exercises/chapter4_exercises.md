# Chapter 4: References, Memory & Garbage Collection - Practice Exercises

> Java has no pointers and no pointer arithmetic. Variables of object type hold *references* (handles to objects on the heap); variables of primitive type hold *values* directly. Memory is reclaimed automatically by the garbage collector (GC) when objects become unreachable. These exercises explore those ideas.

## 1. Reference Basics

### Exercise 1.1: Reference Declaration & Usage
Create a program that:
- Declares a primitive `int x = 42` (holds a value directly)
- Declares an object reference, e.g. `int[] box = {42}` or a small holder class, that refers to a heap object
- Print the primitive value, the object's identity (`System.identityHashCode`), and the value reached through the reference
- Change the object's contents through the reference and verify both references see it

### Exercise 1.2: Array References & Iteration (no pointer arithmetic)
Write code demonstrating:
- Java has **no pointer arithmetic** — note this explicitly
- Iterate an array using index access (`arr[i]`) instead of incrementing a pointer
- Walk forward and backward through an array by index
- Compute the distance between two indices (the Java analog of "pointer difference")

### Exercise 1.3: Null and Generic References
Create a program showing:
- Null reference initialization (`String s = null;`)
- Checking for null before use to avoid `NullPointerException`
- Using `Object` as a generic reference type (the Java analog of `void*`)
- Using `Optional<T>` to model "maybe absent" instead of null
- Casting an `Object` reference back to its concrete type

## 2. Array References

### Exercise 2.1: Array as a Reference
Given array `int[] arr = {10, 20, 30, 40, 50}`:
- Access elements using index notation
- Print all elements using a loop
- Modify elements via the array reference
- Get array size using `arr.length` (no pointers needed)

### Exercise 2.2: Aliasing an Array
Write code with:
- Declare a reference to an entire array
- Create a second reference `int[] alias = arr` (both point to the same object)
- Modify through one reference and observe the change via the other
- Contrast with `int[] copy = arr.clone()` (independent copy)

### Exercise 2.3: Dynamically Sized Arrays
Create a program that:
- Allocates an array of size n with `new int[n]` (size known only at runtime)
- Takes input and fills the array
- Prints the array
- Lets the array become unreachable; explain that the GC reclaims it (no manual free)

## 3. Holder / Out-Parameter Emulation (analog of pointer-to-pointer)

### Exercise 3.1: Mutable Holder
Java has no `int**`. Emulate an out-parameter:
- Create a single-element array `int[] holder = {value}` or a small `Holder<T>` class
- Pass it to a method and have the method change `holder[0]` / `holder.value`
- Observe the change in the caller
- Print identity hash codes to show the reference is shared

### Exercise 3.2: Reference Levels
Show the levels of indirection without pointers:
- A primitive `x`
- A holder referring to a value (analog of pointer to x)
- A holder referring to another holder (analog of pointer to pointer)
- Print the values and identities reached at each level

### Exercise 3.3: Method That Swaps References
Write a method `void swap(Holder<T> a, Holder<T> b)`:
- Swaps the *contents* of the holders (since references themselves are passed by value)
- Show the difference from trying to swap two plain object parameters (won't affect the caller)
- Demonstrate a use case

## 4. References (Aliasing & Identity)

### Exercise 4.1: Reference Basics
Create a program that:
- Declare an object, e.g. `int[] x = {10}`
- Create a second reference `ref` to the same object
- Modify via `ref`
- Show that `ref` and the original observe the same object (`==` is true)
- Note: unlike a C++ reference, a Java reference variable *can* be reassigned and *can* be null

### Exercise 4.2: References vs Values
Compare both:
- Declare a primitive and an object reference
- Modify through each
- Show that reassigning a reference does not change the original object, but mutating the object does
- Explain when each behavior matters

### Exercise 4.3: Immutable / Defensive References
Write code using:
- An immutable object (e.g. `String`, or a `record`) — cannot be modified through any reference
- Efficient parameter passing (references are cheap to pass; no copying of the object)
- Use immutable types in method parameters for safety

## 5. Method Parameters

### Exercise 5.1: Pass by Value (primitives)
Create method `modify(int x)`:
- Takes a value, modifies the local copy
- Original unchanged
- Show in main(): original not modified

### Exercise 5.2: Passing References
Create two methods:
- `void swap(int[] a, int[] b)` — swaps a[0] and b[0] (holders), actually visible to caller
- `void modify(int[] x)` — modifies x[0] of the original object
- Show difference from passing primitives

### Exercise 5.3: Passing Large Objects Cheaply
Write methods with:
- `void print(String str)` — references are cheap, no copy is made
- Avoid unnecessary copying by passing references to `List`, `Map`, etc.
- Use immutable views (`List.copyOf`, `Collections.unmodifiableMap`) when read-only is intended

## 6. Functional Interfaces (analog of function pointers)

### Exercise 6.1: Functional Interface Declaration
Create methods:
- `int add(int a, int b)`
- `int subtract(int a, int b)`
- Store a reference to one as `IntBinaryOperator op = Solution::add;`
- Call through the functional interface

### Exercise 6.2: Collection of Functional Interfaces
Create a list/map of operations:
- `Map<String, IntBinaryOperator> ops = Map.of("+", Solution::add, "-", Solution::subtract, ...)`
- Call different operations via the collection
- Implement a menu system

### Exercise 6.3: Functional Interface as Parameter
Write a method:
- `int applyOperation(int a, int b, IntBinaryOperator func)`
- Pass different lambdas / method references
- Create a callback system (analog of a C function-pointer callback)

## 7. Object Allocation & Garbage Collection

### Exercise 7.1: Allocation with new (no delete)
Create a program:
- Allocate an object with `new` (e.g. `int[] a = new int[10]`, or `new Person(...)`)
- Use it
- Drop all references to it and explain that the GC will reclaim it — there is **no** `delete`
- Optionally observe `System.gc()` as a hint (not a guarantee)

### Exercise 7.2: "Memory Leaks" in Java
Write code that leaks via lingering references:
- Add objects to a `static` collection and never remove them (they stay reachable, so GC can't reclaim)
- Show detection strategies (heap dumps, profilers like VisualVM/JFR)
- Rewrite to avoid the leak: clear references, use bounded caches, or `WeakReference`
- Discuss listener/callback leaks (forgetting to unregister)

### Exercise 7.3: Reachability & Reference Types
Create:
- A strong reference vs a `java.lang.ref.WeakReference<T>` and `SoftReference<T>`
- Show that a weakly-referenced object can be collected once no strong references remain
- Build a structure of objects, then null out the root and discuss reachability
- Check for null before dereferencing reclaimed weak references

## 8. References and Classes

### Exercise 8.1: Object Field Access
Create a class `Person`:
- Allocate with `new`
- Access fields via the reference: `p.name`
- Note: Java uses `.` for both; there is no `->` and no dereference operator `(*p)`
- Show that copying the reference does not copy the object

### Exercise 8.2: Array of Objects
Create:
- An array `Person[] people`
- Note each slot starts as `null` until assigned a `new Person(...)`
- Access each person and their fields
- Modify through the references
- Print all records

### Exercise 8.3: Class with Reference Fields (Linked List)
Create a `Node` class:
- Contains a reference field `Node next`
- Create a chain of nodes
- Traverse the chain following `next` references
- Let the chain become unreachable; explain the GC reclaims the whole chain (no manual delete)

## 9. Strings and Character Arrays

### Exercise 9.1: char[] vs String
Compare:
- `char[] chars = {'H','e','l','l','o'}` — a mutable array of characters
- `String s = "Hello"` — an immutable String object (interned literal)
- Different operations on each (`new String(chars)`, `s.toCharArray()`)
- Safety considerations (Strings are immutable and bounds-checked)

### Exercise 9.2: Character Array
Create:
- `char[] str = new char[50]` — character array
- `char[] alias = str` — a second reference to the same array
- Access characters via index
- Modify the array and observe through both references

### Exercise 9.3: String Operations Implemented Manually
Write methods on `char[]` without using library helpers:
- `int length(char[] str)` — length (count chars, or use `str.length`)
- `void copy(char[] dest, char[] src)` — copy element by element
- `int compare(char[] s1, char[] s2)` — lexicographic compare
- Implement the logic yourself, then compare with `String.length()`, `System.arraycopy`, `String.compareTo`

## 10. Reference Types for Memory Management (Java 21)

### Exercise 10.1: Try-with-Resources (RAII analog)
Create a program using:
- A class implementing `AutoCloseable`
- Use it in a `try (Resource r = new Resource()) { ... }` block
- Show deterministic cleanup via `close()` when the block exits — the analog of C++ RAII / `unique_ptr`
- No manual cleanup call needed

### Exercise 10.2: Shared References & Reachability
Create a program with:
- One object referenced by `ref1` and `ref2` (multiple owners)
- Show both observe the same object (`ref1 == ref2`)
- Drop one reference; the object stays alive while any strong reference remains
- Explain that the GC reclaims it only when *all* references are gone (analog of `shared_ptr` reaching count 0)

### Exercise 10.3: Reference Types with Classes
Create:
- `Person p1 = new Person("Alice", 25)`
- An array/list of `Person` references
- Automatic memory management — let unreachable objects be collected
- Compare `WeakReference<Person>` vs strong reference behavior for caches

## 11. Pitfalls & Best Practices

### Exercise 11.1: Use-After-Close (dangling analog)
Demonstrate the problem:
- Use a resource after calling `close()` on it (e.g. a closed `Scanner`/stream) → exception
- Prevent by scoping with try-with-resources
- Rewrite so the resource cannot be used after closing
- Note: Java cannot "use freed memory" the way C++ dangling pointers can — explain why (no manual free, GC keeps reachable objects valid)

### Exercise 11.2: NullPointerException & Bounds
Show risks and fixes:
- Dereferencing a null reference → `NullPointerException`
- Out-of-bounds array access → `ArrayIndexOutOfBoundsException` (checked, not silent corruption)
- Uninitialized fields default to null/0 (no random garbage)
- Best practices: null checks, `Optional`, `Objects.requireNonNull`, defensive bounds checks

### Exercise 11.3: Resource Management
Create a class that:
- Holds a resource via a raw field and forgets to close it (show the problem)
- Implements `AutoCloseable` and is used with try-with-resources (show the benefit)
- Discuss the RAII principle and its Java equivalent
- Use `WeakReference` where appropriate to avoid retaining objects too long

## Challenge Problems

### Challenge 12.1: Circular Linked List
Create:
- A `Node` class with a reference field `next`
- The last node's `next` points back to the first
- Insert operation
- Traverse and print (careful: stop after one loop)
- Let it become unreachable; note the GC reclaims cycles too (it uses reachability, not reference counting)

### Challenge 12.2: Reference-Based Sorting
Implement:
- Sort an array of references (e.g. `Integer[]` or `Person[]`)
- Without copying the underlying objects — only the references move
- Compare what the references point to (use a `Comparator`)
- Demonstrate a use case (sorting by a field while objects stay in place elsewhere)

### Challenge 12.3: Binary Search Tree
Create a `Node` class:
- Reference to `left` child
- Reference to `right` child
- Insert values
- In-order traversal
- Search operation
- Let the tree become unreachable; the GC reclaims all nodes (no manual delete)

---

## Tips for Solving

1. **Think in references**: a variable holds a handle to an object, not the object itself
2. **Primitives vs objects**: primitives copy by value; references copy the handle (shared object)
3. **Check for null**: before dereferencing, to avoid `NullPointerException`
4. **No manual free**: rely on GC; drop references so objects become unreachable
5. **Deterministic cleanup**: use try-with-resources / `AutoCloseable` for non-memory resources
6. **Watch lingering references**: static collections, listeners, and caches can prevent GC
7. **Use tools**: VisualVM, Java Flight Recorder, heap dumps

## Difficulty Levels
- **🟢 Easy**: Exercises 1.1, 2.1, 4.1, 4.2, 5.1, 6.1, 7.1, 9.1
- **🟡 Medium**: Exercises 1.2-1.3, 2.2, 3.1, 4.3, 5.2-5.3, 6.2-6.3, 7.2-7.3, 8.1, 9.2-9.3, 10.1-10.2, 11.1-11.2
- **🔴 Hard**: Exercises 2.3, 3.2-3.3, 8.2-8.3, 10.3, 11.3, 🏆 Challenge 12.1-12.3

---

## Common Mistakes to Avoid

1. **Dereferencing null references** — always check first (`NullPointerException`)
2. **Holding references too long** — lingering references prevent GC (memory "leak")
3. **Forgetting to unregister listeners** — a classic source of leaks
4. **Using a resource after close()** — throws; scope with try-with-resources
5. **Assuming `System.gc()` frees memory now** — it's only a hint
6. **Confusing `==` with `equals()`** — `==` compares references, `equals()` compares contents
7. **Out-of-bounds index** — checked at runtime (`ArrayIndexOutOfBoundsException`), so validate indices

---

## Helpful Debugging Checklist

- [ ] Is the reference initialized (not null)?
- [ ] Could this throw a `NullPointerException`?
- [ ] Is the object still reachable when you expect it to be?
- [ ] Are you holding references longer than necessary (static collections, caches)?
- [ ] Are non-memory resources closed (try-with-resources / `AutoCloseable`)?
- [ ] Any `ArrayIndexOutOfBoundsException`?
- [ ] Should this be a `WeakReference`/`SoftReference`?
- [ ] Are you using `equals()` vs `==` correctly?

## Java 21 Exercise Example: Method Overloading

```java
public class Solution {
    static int add(int a, int b) { return a + b; }
    static double add(double a, double b) { return a + b; }
}
```

Compile and run:
```
javac Solution.java
java Solution
```
