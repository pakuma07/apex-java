# 08 — Bit Manipulation

Bit tricks give O(1) solutions to many problems and speed up DP with bitmask states.

> Java notes: `int` is 32-bit and `long` is 64-bit (always). There are **no unsigned types** — use `>>>` (logical/unsigned right shift) where C++ would use an unsigned shift, and `>>` for arithmetic (sign-extending) shift. `Integer.bitCount` / `Long.bitCount` replace `__builtin_popcount`.

---

## 8.1 Fundamentals

```java
// Bit indexing: bit 0 is the least significant (rightmost)
int x = 0b1010;   // binary literal (Java 7+); underscores allowed: 0b1010_1100

// The six bitwise operators
x & y    // AND  — 1 only where both bits are 1
x | y    // OR   — 1 where at least one bit is 1
x ^ y    // XOR  — 1 where bits differ
~x       // NOT  — flip all bits
x << k   // left shift  — multiply by 2^k
x >> k   // arithmetic right shift — sign-extends (use for signed divide by 2^k)
x >>> k  // logical right shift — fills with 0 (Java's "unsigned" shift)
```

> Key Java difference: there is no unsigned `int`. Use `>>>` when you want zero-fill (e.g. midpoint `(lo + hi) >>> 1`, or treating an `int` as an unsigned bit pattern). `>>` sign-extends.

---

## 8.2 Single-Bit Operations

```java
int x = 42;
int k = 3;    // target bit position

// Check bit k
boolean isSet = ((x >> k) & 1) == 1;

// Set bit k
x |= (1 << k);

// Clear bit k
x &= ~(1 << k);

// Toggle bit k
x ^= (1 << k);

// Get lowest set bit (LSB) — isolates the lowest 1
int lsb = x & (-x);            // or Integer.lowestOneBit(x)

// Clear lowest set bit
x &= x - 1;

// Count set bits
int cnt  = Integer.bitCount(x);   // int   (= __builtin_popcount)
int cntL = Long.bitCount(yLong);  // long  (= __builtin_popcountll)

// Check power of 2
boolean isPow2 = (x > 0) && (x & (x - 1)) == 0;

// Next power of 2 >= x (use Integer.highestOneBit, or the classic bit-smear)
static int np2(int x) {
    if (x <= 1) return 1;
    return Integer.highestOneBit(x - 1) << 1;
}
// Bit-smear form (works the same as the C++ version; use >>> for zero-fill):
static int np2Smear(int x) {
    if (x == 0) return 1;
    x--;
    x |= x >>> 1; x |= x >>> 2; x |= x >>> 4;
    x |= x >>> 8; x |= x >>> 16;
    return x + 1;
}
```

> Java conveniences: `Integer.lowestOneBit(x)` (= `x & -x`), `Integer.highestOneBit(x)`, and `Integer.bitCount(x)` cover most of these directly.

---

## 8.3 Arithmetic Tricks

```java
x * 2   → x << 1
x / 2   → x >> 1     // arithmetic shift keeps sign for negatives
x % 2   → x & 1      // careful: for negative x this gives 1 if odd, mirroring sign bit
x * 4   → x << 2
x / 8   → x >> 3

// Absolute value without branching (32-bit int)
int mask = x >> 31;          // all 0s if positive, all 1s if negative
int absX = (x + mask) ^ mask;

// Min and max without branching
int mn = y ^ ((x ^ y) & -(x < y ? 1 : 0));
int mx = x ^ ((x ^ y) & -(x < y ? 1 : 0));

// Swap without temp (distinct variables only)
a ^= b; b ^= a; a ^= b;

// Average without overflow
int avg = (a & b) + ((a ^ b) >> 1);
```

> Note: Java has no implicit bool→int conversion, so `-(x < y)` from C++ becomes `-(x < y ? 1 : 0)`. For correctness on negatives in modulo-by-two, prefer `Math.floorMod(x, 2)` when you need a mathematical (non-negative) modulus.

---

## 8.4 Subset Enumeration

```java
int mask = 7;   // e.g. bitmask of N=3 elements

// Enumerate ALL subsets of a given mask (including the empty subset)
for (int sub = mask; ; sub = (sub - 1) & mask) {
    // process sub
    if (sub == 0) break;
}

// Enumerate ALL masks with exactly k bits set (out of N bits)
for (int m = 0; m < (1 << N); ++m)
    if (Integer.bitCount(m) == k) { /* process m */ }

// Sum over subsets DP (SOS DP) — O(N × 2^N)
// dp[mask] = sum of a[sub] for all sub ⊆ mask
long[] dp = new long[1 << N];
// ... initialise dp[mask] = a[mask] ...
for (int i = 0; i < N; ++i)
    for (int mask = 0; mask < (1 << N); ++mask)
        if ((mask & (1 << i)) != 0) dp[mask] += dp[mask ^ (1 << i)];
```

---

## 8.5 XOR Properties

