// Chapter 8: Operator Overloading - Runnable Java Examples
// Compile/run on Java 17:
//   javac chapter8_operator_overloading.java
//   java  chapter8_operator_overloading
//
// BIG PICTURE DIFFERENCE:
//   C++ lets you overload operators (+, -, ==, <, [], ++, <<, (), conversions,
//   ->, &&, ...). JAVA HAS NO USER-DEFINED OPERATOR OVERLOADING. The idiomatic
//   Java approach is to expose well-named METHODS and standard interfaces:
//     +  -> add(), multiply()         (value methods on a record/class)
//     == -> equals() / hashCode()     (value equality)
//     <  -> Comparable.compareTo() / Comparator
//     [] -> get(int) / set(int,...)
//     ++ -> increment()/next() methods
//     << -> toString()
//     () -> a functional interface (Function, etc.)
//   The "+" used on String is the ONLY built-in operator overload in Java,
//   and it is hard-wired in the language, not user-extensible.

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

public class chapter8_operator_overloading {

    // ============================================================
    // EXAMPLE 1: Arithmetic - methods instead of operator+ / operator-
    // We use a record (immutable value type, Java 16+) for Complex.
    // ============================================================
    record Complex(double real, double imag) {
        Complex add(Complex c)      { return new Complex(real + c.real, imag + c.imag); }
        Complex subtract(Complex c) { return new Complex(real - c.real, imag - c.imag); }
        // toString is the Java analogue of operator<<
        @Override public String toString() {
            return imag >= 0 ? real + " + " + imag + "i" : real + " - " + (-imag) + "i";
        }
    }

    static void example1_arithmetic() {
        System.out.println("\n=== EXAMPLE 1: Arithmetic (add/subtract methods) ===");
        Complex c1 = new Complex(3, 4), c2 = new Complex(1, 2);
        System.out.println("c1: " + c1);
        System.out.println("c2: " + c2);
        System.out.println("Sum: " + c1.add(c2));            // c1 + c2 in C++
        System.out.println("Difference: " + c1.subtract(c2)); // c1 - c2 in C++
    }

    // ============================================================
    // EXAMPLE 2: Comparison - equals()/hashCode() and Comparable
    // C++ operator==, operator<, operator> become Java overrides.
    // ============================================================
    static final class Point implements Comparable<Point> {
        final int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
        int magSquared() { return x * x + y * y; }

        @Override public boolean equals(Object o) { // replaces operator==
            if (this == o) return true;
            if (!(o instanceof Point p)) return false;
            return x == p.x && y == p.y;
        }
        @Override public int hashCode() { return Objects.hash(x, y); } // must match equals
        @Override public int compareTo(Point p) { // replaces operator< / >
            return Integer.compare(magSquared(), p.magSquared());
        }
        @Override public String toString() { return "(" + x + ", " + y + ")"; }
    }

    static void example2_comparison() {
        System.out.println("\n=== EXAMPLE 2: Comparison (equals/compareTo) ===");
        Point p1 = new Point(3, 4), p2 = new Point(3, 4), p3 = new Point(1, 1);
        System.out.println("p1: " + p1 + ", p2: " + p2 + ", p3: " + p3);
        System.out.println("p1 equals p2? " + (p1.equals(p2) ? "Yes" : "No"));
        System.out.println("p1 < p3? " + (p1.compareTo(p3) < 0 ? "Yes" : "No"));
        System.out.println("p3 < p1? " + (p3.compareTo(p1) < 0 ? "Yes" : "No"));
    }

    // ============================================================
    // EXAMPLE 3: Subscript - get()/set() instead of operator[]
    // ============================================================
    static class IntArray {
        private final int[] data;
        IntArray(int size) { data = new int[size]; } // already zero-filled in Java
        int get(int i) {
            if (i < 0 || i >= data.length) { System.out.println("Index out of bounds!"); return data[0]; }
            return data[i];
        }
        void set(int i, int v) {
            if (i < 0 || i >= data.length) { System.out.println("Index out of bounds!"); return; }
            data[i] = v;
        }
        int size() { return data.length; }
    }

    static void example3_subscript() {
        System.out.println("\n=== EXAMPLE 3: Subscript (get/set methods) ===");
        IntArray arr = new IntArray(5);
        for (int i = 0; i < 5; i++) arr.set(i, (i + 1) * 10);
        System.out.print("Array contents: ");
        for (int i = 0; i < 5; i++) System.out.print(arr.get(i) + " ");
        System.out.println();
    }

    // ============================================================
    // EXAMPLE 4: Increment/Decrement - methods (no ++ overload in Java)
    // ============================================================
    static class Counter {
        private int value;
        Counter(int v) { value = v; }
        Counter increment() { value++; return this; }  // like ++c (returns self)
        Counter decrement() { value--; return this; }
        int getValue() { return value; }
    }

    static void example4_increment() {
        System.out.println("\n=== EXAMPLE 4: Increment/Decrement (methods) ===");
        Counter c = new Counter(5);
        System.out.println("Initial: " + c.getValue());
        System.out.println("After increment(): " + c.increment().getValue());
        System.out.println("After increment(): " + c.increment().getValue());
        System.out.println("After decrement(): " + c.decrement().getValue());
        System.out.println("Note: Java cannot distinguish pre/post-increment for objects");
    }

    // ============================================================
    // EXAMPLE 5: "Stream" output - toString() replaces operator<<
    // ============================================================
    record Rational(int numerator, int denominator) {
        @Override public String toString() { return numerator + "/" + denominator; }
    }

