import java.util.*;

public class P08BinaryLifting {
    //@tpl lift
    // up[j][v] = the 2^j-th ancestor of v (-1 if none) = up[j-1][up[j-1][v]].
    static int LOG;
    static int[][] up;

    static void buildLifting(int[] parent) {       // parent[root] = -1
        int n = parent.length;
        LOG = 1;
        while ((1 << LOG) < n) LOG++;              // 2^LOG >= n > any depth
        up = new int[LOG][n];
        up[0] = parent.clone();
        for (int j = 1; j < LOG; j++)              // row j needs row j-1 only
            for (int v = 0; v < n; v++) {
                int mid = up[j - 1][v];
                up[j][v] = mid < 0 ? -1 : up[j - 1][mid];
            }
    }

    // k-th ancestor: one jump per set bit of k, so at most LOG jumps.
    static int kthAncestor(int v, int k) {
        if (k >= up[0].length) return -1;          // more than any depth
        for (int j = 0; k > 0 && v >= 0; j++, k >>= 1)
            if ((k & 1) == 1) v = up[j][v];
        return v;
    }
    //@end

    //@tpl lca
    // LCA (depth[] from walk(), 8.9): same depth, then jump while apart.
    static int lca(int a, int b) {
        if (depth[a] < depth[b]) { int t = a; a = b; b = t; }
        a = kthAncestor(a, depth[a] - depth[b]);
        if (a == b) return a;                      // b was an ancestor of a
        for (int j = LOG - 1; j >= 0; j--)
            if (up[j][a] != up[j][b]) {            // both still below the LCA
                a = up[j][a];
                b = up[j][b];
            }
        return up[0][a];
    }
    //@end

    //@tpl equilibrium
    // Edge weight equilibrium queries (weights 1..26): make every edge on the
    // path a-b equal with the fewest changes = path length - most common weight.
    // cnt[v][w] counts weight w on the root-to-v path; the path a-b has
    // cnt[a][w] + cnt[b][w] - 2 * cnt[lca][w] edges of weight w.
    static int[] minOperationsQueries(int n, int[][] edges, int[][] queries) {
        walk(adjacency(n, edges), 0);              // adjacency ignores e[2]
        buildLifting(parent);
        int[] w = new int[n];                      // weight of edge v-parent[v]
        for (int[] e : edges) w[parent[e[0]] == e[1] ? e[0] : e[1]] = e[2];
        int[][] cnt = new int[n][];
        cnt[0] = new int[27];
        for (int i = 1; i < n; i++) {              // parents before children
            int v = order[i];
            cnt[v] = cnt[parent[v]].clone();
            cnt[v][w[v]]++;
        }
        int[] ans = new int[queries.length];
        for (int i = 0; i < queries.length; i++) {
            int a = queries[i][0], b = queries[i][1], c = lca(a, b), most = 0;
            for (int x = 1; x <= 26; x++)
                most = Math.max(most, cnt[a][x] + cnt[b][x] - 2 * cnt[c][x]);
            ans[i] = depth[a] + depth[b] - 2 * depth[c] - most;
        }
        return ans;
    }
    //@end

    // ---------------- walk() from P08TreeEdges (8.9), not printed again ----------------
    static int[] parent, depth, order, size;

    static List<Integer>[] adjacency(int n, int[][] edges) {
        List<Integer>[] g = new List[n];
        for (int v = 0; v < n; v++) g[v] = new ArrayList<>();
        for (int[] e : edges) { g[e[0]].add(e[1]); g[e[1]].add(e[0]); }
        return g;
    }

    static void walk(List<Integer>[] g, int root) {
        int n = g.length, k = 0;
        parent = new int[n]; depth = new int[n]; order = new int[n];
        Deque<Integer> st = new ArrayDeque<>();
        st.push(root);
        parent[root] = -1;
        while (!st.isEmpty()) {
            int v = st.pop();
            order[k++] = v;
            for (int u : g[v]) {
                if (u == parent[v]) continue;      // the edge back up
                parent[u] = v;
                depth[u] = depth[v] + 1;
                st.push(u);
            }
        }
        size = new int[n];
        for (int i = n - 1; i >= 0; i--) {         // children before parents
            int v = order[i];
            size[v] += 1;
            if (parent[v] >= 0) size[parent[v]] += size[v];
        }
    }

