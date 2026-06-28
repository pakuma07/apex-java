# 21 - Advanced Tree Techniques

Tree problems appear in nearly every Codeforces and AtCoder round.
This file covers techniques that top competitors use beyond basic DFS/LCA.

> **Java caveat (deep recursion):** This chapter is recursion-heavy (LCA, HLD,
> centroid decomposition, auxiliary trees, small-to-large, Euler tour). The
> default JVM thread stack (~512 KB) overflows around a few tens of thousands of
> nested frames, so a recursive DFS on a deep tree (e.g. a path of 200000 nodes)
> will throw `StackOverflowError`. Two fixes:
> 1. Run the solver on a thread with a large stack:
>    ```java
>    new Thread(null, Main::solve, "main", 1 << 26).start(); // 64 MB stack
>    ```
> 2. Convert the DFS to an explicit `ArrayDeque`/array stack.
>
> Every recursive routine below is kept recursive for clarity; in contest code,
> wrap it per option 1 or rewrite per option 2.

---

## 21.1 Euler Tour (DFS Order / Linearization)

**Flatten a tree into an array so subtree queries become range queries.**
Every subtree of node u corresponds to the range [in[u], out[u]] in the DFS order.

```java
static final int MAXN = 200005;
static int[] inTime = new int[MAXN], outTime = new int[MAXN], order = new int[MAXN];
static int timerVal = 0;

// adj[u] is the adjacency list of u
static void dfsEuler(int u, int p, List<Integer>[] adj) {
    inTime[u] = timerVal;
    order[timerVal++] = u;
    for (int v : adj[u]) {
        if (v != p) dfsEuler(v, u, adj);
    }
    outTime[u] = timerVal - 1;
}
// After DFS:
// - Subtree of u = array indices [inTime[u], outTime[u]]
// - Subtree sum = segTree.query(inTime[u], outTime[u])
// - Update subtree values = segTree.update(inTime[u], outTime[u], val)
```

**Heavy path linearization for HLD (see 13_advanced_data_structures.md)**:
Same Euler tour but visiting heavy child first.

**Euler tour for LCA (alternative to binary lifting):**
The LCA of u and v is the node with minimum depth in the Euler tour between
the first occurrence of u and the first occurrence of v.
Build sparse table on depths in Euler tour -> O(1) LCA queries.

```java
// Euler tour LCA: visits 2n-1 nodes (back to parent after each child)
static List<Integer> eulerLca = new ArrayList<>();   // 2n-1 entries
static List<Integer> depthLca = new ArrayList<>();
static int[] firstOcc = new int[MAXN];

static void dfsLcaEuler(int u, int p, int d, List<Integer>[] adj) {
    firstOcc[u] = eulerLca.size();
    eulerLca.add(u);
    depthLca.add(d);
    for (int v : adj[u]) {
        if (v != p) {
            dfsLcaEuler(v, u, d + 1, adj);
            eulerLca.add(u);
            depthLca.add(d);
        }
    }
}
// Build sparse table on depthLca
// lca(u, v) = eulerLca[argmin_{depthLca} in [firstOcc[u], firstOcc[v]]]
```

---

## 21.2 Rerooting DP (Tree DP with Rerooting)

**Problem**: Compute some value f(root=u) for every node u as root.
**Technique**: Two-pass DFS.
1. Root at node 1, compute f() for all subtrees downward.
2. Reroot: pass parent's contribution down, recompute f() for each node as root.

**Classic example: sum of distances from each node to all other nodes**

```java
// distSum[u] = sum of distances from u to all other nodes
// Step 1: count subtree sizes and downward dist sums
int[] sz = new int[n + 1];
Arrays.fill(sz, 1);
long[] down = new long[n + 1];   // sum of distances to nodes in subtree

// recursive lambdas are awkward in Java; use static helpers instead
static void dfs1(int u, int p, List<Integer>[] adj, int[] sz, long[] down) {
    for (int v : adj[u]) if (v != p) {
        dfs1(v, u, adj, sz, down);
        sz[u] += sz[v];
        down[u] += down[v] + sz[v];   // each node in subtree[v] is one step further
    }
}
dfs1(1, 0, adj, sz, down);

// Step 2: reroot - propagate answer from parent to child
long[] ans = new long[n + 1];
ans[1] = down[1];

static void dfs2(int u, int p, int n, List<Integer>[] adj, int[] sz, long[] ans) {
    for (int v : adj[u]) if (v != p) {
        // When we reroot to v:
        // - nodes in subtree[v]: distance decreases by 1 each -> -sz[v]
        // - nodes outside subtree[v]: distance increases by 1 each -> +(n-sz[v])
        ans[v] = ans[u] - sz[v] + (n - sz[v]);
        dfs2(v, u, n, adj, sz, ans);
    }
}
dfs2(1, 0, n, adj, sz, ans);
// ans[u] = sum of distances from u to all other nodes
```

