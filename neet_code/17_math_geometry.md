# Math & Geometry -- NeetCode 150

---

## Problem 136: Rotate Image
**LeetCode #48** | Rotate NxN matrix 90 degrees clockwise in-place.

### Brute Force -- O(N^2) copy to new matrix
```java
void rotateBf(int[][] m) {
    int n = m.length;
    int[][] tmp = new int[n][n];
    for (int r = 0; r < n; r++)
        for (int c = 0; c < n; c++) tmp[c][n - 1 - r] = m[r][c];
    for (int r = 0; r < n; r++) m[r] = tmp[r];
}
```

### Optimal -- O(N^2) transpose then reverse each row
```java
void rotate(int[][] m) {
    int n = m.length;
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++) { int t = m[i][j]; m[i][j] = m[j][i]; m[j][i] = t; }
    for (int i = 0; i < n; i++)
        for (int l = 0, r = n - 1; l < r; l++, r--) { int t = m[i][l]; m[i][l] = m[i][r]; m[i][r] = t; }
}
```

---

## Problem 137: Spiral Matrix
**LeetCode #54** | Return elements in spiral order.

### Brute Force -- O(M*N) direction array simulation with visited grid
### Optimal -- O(M*N) shrink boundaries
```java
List<Integer> spiralOrder(int[][] m) {
    int top = 0, bot = m.length - 1, left = 0, right = m[0].length - 1;
    List<Integer> res = new ArrayList<>();
    while (top <= bot && left <= right) {
        for (int c = left; c <= right; c++) res.add(m[top][c]);
        top++;
        for (int r = top; r <= bot; r++) res.add(m[r][right]);
        right--;
        if (top <= bot) { for (int c = right; c >= left; c--) res.add(m[bot][c]); bot--; }
        if (left <= right) { for (int r = bot; r >= top; r--) res.add(m[r][left]); left++; }
    }
    return res;
}
```

---

## Problem 138: Set Matrix Zeroes
**LeetCode #73** | If element is 0, set its row and column to 0 in-place.

### Brute Force -- O(M*N*(M+N)) for each zero, zero out row+col
### Optimal -- O(M*N) time, O(1) space using first row/col as markers
```java
void setZeroes(int[][] m) {
    int rows = m.length, cols = m[0].length;
    boolean row0 = false, col0 = false;
    for (int c = 0; c < cols; c++) if (m[0][c] == 0) row0 = true;
    for (int r = 0; r < rows; r++) if (m[r][0] == 0) col0 = true;
    for (int r = 1; r < rows; r++)
        for (int c = 1; c < cols; c++)
            if (m[r][c] == 0) { m[r][0] = 0; m[0][c] = 0; }
    for (int r = 1; r < rows; r++)
        for (int c = 1; c < cols; c++)
            if (m[r][0] == 0 || m[0][c] == 0) m[r][c] = 0;
    if (row0) for (int c = 0; c < cols; c++) m[0][c] = 0;
    if (col0) for (int r = 0; r < rows; r++) m[r][0] = 0;
}
```

---

## Problem 139: Happy Number
**LeetCode #202** | Sum of squared digits eventually reaches 1 (happy) or cycles.

### Brute Force -- O(log N) with hash set to detect cycle
```java
boolean isHappySet(int n) {
    Set<Integer> seen = new HashSet<>();
    while (n != 1) {
        int s = 0;
        while (n != 0) { s += (n % 10) * (n % 10); n /= 10; }
        n = s;
        if (seen.contains(n)) return false;
        seen.add(n);
    }
    return true;
}
```

### Optimal -- O(log N) Floyd's cycle detection (fast/slow pointer)
```java
boolean isHappy(int n) {
    int slow = n, fast = sumSq(n);
    while (fast != 1 && slow != fast) { slow = sumSq(slow); fast = sumSq(sumSq(fast)); }
    return fast == 1;
}
int sumSq(int x) {
    int s = 0;
    while (x != 0) { s += (x % 10) * (x % 10); x /= 10; }
    return s;
}
```

---

## Problem 140: Plus One
**LeetCode #66** | Increment large integer represented as digit array.

### Brute Force -- convert to int, add 1, convert back (overflow risk)
### Optimal -- O(N) add carry from end
```java
int[] plusOne(int[] d) {
    for (int i = d.length - 1; i >= 0; i--) {
        if (d[i] < 9) { d[i]++; return d; }
        d[i] = 0;
    }
    int[] res = new int[d.length + 1];
    res[0] = 1;
    return res;
}
```

---

## Problem 141: Pow(x, n)
**LeetCode #50** | Implement pow(x, n) without TLE.

### Brute Force -- O(N) multiply x n times
```java
double myPowBf(double x, long n) {
    if (n < 0) { x = 1 / x; n = -n; }
    double r = 1;
    while (n-- > 0) r *= x;
    return r;
}
```

### Optimal -- O(log N) fast exponentiation
```java
double myPow(double x, long n) {
    if (n < 0) { x = 1 / x; n = -n; }
    double res = 1;
    while (n != 0) {
        if ((n & 1) == 1) res *= x;
        x *= x;
        n >>= 1;
    }
    return res;
}
```

---

## Problem 142: Multiply Strings
**LeetCode #43** | Multiply two non-negative integers as strings.

### Brute Force -- O(M*N) grade-school multiplication
```java
String multiply(String num1, String num2) {
    int m = num1.length(), n = num2.length();
    int[] pos = new int[m + n];
    for (int i = m - 1; i >= 0; i--)
        for (int j = n - 1; j >= 0; j--) {
            int mul = (num1.charAt(i) - '0') * (num2.charAt(j) - '0') + pos[i + j + 1];
            pos[i + j + 1] = mul % 10;
            pos[i + j] += mul / 10;
        }
    StringBuilder res = new StringBuilder();
    for (int x : pos) if (!(res.length() == 0 && x == 0)) res.append((char) ('0' + x));
    return res.length() == 0 ? "0" : res.toString();
}
```

### Optimal -- same O(M*N), above is optimal
```java
// FFT-based O(N log N) exists but is overkill for LeetCode constraints
```

---

## Problem 143: Detect Squares
**LeetCode #2013** | Add points; count axis-aligned squares with 3 given points.

### Brute Force -- O(N^3) try all triples of stored points
### Optimal -- O(N) per query using point count map
```java
class DetectSquares {
    private Map<Integer, Map<Integer, Integer>> cnt = new HashMap<>();
    private List<int[]> pts = new ArrayList<>();

    public void add(int[] p) {
        cnt.computeIfAbsent(p[0], k -> new HashMap<>()).merge(p[1], 1, Integer::sum);
        pts.add(new int[]{p[0], p[1]});
    }
    public int count(int[] p) {
        int res = 0, px = p[0], py = p[1];
        for (int[] pt : pts) {
            int x = pt[0], y = pt[1];
            if (Math.abs(px - x) != Math.abs(py - y) || x == px || y == py) continue;
            res += cnt.getOrDefault(px, Map.of()).getOrDefault(y, 0)
                 * cnt.getOrDefault(x, Map.of()).getOrDefault(py, 0);
        }
        return res;
    }
}
```
