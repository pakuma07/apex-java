# Insertion Sort

## Concept

Insertion Sort builds the sorted array one element at a time, the way you sort a hand of playing cards. It takes the next element (the "key") and shifts every larger element in the already-sorted prefix one slot to the right, then drops the key into the gap. The invariant is that `a[0..i-1]` is always sorted before element *i* is inserted. It is stable, in place, and adaptive: on nearly-sorted data it runs in close to O(n), which is why it is used for small arrays and as the base case inside fast sorts like introsort/Timsort.

## Mermaid

```mermaid
flowchart TD
    A[Array, i = 1] --> B{i < n?}
    B -- No --> Z[Sorted]
    B -- Yes --> C[key = a i]
    C --> D[Compare key with sorted prefix right-to-left]
    D --> E{element > key?}
    E -- Yes --> F[Shift element right]
    F --> D
    E -- No --> G[Insert key into the gap]
    G --> H[i = i + 1]
    H --> B
```

## Complexity

- Time (Best): O(n) — already sorted, inner loop never shifts
- Time (Average): O(n^2)
- Time (Worst): O(n^2) — reverse-sorted input
- Space: O(1) — in place
- Stable: Yes

## Java Code

```java
public final class InsertionSort {

    public static void insertionSort(int[] a) {
        int n = a.length;
        for (int i = 1; i < n; i++) {          // a[0..i-1] is already sorted
            int key = a[i];                    // element to insert
            int j = i - 1;
            // Shift every element greater than key one slot to the right
            // to open a gap at the correct insertion point.
            while (j >= 0 && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;                    // drop key into the gap
        }
    }
}
```

## Mini Usage Example

```java
int[] a = {5, 1, 4, 2, 8};
InsertionSort.insertionSort(a);
// a is now {1, 2, 4, 5, 8}
```

## Code Snippet Flow

```mermaid
flowchart LR
    A[i = 1] --> B{i < n?}
    B -- Yes --> C[key = a i, j = i-1]
    C --> D{j >= 0 and a j > key?}
    D -- Yes --> E[a j+1 = a j, j--]
    E --> D
    D -- No --> F[a j+1 = key]
    F --> G[i++]
    G --> B
    B -- No --> Z[done]
```
