import java.util.*;

public class P08TreeDp {
    //@tpl rob
    // House robber III: each node returns two answers for its subtree,
    // {best if t is robbed, best if t is not robbed}.
    static int rob(TreeNode root) {
        int[] r = robPair(root);
        return Math.max(r[0], r[1]);
    }

    static int[] robPair(TreeNode t) {
        if (t == null) return new int[] {0, 0};
        int[] l = robPair(t.left), r = robPair(t.right);
        int take = t.val + l[1] + r[1];            // children must be skipped
        int skip = Math.max(l[0], l[1]) + Math.max(r[0], r[1]);   // free choice
        return new int[] {take, skip};
    }
    //@end

    //@tpl cameras
    // Binary tree cameras. Three states for t, each with everything below t
    // covered: {camera at t, covered by a child's camera, not covered yet}.
    static final int INF = 1 << 20;

    static int minCameraCover(TreeNode root) {
        int[] s = cam(root);
        return Math.min(s[0], s[1]);               // the root has no parent
    }

    static int[] cam(TreeNode t) {
        if (t == null) return new int[] {INF, 0, 0};    // null: covered, no camera
        int[] l = cam(t.left), r = cam(t.right);
        int here = 1 + Math.min(l[0], Math.min(l[1], l[2]))   // t covers both
                     + Math.min(r[0], Math.min(r[1], r[2]));
        int byChild = Math.min(l[0] + Math.min(r[0], r[1]),  // one child has one
                               r[0] + Math.min(l[0], l[1]));
        int open = l[1] + r[1];                    // children covered, no camera
        return new int[] {here, byChild, open};
    }
    //@end

    //@tpl mis
    // Maximum weight independent set on a general tree: the same take/skip
    // pair, filled children-first by walking order[] (from walk(), 8.9) back.
    static long maxIndependentSet(int n, int[][] edges, int[] w) {
        walk(adjacency(n, edges), 0);
        long[] take = new long[n], skip = new long[n];
        for (int i = n - 1; i >= 0; i--) {
            int v = order[i], p = parent[v];
            take[v] += w[v];                       // children already added
            if (p >= 0) {
                take[p] += skip[v];
                skip[p] += Math.max(take[v], skip[v]);
            }
        }
        return Math.max(take[0], skip[0]);
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
    static void collect(TreeNode t, TreeNode par, List<TreeNode> nodes, List<Integer> parIdx, Map<TreeNode, Integer> id) {
        if (t == null) return;
        id.put(t, nodes.size());
        nodes.add(t);
        parIdx.add(par == null ? -1 : id.get(par));
        collect(t.left, t, nodes, parIdx, id);
        collect(t.right, t, nodes, parIdx, id);
    }

    static int bruteRob(TreeNode root) {
        List<TreeNode> nodes = new ArrayList<>();
        List<Integer> par = new ArrayList<>();
        collect(root, null, nodes, par, new IdentityHashMap<>());
        int n = nodes.size(), best = 0;
        for (int m = 0; m < (1 << n); m++) {
            boolean ok = true;
            int s = 0;
            for (int v = 0; v < n && ok; v++) {
                if ((m >> v & 1) == 0) continue;
                if (par.get(v) >= 0 && (m >> par.get(v) & 1) == 1) ok = false;
                s += nodes.get(v).val;
            }
            if (ok) best = Math.max(best, s);
        }
        return best;
    }

    static int bruteCameras(TreeNode root) {
        List<TreeNode> nodes = new ArrayList<>();
        List<Integer> par = new ArrayList<>();
        collect(root, null, nodes, par, new IdentityHashMap<>());
        int n = nodes.size(), best = Integer.MAX_VALUE;
        for (int m = 0; m < (1 << n); m++) {
            boolean[] cov = new boolean[n];
            for (int v = 0; v < n; v++) {
                if ((m >> v & 1) == 0) continue;
                cov[v] = true;
                if (par.get(v) >= 0) cov[par.get(v)] = true;
                for (int u = 0; u < n; u++) if (par.get(u) == v) cov[u] = true;
            }
            boolean all = true;
            for (boolean c : cov) all &= c;
            if (all) best = Math.min(best, Integer.bitCount(m));
        }
        return best;
    }

    static long bruteMis(int n, int[][] edges, int[] w) {
        long best = 0;
        for (int m = 0; m < (1 << n); m++) {
            boolean ok = true;
            for (int[] e : edges) if ((m >> e[0] & 1) == 1 && (m >> e[1] & 1) == 1) { ok = false; break; }
            if (!ok) continue;
            long s = 0;
            for (int v = 0; v < n; v++) if ((m >> v & 1) == 1) s += w[v];
            best = Math.max(best, s);
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(811);
        for (int t = 0; t < 2500; t++) {
            int n = r.nextInt(13);
            TreeNode root = Nodes.randomTree(r, n, 0, 20);
            Check.eq(bruteRob(root), rob(root), "rob " + Nodes.shape(root));
            if (n >= 1) Check.eq(bruteCameras(root), minCameraCover(root), "cameras " + Nodes.shape(root));
            int m = 1 + r.nextInt(14);
            int[][] e = Check.randomTree(r, m);
            for (int[] x : e) if (r.nextBoolean()) { int tmp = x[0]; x[0] = x[1]; x[1] = tmp; }
            int[] w = Check.ints(r, m, 0, 1000000000);
            Check.eq(bruteMis(m, e, w), maxIndependentSet(m, e, w), "mis");
        }
        Check.eq(7, rob(Nodes.tree(3, 2, 3, null, 3, null, 1)), "LC 337 sample 1");
        Check.eq(9, rob(Nodes.tree(3, 4, 5, 1, 3, null, 1)), "LC 337 sample 2");
        Check.eq(1, minCameraCover(Nodes.tree(0, 0, null, 0, 0)), "LC 968 sample 1");
        Check.eq(2, minCameraCover(Nodes.tree(0, 0, null, 0, null, 0, null, null, 0)), "LC 968 sample 2");
        Check.eq(1, minCameraCover(Nodes.tree(0)), "single node");
        Check.done("P08TreeDp");
    }
}
