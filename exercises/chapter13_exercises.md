# Chapter 13: Exception Handling - Exercises

## Section 1: Try-Catch Basics 🟢

1. Write a try-catch for division by zero (`ArithmeticException`)

2. Create a try-catch for array out of bounds (`ArrayIndexOutOfBoundsException`)

3. Handle multiple exception types in catch blocks

## Section 2: Exception Hierarchy 🟡

4. Catch specific vs generic exceptions (`Throwable` / `Exception` / `RuntimeException`)

5. Use standard exception types (`IllegalArgumentException`, `IllegalStateException`, `RuntimeException`)

6. Design an exception class hierarchy for an application

## Section 3: Custom Exceptions 🟡

7. Create a custom exception extending `Exception` (checked) or `RuntimeException` (unchecked)

8. Provide error messages via the message constructor and `getMessage()` (override if needed)

9. Create a domain-specific exception hierarchy

## Section 4: Multiple Catch Blocks 🟡

10. Catch different exception types in sequence

11. Ensure specific catches come before generic catches (ordering matters)

12. Use multi-catch `catch (A | B e)` for shared handling

## Section 5: Re-throwing Exceptions 🟡

13. Catch and re-throw to propagate an exception (`throw e;`)

14. Add context information while re-throwing (wrap with a cause)

15. Implement exception chaining (`new XException("ctx", e)`, retrieve via `getCause()`)

## Section 6: Cleanup with Exceptions 🟡

16. Ensure cleanup happens with exceptions using `finally`

17. Use try-with-resources for automatic resource release

18. Design transaction-like behavior (commit/rollback) with try-with-resources / `finally`

## Section 7: Checked vs Unchecked Exceptions 🟡

19. Mark methods with a `throws` clause for checked exceptions (Java has no `noexcept`)

20. Write a method that declares no checked exceptions (unchecked-only)

21. Design classes that distinguish checked from unchecked exceptions appropriately

## Section 8: Constructor Exception Safety 🔴

22. Handle exceptions during constructor initialization

23. Create an exception-safe constructor that leaves no partially-constructed resource leak

24. Demonstrate a strong guarantee (object either fully constructed or not at all)

## Section 9: Exceptions from close() 🔴

25. Handle exceptions thrown from `AutoCloseable.close()` (analog of "destructors never throw") 🏆

26. Handle exceptions safely in cleanup code and inspect suppressed exceptions via `Throwable.getSuppressed()`

27. Design exception-safe resource management with try-with-resources

## Section 10: Advanced Exception Handling 🔴

28. Design an exception handling strategy for a system

29. Create an exception handling wrapper for functions 🏆

30. Implement logging with exception context (e.g. `java.util.logging`)

---

## Tips for Success

- **Specific before generic**: Order matters in catch blocks
- **getMessage()**: Provide meaningful error messages
- **Checked vs unchecked**: Use `throws` for checked; Java has no `noexcept`
- **close() should avoid throwing**: Prefer suppressing/logging in cleanup
- **try-with-resources / finally**: Resources cleaned even with exceptions
- **Re-throw**: Use `throw e;` or wrap with a cause for chaining
- **Exception guarantee**: Strong (rollback), basic, or nothrow
- **Standard exceptions**: Reuse JDK types where possible

## Difficulty Summary

- **Easy (🟢)**: 3 exercises - Basic try-catch, handling
- **Medium (🟡)**: 18 exercises - Custom exceptions, propagation, cleanup
- **Hard (🔴)**: 9 exercises - Safety guarantees, advanced patterns

## Challenge Problems 🏆

- **Challenge 1**: Safe `close()` with cleanup and suppressed exceptions
- **Challenge 2**: Exception-safe resource management
- **Challenge 3**: Complex exception handling strategy

## Expected Time Commitment

- Easy: 10-15 minutes per exercise
- Medium: 20-40 minutes per exercise
- Hard: 30-60 minutes per exercise
- Total: 8-15 hours for all exercises

## Common Pitfalls to Avoid

- Throwing from `close()` and losing the original exception (use suppressed exceptions)
- Catching overly broad `Exception`/`Throwable` without specific handlers
- Forgetting to re-throw or to chain the cause
- Resource leaks in exception paths
- Swallowing exceptions silently
- Confusing checked vs unchecked exception requirements
- Misordering catch blocks (generic before specific is a compile error)

## Learning Outcomes

After completing these exercises, you will:
✓ Design effective exception hierarchies
✓ Create custom checked and unchecked exceptions
✓ Combine try-with-resources/finally with exceptions
✓ Implement exception-safe code
✓ Use checked vs unchecked exceptions effectively
✓ Handle cleanup (`close()`) and suppressed exceptions safely
✓ Provide strong exception guarantees
✓ Log and propagate exceptions correctly (with cause chaining)
✓ Build robust error handling systems

## Java 21 Exercise Example: Custom Exception & try-with-resources

```java
public class Solution {
    static final class TransactionException extends RuntimeException {
        TransactionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static final class Connection implements AutoCloseable {
        void execute(String sql) {
            if (sql.isBlank()) throw new IllegalArgumentException("empty SQL");
            System.out.println("executed: " + sql);
        }

        @Override
        public void close() {
            System.out.println("connection closed");
        }
    }

    static void runTransaction(String sql) {
        try (Connection c = new Connection()) {
            c.execute(sql);
        } catch (IllegalArgumentException e) {
            // wrap with context and chain the original cause
            throw new TransactionException("transaction failed", e);
        }
    }

    public static void main(String[] args) {
        try {
            runTransaction("");
        } catch (TransactionException e) {
            System.out.println(e.getMessage() + " -> cause: " + e.getCause());
        }
    }
}
```

Compile and run with:

```
javac Solution.java
java Solution
```
