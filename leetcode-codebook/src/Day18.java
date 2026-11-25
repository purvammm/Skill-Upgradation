import java.util.*;

/*@core Trees without recursion: BFS order from a parent array or an edge list
note: Infosys trees arrive as `parent[]` or as edges, with n up to 10⁵ — deep recursion can overflow Java's stack. Get a BFS order once; walking it BACKWARDS visits children before parents (sizes, tree DP), walking it forwards visits parents first (depths, rerooting).
@*/
class Core18 {
    static int[] bfsOrderFromEdges(int n, int[][] edges, int root, int[] parentOut) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) { g.get(e[0]).add(e[1]); g.get(e[1]).add(e[0]); }
        int[] order = new int[n];
        Arrays.fill(parentOut, -1);
        boolean[] seen = new boolean[n];
        int head = 0, tail = 0;
        order[tail++] = root;
        seen[root] = true;
        while (head < tail) {
            int u = order[head++];
            for (int v : g.get(u)) if (!seen[v]) { seen[v] = true; parentOut[v] = u; order[tail++] = v; }
        }
        return order;
    }

    static int[] subtreeSizes(int[] order, int[] parent) {
        int[] size = new int[order.length];
        for (int i = order.length - 1; i >= 0; i--) {       // children before parents
            int v = order[i];
            size[v] += 1;
            if (parent[v] != -1) size[parent[v]] += size[v];
        }
        return size;
    }
}

/*@LC 543
tag: Post-order · bend at a node
cue: Longest path (in edges) between any two nodes of a binary tree.
hook: Each node returns its height; the path bending here is left + right.
time: O(n) time · O(h) space
trap: Return `1 + max(l, r)` to the parent, but record `l + r` in the global best.
@*/
class LC543 {
    private int best = 0;

    public int diameterOfBinaryTree(TreeNode root) {
        best = 0;
        height(root);
        return best;
    }

    private int height(TreeNode node) {                    // nodes on the longest downward path
        if (node == null) return 0;
        int l = height(node.left), r = height(node.right);
        best = Math.max(best, l + r);                      //* bend here
        return 1 + Math.max(l, r);                         //* one side goes up
    }
}

/*@var LC 687 · Longest Univalue Path
note: The same shape; an arm only continues into a child with the same value.
@*/
class LC543v1 {
    private int best;

    public int longestUnivaluePath(TreeNode root) {
        best = 0;
        arm(root);
        return best;
    }

    private int arm(TreeNode n) {
        if (n == null) return 0;
        int l = arm(n.left), r = arm(n.right);
        l = n.left != null && n.left.val == n.val ? l + 1 : 0;
        r = n.right != null && n.right.val == n.val ? r + 1 : 0;
        best = Math.max(best, l + r);
        return Math.max(l, r);
    }
}

/*@LC 236
tag: LCA by recursion
cue: Lowest common ancestor of p and q in a binary tree.
hook: Return what you found; if both sides found something, you are the answer.
time: O(n) time · O(h) space
trap: If root is p (or q), return it at once: the other one is below it or the answer is above.
var: LC 1650 LCA with parent pointers (premium) | Walk both up like "intersection of two lists".
@*/
class LC236 {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if (root == null || root == p || root == q) return root;          //*
        TreeNode l = lowestCommonAncestor(root.left, p, q);
        TreeNode r = lowestCommonAncestor(root.right, p, q);
        if (l != null && r != null) return root;                          //* split here
        return l != null ? l : r;                                         //* pass it up
    }
}

/*@var LC 235 · LCA of a Binary Search Tree
note: Values guide you: both smaller go left, both bigger go right, otherwise this node is the split.
@*/
class LC236v1 {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        TreeNode cur = root;
        while (true) {
            if (p.val < cur.val && q.val < cur.val) cur = cur.left;
            else if (p.val > cur.val && q.val > cur.val) cur = cur.right;
            else return cur;
        }
    }
}

/*@LC 124
tag: Post-order · drop negative branches
cue: Maximum path sum between any two nodes (values may be negative).
hook: Gain of a side is never below zero; the path bends here with both gains.
time: O(n) time · O(h) space
trap: Start `best` at `Integer.MIN_VALUE`: a tree of only negatives must return its largest value.
@*/
class LC124 {
    private int best;

