// Chapter 10: STL Containers -> Java Collections Framework
// Compile/run on Java 17:
//   javac chapter10_stl_containers.java
//   java  chapter10_stl_containers
//
// MAPPING (C++ STL -> Java Collections):
//   std::vector          -> java.util.ArrayList
//   std::deque           -> java.util.ArrayDeque (also good as stack/queue)
//   std::list            -> java.util.LinkedList
//   std::set             -> java.util.TreeSet      (sorted)
//   std::unordered_set   -> java.util.HashSet      (hashed, unordered)
//   std::map             -> java.util.TreeMap      (sorted by key)
//   std::unordered_map   -> java.util.HashMap      (hashed, unordered)
//   std::queue           -> java.util.ArrayDeque   (FIFO)
//   std::stack           -> java.util.ArrayDeque   (LIFO; legacy Stack exists but is discouraged)
//   std::priority_queue  -> java.util.PriorityQueue
//   multiset/multimap    -> no direct type; emulate with Map<K,List<V>> / counts
//                           (or Guava Multimap). We emulate here.

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class chapter10_stl_containers {

    // ============================================================
    // EXAMPLE 1: ArrayList (std::vector)
    // ============================================================
    static void example1_arrayList() {
        System.out.println("\n=== EXAMPLE 1: ArrayList (std::vector) ===");
        List<Integer> v = new ArrayList<>(List.of(1, 2, 3));
        v.add(4); v.add(5);
        System.out.println("ArrayList: " + v);
        v.add(2, 99); // insert at index 2
        System.out.println("After insert at position 2: " + v);
        v.remove(2);  // erase index 2 (Integer index -> position remove)
        System.out.println("After remove: " + v.size() + " elements");
    }

    // ============================================================
    // EXAMPLE 2: ArrayDeque as a double-ended queue (std::deque)
    // ============================================================
    static void example2_deque() {
        System.out.println("\n=== EXAMPLE 2: ArrayDeque (std::deque) ===");
        Deque<Integer> dq = new ArrayDeque<>();
        dq.addLast(2); dq.addLast(3);
        dq.addFirst(1); dq.addFirst(0);
        System.out.println("Deque: " + dq);
        System.out.println("Front: " + dq.peekFirst() + ", Back: " + dq.peekLast());
        dq.pollFirst(); dq.pollLast();
        System.out.println("After pollFirst and pollLast: " + dq);
    }

    // ============================================================
    // EXAMPLE 3: LinkedList (std::list)
    // ============================================================
    static void example3_linkedList() {
        System.out.println("\n=== EXAMPLE 3: LinkedList (std::list) ===");
        LinkedList<Integer> l = new LinkedList<>(List.of(1, 2, 3));
        l.addLast(4);
        l.addFirst(0);
        System.out.println("LinkedList: " + l);
        l.add(2, 99); // insert at position 2
        System.out.println("After insert at position 2: " + l);
        l.remove(Integer.valueOf(99)); // remove by VALUE (not index)
        System.out.println("After remove(99): " + l.size() + " elements");
    }

    // ============================================================
    // EXAMPLE 4: TreeSet (std::set - unique, sorted)
    // ============================================================
    static void example4_treeSet() {
        System.out.println("\n=== EXAMPLE 4: TreeSet (std::set - unique, sorted) ===");
        Set<Integer> s = new TreeSet<>();
        s.add(5); s.add(2); s.add(8); s.add(2); /*dup ignored*/ s.add(1);
        System.out.println("TreeSet (automatically sorted): " + s);
        System.out.println("Contains 5? " + (s.contains(5) ? "Yes" : "No"));
        System.out.println("Contains 3? " + (s.contains(3) ? "Yes" : "No"));
        s.remove(2);
        System.out.println("After remove(2): " + s.size() + " elements");
    }

    // ============================================================
    // EXAMPLE 5: TreeMap (std::map - sorted by key)
    // ============================================================
    static void example5_treeMap() {
        System.out.println("\n=== EXAMPLE 5: TreeMap (std::map - key-value, sorted) ===");
        Map<String, Integer> m = new TreeMap<>();
        m.put("Alice", 25); m.put("Bob", 30); m.put("Charlie", 28);
        System.out.println("TreeMap contents:");
        for (Map.Entry<String, Integer> e : m.entrySet())
            System.out.println("  " + e.getKey() + ": " + e.getValue());
        System.out.println("Alice's age: " + m.get("Alice"));
        m.remove("Bob");
        System.out.println("After remove(\"Bob\"): " + m.size() + " entries");
    }

    // ============================================================
    // EXAMPLE 6: HashSet (std::unordered_set)
    // ============================================================
    static void example6_hashSet() {
        System.out.println("\n=== EXAMPLE 6: HashSet (std::unordered_set) ===");
        Set<Integer> us = new HashSet<>();
        us.add(5); us.add(2); us.add(8); us.add(2); us.add(1);
        System.out.println("HashSet (iteration order not guaranteed): " + us);
        System.out.println("Size: " + us.size());
        System.out.println("Contains 8? " + (us.contains(8) ? "Yes" : "No"));
    }

    // ============================================================
    // EXAMPLE 7: HashMap (std::unordered_map)
    // ============================================================
    static void example7_hashMap() {
        System.out.println("\n=== EXAMPLE 7: HashMap (std::unordered_map) ===");
        Map<String, Integer> um = new HashMap<>();
        um.put("apple", 5); um.put("banana", 3); um.put("orange", 7);
        System.out.println("HashMap (iteration order undefined):");
        for (Map.Entry<String, Integer> e : um.entrySet())
            System.out.println("  " + e.getKey() + ": " + e.getValue());
        System.out.println("Lookup 'banana': " + um.get("banana"));
    }

    // ============================================================
    // EXAMPLE 8: Queue via ArrayDeque (std::queue - FIFO)
    // ============================================================
    static void example8_queue() {
        System.out.println("\n=== EXAMPLE 8: Queue (FIFO) ===");
        Queue<Integer> q = new ArrayDeque<>();
        q.add(1); q.add(2); q.add(3); q.add(4);
        System.out.println("Queue size: " + q.size());
        System.out.println("Front: " + q.peek());
        System.out.print("Dequeue all: ");
        while (!q.isEmpty()) System.out.print(q.poll() + " ");
        System.out.println();
    }

    // ============================================================
    // EXAMPLE 9: Stack via ArrayDeque (std::stack - LIFO)
    // (ArrayDeque is the recommended Java stack; java.util.Stack is legacy.)
    // ============================================================
    static void example9_stack() {
        System.out.println("\n=== EXAMPLE 9: Stack (LIFO) ===");
        Deque<String> st = new ArrayDeque<>();
        st.push("first"); st.push("second"); st.push("third");
        System.out.println("Stack size: " + st.size());
        System.out.println("Top: " + st.peek());
        System.out.print("Pop all: ");
        while (!st.isEmpty()) System.out.print(st.pop() + " ");
        System.out.println();
    }

    // ============================================================
    // EXAMPLE 10: Emulating multiset/multimap + PriorityQueue
    // Java has no multiset/multimap in the standard library; emulate with
    // count maps and Map<K,List<V>>. PriorityQueue maps std::priority_queue.
    // ============================================================
    static void example10_multiAndPriority() {
        System.out.println("\n=== EXAMPLE 10: Multiset/Multimap emulation + PriorityQueue ===");

        // Multiset emulated as TreeMap<value, count>
        Map<Integer, Integer> multiset = new TreeMap<>();
        for (int x : new int[]{1, 2, 2, 2, 3})
            multiset.merge(x, 1, Integer::sum);
        System.out.print("Multiset (value x count): ");
        multiset.forEach((val, cnt) -> System.out.print(val + "x" + cnt + " "));
        System.out.println();
        System.out.println("Count of 2: " + multiset.get(2));

        // Multimap emulated as TreeMap<key, List<value>>
        Map<String, List<Integer>> multimap = new TreeMap<>();
        multimap.computeIfAbsent("fruit", k -> new ArrayList<>()).add(5);
        multimap.computeIfAbsent("vegetable", k -> new ArrayList<>()).add(3);
        multimap.computeIfAbsent("fruit", k -> new ArrayList<>()).add(7);
        multimap.computeIfAbsent("vegetable", k -> new ArrayList<>()).add(2);
        System.out.println("Multimap ('fruit' has multiple values):");
        multimap.forEach((k, vals) -> System.out.println("  " + k + ": " + vals));

        // PriorityQueue (min-heap by default; use comparator for max-heap)
        Queue<Integer> minHeap = new PriorityQueue<>();
        Queue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        for (int x : new int[]{5, 1, 8, 3}) { minHeap.add(x); maxHeap.add(x); }
        System.out.println("PriorityQueue min head: " + minHeap.peek());
        System.out.println("PriorityQueue max head: " + maxHeap.peek());
    }

    static void containerComparison() {
        System.out.println("\n=== CONTAINER CHARACTERISTICS ===");
        System.out.println("ArrayList:   fast random access, slow middle insertion");
        System.out.println("ArrayDeque:  fast front/back, no index access");
        System.out.println("LinkedList:  fast ends/middle (with iterator), O(n) index access");
        System.out.println("TreeSet:     unique sorted, O(log n) add/contains");
        System.out.println("TreeMap:     key-value sorted by key, O(log n)");
        System.out.println("HashSet:     unique, O(1) average add/contains");
        System.out.println("HashMap:     key-value, O(1) average lookup");
        System.out.println("Queue/Stack: ArrayDeque used for both FIFO and LIFO");
        System.out.println("PriorityQueue: heap-ordered, O(log n) add/poll");
    }

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("   CHAPTER 10: STL CONTAINERS -> JAVA COLLECTIONS");
        System.out.println("======================================================");

        example1_arrayList();
        example2_deque();
        example3_linkedList();
        example4_treeSet();
        example5_treeMap();
        example6_hashSet();
        example7_hashMap();
        example8_queue();
        example9_stack();
        example10_multiAndPriority();
        containerComparison();

        System.out.println("\n======================================================");
        System.out.println("All examples completed!");
        System.out.println("======================================================");
    }
}
