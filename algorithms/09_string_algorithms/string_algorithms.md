# String Algorithms

This chapter has been split into micro-files.

## Structure Files

1. [Naive Pattern Matching](structures/naive_pattern_matching.md)
2. [KMP](structures/kmp.md)
3. [Z Algorithm](structures/z_algorithm.md)
4. [Rolling Hash](structures/rolling_hash.md)
5. [String Algorithm Overview](structures/string_algorithm_overview.md)

## Practice

- ../exercises/string_algorithms_exercises.md

---

## Next Steps

- Review the cheat sheet: [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md)
- Previous: [Chapter 8: Graph Algorithms](../08_graph_algorithms/graph_algorithms.md)
- Next: [Chapter 10: Number Theory and Bit Algorithms](../10_number_theory_bit/number_theory_bit.md)

## Java Example: Prefix Function (KMP)

```java
public final class PrefixFunction {
    public static int[] prefixFunction(String s) {
        int[] pi = new int[s.length()];
        for (int i = 1; i < s.length(); i++) {
            int j = pi[i - 1];
            while (j > 0 && s.charAt(i) != s.charAt(j)) j = pi[j - 1];
            if (s.charAt(i) == s.charAt(j)) j++;
            pi[i] = j;
        }
        return pi;
    }
}
```
