// Chapter 3: Functions - Java translation of chapter3_functions.cpp
// Compile: javac chapter3_functions.java
// Run:     java chapter3_functions
// Target: Java 17 (lambdas, functional interfaces, method references). Runs on JDK 17.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

public class chapter3_functions {

    // ============================================================
    // EXAMPLE 1: Basic (static) methods
    // ============================================================
    static int add(int a, int b) {
        return a + b;
    }

    static double divide(double a, double b) {
        if (b == 0) return 0;
        return a / b;
    }

    static void printMessage(String msg) {
        System.out.println("Message: " + msg);
    }

    static void example1_basic() {
        System.out.println("\n=== EXAMPLE 1: Basic Functions ===");

        System.out.println("5 + 3 = " + add(5, 3));
        System.out.println("10.0 / 2.0 = " + divide(10.0, 2.0));
        printMessage("Hello from function!");
    }

    // ============================================================
    // EXAMPLE 2: Pass by Value vs Reference.
    // Java is ALWAYS pass-by-value. Primitives are copied, so a method
    // cannot change the caller's int. To get C++ "pass by reference"
    // behaviour you pass a mutable object (here an int[] holder) and
    // mutate its contents. We show both.
    // ============================================================
    static void incrementByValue(int x) {
        x++;  // modifies local copy only
    }

    static void incrementViaHolder(int[] holder) {
        holder[0]++;  // mutates the shared array object -> caller sees it
    }

    static void example2_passing() {
        System.out.println("\n=== EXAMPLE 2: Pass by Value vs Reference ===");

        int num = 5;
        System.out.println("Original: " + num);

        incrementByValue(num);
        System.out.println("After pass by value: " + num + " (unchanged)");

        int[] holder = {num};
        incrementViaHolder(holder);
        System.out.println("After mutate-via-holder: " + holder[0] + " (changed)");
    }

    // ============================================================
    // EXAMPLE 3: Method Overloading (same as C++ function overloading)
    // ============================================================
    static void display(int x)    { System.out.println("Integer: " + x); }
    static void display(double x) { System.out.println("Double: " + x); }
    static void display(String x) { System.out.println("String: " + x); }

    static int multiply(int a, int b)       { return a * b; }
    static double multiply(double a, double b) { return a * b; }

    static void example3_overloading() {
        System.out.println("\n=== EXAMPLE 3: Function Overloading ===");

        display(42);
        display(3.14);
        display("hello");

        System.out.println("int multiply: " + multiply(5, 3));
        System.out.println("double multiply: " + multiply(5.5, 2.0));
    }

    // ============================================================
    // EXAMPLE 4: Default Arguments.
    // Java has NO default arguments. The idiomatic replacement is
    // overloads that forward to the fullest version.
    // ============================================================
    static void greet()                          { greet("Guest", "Hello"); }
    static void greet(String name)               { greet(name, "Hello"); }
    static void greet(String name, String greeting) {
        System.out.println(greeting + ", " + name + "!");
    }

    static void printRange()                 { printRange(0, 10); }
    static void printRange(int start)        { printRange(start, 10); }
    static void printRange(int start, int end) {
        for (int i = start; i <= end; i++) System.out.print(i + " ");
        System.out.println();
    }

    static void example4_defaults() {
        System.out.println("\n=== EXAMPLE 4: Default Arguments (via overloads) ===");

        greet();
        greet("Alice");
        greet("Bob", "Hi");

        printRange();
        printRange(1);
        printRange(1, 5);
    }

    // ============================================================
    // EXAMPLE 5: Recursion
    // ============================================================
    static int factorial(int n) {
        if (n <= 1) return 1;
        return n * factorial(n - 1);
    }

