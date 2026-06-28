# Exercises: Greedy Algorithms

## Easy

1. Activity selection.
2. Minimum number of coins with canonical denominations.
3. Assign cookies problem.
4. Maximum units on a truck.
5. Buy and sell stock II.

## Medium

1. Fractional knapsack.
2. Job sequencing with deadlines.
3. Minimum platforms.
4. Merge intervals.
5. Huffman coding cost.

## Hard

1. Gas station circuit.
2. Minimum arrows to burst balloons.
3. Rearrange string so adjacent chars differ.
4. Schedule tasks with cooldown.
5. Minimum refueling stops.

## Challenge

For 10 candidate problems, decide whether greedy is valid and explain the greedy-choice property or why it fails.

---

## Next Steps

- Read the matching theory: [../06_greedy/greedy_algorithms.md](../06_greedy/greedy_algorithms.md)
- Previous: [Divide and Conquer Exercises](divide_and_conquer_exercises.md)
- Next: [Dynamic Programming Exercises](dynamic_programming_exercises.md)

## Java 21 Exercise Example: Coin Count Greedy

```java
import java.util.*;

public class CoinCountGreedy {
    static int minCoins(int amount, int[] coinsDesc) {
        int used = 0;
        for (int c : coinsDesc) {
            used += amount / c;
            amount %= c;
        }
        return amount == 0 ? used : -1;
    }
}
```
