// advanced_algorithms.java
// Advanced graph algorithm: all-pairs shortest paths via Floyd-Warshall.

public class advanced_algorithms {

    // All-pairs shortest paths in place on an adjacency matrix.
    // DP over intermediate vertices (Floyd-Warshall). Time O(n^3), space O(1) extra.
    static void floydWarshall(int[][] dist) {
        int n = dist.length;
        // k = highest-numbered intermediate vertex allowed on the path so far.
        for (int k = 0; k < n; ++k) {
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    // Skip if either leg is unreachable (avoids INF+INF overflow).
                    if (dist[i][k] < 1000000000 && dist[k][j] < 1000000000) {
                        // Relax: best of current path vs. routing i->...->k->...->j.
                        dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        final int INF = 1000000000;
        int[][] dist = {
            {0, 3, INF, 7},
            {8, 0, 2, INF},
            {5, INF, 0, 1},
            {2, INF, INF, 0}
        };
        floydWarshall(dist);
        for (int[] row : dist) {
            StringBuilder sb = new StringBuilder();
            for (int x : row) sb.append(x).append(" ");
            System.out.println(sb.toString());
        }
    }
}
