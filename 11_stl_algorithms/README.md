# Chapter 11: Streams and Collections Algorithms

Where C++ groups its generic algorithms — searching, sorting, copying, transforming, counting — into the `<algorithm>` and `<numeric>` headers and glues them to containers through *iterators*, Java splits the same functionality across two complementary tools. The **Streams API** (`java.util.stream`, Java 8+) is the modern, dominant one: a stream is a *pipeline* of operations over a source (a collection, array, or generator) that you build declaratively — `filter`, `map`, `sorted`, then a terminal `collect` or `reduce`. The older **`Collections`/`Arrays` utility methods** (`Collections.sort`, `Arrays.binarySearch`, `Collections.max`) cover the in-place, list-oriented operations.

The central design idea is the same as the STL's "algorithms + iterators + containers": *separate the operation from the data structure*. But the mechanics differ sharply. C++ algorithms take an explicit `[first, last)` iterator pair and usually mutate the range in place or write to an output iterator; Java streams are **non-mutating** — intermediate operations return a *new* stream and the source is left untouched, results being gathered by a terminal operation. Streams are also **lazy** (nothing runs until a terminal op) and can be made **parallel** trivially. This chapter walks the major categories — stream creation, intermediate ops, terminal ops, `Collectors`, primitive streams, parallel streams — then the `Collections` algorithms, mapping each C++ `<algorithm>`/`<numeric>` function to its Java equivalent throughout.

> **C++ `<algorithm>` ↔ Java Streams — at a glance**
> - `std::transform` ↔ `.map(...)`
> - `std::copy_if` / `remove_if` ↔ `.filter(...)`
> - `std::sort` ↔ `.sorted(...)` (stream) or `Collections.sort` (in place)
> - `std::find_if` ↔ `.filter(...).findFirst()`
> - `std::count_if` ↔ `.filter(...).count()`
> - `std::accumulate` ↔ `.reduce(...)` / `.sum()` / `Collectors`
> - `std::all_of`/`any_of`/`none_of` ↔ `.allMatch`/`.anyMatch`/`.noneMatch`
> - `std::unique` ↔ `.distinct()`
> - **Big difference:** C++ algorithms mutate ranges in place via iterators; Java streams produce *new* results and never modify the source.

## 11.1 Streams vs Iterators

C++ algorithms traverse with **iterators** — objects generalizing pointers, dereferenced with `*` and advanced with `++`, with a hierarchy of categories (input/forward/bidirectional/random-access) and the half-open `[begin, end)` convention. Java still *has* iterators (`Iterator`, `ListIterator` — Chapter 10), but for algorithm-style work you use a **`Stream<T>`**. A stream is not a data structure and stores no elements; it is a *one-shot pipeline* that pulls from a source, applies a chain of operations, and produces a result. Unlike an iterator you do not advance it manually, and unlike a C++ range it cannot be reused once a terminal operation has run.

```java
import java.util.*;
import java.util.stream.*;

List<Integer> v = List.of(1, 2, 3, 4, 5);

// A stream pipeline: source -> intermediate ops -> terminal op
List<Integer> doubled = v.stream()      // create stream from collection
    .map(x -> x * 2)                     // intermediate (lazy)
    .collect(Collectors.toList());       // terminal (triggers execution)
// doubled = [2, 4, 6, 8, 10]; v is UNCHANGED

// Streams are single-use:
Stream<Integer> s = v.stream();
s.count();
// s.count();   // IllegalStateException: stream already operated upon or closed
```

### Creating Streams

```java
import java.util.stream.*;
import java.util.*;

Stream<Integer> s1 = List.of(1, 2, 3).stream();      // from a collection
Stream<Integer> s2 = Stream.of(1, 2, 3);             // from explicit values
IntStream      s3 = Arrays.stream(new int[]{1,2,3}); // from a primitive array
IntStream      s4 = IntStream.range(0, 5);           // 0,1,2,3,4 (like iota)
IntStream      s5 = IntStream.rangeClosed(1, 5);     // 1,2,3,4,5
Stream<Integer> inf = Stream.iterate(1, x -> x * 2); // infinite: 1,2,4,8,... (use limit!)
Stream<Double>  rnd = Stream.generate(Math::random); // infinite generator
```

---

## 11.2 Non-Modifying Operations (search, count, match)

