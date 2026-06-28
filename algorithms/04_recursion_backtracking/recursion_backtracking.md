# Recursion and Backtracking

This chapter has been split into micro-files.

## Structure Files

1. [Recursion Basics](structures/recursion_basics.md)
2. [Backtracking](structures/backtracking.md)

## Practice

- ../exercises/recursion_backtracking_exercises.md

---

## Next Steps

- Review the cheat sheet: [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md)
- Previous: [Chapter 3: Pattern-Based Algorithms](../03_pattern_based/two_pointers_sliding_window_prefix.md)
- Next: [Chapter 5: Divide and Conquer](../05_divide_and_conquer/divide_and_conquer.md)

## Java Example: Backtracking Skeleton

```java
import java.util.ArrayList;
import java.util.List;

class Subsets {
    static void buildSubsets(int idx, int[] a, List<Integer> cur, List<List<Integer>> out) {
        if (idx == a.length) { out.add(new ArrayList<>(cur)); return; }
        buildSubsets(idx + 1, a, cur, out);
        cur.add(a[idx]);
        buildSubsets(idx + 1, a, cur, out);
        cur.remove(cur.size() - 1);
    }
}
```
