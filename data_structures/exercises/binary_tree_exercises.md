# Exercises: Binary Tree

## Easy

1. Compute height of binary tree.
2. Count total nodes and leaf nodes.
3. Print preorder, inorder, postorder traversals.
4. Level-order traversal.
5. Check if two trees are identical.

## Medium

1. Diameter of binary tree.
2. Check if tree is balanced.
3. Left view and right view.
4. Zigzag traversal.
5. Lowest common ancestor in binary tree.

## Hard

1. Serialize and deserialize binary tree.
2. Maximum path sum.
3. Boundary traversal.
4. Vertical order traversal.
5. Convert binary tree to doubly linked list.

## Challenge

Implement iterative preorder/inorder/postorder using explicit stack(s).

## Java 21 Exercise Example: Inorder Traversal

```java
import java.util.List;

public class InorderTraversal {
    static class Node {
        int v;
        Node l, r;
        Node(int x) { this.v = x; }
    }

    public static void inorder(Node root, List<Integer> out) {
        if (root == null) return;
        inorder(root.l, out);
        out.add(root.v);
        inorder(root.r, out);
    }
}
```