C++ non-modifying algorithms (`find`, `count`, `for_each`, `all_of`/`any_of`/`none_of`, `binary_search`) inspect a range without changing it. Java's stream terminal operations cover the same ground. Most are O(n) linear scans; the short-circuiting matchers (`anyMatch`, `findFirst`) stop as soon as the answer is known. For the O(log n) sorted-membership test (`binary_search`) you use `Collections.binarySearch`/`Arrays.binarySearch`, *not* a stream (streams have no log-time search).

### Search

```java
import java.util.*;
import java.util.stream.*;

List<Integer> v = List.of(1, 2, 3, 4, 5, 3, 2);

// find first occurrence (C++ std::find) -> Optional
Optional<Integer> first = v.stream().filter(x -> x == 3).findFirst();
first.ifPresent(x -> System.out.println("Found: " + x));

// find_if -> first matching a predicate (same shape: filter + findFirst)
Optional<Integer> gt3 = v.stream().filter(x -> x > 3).findFirst();

// count (C++ std::count)
long cnt = v.stream().filter(x -> x == 3).count();        // 2

// count_if (C++ std::count_if)
long cnt2 = v.stream().filter(x -> x > 2).count();        // 4

// binary_search (O(log n)) — NOT a stream op; use Collections (needs sorted list)
List<Integer> sorted = v.stream().sorted().toList();
int idx = Collections.binarySearch(sorted, 4);            // >= 0 if present

// positional index of a match: use IntStream over indices
int pos = IntStream.range(0, v.size())
    .filter(i -> v.get(i) == 3)
    .findFirst().orElse(-1);                              // distance(begin, it)
```

### Match / Process

`allMatch`, `anyMatch`, `noneMatch` correspond directly to C++'s `all_of`/`any_of`/`none_of`, including the empty-stream conventions (`allMatch`→`true`, `anyMatch`→`false`, `noneMatch`→`true`) and short-circuiting. `forEach` is the `for_each` analogue.

```java
List<Integer> v = List.of(1, 2, 3, 4, 5);

// for_each
v.forEach(x -> System.out.print(x + " "));     // or v.stream().forEach(...)

boolean allPositive = v.stream().allMatch(x -> x > 0);   // all_of   -> true
boolean hasNegative = v.stream().anyMatch(x -> x < 0);   // any_of   -> false
boolean noZeros     = v.stream().noneMatch(x -> x == 0); // none_of  -> true
```

---

## 11.3 Modifying / Transforming Operations

C++ "modifying" algorithms (`copy`, `copy_if`, `transform`, `replace`, `reverse`, `unique`) change a range in place and cannot resize a container (hence the erase-remove idiom). Java streams take the opposite philosophy: they never modify the source — each intermediate operation returns a *new* stream, and you materialize the result with a terminal op. There is therefore no erase-remove dance: `filter` already drops elements, `distinct` already removes duplicates, and the result is collected into a fresh collection. (When you *do* want true in-place mutation of a `List`, use `list.replaceAll(...)`, `list.removeIf(...)`, or `Collections.reverse(...)` — see 11.7.)

```java
import java.util.*;
import java.util.stream.*;

List<Integer> v = List.of(1, 2, 3, 4, 5);

// copy (materialize a stream) — C++ std::copy + back_inserter
List<Integer> v2 = v.stream().toList();

// copy_if -> filter (C++ std::copy_if)
List<Integer> evens = v.stream().filter(x -> x % 2 == 0).toList();   // [2, 4]

// transform -> map (C++ std::transform)
List<Integer> dbl = v.stream().map(x -> x * 2).toList();             // [2,4,6,8,10]

// replace -> map with a conditional (C++ std::replace)
List<Integer> rep = v.stream().map(x -> x == 2 ? 20 : x).toList();

// reverse (stream has no reverse; sort with reverseOrder, or reverse a List in place)
List<Integer> rev = v.stream().sorted(Comparator.reverseOrder()).toList();

// distinct -> remove duplicates (C++ std::unique on a SORTED range; distinct works on ANY order)
List<Integer> dup = List.of(1, 1, 2, 2, 3, 3);
List<Integer> uniq = dup.stream().distinct().toList();               // [1, 2, 3]

// limit / skip — no direct single C++ algorithm
List<Integer> firstThree = v.stream().limit(3).toList();             // [1,2,3]
List<Integer> afterTwo   = v.stream().skip(2).toList();              // [3,4,5]
```

> **Difference:** C++ `std::unique` only removes *consecutive* duplicates (so you sort first), and returns a new logical end you must `erase`. Java `distinct()` removes *all* duplicates regardless of order (using `equals`/`hashCode`) and yields a fresh stream — no erase step.

