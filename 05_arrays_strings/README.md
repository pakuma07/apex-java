# Chapter 5: Arrays & Strings

This chapter covers how Java stores collections of values and sequences of characters. It begins with **arrays** — Java's fixed-length, bounds-checked container — including multidimensional and jagged arrays and the `Arrays` utility class. It then moves to text handling with the immutable `String` class, the string pool, and the rich `String` API, followed by the mutable builders `StringBuilder` and `StringBuffer`. Finally it covers `char` and Unicode, **text blocks**, `String.format`, and the right way to compare strings.

The overarching lesson differs from C++: Java has no raw C-style arrays that decay to pointers and no null-terminated `char*` strings. Every Java array is a real object that **knows its own length** and **bounds-checks every access**, and every `String` manages its own storage and is **immutable**. Whole categories of C++ buffer-overflow and out-of-bounds bugs simply cannot occur — at the cost of the raw pointer control C++ offers.

## 5.1 Arrays

A Java array is a **fixed-length** object holding elements of a single type, allocated on the heap with `new` (or an array literal) and indexed from `0` to `length - 1`. Unlike a C++ raw array, the length is stored on the object as the `final` field `.length`, and the JVM throws `ArrayIndexOutOfBoundsException` on any out-of-range access rather than letting it be undefined behavior. The length is fixed once created; for a resizable sequence use `ArrayList` (Chapter 10).

### 1D Arrays

```java
// Fixed-length array
int[] arr = new int[5];            // 5 elements, all default 0 (NOT uninitialized)
int[] arr2 = {1, 2, 3, 4, 5};      // array literal
int[] arr3 = new int[]{10, 20, 30}; // explicit form (needed e.g. when not at declaration)

// Accessing elements
System.out.println(arr2[0]);       // 1
System.out.println(arr2[4]);       // 5
// System.out.println(arr2[5]);    // throws ArrayIndexOutOfBoundsException (NOT undefined!)

// Array length is a field, not sizeof arithmetic
int size = arr2.length;            // 5  (note: .length, no parentheses)

// Iterate by index
for (int i = 0; i < arr2.length; i++) {
    System.out.print(arr2[i] + " ");
}

// Enhanced for-loop (range-based equivalent)
for (int value : arr2) {
    System.out.print(value + " ");
}

// Modify elements
arr2[0] = 100;
arr2[3] += 10;
```

> **Note the syntax:** Java writes the brackets on the *type* (`int[] arr`), not the name. `int arr[]` is also legal (C-style) but discouraged. Unlike C++, a `new int[5]` array is **zero-initialized** (`0`, `0.0`, `false`, or `null`), never garbage.

### 2D Arrays

A Java 2D array is an **array of arrays** — `int[][]` is a reference to an array whose elements are themselves `int[]` references. This is *not* one contiguous block like a C++ `int[3][3]`; each row is a separate heap object. Because of that, rows can have different lengths (jagged arrays, below), and you index with `matrix[row][col]`.

```java
// 2D array (rectangular)
int[][] matrix = {
    {1, 2, 3},
    {4, 5, 6},
    {7, 8, 9}
};

// Access elements
System.out.println(matrix[0][0]);  // 1
System.out.println(matrix[2][1]);  // 8

// Modify
matrix[1][1] = 0;

// Nested loops — note each dimension has its own .length
for (int i = 0; i < matrix.length; i++) {
    for (int j = 0; j < matrix[i].length; j++) {
        System.out.print(matrix[i][j] + " ");
    }
    System.out.println();
}

// Allocate then fill (all elements default to 0)
int[][] grid = new int[3][3];      // 3x3, all zeros
grid[1][1] = 5;
```

### Jagged and Multidimensional Arrays

Because each row is an independent array, Java natively supports **jagged arrays** — rows of differing lengths — which is the direct, dynamic counterpart of C++'s fixed `vector<vector<int>>` style. You can allocate the outer array first and give each row its own size. The same nesting extends to three or more dimensions.

```java
// Jagged array: rows of different lengths
int[][] jagged = new int[3][];     // 3 rows, each currently null
jagged[0] = new int[]{1};          // length 1
jagged[1] = new int[]{2, 3};       // length 2
jagged[2] = new int[]{4, 5, 6};    // length 3

for (int[] row : jagged) {
    for (int v : row) System.out.print(v + " ");
    System.out.println();
}

// 3D array
int[][][] cube = new int[2][3][4];

// Initialized 3D (tensor)
int[][][] tensor = {
    {{1, 2}, {3, 4}},
    {{5, 6}, {7, 8}}
};
System.out.println(tensor[1][1][0]);  // 7
```

