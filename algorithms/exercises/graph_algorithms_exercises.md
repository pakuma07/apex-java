# Exercises: Graph Algorithms

## Easy

1. BFS traversal.
2. DFS traversal.
3. Count connected components.
4. Check path existence.
5. Shortest path in unweighted graph.

## Medium

1. Dijkstra shortest path.
2. Topological sort.
3. Detect cycle in directed graph.
4. Bipartite graph check.
5. Kruskal MST.

## Hard

1. Bellman-Ford with negative cycle detection.
2. Floyd-Warshall all-pairs shortest path.
3. Kosaraju SCC.
4. Bridges and articulation points.
5. Edmonds-Karp max flow overview implementation.

## Challenge

Implement a generic weighted graph template and plug in BFS, DFS, Dijkstra, and MST algorithms.

---

## Next Steps

- Read the matching theory: [../08_graph_algorithms/graph_algorithms.md](../08_graph_algorithms/graph_algorithms.md)
- Previous: [Dynamic Programming Exercises](dynamic_programming_exercises.md)
- Next: [String Algorithms Exercises](string_algorithms_exercises.md)

## Java 21 Exercise Example: BFS Shortest Path (Unweighted)

```java
import java.util.*;

public class BfsShortestPath {
    static int[] bfsDist(int src, List<List<Integer>> g) {
        int[] dist = new int[g.size()];
        Arrays.fill(dist, -1);
        Queue<Integer> q = new ArrayDeque<>();
        dist[src] = 0;
        q.add(src);
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v : g.get(u)) if (dist[v] == -1) { dist[v] = dist[u] + 1; q.add(v); }
        }
        return dist;
    }
}
```