### flatMap

`flatMap` flattens a stream of streams into one stream — there is no single C++ `<algorithm>` for this; you would nest loops or use ranges. It is essential for working with nested collections.

```java
import java.util.*;
import java.util.stream.*;

List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4), List.of(5));

List<Integer> flat = nested.stream()
    .flatMap(List::stream)      // each inner list -> its element stream, then concatenated
    .toList();                  // [1, 2, 3, 4, 5]
```

---

## 11.4 Sorting

C++ `std::sort` is an in-place, O(n log n), non-stable sort over random-access iterators (with `stable_sort` when needed). Java offers two routes. The **stream** `sorted()` returns a *new* sorted stream and is always **stable** (it is implemented as a stable merge sort). The **in-place** `Collections.sort(list)` / `list.sort(cmp)` / `Arrays.sort(array)` mutate the target directly. For partial needs (`partial_sort`, `nth_element`) Java has no direct equivalent — use a stream `sorted().limit(k)`, or a bounded `PriorityQueue` for top-K.

```java
import java.util.*;
import java.util.stream.*;

List<Integer> v = new ArrayList<>(List.of(5, 2, 8, 1, 9));

// In-place sort (C++ std::sort) — mutates the list
v.sort(null);                                    // natural ascending order
v.sort(Comparator.reverseOrder());               // descending (greater<int>())
v.sort(Comparator.comparingInt(Math::abs));      // custom comparator (by |x|)
Collections.sort(v);                              // equivalent ascending

// Stream sort (returns a NEW sorted list; stable like std::stable_sort)
List<Integer> asc  = v.stream().sorted().toList();
List<Integer> desc = v.stream().sorted(Comparator.reverseOrder()).toList();

// is_sorted (C++ std::is_sorted): no built-in; check pairwise
boolean isSorted = IntStream.range(1, v.size()).allMatch(i -> v.get(i-1) <= v.get(i));

// partial_sort / top-K: sorted + limit (or a bounded PriorityQueue)
List<Integer> top3 = v.stream().sorted(Comparator.reverseOrder()).limit(3).toList();

// Sorting objects by multiple keys (C++ std::tie comparison idiom):
record Person(String name, int age) {}
List<Person> people = new ArrayList<>(/* ... */);
people.sort(Comparator.comparing(Person::name).thenComparingInt(Person::age));
```

> **Difference:** `std::sort` is *not* stable; Java's `Collections.sort`/`List.sort`/stream `sorted` *are* stable. C++'s `nth_element` (average O(n) selection) has no JDK equivalent.

---

## 11.5 Numeric Reductions (`reduce`, primitive-stream sums)

C++ `<numeric>` (`accumulate`, `inner_product`, `partial_sum`, `adjacent_difference`, `iota`) folds or scans a range. Java's general fold is the stream `reduce`; for common arithmetic, **primitive streams** (`IntStream`, `LongStream`, `DoubleStream`) provide `sum()`, `average()`, `max()`, `min()`, and `summaryStatistics()` directly, avoiding boxing. The C++ "watch your accumulator type" pitfall (`accumulate(..., 0)` truncating doubles) maps to Java's choice of `IntStream` vs `DoubleStream` and the *identity* value's type in `reduce`.

```java
import java.util.*;
import java.util.stream.*;

List<Integer> v = List.of(1, 2, 3, 4, 5);

// accumulate (sum) — primitive stream, no boxing (C++ std::accumulate)
int sum = v.stream().mapToInt(Integer::intValue).sum();             // 15

// accumulate with an operation (product) — reduce(identity, op)
int product = v.stream().reduce(1, (a, b) -> a * b);                // 120

// reduce with a custom fold building a String (C++ accumulate into std::string)
String s = v.stream().map(String::valueOf).reduce("", (acc, x) -> acc + x + " ");
// "1 2 3 4 5 "  (Collectors.joining is the idiomatic way — see 11.6)

// inner_product (dot product) — zip two lists by index (no built-in zip)
List<Integer> a = List.of(1, 2, 3), b = List.of(4, 5, 6);
int dot = IntStream.range(0, a.size())
    .map(i -> a.get(i) * b.get(i))
    .sum();                                                         // 32

// statistics in one pass (sum/avg/min/max/count) — beats multiple C++ passes
IntSummaryStatistics stats = v.stream().mapToInt(Integer::intValue).summaryStatistics();
System.out.println(stats.getSum() + " " + stats.getAverage() + " " + stats.getMax());

// iota -> IntStream.range / rangeClosed (C++ std::iota)
List<Integer> seq = IntStream.rangeClosed(1, 5).boxed().toList();   // [1,2,3,4,5]
```

