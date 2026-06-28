# 02 — Time and Space Complexity

Before writing a single line of code, verify your algorithm fits within the time and memory limits.

---

## 2.1 The Golden Rule

> **10^8 simple operations ≈ 1 second** on a typical online judge.

"Simple operation" = arithmetic, comparison, array access.  
Loops with heavy inner work (cache misses, method calls) may be 5–10× slower.

> **Java caveat**: the JVM is typically a constant factor (~1.5–2×) slower than C++ on tight numeric loops, and autoboxing/`HashMap` make this much worse. Budget conservatively: treat ~5×10^7 boxed-collection operations as your "1 second" if you cannot avoid boxing.

---

## 2.2 Constraint → Complexity Table

| N (constraint) | Max complexity | Notes |
|----------------|---------------|-------|
| N ≤ 10 | O(N! ) | Brute-force all permutations |
| N ≤ 15–20 | O(2^N · N) | Bitmask DP, subset enumeration |
| N ≤ 20 | O(2^N) | Meet-in-the-middle |
| N ≤ 100 | O(N³) | Floyd-Warshall, cubic DP |
| N ≤ 500 | O(N² log N) | — |
| N ≤ 1,000 | O(N²) | Bubble sort, quadratic DP |
| N ≤ 10,000 | O(N² / 64) | BitSet DP |
| N ≤ 100,000 | O(N √N) | Mo's algorithm, sqrt decomposition |
| N ≤ 200,000 | O(N log N) | Sort, Dijkstra, BIT, segment tree |
| N ≤ 1,000,000 | O(N) or O(N log N) | Sieve, two pointers, BFS/DFS |
| N ≤ 10^18 | O(log N) or O(√N) | Binary search, prime factorisation |

---

## 2.3 Big-O Cheat Sheet

```
O(1)       Constant   — array access, hash lookup (average)
O(log N)   Logarithmic— binary search, balanced BST operations
O(√N)      Square root— prime testing, sqrt decomposition
O(N)       Linear     — single pass, BFS/DFS
O(N log N) — sort, Dijkstra (with priority queue), segment tree build
O(N √N)    — Mo's algorithm
O(N²)      Quadratic  — naive O(N²) DP, bubble sort
O(N²logN)  — —
O(N³)      Cubic      — Floyd-Warshall, naive matrix multiply
O(2^N)     Exponential— bitmask over N elements
O(N!)      Factorial  — all permutations
```

---

## 2.4 Estimating Your Solution

```java
// Example: N = 200,000 elements, time limit = 2 seconds
// Your algorithm: O(N^2)
// Operations: 200000^2 = 4 × 10^10  →  ~400 seconds  ✗ TLE

// Alternative: O(N log N)
// Operations: 200000 × 17 ≈ 3.4 × 10^6  →  ~0.03 seconds  ✓ AC
```

**Mental math shortcut**:
```
log2(10^5)  ≈ 17
log2(10^6)  ≈ 20
log2(10^9)  ≈ 30
log2(10^18) ≈ 60
```

---

## 2.5 Memory Limits

Typical judge limit: **256 MB**

> **Java caveat**: object overhead is large. A `Integer` is ~16 bytes (vs 4 for `int`), and an `ArrayList<Integer>` of N elements can use ~20× the memory of an `int[N]`. Always prefer primitive arrays for large data. The JVM itself also reserves some heap, so leave headroom.

| Type | Size | Max count in 256 MB |
|------|------|---------------------|
| `int` (4 B) | 4 bytes | ~64 million |
| `long` (8 B) | 8 bytes | ~32 million |
| `double` (8 B) | 8 bytes | ~32 million |
| `int[N][N]` | 4 × N² bytes (+ row-array overhead) | N ≤ ~8,000 |
| `long[N][N]` | 8 × N² bytes (+ overhead) | N ≤ ~5,600 |
| `boolean[]` array | 1 byte each | ~256 million |
| `Integer` (boxed) | ~16 bytes each | ~16 million (avoid!) |
| `java.util.BitSet` | N/8 bytes | very efficient |

