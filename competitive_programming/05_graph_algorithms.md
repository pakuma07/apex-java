# 05 — Graph Algorithms

Graph problems are the most common category in competitive programming. Know every template cold.

> Java notes: prefer adjacency lists built from `ArrayList<Integer>[]` (or `int[]` CSR for max speed). `PriorityQueue` is a **min-heap by default** — convenient for Dijkstra/Prim. For deep recursive DFS, run on a big-stack thread (see 01.10) or go iterative.

---

## 5.1 Graph Representation

```java
int N, M;   // N nodes (0-indexed or 1-indexed), M edges

// Adjacency list — standard for sparse graphs
@SuppressWarnings("unchecked")
ArrayList<Integer>[] adj = new ArrayList[N + 1];
for (int i = 0; i <= N; ++i) adj[i] = new ArrayList<>();
adj[u].add(v);
adj[v].add(u);   // undirected

// Weighted adjacency list — store {neighbor, weight} as int[]
@SuppressWarnings("unchecked")
ArrayList<int[]>[] wadj = new ArrayList[N + 1];
for (int i = 0; i <= N; ++i) wadj[i] = new ArrayList<>();
wadj[u].add(new int[]{v, w});

// Adjacency matrix — for dense graphs or Floyd-Warshall
long[][] dist = new long[N + 1][N + 1];
for (long[] row : dist) Arrays.fill(row, Long.MAX_VALUE / 4);  // avoid overflow on add
for (int i = 0; i <= N; ++i) dist[i][i] = 0;

// Edge list — for Kruskal's. Use int[]{u, v, w} or a small record.
record Edge(int u, int v, int w) {}
ArrayList<Edge> edges = new ArrayList<>();
```

> Use `Long.MAX_VALUE / 4` (or a custom big `INF`) for matrix "infinity" so that `dist[i][k] + dist[k][j]` cannot overflow `long`.

---

## 5.2 BFS — O(V + E)

Use for: **shortest path in unweighted graph**, level-order traversal, flood fill.

```java
static int[] bfs(int src, ArrayList<Integer>[] adj, int N) {
    int[] dist = new int[N + 1];
    Arrays.fill(dist, -1);
    ArrayDeque<Integer> q = new ArrayDeque<>();
    dist[src] = 0;
    q.add(src);
    while (!q.isEmpty()) {
        int u = q.poll();
        for (int v : adj[u]) {
            if (dist[v] == -1) {
                dist[v] = dist[u] + 1;
                q.add(v);
            }
        }
    }
    return dist;   // dist[i] = shortest hops from src, -1 if unreachable
}
```

---

## 5.3 DFS — O(V + E)

Use for: **cycle detection**, **connected components**, **topological sort**, **tree problems**.

```java
static boolean[] visited;

static void dfs(int u, ArrayList<Integer>[] adj) {
    visited[u] = true;
    for (int v : adj[u])
        if (!visited[v]) dfs(v, adj);
}

// Count connected components
int components = 0;
visited = new boolean[N + 1];
for (int i = 1; i <= N; ++i)
    if (!visited[i]) { dfs(i, adj); ++components; }
```

> For N up to ~10^5+ with deep recursion, either run `dfs` on a big-stack thread or rewrite it with an explicit `ArrayDeque<Integer>` stack to avoid `StackOverflowError`.

---

## 5.4 Dijkstra — O((V + E) log V)

Use for: **shortest path in weighted graph with non-negative weights**.

```java
static long[] dijkstra(int src, ArrayList<int[]>[] adj, int N) {
    long[] dist = new long[N + 1];
    Arrays.fill(dist, Long.MAX_VALUE);
    // min-heap of {dist, node}; PriorityQueue is min-heap by default
    PriorityQueue<long[]> pq =
        new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
    dist[src] = 0;
    pq.add(new long[]{0, src});
    while (!pq.isEmpty()) {
        long[] top = pq.poll();
        long d = top[0]; int u = (int) top[1];
        if (d > dist[u]) continue;            // stale entry — skip
        for (int[] e : adj[u]) {
            int v = e[0]; long w = e[1];
            if (dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w;
                pq.add(new long[]{dist[v], v});
            }
        }
    }
    return dist;
}
```

---

## 5.5 Bellman-Ford — O(V × E)

Use for: **negative weight edges**, **detecting negative cycles**.

```java
static long[] bellmanFord(int src, ArrayList<Edge> edges, int N) {
    long[] dist = new long[N + 1];
    Arrays.fill(dist, Long.MAX_VALUE);
    dist[src] = 0;
    for (int iter = 0; iter < N - 1; ++iter) {
        for (Edge e : edges) {
            if (dist[e.u()] != Long.MAX_VALUE && dist[e.u()] + e.w() < dist[e.v()])
                dist[e.v()] = dist[e.u()] + e.w();
        }
    }
    // N-th relaxation detects negative cycle
    for (Edge e : edges) {
        if (dist[e.u()] != Long.MAX_VALUE && dist[e.u()] + e.w() < dist[e.v()])
            System.out.println("Negative cycle detected");
    }
    return dist;
}
```

