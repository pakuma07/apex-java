# Prefix Sum

## Concept

A prefix-sum array stores cumulative totals so that the sum of any contiguous range can be answered in constant time. Define `pref[0] = 0` and `pref[i] = a[0] + a[1] + ... + a[i-1]`; then the sum of `a[l..r]` (inclusive) is simply `pref[r+1] - pref[l]`. The one-time build costs `O(n)`, after which every range-sum query is `O(1)`, which is a huge win when many queries hit the same static array. The size-`n+1` array with a leading zero removes special-casing for `l == 0`. The same idea generalizes to 2D grids and to prefix XOR, prefix products, and difference arrays for range updates.

## Mermaid

```mermaid
flowchart TD
    A[pref[0] = 0] --> B[For i in 0..n-1: pref[i+1] = pref[i] + a[i]]
    B --> C[Build complete: O(n)]
    C --> D[Query sum of a[l..r]]
    D --> E[Return pref[r+1] - pref[l]: O(1)]
```

## Complexity

- Time: Build `O(n)`; each range-sum query `O(1)`.
- Space: `O(n)` for the prefix array.

## Java Code

```java
class PrefixSum {
    // Build pref where pref[i] = a[0] + ... + a[i-1], and pref[0] = 0.
    static long[] buildPrefix(int[] a) {
        long[] pref = new long[a.length + 1];      // all zero by default; pref[0] = 0
        for (int i = 0; i < a.length; i++)
            pref[i + 1] = pref[i] + a[i];          // running cumulative total
        return pref;
    }

    // Inclusive sum of a[l..r] in O(1). Requires 0 <= l <= r < n.
    static long rangeSum(long[] pref, int l, int r) {
        return pref[r + 1] - pref[l];              // cancels the a[0..l-1] portion
    }
}
```

## Mini Usage Example

```java
int[] a = {2, 1, 3, 4};
long[] pref = PrefixSum.buildPrefix(a);    // pref = {0, 2, 3, 6, 10}
long s = PrefixSum.rangeSum(pref, 1, 3);   // a[1]+a[2]+a[3] = 1+3+4 = 8
```

## Code Snippet Flow

```mermaid
flowchart LR
    A[Initialize pref[0]=0] --> B[Iterate elements]
    B --> C[pref[i+1] = pref[i] + a[i]]
    C --> D[Build done]
    D --> E[Query: pref[r+1] - pref[l]]
```
