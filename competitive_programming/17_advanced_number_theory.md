# 17 — Advanced Number Theory

These topics appear in Codeforces Div 1 D/E and require mathematical depth.

> **Java caveat**: C++ leans on `unsigned long long` and `__int128` for overflow-free
> modular multiplication of 64-bit operands. Java has no unsigned 64-bit type and no
> 128-bit integer, but `java.math.BigInteger` covers both cleanly — and for the
> 64-bit modmul specifically, `Math.multiplyHigh` (Java 9+) or the `BigInteger`
> route gives the same result. Where the source uses `__int128 a * b % m`, Java
> uses `BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)).mod(...)` or a
> Montgomery/`multiplyHigh` trick. `__gcd` maps to a small helper (or
> `BigInteger.gcd`). `unordered_map` → `HashMap`.

---

## 17.1 Miller-Rabin Primality Test — O(k log² N)

Deterministic for N < 3.3 × 10^24 using specific witnesses.

```java
// Java has no unsigned 64-bit type. Treating values as signed long is fine for
// n up to ~9.2e18; the only delicate part is a*b mod m without overflow, which we
// route through BigInteger (clean, no __int128 needed) — or Math.multiplyHigh.
static long mulmod(long a, long b, long m) {
    // BigInteger keeps it simple and correct for the full 64-bit range.
    return BigInteger.valueOf(a).multiply(BigInteger.valueOf(b))
            .mod(BigInteger.valueOf(m)).longValue();
}

static long powmod(long a, long b, long m) {
    long res = 1; a %= m;
    for (; b > 0; b >>= 1) { if ((b & 1) == 1) res = mulmod(res, a, m); a = mulmod(a, a, m); }
    return res;
}

static boolean millerTest(long n, long a) {
    if (n % a == 0) return n == a;
    long d = n - 1; int r = 0;
    while (d % 2 == 0) { d /= 2; r++; }
    long x = powmod(a, d, n);
    if (x == 1 || x == n - 1) return true;
    for (int i = 0; i < r - 1; ++i) {
        x = mulmod(x, x, n);
        if (x == n - 1) return true;
    }
    return false;
}

static boolean isPrime(long n) {
    if (n < 2) return false;
    // Deterministic witnesses for n < 3,317,044,064,679,887,385,961,981
    for (long a : new long[]{2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37})
        if (!millerTest(n, a)) return false;
    return true;
}
```

> For 64-bit `n`, the witness set {2,3,…,37} is deterministic well beyond `long`
> range. If you need the absolute fastest modmul, replace `mulmod` with a
> `Math.multiplyHigh`-based 128-bit emulation or Montgomery multiplication.

---

## 17.2 Pollard's Rho — O(N^(1/4)) Factorisation

```java
static long pollardRho(long n) {
    if (n % 2 == 0) return 2;
    java.util.Random rng = new java.util.Random(
            System.nanoTime());
    while (true) {
        long x = Math.floorMod(rng.nextLong(), n - 2) + 2;
        long y = x;
        long c = Math.floorMod(rng.nextLong(), n - 1) + 1;
        long d = 1;
        while (d == 1) {
            x = (mulmod(x, x, n) + c) % n;
            y = (mulmod(y, y, n) + c) % n;
            y = (mulmod(y, y, n) + c) % n;
            d = gcd(Math.abs(x - y), n);
        }
        if (d != n) return d;
    }
}

static long gcd(long a, long b) { return b == 0 ? a : gcd(b, a % b); }

static java.util.Map<Long, Integer> factorize(long n) {
    java.util.Map<Long, Integer> f = new java.util.HashMap<>();
    factorizeInto(n, f);
    return f;
}

static void factorizeInto(long n, java.util.Map<Long, Integer> f) {
    if (n == 1) return;
    if (isPrime(n)) { f.merge(n, 1, Integer::sum); return; }
    long d = n;
    while (d == n) d = pollardRho(n);
    factorizeInto(d, f);
    factorizeInto(n / d, f);
}

// Usage: Map<Long,Integer> factors = factorize(N);  — works up to N ~ 10^18
```

> The recursive `factorizeInto` recurses at most O(log N) deep (each split removes a
> factor), so the default stack is ample here — unlike tree DFS in chapter 21.

---

## 17.3 Number Theoretic Transform (NTT) — O(N log N) Polynomial Multiplication

**Use for**: polynomial multiplication modulo a prime, convolutions.

