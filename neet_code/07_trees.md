# Trees -- NeetCode 150

```java
class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode() {}
    TreeNode(int x) { val = x; }
    TreeNode(int x, TreeNode l, TreeNode r) { val = x; left = l; right = r; }
}
```

---

## Problem 46: Invert Binary Tree
**LeetCode #226**

### Brute Force -- O(N) BFS (queue-based)
```java
TreeNode invertTree(TreeNode root) {
    if (root == null) return null;
    Queue<TreeNode> q = new LinkedList<>();
    q.offer(root);
    while (!q.isEmpty()) {
        TreeNode n = q.poll();
        TreeNode tmp = n.left; n.left = n.right; n.right = tmp;
        if (n.left != null)  q.offer(n.left);
        if (n.right != null) q.offer(n.right);
    }
    return root;
}
```

### Optimal -- O(N) time, O(H) space (recursive DFS)
```java
TreeNode invertTree(TreeNode root) {
    if (root == null) return null;
    TreeNode tmp = root.left; root.left = root.right; root.right = tmp;
    invertTree(root.left); invertTree(root.right);
    return root;
}
```

---

## Problem 47: Maximum Depth of Binary Tree
**LeetCode #104**

### Brute Force -- O(N) BFS level counting
```java
int maxDepth(TreeNode root) {
    if (root == null) return 0;
    Queue<TreeNode> q = new LinkedList<>();
    q.offer(root);
    int depth = 0;
    while (!q.isEmpty()) {
        depth++;
        for (int i = q.size(); i > 0; i--) {
            TreeNode n = q.poll();
            if (n.left != null)  q.offer(n.left);
            if (n.right != null) q.offer(n.right);
        }
    }
    return depth;
}
```

### Optimal -- O(N) time, O(H) space (DFS)
```java
int maxDepth(TreeNode root) {
    if (root == null) return 0;
    return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
}
```

---

## Problem 48: Diameter of Binary Tree
**LeetCode #543** | Longest path (number of edges) between any two nodes.

### Brute Force -- O(N^2) (compute height for each node separately)
```java
int height(TreeNode r) { return r != null ? 1 + Math.max(height(r.left), height(r.right)) : 0; }
int diameterOfBinaryTree(TreeNode root) {
    if (root == null) return 0;
    int through = height(root.left) + height(root.right);
    return Math.max(through, Math.max(diameterOfBinaryTree(root.left), diameterOfBinaryTree(root.right)));
}
```

### Optimal -- O(N) time (single DFS, track global max)
```java
int diameterOfBinaryTree(TreeNode root) {
    int[] best = {0};
    dfs(root, best);
    return best[0];
}
int dfs(TreeNode n, int[] best) {
    if (n == null) return 0;
    int l = dfs(n.left, best), r = dfs(n.right, best);
    best[0] = Math.max(best[0], l + r);
    return 1 + Math.max(l, r);
}
```

---

## Problem 49: Balanced Binary Tree
**LeetCode #110** | Determine if tree is height-balanced (left/right heights differ by at most 1).

### Brute Force -- O(N^2) (compute height at each node)
```java
int ht(TreeNode r) { return r != null ? 1 + Math.max(ht(r.left), ht(r.right)) : 0; }
boolean isBalanced(TreeNode root) {
    if (root == null) return true;
    return Math.abs(ht(root.left) - ht(root.right)) <= 1 && isBalanced(root.left) && isBalanced(root.right);
}
```

### Optimal -- O(N) time (DFS returns -1 on imbalance)
```java
boolean isBalanced(TreeNode root) {
    return dfs(root) != -1;
}
int dfs(TreeNode n) {
    if (n == null) return 0;
    int l = dfs(n.left); if (l == -1) return -1;
    int r = dfs(n.right); if (r == -1) return -1;
    if (Math.abs(l - r) > 1) return -1;
    return 1 + Math.max(l, r);
}
```

---

