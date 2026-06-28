# Stack, Queue, and Deque

This chapter has been split into micro-files.

## Structure Files

1. [Stack](structures/stack.md)
2. [Queue](structures/queue.md)
3. [Deque](structures/deque.md)
4. [Linear Complexity Table](structures/linear_complexity_table.md)

## Practice

- ../exercises/stack_queue_deque_exercises.md

## Java 21 Example: Queue with ArrayDeque

```java
import java.util.ArrayDeque;
import java.util.Deque;

class IntQueue {
    private final Deque<Integer> d = new ArrayDeque<>();
    void push(int x) { d.addLast(x); }
    void pop() { if (!d.isEmpty()) d.pollFirst(); }
    int front() { return d.peekFirst(); }
}
```
