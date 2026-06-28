# Chapter 10: Collections - Exercises

## Section 1: List Operations 🟢

1. Create an `ArrayList<Integer>` and implement: add, remove(last), size, isEmpty

2. Write a function to find max element in a list

3. Implement list element modification (set) and iteration

## Section 2: Deque Operations 🟡

4. Use an `ArrayDeque` for efficient front/back operations (addFirst/addLast/pollFirst/pollLast)

5. Implement queue-like behavior with a `Deque`

6. Compare `ArrayDeque` vs `ArrayList` performance for different operations

## Section 3: LinkedList Operations 🟡

7. Create a `LinkedList` and implement insertion in the middle via `ListIterator`

8. Write a function to reverse a list using `Collections.reverse`

9. Compare `LinkedList` performance vs `ArrayList` for middle operations

## Section 4: Set Operations 🟡

10. Create a `TreeSet` to store unique integers and demonstrate auto-sorting

11. Implement set intersection (`retainAll`) and union (`addAll`) operations

12. Use a `HashSet`/`TreeSet` for duplicate removal

## Section 5: Map Operations 🟡

13. Create a `Map` with `String` keys and `Integer` values

14. Implement a word frequency counter using `merge` or `getOrDefault`

15. Design a phone book system with a `Map`

## Section 6: Hash-based Containers 🟡

16. Compare performance: `TreeSet` vs `HashSet`

17. Create a hash table simulation with `HashMap`

18. Implement a caching system with `HashMap`

## Section 7: Container Adapters 🟡

19. Implement stack-based expression evaluation (use `Deque` as a stack)

20. Create a queue simulation for task scheduling (use `ArrayDeque`/`Queue`)

21. Design a `PriorityQueue` for event management

## Section 8: Iterator Usage 🟡

22. Iterate collections using `Iterator`

23. Implement bidirectional iteration with a `LinkedList` and `ListIterator`

24. Use iterator validity after modifications (observe `ConcurrentModificationException`)

## Section 9: Container Selection 🔴

25. Choose the appropriate collection for each scenario 🏆

26. Implement a custom `Comparator` for collection sorting

27. Design a system demonstrating all collection types

## Section 10: Collections + Algorithms Integration 🔴

28. Combine collections with `Collections` methods / Streams

29. Implement a transform operation on a collection (`Stream.map`)

30. Create a filtering system using multiple collections (`filter`/`collect`)

---

## Tips for Success

- **ArrayList**: General-purpose, random access needed
- **ArrayDeque**: Front/back operations needed
- **LinkedList**: Frequent middle insertions needed
- **TreeSet**: Unique sorted elements needed
- **TreeMap/HashMap**: Key-value pairs, sorting by key (TreeMap)
- **HashSet/HashMap**: Hash-based for speed
- **Collection traits**: Memory, access patterns, operations
- **Iterator categories**: Understand fail-fast behavior and limitations

## Difficulty Summary

- **Easy (🟢)**: 3 exercises - Basic list operations
- **Medium (🟡)**: 18 exercises - All collection types, adapters, iterators
- **Hard (🔴)**: 9 exercises - Collection selection, algorithm integration

## Challenge Problems 🏆

- **Challenge 1**: Choose optimal collections for a complex system
- **Challenge 2**: Custom comparators for sorting
- **Challenge 3**: Efficient collection usage with Streams/Collections

## Expected Time Commitment

- Easy: 10-15 minutes per exercise
- Medium: 20-40 minutes per exercise
- Hard: 30-60 minutes per exercise
- Total: 8-15 hours for all exercises

## Common Pitfalls to Avoid

- `ConcurrentModificationException` from modifying a collection during iteration (use `Iterator.remove`)
- Wrong collection for operation pattern
- Performance issues from poor collection choice
- Misreading return values (e.g. `Map.get` returns `null` if absent)
- Forgetting to implement `equals`/`hashCode` for custom keys
- Memory complexity vs time complexity trade-offs

## Learning Outcomes

After completing these exercises, you will:
✓ Master the core Java Collections Framework
✓ Understand when to use each collection
✓ Work with container adapters (Deque, Queue, PriorityQueue)
✓ Use iterators effectively (including fail-fast behavior)
✓ Optimize collection selection for performance
✓ Combine collections with Streams and Collections utilities
✓ Handle iterator validity and safe removal
✓ Design efficient data structures

## Java 21 Exercise Example: List + Map

```java
import java.util.*;
import java.util.stream.*;

public class Solution {
    static Map<Integer, Long> freq(List<Integer> a) {
        return a.stream()
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));
    }

    // Alternative using merge:
    static Map<Integer, Integer> freqMerge(List<Integer> a) {
        Map<Integer, Integer> m = new HashMap<>();
        for (int x : a) m.merge(x, 1, Integer::sum);
        return m;
    }
}
```

Compile and run with:

```
javac Solution.java
java Solution
```
