// ============================================================
// COMPETITIVE PROGRAMMING TEMPLATE -- Java 21
// World-class: Lazy SegTree, Sparse Table, LCA, Dinic, NTT,
// Miller-Rabin, fast I/O, coordinate compression, geometry
//
// Compile: javac CpTemplate.java
// Run    : java CpTemplate < input.txt
//
// NOTE: A Java public class name cannot start with a digit, so this
// file is named 12_cp_template.java but the public class is CpTemplate.
// To compile, RENAME this file to CpTemplate.java (javac requires the
// file name to match the public class name). Alternatively, drop the
// `public` modifier and keep the .java file name your judge expects.
// On most online judges the submitted file is read as one blob, so the
// class name (CpTemplate / Main) is what matters, not the file name.
// ============================================================
import java.io.*;
import java.util.*;

public class CpTemplate {

    // ============================================================
    // FAST I/O
    // ============================================================
    // FastReader: tokenizing reader over BufferedReader. Far faster than
    // Scanner. Handles whitespace-separated tokens, ints, longs, doubles,
    // full lines. PrintWriter (autoFlush=false) buffers output; flush once.
    static final class FastReader {
        private final DataInputStream in;
        private final byte[] buf = new byte[1 << 16];
        private int ptr = 0, len = 0;

        FastReader(InputStream is) { in = new DataInputStream(is); }

        private int readByte() {
            if (ptr == len) {
                try { len = in.read(buf, 0, buf.length); } catch (IOException e) { throw new RuntimeException(e); }
                ptr = 0;
                if (len <= 0) return -1;
            }
            return buf[ptr++];
        }

        int nextInt() { return (int) nextLong(); }

        long nextLong() {
            int c = readByte();
            while (c == ' ' || c == '\n' || c == '\r' || c == '\t') c = readByte();
            boolean neg = false;
            if (c == '-') { neg = true; c = readByte(); }
            long val = 0;
            while (c >= '0' && c <= '9') { val = val * 10 + (c - '0'); c = readByte(); }
            return neg ? -val : val;
        }

        double nextDouble() { return Double.parseDouble(next()); }

        String next() {
            int c = readByte();
            while (c == ' ' || c == '\n' || c == '\r' || c == '\t') c = readByte();
            StringBuilder sb = new StringBuilder();
            while (c != -1 && c != ' ' && c != '\n' && c != '\r' && c != '\t') {
                sb.append((char) c);
                c = readByte();
            }
            return sb.toString();
        }

        String nextLine() {
            int c = readByte();
            StringBuilder sb = new StringBuilder();
            while (c != -1 && c != '\n') { if (c != '\r') sb.append((char) c); c = readByte(); }
            return sb.toString();
        }

        int[] readIntArray(int n) { int[] a = new int[n]; for (int i = 0; i < n; i++) a[i] = nextInt(); return a; }
        long[] readLongArray(int n) { long[] a = new long[n]; for (int i = 0; i < n; i++) a[i] = nextLong(); return a; }
    }

    static FastReader in;
    static PrintWriter out;

    // ============================================================
    // CONSTANTS
    // ============================================================
    static final int    INF  = 0x3f3f3f3f;
    static final long   LINF = 0x3f3f3f3f3f3f3f3fL;
    static final int    MOD  = 1_000_000_007;
    static final int    MOD2 = 998_244_353;
    static final double PI   = Math.acos(-1.0);
    static final double EPS  = 1e-9;

    // ============================================================
    // RANDOM (splitmix64-seeded; use for hashing / randomized algos)
    // ============================================================
    static final SplittableRandom rng = new SplittableRandom(System.nanoTime());

    // Anti-hack note: Java's HashMap is resistant to the classic Codeforces
    // unordered_map attack because it treewise-buckets collisions (O(log n)
    // worst case for Comparable keys). To be extra safe with Integer/Long
    // keys, XOR-perturb them with a random salt before insertion:
    static final long HASH_SALT = rng.nextLong();
    static long safeKey(long x) {
        x ^= HASH_SALT;
        x += 0x9e3779b97f4a7c15L;
        x  = (x ^ (x >>> 30)) * 0xbf58476d1ce4e5b9L;
        x  = (x ^ (x >>> 27)) * 0x94d049bb133111ebL;
        return x ^ (x >>> 31);
    }

