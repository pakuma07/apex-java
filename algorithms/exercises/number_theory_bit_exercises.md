# Exercises: Number Theory and Bit Algorithms

## Easy

1. Check prime.
2. GCD and LCM.
3. Fast exponentiation.
4. Check/set/clear/toggle bit.
5. Power of two check.

## Medium

1. Sieve of Eratosthenes.
2. Prime factorization.
3. Count set bits from 1 to n.
4. Generate all subsets with bitmask.
5. Modular inverse overview for prime mod.

## Hard

1. Segmented sieve.
2. XOR basis overview problem.
3. Subset DP over masks.
4. Chinese remainder theorem overview.
5. Count pairs with given xor.

## Challenge

Solve 10 bit manipulation tasks using only bitwise operators and explain each transformation.

---

## Next Steps

- Read the matching theory: [../10_number_theory_bit/number_theory_bit.md](../10_number_theory_bit/number_theory_bit.md)
- Previous: [String Algorithms Exercises](string_algorithms_exercises.md)
- Next: [Advanced Algorithms Exercises](advanced_algorithms_exercises.md)

## Java 21 Exercise Example: Fast Power Mod

```java
public class FastPowerMod {
    static long modPow(long a, long e, long mod) {
        long r = 1 % mod;
        a %= mod;
        while (e > 0) {
            if ((e & 1L) == 1L) r = (r * a) % mod;
            a = (a * a) % mod;
            e >>= 1L;
        }
        return r;
    }
}
```
