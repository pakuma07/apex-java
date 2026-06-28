// greedy.java
// Activity-selection problem solved with a greedy strategy.

import java.util.Arrays;

public class greedy {

    // Max non-overlapping activities (start, end). Greedy: always take the activity
    // that finishes earliest, leaving the most room for the rest.
    // Technique: sort by end time + greedy scan. Time O(n log n), Space O(1) extra.
    static int maxActivities(int[][] intervals) {
        // order by earliest finish time (interval[1])
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[1], b[1]));
        int count = 0;
        int lastEnd = Integer.MIN_VALUE;    // finish time of the last chosen activity
        for (int[] it : intervals) {
            if (it[0] >= lastEnd) {         // starts after the last one ended -> no overlap
                count++;
                lastEnd = it[1];
            }
        }
        return count;
    }

    public static void main(String[] args) {
        int[][] acts = {{1, 2}, {3, 4}, {0, 6}, {5, 7}, {8, 9}, {5, 9}};
        System.out.println("max activities = " + maxActivities(acts));
    }
}
