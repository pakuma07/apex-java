# DFS

## Concept

Depth-first search explores a graph by going as deep as possible along each branch before backtracking. It can be written recursively (using the call stack) or iteratively with an explicit stack, and a visited array prevents revisiting vertices. DFS naturally exposes structural properties of a graph: it is the basis for cycle detection, topological sorting, finding connected components, and discovering bridges or articulation points. Like BFS it runs in O(V + E), but the order in which vertices are reached is deep-first rather than level-first.

## Mermaid

```mermaid
graph TD
    0((0)) --> 1((1))
    0 --> 2((2))
    1 --> 3((3))
    2 --> 4((4))
```

Recursive visit order from 0: `0, 1, 3, 2, 4` (dives down 0->1->3 before backtracking).

## Complexity

- Time: O(V + E) — each vertex and edge processed once
- Space: O(V) for the visited array plus O(V) recursion/stack depth in the worst case

## Java Code

```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class Dfs {
    // Recursive DFS: append vertices in the order they are first visited.
    static void dfsRecursive(int u, List<List<Integer>> g,
                             boolean[] visited, List<Integer> order) {
        visited[u] = true;
        order.add(u);
        for (int v : g.get(u))
            if (!visited[v])
                dfsRecursive(v, g, visited, order);
    }

    // Iterative DFS using an explicit stack (ArrayDeque used as a LIFO stack).
    static List<Integer> dfsIterative(int src, List<List<Integer>> g) {
        boolean[] visited = new boolean[g.size()];
        List<Integer> order = new ArrayList<>();
        Deque<Integer> st = new ArrayDeque<>();
        st.push(src);
        while (!st.isEmpty()) {
            int u = st.pop();
            if (visited[u]) continue;   // may be pushed multiple times
            visited[u] = true;
            order.add(u);
            // Push neighbors; they will be popped (visited) in reverse order.
            for (int v : g.get(u))
                if (!visited[v]) st.push(v);
        }
        return order;
    }
}
```

## Mini Usage Example

```java
// Adjacency list: 0->{1,2}, 1->{3}, 2->{4}, 3->{}, 4->{}
List<List<Integer>> g = List.of(
        List.of(1, 2), List.of(3), List.of(4), List.of(), List.of());

boolean[] visited = new boolean[g.size()];
List<Integer> order = new ArrayList<>();
Dfs.dfsRecursive(0, g, visited, order);
// order == [0, 1, 3, 2, 4]

List<Integer> order2 = Dfs.dfsIterative(0, g); // same set, order may differ
```

## Code Snippet Flow

```mermaid
flowchart TD
    A[Mark u visited, record u] --> B[For each neighbor v of u]
    B --> C{v visited?}
    C -- no --> D[Recurse / push v]
    D --> B
    C -- yes --> B
    B --> E[Backtrack when neighbors exhausted]
```
