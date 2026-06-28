# 15 — DP Optimizations

These techniques reduce O(N²) or O(N³) DP to O(N log N) or O(N). They appear in IOI and Codeforces Div 1 E.

> **Java caveat**: Java lacks `__int128`. Where C++ uses 128-bit intermediate
> products (e.g. the CHT "bad" test), Java must use `Math.multiplyHigh` for the
> high 64 bits or fall back to `BigInteger`. Recursive D&C DP can also hit the
> stack limit; run on a big-stack thread for large N.

---

## 15.1 Convex Hull Trick (CHT) — O(N log N) or O(N)

**When to use**: DP of the form  
`dp[i] = min over j<i of { dp[j] + b[j]*x[i] + a[i] }`  
where the lines have the form `y = b[j]*x + dp[j]`.

### Li Chao Tree (handles arbitrary x query order) — O(N log RANGE)

```java
static final class LiChaoTree {
    static final long POS_INF = (long) 4e18;   // line "at +infinity" => never chosen for min
    static final int LO = -1_000_000_000, HI = 1_000_000_000;

    // line m*x + b stored per node; children indices in l[]/r[] (-1 = absent)
    long[] m, b;
    int[] l, r;
    int sz = 0;

    LiChaoTree(int maxNodes) {
        m = new long[maxNodes]; b = new long[maxNodes];
        l = new int[maxNodes];  r = new int[maxNodes];
        newNode(0, POS_INF);    // root: the "infinity" line
    }
    private int newNode(long mm, long bb) {
        m[sz] = mm; b[sz] = bb; l[sz] = -1; r[sz] = -1; return sz++;
    }
    private long eval(int node, long x) { return m[node] * x + b[node]; }

    void addLine(int node, int lo, int hi, long nm, long nb) {
        int mid = lo + (hi - lo) / 2;
        boolean leftBetter = nm * lo + nb < eval(node, lo);
        boolean midBetter  = nm * mid + nb < eval(node, mid);
        if (midBetter) { long tm = m[node], tb = b[node]; m[node] = nm; b[node] = nb; nm = tm; nb = tb; }
        if (lo == hi) return;
        if (leftBetter != midBetter) {
            if (l[node] == -1) l[node] = newNode(0, POS_INF);
            addLine(l[node], lo, mid, nm, nb);
        } else {
            if (r[node] == -1) r[node] = newNode(0, POS_INF);
            addLine(r[node], mid + 1, hi, nm, nb);
        }
    }

    long query(int node, int lo, int hi, long x) {
        long res = eval(node, x);
        if (lo == hi) return res;
        int mid = lo + (hi - lo) / 2;
        if (x <= mid && l[node] != -1) return Math.min(res, query(l[node], lo, mid, x));
        if (x > mid  && r[node] != -1) return Math.min(res, query(r[node], mid + 1, hi, x));
        return res;
    }

    void add(long m, long b) { addLine(0, LO, HI, m, b); }
    long query(long x)       { return query(0, LO, HI, x); }
}
```

### Monotone CHT (when queries and slopes are both sorted) — O(N)

```java
static final class MonotoneStack {
    long[] ms = new long[1 << 20];   // slopes
    long[] bs = new long[1 << 20];   // intercepts
    int headIdx = 0, tail = 0;       // deque over [headIdx, tail)

    // l2 is useless if intersection(l1,l3) is below l2 at that point.
    // Uses Math.multiplyHigh to compare 128-bit products without overflow.
    private boolean bad(int i1, int i2, int i3) {
        // (b3-b1)*(m1-m2) <= (b2-b1)*(m1-m3)  as 128-bit signed
        return cmp128(bs[i3] - bs[i1], ms[i1] - ms[i2],
                      bs[i2] - bs[i1], ms[i1] - ms[i3]) <= 0;
    }
    // compare a*b vs c*d as 128-bit signed products: returns sign(a*b - c*d)
    private static int cmp128(long a, long b, long c, long d) {
        long hi1 = Math.multiplyHigh(a, b), lo1 = a * b;
        long hi2 = Math.multiplyHigh(c, d), lo2 = c * d;
        if (hi1 != hi2) return Long.compare(hi1, hi2);
        return Long.compareUnsigned(lo1, lo2);
    }

    void addLine(long m, long b) {       // slopes added in DECREASING order
        while (tail - headIdx >= 2) {
            ms[tail] = m; bs[tail] = b;   // tentatively place candidate at tail
            if (bad(tail - 2, tail - 1, tail)) tail--; else break;
        }
        ms[tail] = m; bs[tail] = b; tail++;
    }

    long query(long x) {                  // x queries in INCREASING order
        while (tail - headIdx >= 2 &&
               ms[headIdx] * x + bs[headIdx] >= ms[headIdx + 1] * x + bs[headIdx + 1])
            headIdx++;
        return ms[headIdx] * x + bs[headIdx];
    }
}
```

---

## 15.2 Divide and Conquer DP Optimization — O(N log N)

**When to use**: `dp[i][j] = min over k<j { dp[i-1][k] + C(k,j) }` where the optimal `k` is monotone: `opt(j) <= opt(j+1)`.

