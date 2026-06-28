# Backtracking -- NeetCode 150

---

## Problem 71: Subsets
**LeetCode #78** | Return all subsets (power set) of distinct integers.

### Brute Force -- O(N * 2^N) bitmask iteration
```java
List<List<Integer>> subsets(int[] nums) {
    int n = nums.length;
    List<List<Integer>> res = new ArrayList<>();
    for (int mask = 0; mask < (1 << n); mask++) {
        List<Integer> sub = new ArrayList<>();
        for (int i = 0; i < n; i++) if (((mask >> i) & 1) == 1) sub.add(nums[i]);
        res.add(sub);
    }
    return res;
}
```

### Optimal -- O(N * 2^N) backtracking
```java
List<List<Integer>> subsets(int[] nums) {
    List<List<Integer>> res = new ArrayList<>();
    List<Integer> cur = new ArrayList<>();
    backtrack(nums, 0, cur, res);
    return res;
}
void backtrack(int[] nums, int i, List<Integer> cur, List<List<Integer>> res) {
    res.add(new ArrayList<>(cur));
    for (int j = i; j < nums.length; j++) {
        cur.add(nums[j]);
        backtrack(nums, j + 1, cur, res);
        cur.remove(cur.size() - 1);
    }
}
```

---

## Problem 72: Combination Sum
**LeetCode #39** | Find all combinations that sum to target (reuse allowed).

### Brute Force -- generate all combinations up to target length, filter
```java
// Impractical -- use backtracking directly
```

### Optimal -- O(2^(T/min)) backtracking with pruning
```java
List<List<Integer>> combinationSum(int[] candidates, int target) {
    Arrays.sort(candidates);
    List<List<Integer>> res = new ArrayList<>();
    List<Integer> cur = new ArrayList<>();
    backtrack(candidates, 0, target, cur, res);
    return res;
}
void backtrack(int[] candidates, int i, int rem, List<Integer> cur, List<List<Integer>> res) {
    if (rem == 0) { res.add(new ArrayList<>(cur)); return; }
    for (int j = i; j < candidates.length && candidates[j] <= rem; j++) {
        cur.add(candidates[j]);
        backtrack(candidates, j, rem - candidates[j], cur, res);
        cur.remove(cur.size() - 1);
    }
}
```

---

## Problem 73: Combination Sum II
**LeetCode #40** | Each number used once; no duplicate combinations.

### Brute Force -- generate with duplicates, deduplicate using set
```java
List<List<Integer>> combinationSum2(int[] c, int t) {
    Arrays.sort(c);
    Set<List<Integer>> res = new HashSet<>();
    List<Integer> cur = new ArrayList<>();
    backtrack(c, 0, t, cur, res);
    return new ArrayList<>(res);
}
void backtrack(int[] c, int i, int rem, List<Integer> cur, Set<List<Integer>> res) {
    if (rem == 0) { res.add(new ArrayList<>(cur)); return; }
    for (int j = i; j < c.length && c[j] <= rem; j++) {
        cur.add(c[j]);
        backtrack(c, j + 1, rem - c[j], cur, res);
        cur.remove(cur.size() - 1);
    }
}
```

### Optimal -- O(2^N) backtracking, skip duplicates at same level
```java
List<List<Integer>> combinationSum2(int[] c, int t) {
    Arrays.sort(c);
    List<List<Integer>> res = new ArrayList<>();
    List<Integer> cur = new ArrayList<>();
    backtrack(c, 0, t, cur, res);
    return res;
}
void backtrack(int[] c, int i, int rem, List<Integer> cur, List<List<Integer>> res) {
    if (rem == 0) { res.add(new ArrayList<>(cur)); return; }
    for (int j = i; j < c.length && c[j] <= rem; j++) {
        if (j > i && c[j] == c[j - 1]) continue; // skip duplicates
        cur.add(c[j]);
        backtrack(c, j + 1, rem - c[j], cur, res);
        cur.remove(cur.size() - 1);
    }
}
```

---

## Problem 74: Permutations
**LeetCode #46** | Return all permutations of distinct integers.

### Brute Force -- O(N! * N) using next_permutation
```java
// Java has no next_permutation; implement Heap's algorithm or backtrack directly.
// The optimal backtracking below is the idiomatic Java approach.
```

