# Stack -- NeetCode 150

---

## Problem 21: Valid Parentheses
**LeetCode #20** | Return true if brackets are correctly matched and nested.

### Brute Force -- O(N^2) time (repeatedly remove valid pairs)
```java
boolean isValid(String s) {
    while (s.contains("()") || s.contains("[]") || s.contains("{}")) {
        s = s.replace("()", "").replace("[]", "").replace("{}", "");
    }
    return s.isEmpty();
}
```

### Optimal -- O(N) time, O(N) space
```java
boolean isValid(String s) {
    Deque<Character> st = new ArrayDeque<>();
    for (char c : s.toCharArray()) {
        if (c == '(' || c == '[' || c == '{') { st.push(c); continue; }
        if (st.isEmpty()) return false;
        if (c == ')' && st.peek() != '(') return false;
        if (c == ']' && st.peek() != '[') return false;
        if (c == '}' && st.peek() != '{') return false;
        st.pop();
    }
    return st.isEmpty();
}
```

---

## Problem 22: Min Stack
**LeetCode #155** | Stack that supports push, pop, top, and getMin in O(1).

### Brute Force -- O(N) getMin (scan all elements)
```java
class MinStack {
    private Deque<Integer> st = new ArrayDeque<>();
    public void push(int val) { st.push(val); }
    public void pop()         { st.pop(); }
    public int  top()         { return st.peek(); }
    public int  getMin() {
        int m = Integer.MAX_VALUE;
        for (int x : st) m = Math.min(m, x);
        return m;
    }
}
```

### Optimal -- O(1) all operations (parallel min stack)
```java
class MinStack {
    private Deque<Integer> st = new ArrayDeque<>(), minSt = new ArrayDeque<>();
    public void push(int val) {
        st.push(val);
        minSt.push(minSt.isEmpty() ? val : Math.min(val, minSt.peek()));
    }
    public void pop()    { st.pop(); minSt.pop(); }
    public int  top()    { return st.peek(); }
    public int  getMin() { return minSt.peek(); }
}
```

---

## Problem 23: Evaluate Reverse Polish Notation
**LeetCode #150** | Evaluate an expression in Reverse Polish Notation.

### Brute Force (same as optimal, stack is the natural approach)
```java
// No meaningful brute force -- O(N) is already optimal
```

### Optimal -- O(N) time, O(N) space
```java
int evalRPN(String[] tokens) {
    Deque<Long> st = new ArrayDeque<>();
    for (String t : tokens) {
        switch (t) {
            case "+", "-", "*", "/" -> {
                long b = st.pop();
                long a = st.pop();
                st.push(switch (t) {
                    case "+" -> a + b;
                    case "-" -> a - b;
                    case "*" -> a * b;
                    default  -> a / b;
                });
            }
            default -> st.push(Long.parseLong(t));
        }
    }
    return st.peek().intValue();
}
```

---

## Problem 24: Generate Parentheses
**LeetCode #22** | Generate all combinations of n pairs of valid parentheses.

### Brute Force -- O(2^(2N) * N) -- generate all 2^2n strings, validate each
```java
boolean valid(String s) {
    int cnt = 0;
    for (char c : s.toCharArray()) { if (c == '(') cnt++; else cnt--; if (cnt < 0) return false; }
    return cnt == 0;
}
List<String> generateParenthesis(int n) {
    List<String> all = new ArrayList<>(), res = new ArrayList<>();
    gen("", n, all);
    for (String s : all) if (valid(s)) res.add(s);
    return res;
}
void gen(String cur, int n, List<String> all) {
    if (cur.length() == 2 * n) { all.add(cur); return; }
    gen(cur + '(', n, all);
    gen(cur + ')', n, all);
}
```

