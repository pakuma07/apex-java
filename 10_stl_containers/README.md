# Chapter 10: The Java Collections Framework

The **Java Collections Framework (JCF)** is a unified set of interfaces and classes — in `java.util` — that manage collections of objects for you, handling memory allocation, growth, and cleanup automatically (the garbage collector reclaims unreferenced nodes/arrays). Where C++ separates *sequence containers* (`vector`, `list`, `deque`), *associative containers* (`map`, `set`), and *unordered associative containers* (`unordered_map`, `unordered_set`) into distinct, unrelated templates, Java organizes everything under **two root interfaces**: `Collection<E>` (lists, sets, queues) and `Map<K,V>` (key-value tables). Concrete classes such as `ArrayList`, `HashSet`, and `TreeMap` *implement* these interfaces, and you usually program to the interface (`List<Integer> v = new ArrayList<>()`) rather than the class.

Choosing the right collection is mostly about matching the access pattern of your problem to the performance guarantees of the implementation: whether you need random access, fast insertion in the middle, sorted iteration, or constant-time key lookup. This chapter surveys each collection with its core operations and complexity characteristics, then covers iteration (`Iterator`/`ListIterator`/enhanced `for`), the `Collections` utility class, immutable collections (`List.of`), and the all-important difference between Java generics over **reference types only** (no `List<int>`) and C++ templates over any type. Keep the comparison tables in sections 10.9 and the Summary handy as a quick reference.

> **C++ STL container ↔ Java collection — at a glance**
> - `std::vector<T>` ↔ `ArrayList<T>` (dynamic array, O(1) random access)
> - `std::list<T>` ↔ `LinkedList<T>` (doubly linked list)
> - `std::deque<T>` ↔ `ArrayDeque<T>` (array-backed double-ended queue)
> - `std::map<K,V>` ↔ `TreeMap<K,V>` (sorted, red-black tree, O(log n))
> - `std::unordered_map<K,V>` ↔ `HashMap<K,V>` (hash table, O(1) average)
> - `std::set<T>` ↔ `TreeSet<T>`; `std::unordered_set<T>` ↔ `HashSet<T>`
> - `std::stack` ↔ `ArrayDeque` (as a stack) / legacy `Stack`
> - `std::queue` ↔ `ArrayDeque` (as a queue); `std::priority_queue` ↔ `PriorityQueue`
> - `std::multiset`/`multimap` ↔ no direct class — use `TreeMap<K, Integer>` (counts) or `Map<K, List<V>>`
> - **Big difference:** Java collections hold **objects only** — primitives are autoboxed (`List<Integer>`, never `List<int>`).

### The Hierarchy

```
                     Iterable<E>
                         |
                   Collection<E>
        +----------------+----------------+
        |                |                |
     List<E>          Set<E>          Queue<E>
        |                |                |
  ArrayList         HashSet          ArrayDeque (also Deque)
  LinkedList        LinkedHashSet    PriorityQueue
  (Vector)          TreeSet (SortedSet/NavigableSet)
                    (also Deque<E> extends Queue<E>)

      Map<K,V>                 (NOT a Collection — separate root)
        |
   HashMap
   LinkedHashMap
   TreeMap (SortedMap/NavigableMap)
   (Hashtable)
```

`Map` is deliberately **not** a `Collection` (a map is not a sequence of single elements). This mirrors C++ where `std::map` is also conceptually distinct, but in Java the separation is formalized in the type system.

## 10.1 ArrayList (the `vector` equivalent)

`ArrayList<E>` is a dynamically resizable array and is the default collection you should reach for unless you have a specific reason not to. Its elements live in a backing `Object[]`, giving O(1) random access via `get(i)`/`set(i, e)` and excellent iteration performance. Appending with `add` is amortized O(1), but inserting or removing anywhere except the end is O(n) because following elements must shift. When the array outgrows its capacity it allocates a larger backing array (typically 1.5×) and copies — call `ensureCapacity(n)` or the `new ArrayList<>(n)` constructor up front when you know the final size. Unlike C++ `vector::at`, **every** `ArrayList` access is bounds-checked and throws `IndexOutOfBoundsException`.

