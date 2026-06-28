# Chapter 11: Streams & Collections Algorithms - Exercises

## Section 1: Find Algorithms 🟢

1. Use `stream().filter(...).findFirst()` to locate the first occurrence of a value

2. Implement a search with a custom predicate (`anyMatch` / `filter`)

3. Use `count()` and `filter().count()` for counting

## Section 2: Sort Algorithms 🟡

4. Sort a list ascending and descending (`List.sort` / `stream().sorted`)

5. Create a custom `Comparator` for sorting objects

6. Implement top-N selection via `sorted(...).limit(n)`

## Section 3: Transform Algorithms 🟡

7. Use `Stream.map` to apply a function to all elements

8. Square all elements in a list

9. Convert a `String` list to uppercase with `map(String::toUpperCase)`

## Section 4: Copy Algorithms 🟡

10. Collect stream elements into another collection

11. Implement conditional copy with `filter().collect()`

12. Use a filter to exclude specific elements (analog of `remove_copy`)

## Section 5: forEach Algorithm 🟡

13. Use `forEach` to apply an action to elements

14. Process a collection with a lambda in `forEach`

15. Accumulate a result (note: prefer `reduce` over mutating captured state)

## Section 6: Numeric Algorithms 🟡

16. Calculate a sum using `IntStream.sum` / `mapToInt`

17. Implement product calculation with `reduce`

18. Create a custom `BinaryOperator` for `reduce`

## Section 7: Fill and Replace 🟡

19. Fill a list with a value (`Collections.fill`)

20. Replace specific values in a list (`replaceAll` with a `UnaryOperator`)

21. Implement conditional replacement with a predicate

## Section 8: Reverse and Rotate 🟡

22. Reverse list elements (`Collections.reverse`)

23. Implement circular rotation (`Collections.rotate`)

24. Compare a list before and after reversing

## Section 9: Distinct and Remove 🔴

25. Remove adjacent duplicates

26. Implement filtering removal with `removeIf` (Java's replacement for the C++ erase-remove idiom) 🏆

27. Remove conditionally with `removeIf` / `distinct()`

## Section 10: Algorithm Combinations 🔴

28. Chain multiple stream operations for complex operations

29. Create a pipeline: filter -> map -> reduce/collect 🏆

30. Design a sorting and searching system combining algorithms

---

## Tips for Success

- **Intermediate vs terminal**: Know which ops are lazy and which trigger evaluation
- **Stateless lambdas**: Predicates and mappers should avoid shared mutable state
- **removeIf**: Single-call replacement for the C++ erase-remove idiom
- **In-place vs new stream**: Choose `List` mutation vs a fresh collection wisely
- **Lambda predicates**: Powerful with method references and `Predicate`
- **Performance**: Understand time complexity and short-circuiting (`findFirst`, `anyMatch`)
- **Side effects**: Use `forEach` carefully; prefer `reduce`/`collect`
- **Numeric streams**: Use `IntStream`/`LongStream`/`DoubleStream`

## Difficulty Summary

- **Easy (🟢)**: 3 exercises - Find and basic operations
- **Medium (🟡)**: 18 exercises - All algorithm categories
- **Hard (🔴)**: 9 exercises - Complex combinations, optimization

## Challenge Problems 🏆

- **Challenge 1**: `removeIf` mastery (replacing erase-remove)
- **Challenge 2**: Complex stream pipelines
- **Challenge 3**: Optimized algorithm selection

## Expected Time Commitment

- Easy: 10-15 minutes per exercise
- Medium: 20-40 minutes per exercise
- Hard: 30-60 minutes per exercise
- Total: 8-15 hours for all exercises

## Common Pitfalls to Avoid

- Expecting an intermediate op to run without a terminal op (lazy evaluation)
- Reusing a stream after it has been consumed
- Mutating shared state inside lambdas (use `reduce`/`collect`)
- Predicate side effects
- `ConcurrentModificationException` when mutating a source during iteration
- Forgetting `removeIf` and writing manual error-prone removal loops
- Performance issues from poor algorithm choice

## Learning Outcomes

After completing these exercises, you will:
✓ Master the major Stream and Collections operations
✓ Combine stream operations effectively
✓ Write efficient predicates and mapping functions
✓ Understand operation complexity and short-circuiting
✓ Use `removeIf`/`distinct` correctly
✓ Chain operations for complex pipelines
✓ Optimize algorithm selection
✓ Work seamlessly with Java 21 lambdas and method references

## Java 21 Exercise Example: Collections.sort + binarySearch

```java
import java.util.*;

public class Solution {
    static int firstAtLeast(List<Integer> a, int x) {
        List<Integer> sorted = new ArrayList<>(a);
        Collections.sort(sorted);
        int idx = Collections.binarySearch(sorted, x);
        if (idx >= 0) {
            // exact match found; walk back to first equal element if needed
            return idx;
        }
        int insertionPoint = -(idx) - 1; // -(insertion point) - 1
        return insertionPoint < sorted.size() ? insertionPoint : -1;
    }
}
```

Compile and run with:

```
javac Solution.java
java Solution
```
