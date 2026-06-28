# Tries -- NeetCode 150

---

## Problem 61: Implement Trie (Prefix Tree)
**LeetCode #208** | insert, search, startsWith in O(L).

### Brute Force -- O(N*L) search using a set of strings
```java
class Trie {
    private Set<String> words = new HashSet<>(), prefixes = new HashSet<>();
    public void insert(String w) {
        words.add(w);
        for (int i = 1; i <= w.length(); i++) prefixes.add(w.substring(0, i));
    }
    public boolean search(String w)     { return words.contains(w); }
    public boolean startsWith(String p) { return prefixes.contains(p); }
}
```

### Optimal -- O(L) per operation, O(ALPHA * L * N) space
```java
class Trie {
    private static class Node {
        Node[] ch = new Node[26];
        boolean end = false;
    }
    private final Node root = new Node();
    public void insert(String w) {
        Node n = root;
        for (char c : w.toCharArray()) {
            int i = c - 'a';
            if (n.ch[i] == null) n.ch[i] = new Node();
            n = n.ch[i];
        }
        n.end = true;
    }
    public boolean search(String w) {
        Node n = root;
        for (char c : w.toCharArray()) {
            int i = c - 'a';
            if (n.ch[i] == null) return false;
            n = n.ch[i];
        }
        return n.end;
    }
    public boolean startsWith(String p) {
        Node n = root;
        for (char c : p.toCharArray()) {
            int i = c - 'a';
            if (n.ch[i] == null) return false;
            n = n.ch[i];
        }
        return true;
    }
}
```

---

## Problem 62: Design Add and Search Words Data Structure
**LeetCode #211** | addWord, search (where '.' matches any letter).

### Brute Force -- O(N*L^2) search through all words
```java
class WordDictionary {
    private List<String> words = new ArrayList<>();
    public void addWord(String w) { words.add(w); }
    public boolean search(String w) {
        for (String s : words) {
            if (s.length() != w.length()) continue;
            boolean ok = true;
            for (int i = 0; i < w.length(); i++)
                if (w.charAt(i) != '.' && w.charAt(i) != s.charAt(i)) { ok = false; break; }
            if (ok) return true;
        }
        return false;
    }
}
```

### Optimal -- O(26^M) worst case for '.', O(L) average (Trie with DFS on '.')
```java
class WordDictionary {
    private static class Node {
        Node[] ch = new Node[26];
        boolean end = false;
    }
    private final Node root = new Node();
    public void addWord(String w) {
        Node n = root;
        for (char c : w.toCharArray()) {
            int i = c - 'a';
            if (n.ch[i] == null) n.ch[i] = new Node();
            n = n.ch[i];
        }
        n.end = true;
    }
    public boolean search(String w) {
        return dfs(root, w, 0);
    }
    private boolean dfs(Node n, String w, int i) {
        if (i == w.length()) return n.end;
        char c = w.charAt(i);
        if (c == '.') {
            for (int j = 0; j < 26; j++)
                if (n.ch[j] != null && dfs(n.ch[j], w, i + 1)) return true;
            return false;
        }
        if (n.ch[c - 'a'] == null) return false;
        return dfs(n.ch[c - 'a'], w, i + 1);
    }
}
```

---

## Problem 63: Word Search II
**LeetCode #212** | Find all words from a list that exist in a board.

### Brute Force -- O(W * M*N * 4^L) run word search for each word separately
```java
boolean dfs(char[][] b, String w, int i, int r, int c) {
    if (i == w.length()) return true;
    if (r < 0 || r >= b.length || c < 0 || c >= b[0].length || b[r][c] != w.charAt(i)) return false;
    char tmp = b[r][c]; b[r][c] = '#';
    boolean ok = dfs(b, w, i + 1, r + 1, c) || dfs(b, w, i + 1, r - 1, c)
              || dfs(b, w, i + 1, r, c + 1) || dfs(b, w, i + 1, r, c - 1);
    b[r][c] = tmp;
    return ok;
}
List<String> findWordsBF(char[][] board, String[] words) {
    List<String> res = new ArrayList<>();
    for (String w : words) {
        boolean found = false;
        for (int r = 0; r < board.length && !found; r++)
            for (int c = 0; c < board[0].length && !found; c++)
                if (dfs(board, w, 0, r, c)) { res.add(w); found = true; }
    }
    return res;
}
```

### Optimal -- O(M*N*4*3^(L-1)) build Trie of all words, single board DFS
```java
private static class Node {
    Node[] ch = new Node[26];
    String word = null;
}
List<String> findWords(char[][] board, String[] words) {
    Node root = new Node();
    for (String w : words) {
        Node n = root;
        for (char c : w.toCharArray()) {
            int i = c - 'a';
            if (n.ch[i] == null) n.ch[i] = new Node();
            n = n.ch[i];
        }
        n.word = w;
    }
    int m = board.length, n = board[0].length;
    List<String> res = new ArrayList<>();
    for (int r = 0; r < m; r++)
        for (int c = 0; c < n; c++)
            dfs(board, root, r, c, res);
    return res;
}
void dfs(char[][] board, Node node, int r, int c, List<String> res) {
    if (r < 0 || r >= board.length || c < 0 || c >= board[0].length || board[r][c] == '#') return;
    char ch = board[r][c];
    Node next = node.ch[ch - 'a'];
    if (next == null) return;
    if (next.word != null) { res.add(next.word); next.word = null; }
    board[r][c] = '#';
    dfs(board, next, r + 1, c, res);
    dfs(board, next, r - 1, c, res);
    dfs(board, next, r, c + 1, res);
    dfs(board, next, r, c - 1, res);
    board[r][c] = ch;
}
```
