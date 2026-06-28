# Chapter 5: Arrays & Strings - Practice Exercises

## 1. Basic Arrays

### Exercise 1.1: Array Declaration & Access
Create program that:
- Declares `int[] arr = new int[5]`
- Initializes with values
- Prints all elements using loop
- Access specific elements by index
- Modify elements

### Exercise 1.2: Array Input & Output
Write program to:
- Read n integers from user (use `Scanner`)
- Store in array
- Calculate and print: sum, average, max, min
- Count positive and negative numbers

### Exercise 1.3: Array Size & Bounds
Create program with:
- Get array length using `arr.length`
- Test boundary conditions
- Demonstrate out-of-bounds access (throws `ArrayIndexOutOfBoundsException` — checked, not undefined behavior)
- Discuss proper bounds checking

## 2. Multi-dimensional Arrays

### Exercise 2.1: 2D Array Basics
Given 3×3 matrix (`int[][] m`):
- Initialize with values
- Print all elements
- Access diagonal elements
- Calculate sum of row, column, main diagonal

### Exercise 2.2: Matrix Operations
Create two 2×2 matrices:
- Add matrices (A + B)
- Multiply matrices (A × B)
- Transpose matrix
- Display results

### Exercise 2.3: 3D Array
Create program with:
- 2×3×4 3D array (`int[][][]`)
- Initialize with values
- Access specific elements
- Calculate total sum
- Find maximum value

## 3. Copying & Comparing Arrays