```java
import java.util.*;

List<Integer> v = new ArrayList<>(List.of(1, 2, 3, 4, 5));

// Access (always bounds-checked; throws IndexOutOfBoundsException)
v.get(0);                  // 1
v.get(2);                  // 3
v.get(0);                  // 1  (front)
v.get(v.size() - 1);       // 5  (back)

// Size
v.size();                  // 5
v.isEmpty();               // false

// Modification
v.add(6);                  // Add to end
v.remove(v.size() - 1);    // Remove from end
v.add(2, 99);              // Insert 99 at index 2
v.remove(1);               // Remove at index 1 (removes by INDEX for int arg!)
v.clear();                 // Remove all

// Iteration
for (int x : v) System.out.print(x);                 // enhanced for
for (Iterator<Integer> it = v.iterator(); it.hasNext();) System.out.print(it.next());
```

> **Trap:** `remove(int)` removes by *index*, while `remove(Object)` removes by *value*. For a `List<Integer>`, `v.remove(2)` deletes index 2; to delete the value 2 use `v.remove(Integer.valueOf(2))`. C++ has no such ambiguity because `vector` removal is iterator-based.

## 10.2 LinkedList (the `list` equivalent)

`LinkedList<E>` is a doubly linked list: each element sits in its own node holding a value plus references to the previous and next nodes. Because nodes are not contiguous it has O(n) indexed access (`get(i)` walks from the nearer end) and O(n) search, but insertion and deletion *at a known position* (via a `ListIterator`, or at the ends) are O(1). Choose `LinkedList` when you do many insertions/removals at the ends or via an iterator and rarely need indexed access; otherwise `ArrayList` is usually faster in practice due to cache locality. `LinkedList` also implements `Deque`, so it doubles as a stack/queue.

```java
import java.util.*;

LinkedList<Integer> l = new LinkedList<>(List.of(1, 2, 3, 4, 5));

// Indexed access exists but is O(n) — avoid in loops
// l.get(0);  // works, but slow for large lists

// End operations are O(1)
l.addLast(6);              // Add to end   (== add)
l.removeLast();            // Remove from end
l.addFirst(0);             // Add to front
l.removeFirst();           // Remove from front

// Efficient insertion/deletion via a ListIterator at a known position
ListIterator<Integer> it = l.listIterator();
it.next();                 // advance to a known position
it.add(99);                // O(1) insert here
it.previous();
it.remove();               // O(1) remove

for (int x : l) System.out.print(x);
```

> **Iterator stability difference:** In C++ `std::list`, iterators/pointers to *other* elements stay valid across insert/erase. Java's `Iterator` is **fail-fast**: structurally modifying a `LinkedList` through anything other than the iterator's own `add`/`remove` throws `ConcurrentModificationException` on the next `next()`. Use `ListIterator.add`/`remove` to mutate while iterating.

## 10.3 ArrayDeque (the `deque` equivalent)

`ArrayDeque<E>` supports fast amortized O(1) insertion and removal at *both* the front and the back — what `ArrayList` cannot offer at the front. It is backed by a **circular array** that grows as needed. Unlike C++ `std::deque`, it does **not** provide O(1) indexed random access — there is no `get(int)`; it is purely a double-ended queue. It is the recommended implementation for both `Stack` and `Queue` semantics (faster than `LinkedList` and the legacy `Stack`). `null` elements are forbidden (because `null` is used as an internal "empty" sentinel by the polling methods).

```java
import java.util.*;

Deque<Integer> dq = new ArrayDeque<>(List.of(1, 2, 3));

// Efficient at both ends
dq.addFirst(0);            // Add to front  (offerFirst returns boolean)
dq.addLast(4);             // Add to back   (offerLast)
dq.pollFirst();            // Remove from front (null if empty)
dq.pollLast();             // Remove from back

// Peek without removing
dq.peekFirst();            // front element
dq.peekLast();             // back element

// NOTE: no indexed access — for that, use ArrayList instead
```

> **Difference:** C++ `std::deque` gives O(1) `operator[]`; `ArrayDeque` does **not** support indexing at all. If you need both ends *and* random access in Java, there is no single standard class — use `ArrayList` and accept O(n) front removal, or restructure the algorithm.

## 10.4 TreeMap (the `map` equivalent)