---

## 5.6 Floyd-Warshall — O(V³)

Use for: **all-pairs shortest paths**, small N (≤ 400).

```java
static void floydWarshall(long[][] d, int N) {
    for (int k = 1; k <= N; ++k)
        for (int i = 1; i <= N; ++i)
            for (int j = 1; j <= N; ++j)
                if (d[i][k] + d[k][j] < d[i][j])
                    d[i][j] = d[i][k] + d[k][j];
    // Negative cycle: d[i][i] < 0 for some i
}
```

> Using `Long.MAX_VALUE / 4` as INF (see 5.1) lets you skip the explicit `!= INF` checks because `INF + INF` still does not overflow.

---

## 5.7 Topological Sort

**Kahn's Algorithm (BFS-based)** — detects cycles too:

```java
static int[] topoSort(ArrayList<Integer>[] adj, int N) {
    int[] indegree = new int[N + 1];
    for (int u = 1; u <= N; ++u)
        for (int v : adj[u]) indegree[v]++;

    ArrayDeque<Integer> q = new ArrayDeque<>();
    for (int i = 1; i <= N; ++i) if (indegree[i] == 0) q.add(i);

    int[] order = new int[N];
    int sz = 0;
    while (!q.isEmpty()) {
        int u = q.poll();
        order[sz++] = u;
        for (int v : adj[u]) if (--indegree[v] == 0) q.add(v);
    }
    // If sz != N, the graph has a cycle.
    return Arrays.copyOf(order, sz);
}
```

---

## 5.8 Disjoint Set Union (DSU / Union-Find)

```java
static class DSU {
    int[] parent, rank_;
    DSU(int n) {
        parent = new int[n + 1];
        rank_ = new int[n + 1];
        for (int i = 0; i <= n; ++i) parent[i] = i;   // iota
    }
    int find(int x) {
        while (parent[x] != x) parent[x] = parent[parent[x]] = parent[parent[x]];  // see note
        return parent[x];
    }
    boolean unite(int a, int b) {
        a = find(a); b = find(b);
        if (a == b) return false;          // already connected
        if (rank_[a] < rank_[b]) { int t = a; a = b; b = t; }
        parent[b] = a;
        if (rank_[a] == rank_[b]) rank_[a]++;
        return true;
    }
    boolean connected(int a, int b) { return find(a) == find(b); }
}
```

> `find` is written iteratively here (with path halving) to avoid recursion depth issues on huge inputs. A recursive path-compression `find` is fine too if you use a big-stack thread. Path halving (`parent[x] = parent[parent[x]]`) gives the same near-O(α(N)) amortised bound without recursion.

---

## 5.9 Minimum Spanning Tree

### Kruskal's — O(E log E)

```java
static long kruskal(ArrayList<Edge> edges, int N) {
    edges.sort((a, b) -> Integer.compare(a.w(), b.w()));
    DSU dsu = new DSU(N);
    long cost = 0; int cnt = 0;
    for (Edge e : edges) {
        if (dsu.unite(e.u(), e.v())) {
            cost += e.w();
            if (++cnt == N - 1) break;
        }
    }
    return cost;   // cost of MST; cnt < N-1 means graph is disconnected
}
```

### Prim's — O(E log V) with priority queue

```java
static long prim(ArrayList<int[]>[] adj, int N) {
    long[] key = new long[N + 1];
    Arrays.fill(key, Long.MAX_VALUE);
    boolean[] inMST = new boolean[N + 1];
    PriorityQueue<long[]> pq =
        new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
    key[1] = 0; pq.add(new long[]{0, 1});
    long total = 0;
    while (!pq.isEmpty()) {
        long[] top = pq.poll();
        long w = top[0]; int u = (int) top[1];
        if (inMST[u]) continue;
        inMST[u] = true; total += w;
        for (int[] e : adj[u]) {
            int v = e[0]; long ew = e[1];
            if (!inMST[v] && ew < key[v]) { key[v] = ew; pq.add(new long[]{key[v], v}); }
        }
    }
    return total;
}
```

---

## 5.10 Bridges and Articulation Points — O(V + E)

```java
static int[] disc, low;
static int timerVal = 0;
static boolean[] isArticulation;
static ArrayList<int[]> bridges = new ArrayList<>();

static void dfsBridge(int u, int parent, ArrayList<Integer>[] adj) {
    disc[u] = low[u] = ++timerVal;
    int children = 0;
    for (int v : adj[u]) {
        if (disc[v] == 0) {
            children++;
            dfsBridge(v, u, adj);
            low[u] = Math.min(low[u], low[v]);
            if (parent == -1 && children > 1)      isArticulation[u] = true;
            if (parent != -1 && low[v] >= disc[u]) isArticulation[u] = true;
            if (low[v] > disc[u]) bridges.add(new int[]{u, v});
        } else if (v != parent) {
            low[u] = Math.min(low[u], disc[v]);
        }
    }
}
```