```
   C++ int[3][3] (one contiguous block)   Java int[][] (array of array references)

   [ 1 2 3 4 5 6 7 8 9 ]                   matrix --> [ r0 | r1 | r2 ]
                                                         |    |    |
                                                         v    v    v
                                                       [1,2,3][4,5,6][7,8,9]  (separate objects)
```

### Arrays Are References (No Decay)

In C++ an array name decays to a pointer to its first element, losing the size. A Java array variable is a **reference to an array object that retains its `.length`** and never decays. Assigning one array variable to another copies the *reference*, not the elements.

```java
int[] arr = {10, 20, 30};
int[] alias = arr;             // copies the REFERENCE — both names see one array
alias[0] = 999;
System.out.println(arr[0]);    // 999

// To actually copy elements, use Arrays.copyOf or clone():
int[] copy = java.util.Arrays.copyOf(arr, arr.length);  // independent copy
int[] copy2 = arr.clone();                               // shallow copy
copy[0] = 0;
System.out.println(arr[0]);    // 999 (the copy is independent)
```

## 5.2 The `Arrays` Utility Class

C++ relies on raw array functions and `<algorithm>` over iterators. Java packages the common array operations into the static helper class **`java.util.Arrays`**, which provides sorting, searching, filling, copying, comparison, and printing. This is the idiomatic replacement for hand-written loops and for C's `memcpy`/`memset`.

```java
import java.util.Arrays;

int[] a = {5, 3, 1, 4, 2};

// Sort (in place, ascending)
Arrays.sort(a);                       // {1, 2, 3, 4, 5}

// Binary search (array must be sorted)
int idx = Arrays.binarySearch(a, 3);  // 2

// Fill
int[] filled = new int[5];
Arrays.fill(filled, 7);               // {7, 7, 7, 7, 7}

// Copy / copy range
int[] copy  = Arrays.copyOf(a, a.length);     // full copy
int[] slice = Arrays.copyOfRange(a, 1, 4);    // {2, 3, 4} (indices 1..3)

// Compare and print
System.out.println(Arrays.equals(a, copy));   // true (element-wise)
System.out.println(Arrays.toString(a));       // [1, 2, 3, 4, 5]

// Multidimensional printing
int[][] grid = {{1, 2}, {3, 4}};
System.out.println(Arrays.deepToString(grid)); // [[1, 2], [3, 4]]

// Sort with a custom order (Integer[] needed for a Comparator)
Integer[] boxed = {3, 1, 4, 1, 5};
Arrays.sort(boxed, java.util.Comparator.reverseOrder());  // descending

// Bridge to a List or a Stream
java.util.List<Integer> list = Arrays.asList(boxed);
int sum = Arrays.stream(a).sum();
```

**C++ → Java mapping**

| C++ | Java |
|---|---|
| `std::sort(arr, arr + n)` | `Arrays.sort(arr)` |
| `std::binary_search` / `lower_bound` | `Arrays.binarySearch(arr, key)` |
| `memset` | `Arrays.fill(arr, value)` |
| `memcpy` / copy | `Arrays.copyOf`, `Arrays.copyOfRange`, `arr.clone()` |
| Manual print loop | `Arrays.toString` / `Arrays.deepToString` |

## 5.3 Strings

Java's `String` is the standard, safe text type and the rough analogue of C++'s `std::string` — but with two defining differences: a `String` is **immutable** (its characters never change after construction), and identical literals are shared in the **string pool**. There is no Java equivalent of a C-style `char*` with a null terminator; the closest low-level form is a `char[]`, which you rarely need.

### String Immutability and the Pool

Every method that "modifies" a `String` actually returns a **new** `String`; the original is untouched. This makes strings safe to share freely (including across threads). String *literals* are **interned**: the compiler stores one shared copy in the pool, so two equal literals are the same object — which is exactly why you must compare with `equals`, not `==` (see 5.7).