```java
// Key XOR identities:
// x ^ x == 0
// x ^ 0 == x
// x ^ y == y ^ x       (commutative)
// (x^y)^z == x^(y^z)   (associative)

// Find the single non-duplicate in an array where all others appear twice
int single = 0;
for (int v : arr) single ^= v;
// Result is the unique element

// Find two non-duplicates (a and b) when all others appear twice
int xorAll = 0;
for (int v : arr) xorAll ^= v;       // = a ^ b
int bit = xorAll & (-xorAll);         // any differing bit
int a = 0, b = 0;
for (int v : arr) {
    if ((v & bit) != 0) a ^= v;
    else                b ^= v;
}
// a and b are the two unique elements

// Maximum XOR of any two elements — use a binary trie:
// insert all numbers, then for each x query the trie greedily.
```

---

## 8.6 BitSet (java.util.BitSet)

Java's `java.util.BitSet` is dynamically sized and grows as needed — the analogue of C++ `bitset<N>` (and `vector<bool>`). For fixed-width masks you can also use a raw `long[]`.

```java
import java.util.BitSet;

BitSet bs = new BitSet(100);   // initial hint of 100 bits, all 0
bs.set(5);                     // set bit 5
bs.clear(5);                   // clear bit 5
bs.flip(5);                    // toggle bit 5
bs.get(5);                     // test bit 5 (boolean)
bs.cardinality();              // number of set bits (= count())
bs.isEmpty();                  // true if all 0
bs.nextSetBit(0) != -1;        // true if any 1
// "all bits set" must be checked against a known length.

// Bitwise ops on whole bitsets — each op is O(N/64). These MUTATE the receiver:
BitSet a = new BitSet(), b = new BitSet();
a.and(b);   a.or(b);   a.xor(b);   a.andNot(b);   // a.flip(0, n) for ~a over [0,n)

// Shifts: java.util.BitSet has NO shift operator. Either:
//   - use a long[] and shift manually (see knapsack below), or
//   - rebuild via get/set, or BitSet.valueOf(long[]) after shifting the words.

// String / long conversions
BitSet bs2 = new BitSet();      // build from a binary string manually:
String src = "10110011";
for (int i = 0; i < src.length(); ++i)
    if (src.charAt(src.length() - 1 - i) == '1') bs2.set(i);
long[] words = bs2.toLongArray();   // backing words (little-endian)

// Bitset knapsack with a raw long[] (supports the dp |= dp << w shift) — O(N × W/64)
static long[] knapsackBits(int[] weights, int W) {
    int words = (W >> 6) + 1;
    long[] dp = new long[words];
    dp[0] = 1L;                              // dp bit 0 = 1 (weight 0 achievable)
    for (int w : weights) {
        // dp |= (dp << w)
        for (int i = words - 1; i >= 0; --i) {
            int word = w >> 6, off = w & 63;
            long shifted = 0;
            if (i - word >= 0)         shifted  = dp[i - word] << off;
            if (off > 0 && i - word - 1 >= 0) shifted |= dp[i - word - 1] >>> (64 - off);
            dp[i] |= shifted;
        }
    }
    return dp;   // bit W set in dp means weight W is achievable
}
```

> Two caveats vs C++ `bitset`: (1) `java.util.BitSet` operations mutate the receiver rather than returning a new value; (2) there is no `<<`/`>>` on `BitSet`, so for shift-based DP (the classic `dp |= dp << w`) drop down to a `long[]` and shift words manually, as shown.

---

## 8.7 Bitmask DP Patterns

```java
// State: visited set (bitmask) + current position.
// dp[mask][v] = min cost to visit all in mask, ending at v.

// Check if bit i is set in mask
((mask >> i) & 1) == 1

// Add vertex i to the visited set
mask | (1 << i)

// Remove vertex i from the visited set
mask & ~(1 << i)

// Full mask (all N vertices visited)
(1 << N) - 1

// Complement of mask (within N bits)
((1 << N) - 1) ^ mask

// Common: iterate over all subsets of the complement
int comp = ((1 << N) - 1) ^ mask;
for (int sub = comp; sub > 0; sub = (sub - 1) & comp) { /* ... */ }
```

> When N is up to 31, `(1 << N)` still fits in `int`. For N ≈ 32–62, use `long` masks and `1L << i` to avoid overflow.

---

## 8.8 Tricks Summary Table

| Task | Expression |
|------|-----------|
| Check bit k | `((x >> k) & 1) == 1` |
| Set bit k | `x \| (1 << k)` |
| Clear bit k | `x & ~(1 << k)` |
| Toggle bit k | `x ^ (1 << k)` |
| Lowest set bit | `x & (-x)` or `Integer.lowestOneBit(x)` |
| Clear lowest set bit | `x & (x - 1)` |
| Count set bits | `Integer.bitCount(x)` / `Long.bitCount(x)` |
| Is power of 2? | `x > 0 && (x & (x-1)) == 0` |
| All subsets of mask | `for (sub=mask; sub>0; sub=(sub-1)&mask)` |
| XOR all → find single | Fold XOR over array |
| Bitset knapsack | `long[]` with manual `dp \|= dp << w` |
| Unsigned/zero-fill shift | `x >>> k` |

---

**Next**: [09 — Geometry](09_geometry.md)
