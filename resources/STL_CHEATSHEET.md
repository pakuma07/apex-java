# Java Collections & Streams Cheatsheet

> This is the Java counterpart to the C++ STL cheatsheet. The filename is kept as `STL_CHEATSHEET.md` for alignment with the C++ edition. It covers the **Java Collections Framework** (the analogue of STL containers) and the **Streams API** (the analogue of STL algorithms), plus iterators.

## Container Selection Guide
```
Need ordered, random access?      -> ArrayList
Need fast middle insert/remove?   -> LinkedList
Need fast front/back?             -> ArrayDeque
Need key-value pairs, sorted?     -> TreeMap
Need unique values, sorted?       -> TreeSet
Need fast key-value, unordered?   -> HashMap
Need fast unique, unordered?      -> HashSet
Need FIFO?                        -> ArrayDeque (offer/poll) or Queue
Need LIFO?                        -> ArrayDeque (push/pop)
Need insertion-order map/set?     -> LinkedHashMap / LinkedHashSet
Need a max/min heap?              -> PriorityQueue
```

> Mapping from C++ STL: `vector`→`ArrayList`, `list`→`LinkedList`, `deque`→`ArrayDeque`, `map`→`TreeMap`, `unordered_map`→`HashMap`, `set`→`TreeSet`, `unordered_set`→`HashSet`, `stack`/`queue`→`ArrayDeque`, `priority_queue`→`PriorityQueue`, `<algorithm>`→Streams API.

---

## ArrayList (analogue of std::vector)

```java
import java.util.*;

// Creation
List<Integer> v = new ArrayList<>();                  // empty
List<Integer> v2 = new ArrayList<>(Collections.nCopies(10, 5)); // ten 5s
List<Integer> v3 = new ArrayList<>(List.of(1, 2, 3)); // from elements
List<Integer> v4 = new ArrayList<>(other);            // copy of a collection
List<Integer> v5 = new ArrayList<>(100);              // initial capacity

// Access
v.get(0);                                             // by index (bounds-checked)
v.getFirst();                                         // first (Java 21)
v.getLast();                                          // last  (Java 21)

// Modification
v.add(10);                                            // append
v.removeLast();                                       // remove last (Java 21)
v.add(2, 99);                                         // insert at index
v.remove(2);                                          // remove at index
v.set(0, 42);                                         // replace
v.clear();                                            // remove all

// Properties
v.size();                                             // element count
v.isEmpty();

// Iteration
for (int x : v) { }
for (var it = v.iterator(); it.hasNext(); ) { int x = it.next(); }
for (int i = 0; i < v.size(); i++) { }
```

---

## LinkedList (analogue of std::list)

```java
import java.util.*;

LinkedList<Integer> l = new LinkedList<>(List.of(1, 2, 3));
l.getFirst();                       // first element
l.getLast();                        // last element

l.addFirst(0);                      // add to front
l.removeFirst();                    // remove from front
l.addLast(4);                       // add to back
l.removeLast();                     // remove from back
l.add(1, 99);                       // insert at index (O(n) traversal)

for (int x : l) { }
```

---

## HashMap & TreeMap (analogue of unordered_map & map)

```java
import java.util.*;

// Creation
Map<String, Integer> m  = new HashMap<>();            // unordered (hash)
Map<String, Integer> tm = new TreeMap<>();            // sorted by key
Map<String, Integer> lm = new LinkedHashMap<>();      // insertion order

// Insert & Access
m.put("key", 10);                                     // insert/modify
m.putIfAbsent("key", 5);                              // only if missing
m.get("key");                                         // value or null
m.getOrDefault("key", 0);                             // safe access

// Find / membership
m.containsKey("key");
m.containsValue(10);

// Properties
m.size();
m.isEmpty();

// Modification
m.remove("key");
m.clear();
m.merge("key", 1, Integer::sum);                      // increment a counter
m.computeIfAbsent("k", k -> new ArrayList<>()).add(1);// multimap idiom

// Iteration
for (var e : m.entrySet()) {
    System.out.println(e.getKey() + ": " + e.getValue());
}
m.forEach((k, val) -> System.out.println(k + "=" + val));

// Immutable map
Map<String, Integer> imm = Map.of("a", 1, "b", 2);
```

> There is no separate "multimap"; model it as `Map<K, List<V>>` and use `computeIfAbsent`.

---

## HashSet & TreeSet (analogue of unordered_set & set)

