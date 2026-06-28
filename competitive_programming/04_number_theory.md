# 04 — Number Theory

The backbone of math-heavy competitive programming problems.

> All methods below are written as `static` helpers (put them in your `Main` class). Use `long` freely — it is always 64-bit in Java. For values beyond 2^63, use `BigInteger`.

---

## 4.1 GCD and LCM

```java
// Euclidean algorithm — O(log min(a,b))
static long gcd(long a, long b) { return b == 0 ? a : gcd(b, a % b); }

// LCM — always divide first to prevent overflow
static long lcm(long a, long b) { return a / gcd(a, b) * b; }

// Java also has BigInteger.gcd if you need arbitrary precision:
// BigInteger.valueOf(a).gcd(BigInteger.valueOf(b))

// Extended Euclidean: finds x, y such that a*x + b*y = gcd(a,b).
// Java has no output parameters, so return all three values in a long[].
// result = {g, x, y}
static long[] extgcd(long a, long b) {
    if (b == 0) return new long[]{a, 1, 0};
    long[] r = extgcd(b, a % b);
    long g = r[0], x1 = r[1], y1 = r[2];
    return new long[]{g, y1, x1 - (a / b) * y1};
}
```

> Java has no reference/output parameters like C++ `int &x`. The idiom is to return a small array (`long[]`) or a `record`. We use `long[]{g, x, y}` here.

---

## 4.2 Prime Testing

```java
// O(√N) primality test
static boolean isPrime(long n) {
    if (n < 2) return false;
    if (n == 2) return true;
    if (n % 2 == 0) return false;
    for (long i = 3; i * i <= n; i += 2)
        if (n % i == 0) return false;
    return true;
}
```

---

## 4.3 Sieve of Eratosthenes — O(N log log N)

```java
static final int MAXN = 1_000_001;
static boolean[] isComposite = new boolean[MAXN];   // false = prime
static ArrayList<Integer> primes = new ArrayList<>();

static void sieve(int n) {
    // boolean[] is default-initialised to false; no fill needed if fresh.
    isComposite[0] = isComposite[1] = true;
    for (int i = 2; i <= n; ++i) {
        if (!isComposite[i]) {
            primes.add(i);
            for (long j = (long) i * i; j <= n; j += i)
                isComposite[(int) j] = true;
        }
    }
}
// After sieve(1_000_000): primes contains all primes up to 1,000,000
```

> Note: `(long) i * i` casts before multiplying to avoid `int` overflow when `i ≈ 46341`; the index is cast back to `int`.

### Linear Sieve — O(N) (each composite crossed out exactly once)

```java
static int[] lp = new int[MAXN];   // lp[i] = smallest prime factor of i
static ArrayList<Integer> primes = new ArrayList<>();

static void linearSieve(int n) {
    // lp[] defaults to 0.
    for (int i = 2; i <= n; ++i) {
        if (lp[i] == 0) { lp[i] = i; primes.add(i); }
        for (int j = 0; j < primes.size() && primes.get(j) <= lp[i]
                        && (long) i * primes.get(j) <= n; ++j)
            lp[i * primes.get(j)] = primes.get(j);
    }
}
// Use lp[i] to factor any i in O(log i)
static ArrayList<Integer> factorize(int n) {
    ArrayList<Integer> f = new ArrayList<>();
    while (n > 1) { f.add(lp[n]); n /= lp[n]; }
    return f;
}
```

---

## 4.4 Prime Factorisation

```java
// O(√N) factorisation — TreeMap keeps factors in sorted order
static TreeMap<Integer,Integer> factorize(int n) {
    TreeMap<Integer,Integer> f = new TreeMap<>();
    for (int p = 2; (long) p * p <= n; ++p)
        while (n % p == 0) { f.merge(p, 1, Integer::sum); n /= p; }
    if (n > 1) f.merge(n, 1, Integer::sum);
    return f;
}

// Number of divisors from factorisation
// If n = p1^e1 * p2^e2 * ..., then #divisors = (e1+1)(e2+1)...
static int numDivisors(int n) {
    int count = 1;
    for (int p = 2; (long) p * p <= n; ++p) {
        int e = 0;
        while (n % p == 0) { e++; n /= p; }
        count *= (e + 1);
    }
    if (n > 1) count *= 2;
    return count;
}
```

---

## 4.5 Modular Arithmetic

**Golden rules**:
1. Always reduce after **every** operation, not just at the end.
2. Subtraction can go negative → add MOD before reducing.
3. Java's `%` keeps the sign of the dividend, so a negative result is possible → normalise with `((x % MOD) + MOD) % MOD`.

```java
static final long MOD = 1_000_000_007L;

static long addmod(long a, long b) { return (a + b) % MOD; }
static long submod(long a, long b) { return ((a - b) % MOD + MOD) % MOD; }
static long mulmod(long a, long b) { return (a % MOD) * (b % MOD) % MOD; }
```

> Java caveat: unlike C++, Java's `%` for negatives behaves the same (truncated toward zero), so `-3 % 5 == -2`. Always add `MOD` and reduce again after a subtraction. Also note `(a % MOD) * (b % MOD)` can be up to ~10^18, which fits in `long` (< 9.2×10^18) — safe as long as `MOD < 3×10^9`.

---

## 4.6 Fast Modular Exponentiation — O(log exp)

```java
// Computes base^exp % mod
static long power(long base, long exp, long mod) {
    long result = 1;
    base %= mod;
    while (exp > 0) {
        if ((exp & 1) == 1) result = result * base % mod;
        base = base * base % mod;
        exp >>= 1;
    }
    return result;
}

// Usage
long x = power(2, 30, MOD);    // 2^30 mod 10^9+7

// (BigInteger alternative: BigInteger.valueOf(base).modPow(
//   BigInteger.valueOf(exp), BigInteger.valueOf(mod)) — slower but handles huge moduli.)
```

