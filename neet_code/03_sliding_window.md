# Sliding Window -- NeetCode 150

---

## Problem 15: Best Time to Buy and Sell Stock
**LeetCode #121** | Find max profit from one buy/sell (buy before sell).

### Brute Force -- O(N^2) time, O(1) space
```java
int maxProfit(int[] prices) {
    int n = prices.length, best = 0;
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)
            best = Math.max(best, prices[j] - prices[i]);
    return best;
}
```

### Optimal -- O(N) time, O(1) space
```java
int maxProfit(int[] prices) {
    int minPrice = Integer.MAX_VALUE, best = 0;
    for (int p : prices) {
        best = Math.max(best, p - minPrice);
        minPrice = Math.min(minPrice, p);
    }
    return best;
}
```

---

## Problem 16: Longest Substring Without Repeating Characters
**LeetCode #3** | Find length of the longest substring with all unique characters.

### Brute Force -- O(N^2) or O(N^3) time
```java
int lengthOfLongestSubstring(String s) {
    int n = s.length(), best = 0;
    for (int i = 0; i < n; i++) {
        Set<Character> seen = new HashSet<>();
        for (int j = i; j < n; j++) {
            if (!seen.add(s.charAt(j))) break;
            best = Math.max(best, j - i + 1);
        }
    }
    return best;
}
```

### Optimal -- O(N) time, O(1) space (128 ASCII chars)
```java
int lengthOfLongestSubstring(String s) {
    int[] last = new int[128]; // last seen index of each char (-1 = not seen)
    Arrays.fill(last, -1);
    int l = 0, best = 0;
    for (int r = 0; r < s.length(); r++) {
        char c = s.charAt(r);
        if (last[c] >= l) l = last[c] + 1;
        last[c] = r;
        best = Math.max(best, r - l + 1);
    }
    return best;
}
```

---

## Problem 17: Longest Repeating Character Replacement
**LeetCode #424** | Replace at most k characters; find longest substring with all same character.

### Brute Force -- O(N^2) time
```java
int characterReplacement(String s, int k) {
    int n = s.length(), best = 0;
    for (int i = 0; i < n; i++) {
        int[] cnt = new int[26];
        int maxCnt = 0;
        for (int j = i; j < n; j++) {
            cnt[s.charAt(j) - 'A']++;
            maxCnt = Math.max(maxCnt, cnt[s.charAt(j) - 'A']);
            if ((j - i + 1) - maxCnt <= k) best = Math.max(best, j - i + 1);
            else break;
        }
    }
    return best;
}
```

### Optimal -- O(N) time, O(1) space (sliding window)
```java
int characterReplacement(String s, int k) {
    int[] cnt = new int[26];
    int l = 0, maxCnt = 0, best = 0;
    for (int r = 0; r < s.length(); r++) {
        cnt[s.charAt(r) - 'A']++;
        maxCnt = Math.max(maxCnt, cnt[s.charAt(r) - 'A']);
        // window size - max freq > k: shrink left
        if ((r - l + 1) - maxCnt > k) {
            cnt[s.charAt(l) - 'A']--;
            l++;
        }
        best = Math.max(best, r - l + 1);
    }
    return best;
}
```

---

## Problem 18: Permutation in String
**LeetCode #567** | Return true if s2 contains a permutation of s1.

### Brute Force -- O(N1! * N2) -- generate all permutations of s1, search in s2
```java
boolean checkInclusion(String s1, String s2) {
    char[] arr = s1.toCharArray();
    Arrays.sort(arr);
    do {
        if (s2.contains(new String(arr))) return true;
    } while (nextPermutation(arr));
    return false;
}
// Standard next-permutation helper; returns false when the last permutation is reached.
boolean nextPermutation(char[] a) {
    int i = a.length - 2;
    while (i >= 0 && a[i] >= a[i + 1]) i--;
    if (i < 0) return false;
    int j = a.length - 1;
    while (a[j] <= a[i]) j--;
    char tmp = a[i]; a[i] = a[j]; a[j] = tmp;
    for (int l = i + 1, r = a.length - 1; l < r; l++, r--) {
        tmp = a[l]; a[l] = a[r]; a[r] = tmp;
    }
    return true;
}
```

