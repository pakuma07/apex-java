# 22 - Policy-Based Data Structures and Miscellaneous Tricks

Tricks used by top competitors that don't fit neatly elsewhere.
Covers the order-statistics-tree problem (GCC `__gnu_pbds` in C++), segment tree
beats, persistent segment tree, randomization tricks, and contest meta-skills.

> **Java caveat**: several structures here are recursion-heavy (persistent seg
> tree, implicit treap). Deep recursion can overflow the default ~512 KB JVM
> stack — run the solver inside a big-stack thread with
> `new Thread(null, run, "main", 1 << 26).start()`, or rewrite iteratively.
> Prefer primitive `int[]`/`long[]` arrays over boxed collections to avoid both
> MLE and constant-factor TLE.

---

## 22.1 Order-Statistics Tree (the PBDS problem)

In C++, the GCC extension `__gnu_pbds::tree` gives O(log N) order-statistics
operations: `order_of_key(x)` (how many elements are strictly less than `x`,
i.e. the rank) and `find_by_order(k)` (the element at 0-indexed position `k`,
i.e. select). It is available on Codeforces, AtCoder, and most online judges.

**Java has NO standard-library equivalent.** This is the single most important
honest caveat of this chapter:

- `TreeSet`/`TreeMap` give `floor`/`ceiling`/`first`/`last`/`headSet`/`tailSet`
  in O(log N), but they do **not** support indexed access. There is no
  `findByOrder(k)` analog.
- `TreeSet.headSet(x).size()` looks like `order_of_key`, but `size()` on the
  returned view is **O(N)** (it walks the subset), not O(log N). It is useless
  for rank queries at scale.
- The JDK has no `IndexedTreeSet` / rank-select balanced tree.

So **there is no stdlib policy-tree analog**. The idiomatic competitive-Java
approach is one of:

1. A **Fenwick tree (BIT) over compressed coordinates** for rank/select in
   O(log N) — the standard substitute when the set of possible values is known
   (offline, or compressible). Shown below in full.
2. A **hand-rolled order-statistics treap** (subtree sizes) when values arrive
   online and aren't easily compressible — the general dynamic answer. Shown
   below in full.
3. For **static** rank queries over a fixed array, a **merge-sort tree** or
   **wavelet tree** answers "how many values ≤ x in range [l,r]" in
   O(log^2 N) / O(log N); mentioned at the end of this section.

### Substitute 1: Fenwick tree over compressed values (rank + select)

`order_of_key` becomes a prefix-count query; `find_by_order` becomes
binary-lifting on the BIT. Both O(log N). This is the workhorse replacement.

```java
// Order-statistics over a multiset of int values, coordinate-compressed offline.
// rank(x)   = number of inserted elements strictly less than x   (order_of_key)
// select(k) = the (0-indexed) k-th smallest inserted element     (find_by_order)
static final class OrderStatBIT {
    final int n;          // number of distinct compressed coordinates
    final int LOG;        // floor(log2(n)) for binary lifting
    final long[] bit;     // 1-indexed Fenwick tree of counts
    final int[] vals;     // sorted distinct values; compressed index -> value

    OrderStatBIT(int[] allValues) {
        // Compress: sort + dedup the universe of values that may ever appear.
        int[] s = allValues.clone();
        java.util.Arrays.sort(s);
        int m = 0;
        for (int i = 0; i < s.length; i++)
            if (i == 0 || s[i] != s[i - 1]) s[m++] = s[i];
        vals = java.util.Arrays.copyOf(s, m);
        n = m;
        bit = new long[n + 1];
        int lg = 0;
        while ((1 << (lg + 1)) <= n) lg++;
        LOG = lg;
    }

    // compressed 1-based index of value x (x must exist in the universe)
    private int idx(int x) {
        int lo = 0, hi = n - 1, pos = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (vals[mid] < x) lo = mid + 1;
            else { pos = mid; hi = mid - 1; }   // first vals[mid] >= x
        }
        // pos is first index with vals[pos] >= x; for insert/erase x must equal vals[pos]
        return pos + 1; // to 1-based
    }

    private void add(int i, long delta) {            // point update, O(log n)
        for (; i <= n; i += i & (-i)) bit[i] += delta;
    }

    private long prefix(int i) {                     // count of first i coords, O(log n)
        long s = 0;
        for (; i > 0; i -= i & (-i)) s += bit[i];
        return s;
    }

    void insert(int x) { add(idx(x), 1); }
    void erase(int x)  { add(idx(x), -1); }          // assumes x present

    // order_of_key(x): number of inserted elements strictly less than x. O(log n)
    long rank(int x) {
        // count coords with value < x  ==  prefix up to (firstIndex(>=x) - 1)
        return prefix(idx(x) - 1);
    }

    // find_by_order(k): value of the 0-indexed k-th smallest. O(log n) via binary lifting.
    int select(int k) {
        k++;                       // convert to 1-based count target
        int pos = 0;
        long rem = k;
        for (int pw = 1 << LOG; pw > 0; pw >>= 1) {
            int next = pos + pw;
            if (next <= n && bit[next] < rem) {
                pos = next;
                rem -= bit[next];
            }
        }
        // pos is the largest prefix whose cumulative count < k, so pos+1 holds the k-th
        return vals[pos];          // pos is 0..n-1 here (pos+1 1-based -> vals[pos])
    }

    long size() { return prefix(n); }
}
```

