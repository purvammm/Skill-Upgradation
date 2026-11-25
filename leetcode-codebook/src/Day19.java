import java.util.*;

/*@core Tree DP and rerooting, both without recursion
note: Tree DP: walk the BFS order backwards and fold each child into its parent (`dp[v][state]`). Rerooting: first compute the answer for root 0; then walk the BFS order forwards and move the root across each edge, fixing only what that edge changes.
@*/
class Core19 {
    static long maxIndependentSet(int n, int[][] edges, int[] w) {   // no two chosen nodes adjacent
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) { g.get(e[0]).add(e[1]); g.get(e[1]).add(e[0]); }
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
        long[] take = new long[n], skip = new long[n];
        for (int i = n - 1; i >= 0; i--) {                 // children first
            int v = order[i];
            take[v] += w[v];
            int p = parent[v];
            if (p == -1) continue;
            take[p] += skip[v];                            // p taken: v must be skipped
            skip[p] += Math.max(take[v], skip[v]);         // p skipped: v is free
        }
        return Math.max(take[0], skip[0]);
    }
}

/*@LC 337
tag: Tree DP · two states
cue: Rob nodes of a binary tree, never two directly linked.
hook: Each node returns a pair: best if I am robbed, best if I am not.
time: O(n) time · O(h) space
trap: If this node is NOT robbed, each child picks its own better state (not necessarily "robbed").
@*/
class LC337 {
    public int rob(TreeNode root) {
        int[] r = best(root);
        return Math.max(r[0], r[1]);
    }

    private int[] best(TreeNode node) {                    // {robbed, not robbed}
        if (node == null) return new int[]{0, 0};
        int[] l = best(node.left), r = best(node.right);
        int robbed = node.val + l[1] + r[1];               //* children must rest
        int skipped = Math.max(l[0], l[1]) + Math.max(r[0], r[1]);   //* children choose
        return new int[]{robbed, skipped};
    }
}

/*@var LC 968 · Binary Tree Cameras (three states)
note: Return 0 = not covered, 1 = covered, 2 = has a camera. A child that is not covered forces a camera here. Check the root at the end.
@*/
class LC337v1 {
    private int cams;

    public int minCameraCover(TreeNode root) {
        cams = 0;
        return state(root) == 0 ? cams + 1 : cams;
    }

    private int state(TreeNode n) {
        if (n == null) return 1;
        int l = state(n.left), r = state(n.right);
        if (l == 0 || r == 0) { cams++; return 2; }
        if (l == 2 || r == 2) return 1;
        return 0;
    }
}

/*@LC 1519
tag: Subtree counts · merge children
cue: For each node, how many nodes in its subtree share its label?
hook: Every node keeps 26 counters; children add theirs to the parent.
time: O(26 · n) time · O(26 · n) space
trap: The edges are undirected: root the tree at 0 with a BFS first.
@*/
class LC1519 {
    public int[] countSubTrees(int n, int[][] edges, String labels) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) { g.get(e[0]).add(e[1]); g.get(e[1]).add(e[0]); }
        int[] order = new int[n], parent = new int[n];
        Arrays.fill(parent, -1);
        boolean[] seen = new boolean[n];
        int head = 0, tail = 0;
        order[tail++] = 0;
        seen[0] = true;
        while (head < tail) {                                            //* root it at 0
            int u = order[head++];
            for (int v : g.get(u)) if (!seen[v]) { seen[v] = true; parent[v] = u; order[tail++] = v; }
        }
        int[][] cnt = new int[n][26];
        int[] ans = new int[n];
        for (int i = n - 1; i >= 0; i--) {                               //* children first
            int v = order[i];
            ans[v] = ++cnt[v][labels.charAt(v) - 'a'];
            if (parent[v] != -1)
                for (int c = 0; c < 26; c++) cnt[parent[v]][c] += cnt[v][c];   //* merge into parent
        }
        return ans;
    }
}

