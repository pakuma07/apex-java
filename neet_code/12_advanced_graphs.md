# Advanced Graphs -- NeetCode 150

---

## Problem 93: Reconstruct Itinerary
**LeetCode #332** | Find Eulerian path using DFS + Hierholzer's algorithm. Start from "JFK".

### Brute Force -- O(E! * E) try all orderings via backtracking
```java
// Impractical for large inputs; Hierholzer's is the right approach
```

### Optimal -- O(E log E) Hierholzer's algorithm (post-order DFS)
```java
Map<String, PriorityQueue<String>> adj = new HashMap<>();
List<String> res = new ArrayList<>();

List<String> findItinerary(List<List<String>> tickets) {
    for (List<String> t : tickets)
        adj.computeIfAbsent(t.get(0), k -> new PriorityQueue<>()).add(t.get(1));
    dfs("JFK");
    Collections.reverse(res);
    return res;
}
void dfs(String u) {
    PriorityQueue<String> pq = adj.get(u);
    while (pq != null && !pq.isEmpty()) dfs(pq.poll());
    res.add(u);
}
```

---

## Problem 94: Min Cost to Connect All Points
**LeetCode #1584** | Manhattan distance MST.

### Brute Force -- O(N^2 log N) Kruskal's with all edges
```java
int[] par;

int minCostConnectPoints(int[][] pts) {
    int n = pts.length;
    List<int[]> edges = new ArrayList<>(); // {weight, u, v}
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)
            edges.add(new int[]{Math.abs(pts[i][0] - pts[j][0]) + Math.abs(pts[i][1] - pts[j][1]), i, j});
    edges.sort((a, b) -> a[0] - b[0]);
    par = new int[n];
    for (int i = 0; i < n; i++) par[i] = i;
    int cost = 0, cnt = 0;
    for (int[] e : edges) {
        int a = find(e[1]), b = find(e[2]);
        if (a != b) { par[a] = b; cost += e[0]; if (++cnt == n - 1) break; }
    }
    return cost;
}
int find(int x) { return par[x] == x ? x : (par[x] = find(par[x])); }
```

### Optimal -- O(N^2) Prim's algorithm without building all edges
```java
int minCostConnectPoints(int[][] pts) {
    int n = pts.length, cost = 0;
    int[] minDist = new int[n];
    Arrays.fill(minDist, Integer.MAX_VALUE);
    boolean[] inMST = new boolean[n];
    minDist[0] = 0;
    for (int i = 0; i < n; i++) {
        int u = -1;
        for (int j = 0; j < n; j++) if (!inMST[j] && (u == -1 || minDist[j] < minDist[u])) u = j;
        inMST[u] = true; cost += minDist[u];
        for (int v = 0; v < n; v++) if (!inMST[v]) {
            int d = Math.abs(pts[u][0] - pts[v][0]) + Math.abs(pts[u][1] - pts[v][1]);
            minDist[v] = Math.min(minDist[v], d);
        }
    }
    return cost;
}
```

---

## Problem 95: Network Delay Time
**LeetCode #743** | Dijkstra SSSP; return max dist to all nodes, -1 if unreachable.

### Brute Force -- O(V^2 + E) Dijkstra with linear scan instead of heap
### Optimal -- O((V+E) log V) Dijkstra with min-heap
```java
int networkDelayTime(int[][] times, int n, int k) {
    List<List<int[]>> adj = new ArrayList<>(); // {neighbor, weight}
    for (int i = 0; i <= n; i++) adj.add(new ArrayList<>());
    for (int[] t : times) adj.get(t[0]).add(new int[]{t[1], t[2]});
    int[] dist = new int[n + 1];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[k] = 0;
    // min-heap on {dist, node}
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
    pq.add(new int[]{0, k});
    while (!pq.isEmpty()) {
        int[] top = pq.poll();
        int d = top[0], u = top[1];
        if (d > dist[u]) continue;
        for (int[] e : adj.get(u)) {
            int v = e[0], w = e[1];
            if (dist[u] + w < dist[v]) { dist[v] = dist[u] + w; pq.add(new int[]{dist[v], v}); }
        }
    }
    int mx = 0;
    for (int i = 1; i <= n; i++) {
        if (dist[i] == Integer.MAX_VALUE) return -1;
        mx = Math.max(mx, dist[i]);
    }
    return mx;
}
```

---

## Problem 96: Swim in Rising Water
**LeetCode #778** | Find path from (0,0) to (n-1,n-1) minimizing maximum elevation traversed.