**Classic use: count inversions online, k-th-smallest queries**

```java
// Count inversions: for each element a[i], count how many previous elements > a[i].
// Distinct values not required — the BIT naturally handles duplicates by count.
static long countInversions(int[] a) {
    OrderStatBIT os = new OrderStatBIT(a); // universe = the array itself
    long inv = 0;
    for (int x : a) {
        inv += os.size() - os.rank(x + 1); // elements >= x already inserted
        os.insert(x);                      // duplicates handled as multiplicities
    }
    return inv;
}
```

> Note on duplicates: the C++ PBDS `tree` is a *set* and needs the
> `pair<int,int>` (value, unique-id) trick to behave like a multiset. The
> Fenwick substitute above stores **counts**, so it is a multiset for free —
> `insert`/`erase` adjust the count and `rank`/`select` respect multiplicities.

### Substitute 2: order-statistics treap (online, no compression)

When values arrive online and you cannot enumerate the universe up front, use a
randomized balanced BST (treap) augmented with subtree sizes. `orderOfKey` and
`findByOrder` are O(log N) expected. This is the closest faithful analog to the
PBDS `tree`.

```java
// Order-statistics treap (set semantics; use a (value,id) key for multiset).
// insert/erase/orderOfKey/findByOrder all O(log N) expected.
static final class Treap {
    static final java.util.Random RNG = new java.util.Random(88172645463325252L);

    int[] key  = new int[1];
    long[] pri = new long[1];
    int[] left = new int[1];
    int[] right = new int[1];
    int[] size = new int[1]; // subtree size
    int cnt = 0;             // node count; node 0 is the null sentinel
    int root = 0;

    Treap(int capacity) {
        key = new int[capacity + 1];
        pri = new long[capacity + 1];
        left = new int[capacity + 1];
        right = new int[capacity + 1];
        size = new int[capacity + 1];
        // node 0 stays the null sentinel with size 0
    }

    private int newNode(int k) {
        int v = ++cnt;
        key[v] = k; pri[v] = RNG.nextLong();
        left[v] = right[v] = 0; size[v] = 1;
        return v;
    }

    private void pull(int v) { size[v] = 1 + size[left[v]] + size[right[v]]; }

    // split by key: left tree has keys < k, right tree has keys >= k
    private int[] split(int v, int k) {
        if (v == 0) return new int[]{0, 0};
        if (key[v] < k) {
            int[] p = split(right[v], k);
            right[v] = p[0]; pull(v);
            return new int[]{v, p[1]};
        } else {
            int[] p = split(left[v], k);
            left[v] = p[1]; pull(v);
            return new int[]{p[0], v};
        }
    }

    private int merge(int a, int b) {
        if (a == 0 || b == 0) return a | b;
        if (pri[a] > pri[b]) { right[a] = merge(right[a], b); pull(a); return a; }
        else                 { left[b]  = merge(a, left[b]);  pull(b); return b; }
    }

    void insert(int k) {
        int[] p = split(root, k);
        int[] q = split(p[1], k + 1); // q[0] holds existing copies of k (set: skip if present)
        int mid = (q[0] != 0) ? q[0] : newNode(k);
        root = merge(merge(p[0], mid), q[1]);
    }

    void erase(int k) {
        int[] p = split(root, k);
        int[] q = split(p[1], k + 1);
        // drop q[0] (the node equal to k) entirely
        root = merge(p[0], q[1]);
    }

    // order_of_key: number of elements strictly less than k. O(log N)
    int orderOfKey(int k) {
        int[] p = split(root, k);
        int res = size[p[0]];
        root = merge(p[0], p[1]); // restore
        return res;
    }

    // find_by_order: 0-indexed k-th smallest key. O(log N)
    int findByOrder(int k) {
        int v = root;
        while (v != 0) {
            int ls = size[left[v]];
            if (k < ls) v = left[v];
            else if (k == ls) return key[v];
            else { k -= ls + 1; v = right[v]; }
        }
        return -1; // out of range
    }

    int size() { return size[root]; }
}
```

