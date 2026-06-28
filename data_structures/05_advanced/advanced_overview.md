# Advanced Data Structures Overview

This chapter groups structures that are frequently used in algorithm-heavy systems.

## Structure Files

1. [Disjoint Set Union (Union-Find)](structures/disjoint_set_union.md)
2. [Fenwick Tree](structures/fenwick_tree.md)
3. [Segment Tree](structures/segment_tree.md)
4. [Advanced Complexity Table](structures/advanced_complexity_table.md)

## Optional Extensions (Not fully implemented here)

- B-Tree / B+ Tree (database indexing)
- Red-Black Tree (self-balancing BST, used in java.util.TreeMap/TreeSet)
- Suffix Array / Suffix Tree (string algorithms)
- Bloom Filter (probabilistic membership)

## Mermaid Relationship Map

```mermaid
flowchart LR
    A[Advanced DS] --> B[Connectivity]
    A --> C[Range Queries]
    A --> D[Indexing]
    B --> DSU[DSU]
    C --> FT[Fenwick Tree]
    C --> ST[Segment Tree]
    D --> BT[B-Tree Family]
```

## Choosing the Right Structure

- Need dynamic connectivity: DSU
- Need prefix sums with point updates: Fenwick
- Need flexible range aggregation: Segment Tree
- Need disk-friendly index: B-Tree/B+ Tree

## Practice

- ../exercises/dsu_exercises.md
- ../exercises/fenwick_tree_exercises.md
- ../exercises/segment_tree_exercises.md

## Production Tip

Prefer battle-tested standard containers and well-reviewed libraries unless custom implementation is required by constraints.

## Java 21 Example: Fenwick Tree

```java
class Fenwick {
    final int n;
    final long[] bit;
    Fenwick(int n) { this.n = n; this.bit = new long[n + 1]; }
    void add(int idx, long val) { for (++idx; idx <= n; idx += idx & -idx) bit[idx] += val; }
    long sumPrefix(int idx) { long s = 0; for (++idx; idx > 0; idx -= idx & -idx) s += bit[idx]; return s; }
}
```

> Note: Java `long` is a fixed 64-bit signed integer, so accumulating sums can overflow silently (wrapping around) once they exceed 2^63 - 1 — unlike Python's arbitrary-precision integers. Choose `long` over `int` for aggregates and watch for overflow on large inputs.
