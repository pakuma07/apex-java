# Bit Manipulation -- NeetCode 150

---

## Problem 144: Single Number
**LeetCode #136** | Every element appears twice except one. Find it.

### Brute Force -- O(N) hash map count
```java
int singleNumberMap(int[] nums) {
    Map<Integer, Integer> c = new HashMap<>();
    for (int x : nums) c.merge(x, 1, Integer::sum);
    for (Map.Entry<Integer, Integer> e : c.entrySet()) if (e.getValue() == 1) return e.getKey();
    return -1;
}
```

### Optimal -- O(N) time, O(1) space: XOR all numbers
```java
int singleNumber(int[] nums) { int res = 0; for (int x : nums) res ^= x; return res; }
```

---

## Problem 145: Number of 1 Bits
**LeetCode #191** | Count set bits (popcount).

### Brute Force -- O(32) check each bit
```java
int hammingWeightBf(int n) {
    int cnt = 0;
    for (int i = 0; i < 32; i++) { cnt += (n & 1); n >>>= 1; }
    return cnt;
}
```

### Optimal -- O(k) where k = number of set bits, using n & (n-1) trick
```java
int hammingWeight(int n) { int cnt = 0; while (n != 0) { n &= n - 1; cnt++; } return cnt; }
// or: return Integer.bitCount(n);
```

---

## Problem 146: Counting Bits
**LeetCode #338** | Return array where ans[i] = number of 1s in binary of i, for i in [0,n].

### Brute Force -- O(N log N) count bits for each number individually
```java
int[] countBitsBf(int n) {
    int[] res = new int[n + 1];
    for (int i = 0; i <= n; i++) res[i] = Integer.bitCount(i);
    return res;
}
```

### Optimal -- O(N) DP: dp[i] = dp[i>>1] + (i&1)
```java
int[] countBits(int n) {
    int[] dp = new int[n + 1];
    for (int i = 1; i <= n; i++) dp[i] = dp[i >> 1] + (i & 1);
    return dp;
}
```

---

## Problem 147: Reverse Bits
**LeetCode #190** | Reverse bits of 32-bit unsigned integer.

### Brute Force -- O(32) swap bits one by one using string
```java
int reverseBitsBf(int n) {
    int res = 0;
    for (int i = 0; i < 32; i++) { res = (res << 1) | (n & 1); n >>>= 1; }
    return res;
}
```

### Optimal -- O(1) same approach but with explicit 32-bit reversal
```java
int reverseBits(int n) {
    int res = 0;
    for (int i = 0; i < 32; i++) { res = (res << 1) | (n & 1); n >>>= 1; }
    return res;
}
// Or use Integer.reverse(n), but above is clearest
```

---

## Problem 148: Missing Number
**LeetCode #268** | Find missing number in [0, n].

### Brute Force -- O(N) hash set
```java
int missingNumberSet(int[] nums) {
    Set<Integer> s = new HashSet<>();
    for (int x : nums) s.add(x);
    for (int i = 0; i <= nums.length; i++) if (!s.contains(i)) return i;
    return -1;
}
```

### Optimal -- O(N) XOR all indices and values
```java
int missingNumber(int[] nums) {
    int res = nums.length;
    for (int i = 0; i < nums.length; i++) res ^= i ^ nums[i];
    return res;
    // Or: return n*(n+1)/2 - sum(nums);
}
```

---

## Problem 149: Sum of Two Integers
**LeetCode #371** | Add two integers without using + or -.

### Brute Force -- O(N) increment one by one (trivially works but defeats purpose)
### Optimal -- O(1) bit addition: XOR for sum, AND+shift for carry
```java
int getSum(int a, int b) {
    while (b != 0) {
        int carry = (a & b) << 1;
        a = a ^ b;
        b = carry;
    }
    return a;
}
```

---

## Problem 150: Reverse Integer
**LeetCode #7** | Reverse digits of 32-bit signed integer; return 0 on overflow.

### Brute Force -- O(log N) convert to string, reverse, parse
```java
int reverseStr(int x) {
    String s = new StringBuilder(Integer.toString(Math.abs(x))).reverse().toString();
    long r = Long.parseLong(s) * (x < 0 ? -1 : 1);
    return (r > Integer.MAX_VALUE || r < Integer.MIN_VALUE) ? 0 : (int) r;
}
```

### Optimal -- O(log N) extract digits with modulo
```java
int reverse(int x) {
    long res = 0;
    while (x != 0) { res = res * 10 + (x % 10); x /= 10; }
    return (res > Integer.MAX_VALUE || res < Integer.MIN_VALUE) ? 0 : (int) res;
}
```