> `split`/`merge` recurse to depth O(log N) expected, well within the JVM stack.
> For multiset semantics, store a packed `long` key `((long)value << 20) | id`
> with a unique `id`, exactly as the C++ `pair<int,int>` trick does.

### Substitute 3 (static): merge-sort tree / wavelet tree

For a **fixed array** with rank queries restricted to a range "how many values
≤ x in a[l..r]?", a **merge-sort tree** answers in O(log^2 N) per query
(O(N log N) memory) and a **wavelet tree** in O(log N). These are the static
analogs; for the dynamic global rank/select problem the Fenwick or treap above
is the right tool. (Merge-sort tree code appears in Chapter 13.)

There is also a hash-map note: C++'s `gp_hash_table<int,int>` (a fast PBDS open-
addressing map) has no special Java analog — just use `HashMap<Integer,Integer>`
(or a hand-rolled open-addressing `long[]`/`int[]` map for the constant factor).

---

## 22.2 Persistent Segment Tree

Supports historical queries: "what was the range sum/min at version t?"
Each update creates a new root, sharing unchanged nodes with previous versions.

```java
static final class PersistentSegTree {
    int[] left, right;
    long[] sum;
    int[] roots;   // roots[i] = root of version i
    int cnt = 0;   // node count; node 0 is the null sentinel

    PersistentSegTree(int maxNodes) {
        left = new int[maxNodes];
        right = new int[maxNodes];
        sum = new long[maxNodes];
        // node 0 is the implicit null/empty node (sum 0)
    }

    int newNode() { return ++cnt; }

    int build(int l, int r) {
        int v = newNode();
        if (l == r) return v;
        int m = (l + r) >>> 1;
        left[v]  = build(l, m);
        right[v] = build(m + 1, r);
        return v;
    }

    int update(int prev, int l, int r, int i, long val) {
        int v = newNode();
        left[v] = left[prev]; right[v] = right[prev]; sum[v] = sum[prev];
        if (l == r) { sum[v] = val; return v; }
        int m = (l + r) >>> 1;
        if (i <= m) left[v]  = update(left[prev],  l, m, i, val);
        else        right[v] = update(right[prev], m + 1, r, i, val);
        sum[v] = sum[left[v]] + sum[right[v]];
        return v;
    }

    long query(int v, int l, int r, int ql, int qr) {
        if (qr < l || r < ql) return 0;
        if (ql <= l && r <= qr) return sum[v];
        int m = (l + r) >>> 1;
        return query(left[v], l, m, ql, qr) +
               query(right[v], m + 1, r, ql, qr);
    }
}

// Usage: k-th smallest in range [l, r]
// Build persistent seg tree on sorted (compressed) values.
// At each index i, insert a[i] into the tree (new version).
// kthRange(roots[r], roots[l-1], 1, n, k): descend to position where
// (rightCnt - leftCnt) >= k accumulates.
```

**K-th smallest in range [l, r] using persistent segment tree:**

```java
// Build: insert elements left to right, each creating a new version.
// Query: kthRange(roots[r], roots[l-1], lo, hi, k) traverses both trees in parallel.
int kthRange(int vr, int vl, int l, int r, int k) {
    if (l == r) return l;  // coordinate-compressed value index
    int m = (l + r) >>> 1;
    int leftCnt = (int) (sum[left[vr]] - sum[left[vl]]);
    if (leftCnt >= k) return kthRange(left[vr],  left[vl],  l, m, k);
    else              return kthRange(right[vr], right[vl], m + 1, r, k - leftCnt);
}
```