`TreeMap<K,V>` is a sorted map of unique key→value entries, implemented as a balanced binary search tree (a red-black tree). Insertion (`put`), lookup (`get`), and removal (`remove`) are all O(log n), and iterating walks the keys in sorted order. Keys must be mutually `Comparable`, or you supply a `Comparator` to the constructor. Unlike C++ `map::operator[]`, Java's `get` on a missing key simply returns `null` (it does **not** insert a default), and `put` returns the previous value (or `null`). `TreeMap` adds rich *navigable* queries C++ matches with `lower_bound`/`upper_bound`: `floorKey`, `ceilingKey`, `headMap`, `tailMap`, `subMap`.

```java
import java.util.*;

TreeMap<String, Integer> ages = new TreeMap<>();

// Insert
ages.put("Alice", 25);
ages.put("Bob", 30);
ages.putIfAbsent("Charlie", 35);

// Access — returns null if absent (no accidental insertion)
ages.get("Alice");                  // 25
ages.getOrDefault("Dan", -1);       // -1

// Find / contains
if (ages.containsKey("Bob")) {
    System.out.println(ages.get("Bob"));   // 30
}

// Remove
ages.remove("Bob");

// Iterate (sorted by key)
for (Map.Entry<String, Integer> e : ages.entrySet()) {
    System.out.println(e.getKey() + ": " + e.getValue());
}

// Navigable queries (C++ lower_bound / upper_bound analogues)
ages.firstKey();           // smallest key
ages.lastKey();            // largest key
ages.ceilingKey("B");      // least key >= "B"
ages.floorKey("B");        // greatest key <= "B"
```

## 10.5 HashMap (the `unordered_map` equivalent)

`HashMap<K,V>` stores unique key→value entries in a hash table, giving average O(1) `put`, `get`, and `remove` — typically faster than `TreeMap`'s O(log n) — at the cost of *no* ordering when iterating. Worst-case operations historically degraded to O(n) under heavy collisions; modern Java mitigates this by converting an overloaded bucket from a linked list into a balanced tree, bounding the worst case at O(log n). Keys must implement `hashCode()` and `equals()` consistently (built in for `String`, `Integer`, etc.; you must override both for your own classes). Sizing ahead with `new HashMap<>(expectedSize)` avoids repeated *rehashing*. Choose `HashMap` for pure lookup speed; choose `TreeMap` when you need sorted traversal or range queries.

```java
import java.util.*;

Map<String, Integer> map = new HashMap<>();

// Same operations as TreeMap, but unordered
map.put("key1", 10);
map.put("key2", 20);

// Faster average access: O(1) vs TreeMap's O(log n) — but iteration order undefined
map.merge("key1", 5, Integer::sum);          // key1 -> 15 (handy for counting)
map.computeIfAbsent("key3", k -> 0);

for (var entry : map.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}
```

> **Iterator difference:** Rehashing a C++ `unordered_map` invalidates iterators (references survive). In Java, modifying a `HashMap` during iteration is **fail-fast** (throws `ConcurrentModificationException`); remove via the iterator instead.

### LinkedHashMap

`LinkedHashMap<K,V>` is a `HashMap` that *also* maintains a doubly linked list across its entries, so iteration follows **insertion order** (or, optionally, *access* order for building LRU caches). It gives the O(1) lookup of `HashMap` with predictable iteration. C++ has no direct equivalent.

```java
Map<String, Integer> ordered = new LinkedHashMap<>();
ordered.put("c", 3);
ordered.put("a", 1);
ordered.put("b", 2);
// Iterates in insertion order: c, a, b  (HashMap would be arbitrary)
```

## 10.6 TreeSet / HashSet / LinkedHashSet (the `set` family)

`Set<E>` holds *unique* elements (no duplicates). Java offers three implementations, mirroring the map ones:

- **`HashSet`** — hash table, average O(1) add/contains/remove, **unordered**. The `unordered_set` equivalent.
- **`TreeSet`** — red-black tree, O(log n), iterates in **sorted** order, with navigable queries (`first`, `last`, `floor`, `ceiling`, `headSet`, `tailSet`). The `set` equivalent.
- **`LinkedHashSet`** — hash table + linked list, O(1), iterates in **insertion order**.

