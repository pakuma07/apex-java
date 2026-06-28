# Exercises Directory

This directory contains practice problems for each chapter of the Java learning resource. The exercises are designed to reinforce understanding and build practical skills.

## Organization

Each exercise file is named `chapter<N>_exercises.md` and contains multiple exercise sections organized by difficulty level.

## Difficulty Levels

Exercises are marked by difficulty:

- 🟢 **Easy**: Fundamental concepts, straightforward implementation
- 🟡 **Medium**: Combine multiple concepts, require some problem-solving
- 🔴 **Hard**: Complex challenges, require deep understanding and creativity

## How to Use Exercises

### 1. Read the Exercise Description
Understand what you need to implement:
```
Exercise 3.1: Calculator Methods
Create methods for basic operations:
- int add(int a, int b) - returns sum
- int subtract(int a, int b) - returns difference
...
```

### 2. Implement the Solution
Write Java code to solve the problem:
```java
int add(int a, int b) {
    return a + b;
}
```

### 3. Test Your Code
Compile and run with different inputs:
```bash
javac Solution.java
java Solution
```

### 4. Compare with Examples
Check if your approach matches the code examples in [../code_examples/](../code_examples/)

### 5. Review Tips
Check the tips provided in each exercise file

## Available Exercise Sets

### Chapter 1: Basics (25+ exercises)
**File**: `chapter1_exercises.md`

**Topics**:
- Data type operations
- Constants and literals
- Type conversions
- Variable scope
- I/O formatting
- Arithmetic operations
- Challenge problems

**Difficulty Distribution**:
- Easy: 4 exercises
- Medium: 6 exercises
- Hard: 3 exercises

### Chapter 2: Control Flow (40+ exercises)
**File**: `chapter2_exercises.md`

**Topics**:
- If-else statements
- Switch statements / switch expressions
- While loops
- Do-while loops
- For loops
- Enhanced for-each loop
- Break and continue
- Nested loops
- Infinite loops
- Complex control flow

**Difficulty Distribution**:
- Easy: 6 exercises
- Medium: 8 exercises
- Hard: 4 exercises

### Chapter 3: Methods (40+ exercises)
**File**: `chapter3_exercises.md`

**Topics**:
- Basic method definition
- Parameters and pass-by-value
- Object references as parameters
- Overloading
- Default arguments (via overloading)
- Recursion
- Functional interfaces & method references
- Lambda expressions
- Return values
- Challenge problems

**Difficulty Distribution**:
- Easy: 6 exercises
- Medium: 8 exercises
- Hard: 5 exercises

### Chapter 4: References, Memory & Garbage Collection (30+ exercises)
**File**: `chapter4_exercises.md`

**Topics**:
- References vs primitives
- Array references and iteration
- Out-parameters via holders/arrays
- Null references and Optional
- Object allocation and garbage collection
- Reachability and memory leaks
- WeakReference / SoftReference / PhantomReference
- AutoCloseable and try-with-resources
- Functional interfaces (method references)
- Linked lists and BST with references

**Difficulty Distribution**:
- Easy: 8 exercises
- Medium: 10 exercises
- Hard: 5 exercises

### Chapter 5: Arrays & Strings (35+ exercises)
**File**: `chapter5_exercises.md`

**Topics**:
- Array basics and indexing
- Multi-dimensional arrays
- Copying and comparing arrays
- ArrayList / List container
- String operations
- Case conversion
- String parsing
- Array vs ArrayList
- Sorting and searching
- Complex data structures

**Difficulty Distribution**:
- Easy: 8 exercises
- Medium: 10 exercises
- Hard: 5 exercises

### Chapter 16: Concurrency (30+ exercises)
**File**: `chapter16_concurrency_exercises.md`

**Topics**:
- Thread creation and joining (platform & virtual threads)
- synchronized, ReentrantLock, lock lifecycle
- Condition variables (wait/notify, Condition, BlockingQueue)
- Atomic counters and flags
- CompletableFuture, Future, Callable, FutureTask
- Deadlock avoidance and lifecycle management

**Difficulty Distribution**:
- Easy: 3 exercises
- Medium: 12 exercises
- Hard: 12 exercises

### Chapter 17: Reflection & Annotations (32 exercises)
**File**: `chapter17_tmp_exercises.md`

**Topics**:
- Class inspection with reflection
- Runtime type inspection (isPrimitive, isArray, isAssignableFrom)
- Dynamic method invocation (Method.invoke, setAccessible)
- Custom annotations & runtime annotation processing
- Dynamic proxies and InvocationHandler
- Generic algorithms with bounded types
- F-bounded generics (self-referential types)
- Generics with wildcards and reflection on generic types

**Difficulty Distribution**:
- Easy: 5 exercises
- Medium: 11 exercises
- Hard: 11 exercises
- Challenges: 3 problems

### Chapter 18: Java Memory Model (30 exercises)
**File**: `chapter18_memory_model_exercises.md`

**Topics**:
- Data races and atomic types (AtomicInteger, AtomicBoolean)
- volatile and happens-before
- VarHandle ordering (getAcquire, setRelease, getOpaque, getPlain)
- Relaxed-style ordering for counters (LongAdder, opaque)
- VarHandle fences for grouped publishing
- Compare-and-swap (CAS) loops
- Lock-free data structures (Treiber stack, queue, ring buffer)
- ThreadLocal storage and once-only initialization

**Difficulty Distribution**:
- Easy: 7 exercises
- Medium: 12 exercises
- Hard: 8 exercises
- Challenges: 3 problems

## Exercise Statistics