---

## 22.3 Segment Tree Beats (Ji Driver Segmentation)

Supports "range chmin/chmax update" with O(N log^2 N) amortized.
Useful for: range set min, range set max, range sum queries.

```java
// Maintains for each node:
//   max1: first maximum, max2: second maximum, cntMax: count of max
//   min1: first minimum, min2: second minimum, cntMin: count of min
//   sum: range sum
static final class SegBeats {
    int n;
    long[] sum;
    int[] max1, max2, cntMax;
    int[] min1, min2, cntMin;
    long[] lazy;  // pending add

    SegBeats(int n, int[] a) {
        this.n = n;
        sum = new long[4 * n];
        max1 = new int[4 * n]; max2 = new int[4 * n]; cntMax = new int[4 * n];
        min1 = new int[4 * n]; min2 = new int[4 * n]; cntMin = new int[4 * n];
        lazy = new long[4 * n];
        build(a, 1, 1, n);
    }

    void build(int[] a, int v, int l, int r) { /* leaf init + pushUp */ }

    void pushUp(int v) {
        int L = 2 * v, R = 2 * v + 1;
        sum[v] = sum[L] + sum[R];
        // max
        if (max1[L] == max1[R]) {
            max1[v] = max1[L]; cntMax[v] = cntMax[L] + cntMax[R]; max2[v] = Math.max(max2[L], max2[R]);
        } else if (max1[L] > max1[R]) {
            max1[v] = max1[L]; cntMax[v] = cntMax[L]; max2[v] = Math.max(max2[L], max1[R]);
        } else {
            max1[v] = max1[R]; cntMax[v] = cntMax[R]; max2[v] = Math.max(max1[L], max2[R]);
        }
        // min (symmetric)
        if (min1[L] == min1[R]) {
            min1[v] = min1[L]; cntMin[v] = cntMin[L] + cntMin[R]; min2[v] = Math.min(min2[L], min2[R]);
        } else if (min1[L] < min1[R]) {
            min1[v] = min1[L]; cntMin[v] = cntMin[L]; min2[v] = Math.min(min2[L], min1[R]);
        } else {
            min1[v] = min1[R]; cntMin[v] = cntMin[R]; min2[v] = Math.min(min1[L], min2[R]);
        }
    }
    // ... (full implementation is ~150 lines; use a trusted library for contests)
    // Key insight: break condition when max1 <= newCap (chmin) -> only update max values, no recursion.
}
```

---

## 22.4 Randomization Tricks

### Randomized shuffle to avoid worst case

```java
// Always shuffle before sorting to prevent adversarial O(N^2) inputs against
// quicksort. Java's Arrays.sort(int[]) is a dual-pivot quicksort that CAN be
// hit by anti-quicksort tests — sort boxed Integer[] (TimSort, O(N log N) worst
// case) or shuffle primitives first, then sort.
java.util.Random rng = new java.util.Random(
        System.nanoTime() ^ System.identityHashCode(new Object()));
// Fisher-Yates shuffle on int[]:
for (int i = a.length - 1; i > 0; i--) {
    int j = rng.nextInt(i + 1);
    int t = a[i]; a[i] = a[j]; a[j] = t;
}
java.util.Arrays.sort(a);
```

### Random hashing for equality checking

```java
// Assign each element a random 64-bit value.
// Set of elements S has hash = XOR of hash[x] for x in S.
// Two sets are equal iff their XOR hashes match (with high probability).
java.util.Random rng64 = new java.util.Random(System.nanoTime());
java.util.HashMap<Integer, Long> elemHash = new java.util.HashMap<>();
// getHash(x): assign a stable random 64-bit tag on first use.
// Long.toUnsignedString / unsigned compares are not needed — XOR is bitwise.
java.util.function.IntUnaryOperator dummy = x -> x; // (illustration only)
// Long getHash(int x):
//     return elemHash.computeIfAbsent(x, k -> rng64.nextLong());
// Hash of set {a,b,c} = getHash(a) ^ getHash(b) ^ getHash(c)
// Very useful for: "do two multisets become equal after operations?"
```

### Freivalds' algorithm (matrix multiplication verification)