    // ============================================================
    // MATH
    // ============================================================
    static long gcd(long a, long b) { return b == 0 ? a : gcd(b, a % b); }
    static long lcm(long a, long b) { return a / gcd(a, b) * b; }

    static long power(long b, long e, long m) {
        long r = 1; b %= m; if (b < 0) b += m;
        for (; e > 0; e >>= 1) { if ((e & 1) == 1) r = r * b % m; b = b * b % m; }
        return r;
    }
    static long modInv(long a, long m) { return power(a, m - 2, m); }

    // extended gcd: returns {g, x, y} with a*x + b*y = g
    static long[] extgcd(long a, long b) {
        if (b == 0) return new long[]{a, 1, 0};
        long[] r = extgcd(b, a % b);
        return new long[]{r[0], r[2], r[1] - (a / b) * r[2]};
    }

    static final int FACT_MAX = 500_005;
    static long[] fact_ = new long[FACT_MAX], invFact_ = new long[FACT_MAX];
    static void precompute() { precompute(FACT_MAX - 1); }
    static void precompute(int n) {
        fact_[0] = 1;
        for (int i = 1; i <= n; ++i) fact_[i] = fact_[i - 1] * i % MOD;
        invFact_[n] = modInv(fact_[n], MOD);
        for (int i = n - 1; i >= 0; --i) invFact_[i] = invFact_[i + 1] * (i + 1) % MOD;
    }
    static long C(int n, int k) {
        if (k < 0 || k > n) return 0;
        return fact_[n] * invFact_[k] % MOD * invFact_[n - k] % MOD;
    }

    // ============================================================
    // DSU / UNION-FIND (path compression + union by size)
    // ============================================================
    static final class DSU {
        int[] p, sz; int comps;
        DSU(int n) { p = new int[n]; sz = new int[n]; comps = n;
            for (int i = 0; i < n; i++) { p[i] = i; sz[i] = 1; } }
        int find(int x) { while (p[x] != x) { p[x] = p[p[x]]; x = p[x]; } return x; }
        boolean unite(int a, int b) {
            a = find(a); b = find(b); if (a == b) return false;
            if (sz[a] < sz[b]) { int t = a; a = b; b = t; }
            p[b] = a; sz[a] += sz[b]; --comps; return true;
        }
        boolean same(int a, int b) { return find(a) == find(b); }
        int size(int a) { return sz[find(a)]; }
    }

    // ============================================================
    // SPARSE TABLE -- O(1) RMQ (min), 0-indexed, static
    // ============================================================
    static final class SparseTable {
        int n; int[][] tb; int[] lg;
        void build(int[] a) {
            n = a.length; int LOG = 1; while ((1 << LOG) <= n) LOG++;
            tb = new int[LOG][]; lg = new int[n + 1];
            for (int i = 2; i <= n; ++i) lg[i] = lg[i / 2] + 1;
            tb[0] = a.clone();
            for (int j = 1; j < LOG; ++j) {
                tb[j] = new int[n];
                for (int i = 0; i + (1 << j) <= n; ++i)
                    tb[j][i] = Math.min(tb[j - 1][i], tb[j - 1][i + (1 << (j - 1))]);
            }
        }
        int query(int l, int r) { int k = lg[r - l + 1]; return Math.min(tb[k][l], tb[k][r - (1 << k) + 1]); }
    }

