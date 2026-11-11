import java.util.*;

public class P08EulerTour {
    //@tpl tour
    // Euler tour: tin[v] = v's position in order[] from walk() (8.9), a DFS
    // preorder. v's subtree is the block tin[v] .. tout[v] = tin[v]+size[v]-1.
    static int[] tin, tout;

    static void tour(List<Integer>[] g, int root) {
        walk(g, root);
        int n = g.length;
        tin = new int[n]; tout = new int[n];
        for (int i = 0; i < n; i++) tin[order[i]] = i;
        for (int v = 0; v < n; v++) tout[v] = tin[v] + size[v] - 1;
    }

    // u is an ancestor of v (or v itself) iff v's position is in u's block.
    static boolean isAncestor(int u, int v) {
        return tin[u] <= tin[v] && tin[v] <= tout[u];
    }
    //@end

    //@tpl fenwick
    // A Fenwick tree over tour positions: node v's value sits at tin[v].
    static long[] fw;                              // fw = new long[n + 1]

    static void add(int v, long delta) {          // value of node v += delta
        for (int i = tin[v] + 1; i < fw.length; i += i & -i) fw[i] += delta;
    }

    static long prefix(int i) {                    // sum of positions [0, i)
        long s = 0;
        for (; i > 0; i -= i & -i) s += fw[i];
        return s;
    }

    static long subtreeSum(int v) { return prefix(tout[v] + 1) - prefix(tin[v]); }
    //@end

    //@tpl removals
    // Minimum score after removals (n <= 1000). Cutting edge (v, parent[v])
    // splits off v's subtree, so a pair of cuts is a pair of non-root nodes.
    // If one is an ancestor of the other, the inner subtree leaves the outer.
    static int minimumScore(int[] nums, int[][] edges) {
        int n = nums.length;
        tour(adjacency(n, edges), 0);
        int[] x = new int[n];                      // XOR of each subtree
        for (int i = n - 1; i >= 0; i--) {
            int v = order[i];
            x[v] ^= nums[v];
            if (parent[v] >= 0) x[parent[v]] ^= x[v];
        }
        int best = Integer.MAX_VALUE;
        for (int a = 1; a < n; a++)
            for (int b = a + 1; b < n; b++) {
                int u = a, v = b, p, q, r;         // p, q, r: XOR of each part
                if (isAncestor(v, u)) { u = b; v = a; }
                if (isAncestor(u, v)) {            // v's subtree inside u's
                    p = x[v]; q = x[u] ^ x[v]; r = x[0] ^ x[u];
                } else {                           // two separate subtrees
                    p = x[u]; q = x[v]; r = x[0] ^ x[u] ^ x[v];
                }
                int hi = Math.max(p, Math.max(q, r));
                int lo = Math.min(p, Math.min(q, r));
                best = Math.min(best, hi - lo);
            }
        return best;
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

    static boolean naiveAncestor(int u, int v) {
        for (int x = v; x >= 0; x = parent[x]) if (x == u) return true;
        return false;
    }

    // components after removing two edges, by BFS
    static int bruteScore(int[] nums, int[][] edges) {
        int n = nums.length, best = Integer.MAX_VALUE;
        for (int i = 0; i < edges.length; i++)
            for (int j = i + 1; j < edges.length; j++) {
                List<Integer>[] g = new List[n];
                for (int v = 0; v < n; v++) g[v] = new ArrayList<>();
                for (int k = 0; k < edges.length; k++) {
                    if (k == i || k == j) continue;
                    g[edges[k][0]].add(edges[k][1]);
                    g[edges[k][1]].add(edges[k][0]);
                }
                int[] comp = new int[n];
                Arrays.fill(comp, -1);
                List<Integer> xs = new ArrayList<>();
                for (int s = 0; s < n; s++) {
                    if (comp[s] >= 0) continue;
                    int c = xs.size(), acc = 0;
                    Deque<Integer> q = new ArrayDeque<>();
                    q.add(s);
                    comp[s] = c;
                    while (!q.isEmpty()) {
                        int v = q.poll();
                        acc ^= nums[v];
                        for (int u : g[v]) if (comp[u] < 0) { comp[u] = c; q.add(u); }
                    }
                    xs.add(acc);
                }
                best = Math.min(best, Collections.max(xs) - Collections.min(xs));
            }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(813);
        for (int t = 0; t < 2000; t++) {
            int n = 1 + r.nextInt(30);
            int[][] e = shuffledTree(r, n);
            int root = r.nextInt(n);
            tour(adjacency(n, e), root);
            for (int u = 0; u < n; u++)
                for (int v = 0; v < n; v++) Check.eq(naiveAncestor(u, v), isAncestor(u, v), "ancestor " + u + " " + v);
            // Fenwick subtree sums against a brute sum over descendants, with updates
            int[] val = Check.ints(r, n, -1000000000, 1000000000);
            fw = new long[n + 1];
            for (int v = 0; v < n; v++) add(v, val[v]);
            for (int op = 0; op < 20; op++) {
                int v = r.nextInt(n);
                if (r.nextBoolean()) {
                    int d = r.nextInt(2001) - 1000;
                    add(v, d);
                    val[v] += d;
                } else {
                    long want = 0;
                    for (int u = 0; u < n; u++) if (naiveAncestor(v, u)) want += val[u];
                    Check.eq(want, subtreeSum(v), "subtree sum");
                }
            }
            if (n >= 3 && n <= 14) {
                int[] nums = Check.ints(r, n, 1, 60);
                Check.eq(bruteScore(nums, e), minimumScore(nums, e), "removals");
            }
        }
        Check.eq(9, minimumScore(new int[] {1, 5, 5, 4, 11}, new int[][] {{0, 1}, {1, 2}, {1, 3}, {3, 4}}), "LC 2322 sample 1");
        Check.eq(0, minimumScore(new int[] {5, 5, 2, 4, 4, 2}, new int[][] {{0, 1}, {1, 2}, {5, 2}, {4, 3}, {1, 3}}), "LC 2322 sample 2");
        // n = 100000 chain: the tour needs no recursion
        int big = 100000;
        int[][] chain = new int[big - 1][];
        for (int i = 0; i + 1 < big; i++) chain[i] = new int[] {i, i + 1};
        tour(adjacency(big, chain), 0);
        Check.isTrue(isAncestor(0, big - 1) && !isAncestor(big - 1, 0), "big chain ancestor");
        Check.done("P08EulerTour");
    }
}