```java
// Verify A*B == C in O(N^2) instead of O(N^3).
// Random vector r; check A*(B*r) == C*r.
static boolean verify(int[][] A, int[][] B, int[][] C, int n, java.util.Random rng) {
    int[] r = new int[n];
    for (int i = 0; i < n; i++) r[i] = rng.nextInt() & 1;
    // Compute Br
    long[] Br = new long[n];
    for (int i = 0; i < n; i++)
        for (int j = 0; j < n; j++)
            Br[i] += (long) B[i][j] * r[j];
    // Compute A(Br)
    long[] ABr = new long[n];
    for (int i = 0; i < n; i++)
        for (int j = 0; j < n; j++)
            ABr[i] += (long) A[i][j] * Br[j];
    // Compute Cr
    long[] Cr = new long[n];
    for (int i = 0; i < n; i++)
        for (int j = 0; j < n; j++)
            Cr[i] += (long) C[i][j] * r[j];
    return java.util.Arrays.equals(ABr, Cr);
}
```

---

## 22.5 128-bit Arithmetic — BigInteger (a Java advantage)

C++ needs the non-portable `__int128` and even then can't print it directly.
**Java's `BigInteger` is built in, exact, and prints/parses natively** — no
custom read/print routine required. The trade-off is allocation overhead, so use
`long` whenever 64 bits suffice and reach for `BigInteger` only for the
overflow-prone intermediate.

```java
import java.math.BigInteger;

// "Print 128-bit value": BigInteger.toString() just works (no putchar loop).
BigInteger x = BigInteger.valueOf(1_000_000_007L).multiply(BigInteger.valueOf(1_000_000_009L));
System.out.println(x);                       // prints the full 128-bit-range value

// "Read 128-bit value": parse the token directly.
// BigInteger v = new BigInteger(token);

// Safe multiplication without overflow, mod m, using a 128-bit intermediary.
// C++ casts to __int128; Java uses Math.multiplyHigh or BigInteger.
static long mulmod(long a, long b, long m) {
    return BigInteger.valueOf(a)
            .multiply(BigInteger.valueOf(b))
            .mod(BigInteger.valueOf(m))
            .longValue();
}

// Faster, allocation-free alternative when a,b,m fit in 63 bits (Java 9+):
// uses the high 64 bits of the 128-bit product.
static long mulmodFast(long a, long b, long m) {
    long hi = Math.multiplyHigh(a, b);   // high 64 bits of a*b
    long lo = a * b;                     // low 64 bits (wraps, that's fine)
    return Math.floorMod(
        java.math.BigInteger.valueOf(hi).shiftLeft(64)
            .add(new java.math.BigInteger(Long.toUnsignedString(lo)))
            .mod(java.math.BigInteger.valueOf(m)).longValue(), m);
    // In practice, for repeated mulmod prefer Montgomery/Barrett reduction.
}
```

> Note: this is one of the few places Java is *more* convenient than C++ —
> arbitrary-precision integers are first-class, so 128-bit (and beyond)
> arithmetic, exact factorial products, and big-number hashing need no extra
> code.

---

## 22.6 Rope / Implicit Treap (for sequence operations)

