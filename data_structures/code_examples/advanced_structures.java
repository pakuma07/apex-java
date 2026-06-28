// advanced_structures.java
// Demonstrates three advanced data structures for set/range queries:
//   - DSU (Disjoint Set Union / Union-Find)
//   - Fenwick Tree (Binary Indexed Tree) for prefix/range sums
//   - Segment Tree for point updates and range sum queries

public class advanced_structures {

    // DSU: maintains disjoint sets supporting near-O(1) merge and "same set?" queries.
    // Invariant: each element points toward its set's root; rankv bounds tree height.
    // With path compression + union by rank, find/unite run in ~O(alpha(n)) amortized.
    static class DSU {
        private final int[] parent;  // parent[x] = representative parent of x (self if root)
        private final int[] rankv;   // upper bound on the height of the tree rooted at x

        // Initialize n singleton sets: every element is its own root. O(n).
        DSU(int n) {
            parent = new int[n];
            rankv = new int[n];  // all zero by default
            for (int i = 0; i < n; ++i) parent[i] = i;
        }

        // Return the root of x's set; path compression flattens the chain en route.
        int find(int x) {
            if (parent[x] != x) parent[x] = find(parent[x]);  // point x directly at the root
            return parent[x];
        }

        // Merge the sets of a and b; returns false if already joined. Union by rank.
        boolean unite(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) return false;                  // already in the same set
            if (rankv[ra] < rankv[rb]) parent[ra] = rb;  // attach shorter tree under taller
            else if (rankv[ra] > rankv[rb]) parent[rb] = ra;
            else {
                parent[rb] = ra;  // equal ranks: pick one root and bump its rank
                rankv[ra]++;
            }
            return true;
        }
    }

    // Fenwick (Binary Indexed Tree): supports point updates and prefix sums.
    // Each 1-based slot bit[i] holds the sum of a range whose length is i's lowest set bit.
    // update and prefixSum are O(log n); space O(n). Public indices are 0-based.
    static class Fenwick {
        private final int n;
        private final int[] bit;  // 1-based tree; bit[i] sums (i - lowbit, i]

        Fenwick(int n) {
            this.n = n;
            bit = new int[n + 1];
        }

        // Add delta at position idx, propagating up by adding the low bit each step.
        void update(int idx, int delta) {
            for (++idx; idx <= n; idx += idx & -idx) bit[idx] += delta;  // idx & -idx = lowest set bit
        }

        // Sum of elements [0..idx], walking down by stripping the low bit each step.
        int prefixSum(int idx) {
            int sum = 0;
            for (++idx; idx > 0; idx -= idx & -idx) sum += bit[idx];  // remove lowest set bit to jump to parent range
            return sum;
        }

        // Inclusive range sum [l..r] via difference of two prefix sums. O(log n).
        int rangeSum(int l, int r) {
            return prefixSum(r) - (l != 0 ? prefixSum(l - 1) : 0);  // guard l==0 (no prefix below it)
        }
    }

    // SegmentTree: recursive sum-over-range structure with point updates.
    // Each node owns a segment [l..r]; children split at mid, parent = sum of children.
    // Build O(n); point update and range query each O(log n); space O(4n).
    static class SegmentTree {
        private final int n;
        private final int[] tree;  // implicit binary tree: children of node are 2*node, 2*node+1

        // Allocate 4n nodes (safe upper bound) and build the tree over [0..n-1].
        SegmentTree(int[] a) {
            n = a.length;
            tree = new int[4 * n];
            build(a, 1, 0, n - 1);  // root is node 1
        }

        // Recursively build node covering [l..r] from array a, combining children bottom-up.
        private void build(int[] a, int node, int l, int r) {
            if (l == r) {            // leaf: a single element
                tree[node] = a[l];
                return;
            }
            int mid = (l + r) / 2;
            build(a, 2 * node, l, mid);                       // left half
            build(a, 2 * node + 1, mid + 1, r);               // right half
            tree[node] = tree[2 * node] + tree[2 * node + 1]; // internal node = sum of children
        }

        // Set position idx to val, descending into the owning child then re-summing on the way up.
        private void updateRec(int node, int l, int r, int idx, int val) {
            if (l == r) {            // reached the target leaf
                tree[node] = val;
                return;
            }
            int mid = (l + r) / 2;
            if (idx <= mid) updateRec(2 * node, l, mid, idx, val);  // idx in left half
            else updateRec(2 * node + 1, mid + 1, r, idx, val);     // idx in right half
            tree[node] = tree[2 * node] + tree[2 * node + 1];       // refresh parent sum
        }

        // Sum over query range [ql..qr] for node covering [l..r].
        private int queryRec(int node, int l, int r, int ql, int qr) {
            if (qr < l || r < ql) return 0;             // node fully outside query -> identity
            if (ql <= l && r <= qr) return tree[node];  // node fully inside query -> use cached sum
            int mid = (l + r) / 2;                      // partial overlap: recurse both halves
            return queryRec(2 * node, l, mid, ql, qr)
                 + queryRec(2 * node + 1, mid + 1, r, ql, qr);
        }

        // Public wrappers starting recursion at the root node 1.
        void update(int idx, int val) { updateRec(1, 0, n - 1, idx, val); }
        int query(int l, int r) { return queryRec(1, 0, n - 1, l, r); }
    }

    // Driver: exercises each structure and prints representative results.
    // C++ streams print booleans as 1/0, so the demo mirrors that with int casts.
    public static void main(String[] a) {
        System.out.println("== DSU ==");
        DSU dsu = new DSU(6);
        dsu.unite(0, 1);
        dsu.unite(1, 2);
        dsu.unite(3, 4);
        System.out.println("find(0) == find(2): " + (dsu.find(0) == dsu.find(2) ? 1 : 0));
        System.out.println("find(0) == find(4): " + (dsu.find(0) == dsu.find(4) ? 1 : 0));

        System.out.println("\n== Fenwick Tree ==");
        Fenwick fen = new Fenwick(5);
        int[] arr = {1, 2, 3, 4, 5};
        for (int i = 0; i < arr.length; ++i) fen.update(i, arr[i]);
        System.out.println("rangeSum(1,3) = " + fen.rangeSum(1, 3));

        System.out.println("\n== Segment Tree ==");
        SegmentTree seg = new SegmentTree(arr);
        System.out.println("query(0,4) = " + seg.query(0, 4));
        seg.update(2, 10);
        System.out.println("after update idx 2 -> 10, query(0,4) = " + seg.query(0, 4));
    }
}
