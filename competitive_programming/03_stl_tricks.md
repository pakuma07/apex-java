# 03 — Java Collections Tricks for Competitive Programming

The Java Collections Framework is your most powerful tool. Master these patterns to write correct solutions fast. (This is the Java analogue of C++ STL: `ArrayList`, `HashMap`, `TreeMap`/`TreeSet`, `PriorityQueue`, `ArrayDeque`, and the `Collections`/`Arrays` utility classes.)

---

## 3.1 ArrayList (and primitive arrays)

```java
ArrayList<Integer> v = new ArrayList<>(n);   // preallocate capacity
Collections.fill(v, 0);                       // (list must already have size)
v.add(x);                                     // amortised O(1) at the end
v.add(0, x);                                  // O(N) — insert at front (avoid in hot loops)
v.remove(i);                                  // O(N) — remove at index i (Integer index)

// Erase without preserving order — O(1)
v.set(i, v.get(v.size() - 1));
v.remove(v.size() - 1);

// Prefer primitive arrays in hot loops (no autoboxing):
int[] a = new int[n];
Arrays.fill(a, -1);                           // fill all with -1

// 2D array initialisation
int R = 4, C = 5;
int[][] grid = new int[R][C];                 // default-initialised to 0
// or with a fill value:
int[][] g2 = new int[R][C];
for (int[] row : g2) Arrays.fill(row, -1);

// Flatten 2D → 1D index
int idx = r * C + c;
int row = idx / C, col = idx % C;
```

> Note: `int[]` is fixed-size and boxing-free — strongly preferred for performance. Use `ArrayList<Integer>` only when you need dynamic growth and the cost of boxing is acceptable.

---

## 3.2 Sorting

```java
int[] a = {...};
Arrays.sort(a);                               // ascending (primitive, dual-pivot quicksort)

// Descending on primitives: sort ascending then reverse, or box to Integer[].
Integer[] b = {...};
Arrays.sort(b, Collections.reverseOrder());   // descending (objects)

ArrayList<int[]> v = new ArrayList<>();
// Custom comparator — sort by second element of a pair (int[]{first, second})
v.sort((x, y) -> Integer.compare(x[1], y[1]));

// Sort a list ascending / descending
Collections.sort(list);
list.sort(Collections.reverseOrder());

// Stable sort: Collections.sort and Arrays.sort(Object[]) are stable (merge sort).
//             Arrays.sort(int[]) is NOT stable (quicksort) and has no stability concept.

// "Partial sort" / "nth_element": Java has no direct equivalent.
//   - Top-K: use a PriorityQueue of size k (see 3.7).
//   - kth order statistic: quickselect manually, or sort and index.
```

> Anti-quicksort warning: `Arrays.sort(int[])` can be forced to O(N²) by adversarial input. If that's a risk, shuffle the array first (`Collections.shuffle` after boxing, or a manual Fisher–Yates) or sort a boxed `Integer[]` (guaranteed O(N log N) merge sort).

---

## 3.3 Binary Search

```java
// Requires a sorted array.
int[] v = {1, 3, 3, 5, 7};

// Does value exist? Arrays.binarySearch returns an index if found,
// otherwise -(insertionPoint) - 1.
int pos = Arrays.binarySearch(v, 3);
boolean found = pos >= 0;

// lower_bound (first index with v[i] >= val) and upper_bound (first v[i] > val)
// are NOT built in for primitives — write them once and reuse:
static int lowerBound(int[] a, int val) {     // first index >= val
    int lo = 0, hi = a.length;                 // [lo, hi)
    while (lo < hi) {
        int mid = (lo + hi) >>> 1;
        if (a[mid] < val) lo = mid + 1; else hi = mid;
    }
    return lo;
}
static int upperBound(int[] a, int val) {      // first index > val
    int lo = 0, hi = a.length;
    while (lo < hi) {
        int mid = (lo + hi) >>> 1;
        if (a[mid] <= val) lo = mid + 1; else hi = mid;
    }
    return lo;
}

// Count occurrences of val:
int occurrences = upperBound(v, 3) - lowerBound(v, 3);   // 2
```

