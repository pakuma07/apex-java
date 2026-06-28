# Sorting Algorithms

This chapter has been split into micro-files.

## Structure Files

1. [Bubble Sort](structures/bubble_sort.md)
2. [Selection Sort](structures/selection_sort.md)
3. [Insertion Sort](structures/insertion_sort.md)
4. [Merge Sort](structures/merge_sort.md)
5. [Quick Sort](structures/quick_sort.md)
6. [Heap Sort](structures/heap_sort.md)
7. [Sorting Complexity Table](structures/sorting_complexity_table.md)

## Practice

- ../exercises/sorting_exercises.md

## Related Chapter

- searching.md

---

## Next Steps

- Review the cheat sheet: [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md)
- Previous in this chapter: [Searching Algorithms](searching.md)
- Next chapter: [Chapter 3: Pattern-Based Algorithms](../03_pattern_based/two_pointers_sliding_window_prefix.md)

## Java Example: Merge Sort

```java
public final class Sorting {
    public static void mergeRange(int[] a, int l, int m, int r) {
        int[] left = java.util.Arrays.copyOfRange(a, l, m + 1);
        int[] right = java.util.Arrays.copyOfRange(a, m + 1, r + 1);
        int i = 0, j = 0, k = l;
        while (i < left.length && j < right.length) {
            a[k++] = (left[i] <= right[j]) ? left[i++] : right[j++];
        }
        while (i < left.length) a[k++] = left[i++];
        while (j < right.length) a[k++] = right[j++];
    }
}
```
