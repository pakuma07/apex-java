# 10 — Game Theory (Sprague-Grundy)

Combinatorial game theory lets you determine the winner of impartial games without full search.

---

## 10.1 Nim

**Rules**: N piles, each with some stones. Players alternate taking any positive number from one pile. Player who cannot move **loses**.

**Winning condition**: XOR of all pile sizes ≠ 0.

```java
static boolean nimWinner(int[] piles) {
    int xorSum = 0;
    for (int p : piles) xorSum ^= p;
    return xorSum != 0;   // true = first player wins
}

// Winning move: find any pile i where piles[i] ^ xorSum < piles[i]
// Then reduce pile i to piles[i] ^ xorSum
static void nimWinningMove(int[] piles, java.io.PrintWriter out) {
    int xorSum = 0;
    for (int p : piles) xorSum ^= p;
    for (int i = 0; i < piles.length; ++i) {
        int target = piles[i] ^ xorSum;
        if (target < piles[i]) {
            out.println("Take " + (piles[i] - target) + " from pile " + i);
            piles[i] = target;
            return;
        }
    }
}
```

---

## 10.2 Sprague-Grundy Theorem

**Every impartial game** (where both players have the same moves) can be assigned a **Grundy number (nimber)**:

```
G(position) = MEX({ G(reachable positions) })
```

Where **MEX** = Minimum Excludant = smallest non-negative integer NOT in the set.

```java
// Compute Grundy numbers for positions 0..MAXN
static int[] grundy = new int[MAXN];
static boolean[] computed = new boolean[MAXN];

// Example: game where from position n you can move to n-1, n-2, or n-3
static int computeGrundy(int n) {
    if (computed[n]) return grundy[n];
    computed[n] = true;
    java.util.TreeSet<Integer> reachable = new java.util.TreeSet<>();
    if (n >= 1) reachable.add(computeGrundy(n - 1));
    if (n >= 2) reachable.add(computeGrundy(n - 2));
    if (n >= 3) reachable.add(computeGrundy(n - 3));
    int mex = 0;
    while (reachable.contains(mex)) mex++;
    return grundy[n] = mex;
}
```

> **Recursion note**: deep recursive Grundy computation can overflow the default JVM stack (~512 KB). For large `MAXN`, prefer the bottom-up table in 10.7, or launch the work in a thread with a larger stack: `new Thread(null, this::run, "main", 1 << 26).start();`.

---

## 10.3 MEX Function

```java
static int mex(int[] values) {
    java.util.HashSet<Integer> s = new java.util.HashSet<>();
    for (int v : values) s.add(v);
    int m = 0;
    while (s.contains(m)) m++;
    return m;
}
```

A `boolean[]` of size `values.length + 1` is faster than a `HashSet` for MEX, since the answer is at most the number of values.

---

## 10.4 Combining Games (Sum of Games)

When a game splits into independent sub-games:
- **XOR** the Grundy numbers of each sub-game
- XOR ≠ 0 → first player wins
- XOR = 0 → second player wins

```java
static boolean compositeGameWinner(int[] subGameGrundys) {
    int xorAll = 0;
    for (int g : subGameGrundys) xorAll ^= g;
    return xorAll != 0;   // true = first player wins
}
```

---

## 10.5 Common Game Patterns

### Staircase Nim

Players can move stones from any step down to step 0. Only odd-indexed steps matter.

```java
static boolean staircaseNimWinner(int[] steps) {
    int xorOdd = 0;
    for (int i = 1; i < steps.length; i += 2)
        xorOdd ^= steps[i];
    return xorOdd != 0;
}
```

### Grundy Values for "take 1 to k" from a pile

```
G(n) = n % (k+1)
```
If `n % (k+1) == 0`, second player wins (Grundy = 0).

```java
// Take any amount from 1 to k
static int grundyBounded(int n, int k) { return n % (k + 1); }
```

### Wythoff's Game

Two piles; can take any from one pile, or equal amounts from both.

```java
static boolean wythoffWinner(int a, int b) {
    if (a > b) { int t = a; a = b; b = t; }
    int k = b - a;
    double phi = (1.0 + Math.sqrt(5.0)) / 2.0;
    int ak = (int) (k * phi);
    return a != ak;   // true = first player wins
}
```

---

## 10.6 Green Hackenbush

Edges on a graph; player removes one edge, all disconnected parts removed.
- For trees: Grundy of a tree = XOR of edge counts on each path from root to leaf.
- Bamboo (single path of k edges): Grundy = k.

---

## 10.7 Template: Grundy Table

For games with small state spaces, precompute the full Grundy table (iterative — avoids recursion stack limits):

```java
static final int MAXN = 1005;
static int[] G = new int[MAXN];

static void precomputeGrundy(int n) {
    G[0] = 0;  // base: losing position (no moves)
    for (int i = 1; i <= n; ++i) {
        boolean[] seen = new boolean[i + 1];
        // Replace with your game's valid moves from state i:
        for (int move : new int[]{1, 2, 3}) {
            if (i >= move) seen[G[i - move]] = true;
        }
        int mex = 0;
        while (mex < seen.length && seen[mex]) mex++;
        G[i] = mex;
    }
}

// Then for M piles: XOR all G[pile_i]
// XOR != 0 → first player wins
```

---

## 10.8 Decision Framework

```
Is it an impartial game?
  Yes → Compute Grundy numbers
    Single component → G(state) == 0 means second player wins
    Multiple independent components → XOR all Grundy numbers

Is it a partisan game (players have different moves)?
  → Use alpha-beta pruning or minimax (beyond Sprague-Grundy)

Is it a simple take-away game from one pile?
  → Look for period in Grundy sequence (often periodic quickly)

Nim variant?
  → Check: move options from pile of size n → MEX of G(n - moves)
```

---

## 10.9 Summary

| Concept | Rule |
|---------|------|
| Nim | XOR of pile sizes ≠ 0 → first player wins |
| Grundy number | MEX of Grundy numbers of reachable states |
| G = 0 | Losing position (second player wins) |
| G > 0 | Winning position (first player wins) |
| Sum of games | XOR the Grundy numbers |
| "Take 1 to k" | G(n) = n % (k+1) |
| Staircase Nim | XOR of odd-indexed piles |

---

**Next**: [11 — Tips and Tricks](11_tips_and_tricks.md)