> Use `>>> 1` (unsigned shift) for the midpoint to avoid overflow when `lo + hi` exceeds `Integer.MAX_VALUE`.

---

## 3.4 Pairs and Sorting Tricks

```java
// Java has no std::pair. Options:
//  - int[]{a, b}  (fast, mutable, no boxing of the array elements... well, ints)
//  - a record:    record Pair(int first, int second) {}
//  - Map.Entry / AbstractMap.SimpleEntry for key/value

int[] p = {3, 7};               // p[0] = first, p[1] = second

// int[] pairs do NOT sort lexicographically by default — provide a comparator:
ArrayList<int[]> edges = new ArrayList<>(List.of(
    new int[]{3,1}, new int[]{1,2}, new int[]{2,4}));
edges.sort((x, y) -> x[0] != y[0] ? Integer.compare(x[0], y[0])
                                  : Integer.compare(x[1], y[1]));

// Trick: store {-weight, node} in a PriorityQueue (min-heap) for max→min ordering.
// Or use a comparator with reverse order — no negation needed (see 3.7).
```

> A `record` gives you correct `equals`/`hashCode`/`toString` for free, and `Comparable` if you implement `compareTo` — handy when you need pairs as map keys or set members.

---

## 3.5 TreeSet (sorted set; replaces C++ set + lower_bound)

```java
TreeSet<Integer> s = new TreeSet<>();
s.add(3);
s.remove(3);
s.contains(3);          // boolean

// Navigation (the lower_bound / upper_bound equivalents):
Integer ge = s.ceiling(5);   // smallest element >= 5  (lower_bound)
Integer gt = s.higher(5);    // smallest element  > 5  (upper_bound)
Integer le = s.floor(5);     // largest  element <= 5
Integer lt = s.lower(5);     // largest  element  < 5

// Nearest smaller element than x:
Integer below = s.lower(x);  // largest value < x, or null if none

// Java has no multiset. Emulate with a TreeMap<value, count>:
TreeMap<Integer,Integer> ms = new TreeMap<>();
ms.merge(3, 1, Integer::sum);      // insert one 3
ms.merge(3, 1, Integer::sum);      // insert another 3  (count = 2)
// erase ONE occurrence:
ms.merge(3, -1, Integer::sum);
if (ms.get(3) == 0) ms.remove(3);
// total size of the multiset must be tracked separately if you need it.
```

> `ceiling`/`higher`/`floor`/`lower` return `null` (not an end-iterator) when nothing matches — always null-check.

---

## 3.6 HashMap and TreeMap

```java
// TreeMap — sorted by key, O(log N) ops (replaces C++ std::map)
TreeMap<String,Integer> freq = new TreeMap<>();
freq.merge("apple", 1, Integer::sum);   // freq["apple"]++
freq.containsKey("apple");
freq.get("apple");                       // null if absent — use getOrDefault

// Iterate in sorted key order:
for (Map.Entry<String,Integer> kv : freq.entrySet())
    System.out.println(kv.getKey() + " " + kv.getValue());

// HashMap — O(1) average, no ordering (replaces C++ std::unordered_map)
HashMap<Integer,Integer> ump = new HashMap<>(n * 2);   // initial capacity
ump.merge(key, 1, Integer::sum);
int val = ump.getOrDefault(key, 0);

// Anti-hack: Java's Integer.hashCode() is the identity, so HashMap<Integer,...>
// can be attacked with anti-hash inputs (forced collisions). Defences:
//  1. When keys are dense, use a plain int[] indexed by key (no hashing at all).
//  2. Salt the key with a random value chosen at runtime:
long SALT = new java.util.Random().nextLong();
// key' = key ^ SALT (then store key' in the map) — randomises bucket placement.
//  3. Or wrap keys in Long and mix bits with a splitmix-style scrambler.
static long mix(long x) {
    x ^= x >>> 33; x *= 0xff51afd7ed558ccdL;
    x ^= x >>> 33; x *= 0xc4ceb9fe1a85ec53L;
    x ^= x >>> 33; return x;
}
```

