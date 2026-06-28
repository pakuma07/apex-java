# Algorithms Code Examples

Runnable Java 21 examples for the algorithms module.

> Note: each file's public class name matches its (snake_case) filename, e.g.
> `searching_sorting.java` declares `public class searching_sorting`. Compile and
> run each example from this directory.

## Navigation

- Start at [Searching and Sorting Example](#1-searching_sortingjava)
- Pair examples with [../README.md](../README.md)
- Keep [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md) open while reviewing

## Files

1. `searching_sorting.java`
   - Linear search
   - Binary search
   - Merge sort

   Next steps:
   - Matching theory: [../02_searching_sorting/searching.md](../02_searching_sorting/searching.md)
   - Matching theory: [../02_searching_sorting/sorting.md](../02_searching_sorting/sorting.md)
   - Matching exercises: [../exercises/searching_exercises.md](../exercises/searching_exercises.md)
   - Matching exercises: [../exercises/sorting_exercises.md](../exercises/sorting_exercises.md)
   - Next example: `pattern_based.java`

2. `pattern_based.java`
   - Two pointers
   - Sliding window

   Next steps:
   - Matching theory: [../03_pattern_based/two_pointers_sliding_window_prefix.md](../03_pattern_based/two_pointers_sliding_window_prefix.md)
   - Matching exercises: [../exercises/pattern_based_exercises.md](../exercises/pattern_based_exercises.md)
   - Previous example: `searching_sorting.java`
   - Next example: `recursion_backtracking.java`

3. `recursion_backtracking.java`
   - Recursive factorial
   - Subset generation

   Next steps:
   - Matching theory: [../04_recursion_backtracking/recursion_backtracking.md](../04_recursion_backtracking/recursion_backtracking.md)
   - Matching exercises: [../exercises/recursion_backtracking_exercises.md](../exercises/recursion_backtracking_exercises.md)
   - Previous example: `pattern_based.java`
   - Next example: `divide_and_conquer.java`

4. `divide_and_conquer.java`
   - Maximum subarray via divide and conquer

   Next steps:
   - Matching theory: [../05_divide_and_conquer/divide_and_conquer.md](../05_divide_and_conquer/divide_and_conquer.md)
   - Matching exercises: [../exercises/divide_and_conquer_exercises.md](../exercises/divide_and_conquer_exercises.md)
   - Previous example: `recursion_backtracking.java`
   - Next example: `greedy.java`

5. `greedy.java`
   - Activity selection

   Next steps:
   - Matching theory: [../06_greedy/greedy_algorithms.md](../06_greedy/greedy_algorithms.md)
   - Matching exercises: [../exercises/greedy_exercises.md](../exercises/greedy_exercises.md)
   - Previous example: `divide_and_conquer.java`
   - Next example: `dynamic_programming.java`

6. `dynamic_programming.java`
   - Fibonacci tabulation
   - 0/1 knapsack

   Next steps:
   - Matching theory: [../07_dynamic_programming/dynamic_programming.md](../07_dynamic_programming/dynamic_programming.md)
   - Matching exercises: [../exercises/dynamic_programming_exercises.md](../exercises/dynamic_programming_exercises.md)
   - Previous example: `greedy.java`
   - Next example: `graph_algorithms.java`

7. `graph_algorithms.java`
   - Dijkstra shortest path

   Next steps:
   - Matching theory: [../08_graph_algorithms/graph_algorithms.md](../08_graph_algorithms/graph_algorithms.md)
   - Matching exercises: [../exercises/graph_algorithms_exercises.md](../exercises/graph_algorithms_exercises.md)
   - Previous example: `dynamic_programming.java`
   - Next example: `string_algorithms.java`

8. `string_algorithms.java`
   - KMP LPS construction

   Next steps:
   - Matching theory: [../09_string_algorithms/string_algorithms.md](../09_string_algorithms/string_algorithms.md)
   - Matching exercises: [../exercises/string_algorithms_exercises.md](../exercises/string_algorithms_exercises.md)
   - Previous example: `graph_algorithms.java`
   - Next example: `number_theory_bit.java`

9. `number_theory_bit.java`
   - Fast exponentiation
   - Sieve of Eratosthenes

   Next steps:
   - Matching theory: [../10_number_theory_bit/number_theory_bit.md](../10_number_theory_bit/number_theory_bit.md)
   - Matching exercises: [../exercises/number_theory_bit_exercises.md](../exercises/number_theory_bit_exercises.md)
   - Previous example: `string_algorithms.java`
   - Next example: `advanced_algorithms.java`

10. `advanced_algorithms.java`
   - Floyd-Warshall

   Next steps:
   - Matching theory: [../11_advanced_algorithms/advanced_algorithms.md](../11_advanced_algorithms/advanced_algorithms.md)
   - Matching exercises: [../exercises/advanced_algorithms_exercises.md](../exercises/advanced_algorithms_exercises.md)
   - Previous example: `number_theory_bit.java`
   - Return to [Algorithms Module Overview](../README.md)

## Compilation

Compile and run each example (Java 21). The class name matches the filename, so
run with the bare stem (no `.java`):

```bash
javac searching_sorting.java       && java searching_sorting
javac pattern_based.java           && java pattern_based
javac recursion_backtracking.java  && java recursion_backtracking
javac divide_and_conquer.java      && java divide_and_conquer
javac greedy.java                  && java greedy
javac dynamic_programming.java     && java dynamic_programming
javac graph_algorithms.java        && java graph_algorithms
javac string_algorithms.java       && java string_algorithms
javac number_theory_bit.java       && java number_theory_bit
javac advanced_algorithms.java     && java advanced_algorithms
```

You can also compile everything at once:

```bash
javac *.java
```

Java 21 additionally lets you run a single-file source directly without a
separate compile step:

```bash
java searching_sorting.java
```

## Suggested Usage

- Read the matching chapter first.
- Compile and run the example.
- Modify inputs and test edge cases.
- Reimplement from memory afterward.

---

## Flow

- Theory path: [../README.md](../README.md)
- Practice path: [../exercises/README.md](../exercises/README.md)
- Quick cheat sheet: [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md)

## Java 21 Example Selector

```java
public class Selector {
    public static void main(String[] args) {
        System.out.println("Try: javac graph_algorithms.java");
        System.out.println("Then run: java graph_algorithms");
    }
}
```