**General rerooting template:**
```java
// f(u, parent) = some function of subtree
// When moving root from u to child v:
//   remove v's contribution from f(u), add u's contribution to f(v)
// The key: find inverse operation of "adding a child"
```

---

## 21.3 Functional Graphs

A graph where each node has **exactly one outgoing edge**. Consists of several
"rho" structures: a cycle with trees hanging off it.

**Applications**: permutation cycles, "follow the pointer" problems.

```java
// Find cycle length and distance to cycle for each node
int n;
int[] nxt = new int[n + 1];          // nxt[u] = single outgoing edge from u
boolean[] onCycle = new boolean[n + 1];
int[] cycleLen = new int[n + 1];
int[] distToCycle = new int[n + 1];
Arrays.fill(distToCycle, -1);

// Floyd's detection or coloring
int[] color = new int[n + 1];        // 0 = unvisited
for (int start = 1; start <= n; start++) {
    if (color[start] != 0) continue;
    // Trace the path from start
    List<Integer> path = new ArrayList<>();
    int cur = start;
    while (color[cur] == 0) {
        color[cur] = start;          // mark with current component id
        path.add(cur);
        cur = nxt[cur];
    }
    if (color[cur] == start) {
        // Found a new cycle: cur is on the cycle
        int len = 0;
        int tmp = cur;
        do { onCycle[tmp] = true; tmp = nxt[tmp]; len++; } while (tmp != cur);
        for (int node : path) {
            if (onCycle[node]) { cycleLen[node] = len; distToCycle[node] = 0; }
        }
    }
    // Compute distToCycle for tree nodes
    for (int i = path.size() - 1; i >= 0; i--) {
        int u = path.get(i);
        if (distToCycle[u] == -1)
            distToCycle[u] = distToCycle[nxt[u]] + 1;
    }
}
```

**K-th successor in functional graph** (jumping): use binary lifting.

```java
// k-th step from u in functional graph
// Binary lifting: up[u][j] = 2^j-th successor of u
static final int LOG = 60;           // enough for k up to ~2^60
int[][] up = new int[n + 1][LOG];
for (int i = 1; i <= n; i++) up[i][0] = nxt[i];
for (int j = 1; j < LOG; j++)
    for (int i = 1; i <= n; i++)
        up[i][j] = up[up[i][j - 1]][j - 1];

static int kthSuccessor(int u, long k, int[][] up) {
    for (int j = 0; j < LOG; j++)
        if (((k >> j) & 1) != 0) u = up[u][j];
    return u;
}
```

---

## 21.4 Virtual Tree (Auxiliary Tree)

Given a tree and a set of K key nodes, build a virtual tree containing only
the key nodes and their LCAs. Size O(K).

**Use case**: Problems where queries involve only a small subset K of nodes.
Reduces O(N) tree DP to O(K log N).