### Optimal -- O(4^N / sqrt(N)) time, backtracking with pruning
```java
List<String> generateParenthesis(int n) {
    List<String> res = new ArrayList<>();
    bt(new StringBuilder(), 0, 0, n, res);
    return res;
}
void bt(StringBuilder cur, int open, int close, int n, List<String> res) {
    if (cur.length() == 2 * n) { res.add(cur.toString()); return; }
    if (open < n)     { cur.append('('); bt(cur, open + 1, close, n, res); cur.deleteCharAt(cur.length() - 1); }
    if (close < open) { cur.append(')'); bt(cur, open, close + 1, n, res); cur.deleteCharAt(cur.length() - 1); }
}
```

---

## Problem 25: Daily Temperatures
**LeetCode #739** | For each day, find how many days until a warmer temperature.

### Brute Force -- O(N^2) time, O(1) extra space
```java
int[] dailyTemperatures(int[] t) {
    int n = t.length;
    int[] res = new int[n];
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)
            if (t[j] > t[i]) { res[i] = j - i; break; }
    return res;
}
```

### Optimal -- O(N) time, O(N) space (monotonic decreasing stack)
```java
int[] dailyTemperatures(int[] t) {
    int n = t.length;
    int[] res = new int[n];
    Deque<Integer> st = new ArrayDeque<>(); // indices with decreasing temperatures
    for (int i = 0; i < n; i++) {
        while (!st.isEmpty() && t[i] > t[st.peek()]) {
            int idx = st.pop();
            res[idx] = i - idx;
        }
        st.push(i);
    }
    return res;
}
```

---

## Problem 26: Car Fleet
**LeetCode #853** | Find the number of car fleets arriving at the target.

### Brute Force -- O(N^2) time (simulate)
```java
int carFleet(int target, int[] pos, int[] speed) {
    int n = pos.length;
    Integer[] idx = new Integer[n];
    for (int i = 0; i < n; i++) idx[i] = i;
    Arrays.sort(idx, (a, b) -> pos[b] - pos[a]); // sort by position desc
    double[] times = new double[n];
    for (int i = 0; i < n; i++) times[i] = (double)(target - pos[idx[i]]) / speed[idx[i]];
    int fleets = 0;
    double maxT = 0;
    for (double t : times) {
        if (t > maxT) { fleets++; maxT = t; }
    }
    return fleets;
}
```

### Optimal -- O(N log N) time, O(N) space (same logic, stack for clarity)
```java
int carFleet(int target, int[] pos, int[] speed) {
    int n = pos.length;
    Integer[] idx = new Integer[n];
    for (int i = 0; i < n; i++) idx[i] = i;
    Arrays.sort(idx, (a, b) -> pos[b] - pos[a]);
    Deque<Double> st = new ArrayDeque<>();
    for (int i : idx) {
        double t = (double)(target - pos[i]) / speed[i];
        if (st.isEmpty() || t > st.peek()) st.push(t); // new fleet
        // else: catches up to car ahead, same fleet
    }
    return st.size();
}
```

---

## Problem 27: Largest Rectangle in Histogram
**LeetCode #84** | Find the largest rectangle in the histogram.

### Brute Force -- O(N^2) time
```java
int largestRectangleArea(int[] heights) {
    int n = heights.length, best = 0;
    for (int i = 0; i < n; i++) {
        int minH = heights[i];
        for (int j = i; j < n; j++) {
            minH = Math.min(minH, heights[j]);
            best = Math.max(best, minH * (j - i + 1));
        }
    }
    return best;
}
```

### Optimal -- O(N) time, O(N) space (monotonic increasing stack)
```java
int largestRectangleArea(int[] heights) {
    Deque<Integer> st = new ArrayDeque<>(); // indices of increasing heights
    int best = 0, n = heights.length;
    for (int i = 0; i <= n; i++) {
        int h = (i == n) ? 0 : heights[i];
        while (!st.isEmpty() && h < heights[st.peek()]) {
            int height = heights[st.pop()];
            int width  = st.isEmpty() ? i : i - st.peek() - 1;
            best = Math.max(best, height * width);
        }
        st.push(i);
    }
    return best;
}
```
