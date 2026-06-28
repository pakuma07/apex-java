// Chapter 4: Pointers & References - Java adaptation of chapter4_pointers_references.cpp
// Compile: javac chapter4_pointers_references.java
// Run:     java chapter4_pointers_references
// Target: Java 17. Runs on JDK 17.
//
// BIG PICTURE: Java has NO raw pointers, no address-of (&), no dereference (*),
// no pointer arithmetic, and no manual new/delete. Instead it has:
//   - object references (handles to heap objects, managed by the GC)
//   - reference equality (==) vs value/content equality (.equals)
//   - pass-by-value of references (the reference is copied, the object is shared)
//   - autoboxing of primitives into wrapper objects
// Each example below adapts a C++ pointer/reference concept to its Java analogue.

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntBinaryOperator;

public class chapter4_pointers_references {

    // A tiny mutable holder used to stand in for "a pointer to an int".
    static class IntBox {
        int value;
        IntBox(int v) { value = v; }
    }

    // ============================================================
    // EXAMPLE 1: "Pointer" basics -> object references.
    // No &x or *ptr. A reference variable names a heap object; assigning
    // through the shared object is how you "modify through a pointer".
    // ============================================================
    static void example1_pointer_basics() {
        System.out.println("\n=== EXAMPLE 1: Reference Basics (Java has no raw pointers) ===");

        IntBox box = new IntBox(42);          // like int x = 42; with an object wrapper
        IntBox ref = box;                     // ref refers to the SAME object (an alias)

        System.out.println("box.value: " + box.value);
        System.out.println("Identity hash of box: " + System.identityHashCode(box));
        System.out.println("Identity hash of ref: " + System.identityHashCode(ref)
                + " (same object)");

        ref.value = 100;                       // modify through the alias
        System.out.println("After ref.value = 100, box.value = " + box.value);

        IntBox nullRef = null;                 // Java null == C++ nullptr
        if (nullRef == null) {
            System.out.println("nullRef is null");
        }
    }

    // ============================================================
    // EXAMPLE 2: Pointer arithmetic has NO Java analogue.
    // Array element access by index is the safe replacement; there is no
    // ptr++ stepping through memory. We show indexed traversal + bounds.
    // ============================================================
    static void example2_pointer_arithmetic() {
        System.out.println("\n=== EXAMPLE 2: No Pointer Arithmetic -> indexed access ===");

        int[] arr = {10, 20, 30, 40, 50};

        System.out.println("Array elements via index (no ptr++ in Java):");
        for (int i = 0; i < arr.length; i++) {
            System.out.println("arr[" + i + "] = " + arr[i]);
        }

        System.out.println("\nIndex-based 'arithmetic':");
        System.out.println("arr[0] = " + arr[0]);
        System.out.println("arr[0+2] = " + arr[2]);
        System.out.println("arr[0+4] = " + arr[4]);

        int i1 = 1, i2 = 4;
        System.out.println("Distance between index 4 and 1: " + (i2 - i1) + " elements");
    }

    // ============================================================
    // EXAMPLE 3: "Pointer to pointer" -> a reference to an object that
    // itself holds a reference. We nest holders to mimic int** .
    // ============================================================
    static void example3_pointer_to_pointer() {
        System.out.println("\n=== EXAMPLE 3: Reference-to-Reference (int** analogue) ===");

        IntBox x = new IntBox(100);            // the "int"
        IntBox[] ptr1 = { x };                 // a holder referring to x  (like int*)
        IntBox[][] ptr2 = { ptr1 };            // a holder referring to ptr1 (like int**)

        System.out.println("x.value = " + x.value);
        System.out.println("ptr1[0].value (value at ptr1) = " + ptr1[0].value);
        System.out.println("ptr2[0][0].value (value at *ptr2) = " + ptr2[0][0].value);

        ptr2[0][0].value = 999;                // mutate through two levels of indirection
        System.out.println("\nAfter ptr2[0][0].value = 999, x.value = " + x.value);
    }

