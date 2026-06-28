# Pattern-Based Algorithms

This chapter has been split into micro-files.

## Structure Files

1. [Two Pointers](structures/two_pointers.md)
2. [Sliding Window](structures/sliding_window.md)
3. [Prefix Sum](structures/prefix_sum.md)

## Practice

- ../exercises/pattern_based_exercises.md

---

## Next Steps

- Review the cheat sheet: [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md)
- Previous: [Chapter 2: Sorting Algorithms](../02_searching_sorting/sorting.md)
- Next: [Chapter 4: Recursion and Backtracking](../04_recursion_backtracking/recursion_backtracking.md)

## Java Example: Sliding Window

```java
class SlidingWindow {
    static int maxSumWindowK(int[] a, int k) {
        if (k <= 0 || k > a.length) return 0;
        int cur = 0;
        for (int i = 0; i < k; i++) cur += a[i];
        int best = cur;
        for (int i = k; i < a.length; i++) {
            cur += a[i] - a[i - k];
            best = Math.max(best, cur);
        }
        return best;
    }
}
```
