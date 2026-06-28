# 14 — Flows, Matching, and 2-SAT

These topics appear in Codeforces Div 1 C/D and IOI. Master Dinic's and bipartite matching cold.

> **Java caveat**: Dinic's `dfs` and the 2-SAT SCC passes are recursive. For large
> graphs (V ≈ 10^5+) run inside a big-stack thread or convert to an iterative
> stack to avoid `StackOverflowError`. Use primitive arrays for the residual graph
> to keep the constant factor low.

---

## 14.1 Maximum Flow — Dinic's Algorithm

**Complexity**: O(V² × E) general; O(E √V) for unit-capacity graphs (bipartite matching).

This Java version uses a flat edge list (`int[] to`, `long[] cap`, `int[] next`, `int[] head`)
instead of a `vector<vector<Edge>>` — faster and easier on the GC. The reverse edge of `e`
is `e ^ 1`.

```java
static final class Dinic {
    int n;
    int[] eTo, eNext, head, level, iter;
    long[] eCap;
    int ecnt = 0;

    Dinic(int n, int maxEdges) {
        this.n = n;
        head = new int[n]; Arrays.fill(head, -1);
        eTo = new int[2 * maxEdges]; eCap = new long[2 * maxEdges]; eNext = new int[2 * maxEdges];
        level = new int[n]; iter = new int[n];
    }

    void addEdge(int from, int to, long cap) {
        eTo[ecnt] = to;   eCap[ecnt] = cap; eNext[ecnt] = head[from]; head[from] = ecnt++;
        eTo[ecnt] = from; eCap[ecnt] = 0;   eNext[ecnt] = head[to];   head[to]   = ecnt++;
    }

    boolean bfs(int s, int t) {
        Arrays.fill(level, -1);
        ArrayDeque<Integer> q = new ArrayDeque<>();
        level[s] = 0; q.add(s);
        while (!q.isEmpty()) {
            int v = q.poll();
            for (int e = head[v]; e != -1; e = eNext[e])
                if (eCap[e] > 0 && level[eTo[e]] < 0) { level[eTo[e]] = level[v] + 1; q.add(eTo[e]); }
        }
        return level[t] >= 0;
    }

    long dfs(int v, int t, long f) {
        if (v == t) return f;
        for (; iter[v] != -1; iter[v] = eNext[iter[v]]) {
            int e = iter[v], u = eTo[e];
            if (eCap[e] > 0 && level[v] < level[u]) {
                long d = dfs(u, t, Math.min(f, eCap[e]));
                if (d > 0) { eCap[e] -= d; eCap[e ^ 1] += d; return d; }
            }
        }
        return 0;
    }

    long maxflow(int s, int t) {
        long flow = 0;
        while (bfs(s, t)) {
            for (int i = 0; i < n; i++) iter[i] = head[i];
            long d;
            while ((d = dfs(s, t, Long.MAX_VALUE)) > 0) flow += d;
        }
        return flow;
    }
}

// Usage:
// Dinic dinic = new Dinic(N + 2, E);   // N nodes + source + sink, E forward edges
// dinic.addEdge(s, v, cap);
// long ans = dinic.maxflow(s, t);
```

---

## 14.2 Min Cut from Max Flow

By **Max-Flow Min-Cut Theorem**: max flow = min cut.

```java
// After maxflow, find the min cut:
// S-side: all nodes reachable from source in the residual graph
// T-side: everything else
// Cut edges: edges from S-side to T-side with 0 residual capacity

static boolean[] minCutSide(Dinic dinic, int s) {
    int n = dinic.n;
    boolean[] visited = new boolean[n];
    ArrayDeque<Integer> q = new ArrayDeque<>();
    visited[s] = true; q.add(s);
    while (!q.isEmpty()) {
        int v = q.poll();
        for (int e = dinic.head[v]; e != -1; e = dinic.eNext[e])
            if (dinic.eCap[e] > 0 && !visited[dinic.eTo[e]]) { visited[dinic.eTo[e]] = true; q.add(dinic.eTo[e]); }
    }
    return visited;  // true = S-side
}
```

---

## 14.3 Bipartite Matching — Hopcroft-Karp — O(E √V)

