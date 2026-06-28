# Exercises: Searching Algorithms

## Easy

1. Implement linear search.
2. Implement iterative binary search.
3. Implement recursive binary search.
4. Find first occurrence of x in a sorted array.
5. Find last occurrence of x in a sorted array.

## Medium

1. Implement lower_bound and upper_bound manually.
2. Search in a rotated sorted array.
3. Find peak element using binary search logic.
4. Find square root of integer using binary search.
5. Binary search on answer for minimum feasible capacity.

## Hard

1. Median of two sorted arrays.
2. Search in row-wise sorted matrix.
3. K-th smallest pair distance using binary search on answer.
4. Aggressive cows / maximize minimum distance.
5. Split array largest sum using binary search on answer.

## Challenge

Create a reusable binary-search-on-answer template and apply it to 3 different optimization problems.

---

## Next Steps

- Read the matching theory: [../02_searching_sorting/searching.md](../02_searching_sorting/searching.md)
- Previous: [Basics Exercises](basics_exercises.md)
- Next: [Sorting Exercises](sorting_exercises.md)

## Java 21 Exercise Example: Binary Search

```java
import java.util.*;

public class BinarySearchExample {
    static int binarySearch(int[] a, int x) {
        int l = 0, r = a.length - 1;
        while (l <= r) {
            int m = l + (r - l) / 2;
            if (a[m] == x) return m;
            if (a[m] < x) l = m + 1;
            else r = m - 1;
        }
        return -1;
    }
}
```