---

## 4.7 Modular Inverse

The modular inverse of `a` under modulo `m` is `x` such that `a * x ≡ 1 (mod m)`.

**Method 1 — Fermat's Little Theorem** (m must be prime):
```java
// inv(a) = a^(m-2) mod m   (when m is prime)
static long modInverse(long a, long m) {
    return power(a, m - 2, m);
}
```

**Method 2 — Extended Euclidean** (works for any coprime a, m):
```java
static long modInverseExt(long a, long m) {
    long[] r = extgcd(a, m);          // {g, x, y}
    if (r[0] != 1) return -1;          // no inverse
    return (r[1] % m + m) % m;
}
```

**Method 3 — Precompute inverses 1..N in O(N)**:
```java
long[] inv = new long[n + 1];
inv[1] = 1;
for (int i = 2; i <= n; ++i)
    inv[i] = (MOD - (MOD / i) * inv[(int)(MOD % i)] % MOD) % MOD;
```

---

## 4.8 Combinations with Modular Arithmetic

```java
static final int MAXN = 200_005;
static long[] fact = new long[MAXN], invFact = new long[MAXN];

static void precompute(int n) {
    fact[0] = 1;
    for (int i = 1; i <= n; ++i) fact[i] = fact[i-1] * i % MOD;
    invFact[n] = power(fact[n], MOD - 2, MOD);
    for (int i = n - 1; i >= 0; --i) invFact[i] = invFact[i+1] * (i+1) % MOD;
}

static long C(int n, int k) {
    if (k < 0 || k > n) return 0;
    return fact[n] * invFact[k] % MOD * invFact[n-k] % MOD;
}
```

---

## 4.9 Euler's Totient Function

φ(n) = count of integers in [1, n] coprime to n.

```java
// Single value — O(√N)
static int phi(int n) {
    int result = n;
    for (int p = 2; (long) p * p <= n; ++p) {
        if (n % p == 0) {
            while (n % p == 0) n /= p;
            result -= result / p;
        }
    }
    if (n > 1) result -= result / n;
    return result;
}

// Sieve for all values 1..N
static int[] eulerSieve(int n) {
    int[] phi = new int[n + 1];
    for (int i = 0; i <= n; ++i) phi[i] = i;   // iota
    for (int i = 2; i <= n; ++i) {
        if (phi[i] == i) {   // i is prime
            for (int j = i; j <= n; j += i)
                phi[j] -= phi[j] / i;
        }
    }
    return phi;
}
```

---

## 4.10 Chinese Remainder Theorem (CRT)

Given: x ≡ r1 (mod m1),  x ≡ r2 (mod m2)   [m1, m2 coprime]  
Find: x (mod m1·m2)

```java
// Returns {remainder, modulus} for the combined congruence, or {-1, -1} if none.
static long[] crt(long r1, long m1, long r2, long m2) {
    long[] e = extgcd(m1, m2);        // {g, x, y}
    long g = e[0], x = e[1];
    if ((r2 - r1) % g != 0) return new long[]{-1, -1};  // no solution
    long lcm = m1 / g * m2;
    long mod = m2 / g;
    long sol = (r1 + m1 * (((r2 - r1) / g % mod) * (x % mod) % mod)) % lcm;
    return new long[]{(sol % lcm + lcm) % lcm, lcm};
}
```

> If moduli are large (products near 2^63), use `Math.multiplyHigh`/`BigInteger` or a `mulmod` to avoid overflow in the intermediate products.

---

## 4.11 Matrix Exponentiation — O(K³ log N)

Used for computing Nth term of linear recurrences in O(log N).

```java
static long[][] multiply(long[][] A, long[][] B, long mod) {
    int n = A.length;
    long[][] C = new long[n][n];
    for (int i = 0; i < n; ++i)
        for (int k = 0; k < n; ++k) if (A[i][k] != 0)
            for (int j = 0; j < n; ++j)
                C[i][j] = (C[i][j] + A[i][k] * B[k][j]) % mod;
    return C;
}

static long[][] matpow(long[][] A, long p, long mod) {
    int n = A.length;
    long[][] R = new long[n][n];
    for (int i = 0; i < n; ++i) R[i][i] = 1;   // identity
    for (; p > 0; p >>= 1) {
        if ((p & 1) == 1) R = multiply(R, A, mod);
        A = multiply(A, A, mod);
    }
    return R;
}

// Example: Fibonacci F(n) using matrix exponentiation
// | F(n+1) |   | 1 1 |^n   | F(1) |
// | F(n)   | = | 1 0 |   × | F(0) |
static long fib(long n, long mod) {
    if (n == 0) return 0;
    long[][] M = {{1,1},{1,0}};
    long[][] R = matpow(M, n - 1, mod);
    return R[0][0];   // F(n)
}
```

---

## 4.12 Number Theory Summary

| Problem | Tool |
|---------|------|
| GCD/LCM | Euclidean algorithm (or `BigInteger.gcd`) |
| Is N prime? | O(√N) trial division |
| All primes ≤ N | Sieve of Eratosthenes |
| Smallest prime factor of all ≤ N | Linear sieve |
| Factorize N | O(√N) or linear sieve |
| a^b mod m | Fast power (or `BigInteger.modPow`) |
| Division under mod | Modular inverse (Fermat or extGCD) |
| C(n,k) mod p | Precomputed factorials + inverse factorials |
| Linear recurrence Nth term | Matrix exponentiation |
| System of congruences | Chinese Remainder Theorem |

---

**Next**: [05 — Graph Algorithms](05_graph_algorithms.md)
