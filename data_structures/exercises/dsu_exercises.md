# Exercises: DSU (Union-Find)

## Easy

1. Basic find and union operations.
2. Count number of connected components.
3. Detect cycle in undirected graph using DSU.
4. Group equivalent strings.
5. Friend circles problem.

## Medium

1. Number of islands II (dynamic union).
2. Redundant connection detection.
3. Accounts merge.
4. Largest component by common factor.
5. Offline connectivity queries.

## Hard

1. DSU with rollback (persistent queries).
2. Kruskal MST with constraints.
3. Dynamic graph connectivity (partial offline).
4. DSU on tree introduction problem.
5. Parity constraints with DSU.

## Challenge

Implement weighted DSU supporting relative value constraints.

## Java 21 Exercise Example: Union-Find

```java
public class DSU {
    int[] p;

    public DSU(int n) {
        p = new int[n];
        for (int i = 0; i < n; i++) p[i] = i;
    }

    public int find(int x) {
        return p[x] == x ? x : (p[x] = find(p[x]));
    }

    public void unite(int a, int b) {
        p[find(a)] = find(b);
    }
}
```