    // ============================================================
    // SEGMENT TREE -- point update, range sum, 1-indexed
    // ============================================================
    static final class SegTree {
        int n; long[] tree;
        SegTree(int n) { this.n = n; tree = new long[4 * n]; }
        private void upd(int v, int l, int r, int i, long val) {
            if (l == r) { tree[v] = val; return; }
            int m = (l + r) >>> 1;
            if (i <= m) upd(2 * v, l, m, i, val); else upd(2 * v + 1, m + 1, r, i, val);
            tree[v] = tree[2 * v] + tree[2 * v + 1];
        }
        private long qry(int v, int l, int r, int ql, int qr) {
            if (qr < l || r < ql) return 0;
            if (ql <= l && r <= qr) return tree[v];
            int m = (l + r) >>> 1;
            return qry(2 * v, l, m, ql, qr) + qry(2 * v + 1, m + 1, r, ql, qr);
        }
        void update(int i, long val) { upd(1, 1, n, i, val); }
        long query(int l, int r) { return qry(1, 1, n, l, r); }
    }

    // ============================================================
    // LAZY SEGMENT TREE -- range add update, range sum, 1-indexed
    // ============================================================
    static final class LazySegTree {
        int n; long[] tree, lazy;
        LazySegTree(int n) { this.n = n; tree = new long[4 * n]; lazy = new long[4 * n]; }
        private void push(int v, int l, int r) {
            if (lazy[v] == 0) return;
            int m = (l + r) >>> 1;
            tree[2 * v]     += lazy[v] * (m - l + 1); lazy[2 * v]     += lazy[v];
            tree[2 * v + 1] += lazy[v] * (r - m);     lazy[2 * v + 1] += lazy[v];
            lazy[v] = 0;
        }
        private void upd(int v, int l, int r, int ql, int qr, long val) {
            if (qr < l || r < ql) return;
            if (ql <= l && r <= qr) { tree[v] += val * (r - l + 1); lazy[v] += val; return; }
            push(v, l, r); int m = (l + r) >>> 1;
            upd(2 * v, l, m, ql, qr, val); upd(2 * v + 1, m + 1, r, ql, qr, val);
            tree[v] = tree[2 * v] + tree[2 * v + 1];
        }
        private long qry(int v, int l, int r, int ql, int qr) {
            if (qr < l || r < ql) return 0;
            if (ql <= l && r <= qr) return tree[v];
            push(v, l, r); int m = (l + r) >>> 1;
            return qry(2 * v, l, m, ql, qr) + qry(2 * v + 1, m + 1, r, ql, qr);
        }
        void update(int l, int r, long v) { upd(1, 1, n, l, r, v); }
        long query(int l, int r) { return qry(1, 1, n, l, r); }
    }

    // ============================================================
    // BINARY INDEXED TREE / FENWICK TREE (1-indexed)
    // ============================================================
    static final class BIT {
        int n; long[] bit;
        BIT(int n) { this.n = n; bit = new long[n + 1]; }
        void upd(int i, long d) { for (; i <= n; i += i & -i) bit[i] += d; }
        long qry(int i) { long s = 0; for (; i > 0; i -= i & -i) s += bit[i]; return s; }
        long qry(int l, int r) { return qry(r) - qry(l - 1); }
    }

    // ============================================================
    // LCA -- BINARY LIFTING, 1-indexed trees
    // ============================================================
    static final int LCA_LOG = 18;
    static int[][] lcaUp;   // [node][level]
    static int[] lcaDep;
    static List<Integer>[] lcaAdj;

    // Iterative DFS to avoid StackOverflow on deep trees.
    static void buildLCA(int root, int n) {
        lcaUp = new int[n + 1][LCA_LOG];
        lcaDep = new int[n + 1];
        int[] stack = new int[n + 1], parent = new int[n + 1];
        int sp = 0;
        stack[sp++] = root; parent[root] = root; lcaDep[root] = 0;
        boolean[] vis = new boolean[n + 1];
        while (sp > 0) {
            int u = stack[--sp];
            if (vis[u]) continue; vis[u] = true;
            lcaUp[u][0] = parent[u];
            for (int j = 1; j < LCA_LOG; ++j) lcaUp[u][j] = lcaUp[lcaUp[u][j - 1]][j - 1];
            for (int v : lcaAdj[u]) if (!vis[v]) { parent[v] = u; lcaDep[v] = lcaDep[u] + 1; stack[sp++] = v; }
        }
    }
    static int lca(int u, int v) {
        if (lcaDep[u] < lcaDep[v]) { int t = u; u = v; v = t; }
        int diff = lcaDep[u] - lcaDep[v];
        for (int j = 0; j < LCA_LOG; ++j) if (((diff >> j) & 1) == 1) u = lcaUp[u][j];
        if (u == v) return u;
        for (int j = LCA_LOG - 1; j >= 0; --j)
            if (lcaUp[u][j] != lcaUp[v][j]) { u = lcaUp[u][j]; v = lcaUp[v][j]; }
        return lcaUp[u][0];
    }
    static int treeDist(int u, int v) { return lcaDep[u] + lcaDep[v] - 2 * lcaDep[lca(u, v)]; }

