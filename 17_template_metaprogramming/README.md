# Chapter 17: Reflection, Annotations & Metaprogramming

C++ does its metaprogramming **at compile time** using templates: the compiler is the interpreter, and template instantiation is the execution mechanism, producing zero-runtime-overhead abstractions. **Java has no template metaprogramming.** Java generics are erased (no per-type code generation), and there is no Turing-complete type-level computation. Instead, Java performs metaprogramming in two places:

1. **At runtime**, via the **Reflection API**, **dynamic proxies**, and **MethodHandles/VarHandle** — inspecting and manipulating types, methods, and fields while the program runs.
2. **At build time**, via **annotation processing** (APT) and **annotations** — generating or validating source code before the program runs.

> **The fundamental contrast.** C++ TMP: *compile-time*, zero runtime cost, computation in the type system. Java: *runtime* introspection (reflection has a cost) plus *build-time* code generation (annotation processors). Where C++ asks "what can the compiler compute about types?", Java asks "what can the program discover and do with types while running?"

---

## 17.1 What Replaces Template Metaprogramming in Java?

There is no Java equivalent to compile-time `Factorial<5>::value`. A C++ recursive template computes a value the compiler bakes into the binary; the Java analog is just a `static final` constant (computed by the compiler as a constant expression) or a plain method:

```java
public class Main {
    // Java's "compile-time constant": a constant expression folded by javac
    static final int FACT5 = 1 * 2 * 3 * 4 * 5;     // 120 — folded at compile time

    // Runtime version (no compile-time template machinery exists)
    static int factorial(int n) {
        return (n <= 1) ? 1 : n * factorial(n - 1);
    }

    public static void main(String[] args) {
        System.out.println(FACT5);          // 120
        System.out.println(factorial(10));  // 3628800
    }
}
```

**Why the difference?**
- C++ templates are Turing-complete and instantiated per type — *code is generated* for each instantiation.
- Java generics use **type erasure**: `List<Integer>` and `List<String>` share one compiled class. No code is specialized per type, so there is nothing to "compute" at the type level.

So Java's metaprogramming toolbox is: **Reflection**, **Annotations + processors**, **Dynamic Proxies**, and **MethodHandles/VarHandle** — the subject of this chapter.

| C++ technique | Java counterpart |
|---|---|
| Struct-value TMP (`Factorial<N>`) | `static final` constants / plain recursion |
| Type traits (`is_pointer<T>`) | `Class` reflection (`Class::isArray`, `isInterface`, ...) |
| Tag dispatch | Runtime `instanceof` / pattern matching, or overloading |
| Policy-based design | Strategy interfaces injected at runtime (composition) |
| `constexpr` | `static final` constant expressions |
| Variadic templates | Varargs + generics (erased) |
| SFINAE / detection | Reflection probing for members at runtime |
| (build-time codegen) | **Annotation processing (APT)** |

---

## 17.2 The Reflection API: `Class`, `Method`, `Field`, `Constructor`

Reflection lets a running program inspect and manipulate types it may not have known at compile time. Every loaded type has a `Class<?>` object — the runtime entry point, analogous to (but far richer than) C++ RTTI's `typeid`.

```java
import java.lang.reflect.*;

class Point {
    public int x, y;
    private String label = "p";
    public Point() {}
    public Point(int x, int y) { this.x = x; this.y = y; }
    public int distSq() { return x * x + y * y; }
    private void secret() { System.out.println("hidden"); }
}

public class Main {
    public static void main(String[] args) throws Exception {
        // Three ways to obtain a Class object
        Class<?> c1 = Point.class;                 // class literal (compile-time)
        Class<?> c2 = new Point().getClass();      // from an instance
        Class<?> c3 = Class.forName("Point");      // by name (runtime, dynamic)

        System.out.println(c1.getName());          // Point
        System.out.println(c1.getSuperclass());    // class java.lang.Object

        // Enumerate members
        for (Field f : c1.getDeclaredFields())
            System.out.println("field: " + f.getType() + " " + f.getName());
        for (Method m : c1.getDeclaredMethods())
            System.out.println("method: " + m.getName());
        for (Constructor<?> k : c1.getDeclaredConstructors())
            System.out.println("ctor params: " + k.getParameterCount());
    }
}
```

### `getDeclaredX` vs `getX`
- `getMethods()` / `getFields()` — **public** members, including inherited ones.
- `getDeclaredMethods()` / `getDeclaredFields()` — **all** members declared by this class (any access level), but *not* inherited.

---

## 17.3 Dynamic Invocation

The real power of reflection is invoking methods and reading/writing fields chosen at runtime — including private members (subject to the module system and `setAccessible`).

