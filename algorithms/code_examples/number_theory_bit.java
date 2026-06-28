// number_theory_bit.java
// Number theory with bit tricks: modular exponentiation and the Sieve of Eratosthenes.

import java.util.ArrayList;
import java.util.List;

public class number_theory_bit {

    // Computes (a^e) mod m by exponentiation-by-squaring (binary exponentiation).
    // Each bit of e either multiplies in a or not. Time O(log e), space O(1).
    static long modPow(long a, long e, long mod) {
        long r = 1 % mod;                  // 1 % mod handles mod == 1 correctly
        while (e > 0) {
            if ((e & 1) != 0) r = (r * a) % mod;  // current low bit set -> fold a into result
            a = (a * a) % mod;             // square base for the next bit's weight
            e >>= 1;                       // advance to next bit of the exponent
        }
        return r;
    }

    // Returns all primes <= n using the Sieve of Eratosthenes.
    // Marks composites by crossing off multiples. Time O(n log log n), space O(n).
    static List<Integer> sieve(int n) {
        boolean[] isPrime = new boolean[n + 1];
        for (int i = 0; i <= n; ++i) isPrime[i] = true;
        List<Integer> primes = new ArrayList<>();
        if (n >= 0) isPrime[0] = false;    // 0 and 1 are not prime
        if (n >= 1) isPrime[1] = false;
        for (int i = 2; i <= n; ++i) {
            if (isPrime[i]) {
                primes.add(i);
                // Start at i*i; smaller multiples already crossed by smaller primes.
                if ((long) i * i <= n) {   // long guard avoids int overflow in i*i
                    for (int j = i * i; j <= n; j += i) isPrime[j] = false;
                }
            }
        }
        return primes;
    }

    public static void main(String[] args) {
        System.out.println(modPow(2, 10, 1000000007L));
        List<Integer> primes = sieve(20);
        StringBuilder sb = new StringBuilder();
        for (int p : primes) sb.append(p).append(" ");
        System.out.println(sb.toString());
    }
}