```java
import java.util.*;

// TreeSet: sorted, unique (C++ std::set)
TreeSet<Integer> s = new TreeSet<>(List.of(3, 1, 4, 1, 5));  // duplicates ignored

s.size();                  // 4 (one duplicate removed)
s.add(2);

if (s.contains(3)) {       // O(log n)
    System.out.println(s.first() + ".." + s.last());  // 1..5
}

for (int x : s) System.out.print(x + " ");  // 1 2 3 4 5 (sorted)

s.remove(2);

// HashSet: fast membership, unordered (C++ std::unordered_set)
Set<Integer> seen = new HashSet<>(List.of(3, 1, 4, 5));
seen.add(2);
seen.remove(3);
if (seen.contains(4)) System.out.println("Found");  // O(1) average
```

> **Difference:** C++ `set` elements are effectively `const`; in Java you likewise must not mutate an element in a way that changes its ordering/`hashCode` while it is in the set — remove and re-add instead.

## 10.7 Queue & Stack (container adaptors)

C++ exposes `std::stack` and `std::queue` as *adaptors* over an underlying container. Java instead defines the `Queue<E>` and `Deque<E>` **interfaces** and recommends `ArrayDeque` as the implementation for both stack and queue use.

A `Queue` is FIFO — add to the back, remove from the front. A stack is LIFO — push and pop the same end. `Deque` (double-ended queue) provides both disciplines with explicit `push`/`pop`/`peek` (stack view) and `offer`/`poll`/`peek` (queue view). All are O(1).

```java
import java.util.*;

// Queue (FIFO) — use ArrayDeque via the Queue interface
Queue<Integer> q = new ArrayDeque<>();
q.offer(1);
q.offer(2);
q.offer(3);
System.out.println(q.peek());   // 1   (front, null if empty)
q.poll();                       // remove 1
System.out.println(q.peek());   // 2

// Stack (LIFO) — use ArrayDeque as a stack (preferred over legacy java.util.Stack)
Deque<Integer> st = new ArrayDeque<>();
st.push(1);
st.push(2);
st.push(3);
System.out.println(st.peek());  // 3 (top)
st.pop();                       // remove 3
System.out.println(st.peek());  // 2
```

> **Difference:** the legacy `java.util.Stack` (a synchronized subclass of `Vector`) still exists but is discouraged; prefer `ArrayDeque`. Java queues offer *two* method families: throwing (`add`/`remove`/`element`) and returning-special-value (`offer`/`poll`/`peek`). C++ adaptors only ever throw/UB.

## 10.8 HashSet vs TreeSet recap

(See 10.6.) The hashed `HashSet` is the go-to for "have I seen this value before?" when order does not matter; `TreeSet` is for sorted, navigable traversal. Both reject duplicates by `equals`/`hashCode` (`HashSet`) or by `compareTo`/`Comparator` (`TreeSet`).

```java
Set<Integer> s = new HashSet<>(List.of(3, 1, 4, 1, 5));  // {1,3,4,5} (some order)
s.add(2);
s.remove(3);
if (s.contains(4)) System.out.println("Found");   // O(1) average
for (int x : s) System.out.print(x + " ");          // unordered
```

---

## 10.9 Collection Comparison

The table summarizes asymptotic complexity and ordering for the collections in this chapter. Read it by asking what operation dominates your workload — random access, end insertion, search, or ordered iteration — then pick the implementation whose cheapest column matches. As with C++, these are *Big-O* figures: an `ArrayList` with contiguous backing storage often beats node-based collections in real timings thanks to cache locality, so treat the table as a starting point.

| Collection | Random Access | Insert/Delete | Search | Order | C++ analogue |
|------------|---------------|---------------|--------|-------|--------------|
| **ArrayList** | O(1) | O(n) (O(1) at end) | O(n) | Insertion | `vector` |
| **LinkedList** | O(n) | O(1) at ends/iterator | O(n) | Insertion | `list` |
| **ArrayDeque** | — (no index) | O(1) both ends | O(n) | Insertion | `deque` |
| **TreeMap** | — | O(log n) | O(log n) | Sorted key | `map` |
| **TreeSet** | — | O(log n) | O(log n) | Sorted | `set` |
| **HashMap** | — | O(1) avg | O(1) avg | Unordered | `unordered_map` |
| **HashSet** | — | O(1) avg | O(1) avg | Unordered | `unordered_set` |
| **LinkedHashMap/Set** | — | O(1) avg | O(1) avg | Insertion/access | (none) |
| **ArrayDeque (Queue)** | — | O(1) | — | FIFO | `queue` |
| **ArrayDeque (Stack)** | — | O(1) | — | LIFO | `stack` |
| **PriorityQueue** | — | O(log n) | O(n) | Heap (min by default) | `priority_queue` |

