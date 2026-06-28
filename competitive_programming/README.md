# Competitive Programming -- Complete Reference (Java Edition)

All code uses **Java 21** (`javac` / `java`, e.g. `javac Main.java && java Main`). JVM-specific notes (startup cost, autoboxing, recursion depth, GC) are called out where relevant.
Based on techniques used by: Tourist, Errichto, neal, jiangly, Um_nik, maroonrk, ecnerwala (translated to idiomatic competitive Java).

---

## Table of Contents

### Foundations
| File | Topics |
|------|--------|
| [01 - I/O and CP Basics](01_io_and_basics.md) | Fast I/O (BufferedReader/StreamTokenizer/FastReader), why Scanner is slow, constants, JVM notes |
| [02 - Time and Space Complexity](02_time_complexity.md) | Big-O, constraint->complexity table, overflow (long/BigInteger), memory, autoboxing |
| [03 - Java Collections Tricks](03_stl_tricks.md) | ArrayList, HashMap, TreeMap/TreeSet, PriorityQueue, ArrayDeque, Collections, bit helpers |
| [11 - Tips and Tricks](11_tips_and_tricks.md) | Contest strategy, debugging, stress testing, common bugs |
| [12 - CP Template](12_cp_template.md) | Ready-to-use contest template |

### Core Algorithms
| File | Topics |
|------|--------|
| [04 - Number Theory](04_number_theory.md) | GCD, sieve, linear sieve, fast power, mod inverse, C(n,k), CRT, matrix exp |
| [05 - Graph Algorithms](05_graph_algorithms.md) | BFS, DFS, Dijkstra, 0-1 BFS, Floyd, MST, DSU, SCC, bridges, LCA |
| [06 - Dynamic Programming](06_dynamic_programming.md) | LCS, LIS, knapsack, interval, bitmask, tree, digit DP, rolling array |
| [07 - String Algorithms](07_string_algorithms.md) | KMP, Z-function, hashing, Trie, Manacher, Suffix Array |
| [08 - Bit Manipulation](08_bit_manipulation.md) | Bit tricks, XOR, subset enumeration, SOS DP, BitSet |
| [09 - Geometry](09_geometry.md) | Points, cross product, convex hull, polygon area, line intersection |
| [10 - Game Theory](10_game_theory.md) | Nim, Sprague-Grundy, MEX, Staircase Nim, Wythoff |

### Advanced (Div 1 / IOI / ICPC Level)
| File | Topics |
|------|--------|
| [13 - Advanced Data Structures](13_advanced_data_structures.md) | Sparse Table, Lazy Seg Tree, LCA, HLD, Centroid Decomp, Mo's, Treap, DSU on Tree |
| [14 - Flows and Matching](14_flows_and_matching.md) | Dinic's Max Flow, Min Cut, Hopcroft-Karp, 2-SAT, MCMF, Euler Path |
| [15 - DP Optimizations](15_dp_optimizations.md) | Convex Hull Trick, Li Chao Tree, D&C DP, Knuth's, Aliens Trick, SOS DP |
| [16 - Advanced Strings](16_advanced_strings.md) | Aho-Corasick, Suffix Automaton, Palindromic Tree, SA+LCP, anti-hack hashing |
| [17 - Advanced Number Theory](17_advanced_number_theory.md) | Miller-Rabin, Pollard's Rho, NTT, Berlekamp-Massey, Discrete Log, Mobius |

### Expert Level (Codeforces Div 1 A-E / Grand Round Level)
| File | Topics |
|------|--------|
| [18 - Greedy and Constructive](18_greedy_and_constructive.md) | Exchange argument, interval greedy, ternary search, parallel binary search, fractional programming |
| [19 - Advanced Math](19_advanced_math.md) | XOR linear basis, Gaussian GF(2), Catalan, Lucas, Burnside, expected value DP, inclusion-exclusion |
| [20 - Sweep Line and Offline](20_sweep_line_and_offline.md) | Sweep line, CDQ offline D&C, inversion count, closest pair, area of union |
| [21 - Advanced Tree Techniques](21_tree_advanced.md) | Euler tour, rerooting DP, functional graphs, virtual tree, tree isomorphism, Link-Cut Tree |
| [22 - PBDS and Misc Tricks](22_pbds_and_misc.md) | Order-statistics tree, persistent seg tree, seg tree beats, implicit treap, random hashing |

---

## The Five Pillars of Competitive Programming

```
1. IDENTIFY   -- Recognise the problem type (graph, DP, greedy, math...)
2. DESIGN     -- Choose an algorithm and verify the complexity fits
3. IMPLEMENT  -- Write clean, bug-free code fast
4. TEST       -- Run sample cases, corner cases, max constraints
5. SUBMIT     -- Trust your solution or debug systematically
```

---

## Complexity Quick Reference

| N (constraint) | Max Complexity | Typical Algorithm |
|----------------|---------------|-------------------|
| N <= 10 | O(N!) | Brute force, all permutations |
| N <= 20 | O(2^N) | Bitmask DP |
| N <= 100 | O(N^3) | Floyd-Warshall, cubic DP |
| N <= 1,000 | O(N^2) | Quadratic DP, Bellman-Ford |
| N <= 100,000 | O(N log N) | Sort, BFS/DFS, segment tree |
| N <= 1,000,000 | O(N) or O(N log N) | Sieve, two pointers, hashing |
| N <= 10^18 | O(log N) or O(sqrt N) | Binary search, Miller-Rabin |

