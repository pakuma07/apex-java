# Exercises: Sorting Algorithms

## Easy

1. Implement bubble sort.
2. Implement selection sort.
3. Implement insertion sort.
4. Check whether an array is already sorted.
5. Count swaps in bubble sort.

## Medium

1. Implement merge sort.
2. Implement quick sort.
3. Implement heap sort.
4. Sort nearly sorted array using heap.
5. Stable sort custom records by multiple keys.

## Hard

1. Count inversions using merge sort.
2. Quickselect for k-th smallest element.
3. External sorting design overview.
4. Sort linked list in O(n log n).
5. Radix sort for non-negative integers.

## Challenge

Benchmark bubble, insertion, merge, quick, and heap sort on random, sorted, and reverse-sorted inputs.

---

## Next Steps

- Read the matching theory: [../02_searching_sorting/sorting.md](../02_searching_sorting/sorting.md)
- Previous: [Searching Exercises](searching_exercises.md)
- Next: [Pattern-Based Exercises](pattern_based_exercises.md)

## Java 21 Exercise Example: Insertion Sort

```java
import java.util.*;

public class InsertionSort {
    static void insertionSort(int[] a) {
        for (int i = 1; i < a.length; i++) {
            int key = a[i], j = i - 1;
            while (j >= 0 && a[j] > key) { a[j + 1] = a[j]; j--; }
            a[j + 1] = key;
        }
    }
}
```
