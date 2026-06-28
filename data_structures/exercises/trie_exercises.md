# Exercises: Trie

## Easy

1. Insert and search words.
2. Prefix check (startsWith).
3. Count words with given prefix.
4. Delete a word from trie.
5. Print all words stored in lexicographic order.

## Medium

1. Longest common prefix of words.
2. Replace words in sentence by root dictionary.
3. Word dictionary with wildcard dot character.
4. Count distinct substrings using trie.
5. Auto-complete top suggestions.

## Hard

1. Implement compressed trie (radix tree) overview.
2. Maximum xor pair using bitwise trie.
3. Streaming dictionary matcher.
4. Word break with trie + DP.
5. Multi-pattern search (Aho-Corasick intro).

## Challenge

Design contact search engine with ranked prefix suggestions.

## Java 21 Exercise Example: Trie Insert/Search

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Trie {
    static class Node {
        int[] nxt = new int[26];
        boolean end;
        Node() { Arrays.fill(nxt, -1); }
    }

    List<Node> t = new ArrayList<>(List.of(new Node()));

    void insert(String s) {
        int v = 0;
        for (char c : s.toCharArray()) {
            int x = c - 'a';
            if (t.get(v).nxt[x] == -1) {
                t.get(v).nxt[x] = t.size();
                t.add(new Node());
            }
            v = t.get(v).nxt[x];
        }
        t.get(v).end = true;
    }
}
```