```java
static final class HopcroftKarp {
    int n, m;                       // left: 0..n-1, right: 0..m-1
    List<Integer>[] adj;
    int[] matchL, matchR, dist_;

    @SuppressWarnings("unchecked")
    HopcroftKarp(int n, int m) {
        this.n = n; this.m = m;
        adj = new List[n];
        for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
        matchL = new int[n]; Arrays.fill(matchL, -1);
        matchR = new int[m]; Arrays.fill(matchR, -1);
        dist_ = new int[n];
    }

    void addEdge(int u, int v) { adj[u].add(v); }

    boolean bfs() {
        ArrayDeque<Integer> q = new ArrayDeque<>();
        for (int u = 0; u < n; ++u) {
            if (matchL[u] == -1) { dist_[u] = 0; q.add(u); }
            else dist_[u] = Integer.MAX_VALUE;
        }
        boolean found = false;
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v : adj[u]) {
                int w = matchR[v];
                if (w == -1) found = true;
                else if (dist_[w] == Integer.MAX_VALUE) { dist_[w] = dist_[u] + 1; q.add(w); }
            }
        }
        return found;
    }

    boolean dfs(int u) {
        for (int v : adj[u]) {
            int w = matchR[v];
            if (w == -1 || (dist_[w] == dist_[u] + 1 && dfs(w))) {
                matchL[u] = v; matchR[v] = u; return true;
            }
        }
        dist_[u] = Integer.MAX_VALUE; return false;
    }

    int maxMatching() {
        int match = 0;
        while (bfs())
            for (int u = 0; u < n; ++u)
                if (matchL[u] == -1 && dfs(u)) match++;
        return match;
    }
}
// Minimum vertex cover = max matching (König's theorem)
// Maximum independent set = n - max matching (in bipartite)
```

---

## 14.4 2-SAT (Two-Variable Boolean Satisfiability)

**Use for**: constraints of the form `(a OR b)` where a, b are boolean literals.  
Every 2-SAT problem is solvable in O(V + E) via SCC.

```java
static final class TwoSAT {
    int n;
    List<Integer>[] adj, radj;
    List<Integer> order = new ArrayList<>();
    int[] comp;
    boolean[] visited;

    @SuppressWarnings("unchecked")
    TwoSAT(int n) {
        this.n = n;
        adj = new List[2 * n]; radj = new List[2 * n];
        for (int i = 0; i < 2 * n; i++) { adj[i] = new ArrayList<>(); radj[i] = new ArrayList<>(); }
        comp = new int[2 * n]; Arrays.fill(comp, -1);
        visited = new boolean[2 * n];
    }

    // Variable i: literal "x_i = true" is 2*i, "x_i = false" is 2*i+1.
    // Add clause (x_i == vi) OR (x_j == vj).
    void addClause(int i, boolean vi, int j, boolean vj) {
        int a = 2 * i + (vi ? 0 : 1);   // literal x_i == vi
        int b = 2 * j + (vj ? 0 : 1);   // literal x_j == vj
        // (!a -> b) and (!b -> a); negation of literal L is L ^ 1
        adj[a ^ 1].add(b);  radj[b].add(a ^ 1);
        adj[b ^ 1].add(a);  radj[a].add(b ^ 1);
    }

    void dfs1(int v) { visited[v] = true; for (int u : adj[v]) if (!visited[u]) dfs1(u); order.add(v); }
    void dfs2(int v, int c) { comp[v] = c; for (int u : radj[v]) if (comp[u] == -1) dfs2(u, c); }

    // Returns assignment if satisfiable, else null. values[i] = assignment for variable i.
    boolean[] solve() {
        for (int i = 0; i < 2 * n; ++i) if (!visited[i]) dfs1(i);
        int c = 0;
        for (int i = order.size() - 1; i >= 0; --i)
            if (comp[order.get(i)] == -1) dfs2(order.get(i), c++);
        boolean[] values = new boolean[n];
        for (int i = 0; i < n; ++i) {
            if (comp[2 * i] == comp[2 * i + 1]) return null;   // unsatisfiable
            values[i] = comp[2 * i] > comp[2 * i + 1];
        }
        return values;
    }
}

// Example: "at most one of x_i, x_j is true" = (!x_i OR !x_j)
// sat.addClause(i, false, j, false);
```

> The recursive `dfs1`/`dfs2` (Tarjan/Kosaraju) can overflow the stack for large
> V; for contest sizes wrap `solve()` in a big-stack thread or rewrite the SCC
> passes iteratively.

---

## 14.5 Minimum Cost Maximum Flow (MCMF)

**Use for**: flow with costs (assignment problems, transportation).