Supports O(log N) split, merge, reverse, rotate on sequences. Java has **no
`rope` analog** (C++'s SGI `rope` / `crope`). For heavy in-place sequence edits
use a `StringBuilder` (O(N) inserts) for small cases, or the implicit treap
below (a balanced BST of positions) for O(log N) split/merge/reverse — that is
the idiomatic Java replacement.

```java
// Implicit treap: array-like structure with O(log N) split/merge.
// Node index = implicit key (position in sequence). Flat int[] arrays, no objects.
static final class ImplicitTreap {
    static final java.util.Random TREAP_RNG = new java.util.Random(42);

    int[] val, priority, size, left, right;
    boolean[] rev;     // lazy reverse flag
    int cnt = 0;       // node 0 is the null sentinel

    ImplicitTreap(int capacity) {
        val = new int[capacity + 1];
        priority = new int[capacity + 1];
        size = new int[capacity + 1];
        left = new int[capacity + 1];
        right = new int[capacity + 1];
        rev = new boolean[capacity + 1];
    }

    int newNode(int v) {
        int x = ++cnt;
        val[x] = v; priority[x] = TREAP_RNG.nextInt(); size[x] = 1;
        rev[x] = false; left[x] = right[x] = 0;
        return x;
    }

    void push(int v) {
        if (v == 0 || !rev[v]) return;
        int t = left[v]; left[v] = right[v]; right[v] = t; // swap children
        if (left[v]  != 0) rev[left[v]]  ^= true;
        if (right[v] != 0) rev[right[v]] ^= true;
        rev[v] = false;
    }

    int sz(int v) { return v != 0 ? size[v] : 0; }
    void upd(int v) { if (v != 0) size[v] = 1 + sz(left[v]) + sz(right[v]); }

    // Split into [0, pos-1] and [pos, n-1]; returns {leftRoot, rightRoot}.
    int[] split(int v, int pos) {
        if (v == 0) return new int[]{0, 0};
        push(v);
        int leftSz = sz(left[v]);
        if (leftSz >= pos) {
            int[] p = split(left[v], pos);
            left[v] = p[1]; upd(v);
            return new int[]{p[0], v};
        } else {
            int[] p = split(right[v], pos - leftSz - 1);
            right[v] = p[0]; upd(v);
            return new int[]{v, p[1]};
        }
    }

    int merge(int l, int r) {
        if (l == 0 || r == 0) return l | r;
        push(l); push(r);
        if (priority[l] > priority[r]) { right[l] = merge(right[l], r); upd(l); return l; }
        else                           { left[r]  = merge(l, left[r]);  upd(r); return r; }
    }

    // Reverse segment [l, r] (0-indexed); returns the new root.
    int reverseSeg(int root, int l, int r) {
        int[] a = split(root, l);          // a = {left, mid_right}
        int[] b = split(a[1], r - l + 1);  // b = {mid, right}
        rev[b[0]] ^= true;
        return merge(merge(a[0], b[0]), b[1]);
    }
}
```

---

## 22.7 Tricks from Top Competitors

### Tourist's tricks
```text
// Use "int" for n (array sizes, loop limits); cast to long for products: (long)a * b.
// Keep code short: factor recurring local logic into small static helpers.
// Use var for obvious local types (Java 10+); records for lightweight tuples.
// Java arrays are zero-initialized; prefer primitive arrays over ArrayList for
//   large allocations to dodge boxing (MLE/TLE).
```

### Errichto's tricks
```text
// Stress testing: generate random tests, compare brute force vs fast solution.
// Time limit: if N<=2000 and TL=2s, O(N^2) is fine; N<=300 for O(N^3)
//   (Java's constant factor is larger than C++; halve those bounds as a guide).
// "Think about the answer, not the algorithm": what does the optimal solution look like?
// Use long from the start if any multiplication is involved.
```

### neal's tricks (Codeforces)
```text
// Integer.numberOfTrailingZeros / numberOfLeadingZeros replace __builtin_ctz/clz.
// For segment trees: use iterative (bottom-up) instead of recursive for ~2x speedup.
// Compress coordinates before anything involving a range.
// If stuck: check if the problem is a known reduction (max flow, matching, SCC).
```

### Um_nik / jiangly tricks
```java
// For hashing: always use two independent hashes to reduce collision probability.
// Polynomial hash with mod1 and mod2 simultaneously.
final long MOD1 = 1_000_000_007L, MOD2 = 1_000_000_009L;
final long BASE1 = 131, BASE2 = 137;
// Hash = (h1, h2): collision probability ~ 1/(MOD1 * MOD2) ~ 10^-18.

// For graph problems: always draw small examples and find the pattern.
// Suspected greedy: prove by exchange argument or find a counterexample.
```

---

## 22.8 Iterative Segment Tree (Bottom-Up, 2x Faster)

```java
// Iterative segment tree: faster constant, simpler code.
// 1-indexed internally: leaves at [n, 2n-1].
static final class SegTreeFast {
    int n;
    long[] t;
    SegTreeFast(int n) { this.n = n; t = new long[2 * n]; }

    void update(int i, long val) {            // 0-indexed
        for (t[i += n] = val, i >>= 1; i >= 1; i >>= 1) t[i] = t[2 * i] + t[2 * i + 1];
    }

    long query(int l, int r) {                // 0-indexed, inclusive [l, r]
        long res = 0;
        for (l += n, r += n + 1; l < r; l >>= 1, r >>= 1) {
            if ((l & 1) != 0) res += t[l++];
            if ((r & 1) != 0) res += t[--r];
        }
        return res;
    }
}
```

---

## 22.9 Sqrt Trick: Divide Array into Blocks

**Block decomposition as an alternative when a specialized DS is hard to implement:**

```java
// Range sum with point updates in O(sqrt N).
static final class SqrtDecomp {
    int B;            // block size
    long[] block;     // per-block sums
    int[] a;

    SqrtDecomp(int n) {
        B = (int) Math.sqrt(n) + 1;
        block = new long[n / B + 1];
        a = new int[n];
    }

    void update(int i, int val) {
        block[i / B] += (long) val - a[i];
        a[i] = val;
    }

    long query(int l, int r) {
        long res = 0;
        int bl = l / B, br = r / B;
        if (bl == br) { for (int i = l; i <= r; i++) res += a[i]; return res; }
        for (int i = l; i < (bl + 1) * B; i++) res += a[i];
        for (int b = bl + 1; b < br; b++) res += block[b];
        for (int i = br * B; i <= r; i++) res += a[i];
        return res;
    }
}
```

---

## 22.10 Helpful Java Bit-Manipulation Built-ins

Java has no `__builtin_*` intrinsics; the equivalents live in `Integer`/`Long`
(the JIT lowers them to the same CPU instructions like `POPCNT`/`TZCNT`/`LZCNT`).

```java
Integer.bitCount(x)              // count set bits in int  (was __builtin_popcount)
Long.bitCount(x)                 // count set bits in long (was __builtin_popcountll)
Integer.numberOfTrailingZeros(x) // trailing zeros; returns 32 for x==0 (was __builtin_ctz)
Integer.numberOfLeadingZeros(x)  // leading zeros;  returns 32 for x==0 (was __builtin_clz)
Integer.bitCount(x) & 1          // parity of set bits (was __builtin_parity)
31 - Integer.numberOfLeadingZeros(x) // floor(log2(x)) for x > 0 (was __lg)

// Fast lowest set bit
int lsb = x & (-x);              // or x & -x

// Clear lowest set bit
x = x & (x - 1);                 // iterate over all set bits: while (x != 0) { use x; x &= x - 1; }

// Iterate over all subsets of mask (excluding empty):
for (int sub = mask; sub > 0; sub = (sub - 1) & mask) { /* use sub */ }
```

> Difference from GCC: `__builtin_ctz(0)`/`__builtin_clz(0)` are **undefined**,
> but `Integer.numberOfTrailingZeros(0)` and `numberOfLeadingZeros(0)` are
> **well-defined** and return 32 (64 for the `Long` variants). For bitsets,
> `long[]` plus `Long.bitCount`/`Long.numberOfTrailingZeros`, or
> `java.util.BitSet`, replace C++ `bitset`.

---

## 22.11 Summary: When to Use

| Tool | Use Case | Java reality |
|------|----------|--------------|
| Order-statistics tree (C++ PBDS) | Online k-th-smallest, inversion count, rank queries | **No stdlib analog** — use Fenwick-over-compressed (offline) or order-stat treap (online) |
| Fenwick over compressed values | Rank (`order_of_key`) + select (`find_by_order`), O(log N) | Standard CP substitute; multiset for free via counts |
| Order-statistics treap | Same, but online / no compression | Hand-rolled; closest faithful analog |
| Persistent seg tree | Historical queries, offline k-th-smallest in range | Flat `int[]`/`long[]` node pool |
| Segment tree beats | Range chmin/chmax with range sum queries | Same as C++ |
| Random XOR hash | Fast set equality / multiset comparison | `Random.nextLong()` tags |
| Freivalds | Verify matrix product in O(N^2) | Same as C++ |
| Implicit treap | Sequence with split/merge/reverse in O(log N) | Replaces C++ `rope` (no rope in Java) |
| Iterative seg tree | Drop-in replacement for recursive, ~2x faster | Same as C++ |
| Sqrt decomp | When a specialized DS is too hard to code under time pressure | Same as C++ |
| 128-bit arithmetic | Overflow-safe mulmod, big products | **`BigInteger` built in** (Java advantage); `Math.multiplyHigh` for speed |
| `gp_hash_table` | Fast hash map | `HashMap`, or hand-rolled open-addressing arrays |

---

**Back to**: [README](README.md)