/*@LC 834
tag: Rerooting
cue: For every node, the sum of distances to all other nodes.
hook: Moving the root to a child brings size[child] nodes one closer and the other n − size[child] one farther.
time: O(n) time · O(n) space
trap: Compute `ans[0]` (sum of depths) and all subtree sizes BEFORE the forward pass.
@*/
class LC834 {
    public int[] sumOfDistancesInTree(int n, int[][] edges) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) { g.get(e[0]).add(e[1]); g.get(e[1]).add(e[0]); }
        int[] order = new int[n], parent = new int[n], depth = new int[n], size = new int[n];
        Arrays.fill(parent, -1);
        boolean[] seen = new boolean[n];
        int head = 0, tail = 0;
        order[tail++] = 0;
        seen[0] = true;
        while (head < tail) {
            int u = order[head++];
            for (int v : g.get(u)) if (!seen[v]) { seen[v] = true; parent[v] = u; depth[v] = depth[u] + 1; order[tail++] = v; }
        }
        int[] ans = new int[n];
        for (int i = n - 1; i >= 0; i--) {                       //* pass 1: sizes, ans[0]
            int v = order[i];
            size[v]++;
            ans[0] += depth[v];
            if (parent[v] != -1) size[parent[v]] += size[v];
        }
        for (int i = 1; i < n; i++) {                            //* pass 2: parents first
            int v = order[i];
            ans[v] = ans[parent[v]] - size[v] + (n - size[v]);   //*
        }
        return ans;
    }
}

/*@LC 3068
tag: Parity trick (tree shape does not matter)
cue: Operation: pick an edge and XOR both ends with k. Maximise the sum.
hook: Repeating along paths lets you flip ANY even number of nodes: take every gain, fix the parity once.
time: O(n) time · O(1) space
trap: If an odd number of nodes want flipping, pay the smallest |x ^ k − x| once.
var: Tree DP version | `dp[v][parity of flipped nodes in the subtree]` gives the same answer.
@*/
class LC3068 {
    public long maximumValueSum(int[] nums, int k, int[][] edges) {
        long sum = 0;
        int flips = 0, minLoss = Integer.MAX_VALUE;
        for (int x : nums) {
            int y = x ^ k;
            sum += Math.max(x, y);                                //* take the better value
            if (y > x) flips++;
            minLoss = Math.min(minLoss, Math.abs(y - x));
        }
        return flips % 2 == 0 ? sum : sum - minLoss;              //* fix odd parity
    }
}

/*@LC 2858
tag: Rerooting over edge directions
cue: For every node, fewest edge reversals so it can reach every other node.
hook: Count reversals from root 0; crossing an edge to the child changes the total by exactly ±1.
time: O(n) time · O(n) space
trap: Store each edge both ways: cost 0 along its direction, cost 1 against it.
@*/
class LC2858 {
    public int[] minEdgeReversals(int n, int[][] edges) {
        List<List<int[]>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) {
            g.get(e[0]).add(new int[]{e[1], 0});                       //* along the arrow
            g.get(e[1]).add(new int[]{e[0], 1});                       //* against it
        }
        int[] order = new int[n], parent = new int[n], costDown = new int[n];
        Arrays.fill(parent, -1);
        boolean[] seen = new boolean[n];
        int head = 0, tail = 0, base = 0;
        order[tail++] = 0;
        seen[0] = true;
        while (head < tail) {
            int u = order[head++];
            for (int[] e : g.get(u))
                if (!seen[e[0]]) {
                    seen[e[0]] = true;
                    parent[e[0]] = u;
                    costDown[e[0]] = e[1];                               // cost to go parent -> child
                    base += e[1];                                        //* reversals for root 0
                    order[tail++] = e[0];
                }
        }
        int[] ans = new int[n];
        ans[0] = base;
        for (int i = 1; i < n; i++) {
            int v = order[i];
            ans[v] = ans[parent[v]] + (costDown[v] == 0 ? 1 : -1);      //* the edge flips its role
        }
        return ans;
    }
}

