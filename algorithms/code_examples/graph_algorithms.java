// graph_algorithms.java
// Single-source shortest paths on a weighted graph via Dijkstra's algorithm.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class graph_algorithms {

    // Simple (neighbor, weight) edge holder, mirroring the C++ pair<int,int>.
    static class Edge {
        int to, w;
        Edge(int to, int w) { this.to = to; this.w = w; }
    }

    // Shortest distance from src to every vertex (non-negative weights).
    // Greedy + min-heap; adj holds (neighbor, weight). Time O((V+E) log V), space O(V).
    static int[] dijkstra(int n, List<List<Edge>> adj, int src) {
        final int INF = 1000000000;
        int[] dist = new int[n];
        Arrays.fill(dist, INF);
        // Min-heap of (distance, vertex); smallest distance pops first (compare on distance).
        PriorityQueue<int[]> pq = new PriorityQueue<>((x, y) -> Integer.compare(x[0], y[0]));
        dist[src] = 0;
        pq.add(new int[]{0, src});

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int d = cur[0], u = cur[1];
            if (d != dist[u]) continue;            // stale entry (already improved): skip
            for (Edge edge : adj.get(u)) {
                int v = edge.to, w = edge.w;
                if (dist[u] + w < dist[v]) {        // relax edge u->v
                    dist[v] = dist[u] + w;
                    pq.add(new int[]{dist[v], v});  // push improved distance (lazy deletion)
                }
            }
        }
        return dist;
    }

    public static void main(String[] args) {
        int n = 4;
        List<List<Edge>> adj = new ArrayList<>();
        for (int i = 0; i < n; ++i) adj.add(new ArrayList<>());
        adj.get(0).add(new Edge(1, 4));
        adj.get(0).add(new Edge(2, 1));
        adj.get(2).add(new Edge(1, 2));
        adj.get(1).add(new Edge(3, 1));
        adj.get(2).add(new Edge(3, 5));

        int[] dist = dijkstra(n, adj, 0);
        StringBuilder sb = new StringBuilder();
        for (int d : dist) sb.append(d).append(" ");
        System.out.println(sb.toString());
    }
}
