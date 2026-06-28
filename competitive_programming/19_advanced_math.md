# 19 - Advanced Mathematics for Competitive Programming

Topics used by top CF/AtCoder competitors that go beyond basic number theory.
Covers XOR basis, combinatorics identities, probability DP, Burnside's lemma,
Gaussian elimination, and generating functions.

---

## 19.1 XOR Linear Basis (GF(2) Basis)

**Extremely common in Codeforces Div 1 C/D.**
A set of integers that can represent all XOR combinations of a given array
using at most 64 basis vectors.

**Applications:**
- Maximum XOR subset
- Check if XOR x is achievable from an array
- Count distinct XOR values achievable
- K-th smallest XOR value

```java
static final class XorBasis {
    long[] b = new long[64];
    int sz = 0;

    // Insert x into basis. Returns false if x is linearly dependent (already spanned).
    boolean insert(long x) {
        for (int i = 62; i >= 0; i--) {
            if (((x >> i) & 1) == 0) continue;
            if (b[i] == 0) { b[i] = x; sz++; return true; }
            x ^= b[i];
        }
        return false;  // x == 0 after reduction: dependent
    }

    // Maximum XOR achievable (greedy from high bit down)
    long maxXor() {
        long res = 0;
        for (int i = 62; i >= 0; i--)
            res = Math.max(res, res ^ b[i]);
        return res;
    }

    // Maximum XOR of x with any subset of the basis
    long maxXorWith(long x) {
        for (int i = 62; i >= 0; i--)
            x = Math.max(x, x ^ b[i]);
        return x;
    }

    // Check if x is achievable as XOR of some subset
    boolean canAchieve(long x) {
        for (int i = 62; i >= 0; i--) {
            if (((x >> i) & 1) == 0) continue;
            if (b[i] == 0) return false;
            x ^= b[i];
        }
        return true;  // x == 0
    }

    // K-th smallest XOR value (0-indexed, 0 = XOR of empty set)
    // Requires basis to be reduced to row echelon form first
    void reduce() {
        for (int i = 62; i >= 0; i--) {
            if (b[i] == 0) continue;
            for (int j = i + 1; j <= 62; j++)
                if (((b[j] >> i) & 1) != 0) b[j] ^= b[i];
        }
    }

    long kth(long k) {
        // Call reduce() first
        ArrayList<Long> basis = new ArrayList<>();
        for (int i = 0; i <= 62; i++) if (b[i] != 0) basis.add(b[i]);
        // If 0 is achievable (sz < n), k=0 means 0; adjust k accordingly
        long res = 0;
        for (int i = 0; i < basis.size(); i++)
            if (((k >> i) & 1) != 0) res ^= basis.get(i);
        return res;
    }
}

// Usage example: maximum XOR of any subarray
// Add prefix XORs to basis, answer is maxXor()
XorBasis xb = new XorBasis();
long prefix = 0;
xb.insert(0);  // empty subarray
for (int x : a) {
    prefix ^= x;
    xb.insert(prefix);
}
System.out.println(xb.maxXor());
```

---

## 19.2 Gaussian Elimination over GF(2)

Solve systems of linear equations modulo 2 (XOR equations).
Used in problems involving XOR constraints, light switching puzzles.

```java
// Solve A*x = b over GF(2). A is n x m matrix, b is n-vector.
// Returns: number of free variables (-1 if no solution).
// Solution stored in x[].
static int gaussGF2(int[][] A, int[] b, int[] x) {
    int n = A.length, m = A[0].length;
    int[] pivot = new int[m];
    Arrays.fill(pivot, -1);
    int row = 0;
    for (int col = 0; col < m && row < n; col++) {
        // Find pivot
        int sel = -1;
        for (int i = row; i < n; i++) if (A[i][col] != 0) { sel = i; break; }
        if (sel == -1) continue;
        int[] tmpRow = A[row]; A[row] = A[sel]; A[sel] = tmpRow;
        int tmpB = b[row]; b[row] = b[sel]; b[sel] = tmpB;
        pivot[col] = row;
        for (int i = 0; i < n; i++) {
            if (i != row && A[i][col] != 0) {
                for (int j = 0; j < m; j++) A[i][j] ^= A[row][j];
                b[i] ^= b[row];
            }
        }
        row++;
    }
    // Check consistency
    for (int i = row; i < n; i++) if (b[i] != 0) return -1;  // no solution
    Arrays.fill(x, 0);
    for (int col = 0; col < m; col++)
        if (pivot[col] != -1) x[col] = b[pivot[col]];
    return m - row;  // number of free variables
}
```

**Light puzzle pattern**: N lights, each switch toggles a subset. XOR equations give the switch configuration.

> Java caveat: for dense GF(2) systems pack each row into a `long[]` bitset and XOR whole words at once; `BitSet.xor` works too but the manual `long[]` form is the fastest in hot loops.

---

## 19.3 Catalan Numbers

C(n) = C(2n, n) / (n+1) = 1, 1, 2, 5, 14, 42, 132, ...