/*@LC 2003
tag: Only one path matters
cue: For every subtree, the smallest missing genetic value (values are distinct, ≥ 1).
hook: A subtree without the value 1 answers 1; only the path from "the node with 1" up to the root needs work.
time: O(n + V) time · O(n + V) space
trap: Do not re-walk the branch you came from: a `visited` array makes the whole walk O(n).
@*/
class LC2003 {
    public int[] smallestMissingValueSubtree(int[] parents, int[] nums) {
        int n = parents.length;
        int[] ans = new int[n];
        Arrays.fill(ans, 1);                                          //* most subtrees miss 1
        int start = -1;
        for (int i = 0; i < n; i++) if (nums[i] == 1) start = i;
        if (start == -1) return ans;
        List<List<Integer>> children = new ArrayList<>();
        for (int i = 0; i < n; i++) children.add(new ArrayList<>());
        for (int i = 1; i < n; i++) children.get(parents[i]).add(i);
        boolean[] visited = new boolean[n];
        boolean[] has = new boolean[100_002];
        int missing = 1;
        for (int v = start; v != -1; v = parents[v]) {                //* walk up from the 1
            Deque<Integer> st = new ArrayDeque<>();
            st.push(v);
            while (!st.isEmpty()) {                                   //* add the new part of the subtree
                int u = st.pop();
                if (visited[u]) continue;
                visited[u] = true;
                has[nums[u]] = true;
                for (int c : children.get(u)) if (!visited[c]) st.push(c);
            }
            while (has[missing]) missing++;                           //* only moves forward
            ans[v] = missing;
        }
        return ans;
    }
}

