# Chapter 12: Memory, References & Resource Management - Exercises

## Section 1: Object Allocation & GC 🟢

1. Allocate an object with `new`; let the garbage collector reclaim it (no manual `delete` in Java)

2. Create an array with `new T[n]`; note arrays are reclaimed by the GC, not freed manually

3. Explore reference vs value semantics; note Java has no pointer arithmetic

## Section 2: Single Ownership (references) 🟡

4. Model single ownership with a plain reference; "transfer" by reassigning and nulling the old reference (note: Java has no move semantics)

5. Use object references in a collection

6. Access object members through a reference (`.` operator); compare with C++ `->`

## Section 3: Shared References 🟡

7. Create multiple references to one object and share it

8. Discuss reachability: the GC frees an object when it is unreachable (Java exposes no reference count, unlike `shared_ptr::use_count`)

9. Use the same object reference in multiple containers

## Section 4: Construction Helpers 🟡

10. Use `new` directly (Java's analog to `make_unique`)

11. Use a factory method for construction (analog to `make_shared`)

12. Compare direct `new` vs factory methods for readability and reuse

## Section 5: Weak References 🟡

13. Create a `java.lang.ref.WeakReference` so the referent can be collected

14. Demonstrate that Java's tracing GC reclaims **circular references** automatically -- circular references are NOT a leak in Java (key difference from C++ `shared_ptr`); use `WeakReference` for cache/observer scenarios instead

15. Retrieve the referent with `WeakReference.get()` (the analog of `shared_ptr` `lock()`), handling a possible `null`

## Section 6: Resource Management (AutoCloseable) 🟡

16. Implement a custom resource class that implements `AutoCloseable`

17. Create a file handle with automatic cleanup via try-with-resources

18. Design a resource-managing class with deterministic `close()`

## Section 7: Immutability & Object Reuse 🟡

19. Reframe a "move constructor" as returning a new immutable instance (Java has no move semantics)

20. Reframe "move assignment" as reassigning a reference; discuss why this differs from C++

21. Demonstrate efficient resource handover by transferring a reference and nulling the source

## Section 8: References with Polymorphism 🔴

22. Use a `Base` reference holding a subclass instance

23. Create a `List<Base>` containing subclass instances

24. Show that no "virtual destructor" is needed -- the GC plus `close()` handle cleanup

## Section 9: Custom Cleanup 🔴

25. Implement custom cleanup logic in `AutoCloseable.close()` 🏆

26. Use `java.lang.ref.Cleaner` to register cleanup actions (analog of a custom deleter)

27. Compare cleanup for array-backed vs single-object resources

## Section 10: Memory Safety Practices 🔴

28. Identify and fix leaks from lingering references (static collections, unremoved listeners)

29. Demonstrate exception-safe cleanup with try-with-resources and `finally`

30. Create a leak-proof resource management system

---

## Tips for Success

- **References**: Single ownership is by convention, not enforced
- **Reachability**: The GC frees objects once unreachable
- **WeakReference**: Allows the referent to be collected; use for caches
- **Factory methods**: Useful for readable, reusable construction
- **No move semantics**: Reassign references or return new instances
- **AutoCloseable + try-with-resources**: Deterministic resource cleanup
- **No manual delete**: The GC reclaims memory; you manage resources, not memory
- **Tracing GC**: Handles cyclic references automatically

## Difficulty Summary

- **Easy (🟢)**: 3 exercises - Allocation and GC basics
- **Medium (🟡)**: 18 exercises - References, weak references, AutoCloseable, immutability
- **Hard (🔴)**: 9 exercises - Polymorphism, custom cleanup, safety

## Challenge Problems 🏆

- **Challenge 1**: Custom cleanup for complex resources (`Cleaner` / `close()`)
- **Challenge 2**: Understanding why circular references are safe under tracing GC
- **Challenge 3**: Exception-safe resource management

## Expected Time Commitment

- Easy: 10-15 minutes per exercise
- Medium: 20-40 minutes per exercise
- Hard: 30-60 minutes per exercise
- Total: 8-15 hours for all exercises

## Common Pitfalls to Avoid

- Leaks from lingering references (static collections, caches, listeners)
- Forgetting to close resources (use try-with-resources)
- Relying on finalizers (deprecated; prefer `Cleaner`/`AutoCloseable`)
- Holding a strong reference when a `WeakReference` is appropriate
- Assuming `close()` runs without try-with-resources or `finally`
- Exception paths skipping cleanup
- Resource leaks from exceptions

## Learning Outcomes

After completing these exercises, you will:
✓ Eliminate memory leaks by managing reachability
✓ Understand reference-based ownership models
✓ Implement deterministic cleanup with `AutoCloseable`
✓ Understand why Java has no move semantics (and what to do instead)
✓ Use `WeakReference` for caches and observers
✓ Know that tracing GC reclaims circular references safely
✓ Design resource-managing classes
✓ Work with polymorphism safely (no virtual destructors needed)
✓ Use `Cleaner` for custom cleanup
✓ Write exception-safe code with try-with-resources

## Java 21 Exercise Example: AutoCloseable & try-with-resources

```java
public class Solution {
    static final class Node implements AutoCloseable {
        final int v;
        Node(int x) { this.v = x; }

        @Override
        public void close() {
            System.out.println("releasing node " + v);
        }
    }

    static Node makeNode(int x) {
        return new Node(x); // GC reclaims memory; close() handles resources
    }

    public static void main(String[] args) {
        try (Node n = makeNode(42)) {
            System.out.println("using node " + n.v);
        } // close() runs automatically, even on exception
    }
}
```

Compile and run with:

```
javac Solution.java
java Solution
```
