# 18 - Greedy and Constructive Algorithms

Used constantly by top Codeforces/AtCoder competitors. Greedy solves the
majority of Div 2 C/D and many Div 1 B/C problems. Constructive appears in
almost every round.

> **Java caveat**: products like `p[i] * w[j]` in greedy comparators overflow
> 32-bit `int`; cast to `long` first (Java `long` is 64-bit signed, the analogue
> of C++ `long long`). When intermediate products can exceed 64 bits, use
> `java.math.BigInteger` — a Java convenience over C++'s non-standard `__int128`.

---

## 18.1 The Exchange Argument (Greedy Proof Technique)

**How to prove a greedy ordering is optimal:**

Assume an optimal solution differs from the greedy solution in some adjacent
pair (A, B). Show that swapping them to match greedy order does not worsen the
answer. Therefore greedy is at least as good as optimal.

**Classic example: Job Scheduling (minimize weighted completion time)**

Jobs have processing time `p[i]` and weight `w[i]`. Optimal order:
sort by `p[i] / w[i]` ascending (equivalently `p[i] * w[j] < p[j] * w[i]`).

```java
// Sort jobs to minimize sum of (completion_time * weight)
// jobs[i] = {p, w}
Arrays.sort(jobs, (a, b) ->
    Long.compare((long) a[0] * b[1], (long) b[0] * a[1]));
```

**Exchange argument checklist:**
1. Define your ordering comparator (say `A before B` is better)
2. Show transitivity: `A<B` and `B<C` implies `A<C`
3. Show that any out-of-order adjacent swap worsens the answer
4. Conclude: any permutation can be sorted to greedy order without loss

---

## 18.2 Greedy with Priority Queues

**Pattern: always process the locally best option available.**

```java
// Classic: Task scheduler - assign tasks to machines, minimize makespan
// Sort tasks descending, assign to least loaded machine
int m = ...;                       // m machines
int[] tasks = {5, 3, 8, 2, 7};
Arrays.sort(tasks);                // ascending; iterate from the back for desc
PriorityQueue<Long> pq = new PriorityQueue<>();  // min-heap of machine loads
for (int i = 0; i < m; i++) pq.add(0L);
for (int i = tasks.length - 1; i >= 0; i--) {
    long load = pq.poll();
    pq.add(load + tasks[i]);
}
long makespan = 0;
while (!pq.isEmpty()) makespan = Math.max(makespan, pq.poll());
```

**Pattern: Scheduling with deadlines (maximize jobs completed)**

```java
// Each job has (deadline, profit). Maximize total profit.
// Greedy: sort by profit desc, use DSU to find latest free slot <= deadline
// jobs[i] = {deadline, profit}
Arrays.sort(jobs, (a, b) -> Integer.compare(b[1], a[1]));  // profit desc
int[] par = new int[maxD + 2];
for (int i = 0; i < par.length; i++) par[i] = i;
// iterative path-compression find (avoids deep recursion)
// find(x) = latest free slot <= x, or 0 if none
int profit = 0;
for (int[] job : jobs) {
    int slot = find(par, job[0]);
    if (slot > 0) { profit += job[1]; par[slot] = slot - 1; }
}
// ...
static int find(int[] par, int x) {
    while (par[x] != x) { par[x] = par[par[x]]; x = par[x]; }
    return x;
}
```

---

## 18.3 Interval Greedy Patterns

### Activity Selection (maximize non-overlapping intervals)
Sort by **right endpoint** ascending. Greedily pick intervals that don't overlap.

```java
// intervals[i] = {left, right}
Arrays.sort(intervals, (a, b) -> Integer.compare(a[1], b[1]));
int count = 0, last = Integer.MIN_VALUE;
for (int[] in : intervals) {
    if (in[0] >= last) { count++; last = in[1]; }
}
```

### Interval Cover (minimum intervals to cover [0, L])
Sort by left endpoint. Among all intervals starting <= current reach, pick the one extending furthest right.

```java
Arrays.sort(intervals, (a, b) ->
    a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));
int count = 0, reach = 0, best = 0, i = 0, n = intervals.length;
while (reach < L) {
    while (i < n && intervals[i][0] <= reach)
        best = Math.max(best, intervals[i++][1]);
    if (best == reach) { /* impossible */ break; }
    reach = best; count++;
}
```

### Merge Overlapping Intervals
Sort by left. Merge if current.left <= prev.right.

```java
Arrays.sort(intervals, (a, b) ->
    a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));
List<int[]> merged = new ArrayList<>();
for (int[] in : intervals) {
    if (!merged.isEmpty() && in[0] <= merged.get(merged.size() - 1)[1])
        merged.get(merged.size() - 1)[1] =
            Math.max(merged.get(merged.size() - 1)[1], in[1]);
    else
        merged.add(new int[]{in[0], in[1]});
}
```

---

## 18.4 Median Minimization

**Claim**: To minimize the sum of |x - a[i]|, set x = median(a[]).
**Claim**: To minimize the maximum |x - a[i]|, set x = (min + max) / 2.

