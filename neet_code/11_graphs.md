# Graphs -- NeetCode 150

---

## Problem 80: Number of Islands
**LeetCode #200** | Count islands (groups of '1') in a 2D grid.

### Brute Force -- O((M*N)^2) for each '1', do a BFS and mark visited separately each time
```java
// Naive: re-scan grid for each unvisited '1' without union-find
```

### Optimal -- O(M*N) DFS/BFS flood fill
```java
int numIslands(char[][] grid) {
    int m = grid.length, n = grid[0].length, cnt = 0;
    for (int r = 0; r < m; r++)
        for (int c = 0; c < n; c++)
            if (grid[r][c] == '1') { dfs(grid, r, c); cnt++; }
    return cnt;
}
void dfs(char[][] grid, int r, int c) {
    if (r < 0 || r >= grid.length || c < 0 || c >= grid[0].length || grid[r][c] != '1') return;
    grid[r][c] = '0';
    dfs(grid, r + 1, c); dfs(grid, r - 1, c); dfs(grid, r, c + 1); dfs(grid, r, c - 1);
}
```

---

## Problem 81: Clone Graph
**LeetCode #133**

### Brute Force -- O(V+E) BFS, same as optimal
### Optimal -- O(V+E) DFS with hash map
```java
class Node {
    int val;
    List<Node> neighbors;
    Node() { this.val = 0; this.neighbors = new ArrayList<>(); }
    Node(int v) { this.val = v; this.neighbors = new ArrayList<>(); }
}

Node cloneGraph(Node node) {
    if (node == null) return null;
    Map<Node, Node> mp = new HashMap<>();
    return dfs(node, mp);
}
Node dfs(Node n, Map<Node, Node> mp) {
    if (mp.containsKey(n)) return mp.get(n);
    Node copy = new Node(n.val);
    mp.put(n, copy);
    for (Node nb : n.neighbors) copy.neighbors.add(dfs(nb, mp));
    return copy;
}
```

---

## Problem 82: Max Area of Island
**LeetCode #695**

### Brute Force -- O((M*N)^2) scan all cells repeatedly
### Optimal -- O(M*N) DFS returning area
```java
int maxAreaOfIsland(int[][] grid) {
    int m = grid.length, n = grid[0].length, best = 0;
    for (int r = 0; r < m; r++)
        for (int c = 0; c < n; c++)
            best = Math.max(best, dfs(grid, r, c));
    return best;
}
int dfs(int[][] grid, int r, int c) {
    if (r < 0 || r >= grid.length || c < 0 || c >= grid[0].length || grid[r][c] == 0) return 0;
    grid[r][c] = 0;
    return 1 + dfs(grid, r + 1, c) + dfs(grid, r - 1, c) + dfs(grid, r, c + 1) + dfs(grid, r, c - 1);
}
```

---

## Problem 83: Pacific Atlantic Water Flow
**LeetCode #417**

### Brute Force -- O(M^2*N^2) BFS/DFS from each cell to check both oceans
### Optimal -- O(M*N) reverse BFS from both oceans
```java
int[] dx = {1, -1, 0, 0}, dy = {0, 0, 1, -1};

List<List<Integer>> pacificAtlantic(int[][] h) {
    int m = h.length, n = h[0].length;
    boolean[][] pac = new boolean[m][n], atl = new boolean[m][n];
    Queue<int[]> pq = new ArrayDeque<>(), aq = new ArrayDeque<>();
    for (int r = 0; r < m; r++) {
        pac[r][0] = true; pq.add(new int[]{r, 0});
        atl[r][n - 1] = true; aq.add(new int[]{r, n - 1});
    }
    for (int c = 0; c < n; c++) {
        pac[0][c] = true; pq.add(new int[]{0, c});
        atl[m - 1][c] = true; aq.add(new int[]{m - 1, c});
    }
    bfs(h, pq, pac); bfs(h, aq, atl);
    List<List<Integer>> res = new ArrayList<>();
    for (int r = 0; r < m; r++)
        for (int c = 0; c < n; c++)
            if (pac[r][c] && atl[r][c]) res.add(List.of(r, c));
    return res;
}
void bfs(int[][] h, Queue<int[]> q, boolean[][] vis) {
    int m = h.length, n = h[0].length;
    while (!q.isEmpty()) {
        int[] cell = q.poll();
        int r = cell[0], c = cell[1];
        for (int d = 0; d < 4; d++) {
            int nr = r + dx[d], nc = c + dy[d];
            if (nr >= 0 && nr < m && nc >= 0 && nc < n && !vis[nr][nc] && h[nr][nc] >= h[r][c]) {
                vis[nr][nc] = true; q.add(new int[]{nr, nc});
            }
        }
    }
}
```

---

## Problem 84: Surrounded Regions
**LeetCode #130** | Capture all 'O' regions not connected to border.

