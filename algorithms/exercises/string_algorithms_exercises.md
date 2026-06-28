# Exercises: String Algorithms

## Easy

1. Naive pattern matching.
2. Prefix function/LPS array construction.
3. Check if one string is substring of another.
4. Count occurrences of a pattern.
5. Find longest common prefix of strings.

## Medium

1. KMP search.
2. Z algorithm pattern match.
3. Rabin-Karp rolling hash search.
4. Group anagrams.
5. Longest palindromic substring.

## Hard

1. Shortest palindrome using prefix ideas.
2. Wildcard matching.
3. Build suffix array overview.
4. Distinct substrings count.
5. Minimum window substring.

## Challenge

Compare naive matching, KMP, and rolling hash on the same corpus and explain the runtime differences.

---

## Next Steps

- Read the matching theory: [../09_string_algorithms/string_algorithms.md](../09_string_algorithms/string_algorithms.md)
- Previous: [Graph Algorithms Exercises](graph_algorithms_exercises.md)
- Next: [Number Theory and Bit Exercises](number_theory_bit_exercises.md)

## Java 21 Exercise Example: Naive Pattern Match

```java
import java.util.*;

public class NaivePatternMatch {
    static List<Integer> findAll(String text, String pat) {
        List<Integer> pos = new ArrayList<>();
        for (int i = 0; i + pat.length() <= text.length(); i++)
            if (text.substring(i, i + pat.length()).equals(pat)) pos.add(i);
        return pos;
    }
}
```