    static void example5_streamOutput() {
        System.out.println("\n=== EXAMPLE 5: Output via toString() (replaces operator<<) ===");
        Rational r1 = new Rational(3, 4), r2 = new Rational(5, 6);
        System.out.println("Rational numbers: " + r1 + " and " + r2);
        System.out.println("toString() gives custom output format");
    }

    // ============================================================
    // EXAMPLE 6: Assignment - Java references vs C++ copy assignment
    // C++ overloads operator= for deep copy. Java '=' only rebinds a
    // reference (no copy). For a copy you write a copy constructor / method.
    // ============================================================
    static class Text {
        private String str;
        Text(String s) { this.str = s; }
        Text(Text other) { this.str = other.str; }  // copy constructor (explicit)
        void display() { System.out.println(str); }
    }

    static void example6_assignment() {
        System.out.println("\n=== EXAMPLE 6: Assignment (references, not deep copy) ===");
        Text s1 = new Text("Hello");
        Text s2 = new Text("World");
        System.out.print("s1: "); s1.display();
        System.out.print("s2: "); s2.display();
        s1 = s2; // rebinds s1 to the SAME object as s2 (no copy made)
        System.out.println("After s1 = s2 (s1 now references the same object):");
        System.out.print("s1: "); s1.display();
        Text copy = new Text(s2); // explicit copy if you actually need one
        System.out.print("Explicit copy: "); copy.display();
    }

    // ============================================================
    // EXAMPLE 7: Function call operator () -> functional interface
    // C++ functors overload operator(). In Java we use a functional
    // interface (here IntUnaryOperator) implemented by a lambda.
    // ============================================================
    static void example7_functionCall() {
        System.out.println("\n=== EXAMPLE 7: Callable objects (functional interfaces) ===");
        IntUnaryOperator times3 = x -> x * 3;
        IntUnaryOperator times5 = x -> x * 5;
        System.out.println("times3.applyAsInt(10) = " + times3.applyAsInt(10));
        System.out.println("times5.applyAsInt(10) = " + times5.applyAsInt(10));
        System.out.println("Lambdas/functional interfaces replace C++ functors");
    }

    // ============================================================
    // EXAMPLE 8: Type conversion - explicit methods (no conversion operators)
    // C++ allows "operator double()". Java requires explicit conversion methods.
    // ============================================================
    static class Temperature {
        private final double celsius;
        Temperature(double c) { this.celsius = c; }
        double toDouble() { return celsius; }     // replaces operator double()
        int toInt() { return (int) celsius; }      // replaces operator int()
    }

    static void example8_typeConversion() {
        System.out.println("\n=== EXAMPLE 8: Type Conversion (explicit methods) ===");
        Temperature t = new Temperature(98.6);
        System.out.println("Temperature: " + t.toDouble() + " degrees");
        System.out.println("As integer: " + t.toInt() + " degrees");
        System.out.println("Java has no implicit user-defined conversions (safer)");
    }

    // ============================================================
    // EXAMPLE 9: Member access -> ; Java has no operator-> overload.
    // Java always uses '.' on references; there are no raw pointers.
    // We demonstrate plain accessor methods on a 3D vector record.
    // ============================================================
    record Vector3D(double x, double y, double z) {
        double getX() { return x; }
        double getY() { return y; }
        double getZ() { return z; }
    }

    static void example9_memberAccess() {
        System.out.println("\n=== EXAMPLE 9: Member Access (always '.', no -> ) ===");
        Vector3D v = new Vector3D(1, 2, 3);
        System.out.println("X: " + v.getX());
        System.out.println("Y: " + v.getY());
        System.out.println("Z: " + v.getZ());
        System.out.println("Java references use '.'; no operator-> to overload");
    }

    // ============================================================
    // EXAMPLE 10: Logical operations - plain methods / boolean ops
    // C++ can overload && || ! (losing short-circuit). In Java the built-in
    // &&, ||, ! work only on boolean and always short-circuit. For a value
    // class we just expose methods.
    // ============================================================
    record Condition(boolean value) {
        Condition and(Condition c) { return new Condition(value && c.value); }
        Condition or(Condition c)  { return new Condition(value || c.value); }
        Condition not()            { return new Condition(!value); }
    }

    static void example10_logical() {
        System.out.println("\n=== EXAMPLE 10: Logical (methods) ===");
        Condition c1 = new Condition(true), c2 = new Condition(false), c3 = new Condition(true);
        System.out.println("c1: " + c1.value() + ", c2: " + c2.value());
        System.out.println("c1 AND c3: " + c1.and(c3).value());
        System.out.println("c1 OR c2: " + c1.or(c2).value());
        System.out.println("NOT c2: " + c2.not().value());
    }

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("   CHAPTER 8: OPERATOR OVERLOADING -> JAVA METHODS");
        System.out.println("======================================================");
        System.out.println("Reminder: Java has no user-defined operator overloading.");

        example1_arithmetic();
        example2_comparison();
        example3_subscript();
        example4_increment();
        example5_streamOutput();
        example6_assignment();
        example7_functionCall();
        example8_typeConversion();
        example9_memberAccess();
        example10_logical();

        // Bonus: demonstrate the only overloaded operator in Java ('+' on String)
        // and array sorting using compareTo from Example 2.
        Point[] pts = { new Point(3, 4), new Point(0, 1), new Point(2, 2) };
        Arrays.sort(pts); // uses Comparable.compareTo
        System.out.println("\nSorted points by magnitude: " + Arrays.toString(pts));

        System.out.println("\n======================================================");
        System.out.println("All examples completed!");
        System.out.println("======================================================");
    }
}
