# Two Pointers -- NeetCode 150

---

## Problem 10: Valid Palindrome
**LeetCode #125** | A phrase is a palindrome if it reads the same after lowercasing and removing non-alphanumeric chars.

### Brute Force -- O(N) time, O(N) space
```java
boolean isPalindrome(String s) {
    StringBuilder clean = new StringBuilder();
    for (char c : s.toCharArray())
        if (Character.isLetterOrDigit(c)) clean.append(Character.toLowerCase(c));
    String forward = clean.toString();
    String rev = clean.reverse().toString();
    return forward.equals(rev);
}
```

### Optimal -- O(N) time, O(1) space
```java
boolean isPalindrome(String s) {
    int l = 0, r = s.length() - 1;
    while (l < r) {
        while (l < r && !Character.isLetterOrDigit(s.charAt(l))) l++;
        while (l < r && !Character.isLetterOrDigit(s.charAt(r))) r--;
        if (Character.toLowerCase(s.charAt(l)) != Character.toLowerCase(s.charAt(r))) return false;
        l++; r--;
    }
    return true;
}
```

---

## Problem 11: Two Sum II (Input Array is Sorted)
**LeetCode #167** | Find two numbers that sum to target in a sorted array. Return 1-indexed positions.

### Brute Force -- O(N^2) time, O(1) space
```java
int[] twoSum(int[] numbers, int target) {
    int n = numbers.length;
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)
            if (numbers[i] + numbers[j] == target) return new int[]{i + 1, j + 1};
    return new int[]{};
}
```

### Optimal -- O(N) time, O(1) space (two pointers on sorted array)
```java
int[] twoSum(int[] numbers, int target) {
    int l = 0, r = numbers.length - 1;
    while (l < r) {
        int s = numbers[l] + numbers[r];
        if (s == target) return new int[]{l + 1, r + 1};
        else if (s < target) l++;
        else r--;
    }
    return new int[]{};
}
```

---

## Problem 12: 3Sum
**LeetCode #15** | Find all unique triplets that sum to zero.

### Brute Force -- O(N^3) time, O(N) space
```java
List<List<Integer>> threeSum(int[] nums) {
    int n = nums.length;
    Set<List<Integer>> res = new HashSet<>();
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)
            for (int k = j + 1; k < n; k++)
                if (nums[i] + nums[j] + nums[k] == 0) {
                    List<Integer> t = new ArrayList<>(List.of(nums[i], nums[j], nums[k]));
                    Collections.sort(t);
                    res.add(t);
                }
    return new ArrayList<>(res);
}
```

### Optimal -- O(N^2) time, O(1) extra space (sort + two pointers)
```java
List<List<Integer>> threeSum(int[] nums) {
    Arrays.sort(nums);
    List<List<Integer>> res = new ArrayList<>();
    int n = nums.length;
    for (int i = 0; i < n - 2; i++) {
        if (i > 0 && nums[i] == nums[i - 1]) continue; // skip duplicates
        int l = i + 1, r = n - 1;
        while (l < r) {
            int s = nums[i] + nums[l] + nums[r];
            if (s == 0) {
                res.add(List.of(nums[i], nums[l], nums[r]));
                while (l < r && nums[l] == nums[l + 1]) l++;
                while (l < r && nums[r] == nums[r - 1]) r--;
                l++; r--;
            } else if (s < 0) l++;
            else r--;
        }
    }
    return res;
}
```

---

## Problem 13: Container With Most Water
**LeetCode #11** | Find two lines that contain the most water.

### Brute Force -- O(N^2) time, O(1) space
```java
int maxArea(int[] height) {
    int n = height.length, best = 0;
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)
            best = Math.max(best, Math.min(height[i], height[j]) * (j - i));
    return best;
}
```

### Optimal -- O(N) time, O(1) space (greedy two pointers: always move the shorter side)
```java
int maxArea(int[] height) {
    int l = 0, r = height.length - 1, best = 0;
    while (l < r) {
        best = Math.max(best, Math.min(height[l], height[r]) * (r - l));
        if (height[l] < height[r]) l++;
        else r--;
    }
    return best;
}
```

---

## Problem 14: Trapping Rain Water
**LeetCode #42** | Compute how much water can be trapped after raining.

### Brute Force -- O(N^2) time, O(1) space
```java
int trap(int[] height) {
    int n = height.length, water = 0;
    for (int i = 1; i < n - 1; i++) {
        int lmax = 0, rmax = 0;
        for (int j = 0; j <= i; j++) lmax = Math.max(lmax, height[j]);
        for (int j = i; j < n; j++) rmax = Math.max(rmax, height[j]);
        water += Math.min(lmax, rmax) - height[i];
    }
    return water;
}
```

### Optimal -- O(N) time, O(1) space (two pointers)
```java
int trap(int[] height) {
    int l = 0, r = height.length - 1;
    int lmax = 0, rmax = 0, water = 0;
    while (l < r) {
        if (height[l] <= height[r]) {
            lmax = Math.max(lmax, height[l]);
            water += lmax - height[l];
            l++;
        } else {
            rmax = Math.max(rmax, height[r]);
            water += rmax - height[r];
            r--;
        }
    }
    return water;
}
```
