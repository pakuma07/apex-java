# Exercises: Recursion and Backtracking

## Easy

1. Factorial recursively.
2. Power function recursively.
3. Generate all subsets.
4. Generate all binary strings of length n.
5. Print all root-to-leaf paths in a tree.

## Medium

1. Generate permutations.
2. Combination sum.
3. Phone keypad combinations.
4. Word search in a grid.
5. Palindrome partitioning.

## Hard

1. N-Queens.
2. Sudoku solver.
3. Rat in a maze with path listing.
4. Expression add operators.
5. Partition into k equal sum subsets.

## Challenge

Implement a generic backtracking template with choose / explore / undo stages and apply it to 3 problems.

---

## Next Steps

- Read the matching theory: [../04_recursion_backtracking/recursion_backtracking.md](../04_recursion_backtracking/recursion_backtracking.md)
- Previous: [Pattern-Based Exercises](pattern_based_exercises.md)
- Next: [Divide and Conquer Exercises](divide_and_conquer_exercises.md)

## Java 21 Exercise Example: Generate Permutations

```java
import java.util.*;

public class Permutations {
    static void permute(int i, int[] a, List<List<Integer>> out) {
        if (i == a.length) {
            List<Integer> cur = new ArrayList<>();
            for (int v : a) cur.add(v);
            out.add(cur);
            return;
        }
        for (int j = i; j < a.length; j++) {
            swap(a, i, j);
            permute(i + 1, a, out);
            swap(a, i, j);
        }
    }

    static void swap(int[] a, int i, int j) {
        int t = a[i]; a[i] = a[j]; a[j] = t;
    }
}
```
