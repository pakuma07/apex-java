# Hash Tables

This chapter has been split into micro-files.

## Structure Files

1. [Hash Table: Separate Chaining](structures/hash_table_chaining.md)
2. [Hash Table: Open Addressing](structures/hash_table_open_addressing.md)
3. [HashMap and HashSet](structures/unordered_map_unordered_set.md)
4. [Hashing Complexity Table](structures/hashing_complexity_table.md)

## Practice

- ../exercises/hash_tables_exercises.md

## Java 21 Example: Frequency Map

```java
import java.util.HashMap;
import java.util.List;
import java.util.Map;

static Map<Integer, Integer> freqCount(List<Integer> a) {
    Map<Integer, Integer> freq = new HashMap<>();
    for (int x : a) freq.merge(x, 1, Integer::sum);
    return freq;
}
```
