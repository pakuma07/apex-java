# Graph Algorithms

This chapter has been split into micro-files.

## Structure Files

1. [Graph Traversals](structures/traversals.md)
2. [Dijkstra](structures/dijkstra.md)
3. [Bellman-Ford](structures/bellman_ford.md)
4. [Floyd-Warshall](structures/floyd_warshall.md)
5. [Topological Sort](structures/topological_sort.md)
6. [Minimum Spanning Tree](structures/minimum_spanning_tree.md)

## Practice

- ../exercises/graph_algorithms_exercises.md

## Related Data Structures

- ../../data_structures/04_graphs/graphs.md

---

## Next Steps

- Review the cheat sheet: [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md)
- Previous: [Chapter 7: Dynamic Programming](../07_dynamic_programming/dynamic_programming.md)
- Next: [Chapter 9: String Algorithms](../09_string_algorithms/string_algorithms.md)


## Java Example: BFS Traversal

```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

static List<Integer> bfs(int src, List<List<Integer>> g) {
    boolean[] vis = new boolean[g.size()];
    List<Integer> order = new ArrayList<>();
    Queue<Integer> q = new ArrayDeque<>();
    vis[src] = true;
    q.add(src);
    while (!q.isEmpty()) {
        int u = q.poll();
        order.add(u);
        for (int v : g.get(u)) {
            if (!vis[v]) { vis[v] = true; q.add(v); }
        }
    }
    return order;
}
```
