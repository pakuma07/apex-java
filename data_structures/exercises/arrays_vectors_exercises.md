# Exercises: Arrays and Vectors

## Easy

1. Find maximum and minimum element in an array.
2. Reverse an array in-place.
3. Rotate array right by k positions.
4. Remove duplicates from a sorted list.
5. Merge two sorted arrays.

## Medium

1. Find second largest element in one pass.
2. Implement dynamic array resize logic manually.
3. Compute prefix sums and answer range sum queries.
4. Rearrange array by sign (positive/negative alternating).
5. Find majority element (Boyer-Moore).

## Hard

1. Maximum subarray sum (Kadane + index reconstruction).
2. Count inversions in O(n log n).
3. Trapping rain water.
4. Product of array except self without division.
5. Longest consecutive sequence.

## Challenge

Implement a mini dynamic-array class with:
- add (push_back)
- removeLast (pop_back)
- reserve
- get/set (operator[])
- size/capacity

## Java 21 Exercise Example: Prefix Sum Queries

```java
import java.util.List;

public class PrefixSum {
    public static long[] buildPref(List<Integer> a) {
        long[] pref = new long[a.size() + 1];
        for (int i = 0; i < a.size(); i++) pref[i + 1] = pref[i] + a.get(i);
        return pref;
    }
}
```
