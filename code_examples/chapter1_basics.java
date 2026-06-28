// Chapter 1: Basics & Fundamentals - Java translation of chapter1_basics.cpp
// Compile: javac chapter1_basics.java
// Run:     java chapter1_basics
// Target: Java 17 (uses var, text blocks where helpful). Runs on JDK 17.

public class chapter1_basics {

    // ============================================================
    // EXAMPLE 1: Data Types & Variables
    // C++ has signed char / short / int / long / long long. Java's
    // integral types are byte (8), short (16), int (32), long (64).
    // Java sizes are fixed by the spec (unlike C++ where they are
    // implementation-defined). sizeof() has no Java analogue, so we
    // use the *.BYTES constants from the wrapper classes.
    // ============================================================
    static void example1_data_types() {
        System.out.println("\n=== EXAMPLE 1: Data Types & Variables ===");

        byte sc = -128;                       // closest analogue to signed char
        short s = -32768;
        int i = 2147483647;
        long l = 9223372036L;
        long ll = 9223372036854775807L;       // Java long == C++ long long (64-bit)

        float f = 3.14f;
        double d = 3.14159265;

        boolean flag = true;

        char c = 'A';

        System.out.println("Int: " + i + " (size: " + Integer.BYTES + " bytes)");
        System.out.println("Float: " + f + " (size: " + Float.BYTES + " bytes)");
        System.out.println("Double: " + d + " (size: " + Double.BYTES + " bytes)");
        // Java does not define a byte-size for boolean; the JVM typically uses 1 byte.
        System.out.println("Bool: " + flag + " (size: ~1 byte, JVM-dependent)");
        System.out.println("Char: " + c + " (size: " + Character.BYTES + " bytes, UTF-16)");
        // Reference the others so they are "used".
        System.out.println("byte=" + sc + ", short=" + s + ", long=" + l + ", long long=" + ll);
    }

    // ============================================================
    // EXAMPLE 2: Initialization (C++11 uniform/brace init).
    // Java has no brace-init for scalars; it does narrowing-prevention
    // at compile time too (e.g. `int z = 3.14;` is a compile error).
    // ============================================================
    static void example2_uniform_init() {
        System.out.println("\n=== EXAMPLE 2: Initialization ===");

        int x = 10;
        int y = 20;
        // int z = 3.14;  // ERROR in Java too: incompatible types (narrowing)

        int[] arr = {1, 2, 3, 4, 5};

        System.out.println("x = " + x + ", y = " + y);
        System.out.print("Array: ");
        for (int val : arr) System.out.print(val + " ");
        System.out.println();
    }

    // ============================================================
    // EXAMPLE 3: var Type Inference (Java 10+, analogue of C++ auto)
    // ============================================================
    static void example3_auto() {
        System.out.println("\n=== EXAMPLE 3: var Type Inference ===");

        var a = 10;        // int
        var b = 3.14;      // double
        var c = "hello";   // String (C++ auto would give const char*)
        var d = true;      // boolean

        System.out.println("a = " + a + " (inferred int)");
        System.out.println("b = " + b + " (inferred double)");
        System.out.println("c = " + c + " (inferred String)");
        System.out.println("d = " + d + " (inferred boolean)");
    }

    // ============================================================
    // EXAMPLE 4: Constants. C++ const/constexpr -> Java `final`.
    // Java has no constexpr, but `static final` of a primitive is a
    // compile-time constant usable in switch labels, etc.
    // ============================================================
    static final int MAX_SIZE = 100;
    static final double PI = 3.14159;

    static void example4_constants() {
        System.out.println("\n=== EXAMPLE 4: Constants (final) ===");

        System.out.println("MAX_SIZE = " + MAX_SIZE);
        System.out.println("PI = " + PI);

        int[] arr = new int[MAX_SIZE];  // array sized by the constant
        System.out.println("Created array of size: " + arr.length);
    }

    // ============================================================
    // EXAMPLE 5: Output & formatting. C++ iostream manipulators
    // (fixed, setprecision, hex, setw, setfill) map to printf-style
    // format strings in Java.
    // ============================================================
    static void example5_io() {
        System.out.println("\n=== EXAMPLE 5: Output & Formatting ===");

        System.out.println("Simple output with println");
        System.out.println("Value: " + 42 + ", Float: " + 3.14);

        System.out.println("\nFormatted Output:");
        System.out.printf("PI with 2 decimals: %.2f%n", 3.14159);

        System.out.printf("42 in hex: %x%n", 42);
        System.out.println("Back to decimal: " + 42);

        System.out.println("\nAlignment:");
        System.out.printf("%10s%-10s%n", "Right", "Left"); // right then left within width 10
        // setfill('*') equivalent: pad manually.
        System.out.println(String.format("%10s", "Padded").replace(' ', '*'));
    }