### Brute Force -- O(M*N) BFS for each 'O' region, check if connected to border
### Optimal -- O(M*N) mark border-connected 'O's, then flip rest
```java
void solve(char[][] board) {
    int m = board.length, n = board[0].length;
    for (int r = 0; r < m; r++) { dfs(board, r, 0); dfs(board, r, n - 1); }
    for (int c = 0; c < n; c++) { dfs(board, 0, c); dfs(board, m - 1, c); }
    for (int r = 0; r < m; r++)
        for (int c = 0; c < n; c++) {
            if (board[r][c] == 'O') board[r][c] = 'X';
            else if (board[r][c] == 'S') board[r][c] = 'O';
        }
}
void dfs(char[][] board, int r, int c) {
    if (r < 0 || r >= board.length || c < 0 || c >= board[0].length || board[r][c] != 'O') return;
    board[r][c] = 'S';
    dfs(board, r + 1, c); dfs(board, r - 1, c); dfs(board, r, c + 1); dfs(board, r, c - 1);
}
```

---

## Problem 85: Rotting Oranges
**LeetCode #994** | Min minutes to rot all oranges; -1 if impossible.

### Brute Force -- O((M*N)^2) simulate minute by minute by scanning full grid
### Optimal -- O(M*N) multi-source BFS
```java
int orangesRotting(int[][] grid) {
    int m = grid.length, n = grid[0].length, fresh = 0, time = 0;
    Queue<int[]> q = new ArrayDeque<>();
    for (int r = 0; r < m; r++)
        for (int c = 0; c < n; c++) {
            if (grid[r][c] == 2) q.add(new int[]{r, c});
            else if (grid[r][c] == 1) fresh++;
        }
    int[] dx = {1, -1, 0, 0}, dy = {0, 0, 1, -1};
    while (!q.isEmpty() && fresh > 0) {
        time++;
        for (int i = q.size(); i > 0; i--) {
            int[] cell = q.poll();
            int r = cell[0], c = cell[1];
            for (int d = 0; d < 4; d++) {
                int nr = r + dx[d], nc = c + dy[d];
                if (nr >= 0 && nr < m && nc >= 0 && nc < n && grid[nr][nc] == 1) {
                    grid[nr][nc] = 2; fresh--; q.add(new int[]{nr, nc});
                }
            }
        }
    }
    return fresh == 0 ? time : -1;
}
```

---

## Problem 86: Walls and Gates
**LeetCode #286** | Fill each empty room with distance to nearest gate.

### Brute Force -- O(M^2*N^2) BFS from each empty room
### Optimal -- O(M*N) multi-source BFS from all gates simultaneously
```java
void wallsAndGates(int[][] rooms) {
    int m = rooms.length, n = rooms[0].length, INF = 2147483647;
    Queue<int[]> q = new ArrayDeque<>();
    for (int r = 0; r < m; r++)
        for (int c = 0; c < n; c++)
            if (rooms[r][c] == 0) q.add(new int[]{r, c});
    int[] dx = {1, -1, 0, 0}, dy = {0, 0, 1, -1};
    while (!q.isEmpty()) {
        int[] cell = q.poll();
        int r = cell[0], c = cell[1];
        for (int d = 0; d < 4; d++) {
            int nr = r + dx[d], nc = c + dy[d];
            if (nr >= 0 && nr < m && nc >= 0 && nc < n && rooms[nr][nc] == INF) {
                rooms[nr][nc] = rooms[r][c] + 1; q.add(new int[]{nr, nc});
            }
        }
    }
}
```

---

## Problem 87: Course Schedule
**LeetCode #207** | Detect cycle in directed graph (can finish all courses?).

### Brute Force -- O(V*(V+E)) DFS from each node checking for cycle
### Optimal -- O(V+E) DFS with 3-color marking (0=unvisited, 1=visiting, 2=done)
```java
boolean canFinish(int n, int[][] prereqs) {
    List<List<Integer>> adj = new ArrayList<>();
    for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    for (int[] e : prereqs) adj.get(e[1]).add(e[0]);
    int[] state = new int[n];
    for (int i = 0; i < n; i++) if (!dfs(i, adj, state)) return false;
    return true;
}
boolean dfs(int u, List<List<Integer>> adj, int[] state) {
    if (state[u] == 1) return false; // cycle
    if (state[u] == 2) return true;
    state[u] = 1;
    for (int v : adj.get(u)) if (!dfs(v, adj, state)) return false;
    state[u] = 2; return true;
}
```

---

## Problem 88: Course Schedule II
**LeetCode #210** | Return topological order, or [] if cycle.

### Brute Force -- O(V^2+E) repeated removal of nodes with in-degree 0
### Optimal -- O(V+E) Kahn's BFS topological sort
```java
int[] findOrder(int n, int[][] prereqs) {
    List<List<Integer>> adj = new ArrayList<>();
    for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    int[] indeg = new int[n];
    for (int[] e : prereqs) { adj.get(e[1]).add(e[0]); indeg[e[0]]++; }
    Queue<Integer> q = new ArrayDeque<>();
    for (int i = 0; i < n; i++) if (indeg[i] == 0) q.add(i);
    int[] order = new int[n]; int idx = 0;
    while (!q.isEmpty()) {
        int u = q.poll();
        order[idx++] = u;
        for (int v : adj.get(u)) if (--indeg[v] == 0) q.add(v);
    }
    return idx == n ? order : new int[0];
}
```

