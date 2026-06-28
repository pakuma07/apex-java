// graphs.java
// Adjacency-list graph supporting breadth-first (BFS) and depth-first (DFS) traversal.

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class graphs {

    // Graph: stores vertices 0..n-1 as an adjacency list (list of neighbor lists).
    // Space O(n + edges). BFS and DFS each visit every vertex/edge once: O(n + edges).
    static class Graph {
        private final int n;
        private final List<List<Integer>> adj;  // adj[u] = list of vertices adjacent to u

        // Build an empty graph with the given vertex count. O(n).
        Graph(int vertices) {
            n = vertices;
            adj = new ArrayList<>(n);
            for (int i = 0; i < n; ++i) adj.add(new ArrayList<>());
        }

        // Add edge u->v; for undirected graphs also add the reverse edge v->u. O(1).
        void addEdge(int u, int v, boolean undirected) {
            adj.get(u).add(v);
            if (undirected) adj.get(v).add(u);
        }

        // Convenience overload matching the C++ default (undirected = true).
        void addEdge(int u, int v) {
            addEdge(u, v, true);
        }

        // Recursive DFS helper: mark u, print it, then recurse into unvisited neighbors.
        private void dfsUtil(int u, boolean[] visited) {
            visited[u] = true;
            System.out.print(u + " ");
            for (int v : adj.get(u)) {
                if (!visited[v]) dfsUtil(v, visited);  // descend only into unseen vertices
            }
        }

        // Breadth-first traversal from start, printing vertices in level order. O(n + edges).
        void bfs(int start) {
            boolean[] visited = new boolean[n];
            Queue<Integer> q = new ArrayDeque<>();
            visited[start] = true;  // mark on enqueue to avoid re-adding the same vertex
            q.add(start);

            while (!q.isEmpty()) {
                int u = q.poll();  // dequeue next frontier vertex
                System.out.print(u + " ");
                for (int v : adj.get(u)) {
                    if (!visited[v]) {
                        visited[v] = true;   // mark before enqueue (prevents duplicate enqueues)
                        q.add(v);
                    }
                }
            }
            System.out.println();
        }

        // Depth-first traversal from start via the recursive helper. O(n + edges).
        void dfs(int start) {
            boolean[] visited = new boolean[n];
            dfsUtil(start, visited);
            System.out.println();
        }
    }

    // Driver: builds a small graph and prints its BFS and DFS orders from vertex 0.
    public static void main(String[] a) {
        Graph g = new Graph(6);
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 4);
        g.addEdge(2, 5);

        System.out.print("BFS from 0: ");
        g.bfs(0);

        System.out.print("DFS from 0: ");
        g.dfs(0);
    }
}
