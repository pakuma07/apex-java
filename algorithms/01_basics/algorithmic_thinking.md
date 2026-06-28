# Algorithmic Thinking Basics

This chapter has been split into micro-files.

## Structure Files

1. [Problem Solving Pipeline](structures/problem_solving_pipeline.md)
2. [Complexity Analysis](structures/complexity_analysis.md)
3. [Correctness and Invariants](structures/correctness_and_invariants.md)

## Practice

- ../exercises/basics_exercises.md

---

## Next Steps

- Review the cheat sheet: [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md)
- Move to [Chapter 2: Searching and Sorting](../02_searching_sorting/searching.md)

## Java 21 Example: Complexity Mindset

```java
public final class ComplexityMindset {
    public static long sumLinear(int[] a) {
        long s = 0;
        for (int x : a) s += x; // O(n)
        return s;
    }
}
```
