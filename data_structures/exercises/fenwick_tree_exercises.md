# Exercises: Fenwick Tree

## Easy

1. Build Fenwick from array.
2. Point update and prefix query.
3. Range sum query using two prefixes.
4. Frequency table queries.
5. Inversion count idea with BIT.

## Medium

1. Coordinate compression + BIT.
2. Count smaller elements after self.
3. Dynamic order statistics (frequency BIT).
4. Range update, point query variant.
5. 2D Fenwick tree basics.

## Hard

1. Range update and range query with two BITs.
2. Number of increasing subsequences.
3. K-th order statistic via binary lifting on BIT.
4. Offline rectangle sum queries.
5. Dynamic inversion count updates.

## Challenge

Benchmark Fenwick vs Segment Tree for point-update/range-sum workload.

## Java 21 Exercise Example: Fenwick Tree

```java
public class Fenwick {
    int n;
    int[] bit;

    public Fenwick(int n) {
        this.n = n;
        this.bit = new int[n + 1];
    }

    public void add(int i, int v) {
        for (++i; i <= n; i += i & -i) bit[i] += v;
    }

    public int sum(int i) {
        int s = 0;
        for (++i; i > 0; i -= i & -i) s += bit[i];
        return s;
    }
}
```