    public int maxPathSum(TreeNode root) {
        best = Integer.MIN_VALUE;                          //*
        gain(root);
        return best;
    }

    private int gain(TreeNode node) {
        if (node == null) return 0;
        int l = Math.max(0, gain(node.left));              //* drop negatives
        int r = Math.max(0, gain(node.right));
        best = Math.max(best, node.val + l + r);           //* bend here
        return node.val + Math.max(l, r);                  //* one side goes up
    }
}

/*@LC 2246
tag: Parent array · children before parents
cue: Longest path where neighbouring nodes have different letters; the tree is a parent array.
hook: Keep the two longest child chains with a different letter; the path bends at the node.
time: O(n) time · O(n) space
trap: n is 10⁵: no recursion. Process leaves first (a counter of unfinished children per node).
@*/
class LC2246 {
    public int longestPath(int[] parent, String s) {
        int n = parent.length;
        int[] pending = new int[n];                        // children not processed yet
        for (int i = 1; i < n; i++) pending[parent[i]]++;
        int[] best1 = new int[n], best2 = new int[n];      // two longest usable child chains
        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (pending[i] == 0) q.offer(i);   //* leaves first
        int ans = 1;
        while (!q.isEmpty()) {
            int v = q.poll();
            int down = 1 + best1[v];
            ans = Math.max(ans, 1 + best1[v] + best2[v]);             //* bend at v
            int p = parent[v];
            if (p == -1) continue;
            if (s.charAt(p) != s.charAt(v)) {                         //* only different letters join
                if (down > best1[p]) { best2[p] = best1[p]; best1[p] = down; }
                else if (down > best2[p]) best2[p] = down;
            }
            if (--pending[p] == 0) q.offer(p);
        }
        return ans;
    }
}

/*@var Tree diameter from an edge list: BFS twice (LC 1245, premium)
note: BFS from any node; the farthest node is one end of a diameter. BFS again from it; the farthest distance is the diameter.
@*/
class LC2246v1 {
    public int treeDiameter(int[][] edges) {
        int n = edges.length + 1;
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) { g.get(e[0]).add(e[1]); g.get(e[1]).add(e[0]); }
        int[] first = farthest(g, 0);
        return farthest(g, first[0])[1];
    }

    private int[] farthest(List<List<Integer>> g, int src) {   // {node, distance}
        int[] dist = new int[g.size()];
        Arrays.fill(dist, -1);
        dist[src] = 0;
        Deque<Integer> q = new ArrayDeque<>();
        q.offer(src);
        int last = src;
        while (!q.isEmpty()) {
            int u = q.poll();
            last = u;
            for (int v : g.get(u)) if (dist[v] == -1) { dist[v] = dist[u] + 1; q.offer(v); }
        }
        return new int[]{last, dist[last]};
    }
}

/*@LC 102
tag: BFS by levels
cue: Values of a binary tree level by level.
hook: Freeze `q.size()`: that many nodes are this level.
time: O(n) time · O(n) space
trap: Read `q.size()` ONCE before the inner loop; it changes as you push children.
var: LC 103 Zigzag Level Order | Reverse every second level (or add to the front).
@*/
class LC102 {
    public List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> res = new ArrayList<>();
        if (root == null) return res;
        Deque<TreeNode> q = new ArrayDeque<>();
        q.offer(root);
        while (!q.isEmpty()) {
            List<Integer> level = new ArrayList<>();
            for (int size = q.size(); size > 0; size--) {          //* one level
                TreeNode node = q.poll();
                level.add(node.val);
                if (node.left != null) q.offer(node.left);
                if (node.right != null) q.offer(node.right);
            }
            res.add(level);
        }
        return res;
    }
}

/*@var LC 199 · Binary Tree Right Side View
note: The same level loop; keep only the last node of each level.
@*/
class LC102v1 {
    public List<Integer> rightSideView(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        if (root == null) return res;
        Deque<TreeNode> q = new ArrayDeque<>();
        q.offer(root);
        while (!q.isEmpty()) {
            for (int size = q.size(); size > 0; size--) {
                TreeNode node = q.poll();
                if (size == 1) res.add(node.val);
                if (node.left != null) q.offer(node.left);
                if (node.right != null) q.offer(node.right);
            }
        }
        return res;
    }
}