### Optimal -- O(N) time, O(1) space (fixed-size window)
```java
boolean checkInclusion(String s1, String s2) {
    if (s1.length() > s2.length()) return false;
    int[] need = new int[26], have = new int[26];
    for (char c : s1.toCharArray()) need[c - 'a']++;
    int k = s1.length(), matches = 0;
    // matches = number of chars where have[c] == need[c]
    for (int r = 0; r < s2.length(); r++) {
        int c = s2.charAt(r) - 'a';
        have[c]++;
        if (have[c] == need[c]) matches++;
        if (r >= k) {
            int lc = s2.charAt(r - k) - 'a';
            if (have[lc] == need[lc]) matches--;
            have[lc]--;
        }
        if (matches == 26) return true;
    }
    return false;
}
```

---

## Problem 19: Minimum Window Substring
**LeetCode #76** | Find minimum window in s that contains all chars of t.

### Brute Force -- O(N^2) time
```java
String minWindow(String s, String t) {
    int n = s.length(), best = Integer.MAX_VALUE, start = 0;
    for (int i = 0; i < n; i++) {
        Map<Character, Integer> need = new HashMap<>();
        for (char c : t.toCharArray()) need.merge(c, 1, Integer::sum);
        int cnt = t.length();
        for (int j = i; j < n; j++) {
            char c = s.charAt(j);
            if (need.containsKey(c)) {
                if (need.get(c) > 0) cnt--;
                need.merge(c, -1, Integer::sum);
            }
            if (cnt == 0 && j - i + 1 < best) { best = j - i + 1; start = i; break; }
        }
    }
    return best == Integer.MAX_VALUE ? "" : s.substring(start, start + best);
}
```

### Optimal -- O(N) time, O(1) space (sliding window with have/need counts)
```java
String minWindow(String s, String t) {
    if (t.isEmpty()) return "";
    Map<Character, Integer> need = new HashMap<>(), have = new HashMap<>();
    for (char c : t.toCharArray()) need.merge(c, 1, Integer::sum);
    int formed = 0, required = need.size();
    int l = 0, best = Integer.MAX_VALUE, bstart = 0;
    for (int r = 0; r < s.length(); r++) {
        char c = s.charAt(r);
        have.merge(c, 1, Integer::sum);
        if (need.containsKey(c) && have.get(c).equals(need.get(c))) formed++;
        while (formed == required) {
            if (r - l + 1 < best) { best = r - l + 1; bstart = l; }
            char lc = s.charAt(l);
            have.merge(lc, -1, Integer::sum);
            if (need.containsKey(lc) && have.get(lc) < need.get(lc)) formed--;
            l++;
        }
    }
    return best == Integer.MAX_VALUE ? "" : s.substring(bstart, bstart + best);
}
```

---

## Problem 20: Sliding Window Maximum
**LeetCode #239** | Return max of every window of size k.

### Brute Force -- O(N*K) time, O(N) space
```java
int[] maxSlidingWindow(int[] nums, int k) {
    int n = nums.length;
    int[] res = new int[n - k + 1];
    for (int i = 0; i + k <= n; i++) {
        int mx = nums[i];
        for (int j = i; j < i + k; j++) mx = Math.max(mx, nums[j]);
        res[i] = mx;
    }
    return res;
}
```

### Optimal -- O(N) time, O(K) space (monotonic deque)
```java
int[] maxSlidingWindow(int[] nums, int k) {
    Deque<Integer> dq = new ArrayDeque<>(); // stores indices; front = index of max
    int[] res = new int[nums.length - k + 1];
    int idx = 0;
    for (int r = 0; r < nums.length; r++) {
        // remove indices out of window
        if (!dq.isEmpty() && dq.peekFirst() < r - k + 1) dq.pollFirst();
        // maintain decreasing order: remove smaller elements from back
        while (!dq.isEmpty() && nums[dq.peekLast()] < nums[r]) dq.pollLast();
        dq.addLast(r);
        if (r >= k - 1) res[idx++] = nums[dq.peekFirst()];
    }
    return res;
}
```
