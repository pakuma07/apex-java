# Binary Search Tree

## Concept

A binary search tree (BST) is a binary tree that maintains a strict ordering invariant: for every node, all keys in its left subtree are smaller and all keys in its right subtree are larger. This ordering lets you locate, insert, or remove a key by walking down a single root-to-leaf path, comparing at each step. An inorder traversal of a BST therefore yields the keys in sorted order. BSTs are ideal when you need an ordered collection with reasonably fast lookup, insertion, and deletion, but note that without balancing the tree can degrade to a linked list.

## Mermaid

```mermaid
graph TD
    A((8)) --> B((3))
    A --> C((10))
    B --> D((1))
    B --> E((6))
    C --> F((14))
    E --> G((4))
    E --> H((7))
```

## Complexity

- Search / Insert / Delete: O(log n) average on a balanced tree, O(h) in general
- Worst case (degenerate / sorted insertion order): O(n) per operation, since the tree becomes a chain
- Inorder traversal: O(n)
- Space: O(n) for storage, O(h) recursion depth

## Java Code

```java
public class BST {
    static class Node {
        int key;
        Node left;
        Node right;
        Node(int k) { this.key = k; }
    }

    // Insert returns the (possibly new) subtree root.
    static Node insert(Node root, int key) {
        if (root == null) return new Node(key);
        if (key < root.key)      root.left  = insert(root.left, key);
        else if (key > root.key) root.right = insert(root.right, key);
        // equal keys are ignored (set semantics)
        return root;
    }

    // Search returns the node holding key, or null.
    static Node search(Node root, int key) {
        while (root != null && root.key != key)
            root = (key < root.key) ? root.left : root.right;
        return root;
    }

    // Leftmost node = smallest key in a subtree.
    static Node findMin(Node root) {
        while (root != null && root.left != null) root = root.left;
        return root;
    }

    // Delete using the inorder successor when the node has two children.
    static Node removeNode(Node root, int key) {
        if (root == null) return null;
        if (key < root.key) {
            root.left = removeNode(root.left, key);
        } else if (key > root.key) {
            root.right = removeNode(root.right, key);
        } else {
            // Found it. Handle 0 or 1 child first.
            if (root.left == null)  return root.right;
            if (root.right == null) return root.left;
            // Two children: copy successor key, then delete successor.
            Node succ = findMin(root.right);
            root.key = succ.key;
            root.right = removeNode(root.right, succ.key);
        }
        return root;
    }
}
```

## Mini Usage Example

```java
BST.Node root = null;
int[] keys = {8, 3, 10, 1, 6, 14, 4, 7};
for (int k : keys) root = BST.insert(root, k);

System.out.println(BST.search(root, 6) != null ? "found" : "missing");  // found
root = BST.removeNode(root, 3);   // remove node with two children
System.out.println(BST.search(root, 3) != null ? "found" : "missing");  // missing
```

## Code Snippet Flow

```mermaid
flowchart TD
    A[Start at root] --> B{key == node?}
    B -- yes --> C[Found / act on node]
    B -- key < node --> D[Go left]
    B -- key > node --> E[Go right]
    D --> F{child null?}
    E --> F
    F -- no --> B
    F -- yes --> G[Reached insertion point / not found]
```
