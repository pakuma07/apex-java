# Arrays and Vectors

This chapter has been split into micro-files.

## Structure Files

1. [Array](structures/array.md)
2. [Vector](structures/vector.md)
3. [Linear Complexity Table](structures/linear_complexity_table.md)

## Practice

- ../exercises/arrays_vectors_exercises.md

## Java 21 Example: Dynamic Array Usage

```java
import java.util.ArrayList;
import java.util.List;

static List<Integer> buildArray(int n) {
    List<Integer> a = new ArrayList<>();
    for (int i = 0; i < n; i++) a.add(i * i);
    return a;
}
```