```java
static final int NTT_MOD = 998244353;  // = 119 × 2^23 + 1, primitive root g = 3
static final int NTT_G   = 3;

static long power(long a, long b, long mod) {
    long res = 1; a %= mod;
    for (; b > 0; b >>= 1) { if ((b & 1) == 1) res = res * a % mod; a = a * a % mod; }
    return res;
}

static void ntt(long[] a, boolean inv) {
    int n = a.length;
    for (int i = 1, j = 0; i < n; ++i) {
        int bit = n >> 1;
        for (; (j & bit) != 0; bit >>= 1) j ^= bit;
        j ^= bit;
        if (i < j) { long t = a[i]; a[i] = a[j]; a[j] = t; }
    }
    for (int len = 2; len <= n; len <<= 1) {
        long w = inv ? power(NTT_G, NTT_MOD - 1 - (NTT_MOD - 1) / len, NTT_MOD)
                     : power(NTT_G, (NTT_MOD - 1) / len, NTT_MOD);
        for (int i = 0; i < n; i += len) {
            long wn = 1;
            for (int j = 0; j < len / 2; ++j) {
                long u = a[i + j], v = a[i + j + len / 2] * wn % NTT_MOD;
                a[i + j] = (u + v) % NTT_MOD;
                a[i + j + len / 2] = (u - v + NTT_MOD) % NTT_MOD;
                wn = wn * w % NTT_MOD;
            }
        }
    }
    if (inv) {
        long nInv = power(n, NTT_MOD - 2, NTT_MOD);
        for (int i = 0; i < n; ++i) a[i] = a[i] * nInv % NTT_MOD;
    }
}

static long[] multiply(long[] a0, long[] b0) {
    int resultSize = a0.length + b0.length - 1;
    int n = 1; while (n < resultSize) n <<= 1;
    long[] a = java.util.Arrays.copyOf(a0, n);
    long[] b = java.util.Arrays.copyOf(b0, n);
    ntt(a, false); ntt(b, false);
    for (int i = 0; i < n; ++i) a[i] = a[i] * b[i] % NTT_MOD;
    ntt(a, true);
    return java.util.Arrays.copyOf(a, resultSize);
}
```

> **Java advantage**: when the modulus is *not* NTT-friendly, C++ either juggles
> three NTTs (CRT) or `__int128` long-double FFT to dodge overflow. In Java you can
> sidestep all of that with `BigInteger.multiply`, which uses Karatsuba/Toom-Cook
> automatically — slower asymptotically than NTT but trivially correct and overflow-free
> for one-off big-coefficient convolutions.

---

## 17.4 Linear Recurrence — Berlekamp-Massey + Kitamasa

**Use for**: given the first few terms of a linear recurrence, find the recurrence relation, then compute the Nth term in O(K² log N) or O(K log K log N) with NTT.

```java
// Berlekamp-Massey: find the shortest linear recurrence for a sequence
static long[] berlekampMassey(long[] s, long mod) {
    int n = s.length;
    java.util.List<Long> cur = new java.util.ArrayList<>();
    java.util.List<Long> last = new java.util.ArrayList<>();
    int lf = 0; long ld = 0;
    for (int i = 0; i < n; ++i) {
        long t = 0;
        for (int j = 0; j < cur.size(); ++j)
            t = (t + cur.get(j) * s[i - 1 - j]) % mod;
        if ((s[i] - t) % mod == 0) continue;
        if (cur.isEmpty()) {
            for (int k = 0; k <= i; ++k) cur.add(0L);
            lf = i; ld = (s[i] - t) % mod; continue;
        }
        long k = (mod - (s[i] - t) % mod) % mod * power(ld, mod - 2, mod) % mod;
        java.util.List<Long> c = new java.util.ArrayList<>();
        for (int z = 0; z < i - lf - 1; ++z) c.add(0L);
        c.add((mod - k) % mod);
        for (long x : last) c.add(x * k % mod);
        while (c.size() < cur.size()) c.add(0L);
        for (int j = 0; j < cur.size(); ++j) c.set(j, (c.get(j) + cur.get(j)) % mod);
        if (i - lf + last.size() >= cur.size()) {
            last = cur; lf = i; ld = (s[i] - t) % mod;
        }
        cur = c;
    }
    long[] r = new long[cur.size()];
    for (int j = 0; j < r.length; ++j) r[j] = ((cur.get(j) % mod) + mod) % mod;
    return r;
}

// Compute Nth term of linear recurrence f[] with initial values s[]
// Using matrix exponentiation O(K² log N) or Kitamasa's O(K log K log N)
static long linearRecurrence(long[] f, long[] s, long n, long mod) {
    int k = f.length;
    if (n < k) return s[(int) n];
    // Build companion matrix, then use matrix exponentiation (see section 4.11)
    // For simplicity: iterative O(K²) per step is fine for small K
    long[] cur = java.util.Arrays.copyOf(s, k);
    for (long i = k; i <= n; ++i) {
        long next = 0;
        for (int j = 0; j < k; ++j) next = (next + f[j] * cur[k - 1 - j]) % mod;
        System.arraycopy(cur, 1, cur, 0, k - 1);
        cur[k - 1] = next;
    }
    return cur[k - 1];
}
```

