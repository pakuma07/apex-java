# 07 — String Algorithms

> Java notes: `String` is immutable and `charAt` is fine, but for tight inner loops convert once with `char[] cs = s.toCharArray()` and index `cs[i]`. Build output with `StringBuilder`. Use `long` for hashes.

---

## 7.1 KMP (Knuth-Morris-Pratt) — O(N + M)

**Use for**: pattern matching, find all occurrences of pattern in text.

```java
// Build failure function (prefix function)
static int[] buildKMP(String pat) {
    int m = pat.length();
    int[] fail = new int[m];
    for (int i = 1; i < m; ++i) {
        int j = fail[i - 1];
        while (j > 0 && pat.charAt(i) != pat.charAt(j)) j = fail[j - 1];
        if (pat.charAt(i) == pat.charAt(j)) j++;
        fail[i] = j;
    }
    return fail;
}

// Search: returns all starting positions (0-indexed)
static ArrayList<Integer> kmpSearch(String text, String pat) {
    int[] fail = buildKMP(pat);
    ArrayList<Integer> matches = new ArrayList<>();
    int j = 0;
    for (int i = 0; i < text.length(); ++i) {
        while (j > 0 && text.charAt(i) != pat.charAt(j)) j = fail[j - 1];
        if (text.charAt(i) == pat.charAt(j)) j++;
        if (j == pat.length()) {
            matches.add(i - j + 1);
            j = fail[j - 1];
        }
    }
    return matches;
}

// Useful property: fail[m-1] = length of longest proper prefix = suffix
// Period of string s: m - fail[m-1]  (if m % period == 0, s is periodic)
```

---

## 7.2 Z-Function — O(N)

**Use for**: pattern matching (same power as KMP, often simpler), count occurrences, string period.

`z[i]` = length of the longest string starting at `s[i]` that is also a prefix of `s`.

```java
static int[] zFunction(String s) {
    int n = s.length();
    int[] z = new int[n];
    z[0] = n;
    int l = 0, r = 0;
    for (int i = 1; i < n; ++i) {
        if (i < r) z[i] = Math.min(r - i, z[i - l]);
        while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) z[i]++;
        if (i + z[i] > r) { l = i; r = i + z[i]; }
    }
    return z;
}

// Pattern matching: search for pat in text.
// Build s = pat + "#" + text, compute z-function;
// z[i] == pat.length() means pattern found at text[i - pat.length() - 1].
static ArrayList<Integer> zSearch(String text, String pat) {
    String s = pat + "#" + text;
    int[] z = zFunction(s);
    int m = pat.length();
    ArrayList<Integer> matches = new ArrayList<>();
    for (int i = m + 1; i < s.length(); ++i)
        if (z[i] == m) matches.add(i - m - 1);
    return matches;
}
```

---

## 7.3 Hashing — O(N) build, O(1) query

**Use for**: fast string comparison, finding duplicate substrings, rolling hash.

```java
// Double hashing to resist collisions. long holds the intermediate products safely.
static final long BASE = 131, MOD1 = 1_000_000_007L, MOD2 = 1_000_000_009L;

static class DoubleHash {
    int n;
    long[] h1, h2, pw1, pw2;

    DoubleHash(String s) {
        n = s.length();
        h1 = new long[n + 1]; h2 = new long[n + 1];
        pw1 = new long[n + 1]; pw2 = new long[n + 1];
        pw1[0] = pw2[0] = 1;
        for (int i = 0; i < n; ++i) {
            char c = s.charAt(i);
            h1[i+1] = (h1[i] * BASE + c) % MOD1;
            h2[i+1] = (h2[i] * BASE + c) % MOD2;
            pw1[i+1] = pw1[i] * BASE % MOD1;
            pw2[i+1] = pw2[i] * BASE % MOD2;
        }
    }

    // Combined hash of s[l..r] (0-indexed, inclusive), packed into one long.
    long get(int l, int r) {
        long v1 = (h1[r+1] - h1[l] * pw1[r-l+1] % MOD1 + MOD1 * 2) % MOD1;
        long v2 = (h2[r+1] - h2[l] * pw2[r-l+1] % MOD2 + MOD2 * 2) % MOD2;
        return v1 * MOD2 + v2;   // pack two 30-bit hashes into one long key
    }

    boolean equal(int l1, int r1, int l2, int r2) {
        return get(l1, r1) == get(l2, r2);
    }
}
```

> Anti-hack tip: choose `BASE` (and optionally the moduli) at runtime from a random source so the test setter cannot pre-compute a colliding input.

---

## 7.4 Trie (Prefix Tree)

**Use for**: autocomplete, prefix queries, XOR maximisation.

