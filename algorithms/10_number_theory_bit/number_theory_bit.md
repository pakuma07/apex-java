# Number Theory and Bit Algorithms

This chapter has been split into micro-files.

## Structure Files

1. [Prime Checking](structures/prime_checking.md)
2. [Sieve of Eratosthenes](structures/sieve.md)
3. [GCD and LCM](structures/gcd_lcm.md)
4. [Fast Exponentiation](structures/fast_exponentiation.md)
5. [Bit Operations](structures/bit_operations.md)
6. [Bitmask Subsets](structures/bitmask_subsets.md)
7. [Number and Bit Overview](structures/number_bit_overview.md)

## Practice

- ../exercises/number_theory_bit_exercises.md

---

## Next Steps

- Review the cheat sheet: [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md)
- Previous: [Chapter 9: String Algorithms](../09_string_algorithms/string_algorithms.md)
- Next: [Chapter 11: Advanced Algorithms](../11_advanced_algorithms/advanced_algorithms.md)

## Java Example: Sieve of Eratosthenes

```java
import java.util.ArrayList;
import java.util.List;

static List<Integer> sieve(int n) {
    boolean[] prime = new boolean[n + 1];
    Arrays.fill(prime, true);
    List<Integer> ans = new ArrayList<>();
    if (n >= 0) prime[0] = false;
    if (n >= 1) prime[1] = false;
    for (int i = 2; i <= n; i++) {
        if (!prime[i]) continue;
        ans.add(i);
        if ((long) i * i <= n)
            for (int j = i * i; j <= n; j += i) prime[j] = false;
    }
    return ans;
}
```
