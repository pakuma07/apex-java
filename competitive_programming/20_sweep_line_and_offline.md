# 20 — Sweep Line and Offline Algorithms

Essential for geometry problems, interval problems, and offline query processing.
Heavily used in Codeforces Div 1 C/D and ICPC problems.

> **Java caveat**: Java has no `multiset` — model it as a `TreeMap<value,Integer>`
> count map, or use a `TreeSet` of packed/composite keys when elements are unique.
> `Arrays.sort` accepts a `Comparator` only for object arrays (`int[][]`, `long[][]`,
> records); a primitive `int[]` cannot be sorted with a comparator, so either pack
> the event into one `long` and sort ascending, or use a boxed/object representation.
> Several routines here recurse (merge sort, CDQ) — for `n` up to ~10^7 prefer the
> iterative bottom-up merge or run inside `new Thread(null, run, "main", 1 << 26).start()`.

---

## 20.1 Sweep Line Fundamentals

**Core idea**: Sort events by one dimension (usually x or time), process left to right
while maintaining a data structure for the other dimension.

**Event types:**
- **Point event**: process at a single coordinate
- **Interval event**: open at left endpoint, close at right endpoint

```java
// Generic sweep line template
record Event(int x, int type, int id) {}  // type: -1 = close, 0 = point, 1 = open
                                          // (process closes before opens)
Event[] events = /* ... */;
Arrays.sort(events, Comparator
        .comparingInt(Event::x)
        .thenComparingInt(Event::type));
```

---

## 20.2 Count Rectangles Containing a Point

Given N rectangles and Q query points, count how many rectangles contain each point.

```java
// Each rectangle [x1,x2] x [y1,y2], query point (px, py)
// Sweep x: at x1 add y-interval, at x2+1 remove y-interval
// At query x, count active y-intervals covering py using BIT
record Event(int x, int type, int y1, int y2, int idx) {}
ArrayList<Event> events = new ArrayList<>();
for (int i = 0; i < N; i++) {
    events.add(new Event(x1[i],     1, y1[i], y2[i], i));  // open
    events.add(new Event(x2[i] + 1, -1, y1[i], y2[i], i)); // close
}
for (int i = 0; i < Q; i++) {
    events.add(new Event(px[i], 0, py[i], py[i], i));      // query
}
events.sort(Comparator
        .comparingInt(Event::x)
        .thenComparingInt(Event::type));

BIT bit = new BIT(MAXY);
int[] ans = new int[Q];
for (Event e : events) {
    if (e.type() == 1)       { bit.upd(e.y1(), 1);  bit.upd(e.y2() + 1, -1); }  // add
    else if (e.type() == -1) { bit.upd(e.y1(), -1); bit.upd(e.y2() + 1, 1);  }  // remove
    else                     ans[e.idx()] = bit.qry(e.y1());                     // query prefix sum
}
```

---

## 20.3 Area of Union of Rectangles

Sweep x, maintain active y-intervals in a segment tree with lazy counting.

```java
// Segment tree that counts covered length of y-axis
static final class SegCover {
    int n;
    int[] cnt;   // how many times this node's range is fully covered
    int[] len;   // covered length

    SegCover(int n) { this.n = n; cnt = new int[2 * n + 4]; len = new int[2 * n + 4]; }

    void upd(int l, int r, int v, int node, int lo, int hi) {
        if (r < lo || hi < l) return;
        if (l <= lo && hi <= r) { cnt[node] += v; }
        else {
            int mid = (lo + hi) / 2;
            upd(l, r, v, 2 * node,     lo,      mid);
            upd(l, r, v, 2 * node + 1, mid + 1, hi);
        }
        if (cnt[node] != 0)   len[node] = hi - lo + 1;
        else if (lo == hi)    len[node] = 0;
        else                  len[node] = len[2 * node] + len[2 * node + 1];
    }
    void update(int l, int r, int v) { upd(l, r, v, 1, 0, n - 1); }
    int query() { return len[1]; }
}

// Main: coordinate compress y, sweep x
// At left edge of rectangle: update(y1, y2, +1)
// At right edge:             update(y1, y2, -1)
// Between events: area += covered_length * (next_x - cur_x)
```

---

## 20.4 Closest Pair of Points (Sweep Line)

O(N log N) algorithm using sweep line + sorted strip ordered by y-coordinate.