```java
// Steps:
// 1. Sort key nodes by DFS in-time
// 2. Add consecutive LCAs to the key set
// 3. Build virtual tree using a stack

// Requires: precomputed LCA (binary lifting) and inTime[]
static int[] buildVirtualTree(int[] keysIn) {
    Integer[] keys = Arrays.stream(keysIn).boxed().toArray(Integer[]::new);
    Arrays.sort(keys, (a, b) -> Integer.compare(inTime[a], inTime[b]));
    // deduplicate keys
    List<Integer> uniq = new ArrayList<>();
    for (int k : keys) if (uniq.isEmpty() || uniq.get(uniq.size() - 1) != k) uniq.add(k);
    int sz = uniq.size();

    // Add pairwise LCAs of adjacent keys in sorted order
    List<Integer> nodes = new ArrayList<>(uniq);
    for (int i = 0; i + 1 < sz; i++) nodes.add(lca(uniq.get(i), uniq.get(i + 1)));
    // Add LCA of the extreme keys (effective root of the virtual tree)
    nodes.add(lca(uniq.get(0), uniq.get(sz - 1)));

    // Sort by inTime, deduplicate
    nodes.sort((a, b) -> Integer.compare(inTime[a], inTime[b]));
    List<Integer> nodeList = new ArrayList<>();
    for (int x : nodes) if (nodeList.isEmpty() || nodeList.get(nodeList.size() - 1) != x) nodeList.add(x);

    // Build adjacency using a stack of ancestors
    // ... (use stack of ancestors to connect edges)
    return nodeList.stream().mapToInt(Integer::intValue).toArray();   // virtual tree nodes
}
```

---

## 21.5 Small-to-Large Merging (DSU on Tree / Heavy-Light Merging)

When merging two sets at a tree node, always iterate over the smaller set.
Each element is moved O(log N) times total -> O(N log N) overall.

```java
// Merge sets at tree nodes, maintaining some property
// Each node has a set; merge children's sets into parent's set
@SuppressWarnings("unchecked")
TreeSet<Integer>[] nodeSet = new TreeSet[n + 1];
for (int i = 0; i <= n; i++) nodeSet[i] = new TreeSet<>();
// Initialize: nodeSet[leaf] = {value[leaf]}

static void dfs(int u, int p, List<Integer>[] adj, TreeSet<Integer>[] nodeSet) {
    for (int v : adj[u]) if (v != p) {
        dfs(v, u, adj, nodeSet);
        // Merge smaller into larger
        if (nodeSet[u].size() < nodeSet[v].size()) {
            TreeSet<Integer> tmp = nodeSet[u]; nodeSet[u] = nodeSet[v]; nodeSet[v] = tmp;
        }
        nodeSet[u].addAll(nodeSet[v]);
    }
    // Now nodeSet[u] contains all values in subtree of u
    // Answer queries for u here
}
```

---

## 21.6 Centroid Decomposition (covered in 13, extended pattern)

**Pattern: solve path queries by decomposing at centroid**

```java
// centDist[u][v] = sum of edge weights on path u -> centroid -> v
// For all paths through centroid c:
//   answer(u, v) = f(u, c) combined with f(v, c)
// Store f(u, c) in a HashMap / sorted array at centroid c
// After processing, "delete" centroid and recurse on subtrees

// Template:
static boolean[] removed = new boolean[MAXN];
static int[] szCent = new int[MAXN];

static void getSize(int u, int p, List<Integer>[] adj) {
    szCent[u] = 1;
    for (int v : adj[u]) if (v != p && !removed[v]) {
        getSize(v, u, adj); szCent[u] += szCent[v];
    }
}
static int getCentroid(int u, int p, int treeSize, List<Integer>[] adj) {
    for (int v : adj[u]) if (v != p && !removed[v])
        if (szCent[v] > treeSize / 2) return getCentroid(v, u, treeSize, adj);
    return u;
}
static void decompose(int u, List<Integer>[] adj) {
    getSize(u, -1, adj);
    int c = getCentroid(u, -1, szCent[u], adj);
    removed[c] = true;
    // Process all paths through c
    // ...
    for (int v : adj[c]) if (!removed[v]) decompose(v, adj);
}
```

---

## 21.7 Tree Isomorphism

Check if two trees are identical (up to relabeling).
AHU (Aho-Hopcroft-Ullman) algorithm: O(N log N).

```java
// Canonical hash for rooted tree
// Hash of leaf = 0 (base case)
// Hash of node = hash of sorted multiset of children hashes

static Map<List<Integer>, Integer> hashTable = new HashMap<>();
static int hashCounter = 0;

static int treeHash(int u, int p, List<Integer>[] adj) {
    List<Integer> childHashes = new ArrayList<>();
    for (int v : adj[u]) if (v != p)
        childHashes.add(treeHash(v, u, adj));
    Collections.sort(childHashes);
    return hashTable.computeIfAbsent(childHashes, k -> hashCounter++);
}

// Two rooted trees T1, T2 (at roots r1, r2) are isomorphic iff
//   treeHash(r1, -1) == treeHash(r2, -1)

// For unrooted trees: try all possible roots (centroid(s)) as root
```

