# Data Structures in Java 21

A comprehensive guide to core and advanced data structures with:
- Clear explanations
- Mermaid diagrams
- Java 21 implementations
- Complexity analysis
- Practical use cases

## Folder Structure

```
data_structures/
├── README.md
├── 01_linear/
│   ├── arrays_and_vectors.md
│   ├── linked_lists.md
│   ├── stack_queue_deque.md
│   └── structures/
│       ├── array.md
│       ├── vector.md
│       ├── singly_linked_list.md
│       ├── doubly_linked_list.md
│       ├── stack.md
│       ├── queue.md
│       ├── deque.md
│       └── linear_complexity_table.md
├── 02_hashing/
│   ├── hash_tables.md
│   └── structures/
│       ├── hash_table_chaining.md
│       ├── hash_table_open_addressing.md
│       ├── unordered_map_unordered_set.md
│       └── hashing_complexity_table.md
├── 03_trees/
│   ├── trees.md
│   └── structures/
│       ├── binary_tree.md
│       ├── binary_search_tree.md
│       ├── avl_tree.md
│       ├── heap.md
│       └── trie.md
├── 04_graphs/
│   ├── graphs.md
│   └── structures/
│       ├── adjacency_matrix.md
│       ├── adjacency_list.md
│       ├── bfs.md
│       └── dfs.md
├── 05_advanced/
│   ├── disjoint_set_union.md
│   ├── range_query_structures.md
│   ├── advanced_overview.md
│   └── structures/
│       ├── disjoint_set_union.md
│       ├── fenwick_tree.md
│       ├── segment_tree.md
│       └── advanced_complexity_table.md
├── exercises/
│   ├── README.md
│   ├── arrays_vectors_exercises.md
│   ├── linked_lists_exercises.md
│   ├── stack_queue_deque_exercises.md
│   ├── hash_tables_exercises.md
│   ├── binary_tree_exercises.md
│   ├── bst_exercises.md
│   ├── avl_tree_exercises.md
│   ├── heap_exercises.md
│   ├── trie_exercises.md
│   ├── graph_representation_exercises.md
│   ├── graph_traversal_exercises.md
│   ├── dsu_exercises.md
│   ├── fenwick_tree_exercises.md
│   └── segment_tree_exercises.md
└── code_examples/
    ├── LinearStructures.java
    ├── HashTables.java
    ├── Trees.java
    ├── Graphs.java
    └── AdvancedStructures.java
```

## Data Structures Covered

## 1. Linear Data Structures
- Arrays
- Dynamic Arrays (ArrayList)
- Singly Linked List
- Doubly Linked List
- Stack
- Queue
- Deque

## 2. Hash-Based Structures
- Hash Table (Chaining)
- Hash Table (Open Addressing overview)
- HashSet and HashMap usage

Linear, hashing, trees, graphs, and advanced categories now follow a uniform layout:

- Index chapter file
- `structures/` micro-files (one structure per file)
- `exercises/` chapter-style practice files

## 3. Tree-Based Structures
- Binary Tree
- Binary Search Tree (BST)
- AVL Tree (balanced BST overview)
- Heap (Min-Heap and Max-Heap)
- Trie

## 4. Graph Structures
- Adjacency Matrix
- Adjacency List
- Directed and Undirected Graphs
- Weighted Graphs

## 5. Advanced Structures
- Disjoint Set Union (Union-Find)
- Fenwick Tree (Binary Indexed Tree)
- Segment Tree

## 6. Chapter-Style Exercises

Each structure now has a dedicated exercise chapter under `exercises/` with Easy, Medium, Hard, and Challenge sections.

## Time Complexity Quick Table

| Structure | Access | Search | Insert | Delete |
|----------|--------|--------|--------|--------|
| Array | O(1) | O(n) | O(n) | O(n) |
| ArrayList (amortized add) | O(1) | O(n) | O(1) amortized end | O(n) |
| Linked List | O(n) | O(n) | O(1) at head | O(1) at head |
| Stack | O(1) top | O(n) | O(1) push | O(1) pop |
| Queue | O(1) front/back | O(n) | O(1) enqueue | O(1) dequeue |
| Hash Table | - | O(1) avg | O(1) avg | O(1) avg |
| BST | O(log n) avg | O(log n) avg | O(log n) avg | O(log n) avg |
| Heap | O(1) top | O(n) | O(log n) | O(log n) |
| Trie | O(k) | O(k) | O(k) | O(k) |
| Graph (Adj List) | O(1) edge iterate start | O(V+E) traversal | O(1) edge add | O(deg) edge remove |
| DSU | - | O(alpha(n)) | O(alpha(n)) union | O(alpha(n)) find |
| Segment Tree | - | - | O(log n) point update | O(log n) |
| Fenwick Tree | - | - | O(log n) update | O(log n) prefix/range |

Where k is key length, V is number of vertices, E is number of edges.

## How to Compile and Run Examples

From `data_structures/code_examples/`:

```bash
javac LinearStructures.java
java LinearStructures

javac HashTables.java
java HashTables

javac Trees.java
java Trees

javac Graphs.java
java Graphs

javac AdvancedStructures.java
java AdvancedStructures
```

## Suggested Learning Order

1. Linear structures (arrays, linked list, stack, queue)
2. Hash tables
3. Trees (BST, heap, trie)
4. Graph representations and traversals
5. Advanced structures (DSU, Fenwick, Segment Tree)

## Notes

- All code targets Java 21.
- Examples favor clarity first, then optimization.
- Use the JDK Collections Framework for production unless interview/learning goals require manual implementation.

## Java 21 Data-Structures Hub Example

```java
import java.util.ArrayList;
import java.util.List;

class Stack {
    private final List<Integer> a = new ArrayList<>();
    void push(int x) { a.add(x); }
    void pop() { if (!a.isEmpty()) a.remove(a.size() - 1); }
    int top() { return a.get(a.size() - 1); }
}
```