```java
import java.util.*;

static long dist2(long[] a, long[] b) {
    long dx = a[0] - b[0], dy = a[1] - b[1];
    return dx * dx + dy * dy;
}

// pts[i] = {x, y}
static double closestPair(long[][] pts) {
    int n = pts.length;
    // sort by x then y
    Arrays.sort(pts, (p, q) -> p[0] != q[0]
            ? Long.compare(p[0], q[0])
            : Long.compare(p[1], q[1]));
    double d = 1e18;  // current min distance
    // Strip contains points within x-distance d, ordered by (y, index).
    // Java has no multiset, but (y, index) pairs are unique, so a TreeSet of
    // packed keys works. Pack as long: (y << 21) | index, with index < 2^21.
    TreeSet<long[]> strip = new TreeSet<>((p, q) -> p[0] != q[0]
            ? Long.compare(p[0], q[0])   // by y
            : Long.compare(p[1], q[1])); // tie-break by index
    int left = 0;
    for (int i = 0; i < n; i++) {
        long D = (long) Math.ceil(d);
        // Remove points too far left
        while (left < i && pts[i][0] - pts[left][0] > D) {
            strip.remove(new long[]{pts[left][1], left});
            left++;
        }
        // Check candidates within y-strip of size 2d
        long lo = pts[i][1] - D, hi = pts[i][1] + D;
        for (long[] it : strip.tailSet(new long[]{lo, Long.MIN_VALUE})) {
            if (it[0] > hi) break;
            d = Math.min(d, Math.sqrt((double) dist2(pts[i], pts[(int) it[1]])));
        }
        strip.add(new long[]{pts[i][1], i});
    }
    return d;
}
```

> **Java caveat**: `strip.lower_bound(...)` maps to `TreeSet.tailSet(key)` (inclusive
> from `key`); iterate and `break` once the upper bound is exceeded. Storing each entry
> as a `long[]{y, index}` keeps elements unique so a plain `TreeSet` substitutes for
> the C++ `set<pair<...>>`.

---

## 20.5 Merge Sort Inversion Count

Count pairs (i, j) where i < j but a[i] > a[j]. Classic sweep / CDQ technique.

```java
static long mergeCount(int[] a, int l, int r) {
    if (r - l <= 1) return 0;
    int mid = (l + r) / 2;
    long cnt = mergeCount(a, l, mid) + mergeCount(a, mid, r);
    int[] tmp = new int[r - l];
    int i = l, j = mid, k = 0;
    while (i < mid && j < r) {
        if (a[i] <= a[j]) tmp[k++] = a[i++];
        else { cnt += mid - i; tmp[k++] = a[j++]; }
    }
    while (i < mid) tmp[k++] = a[i++];
    while (j < r)   tmp[k++] = a[j++];
    System.arraycopy(tmp, 0, a, l, r - l);
    return cnt;
}
// Call: long inv = mergeCount(a, 0, n);
```

> **Java caveat**: recursion depth is O(log n), so the default JVM stack is fine here.
> For very large `n` an iterative bottom-up merge avoids per-call `int[]` allocation —
> reuse one scratch buffer across all merges.

---

## 20.6 CDQ (Offline Divide and Conquer)

**Used by top CF competitors instead of segment trees with persistent structures.**
CDQ handles problems where updates and queries are offline and can be split.

**Classic problem**: Count points (x,y) in rectangle (0,0) to (qx,qy) after k insertions.
Offline: sort by x, split by time/index, count with BIT.

```java
// 3D dominance: count pairs (i,j) with i<j, a[j]<a[i], b[j]<b[i], c[j]<c[i]
// (or: each query asks "how many insertions before this query have a<=qA, b<=qB")
static final class Point {
    int a, b, c, type, idx;  // type: 0=update, 1=query
    Point(int a, int b, int c, int type, int idx) {
        this.a = a; this.b = b; this.c = c; this.type = type; this.idx = idx;
    }
}

static void cdq(Point[] pts, int l, int r, BIT bit, int[] ans) {
    if (r - l <= 1) return;
    int mid = (l + r) / 2;
    cdq(pts, l, mid, bit, ans);
    cdq(pts, mid, r, bit, ans);
    // Merge: left side updates, right side queries, sorted by b
    ArrayList<Point> leftUpd = new ArrayList<>();
    ArrayList<Point> rightQry = new ArrayList<>();
    for (int i = l;   i < mid; i++) if (pts[i].type == 0) leftUpd.add(pts[i]);
    for (int i = mid; i < r;   i++) if (pts[i].type == 1) rightQry.add(pts[i]);
    // Sort both by b
    leftUpd.sort(Comparator.comparingInt(p -> p.b));
    rightQry.sort(Comparator.comparingInt(p -> p.b));
    int j = 0;
    for (Point q : rightQry) {
        while (j < leftUpd.size() && leftUpd.get(j).b <= q.b) {
            bit.upd(leftUpd.get(j).c, 1); j++;
        }
        ans[q.idx] += bit.qry(q.c);
    }
    // Clean up BIT
    for (int k = 0; k < j; k++) bit.upd(leftUpd.get(k).c, -1);
    // Merge the two halves by a (already sorted by a due to recursion)
    inplaceMergeByA(pts, l, mid, r);
}

// Stable in-place-style merge of pts[l..mid) and pts[mid..r) by field a
static void inplaceMergeByA(Point[] pts, int l, int mid, int r) {
    Point[] tmp = new Point[r - l];
    int i = l, j = mid, k = 0;
    while (i < mid && j < r)
        tmp[k++] = (pts[i].a <= pts[j].a) ? pts[i++] : pts[j++];
    while (i < mid) tmp[k++] = pts[i++];
    while (j < r)   tmp[k++] = pts[j++];
    System.arraycopy(tmp, 0, pts, l, r - l);
}
```

