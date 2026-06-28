# Exercises: Graph Representations

## Easy

1. Build adjacency list from edge list.
2. Build adjacency matrix from edge list.
3. Count degrees of all vertices.
4. Convert adjacency matrix -> adjacency list.
5. Convert adjacency list -> adjacency matrix.

## Medium

1. Detect self-loops and multi-edges.
2. Count connected components (undirected).
3. Determine if graph is sparse or dense.
4. Read weighted graph and print sorted edges.
5. Build directed graph and compute indegree/outdegree.

## Hard

1. Dynamic edge add/remove with fast queries.
2. Memory analysis for matrix vs list at scale.
3. Support both directed and undirected edges in one API.
4. Multi-graph support with edge IDs.
5. Graph serialization/deserialization format.

## Challenge

Design a generic graph class for int/string vertex labels.

## Java 21 Exercise Example: Edge List to Adjacency List

```java
import java.util.ArrayList;
import java.util.List;

public class MakeAdj {
    public static List<List<Integer>> makeAdj(int n, List<int[]> edges) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) g.get(e[0]).add(e[1]);
        return g;
    }
}
```
