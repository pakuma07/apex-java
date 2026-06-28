# Algorithms in Java 21

A comprehensive end-to-end algorithms roadmap covering fundamental, intermediate, and advanced algorithmic techniques.

## What This Module Includes

- Concept-first explanations
- Mermaid diagrams for intuition
- Java 21 implementations
- Complexity analysis
- Suggested learning order

## Directory Structure

```
algorithms/
├── README.md
├── QUICK_REFERENCE.md
├── 01_basics/
│   ├── algorithmic_thinking.md
│   └── structures/
│       ├── problem_solving_pipeline.md
│       ├── complexity_analysis.md
│       └── correctness_and_invariants.md
├── 02_searching_sorting/
│   ├── searching.md
│   ├── sorting.md
│   └── structures/
│       ├── linear_search.md
│       ├── binary_search.md
│       ├── lower_bound.md
│       ├── bubble_sort.md
│       ├── selection_sort.md
│       ├── insertion_sort.md
│       ├── merge_sort.md
│       ├── quick_sort.md
│       ├── heap_sort.md
│       └── sorting_complexity_table.md
├── 03_pattern_based/
│   ├── two_pointers_sliding_window_prefix.md
│   └── structures/
│       ├── two_pointers.md
│       ├── sliding_window.md
│       └── prefix_sum.md
├── 04_recursion_backtracking/
│   ├── recursion_backtracking.md
│   └── structures/
│       ├── recursion_basics.md
│       └── backtracking.md
├── 05_divide_and_conquer/
│   ├── divide_and_conquer.md
│   └── structures/
│       ├── divide_and_conquer_paradigm.md
│       └── maximum_subarray.md
├── 06_greedy/
│   ├── greedy_algorithms.md
│   └── structures/
│       ├── greedy_choice_property.md
│       ├── activity_selection.md
│       └── greedy_patterns.md
├── 07_dynamic_programming/
│   ├── dynamic_programming.md
│   └── structures/
│       ├── memoization_vs_tabulation.md
│       ├── fibonacci_dp.md
│       ├── knapsack_01.md
│       ├── longest_increasing_subsequence.md
│       ├── longest_common_subsequence.md
│       └── dp_patterns.md
├── 08_graph_algorithms/
│   ├── graph_algorithms.md
│   └── structures/
│       ├── traversals.md
│       ├── dijkstra.md
│       ├── bellman_ford.md
│       ├── floyd_warshall.md
│       ├── topological_sort.md
│       └── minimum_spanning_tree.md
├── 09_string_algorithms/
│   ├── string_algorithms.md
│   └── structures/
│       ├── naive_pattern_matching.md
│       ├── kmp.md
│       ├── z_algorithm.md
│       ├── rolling_hash.md
│       └── string_algorithm_overview.md
├── 10_number_theory_bit/
│   ├── number_theory_bit.md
│   └── structures/
│       ├── prime_checking.md
│       ├── sieve.md
│       ├── gcd_lcm.md
│       ├── fast_exponentiation.md
│       ├── bit_operations.md
│       ├── bitmask_subsets.md
│       └── number_bit_overview.md
├── 11_advanced_algorithms/
│   ├── advanced_algorithms.md
│   └── structures/
│       ├── floyd_warshall.md
│       ├── network_flow.md
│       ├── strongly_connected_components.md
│       ├── binary_lifting.md
│       ├── convex_hull.md
│       ├── bitmask_dp.md
│       └── advanced_overview.md
├── exercises/
│   ├── README.md
│   ├── basics_exercises.md
│   ├── searching_exercises.md
│   ├── sorting_exercises.md
│   ├── pattern_based_exercises.md
│   ├── recursion_backtracking_exercises.md
│   ├── divide_and_conquer_exercises.md
│   ├── greedy_exercises.md
│   ├── dynamic_programming_exercises.md
│   ├── graph_algorithms_exercises.md
│   ├── string_algorithms_exercises.md
│   ├── number_theory_bit_exercises.md
│   └── advanced_algorithms_exercises.md
└── code_examples/
    ├── README.md
    ├── SearchingSorting.java
    ├── PatternBased.java
    ├── RecursionBacktracking.java
    ├── DivideAndConquer.java
    ├── Greedy.java
    ├── DynamicProgramming.java
    ├── GraphAlgorithms.java
    ├── StringAlgorithms.java
    ├── NumberTheoryBit.java
    └── AdvancedAlgorithms.java
```

## Learning Path

1. Basics of complexity, correctness, and proof ideas
2. Searching and sorting
3. Pattern-based array/string techniques
4. Recursion and backtracking
5. Divide and conquer
6. Greedy methods
7. Dynamic programming
8. Graph algorithms
9. String algorithms
10. Number theory and bit manipulation
11. Advanced algorithms

## Core Algorithms Covered

### Basic
- Linear search, binary search
- Bubble, selection, insertion sort
- Merge sort, quick sort
- Prefix sums, two pointers, sliding window
- BFS, DFS

### Intermediate
- Heap sort, counting/radix overview
- Topological sort
- Dijkstra, Bellman-Ford
- Union-Find applications
- KMP, Z algorithm, rolling hash
- Classic DP patterns

### Advanced
- Floyd-Warshall
- SCC (Kosaraju/Tarjan overview)
- Network flow (Edmonds-Karp)
- Binary lifting overview
- Convex hull overview
- Bitmask DP overview

## Complexity Reference

| Family | Typical Complexity |
|--------|---------------------|
| Binary Search | O(log n) |
| Comparison Sorting | O(n log n) |
| Graph Traversal | O(V + E) |
| Dijkstra (heap) | O((V + E) log V) |
| DP (1D/2D common) | O(n) to O(n^2) |
| String matching (KMP) | O(n + m) |

## Compilation

From `algorithms/code_examples/`:

```bash
javac SearchingSorting.java && java SearchingSorting
javac DynamicProgramming.java && java DynamicProgramming
javac GraphAlgorithms.java && java GraphAlgorithms
```

## Notes

- Focus is practical algorithm design with Java 21.
- For production, use the Java Collections Framework and tested libraries where suitable.
- For interviews/contests, understand manual implementation deeply.

## Quick Reference

- [QUICK_REFERENCE.md](QUICK_REFERENCE.md) for paradigms, common recurrences, complexity tables, and selection hints.

## Exercises

Chapter-style exercise sets are available under `algorithms/exercises/` with Easy, Medium, Hard, and Challenge sections.

## Layout

Larger algorithm categories now follow a consistent pattern:

- Chapter index file
- `structures/` micro-files per algorithm or technique
- `exercises/` chapter-style practice set

Runnable examples are indexed in `algorithms/code_examples/README.md`.


## Java 21 Algorithms Hub Example

```java
public final class AlgorithmsHub {
    public static int maxSubarrayKadane(int[] a) {
        if (a.length == 0) return 0;
        int best = a[0], cur = best;
        for (int i = 1; i < a.length; i++) {
            cur = Math.max(a[i], cur + a[i]);
            best = Math.max(best, cur);
        }
        return best;
    }
}
```
