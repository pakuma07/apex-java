# Linked Lists

This chapter has been split into micro-files.

## Structure Files

1. [Singly Linked List](structures/singly_linked_list.md)
2. [Doubly Linked List](structures/doubly_linked_list.md)
3. [Linear Complexity Table](structures/linear_complexity_table.md)

## Practice

- ../exercises/linked_lists_exercises.md

## Java 21 Example: Singly Linked List Insert Front

```java
class Node {
    int val;
    Node next;
    Node(int v, Node n) { this.val = v; this.next = n; }
}

static Node pushFront(Node head, int x) {
    return new Node(x, head);
}
```
