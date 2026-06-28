# Chapter 2: Control Flow & Loops - Practice Exercises

## 1. If-Else Statements

### Exercise 1.1: Number Classification
Write a program that:
- Reads an integer from user
- Prints if it's positive, negative, or zero
- Also indicate if it's even or odd

### Exercise 1.2: Grade Calculator
Create a program that:
- Takes a score (0-100)
- Prints grade: A (90+), B (80-89), C (70-79), D (60-69), F (<60)
- Prints if student passed (>=60) or failed

### Exercise 1.3: Triangle Type
Given three sides of a triangle:
- Check if it forms a valid triangle
- If valid, determine type: equilateral, isosceles, or scalene
- Handle invalid input

## 2. Switch Statements

### Exercise 2.1: Day of Week
Create a menu that:
- Takes a number 1-7
- Prints corresponding day name using switch
- Show error for invalid input

### Exercise 2.2: Calculator
Implement a calculator:
- Take two numbers and an operator (+, -, *, /, %)
- Use switch for operator selection
- Handle division by zero

### Exercise 2.3: Menu System
Create a menu with options:
- 1: Print hello
- 2: Print goodbye
- 3: Print your name
- 4: Exit
- Use switch and allow multiple operations

## 3. Loops: While & Do-While

### Exercise 3.1: Input Validation
Write a program that:
- Asks user for a number between 1-10
- Keep asking until valid input received
- Use while loop with condition

### Exercise 3.2: Countdown
Create a countdown program:
- Takes starting number from user
- Counts down to 0 using while loop
- Then counts up to that number using do-while

### Exercise 3.3: Password Retry
Implement login system:
- Takes password "Java2024"
- Allows 3 attempts
- Exits after 3 failures
- Use do-while loop

## 4. For Loops

### Exercise 4.1: Multiplication Table
Print multiplication table:
- Takes number n from user
- Prints n×1, n×2, ..., n×10
- Format nicely with spacing

### Exercise 4.2: Sum of Series
Calculate:
- Sum of numbers 1 to n: 1+2+3+...+n
- Sum of squares: 1²+2²+3²+...+n²
- Sum of cubes: 1³+2³+3³+...+n³

### Exercise 4.3: Nested Loops - Patterns
Print patterns:
- 5×5 square of asterisks
- Right triangle of asterisks
- Pyramid of asterisks

## 5. Nested Loops

### Exercise 5.1: Multiplication Table Grid
Print full multiplication table:
- 10×10 grid showing i×j
- Format numbers in columns

### Exercise 5.2: 2D Array Processing
Given a 3×3 array:
- Print array with nested loops
- Calculate sum of row
- Calculate sum of column
- Calculate diagonal sum

### Exercise 5.3: Prime Number Table
Print all prime numbers:
- Up to 100
- Use nested loops to check primality
- Display in formatted table

## 6. Enhanced For Loop (Java 21)

### Exercise 6.1: List Processing
Given List<Integer> = {1, 2, 3, 4, 5} (an ArrayList):
- Print all elements using enhanced for
- Print all even elements
- Double all elements and print

### Exercise 6.2: String Iteration
Given String "Hello":
- Print each character
- Print in reverse order
- Count vowels and consonants

### Exercise 6.3: Container Types
Use enhanced for with:
- List<String> of names (ArrayList)
- Deque<Integer> of numbers (ArrayDeque)
- Set<Integer> of unique values (HashSet/TreeSet)
- Map<String, Integer> with keys and values (HashMap)

## 7. Break & Continue

### Exercise 7.1: Loop Control
Print numbers 1-20 but:
- Skip multiples of 3
- Stop at 15
- Use continue and break

### Exercise 7.2: Search with Break
Given a List of integers:
- Find first number > 50
- Print its position
- Exit loop when found

### Exercise 7.3: Validated Loop
Read numbers until:
- User enters negative number (use break)
- Or enters 0 to skip (use continue)
- Print sum of valid numbers

## 8. Infinite Loops & Loop Control

### Exercise 8.1: Menu-Driven Program
Create a program with:
- while(true) infinite loop
- Menu with options
- Option to exit (break)
- Display menu after each operation

### Exercise 8.2: Game Loop
Simulate simple game:
- While loop checking game active
- Players take turns
- Check win condition to break

### Exercise 8.3: Error Recovery
Read operations:
- Invalid operation shows error
- Asks to retry (continue)
- Valid operation processes
- "quit" exits (break)

## 9. Loop Performance

### Exercise 9.1: Compare Loop Types
Calculate sum 1 to 1000:
- Using for loop
- Using while loop
- Using enhanced for with a List
- Compare readability and efficiency

### Exercise 9.2: Optimization
Given nested loop structure:
- Calculate time complexity
- Identify improvements
- Example: check only up to sqrt(n) for prime

### Exercise 9.3: Break Optimization
Search in array:
- Without break: always check all elements
- With break: stop early if found
- Measure effectiveness

## 10. Combined Control Flow

### Exercise 10.1: Complex Menu System
Create program with:
- If-else for input validation
- Switch for menu selection
- Loops for operations
- Break/continue for flow control
- Proper error handling

### Exercise 10.2: Game with Control Flow
Implement guessing game:
- Random number 1-100
- If-else: check guess vs number
- Loop: allow multiple attempts
- Break: when correct guess
- Show remaining attempts

### Exercise 10.3: Data Processing
Read input with:
- While loop for input collection
- For loop for processing
- Switch for analysis type
- If-else for displaying results
- Continue/break for flow

## Challenge Problems

### Challenge 11.1: Spiral Pattern
Print spiral pattern:
```
1  2  3  4  5
16 17 18 19 6
15 24 25 20 7
14 23 22 21 8
13 12 11 10 9
```

### Challenge 11.2: Fibonacci Sequence
Print first n Fibonacci numbers:
- Use loops to generate
- Show pattern/growth
- Nested loops for spacing

### Challenge 11.3: Prime Factorization
Given a number, find:
- All prime factors
- Use nested loops and continue/break
- Display: 12 = 2² × 3

---

## Tips for Solving

1. **Start simple**: Test with small loops first
2. **Trace execution**: Follow variable changes
3. **Nested loops**: Visualize outer and inner separately
4. **Break/continue**: Understand impact on loop flow
5. **Edge cases**: Test boundaries (first, last, empty)
6. **Formatting**: Use loops to create patterns
7. **Performance**: Consider loop optimization

## Difficulty Levels
- **🟢 Easy**: Exercises 1.1-1.2, 2.1, 3.1, 4.1, 6.1
- **🟡 Medium**: Exercises 1.3, 2.2-2.3, 3.2-3.3, 4.2-4.3, 5.1-5.2, 6.2-6.3, 7.1-7.2, 8.1, 9.1-9.2, 10.1
- **🔴 Hard**: Exercises 5.3, 7.3, 8.2-8.3, 9.3, 10.2-10.3, 🏆 Challenge 11.1-11.3

---

## Expected Outputs

### Multiplication Table (Exercise 4.1)
```
5 x 1 = 5
5 x 2 = 10
5 x 3 = 15
...
5 x 10 = 50
```

### Pattern (Exercise 5.1)
```
*****
*****
*****
*****
*****
```

### Prime Numbers (Exercise 5.3)
```
2  3  5  7  11 13 17 19 23 29
31 37 41 43 47 53 59 61 67 71
...
```

## Java 21 Exercise Example: If/Else Classification

```java
public class Solution {
    static String grade(int score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        return "D";
    }
}
```

Compile and run:
```
javac Solution.java
java Solution
```