```java
import java.lang.reflect.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("Point");

        // Construct an instance reflectively
        Constructor<?> ctor = c.getConstructor(int.class, int.class);
        Object p = ctor.newInstance(3, 4);

        // Invoke a public method by name
        Method distSq = c.getMethod("distSq");
        Object result = distSq.invoke(p);          // boxed Integer
        System.out.println(result);                // 25

        // Read and write a public field
        Field x = c.getField("x");
        System.out.println(x.getInt(p));           // 3
        x.setInt(p, 10);

        // Access a private member (requires setAccessible)
        Field label = c.getDeclaredField("label");
        label.setAccessible(true);                 // bypass access check (if allowed)
        System.out.println(label.get(p));          // p

        Method secret = c.getDeclaredMethod("secret");
        secret.setAccessible(true);
        secret.invoke(p);                          // hidden
    }
}
```

### Reflection vs C++ RTTI
| Capability | C++ | Java |
|---|---|---|
| Runtime type identity | `typeid`, `type_info` | `Class<?>` |
| Safe downcast | `dynamic_cast` | `instanceof` + cast, `Class::cast` |
| List a type's members | **Not possible** | `getDeclaredFields/Methods` |
| Call a method by string name | **Not possible** | `Method.invoke` |
| Construct a type by name | **Not possible** | `Class.forName(...).newInstance(...)` |

Reflection has costs C++ TMP does not: runtime overhead, loss of compile-time type checking, and exceptions (`NoSuchMethodException`, `IllegalAccessException`) instead of compile errors. Use it for frameworks (DI, serialization, ORMs, test runners), not hot paths.

---

## 17.4 Generics and Type Erasure (Why Reflection Has Limits)

Because Java generics are **erased**, reflection on a generic type sees only the raw type at the object level — there is no `List<String>.class`. This is the deepest difference from C++ templates, where each instantiation is a distinct, fully-typed entity.

```java
import java.util.*;
import java.lang.reflect.*;

List<String> a = new ArrayList<>();
List<Integer> b = new ArrayList<>();
System.out.println(a.getClass() == b.getClass());   // true! same erased class

// Generic type arguments ARE preserved in *signatures* (fields, methods, supertypes),
// just not in object instances:
class Box extends ArrayList<String> {}
ParameterizedType pt = (ParameterizedType) Box.class.getGenericSuperclass();
System.out.println(pt.getActualTypeArguments()[0]);  // class java.lang.String
```

### Bounded type parameters and reflection tricks

A common idiom — pass a `Class<T>` token to recover the type erasure stole, and use **bounded type parameters** to constrain generics (the runtime analog of C++ template constraints / concepts):

```java
// "Super type token": capture a generic type via an anonymous subclass
abstract class TypeRef<T> {
    final java.lang.reflect.Type type =
        ((java.lang.reflect.ParameterizedType) getClass().getGenericSuperclass())
            .getActualTypeArguments()[0];
}
var ref = new TypeRef<List<String>>() {};            // captures List<String>
System.out.println(ref.type);                        // java.util.List<java.lang.String>

// Class token pattern — recover the runtime type
static <T> T create(Class<T> type) throws Exception {
    return type.getDeclaredConstructor().newInstance();
}

// Bounded type parameter: T must be Comparable (a compile-time constraint)
static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}
```

> C++ would express `max` with a template + `concept`; Java expresses it with `<T extends Comparable<T>>`. Both are checked at compile time, but Java's is erased afterward.

---

## 17.5 Annotations: Built-in, Custom, Meta-Annotations

Annotations attach **metadata** to declarations. They are Java's primary metaprogramming hook: read at compile time by processors, or at runtime by reflection. C++ has no direct equivalent (the nearest modern idea is C++11 `[[attributes]]`, but those are far less expressive).

### Built-in annotations

```java
class Base { void run() {} }
class Derived extends Base {
    @Override void run() {}            // compile error if it doesn't override
    @Deprecated void old() {}          // warn on use
    @SuppressWarnings("unchecked")     // silence a specific compiler warning
    void f() {}
}
@FunctionalInterface                   // enforce exactly one abstract method
interface Op { int apply(int x); }
```

### Defining a custom annotation with meta-annotations

**Meta-annotations** annotate annotations — they configure *where* an annotation may appear (`@Target`) and *how long* it survives (`@Retention`).

```java
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)    // keep in the class file AND visible to reflection
@Target({ElementType.METHOD, ElementType.TYPE})   // where it may be applied
@Documented
@interface Benchmark {
    int iterations() default 1;        // annotation "members" (no body, only attributes)
    String label() default "";
}

@Benchmark(iterations = 100, label = "hot path")
class Service {
    @Benchmark
    void compute() {}
}
```

