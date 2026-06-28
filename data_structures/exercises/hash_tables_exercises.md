# Exercises: Hash Tables

## Easy

1. Count frequency of elements.
2. Check if two arrays are equal as multisets.
3. Find first unique character in a string.
4. Group duplicate words with counts.
5. Check anagram using hashing.

## Medium

1. Two-sum and three-sum variations.
2. Longest substring without repeating characters.
3. Top-k frequent elements.
4. Subarray with sum k.
5. Longest consecutive sequence.

## Hard

1. Design hashmap from scratch (chaining).
2. Design hashmap from scratch (open addressing).
3. LFU cache.
4. Distinct count in every window of size k.
5. Find all pairs with given xor.

## Challenge

Benchmark custom hashmap vs HashMap on large random input.

## Java 21 Exercise Example: Two Sum

```java
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwoSum {
    public static int[] twoSum(List<Integer> a, int target) {
        Map<Integer, Integer> pos = new HashMap<>();
        for (int i = 0; i < a.size(); i++) {
            int need = target - a.get(i);
            if (pos.containsKey(need)) return new int[]{pos.get(need), i};
            pos.put(a.get(i), i);
        }
        return new int[]{-1, -1};
    }
}
```