## Problem 50: Same Tree
**LeetCode #100**

### Brute Force -- O(N) serialize both trees, compare strings
```java
String ser(TreeNode r) { return r != null ? r.val + "," + ser(r.left) + "," + ser(r.right) : "#"; }
boolean isSameTree(TreeNode p, TreeNode q) { return ser(p).equals(ser(q)); }
```

### Optimal -- O(N) time, O(H) space (recursive comparison)
```java
boolean isSameTree(TreeNode p, TreeNode q) {
    if (p == null && q == null) return true;
    if (p == null || q == null || p.val != q.val) return false;
    return isSameTree(p.left, q.left) && isSameTree(p.right, q.right);
}
```

---

## Problem 51: Subtree of Another Tree
**LeetCode #572**

### Brute Force -- O(S*T) check isSameTree at every node of s
```java
boolean isSame(TreeNode a, TreeNode b) {
    if (a == null && b == null) return true;
    if (a == null || b == null || a.val != b.val) return false;
    return isSame(a.left, b.left) && isSame(a.right, b.right);
}
boolean isSubtree(TreeNode root, TreeNode subRoot) {
    if (root == null) return false;
    if (isSame(root, subRoot)) return true;
    return isSubtree(root.left, subRoot) || isSubtree(root.right, subRoot);
}
```

### Optimal -- O(S*T) worst case same, but subtree check is clean single pass
```java
// Same as above -- O(S*T) is accepted; S, T <= 2000
// Truly optimal uses tree hashing/serialization: O(S+T) -- for reference:
// Serialize both trees (null markers included), check if sub-serialization of subRoot
// is a substring of root serialization.
```

---

## Problem 52: Lowest Common Ancestor of BST
**LeetCode #235**

### Brute Force -- O(N) generic LCA without using BST property
```java
TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
    if (root == null || root == p || root == q) return root;
    TreeNode l = lowestCommonAncestor(root.left, p, q);
    TreeNode r = lowestCommonAncestor(root.right, p, q);
    if (l != null && r != null) return root;
    return l != null ? l : r;
}
```

### Optimal -- O(H) time using BST property
```java
TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
    while (root != null) {
        if (p.val < root.val && q.val < root.val) root = root.left;
        else if (p.val > root.val && q.val > root.val) root = root.right;
        else return root;
    }
    return null;
}
```

---

## Problem 53: Binary Tree Level Order Traversal
**LeetCode #102**

### Brute Force -- O(N) DFS with depth tracking
```java
List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> res = new ArrayList<>();
    dfs(root, 0, res);
    return res;
}
void dfs(TreeNode n, int d, List<List<Integer>> res) {
    if (n == null) return;
    if (d == res.size()) res.add(new ArrayList<>());
    res.get(d).add(n.val);
    dfs(n.left, d + 1, res); dfs(n.right, d + 1, res);
}
```

### Optimal -- O(N) time, O(N) space (BFS)
```java
List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> res = new ArrayList<>();
    if (root == null) return res;
    Queue<TreeNode> q = new LinkedList<>();
    q.offer(root);
    while (!q.isEmpty()) {
        List<Integer> level = new ArrayList<>();
        for (int i = q.size(); i > 0; i--) {
            TreeNode n = q.poll();
            level.add(n.val);
            if (n.left != null)  q.offer(n.left);
            if (n.right != null) q.offer(n.right);
        }
        res.add(level);
    }
    return res;
}
```

---

## Problem 54: Binary Tree Right Side View
**LeetCode #199**

### Brute Force -- O(N) BFS, collect rightmost of each level
```java
List<Integer> rightSideView(TreeNode root) {
    List<Integer> res = new ArrayList<>();
    if (root == null) return res;
    Queue<TreeNode> q = new LinkedList<>();
    q.offer(root);
    while (!q.isEmpty()) {
        TreeNode last = null;
        for (int i = q.size(); i > 0; i--) {
            TreeNode n = q.poll();
            last = n;
            if (n.left != null)  q.offer(n.left);
            if (n.right != null) q.offer(n.right);
        }
        res.add(last.val);
    }
    return res;
}
```

