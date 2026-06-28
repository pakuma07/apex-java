// linear_structures.java
// Demonstrates linear data structures: a plain Java array, ArrayList (the analog
// of std::vector), a hand-written singly linked list, and stack/queue/deque
// behaviour built on ArrayDeque (the natural Java container for all three).

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class linear_structures {

    // A single node of a singly linked list: a value plus a reference to the next node.
    static class Node {
        int data;
        Node next;
        Node(int v) { this.data = v; this.next = null; }
    }

    // Singly linked list with head-insertion. Nodes are chained one direction.
    // Invariant: `head` references the first node (or null if empty).
    // pushFront/print are O(1)/O(n); space O(n) for n nodes.
    // (Java has garbage collection, so there is no explicit destructor to free nodes.)
    static class SinglyLinkedList {
        private Node head = null;

        // Insert at the front: link the new node ahead of the old head, then
        // make it the new head. O(1) (no traversal needed).
        void pushFront(int value) {
            Node n = new Node(value);
            n.next = head;  // new node points to former first element
            head = n;       // head now references the new node
        }

        // Traverse from head to tail printing each value. O(n).
        void print() {
            Node cur = head;
            while (cur != null) {
                System.out.print(cur.data + " ");
                cur = cur.next;  // advance the walking pointer
            }
            System.out.println();
        }
    }

    public static void main(String[] a) {
        System.out.println("== Array and Vector ==");
        // Fixed-size array: contiguous, O(1) random access, size fixed at creation.
        int[] arr = {10, 20, 30, 40, 50};
        System.out.println("arr[2] = " + arr[2]);

        // ArrayList: dynamic array (analog of std::vector). add is amortized O(1);
        // insert in the middle is O(n) because trailing elements shift right.
        List<Integer> v = new ArrayList<>();
        v.add(1);
        v.add(2);
        v.add(3);
        v.add(1, 99);  // insert 99 at index 1, shifting 2 and 3 over

        System.out.print("vector: ");
        for (int x : v) System.out.print(x + " ");
        System.out.println();

        System.out.println("\n== Singly Linked List ==");
        SinglyLinkedList list = new SinglyLinkedList();
        list.pushFront(30);
        list.pushFront(20);
        list.pushFront(10);
        list.print();

        System.out.println("\n== Stack using deque ==");
        // Stack (LIFO): use an ArrayDeque, treating its head as the top. push/pop/peek all O(1).
        Deque<Integer> stackv = new ArrayDeque<>();
        stackv.push(10);
        stackv.push(20);
        stackv.push(30);
        System.out.println("stack top: " + stackv.peek());
        stackv.pop();  // remove the most recently pushed element
        System.out.println("stack top after pop: " + stackv.peek());

        System.out.println("\n== Queue using deque ==");
        // Queue (FIFO): enqueue at the back, dequeue from the front. Both O(1) on a deque.
        Deque<Integer> q = new ArrayDeque<>();
        q.addLast(100);
        q.addLast(200);
        q.addLast(300);
        System.out.println("queue front: " + q.peekFirst());
        q.pollFirst();  // dequeue: remove the oldest element
        System.out.println("queue front after dequeue: " + q.peekFirst());

        System.out.println("\n== Deque ==");
        // Double-ended queue: O(1) insert/remove at both ends.
        Deque<Integer> d = new ArrayDeque<>();
        d.addLast(2);
        d.addFirst(1);
        d.addLast(3);
        System.out.print("deque: ");
        for (int x : d) System.out.print(x + " ");
        System.out.println();
    }
}
