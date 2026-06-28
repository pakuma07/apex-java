# Exercises: Heap

## Easy

1. Build min-heap from array.
2. Insert and extract-min.
3. Convert min-heap to max-heap.
4. Check if array satisfies heap property.
5. Kth largest using heap.

## Medium

1. Heap sort implementation.
2. Merge k sorted arrays.
3. Running median with two heaps.
4. Top-k frequent numbers.
5. Reorganize string with max-heap.

## Hard

1. Design indexed priority queue.
2. Dijkstra with binary heap.
3. Sliding window median.
4. Merge k sorted linked lists.
5. Min cost to connect ropes.

## Challenge

Implement custom binary heap class with decrease-key.

## Java 21 Exercise Example: Min-Heap Top K

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class TopK {
    public static List<Integer> kSmallest(List<Integer> a, int k) {
        // Max-heap keeps the k smallest seen so far at its root.
        PriorityQueue<Integer> pq = new PriorityQueue<>(Collections.reverseOrder());
        for (int x : a) {
            pq.add(x);
            if (pq.size() > k) pq.poll();
        }
        List<Integer> out = new ArrayList<>();
        while (!pq.isEmpty()) out.add(pq.poll());
        return out;
    }
}
```
