# 13 — Advanced Data Structures

These are the data structures that separate Div 1 D/E solvers from the rest.

> **Java caveat**: most of these are recursion-heavy or pointer-heavy. Deep DFS
> (HLD, centroid, treap) can overflow the default ~512 KB JVM stack — run inside a
> thread with `new Thread(null, run, "main", 1 << 26).start()` or convert to
> iteration. Prefer primitive `int[]`/`long[]` arrays over boxed `Integer`
> collections to avoid both MLE and constant-factor TLE.

---

## 13.1 Sparse Table — O(N log N) build, O(1) RMQ

**Use for**: static range minimum/maximum queries (no updates). The gold standard for RMQ.

```java
static final class SparseTable {
    int n, LOG;
    int[][] table;
    int[] log2_;

    void build(int[] a) {
        n = a.length; LOG = 1;
        while ((1 << LOG) <= n) LOG++;
        table = new int[LOG][n];
        log2_ = new int[n + 1];
        for (int i = 2; i <= n; ++i) log2_[i] = log2_[i / 2] + 1;

        table[0] = a.clone();
        for (int j = 1; j < LOG; ++j)
            for (int i = 0; i + (1 << j) <= n; ++i)
                table[j][i] = Math.min(table[j - 1][i], table[j - 1][i + (1 << (j - 1))]);
    }

    // Range minimum on [l, r] (0-indexed), O(1)
    int query(int l, int r) {
        int k = log2_[r - l + 1];
        return Math.min(table[k][l], table[k][r - (1 << k) + 1]);
    }
}

// For RMQ returning the INDEX (needed for LCA):
// Store {value, index} (e.g. as a packed long value<<20 | index) and take min by value
```

---

## 13.2 Segment Tree with Lazy Propagation

**Use for**: range update + range query (sum, min, max).

```java
static final class LazySegTree {
    int n;
    long[] tree, lazy;

    LazySegTree(int n) { this.n = n; tree = new long[4 * n]; lazy = new long[4 * n]; }

    void pushDown(int node, int l, int r) {
        if (lazy[node] != 0) {
            int mid = (l + r) >>> 1;
            // range sum: add lazy to each child, scaled by its segment length
            tree[2 * node]     += lazy[node] * (mid - l + 1); lazy[2 * node]     += lazy[node];
            tree[2 * node + 1] += lazy[node] * (r - mid);     lazy[2 * node + 1] += lazy[node];
            lazy[node] = 0;
        }
    }

    void update(int node, int l, int r, int ql, int qr, long val) {
        if (qr < l || r < ql) return;
        if (ql <= l && r <= qr) {
            tree[node] += val * (r - l + 1);  // range sum: add val to all
            lazy[node] += val;
            return;
        }
        pushDown(node, l, r);
        int mid = (l + r) >>> 1;
        update(2 * node, l, mid, ql, qr, val);
        update(2 * node + 1, mid + 1, r, ql, qr, val);
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    long query(int node, int l, int r, int ql, int qr) {
        if (qr < l || r < ql) return 0;
        if (ql <= l && r <= qr) return tree[node];
        pushDown(node, l, r);
        int mid = (l + r) >>> 1;
        return query(2 * node, l, mid, ql, qr) + query(2 * node + 1, mid + 1, r, ql, qr);
    }

    void update(int l, int r, long val) { update(1, 1, n, l, r, val); }
    long query(int l, int r)            { return query(1, 1, n, l, r); }
}

// For range assign (set all in [l,r] to val):
// lazy means "assigned value", use a sentinel (e.g. Long.MIN_VALUE) for "no pending assign"
// pushDown: if lazy[node] != sentinel, set children's tree and lazy
```

---

## 13.3 LCA — Lowest Common Ancestor (Binary Lifting)

**Use for**: LCA queries, path queries on trees, distance between nodes.  
Build: O(N log N). Query: O(log N).

```java
static final int MAXN = 100005, LOG = 17;
static int[][] parent = new int[MAXN][LOG];
static int[] depth = new int[MAXN];
static List<Integer>[] tree;   // tree = new List[MAXN]; fill with ArrayList

// Iterative DFS — avoids StackOverflow on deep trees.
static void buildLCA(int root, int n) {
    int[] stack = new int[n + 1]; int sp = 0;
    boolean[] vis = new boolean[n + 1];
    parent[root][0] = root; depth[root] = 0;
    stack[sp++] = root;
    while (sp > 0) {
        int u = stack[--sp];
        if (vis[u]) continue; vis[u] = true;
        for (int j = 1; j < LOG; ++j) parent[u][j] = parent[parent[u][j - 1]][j - 1];
        for (int v : tree[u]) if (!vis[v]) { parent[v][0] = u; depth[v] = depth[u] + 1; stack[sp++] = v; }
    }
}

static int lca(int u, int v) {
    if (depth[u] < depth[v]) { int t = u; u = v; v = t; }
    int diff = depth[u] - depth[v];
    for (int j = 0; j < LOG; ++j) if (((diff >> j) & 1) == 1) u = parent[u][j];
    if (u == v) return u;
    for (int j = LOG - 1; j >= 0; --j)
        if (parent[u][j] != parent[v][j]) { u = parent[u][j]; v = parent[v][j]; }
    return parent[u][0];
}

static int dist(int u, int v) { return depth[u] + depth[v] - 2 * depth[lca(u, v)]; }
```

