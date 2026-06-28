# Dynamic Programming

DP solves overlapping subproblems and stores answers.

This chapter has been split into micro-files.

## Structure Files

1. [Memoization vs Tabulation](structures/memoization_vs_tabulation.md)
2. [Fibonacci DP](structures/fibonacci_dp.md)
3. [0/1 Knapsack](structures/knapsack_01.md)
4. [Longest Increasing Subsequence](structures/longest_increasing_subsequence.md)
5. [Longest Common Subsequence](structures/longest_common_subsequence.md)
6. [DP Pattern Families](structures/dp_patterns.md)

## Practice

- ../exercises/dynamic_programming_exercises.md

---

## Next Steps

- Review the cheat sheet: [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md)
- Previous: [Chapter 6: Greedy Algorithms](../06_greedy/greedy_algorithms.md)
- Next: [Chapter 8: Graph Algorithms](../08_graph_algorithms/graph_algorithms.md)

## Java Example: 0/1 Knapsack (1D DP)

```java
public final class Knapsack {
    public static int knapsack01(int[] wt, int[] val, int W) {
        int[] dp = new int[W + 1];
        for (int i = 0; i < wt.length; i++)
            for (int w = W; w >= wt[i]; w--)
                dp[w] = Math.max(dp[w], dp[w - wt[i]] + val[i]);
        return dp[W];
    }
}
```