    // ============================================================
    // EXAMPLE 4: References. Java references can be REBOUND (unlike C++
    // references), but doing so does NOT change the object previously
    // referred to. Compare with C++ where `ref = y` assigns into x.
    // ============================================================
    static void example4_references() {
        System.out.println("\n=== EXAMPLE 4: References ===");

        IntBox x = new IntBox(42);
        IntBox ref = x;  // alias to the same object

        System.out.println("x.value = " + x.value);
        System.out.println("ref.value = " + ref.value);
        System.out.println("ref and x same object? " + (ref == x));

        ref.value = 100;  // mutates the shared object
        System.out.println("\nAfter ref.value = 100:");
        System.out.println("x.value = " + x.value);
        System.out.println("ref.value = " + ref.value);

        // Rebinding the Java reference (no C++ equivalent for true references):
        IntBox y = new IntBox(200);
        ref = y;  // ref now points at a DIFFERENT object; x is untouched
        System.out.println("\nAfter ref = y (rebinding the reference):");
        System.out.println("x.value = " + x.value + " (unchanged, unlike C++)");
        System.out.println("y.value = " + y.value);
        System.out.println("ref.value = " + ref.value + " (now aliases y)");
    }

    // ============================================================
    // EXAMPLE 5: References vs (C++) pointers, expressed in Java terms.
    // ============================================================
    static void example5_refs_vs_pointers() {
        System.out.println("\n=== EXAMPLE 5: Java References vs C++ Pointers ===");

        IntBox x = new IntBox(10);

        IntBox ref = x;
        System.out.println("Java reference:");
        System.out.println("  ref.value = " + ref.value);
        System.out.println("  No explicit dereference operator (* )");
        System.out.println("  CAN be null");
        System.out.println("  CAN be reassigned to another object");

        System.out.println("\nC++ pointer (for contrast):");
        System.out.println("  needs *ptr to dereference");
        System.out.println("  supports pointer arithmetic (Java does not)");
        System.out.println("  not garbage collected (Java references are)");

        IntBox y = new IntBox(20);
        ref = y;  // reassignment, allowed for Java references
        System.out.println("  After ref = y, ref.value = " + ref.value);
    }

    // ============================================================
    // EXAMPLE 6: "Pass by reference" for swap. Java is pass-by-value, so
    // swapping two ints inside a method cannot affect the caller. We show
    // the failing value version and the working holder/array version.
    // ============================================================
    static void swapByValue(int a, int b) {
        System.out.println("\n  swapByValue: BEFORE swap - a=" + a + ", b=" + b);
        int temp = a;
        a = b;
        b = temp;
        System.out.println("  swapByValue: AFTER swap - a=" + a + ", b=" + b);
    }

    static void swapViaArray(int[] pair) {
        System.out.println("\n  swapViaArray: BEFORE swap - a=" + pair[0] + ", b=" + pair[1]);
        int temp = pair[0];
        pair[0] = pair[1];
        pair[1] = temp;
        System.out.println("  swapViaArray: AFTER swap - a=" + pair[0] + ", b=" + pair[1]);
    }

    static void example6_pass_by_reference() {
        System.out.println("\n=== EXAMPLE 6: Pass by Value (swap workaround) ===");

        int x = 5, y = 10;
        System.out.println("Original: x=" + x + ", y=" + y);

        swapByValue(x, y);
        System.out.println("After swapByValue: x=" + x + ", y=" + y + " (unchanged)");

        int[] pair = {x, y};
        swapViaArray(pair);
        System.out.println("After swapViaArray: x=" + pair[0] + ", y=" + pair[1] + " (changed)");
    }

    // ============================================================
    // EXAMPLE 7: "Function pointers" -> functional interfaces / method refs.
    // ============================================================
    static int add(int a, int b)      { return a + b; }
    static int subtract(int a, int b) { return a - b; }
    static int multiply(int a, int b) { return a * b; }

    static int applyOperation(int a, int b, IntBinaryOperator func) {
        return func.applyAsInt(a, b);
    }

    static void example7_function_pointers() {
        System.out.println("\n=== EXAMPLE 7: Function References ===");

        IntBinaryOperator[] operations = {
            chapter4_pointers_references::add,
            chapter4_pointers_references::subtract,
            chapter4_pointers_references::multiply
        };

        System.out.println("Using an array of function references:");
        System.out.println("10 + 5 = " + applyOperation(10, 5, operations[0]));
        System.out.println("10 - 5 = " + applyOperation(10, 5, operations[1]));
        System.out.println("10 * 5 = " + applyOperation(10, 5, operations[2]));

        IntBinaryOperator op = chapter4_pointers_references::add;
        System.out.println("\nUsing a function reference directly:");
        System.out.println("20 + 3 = " + op.applyAsInt(20, 3));

        op = chapter4_pointers_references::subtract;  // rebind
        System.out.println("20 - 3 = " + op.applyAsInt(20, 3));
    }