> The original C++ uses a recursive DFS; in Java a recursive version risks
> `StackOverflowError` for N ≈ 10^5 on a path-shaped tree, so the iterative form
> above is preferred (or wrap a recursive build in a big-stack thread).

---

## 13.4 Heavy-Light Decomposition (HLD)

**Use for**: path queries/updates on trees — decompose any root-to-node path into O(log N) chains, then use a segment tree for each chain.

```java
static final int MAXN = 100005;
static int[] par = new int[MAXN], dep = new int[MAXN], sz = new int[MAXN], heavy = new int[MAXN];
static int[] head = new int[MAXN], pos = new int[MAXN];
static int curPos;
static List<Integer>[] adj;   // adj = new List[MAXN]; fill with ArrayList

static int dfsSz(int u, int p, int d) {
    par[u] = p; dep[u] = d; sz[u] = 1; heavy[u] = -1;
    int maxSz = 0;
    for (int v : adj[u]) if (v != p) {
        sz[u] += dfsSz(v, u, d + 1);
        if (sz[v] > maxSz) { maxSz = sz[v]; heavy[u] = v; }
    }
    return sz[u];
}

static void dfsHld(int u, int h) {
    head[u] = h; pos[u] = curPos++;
    if (heavy[u] != -1) dfsHld(heavy[u], h);
    for (int v : adj[u]) if (v != par[u] && v != heavy[u]) dfsHld(v, v);
}

static void buildHLD(int root) {
    curPos = 0;
    dfsSz(root, root, 0);
    dfsHld(root, root);
}

// Query path [u, v] — O(log^2 N) with segment tree
static long queryPath(int u, int v, SegTree seg) {
    long res = 0;
    while (head[u] != head[v]) {
        if (dep[head[u]] < dep[head[v]]) { int t = u; u = v; v = t; }
        res += seg.query(pos[head[u]], pos[u]);
        u = par[head[u]];
    }
    if (dep[u] > dep[v]) { int t = u; u = v; v = t; }
    res += seg.query(pos[u], pos[v]);
    return res;
}
```

> Both DFS passes recurse; run on a big-stack thread for N ≈ 10^5.

---

## 13.5 Centroid Decomposition

**Use for**: problems involving paths of a certain length, distance queries on trees.

```java
static int[] sz2 = new int[MAXN], centroidPar = new int[MAXN];
static boolean[] removed = new boolean[MAXN];

static int getSize(int u, int p) {
    sz2[u] = 1;
    for (int v : adj[u]) if (v != p && !removed[v]) sz2[u] += getSize(v, u);
    return sz2[u];
}

static int getCentroid(int u, int p, int treeSize) {
    for (int v : adj[u]) if (v != p && !removed[v])
        if (sz2[v] > treeSize / 2) return getCentroid(v, u, treeSize);
    return u;
}

static void decompose(int u, int p) {
    int s = getSize(u, -1);
    int c = getCentroid(u, -1, s);
    centroidPar[c] = p;
    removed[c] = true;
    // Process: for every node in current component, dist to centroid is computable
    for (int v : adj[c]) if (!removed[v]) decompose(v, c);
}
```

---

## 13.6 Mo's Algorithm — O((N + Q) √N)

**Use for**: offline range queries where adding/removing elements is O(1) or O(log N).

```java
static int BLOCK;

// Sort queries (each {l, r, idx}) by block of l, then by r (snake order)
static void sortQueries(int[][] queries) {
    Arrays.sort(queries, (a, b) -> {
        int ba = a[0] / BLOCK, bb = b[0] / BLOCK;
        if (ba != bb) return Integer.compare(ba, bb);
        return ((ba & 1) == 1) ? Integer.compare(b[1], a[1])   // odd block: r descending
                               : Integer.compare(a[1], b[1]);  // even block: r ascending
    });
}

// Current window state
static int[] cnt = new int[MAXN];
static int distinct = 0;
static void add(int x) { if (++cnt[x] == 1) ++distinct; }
static void rem(int x) { if (--cnt[x] == 0) --distinct; }

static int[] mo(int[][] queries, int[] a) {
    int q = queries.length;             // each query: {l, r, idx}
    BLOCK = Math.max(1, (int) Math.sqrt(a.length));
    sortQueries(queries);

    int[] ans = new int[q];
    int curL = 0, curR = -1;
    for (int[] qr : queries) {
        int l = qr[0], r = qr[1], idx = qr[2];
        while (curR < r) add(a[++curR]);
        while (curL > l) add(a[--curL]);
        while (curR > r) rem(a[curR--]);
        while (curL < l) rem(a[curL++]);
        ans[idx] = distinct;            // or whatever aggregate you need
    }
    return ans;
}
```

