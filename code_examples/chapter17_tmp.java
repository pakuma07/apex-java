// Chapter 17: Reflection & Annotations (Java analogue of C++ Template Metaprogramming)
// Compile: javac chapter17_tmp.java
// Run:     java chapter17_tmp
//
// WHY THIS IS DIFFERENT FROM C++ TMP
// ----------------------------------
// C++ template metaprogramming runs ENTIRELY AT COMPILE TIME: the compiler
// instantiates templates, computes Factorial<5>::value, picks overloads via
// tag dispatch, and folds constexpr functions into constants -- producing
// ZERO runtime cost. There is no template machinery left in the binary.
//
// Java has NO compile-time computation of this kind. Java generics are ERASED
// at runtime (type erasure), so the closest analogue to "metaprogramming" is
// REFLECTION: inspecting and manipulating classes/methods/fields AT RUNTIME,
// driven by ANNOTATIONS. So:
//   C++ TMP            -> compile-time, no runtime trace, fails to compile on error
//   Java reflection    -> runtime, has overhead, fails with exceptions at runtime
//
// This file mirrors the C++ chapter's spirit (introspection, dispatch,
// policies, traits) using Java reflection + custom runtime annotations.

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class chapter17_tmp {

    // ============================================================
    // Custom runtime annotations (the "metadata" reflection reads)
    // ============================================================
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Component {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Benchmark {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Inject {}

    // ============================================================
    // Example 1: Compile-time TMP -> runtime recursion + inspection
    // ============================================================
    static long factorial(int n) { return n <= 1 ? 1 : n * factorial(n - 1); }
    static long fib(int n) { return n <= 1 ? n : fib(n - 1) + fib(n - 2); }

    static void example1_runtimeCompute() {
        System.out.println("=== Example 1: Compute (runtime, not compile-time) ===");
        System.out.println("factorial(5)  = " + factorial(5));   // 120
        System.out.println("factorial(10) = " + factorial(10));  // 3628800
        System.out.println("fib(10)       = " + fib(10));        // 55
        System.out.println("fib(15)       = " + fib(15));        // 610
        System.out.println("In C++ these would be Factorial<5>::value folded by the compiler.");
    }

    // ============================================================
    // Example 2: Type Traits -> runtime reflection on Class objects
    // ============================================================
    static void example2_typeTraits() {
        System.out.println("\n=== Example 2: Type Traits via Reflection ===");
        System.out.println("int.class == int.class      : " + (int.class == int.class));
        System.out.println("int.class == float.class    : " + (int.class == (Object) float.class));
        System.out.println("int[].class isArray         : " + int[].class.isArray());
        System.out.println("Integer is primitive        : " + Integer.class.isPrimitive());
        System.out.println("int is primitive            : " + int.class.isPrimitive());
        System.out.println("String superclass           : " + String.class.getSuperclass().getSimpleName());
        System.out.println("ArrayList interfaces        : " + List.of(
                java.util.Arrays.toString(ArrayList.class.getInterfaces())));
    }

    // ============================================================
    // Example 3: Tag Dispatch -> runtime dynamic dispatch / instanceof
    // ============================================================
    static void advance(Object iterable) {
        // C++ chose the overload at compile time via tags; Java inspects at runtime.
        if (iterable instanceof java.util.RandomAccess) {
            System.out.println("[fast] random-access container: " + iterable.getClass().getSimpleName());
        } else {
            System.out.println("[slow] sequential container: " + iterable.getClass().getSimpleName());
        }
    }

    static void example3_dispatch() {
        System.out.println("\n=== Example 3: Dispatch (runtime instanceof) ===");
        advance(new ArrayList<>(List.of(1, 2, 3)));   // ArrayList implements RandomAccess
        advance(new java.util.LinkedList<>(List.of(1, 2, 3)));
    }

    // ============================================================
    // Example 4: Policy-Based Design -> annotation-driven behaviour
    // A @Component class whose methods/fields are discovered reflectively.
    // ============================================================
    @Component("ServiceA")
    static class Service {
        @Inject String dependency = "DB";
        @Benchmark void doWork() { /* pretend work */ }
        void helper() { /* not benchmarked */ }
    }

    static void example4_annotationPolicy() throws Exception {
        System.out.println("\n=== Example 4: Annotation-Driven Policy ===");
        Class<?> cls = Service.class;

        Component comp = cls.getAnnotation(Component.class);
        if (comp != null) {
            System.out.println("Discovered @Component named: '" + comp.value() + "'");
        }
        for (Method m : cls.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Benchmark.class)) {
                System.out.println("Method @Benchmark: " + m.getName() + "()");
            }
        }
        for (Field f : cls.getDeclaredFields()) {
            if (f.isAnnotationPresent(Inject.class)) {
                System.out.println("Field @Inject: " + f.getName() + " (type " + f.getType().getSimpleName() + ")");
            }
        }
    }

    // ============================================================
    // Example 5: Expression templates -> dynamic method invocation
    // ============================================================
    static class Calc {
        public int add(int a, int b) { return a + b; }
        public int mul(int a, int b) { return a * b; }
    }

    static void example5_dynamicInvoke() throws Exception {
        System.out.println("\n=== Example 5: Dynamic Invocation (reflection) ===");
        Calc calc = new Calc();
        for (String name : new String[]{"add", "mul"}) {
            Method m = Calc.class.getMethod(name, int.class, int.class);
            Object result = m.invoke(calc, 6, 7);
            System.out.println("invoked " + name + "(6,7) -> " + result);
        }
    }

    // ============================================================
    // Example 6: constexpr -> static final constants + reflection of them
    // ============================================================
    static final int POW_2_10 = 1024;
    static final int GCD_48_18 = 6;

    static void example6_constants() throws Exception {
        System.out.println("\n=== Example 6: Constants (runtime final, not constexpr) ===");
        for (Field f : chapter17_tmp.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                    && f.getType() == int.class) {
                System.out.println("static final int " + f.getName() + " = " + f.getInt(null));
            }
        }
        System.out.println("Java cannot fold these at compile time the way C++ constexpr does.");
    }

    // ============================================================
    // Example 7: CRTP / generics introspection (type erasure caveat)
    // ============================================================
    // A field with a generic type whose argument we can recover via reflection,
    // even though erasure removes it from the runtime instance itself.
    static class Box {
        List<String> names = new ArrayList<>();
    }

    static void example7_genericsIntrospection() throws Exception {
        System.out.println("\n=== Example 7: Generics Introspection (erasure) ===");
        Field f = Box.class.getDeclaredField("names");
        System.out.println("Raw runtime type (erased): " + f.getType().getSimpleName());

        Type generic = f.getGenericType();
        if (generic instanceof ParameterizedType pt) {
            // The declared type argument survives in the .class metadata (not the object).
            Type arg = pt.getActualTypeArguments()[0];
            System.out.println("Declared type argument: " + arg.getTypeName());
        }
        System.out.println("Lesson: instances are erased; declarations keep generic signatures.");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("======================================================");
        System.out.println("   CHAPTER 17: REFLECTION & ANNOTATIONS (Java)");
        System.out.println("   (the runtime analogue of C++ template metaprogramming)");
        System.out.println("======================================================");

        example1_runtimeCompute();
        example2_typeTraits();
        example3_dispatch();
        example4_annotationPolicy();
        example5_dynamicInvoke();
        example6_constants();
        example7_genericsIntrospection();

        System.out.println("\n======================================================");
        System.out.println("All examples completed!");
        System.out.println("======================================================");
    }
}