> **Difference:** `partial_sum`/`adjacent_difference` (prefix scans) have **no direct stream operation** — streams are designed for stateless, order-independent fold, not running scans. Use an explicit loop or `Arrays.parallelPrefix(arr, op)` for prefix sums.

---

## 11.6 Collectors

`Collectors` (used with the terminal `collect`) gather a stream into a container or summary — there is no single C++ counterpart because the STL produces results via output iterators/inserters instead. This is one of the most powerful parts of the API: `groupingBy` and `partitioningBy` build maps in a single pass, replacing nested C++ loops over `map`/`unordered_map`.

```java
import java.util.*;
import java.util.stream.*;

List<String> words = List.of("apple", "banana", "avocado", "cherry", "blueberry");

// toList / toSet (materialize)
List<String> list = words.stream().filter(w -> w.length() > 5).collect(Collectors.toList());
Set<String>  set  = words.stream().collect(Collectors.toSet());

// joining (C++: accumulate into a string with separators)
String csv = words.stream().collect(Collectors.joining(", ", "[", "]"));
// "[apple, banana, avocado, cherry, blueberry]"

// groupingBy — partition into a Map by a classifier (no STL one-liner)
Map<Character, List<String>> byFirst =
    words.stream().collect(Collectors.groupingBy(w -> w.charAt(0)));
// {a=[apple, avocado], b=[banana, blueberry], c=[cherry]}

// groupingBy + downstream collector (count per group) — the "multiset" pattern
Map<Character, Long> countByFirst =
    words.stream().collect(Collectors.groupingBy(w -> w.charAt(0), Collectors.counting()));
// {a=2, b=2, c=1}

// partitioningBy — split into true/false buckets by a predicate
Map<Boolean, List<String>> longShort =
    words.stream().collect(Collectors.partitioningBy(w -> w.length() > 6));
// {false=[apple, banana, cherry], true=[avocado, blueberry]}

// toMap — build a Map from each element
Map<String, Integer> lengths =
    words.stream().collect(Collectors.toMap(w -> w, String::length));

// summing / averaging downstream
Map<Character, Integer> totalLenByFirst =
    words.stream().collect(Collectors.groupingBy(w -> w.charAt(0),
                                                 Collectors.summingInt(String::length)));
```

| Collector | Produces | C++ rough analogue |
|-----------|----------|--------------------|
| `toList()` / `toSet()` | List / Set | `copy` + `back_inserter` / insert into `set` |
| `joining(sep)` | String | `accumulate` into `std::string` |
| `groupingBy(fn)` | `Map<K, List<T>>` | manual loop into `multimap` |
| `groupingBy(fn, counting())` | `Map<K, Long>` | manual loop into `map<K,int>` |
| `partitioningBy(pred)` | `Map<Boolean, List<T>>` | `std::partition` (but copies, not in place) |
| `toMap(k, v)` | `Map<K, V>` | manual loop into `map` |
| `summingInt`/`averagingInt` | sum/avg | `accumulate` |

---

## 11.7 In-Place List Algorithms (`Collections` / `List`)

When you genuinely want to mutate a `List` in place — closer to C++'s in-place algorithms — use the `Collections` utility methods and the default `List` methods, not a stream. These are the direct counterparts to `std::sort`, `std::reverse`, `std::replace`, `std::remove_if`, etc.

```java
import java.util.*;

List<Integer> v = new ArrayList<>(List.of(5, 2, 8, 1, 9, 2));

Collections.sort(v);                       // in place ascending (std::sort)
Collections.reverse(v);                    // std::reverse
Collections.rotate(v, 2);                  // std::rotate
Collections.shuffle(v);                    // std::shuffle
Collections.swap(v, 0, 1);                 // std::iter_swap
Collections.replaceAll(v, 2, 20);          // std::replace (all 2 -> 20)

v.replaceAll(x -> x * 2);                  // std::transform in place
v.removeIf(x -> x > 10);                   // std::remove_if + erase  (the erase-remove idiom, in ONE call)

int max = Collections.max(v);              // std::max_element (returns value)
int min = Collections.min(v);              // std::min_element (returns value)
int freq = Collections.frequency(v, 20);   // std::count
```

> **Difference:** C++'s erase-remove idiom (`v.erase(remove_if(...), v.end())`) is two steps; Java folds it into the single `list.removeIf(predicate)`.

