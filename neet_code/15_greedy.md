# Greedy -- NeetCode 150

---

## Problem 122: Maximum Subarray
**LeetCode #53** | Find contiguous subarray with the largest sum (Kadane's).

### Brute Force -- O(N^2) try all subarrays
```java
int maxSubArrayBf(int[] nums) {
    int res = Integer.MIN_VALUE;
    for (int i = 0; i < nums.length; i++) {
        int s = 0;
        for (int j = i; j < nums.length; j++) { s += nums[j]; res = Math.max(res, s); }
    }
    return res;
}
```

### Optimal -- O(N) Kadane's algorithm
```java
int maxSubArray(int[] nums) {
    int cur = nums[0], res = nums[0];
    for (int i = 1; i < nums.length; i++) { cur = Math.max(nums[i], cur + nums[i]); res = Math.max(res, cur); }
    return res;
}
```

---

## Problem 123: Jump Game
**LeetCode #55** | Can you reach the last index?

### Brute Force -- O(N^2) BFS/DP array
```java
boolean canJumpDp(int[] nums) {
    int n = nums.length;
    boolean[] dp = new boolean[n];
    dp[0] = true;
    for (int i = 1; i < n; i++)
        for (int j = 0; j < i; j++)
            if (dp[j] && j + nums[j] >= i) { dp[i] = true; break; }
    return dp[n - 1];
}
```

### Optimal -- O(N) track max reachable index
```java
boolean canJump(int[] nums) {
    int reach = 0;
    for (int i = 0; i < nums.length; i++) {
        if (i > reach) return false;
        reach = Math.max(reach, i + nums[i]);
    }
    return true;
}
```

---

## Problem 124: Jump Game II
**LeetCode #45** | Min number of jumps to reach the last index.

### Brute Force -- O(N^2) BFS counting levels
### Optimal -- O(N) greedy: track current level end and next level farthest reach
```java
int jump(int[] nums) {
    int n = nums.length, jumps = 0, cur = 0, far = 0;
    for (int i = 0; i < n - 1; i++) {
        far = Math.max(far, i + nums[i]);
        if (i == cur) { jumps++; cur = far; }
    }
    return jumps;
}
```

---

## Problem 125: Gas Station
**LeetCode #134** | Find starting gas station to complete circuit; return -1 if impossible.

### Brute Force -- O(N^2) try starting from each station
```java
int canCompleteCircuitBf(int[] gas, int[] cost) {
    int n = gas.length;
    for (int s = 0; s < n; s++) {
        int tank = 0;
        boolean ok = true;
        for (int i = 0; i < n; i++) {
            tank += gas[(s + i) % n] - cost[(s + i) % n];
            if (tank < 0) { ok = false; break; }
        }
        if (ok) return s;
    }
    return -1;
}
```

### Optimal -- O(N) one pass: if total >= 0 a solution exists; start resets when tank < 0
```java
int canCompleteCircuit(int[] gas, int[] cost) {
    int total = 0, tank = 0, start = 0;
    for (int i = 0; i < gas.length; i++) {
        total += gas[i] - cost[i];
        tank += gas[i] - cost[i];
        if (tank < 0) { start = i + 1; tank = 0; }
    }
    return total >= 0 ? start : -1;
}
```

---

## Problem 126: Hand of Straights
**LeetCode #846** | Can cards be rearranged into groups of W consecutive cards?

### Brute Force -- O(N^2) sort and repeatedly pick smallest available card
### Optimal -- O(N log N) ordered map + greedy
```java
boolean isNStraightHand(int[] hand, int groupSize) {
    if (hand.length % groupSize != 0) return false;
    TreeMap<Integer, Integer> cnt = new TreeMap<>();
    for (int x : hand) cnt.merge(x, 1, Integer::sum);
    for (int k : new ArrayList<>(cnt.keySet())) {
        int v = cnt.getOrDefault(k, 0);
        if (v == 0) continue;
        for (int i = 1; i < groupSize; i++) {
            int next = cnt.getOrDefault(k + i, 0);
            if (next < v) return false;
            cnt.put(k + i, next - v);
        }
        cnt.put(k, 0);
    }
    return true;
}
```

---

## Problem 127: Merge Triplets to Form Target Triplet
**LeetCode #1899** | Select triplets and OR them together; can we reach target?

### Brute Force -- O(N^3) try all pairs of triplets combined
### Optimal -- O(N) filter triplets that don't exceed target, check if union covers target
```java
boolean mergeTriplets(int[][] triplets, int[] target) {
    int a = 0, b = 0, c = 0;
    for (int[] t : triplets) {
        if (t[0] > target[0] || t[1] > target[1] || t[2] > target[2]) continue;
        a = Math.max(a, t[0]); b = Math.max(b, t[1]); c = Math.max(c, t[2]);
    }
    return a == target[0] && b == target[1] && c == target[2];
}
```

---

## Problem 128: Partition Labels
**LeetCode #763** | Partition string so each letter appears in at most one part. Maximize parts.

### Brute Force -- O(N^2) repeatedly find the farthest extent of all chars in current window
### Optimal -- O(N) precompute last occurrence of each char
```java
List<Integer> partitionLabels(String s) {
    int[] last = new int[26];
    for (int i = 0; i < s.length(); i++) last[s.charAt(i) - 'a'] = i;
    List<Integer> res = new ArrayList<>();
    int start = 0, end = 0;
    for (int i = 0; i < s.length(); i++) {
        end = Math.max(end, last[s.charAt(i) - 'a']);
        if (i == end) { res.add(end - start + 1); start = end + 1; }
    }
    return res;
}
```

---

## Problem 129: Valid Parenthesis String
**LeetCode #678** | '*' can be '(', ')' or empty. Check if valid.

### Brute Force -- O(3^N) try all replacements for '*'
```java
boolean checkValidStringR(String s, int i, int cnt) {
    if (cnt < 0) return false;
    if (i == s.length()) return cnt == 0;
    if (s.charAt(i) == '(') return checkValidStringR(s, i + 1, cnt + 1);
    if (s.charAt(i) == ')') return checkValidStringR(s, i + 1, cnt - 1);
    return checkValidStringR(s, i + 1, cnt + 1)
        || checkValidStringR(s, i + 1, cnt - 1)
        || checkValidStringR(s, i + 1, cnt);
}
boolean checkValidStringBf(String s) { return checkValidStringR(s, 0, 0); }
```

### Optimal -- O(N) track [minOpen, maxOpen] range
```java
boolean checkValidString(String s) {
    int lo = 0, hi = 0;
    for (char c : s.toCharArray()) {
        if (c == '(') { lo++; hi++; }
        else if (c == ')') { lo--; hi--; }
        else { lo--; hi++; } // '*'
        if (hi < 0) return false;
        lo = Math.max(lo, 0);
    }
    return lo == 0;
}
```