---

## 10.10 Arrays and `Arrays`/`List.of` (the `std::array` story)

C++'s `std::array<T,N>` is a fixed-size, stack-allocated array that knows its own size and works with algorithms. Java's nearest equivalents are the built-in **array** (`int[]`, fixed length, heap-allocated, knows its length via `.length`) and immutable **`List.of(...)`** for an object list. The `java.util.Arrays` utility class supplies the algorithm-style operations.

```java
import java.util.*;

// Built-in array: fixed length, knows its size, heap-allocated (not stack)
int[] arr = {10, 20, 30, 40, 50};
arr[0];                    // 10 (bounds-checked: ArrayIndexOutOfBoundsException)
arr.length;                // 5  (a field, not a method)

Arrays.fill(arr, 0);       // all elements become 0  (C++ arr.fill(0))

int[] nums = {5, 3, 1, 4, 2};
Arrays.sort(nums);                          // {1,2,3,4,5}  (C++ std::sort)
int idx = Arrays.binarySearch(nums, 4);     // index of 4
System.out.println(Arrays.toString(nums));  // [1, 2, 3, 4, 5]

// Immutable fixed list (closest to "array that is also a Collection")
List<Integer> fixed = List.of(10, 20, 30); // unmodifiable; add() throws
```

| Feature | C++ raw array | `std::array` | Java `int[]` | Java `List.of` |
|---------|---------------|--------------|--------------|----------------|
| Knows its own size | No (decays) | Yes `.size()` | Yes `.length` | Yes `.size()` |
| Bounds-checked access | No | `.at()` | **Always** | Always |
| Allocation | Stack/static | Stack | **Heap** (GC) | Heap (GC) |
| Resize at runtime | No | No | No | No |
| Works with algorithms | Partial | Full | via `Arrays`/streams | via Stream |

> **Difference:** Java has **no stack-allocated objects**. Even a fixed-size array lives on the heap and is reclaimed by the GC; the array *reference* lives on the stack.

---

## 10.11 PriorityQueue

`PriorityQueue<E>` is a binary-heap collection. By default it is a **min-heap** (smallest element on top) — the *opposite* of C++ `std::priority_queue`, which defaults to a **max-heap**.

```java
import java.util.*;

// Min-heap (DEFAULT in Java) — smallest on top
PriorityQueue<Integer> minpq = new PriorityQueue<>();
minpq.add(3); minpq.add(1); minpq.add(4); minpq.add(5);
System.out.println(minpq.peek());   // 1 (smallest)
minpq.poll();                       // remove 1
System.out.println(minpq.peek());   // 3

// Max-heap — pass a reverse comparator (C++'s default behaviour)
PriorityQueue<Integer> maxpq = new PriorityQueue<>(Comparator.reverseOrder());
maxpq.add(3); maxpq.add(1); maxpq.add(4); maxpq.add(5);
System.out.println(maxpq.peek());   // 5 (largest)

// Custom comparator for objects (C++ struct + comparator functor)
record Task(int priority, String name) {}

PriorityQueue<Task> taskQueue =
    new PriorityQueue<>(Comparator.comparingInt(Task::priority).reversed());
taskQueue.add(new Task(3, "Low priority task"));
taskQueue.add(new Task(10, "Urgent task"));
taskQueue.add(new Task(5, "Medium task"));
System.out.println(taskQueue.peek().name());  // "Urgent task"
```

| Operation | Complexity |
|-----------|-----------|
| `add(x)` / `offer(x)` | O(log n) |
| `peek()` | O(1) |
| `poll()` | O(log n) |
| `isEmpty()` / `size()` | O(1) |

> **Two key differences from C++:** (1) Java defaults to **min-heap**, C++ to **max-heap** — flip the comparator. (2) `PriorityQueue` iteration (`for`/`iterator`) does **not** visit elements in priority order; only `poll`/`peek` respect ordering.

**Common uses:** Dijkstra's algorithm, scheduling, top-K elements.

---

## 10.12 Multiset / multimap — there is no direct equivalent

Java has **no** built-in `multiset` or `multimap`. The idiomatic substitutes:

- **Multiset (frequency bag):** `Map<K, Integer>` with `merge(key, 1, Integer::sum)` to count, or `Map<K, Long>` from a stream `groupingBy(..., counting())`.
- **Multimap (one-to-many):** `Map<K, List<V>>` with `computeIfAbsent(key, k -> new ArrayList<>()).add(value)`. (Guava's `Multimap`/`Multiset` are popular third-party options, but not part of the JDK.)

```java
import java.util.*;

// --- "multiset": count occurrences using a Map<K,Integer> ---
int[] data = {3, 1, 4, 1, 5, 9, 2, 6, 5};
Map<Integer, Integer> counts = new TreeMap<>();   // TreeMap keeps keys sorted
for (int x : data) counts.merge(x, 1, Integer::sum);

System.out.println(counts.getOrDefault(5, 0));    // 2
System.out.println(counts.getOrDefault(7, 0));    // 0

// "erase one occurrence": decrement, removing the key at zero
counts.computeIfPresent(5, (k, c) -> c > 1 ? c - 1 : null);

// --- "multimap": one key, many values using Map<K, List<V>> ---
Map<String, List<Integer>> mm = new HashMap<>();
mm.computeIfAbsent("alice", k -> new ArrayList<>()).add(90);
mm.computeIfAbsent("alice", k -> new ArrayList<>()).add(85);  // duplicate key kept
mm.computeIfAbsent("bob",   k -> new ArrayList<>()).add(72);

System.out.println(mm.get("alice").size());       // 2
for (int v : mm.get("alice")) System.out.print(v + " ");  // 90 85
```

| C++ container | Java substitute |
|---------------|-----------------|
| `multiset` | `TreeMap<K, Integer>` / `HashMap<K, Integer>` (counts) |
| `multimap` | `TreeMap<K, List<V>>` / `HashMap<K, List<V>>` |

---

## 10.13 Pair and record (the `std::pair`/`std::tuple` story)

Java's standard library has **no general `Pair` or `Tuple`** type (the old `AbstractMap.SimpleEntry` and `Map.Entry` are the closest two-value holders). The modern, idiomatic way to bundle a fixed set of heterogeneous values is a **`record`** (Java 16+) — a concise, immutable data class with auto-generated constructor, accessors, `equals`, `hashCode`, and `toString`.

```java
import java.util.*;

// --- two values: Map.Entry is the JDK's "pair" ---
Map.Entry<String, Integer> person = Map.entry("Alice", 30);
person.getKey();           // "Alice"
person.getValue();         // 30   (entries from Map.entry are immutable)

// Entries are what Map iteration yields:
Map<String, Integer> scores = new TreeMap<>();
scores.put("Alice", 95);
scores.put("Bob", 82);
for (Map.Entry<String, Integer> e : scores.entrySet()) {
    System.out.println(e.getKey() + ": " + e.getValue());
}

// --- N values / heterogeneous record: use a record (replaces std::tuple) ---
record Rec(String name, int age, double gpa) {}

Rec rec = new Rec("Bob", 26, 4.0);
rec.name();                // "Bob"   (named accessors — clearer than get<0>)
rec.age();                 // 26
rec.gpa();                 // 4.0

// "Unpacking" via record pattern (Java 21 pattern matching):
if (parseInput("hello") instanceof Parsed(boolean ok, int len, String val)) {
    System.out.println(ok + " " + len + " " + val);   // true 5 hello
}

record Parsed(boolean ok, int len, String val) {}
static Parsed parseInput(String s) {
    return s.isEmpty() ? new Parsed(false, 0, "empty")
                       : new Parsed(true, s.length(), s);
}
```

| | C++ `pair` | C++ `tuple` | Java `Map.Entry` | Java `record` |
|---|---|---|---|---|
| Elements | Exactly 2 | Any number | Exactly 2 | Any number |
| Access | `.first`/`.second` | `get<N>` | `getKey`/`getValue` | named accessors |
| Mutable | Yes | Yes | No (immutable) | No (immutable) |
| Named fields | No | No | No | **Yes** |

> **Difference:** C++ tuples are positional (`get<0>`); a Java `record` gives every field a *name*, which is more readable and is the recommended replacement for returning multiple values.

---

## Summary

| Collection | Best For | C++ analogue |
|------------|----------|--------------|
| **ArrayList** | General purpose, random access | `vector` |
| **LinkedList** | Frequent end/iterator insert-delete; also a Deque | `list` |
| **ArrayDeque** | Add/remove both ends; stacks & queues | `deque` |
| **TreeMap** | Sorted key-value pairs, range queries | `map` |
| **TreeSet** | Unique elements, sorted, navigable | `set` |
| **HashMap** | Key-value pairs, fast lookup | `unordered_map` |
| **HashSet** | Unique elements, fast membership | `unordered_set` |
| **LinkedHashMap/Set** | Predictable iteration order, LRU caches | (none) |
| **PriorityQueue** | Heap-ordered access, top-K | `priority_queue` |
| **record** | N heterogeneous values; multiple returns | `tuple` |
| **Map.Entry** | Two values; map iteration | `pair` |

---

## 10.14 Fail-Fast Iterators (the iterator-invalidation story)

C++ has detailed per-container *iterator-invalidation* rules: a `vector` reallocation invalidates all iterators, a `list` erase invalidates only the erased one, and so on — using an invalidated iterator is **undefined behavior**. Java takes a different, safer approach: most `java.util` collections return **fail-fast iterators**. They track a modification count, and if the collection is structurally modified by anything *other than the iterator itself* between iterator creation and use, the next `next()` throws `ConcurrentModificationException` — a clean, immediate failure instead of silent corruption.

```java
import java.util.*;

List<Integer> v = new ArrayList<>(List.of(1, 2, 3, 4, 5));

// ❌ Structural modification during enhanced-for -> ConcurrentModificationException
// for (int x : v) { if (x == 3) v.remove(Integer.valueOf(3)); }

// ✅ Correct: remove THROUGH the iterator
for (Iterator<Integer> it = v.iterator(); it.hasNext();) {
    if (it.next() == 3) it.remove();      // safe; updates the mod count
}

// ✅ Even simpler with a predicate (Java 8+)
v.removeIf(x -> x == 3);

// ListIterator can insert/replace while traversing a List
ListIterator<Integer> lit = v.listIterator();
while (lit.hasNext()) {
    int x = lit.next();
    if (x % 2 == 0) lit.set(x * 10);      // replace in place
}
```

| Action while iterating | C++ | Java |
|------------------------|-----|------|
| Mutate via container directly | Undefined behavior (UB) | `ConcurrentModificationException` (fail-fast) |
| Mutate via the iterator | Often safe (`erase` returns next) | Safe (`Iterator.remove`, `ListIterator.add/set`) |
| Detection | None at runtime (need ASan/Valgrind) | Built-in, immediate exception |

**Golden rules:**
1. Never structurally modify a collection inside an enhanced `for` over it — use `Iterator.remove`, `removeIf`, or `ListIterator`.
2. Fail-fast is *best-effort*, not a concurrency guarantee. For concurrent access use `java.util.concurrent` collections (e.g. `ConcurrentHashMap`, `CopyOnWriteArrayList`), whose iterators are weakly consistent and don't throw.
3. There is no manual `delete` and no dangling iterator: the GC keeps backing nodes/arrays alive as long as anything references them.

---

## 10.15 Iterator and ListIterator

`Iterator<E>` is the universal forward cursor returned by `Collection.iterator()`: `hasNext()`, `next()`, and the optional `remove()`. `ListIterator<E>` (from `List.listIterator()`) extends it for sequences with **bidirectional** traversal plus in-place `add`, `set`, and index queries — the closest match to C++'s bidirectional/random-access iterators.

```java
import java.util.*;

List<Integer> v = new ArrayList<>(List.of(1, 2, 3, 4, 5));

// Iterator: forward-only (like a C++ input/forward iterator)
Iterator<Integer> it = v.iterator();
while (it.hasNext()) {
    int x = it.next();
    if (x == 2) it.remove();        // removes 2; all other elements survive
}

// ListIterator: bidirectional + add/set (like a C++ bidirectional iterator)
ListIterator<Integer> lit = v.listIterator();
while (lit.hasNext()) {
    int idx = lit.nextIndex();      // current index (C++ distance(begin,it))
    int x = lit.next();
    if (x > 3) lit.set(x * 2);      // replace in place
}
// Walk backward
while (lit.hasPrevious()) {
    System.out.print(lit.previous() + " ");
}
```

| Capability | `Iterator` | `ListIterator` | C++ analogue |
|------------|-----------|----------------|--------------|
| `hasNext`/`next` | Yes | Yes | `it != end()`, `*it++` |
| `remove` | Yes | Yes | `erase(it)` |
| `hasPrevious`/`previous` | No | Yes | bidirectional `--it` |
| `add` / `set` | No | Yes | `insert` / `*it = v` |
| index (`nextIndex`) | No | Yes | `distance(begin, it)` |

> **Note:** Java iterators are *not* dereferenced (`*it`) or incremented (`++it`); you call `next()` which both advances and returns. There is no separate `end()` sentinel — you test `hasNext()`.

---

## 10.16 The `Collections` Utility Class

`java.util.Collections` is a class of static helper methods that operate on collections — the rough analogue of free-standing C++ `<algorithm>` calls plus some `vector`/container conveniences. (The bulk of C++ `<algorithm>` maps to the **Streams API**, covered in Chapter 11; this section covers the in-place, `List`-oriented helpers.)

```java
import java.util.*;

List<Integer> v = new ArrayList<>(List.of(5, 2, 8, 1, 9));

Collections.sort(v);                              // ascending (C++ std::sort)
Collections.sort(v, Comparator.reverseOrder());   // descending (greater<int>)
Collections.reverse(v);                            // C++ std::reverse
Collections.shuffle(v);                            // C++ std::shuffle

int max = Collections.max(v);                      // C++ std::max_element (value)
int min = Collections.min(v);                      // C++ std::min_element (value)

Collections.sort(v);
int idx = Collections.binarySearch(v, 8);          // needs sorted list (std::binary_search)

int freq = Collections.frequency(v, 8);            // count of 8 (std::count)
Collections.swap(v, 0, 4);                          // std::swap on elements
Collections.fill(v, 0);                             // std::fill
List<Integer> immutable = Collections.unmodifiableList(v);  // read-only view
```

| `Collections` method | C++ `<algorithm>` analogue |
|----------------------|----------------------------|
| `sort(list)` / `sort(list, cmp)` | `sort(first, last[, cmp])` |
| `binarySearch(list, key)` | `binary_search` / `lower_bound` |
| `max(coll)` / `min(coll)` | `max_element` / `min_element` |
| `reverse` / `rotate` / `shuffle` | `reverse` / `rotate` / `shuffle` |
| `frequency(coll, o)` | `count` |
| `unmodifiableList`/`Set`/`Map` | (no STL equivalent) |

---

## 10.17 Immutable Collections (`List.of`, `Set.of`, `Map.of`)

Java 9 added concise factory methods for **immutable** (unmodifiable) collections: `List.of(...)`, `Set.of(...)`, `Map.of(k1,v1,...)`, and `Map.ofEntries(...)`. The returned objects throw `UnsupportedOperationException` on any mutating call, reject `null` elements, and are compact and shareable. They are the idiomatic way to declare constant collections — far more concise than the old `Collections.unmodifiableList(new ArrayList<>(Arrays.asList(...)))`.

```java
import java.util.*;

List<Integer> nums = List.of(1, 2, 3);             // immutable list
Set<String>  tags  = Set.of("a", "b", "c");        // immutable set (no dup, no null)
Map<String, Integer> m = Map.of("x", 1, "y", 2);   // immutable map (up to 10 pairs)

// nums.add(4);   // throws UnsupportedOperationException

// Many entries:
Map<String, Integer> big = Map.ofEntries(
    Map.entry("a", 1),
    Map.entry("b", 2));

// Defensive immutable copy of an existing collection (Java 10+):
List<Integer> snapshot = List.copyOf(nums);

// Stream collectors can also produce unmodifiable results:
List<Integer> evens = nums.stream().filter(x -> x % 2 == 0).toList();  // Java 16+: immutable
```

> **Contrast with C++:** C++ expresses immutability with `const` on the container or `constexpr` for compile-time data. Java has no `const` for objects; immutability is achieved by *unmodifiable wrappers/factories* enforced at runtime. A `final List` reference cannot be reassigned, but its contents are still mutable unless the list itself is unmodifiable — `final` ≠ immutable.

---

## Next Steps
- Program to the interface (`List`, `Map`, `Set`), not the implementation.
- Choose `ArrayList`/`HashMap`/`HashSet` by default; escalate to `Tree*`/`Linked*` for ordering needs.
- Prefer immutable `List.of`/`Map.of` for constants.
- Move to [Chapter 11: Streams and Collections Algorithms](../11_stl_algorithms/README.md)
```