---

## 21.8 Link-Cut Tree (Dynamic Trees)

Maintains a forest under link/cut operations, supporting path queries in O(log N).
Complex to implement; used in advanced CF problems.

```java
// Simplified interface (use a trusted implementation):
class LCT {
    // path-parent pointer tree using splay trees
    // Operations: link(u,v), cut(u,v), findRoot(u), pathSum(u,v), pathMin(u,v)
    // All O(log N) amortized
}

// When to use Link-Cut Tree:
// - Dynamic tree with edge insertions/deletions AND path queries
// - Online minimum spanning forest with deletions
// - Network flow related dynamic problems

// If edges are only added (no deletions), use offline DSU instead.
// If queries are offline, use centroid decomp or virtual tree.
```

---

## 21.9 Tree DP Patterns

### Pattern 1: DP on subtrees with counting

```java
// dp[u][j] = number of ways to choose j nodes from subtree of u
// Merge: dp[u] = convolution of dp[u] with dp[v] for each child v
// Naive: O(N^2) but OK for most problems with N<=1000
// With knapsack trick: O(N^2) total (each pair considered once)
long[] dpu = new long[]{ 1 };   // dp[u], starts as {1}: choose u itself
for (int v : adj[u]) if (v != parent) {
    long[] dpv = dp[v];         // dp[v] computed by a prior DFS
    // dp[u] = dp[u] convolved with dp[v]
    long[] newDp = new long[dpu.length + dpv.length - 1];
    for (int j = 0; j < dpu.length; j++)
        for (int k = 0; k < dpv.length; k++)
            newDp[j + k] += dpu[j] * dpv[k];
    dpu = newDp;
}
```

### Pattern 2: Max independent set on tree

```java
// inc[u] = max independent set in subtree of u where u IS included
// exc[u] = max independent set in subtree of u where u is NOT included
long[] inc = new long[MAXN], exc = new long[MAXN];
static void dfs(int u, int p, List<Integer>[] adj, long[] val, long[] inc, long[] exc) {
    inc[u] = val[u];
    exc[u] = 0;
    for (int v : adj[u]) if (v != p) {
        dfs(v, u, adj, val, inc, exc);
        inc[u] += exc[v];                       // if u included, children must be excluded
        exc[u] += Math.max(inc[v], exc[v]);     // if u excluded, children can be either
    }
}
// Answer: Math.max(inc[root], exc[root])
```

### Pattern 3: Longest path in tree (diameter)

```java
static long diameter = 0;
static long dfsDiameter(int u, int p, List<Integer>[] adj) {
    long best1 = 0, best2 = 0;   // two longest downward paths
    for (int v : adj[u]) if (v != p) {
        long d = dfsDiameter(v, u, adj) + 1;
        if (d >= best1) { best2 = best1; best1 = d; }
        else if (d > best2) best2 = d;
    }
    diameter = Math.max(diameter, best1 + best2);
    return best1;
}
// Alternatively: BFS from any node, then BFS from farthest -> diameter
```

---

## 21.10 Summary

| Technique | Use Case | Complexity |
|-----------|----------|-----------|
| Euler Tour | Subtree queries with segment tree | O(N log N) per query |
| Rerooting DP | Compute f(root=u) for all u | O(N) |
| Functional Graph | Permutation cycles, k-th successor | O(N log N) build |
| Virtual Tree | Path queries on K key nodes | O(K log N) per query set |
| Small-to-Large | Merge sets at tree nodes | O(N log N) total |
| Centroid Decomp | Path queries, offline or online | O(N log^2 N) |
| Tree Isomorphism | Check structural equality | O(N log N) |
| Link-Cut Tree | Dynamic tree with path queries | O(log N) per op |
| Rerooting + DP | Sum of distances, answer for all roots | O(N) |
| Tree diameter | Longest path | O(N) BFS or O(N) DFS |

Back to: [README](README.md)