    // ============================================================
    // EXAMPLE 6: Type Conversion. Implicit widening, explicit cast.
    // Java has no static_cast; you use the (type) cast syntax.
    // ============================================================
    static void example6_casting() {
        System.out.println("\n=== EXAMPLE 6: Type Conversion ===");

        int x = 10;
        double y = x;  // implicit widening int -> double
        System.out.println("Implicit: int " + x + " -> double " + y);

        double z = 3.14;
        int w = (int) z;  // explicit narrowing required in Java
        System.out.println("Narrowing: double " + z + " -> int " + w);

        int a = 5;
        double b = (double) a;  // Java cast, like C++ static_cast<double>(a)
        System.out.println("Explicit cast: " + a + " -> " + b);
    }

    // ============================================================
    // EXAMPLE 7: Variable Scope & shadowing in nested blocks.
    // Note: Java does NOT allow a local variable to shadow another
    // local in an enclosing block within the same method, so we use
    // a separate helper block-method to demonstrate shadowing safely.
    // ============================================================
    static void example7_scope() {
        System.out.println("\n=== EXAMPLE 7: Variable Scope ===");

        int x = 10;
        System.out.println("Outer scope x = " + x);

        innerScope();  // a different scope with its own x = 20

        System.out.println("Back to outer scope x = " + x);
    }

    static void innerScope() {
        int x = 20;  // independent variable; in C++ this would shadow the outer x
        System.out.println("Inner scope x = " + x);
    }

    // ============================================================
    // EXAMPLE 8: Literals. Java supports decimal, octal (0..), hex (0x..),
    // binary (0b.., Java 7+) and underscores in literals.
    // ============================================================
    static void example8_literals() {
        System.out.println("\n=== EXAMPLE 8: Literals ===");

        int decimal = 42;
        int octal = 052;        // leading 0
        int hex = 0x2A;         // 0x prefix
        int binary = 0b101010;  // 0b prefix

        System.out.println("Decimal: " + decimal);
        System.out.println("Octal: " + octal);
        System.out.println("Hex: " + hex);
        System.out.println("Binary: " + binary);

        // Java has only one string type (no C-string vs std::string distinction).
        String str1 = "Java string (no separate C-string type)";
        String str2 = "Another string";

        System.out.println("str1: " + str1);
        System.out.println("str2: " + str2);
    }

    // ============================================================
    // EXAMPLE 9: Compound Types. C++ shows array, pointer, reference.
    // Java has arrays and object references (no raw pointers / address-of).
    // Reassigning a reference points it at another object; for primitives
    // there is no aliasing, so we use an int[] to demonstrate aliasing.
    // ============================================================
    static void example9_compound() {
        System.out.println("\n=== EXAMPLE 9: Compound Types ===");

        int[] arr = {1, 2, 3, 4, 5};
        System.out.print("Array: ");
        for (int val : arr) System.out.print(val + " ");
        System.out.println();

        // Java has no &x address-of and no *ptr. References point to objects.
        int[] box = {42};        // single-element array acts like a "boxed" mutable int
        int[] alias = box;       // alias refers to the SAME array object
        System.out.println("box[0] via reference: " + alias[0]);

        alias[0] = 100;          // mutate through the alias
        System.out.println("After alias[0] = 100: box[0] = " + box[0]);
    }

    // ============================================================
    // EXAMPLE 10: "sizeof". Java has no sizeof; primitive widths are
    // fixed and exposed via *.SIZE (bits) / *.BYTES (bytes).
    // ============================================================
    static void example10_sizeof() {
        System.out.println("\n=== EXAMPLE 10: Type Sizes (no sizeof in Java) ===");

        System.out.println("Byte.BYTES (closest to char): " + Byte.BYTES);
        System.out.println("Short.BYTES: " + Short.BYTES);
        System.out.println("Integer.BYTES: " + Integer.BYTES);
        System.out.println("Long.BYTES: " + Long.BYTES);
        System.out.println("Float.BYTES: " + Float.BYTES);
        System.out.println("Double.BYTES: " + Double.BYTES);
        System.out.println("Character.BYTES (UTF-16): " + Character.BYTES);

        int[] arr = new int[10];
        // .length is element count; total bytes = length * Integer.BYTES (payload only).
        System.out.println("int[10] payload bytes: " + (arr.length * Integer.BYTES));
    }

    public static void main(String[] args) {
        System.out.println("=======================================");
        System.out.println("    Chapter 1: Basics & Fundamentals   ");
        System.out.println("=======================================");

        example1_data_types();
        example2_uniform_init();
        example3_auto();
        example4_constants();
        example5_io();
        example6_casting();
        example7_scope();
        example8_literals();
        example9_compound();
        example10_sizeof();

        System.out.println("\n=======================================");
        System.out.println("       All Examples Completed!");
        System.out.println("=======================================");
    }
}
