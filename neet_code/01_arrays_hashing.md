# Arrays & Hashing -- NeetCode 150

---

## Problem 1: Contains Duplicate
**LeetCode #217** | Given an integer array, return `true` if any value appears at least twice.

### Brute Force -- O(N^2) time, O(1) space
```java
boolean containsDuplicate(int[] nums) {
    int n = nums.length;
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)
            if (nums[i] == nums[j]) return true;
    return false;
}
```

### Optimal -- O(N) time, O(N) space
```java
boolean containsDuplicate(int[] nums) {
    Set<Integer> seen = new HashSet<>();
    for (int x : nums) {
        if (!seen.add(x)) return true;
    }
    return false;
}
```

---

## Problem 2: Valid Anagram
**LeetCode #242** | Return `true` if `t` is an anagram of `s`.

### Brute Force -- O(N log N) time, O(N) space
```java
boolean isAnagram(String s, String t) {
    if (s.length() != t.length()) return false;
    char[] a = s.toCharArray();
    char[] b = t.toCharArray();
    Arrays.sort(a);
    Arrays.sort(b);
    return Arrays.equals(a, b);
}
```

### Optimal -- O(N) time, O(1) space (26-letter alphabet)
```java
boolean isAnagram(String s, String t) {
    if (s.length() != t.length()) return false;
    int[] cnt = new int[26];
    for (int i = 0; i < s.length(); i++) {
        cnt[s.charAt(i) - 'a']++;
        cnt[t.charAt(i) - 'a']--;
    }
    for (int c : cnt) if (c != 0) return false;
    return true;
}
```

---

## Problem 3: Two Sum
**LeetCode #1** | Return indices of two numbers that add up to target.

### Brute Force -- O(N^2) time, O(1) space
```java
int[] twoSum(int[] nums, int target) {
    int n = nums.length;
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)
            if (nums[i] + nums[j] == target) return new int[]{i, j};
    return new int[]{};
}
```

### Optimal -- O(N) time, O(N) space
```java
int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> seen = new HashMap<>(); // value -> index
    for (int i = 0; i < nums.length; i++) {
        int need = target - nums[i];
        if (seen.containsKey(need)) return new int[]{seen.get(need), i};
        seen.put(nums[i], i);
    }
    return new int[]{};
}
```

---

## Problem 4: Group Anagrams
**LeetCode #49** | Group strings that are anagrams of each other.

### Brute Force -- O(N^2 * K log K) time
```java
List<List<String>> groupAnagrams(String[] strs) {
    int n = strs.length;
    boolean[] used = new boolean[n];
    List<List<String>> res = new ArrayList<>();
    for (int i = 0; i < n; i++) {
        if (used[i]) continue;
        List<String> group = new ArrayList<>();
        group.add(strs[i]);
        char[] si = strs[i].toCharArray();
        Arrays.sort(si);
        String keyI = new String(si);
        for (int j = i + 1; j < n; j++) {
            char[] sj = strs[j].toCharArray();
            Arrays.sort(sj);
            if (keyI.equals(new String(sj))) {
                group.add(strs[j]);
                used[j] = true;
            }
        }
        res.add(group);
    }
    return res;
}
```

### Optimal -- O(N * K log K) time, O(N*K) space
```java
List<List<String>> groupAnagrams(String[] strs) {
    Map<String, List<String>> mp = new HashMap<>();
    for (String s : strs) {
        char[] key = s.toCharArray();
        Arrays.sort(key);
        mp.computeIfAbsent(new String(key), k -> new ArrayList<>()).add(s);
    }
    return new ArrayList<>(mp.values());
}
```

---

## Problem 5: Top K Frequent Elements
**LeetCode #347** | Return the k most frequent elements.

### Brute Force -- O(N log N) time, O(N) space
```java
int[] topKFrequent(int[] nums, int k) {
    Map<Integer, Integer> cnt = new HashMap<>();
    for (int x : nums) cnt.merge(x, 1, Integer::sum);
    List<Map.Entry<Integer, Integer>> freq = new ArrayList<>(cnt.entrySet());
    freq.sort((a, b) -> b.getValue() - a.getValue());
    int[] res = new int[k];
    for (int i = 0; i < k; i++) res[i] = freq.get(i).getKey();
    return res;
}
```

### Optimal -- O(N) time (bucket sort), O(N) space
```java
int[] topKFrequent(int[] nums, int k) {
    Map<Integer, Integer> cnt = new HashMap<>();
    for (int x : nums) cnt.merge(x, 1, Integer::sum);
    // bucket[i] = list of numbers with frequency i
    List<List<Integer>> bucket = new ArrayList<>();
    for (int i = 0; i <= nums.length; i++) bucket.add(new ArrayList<>());
    for (Map.Entry<Integer, Integer> p : cnt.entrySet())
        bucket.get(p.getValue()).add(p.getKey());
    int[] res = new int[k];
    int idx = 0;
    for (int i = bucket.size() - 1; i >= 0 && idx < k; i--)
        for (int x : bucket.get(i)) {
            res[idx++] = x;
            if (idx == k) break;
        }
    return res;
}
```

