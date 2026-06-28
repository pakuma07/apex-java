// Chapter 9: Templates -> Java GENERICS - Runnable Examples
// Compile/run on Java 17:
//   javac chapter9_templates.java
//   java  chapter9_templates
//
// BIG PICTURE DIFFERENCE: C++ templates vs Java generics
//   - C++ templates are COMPILE-TIME CODE GENERATION: the compiler stamps out
//     a separate, specialized version of the class/function for every type used
//     ("template instantiation"). Each instantiation is real, distinct code.
//   - Java generics use TYPE ERASURE: there is ONE compiled class. Type
//     parameters exist only at compile time for type checking; at runtime they
//     are erased to their bound (usually Object). Consequences:
//       * No "new T()", no "new T[]", no T.class - the type isn't available.
//       * No primitive type arguments (must box: Integer not int).
//       * No template specialization for specific types; instead we use
//         bounded type parameters and overloading.
//   - Java has wildcards (? extends / ? super) for use-site variance, which
//     C++ does not need because each instantiation is concrete.

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class chapter9_templates {

    // ============================================================
    // EXAMPLE 1: Generic Methods (C++ function templates)
    // <T extends Comparable<T>> is a BOUNDED type parameter so we can call
    // compareTo - the equivalent of relying on operator> in C++.
    // ============================================================
    static <T extends Comparable<T>> T maximum(T a, T b) {
        return (a.compareTo(b) > 0) ? a : b;
    }

    // Java passes references by value, so a generic swap of two locals cannot
    // affect the caller. We demonstrate swapping inside a list instead.
    static <T> void swapInList(List<T> list, int i, int j) {
        T tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }

    static void example1_genericMethods() {
        System.out.println("\n=== EXAMPLE 1: Generic Methods ===");
        System.out.println("Maximum of 5 and 3: " + maximum(5, 3));
        System.out.println("Maximum of 3.5 and 2.1: " + maximum(3.5, 2.1));
        System.out.println("Maximum of \"apple\" and \"banana\": " + maximum("apple", "banana"));

        List<Integer> nums = new ArrayList<>(List.of(10, 20));
        System.out.println("Before swap: " + nums);
        swapInList(nums, 0, 1);
        System.out.println("After swap: " + nums);
        System.out.println("(Java cannot swap two local variables generically - pass-by-value)");
    }

    // ============================================================
    // EXAMPLE 2: Generic Classes (C++ class templates)
    // ============================================================
    static class Stack<T> {
        private final List<T> data = new ArrayList<>();
        void push(T value) { data.add(value); }
        T pop() { return data.remove(data.size() - 1); }
        boolean isEmpty() { return data.isEmpty(); }
        int size() { return data.size(); }
    }

    static void example2_genericClasses() {
        System.out.println("\n=== EXAMPLE 2: Generic Classes ===");
        Stack<Integer> intStack = new Stack<>();
        intStack.push(10); intStack.push(20); intStack.push(30);
        System.out.println("Integer stack size: " + intStack.size());
        System.out.println("Pop: " + intStack.pop());
        System.out.println("Pop: " + intStack.pop());

        Stack<String> stringStack = new Stack<>();
        stringStack.push("Hello"); stringStack.push("World");
        System.out.println("String stack size: " + stringStack.size());
        System.out.println("Pop: " + stringStack.pop());
    }

    // ============================================================
    // EXAMPLE 3: "Specialization" via overloading
    // Java has NO template specialization. The closest idiom is method
    // overloading: the compiler picks the most specific overload.
    // ============================================================
    static void display(Object value) { System.out.println("Generic: " + value); }
    static void display(boolean value) { System.out.println("Overload for boolean: " + value); }
    static void display(String value) { System.out.println("Overload for String: \"" + value + "\""); }

    static void example3_specialization() {
        System.out.println("\n=== EXAMPLE 3: Specialization via Overloading ===");
        display(42);       // -> Object overload (autoboxed Integer)
        display(3.14);     // -> Object overload (autoboxed Double)
        display(true);     // -> boolean overload
        display("Hello");  // -> String overload
    }

    // ============================================================
    // EXAMPLE 4: Multiple type parameters (C++ <typename K, typename V>)
    // ============================================================
    static class Pair<K, V> {
        private final K key; private final V value;
        Pair(K k, V v) { key = k; value = v; }
        K getKey() { return key; }
        V getValue() { return value; }
        void display() { System.out.println("Key: " + key + ", Value: " + value); }
    }

    static void example4_multipleParameters() {
        System.out.println("\n=== EXAMPLE 4: Multiple Type Parameters ===");
        new Pair<String, Integer>("age", 25).display();
        new Pair<String, Double>("height", 5.9).display();
        new Pair<Integer, String>(1, "one").display();
    }

    // ============================================================
    // EXAMPLE 5: No default type parameters in Java
    // C++ templates allow default args (typename T = int, int SIZE = 10).
    // Java generics do NOT. The idiom is overloaded constructors / factories.
    // ============================================================
    static class Bag<T> {
        private final List<T> items = new ArrayList<>();
        void add(T value) { items.add(value); }
        void display() { System.out.println("Bag (" + items.size() + " elements): " + items); }
    }

    static void example5_defaults() {
        System.out.println("\n=== EXAMPLE 5: (No) Default Type Parameters ===");
        Bag<Integer> a1 = new Bag<>(); // you must state the type argument
        a1.add(1); a1.add(2); a1.add(3);
        a1.display();
        Bag<Double> a2 = new Bag<>();
        a2.add(1.1); a2.add(2.2);
        a2.display();
        System.out.println("Java has no default type args; specify them explicitly");
    }

    // ============================================================
    // EXAMPLE 6: Varargs (C++ variadic templates -> Java varargs)
    // C++ variadic templates are type-safe per type; Java varargs collect
    // arguments into an array (here Object... ).
    // ============================================================
    @SafeVarargs
    static void printAll(Object... args) {
        StringBuilder sb = new StringBuilder();
        for (Object o : args) sb.append(o).append(' ');
        System.out.println(sb.toString().trim());
    }

    static void example6_varargs() {
        System.out.println("\n=== EXAMPLE 6: Varargs ===");
        System.out.print("Printing multiple values: ");
        printAll(1, 2.5, "hello", true, 42);
    }

    // ============================================================
    // EXAMPLE 7: Constraints via BOUNDED type parameters
    // C++ uses enable_if/type traits. Java uses bounds: <T extends Number>.
    // ============================================================
    static <T extends Number> void processNumber(T value) {
        System.out.println("Number (" + value.getClass().getSimpleName()
                + "), doubleValue = " + value.doubleValue());
    }

    static void example7_constraints() {
        System.out.println("\n=== EXAMPLE 7: Constraints (bounded type parameters) ===");
        processNumber(42);     // Integer
        processNumber(3.14);   // Double
        processNumber(7L);     // Long
        System.out.println("<T extends Number> restricts T to numeric types at compile time");
    }

    // ============================================================
    // EXAMPLE 8: Wildcards (Java-only; C++ has no analogue)
    // ? extends Number  -> producer (read), covariant
    // ? super Integer   -> consumer (write), contravariant  (PECS rule)
    // ============================================================
    static double sumOfList(List<? extends Number> list) { // upper-bounded wildcard
        double sum = 0;
        for (Number n : list) sum += n.doubleValue();
        return sum;
    }
    static void addIntegers(List<? super Integer> list) { // lower-bounded wildcard
        for (int i = 1; i <= 3; i++) list.add(i);
    }

    static void example8_wildcards() {
        System.out.println("\n=== EXAMPLE 8: Wildcards (? extends / ? super) ===");
        List<Integer> ints = List.of(1, 2, 3);
        List<Double> dbls = List.of(1.5, 2.5);
        System.out.println("Sum of ints: " + sumOfList(ints));
        System.out.println("Sum of doubles: " + sumOfList(dbls));
        List<Number> sink = new ArrayList<>();
        addIntegers(sink);
        System.out.println("After addIntegers into List<Number>: " + sink);
        System.out.println("Wildcards give use-site variance; C++ templates don't need them");
    }

    // ============================================================
    // EXAMPLE 9: Generic method inside a generic class
    // Mirrors the C++ "template member function" (count_if with a predicate).
    // ============================================================
    static class Container<T> {
        private final List<T> items = new ArrayList<>();
        void add(T item) { items.add(item); }
        int countIf(Predicate<T> pred) {
            int cnt = 0;
            for (T item : items) if (pred.test(item)) cnt++;
            return cnt;
        }
    }

    static void example9_genericMethodInGenericClass() {
        System.out.println("\n=== EXAMPLE 9: Generic Method in Generic Class ===");
        Container<Integer> c = new Container<>();
        for (int i = 1; i <= 5; i++) c.add(i);
        int evenCount = c.countIf(x -> x % 2 == 0); // predicate lambda
        System.out.println("Even numbers: " + evenCount);
    }

    // ============================================================
    // EXAMPLE 10: Type erasure demonstrated (vs C++ instantiation/SFINAE)
    // At runtime List<Integer> and List<String> are the SAME class.
    // ============================================================
    static void example10_typeErasure() {
        System.out.println("\n=== EXAMPLE 10: Type Erasure (vs C++ instantiation) ===");
        List<Integer> ints = new ArrayList<>();
        List<String> strs = new ArrayList<>();
        System.out.println("ints.getClass()  = " + ints.getClass().getName());
        System.out.println("strs.getClass()  = " + strs.getClass().getName());
        System.out.println("Same runtime class? " + (ints.getClass() == strs.getClass()));
        System.out.println("=> Generic type info is ERASED at runtime (single class).");
        System.out.println("C++ would generate TWO distinct concrete types instead.");
    }

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("   CHAPTER 9: TEMPLATES -> JAVA GENERICS (Java 17)");
        System.out.println("======================================================");

        example1_genericMethods();
        example2_genericClasses();
        example3_specialization();
        example4_multipleParameters();
        example5_defaults();
        example6_varargs();
        example7_constraints();
        example8_wildcards();
        example9_genericMethodInGenericClass();
        example10_typeErasure();

        System.out.println("\n======================================================");
        System.out.println("All examples completed!");
        System.out.println("======================================================");
    }
}