    // ============================================================
    // EXAMPLE 8: Dynamic memory. Java has `new` but NO delete: the garbage
    // collector reclaims unreachable objects. We allocate and then just
    // drop the reference (set to null) to make it eligible for GC.
    // ============================================================
    static void example8_dynamic_memory() {
        System.out.println("\n=== EXAMPLE 8: Allocation & Garbage Collection ===");

        IntBox single = new IntBox(42);  // like new int; but GC-managed
        System.out.println("Single allocated: " + single.value);
        single = null;  // no delete; eligible for GC once unreachable
        System.out.println("Reference dropped (set to null); GC reclaims it later");

        int size = 5;
        int[] arr = new int[size];  // like new int[size]; no delete[] needed
        System.out.println("\nDynamic array:");
        for (int i = 0; i < size; i++) {
            arr[i] = (i + 1) * 10;
            System.out.println("arr[" + i + "] = " + arr[i]);
        }
        arr = null;  // again, no manual deallocation
        System.out.println("Array reference dropped; memory freed automatically");
    }

    // ============================================================
    // EXAMPLE 9: "Smart pointers". Java references already behave like a
    // shared, reference-counted (actually tracing-GC) smart pointer. There
    // is no unique_ptr/shared_ptr; the GC tracks reachability. We illustrate
    // shared ownership: two references to one object, drop one, object lives.
    // ============================================================
    static void example9_smart_pointers() {
        System.out.println("\n=== EXAMPLE 9: GC vs Smart Pointers ===");

        // "unique-like": single owner, then transfer by reassigning.
        IntBox owner = new IntBox(100);
        System.out.println("owner.value: " + owner.value);
        IntBox newOwner = owner;  // both now refer to it
        owner = null;             // "moved": original handle released
        System.out.println("After transfer, newOwner.value: " + newOwner.value);

        // "shared-like": multiple references keep the object alive.
        List<String> shared = new ArrayList<>(List.of("Hello"));
        List<String> alias = shared;  // second reference to the SAME list
        System.out.println("\nTwo references to one object; value: " + alias.get(0));
        alias = null;  // drop one reference; object still reachable via `shared`
        System.out.println("After dropping one reference, still alive: " + shared.get(0));
        System.out.println("GC frees the object only when NO references remain");
    }

    // ============================================================
    // EXAMPLE 10: "Const references". Java has no const parameter qualifier.
    // The closest analogues: pass an immutable view (List.copyOf /
    // Collections.unmodifiableList) so the callee cannot modify it, and
    // use `final` on locals/params to prevent rebinding the reference.
    // ============================================================
    static void printList(final List<Integer> v) {
        System.out.print("List (unmodifiable view): ");
        for (int x : v) System.out.print(x + " ");
        System.out.println();
        // v.add(99);  // would throw UnsupportedOperationException on an unmodifiable list
    }

    static void example10_const_references() {
        System.out.println("\n=== EXAMPLE 10: Immutability instead of const ===");

        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5));

        printList(List.copyOf(nums));  // pass an unmodifiable snapshot

        System.out.print("Original list unchanged: ");
        for (int x : nums) System.out.print(x + " ");
        System.out.println();

        final int x = 42;  // final local: cannot be reassigned (like a const)
        System.out.println("\nfinal int x = " + x + " (cannot be reassigned)");
        // x = 100;  // compile error: cannot assign a value to final variable x
    }

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("   CHAPTER 4: POINTERS & REFERENCES (Java adaptation)");
        System.out.println("======================================================");

        example1_pointer_basics();
        example2_pointer_arithmetic();
        example3_pointer_to_pointer();
        example4_references();
        example5_refs_vs_pointers();
        example6_pass_by_reference();
        example7_function_pointers();
        example8_dynamic_memory();
        example9_smart_pointers();
        example10_const_references();

        System.out.println("\n======================================================");
        System.out.println("All examples completed!");
        System.out.println("======================================================");
    }
}
