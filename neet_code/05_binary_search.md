# Binary Search -- NeetCode 150

---

## Problem 28: Binary Search
**LeetCode #704** | Search for a target in a sorted array.

### Brute Force -- O(N) linear scan
```java
int search(int[] nums, int target) {
    for (int i = 0; i < nums.length; i++)
        if (nums[i] == target) return i;
    return -1;
}
```

### Optimal -- O(log N) time, O(1) space
```java
int search(int[] nums, int target) {
    int l = 0, r = nums.length - 1;
    while (l <= r) {
        int m = l + (r - l) / 2;
        if (nums[m] == target) return m;
        else if (nums[m] < target) l = m + 1;
        else r = m - 1;
    }
    return -1;
}
```

---

## Problem 29: Search a 2D Matrix
**LeetCode #74** | Search in a matrix where rows are sorted and first element of each row > last of previous.

### Brute Force -- O(M*N) time
```java
boolean searchMatrix(int[][] matrix, int target) {
    for (int[] row : matrix)
        for (int x : row)
            if (x == target) return true;
    return false;
}
```

### Optimal -- O(log(M*N)) time (treat as one sorted array)
```java
boolean searchMatrix(int[][] matrix, int target) {
    int m = matrix.length, n = matrix[0].length;
    int l = 0, r = m * n - 1;
    while (l <= r) {
        int mid = l + (r - l) / 2;
        int val = matrix[mid / n][mid % n];
        if (val == target) return true;
        else if (val < target) l = mid + 1;
        else r = mid - 1;
    }
    return false;
}
```

---

## Problem 30: Koko Eating Bananas
**LeetCode #875** | Find minimum eating speed k to eat all piles within h hours.

### Brute Force -- O(max(piles) * N) time (try every speed from 1 upward)
```java
int minEatingSpeed(int[] piles, int h) {
    for (int k = 1; ; k++) {
        long hours = 0;
        for (int p : piles) hours += (p + k - 1) / k;
        if (hours <= h) return k;
    }
}
```

### Optimal -- O(N log(max(piles))) time, binary search on speed
```java
int minEatingSpeed(int[] piles, int h) {
    int l = 1, r = 0;
    for (int p : piles) r = Math.max(r, p);
    while (l < r) {
        int m = l + (r - l) / 2;
        long hours = 0;
        for (int p : piles) hours += (p + m - 1) / m;
        if (hours <= h) r = m;
        else l = m + 1;
    }
    return l;
}
```

---

## Problem 31: Find Minimum in Rotated Sorted Array
**LeetCode #153** | Find the minimum in a rotated sorted array (no duplicates).

### Brute Force -- O(N) linear scan
```java
int findMin(int[] nums) {
    int min = nums[0];
    for (int x : nums) min = Math.min(min, x);
    return min;
}
```

### Optimal -- O(log N) time, binary search
```java
int findMin(int[] nums) {
    int l = 0, r = nums.length - 1;
    while (l < r) {
        int m = l + (r - l) / 2;
        if (nums[m] > nums[r]) l = m + 1; // min is in right half
        else r = m;                       // min is in left half (including m)
    }
    return nums[l];
}
```

---

## Problem 32: Search in Rotated Sorted Array
**LeetCode #33** | Search for target in rotated sorted array.

### Brute Force -- O(N) linear scan
```java
int search(int[] nums, int target) {
    for (int i = 0; i < nums.length; i++)
        if (nums[i] == target) return i;
    return -1;
}
```

### Optimal -- O(log N) time, modified binary search
```java
int search(int[] nums, int target) {
    int l = 0, r = nums.length - 1;
    while (l <= r) {
        int m = l + (r - l) / 2;
        if (nums[m] == target) return m;
        // left half is sorted
        if (nums[l] <= nums[m]) {
            if (target >= nums[l] && target < nums[m]) r = m - 1;
            else l = m + 1;
        } else { // right half is sorted
            if (target > nums[m] && target <= nums[r]) l = m + 1;
            else r = m - 1;
        }
    }
    return -1;
}
```

---

## Problem 33: Time Based Key-Value Store
**LeetCode #981** | Store key-value pairs with timestamps; get value at or before a given timestamp.

### Brute Force -- O(N) get (linear scan backward)
```java
class TimeMap {
    private record Entry(int time, String value) {}
    private Map<String, List<Entry>> mp = new HashMap<>();
    public void set(String key, String value, int timestamp) {
        mp.computeIfAbsent(key, k -> new ArrayList<>()).add(new Entry(timestamp, value));
    }
    public String get(String key, int timestamp) {
        if (!mp.containsKey(key)) return "";
        List<Entry> v = mp.get(key);
        for (int i = v.size() - 1; i >= 0; i--)
            if (v.get(i).time() <= timestamp) return v.get(i).value();
        return "";
    }
}
```

### Optimal -- O(log N) get with binary search (timestamps added in order)
```java
class TimeMap {
    private record Entry(int time, String value) {}
    private Map<String, List<Entry>> mp = new HashMap<>();
    public void set(String key, String value, int timestamp) {
        mp.computeIfAbsent(key, k -> new ArrayList<>()).add(new Entry(timestamp, value));
    }
    public String get(String key, int timestamp) {
        if (!mp.containsKey(key)) return "";
        List<Entry> v = mp.get(key);
        int l = 0, r = v.size() - 1, res = -1;
        while (l <= r) {
            int m = l + (r - l) / 2;
            if (v.get(m).time() <= timestamp) { res = m; l = m + 1; }
            else r = m - 1;
        }
        return res == -1 ? "" : v.get(res).value();
    }
}
```

---

## Problem 34: Median of Two Sorted Arrays
**LeetCode #4** | Find median of two sorted arrays. O(log(min(M,N))) required.

### Brute Force -- O((M+N) log(M+N)) time (merge and find median)
```java
double findMedianSortedArrays(int[] nums1, int[] nums2) {
    int[] merged = new int[nums1.length + nums2.length];
    System.arraycopy(nums1, 0, merged, 0, nums1.length);
    System.arraycopy(nums2, 0, merged, nums1.length, nums2.length);
    Arrays.sort(merged);
    int n = merged.length;
    if (n % 2 == 1) return merged[n / 2];
    return (merged[n / 2 - 1] + merged[n / 2]) / 2.0;
}
```

### Optimal -- O(log(min(M,N))) time, binary search on partition
```java
double findMedianSortedArrays(int[] A, int[] B) {
    if (A.length > B.length) { int[] t = A; A = B; B = t; }
    int m = A.length, n = B.length;
    int l = 0, r = m;
    while (l <= r) {
        int i = l + (r - l) / 2;      // partition A
        int j = (m + n + 1) / 2 - i;  // partition B
        int AL = (i > 0) ? A[i - 1] : Integer.MIN_VALUE;
        int AR = (i < m) ? A[i]     : Integer.MAX_VALUE;
        int BL = (j > 0) ? B[j - 1] : Integer.MIN_VALUE;
        int BR = (j < n) ? B[j]     : Integer.MAX_VALUE;
        if (AL <= BR && BL <= AR) {
            if ((m + n) % 2 == 1) return Math.max(AL, BL);
            return (Math.max(AL, BL) + Math.min(AR, BR)) / 2.0;
        } else if (AL > BR) r = i - 1;
        else l = i + 1;
    }
    return 0;
}
```
