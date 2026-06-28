// Chapter 13: Exception Handling - Runnable Java Examples
// Compile: javac chapter13_exceptions.java
// Run:     java chapter13_exceptions
//
// This is the Java adaptation of the C++ chapter on exceptions. Java's model
// differs from C++ in several important ways, demonstrated below:
//   - Java distinguishes CHECKED exceptions (must be declared/caught) from
//     UNCHECKED exceptions (RuntimeException / Error subclasses).
//   - Java has a `finally` block (C++ relies on RAII / destructors instead).
//   - Java has try-with-resources (the idiomatic replacement for C++ RAII).
//   - Java supports multi-catch (catch (A | B e)) and exception chaining
//     (new Exception(msg, cause)).

import java.util.ArrayList;
import java.util.List;

public class chapter13_exceptions {

    // ============================================================
    // EXAMPLE 1: Basic Try-Catch
    // ============================================================
    static void example1BasicTryCatch() {
        System.out.println("\n=== EXAMPLE 1: Basic Try-Catch ===");
        try {
            int[] arr = {1, 2, 3, 4, 5};
            int index = 10;
            // Java throws ArrayIndexOutOfBoundsException automatically,
            // but we throw explicitly to mirror the C++ example.
            if (index >= arr.length) {
                throw new IndexOutOfBoundsException("Index out of bounds!");
            }
            System.out.println("arr[" + index + "] = " + arr[index]);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Caught exception: " + e.getMessage());
        }
    }

    // ============================================================
    // EXAMPLE 2: Multiple Catch Blocks
    // ============================================================
    static void divide(int a, int b) {
        if (b == 0) throw new IllegalArgumentException("Division by zero!");
        System.out.println(a + " / " + b + " = " + (a / b));
    }

    static void example2MultipleCatch() {
        System.out.println("\n=== EXAMPLE 2: Multiple Catch Blocks ===");
        try {
            divide(10, 0);
        } catch (IllegalArgumentException e) {
            // Most specific handler first (like C++).
            System.out.println("Invalid argument: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("General exception: " + e.getMessage());
        }
    }

    // ============================================================
    // EXAMPLE 3: Custom Exceptions (checked, with a hierarchy)
    // ============================================================
    static class BankException extends Exception {       // checked exception
        BankException(String msg) { super(msg); }
    }

    static class InsufficientFundsException extends BankException {
        InsufficientFundsException(double amount, double balance) {
            super("Insufficient funds: need " + amount + ", have " + balance);
        }
    }

    static void example3CustomExceptions() {
        System.out.println("\n=== EXAMPLE 3: Custom Exceptions ===");
        double balance = 100.0;
        double amount = 150.0;
        try {
            if (amount > balance) {
                throw new InsufficientFundsException(amount, balance);
            }
            balance -= amount;
        } catch (InsufficientFundsException e) {
            System.out.println("Bank error: " + e.getMessage());
        }
    }

    // ============================================================
    // EXAMPLE 4: Exception Hierarchy & Multi-Catch
    // ============================================================
    static void example4ExceptionHierarchy() {
        System.out.println("\n=== EXAMPLE 4: Exception Hierarchy / Multi-Catch ===");
        try {
            throw new IllegalStateException("Runtime error example");
        }
        // Java multi-catch: handle several unrelated types in one block.
        catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Specific (multi-catch): " + e.getMessage());
        }
        catch (RuntimeException e) {
            System.out.println("Generic exception: " + e.getMessage());
        }
    }

    // ============================================================
    // EXAMPLE 5: Re-throwing Exceptions
    // ============================================================
    static void processData(int value) {
        try {
            if (value < 0) throw new IllegalArgumentException("Negative value");
        } catch (RuntimeException e) {
            System.out.println("processData caught: " + e.getMessage());
            throw e;  // Re-throw to caller (C++ uses bare `throw;`)
        }
    }

    static void example5Rethrow() {
        System.out.println("\n=== EXAMPLE 5: Re-throwing Exceptions ===");
        try {
            processData(-5);
        } catch (RuntimeException e) {
            System.out.println("Caller caught: " + e.getMessage());
        }
    }