---

## Problem 89: Redundant Connection
**LeetCode #684** | Find the edge that creates a cycle (return last such edge).

### Brute Force -- O(N^2) add edges one by one, BFS/DFS to check for cycle
### Optimal -- O(N * alpha(N)) Union-Find
```java
int[] par, rank_;

int[] findRedundantConnection(int[][] edges) {
    int n = edges.length;
    par = new int[n + 1]; rank_ = new int[n + 1];
    for (int i = 0; i <= n; i++) par[i] = i;
    for (int[] e : edges) {
        int a = find(e[0]), b = find(e[1]);
        if (a == b) return e;
        if (rank_[a] < rank_[b]) { int t = a; a = b; b = t; }
        par[b] = a;
        if (rank_[a] == rank_[b]) rank_[a]++;
    }
    return new int[0];
}
int find(int x) { return par[x] == x ? x : (par[x] = find(par[x])); }
```

---

## Problem 90: Number of Connected Components
**LeetCode #323** | Count connected components in undirected graph.

### Brute Force -- O(V+E) DFS from each unvisited node
```java
int countComponents(int n, int[][] edges) {
    List<List<Integer>> adj = new ArrayList<>();
    for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    for (int[] e : edges) { adj.get(e[0]).add(e[1]); adj.get(e[1]).add(e[0]); }
    boolean[] vis = new boolean[n]; int cnt = 0;
    for (int i = 0; i < n; i++) if (!vis[i]) { dfs(i, adj, vis); cnt++; }
    return cnt;
}
void dfs(int u, List<List<Integer>> adj, boolean[] vis) {
    vis[u] = true;
    for (int v : adj.get(u)) if (!vis[v]) dfs(v, adj, vis);
}
```

### Optimal -- O(N * alpha(N)) Union-Find
```java
int[] par;

int countComponents(int n, int[][] edges) {
    par = new int[n];
    for (int i = 0; i < n; i++) par[i] = i;
    int cnt = n;
    for (int[] e : edges) {
        int a = find(e[0]), b = find(e[1]);
        if (a != b) { par[a] = b; cnt--; }
    }
    return cnt;
}
int find(int x) { return par[x] == x ? x : (par[x] = find(par[x])); }
```

---

## Problem 91: Graph Valid Tree
**LeetCode #261** | Check if undirected graph is a valid tree (connected + no cycles).

### Brute Force -- O(V+E) DFS cycle check + connectivity check
```java
int cnt;

boolean validTree(int n, int[][] edges) {
    if (edges.length != n - 1) return false; // necessary condition
    List<List<Integer>> adj = new ArrayList<>();
    for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    for (int[] e : edges) { adj.get(e[0]).add(e[1]); adj.get(e[1]).add(e[0]); }
    boolean[] vis = new boolean[n];
    cnt = 0;
    dfs(0, adj, vis);
    return cnt == n;
}
void dfs(int u, List<List<Integer>> adj, boolean[] vis) {
    vis[u] = true; cnt++;
    for (int v : adj.get(u)) if (!vis[v]) dfs(v, adj, vis);
}
```

### Optimal -- O(N * alpha(N)) Union-Find
```java
int[] par;

boolean validTree(int n, int[][] edges) {
    par = new int[n];
    for (int i = 0; i < n; i++) par[i] = i;
    for (int[] e : edges) {
        int a = find(e[0]), b = find(e[1]);
        if (a == b) return false;
        par[a] = b; n--;
    }
    return n == 1;
}
int find(int x) { return par[x] == x ? x : (par[x] = find(par[x])); }
```

---

## Problem 92: Word Ladder
**LeetCode #127** | Minimum transformations to change beginWord to endWord changing one letter at a time.

### Brute Force -- O(N^2 * L) BFS comparing every pair
```java
// Naive: for each word in queue, compare with every word in wordList
```

### Optimal -- O(N * L^2) BFS with character substitution
```java
int ladderLength(String begin, String end, List<String> wordList) {
    Set<String> ws = new HashSet<>(wordList);
    if (!ws.contains(end)) return 0;
    Queue<String> q = new ArrayDeque<>();
    q.add(begin);
    int steps = 1;
    while (!q.isEmpty()) {
        for (int i = q.size(); i > 0; i--) {
            String w = q.poll();
            if (w.equals(end)) return steps;
            char[] arr = w.toCharArray();
            for (int j = 0; j < arr.length; j++) {
                char orig = arr[j];
                for (char c = 'a'; c <= 'z'; c++) {
                    arr[j] = c;
                    String next = new String(arr);
                    if (ws.contains(next)) { q.add(next); ws.remove(next); }
                }
                arr[j] = orig;
            }
        }
        steps++;
    }
    return 0;
}
```
