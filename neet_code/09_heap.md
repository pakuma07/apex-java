# Heap / Priority Queue -- NeetCode 150

---

## Problem 64: Kth Largest Element in a Stream
**LeetCode #703** | Design class that finds kth largest element in a stream.

### Brute Force -- O(N log N) sort on every add
```java
class KthLargest {
    private List<Integer> data;
    private int k;
    public KthLargest(int k, int[] nums) {
        this.k = k;
        data = new ArrayList<>();
        for (int x : nums) data.add(x);
    }
    public int add(int val) {
        data.add(val);
        data.sort(Collections.reverseOrder());
        return data.get(k - 1);
    }
}
```

### Optimal -- O(log K) add using min-heap of size k
```java
class KthLargest {
    private PriorityQueue<Integer> pq = new PriorityQueue<>(); // min-heap
    private int k;
    public KthLargest(int k, int[] nums) {
        this.k = k;
        for (int x : nums) add(x);
    }
    public int add(int val) {
        pq.offer(val);
        if (pq.size() > k) pq.poll();
        return pq.peek();
    }
}
```

---

## Problem 65: Last Stone Weight
**LeetCode #1046** | Smash two heaviest stones repeatedly; return last stone or 0.

### Brute Force -- O(N^2 log N) sort on each step
```java
int lastStoneWeight(int[] stones) {
    List<Integer> s = new ArrayList<>();
    for (int x : stones) s.add(x);
    Collections.sort(s);
    while (s.size() > 1) {
        int a = s.remove(s.size() - 1);
        int b = s.remove(s.size() - 1);
        if (a != b) { s.add(a - b); Collections.sort(s); }
    }
    return s.isEmpty() ? 0 : s.get(0);
}
```

### Optimal -- O(N log N) max-heap
```java
int lastStoneWeight(int[] stones) {
    PriorityQueue<Integer> pq = new PriorityQueue<>(Collections.reverseOrder());
    for (int x : stones) pq.offer(x);
    while (pq.size() > 1) {
        int a = pq.poll();
        int b = pq.poll();
        if (a != b) pq.offer(a - b);
    }
    return pq.isEmpty() ? 0 : pq.peek();
}
```

---

## Problem 66: K Closest Points to Origin
**LeetCode #973**

### Brute Force -- O(N log N) sort all by distance
```java
int[][] kClosest(int[][] pts, int k) {
    Arrays.sort(pts, (a, b) -> (a[0] * a[0] + a[1] * a[1]) - (b[0] * b[0] + b[1] * b[1]));
    return Arrays.copyOfRange(pts, 0, k);
}
```

### Optimal -- O(N log K) max-heap of size k
```java
int[][] kClosest(int[][] pts, int k) {
    // max-heap on {dist, index}; keep k smallest distances
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> b[0] - a[0]);
    for (int i = 0; i < pts.length; i++) {
        int dist = pts[i][0] * pts[i][0] + pts[i][1] * pts[i][1];
        pq.offer(new int[]{dist, i});
        if (pq.size() > k) pq.poll();
    }
    int[][] res = new int[pq.size()][];
    int idx = 0;
    while (!pq.isEmpty()) res[idx++] = pts[pq.poll()[1]];
    return res;
}
```

---

## Problem 67: Kth Largest Element in an Array
**LeetCode #215**

### Brute Force -- O(N log N) sort
```java
int findKthLargest(int[] nums, int k) {
    Arrays.sort(nums);
    return nums[nums.length - k];
}
```

### Optimal -- O(N) average QuickSelect
```java
int findKthLargest(int[] nums, int k) {
    int n = nums.length, target = n - k;
    return qs(nums, 0, n - 1, target);
}
int qs(int[] nums, int l, int r, int target) {
    int pivot = nums[r], p = l;
    for (int i = l; i < r; i++) if (nums[i] <= pivot) swap(nums, i, p++);
    swap(nums, p, r);
    if (p == target) return nums[p];
    return p < target ? qs(nums, p + 1, r, target) : qs(nums, l, p - 1, target);
}
void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }
```

---

## Problem 68: Task Scheduler
**LeetCode #621** | Min intervals to run all tasks with cooldown n between same tasks.

### Brute Force -- O(T * intervals) simulate the scheduling
```java
int leastInterval(char[] tasks, int n) {
    int[] cnt = new int[26];
    for (char c : tasks) cnt[c - 'A']++;
    int time = 0;
    while (true) {
        boolean any = false;
        int[] cooldown = new int[26];
        Arrays.fill(cooldown, -1);
        // simulation gets complex -- use formula instead
        break;
    }
    return 0; // placeholder
}
```