/*@LC 1483
class: TreeAncestor
tag: Binary lifting
cue: Many queries "k-th ancestor of node".
hook: Store the 1st, 2nd, 4th, 8th … ancestor; jump by the set bits of k.
time: O(n log n) build · O(log n) per query
trap: Use −1 for "no ancestor" and stop as soon as the node becomes −1.
@*/
class LC1483 {
    private static final int LOG = 16;                     // 2^16 > 5·10^4
    private final int[][] up;                              // up[j][v] = 2^j-th ancestor

    public LC1483(int n, int[] parent) {
        up = new int[LOG][];
        up[0] = parent.clone();
        for (int j = 1; j < LOG; j++) {
            up[j] = new int[n];
            for (int v = 0; v < n; v++)
                up[j][v] = up[j - 1][v] == -1 ? -1 : up[j - 1][up[j - 1][v]];   //* 2^j = 2^(j-1) twice
        }
    }

    public int getKthAncestor(int node, int k) {
        for (int j = 0; j < LOG && node != -1; j++)
            if (((k >> j) & 1) == 1) node = up[j][node];                       //* jump by set bits
        return node;
    }
}

/*@LC 2477
tag: Subtree sizes
cue: Every city sends one representative to the capital; cars have `seats`; minimise litres of fuel.
hook: The edge above a subtree of size s carries s people, so it needs ceil(s / seats) cars.
time: O(n) time · O(n) space
trap: The answer can exceed 2³¹: return a `long`.
@*/
class LC2477 {
    public long minimumFuelCost(int[][] roads, int seats) {
        int n = roads.length + 1;
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] r : roads) { g.get(r[0]).add(r[1]); g.get(r[1]).add(r[0]); }
        int[] order = new int[n], parent = new int[n];
        Arrays.fill(parent, -1);
        boolean[] seen = new boolean[n];
        int head = 0, tail = 0;
        order[tail++] = 0;
        seen[0] = true;
        while (head < tail) {
            int u = order[head++];
            for (int v : g.get(u)) if (!seen[v]) { seen[v] = true; parent[v] = u; order[tail++] = v; }
        }
        long[] size = new long[n];
        long fuel = 0;
        for (int i = n - 1; i > 0; i--) {                          //* children first, skip the root
            int v = order[i];
            size[v] += 1;
            fuel += (size[v] + seats - 1) / seats;                 //* cars on the edge above v
            size[parent[v]] += size[v];
        }
        return fuel;
    }
}

/*@LC 2846
tag: LCA + counts on root paths
cue: For each query path, fewest edge weight changes to make all weights equal (weights 1..26).
hook: Count each weight from the root; path count = cnt[a] + cnt[b] − 2·cnt[lca]. Keep the most common, change the rest.
time: O((n + q) · (log n + 26)) time · O(26 · n) space
trap: Path length is `depth[a] + depth[b] − 2·depth[lca]`; the answer is that minus the most frequent weight.
@*/
class LC2846 {
    private static final int LOG = 15;                             // 2^14 > 10^4
    private int[][] up;
    private int[] depth;

    public int[] minOperationsQueries(int n, int[][] edges, int[][] queries) {
        List<List<int[]>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) { g.get(e[0]).add(new int[]{e[1], e[2]}); g.get(e[1]).add(new int[]{e[0], e[2]}); }
        up = new int[LOG][n];
        depth = new int[n];
        int[][] cnt = new int[n][27];                              // cnt[v][w] = edges of weight w on root..v
        boolean[] seen = new boolean[n];
        Deque<Integer> q = new ArrayDeque<>();
        q.offer(0);
        seen[0] = true;
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int[] e : g.get(u)) {
                int v = e[0];
                if (seen[v]) continue;
                seen[v] = true;
                up[0][v] = u;
                depth[v] = depth[u] + 1;
                cnt[v] = cnt[u].clone();
                cnt[v][e[1]]++;                                    //*
                q.offer(v);
            }
        }
        for (int j = 1; j < LOG; j++)
            for (int v = 0; v < n; v++) up[j][v] = up[j - 1][up[j - 1][v]];   // the root points to itself
        int[] ans = new int[queries.length];
        for (int i = 0; i < queries.length; i++) {
            int a = queries[i][0], b = queries[i][1], l = lca(a, b), most = 0;
            for (int w = 1; w <= 26; w++) most = Math.max(most, cnt[a][w] + cnt[b][w] - 2 * cnt[l][w]);   //*
            ans[i] = depth[a] + depth[b] - 2 * depth[l] - most;                                         //*
        }
        return ans;
    }

    private int lca(int a, int b) {
        if (depth[a] < depth[b]) { int t = a; a = b; b = t; }
        int diff = depth[a] - depth[b];
        for (int j = 0; j < LOG; j++) if (((diff >> j) & 1) == 1) a = up[j][a];   //* same depth
        if (a == b) return a;
        for (int j = LOG - 1; j >= 0; j--)
            if (up[j][a] != up[j][b]) { a = up[j][a]; b = up[j][b]; }             //* climb together
        return up[0][a];
    }
}

