# Floyd Warshall

## Concept

Floyd-Warshall solves the all-pairs shortest-path problem with dynamic programming over which vertices may serve as intermediate stops. Beginning from the weighted adjacency matrix, it considers each vertex k in turn and asks, for every ordered pair (i, j), whether the path i -> k -> j improves the current best: dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j]). The invariant after processing k is that dist[i][j] is optimal among paths whose intermediate vertices all lie in {0..k}; after the last k it is the true shortest distance. It handles negative edges, and a negative value on the diagonal (dist[i][i] < 0) reveals a negative cycle. With three nested loops it costs O(V^3) time and O(V^2) space, ideal for small or dense graphs where every pair is needed.

## Mermaid

```mermaid
flowchart TD
    A[Load weighted adjacency matrix] --> B[k = 0]
    B --> C[For every pair i, j]
    C --> D{"dist[i][k] + dist[k][j] < dist[i][j]?"}
    D -- Yes --> E[Update dist[i][j]]
    D -- No --> F[Keep dist[i][j]]
    E --> G{More pairs?}
    F --> G
    G -- Yes --> C
    G -- No --> H{k < V-1?}
    H -- Yes --> I[k = k + 1] --> C
    H -- No --> J[All-pairs shortest paths ready]
```

## Complexity

- Time: O(V^3)
- Space: O(V^2) for the distance matrix

## Java Code

```java
import java.util.Arrays;

public final class FloydWarshall {

    // Use half of Long.MAX_VALUE so that INF + INF still fits in a 64-bit long
    // without overflowing (Java longs wrap silently, unlike Python's big ints).
    public static final long INF = Long.MAX_VALUE / 2;

    // dist starts as the adjacency matrix: dist[i][i]=0, INF where no direct edge.
    // Returns the all-pairs shortest-distance matrix (operating on a copy).
    public static long[][] floydWarshall(long[][] input) {
        int n = input.length;
        long[][] dist = new long[n][];
        for (int i = 0; i < n; i++) dist[i] = Arrays.copyOf(input[i], n);

        for (int k = 0; k < n; k++)                  // candidate intermediate vertex
            for (int i = 0; i < n; i++)              // source
                for (int j = 0; j < n; j++) {        // destination
                    // Only relax through reachable paths to keep INF "sticky"
                    if (dist[i][k] < INF && dist[k][j] < INF)
                        dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
                }

        return dist;
    }

    // Optional: a negative dist[i][i] after the run means a negative cycle exists.
    public static boolean hasNegativeCycle(long[][] dist) {
        for (int i = 0; i < dist.length; i++)
            if (dist[i][i] < 0) return true;
        return false;
    }
}
```

## Mini Usage Example

```java
int n = 4;
long[][] dist = new long[n][n];
for (long[] row : dist) Arrays.fill(row, FloydWarshall.INF);
for (int i = 0; i < n; i++) dist[i][i] = 0;
dist[0][1] = 4; dist[0][2] = 2;
dist[1][2] = 1; dist[1][3] = 5;
dist[2][3] = 8;

long[][] sp = FloydWarshall.floydWarshall(dist);
// sp[0][3] == 11 via 0 -> 2 -> 3 (2 + ... ) vs 0 -> 1 -> 3 (4 + 5 = 9): min is 9
// Shortest 0 -> 3 = 9 (path 0 -> 1 -> 3); sp[0][2] = 2; sp[0][1] = 4
```

## Code Snippet Flow

```mermaid
flowchart LR
    A[dist = adjacency matrix] --> B[For k = 0..V-1]
    B --> C[For i = 0..V-1]
    C --> D[For j = 0..V-1]
    D --> E{Both dist[i][k] and dist[k][j] finite?}
    E -- Yes --> F["dist[i][j] = min(dist[i][j], dist[i][k]+dist[k][j])"]
    E -- No --> G[Skip]
    F --> H{Loops done?}
    G --> H
    H -- No --> D
    H -- Yes --> I[Return shortest-path matrix]
```
