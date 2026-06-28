# Trees

Trees are hierarchical data structures where nodes are connected by parent-child relationships.

This chapter has been split into one file per tree structure for deeper coverage.

## Tree Structure Chapters

1. [Binary Tree](structures/binary_tree.md)
2. [Binary Search Tree](structures/binary_search_tree.md)
3. [AVL Tree](structures/avl_tree.md)
4. [Heap](structures/heap.md)
5. [Trie](structures/trie.md)

## Complexity Summary

| Structure | Search | Insert | Delete |
|----------|--------|--------|--------|
| BST (average) | O(log n) | O(log n) | O(log n) |
| BST (worst skewed) | O(n) | O(n) | O(n) |
| AVL | O(log n) | O(log n) | O(log n) |
| Heap | O(n) search | O(log n) | O(log n) |
| Trie | O(k) | O(k) | O(k) |

Where k is key length.

## Practice

Use chapter-style exercises from:
- ../exercises/binary_tree_exercises.md
- ../exercises/bst_exercises.md
- ../exercises/avl_tree_exercises.md
- ../exercises/heap_exercises.md
- ../exercises/trie_exercises.md

## Java 21 Example: BST Insert

```java
class Node {
    int val;
    Node left;
    Node right;
    Node(int v) { this.val = v; }
}

static Node insertBST(Node root, int x) {
    if (root == null) return new Node(x);
    if (x < root.val) root.left = insertBST(root.left, x);
    else if (x > root.val) root.right = insertBST(root.right, x);
    return root;
}
```
