# Recursion Basics

## Concept

Recursion solves a problem by reducing it to a smaller instance of the same problem and combining the result. Every correct recursion needs a base case that stops the descent and a recursive case that makes progress toward that base case; without both you get infinite recursion and a stack overflow. Each call gets its own stack frame holding its parameters and locals, so the depth of recursion determines the space used. A useful mental model is the call tree: the leaves are base cases and each internal node combines its children's results. Recursion is natural for problems with self-similar structure (factorials, tree traversals, divide-and-conquer) and can always be rewritten iteratively with an explicit stack.

## Mermaid

```mermaid
flowchart TD
    A[Call f(n)] --> B{Base case?}
    B -->|yes| C[Return base value directly]
    B -->|no| D[Reduce: compute on smaller input f(n-1)]
    D --> E[Combine smaller result with current data]
    E --> F[Return combined result]
```

## Complexity

- For the factorial example below: Time `O(n)`, Space `O(n)` stack frames (recursion depth `n`).
- In general, cost = (number of calls) x (work per call); recursion depth sets the stack space.

## Java Code

```java
class RecursionBasics {
    // Factorial via recursion: n! = n * (n-1)!, with 0! = 1.
    static long factorial(int n) {
        if (n <= 1) return 1;                 // base case: stops the recursion
        return (long) n * factorial(n - 1);   // recursive case: smaller subproblem
    }

    // Sum of an array over [i, length): another reduce-and-combine recursion.
    static long sumFrom(int[] a, int i) {
        if (i == a.length) return 0;          // base case: empty suffix sums to 0
        return a[i] + sumFrom(a, i + 1);      // combine current element with rest
    }
}
```

## Mini Usage Example

```java
long f = RecursionBasics.factorial(5);   // 5! = 120
int[] a = {4, 2, 8, 1};
long s = RecursionBasics.sumFrom(a, 0);  // 4+2+8+1 = 15
```

## Code Snippet Flow

```mermaid
flowchart LR
    A[factorial(5)] --> B[5 * factorial(4)]
    B --> C[4 * factorial(3)]
    C --> D[3 * factorial(2)]
    D --> E[2 * factorial(1)]
    E --> F[base: returns 1]
    F --> G[Unwind: 2,6,24,120]
```
