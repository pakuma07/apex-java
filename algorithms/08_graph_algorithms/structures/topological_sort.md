# Topological Sort

## Concept

A topological sort linearly orders the vertices of a directed acyclic graph (DAG) so that every edge u -> v places u before v. Kahn's algorithm computes the in-degree of each vertex, seeds a queue with all in-degree-zero vertices (those with no prerequisites), then repeatedly removes a vertex, appends it to the order, and decrements its neighbors' in-degrees, enqueuing any that reach zero. If the produced order contains fewer than V vertices, the graph has a cycle and no topological order exists. It runs in O(V+E). Use it to schedule tasks with dependencies, resolve build/compilation order, and sequence course prerequisites.

## Mermaid

```mermaid
flowchart TD
    A[Input directed graph] --> B[Calculate in-degree for each vertex]
    B --> C[Initialize queue with in-degree 0 vertices]
    C --> D[While queue not empty]
    D --> E[Dequeue vertex u]
    E --> F[Add u to topological order]
    F --> G[For each neighbor v of u]
    G --> H[Decrease in-degree of v]
    H --> I{in-degree[v] == 0?}
    I -- Yes --> J[Enqueue v]
    I -- No --> K{More neighbors?}
    J --> K
    K -- Yes --> G
    K -- No --> L{Queue empty?}
    L -- No --> D
    L -- Yes --> M[Return topological order]
```

## Complexity

- Time: O(V+E)
- Space: O(V)

## Java Code

```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

static List<Integer> topologicalSort(int n, List<List<Integer>> adj) {
    int[] inDeg = new int[n];
    for (int u = 0; u < n; u++) {
        for (int v : adj.get(u)) {
            inDeg[v]++;
        }
    }

    Queue<Integer> q = new ArrayDeque<>();
    for (int i = 0; i < n; i++) {
        if (inDeg[i] == 0) q.add(i);
    }

    List<Integer> topoOrder = new ArrayList<>();
    while (!q.isEmpty()) {
        int u = q.poll();
        topoOrder.add(u);

        for (int v : adj.get(u)) {
            inDeg[v]--;
            if (inDeg[v] == 0) {
                q.add(v);
            }
        }
    }

    return topoOrder;
}
```

## Mini Usage Example

```java
List<List<Integer>> adj = new ArrayList<>();
for (int i = 0; i < 4; i++) adj.add(new ArrayList<>());
adj.get(0).add(1);
adj.get(0).add(2);
adj.get(1).add(3);
adj.get(2).add(3);
List<Integer> topoOrder = topologicalSort(4, adj);
```

## Code Snippet Flow

```mermaid
flowchart LR
    A[For each edge u to v: inDeg[v]++] --> B[Initialize queue]
    B --> C[Enqueue all vertices with inDeg 0]
    C --> D[topoOrder = empty]
    D --> E{Queue not empty?}
    E -- No --> F[Return topoOrder]
    E -- Yes --> G[Dequeue vertex u]
    G --> H[Add u to topoOrder]
    H --> I[For each neighbor v of u]
    I --> J[Decrease inDeg[v]]
    J --> K{inDeg[v] == 0?}
    K -- Yes --> L[Enqueue v]
    K -- No --> M{More neighbors?}
    L --> M
    M -- Yes --> I
    M -- No --> E
```