    static int fibonacci(int n) {
        if (n <= 2) return 1;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    static int power(int base, int exp) {
        if (exp == 0) return 1;
        return base * power(base, exp - 1);
    }

    static void example5_recursion() {
        System.out.println("\n=== EXAMPLE 5: Recursion ===");

        System.out.println("Factorial(5) = " + factorial(5));
        System.out.println("Fibonacci(8) = " + fibonacci(8));
        System.out.println("2^5 = " + power(2, 5));
    }

    // ============================================================
    // EXAMPLE 6: "Function pointers" -> functional interfaces / method refs.
    // Java has no function pointers. A method reference (Class::method)
    // assigned to a functional interface is the idiomatic equivalent.
    // ============================================================
    static int sum(int a, int b)  { return a + b; }
    static int diff(int a, int b) { return a - b; }

    static int operation(int a, int b, IntBinaryOperator func) {
        return func.applyAsInt(a, b);
    }

    static void example6_function_pointers() {
        System.out.println("\n=== EXAMPLE 6: Function References ===");

        System.out.println("10 + 5 = " + operation(10, 5, chapter3_functions::sum));
        System.out.println("10 - 5 = " + operation(10, 5, chapter3_functions::diff));

        // "Array of function pointers" -> array of functional interfaces.
        IntBinaryOperator[] ops = { chapter3_functions::sum, chapter3_functions::diff };
        System.out.println("ops[0](3, 2) = " + ops[0].applyAsInt(3, 2));
        System.out.println("ops[1](3, 2) = " + ops[1].applyAsInt(3, 2));
    }

    // ============================================================
    // EXAMPLE 7: Lambdas (Java 8+, analogue of C++11 lambdas).
    // Capture-by-value is default; Java requires captured locals to be
    // effectively final, so for a mutable "capture by reference" counter
    // we use a one-element array.
    // ============================================================
    static void example7_lambdas() {
        System.out.println("\n=== EXAMPLE 7: Lambda Functions ===");

        IntBinaryOperator add = (a, b) -> a + b;
        System.out.println("Lambda add: 5 + 3 = " + add.applyAsInt(5, 3));

        int multiplier = 3;  // effectively final -> captured by value
        IntUnaryOperator multiply = x -> x * multiplier;
        System.out.println("Lambda multiply (factor=3): 5 * 3 = " + multiply.applyAsInt(5));

        int[] counter = {0};  // mutable holder simulates capture-by-reference
        Runnable increment = () -> counter[0]++;
        increment.run();
        increment.run();
        increment.run();
        System.out.println("Counter after 3 increments: " + counter[0]);

        int[] v = {1, 2, 3, 4, 5};
        int[] squares = Arrays.stream(v).map(x -> x * x).toArray();

        System.out.print("Original: ");
        for (int x : v) System.out.print(x + " ");
        System.out.print("\nSquares: ");
        for (int x : squares) System.out.print(x + " ");
        System.out.println();
    }

    // ============================================================
    // EXAMPLE 8: "Return by reference".
    // Java cannot return a reference to a primitive/local. But returning
    // an OBJECT returns a reference to it, so mutating the returned object
    // is visible through the original variable. We use a small mutable
    // StringBuilder holder to demonstrate the aliasing.
    // ============================================================
    static StringBuilder getLargerString(StringBuilder a, StringBuilder b) {
        return a.length() > b.length() ? a : b;
    }

    static void example8_return_reference() {
        System.out.println("\n=== EXAMPLE 8: Return by Reference (object aliasing) ===");

        StringBuilder str1 = new StringBuilder("Hello");
        StringBuilder str2 = new StringBuilder("World!");

        StringBuilder larger = getLargerString(str1, str2);  // refers to str2
        System.out.println("Larger string: " + larger);

        larger.setLength(0);
        larger.append("Modified");                            // mutates the same object
        System.out.println("After modification: " + str2);
    }

    // ============================================================
    // EXAMPLE 9: Scope + "static" local variable.
    // Java has no function-local static. The equivalent persistent state
    // is a static field on the class. Shadowing across nested blocks of
    // the same method is illegal in Java, so the inner x lives in a helper.
    // ============================================================
    static int staticVar = 0;  // class-level state == C++ function-local static

    static void example9_function_scope() {
        System.out.println("\n=== EXAMPLE 9: Function Scope ===");

        int x = 10;
        System.out.println("Outside block: x = " + x);

        innerBlock();  // separate scope with its own x = 20

        System.out.println("Back outside: x = " + x);

        staticVar++;
        System.out.println("Static var (call 1): " + staticVar);
        staticVar++;
        System.out.println("Static var (call 2): " + staticVar);
    }

    static void innerBlock() {
        int x = 20;
        System.out.println("Inside block: x = " + x);
    }

    // ============================================================
    // EXAMPLE 10: Recursion with Memoization
    // ============================================================
    static int fibMemo(int n, int[] memo) {
        if (n <= 2) return 1;
        if (memo[n] != -1) return memo[n];

        memo[n] = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
        return memo[n];
    }

    static void example10_memoization() {
        System.out.println("\n=== EXAMPLE 10: Recursion with Memoization ===");

        int n = 10;
        int[] memo = new int[n + 1];
        Arrays.fill(memo, -1);

        System.out.println("Fibonacci(" + n + ") with memoization = " + fibMemo(n, memo));

        List<Integer> filled = new ArrayList<>();
        for (int val : memo) if (val != -1) filled.add(val);
        System.out.print("Memo array: ");
        for (int val : filled) System.out.print(val + " ");
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=======================================");
        System.out.println("        Chapter 3: Functions           ");
        System.out.println("=======================================");

        example1_basic();
        example2_passing();
        example3_overloading();
        example4_defaults();
        example5_recursion();
        example6_function_pointers();
        example7_lambdas();
        example8_return_reference();
        example9_function_scope();
        example10_memoization();

        System.out.println("\n=======================================");
        System.out.println("       All Examples Completed!");
        System.out.println("=======================================");
    }
}
