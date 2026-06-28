# 11 — Tips, Tricks, and Contest Strategy

The difference between a good coder and a good competitive programmer is **contest discipline**.

---

## 11.1 Reading the Problem (Do This Every Time)

```
✓ Read the problem TWICE before writing any code
✓ Identify: input format, output format, constraints (N, M, T, value ranges)
✓ Check: is output a number? a string? a sequence? yes/no?
✓ Note: how many test cases? (T ≤ ?, per input or single?)
✓ Check time limit and memory limit
✓ Re-read the examples — understand them fully
✓ Look at the limits: what complexity is allowed?
✓ Note any special conditions ("distinct", "connected", "non-negative")
```

---

## 11.2 Contest Strategy

### Time Management

```
First pass (10 min): Read ALL problems, sort by perceived difficulty
Code easiest first — early AC builds momentum
Allocate time budgets per problem (e.g., 30/45/60 min)
If stuck for 20+ min → move to next problem, come back later
Never leave a solved problem un-submitted — partial credit counts
Last 10 min: Check if any penalty-free attempts remain
```

### Attempt Order

```
1. Problems you can solve immediately (A, B in Codeforces Div 2)
2. Problems you see a clear approach but need implementation
3. Problems you need to think about
4. Problems you have no idea about yet
```

---

## 11.3 Before You Submit

```
✓ Sample test cases all pass
✓ long used where values can exceed 2 × 10^9 (Java int is 32-bit, overflows silently)
✓ Array sizes are N+5 or larger (off-by-one safety)
✓ Modular arithmetic applied everywhere needed
✓ Output flushed (out.flush()) and NO per-line flush in loops (TLE risk)
✓ All debug output removed
✓ Reading input with BufferedReader, not Scanner (Scanner is 5–10× slower → TLE)
✓ Edge cases checked (see 11.4)
```

> **Java TLE caveat**: Java is typically 2–3× slower than C++ and the JVM has startup/JIT warm-up cost. Always use `BufferedReader`/`StreamTokenizer` for input and a single `StringBuilder` or `PrintWriter` (with one `flush()` at the end) for output. `System.out.println` in a loop and `Scanner` are the two most common causes of a Java TLE on a solution that is algorithmically correct.

---

## 11.4 Universal Edge Cases Checklist

```
N = 0, N = 1, N = 2 (smallest valid inputs)
N = MAX_N (largest input — check for TLE/MLE)
All elements equal
All elements 0 or negative
Sorted input (ascending and descending)
Answer is 0 or negative
Empty string, single character
Self-loops and multiple edges in graphs
Disconnected graph when problem implies connectivity
Graph with a single node
Overflow: multiply two values near Integer.MAX_VALUE
```

---

## 11.5 Debugging Tips

### Binary Search Your Bug

```
Work backward from wrong output to wrong state
Insert assert statements at intermediate steps (run with -ea):
  assert dist[src] == 0;
  assert answer >= 0 && answer <= N;
  assert dp[i] >= dp[i-1];   // if DP should be monotone
```

> Java assertions are **disabled by default** — enable them locally with `java -ea Solution`. Never rely on them for control flow in submissions.

### Stress Testing

```java
// Run your slow brute force vs fast solution on random inputs
import java.util.Random;

static int brute(int n) { /* O(N^2) correct solution */ return 0; }
static int fast(int n)  { /* O(N log N) solution being tested */ return 0; }

public static void main(String[] args) {
    Random rnd = new Random(42);
    for (int iter = 0; iter < 10000; ++iter) {
        int n = rnd.nextInt(20) + 1;
        // generate random input...
        if (brute(n) != fast(n)) {
            System.out.println("MISMATCH at n=" + n);
            return;
        }
    }
    System.out.println("All tests passed");
}
```

---

## 11.6 Common Mistakes

### Off-By-One Errors

```java
// Wrong: loop one too many or too few
for (int i = 1; i < n; ++i)   // misses i = n
for (int i = 0; i <= n; ++i)  // array out of bounds if size n

// Fix: always double-check loop bounds against array size
```

### Integer Overflow

```java
// Wrong
int a = 100000, b = 100000;
int c = a * b;           // 10^10 overflows int silently!

// Right
long c = (long) a * b;   // cast ONE operand before multiplying
```

### Uninitialized dp/dist Arrays

```java
// Java zero-initializes arrays automatically:
int[] dp = new int[1005];          // all zeros — no garbage (unlike C++ local arrays)

// For an "infinity" fill:
int[] dist = new int[1005];
java.util.Arrays.fill(dist, Integer.MAX_VALUE / 2);  // /2 avoids overflow when adding
// or use a sentinel like 0x3f3f3f3f:
java.util.Arrays.fill(dist, 0x3f3f3f3f);             // ~10^9, safe to add two of them
```

### Wrong Modular Subtraction

```java
// Wrong: can go negative (Java % can return a negative result)
long ans = (a - b) % MOD;

// Right: add MOD before taking %
long ans = ((a - b) % MOD + MOD) % MOD;
```