```java
// Minimize sum of |x - a[i]| where x is integer
Arrays.sort(a);
int x = a[n / 2];  // median

// For two arrays: merge, take median
// For weighted: weighted median (point where cumulative weight >= total/2)
```

---

## 18.5 Greedy: Always Take Positive / Avoid Negative

**Subarray sum problems:**
- Max subarray sum: Kadane's algorithm (reset to 0 when prefix goes negative)

```java
long maxSub = 0, cur = 0;
for (int x : a) { cur = Math.max((long) x, cur + x); maxSub = Math.max(maxSub, cur); }
```

**Maximize sum with at most k sign flips:**
Sort absolute values descending. Flip k smallest negative (or if more flips needed
and all positive, flip smallest positive repeatedly).

```java
// Maximize sum of array by flipping exactly k elements' signs
Arrays.sort(a);
for (int i = 0; i < n && k > 0 && a[i] < 0; i++, k--) a[i] = -a[i];
if (k % 2 == 1) {  // odd flips remaining: flip smallest absolute value
    // a is int[]; box only to attach a comparator on |x|
    Integer[] b = Arrays.stream(a).boxed().toArray(Integer[]::new);
    Arrays.sort(b, (x, y) -> Integer.compare(Math.abs(x), Math.abs(y)));
    b[0] = -b[0];
    for (int i = 0; i < n; i++) a[i] = b[i];
}
```

---

## 18.6 Constructive: Common Patterns

### Pattern 1: Split into two groups with some property
Think: color with 2 colors, assign 0/1, place on left/right.

```java
// Make array non-decreasing by assigning each element to either A or B
// If a[i] >= last_A -> put in A, else if a[i] >= last_B -> put in B, else impossible
int lastA = Integer.MIN_VALUE, lastB = Integer.MIN_VALUE;
boolean ok = true;
for (int x : a) {
    if (x >= lastA) lastA = x;
    else if (x >= lastB) lastB = x;
    else { ok = false; break; }
}
```

### Pattern 2: Build from both ends (greedy two-pointer)
Place the largest unused element at left or right to construct a permutation.

```java
// Construct permutation 1..n such that |p[i]-p[i+1]| alternates large/small
// Tourist's trick: place n, 1, n-1, 2, n-2, 3 ...
int lo = 1, hi = n;
int[] res = new int[n];
boolean takeHi = true;
for (int i = 0; i < n; i++) {
    if (takeHi) res[i] = hi--;
    else        res[i] = lo++;
    takeHi = !takeHi;
}
```

### Pattern 3: Greedy construction with invariant
Maintain an invariant as you build the answer. Undo a decision if it breaks the invariant.

```java
// Build string with no two identical adjacent characters using given counts
// Greedy: always place the most frequent remaining character (not same as last)
// pq holds {count, char}, max-heap by count
StringBuilder ans = new StringBuilder();
PriorityQueue<int[]> pq = new PriorityQueue<>((x, y) -> Integer.compare(y[0], x[0]));
for (var e : charCount.entrySet()) pq.add(new int[]{e.getValue(), e.getKey()});
while (!pq.isEmpty()) {
    int[] top = pq.poll();           // {cnt1, c1}
    int cnt1 = top[0], c1 = top[1];
    if (ans.length() > 0 && ans.charAt(ans.length() - 1) == (char) c1) {
        if (pq.isEmpty()) { ans.setLength(0); break; }  // impossible
        int[] nxt = pq.poll();        // {cnt2, c2}
        int cnt2 = nxt[0], c2 = nxt[1];
        ans.append((char) c2);
        if (cnt2 - 1 > 0) pq.add(new int[]{cnt2 - 1, c2});
        pq.add(new int[]{cnt1, c1});
    } else {
        ans.append((char) c1);
        if (cnt1 - 1 > 0) pq.add(new int[]{cnt1 - 1, c1});
    }
}
```

### Pattern 4: Work backwards
If the forward greedy is hard, reverse the operations and apply greedy in reverse.

```java
// Example: repeatedly remove "ab" from string until none remain.
// How many operations? Work backwards: insert "ab"s.
// Or just simulate with a stack:
String s = "aababb";
StringBuilder stk = new StringBuilder();
int count = 0;
for (char c : s.toCharArray()) {
    if (c == 'b' && stk.length() > 0 && stk.charAt(stk.length() - 1) == 'a') {
        stk.deleteCharAt(stk.length() - 1); count++;
    } else stk.append(c);
}
```

---

## 18.7 Ternary Search (Unimodal Functions)

Find minimum/maximum of a **unimodal** function on [lo, hi].
f(x) is unimodal if it first decreases then increases (or vice versa).

