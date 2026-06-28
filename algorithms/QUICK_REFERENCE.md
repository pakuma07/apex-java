# Algorithms Quick Reference

A compact one-page cheat sheet for algorithm paradigms, common recurrences, typical complexities, and selection hints.

## Paradigms

| Paradigm | Core Idea | Typical Signal | Example |
|----------|-----------|----------------|---------|
| Brute Force | Try all possibilities | Small constraints | All subsets |
| Divide and Conquer | Split, solve, combine | Recursive split structure | Merge sort |
| Greedy | Best local choice | Exchange-argument possible | Activity selection |
| Dynamic Programming | Reuse overlapping subproblems | Same states recur | Knapsack |
| Backtracking | Explore and undo choices | Enumerating valid solutions | N-Queens |
| Graph Traversal | Visit nodes/edges systematically | Connectivity/path tasks | BFS, DFS |
| Binary Search | Halve answer/search space | Monotonic condition | Lower bound |
| Prefix / Window | Incremental range computation | Contiguous ranges | Sliding window |
| Bitmasking | Compact subset state | Small-n subsets | TSP DP |

## When To Use What

| If the problem looks like... | Use... |
|------------------------------|--------|
| Sorted data, exact/first/last position | Binary search |
| Contiguous subarray/window | Sliding window or prefix sum |
| Pair search in sorted array | Two pointers |
| Need all combinations/permutations | Backtracking |
| Same subproblem repeats | Dynamic programming |
| Need shortest path in unweighted graph | BFS |
| Need shortest path with non-negative weights | Dijkstra |
| Need all-pairs shortest paths | Floyd-Warshall |
| Need max non-overlapping selections | Greedy |
| Need fast exact string matching | KMP / Z algorithm |
| Need prime generation up to n | Sieve |

## Common Recurrences

### Binary Search
$$
T(n) = T(n/2) + O(1) = O(\log n)
$$

### Merge Sort
$$
T(n) = 2T(n/2) + O(n) = O(n \log n)
$$

### Quick Sort Average
$$
T(n) = T(k) + T(n-k-1) + O(n)\ \text{(average } O(n\log n)\text{)}
$$

### Fibonacci DP
$$
F(n) = F(n-1) + F(n-2)
$$

### 0/1 Knapsack
$$
dp[i][w] = \max(dp[i-1][w],\ value_i + dp[i-1][w-weight_i])
$$

### LCS
$$
dp[i][j] =
\begin{cases}
1 + dp[i-1][j-1] & \text{if } a[i-1] = b[j-1] \\
\max(dp[i-1][j], dp[i][j-1]) & \text{otherwise}
\end{cases}
$$

## Complexity Table

| Algorithm | Time | Space |
|-----------|------|-------|
| Linear Search | O(n) | O(1) |
| Binary Search | O(log n) | O(1) |
| Bubble / Selection / Insertion Sort | O(n^2) | O(1) |
| Merge Sort | O(n log n) | O(n) |
| Quick Sort | O(n log n) avg, O(n^2) worst | O(log n) avg stack |
| Heap Sort | O(n log n) | O(1) |
| BFS / DFS | O(V + E) | O(V) |
| Dijkstra (heap) | O((V + E) log V) | O(V) |
| Bellman-Ford | O(VE) | O(V) |
| Floyd-Warshall | O(V^3) | O(V^2) |
| KMP | O(n + m) | O(m) |
| Sieve | O(n log log n) | O(n) |
| Fast Power | O(log e) | O(1) |
| 0/1 Knapsack | O(nW) | O(nW) |
| LIS | O(n^2) or O(n log n) | O(n) |

## Sorting Selection

| Situation | Good Choice |
|-----------|-------------|
| Need stable O(n log n) | Merge sort |
| Need in-place fast average sort | Quick sort |
| Need guaranteed O(n log n) and in-place | Heap sort |
| Nearly sorted input | Insertion sort |
| Tiny input / teaching basics | Bubble or selection sort |

## Graph Algorithm Selection

| Goal | Algorithm |
|------|-----------|
| Reachability / components | DFS / BFS |
| Unweighted shortest path | BFS |
| Weighted shortest path, non-negative | Dijkstra |
| Negative edges, single source | Bellman-Ford |
| All-pairs shortest path | Floyd-Warshall |
| DAG ordering | Topological sort |
| Minimum spanning tree | Kruskal / Prim |
| SCC decomposition | Kosaraju / Tarjan |

## DP Checklist

1. Define the state precisely.
2. Write the transition.
3. Identify base cases.
4. Choose memoization or tabulation.
5. Check iteration order dependencies.
6. Optimize space only after correctness.

## Backtracking Template

```java
void solve(State state) {
    if (isComplete(state)) {
        record(state);
        return;
    }
    for (Choice choice : choices(state)) {
        apply(state, choice);
        solve(state);
        undo(state, choice);
    }
}
```

## Binary Search On Answer Checklist

- Search space is ordered.
- Feasibility predicate is monotonic.
- You know whether you need first true or last true.
- Boundaries are valid and tested.

## Common Red Flags

- Recursion repeating identical subproblems: use DP.
- Negative edge weights with Dijkstra: wrong algorithm.
- Need exact contiguous segment info: think prefix sum/window.
- Need subset optimization with small n: think bitmask DP.
- Need all answers, not one optimum: greedy often insufficient.

## Study Order

1. Searching and sorting
2. Pattern-based arrays/strings
3. Recursion and backtracking
4. Divide and conquer
5. Greedy
6. Dynamic programming
7. Graph algorithms
8. String algorithms
9. Number theory / bit
10. Advanced algorithms