---

## 5.11 Strongly Connected Components — Kosaraju's O(V + E)

```java
static ArrayList<Integer> order = new ArrayList<>();
static boolean[] visited;

static void dfs1(int u, ArrayList<Integer>[] adj) {
    visited[u] = true;
    for (int v : adj[u]) if (!visited[v]) dfs1(v, adj);
    order.add(u);
}

static void dfs2(int u, int comp, int[] component, ArrayList<Integer>[] radj) {
    component[u] = comp;
    for (int v : radj[u]) if (component[v] == 0) dfs2(v, comp, component, radj);
}

static int kosaraju(int N, ArrayList<Integer>[] adj, ArrayList<Integer>[] radj) {
    visited = new boolean[N + 1];
    for (int i = 1; i <= N; ++i) if (!visited[i]) dfs1(i, adj);
    int[] component = new int[N + 1];
    int comp = 0;
    for (int i = order.size() - 1; i >= 0; --i) {
        int u = order.get(i);
        if (component[u] == 0) dfs2(u, ++comp, component, radj);
    }
    return comp;   // number of SCCs
}
```

---

## 5.12 Bipartite Check

```java
static boolean isBipartite(ArrayList<Integer>[] adj, int N) {
    int[] color = new int[N + 1];
    Arrays.fill(color, -1);
    for (int s = 1; s <= N; ++s) {
        if (color[s] != -1) continue;
        ArrayDeque<Integer> q = new ArrayDeque<>();
        q.add(s); color[s] = 0;
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v : adj[u]) {
                if (color[v] == -1) { color[v] = 1 - color[u]; q.add(v); }
                else if (color[v] == color[u]) return false;
            }
        }
    }
    return true;
}
```

---

## 5.13 LCA — Lowest Common Ancestor (Binary Lifting) — O(N log N) / O(log N)

```java
static final int LOG = 17;
static int[][] up = new int[100005][LOG];
static int[] dep = new int[100005];
@SuppressWarnings("unchecked")
static ArrayList<Integer>[] ch = new ArrayList[100005];

static void dfsLca(int u, int p, int d) {
    up[u][0] = p; dep[u] = d;
    for (int j = 1; j < LOG; ++j) up[u][j] = up[up[u][j-1]][j-1];
    for (int v : ch[u]) if (v != p) dfsLca(v, u, d + 1);
}

static int lca(int u, int v) {
    if (dep[u] < dep[v]) { int t = u; u = v; v = t; }
    int diff = dep[u] - dep[v];
    for (int j = 0; j < LOG; ++j) if (((diff >> j) & 1) == 1) u = up[u][j];
    if (u == v) return u;
    for (int j = LOG - 1; j >= 0; --j)
        if (up[u][j] != up[v][j]) { u = up[u][j]; v = up[v][j]; }
    return up[u][0];
}
// Tree distance: dep[u] + dep[v] - 2*dep[lca(u,v)]
// Call: dfsLca(root, root, 0);
```

---

## 5.14 0-1 BFS — O(V + E)

**Use for**: graphs where edge weights are only 0 or 1. Replace the priority queue with a deque.

```java
static int[] bfs01(int src, ArrayList<int[]>[] adj, int N) {
    int[] dist = new int[N + 1];
    Arrays.fill(dist, Integer.MAX_VALUE);
    ArrayDeque<Integer> dq = new ArrayDeque<>();
    dist[src] = 0; dq.addLast(src);
    while (!dq.isEmpty()) {
        int u = dq.pollFirst();
        for (int[] e : adj[u]) {
            int v = e[0], w = e[1];
            if (dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w;
                if (w == 0) dq.addFirst(v);   // 0-weight: push front
                else        dq.addLast(v);    // 1-weight: push back
            }
        }
    }
    return dist;
}
```

---

## 5.15 Graph Algorithm Summary

| Problem | Algorithm | Complexity |
|---------|-----------|------------|
| Shortest path, unweighted | BFS | O(V + E) |
| Shortest path, 0/1 weights | 0-1 BFS | O(V + E) |
| Shortest path, non-negative weights | Dijkstra | O((V+E) log V) |
| Shortest path, negative weights | Bellman-Ford | O(VE) |
| All-pairs shortest path | Floyd-Warshall | O(V³) |
| Topological order | Kahn's / DFS | O(V + E) |
| Connected components | DFS / DSU | O(V + E) |
| Minimum spanning tree | Kruskal / Prim | O(E log E) |
| Bridges | Tarjan DFS | O(V + E) |
| SCCs | Kosaraju / Tarjan | O(V + E) |
| Bipartite check | BFS colouring | O(V + E) |
| LCA | Binary lifting | O(N log N) build, O(log N) query |
| Path queries on tree | HLD (see ch13) | O(log² N) |

---

**Next**: [06 — Dynamic Programming](06_dynamic_programming.md)
