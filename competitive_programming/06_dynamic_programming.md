# 06 — Dynamic Programming

DP = breaking a problem into overlapping subproblems and storing results to avoid recomputation.

> Java notes: use primitive arrays (`int[]`, `long[]`, `int[][]`) for DP tables — never boxed collections, which destroy performance. For memoised (top-down) DP, initialise the memo with a sentinel (e.g. `-1`) via `Arrays.fill` and watch recursion depth (big-stack thread if needed).

---

## 6.1 DP Framework

```
1. Define the state: dp[i], dp[i][j], dp[mask][i], ...
2. Write the recurrence (transition)
3. Identify base cases
4. Choose direction: top-down (memoisation) or bottom-up (tabulation)
5. Identify the answer
```

---

## 6.2 Classic DPs

### Longest Common Subsequence (LCS) — O(NM)

```java
static int lcs(String a, String b) {
    int n = a.length(), m = b.length();
    int[][] dp = new int[n + 1][m + 1];
    for (int i = 1; i <= n; ++i)
        for (int j = 1; j <= m; ++j)
            dp[i][j] = (a.charAt(i-1) == b.charAt(j-1))
                       ? dp[i-1][j-1] + 1
                       : Math.max(dp[i-1][j], dp[i][j-1]);
    return dp[n][m];
}
```

### Longest Increasing Subsequence (LIS) — O(N log N)

```java
static int lis(int[] a) {
    int[] tails = new int[a.length];   // tails[i] = smallest tail of IS of length i+1
    int len = 0;
    for (int x : a) {
        int pos = lowerBound(tails, len, x);   // first index in [0,len) with tails[i] >= x
        tails[pos] = x;
        if (pos == len) len++;
    }
    return len;
}
// lowerBound over tails[0..len)
static int lowerBound(int[] t, int len, int x) {
    int lo = 0, hi = len;
    while (lo < hi) { int mid = (lo + hi) >>> 1; if (t[mid] < x) lo = mid + 1; else hi = mid; }
    return lo;
}
// For non-strict (non-decreasing): use an upperBound (t[mid] <= x → lo = mid + 1) instead.
```

> There is no `Collections.lowerBound` in Java; the hand-written binary search above replaces C++ `lower_bound`.

### 0/1 Knapsack — O(N × W)

```java
static int knapsack(int W, int[] weight, int[] val) {
    int n = weight.length;
    int[] dp = new int[W + 1];
    for (int i = 0; i < n; ++i)
        for (int w = W; w >= weight[i]; --w)   // reverse to avoid using item twice
            dp[w] = Math.max(dp[w], dp[w - weight[i]] + val[i]);
    return dp[W];
}
```

### Unbounded Knapsack (each item unlimited times)

```java
static int unboundedKnapsack(int W, int[] weight, int[] val) {
    int n = weight.length;
    int[] dp = new int[W + 1];
    for (int w = 1; w <= W; ++w)
        for (int i = 0; i < n; ++i)
            if (w >= weight[i])
                dp[w] = Math.max(dp[w], dp[w - weight[i]] + val[i]);
    return dp[W];
}
```

### Coin Change — O(N × amount)

```java
static int coinChange(int[] coins, int amount) {
    int[] dp = new int[amount + 1];
    Arrays.fill(dp, Integer.MAX_VALUE);
    dp[0] = 0;
    for (int i = 1; i <= amount; ++i)
        for (int c : coins)
            if (c <= i && dp[i - c] != Integer.MAX_VALUE)
                dp[i] = Math.min(dp[i], dp[i - c] + 1);
    return dp[amount] == Integer.MAX_VALUE ? -1 : dp[amount];
}
```

---

## 6.3 Interval DP

Use for: merging intervals (matrix chain multiplication, balloon burst, stone merging).

```java
// Template: dp[l][r] = optimal cost for range [l, r]
static int intervalDP(int[] a) {
    int n = a.length;
    int[][] dp = new int[n][n];
    // len = length of interval
    for (int len = 2; len <= n; ++len) {
        for (int l = 0; l + len - 1 < n; ++l) {
            int r = l + len - 1;
            dp[l][r] = Integer.MAX_VALUE;
            for (int k = l; k < r; ++k)
                dp[l][r] = Math.min(dp[l][r], dp[l][k] + dp[k+1][r] + /*merge cost*/0);
        }
    }
    return dp[0][n-1];
}
```

---

## 6.4 Bitmask DP

Use for: small N (≤ 20), "visit all subsets", assignment problems, TSP.

```java
// Traveling Salesman Problem (TSP) — O(2^N × N²)
static int tsp(int[][] dist, int N) {
    int[][] dp = new int[1 << N][N];
    for (int[] row : dp) Arrays.fill(row, Integer.MAX_VALUE);
    dp[1][0] = 0;   // start at node 0, visited = {0}

    for (int mask = 1; mask < (1 << N); ++mask) {
        for (int u = 0; u < N; ++u) {
            if ((mask & (1 << u)) == 0) continue;
            if (dp[mask][u] == Integer.MAX_VALUE) continue;
            for (int v = 0; v < N; ++v) {
                if ((mask & (1 << v)) != 0) continue;
                int newMask = mask | (1 << v);
                dp[newMask][v] = Math.min(dp[newMask][v],
                                          dp[mask][u] + dist[u][v]);
            }
        }
    }
    int ans = Integer.MAX_VALUE;
    int fullMask = (1 << N) - 1;
    for (int u = 1; u < N; ++u)
        if (dp[fullMask][u] != Integer.MAX_VALUE)
            ans = Math.min(ans, dp[fullMask][u] + dist[u][0]);
    return ans;
}

// Enumerate all subsets of a mask
// for (int sub = mask; sub > 0; sub = (sub - 1) & mask) { /* process sub */ }

// Enumerate all masks with exactly k bits set:
// iterate all masks and check Integer.bitCount(m) == k
```

