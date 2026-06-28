// Chapter 15: Advanced Features - Runnable Java Examples
// Compile: javac chapter15_advanced_features.java
// Run:     java chapter15_advanced_features
//
// The C++ chapter showcased C++11 features (lambdas, auto, range-for, nullptr,
// type traits, constexpr, move semantics, delegating constructors). This file
// maps each to its idiomatic MODERN JAVA counterpart and adds Java-only
// goodies that have no direct C++ analogue (Optional, Streams, records,
// sealed types, enums with methods, switch expressions, text blocks).

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class chapter15_advanced_features {

    // ============================================================
    // EXAMPLE 1: Lambdas & Functional Interfaces (C++ lambda analogue)
    // ============================================================
    static void example1Lambdas() {
        System.out.println("\n=== EXAMPLE 1: Lambdas & Functional Interfaces ===");
        Runnable greet = () -> System.out.println("Hello from lambda!");
        greet.run();

        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
        System.out.println("3 + 4 = " + add.apply(3, 4));

        BiFunction<Integer, Integer, Integer> multiply = (a, b) -> a * b;
        System.out.println("5 * 6 = " + multiply.apply(5, 6));
    }

    // ============================================================
    // EXAMPLE 2: Closures (Java captures are effectively-final, by value)
    // ============================================================
    static void example2Closures() {
        System.out.println("\n=== EXAMPLE 2: Closures / Captures ===");
        // Unlike C++ [&], Java only captures effectively-final variables (by value).
        int x = 10, y = 20;
        Runnable byValue = () -> System.out.println("Captured: x=" + x + ", y=" + y);
        byValue.run();

        // To mimic capture-by-reference you box mutable state in an array/object.
        int[] counter = {0};
        Runnable inc = () -> counter[0]++;
        inc.run(); inc.run(); inc.run();
        System.out.println("Mutable captured state via array box: " + counter[0]);
    }

    // ============================================================
    // EXAMPLE 3: var - Local Type Inference (C++ auto analogue, Java 10+)
    // ============================================================
    static void example3Var() {
        System.out.println("\n=== EXAMPLE 3: var Type Inference ===");
        var i = 42;                       // int
        var d = 3.14;                     // double
        var s = "string";                 // String
        var list = List.of(1, 2, 3);      // List<Integer>
        System.out.println("i: " + ((Object) i).getClass().getSimpleName());
        System.out.println("d: " + ((Object) d).getClass().getSimpleName());
        System.out.println("s: " + s.getClass().getSimpleName());
        System.out.println("list: " + list.getClass().getSimpleName());
    }

    // ============================================================
    // EXAMPLE 4: Enhanced for + Streams (C++ range-based for analogue)
    // ============================================================
    static void example4ForEachAndStreams() {
        System.out.println("\n=== EXAMPLE 4: for-each & Streams ===");
        List<Integer> v = List.of(1, 2, 3, 4, 5);
        System.out.print("By value: ");
        for (int x : v) System.out.print(x + " ");
        System.out.println();

        // Java is immutable here; transform via a stream instead of in-place doubling.
        List<Integer> doubled = v.stream().map(n -> n * 2).collect(Collectors.toList());
        System.out.println("After doubling (stream map): " + doubled);

        int sum = v.stream().mapToInt(Integer::intValue).sum();
        System.out.println("Sum (stream reduce): " + sum);
    }

    // ============================================================
    // EXAMPLE 5: Optional (Java's null-safety, C++ nullptr analogue)
    // ============================================================
    static Optional<String> findUser(int id) {
        return id == 1 ? Optional.of("Alice") : Optional.empty();
    }

    static void example5Optional() {
        System.out.println("\n=== EXAMPLE 5: Optional (null safety) ===");
        Optional<String> found = findUser(1);
        Optional<String> missing = findUser(99);
        System.out.println("found present : " + found.isPresent()
                + " -> " + found.orElse("<none>"));
        System.out.println("missing present: " + missing.isPresent()
                + " -> " + missing.orElse("<none>"));
        // map / filter chain without explicit null checks
        findUser(1).map(String::toUpperCase).ifPresent(u -> System.out.println("Upper: " + u));
    }

    // ============================================================
    // EXAMPLE 6: Generics Introspection / instanceof pattern
    // (C++ type_traits analogue — but Java decides at RUNTIME)
    // ============================================================
    static String classify(Object o) {
        // Enhanced instanceof with pattern binding (Java 16+).
        if (o instanceof Integer n) return "Integer value=" + n;
        if (o instanceof Double d)  return "Double value=" + d;
        if (o instanceof String s)  return "String length=" + s.length();
        return "Unknown";
    }

    static void example6TypeChecks() {
        System.out.println("\n=== EXAMPLE 6: Runtime Type Checks (instanceof) ===");
        System.out.println(classify(42));
        System.out.println(classify(3.14));
        System.out.println(classify("hello"));
        System.out.println("Note: C++ type_traits are compile-time;"
                + " Java instanceof is runtime.");
    }

    // ============================================================
    // EXAMPLE 7: Recursion + memo-free compute (constexpr analogue)
    // Java has no constexpr; compute at runtime, document the difference.
    // ============================================================
    static long fibonacci(int n) { return n <= 1 ? n : fibonacci(n - 1) + fibonacci(n - 2); }
    static long factorial(int n) { return n <= 1 ? 1 : n * factorial(n - 1); }

    static void example7Compute() {
        System.out.println("\n=== EXAMPLE 7: Compute (no constexpr in Java) ===");
        System.out.println("Fibonacci(10): " + fibonacci(10));
        System.out.println("Factorial(5): " + factorial(5));
        System.out.println("Fibonacci(7): " + fibonacci(7));
        System.out.println("Java evaluates these at runtime;"
                + " C++ constexpr can fold at compile time.");
    }

    // ============================================================
    // EXAMPLE 8: Records (concise immutable data carriers)
    // ============================================================
    record Point(int x, int y) {
        // Compact constructor for validation; records auto-generate
        // equals/hashCode/toString and accessors.
        Point {
            // no-op validation example
        }
        int distanceSq() { return x * x + y * y; }
    }

    static void example8Records() {
        System.out.println("\n=== EXAMPLE 8: Records ===");
        Point p = new Point(3, 4);
        System.out.println("Point: " + p);                 // auto toString
        System.out.println("x=" + p.x() + ", y=" + p.y()); // accessors
        System.out.println("distanceSq=" + p.distanceSq());
        System.out.println("equals: " + p.equals(new Point(3, 4)));
    }

    // ============================================================
    // EXAMPLE 9: Sealed Types + Switch Expression + Enums-with-methods
    // ============================================================
    sealed interface Shape permits Circle, Rectangle {}
    record Circle(double r) implements Shape {}
    record Rectangle(double w, double h) implements Shape {}

    // Enum carrying behaviour (impossible as a plain C++ enum).
    enum Operation {
        ADD("+") { int apply(int a, int b) { return a + b; } },
        MUL("*") { int apply(int a, int b) { return a * b; } };
        private final String symbol;
        Operation(String s) { this.symbol = s; }
        abstract int apply(int a, int b);
        String symbol() { return symbol; }
    }

    static double area(Shape s) {
        // Pattern matching for instanceof over a sealed hierarchy (Java 16+,
        // standard in Java 17). Pattern matching in *switch* was still a
        // preview feature in Java 17, so we use instanceof here instead.
        if (s instanceof Circle c) return Math.PI * c.r() * c.r();
        if (s instanceof Rectangle r) return r.w() * r.h();
        throw new IllegalStateException("Unknown shape: " + s);
    }

    static void example9SealedAndEnums() {
        System.out.println("\n=== EXAMPLE 9: Sealed Types, Switch, Enums ===");
        List<Shape> shapes = List.of(new Circle(2.0), new Rectangle(3.0, 4.0));
        for (Shape sh : shapes) {
            System.out.printf("area(%s) = %.4f%n", sh, area(sh));
        }
        for (Operation op : Operation.values()) {
            System.out.println("6 " + op.symbol() + " 7 = " + op.apply(6, 7));
        }
    }

    // ============================================================
    // EXAMPLE 10: Method References, Function composition, Text Blocks
    // ============================================================
    static void example10ModernJava() {
        System.out.println("\n=== EXAMPLE 10: Modern Java Best Practices ===");

        // Function composition
        Function<Integer, Integer> times2 = n -> n * 2;
        Function<Integer, Integer> plus3 = n -> n + 3;
        System.out.println("(x*2)+3 at x=5 -> " + times2.andThen(plus3).apply(5));

        // Method reference + stream pipeline
        String joined = IntStream.rangeClosed(1, 5)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(", "));
        System.out.println("Stream join: " + joined);

        // Supplier (lazy)
        Supplier<String> lazy = () -> "computed lazily";
        System.out.println("Supplier: " + lazy.get());

        // Text block (multi-line string literal, Java 15+)
        String summary = """
                Modern Java Key Features:
                  - lambdas & functional interfaces
                  - var local inference
                  - Optional null safety
                  - streams
                  - records
                  - sealed types & pattern switch
                  - enums with methods
                  - text blocks""";
        System.out.println(summary);

        // Quick sanity use of Arrays to avoid unused import.
        System.out.println("Reversed sort demo: "
                + Arrays.toString(new int[]{3, 1, 2}));
    }

    public static void main(String[] args) throws Exception {
        System.out.println("======================================================");
        System.out.println("   CHAPTER 15: ADVANCED FEATURES (Java)");
        System.out.println("======================================================");

        example1Lambdas();
        example2Closures();
        example3Var();
        example4ForEachAndStreams();
        example5Optional();
        example6TypeChecks();
        example7Compute();
        example8Records();
        example9SealedAndEnums();
        example10ModernJava();

        System.out.println("\n======================================================");
        System.out.println("All examples completed!");
        System.out.println("======================================================");
    }
}