```java
// Integer ternary search: minimize f(x) on [lo, hi]
// Converges in O(log_{1.5}(hi-lo)) ~ 100 iterations for 10^9 range
static long ternarySearch(long lo, long hi) {
    while (hi - lo > 2) {
        long m1 = lo + (hi - lo) / 3;
        long m2 = hi - (hi - lo) / 3;
        if (f(m1) < f(m2)) hi = m2;  // change to > for maximum
        else               lo = m1;
    }
    long best = lo;
    for (long x = lo + 1; x <= hi; x++)
        if (f(x) < f(best)) best = x;  // change to > for maximum
    return best;
}

// Floating point ternary search (200 iterations is enough)
static double ternaryF(double lo, double hi) {
    for (int iter = 0; iter < 200; iter++) {
        double m1 = lo + (hi - lo) / 3;
        double m2 = hi - (hi - lo) / 3;
        if (f(m1) < f(m2)) hi = m2;
        else               lo = m1;
    }
    return (lo + hi) / 2;
}
```

**Ternary search on 2D**: apply nested ternary searches when f(x,y) is unimodal in each coordinate for fixed other.

---

## 18.8 Parallel Binary Search

Run B binary searches simultaneously, reducing total DFS/query cost.

**Use case**: Q offline queries "what is the answer for query i?" where the
answer is monotone in some parameter and checking requires a data structure update.

```java
// Parallel binary search template
// For each query i, answer is in [lo[i], hi[i]]
// check(mid, i) = does query i have answer <= mid?
int Q = queries.length;
int[] lo = new int[Q], hi = new int[Q], ans = new int[Q];
Arrays.fill(hi, MAXVAL);
for (int iter = 0; iter < 30; iter++) {
    List<List<Integer>> buckets = new ArrayList<>();
    for (int v = 0; v <= MAXVAL; v++) buckets.add(new ArrayList<>());
    for (int i = 0; i < Q; i++) {
        if (lo[i] <= hi[i]) {
            int mid = (lo[i] + hi[i]) / 2;
            buckets.get(mid).add(i);
        }
    }
    // Process values 0..MAXVAL, accumulating state
    // state.reset();
    for (int v = 0; v <= MAXVAL; v++) {
        // state.add(v);  // e.g., insert edge v, process event v
        for (int i : buckets.get(v)) {
            if (/*state satisfies query i*/ true) hi[i] = v - 1;
            else                                  lo[i] = v + 1;
        }
    }
}
for (int i = 0; i < Q; i++) ans[i] = lo[i];
```

**Complexity**: O((N + Q) log N × cost_per_check)
**Classic use**: "minimum edge weight on path between u and v" for Q queries — binary search on sorted edges + DSU.

---

## 18.9 Fractional Programming (Binary Search on Ratio)

Maximize f(S) = sum(a[i] for i in S) / sum(b[i] for i in S).

**Key insight**: f(S) >= lambda iff sum(a[i] - lambda*b[i]) >= 0 for best S.

```java
// Maximize average: choose k elements to maximize sum(a) / sum(b)
// Binary search on answer lambda in [0, max_a/min_b]
// check(lambda): can we achieve ratio >= lambda?
//   greedily pick k elements with largest (a[i] - lambda * b[i])
static boolean check(double lam, double[] a, double[] b, int n, int k) {
    double[] diff = new double[n];
    for (int i = 0; i < n; i++) diff[i] = a[i] - lam * b[i];
    Arrays.sort(diff);                 // ascending; take the k largest at the end
    double s = 0;
    for (int i = n - 1; i >= n - k; i--) s += diff[i];
    return s >= 0;
}
// ...
double lo = 0, hi = 1e9;
for (int iter = 0; iter < 100; iter++) {
    double mid = (lo + hi) / 2;
    if (check(mid, a, b, n, k)) lo = mid;
    else                        hi = mid;
}
```

---

## 18.10 Common Greedy Decision Checklist

```
1. Sort by some key and process in order
   -> What key? Try: value, -value, ratio, deadline, size, finish time
2. Always pick the locally best option (priority queue)
   -> Prove it with exchange argument
3. Reduce to known greedy: interval scheduling, coin change, Huffman
4. If greedy fails, try DP
5. Constructive: think about what the answer looks like, then build it
6. Invariant: what property does your partial solution maintain?
7. Work backwards if forward is hard
8. Check: does it matter which tied elements you pick first?
```

---

## 18.11 Summary Table

| Problem Type | Greedy Strategy | Proof |
|-------------|----------------|-------|
| Maximize intervals selected | Sort by right endpoint | Exchange argument |
| Minimize intervals to cover | Sort by left, extend reach | Exchange argument |
| Job scheduling (minimize lateness) | Sort by deadline | Exchange argument |
| Huffman coding | Priority queue, merge two smallest | Optimal substructure |
| Maximize sum with k flips | Flip k most negative | Local optimality |
| Fractional knapsack | Sort by value/weight ratio | Exchange argument |
| Find minimum spanning tree | Kruskal / Prim | Cut property |
| Maximize average of k elements | Binary search + sort | Parametric search |
| Unimodal function min/max | Ternary search | Unimodality |
| Q binary searches with shared state | Parallel binary search | Amortization |

---

**Back to**: [README](README.md)
