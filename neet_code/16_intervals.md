# Intervals -- NeetCode 150

---

## Problem 130: Insert Interval
**LeetCode #57** | Insert a new interval into sorted non-overlapping intervals.

### Brute Force -- O(N log N) append + merge
```java
int[][] insertBf(int[][] intervals, int[] newInterval) {
    List<int[]> all = new ArrayList<>(Arrays.asList(intervals));
    all.add(newInterval);
    all.sort((a, b) -> a[0] - b[0]);
    List<int[]> res = new ArrayList<>();
    res.add(all.get(0));
    for (int i = 1; i < all.size(); i++) {
        int[] cur = all.get(i), last = res.get(res.size() - 1);
        if (cur[0] <= last[1]) last[1] = Math.max(last[1], cur[1]);
        else res.add(cur);
    }
    return res.toArray(new int[0][]);
}
```

### Optimal -- O(N) three-phase single pass
```java
int[][] insert(int[][] intervals, int[] newInterval) {
    List<int[]> res = new ArrayList<>();
    int i = 0, n = intervals.length;
    while (i < n && intervals[i][1] < newInterval[0]) res.add(intervals[i++]);
    while (i < n && intervals[i][0] <= newInterval[1]) {
        newInterval[0] = Math.min(newInterval[0], intervals[i][0]);
        newInterval[1] = Math.max(newInterval[1], intervals[i][1]);
        i++;
    }
    res.add(newInterval);
    while (i < n) res.add(intervals[i++]);
    return res.toArray(new int[0][]);
}
```

---

## Problem 131: Merge Intervals
**LeetCode #56**

### Brute Force -- O(N^2) repeatedly merge any two overlapping intervals
### Optimal -- O(N log N) sort by start, merge
```java
int[][] merge(int[][] intervals) {
    Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
    List<int[]> res = new ArrayList<>();
    res.add(intervals[0]);
    for (int i = 1; i < intervals.length; i++) {
        int[] cur = intervals[i], last = res.get(res.size() - 1);
        if (cur[0] <= last[1]) last[1] = Math.max(last[1], cur[1]);
        else res.add(cur);
    }
    return res.toArray(new int[0][]);
}
```

---

## Problem 132: Non-overlapping Intervals
**LeetCode #435** | Min intervals to remove to make rest non-overlapping.

### Brute Force -- O(2^N) try all subsets of intervals
### Optimal -- O(N log N) sort by end, greedy (similar to activity selection)
```java
int eraseOverlapIntervals(int[][] intervals) {
    Arrays.sort(intervals, (a, b) -> a[1] - b[1]);
    int cnt = 0, end = Integer.MIN_VALUE;
    for (int[] iv : intervals) {
        if (iv[0] >= end) end = iv[1];
        else cnt++;
    }
    return cnt;
}
```

---

## Problem 133: Meeting Rooms
**LeetCode #252** | Can a person attend all meetings? (no overlap check)

### Brute Force -- O(N^2) check all pairs for overlap
### Optimal -- O(N log N) sort by start, check consecutive
```java
boolean canAttendMeetings(int[][] intervals) {
    Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
    for (int i = 1; i < intervals.length; i++)
        if (intervals[i][0] < intervals[i - 1][1]) return false;
    return true;
}
```

---

## Problem 134: Meeting Rooms II
**LeetCode #253** | Min number of conference rooms required.

### Brute Force -- O(N^2) simulate with room tracking
### Optimal -- O(N log N) min-heap of room end times
```java
int minMeetingRooms(int[][] intervals) {
    Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
    PriorityQueue<Integer> pq = new PriorityQueue<>(); // min-heap of end times
    for (int[] iv : intervals) {
        if (!pq.isEmpty() && pq.peek() <= iv[0]) pq.poll();
        pq.offer(iv[1]);
    }
    return pq.size();
}
```

---

## Problem 135: Minimum Interval to Include Each Query
**LeetCode #1851** | For each query, find the smallest interval containing it.

### Brute Force -- O(N * Q) check all intervals for each query
```java
int[] minIntervalBf(int[][] intervals, int[] queries) {
    int[] res = new int[queries.length];
    for (int qi = 0; qi < queries.length; qi++) {
        int q = queries[qi], best = Integer.MAX_VALUE;
        for (int[] iv : intervals)
            if (iv[0] <= q && q <= iv[1]) best = Math.min(best, iv[1] - iv[0] + 1);
        res[qi] = best == Integer.MAX_VALUE ? -1 : best;
    }
    return res;
}
```

### Optimal -- O((N+Q) log(N+Q)) sort intervals and queries, sweep with min-heap
```java
int[] minInterval(int[][] intervals, int[] queries) {
    Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
    int n = queries.length;
    Integer[] idx = new Integer[n];
    for (int i = 0; i < n; i++) idx[i] = i;
    Arrays.sort(idx, (a, b) -> queries[a] - queries[b]);
    // min-heap: {size, end}
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
    int[] res = new int[n];
    Arrays.fill(res, -1);
    int i = 0;
    for (int qi : idx) {
        int q = queries[qi];
        while (i < intervals.length && intervals[i][0] <= q) {
            int sz = intervals[i][1] - intervals[i][0] + 1;
            pq.offer(new int[]{sz, intervals[i][1]});
            i++;
        }
        while (!pq.isEmpty() && pq.peek()[1] < q) pq.poll();
        if (!pq.isEmpty()) res[qi] = pq.peek()[0];
    }
    return res;
}
```