**Occurrences of Catalan numbers:**
- Valid parenthesizations of n pairs
- Number of BSTs with n nodes
- Triangulations of (n+2)-gon
- Monotonic lattice paths that don't cross diagonal
- Stack-sortable permutations

```java
// Catalan(n) = C(2n,n) / (n+1)  (requires precomputed factorials)
static long catalan(int n) {
    return C(2 * n, n) * modInv(n + 1, MOD) % MOD;
}

// Alternatively by recurrence: C(0)=1, C(n) = sum C(i)*C(n-1-i)
long[] cat = new long[n + 1];
cat[0] = 1;
for (int i = 1; i <= n; i++)
    for (int j = 0; j < i; j++)
        cat[i] = (cat[i] + cat[j] * cat[i - 1 - j]) % MOD;
```

---

## 19.4 Lucas' Theorem

Compute C(n, k) mod p for **prime p** efficiently, even when n is huge.

**Theorem**: C(n, k) mod p = product of C(n_i, k_i) mod p,
where n_i, k_i are digits of n, k in base p.

```java
// C(n, k) mod p where p is prime (p can be small like 2, 3)
// Uses precomputed fact[]/invFact[] up to p
static long lucas(long n, long k, long p) {
    if (k < 0 || k > n) return 0;
    if (k == 0 || k == n) return 1;
    // C(n%p, k%p) * lucas(n/p, k/p, p)
    return lucas(n % p, k % p, p) * lucas(n / p, k / p, p) % p;
}
// Note: for small p, precompute fact[] up to p, use modInv(fact[k%p]*fact[(n-k)%p], p)

// Generalized Lucas (Andrew Granville) for prime powers: more complex, rarely needed
```

---

## 19.5 Burnside's Lemma (Polya Enumeration)

Count distinct objects under a group of symmetries.
**|distinct colorings| = (1/|G|) * sum_{g in G} |Fix(g)|**
where Fix(g) = number of colorings fixed by symmetry g.

```java
// Example: count distinct necklaces of n beads with k colors
// Symmetry group = rotations (cyclic group C_n)
// Fix(rotation by r positions) = k^gcd(n, r) colorings
static long necklaces(int n, int k, long mod) {
    long ans = 0;
    for (int r = 0; r < n; r++)
        ans = (ans + power(k, gcd(n, r), mod)) % mod;
    ans = ans % mod * modInv(n, mod) % mod;
    return ans;
}

// With reflections (dihedral group D_n = rotations + flips):
// Add n more symmetries (reflections)
// Each reflection fixes k^(ceil(n/2)) or k^((n+1)/2) colorings
static long bracelet(int n, int k, long mod) {
    long ans = 0;
    for (int r = 0; r < n; r++)
        ans = (ans + power(k, gcd(n, r), mod)) % mod;
    // Add reflections
    if (n % 2 == 0) {
        ans = (ans + (long) (n / 2) * power(k, n / 2 + 1, mod)) % mod;
        ans = (ans + (long) (n / 2) * power(k, n / 2, mod)) % mod;
    } else {
        ans = (ans + (long) n * power(k, (n + 1) / 2, mod)) % mod;
    }
    ans = ans % mod * modInv(2L * n, mod) % mod;
    return ans;
}
```

---

## 19.6 Expected Value and Probability DP

**Linearity of expectation**: E[X + Y] = E[X] + E[Y], even if X, Y are dependent.

**Pattern 1: E[total] = sum of E[contribution of each element]**

```java
// Expected number of comparisons in random permutation sort? E = sum_{i<j} Pr[i,j compared]
// Pr[i,j compared in quicksort] = 2/(j-i+1) for sorted positions i<j
static double expectedComparisons(int n) {
    double ans = 0;
    for (int i = 1; i <= n; i++)
        for (int j = i + 1; j <= n; j++)
            ans += 2.0 / (j - i + 1);
    return ans;
}
```

**Pattern 2: DP on states with probability transitions**

```java
// E[steps to reach absorbing state from state i]
// E[i] = 1 + sum_j p[i][j] * E[j], E[absorbing] = 0
// Solve linear system (Gaussian elimination) or use topological order if DAG

// Example: Expected steps to complete coupon collector (n coupons)
// E[n] = n * H_n where H_n = 1 + 1/2 + ... + 1/n
static double couponCollector(int n) {
    double ans = 0;
    for (int i = 1; i <= n; i++) ans += (double) n / i;
    return ans;
}
```

**Pattern 3: Contribution technique for expected value**

```java
// Expected maximum of n uniform[0,1] variables = n/(n+1)
// Expected minimum = 1/(n+1)
// In discrete: E[max of n dice] = sum_{x=1}^{6} Pr[max >= x]
//            = sum_{x=1}^{6} (1 - Pr[all < x]) = sum (1 - ((x-1)/6)^n)
```

**Pattern 4: Geometric distribution**
If each trial succeeds with probability p, E[trials until success] = 1/p.

```java
// E[rolls of 6-sided die until we see all faces] = couponCollector(6) = 14.7
// E[rolls until first 6] = 6
```

---

## 19.7 Inclusion-Exclusion Principle