```java
String s = "Hello";
String upper = s.toUpperCase();   // returns a NEW string
System.out.println(s);            // Hello   -- original unchanged
System.out.println(upper);        // HELLO

// The pool: equal literals share one object
String a = "hi";
String b = "hi";
System.out.println(a == b);       // true  -- same pooled object (do NOT rely on this)

// `new String` forces a distinct object (off the pool)
String c = new String("hi");
System.out.println(a == c);       // false -- different object
System.out.println(a.equals(c));  // true  -- same content (the correct test)
```

```
   String pool                       heap
  +-----------+                     +----------------+
  | "hi"  <---+----- a, b           | new String("hi")| <--- c
  +-----------+                     +----------------+
   (literals interned, shared)       (new String -> separate object)
```

### String Basics

```java
// Creation
String s1 = "";                    // empty
String s2 = "Hello";               // literal
String s3 = "A".repeat(5);         // "AAAAA"  (Java 11+; like C++ string(5,'A'))
String s4 = new String(s2);        // explicit copy (rarely needed)
String s5 = String.valueOf(42);    // "42" from any value

// Length
System.out.println(s2.length());   // 5   -- a method, not a .length field

// Access (no operator[]; charAt returns a primitive char)
System.out.println(s2.charAt(0));  // 'H'
// s2.charAt(99)                    // throws StringIndexOutOfBoundsException

// There is NO s2[0] = 'h' — strings are immutable. Build a new one instead:
String changed = "h" + s2.substring(1);   // "hello"
```

### String Operations

