# Exercises: Dynamic Programming

## Easy

1. Fibonacci with memoization and tabulation.
2. Climbing stairs.
3. House robber.
4. Coin change minimum coins.
5. Maximum sum of non-adjacent elements.

## Medium

1. 0/1 knapsack.
2. Longest increasing subsequence.
3. Longest common subsequence.
4. Edit distance.
5. Unique paths with obstacles.

## Hard

1. Matrix chain multiplication.
2. Palindrome partitioning II.
3. Bitmask DP for TSP overview.
4. Burst balloons.
5. Regular expression matching.

## Challenge

Take 5 recursion problems and convert them into memoization and tabulation forms.

---

## Next Steps

- Read the matching theory: [../07_dynamic_programming/dynamic_programming.md](../07_dynamic_programming/dynamic_programming.md)
- Previous: [Greedy Exercises](greedy_exercises.md)
- Next: [Graph Algorithms Exercises](graph_algorithms_exercises.md)

## Java 21 Exercise Example: Fibonacci DP

```java
import java.util.*;

public class FibonacciDP {
    static int fibDP(int n) {
        if (n <= 1) return n;
        int[] dp = new int[n + 1];
        dp[1] = 1;
        for (int i = 2; i <= n; i++) dp[i] = dp[i - 1] + dp[i - 2];
        return dp[n];
    }
}
```
