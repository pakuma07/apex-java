# Advanced Algorithms

This chapter has been split into micro-files.

## Structure Files

1. [Floyd-Warshall](structures/floyd_warshall.md)
2. [Network Flow](structures/network_flow.md)
3. [Strongly Connected Components](structures/strongly_connected_components.md)
4. [Binary Lifting](structures/binary_lifting.md)
5. [Convex Hull](structures/convex_hull.md)
6. [Bitmask DP](structures/bitmask_dp.md)
7. [Advanced Overview](structures/advanced_overview.md)

## Practice

- ../exercises/advanced_algorithms_exercises.md

## Why Advanced Algorithms Matter

- Larger problem classes
- Better asymptotic performance
- Foundation for competitive programming and systems work

---

## Next Steps

- Review the cheat sheet: [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md)
- Previous: [Chapter 10: Number Theory and Bit Algorithms](../10_number_theory_bit/number_theory_bit.md)
- Return to [Algorithms Module Overview](../README.md)


## Java Example: Disjoint Set Union

```java
public final class DSU {
    private final int[] parent;
    private final int[] rank;

    public DSU(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
    }

    public int find(int x) {                 // path compression
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }

    public boolean union(int a, int b) {
        a = find(a);
        b = find(b);
        if (a == b) return false;
        if (rank[a] < rank[b]) {             // union by rank
            int t = a; a = b; b = t;
        }
        parent[b] = a;
        if (rank[a] == rank[b]) rank[a]++;
        return true;
    }
}
```
