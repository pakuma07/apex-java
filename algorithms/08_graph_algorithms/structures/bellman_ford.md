# Bellman Ford

## Concept

Bellman-Ford computes single-source shortest paths and, unlike Dijkstra, tolerates negative edge weights. It relaxes every edge repeatedly: after k full passes over all edges, every shortest path using at most k edges is correct, so V-1 passes suffice for any simple path in a V-vertex graph. A final extra pass detects negative cycles: if any edge can still be relaxed, a negative-weight cycle is reachable and no shortest path is well-defined. It runs in O(V*E), slower than Dijkstra, but is the standard choice when negative weights are present or when you must detect negative cycles (e.g., currency-arbitrage checks).

## Mermaid

```mermaid
flowchart TD
    A[Input Edges and Weights] --> B[Initialize distances to INF]
    B --> C[Set source distance to 0]
    C --> D[Relax edges V-1 times]
    D --> E[Processed Distances]
    E --> F[Final Shortest Paths]
```

## Complexity

- Time: O(VE)
- Space: O(V)

## Java Code

```java
import java.util.Arrays;
import java.util.List;

// Java long is 64-bit; INF is halved so dist[u] + w cannot overflow.
static final long INF = Long.MAX_VALUE / 2;

// Each edge is {u, v, w}.
static long[] bellmanFord(int src, int n, List<int[]> edges) {
    long[] dist = new long[n];
    Arrays.fill(dist, INF);
    dist[src] = 0;

    for (int i = 0; i < n - 1; i++) {
        for (int[] e : edges) {
            int u = e[0];
            int v = e[1];
            int w = e[2];
            if (dist[u] != INF && dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w;
            }
        }
    }

    return dist;
}
```

## Mini Usage Example

```java
List<int[]> edges = List.of(
        new int[]{0, 1, 4},
        new int[]{0, 2, 2},
        new int[]{1, 2, 1},
        new int[]{1, 3, 5},
        new int[]{2, 3, 8});
long[] dist = bellmanFord(0, 4, edges);
```

## Code Snippet Flow

```mermaid
flowchart LR
    A[Initialize all distances to INF] --> B[Set source to 0]
    B --> C[For each of V-1 iterations]
    C --> D[For each edge u-v-w]
    D --> E{dist[u] != INF?}
    E -- Yes --> F{dist[u] + w < dist[v]?}
    F -- Yes --> G[Update dist[v]]
    G --> H[Continue next edge]
    E -- No --> H
    F -- No --> H
    H --> I[Return final distances]
```
