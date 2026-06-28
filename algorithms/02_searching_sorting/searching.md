# Searching Algorithms

This chapter has been split into micro-files.

## Structure Files

1. [Linear Search](structures/linear_search.md)
2. [Binary Search](structures/binary_search.md)
3. [Lower Bound Pattern](structures/lower_bound.md)

## Practice

- ../exercises/searching_exercises.md

## Related Chapter

- sorting.md

---

## Next Steps

- Review the cheat sheet: [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md)
- Previous: [Chapter 1: Algorithmic Thinking Basics](../01_basics/algorithmic_thinking.md)
- Continue in this chapter: [Sorting Algorithms](sorting.md)
- Next chapter: [Chapter 3: Pattern-Based Algorithms](../03_pattern_based/two_pointers_sliding_window_prefix.md)

## Java Example: Binary Search

```java
public final class Searching {
    public static int binarySearch(int[] a, int target) {
        int l = 0, r = a.length - 1;
        while (l <= r) {
            int m = l + (r - l) / 2;
            if (a[m] == target) return m;
            if (a[m] < target) l = m + 1;
            else r = m - 1;
        }
        return -1;
    }
}
```