### `@Retention` policies — the lifetime axis
| Policy | Lives until | Read by |
|---|---|---|
| `SOURCE` | Compilation only (discarded) | Annotation processors, lint tools (`@Override`) |
| `CLASS` | In the `.class` file, not loaded | Bytecode tools |
| `RUNTIME` | Loaded into the JVM | **Reflection** at runtime |

### `@Target` element types
`TYPE`, `METHOD`, `FIELD`, `PARAMETER`, `CONSTRUCTOR`, `LOCAL_VARIABLE`, `ANNOTATION_TYPE`, `PACKAGE`, `TYPE_PARAMETER`, `TYPE_USE`, `RECORD_COMPONENT`.

---

## 17.6 Reading Annotations at Runtime

Only `RUNTIME`-retained annotations are visible to reflection. Frameworks (JUnit, Spring, JPA) are essentially big reflective annotation readers. Here is a miniature `@Benchmark` runner — note how it mirrors C++ tag dispatch in *spirit* (select behavior from a type's metadata) but does it at **runtime**:

```java
import java.lang.annotation.*;
import java.lang.reflect.*;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface Benchmark { int iterations() default 1; }

class Tasks {
    @Benchmark(iterations = 3) public void work() { System.out.println("working"); }
    public void untracked()  { /* no annotation */ }
}

public class Main {
    public static void main(String[] args) throws Exception {
        Object obj = new Tasks();
        for (Method m : obj.getClass().getDeclaredMethods()) {
            Benchmark b = m.getAnnotation(Benchmark.class);
            if (b != null) {                                  // metadata-driven dispatch
                System.out.println("Running " + m.getName()
                                   + " x" + b.iterations());
                for (int i = 0; i < b.iterations(); i++) m.invoke(obj);
            }
        }
    }
}
```

This is the runtime analog of compile-time selection: C++ chooses an overload from a tag type during instantiation; Java inspects an annotation during execution and dispatches accordingly.

---

## 17.7 Annotation Processing (APT) — Build-Time Metaprogramming

This is Java's closest match to C++ TMP's *spirit*: **code transformation before the program runs, with zero runtime cost**. An **annotation processor** runs inside `javac`, reads `SOURCE`/`CLASS` annotations, and generates new source files. Libraries like Lombok, Dagger, AutoValue, and MapStruct use this to generate boilerplate.

```java
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.util.Set;

@SupportedAnnotationTypes("com.example.GenerateBuilder")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class BuilderProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment env) {
        for (Element e : env.getElementsAnnotatedWith(
                processingEnv.getElementUtils().getTypeElement("com.example.GenerateBuilder"))) {
            // Inspect `e` (a TypeElement) and write a new .java file via
            // processingEnv.getFiler().createSourceFile(...)
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE, "Generating builder for " + e.getSimpleName());
        }
        return true;   // claim the annotations
    }
}
```

How it runs:
- The processor is discovered via `META-INF/services/javax.annotation.processing.Processor` (or `-processor`).
- It operates on the **language model** (`javax.lang.model`: `Element`, `TypeMirror`) — *not* reflection. There are no instances yet; the code is still being compiled.
- It emits source with the `Filer`, which `javac` then compiles in a subsequent round.

| Axis | C++ TMP | Java APT |
|---|---|---|
| Runs | During compilation (template instantiation) | During compilation (extra `javac` rounds) |
| Operates on | Types, in the type system | Source model (`Element`/`TypeMirror`) |
| Output | Specialized instantiated code | New `.java` source files |
| Runtime cost | Zero | Zero |
| Turing-complete? | Yes | The processor is ordinary Java, so yes |

---

## 17.8 Dynamic Proxies (`java.lang.reflect.Proxy`)

A **dynamic proxy** synthesizes, at runtime, a class implementing a set of interfaces, routing every method call to a single `InvocationHandler`. This enables AOP-style cross-cutting behavior (logging, transactions, lazy loading, mocking) with no generated source. C++ has no runtime equivalent — the nearest static analog is CRTP/policy mixins, but those are compile-time and per-type.

```java
import java.lang.reflect.*;

interface Greeter {
    String greet(String name);
}

public class Main {
    public static void main(String[] args) {
        InvocationHandler handler = (proxy, method, methodArgs) -> {
            // Cross-cutting: log, then provide behavior
            System.out.println("[proxy] calling " + method.getName());
            if (method.getName().equals("greet"))
                return "Hello, " + methodArgs[0];
            return null;
        };

        Greeter g = (Greeter) Proxy.newProxyInstance(
            Greeter.class.getClassLoader(),
            new Class<?>[]{ Greeter.class },     // interfaces to implement
            handler);                            // all calls funnel here

        System.out.println(g.greet("Ada"));
        // [proxy] calling greet
        // Hello, Ada
    }
}
```

- Works only for **interfaces**. For concrete classes, libraries use bytecode generation (CGLIB, ByteBuddy).
- The `InvocationHandler` receives the `Method` and arguments — pure reflection underneath.
- This is how Spring AOP, JDK RMI stubs, and mocking frameworks (Mockito) operate.

---

## 17.9 `MethodHandles` and `VarHandle`

`java.lang.invoke` provides a **faster, more type-safe** alternative to reflection. A `MethodHandle` is a directly-invokable, typed reference to a method/field/constructor; the JIT optimizes it far better than `Method.invoke`. `VarHandle` (Java 9) does the same for fields/array elements and exposes explicit **memory-ordering** access modes (see Chapter 18) — the closest Java gets to C++ `std::atomic` memory orders.

```java
import java.lang.invoke.*;

public class Main {
    int counter = 0;

    public static void main(String[] args) throws Throwable {
        // --- MethodHandle: typed, fast invocation ---
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType mt = MethodType.methodType(String.class, char.class, char.class);
        MethodHandle replace = lookup.findVirtual(String.class, "replace", mt);
        String out = (String) replace.invoke("data", 'a', 'o');   // "doto"
        System.out.println(out);

        // --- VarHandle: field access with memory-ordering semantics ---
        VarHandle COUNTER = MethodHandles
            .lookup()
            .findVarHandle(Main.class, "counter", int.class);

        Main m = new Main();
        COUNTER.setVolatile(m, 1);                 // volatile store (Chapter 18)
        int v = (int) COUNTER.getAcquire(m);       // acquire load
        COUNTER.compareAndSet(m, 1, 2);            // atomic CAS, no Atomic* wrapper
        COUNTER.getAndAdd(m, 10);                  // atomic fetch-add
        System.out.println(COUNTER.get(m));        // 12
    }
}
```

| Tool | Best for | Speed |
|---|---|---|
| Reflection (`Method.invoke`) | Generic frameworks, max flexibility | Slowest |
| `MethodHandle` | Repeated/typed invocation, JIT-friendly | Fast |
| `VarHandle` | Atomic/ordered field access without `Atomic*` | Fast |

`VarHandle` access modes (`getPlain`, `getOpaque`, `getAcquire`, `getVolatile`, `compareAndSet`, ...) map almost one-to-one onto C++'s `memory_order_relaxed`/`acquire`/`seq_cst` — Chapter 18 covers the semantics.

---

## 17.10 Best Practices

```java
// ✅ Prefer compile-time safety: use generics + interfaces before reaching for reflection.
//    Reflection trades compile-time checks for runtime flexibility — use it deliberately.

// ✅ Cache reflective objects (Method, Field, MethodHandle). Lookups are expensive;
//    invocation on a cached handle is cheap.

// ✅ Use MethodHandle/VarHandle instead of Method/Field on hot paths.

// ✅ Use annotations + APT to GENERATE code (zero runtime cost) rather than
//    reflecting at runtime, when the inputs are known at build time.

// ✅ Mark runtime-read annotations with @Retention(RUNTIME) and a precise @Target.

// ⚠️ Respect the module system: setAccessible(true) can be blocked by strong
//    encapsulation (--add-opens may be required). Don't break encapsulation casually.

// ⚠️ Reflection defeats the optimizer and obscures call graphs — avoid in tight loops.

// ⚠️ Dynamic proxies only proxy interfaces; design to interfaces if you need them.
```

---

## Summary

| Java technique | Purpose | When it runs |
|---|---|---|
| **Reflection API** | Inspect/invoke types, methods, fields by name | Runtime |
| **Dynamic invocation** | Call methods / set fields chosen at runtime | Runtime |
| **Generics + bounds** | Compile-time type constraints (then erased) | Compile time → erased |
| **Annotations** | Attach metadata to declarations | Source/class/runtime |
| **Annotation processing (APT)** | Generate/validate source code | Build time (in `javac`) |
| **Dynamic proxies** | Synthesize interface implementations, intercept calls | Runtime |
| **MethodHandles/VarHandle** | Fast typed invocation; ordered atomic field access | Runtime (JIT-optimized) |

**The big picture:** C++ metaprograms the *type system at compile time* (templates, `constexpr`, SFINAE). Java metaprograms *running programs at runtime* (reflection, proxies, handles) and *source at build time* (annotation processors). Different mechanisms, overlapping goals: extensibility, code generation, and removing boilerplate.

---

## Next Steps

- Study how JUnit 5, Spring, Jackson, and JPA use runtime annotations + reflection.
- Explore Lombok / AutoValue / Dagger to see annotation processing generating real code.
- Benchmark `Method.invoke` vs `MethodHandle.invokeExact` to feel the difference.
- Read about the module system's effect on `setAccessible` (strong encapsulation).
- Move to [Chapter 18: The Java Memory Model](../18_memory_model/README.md), where `VarHandle` ordering modes return in full detail.