**Rule of thumb**: 10^8 simple operations = ~1 second on a modern judge. The JVM is typically a constant factor (~1.5-2x) slower than C++, so budget conservatively and always use fast I/O.

---

## Most Common Problem Types and Techniques

```
Graphs          BFS, DFS, Dijkstra, Kruskal, DSU, Topo sort, LCA, HLD
DP              LCS, LIS, Knapsack, interval, tree DP, bitmask, digit DP
Math/NT         Sieve, GCD, mod arithmetic, fast power, NTT, Miller-Rabin
                XOR basis, Catalan, Burnside, inclusion-exclusion
Strings         KMP, Z-function, Aho-Corasick, hashing, SAM, Suffix Array
Binary Search   On answer, on sorted array, ternary search, parallel BS
Greedy          Interval scheduling, exchange argument, fractional programming
Flows/Matching  Dinic's, Hopcroft-Karp, 2-SAT, MCMF
Trees           Euler tour, rerooting DP, virtual tree, functional graph
Sweep Line      Event processing, CDQ offline, inversion counting
Data Structures Seg tree, lazy seg tree, HLD, centroid, persistent seg tree
                TreeMap order queries, implicit treap, seg tree beats
Constructive    Build answer step by step, invariant maintenance
Randomization   Random hashing, shuffle to avoid worst case
```

---

## Skill Level Map

```
Beginner   (Codeforces Div 2 A/B)      --> 01, 02, 03, 08
Intermediate (Div 2 C/D)               --> 04, 05, 06, 07, 09, 10, 11
Advanced   (Div 1 B/C, IOI, ICPC)      --> 13, 14, 15, 16, 17, 18, 19
Expert     (Div 1 D/E, Grand Rounds)   --> 20, 21, 22
```

---

## Java CP Caveats (read once, remember forever)

- **Always use fast I/O**: `BufferedReader`/`StreamTokenizer` or a custom `FastReader`, and a `StringBuilder`/`PrintWriter` for output. `Scanner` is far too slow for large inputs.
- **Avoid autoboxing in hot loops**: prefer `int[]`/`long[]` over `ArrayList<Integer>`; prefer primitive arrays over `HashMap<Integer,Integer>` when keys are dense.
- **Recursion depth**: the default JVM stack overflows around ~10^4-10^5 frames. For deep DFS, run the work on a `Thread` with a large stack (e.g. `new Thread(null, task, "main", 1 << 26)`) or convert to an explicit-stack iterative form.
- **`long` is always 64-bit**; `int` is always 32-bit. Use `long` whenever a product or sum can exceed ~2.1x10^9. For values beyond 2^63 use `BigInteger`.
- **No unsigned types**: use `long` to hold large positive `int` values, or the `Integer.toUnsignedLong` / `Long.compareUnsigned` helpers.

---

## Keyword -> File Quick Lookup

| Keyword / Problem Signal | File |
|--------------------------|------|
| XOR maximum, XOR subset | 19 (XOR basis) |
| Symmetry, rotations, distinct colorings | 19 (Burnside) |
| Expected steps, probability DP | 19 (Expected value) |
| Greedy proof, exchange argument | 18 |
| Unimodal function, ternary search | 18 |
| K binary searches sharing state | 18 (parallel binary search) |
| Ratio maximization, binary search on answer | 18 (fractional programming) |
| Sweep line, event processing | 20 |
| Count inversions, 3D dominance | 20 (CDQ) |
| Offline queries with time dimension | 20 (CDQ) |
| Path queries on all nodes as root | 21 (rerooting DP) |
| K-th successor in permutation | 21 (functional graph) |
| Queries on K special nodes of tree | 21 (virtual tree) |
| Tree structure equality | 21 (tree isomorphism) |
| Dynamic tree with path queries | 21 (Link-Cut Tree) |
| K-th smallest in range [l,r] | 22 (persistent seg tree) |
| Order statistics, rank, kth element | 22 (TreeMap / indexed structure) |
| Range chmin/chmax with range sum | 22 (seg tree beats) |
| Sequence split/merge/reverse | 22 (implicit treap) |
| Set equality check fast | 22 (random XOR hash) |

---

## Next Steps

- Read [01 - I/O and CP Basics](01_io_and_basics.md) first - fast I/O alone can save TLE.
- Start every contest with [12 - CP Template](12_cp_template.md) - never start from scratch.
- Foundation path: 01 -> 02 -> 03 -> 04 -> 05 -> 06 -> 07 -> 08 -> 11
- Advanced path: 13 -> 14 -> 15 -> 16 -> 17 -> 18 -> 19 -> 20 -> 21 -> 22
- Practice on [Codeforces](https://codeforces.com), [AtCoder](https://atcoder.jp), [USACO](https://usaco.org).
- For each problem, always analyse complexity **before** coding.
