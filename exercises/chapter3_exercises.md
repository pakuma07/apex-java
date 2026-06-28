# Chapter 3: Methods - Practice Exercises

## 1. Basic Methods

### Exercise 1.1: Calculator Methods
Create methods for basic operations:
- `int add(int a, int b)` - returns sum
- `int subtract(int a, int b)` - returns difference
- `int multiply(int a, int b)` - returns product
- `double divide(double a, double b)` - returns quotient (handle division by zero)

Test each method with sample values from main().

### Exercise 1.2: String Operations
Create methods:
- `int stringLength(String s)` - return string length
- `String reverseString(String s)` - return reversed string
- `boolean isPalindrome(String s)` - check if palindrome (ignore case)
- `String toUpperCase(String s)` - convert to uppercase

### Exercise 1.3: Geometric Methods
Create methods for geometry:
- `double circleArea(double radius)`
- `double rectangleArea(double length, double width)`
- `double triangleArea(double base, double height)`
- `double sphereVolume(double radius)`

## 2. Parameters & Arguments

### Exercise 2.1: Pass by Value vs Reference Semantics
Write methods that:
- `void incrementByValue(int x)` - increments local copy (primitives are passed by value)
- `void incrementHolder(int[] x)` - increments x[0] through a reference (objects/arrays are references)

In main(), call both and show the difference. Note: Java is always pass-by-value, but for objects the *value* passed is a reference, so the referenced object can be mutated.

### Exercise 2.2: Swap Values
Create two versions:
- `void swapByValue(int a, int b)` - won't work! (primitives copied by value)
- `void swapByReference(int[] a, int[] b)` - swaps using single-element array holders

Show why the first doesn't work and the second does.

### Exercise 2.3: Defensive / Read-only Parameters
Create a method:
- `void printList(List<Integer> v)` - reads but does not modify (consider `Collections.unmodifiableList` or `List.copyOf` for safety)
- Modify a list and display it from main

## 3. Method Overloading

### Exercise 3.1: Overloaded Add
Create overloaded `add()` methods for:
- Two integers
- Two doubles
- Three integers
- String concatenation (add two strings)

### Exercise 3.2: Overloaded Print
Create overloaded `print()` methods for:
- `print(int x)`
- `print(double x)`
- `print(String s)`
- `print(List<Integer> v)` - print all elements

### Exercise 3.3: Overloaded Area
Create overloaded `area()` methods:
- `area(double side)` - square area
- `area(double length, double width)` - rectangle
- `area(double radius, boolean circle)` - circle (use boolean to distinguish)

## 4. Default Argument Values (Overloading Idiom)

Java has no default arguments; emulate them with overloads.

### Exercise 4.1: Flexible Greeting
Create methods:
```java
void greet()                          // -> greet("Guest", "Hello")
void greet(String name)               // -> greet(name, "Hello")
void greet(String name, String greeting)
```

Call it with:
- No arguments
- Just name
- Both name and greeting

### Exercise 4.2: Range Printer
Create overloads emulating: `printRange(int start = 1, int end = 10, int step = 1)`

Display ranges with different parameters:
- Default (1-10)
- Custom start and end
- Custom step

### Exercise 4.3: Customizable Output
Create overloads emulating: `printArray(List<Integer> arr, String prefix = "Element", String separator = " ")`

Print arrays with different prefixes and separators.

## 5. Recursion

### Exercise 5.1: Mathematical Methods
Implement recursively:
- `int factorial(int n)` - n!
- `int fibonacci(int n)` - nth Fibonacci number
- `int power(int base, int exp)` - base^exp
- `int gcd(int a, int b)` - greatest common divisor

### Exercise 5.2: Array Processing
Recursive methods on arrays:
- `int sumArray(int[] arr, int size)` - sum all elements
- `int maxArray(int[] arr, int size)` - find maximum
- `void reverseArray(int[] arr, int start, int end)` - reverse in-place
- `void printArray(int[] arr, int size)` - print all

### Exercise 5.3: String Recursion
Create recursive methods:
- `boolean isPalindrome(String s, int start, int end)` - check palindrome
- `int countOccurrences(String s, char c, int index)` - count character occurrences
- `String removeVowels(String s, int index)` - remove all vowels

### Exercise 5.4: Recursion with Backtracking
Implement permutations:
- `void permute(char[] s, int l, int r)` - print all permutations of string

## 6. Method References & Functional Interfaces