> Pitfall: `HashMap.get` returns a boxed `Integer` that may be `null`. Prefer `getOrDefault`/`merge`/`computeIfAbsent` to avoid `NullPointerException` and reduce boxing.

---

## 3.7 PriorityQueue

```java
// Min-heap is the DEFAULT in Java (opposite of C++ priority_queue, which is max).
PriorityQueue<Integer> minpq = new PriorityQueue<>();
minpq.add(3);
minpq.peek();    // smallest
minpq.poll();    // remove + return smallest

// Max-heap:
PriorityQueue<Integer> maxpq = new PriorityQueue<>(Collections.reverseOrder());

// Min-heap of pairs (int[]{dist, node}) — order by first element (distance):
PriorityQueue<int[]> pq = new PriorityQueue<>((x, y) -> Integer.compare(x[0], y[0]));

// Push {distance, node}:
pq.add(new int[]{0, src});
int[] top = pq.poll();
int dist = top[0], u = top[1];
```

> Remember: Java `PriorityQueue` is a **min-heap by default**. This is the single most common C++→Java porting bug — in C++ `priority_queue<int>` is a max-heap.

---

## 3.8 ArrayDeque (deque / stack / queue)

```java
ArrayDeque<Integer> dq = new ArrayDeque<>();
dq.addFirst(1);  dq.addLast(2);
dq.pollFirst();  dq.pollLast();
dq.peekFirst();  dq.peekLast();

// Use ArrayDeque for BOTH stack and queue — it is faster than Stack and
// LinkedList, and Stack/Vector are legacy/synchronised (avoid them).
// As a stack: push() = addFirst, pop() = pollFirst.
// As a queue: add()/offer() = addLast, poll() = pollFirst.

// Monotonic deque — sliding window maximum in O(N) (stores indices)
ArrayDeque<Integer> mono = new ArrayDeque<>();
for (int i = 0; i < n; ++i) {
    while (!mono.isEmpty() && a[mono.peekLast()] <= a[i]) mono.pollLast();
    mono.addLast(i);
    if (mono.peekFirst() <= i - k) mono.pollFirst();    // out of window
    if (i >= k - 1) sb.append(a[mono.peekFirst()]).append(' ');  // window max
}
```

---

## 3.9 Useful Utility Methods (Collections / Arrays / Math)

```java
int[] v = {...};

// Min / Max of an array (no streams in hot loops; loop or use streams for clarity)
int lo = Arrays.stream(v).min().getAsInt();
int hi = Arrays.stream(v).max().getAsInt();

// Sum of range (use long to avoid overflow)
long total = Arrays.stream(v).asLongStream().sum();

// Reverse a list
Collections.reverse(list);
// Reverse a primitive array: write a two-pointer swap loop (no built-in).

// Rotate a list left by k
Collections.rotate(list, -k);     // negative = left rotate

// Remove duplicates from a sorted array (analogue of sort + unique + erase):
Arrays.sort(v);
int m = 0;
for (int i = 0; i < v.length; ++i)
    if (i == 0 || v[i] != v[i - 1]) v[m++] = v[i];
// v[0..m) now holds the distinct values

// Next permutation: Java has no built-in. Implement it once:
static boolean nextPermutation(int[] a) {
    int i = a.length - 2;
    while (i >= 0 && a[i] >= a[i + 1]) i--;
    if (i < 0) return false;
    int j = a.length - 1;
    while (a[j] <= a[i]) j--;
    int t = a[i]; a[i] = a[j]; a[j] = t;
    for (int l = i + 1, r = a.length - 1; l < r; l++, r--) {
        t = a[l]; a[l] = a[r]; a[r] = t;
    }
    return true;
}

// "iota + sort by key" — index sort:
Integer[] idx = new Integer[n];
for (int i = 0; i < n; ++i) idx[i] = i;
Arrays.sort(idx, (x, y) -> Integer.compare(v[x], v[y]));
// idx is now a sorted index array (boxed; acceptable for one-off sorts)
```