| Chapter | Easy | Medium | Hard | Total |
|---------|------|--------|------|-------|
| 1       | 4    | 6      | 3    | 13+   |
| 2       | 6    | 8      | 4    | 18+   |
| 3       | 6    | 8      | 5    | 19+   |
| 4       | 8    | 10     | 5    | 23+   |
| 5       | 8    | 10     | 5    | 23+   |
| 16      | 3    | 12     | 12   | 30+   |
| 6-15    | See individual files | See individual files | See individual files | Available |

**Total Completed**: 188+ exercises

## Recommended Learning Path

### Day 1-2: Fundamentals
1. Start with **Chapter 1** - Basics
2. Do 2-3 easy exercises per session
3. Understand data types and I/O

### Day 3-4: Control Flow
1. Practice **Chapter 2** - Control Flow
2. Focus on loop patterns
3. Build intuition for conditionals

### Day 5-6: Methods
1. Work through **Chapter 3** - Methods
2. Implement various method types
3. Practice recursion

### Day 7-8: References & Memory
1. Study **Chapter 4** - References, Memory & Garbage Collection
2. Start with simple reference exercises
3. Progress to WeakReference and resource management

### Day 9-10: Data Structures
1. Practice **Chapter 5** - Arrays & Strings
2. Implement searching and sorting
3. Use ArrayList and String

### Day 11-12: Concurrency
1. Study **Chapter 16** - Concurrency
2. Practice thread lifecycle and lock safety
3. Build producer/consumer and future-based solutions

## Tips for Success

### Before Coding
- [ ] Read the exercise description carefully
- [ ] Understand what input/output is expected
- [ ] Sketch algorithm on paper if complex
- [ ] Think about edge cases

### While Coding
- [ ] Write clean, readable code
- [ ] Use meaningful variable names
- [ ] Add comments for complex logic
- [ ] Test with multiple inputs

### After Coding
- [ ] Verify output is correct
- [ ] Check edge cases (empty, single, large inputs)
- [ ] Compare with example code
- [ ] Refactor for clarity

### Common Mistakes to Avoid
- Off-by-one errors in loops
- Uninitialized variables
- Not handling special cases
- Missing method parameter checks
- Comparing objects with `==` instead of `equals()`

## Solution Structure

A typical solution looks like:

```java
import java.util.*;

public class Solution {

    // Your method implementations
    static int myMethod(int x) {
        // Implementation
        return x;
    }

    public static void main(String[] args) {
        // Test your methods
        System.out.println(myMethod(5));
    }
}
```

## Testing Your Solutions

### Compile
```bash
javac -Xlint:all Solution.java
```

### Run
```bash
java Solution
```

### Test with Input
```bash
echo "5" | java Solution
```

### Debug with jdb
```bash
javac -g Solution.java
jdb Solution
```

## When You're Stuck

1. **Review the code example**: Check [../code_examples/](../code_examples/)
2. **Read the chapter**: See [../01_basics/README.md](../01_basics/README.md), etc.
3. **Check the tips section**: Each exercise file has helpful tips
4. **Try a simpler version**: Start with an easier variant
5. **Ask questions**: Refer to quick reference sheets

## Quick Reference Links

- **Java Syntax**: [../resources/QUICK_REFERENCE.md](../resources/QUICK_REFERENCE.md)
- **OOP Guide**: [../resources/OOP_CHEATSHEET.md](../resources/OOP_CHEATSHEET.md)
- **Collections Guide**: [../resources/COLLECTIONS_CHEATSHEET.md](../resources/COLLECTIONS_CHEATSHEET.md)
- **Chapter Topics**: [../README.md](../README.md)

## Progress Tracker

Track your completion:

- [x] Chapter 1: Basics (13+ exercises) ✅ Complete
- [x] Chapter 2: Control Flow (18+ exercises) ✅ Complete
- [x] Chapter 3: Methods (19+ exercises) ✅ Complete
- [x] Chapter 4: References & Memory (23+ exercises) ✅ Complete
- [x] Chapter 5: Arrays/Strings (23+ exercises) ✅ Complete
- [x] Chapter 6: OOP Basics (20+ exercises) ✅ Complete
- [x] Chapter 7: Inheritance & Polymorphism (30+ exercises) ✅ Complete
- [x] Chapter 8: Equality, Ordering & Operators (30+ exercises) ✅ Complete
- [x] Chapter 9: Generics (30+ exercises) ✅ Complete
- [x] Chapter 10: Collections (30+ exercises) ✅ Complete
- [x] Chapter 11: Streams & Algorithms (30+ exercises) ✅ Complete
- [x] Chapter 12: Memory & Resources (30+ exercises) ✅ Complete
- [x] Chapter 13: Exceptions (30+ exercises) ✅ Complete
- [x] Chapter 14: File I/O (30+ exercises) ✅ Complete
- [x] Chapter 15: Modern Java Features (30+ exercises) ✅ Complete

**STATUS: 18/18 CHAPTERS COMPLETE ✅ (100% EXERCISES)**

## Challenge Problems

Each chapter ends with challenge problems marked with 🏆:

- **Challenge 10.x**: Comprehensive problem combining multiple concepts
- Recommended after completing all basic and medium exercises
- Great for portfolio and interview preparation

## Expected Time Commitment

- **Easy exercises**: 10-20 minutes each
- **Medium exercises**: 20-40 minutes each
- **Hard exercises**: 40-90+ minutes each
- **Challenge problems**: 60-120+ minutes each

Plan 1-2 hours per chapter for thorough practice.

## Feedback & Improvement

As you work through exercises:
- Note which concepts need more practice
- Revisit easier exercises if later topics confuse you
- Create your own variations to solidify understanding
- Teach someone else to deepen your knowledge

---

**Remember**: The goal is learning, not just solving. Take time to understand the "why" behind each solution.