---

## 17.5 Primitive Root and Discrete Logarithm

```java
// Find primitive root modulo p (p is prime)
static long primitiveRoot(long p) {
    java.util.Map<Long, Integer> phiFactors = factorize(p - 1);
    for (long g = 2; ; ++g) {
        boolean ok = true;
        for (long q : phiFactors.keySet())
            if (power(g, (p - 1) / q, p) == 1) { ok = false; break; }
        if (ok) return g;
    }
}

// Baby-step Giant-step discrete log: find x such that g^x ≡ a (mod p)
// O(√p) time and space
static long babyGiantStep(long g, long a, long p) {
    long m = (long) Math.ceil(Math.sqrt((double) p));
    java.util.HashMap<Long, Long> table = new java.util.HashMap<>();
    long gm = power(g, m, p);
    long val = a;
    for (long j = 0; j <= m; ++j) { table.put(val, j); val = val * g % p; }
    val = 1;
    for (long i = 1; i <= m; ++i) {
        val = val * gm % p;
        Long jj = table.get(val);
        if (jj != null) {
            long ans = i * m - jj;
            return ((ans % (p - 1)) + (p - 1)) % (p - 1);
        }
    }
    return -1;  // no solution
}
```

---

## 17.6 Integer Partitions and Generating Functions

```java
// Number of ways to partition n using parts from set S (generating function approach)
// dp[i] = number of ways to form sum i
static long[] countPartitions(int n, int[] parts, long mod) {
    long[] dp = new long[n + 1];
    dp[0] = 1;
    for (int p : parts)
        for (int i = p; i <= n; ++i)
            dp[i] = (dp[i] + dp[i - p]) % mod;
    return dp;
}

// Euler's partition function p(n) — number of ways to partition n
// p(0)=1, p(1)=1, p(2)=2, p(3)=3, p(4)=5, p(5)=7, ...
// Computed via Euler's pentagonal theorem in O(N√N)
```

---

## 17.7 Möbius Function and Inclusion-Exclusion over Divisors

```java
// Compute Möbius function mu[1..N]
// mu[n] = 0 if n has squared prime factor
//       = (-1)^k if n is product of k distinct primes
static int[] mobiusSieve(int N) {
    int[] mu = new int[N + 1];
    boolean[] notPrime = new boolean[N + 1];
    int[] primes = new int[N + 1];
    int pc = 0;
    mu[1] = 1;
    for (int i = 2; i <= N; ++i) {
        if (!notPrime[i]) { primes[pc++] = i; mu[i] = -1; }
        for (int j = 0; j < pc && (long) i * primes[j] <= N; ++j) {
            notPrime[i * primes[j]] = true;
            if (i % primes[j] == 0) { mu[i * primes[j]] = 0; break; }
            mu[i * primes[j]] = -mu[i];
        }
    }
    return mu;
}

// Count pairs (a,b) with 1<=a,b<=N and gcd(a,b)=1 using Möbius
// Answer = sum_{d=1}^{N} mu[d] * floor(N/d)^2
static long coprimePairs(int N, int[] mu) {
    long ans = 0;
    for (int d = 1; d <= N; ++d)
        if (mu[d] != 0) ans += (long) mu[d] * (N / d) * (N / d);
    return ans;
}
```

---

## 17.8 Summary

| Problem | Algorithm | Complexity |
|---------|-----------|------------|
| Is N prime? (N up to 10^18) | Miller-Rabin | O(12 log²N) |
| Factorise N (N up to 10^18) | Pollard's Rho + Miller-Rabin | O(N^(1/4)) |
| Polynomial multiplication mod p | NTT | O(N log N) |
| Nth term of linear recurrence | BM + matrix exp | O(K² log N) |
| Discrete log g^x = a (mod p) | Baby-step Giant-step | O(√p) |
| Count coprime pairs | Möbius sieve + summation | O(N log log N) |
| Find primitive root mod p | Trial with Euler's criterion | O(p^(1/4) log p) |

> **Java note**: every modular routine here works in `long`. The single overflow
> hazard is 64-bit × 64-bit modmul (17.1/17.2), solved with `BigInteger` or
> `Math.multiplyHigh`. Everything else (NTT, sieves, BM) multiplies values < 10^9,
> so plain `long` arithmetic stays well within range.

---

**Back to**: [README](README.md)