    // ---------------- tests (not printed) ----------------
    static int[][] shuffledTree(Random r, int n) {
        int[][] e = Check.randomTree(r, n);
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;
        for (int i = n - 1; i > 0; i--) { int j = r.nextInt(i + 1); int t = perm[i]; perm[i] = perm[j]; perm[j] = t; }
        for (int[] x : e) {
            x[0] = perm[x[0]];
            x[1] = perm[x[1]];
            if (r.nextBoolean()) { int t = x[0]; x[0] = x[1]; x[1] = t; }
        }
        return e;
    }

    static int naiveKth(int[] par, int v, int k) {
        while (k-- > 0 && v >= 0) v = par[v];
        return v;
    }

    static int naiveLca(int[] par, int a, int b) {
        Set<Integer> anc = new HashSet<>();
        for (int x = a; x >= 0; x = par[x]) anc.add(x);
        for (int y = b; ; y = par[y]) if (anc.contains(y)) return y;
    }

    public static void main(String[] args) {
        Random r = new Random(810);
        for (int t = 0; t < 2000; t++) {
            int n = 1 + r.nextInt(40);
            if (t % 50 == 0) n = 200 + r.nextInt(300);
            int[][] e = shuffledTree(r, n);
            int root = r.nextInt(n);
            walk(adjacency(n, e), root);
            int[] par = parent.clone();
            buildLifting(par);
            for (int q = 0; q < 30; q++) {
                int v = r.nextInt(n), k = r.nextInt(n + 3);
                Check.eq(naiveKth(par, v, k), kthAncestor(v, k), "kth " + v + " " + k);
                int a = r.nextInt(n), b = r.nextInt(n);
                Check.eq(naiveLca(par, a, b), lca(a, b), "lca " + a + " " + b);
            }
            // equilibrium queries: weighted edges, brute path walk
            int[][] we = new int[n - 1][];
            int maxW = 1 + r.nextInt(26);
            for (int i = 0; i < n - 1; i++) we[i] = new int[] {e[i][0], e[i][1], 1 + r.nextInt(maxW)};
            int[][] qs = new int[10][];
            for (int i = 0; i < 10; i++) qs[i] = new int[] {r.nextInt(n), r.nextInt(n)};
            int[] got = minOperationsQueries(n, we, qs);
            Map<Long, Integer> wt = new HashMap<>();
            for (int[] x : we) { wt.put((long) x[0] * n + x[1], x[2]); wt.put((long) x[1] * n + x[0], x[2]); }
            for (int i = 0; i < 10; i++) {
                int a = qs[i][0], b = qs[i][1], c = naiveLca(parent, a, b);
                int[] count = new int[27];
                int len = 0;
                for (int x = a; x != c; x = parent[x]) { count[wt.get((long) x * n + parent[x])]++; len++; }
                for (int x = b; x != c; x = parent[x]) { count[wt.get((long) x * n + parent[x])]++; len++; }
                int most = 0;
                for (int c2 : count) most = Math.max(most, c2);
                Check.eq(len - most, got[i], "equilibrium");
            }
        }
        // LC 1483 sample
        buildLifting(new int[] {-1, 0, 0, 1, 1, 2, 2});
        Check.eq(1, kthAncestor(3, 1), "LC 1483 sample q1");
        Check.eq(0, kthAncestor(5, 2), "LC 1483 sample q2");
        Check.eq(-1, kthAncestor(6, 3), "LC 1483 sample q3");
        Check.eq(new int[] {0, 0, 1, 3}, minOperationsQueries(7,
                 new int[][] {{0, 1, 1}, {1, 2, 1}, {2, 3, 1}, {3, 4, 2}, {4, 5, 2}, {5, 6, 2}},
                 new int[][] {{0, 3}, {3, 6}, {2, 6}, {0, 6}}), "LC 2846 sample 1");
        Check.eq(new int[] {1, 2, 2, 3}, minOperationsQueries(8,
                 new int[][] {{1, 2, 6}, {1, 3, 4}, {2, 4, 6}, {2, 5, 3}, {3, 6, 6}, {3, 0, 8}, {7, 0, 2}},
                 new int[][] {{4, 6}, {0, 4}, {6, 5}, {7, 4}}), "LC 2846 sample 2");
        // big chain: n = 100000, deep queries
        int big = 100000;
        int[] chainPar = new int[big];
        for (int i = 0; i < big; i++) chainPar[i] = i - 1;
        buildLifting(chainPar);
        Check.eq(0, kthAncestor(big - 1, big - 1), "chain kth to root");
        Check.eq(-1, kthAncestor(big - 1, big), "chain kth past root");
        Check.eq(12345, kthAncestor(99999, 99999 - 12345), "chain kth middle");
        Check.done("P08BinaryLifting");
    }
}
