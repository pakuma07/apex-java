// divide_and_conquer.java
// Maximum-subarray sum solved by divide and conquer.

public class divide_and_conquer {

    // Best subarray sum that MUST straddle the midpoint m (uses both halves).
    // Technique: grow a run leftward from m and rightward from m+1. Time O(r-l), Space O(1).
    static int maxCrossing(int[] a, int l, int m, int r) {
        int leftBest = Integer.MIN_VALUE, sum = 0;
        for (int i = m; i >= l; --i) {     // extend left from the center
            sum += a[i];
            leftBest = Math.max(leftBest, sum);
        }
        int rightBest = Integer.MIN_VALUE;
        sum = 0;
        for (int i = m + 1; i <= r; ++i) { // extend right from the center
            sum += a[i];
            rightBest = Math.max(rightBest, sum);
        }
        return leftBest + rightBest;       // both halves are non-empty by construction
    }

    // Maximum subarray sum: answer is the best of left half, right half, or crossing.
    // Technique: divide and conquer. Time O(n log n), Space O(log n) recursion depth.
    static int maxSubarrayDC(int[] a, int l, int r) {
        if (l == r) return a[l];           // base case: single element
        int m = l + (r - l) / 2;
        return Math.max(Math.max(maxSubarrayDC(a, l, m), maxSubarrayDC(a, m + 1, r)),
                        maxCrossing(a, l, m, r));
    }

    public static void main(String[] args) {
        int[] a = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println("max subarray = " + maxSubarrayDC(a, 0, a.length - 1));
    }
}