```java
static final class MCMF {
    int n;
    int[] eTo, eNext, head;
    long[] eCap, eCost, eFlow;
    int ecnt = 0;
    long[] d; int[] p; long[] a; boolean[] inq;

    MCMF(int n, int maxEdges) {
        this.n = n;
        head = new int[n]; Arrays.fill(head, -1);
        eTo = new int[2 * maxEdges]; eNext = new int[2 * maxEdges];
        eCap = new long[2 * maxEdges]; eCost = new long[2 * maxEdges]; eFlow = new long[2 * maxEdges];
        d = new long[n]; p = new int[n]; a = new long[n]; inq = new boolean[n];
    }

    void addEdge(int from, int to, long cap, long cost) {
        eTo[ecnt] = to;   eCap[ecnt] = cap; eCost[ecnt] = cost;  eNext[ecnt] = head[from]; head[from] = ecnt++;
        eTo[ecnt] = from; eCap[ecnt] = 0;   eCost[ecnt] = -cost; eNext[ecnt] = head[to];   head[to]   = ecnt++;
    }

    // returns {flow, cost}
    long[] minCostFlow(int s, int t, long maxFlow) {
        long flow = 0, cost = 0;
        while (flow < maxFlow) {
            Arrays.fill(d, Long.MAX_VALUE);
            Arrays.fill(inq, false);
            d[s] = 0; a[s] = maxFlow - flow;
            ArrayDeque<Integer> q = new ArrayDeque<>();
            q.add(s); inq[s] = true;
            while (!q.isEmpty()) {              // SPFA (Bellman-Ford with queue)
                int u = q.poll(); inq[u] = false;
                for (int e = head[u]; e != -1; e = eNext[e]) {
                    if (eCap[e] > eFlow[e] && d[eTo[e]] > d[u] + eCost[e]) {
                        d[eTo[e]] = d[u] + eCost[e];
                        p[eTo[e]] = e;
                        a[eTo[e]] = Math.min(a[u], eCap[e] - eFlow[e]);
                        if (!inq[eTo[e]]) { inq[eTo[e]] = true; q.add(eTo[e]); }
                    }
                }
            }
            if (d[t] == Long.MAX_VALUE) break;
            flow += a[t]; cost += a[t] * d[t];
            int u = t;
            while (u != s) { eFlow[p[u]] += a[t]; eFlow[p[u] ^ 1] -= a[t]; u = eTo[p[u] ^ 1]; }
        }
        return new long[]{flow, cost};
    }
}
```

---

## 14.6 Euler Path and Circuit — O(V + E)

**Euler path**: visits every edge exactly once.  
**Euler circuit**: same, but starts and ends at the same vertex.

```java
// Conditions (undirected):
// Circuit: all vertices have even degree (and graph is connected)
// Path: exactly 2 vertices have odd degree

// Hierholzer's algorithm (iterative — already stack-based, no recursion risk)
// adj[u] holds entries {v, edgeIndex}; usedEdge marks consumed edges.
static List<Integer> eulerPath(int start, int N, List<int[]>[] adj, boolean[] usedEdge) {
    List<Integer> path = new ArrayList<>();
    Deque<Integer> st = new ArrayDeque<>();
    int[] ptr = new int[N + 1];
    st.push(start);
    while (!st.isEmpty()) {
        int u = st.peek();
        if (ptr[u] == adj[u].size()) { path.add(u); st.pop(); }
        else {
            int[] e = adj[u].get(ptr[u]++);
            int v = e[0], idx = e[1];
            if (!usedEdge[idx]) { usedEdge[idx] = true; st.push(v); }
        }
    }
    Collections.reverse(path);
    return path;  // size should be E+1 for Euler path/circuit
}
```

---

## 14.7 Flow/Matching Decision Guide

```
Maximum flow needed?                    → Dinic's
Bipartite matching?                     → Hopcroft-Karp (or Dinic's on bipartite)
Minimum vertex cover in bipartite?      → König: = max matching
Maximum independent set in bipartite?  → n - max matching
Assignment problem (min cost matching)? → MCMF or Hungarian O(N³)
Boolean satisfiability (2 literals)?    → 2-SAT
Euler circuit/path exists?              → Check degree conditions, Hierholzer
```

---

## 14.8 Key Theorems

```
Max-Flow Min-Cut   : max flow from s to t = min capacity of any s-t cut
König's Theorem    : in bipartite graph, max matching = min vertex cover
Hall's Theorem     : bipartite graph has perfect matching iff for every subset S
                     of left vertices, |N(S)| >= |S|
Dilworth's Theorem : in a DAG, min number of chains to cover = max antichain size
                     (solved with bipartite matching)
```

---

**Next**: [15 — DP Optimizations](15_dp_optimizations.md)