### Optimal -- O(N) DFS (right child first, record first node per depth)
```java
List<Integer> rightSideView(TreeNode root) {
    List<Integer> res = new ArrayList<>();
    dfs(root, 0, res);
    return res;
}
void dfs(TreeNode n, int d, List<Integer> res) {
    if (n == null) return;
    if (d == res.size()) res.add(n.val);
    dfs(n.right, d + 1, res); dfs(n.left, d + 1, res);
}
```

---

## Problem 55: Count Good Nodes in Binary Tree
**LeetCode #1448** | Node X is "good" if no node on path from root to X has value > X.

### Brute Force -- O(N * H) (for each node, trace path to root)
```java
// Impractical without parent pointers; use DFS with max instead
```

### Optimal -- O(N) DFS tracking max value on path
```java
int goodNodes(TreeNode root) {
    int[] cnt = {0};
    dfs(root, Integer.MIN_VALUE, cnt);
    return cnt[0];
}
void dfs(TreeNode n, int mx, int[] cnt) {
    if (n == null) return;
    if (n.val >= mx) cnt[0]++;
    mx = Math.max(mx, n.val);
    dfs(n.left, mx, cnt); dfs(n.right, mx, cnt);
}
```

---

## Problem 56: Validate Binary Search Tree
**LeetCode #98**

### Brute Force -- O(N^2) (for each node verify all left < node < all right)
```java
boolean allLess(TreeNode r, int v)    { return r == null || (r.val < v && allLess(r.left, v) && allLess(r.right, v)); }
boolean allGreater(TreeNode r, int v) { return r == null || (r.val > v && allGreater(r.left, v) && allGreater(r.right, v)); }
boolean isValidBST(TreeNode root) {
    if (root == null) return true;
    return allLess(root.left, root.val) && allGreater(root.right, root.val)
        && isValidBST(root.left) && isValidBST(root.right);
}
```

### Optimal -- O(N) DFS with valid range [min, max]
```java
boolean isValidBST(TreeNode root) {
    return dfs(root, Long.MIN_VALUE, Long.MAX_VALUE);
}
boolean dfs(TreeNode n, long lo, long hi) {
    if (n == null) return true;
    if (n.val <= lo || n.val >= hi) return false;
    return dfs(n.left, lo, n.val) && dfs(n.right, n.val, hi);
}
```

---

## Problem 57: Kth Smallest Element in BST
**LeetCode #230**

### Brute Force -- O(N) collect all values in list, return k-th (inorder is sorted)
```java
int kthSmallest(TreeNode root, int k) {
    List<Integer> vals = new ArrayList<>();
    inorder(root, vals);
    return vals.get(k - 1);
}
void inorder(TreeNode n, List<Integer> vals) {
    if (n == null) return;
    inorder(n.left, vals);
    vals.add(n.val);
    inorder(n.right, vals);
}
```

### Optimal -- O(H+k) inorder traversal, stop at k-th node
```java
int kthSmallest(TreeNode root, int k) {
    int[] state = {0, 0}; // {count, result}
    inorder(root, k, state);
    return state[1];
}
void inorder(TreeNode n, int k, int[] state) {
    if (n == null || state[0] >= k) return;
    inorder(n.left, k, state);
    if (++state[0] == k) { state[1] = n.val; return; }
    inorder(n.right, k, state);
}
```

---

## Problem 58: Construct Binary Tree from Preorder and Inorder Traversal
**LeetCode #105**