public class Day18 {
    public static void main(String[] args) {
        int[] par = new int[5];
        int[] order = Core18.bfsOrderFromEdges(5, T.grid("[[0,1],[0,2],[1,3],[1,4]]"), 0, par);
        T.eq("core order", order[0], 0);
        T.eq("core sizes", Core18.subtreeSizes(order, par), T.ints("[5,3,1,1,1]"));

        T.eq("543a", new LC543().diameterOfBinaryTree(T.tree(1, 2, 3, 4, 5)), 3);
        T.eq("543b", new LC543().diameterOfBinaryTree(T.tree(1, 2)), 1);
        T.eq("687a", new LC543v1().longestUnivaluePath(T.tree(5, 4, 5, 1, 1, null, 5)), 2);
        T.eq("687b", new LC543v1().longestUnivaluePath(T.tree(1, 4, 5, 4, 4, null, 5)), 2);

        TreeNode r = T.tree(3, 5, 1, 6, 2, 0, 8, null, null, 7, 4);
        T.eq("236a", new LC236().lowestCommonAncestor(r, T.find(r, 5), T.find(r, 1)).val, 3);
        T.eq("236b", new LC236().lowestCommonAncestor(r, T.find(r, 5), T.find(r, 4)).val, 5);
        TreeNode bst = T.tree(6, 2, 8, 0, 4, 7, 9, null, null, 3, 5);
        T.eq("235a", new LC236v1().lowestCommonAncestor(bst, T.find(bst, 2), T.find(bst, 8)).val, 6);
        T.eq("235b", new LC236v1().lowestCommonAncestor(bst, T.find(bst, 2), T.find(bst, 4)).val, 2);

        T.eq("124a", new LC124().maxPathSum(T.tree(1, 2, 3)), 6);
        T.eq("124b", new LC124().maxPathSum(T.tree(-10, 9, 20, null, null, 15, 7)), 42);
        T.eq("124c", new LC124().maxPathSum(T.tree(-3)), -3);

        T.eq("2246a", new LC2246().longestPath(T.ints("[-1,0,0,1,1,2]"), "abacbe"), 3);
        T.eq("2246b", new LC2246().longestPath(T.ints("[-1,0,0,0]"), "aabc"), 3);
        for (int t = 0; t < 150; t++) {
            int n = T.rnd(1, 9);
            int[] p = new int[n];
            p[0] = -1;
            for (int i = 1; i < n; i++) p[i] = T.rnd(0, i - 1);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) sb.append((char) ('a' + T.rnd(0, 2)));
            String s = sb.toString();
            List<List<Integer>> g = new ArrayList<>();
            for (int i = 0; i < n; i++) g.add(new ArrayList<>());
            for (int i = 1; i < n; i++) if (s.charAt(i) != s.charAt(p[i])) { g.get(i).add(p[i]); g.get(p[i]).add(i); }
            int best = 1;
            for (int a = 0; a < n; a++) {
                int[] d = new int[n]; Arrays.fill(d, -1); d[a] = 1;
                Deque<Integer> q = new ArrayDeque<>(); q.add(a);
                while (!q.isEmpty()) { int u = q.poll(); best = Math.max(best, d[u]); for (int v : g.get(u)) if (d[v] == -1) { d[v] = d[u] + 1; q.add(v); } }
            }
            T.eq("2246 rand", new LC2246().longestPath(p, s), best);
            int[][] e = T.randTreeEdges(n);
            if (n >= 2) {
                List<List<Integer>> h = new ArrayList<>();
                for (int i = 0; i < n; i++) h.add(new ArrayList<>());
                for (int[] x : e) { h.get(x[0]).add(x[1]); h.get(x[1]).add(x[0]); }
                int dia = 0;
                for (int a = 0; a < n; a++) {
                    int[] d = new int[n]; Arrays.fill(d, -1); d[a] = 0;
                    Deque<Integer> q = new ArrayDeque<>(); q.add(a);
                    while (!q.isEmpty()) { int u = q.poll(); dia = Math.max(dia, d[u]); for (int v : h.get(u)) if (d[v] == -1) { d[v] = d[u] + 1; q.add(v); } }
                }
                T.eq("1245 rand", new LC2246v1().treeDiameter(e), dia);
            }
        }

