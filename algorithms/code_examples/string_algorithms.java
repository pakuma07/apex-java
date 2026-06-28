// string_algorithms.java
// String matching prerequisite: the KMP longest-prefix-suffix (failure) table.

public class string_algorithms {

    // Builds the KMP LPS table: lps[i] = length of the longest proper prefix of
    // p[0..i] that is also a suffix of it. Time O(m), space O(m).
    static int[] buildLPS(String p) {
        int[] lps = new int[p.length()];
        // i = index being filled; len = length of current matching prefix.
        for (int i = 1, len = 0; i < p.length(); ) {
            if (p.charAt(i) == p.charAt(len)) lps[i++] = ++len; // extend match: prefix grows by 1
            else if (len != 0) len = lps[len - 1];              // mismatch: fall back to shorter border
            else lps[i++] = 0;                                  // no border possible at this position
        }
        return lps;
    }

    public static void main(String[] args) {
        String p = "ababcabab";
        int[] lps = buildLPS(p);
        StringBuilder sb = new StringBuilder();
        for (int x : lps) sb.append(x).append(" ");
        System.out.println(sb.toString());
    }
}