---

## 13.7 Treap (Implicit Key Treap)

**Use for**: sequences with insert/delete/split/merge in O(log N) — more flexible than segment tree.

```java
static final SplittableRandom rng = new SplittableRandom(42);

static final class Node {
    int val, pri, sz;
    Node l, r;
    Node(int v) { val = v; pri = rng.nextInt(); sz = 1; }
}

static int sz(Node t) { return t == null ? 0 : t.sz; }
static void upd(Node t) { if (t != null) t.sz = 1 + sz(t.l) + sz(t.r); }

// Split into left (size k) and right; returns {left, right}
static Node[] split(Node t, int k) {
    if (t == null) return new Node[]{null, null};
    if (sz(t.l) >= k) {
        Node[] lr = split(t.l, k);
        t.l = lr[1]; upd(t);
        return new Node[]{lr[0], t};
    } else {
        Node[] lr = split(t.r, k - sz(t.l) - 1);
        t.r = lr[0]; upd(t);
        return new Node[]{t, lr[1]};
    }
}

static Node merge(Node l, Node r) {
    if (l == null) return r;
    if (r == null) return l;
    if (l.pri > r.pri) { l.r = merge(l.r, r); upd(l); return l; }
    else               { r.l = merge(l, r.l); upd(r); return r; }
}

// Insert at position k (0-indexed)
static Node insert(Node t, int k, int val) {
    Node[] lr = split(t, k);
    return merge(merge(lr[0], new Node(val)), lr[1]);
}

// Erase position k
static Node erase(Node t, int k) {
    Node[] lr = split(t, k);
    Node[] mr = split(lr[1], 1);   // mr[0] is the single node to drop (GC handles it)
    return merge(lr[0], mr[1]);
}
```

> Java has garbage collection, so there is no `delete` — dropping the reference is
> enough. The original C++ `delete m;` simply becomes "stop referencing it".

---

## 13.8 DSU on Tree (Small-to-Large Merging)

**Use for**: subtree queries — count distinct values, most frequent element, etc.

```java
// Each node has a set of values. Merge children into parent.
// Total complexity: O(N log N) due to small-to-large invariant.

static HashSet<Integer> dsuSolve(int u, int p, int[] val, List<Integer>[] adj, int[] ans) {
    HashSet<Integer> cur = new HashSet<>();
    cur.add(val[u]);
    for (int v : adj[u]) {
        if (v == p) continue;
        HashSet<Integer> child = dsuSolve(v, u, val, adj, ans);
        if (child.size() > cur.size()) { HashSet<Integer> t = cur; cur = child; child = t; } // small-to-large
        cur.addAll(child);          // merge smaller into larger
    }
    ans[u] = cur.size();            // distinct values in subtree of u
    return cur;
}
```

> The `swap` of the two sets is what guarantees the small-to-large bound: always
> keep the larger set as `cur` and copy the smaller one into it.

---

## 13.9 Sqrt Decomposition — O(√N) per query/update

```java
// Block decomposition for range sum
static final class SqrtDecomp {
    int n, B;
    long[] a, block;

    SqrtDecomp(int[] arr) {
        n = arr.length;
        B = (int) Math.sqrt(n) + 1;
        a = new long[n];
        for (int i = 0; i < n; i++) a[i] = arr[i];
        block = new long[n / B + 1];
        for (int i = 0; i < n; ++i) block[i / B] += a[i];
    }

    void update(int i, long val) {
        block[i / B] += val - a[i];
        a[i] = val;
    }

    long query(int l, int r) {
        long res = 0;
        int bl = l / B, br = r / B;
        if (bl == br) { for (int i = l; i <= r; ++i) res += a[i]; return res; }
        for (int i = l; i < (bl + 1) * B; ++i) res += a[i];
        for (int b = bl + 1; b < br; ++b) res += block[b];
        for (int i = br * B; i <= r; ++i) res += a[i];
        return res;
    }
}
```

---

## 13.10 Summary

| Structure | Build | Query | Update | Best Use Case |
|-----------|-------|-------|--------|--------------|
| Sparse Table | O(N log N) | O(1) | — | Static RMQ |
| Segment Tree | O(N) | O(log N) | O(log N) | Dynamic range queries |
| Lazy Seg Tree | O(N) | O(log N) | O(log N) | Range update + range query |
| Fenwick Tree | O(N) | O(log N) | O(log N) | Prefix sums |
| LCA (binary lifting) | O(N log N) | O(log N) | — | Tree path queries |
| HLD | O(N log N) | O(log² N) | O(log² N) | Path queries on trees |
| Centroid Decomp | O(N log N) | O(log N) | — | Distance-based tree problems |
| Mo's Algorithm | O(N√N) | O(√N) amort. | — | Offline range queries |
| Treap | O(log N) | O(log N) | O(log N) | Flexible sequence ops |
| Sqrt Decomp | O(N) | O(√N) | O(1) | When log is too slow to code |

---

**Next**: [14 — Flows and Matching](14_flows_and_matching.md)