```java
static class Trie {
    // Flat arrays scale better than per-node objects in Java.
    int[][] children;   // children[node][c]
    boolean[] isEnd;
    int[] count;        // number of strings passing through
    int size = 1;       // node 0 is the root

    Trie(int maxNodes) {
        children = new int[maxNodes][26];
        for (int[] row : children) Arrays.fill(row, -1);
        isEnd = new boolean[maxNodes];
        count = new int[maxNodes];
    }

    void insert(String s) {
        int cur = 0;
        for (int i = 0; i < s.length(); ++i) {
            int idx = s.charAt(i) - 'a';
            if (children[cur][idx] == -1) {
                children[cur][idx] = size;
                Arrays.fill(children[size], -1);
                size++;
            }
            cur = children[cur][idx];
            count[cur]++;
        }
        isEnd[cur] = true;
    }

    boolean search(String s) {
        int cur = 0;
        for (int i = 0; i < s.length(); ++i) {
            int idx = s.charAt(i) - 'a';
            if (children[cur][idx] == -1) return false;
            cur = children[cur][idx];
        }
        return isEnd[cur];
    }

    int countPrefix(String prefix) {
        int cur = 0;
        for (int i = 0; i < prefix.length(); ++i) {
            int idx = prefix.charAt(i) - 'a';
            if (children[cur][idx] == -1) return 0;
            cur = children[cur][idx];
        }
        return count[cur];
    }
}

// Binary Trie for XOR maximum — store numbers bit by bit (high to low)
// Same structure but children[node][0..1] for bits.
```

> In Java, allocating one object per Trie node is slow and memory-heavy. The flat `int[][]` arrays above (a "node pool") are the idiomatic CP form.

---

## 7.5 Manacher's Algorithm — O(N)

**Use for**: longest palindromic substring.

```java
// Returns the longest palindromic substring of s.
static String manacher(String s) {
    // Transform: abc → #a#b#c#  (handles even/odd uniformly)
    StringBuilder tb = new StringBuilder("#");
    for (int i = 0; i < s.length(); ++i) tb.append(s.charAt(i)).append('#');
    char[] t = tb.toString().toCharArray();
    int n = t.length;
    int[] p = new int[n];
    int c = 0, r = 0;
    for (int i = 0; i < n; ++i) {
        if (i < r) p[i] = Math.min(r - i, p[2*c - i]);
        while (i - p[i] - 1 >= 0 && i + p[i] + 1 < n && t[i-p[i]-1] == t[i+p[i]+1])
            p[i]++;
        if (i + p[i] > r) { c = i; r = i + p[i]; }
    }
    int maxLen = 0, centre = 0;
    for (int i = 0; i < n; ++i) if (p[i] > maxLen) { maxLen = p[i]; centre = i; }
    int start = (centre - maxLen) / 2;
    return s.substring(start, start + maxLen);
}
```

> Reminder: `substring(start, start + maxLen)` uses an **exclusive** end index in Java.

---

## 7.6 Suffix Array — O(N log²N)

**Use for**: number of distinct substrings, longest common substring, string comparisons.

```java
// O(N log²N) version suitable for CP. Sorts indices by (rank, rank+gap).
static int[] buildSuffixArray(String s) {
    int n = s.length();
    Integer[] sa = new Integer[n];
    int[] rank = new int[n], tmp = new int[n];
    for (int i = 0; i < n; ++i) { sa[i] = i; rank[i] = s.charAt(i); }

    for (int gap = 1; gap < n; gap <<= 1) {
        final int g = gap;
        final int[] rk = rank;          // effectively-final for the lambda
        Comparator<Integer> cmp = (a, b) -> {
            if (rk[a] != rk[b]) return Integer.compare(rk[a], rk[b]);
            int ra = a + g < n ? rk[a + g] : -1;
            int rb = b + g < n ? rk[b + g] : -1;
            return Integer.compare(ra, rb);
        };
        Arrays.sort(sa, cmp);
        tmp[sa[0]] = 0;
        for (int i = 1; i < n; ++i)
            tmp[sa[i]] = tmp[sa[i-1]] + (cmp.compare(sa[i-1], sa[i]) < 0 ? 1 : 0);
        System.arraycopy(tmp, 0, rank, 0, n);
    }
    int[] result = new int[n];
    for (int i = 0; i < n; ++i) result[i] = sa[i];
    return result;
}

// Number of distinct substrings = N*(N+1)/2 - sum(lcp)
// Build the LCP array with Kasai's algorithm after the suffix array.
```

> Note: lambdas may only capture effectively-final variables, so we copy `rank`/`gap` into locals (`rk`, `g`) inside the loop. We sort a boxed `Integer[]` so we can use a `Comparator` (primitive `int[]` has no comparator-based sort).

---

## 7.7 String Algorithm Summary

| Algorithm | Use Case | Complexity |
|-----------|----------|------------|
| KMP | All occurrences of pattern in text | O(N + M) |
| Z-function | Same as KMP, also string period | O(N) |
| Rabin-Karp hashing | Duplicate substring, multi-pattern | O(N) avg |
| Double hashing | Substring equality in O(1) | O(N) build |
| Trie | Prefix queries, XOR max | O(NL) |
| Manacher | Longest palindromic substring | O(N) |
| Suffix array | All suffix-based queries | O(N log²N) |

---

**Next**: [08 — Bit Manipulation](08_bit_manipulation.md)