`String` offers a full toolkit, all of which **return new strings**. Concatenation uses `+`/`concat`, `substring(begin, end)` slices (the second index is *exclusive*, unlike C++'s position+length), and `indexOf` searches — returning **`-1`** when nothing is found (not a special `npos`).

```java
String s = "Hello";

// Concatenation (each produces a new String)
s = s + " World";                  // "Hello World"
s = s.concat(" Java");             // "Hello World Java"

// Substring — note: substring(begin, endExclusive)
String sub  = s.substring(0, 5);   // "Hello"
String sub2 = s.substring(6);      // "World Java"

// Find — returns -1 if absent (NOT string::npos)
int pos = s.indexOf("World");      // 6
if (pos != -1) {
    System.out.println("Found at " + pos);
}
int last = s.lastIndexOf("o");     // rightmost 'o'

// "Replace" / "delete" / "insert" all return NEW strings:
String r = s.replace("World", "Java");   // replaces all occurrences
String stripped = s.strip();              // trim whitespace (Java 11+, Unicode-aware)

// Compare ordering
System.out.println("apple".compareTo("banana") < 0);  // true (apple comes first)

// Case conversion (whole-string, returns new strings)
System.out.println("hello".toUpperCase());  // "HELLO"
System.out.println("HELLO".toLowerCase());  // "hello"

// Split / join (no clean C++ equivalent without streams)
String[] parts = "a,b,c".split(",");        // ["a", "b", "c"]
String joined  = String.join("-", parts);   // "a-b-c"

// Query helpers
System.out.println("Hello World".contains("World"));   // true
System.out.println("Hello".startsWith("He"));          // true
System.out.println("Hello".isBlank());                 // false (Java 11+)
```

### String ↔ Number Conversions

C++ uses `to_string` and `stoi`/`stod`. Java uses the boxed wrapper types' static parse methods and `String.valueOf` / concatenation. Invalid input throws `NumberFormatException`.

```java
// Number -> String
String str = String.valueOf(42);       // "42"
String str2 = Integer.toString(42);    // "42"
String str3 = "answer: " + 42;         // "answer: 42" (concatenation)

// String -> number (throws NumberFormatException on bad input)
int num    = Integer.parseInt("123");  // 123
double d   = Double.parseDouble("3.14"); // 3.14
long lng   = Long.parseLong("9999999"); // 9999999
```

## 5.4 `StringBuilder` and `StringBuffer`

Because `String` is immutable, building a string by repeated `+` in a loop creates many throwaway objects (quadratic work). The mutable builder **`StringBuilder`** is the right tool — it is Java's analogue of using a growable buffer or `std::ostringstream` for assembly. **`StringBuffer`** is the older, **thread-safe (synchronized)** variant; prefer `StringBuilder` unless multiple threads share one builder.

```java
StringBuilder sb = new StringBuilder();   // mutable, grows automatically
sb.append("Hello");
sb.append(' ').append("World").append(' ').append(42);  // chaining
sb.insert(0, "[");                 // "[Hello World 42"
sb.append("]");                    // "[Hello World 42]"
sb.reverse();                      // in-place reverse
sb.reverse();                      // back again
sb.setCharAt(1, 'h');              // mutate in place: "[hello World 42]"

String result = sb.toString();     // materialize the final String
System.out.println(result);
System.out.println(sb.length());   // current length

// ❌ Avoid: O(n^2) garbage from repeated immutable concatenation in a loop
String bad = "";
for (int i = 0; i < 1000; i++) bad += i;     // creates ~1000 temporary Strings

// ✅ Use a StringBuilder
StringBuilder good = new StringBuilder();
for (int i = 0; i < 1000; i++) good.append(i);
String built = good.toString();
```

| Type | Mutable | Thread-safe | Use when |
|---|---|---|---|
| `String` | No (immutable) | Yes (because immutable) | Default for fixed text |
| `StringBuilder` | Yes | No | Building/editing text in one thread |
| `StringBuffer` | Yes | Yes (synchronized) | A builder genuinely shared across threads (rare) |

## 5.5 `char` and Unicode

Java's `char` is a **16-bit unsigned UTF-16 code unit** (`' '`..`'￿'`) — not C++'s 8-bit `char`. A `String` is internally a sequence of UTF-16 code units. Characters outside the Basic Multilingual Plane (e.g. many emoji) need **two** code units (a surrogate pair), so the code-unit `length()` is not always the number of visible characters. Java exposes full Unicode code points via `int`-based code point methods.

```java
char c = 'A';
System.out.println((int) c);            // 65  -- char promotes to its code unit value
char unicode = '世';                // a CJK character

// A code point may need TWO chars (a surrogate pair):
String emoji = "😀";          // 😀 — one code point, two UTF-16 code units
System.out.println(emoji.length());     // 2  -- code UNITS, not visible characters
System.out.println(emoji.codePointCount(0, emoji.length())); // 1 -- actual code points

// Iterate by Unicode code point (the correct way for full Unicode):
"a😀b".codePoints().forEach(cp ->
    System.out.println(Character.toChars(cp)));

// Character utility methods (analogue of <cctype>)
System.out.println(Character.isDigit('7'));    // true
System.out.println(Character.isLetter('x'));   // true
System.out.println(Character.toUpperCase('a')); // 'A'
```

**C++ → Java mapping**

| C++ | Java |
|---|---|
| `char` (8-bit) | `byte` is the 8-bit type; `char` is a 16-bit UTF-16 code unit |
| `wchar_t`, `char16_t`, `char32_t` | `char` ≈ `char16_t`; full code points handled as `int` |
| `<cctype>` (`isdigit`, `toupper`) | `Character.isDigit`, `Character.toUpperCase`, etc. |
| `u8"..."`, `u"..."`, `U"..."` literals | Java source is Unicode; strings are UTF-16 internally — no prefixes |

## 5.6 Text Blocks and `String.format`

C++11 added raw string literals `R"(...)"` to avoid escaping. Java's equivalent is the **text block** (Java 15+): a multi-line string delimited by `"""` that needs no escaping for quotes or newlines and auto-strips common indentation. For formatted output, Java uses `String.format` / `printf`, the analogue of C++ `std::stringstream` formatting or `printf`.

### Text Blocks (Java 15+)

```java
// Without a text block: escape-heavy, like a normal C++ string
String json1 = "{\n  \"key\": \"value\",\n  \"arr\": [1, 2, 3]\n}";

// Text block: no escaping of quotes or newlines (≈ C++ raw string R"(...)")
String json2 = """
    {
      "key": "value",
      "arr": [1, 2, 3]
    }
    """;     // common leading indentation is stripped automatically

String html = """
    <html>
      <body>Hello</body>
    </html>
    """;

// Windows paths still need escaping or just forward slashes —
// text blocks remove quote/newline escaping, but \ is still an escape char.
String path = "C:\\Users\\user\\file.txt";
```

> **Difference from C++ raw strings:** a text block still processes backslash escapes (so `\\` is needed for a literal backslash), whereas C++ `R"(...)"` disables *all* escape processing. The text block's main wins are multi-line literals and not escaping `"`.

### `String.format` and `printf`

```java
// Build a formatted string (analogue of stringstream / sprintf)
String formatted = String.format("Name: %s, Age: %d, GPA: %.2f", "Alice", 25, 3.789);
// "Name: Alice, Age: 25, GPA: 3.79"

// Print directly
System.out.printf("Hex of 255 = %x%n", 255);   // Hex of 255 = ff
System.out.printf("%-10s|%n", "left");          // left-justified in width 10

// Instance method form (Java 15+)
String s = "Total: %d".formatted(42);
```

Common conversions: `%s` (any object via `toString`), `%d` (integer), `%f`/`.2f` (float), `%x` (hex), `%b` (boolean), `%n` (platform newline), `%%` (literal percent).

## 5.7 String Comparison

This is the most important string gotcha for newcomers from C++. In C++ `std::string`'s `==` compares **contents**. In Java, `==` on `String` compares **object identity** (the same reference), so it only *accidentally* works for pooled literals. **Always use `equals` for content** and `compareTo` for ordering.

```java
String a = new String("hello");
String b = new String("hello");

System.out.println(a == b);                 // false -- different objects (identity)
System.out.println(a.equals(b));            // true  -- same content (correct)
System.out.println(a.equalsIgnoreCase("HELLO")); // true

// Ordering (like C++ operator< / compare): negative, zero, or positive
System.out.println("apple".compareTo("banana")); // negative -> apple first

// Null-safe equality without risking an NPE on the left operand:
System.out.println(java.util.Objects.equals(a, b));  // true, and safe if either is null
```

**C++ → Java mapping**

| C++ `std::string` | Java `String` |
|---|---|
| `s1 == s2` (content) | `s1.equals(s2)` (`==` is identity!) |
| `s1 < s2` / `s1.compare(s2)` | `s1.compareTo(s2)` |
| `s.length()` / `s.size()` | `s.length()` |
| `s[i]` (mutable) | `s.charAt(i)` (read-only; String is immutable) |
| `s.substr(pos, len)` | `s.substring(begin, endExclusive)` |
| `s.find(x)` → `npos` if absent | `s.indexOf(x)` → `-1` if absent |
| mutable in place | use `StringBuilder` |

## 5.8 Best Practices

The recurring theme is the same as the chapter: lean on the safe, self-describing standard types, and respect immutability.

```java
import java.util.Arrays;
import java.util.List;

// ✅ Use equals (not ==) to compare String contents
if (a.equals(b)) { /* ... */ }

// ✅ Use StringBuilder for loops that assemble text
StringBuilder sb = new StringBuilder();
for (String part : parts) sb.append(part);

// ✅ Use the Arrays helpers instead of hand-rolled loops
int[] nums = {3, 1, 4, 1, 5};
Arrays.sort(nums);
System.out.println(Arrays.toString(nums));

// ✅ Indexing is always bounds-checked — but validate before use to give better errors
if (index >= 0 && index < nums.length) {
    int v = nums[index];
}

// ✅ Prefer ArrayList<>/List for sequences whose size changes (Chapter 10)
List<Integer> dynamic = new java.util.ArrayList<>(List.of(1, 2, 3));
dynamic.add(4);

// ✅ Use text blocks for multi-line literals; String.format for formatting
String report = String.format("processed %d of %d", done, total);
```

## Summary

| Concept | Key Points |
|---------|-----------|
| **Arrays** | Fixed length, heap objects, `.length` field, bounds-checked, zero-initialized |
| **Multidimensional** | Array of arrays; supports jagged rows; `Arrays.deepToString` to print |
| **`Arrays` class** | `sort`, `binarySearch`, `fill`, `copyOf`, `equals`, `toString` |
| **`String`** | Immutable; pooled literals; compare with `equals`/`compareTo`, never `==` |
| **`StringBuilder`/`Buffer`** | Mutable text; `StringBuilder` unsynchronized, `StringBuffer` synchronized |
| **`char`/Unicode** | 16-bit UTF-16 code unit; use code points for full Unicode |
| **Text blocks** | Multi-line literals (`"""`); analogue of C++ raw strings |
| **`String.format`** | `%s %d %f %x %n`; analogue of `printf`/`stringstream` formatting |

## Next Steps
- Work with arrays and the `Arrays` utility class
- Practice immutable `String` operations and `StringBuilder`
- Use text blocks and `String.format` for output
- Move to [Chapter 6: OOP Basics](../06_oop_basics/README.md)