---

## Summary

| Category | C++ `<algorithm>`/`<numeric>` | Java equivalent |
|----------|-------------------------------|-----------------|
| **Search** | `find`, `find_if`, `binary_search`, `count` | `filter().findFirst()`, `Collections.binarySearch`, `filter().count()` |
| **Match** | `all_of`, `any_of`, `none_of` | `allMatch`, `anyMatch`, `noneMatch` |
| **Transform** | `transform`, `copy_if`, `replace` | `map`, `filter`, `map(cond ? .. : ..)` |
| **Sort** | `sort`, `stable_sort`, `partial_sort` | `sorted` / `Collections.sort` / `List.sort` |
| **Numeric** | `accumulate`, `inner_product`, `iota` | `reduce`, `sum`, `IntStream.range` |
| **Dedup** | `unique` (consecutive) | `distinct` (any order) |
| **Collect** | output iterators / inserters | `Collectors.toList/groupingBy/joining` |
| **In-place** | `reverse`, `rotate`, `remove_if` | `Collections.reverse/rotate`, `removeIf` |

---

## 11.8 Stream Sources and Output ("iterator adaptors")

C++ iterator adaptors (`back_inserter`, `front_inserter`, `inserter`, `ostream_iterator`, `istream_iterator`) let algorithms feed directly into containers or streams without explicit loops. Java's stream API folds these roles into stream *sources* (reading) and *collectors/terminals* (writing), so there is no separate adaptor object.

```java
import java.util.*;
import java.util.stream.*;
import java.io.*;

List<Integer> src = List.of(1, 2, 3, 4, 5);

// back_inserter equivalent: a collector grows the target for you
List<Integer> dst = src.stream().toList();                 // copy
List<Integer> evens = src.stream().filter(x -> x % 2 == 0).collect(Collectors.toList());

// inserter into a set (auto-dedup + sort) — like copy(..., inserter(set,...))
TreeSet<Integer> s = src.stream().collect(Collectors.toCollection(TreeSet::new));

// ostream_iterator equivalent: print with a separator
System.out.println(src.stream().map(String::valueOf).collect(Collectors.joining(" ")));
src.stream().map(x -> x * x).forEach(x -> System.out.print(x + " "));   // 1 4 9 16 25

// istream_iterator equivalent: read whitespace-separated tokens from input
String input = "10 20 30 40 50";
List<Integer> parsed = Arrays.stream(input.split("\\s+"))
    .map(Integer::parseInt)
    .toList();                                              // [10,20,30,40,50]

// Read tokens, sort, print — the full C++ istream/ostream-iterator example
String unsorted = "5 3 1 4 2";
String result = Arrays.stream(unsorted.split("\\s+"))
    .mapToInt(Integer::parseInt)
    .sorted()
    .mapToObj(String::valueOf)
    .collect(Collectors.joining(" "));                      // "1 2 3 4 5"

// Read all lines of a file as a stream (C++ would loop with istream_iterator<string>)
// try (Stream<String> lines = Files.lines(Path.of("data.txt"))) { ... }
```

| C++ adaptor | Java equivalent |
|-------------|-----------------|
| `back_inserter(c)` | `Collectors.toList()` / `toCollection(ArrayList::new)` |
| `inserter(set, ...)` | `Collectors.toCollection(TreeSet::new)` / `toSet()` |
| `ostream_iterator<T>(os, sep)` | `Collectors.joining(sep)` / `forEach(print)` |
| `istream_iterator<T>(is)` | `Arrays.stream(line.split(...))` / `Files.lines(path)` |

---

## 11.9 Primitive Streams (`IntStream`, `LongStream`, `DoubleStream`)

Because Java generics work over reference types only, a `Stream<Integer>` boxes every element. The **primitive streams** avoid that overhead and add numeric conveniences (`sum`, `average`, `range`, `summaryStatistics`). Converting between them is explicit: `mapToInt`/`mapToObj`/`boxed`.

```java
import java.util.*;
import java.util.stream.*;

// IntStream: numeric work without boxing
int sum   = IntStream.rangeClosed(1, 100).sum();                    // 5050
double avg = IntStream.of(1, 2, 3, 4, 5).average().orElse(0);       // 3.0
int max    = IntStream.of(4, 2, 9, 1, 7).max().orElseThrow();       // 9

// iota analogue
List<Integer> idx = IntStream.range(0, 5).boxed().toList();         // [0,1,2,3,4]

// Object stream -> primitive stream and back
List<String> words = List.of("a", "bb", "ccc");
int totalChars = words.stream().mapToInt(String::length).sum();      // 6
List<Integer> lengths = words.stream().mapToInt(String::length).boxed().toList();

// summaryStatistics: min/max/sum/avg/count in a single pass
IntSummaryStatistics st = IntStream.of(4, 2, 9, 1, 7).summaryStatistics();
System.out.println(st.getMin() + " " + st.getMax() + " " + st.getAverage());  // 1 9 4.6
```