    // ============================================================
    // EXAMPLE 6: finally Block (the Java analogue of C++ RAII cleanup)
    // ============================================================
    static void example6Finally() {
        System.out.println("\n=== EXAMPLE 6: finally Block ===");
        boolean committed = false;
        try {
            System.out.println("Starting transaction: Database update");
            throw new RuntimeException("Update failed!");
            // log.commit(); committed = true;  // unreachable here
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            // Always runs, exception or not. C++ would use a destructor.
            if (!committed) {
                System.out.println("Transaction rolled back: Database update");
            }
        }
    }

    // ============================================================
    // EXAMPLE 7: Checked vs Unchecked Exceptions
    // ============================================================
    // Checked: must be declared with `throws` or caught.
    static void riskyChecked() throws Exception {
        throw new Exception("Something went wrong (checked)");
    }
    // Unchecked: RuntimeException, no `throws` needed.
    static void riskyUnchecked() {
        throw new RuntimeException("Something went wrong (unchecked)");
    }

    static void example7CheckedVsUnchecked() {
        System.out.println("\n=== EXAMPLE 7: Checked vs Unchecked ===");
        System.out.println("This method won't throw (analogous to C++ noexcept)");
        try {
            riskyChecked();
        } catch (Exception e) {
            System.out.println("Caught checked: " + e.getMessage());
        }
        try {
            riskyUnchecked();
        } catch (RuntimeException e) {
            System.out.println("Caught unchecked: " + e.getMessage());
        }
    }

    // ============================================================
    // EXAMPLE 8: Try-With-Resources (Java's RAII)
    // ============================================================
    // AutoCloseable resources are closed automatically in reverse order,
    // replacing C++ destructor-based cleanup.
    static class Resource implements AutoCloseable {
        private final String name;
        Resource(String name) {
            this.name = name;
            System.out.println("Opened resource: " + name);
        }
        void use() { System.out.println("Using resource: " + name); }
        @Override public void close() {
            System.out.println("Closed resource: " + name);
        }
    }

    static void example8TryWithResources() {
        System.out.println("\n=== EXAMPLE 8: Try-With-Resources ===");
        try (Resource a = new Resource("A");
             Resource b = new Resource("B")) {
            a.use();
            b.use();
            throw new RuntimeException("Error during execution");
        } catch (RuntimeException e) {
            System.out.println("Caught: " + e.getMessage());
            System.out.println("Resources were closed safely (reverse order)");
        }
    }

    // ============================================================
    // EXAMPLE 9: Exception Chaining
    // ============================================================
    static void example9ExceptionChaining() {
        System.out.println("\n=== EXAMPLE 9: Exception Chaining ===");
        try {
            try {
                throw new IllegalArgumentException("low-level cause");
            } catch (IllegalArgumentException cause) {
                // Wrap a low-level exception in a higher-level one, preserving cause.
                throw new IllegalStateException("high-level failure", cause);
            }
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage());
            System.out.println("Caused by: " + e.getCause().getMessage());
        }
    }

    // ============================================================
    // EXAMPLE 10: Exception Neutral Code (catch, propagate)
    // ============================================================
    static void processContainer(List<Integer> list) {
        try {
            for (int i = 0; i < list.size(); i++) {
                int val = list.get(i);
                if (val < 0) throw new IllegalArgumentException("Negative value");
                list.set(i, val * 2);
            }
        } catch (RuntimeException e) {
            System.out.println("Processing error: " + e.getMessage());
            throw e;  // Propagate to caller; do not suppress.
        }
    }

    static void example10ExceptionNeutral() {
        System.out.println("\n=== EXAMPLE 10: Exception Neutral Code ===");
        List<Integer> data = new ArrayList<>(List.of(1, 2, 3, -4, 5));
        try {
            processContainer(data);
        } catch (RuntimeException e) {
            System.out.println("Error handled by caller");
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("======================================================");
        System.out.println("   CHAPTER 13: EXCEPTION HANDLING (Java)");
        System.out.println("======================================================");

        example1BasicTryCatch();
        example2MultipleCatch();
        example3CustomExceptions();
        example4ExceptionHierarchy();
        example5Rethrow();
        example6Finally();
        example7CheckedVsUnchecked();
        example8TryWithResources();
        example9ExceptionChaining();
        example10ExceptionNeutral();

        System.out.println("\n======================================================");
        System.out.println("All examples completed!");
        System.out.println("======================================================");
    }
}