### Optimal -- O(N) formula-based
```java
int leastInterval(char[] tasks, int n) {
    int[] cnt = new int[26];
    for (char c : tasks) cnt[c - 'A']++;
    int maxCnt = 0;
    for (int c : cnt) maxCnt = Math.max(maxCnt, c);
    int maxFreq = 0;
    for (int c : cnt) if (c == maxCnt) maxFreq++;
    // Formula: max(tasks.length, (maxCnt-1)*(n+1)+maxFreq)
    return Math.max(tasks.length, (maxCnt - 1) * (n + 1) + maxFreq);
}
```

---

## Problem 69: Design Twitter
**LeetCode #355** | postTweet, getNewsFeed (10 most recent), follow, unfollow.

### Brute Force -- O(N log N) collect all followed tweets, sort
```java
class Twitter {
    private int ts = 0;
    private Map<Integer, List<int[]>> tweets = new HashMap<>();       // userId -> [{time, tweetId}]
    private Map<Integer, Set<Integer>> following = new HashMap<>();
    public void postTweet(int u, int t) {
        tweets.computeIfAbsent(u, k -> new ArrayList<>()).add(new int[]{ts++, t});
    }
    public List<Integer> getNewsFeed(int u) {
        List<int[]> all = new ArrayList<>();
        following.computeIfAbsent(u, k -> new HashSet<>()).add(u);
        for (int f : following.get(u))
            for (int[] p : tweets.getOrDefault(f, List.of())) all.add(p);
        all.sort((a, b) -> b[0] - a[0]);
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < Math.min(10, all.size()); i++) res.add(all.get(i)[1]);
        return res;
    }
    public void follow(int u, int f)   { following.computeIfAbsent(u, k -> new HashSet<>()).add(f); }
    public void unfollow(int u, int f) { following.computeIfAbsent(u, k -> new HashSet<>()).remove(f); }
}
```

### Optimal -- O(K log K) where K = number of followed users (heap merge)
```java
class Twitter {
    private int ts = 0;
    private Map<Integer, List<int[]>> tweets = new HashMap<>();
    private Map<Integer, Set<Integer>> following = new HashMap<>();
    public void postTweet(int u, int t) {
        tweets.computeIfAbsent(u, k -> new ArrayList<>()).add(new int[]{ts++, t});
    }
    public List<Integer> getNewsFeed(int u) {
        // max-heap by time: {time, tweetId, userId, tweetIndex}
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> b[0] - a[0]);
        following.computeIfAbsent(u, k -> new HashSet<>()).add(u);
        for (int f : following.get(u)) {
            List<int[]> ft = tweets.getOrDefault(f, List.of());
            if (!ft.isEmpty()) {
                int idx = ft.size() - 1;
                pq.offer(new int[]{ft.get(idx)[0], ft.get(idx)[1], f, idx});
            }
        }
        List<Integer> res = new ArrayList<>();
        while (!pq.isEmpty() && res.size() < 10) {
            int[] top = pq.poll();
            res.add(top[1]);
            int f = top[2], idx = top[3];
            if (idx > 0) {
                List<int[]> ft = tweets.get(f);
                pq.offer(new int[]{ft.get(idx - 1)[0], ft.get(idx - 1)[1], f, idx - 1});
            }
        }
        return res;
    }
    public void follow(int u, int f)   { following.computeIfAbsent(u, k -> new HashSet<>()).add(f); }
    public void unfollow(int u, int f) { following.computeIfAbsent(u, k -> new HashSet<>()).remove(f); }
}
```

---

## Problem 70: Find Median from Data Stream
**LeetCode #295** | addNum, findMedian in O(log N) and O(1).

### Brute Force -- O(N log N) sort on each findMedian
```java
class MedianFinder {
    private List<Integer> data = new ArrayList<>();
    public void addNum(int n) { data.add(n); Collections.sort(data); }
    public double findMedian() {
        int n = data.size();
        return n % 2 == 1 ? data.get(n / 2) : (data.get(n / 2 - 1) + data.get(n / 2)) / 2.0;
    }
}
```

### Optimal -- O(log N) add, O(1) findMedian (two heaps)
```java
class MedianFinder {
    private PriorityQueue<Integer> lo = new PriorityQueue<>(Collections.reverseOrder()); // max-heap, lower half
    private PriorityQueue<Integer> hi = new PriorityQueue<>();                            // min-heap, upper half
    public void addNum(int n) {
        lo.offer(n);
        hi.offer(lo.poll());
        if (hi.size() > lo.size() + 1) lo.offer(hi.poll());
    }
    public double findMedian() {
        if (hi.size() > lo.size()) return hi.peek();
        return (lo.peek() + hi.peek()) / 2.0;
    }
}
```
