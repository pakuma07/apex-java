// pattern_based.java
// Classic array patterns: two-pointer search and fixed-size sliding window.

public class pattern_based {

    // Two-pointer pair sum: does any pair in a SORTED array add up to target?
    // Technique: converge two ends toward the middle. Time O(n), Space O(1).
    static boolean hasPairSum(int[] a, int target) {
        int l = 0, r = a.length - 1;
        while (l < r) {
            int s = a[l] + a[r];
            if (s == target) return true;
            if (s < target) l++; // need a larger sum -> advance left up
            else r--;            // need a smaller sum -> pull right down
        }
        return false;
    }

    // Maximum sum of any contiguous window of size k.
    // Technique: fixed-size sliding window. Time O(n), Space O(1).
    static int maxSumK(int[] a, int k) {
        if (a.length < k) return 0;
        int sum = 0;
        for (int i = 0; i < k; ++i) sum += a[i]; // sum of the first window
        int best = sum;
        for (int i = k; i < a.length; ++i) {
            sum += a[i] - a[i - k]; // slide: add entering, drop leaving element
            best = Math.max(best, sum);
        }
        return best;
    }

    public static void main(String[] args) {
        int[] sorted = {1, 2, 3, 4, 7, 10};
        // print 1/0 to mirror C++ stream output of a bool
        System.out.println("pair sum 11: " + (hasPairSum(sorted, 11) ? 1 : 0));

        int[] a = {2, 1, 5, 1, 3, 2};
        System.out.println("max sum window size 3: " + maxSumK(a, 3));
    }
}