```java
// Danger: int[5000][5000]
// 5000 × 5000 × 4 bytes ≈ 100 MB  — borderline

// Safe: int[1000][1000]
// 1000 × 1000 × 4 bytes = 4 MB  — fine
```

---

## 2.6 Overflow: Know Your Limits

```java
// int: up to ~2.1 × 10^9  (2^31 - 1 = 2,147,483,647)
// long: up to ~9.2 × 10^18  (2^63 - 1)

// Common overflow situations:
int a = 100000, b = 100000;
int bad  = a * b;             // 10^10 overflows int! (silently wraps)
long good = (long) a * b;     // cast ONE operand to long before multiply

// Always use long when:
// - N * N > 2 × 10^9
// - Accumulating sums of large arrays
// - Path lengths or costs in graphs with large weights
// - Factorials, combinations without modular arithmetic

// Beyond 2^63 there is no __int128 in Java — use BigInteger:
import java.math.BigInteger;
BigInteger huge = BigInteger.valueOf((long) 1e18)
                            .multiply(BigInteger.valueOf((long) 1e18));
// BigInteger is exact but ~10-50× slower; use only when truly needed.
```

> Java `int`/`long` overflow wraps silently (two's complement) — there is no exception. The classic bug `(long) a * b` vs `a * b` is identical to C++: cast before multiplying.

---

## 2.7 Tight Time Limit Strategies

When your correct O(N log N) solution barely passes:

```java
// 1. Use a StringBuilder for output; avoid println in loops.
StringBuilder sb = new StringBuilder();
sb.append(ans).append('\n');

// 2. Preallocate ArrayList capacity if you must use one.
ArrayList<Integer> v = new ArrayList<>(n);

// 3. Use primitive arrays instead of collections in inner loops.
int[] a = new int[200005];     // far faster than ArrayList<Integer>

// 4. Avoid HashMap when keys are dense — use an array indexed by key.
//    HashMap<Integer,Integer> boxes every key AND value.
int[] count = new int[maxKey + 1];   // O(1), no boxing

// 5. Avoid recursion overhead for huge N — convert DFS to iterative,
//    or use a big-stack thread (see 01.10).

// 6. Use bitwise operations instead of division/modulo by powers of two.
x >> 1;   // x / 2
x & 1;    // x % 2
x << 1;   // x * 2

// 7. Sort primitives with Arrays.sort(int[]) (dual-pivot quicksort).
//    But beware: Arrays.sort(int[]) can be attacked into O(N^2) on
//    adversarial inputs — shuffle first, or box to Integer[] (merge sort,
//    guaranteed O(N log N)) if anti-quicksort hacks are a concern.
```

---

## 2.8 Space vs Time Trade-offs

```
Memoisation (top-down DP)  — trades memory for time (avoid recomputation)
Prefix sums                — O(N) preprocess, O(1) range query
Hashing                    — O(N) build, O(1) lookup (vs O(log N) TreeMap)
BitSet                     — 64× compression of boolean arrays
```

---

## 2.9 Recurrence Relations — Complexity Analysis

```
T(N) = 2T(N/2) + O(N)    →  O(N log N)   [merge sort]
T(N) = T(N-1) + O(1)     →  O(N)         [linear recursion]
T(N) = 2T(N/2) + O(1)    →  O(N)         [binary search variant]
T(N) = T(N-1) + O(N)     →  O(N²)        [selection sort recursion]
T(N) = T(√N) + O(1)      →  O(log log N) [rare]
```

---

## Summary

```
Step 1: Read constraints → pick the allowed complexity
Step 2: Think of an algorithm with that complexity
Step 3: Estimate operations: algorithm_complexity(N) < 10^8 ?  (be conservative on JVM)
Step 4: Check memory: total_memory < 256 MB ?  (avoid boxed collections for big data)
Step 5: Check overflow: any intermediate value > 2×10^9 → use long; > 2^63 → BigInteger
```

---

**Next**: [03 — Java Collections Tricks](03_stl_tricks.md)