### Using `println` per line / Scanner

```java
// println flushes nothing per call, but Scanner + System.out in a loop is slow → TLE risk
Scanner sc = new Scanner(System.in);             // SLOW
for (int i = 0; i < n; ++i) System.out.println(i);

// Fix: BufferedReader in, StringBuilder/PrintWriter out
StringBuilder sb = new StringBuilder();
for (int i = 0; i < n; ++i) sb.append(i).append('\n');
System.out.print(sb);   // single write
```

---

## 11.7 Algorithm Selection Guide

```
Sum/Product queries on static array?  → Prefix sums / sparse table
Range update + range query?           → Segment tree with lazy propagation
Connectivity of dynamic edge set?     → DSU
Shortest path, positive weights?      → Dijkstra
Shortest path, negative weights?      → Bellman-Ford
All pairs?                            → Floyd-Warshall (small N)
Frequencies/existence queries?        → HashMap or counting sort
Pattern in string?                    → KMP or Z-function
Binary search on answer?              → When "is X achievable?" is easy to check
Greedy exchange argument?             → Sort + scan
DP state explosion?                   → Try bitmask, reduce dimension
```

---

## 11.8 Binary Search on the Answer

When: the answer is monotonic — if X is achievable, X-1 is too (or vice versa).

```java
// Is it possible to achieve score >= mid?
static boolean check(long mid) { /* problem-specific */ return true; }

long lo = 0, hi = (long) 1e9;
while (lo < hi) {
    long mid = lo + (hi - lo + 1) / 2;   // upper mid — avoids infinite loop
    if (check(mid)) lo = mid;
    else            hi = mid - 1;
}
out.println(lo);   // largest value where check is true

// For "smallest X where check is true":
while (lo < hi) {
    long mid = lo + (hi - lo) / 2;       // lower mid
    if (check(mid)) hi = mid;
    else            lo = mid + 1;
}
out.println(lo);
```

---

## 11.9 Coordinate Compression

When values are large but count is small (N ≤ 10^5, values ≤ 10^9):

```java
int[] vals = { /* raw values */ };
int[] sorted = vals.clone();
java.util.Arrays.sort(sorted);
// dedupe in place
int m = 0;
for (int i = 0; i < sorted.length; ++i)
    if (i == 0 || sorted[i] != sorted[i - 1]) sorted[m++] = sorted[i];
final int len = m;

// Compress: map value to index 0..K-1 (lower_bound == Arrays.binarySearch on unique array)
// Arrays.binarySearch returns the exact index for present values.
java.util.function.IntUnaryOperator compress =
    v -> java.util.Arrays.binarySearch(sorted, 0, len, v);
// Now use compressed indices in segment tree / BIT
```

For a general lower_bound (smallest index with `sorted[i] >= v`), write a manual binary search, since `Arrays.binarySearch` returns `-(insertionPoint)-1` for absent keys.

---

## 11.10 Two Pointers and Sliding Window

```java
// Two pointers — sorted array, find pair summing to target
int l = 0, r = n - 1;
while (l < r) {
    int s = a[l] + a[r];
    if      (s == target) { /* found */ l++; r--; }
    else if (s <  target) l++;
    else                  r--;
}

// Sliding window — longest subarray with sum ≤ K
int lo = 0, maxLen = 0;
long curSum = 0;
for (int hi = 0; hi < n; ++hi) {
    curSum += a[hi];
    while (curSum > K) curSum -= a[lo++];
    maxLen = Math.max(maxLen, hi - lo + 1);
}
```

---

## 11.11 Greedy Tips

```
Exchange argument: assume optimal differs from greedy, show swapping two adjacent elements
doesn't improve → greedy is optimal.

Sort by what you're optimising: deadlines, ratios, differences.

Common greedy patterns:
  - Interval scheduling: sort by end time, take earliest-ending non-overlapping
  - Fractional knapsack: sort by value/weight ratio
  - Huffman coding: always merge two smallest
  - Activity selection: sort by start or end time
```

---

## 11.12 Mental Model for Counting Problems

```
Permutations of n distinct: n!
Choose k from n (order doesn't matter): C(n,k) = n! / (k!(n-k)!)
Arrange n with repetitions (e.g. a of A, b of B): (a+b)! / (a! × b!)
Stars and bars (k non-negative integers summing to n): C(n+k-1, k-1)
Inclusion-exclusion: |A ∪ B| = |A| + |B| - |A ∩ B|
Derangements (no fixed point): D(n) = (n-1)(D(n-1) + D(n-2))
```

---

## Summary: The CP Mindset

```
1. Identify the problem type immediately
2. Verify complexity BEFORE coding
3. Write readable code — bugs cost more time than clean code saves
4. Test on samples, then on edge cases
5. If WA: think, don't randomly change code
6. If TLE: first verify algorithm is correct, then optimise (and check I/O is buffered!)
7. If MLE: check array sizes, prefer primitive arrays over boxed collections
8. Never give up on a problem you have partial insight into
```

---

**Next**: [12 — CP Template](12_cp_template.md)
