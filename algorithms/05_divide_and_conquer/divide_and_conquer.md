# Divide and Conquer

This chapter has been split into micro-files.

## Structure Files

1. [Divide and Conquer Paradigm](structures/divide_and_conquer_paradigm.md)
2. [Maximum Subarray](structures/maximum_subarray.md)

## Practice

- ../exercises/divide_and_conquer_exercises.md

---

## Next Steps

- Review the cheat sheet: [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md)
- Previous: [Chapter 4: Recursion and Backtracking](../04_recursion_backtracking/recursion_backtracking.md)
- Next: [Chapter 6: Greedy Algorithms](../06_greedy/greedy_algorithms.md)

## Java Example: Divide and Conquer Sum

```java
class DacSum {
    static long dacSum(int[] a, int l, int r) {
        if (l == r) return a[l];
        int m = l + (r - l) / 2;
        return dacSum(a, l, m) + dacSum(a, m + 1, r);
    }
}
```