```java
import java.util.*;

Set<Integer> s  = new HashSet<>();                    // unordered, unique
Set<Integer> ts = new TreeSet<>();                    // sorted, unique
Set<Integer> ls = new LinkedHashSet<>();              // insertion order

s.add(10);                                            // returns false if present
s.addAll(List.of(1, 2, 3));
s.contains(10);                                       // membership
s.remove(10);
s.clear();

for (int x : s) { }

// TreeSet navigation
TreeSet<Integer> nav = new TreeSet<>(List.of(1, 3, 5, 7));
nav.first();                                          // smallest
nav.last();                                           // largest
nav.floor(4);                                         // largest <= 4  -> 3
nav.ceiling(4);                                       // smallest >= 4 -> 5

// Immutable set
Set<Integer> imm = Set.of(1, 2, 3);
```

> A "multiset" (counting duplicates) is typically modeled as `Map<T, Integer>` with `merge(x, 1, Integer::sum)`.

---

## ArrayDeque (analogue of std::deque, std::stack, std::queue)

```java
import java.util.*;

Deque<Integer> dq = new ArrayDeque<>();

// As a double-ended queue
dq.addFirst(0);     dq.addLast(10);
dq.pollFirst();     dq.pollLast();
dq.peekFirst();     dq.peekLast();

// As a stack (LIFO)
dq.push(1);  dq.push(2);  dq.push(3);
dq.peek();          // 3
dq.pop();           // removes 3

// As a queue (FIFO)
dq.offer(1);  dq.offer(2);
dq.peek();          // 1
dq.poll();          // removes 1

dq.size();
dq.isEmpty();
for (int x : dq) { }
```

---

## PriorityQueue (analogue of std::priority_queue)

```java
import java.util.*;

PriorityQueue<Integer> minHeap = new PriorityQueue<>();                 // min-heap
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());

minHeap.offer(5); minHeap.offer(1); minHeap.offer(3);
minHeap.peek();     // 1 (smallest)
minHeap.poll();     // removes 1

// Custom comparator
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
```

---

## Streams API (analogue of <algorithm>)

> In C++ you call free algorithms on iterator ranges. In Java you build a **stream pipeline**: a source, zero or more intermediate operations, and one terminal operation.

### Non-Modifying / Querying

```java
import java.util.*;
import java.util.stream.*;

List<Integer> v = List.of(1, 2, 3, 4, 5);

// find  (std::find / std::find_if)
Optional<Integer> first = v.stream().filter(x -> x > 3).findFirst();
boolean has = v.contains(3);

// count  (std::count / std::count_if)
long c1 = v.stream().filter(x -> x == 3).count();
long c2 = v.stream().filter(x -> x % 2 == 0).count();

// all/any/none  (std::all_of / any_of / none_of)
boolean allPos  = v.stream().allMatch(x -> x > 0);
boolean hasEven = v.stream().anyMatch(x -> x % 2 == 0);
boolean noNeg   = v.stream().noneMatch(x -> x < 0);

// min/max  (std::min_element / max_element)
Optional<Integer> max = v.stream().max(Comparator.naturalOrder());
int maxVal = v.stream().mapToInt(Integer::intValue).max().orElseThrow();

// forEach  (std::for_each)
v.forEach(x -> System.out.print(x + " "));

// binary search (on a sorted List/array)
int idx = Collections.binarySearch(new ArrayList<>(v), 3);
```

### Modifying / Producing New Data

```java
// map  (std::transform)
List<Integer> doubled = v.stream().map(x -> x * 2).toList();

// filter into a new list  (std::copy_if)
List<Integer> evens = v.stream().filter(x -> x % 2 == 0).toList();

// flatMap (flatten nested)
List<Integer> flat = Stream.of(List.of(1,2), List.of(3,4))
    .flatMap(List::stream).toList();

// distinct  (std::unique on sorted, but works unsorted here)
List<Integer> uniq = Stream.of(1,1,2,3,3).distinct().toList();

// limit / skip
List<Integer> firstThree = v.stream().limit(3).toList();
List<Integer> afterTwo   = v.stream().skip(2).toList();

// in-place transform of a mutable list
List<Integer> m = new ArrayList<>(v);
m.replaceAll(x -> x * 2);                      // std::transform in place
m.removeIf(x -> x % 2 == 0);                   // std::remove + erase
Collections.reverse(m);                        // std::reverse
Collections.fill(m, 0);                        // std::fill
```

### Sorting (analogue of std::sort)

```java
List<Integer> m = new ArrayList<>(List.of(3, 1, 2));

m.sort(Comparator.naturalOrder());             // ascending
m.sort(Comparator.reverseOrder());             // descending
m.sort(Comparator.comparingInt(Math::abs));    // custom key

// Stream (produces a new sorted list)
List<Integer> sorted = m.stream().sorted().toList();

// Multi-key sort
people.sort(Comparator.comparing(Person::lastName)
                      .thenComparing(Person::firstName));

boolean isSorted = m.equals(m.stream().sorted().toList());
```