### Exercise 6.1: Functional Interface Operations
Create:
- `int operation(int a, int b, IntBinaryOperator func)` (or `BiFunction<Integer,Integer,Integer>`)
- Use with different methods (add, multiply, subtract) via method references

### Exercise 6.2: List of Functional Interfaces
Create a collection of operations:
- Define `add()`, `subtract()`, `multiply()`, `divide()`
- Store in `List<IntBinaryOperator> ops` (or a `Map<String, IntBinaryOperator>`)
- Call different operations via the collection

### Exercise 6.3: Sort with Comparator
Implement sorting with a functional interface:
- `void sortArray(Integer[] arr, Comparator<Integer> compare)` (or use `Arrays.sort` with a comparator)
- Use with ascending and descending comparators

## 7. Lambdas (Java 21)

### Exercise 7.1: Basic Lambdas
Create lambdas for:
- `IntUnaryOperator square = x -> x * x;`
- `IntPredicate isEven = x -> x % 2 == 0;`
- `Function<String,String> greet = name -> "Hello, " + name;`

### Exercise 7.2: Lambda with Capture
Create lambdas that capture (closures capture effectively-final variables):
- Multiply by factor: `IntUnaryOperator multiply = x -> x * factor;`
- Count occurrences via a mutable holder: `int[] count = {0}; Runnable counter = () -> count[0]++;`

### Exercise 7.3: Streams with Lambdas
Use lambdas with the Stream API:
- `filter(...).findFirst()` to find first even number in a list
- `filter(x -> x > 10).count()` to count numbers > 10
- `map(x -> x * x)` to square all elements
- `sorted(comparator)` with a custom comparator

## 8. Advanced Method Concepts

### Exercise 8.1: Recursive Lambda
Create a recursive lambda using a holder reference:
```java
Function<Integer,Integer>[] fac = new Function[1];
fac[0] = n -> n <= 1 ? 1 : n * fac[0].apply(n - 1);
```

### Exercise 8.2: Higher-Order Methods
Create a method that:
- Takes a functional interface as parameter
- Returns a new function that modifies behavior
- Example: compose functions (`Function::andThen`), create a wrapper

### Exercise 8.3: Memoization
Implement memoized Fibonacci:
- Use `Map<Integer, Integer>` (HashMap) to cache results
- Compare performance vs regular recursion

## 9. Return Values

### Exercise 9.1: Multiple Return Values
Create methods that return:
- A single value (simple case)
- An array or List (multiple values)
- A record (e.g. `record Pair(int a, int b) {}`)
- A `Map.Entry` or custom class for grouped results

### Exercise 9.2: Error Handling
Methods that return status:
- `boolean divide(double a, double b, double[] result)` (use single-element array as out-param)
- `int findElement(List<Integer> v, int target)`
- Return -1 for not found, 0+ for index
- Alternatively, return `Optional<Integer>` instead of -1

### Exercise 9.3: Return Objects
Methods that return:
- Simple objects: `Person createPerson(String name, int age)`
- List/array: `List<Integer> generateFibonacci(int n)`
- A newly allocated array: `int[] allocateArray(int size)` (allocated with `new`, reclaimed by GC)

## Challenge Problems

### Challenge 10.1: Calculator with Functional Interfaces
Create a simple calculator that:
- Uses functional interfaces / method references for operations
- Takes operator (+, -, *, /) and two numbers
- Displays result

### Challenge 10.2: Recursive Directory Size
Simulate recursive directory traversal:
- `int dirSize(String path, int depth)`
- Display tree structure with sizes

### Challenge 10.3: Function Composition
Create methods that:
- Compose multiple operations
- Example: `int result = applyMany(5, List.of(square, doubleIt, add5));`

---

## Tips for Solving

1. **Start simple**: Write basic method first, then enhance
2. **Test thoroughly**: Call with various inputs including edge cases
3. **Use clear names**: Method names should describe what they do
4. **Document parameters**: Add comments explaining parameter requirements
5. **Handle errors**: Check for invalid inputs (division by zero, null references, etc.)

## Difficulty Levels
- **🟢 Easy**: Exercises 1.1-1.3, 2.1, 3.1-3.2, 4.1-4.2
- **🟡 Medium**: Exercises 2.2-2.3, 3.3, 4.3, 5.1-5.2, 6.1-6.2, 7.1-7.2
- **🔴 Hard**: Exercises 5.3-5.4, 6.3, 7.3, 8.1-8.3, 🏆 Challenge 10.1-10.3