### Brute Force -- O(N^2 log N) binary search on answer + BFS for each
```java
int[] dx = {1, -1, 0, 0}, dy = {0, 0, 1, -1};

boolean canReach(int[][] g, int t) {
    int n = g.length;
    if (g[0][0] > t || g[n - 1][n - 1] > t) return false;
    boolean[][] vis = new boolean[n][n];
    Queue<int[]> q = new ArrayDeque<>();
    q.add(new int[]{0, 0}); vis[0][0] = true;
    while (!q.isEmpty()) {
        int[] cell = q.poll();
        int r = cell[0], c = cell[1];
        if (r == n - 1 && c == n - 1) return true;
        for (int d = 0; d < 4; d++) {
            int nr = r + dx[d], nc = c + dy[d];
            if (nr >= 0 && nr < n && nc >= 0 && nc < n && !vis[nr][nc] && g[nr][nc] <= t) {
                vis[nr][nc] = true; q.add(new int[]{nr, nc});
            }
        }
    }
    return false;
}
int swimInWater(int[][] grid) {
    int n = grid.length, lo = grid[0][0], hi = n * n - 1;
    while (lo < hi) { int mid = (lo + hi) / 2; if (canReach(grid, mid)) hi = mid; else lo = mid + 1; }
    return lo;
}
```

### Optimal -- O(N^2 log N) Dijkstra (treat elevation as edge weight)
```java
int swimInWater(int[][] grid) {
    int n = grid.length;
    // min-heap on {time, r, c}
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
    pq.add(new int[]{grid[0][0], 0, 0});
    int[][] dist = new int[n][n];
    for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
    dist[0][0] = grid[0][0];
    int[] dx = {1, -1, 0, 0}, dy = {0, 0, 1, -1};
    while (!pq.isEmpty()) {
        int[] top = pq.poll();
        int t = top[0], r = top[1], c = top[2];
        if (r == n - 1 && c == n - 1) return t;
        for (int d = 0; d < 4; d++) {
            int nr = r + dx[d], nc = c + dy[d];
            if (nr >= 0 && nr < n && nc >= 0 && nc < n) {
                int nt = Math.max(t, grid[nr][nc]);
                if (nt < dist[nr][nc]) { dist[nr][nc] = nt; pq.add(new int[]{nt, nr, nc}); }
            }
        }
    }
    return -1;
}
```

---

## Problem 97: Alien Dictionary
**LeetCode #269** | Determine character order from sorted alien dictionary words.

### Brute Force -- O(C^2) compare all pairs of characters
### Optimal -- O(V+E) topological sort from derived character order
```java
String alienOrder(String[] words) {
    Map<Character, Set<Character>> adj = new HashMap<>();
    Map<Character, Integer> indeg = new HashMap<>();
    for (String w : words) for (char c : w.toCharArray()) indeg.putIfAbsent(c, 0);
    for (int i = 0; i < words.length - 1; i++) {
        String a = words[i], b = words[i + 1];
        int mn = Math.min(a.length(), b.length());
        boolean found = false;
        for (int j = 0; j < mn; j++) {
            if (a.charAt(j) != b.charAt(j)) {
                adj.putIfAbsent(a.charAt(j), new HashSet<>());
                if (!adj.get(a.charAt(j)).contains(b.charAt(j))) {
                    adj.get(a.charAt(j)).add(b.charAt(j));
                    indeg.merge(b.charAt(j), 1, Integer::sum);
                }
                found = true; break;
            }
        }
        if (!found && a.length() > b.length()) return ""; // invalid
    }
    Queue<Character> q = new ArrayDeque<>();
    for (Map.Entry<Character, Integer> e : indeg.entrySet()) if (e.getValue() == 0) q.add(e.getKey());
    StringBuilder res = new StringBuilder();
    while (!q.isEmpty()) {
        char u = q.poll();
        res.append(u);
        for (char v : adj.getOrDefault(u, Set.of()))
            if (indeg.merge(v, -1, Integer::sum) == 0) q.add(v);
    }
    return res.length() == indeg.size() ? res.toString() : "";
}
```

---

## Problem 98: Cheapest Flights Within K Stops
**LeetCode #787** | Bellman-Ford with at most K+1 relaxation rounds.

### Brute Force -- O(K * E^K) DFS exploring all paths up to K stops
### Optimal -- O(K * E) Bellman-Ford, K+1 rounds
```java
int findCheapestPrice(int n, int[][] flights, int src, int dst, int k) {
    int[] price = new int[n];
    Arrays.fill(price, Integer.MAX_VALUE);
    price[src] = 0;
    for (int i = 0; i <= k; i++) {
        int[] tmp = price.clone();
        for (int[] f : flights) {
            if (price[f[0]] == Integer.MAX_VALUE) continue;
            tmp[f[1]] = Math.min(tmp[f[1]], price[f[0]] + f[2]);
        }
        price = tmp;
    }
    return price[dst] == Integer.MAX_VALUE ? -1 : price[dst];
}
```
