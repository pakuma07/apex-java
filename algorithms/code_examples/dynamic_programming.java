// dynamic_programming.java
// Classic DP examples: Fibonacci (1-D table) and 0/1 knapsack (2-D table).

public class dynamic_programming {

    // n-th Fibonacci number via bottom-up tabulation.
    // Recurrence dp[i] = dp[i-1] + dp[i-2]. Time O(n), space O(n).
    static long fib(int n) {
        if (n <= 1) return n;             // base cases F(0)=0, F(1)=1
        long[] dp = new long[n + 1];
        dp[1] = 1;
        for (int i = 2; i <= n; ++i) dp[i] = dp[i - 1] + dp[i - 2];
        return dp[n];
    }

    // Max value fitting capacity W choosing each item at most once (0/1 knapsack).
    // 2-D DP over (items considered, capacity). Time O(n*W), space O(n*W).
    static int knapsack01(int[] wt, int[] val, int W) {
        int n = wt.length;
        // dp[i][w] = best value using first i items within capacity w.
        int[][] dp = new int[n + 1][W + 1];
        for (int i = 1; i <= n; ++i) {
            for (int w = 0; w <= W; ++w) {
                dp[i][w] = dp[i - 1][w];          // skip item i (default choice)
                if (wt[i - 1] <= w) {             // item fits -> consider taking it
                    // Take item i: its value plus best for remaining capacity.
                    dp[i][w] = Math.max(dp[i][w], val[i - 1] + dp[i - 1][w - wt[i - 1]]);
                }
            }
        }
        return dp[n][W];
    }

    public static void main(String[] args) {
        System.out.println("fib(10) = " + fib(10));
        int[] wt = {1, 3, 4, 5};
        int[] val = {1, 4, 5, 7};
        System.out.println("knapsack = " + knapsack01(wt, val, 7));
    }
}