### Brute Force -- O(N^2) (linear search for root in inorder each time)
```java
TreeNode buildTree(int[] pre, int[] in) {
    return build(pre, in, 0, pre.length - 1, 0, in.length - 1);
}
TreeNode build(int[] pre, int[] in, int ps, int pe, int is, int ie) {
    if (ps > pe) return null;
    TreeNode root = new TreeNode(pre[ps]);
    int idx = is;
    while (in[idx] != pre[ps]) idx++;
    int leftSz = idx - is;
    root.left  = build(pre, in, ps + 1, ps + leftSz, is, idx - 1);
    root.right = build(pre, in, ps + leftSz + 1, pe, idx + 1, ie);
    return root;
}
```

### Optimal -- O(N) with hash map for inorder index lookup
```java
private int pi = 0;
TreeNode buildTree(int[] pre, int[] in) {
    Map<Integer, Integer> idx = new HashMap<>();
    for (int i = 0; i < in.length; i++) idx.put(in[i], i);
    pi = 0;
    return build(pre, idx, 0, in.length - 1);
}
TreeNode build(int[] pre, Map<Integer, Integer> idx, int l, int r) {
    if (l > r) return null;
    TreeNode root = new TreeNode(pre[pi++]);
    int mid = idx.get(root.val);
    root.left  = build(pre, idx, l, mid - 1);
    root.right = build(pre, idx, mid + 1, r);
    return root;
}
```

---

## Problem 59: Binary Tree Maximum Path Sum
**LeetCode #124** | Path can start/end at any node; find max sum.

### Brute Force -- O(N^2) (try every pair of nodes as path endpoints)
```java
// Difficult to implement cleanly; DFS approach is both correct and optimal
```

### Optimal -- O(N) DFS, at each node try including both children
```java
int maxPathSum(TreeNode root) {
    int[] best = {Integer.MIN_VALUE};
    dfs(root, best);
    return best[0];
}
int dfs(TreeNode n, int[] best) {
    if (n == null) return 0;
    int l = Math.max(0, dfs(n.left, best)), r = Math.max(0, dfs(n.right, best));
    best[0] = Math.max(best[0], n.val + l + r); // path through n
    return n.val + Math.max(l, r);              // return best single-branch to parent
}
```

---

## Problem 60: Serialize and Deserialize Binary Tree
**LeetCode #297**

### Brute Force -- O(N) level-order (BFS) serialization
```java
String serialize(TreeNode root) {
    if (root == null) return "";
    StringBuilder res = new StringBuilder();
    Queue<TreeNode> q = new LinkedList<>();
    q.offer(root);
    while (!q.isEmpty()) {
        TreeNode n = q.poll();
        if (n != null) { res.append(n.val).append(","); q.offer(n.left); q.offer(n.right); }
        else res.append("#,");
    }
    return res.toString();
}
TreeNode deserialize(String data) {
    if (data.isEmpty()) return null;
    String[] tokens = data.split(",");
    TreeNode root = new TreeNode(Integer.parseInt(tokens[0]));
    Queue<TreeNode> q = new LinkedList<>();
    q.offer(root);
    int i = 1;
    while (!q.isEmpty() && i < tokens.length) {
        TreeNode n = q.poll();
        if (!tokens[i].equals("#")) { n.left = new TreeNode(Integer.parseInt(tokens[i])); q.offer(n.left); }
        i++;
        if (i < tokens.length && !tokens[i].equals("#")) { n.right = new TreeNode(Integer.parseInt(tokens[i])); q.offer(n.right); }
        i++;
    }
    return root;
}
```

### Optimal -- O(N) preorder DFS serialization (cleaner, same complexity)
```java
String serialize(TreeNode root) {
    if (root == null) return "#,";
    return root.val + "," + serialize(root.left) + serialize(root.right);
}
TreeNode deserialize(String data) {
    Deque<String> tokens = new ArrayDeque<>(Arrays.asList(data.split(",")));
    return build(tokens);
}
TreeNode build(Deque<String> tokens) {
    String tok = tokens.poll();
    if (tok.equals("#")) return null;
    TreeNode n = new TreeNode(Integer.parseInt(tok));
    n.left = build(tokens); n.right = build(tokens);
    return n;
}
```
