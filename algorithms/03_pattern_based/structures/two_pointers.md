# Two Pointers

## Concept

The two-pointers technique uses two indices that move through a sequence to replace an `O(n^2)` brute-force search with an `O(n)` scan. The most common form runs the pointers inward from both ends of a sorted array: if the pair sum is too small, advance the left pointer to increase it; if too large, retreat the right pointer to decrease it. The invariant is that every pair worth considering still lies within `[l, r]`, so no valid answer is skipped. Other variants move both pointers in the same direction (fast/slow). The technique applies when the data is sorted or has monotonic structure that lets you safely discard one side after each comparison.

## Mermaid

```mermaid
flowchart TD
    A[Sort array; l=0, r=n-1] --> B{l < r?}
    B -->|no| F[Return: no pair found]
    B -->|yes| C[s = a[l] + a[r]]
    C --> D{s vs target}
    D -->|s == target| E[Return pair found]
    D -->|s < target| G[++l: need a larger sum]
    D -->|s > target| H[--r: need a smaller sum]
    G --> B
    H --> B
```

## Complexity

- Time: `O(n)` for the scan (assumes the array is already sorted; add `O(n log n)` if you must sort first).
- Space: `O(1)` extra.

## Java Code

```java
class TwoPointers {
    // Returns indices {i, j} with a[i] + a[j] == target on a SORTED ascending
    // array, or {-1, -1} if no such pair exists.
    static int[] twoSumSorted(int[] a, int target) {
        int l = 0, r = a.length - 1;
        while (l < r) {                       // invariant: any valid pair lies within [l, r]
            int s = a[l] + a[r];
            if (s == target) return new int[] {l, r};
            if (s < target) l++;              // sum too small -> need a bigger left value
            else            r--;              // sum too large -> need a smaller right value
        }
        return new int[] {-1, -1};            // pointers crossed: no pair sums to target
    }
}
```

## Mini Usage Example

```java
int[] a = {1, 3, 4, 5, 7, 11};       // must be sorted ascending
int[] p = TwoPointers.twoSumSorted(a, 9); // a[1]+a[4] = 3+7 = 9 -> {1, 4}
```

## Code Snippet Flow

```mermaid
flowchart LR
    A[l at front, r at back] --> B[Compute a[l]+a[r]]
    B --> C{Compare to target}
    C -->|equal| D[Return (l, r)]
    C -->|less| E[++l]
    C -->|greater| F[--r]
    E --> B
    F --> B
```
