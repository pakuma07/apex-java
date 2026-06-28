# 12 - CP Template Reference

The file `12_cp_template.java` is a world-class, ready-to-use contest template.
Copy it as your starting file for every problem.

> **Java naming note**: a public class name cannot start with a digit, so
> `public class 12_cp_template` is **invalid**. The template's public class is
> named `CpTemplate`. `javac` requires the file name to match the public class,
> so rename the file to `CpTemplate.java` before compiling locally. Most online
> judges read the submitted blob and only care about the class name (`CpTemplate`
> or `Main`), not the file name.

---

## What Is Included

| Section | What it provides |
|---------|-----------------|
| Fast I/O | `FastReader` over a buffered `DataInputStream` + `PrintWriter` (single flush) |
| Constants | `INF=0x3f3f3f3f`, `LINF`, `MOD=1e9+7`, `MOD2=998244353`, `PI`, `EPS` |
| Random | `SplittableRandom rng` (seeded with `System.nanoTime()`) |
| Safe Hash | splitmix64 `safeKey(long)` + note on Java `HashMap` collision treeing (anti-hack) |
| Math | `gcd`, `lcm`, `power`, `modInv`, `extgcd`, `C(n,k)` with precomputed factorials |
| DSU | Path compression + union by size, `comps` counter |
| Sparse Table | O(1) RMQ, 0-indexed, static `int[]` |
| Segment Tree | Point update, range sum, 1-indexed |
| Lazy Segment Tree | Range add update, range sum, lazy propagation, 1-indexed |
| Fenwick / BIT | Point update, prefix sum, range sum, 1-indexed |
| LCA | Binary lifting, O(N log N) build (iterative DFS) / O(log N) query, `treeDist()` |
| Dijkstra | Min-heap (`PriorityQueue`), 1-indexed, returns `long[]` dist |
| BFS | Unweighted shortest path (`ArrayDeque`) |
| 0-1 BFS | Deque-based BFS for 0/1 edge weights |
| Dinic Max Flow | Array-based adjacency: `addEdge`, `maxflow(s,t)` |
| NTT | Polynomial multiplication mod 998244353, `NTT.multiply(a,b)` |
| KMP | `kmpFail(pattern)` failure function |
| Z-function | `zFunc(s)` |
| Geometry | `class P`, `cross(O,A,B)`, `convexHull(pts)` |
| Coordinate Compress | `compress(a)` - returns 0-indexed ranks |
| Miller-Rabin | `isPrime(n)` deterministic for n < 3.3e24 (BigInteger mulmod) |
| Debug | `dbg(...)` gated by a `LOCAL` boolean constant |
| solve() | Your solution entry point |
| main() | Fast I/O + T test case loop template |

---

## How to Use

```bash
# Copy template to solution file (matching the public class name)
cp 12_cp_template.java CpTemplate.java

# Write your solution in solve()
# Remove unused data structures to keep the file short

# Compile
javac CpTemplate.java

# Run with input redirected from a file
java CpTemplate < input.txt

# Run with assertions enabled (local debugging)
java -ea CpTemplate < input.txt

# If you hit StackOverflowError on deep recursion, raise the stack size:
java -Xss256m CpTemplate < input.txt
```

---

## Key Design Choices

### 64-bit `long` and BigInteger
Java `int` is 32-bit and overflows **silently**; use `long` (64-bit) for any value
that can exceed ~2×10^9. For products of two values near `Long.MAX_VALUE`
(e.g. Miller-Rabin's `mulmod`), Java has no 128-bit integer type, so the template
falls back to `BigInteger` for the multiply-mod step.
```java
long x = (long) a * b;          // cast ONE operand before multiplying
// 128-bit safe product mod m:  use mulmod(a, b, m) (BigInteger-backed)
```

### Fast I/O (the #1 Java TLE fix)
`Scanner` is 5–10× too slow for CP. The template's `FastReader` reads raw bytes
through a `DataInputStream` buffer and parses ints/longs by hand. Output goes
through a `PrintWriter`/`BufferedWriter` that is flushed **once** at the end of
`main()`. Never flush per line inside a loop.

### Safe Hash (anti-hack)
Java's `HashMap` already treeifies high-collision buckets (O(log n) worst case for
`Comparable` keys), so it is far harder to hack than C++ `unordered_map`. For extra
safety with `Integer`/`Long` keys, perturb them with `safeKey(x)` (splitmix64 + a
random per-run salt) before inserting.

### MOD vs MOD2
- `MOD = 1_000_000_007` : classic prime, use for most modular arithmetic
- `MOD2 = 998_244_353` : NTT-friendly prime (primitive root = 3), use for polynomial multiplication

### Debug Output
```java
static final boolean LOCAL = false;   // flip to true locally
dbg("x", x, "y", y);                  // prints to stderr only when LOCAL
```
Set `LOCAL = false` before submitting so debug calls become no-ops.

### Segment Tree vs Lazy Segment Tree
- `SegTree`: point update (set single index), range query
- `LazySegTree`: range update (add to interval), range query

Both are 1-indexed.

### Recursion and the JVM Stack
The default JVM thread stack (~512 KB) overflows on deep recursion (large DFS,
divide-and-conquer DP). The template's LCA build uses an **iterative DFS** for this
reason. For your own recursive solves, either convert to iteration or run `solve()`
on a thread with a bigger stack:
```java
new Thread(null, () -> { solve(); out.flush(); }, "main", 1 << 26).start();
```

### LCA Usage
```java
lcaAdj = new List[N + 1];
for (int i = 1; i <= N; i++) lcaAdj[i] = new ArrayList<>();
// add tree edges to lcaAdj...
buildLCA(1, N);              // call once on root
int anc  = lca(u, v);        // LCA of u and v
int d    = treeDist(u, v);   // edge distance
```

### Dinic Max Flow Usage
```java
Dinic g = new Dinic(N, maxEdges);  // N nodes (0-indexed), capacity for E forward edges
g.addEdge(u, v, capacity);         // directed edge; reverse auto-added with cap 0
long flow = g.maxflow(source, sink);
```

### NTT Usage
```java
long[] a = {1, 2, 3};
long[] b = {4, 5, 6};
long[] c = NTT.multiply(a, b);  // polynomial product mod 998244353
```

---

## Compile / Run Commands

| Purpose | Command |
|---------|---------|
| Standard compile | `javac CpTemplate.java` |
| Run | `java CpTemplate < input.txt` |
| With assertions (debug) | `java -ea CpTemplate < input.txt` |
| Bigger stack (deep recursion) | `java -Xss256m CpTemplate` |

> Java online judges usually expect the public class named `Main`. If so, rename
> `CpTemplate` → `Main` (and the file to `Main.java`).

---

## Template vs File Index

The template is self-contained. The other files in this folder expand on each topic:

- Math details -> [04 - Number Theory](04_number_theory.md)
- Graph details -> [05 - Graph Algorithms](05_graph_algorithms.md)
- DP details -> [06 - Dynamic Programming](06_dynamic_programming.md)
- Advanced DS -> [13 - Advanced Data Structures](13_advanced_data_structures.md)
- Flow/Matching -> [14 - Flows and Matching](14_flows_and_matching.md)
- DP Optimizations -> [15 - DP Optimizations](15_dp_optimizations.md)
- Advanced Strings -> [16 - Advanced Strings](16_advanced_strings.md)
- Advanced NT -> [17 - Advanced Number Theory](17_advanced_number_theory.md)