    // ============================================================
    // GRAPH: DIJKSTRA, BFS, 0-1 BFS  (adjacency as int[]{to, weight})
    // ============================================================
    static long[] dijkstra(int src, List<int[]>[] adj, int N) {
        long[] dist = new long[N + 1];
        Arrays.fill(dist, LINF);
        // PriorityQueue of long packed as dist<<20 | node, or use long[]{dist, node}
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(p -> p[0]));
        dist[src] = 0; pq.add(new long[]{0, src});
        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            long d = top[0]; int u = (int) top[1];
            if (d > dist[u]) continue;
            for (int[] e : adj[u]) {
                int v = e[0]; long w = e[1];
                if (dist[u] + w < dist[v]) { dist[v] = dist[u] + w; pq.add(new long[]{dist[v], v}); }
            }
        }
        return dist;
    }
    static int[] bfsGraph(int src, List<Integer>[] adj, int N) {
        int[] dist = new int[N + 1]; Arrays.fill(dist, -1);
        ArrayDeque<Integer> q = new ArrayDeque<>();
        dist[src] = 0; q.add(src);
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v : adj[u]) if (dist[v] == -1) { dist[v] = dist[u] + 1; q.add(v); }
        }
        return dist;
    }
    static long[] bfs01(int src, List<int[]>[] adj, int N) {
        long[] dist = new long[N + 1]; Arrays.fill(dist, LINF);
        ArrayDeque<Integer> dq = new ArrayDeque<>();
        dist[src] = 0; dq.addFirst(src);
        while (!dq.isEmpty()) {
            int u = dq.pollFirst();
            for (int[] e : adj[u]) {
                int v = e[0]; long w = e[1];
                if (dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    if (w == 0) dq.addFirst(v); else dq.addLast(v);
                }
            }
        }
        return dist;
    }

    // ============================================================
    // MAX FLOW -- DINIC'S ALGORITHM
    // ============================================================
    static final class Dinic {
        int n;
        int[] eTo; long[] eCap; int[] eNext; int[] head; int ecnt = 0;
        int[] level, iter;
        Dinic(int n, int maxEdges) {
            this.n = n; head = new int[n]; Arrays.fill(head, -1);
            eTo = new int[2 * maxEdges]; eCap = new long[2 * maxEdges]; eNext = new int[2 * maxEdges];
            level = new int[n]; iter = new int[n];
        }
        void addEdge(int u, int v, long c) {
            eTo[ecnt] = v; eCap[ecnt] = c; eNext[ecnt] = head[u]; head[u] = ecnt++;
            eTo[ecnt] = u; eCap[ecnt] = 0; eNext[ecnt] = head[v]; head[v] = ecnt++;
        }
        boolean bfs(int s, int t) {
            Arrays.fill(level, -1);
            ArrayDeque<Integer> q = new ArrayDeque<>();
            level[s] = 0; q.add(s);
            while (!q.isEmpty()) {
                int v = q.poll();
                for (int e = head[v]; e != -1; e = eNext[e])
                    if (eCap[e] > 0 && level[eTo[e]] < 0) { level[eTo[e]] = level[v] + 1; q.add(eTo[e]); }
            }
            return level[t] >= 0;
        }
        long dfs(int v, int t, long f) {
            if (v == t) return f;
            for (; iter[v] != -1; iter[v] = eNext[iter[v]]) {
                int e = iter[v], u = eTo[e];
                if (eCap[e] > 0 && level[v] < level[u]) {
                    long d = dfs(u, t, Math.min(f, eCap[e]));
                    if (d > 0) { eCap[e] -= d; eCap[e ^ 1] += d; return d; }
                }
            }
            return 0;
        }
        long maxflow(int s, int t) {
            long flow = 0;
            while (bfs(s, t)) {
                for (int i = 0; i < n; i++) iter[i] = head[i];
                long d; while ((d = dfs(s, t, LINF)) > 0) flow += d;
            }
            return flow;
        }
    }

    // ============================================================
    // NTT -- polynomial multiplication mod 998244353
    // ============================================================
    static final class NTT {
        static final long MOD = 998_244_353, G = 3;
        static long pw(long a, long b) { long r = 1; a %= MOD; for (; b > 0; b >>= 1) { if ((b & 1) == 1) r = r * a % MOD; a = a * a % MOD; } return r; }
        static void ntt(long[] a, boolean inv) {
            int n = a.length;
            for (int i = 1, j = 0; i < n; ++i) {
                int b = n >> 1;
                for (; (j & b) != 0; b >>= 1) j ^= b;
                j ^= b;
                if (i < j) { long t = a[i]; a[i] = a[j]; a[j] = t; }
            }
            for (int len = 2; len <= n; len <<= 1) {
                long w = inv ? pw(G, MOD - 1 - (MOD - 1) / len) : pw(G, (MOD - 1) / len);
                for (int i = 0; i < n; i += len) {
                    long wn = 1;
                    for (int j = 0; j < len / 2; ++j) {
                        long u = a[i + j], v = a[i + j + len / 2] * wn % MOD;
                        a[i + j] = (u + v) % MOD; a[i + j + len / 2] = (u - v + MOD) % MOD; wn = wn * w % MOD;
                    }
                }
            }
            if (inv) { long ni = pw(n, MOD - 2); for (int i = 0; i < n; i++) a[i] = a[i] * ni % MOD; }
        }
        static long[] multiply(long[] a, long[] b) {
            int sz2 = a.length + b.length - 1, n = 1; while (n < sz2) n <<= 1;
            long[] fa = Arrays.copyOf(a, n), fb = Arrays.copyOf(b, n);
            ntt(fa, false); ntt(fb, false);
            for (int i = 0; i < n; ++i) fa[i] = fa[i] * fb[i] % MOD;
            ntt(fa, true);
            return Arrays.copyOf(fa, sz2);
        }
    }

    // ============================================================
    // STRING: KMP + Z-FUNCTION
    // ============================================================
    static int[] kmpFail(String p) {
        int m = p.length(); int[] f = new int[m];
        for (int i = 1; i < m; ++i) {
            int j = f[i - 1];
            while (j > 0 && p.charAt(i) != p.charAt(j)) j = f[j - 1];
            if (p.charAt(i) == p.charAt(j)) j++;
            f[i] = j;
        }
        return f;
    }
    static int[] zFunc(String s) {
        int n = s.length(); int[] z = new int[n]; z[0] = n;
        for (int i = 1, l = 0, r = 0; i < n; ++i) {
            if (i < r) z[i] = Math.min(r - i, z[i - l]);
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) z[i]++;
            if (i + z[i] > r) { l = i; r = i + z[i]; }
        }
        return z;
    }

    // ============================================================
    // GEOMETRY -- integer coordinates
    // ============================================================
    static final class P {
        long x, y; P(long x, long y) { this.x = x; this.y = y; }
        P sub(P o) { return new P(x - o.x, y - o.y); }
        P add(P o) { return new P(x + o.x, y + o.y); }
        long cross(P o) { return x * o.y - y * o.x; }
        long dot(P o) { return x * o.x + y * o.y; }
        long norm2() { return x * x + y * y; }
    }
    static final Comparator<P> P_BY_XY =
        Comparator.comparingLong((P p) -> p.x).thenComparingLong(p -> p.y);
    static long cross(P o, P a, P b) { return a.sub(o).cross(b.sub(o)); }
    static P[] convexHull(P[] in) {
        P[] pts = in.clone();
        Arrays.sort(pts, P_BY_XY);
        int u = 0;
        for (int i = 0; i < pts.length; ++i)
            if (u == 0 || pts[u - 1].x != pts[i].x || pts[u - 1].y != pts[i].y) pts[u++] = pts[i];
        pts = Arrays.copyOf(pts, u);
        int n = pts.length; if (n < 3) return pts;
        P[] h = new P[2 * n]; int k = 0;
        for (int i = 0; i < n; ++i) { while (k >= 2 && cross(h[k - 2], h[k - 1], pts[i]) <= 0) k--; h[k++] = pts[i]; }
        int lo = k + 1;
        for (int i = n - 2; i >= 0; --i) { while (k >= lo && cross(h[k - 2], h[k - 1], pts[i]) <= 0) k--; h[k++] = pts[i]; }
        return Arrays.copyOf(h, k - 1);
    }

    // ============================================================
    // UTILITIES
    // ============================================================
    // Coordinate compression (0-indexed ranks)
    static int[] compress(int[] a) {
        int[] s = a.clone(); Arrays.sort(s);
        int m = 0;
        for (int i = 0; i < s.length; ++i) if (i == 0 || s[i] != s[i - 1]) s[m++] = s[i];
        int[] r = new int[a.length];
        for (int i = 0; i < a.length; ++i) {
            // lower_bound on unique prefix s[0..m)
            int lo = 0, hi = m;
            while (lo < hi) { int mid = (lo + hi) >>> 1; if (s[mid] < a[i]) lo = mid + 1; else hi = mid; }
            r[i] = lo;
        }
        return r;
    }

    // Miller-Rabin (deterministic for n < 3.3e24 using these bases).
    // Uses Math.multiplyHigh-free 128-bit mulmod via Math.multiplyHigh? We use
    // BigInteger-free approach with __int128 emulation through Math.multiplyHigh.
    static long mulmod(long a, long b, long m) {
        // 128-bit safe multiply-mod (a,b < m <= ~9.2e18)
        return java.math.BigInteger.valueOf(a)
                .multiply(java.math.BigInteger.valueOf(b))
                .mod(java.math.BigInteger.valueOf(m)).longValue();
    }
    static long powmod(long a, long b, long m) {
        long r = 1; a %= m; for (; b > 0; b >>= 1) { if ((b & 1) == 1) r = mulmod(r, a, m); a = mulmod(a, a, m); } return r;
    }
    static boolean isPrime(long n) {
        if (n < 2) return false;
        for (long a : new long[]{2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37}) {
            if (n == a) return true;
            if (n % a == 0) return false;
            long d = n - 1; int r = 0; while (d % 2 == 0) { d /= 2; r++; }
            long x = powmod(a, d, n);
            if (x == 1 || x == n - 1) continue;
            boolean ok = false;
            for (int i = 0; i < r - 1 && !ok; ++i) { x = mulmod(x, x, n); if (x == n - 1) ok = true; }
            if (!ok) return false;
        }
        return true;
    }

    // ============================================================
    // DEBUG (no-ops unless LOCAL flag; toggle the constant below)
    // ============================================================
    static final boolean LOCAL = false;
    static void dbg(Object... xs) { if (LOCAL) System.err.println(Arrays.deepToString(xs)); }

    // ============================================================
    // SOLUTION
    // ============================================================
    static void solve() {
        int n = in.nextInt();

        // --- write your solution here ---

    }

    // ============================================================
    // MAIN -- fast I/O + T test case loop
    // ============================================================
    public static void main(String[] args) {
        in  = new FastReader(System.in);
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));

        int T = 1;
        // T = in.nextInt();   // uncomment for multiple test cases
        while (T-- > 0) solve();

        out.flush();
    }

    // Deep recursion (DFS, divide-and-conquer DP) can overflow the default
    // JVM stack. If you hit StackOverflowError, run solve() inside a thread
    // with a larger stack instead of calling it directly from main():
    //
    //   new Thread(null, () -> { solve(); out.flush(); }, "main", 1 << 26).start();
}