```java
// cost is a functional interface: long cost(int k, int j)
interface Cost { long apply(int k, int j); }

// Compute dp[i][lo..hi] using opt in [optLo..optHi]
static void solve(int i, int lo, int hi, int optLo, int optHi, long[][] dp, Cost cost) {
    if (lo > hi) return;
    int mid = (lo + hi) / 2;
    int bestK = optLo;
    long bestVal = Long.MAX_VALUE;
    for (int k = optLo; k <= Math.min(mid - 1, optHi); ++k) {
        long val = dp[i - 1][k] + cost.apply(k, mid);
        if (val < bestVal) { bestVal = val; bestK = k; }
    }
    dp[i][mid] = bestVal;
    solve(i, lo, mid - 1, optLo, bestK, dp, cost);
    solve(i, mid + 1, hi, bestK, optHi, dp, cost);
}
```

> Recursion depth is O(log N) here, so the stack is safe even for large N.

---

## 15.3 Knuth's Optimization — O(N³) → O(N²)

**When to use**: interval DP `dp[i][j] = min over i<=k<j { dp[i][k] + dp[k+1][j] + C(i,j) }` where C satisfies the **quadrangle inequality**: `C(a,c) + C(b,d) <= C(a,d) + C(b,c)` for a<=b<=c<=d.

```java
// opt[i][j] = optimal split point for interval [i,j]
// Constraint: opt[i][j-1] <= opt[i][j] <= opt[i+1][j]
final long INF = Long.MAX_VALUE / 2;

long[][] dp  = new long[n + 1][n + 1];
int[][]  opt = new int[n + 1][n + 1];
for (long[] row : dp) Arrays.fill(row, INF);

for (int i = 1; i <= n; ++i) { dp[i][i] = 0; opt[i][i] = i; }

for (int len = 2; len <= n; ++len) {
    for (int i = 1; i + len - 1 <= n; ++i) {
        int j = i + len - 1;
        dp[i][j] = INF;
        for (int k = opt[i][j - 1]; k <= Math.min(j - 1, opt[i + 1][j]); ++k) {
            long val = dp[i][k] + dp[k + 1][j] + cost(i, j);
            if (val < dp[i][j]) { dp[i][j] = val; opt[i][j] = k; }
        }
    }
}
```

---

## 15.4 Aliens Trick (WQS Binary Search / Lambda Optimization)

**When to use**: "find optimal value using exactly K items" where the cost-as-a-function-of-K is convex.

```java
// Instead of dp[i][k] (too many states), solve dpLambda[i] where
// each item costs an extra penalty lambda. Binary search on lambda
// to find the solution with exactly K items.
// usedCount returned via a 1-element array (Java has no out-params).

static long solve(double lambda, int n, int[] usedCount /* length 1 */) {
    // Standard DP but add lambda to cost of each item used.
    // Track count of items used in the optimal solution.
    usedCount[0] = 0;
    // return dp[n];
    return 0;
}

static long aliensDP(int K, int n) {
    double lo = -1e9, hi = 1e9;
    int[] cnt = new int[1];
    for (int iter = 0; iter < 200; ++iter) {   // binary search on lambda
        double mid = (lo + hi) / 2;
        solve(mid, n, cnt);
        if (cnt[0] >= K) lo = mid;
        else             hi = mid;
    }
    long ans = solve((lo + hi) / 2, n, cnt);
    return ans - (long) ((lo + hi) / 2) * K;   // adjust for the lambda penalty
}
```

---

## 15.5 Sum over Subsets (SOS) DP — O(N × 2^N)

```java
// dp[mask] = sum of a[sub] for all sub that are subsets of mask
// Build:
for (int i = 0; i < N; ++i)
    for (int mask = 0; mask < (1 << N); ++mask)
        if ((mask & (1 << i)) != 0)
            dp[mask] += dp[mask ^ (1 << i)];

// Sum over supersets (complement direction):
for (int i = 0; i < N; ++i)
    for (int mask = (1 << N) - 1; mask >= 0; --mask)
        if ((mask & (1 << i)) == 0)
            dp[mask] += dp[mask | (1 << i)];
```

---

## 15.6 Profile DP (Broken Profile DP)

**Use for**: tiling problems, counting perfect matchings, grid DP with a bitmask of one row as the state.

```java
// State: dp[mask] = ways to tile first (col) columns with mask describing
// which cells of column col+1 are already filled by pieces from col.
// Transition: try all ways to fill column col starting from mask state.
// O(N × 2^M) where M = number of rows.
// In Java, use long[] dp of size (1<<M) and reduce mod MOD as you go.
```

---

## 15.7 DP Optimization Selection Guide

```
Is the recurrence dp[i] = min(dp[j] + b[j]*x[i])?
  Slopes are monotone AND queries are monotone  → Monotone CHT, O(N)
  Otherwise                                     → Li Chao Tree, O(N log N)

Is it interval DP with monotone opt?
  Prove quadrangle inequality holds             → Knuth's O(N²)
  Prove opt[i][j-1] <= opt[i+1][j]             → D&C optimization O(N log N)

Is it "minimize cost using exactly K items"?
  Cost function is convex in K                  → Aliens trick O(N log RANGE)

Is it "sum over all subsets"?
  Bitmask of ≤ 20 elements                      → SOS DP O(N × 2^N)

Is the DP state an entire row/column of a grid?
  Tiling, matching on grid                      → Profile DP
```

---

## 15.8 Verifying CHT Applicability

```
dp[i] = min over j<i of { f(j) + g(i, j) }

Rewrite g(i,j) = slope(j) * query(i) + intercept(j)

If slope(j) is monotone              → Monotone CHT
If query(i) is also monotone         → Pure deque, O(N)
If only slope monotone, mixed query  → Sort queries + monotone deque
If neither monotone                  → Li Chao tree, O(N log N)
```

---

**Next**: [16 — Advanced Strings](16_advanced_strings.md)