| Method | Purpose | C++ analogue |
|--------|---------|--------------|
| `IntStream.range(a, b)` | a..b-1 | `iota` |
| `IntStream.rangeClosed(a, b)` | a..b | `iota` (inclusive) |
| `.sum()` / `.average()` | total / mean | `accumulate` |
| `.max()` / `.min()` | extremes (Optional) | `max_element` / `min_element` |
| `.summaryStatistics()` | all of the above, one pass | (no single STL call) |
| `.boxed()` | `IntStream` → `Stream<Integer>` | (n/a) |

---

## 11.10 Parallel Streams

C++ added *parallel algorithms* in C++17 via execution policies (`std::sort(std::execution::par, ...)`). Java's equivalent is even simpler: call `.parallelStream()` (or `.parallel()` on an existing stream) and the pipeline is split across the common `ForkJoinPool`. The operations must be **stateless, non-interfering, and associative** for the result to be correct — the same requirements C++ imposes on parallel execution policies.

```java
import java.util.*;
import java.util.stream.*;

List<Integer> big = IntStream.rangeClosed(1, 1_000_000).boxed().toList();

// Sequential
long sumSeq = big.stream().mapToLong(Integer::longValue).sum();

// Parallel — splits across ForkJoinPool worker threads (C++ std::execution::par)
long sumPar = big.parallelStream().mapToLong(Integer::longValue).sum();

// Reduction must be ASSOCIATIVE to parallelize correctly
int product = big.parallelStream().reduce(1, (a, b) -> a * b);   // associative -> OK

// ⚠️ Pitfalls (same spirit as C++ data races):
//  - forEach on a parallel stream does NOT preserve order; use forEachOrdered if order matters
//  - do NOT mutate shared state from a lambda (use a thread-safe collector instead)
//  - parallelism only pays off for large data + CPU-bound work; small streams are slower parallel
```

| Concern | C++ (`std::execution::par`) | Java parallel stream |
|---------|-----------------------------|----------------------|
| Opt-in | execution policy argument | `.parallelStream()` / `.parallel()` |
| Thread pool | implementation-defined | common `ForkJoinPool` |
| Correctness rule | no data races, associative | stateless, non-interfering, associative |
| Order | unspecified unless seq | use `forEachOrdered` to preserve |

---

## 11.11 Optional (avoiding the `end()` sentinel)

C++ search algorithms return an iterator equal to `end()` when nothing is found, and you must remember to check before dereferencing. Java stream terminals that "might not find anything" — `findFirst`, `findAny`, `max`, `min`, `reduce` (no identity) — return an **`Optional<T>`** instead, making the absent case explicit in the type and impossible to ignore.

```java
import java.util.*;

List<Integer> v = List.of(1, 2, 3, 4, 5);

Optional<Integer> found = v.stream().filter(x -> x > 3).findFirst();

// Safe consumption — no risk of dereferencing an "end()" iterator
found.ifPresent(x -> System.out.println("Found " + x));   // prints 4
int value     = found.orElse(-1);                          // default if empty
int orThrow   = found.orElseThrow();                       // NoSuchElementException if empty
Optional<String> mapped = found.map(x -> "val=" + x);

// max/min return Optional (vs C++ max_element returning end() on empty range)
Optional<Integer> maxOpt = v.stream().max(Comparator.naturalOrder());
```

> **Difference:** C++ signals "not found" with the sentinel `end()` iterator (dereferencing it is UB). Java signals it with an empty `Optional`, which the compiler-visible type forces you to handle — eliminating a whole class of "forgot to check `!= end()`" bugs.

---

## Next Steps
- Reach for streams (`filter`/`map`/`collect`) over hand-written loops, as you would reach for `<algorithm>` over raw loops in C++.
- Use primitive streams for numeric work; use `Collectors.groupingBy` to replace nested loops.
- Remember streams are non-mutating and single-use; use `Collections`/`List` methods for in-place changes.
- Move to [Chapter 12: Memory Management and Garbage Collection](../12_memory_management/README.md)
```