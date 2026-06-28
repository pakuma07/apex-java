// trees.java
// Tree-based structures: an unbalanced Binary Search Tree (insert/search/inorder),
// a min-heap via PriorityQueue, and a 26-way Trie for lowercase strings.

import java.util.PriorityQueue;

public class trees {

    // BSTNode: a single binary-search-tree node holding a key and two child references.
    static class BSTNode {
        int key;
        BSTNode left, right;
        BSTNode(int k) { key = k; left = null; right = null; }
    }

    // Insert key into the BST, returning the (possibly new) subtree root.
    // Invariant: left keys < node key < right keys; duplicates ignored.
    // O(h) time, h = tree height (O(log n) if balanced, O(n) worst case).
    static BSTNode insertBST(BSTNode root, int key) {
        if (root == null) return new BSTNode(key);                       // empty spot: create the node
        if (key < root.key) root.left = insertBST(root.left, key);       // go left for smaller keys
        else if (key > root.key) root.right = insertBST(root.right, key);// go right for larger
        return root;                                                     // equal key: no-op, return current root
    }

    // Search for key, following the BST ordering to prune half the tree each step. O(h).
    static boolean searchBST(BSTNode root, int key) {
        if (root == null) return false;                       // fell off the tree: not present
        if (root.key == key) return true;
        if (key < root.key) return searchBST(root.left, key); // smaller -> left subtree
        return searchBST(root.right, key);                    // larger -> right subtree
    }

    // In-order traversal (left, node, right) prints keys in sorted ascending order. O(n).
    static void inorder(BSTNode root) {
        if (root == null) return;
        inorder(root.left);
        System.out.print(root.key + " ");
        inorder(root.right);
    }

    // TrieNode: node in a prefix tree; one child slot per lowercase letter ('a'..'z').
    // isEnd marks that some inserted word terminates here.
    static class TrieNode {
        boolean isEnd = false;
        TrieNode[] children = new TrieNode[26];  // start with no children (all null)
    }

    // Insert word by walking/creating one node per character. O(L) for word length L.
    static void trieInsert(TrieNode root, String word) {
        TrieNode cur = root;
        for (int i = 0; i < word.length(); ++i) {
            int idx = word.charAt(i) - 'a';                              // map letter to child slot 0..25
            if (cur.children[idx] == null) cur.children[idx] = new TrieNode();  // create branch if missing
            cur = cur.children[idx];                                     // descend
        }
        cur.isEnd = true;  // mark the final node as a complete word
    }

    // Search for an exact word: follow each character, then require an end marker. O(L).
    static boolean trieSearch(TrieNode root, String word) {
        TrieNode cur = root;
        for (int i = 0; i < word.length(); ++i) {
            int idx = word.charAt(i) - 'a';
            if (cur.children[idx] == null) return false;  // missing branch -> word absent
            cur = cur.children[idx];
        }
        return cur.isEnd;  // path exists; true only if it marks a full word (not just a prefix)
    }

    // Driver: exercises the BST, a min-heap, and the Trie, printing sample results.
    // (Java is garbage collected, so there are no freeBST/freeTrie cleanup passes.)
    public static void main(String[] a) {
        System.out.println("== Binary Search Tree ==");
        BSTNode root = null;
        int[] keys = {15, 8, 22, 4, 10, 18, 30};
        for (int k : keys) root = insertBST(root, k);

        System.out.print("BST inorder: ");
        inorder(root);
        System.out.println();
        System.out.println("search 10: " + (searchBST(root, 10) ? "found" : "not found"));

        System.out.println("\n== Min Heap (priority_queue) ==");
        // Java's PriorityQueue is a min-heap by default (smallest on top).
        // add/poll are O(log n); peek is O(1).
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        minHeap.add(5);
        minHeap.add(2);
        minHeap.add(8);
        minHeap.add(1);
        System.out.print("heap pop order: ");
        while (!minHeap.isEmpty()) {
            System.out.print(minHeap.peek() + " ");
            minHeap.poll();
        }
        System.out.println();

        System.out.println("\n== Trie ==");
        TrieNode trieRoot = new TrieNode();
        trieInsert(trieRoot, "cat");
        trieInsert(trieRoot, "car");
        trieInsert(trieRoot, "dog");

        System.out.println("search cat: " + (trieSearch(trieRoot, "cat") ? "yes" : "no"));
        System.out.println("search cap: " + (trieSearch(trieRoot, "cap") ? "yes" : "no"));
    }
}