### Numeric / Reduction (analogue of <numeric>)

```java
// accumulate / sum  (std::accumulate)
int sum = v.stream().mapToInt(Integer::intValue).sum();

// reduce with an operation
int product = v.stream().reduce(1, (a, b) -> a * b);

// statistics
IntSummaryStatistics stats = v.stream().mapToInt(Integer::intValue).summaryStatistics();
stats.getMax(); stats.getMin(); stats.getAverage(); stats.getSum();

// iota  (std::iota)
List<Integer> seq = IntStream.rangeClosed(1, 5).boxed().toList();  // [1..5]

// GCD-style fold
int gcd = v.stream().reduce(0, (a, b) -> b == 0 ? a : gcd(b, a % b));
```

### Collectors

```java
import static java.util.stream.Collectors.*;

// to a collection
List<Integer> list = v.stream().collect(toList());
Set<Integer> set   = v.stream().collect(toSet());

// joining strings  (std::accumulate over strings)
String csv = v.stream().map(String::valueOf).collect(joining(", "));

// grouping  (no direct STL analogue)
Map<Boolean, List<Integer>> byParity =
    v.stream().collect(groupingBy(x -> x % 2 == 0));

// partitioning
Map<Boolean, List<Integer>> parts =
    v.stream().collect(partitioningBy(x -> x > 2));

// counting per group
Map<Integer, Long> counts =
    v.stream().collect(groupingBy(x -> x % 3, counting()));

// to a map
Map<Integer, Integer> squares =
    v.stream().collect(toMap(x -> x, x -> x * x));
```

---

## Iterators

### Iterator Basics
```java
List<Integer> v = new ArrayList<>(List.of(1, 2, 3));

// Forward iterator
Iterator<Integer> it = v.iterator();
while (it.hasNext()) {
    int x = it.next();
    if (x == 2) it.remove();           // safe removal during iteration
}

// ListIterator (bidirectional, like a random-access iterator)
ListIterator<Integer> lit = v.listIterator();
lit.next();
lit.previous();
lit.set(99);                           // replace current

// Enhanced-for uses an iterator under the hood
for (int x : v) { }
```

### Common Patterns
```java
// Remove while iterating (preferred)
v.removeIf(x -> x % 2 == 0);

// Find and remove first match
v.remove(Integer.valueOf(42));         // by value (not index!)

// Stream-based search
Optional<Integer> found = v.stream().filter(x -> x > 1).findFirst();
```

---

## Performance Characteristics

| Collection | Access | Insert | Delete | Search |
|------------|--------|--------|--------|--------|
| ArrayList | O(1) | O(n) | O(n) | O(n) |
| LinkedList | O(n) | O(1)* | O(1)* | O(n) |
| ArrayDeque | O(1) ends | O(1) ends | O(1) ends | O(n) |
| HashSet / HashMap | - | O(1) avg | O(1) avg | O(1) avg |
| TreeSet / TreeMap | - | O(log n) | O(log n) | O(log n) |
| LinkedHashSet/Map | - | O(1) avg | O(1) avg | O(1) avg |
| PriorityQueue | O(1) peek | O(log n) | O(log n) | O(n) |

\* O(1) once you hold the node/iterator; locating the position is O(n).

---

## Quick Examples

### Finding Maximum
```java
int max = list.stream().mapToInt(Integer::intValue).max().orElseThrow();
```

### Sum of a List
```java
int sum = list.stream().mapToInt(Integer::intValue).sum();
```

### Count Occurrences
```java
long cnt = list.stream().filter(x -> x == value).count();
```

### Sort and Remove Duplicates
```java
List<Integer> result = list.stream().distinct().sorted().toList();
```

### Transform
```java
List<Integer> doubled = list.stream().map(x -> x * 2).toList();
```

### Check if Element Exists
```java
boolean exists = list.contains(value);
```

### Custom-Key Sort
```java
list.sort(Comparator.comparingInt(Math::abs));
```

---

## Tips

1. **ArrayList** for general-purpose random access (the default)
2. **ArrayDeque** for stack/queue and efficient front/back operations
3. **LinkedList** only when you truly need cheap node splicing
4. **TreeMap/TreeSet** when you need sorted order or navigation
5. **HashMap/HashSet** for fast unordered lookup
6. Use `computeIfAbsent` / `merge` for multimap and counter idioms
7. Use `removeIf` instead of manual iterator removal loops
8. Prefer streams for declarative transforms; loops for side-effect-heavy code
9. Use `List.of` / `Map.of` / `Set.of` for immutable snapshots
10. Remember to `import java.util.*` and `java.util.stream.*`
