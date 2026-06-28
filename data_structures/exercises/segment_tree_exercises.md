# Exercises: Segment Tree

## Easy

1. Build segment tree for sum.
2. Range sum query.
3. Point update.
4. Range minimum query variant.
5. Count odd/even in range.

## Medium

1. Lazy propagation for range add.
2. Range assignment + range sum.
3. Maximum subarray segment tree.
4. K-th zero query.
5. Merge sort tree introduction.

## Hard

1. Persistent segment tree basics.
2. Dynamic segment tree for large coordinates.
3. Segment tree beats overview problem.
4. 2D segment tree concept problem.
5. Offline queries with segment tree + sweepline.

## Challenge

Implement generic segment tree with function object combine operation.

## Java 21 Exercise Example: Segment Tree Build

```java
public class SegmentTree {
    public static void build(int v, int l, int r, int[] a, long[] t) {
        if (l == r) { t[v] = a[l]; return; }
        int m = (l + r) / 2;
        build(v * 2, l, m, a, t);
        build(v * 2 + 1, m + 1, r, a, t);
        t[v] = t[v * 2] + t[v * 2 + 1];
    }
}
```
