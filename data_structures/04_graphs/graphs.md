# Graphs

A graph is a collection of vertices and edges.

This chapter has been split into one file per representation and traversal.

## Graph Structure Chapters

1. [Adjacency Matrix](structures/adjacency_matrix.md)
2. [Adjacency List](structures/adjacency_list.md)
3. [BFS](structures/bfs.md)
4. [DFS](structures/dfs.md)

## Complexity

| Representation | Space | Edge Check |
|----------------|-------|------------|
| Adjacency Matrix | O(V^2) | O(1) |
| Adjacency List | O(V + E) | O(deg(u)) |

## Practice

Use chapter-style exercises from:
- ../exercises/graph_representation_exercises.md
- ../exercises/graph_traversal_exercises.md

## Java 21 Example: Adjacency List Build

```java
import java.util.ArrayList;
import java.util.List;

class GraphBuilder {
    static List<List<Integer>> buildGraph(int n, int[][] edges) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) {
            g.get(e[0]).add(e[1]);
            g.get(e[1]).add(e[0]);
        }
        return g;
    }
}
```
