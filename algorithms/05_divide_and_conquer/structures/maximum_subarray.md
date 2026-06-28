# Maximum Subarray

## Concept

The maximum-subarray problem asks for the largest possible sum of any contiguous subarray. Kadane's algorithm solves it in a single `O(n)` pass using the observation that the best subarray ending at index `i` is either `a[i]` alone or `a[i]` appended to the best subarray ending at `i-1`. We keep a running `bestEndingHere` and reset it to `a[i]` whenever the previous run would only drag the sum down (i.e. it went negative), while tracking the global best seen. The invariant is that `bestEndingHere` is always the maximum sum of a subarray that ends exactly at the current index. Initializing the answer to the first element (rather than 0) handles arrays where every element is negative. A divide-and-conquer formulation also exists at `O(n log n)` by combining best-left, best-right, and best-crossing sums.

## Mermaid

```mermaid
flowchart TD
    A[best = a[0]; cur = a[0]] --> B[i = 1]
    B --> C{i < n?}
    C -->|no| F[Return best]
    C -->|yes| D[cur = max(a[i], cur + a[i])]
    D --> E[best = max(best, cur)]
    E --> G[++i]
    G --> C
```

## Complexity

- Kadane's algorithm: Time `O(n)`, Space `O(1)`.
- Divide-and-conquer alternative: Time `O(n log n)`, Space `O(log n)` recursion.

## Java Code

```java
class MaximumSubarray {
    // Kadane's algorithm: maximum sum of any non-empty contiguous subarray.
    // Assumes a is non-empty.
    static long maxSubarray(int[] a) {
        long best = a[0];   // global best so far
        long cur  = a[0];   // best subarray sum ENDING at the current index
        for (int i = 1; i < a.length; i++) {
            // Either start fresh at a[i], or extend the previous run.
            cur = Math.max((long) a[i], cur + a[i]);
            best = Math.max(best, cur);          // update global maximum
        }
        return best;
    }
}
```

## Mini Usage Example

```java
int[] a = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
long ans = MaximumSubarray.maxSubarray(a); // subarray {4,-1,2,1} sums to 6
```

## Code Snippet Flow

```mermaid
flowchart LR
    A[cur = best = a[0]] --> B[Visit a[i]]
    B --> C{cur + a[i] vs a[i]}
    C -->|extend| D[cur = cur + a[i]]
    C -->|restart| E[cur = a[i]]
    D --> F[best = max(best, cur)]
    E --> F
    F --> G{More?}
    G -->|yes| B
    G -->|no| H[Return best]
```