### Exercise 3.1: Array Copy
Write methods:
- `void copy(int[] src, int[] dst, int size)` - copy elements
- Verify copy independence (changing one doesn't affect other)
- Use both a loop and library methods (`System.arraycopy`, `Arrays.copyOf`)

### Exercise 3.2: Array Comparison
Create methods:
- `boolean areEqual(int[] arr1, int[] arr2, int size)`
- `int findDifference(int[] arr1, int[] arr2, int size)` - first index where different
- Test with various arrays (compare with `Arrays.equals`)

### Exercise 3.3: Array Search
Implement:
- `int linearSearch(int[] arr, int size, int target)` - returns index
- `int binarySearch(int[] arr, int size, int target)` - requires sorted
- Compare performance with timing (`System.nanoTime`)

## 4. ArrayList / List (Java 21)

### Exercise 4.1: List Basics
Create program using:
- `List<Integer> v = new ArrayList<>(List.of(1, 2, 3))`
- Add elements: `add()`
- Access elements: `get(i)`, first via `get(0)`, last via `get(size()-1)`
- Iterate: enhanced for loop
- Check size with `size()`

### Exercise 4.2: List Operations
Write code demonstrating:
- Insert at position: `add(index, element)`
- Remove element: `remove(index)` / `remove(Object)`
- Clear all elements: `clear()`
- Resize behavior (ArrayList grows automatically)
- Pre-size with `new ArrayList<>(capacity)`

### Exercise 4.3: List of Records
Create:
- `record Student(String name, int score) {}`
- `List<Student> students`
- Add, remove, search students
- Sort by score (`Comparator.comparingInt(Student::score)`)
- Print all records

## 5. String Operations

### Exercise 5.1: String Basics
Create program with:
- `String s = "Hello"`
- Access characters: `s.charAt(0)`
- Get length: `s.length()`
- Concatenate: `+` operator
- Modify: Strings are immutable — build a new one or use `StringBuilder` (e.g. `sb.setCharAt(0, 'J')`)

### Exercise 5.2: String Methods
Implement or use:
- `substring(begin, end)` - extract substring
- `indexOf(substr)` - locate substring
- Replace a portion using `StringBuilder.replace(start, end, str)`
- Insert with `StringBuilder.insert(pos, str)`
- Remove with `StringBuilder.delete(start, end)`

### Exercise 5.3: String Case & Comparison
Write methods:
- `String toUpperCase(String s)` - convert to uppercase
- `String toLowerCase(String s)` - convert to lowercase
- `boolean isPalindrome(String s)` - check palindrome
- `boolean isAnagram(String s1, String s2)` - check anagram

## 6. String Searching & Manipulation

### Exercise 6.1: Pattern Matching
Create methods:
- Count occurrences of substring
- Find all positions of a character
- Check if string contains another (`contains`)
- Replace all occurrences (`replace` / `replaceAll`)

### Exercise 6.2: Word Operations
Write program that:
- Reads a sentence
- Count words (split by space, e.g. `s.split("\\s+")`)
- Find longest word
- Reverse word order
- Print each word on a new line

### Exercise 6.3: String Parsing
Parse CSV string "Alice,25,Engineer":
- Split by delimiter (`split(",")`)
- Extract fields
- Store in a record/class
- Reconstruct CSV (`String.join(",", ...)`)

## 7. Character Array vs String Object

### Exercise 7.1: char[] Strings
Using char arrays:
- `char[] str = "Hello".toCharArray()`
- Access characters by index
- Manual length handling via `str.length` (Java arrays carry their length — no null terminator)
- Compare safety with `String`

### Exercise 7.2: String Object Benefits
Using `String`:
- Automatic length handling
- Built-in operations
- Bounds checking (throws on bad index)
- Cleaner syntax; immutability for safety

### Exercise 7.3: Conversion
Create methods:
- Convert `char[]` to `String` (`new String(chars)` / `String.valueOf(chars)`)
- Convert `String` to `char[]` (`s.toCharArray()`)
- Convert to/from `byte[]` for legacy/IO requirements

## 8. List vs Array

### Exercise 8.1: List Advantages
Demonstrate:
- Dynamic sizing
- No need to know size beforehand
- Built-in methods (add, remove, contains, ...)
- Safety: indexing throws `IndexOutOfBoundsException` instead of corrupting memory

### Exercise 8.2: Performance Comparison
Compare ArrayList and array:
- Access speed (same - both O(1))
- Insertion (array shift O(n), ArrayList O(n))
- Dynamic growth efficiency (amortized O(1) append)
- Memory usage (boxing overhead for `Integer` vs `int[]`)

### Exercise 8.3: When to Use Each
Create scenarios:
- Fixed size, primitives, hot path → array (`int[]`)
- Dynamic size → ArrayList
- Performance critical / no boxing → array
- Convenience important → ArrayList

## 9. Sorting & Searching

### Exercise 9.1: Bubble Sort
Implement bubble sort:
- Sort array of integers
- Count comparisons
- Optimize by stopping early
- Time complexity: O(n²)

### Exercise 9.2: Binary Search
Implement binary search:
- Requires sorted array
- Divide and conquer approach
- Time complexity: O(log n)
- Return index or -1 if not found

### Exercise 9.3: Using Collections / Arrays Utilities
Use the library without implementing:
- `Arrays.sort()` / `Collections.sort()` - automatic sorting
- `Arrays.binarySearch()` / `Collections.binarySearch()` - returns index
- Stream-based positioning (`filter`, `count`) for lower/upper-bound-like queries
- Compare with manual implementations

## 10. Sorting Objects

### Exercise 10.1: Sort by Different Criteria
Create a `Person` record:
- `record Person(String name, int age) {}`
- Sort by name (alphabetically)
- Sort by age (numerically)
- Use a custom `Comparator`

### Exercise 10.2: Multi-key Sorting
Sort a list of students by:
- Primary: grade (descending)
- Secondary: name (ascending)
- Implement with `Comparator.comparingInt(...).reversed().thenComparing(...)`
- Use in `list.sort(...)`

### Exercise 10.3: Sorting References
Create:
- An array of references (e.g. `Integer[]` or `Person[]`)
- Sort the references (only the handles move, not the underlying objects)
- Access through each reference to verify ordering
- Demonstrate a use case

## 11. Special Containers

### Exercise 11.1: Deque Usage
Use `Deque<Integer>` (`ArrayDeque`):
- Add to front and back efficiently (`addFirst`, `addLast`)
- Remove from front and back (`pollFirst`, `pollLast`)
- Compare with ArrayList
- Use for queue/stack-like structures

### Exercise 11.2: Set for Unique Values
Using `Set<Integer>`:
- `TreeSet` keeps elements sorted and unique; `HashSet` is unordered
- Insert and check membership
- Find operations (`contains`)
- Difference from a List (no duplicates)

### Exercise 11.3: Map for Key-Value
Using `Map<String, Integer>` (`HashMap`):
- Store name → score mapping
- Access by key (`get`)
- Iterate through entries (`entrySet()`)
- Find and remove (`containsKey`, `remove`)

## Challenge Problems

### Challenge 12.1: Sudoku Validator
Create program that:
- Reads 9×9 grid
- Validates all rows contain 1-9
- Validates all columns contain 1-9
- Validates all 3×3 boxes contain 1-9
- Multi-dimensional array practice

### Challenge 12.2: String Compression
Implement:
- Input: "aaabbbc"
- Output: "a3b3c1"
- Also reverse: decompress
- Handle empty strings and special cases

### Challenge 12.3: Anagram Grouper
Given list of words:
- Group anagrams together
- Use a `List` and a `Map<String, List<String>>`
- Sort within groups
- Display results

---

## Tips for Solving

1. **Arrays**: Remember index starts at 0
2. **Bounds**: Always check valid indices (Java throws instead of corrupting memory)
3. **Strings**: Immutable — use `StringBuilder` for repeated edits
4. **Lists**: More flexible than arrays
5. **Algorithms**: Use `Collections`/`Arrays`/Streams instead of manual code
6. **Performance**: Profile before optimizing
7. **Sorting**: Understand time complexity

## Difficulty Levels
- **🟢 Easy**: Exercises 1.1-1.2, 2.1, 4.1, 5.1-5.2, 7.1, 8.1
- **🟡 Medium**: Exercises 1.3, 2.2, 3.1-3.2, 4.2-4.3, 5.3, 6.1, 7.2-7.3, 8.2, 9.1-9.2, 10.1, 11.1-11.2
- **🔴 Hard**: Exercises 2.3, 3.3, 6.2-6.3, 8.3, 9.3, 10.2-10.3, 11.3, 🏆 Challenge 12.1-12.3

---

## Common Array Mistakes

1. **Out of bounds access** - throws `ArrayIndexOutOfBoundsException`
2. **Off-by-one errors** - Accessing index n when array length is n
3. **Not initializing** - elements default to 0/null (no garbage values)
4. **Fixed size arrays** - Cannot resize (use ArrayList instead)
5. **Confusing `==` with `equals`/`Arrays.equals`** - `==` compares references
6. **Wrong copy semantics** - assigning shares the reference; use `clone`/`Arrays.copyOf` for a copy

---

## String Safety Tips

1. **Use String/StringBuilder** rather than manual char arrays when possible
2. **Check bounds** before `charAt`/`substring` to avoid exceptions
3. **Use `indexOf` safely** - returns -1 if not found
4. **Immutability** - String operations return new Strings; reassign the result
5. **Substring bounds** - check length before calling `substring`

---

## Reference: Collections Summary

| Container  | Best For            | Access   | Insert     |
|------------|---------------------|----------|------------|
| array      | Fixed size          | O(1)     | -          |
| ArrayList  | General purpose     | O(1)     | O(n)       |
| ArrayDeque | Front/back ops      | O(1)     | O(1) amort |
| LinkedList | Middle insert/delete| O(n)     | O(1)       |
| HashSet    | Unique, unordered   | O(1)     | O(1)       |
| TreeSet    | Unique, sorted      | O(log n) | O(log n)   |
| HashMap    | Key-value pairs     | O(1)     | O(1)       |
| TreeMap    | Sorted key-value    | O(log n) | O(log n)   |

## Java 21 Exercise Example: Reverse String

```java
public class Solution {
    static String reversed(String s) {
        return new StringBuilder(s).reverse().toString();
    }
}
```

Compile and run:
```
javac Solution.java
java Solution
```
