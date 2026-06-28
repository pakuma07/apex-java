# Disjoint Set Union (Union-Find)

This chapter was split for consistency.

Use the detailed version here:

- [structures/disjoint_set_union.md](structures/disjoint_set_union.md)

Practice problems:

- ../exercises/dsu_exercises.md

## Java 21 Example: DSU

```java
class DSU {
    final int[] p, r;
    DSU(int n) {
        p = new int[n];
        r = new int[n];
        for (int i = 0; i < n; i++) p[i] = i;
    }
    int find(int x) { return p[x] == x ? x : (p[x] = find(p[x])); }
    boolean unite(int a, int b) {
        a = find(a); b = find(b);
        if (a == b) return false;
        if (r[a] < r[b]) { int t = a; a = b; b = t; }
        p[b] = a;
        if (r[a] == r[b]) ++r[a];
        return true;
    }
}
```