> **Java caveat**: there is no `inplace_merge`; emulate it with a scratch array as above
> (still O(n) extra per level, O(n log n) total). Comparators must be over objects, so
> `Point` is a class rather than a primitive array here for readable `.b`/`.c` access.

**When to use CDQ:**
- Multi-dimensional dominance counting
- Point updates + range queries in offline setting
- Avoids 2D segment tree or persistent structures

---

## 20.7 Offline BFS / Kruskal Order Queries

**Pattern**: process edges sorted by weight; DSU connects components; answer "min edge on path u-v" queries as u and v merge.

```java
// For each query (u, v): find minimum weight edge on path u-v in MST
// Offline: sort edges by weight, sort queries, use Kruskal + small-to-large / LCT
// Simple version: binary search on sorted edges + DSU
record Query(int u, int v, int idx) {}
// Sort edges by weight ascending
// Binary search: for each query, find minimum weight w such that u,v are connected
// Use parallel binary search or offline sort

// Offline Kruskal approach:
// Build MST. For each query, LCA on Kruskal tree gives min edge on path.
// Kruskal tree: when merging components A and B with edge w,
//               create new node with value w, children = roots of A and B.
```

---

## 20.8 Offline Queries with Mo's Algorithm (covered in 13, extended here)

**Mo's on trees**: answer path queries offline.
Flatten tree with Euler tour, use parity trick for path queries.

```java
// Mo's on tree for path queries [u, v]:
// Use in/out times. If euler[u] > euler[v], swap.
// If lca(u,v) == u: query segment [in[u], in[v]]
// Else: query segment [out[u], in[v]], add lca separately

// Block size for Mo's: sqrt(n)
// Sort queries: primary = block of left endpoint, secondary = right endpoint
//   (alternate secondary direction for odd/even blocks for better constant)
final int BLOCK = (int) Math.sqrt(n);
Comparator<Query> cmp = (a, b) -> {
    int ba = a.l / BLOCK, bb = b.l / BLOCK;
    if (ba != bb) return Integer.compare(ba, bb);
    return (ba & 1) == 1 ? Integer.compare(b.r, a.r)   // Hilbert-curve-like order
                         : Integer.compare(a.r, b.r);
};
```

---

## 20.9 Sweep Line for Interval Problems

**Maximum number of overlapping intervals at any point:**

```java
// intervals[i] = {start, end}
ArrayList<int[]> events = new ArrayList<>();  // {x, delta}
for (int[] in : intervals) {
    events.add(new int[]{in[0],     1});   // start
    events.add(new int[]{in[1] + 1, -1});  // end (exclusive)
}
events.sort(Comparator
        .comparingInt((int[] e) -> e[0])
        .thenComparingInt(e -> e[1]));
int cur = 0, maxOverlap = 0;
for (int[] e : events) { cur += e[1]; maxOverlap = Math.max(maxOverlap, cur); }
```

**Minimum number of intervals to remove so no two overlap:**
= total intervals - maximum non-overlapping intervals (activity selection).

---

## 20.10 Coordinate Compression for Sweep Line

```java
// Compress y-coordinates before building segment tree for sweep
// rects[i] = {x1, y1, x2, y2}
int[] ys = new int[2 * rects.length];
int p = 0;
for (int[] r : rects) { ys[p++] = r[1]; ys[p++] = r[3] + 1; }
Arrays.sort(ys);
// Deduplicate in place -> M distinct values in ys[0..M)
int M = 0;
for (int i = 0; i < ys.length; i++)
    if (i == 0 || ys[i] != ys[i - 1]) ys[M++] = ys[i];

// getIdx = lower_bound over the distinct prefix ys[0..M)
java.util.function.IntUnaryOperator getIdx = y -> {
    int lo = 0, hi = M;            // search only the distinct prefix
    while (lo < hi) {
        int mid = (lo + hi) >>> 1;
        if (ys[mid] < y) lo = mid + 1; else hi = mid;
    }
    return lo;
};
// Now segment tree has size M, each unit represents a compressed y-coordinate
```

> **Java caveat**: there is no `unique`/`erase` idiom; compact distinct values into the
> array prefix yourself (loop above), and binary-search only that prefix `ys[0..M)`.
> `Arrays.binarySearch` is unsafe here because it returns an arbitrary match on
> duplicates — write an explicit `lower_bound` as shown.

---

## 20.11 Summary

| Problem | Technique | Complexity |
|---------|-----------|-----------|
| Count rectangles containing point | Sweep x + BIT on y | O((N+Q) log N) |
| Area of union of rectangles | Sweep x + segment tree (count coverage) | O(N log N) |
| Closest pair | Sweep x + sorted strip (TreeSet) | O(N log N) |
| Inversion count | Merge sort / BIT | O(N log N) |
| 3D dominance counting | CDQ D&C + BIT | O(N log^2 N) |
| Point updates + range queries offline | CDQ / offline BIT | O(N log N) |
| Max overlap of intervals | Sweep with events | O(N log N) |
| Path queries on trees offline | Mo's on trees | O((N+Q) sqrt N) |
| Min edge on path queries | Kruskal tree + LCA | O((N+Q) log N) |

Back to: [README](README.md)