        T.eq("102a", new LC102().levelOrder(T.tree(3, 9, 20, null, null, 15, 7)).toString(), "[[3], [9, 20], [15, 7]]");
        T.eq("102b", new LC102().levelOrder(null).toString(), "[]");
        T.eq("199a", new LC102v1().rightSideView(T.tree(1, 2, 3, null, 5, null, 4)), List.of(1, 3, 4));
        T.eq("199b", new LC102v1().rightSideView(T.tree(1, 2, 3, 4, null, null, null, 5)), List.of(1, 3, 4, 5));

        LC1483 ta = new LC1483(7, T.ints("[-1,0,0,1,1,2,2]"));
        T.eq("1483", new int[]{ta.getKthAncestor(3, 1), ta.getKthAncestor(5, 2), ta.getKthAncestor(6, 3)}, T.ints("[1,0,-1]"));

        T.eq("2477a", new LC2477().minimumFuelCost(T.grid("[[0,1],[0,2],[0,3]]"), 5), 3L);
        T.eq("2477b", new LC2477().minimumFuelCost(T.grid("[[3,1],[3,2],[1,0],[0,4],[0,5],[4,6]]"), 2), 7L);
        T.eq("2477c", new LC2477().minimumFuelCost(new int[0][], 1), 0L);

        T.eq("2846a", new LC2846().minOperationsQueries(7, T.grid("[[0,1,1],[1,2,1],[2,3,1],[3,4,2],[4,5,2],[5,6,2]]"), T.grid("[[0,3],[3,6],[2,6],[0,6]]")), T.ints("[0,0,1,3]"));
        T.eq("2846b", new LC2846().minOperationsQueries(8, T.grid("[[1,2,6],[1,3,4],[2,4,6],[2,5,3],[3,6,6],[3,0,8],[7,0,2]]"), T.grid("[[4,6],[0,4],[6,5],[7,4]]")), T.ints("[1,2,2,3]"));
        for (int t = 0; t < 100; t++) {
            int n = T.rnd(1, 12);
            int[][] base = T.randTreeEdges(n), e = new int[n - 1][];
            for (int i = 0; i < n - 1; i++) e[i] = new int[]{base[i][0], base[i][1], T.rnd(1, 3)};
            int[][] qs = new int[5][];
            int[] want = new int[5];
            List<List<int[]>> g = new ArrayList<>();
            for (int i = 0; i < n; i++) g.add(new ArrayList<>());
            for (int[] x : e) { g.get(x[0]).add(new int[]{x[1], x[2]}); g.get(x[1]).add(new int[]{x[0], x[2]}); }
            for (int i = 0; i < 5; i++) {
                int a = T.rnd(0, n - 1), b = T.rnd(0, n - 1);
                qs[i] = new int[]{a, b};
                int[] from = new int[n], w = new int[n];
                Arrays.fill(from, -2);
                from[a] = -1;
                Deque<Integer> q = new ArrayDeque<>(); q.add(a);
                while (!q.isEmpty()) { int u = q.poll(); for (int[] x : g.get(u)) if (from[x[0]] == -2) { from[x[0]] = u; w[x[0]] = x[1]; q.add(x[0]); } }
                int[] c = new int[27]; int len = 0;
                for (int v = b; v != a; v = from[v]) { c[w[v]]++; len++; }
                int most = 0;
                for (int x : c) most = Math.max(most, x);
                want[i] = len - most;
            }
            T.eq("2846 rand", new LC2846().minOperationsQueries(n, e, qs), want);
        }
        T.summary("Day18");
    }
}
