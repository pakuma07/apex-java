# Exercises: Stack, Queue, Deque

## Easy

1. Implement stack using array.
2. Implement queue using circular array.
3. Check balanced parentheses.
4. Reverse a queue using stack.
5. Simulate browser back/forward with deque.

## Medium

1. Next greater element for each array value.
2. Evaluate postfix expression.
3. Implement min-stack with O(1) getMin.
4. Sliding window maximum using deque.
5. Queue using two stacks.

## Hard

1. Largest rectangle in histogram.
2. Maximal rectangle in binary matrix.
3. First non-repeating character in stream.
4. Design data structure with O(1) enqueue/dequeue/getMax.
5. Implement expression parser (infix to postfix + evaluate).

## Challenge

Build a job scheduler simulation using queue priorities and processing windows.

## Java 21 Exercise Example: Valid Parentheses

```java
import java.util.ArrayDeque;
import java.util.Deque;

public class ValidParentheses {
    public static boolean isValid(String s) {
        Deque<Character> st = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if (c == '(') st.push(c);
            else {
                if (st.isEmpty()) return false;
                st.pop();
            }
        }
        return st.isEmpty();
    }
}
```
