# Exercises: Divide and Conquer

## Easy

1. Binary search recursively.
2. Merge two sorted halves.
3. Find max/min recursively.
4. Implement merge sort.
5. Implement quick sort partition step.

## Medium

1. Maximum subarray via divide and conquer.
2. Count inversions.
3. Search in rotated sorted array.
4. Closest element to target in sorted array.
5. Majority element using divide and conquer.

## Hard

1. Closest pair of points overview implementation.
2. Skyline problem.
3. Fast polynomial multiplication overview.
4. Merge k sorted arrays with divide and conquer.
5. Count smaller numbers after self via merge idea.

## Challenge

Compare one problem solved by divide and conquer versus a DP or greedy alternative and explain tradeoffs.

---

## Next Steps

- Read the matching theory: [../05_divide_and_conquer/divide_and_conquer.md](../05_divide_and_conquer/divide_and_conquer.md)
- Previous: [Recursion and Backtracking Exercises](recursion_backtracking_exercises.md)
- Next: [Greedy Exercises](greedy_exercises.md)

## Java 21 Exercise Example: Merge Two Sorted Arrays

```java
import java.util.*;

public class MergeTwo {
    static List<Integer> mergeTwo(int[] a, int[] b) {
        List<Integer> out = new ArrayList<>();
        int i = 0, j = 0;
        while (i < a.length && j < b.length) out.add(a[i] <= b[j] ? a[i++] : b[j++]);
        while (i < a.length) out.add(a[i++]);
        while (j < b.length) out.add(b[j++]);
        return out;
    }
}
```