---

## 6.5 Tree DP

```java
// dp0[u] = best if u not selected
// dp1[u] = best if u selected
// Example: Maximum Independent Set on tree
static int[] dp0, dp1;
static void treeDp(int u, int parent, ArrayList<Integer>[] adj, int[] val) {
    dp0[u] = 0;
    dp1[u] = val[u];
    for (int v : adj[u]) {
        if (v == parent) continue;
        treeDp(v, u, adj, val);
        dp0[u] += Math.max(dp0[v], dp1[v]);   // u not selected: child can be either
        dp1[u] += dp0[v];                     // u selected: child must not be selected
    }
}
static int maxIndependentSet(int root) { return Math.max(dp0[root], dp1[root]); }
```

> Deep trees can blow the recursion stack — use a big-stack thread (01.10) or an explicit-stack post-order traversal.

---

## 6.6 Digit DP

Use for: count numbers in [L, R] satisfying some digit-based property.

```java
static char[] num;
static int[][][] memo;   // memo[pos][tight][started]

static int digitDP(int pos, int tight, int started) {
    if (pos == num.length) return started == 1 ? 1 : 0;
    if (memo[pos][tight][started] != -1) return memo[pos][tight][started];
    int limit = tight == 1 ? (num[pos] - '0') : 9;
    int result = 0;
    for (int d = 0; d <= limit; ++d) {
        int newTight   = (tight == 1 && d == limit) ? 1 : 0;
        int newStarted = (started == 1 || d > 0) ? 1 : 0;
        // Add your constraint check here:
        // e.g., if (started == 0 && d == 0) skip some conditions
        result += digitDP(pos + 1, newTight, newStarted);
    }
    return memo[pos][tight][started] = result;
}

// Count numbers in [1, N] satisfying condition:
// num = Long.toString(N).toCharArray();
// memo = new int[num.length][2][2];
// for (int[][] a : memo) for (int[] b : a) Arrays.fill(b, -1);
// int ans = digitDP(0, 1, 0);
```

> Java has no `bool` array indexing trick — we use `int` flags (0/1) so they can index the `memo` array directly, matching the C++ `memo[pos][tight][started]` layout.

---

## 6.7 DP Optimisations

### Prefix Sum Optimisation

```java
// If dp[i] = sum of dp[j] for all j < i satisfying some condition,
// maintain a running prefix sum instead of a nested loop. O(N²) → O(N).
long[] prefix = new long[n + 1];
for (int i = 1; i <= n; ++i) {
    dp[i] = prefix[i - 1];                  // sum of all valid previous dp values
    prefix[i] = prefix[i - 1] + dp[i];
}
```

### Sliding Window Optimisation (Deque)

```java
// dp[i] = min(dp[j] + cost) for j in [i-k, i-1]; monotonic deque gives min in O(1).
ArrayDeque<Integer> dq = new ArrayDeque<>();
for (int i = 1; i <= n; ++i) {
    while (!dq.isEmpty() && dq.peekFirst() < i - k) dq.pollFirst();
    dp[i] = dp[dq.peekFirst()] + cost[i];
    while (!dq.isEmpty() && dp[dq.peekLast()] >= dp[i]) dq.pollLast();
    dq.addLast(i);
}
```

### Space Optimisation (Rolling Array)

```java
// If dp[i] only depends on dp[i-1], keep two rows and toggle with i % 2.
int[][] dp = new int[2][m + 1];
for (int i = 1; i <= n; ++i) {
    int cur = i % 2, prev = 1 - cur;
    for (int j = 0; j <= m; ++j)
        dp[cur][j] = Math.max(dp[prev][j], /* ... */ 0);
}
```

---

## 6.8 Common DP Patterns

| Problem Type | State | Transition |
|-------------|-------|------------|
| Subsequence | dp[i] = best ending at i | dp[i] = f(dp[j]) for j < i |
| Substring | dp[i][j] = answer for [i,j] | dp[i][j] = f(dp[i+1][j], dp[i][j-1]) |
| Knapsack | dp[w] = best with weight ≤ w | dp[w] = max(dp[w], dp[w-wi]+vi) |
| Counting paths | dp[i][j] = # ways to reach (i,j) | dp[i][j] = dp[i-1][j] + dp[i][j-1] |
| Partition | dp[i] = best split for prefix i | dp[i] = min(dp[j] + cost(j,i)) |
| Bitmask | dp[mask][i] = cost visiting mask ending at i | TSP-style |

---

**Next**: [07 — String Algorithms](07_string_algorithms.md)
