# Data Structures Exercises

Chapter-style exercise sets for each data structure topic.

## How to Use

1. Read the theory chapter first.
2. Solve Easy -> Medium -> Hard in order.
3. Implement in Java 21 without using built-in Collections shortcuts first.
4. Then solve again with the Java Collections Framework for comparison.

## Exercise Chapters

1. arrays_vectors_exercises.md
2. linked_lists_exercises.md
3. stack_queue_deque_exercises.md
4. hash_tables_exercises.md
5. binary_tree_exercises.md
6. bst_exercises.md
7. avl_tree_exercises.md
8. heap_exercises.md
9. trie_exercises.md
10. graph_representation_exercises.md
11. graph_traversal_exercises.md
12. dsu_exercises.md
13. fenwick_tree_exercises.md
14. segment_tree_exercises.md

## Suggested Pace

- 1 chapter per day
- Revisit failed problems after 48 hours
- Keep a notebook of edge cases and bugs

## Java 21 Exercise Driver

```java
import java.io.*;
import java.util.*;

public class Main {
    static void solve(BufferedReader in, PrintWriter out) throws IOException {
        // Implement one selected data-structure exercise.
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
        solve(in, out);
        out.flush();
    }
}
```