### Optimal -- O(N * N!) backtracking with visited array
```java
List<List<Integer>> permute(int[] nums) {
    List<List<Integer>> res = new ArrayList<>();
    List<Integer> cur = new ArrayList<>();
    boolean[] used = new boolean[nums.length];
    backtrack(nums, used, cur, res);
    return res;
}
void backtrack(int[] nums, boolean[] used, List<Integer> cur, List<List<Integer>> res) {
    if (cur.size() == nums.length) { res.add(new ArrayList<>(cur)); return; }
    for (int i = 0; i < nums.length; i++) {
        if (used[i]) continue;
        used[i] = true; cur.add(nums[i]);
        backtrack(nums, used, cur, res);
        cur.remove(cur.size() - 1); used[i] = false;
    }
}
```

---

## Problem 75: Subsets II
**LeetCode #90** | Array may have duplicates; return all unique subsets.

### Brute Force -- generate all, deduplicate with set
```java
List<List<Integer>> subsetsWithDup(int[] nums) {
    Arrays.sort(nums);
    Set<List<Integer>> res = new HashSet<>();
    List<Integer> cur = new ArrayList<>();
    backtrack(nums, 0, cur, res);
    return new ArrayList<>(res);
}
void backtrack(int[] nums, int i, List<Integer> cur, Set<List<Integer>> res) {
    res.add(new ArrayList<>(cur));
    for (int j = i; j < nums.length; j++) {
        cur.add(nums[j]);
        backtrack(nums, j + 1, cur, res);
        cur.remove(cur.size() - 1);
    }
}
```

### Optimal -- sort + skip same element at same depth level
```java
List<List<Integer>> subsetsWithDup(int[] nums) {
    Arrays.sort(nums);
    List<List<Integer>> res = new ArrayList<>();
    List<Integer> cur = new ArrayList<>();
    backtrack(nums, 0, cur, res);
    return res;
}
void backtrack(int[] nums, int i, List<Integer> cur, List<List<Integer>> res) {
    res.add(new ArrayList<>(cur));
    for (int j = i; j < nums.length; j++) {
        if (j > i && nums[j] == nums[j - 1]) continue;
        cur.add(nums[j]);
        backtrack(nums, j + 1, cur, res);
        cur.remove(cur.size() - 1);
    }
}
```

---

## Problem 76: Word Search
**LeetCode #79** | Find if word exists in board using adjacent cells.

### Brute Force -- O(M*N*4^L) DFS from every cell
```java
boolean exist(char[][] board, String word) {
    int m = board.length, n = board[0].length;
    for (int r = 0; r < m; r++)
        for (int c = 0; c < n; c++)
            if (dfs(board, word, r, c, 0)) return true;
    return false;
}
boolean dfs(char[][] board, String word, int r, int c, int i) {
    if (i == word.length()) return true;
    if (r < 0 || r >= board.length || c < 0 || c >= board[0].length || board[r][c] != word.charAt(i))
        return false;
    char tmp = board[r][c]; board[r][c] = '#';
    boolean ok = dfs(board, word, r + 1, c, i + 1) || dfs(board, word, r - 1, c, i + 1)
              || dfs(board, word, r, c + 1, i + 1) || dfs(board, word, r, c - 1, i + 1);
    board[r][c] = tmp; return ok;
}
```

### Optimal -- same DFS but with early termination; this is already optimal O(M*N*4^L)
```java
// Add pruning: check character frequency first
boolean exist(char[][] board, String word) {
    int m = board.length, n = board[0].length;
    int[] cnt = new int[128];
    for (char[] row : board) for (char c : row) cnt[c]++;
    for (char c : word.toCharArray()) if (--cnt[c] < 0) return false; // frequency pruning
    if (word.charAt(0) != word.charAt(word.length() - 1)) { // reverse if last char rarer (fewer starts)
        int fc = 0, lc = 0;
        for (char[] row : board) for (char c : row) {
            if (c == word.charAt(0)) fc++;
            if (c == word.charAt(word.length() - 1)) lc++;
        }
        if (lc < fc) word = new StringBuilder(word).reverse().toString();
    }
    for (int r = 0; r < m; r++)
        for (int c = 0; c < n; c++)
            if (dfs(board, word, r, c, 0)) return true;
    return false;
}
boolean dfs(char[][] board, String word, int r, int c, int i) {
    if (i == word.length()) return true;
    if (r < 0 || r >= board.length || c < 0 || c >= board[0].length || board[r][c] != word.charAt(i))
        return false;
    char tmp = board[r][c]; board[r][c] = '#';
    boolean ok = dfs(board, word, r + 1, c, i + 1) || dfs(board, word, r - 1, c, i + 1)
              || dfs(board, word, r, c + 1, i + 1) || dfs(board, word, r, c - 1, i + 1);
    board[r][c] = tmp; return ok;
}
```

---

## Problem 77: Palindrome Partitioning
**LeetCode #131** | Partition s so every substring is a palindrome; return all partitions.

