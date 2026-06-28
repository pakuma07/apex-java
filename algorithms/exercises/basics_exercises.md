# Exercises: Algorithmic Thinking Basics

## Easy

1. Analyze time complexity of nested loops with fixed and variable bounds.
2. Compare O(n), O(n log n), and O(n^2) for n = 10, 100, 1000.
3. Find best, average, and worst case for linear search.
4. Write loop invariants for prefix sum computation.
5. Identify edge cases for empty input, single element, duplicates.

## Medium

1. Prove correctness of iterative binary search.
2. Explain why amortized analysis matters for dynamic arrays.
3. Rewrite a brute-force O(n^2) solution into O(n log n) using sorting.
4. Derive space complexity for recursive merge sort.
5. Explain when asymptotically faster code can still be slower in practice.

## Hard

1. Give a correctness proof for merge sort by induction.
2. Prove why comparison sorting needs Omega(n log n) comparisons in the worst case.
3. Analyze a recursive algorithm with recurrence T(n) = 2T(n/2) + n.
4. Explain a case where greedy intuition fails and DP is needed.
5. Design adversarial tests for a naive quick sort implementation.

## Challenge

Take 5 problems you solved earlier and write a full correctness + complexity explanation for each.

---

## Next Steps

- Read the matching theory: [../01_basics/algorithmic_thinking.md](../01_basics/algorithmic_thinking.md)
- Review the cheat sheet: [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md)
- Next: [Searching Exercises](searching_exercises.md)

## Java 21 Exercise Example: Prefix Sums

```java
import java.util.*;

public class PrefixSums {
    static long[] prefixSums(int[] a) {
        long[] pref = new long[a.length + 1];
        for (int i = 0; i < a.length; i++) pref[i + 1] = pref[i] + a[i];
        return pref;
    }
}
```
