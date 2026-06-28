# Greedy Algorithms

This chapter has been split into micro-files.

## Structure Files

1. [Greedy-Choice Property](structures/greedy_choice_property.md)
2. [Activity Selection](structures/activity_selection.md)
3. [Greedy Patterns](structures/greedy_patterns.md)

## Practice

- ../exercises/greedy_exercises.md

---

## Next Steps

- Review the cheat sheet: [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md)
- Previous: [Chapter 5: Divide and Conquer](../05_divide_and_conquer/divide_and_conquer.md)
- Next: [Chapter 7: Dynamic Programming](../07_dynamic_programming/dynamic_programming.md)

## Java Example: Activity Selection

```java
import java.util.Arrays;
import java.util.Comparator;

class ActivitySelection {
    static int maxActivities(int[][] a) {
        // a[i] = {start, finish}; sort by finish time ascending
        Arrays.sort(a, Comparator.comparingInt(x -> x[1]));
        int ans = 0, endT = -1;
        for (int[] it : a) if (it[0] >= endT) { ans++; endT = it[1]; }
        return ans;
    }
}
```