public class Day19 {
    static int[][] distAll(int n, int[][] edges) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) { g.get(e[0]).add(e[1]); g.get(e[1]).add(e[0]); }
        int[][] d = new int[n][n];
        for (int a = 0; a < n; a++) {
            Arrays.fill(d[a], -1);
            d[a][a] = 0;
            Deque<Integer> q = new ArrayDeque<>();
            q.add(a);
            while (!q.isEmpty()) { int u = q.poll(); for (int v : g.get(u)) if (d[a][v] == -1) { d[a][v] = d[a][u] + 1; q.add(v); } }
        }
        return d;
    }

    public static void main(String[] args) {
        T.eq("core mis", Core19.maxIndependentSet(4, T.grid("[[0,1],[1,2],[2,3]]"), T.ints("[1,5,5,1]")), 6L);

        T.eq("337a", new LC337().rob(T.tree(3, 2, 3, null, 3, null, 1)), 7);
        T.eq("337b", new LC337().rob(T.tree(3, 4, 5, 1, 3, null, 1)), 9);
        T.eq("968a", new LC337v1().minCameraCover(T.tree(0, 0, null, 0, 0)), 1);
        T.eq("968b", new LC337v1().minCameraCover(T.tree(0, 0, null, 0, null, 0, null, null, 0)), 2);
        T.eq("968c", new LC337v1().minCameraCover(T.tree(0)), 1);

        T.eq("1519a", new LC1519().countSubTrees(7, T.grid("[[0,1],[0,2],[1,4],[1,5],[2,3],[2,6]]"), "abaedcd"), T.ints("[2,1,1,1,1,1,1]"));
        T.eq("1519b", new LC1519().countSubTrees(4, T.grid("[[0,1],[1,2],[0,3]]"), "bbbb"), T.ints("[4,2,1,1]"));
        T.eq("1519c", new LC1519().countSubTrees(5, T.grid("[[0,1],[0,2],[1,3],[0,4]]"), "aabab"), T.ints("[3,2,1,1,1]"));

        T.eq("834a", new LC834().sumOfDistancesInTree(6, T.grid("[[0,1],[0,2],[2,3],[2,4],[2,5]]")), T.ints("[8,12,6,10,10,10]"));
        T.eq("834b", new LC834().sumOfDistancesInTree(1, new int[0][]), T.ints("[0]"));
        T.eq("834c", new LC834().sumOfDistancesInTree(2, T.grid("[[1,0]]")), T.ints("[1,1]"));
        for (int t = 0; t < 150; t++) {
            int n = T.rnd(1, 10);
            int[][] e = T.randTreeEdges(n), d = distAll(n, e);
            int[] want = new int[n];
            for (int a = 0; a < n; a++) for (int b = 0; b < n; b++) want[a] += d[a][b];
            T.eq("834 rand", new LC834().sumOfDistancesInTree(n, e), want);
            int[] rev = new int[n];
            for (int root = 0; root < n; root++) {
                for (int[] x : e) {   // edge x[0] -> x[1] must point away from root: reverse if x[1] is closer
                    if (d[root][x[1]] < d[root][x[0]]) rev[root]++;
                }
            }
            T.eq("2858 rand", new LC2858().minEdgeReversals(n, e), rev);
            int[] w = T.rand(n, 0, 9);
            long mis = 0;
            for (int m = 0; m < 1 << n; m++) {
                boolean ok = true; long s = 0;
                for (int[] x : e) if ((m >> x[0] & 1) == 1 && (m >> x[1] & 1) == 1) ok = false;
                for (int i = 0; i < n; i++) if ((m >> i & 1) == 1) s += w[i];
                if (ok) mis = Math.max(mis, s);
            }
            T.eq("core mis rand", Core19.maxIndependentSet(n, e, w), mis);
            int k = T.rnd(1, 15);
            long best = 0;
            for (int m = 0; m < 1 << n; m++) {
                if (Integer.bitCount(m) % 2 == 1) continue;
                long s = 0;
                for (int i = 0; i < n; i++) s += (m >> i & 1) == 1 ? (w[i] ^ k) : w[i];
                best = Math.max(best, s);
            }
            if (n >= 2) T.eq("3068 rand", new LC3068().maximumValueSum(w, k, e), best);
        }

        T.eq("3068a", new LC3068().maximumValueSum(T.ints("[1,2,1]"), 3, T.grid("[[0,1],[0,2]]")), 6L);
        T.eq("3068b", new LC3068().maximumValueSum(T.ints("[2,3]"), 7, T.grid("[[0,1]]")), 9L);
        T.eq("3068c", new LC3068().maximumValueSum(T.ints("[7,7,7,7,7,7]"), 3, T.grid("[[0,1],[0,2],[0,3],[0,4],[0,5]]")), 42L);

        T.eq("2858a", new LC2858().minEdgeReversals(4, T.grid("[[2,0],[2,1],[1,3]]")), T.ints("[1,1,0,2]"));
        T.eq("2858b", new LC2858().minEdgeReversals(3, T.grid("[[1,2],[2,0]]")), T.ints("[2,0,1]"));

        T.eq("2003a", new LC2003().smallestMissingValueSubtree(T.ints("[-1,0,0,2]"), T.ints("[1,2,3,4]")), T.ints("[5,1,1,1]"));
        T.eq("2003b", new LC2003().smallestMissingValueSubtree(T.ints("[-1,0,1,0,3,3]"), T.ints("[5,4,6,2,1,3]")), T.ints("[7,1,1,4,2,1]"));
        T.eq("2003c", new LC2003().smallestMissingValueSubtree(T.ints("[-1,2,3,0,2,4,1]"), T.ints("[2,3,4,5,6,7,8]")), T.ints("[1,1,1,1,1,1,1]"));
        for (int t = 0; t < 150; t++) {
            int n = T.rnd(1, 10);
            int[] p = new int[n];
            p[0] = -1;
            for (int i = 1; i < n; i++) p[i] = T.rnd(0, i - 1);
            List<Integer> vals = new ArrayList<>();
            for (int v = 1; v <= n + 2; v++) vals.add(v);
            Collections.shuffle(vals, T.R);
            int[] nums = new int[n];
            for (int i = 0; i < n; i++) nums[i] = vals.get(i);
            int[] want = new int[n];
            for (int v = 0; v < n; v++) {
                Set<Integer> s = new HashSet<>();
                for (int u = 0; u < n; u++) { int x = u; while (x != -1 && x != v) x = p[x]; if (x == v) s.add(nums[u]); }
                int m = 1;
                while (s.contains(m)) m++;
                want[v] = m;
            }
            T.eq("2003 rand", new LC2003().smallestMissingValueSubtree(p, nums), want);
        }
        T.summary("Day19");
    }
}
