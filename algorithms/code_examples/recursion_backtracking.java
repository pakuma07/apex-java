// recursion_backtracking.java
// Recursion (factorial) and backtracking (enumerating all subsets).

import java.util.ArrayList;
import java.util.List;

public class recursion_backtracking {

    // Factorial via recursion: n! = n * (n-1)!.
    // Technique: linear recursion. Time O(n), Space O(n) call stack.
    static long fact(int n) {
        if (n <= 1) return 1;            // base case 0! = 1! = 1
        return (long) n * fact(n - 1);   // (long) forces 64-bit multiply
    }

    // Enumerate all subsets of a via DFS: at each index, either skip or include it.
    // Technique: backtracking over a binary choice tree. Time O(n * 2^n), Space O(n) depth.
    static void dfsSubsets(int idx, int[] a, List<Integer> cur, List<List<Integer>> out) {
        if (idx == a.length) {           // reached a leaf: one complete subset
            out.add(new ArrayList<>(cur));
            return;
        }
        dfsSubsets(idx + 1, a, cur, out); // choice 1: exclude a[idx]
        cur.add(a[idx]);                  // choice 2: include a[idx]
        dfsSubsets(idx + 1, a, cur, out);
        cur.remove(cur.size() - 1);       // backtrack: undo the include
    }

    public static void main(String[] args) {
        System.out.println("fact(5) = " + fact(5));
        int[] a = {1, 2, 3};
        List<Integer> cur = new ArrayList<>();
        List<List<Integer>> subsets = new ArrayList<>();
        dfsSubsets(0, a, cur, subsets);
        System.out.println("subsets count = " + subsets.size());
    }
}