---

## Problem 6: Encode and Decode Strings
**LeetCode #271** | Encode a list of strings into a single string; decode back.

### Brute Force (delimiter-based, fragile with special chars)
```java
String encode(List<String> strs) {
    StringBuilder res = new StringBuilder();
    for (String s : strs) res.append(s).append('\u0001'); // hope no 0x01 in input
    return res.toString();
}
List<String> decode(String s) {
    List<String> res = new ArrayList<>();
    StringBuilder cur = new StringBuilder();
    for (char c : s.toCharArray()) {
        if (c == '\u0001') { res.add(cur.toString()); cur.setLength(0); }
        else cur.append(c);
    }
    return res;
}
```

### Optimal -- length-prefix encoding, O(N) time
```java
// Format: "4#hell5#world" -- length, #, content
String encode(List<String> strs) {
    StringBuilder res = new StringBuilder();
    for (String s : strs) res.append(s.length()).append('#').append(s);
    return res.toString();
}
List<String> decode(String s) {
    List<String> res = new ArrayList<>();
    int i = 0;
    while (i < s.length()) {
        int j = i;
        while (s.charAt(j) != '#') j++;
        int len = Integer.parseInt(s.substring(i, j));
        res.add(s.substring(j + 1, j + 1 + len));
        i = j + 1 + len;
    }
    return res;
}
```

---

## Problem 7: Product of Array Except Self
**LeetCode #238** | Return array where output[i] = product of all elements except nums[i]. No division.

### Brute Force -- O(N^2) time, O(1) extra space
```java
int[] productExceptSelf(int[] nums) {
    int n = nums.length;
    int[] res = new int[n];
    Arrays.fill(res, 1);
    for (int i = 0; i < n; i++)
        for (int j = 0; j < n; j++)
            if (i != j) res[i] *= nums[j];
    return res;
}
```

### Optimal -- O(N) time, O(1) extra space (output array not counted)
```java
int[] productExceptSelf(int[] nums) {
    int n = nums.length;
    int[] res = new int[n];
    // left pass: res[i] = product of nums[0..i-1]
    int prefix = 1;
    for (int i = 0; i < n; i++) { res[i] = prefix; prefix *= nums[i]; }
    // right pass: multiply by product of nums[i+1..n-1]
    int suffix = 1;
    for (int i = n - 1; i >= 0; i--) { res[i] *= suffix; suffix *= nums[i]; }
    return res;
}
```

---

## Problem 8: Valid Sudoku
**LeetCode #36** | Determine if a 9x9 board is valid (no duplicates in rows, cols, boxes).

### Brute Force -- O(1) time (fixed 9x9), clear but verbose
```java
boolean isValidSudoku(char[][] board) {
    // Check rows
    for (int r = 0; r < 9; r++) {
        Set<Character> s = new HashSet<>();
        for (int c = 0; c < 9; c++) {
            if (board[r][c] == '.') continue;
            if (!s.add(board[r][c])) return false;
        }
    }
    // Check columns
    for (int c = 0; c < 9; c++) {
        Set<Character> s = new HashSet<>();
        for (int r = 0; r < 9; r++) {
            if (board[r][c] == '.') continue;
            if (!s.add(board[r][c])) return false;
        }
    }
    // Check 3x3 boxes
    for (int br = 0; br < 3; br++) for (int bc = 0; bc < 3; bc++) {
        Set<Character> s = new HashSet<>();
        for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++) {
            char ch = board[br * 3 + r][bc * 3 + c];
            if (ch == '.') continue;
            if (!s.add(ch)) return false;
        }
    }
    return true;
}
```

### Optimal -- O(1) time, single pass with bit masks
```java
boolean isValidSudoku(char[][] board) {
    int[] rows = new int[9], cols = new int[9], boxes = new int[9];
    for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
        if (board[r][c] == '.') continue;
        int bit = 1 << (board[r][c] - '1');
        int box = (r / 3) * 3 + c / 3;
        if (((rows[r] | cols[c] | boxes[box]) & bit) != 0) return false;
        rows[r] |= bit; cols[c] |= bit; boxes[box] |= bit;
    }
    return true;
}
```

---

## Problem 9: Longest Consecutive Sequence
**LeetCode #128** | Find the length of the longest consecutive elements sequence. O(N) required.

### Brute Force -- O(N^2) time
```java
int longestConsecutive(int[] nums) {
    int best = 0;
    Set<Integer> s = new HashSet<>();
    for (int x : nums) s.add(x);
    for (int x : nums) {
        int len = 1;
        while (s.contains(x + len)) len++;
        best = Math.max(best, len);
    }
    return best; // still O(N^2) worst case without start-check
}
```

### Optimal -- O(N) time, O(N) space (only start sequences)
```java
int longestConsecutive(int[] nums) {
    Set<Integer> s = new HashSet<>();
    for (int x : nums) s.add(x);
    int best = 0;
    for (int x : nums) {
        if (s.contains(x - 1)) continue; // x is not the start of a sequence
        int len = 1;
        while (s.contains(x + len)) len++;
        best = Math.max(best, len);
    }
    return best;
}
```