### Brute Force -- O(N * 2^N) generate all partitions, check each piece
```java
boolean isPalin(String s, int l, int r) {
    while (l < r) if (s.charAt(l++) != s.charAt(r--)) return false;
    return true;
}
List<List<String>> partition(String s) {
    List<List<String>> res = new ArrayList<>();
    List<String> cur = new ArrayList<>();
    backtrack(s, 0, cur, res);
    return res;
}
void backtrack(String s, int i, List<String> cur, List<List<String>> res) {
    if (i == s.length()) { res.add(new ArrayList<>(cur)); return; }
    for (int j = i; j < s.length(); j++) {
        if (isPalin(s, i, j)) {
            cur.add(s.substring(i, j + 1));
            backtrack(s, j + 1, cur, res);
            cur.remove(cur.size() - 1);
        }
    }
}
```

### Optimal -- precompute palindrome DP, then backtrack O(N * 2^N)
```java
List<List<String>> partition(String s) {
    int n = s.length();
    boolean[][] dp = new boolean[n][n];
    for (int i = n - 1; i >= 0; i--)
        for (int j = i; j < n; j++)
            dp[i][j] = (s.charAt(i) == s.charAt(j)) && (j - i <= 2 || dp[i + 1][j - 1]);
    List<List<String>> res = new ArrayList<>();
    List<String> cur = new ArrayList<>();
    backtrack(s, 0, dp, cur, res);
    return res;
}
void backtrack(String s, int i, boolean[][] dp, List<String> cur, List<List<String>> res) {
    if (i == s.length()) { res.add(new ArrayList<>(cur)); return; }
    for (int j = i; j < s.length(); j++) {
        if (dp[i][j]) {
            cur.add(s.substring(i, j + 1));
            backtrack(s, j + 1, dp, cur, res);
            cur.remove(cur.size() - 1);
        }
    }
}
```

---

## Problem 78: Letter Combinations of a Phone Number
**LeetCode #17**

### Brute Force -- O(4^N * N) iterative product
```java
List<String> letterCombinations(String digits) {
    if (digits.isEmpty()) return new ArrayList<>();
    String[] mp = {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
    List<String> res = new ArrayList<>(List.of(""));
    for (char d : digits.toCharArray()) {
        List<String> tmp = new ArrayList<>();
        for (String s : res)
            for (char c : mp[d - '0'].toCharArray()) tmp.add(s + c);
        res = tmp;
    }
    return res;
}
```

### Optimal -- O(4^N * N) backtracking (same complexity, less memory)
```java
List<String> letterCombinations(String digits) {
    if (digits.isEmpty()) return new ArrayList<>();
    String[] mp = {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
    List<String> res = new ArrayList<>();
    backtrack(digits, 0, mp, new StringBuilder(), res);
    return res;
}
void backtrack(String digits, int i, String[] mp, StringBuilder cur, List<String> res) {
    if (i == digits.length()) { res.add(cur.toString()); return; }
    for (char c : mp[digits.charAt(i) - '0'].toCharArray()) {
        cur.append(c);
        backtrack(digits, i + 1, mp, cur, res);
        cur.deleteCharAt(cur.length() - 1);
    }
}
```

---

## Problem 79: N-Queens
**LeetCode #51** | Place N queens on NxN board so no two attack each other.

### Brute Force -- O(N! * N) try all permutations of column placements, validate
```java
// Generate all permutations of 0..N-1 as column positions, check diagonals
```

### Optimal -- O(N!) backtracking with 3 sets for cols/diags/anti-diags
```java
List<List<String>> solveNQueens(int n) {
    List<List<String>> res = new ArrayList<>();
    char[][] board = new char[n][n];
    for (char[] row : board) Arrays.fill(row, '.');
    Set<Integer> cols = new HashSet<>(), diag = new HashSet<>(), anti = new HashSet<>();
    backtrack(0, n, board, cols, diag, anti, res);
    return res;
}
void backtrack(int r, int n, char[][] board, Set<Integer> cols, Set<Integer> diag,
               Set<Integer> anti, List<List<String>> res) {
    if (r == n) {
        List<String> snapshot = new ArrayList<>();
        for (char[] row : board) snapshot.add(new String(row));
        res.add(snapshot);
        return;
    }
    for (int c = 0; c < n; c++) {
        if (cols.contains(c) || diag.contains(r - c) || anti.contains(r + c)) continue;
        cols.add(c); diag.add(r - c); anti.add(r + c); board[r][c] = 'Q';
        backtrack(r + 1, n, board, cols, diag, anti, res);
        cols.remove(c); diag.remove(r - c); anti.remove(r + c); board[r][c] = '.';
    }
}
```
