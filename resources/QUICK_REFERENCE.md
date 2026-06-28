# Java 21 Quick Reference Cheat Sheet

> Adapted from the C++11 Quick Reference. Reworked for modern Java (Java 21).

## Table of Contents
1. [Basic Syntax](#basic-syntax)
2. [Data Types](#data-types)
3. [Operators](#operators)
4. [Control Flow](#control-flow)
5. [Methods](#methods)
6. [Classes & Objects](#classes--objects)
7. [References & null](#references--null)
8. [Collections](#collections)
9. [Streams & Algorithms](#streams--algorithms)
10. [String Operations](#string-operations)

---

## Basic Syntax

### Hello World
```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

### Compilation & Run
```bash
javac --release 21 Main.java
java Main

# Or run a single file directly (Java 11+)
java Main.java
```

### Comments
```java
// Single line comment
/* Multi-line
   comment */
/** Javadoc comment */
```

---

## Data Types

### Primitive Types
```java
byte   b  = 100;                  // 1 byte
short  s  = 1000;                 // 2 bytes
int    i  = 100000;               // 4 bytes
long   l  = 10000000000L;         // 8 bytes
float  f  = 3.14f;                // 4 bytes
double d  = 3.14159;              // 8 bytes
char   c  = 'A';                  // 2 bytes (UTF-16)
boolean bool = true;              // true/false
```

### Type Inference (Java 10+)
```java
var x = 10;                       // int
var name = "hello";               // String
var list = new ArrayList<Integer>();
```

### Wrappers & Autoboxing
```java
Integer boxed = 42;               // autobox int -> Integer
int unboxed = boxed;              // auto-unbox
List<Integer> nums = List.of(1, 2, 3);   // generics need wrappers
```

### Constants
```java
final int MAX = 100;              // cannot reassign
static final double PI = 3.14159; // class-level constant
```

---

## Operators

### Arithmetic
```java
+  (addition)        a + b
-  (subtraction)     a - b
*  (multiplication)  a * b
/  (division)        a / b
%  (modulo)          a % b
```

### Comparison
```java
==  (equal)          a == b     // identity for objects, value for primitives
!=  (not equal)      a != b
<   (less than)      a < b
>   (greater than)   a > b
<=  (less/equal)     a <= b
>=  (greater/equal)  a >= b
```

> For object value equality use `a.equals(b)`, not `==`.

### Logical
```java
&&  (AND, short-circuit)   a && b
||  (OR, short-circuit)    a || b
!   (NOT)                  !a
```

### Assignment
```java
=   a = b
+=  a += b
-=  a -= b
*=  a *= b
/=  a /= b
%=  a %= b
```

### Increment / Decrement
```java
++a   // pre-increment
a++   // post-increment
--a   // pre-decrement
a--   // post-decrement
```

### Ternary
```java
condition ? valueIfTrue : valueIfFalse
```

---

## Control Flow

### If Statement
```java
if (condition) {
    // if true
} else if (other) {
    // else if
} else {
    // otherwise
}
```

### Switch Statement & Expression
```java
// Classic statement
switch (value) {
    case 1: doOne(); break;
    case 2: doTwo(); break;
    default: doDefault();
}

// Switch expression (Java 14+) - arrow form, no fall-through
String name = switch (day) {
    case MON, TUE, WED, THU, FRI -> "Weekday";
    case SAT, SUN -> "Weekend";
};

// With yield for blocks
int size = switch (item) {
    case "big" -> 3;
    default -> { yield 1; }
};
```

### While / Do-While
```java
while (condition) { }

do {
    // runs at least once
} while (condition);
```

### For Loops
```java
for (int i = 0; i < 10; i++) { }

for (int val : container) {        // enhanced for
    // each element
}
```

### Loop Control
```java
break;          // exit loop
continue;       // next iteration
outer: for (...) { for (...) { break outer; } }   // labeled break
return;         // exit method
```

---

## Methods

### Definition
```java
returnType methodName(Type p1, Type p2) {
    return value;
}
```

### Examples
```java
int add(int a, int b) { return a + b; }

void printMessage(String msg) { System.out.println(msg); }

double divide(double a, double b) {
    if (b == 0) return 0;
    return a / b;
}
```

### Overloading
```java
void display(int x) { }
void display(double x) { }
void display(String s) { }
```

### Varargs
```java
int sum(int... nums) {
    int total = 0;
    for (int n : nums) total += n;
    return total;
}
sum(1, 2, 3, 4);
```

### Lambdas & Method References
```java
Function<Integer, Integer> square = x -> x * x;
int r = square.apply(5);                 // 25

int factor = 2;
Function<Integer, Integer> mul = x -> x * factor;  // captures factor

list.forEach(System.out::println);       // method reference
```

---

## Classes & Objects

### Class Definition
```java
class Person {
    private int age;

    Person(int a) { this.age = a; }

    int getAge() { return age; }
    void setAge(int a) { age = a; }
}
```

### Creating Objects
```java
Person p = new Person(25);        // all objects on the heap
// No manual delete - the garbage collector reclaims unreachable objects
```

### Record (data class)
```java
record Point(int x, int y) { }    // auto equals/hashCode/toString/accessors
Point pt = new Point(3, 4);
pt.x();                           // 3
```

### Access Modifiers
```java
public      // accessible everywhere
protected   // subclasses + same package
            // (no keyword) package-private
private     // only within class
```

### Static Members
```java
class Counter {
    private static int count = 0;
    static int getCount() { return count; }
}
```

### The this Reference
```java
void modify() { this.value = 10; }
```

---

## References & null

### Reference Semantics
```java
int[] a = {1, 2, 3};
int[] b = a;            // b refers to the SAME array
b[0] = 99;              // a[0] is now 99 too

// Primitives are copied by value
int x = 10;
int y = x;              // independent copy
```

### null and Optional
```java
String s = null;                 // a reference to nothing
if (s != null) s.length();       // guard against NullPointerException

Optional<String> maybe = Optional.ofNullable(s);
String val = maybe.orElse("default");
maybe.ifPresent(System.out::println);
```

### Identity vs Equality
```java
String a = new String("hi");
String b = new String("hi");
a == b;          // false (different objects)
a.equals(b);     // true  (same value)
```

---

## Collections

### List (ArrayList)
```java
import java.util.*;

List<Integer> v = new ArrayList<>();
v.add(1);                 // add element
v.remove(v.size() - 1);   // remove last
v.set(0, 10);             // replace at index
v.get(0);                 // access
v.size();                 // count
v.isEmpty();              // empty?
v.clear();                // remove all

for (int x : v) { }       // enhanced for

List<Integer> imm = List.of(1, 2, 3);    // immutable
```

### Map (HashMap)
```java
Map<String, Integer> m = new HashMap<>();
m.put("key", 10);                  // insert/modify
m.get("key");                      // access (null if missing)
m.getOrDefault("key", 0);          // safe access
m.containsKey("key");
m.remove("key");
m.merge("key", 1, Integer::sum);   // increment

for (var e : m.entrySet()) {
    System.out.println(e.getKey() + ": " + e.getValue());
}
```

### Set (HashSet)
```java
Set<Integer> s = new HashSet<>();
s.add(10);                // add
s.remove(10);             // remove
s.contains(10);           // membership test
```

### Other Containers
```java
Deque<Integer> dq = new ArrayDeque<>();    // double-ended queue / stack / queue
Queue<Integer> q = new LinkedList<>();     // FIFO
Deque<Integer> stack = new ArrayDeque<>(); // use push/pop for LIFO
List<Integer> linked = new LinkedList<>(); // linked list
Map<K,V> tree = new TreeMap<>();           // sorted map
Set<Integer> tset = new TreeSet<>();       // sorted set
```

---

## Streams & Algorithms

### Searching / Matching
```java
import java.util.stream.*;

list.stream().filter(x -> x > 3).findFirst();       // first match
list.contains(value);                               // membership
list.stream().anyMatch(x -> x > 10);                // any
list.stream().allMatch(x -> x > 0);                 // all
list.stream().noneMatch(x -> x < 0);                // none
```

### Transforming
```java
List<Integer> doubled = list.stream()
    .map(x -> x * 2)
    .toList();

List<Integer> evens = list.stream()
    .filter(x -> x % 2 == 0)
    .collect(Collectors.toList());

Collections.sort(mutableList);                      // ascending in place
mutableList.sort(Comparator.reverseOrder());        // descending
Collections.reverse(mutableList);
```

### Counting / Aggregating
```java
long count = list.stream().filter(x -> x > 0).count();
int sum = list.stream().mapToInt(Integer::intValue).sum();
OptionalInt max = list.stream().mapToInt(Integer::intValue).max();
```

### Reducing
```java
int product = list.stream().reduce(1, (a, b) -> a * b);
String joined = list.stream().map(String::valueOf)
                    .collect(Collectors.joining(", "));
```

---

## String Operations

### Creation & Concatenation
```java
String s1;                          // null until assigned
String s2 = "Hello";
String s3 = new String(s2);         // explicit copy
String s4 = s2 + " World";          // concatenation
```

### Access & Modification (Strings are immutable)
```java
s.charAt(0);             // character at index
s.length();              // length
s.substring(0, 5);       // substring [0,5)
s.replace("a", "b");     // returns a new String
s.indexOf("World");      // position or -1
s.toUpperCase();         // new String
s.strip();               // trim whitespace (Java 11+)
s.isBlank();             // empty or whitespace
```

### Building Strings
```java
StringBuilder sb = new StringBuilder();
sb.append("Hello").append(" ").append("World");
String result = sb.toString();
```

### Comparison
```java
s1.equals(s2);           // value equality
s1.equalsIgnoreCase(s2);
s1.compareTo(s2);        // lexicographic (<0, 0, >0)
// s1 == s2;             // identity - avoid for content comparison
```

### Conversion
```java
Integer.parseInt("123");      // String -> int
Double.parseDouble("3.14");   // String -> double
String.valueOf(42);           // any -> String
Integer.toString(42);
```

### Text Blocks (Java 15+)
```java
String html = """
    <html>
      <body>Hello</body>
    </html>
    """;
```

---

## I/O Formatting

### Formatted Output
```java
System.out.printf("%.2f%n", 3.14159);         // 3.14
System.out.printf("%10d%n", 42);              // right-aligned width 10
System.out.printf("%-10s|%n", "left");        // left-aligned
System.out.printf("%x%n", 255);               // ff (hex)
String s = String.format("%05d", 42);         // "00042"
```

---

## File I/O (java.nio.file)

### Reading
```java
import java.nio.file.*;

List<String> lines = Files.readAllLines(Path.of("file.txt"));
String content = Files.readString(Path.of("file.txt"));

try (var reader = Files.newBufferedReader(Path.of("file.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
}
```

### Writing
```java
Files.writeString(Path.of("file.txt"), "Hello\n");

try (var writer = Files.newBufferedWriter(Path.of("file.txt"))) {
    writer.write("Hello");
    writer.newLine();
}
```

### Console Input
```java
import java.util.Scanner;

Scanner sc = new Scanner(System.in);
String word = sc.next();          // read token
int n = sc.nextInt();             // read int
String line = sc.nextLine();      // read whole line
```

---

## Useful Snippets

### Check if Container Has Element
```java
if (list.contains(element)) { }
if (set.contains(element)) { }
if (map.containsKey(key)) { }
```

### Common Loop Patterns
```java
// Iterate a list with index
for (int i = 0; i < list.size(); i++) { }

// Iterate a map
for (var e : map.entrySet()) { }

// Remove while iterating (use iterator or removeIf)
list.removeIf(x -> x % 2 == 0);
```

### Exception Handling
```java
try {
    // code that might throw
} catch (Exception e) {
    System.out.println(e.getMessage());
}
```

---

## Quick Decision Guide

| Task | Use This |
|------|----------|
| Store multiple values of same type | `ArrayList<T>` |
| Key-value pairs | `HashMap<K,V>` or `TreeMap` |
| Unique values, sorted | `TreeSet<T>` |
| Fast lookup (order doesn't matter) | `HashSet<T>` |
| Add/remove at both ends | `ArrayDeque<T>` |
| FIFO operations | `ArrayDeque<T>` (offer/poll) |
| LIFO operations | `ArrayDeque<T>` (push/pop) |
| Frequently insert/delete middle | `LinkedList<T>` |
| Immutable snapshot | `List.of(...)` / `List.copyOf(...)` |
| Possibly-absent value | `Optional<T>` |
| Text data | `String` (build with `StringBuilder`) |
| When in doubt | `ArrayList<T>` |

---

**Remember**: Import the right packages (`java.util.*`, `java.util.stream.*`, `java.nio.file.*`). Every program needs a class with a `public static void main(String[] args)` entry point.
