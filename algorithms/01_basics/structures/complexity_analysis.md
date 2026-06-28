# Complexity Analysis

## Concept

Complexity analysis describes how an algorithm's running time and memory grow as the input size `n` grows, ignoring constant factors and lower-order terms. Big-O gives an upper bound (worst case), Big-Omega a lower bound, and Big-Theta a tight bound. We count the dominant operations: a single loop over `n` items is `O(n)`, two nested loops are `O(n^2)`, repeatedly halving the input is `O(log n)`, and a sort is typically `O(n log n)`. The point of the analysis is to predict scalability and pick an approach that fits the constraints before writing code. Amortized analysis (e.g. an `ArrayList`'s `add` averaging `O(1)`) and space complexity (extra memory beyond the input) are part of the same toolkit.

## Mermaid

```mermaid
flowchart TD
    A[Count dominant operations as f(n)] --> B[Drop constants & lower-order terms]
    B --> C{Growth shape?}
    C -->|constant| D[O(1)]
    C -->|halving| E[O(log n)]
    C -->|single pass| F[O(n)]
    C -->|sort / n passes of log| G[O(n log n)]
    C -->|nested loops| H[O(n^2)]
    C -->|branch per element| I[O(2^n)]
```

## Complexity

Common growth classes from fastest to slowest, with a representative example:

| Big-O | Name | Example |
|-------|------|---------|
| O(1) | constant | array index, ArrayList.add (amortized) |
| O(log n) | logarithmic | binary search |
| O(n) | linear | linear search, single scan |
| O(n log n) | linearithmic | mergesort, sort + sweep |
| O(n^2) | quadratic | naive all-pairs, bubble sort |
| O(2^n) | exponential | subset enumeration |
| O(n!) | factorial | brute-force permutations |

## Java Code

```java
import java.util.Arrays;

public final class DuplicateDetection {

    // Two implementations of "does any value repeat?" to contrast complexity.

    // O(n^2) time, O(1) extra space: compare every pair.
    public static boolean hasDuplicateQuadratic(int[] a) {
        for (int i = 0; i < a.length; i++)
            for (int j = i + 1; j < a.length; j++)
                if (a[i] == a[j]) return true;
        return false;
    }

    // O(n log n) time (sort dominates), O(n) extra space for the copy.
    public static boolean hasDuplicateSorted(int[] input) {
        int[] a = Arrays.copyOf(input, input.length); // copy so we don't mutate the caller's array
        Arrays.sort(a);                               // O(n log n)
        for (int i = 1; i < a.length; i++)            // O(n) single pass
            if (a[i] == a[i - 1]) return true;        // duplicates are now adjacent
        return false;
    }
}
```

## Mini Usage Example

```java
int[] data = {4, 2, 8, 2};
boolean dupA = DuplicateDetection.hasDuplicateQuadratic(data); // true, O(n^2)
boolean dupB = DuplicateDetection.hasDuplicateSorted(data);    // true, O(n log n)
```

## Code Snippet Flow

```mermaid
flowchart LR
    A[Same problem: detect a duplicate] --> B[Quadratic: compare all pairs O(n^2)]
    A --> C[Sort then scan O(n log n)]
    B --> D[Trade time for low space]
    C --> E[Trade space for faster time]
```