---

## 3.10 Bit Helpers (the Java analogue of GCC built-ins)

```java
Integer.bitCount(x);          // popcount of an int  (= __builtin_popcount)
Long.bitCount(x);             // popcount of a long  (= __builtin_popcountll)
Integer.numberOfLeadingZeros(x);   // = __builtin_clz (returns 32 when x == 0)
Integer.numberOfTrailingZeros(x);  // = __builtin_ctz (returns 32 when x == 0)
Long.numberOfLeadingZeros(x);
Long.numberOfTrailingZeros(x);
// Parity: Integer.bitCount(x) & 1   (no direct builtin)

// floor(log2(x)) for x > 0:
int floorLog2 = 31 - Integer.numberOfLeadingZeros(x);     // int
int floorLog2L = 63 - Long.numberOfLeadingZeros(x);       // long

// GCD: no std::gcd; use a custom one or BigInteger.gcd.
static long gcd(long a, long b) { return b == 0 ? a : gcd(b, a % b); }
static long lcm(long a, long b) { return a / gcd(a, b) * b; }
// For very large values: BigInteger.valueOf(a).gcd(BigInteger.valueOf(b))
```

> Note the difference: Java's `numberOfLeadingZeros`/`numberOfTrailingZeros` are **defined** for input 0 (they return the bit width, 32 or 64), whereas C++ `__builtin_clz`/`__builtin_ctz` are *undefined* for 0. One fewer corner case to worry about.

---

## 3.11 String Tips

```java
String s = "hello";
s.length();                    // length
s.substring(start, end);       // [start, end) — note: end is EXCLUSIVE (unlike C++ pos,len)
s.substring(start);            // to the end
s.indexOf("ell");              // returns index or -1 (not string::npos)
String s2 = s + " world";      // append (creates a new String — see below)

// Strings are IMMUTABLE. Building char-by-char with += in a loop is O(N^2).
// Use a StringBuilder:
StringBuilder b = new StringBuilder();
for (char c : s.toCharArray()) b.append(c);
String result = b.toString();

// Number conversions
int    x = Integer.parseInt("42");
long   y = Long.parseLong("999999999999");
String t = Integer.toString(42);   // or String.valueOf(42)

// Character operations (Character helpers)
char c = 'A';
Character.isLowerCase(c); Character.isUpperCase(c);
Character.isDigit(c);     Character.isLetter(c);
Character.toLowerCase(c); Character.toUpperCase(c);
int digit = c - '0';                 // char '5' → int 5
char letter = (char) ('a' + i);      // int 0-25 → 'a'-'z' (cast required)

// Work on char[] for speed when you index heavily:
char[] cs = s.toCharArray();
```

> Two key differences from C++: (1) `substring(a, b)` uses an **exclusive** end index; (2) `String` is immutable, so concatenation in loops must go through `StringBuilder`.

---

## Summary Table

| Task | Best Tool |
|------|-----------|
| Sorted unique elements | `TreeSet<T>` |
| Frequency map | `HashMap<T,Integer>` (faster) or `TreeMap<T,Integer>` (sorted); `int[]` if keys dense |
| Min/Max element repeatedly | `PriorityQueue` (min-heap by default!) |
| Sorted order + nearest queries | `TreeSet` with `ceiling`/`floor`/`higher`/`lower` |
| Sliding window max/min | Monotonic `ArrayDeque` |
| Stack or queue | `ArrayDeque` (not `Stack`/`LinkedList`) |
| Coordinate compression | sort `int[]` + custom `lowerBound` |
| Multiset | `TreeMap<value, count>` |

---

**Next**: [04 — Number Theory](04_number_theory.md)