|A1 union A2 union ... union An| = sum|Ai| - sum|Ai ∩ Aj| + sum|Ai ∩ Aj ∩ Ak| - ...

**Template for bitmask inclusion-exclusion:**

```java
// Count integers in [1, N] divisible by at least one of p[0..m-1]
// Via inclusion-exclusion over subsets
long[] p = {2, 3, 5};
int m = p.length;
long ans = 0;
for (int mask = 1; mask < (1 << m); mask++) {
    long lcmVal = 1;
    int bits = Integer.bitCount(mask);
    for (int i = 0; i < m; i++)
        if (((mask >> i) & 1) != 0) {
            lcmVal = lcmVal / gcd(lcmVal, p[i]) * p[i];
            if (lcmVal > N) { lcmVal = N + 1; break; }  // overflow guard
        }
    if (bits % 2 == 1) ans += N / lcmVal;
    else               ans -= N / lcmVal;
}
```

**Derangements** (permutations with no fixed point):
D(n) = n! * sum_{k=0}^{n} (-1)^k / k!
D(0)=1, D(1)=0, D(2)=1, D(3)=2, D(4)=9
Recurrence: D(n) = (n-1)*(D(n-1) + D(n-2))

```java
long[] D = new long[n + 1];
D[0] = 1; D[1] = 0;
for (int i = 2; i <= n; i++)
    D[i] = (long) (i - 1) % MOD * ((D[i - 1] + D[i - 2]) % MOD) % MOD;
```

---

## 19.8 Stirling Numbers

**Stirling numbers of the second kind S(n, k)**: number of ways to partition
n elements into k non-empty groups.

```java
// S(n, k) = k*S(n-1,k) + S(n-1,k-1)
// Explicit: S(n,k) = (1/k!) * sum_{j=0}^{k} (-1)^(k-j) * C(k,j) * j^n
long[][] S = new long[n + 1][n + 1];
S[0][0] = 1;
for (int i = 1; i <= n; i++)
    for (int k = 1; k <= i; k++)
        S[i][k] = ((long) k * S[i - 1][k] + S[i - 1][k - 1]) % MOD;
```

**Stirling numbers of the first kind |s(n,k)|**: number of permutations of n
elements with exactly k cycles.

---

## 19.9 Generating Functions (Overview)

**Ordinary Generating Function (OGF)**: f(x) = sum a[n] * x^n
**Exponential Generating Function (EGF)**: f(x) = sum a[n] * x^n / n!

**Key uses:**
- OGF for coin change: f(x) = product 1/(1 - x^{c_i})
- EGF for labeled structures: sequences use e^x, sets use e^x - 1

```java
// Polynomial multiplication via NTT (see 17_advanced_number_theory.md)
// Counts ways to make change using coins of denominations d[]:
// f(x) = product_{i} (1 + x^d[i] + x^{2*d[i]} + ...) = product 1/(1-x^{d[i]})
// Coefficient of x^n = number of ways

// Practical: DP is equivalent but generating functions allow O(N log N) solutions
// using polynomial inverse, logarithm, exponentiation
```

---

## 19.10 Fibonacci and Golden Ratio Tricks

```java
// Fast Fibonacci: matrix exponentiation O(log n)
// [F(n+1)] = [1 1]^n * [1]
// [F(n)  ]   [1 0]     [0]

// Fibonacci identities:
// F(2n) = F(n) * (2*F(n+1) - F(n))
// F(2n+1) = F(n)^2 + F(n+1)^2
// gcd(F(m), F(n)) = F(gcd(m,n))
// F(n) mod k is periodic (Pisano period)

// Zeckendorf's theorem: every positive integer uniquely represents as
// sum of non-consecutive Fibonacci numbers

// Pisano period pi(m): F(n) mod m has period pi(m)
// pi(10) = 60, pi(10^9+7) is huge, pi(2) = 3, pi(5) = 20
```

> Java advantage: when multiplying matrices or accumulating products mod p, the
> intermediate `a*b` can exceed 63 bits. C++ reaches for `__int128`; in Java just
> do the multiply with `long` if both factors are below ~3.0e9, otherwise use
> `java.math.BigInteger` (or `Math.multiplyHigh` / 128-bit mulmod) -- no manual
> mulmod hack is needed, and `BigInteger.modPow` handles huge exponents directly.

---

## 19.11 Summary: When to Use Which

| Topic | Trigger Words |
|-------|--------------|
| XOR basis | "maximum XOR subset", "XOR achievable", "k-th XOR" |
| Gaussian GF(2) | "XOR equations", "light switching", "flip operations" |
| Catalan | "valid brackets", "BSTs", "non-crossing paths" |
| Lucas | "C(n,k) mod small prime", "n can be 10^18" |
| Burnside | "count distinct colorings/rotations/symmetries" |
| Expected value DP | "expected number of steps/operations" |
| Linearity of expectation | "expected count of [property]" |
| Inclusion-Exclusion | "at least one of", "exactly k bad things" |
| Derangements | "no element in original position" |
| Stirling | "partition into k groups", "permutations with k cycles" |
| Generating functions | "counting problems with polynomial structure" |

Back to: [README](README.md)
