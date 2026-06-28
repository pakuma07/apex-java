# Exercises: Pattern-Based Algorithms

## Easy

1. Two sum in sorted array using two pointers.
2. Remove duplicates from sorted array.
3. Maximum sum subarray of fixed size k.
4. Prefix sum range query.
5. Count zeros/ones in a sliding window.

## Medium

1. Longest substring without repeating characters.
2. Smallest subarray with sum at least k.
3. Container with most water.
4. Count subarrays with sum k using prefix sums.
5. Trapping rain water using two pointers.

## Hard

1. Minimum window substring.
2. Subarrays divisible by k.
3. Sliding window median.
4. Longest repeating character replacement.
5. Count nice subarrays with exactly k odd numbers.

## Challenge

For 10 array/string problems, classify whether the best approach is two pointers, sliding window, or prefix sum and justify the choice.

---

## Next Steps

- Read the matching theory: [../03_pattern_based/two_pointers_sliding_window_prefix.md](../03_pattern_based/two_pointers_sliding_window_prefix.md)
- Previous: [Sorting Exercises](sorting_exercises.md)
- Next: [Recursion and Backtracking Exercises](recursion_backtracking_exercises.md)

## Java 21 Exercise Example: Two Pointers

```java
import java.util.*;

public class TwoPointers {
    static boolean hasPairSumSorted(int[] a, int target) {
        int l = 0, r = a.length - 1;
        while (l < r) {
            int s = a[l] + a[r];
            if (s == target) return true;
            if (s < target) l++;
            else r--;
        }
        return false;
    }
}
```
