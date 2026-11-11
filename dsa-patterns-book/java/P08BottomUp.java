import java.util.*;

public class P08BottomUp {
    //@tpl depth
    // Each call returns one number about its subtree; the parent combines two.
    static int maxDepth(TreeNode t) {
        if (t == null) return 0;
        return 1 + Math.max(maxDepth(t.left), maxDepth(t.right));
    }

    // Balanced: return the height, or -1 as soon as any subtree is unbalanced.
    static boolean isBalanced(TreeNode root) {
        return height(root) != -1;
    }

    static int height(TreeNode t) {
        if (t == null) return 0;
        int l = height(t.left);
        if (l == -1) return -1;                    // pass the failure up
        int r = height(t.right);
        if (r == -1 || Math.abs(l - r) > 1) return -1;
        return 1 + Math.max(l, r);
    }
    //@end

    //@tpl diameter
    // Diameter (in edges): return the height up, and at every node record
    // the longest path that bends there: left height + right height.
    static int best;

    static int diameterOfBinaryTree(TreeNode root) {
        best = 0;                                  // reset: static outlives a call
        depth(root);
        return best;
    }

    static int depth(TreeNode t) {
        if (t == null) return 0;
        int l = depth(t.left), r = depth(t.right);
        best = Math.max(best, l + r);              // path through t
        return 1 + Math.max(l, r);                 // only one side can go up
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int naiveHeight(TreeNode t) { return t == null ? 0 : 1 + Math.max(naiveHeight(t.left), naiveHeight(t.right)); }

    static boolean naiveBalanced(TreeNode t) {
        if (t == null) return true;
        return Math.abs(naiveHeight(t.left) - naiveHeight(t.right)) <= 1 && naiveBalanced(t.left) && naiveBalanced(t.right);
    }

    // all-pairs BFS on the tree as an undirected graph
    static int bruteDiameter(TreeNode root) {
        if (root == null) return 0;
        List<TreeNode> nodes = new ArrayList<>();
        Map<TreeNode, List<TreeNode>> g = new IdentityHashMap<>();
        Deque<TreeNode> st = new ArrayDeque<>();
        st.push(root);
        while (!st.isEmpty()) {
            TreeNode t = st.pop();
            nodes.add(t);
            g.putIfAbsent(t, new ArrayList<>());
            for (TreeNode c : new TreeNode[] {t.left, t.right}) {
                if (c == null) continue;
                g.putIfAbsent(c, new ArrayList<>());
                g.get(t).add(c);
                g.get(c).add(t);
                st.push(c);
            }
        }
        int best = 0;
        for (TreeNode s : nodes) {
            Map<TreeNode, Integer> dist = new IdentityHashMap<>();
            Deque<TreeNode> q = new ArrayDeque<>();
            dist.put(s, 0);
            q.add(s);
            while (!q.isEmpty()) {
                TreeNode t = q.poll();
                best = Math.max(best, dist.get(t));
                for (TreeNode u : g.get(t)) if (!dist.containsKey(u)) { dist.put(u, dist.get(t) + 1); q.add(u); }
            }
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(83);
        for (int t = 0; t < 3000; t++) {
            int n = r.nextInt(25);
            TreeNode root = Nodes.randomTree(r, n, 0, 9);
            if (r.nextInt(3) == 0) {   // denser chance of balanced trees: random BST
                int[] vals = new int[n];
                for (int i = 0; i < n; i++) vals[i] = i;
                root = Nodes.randomBst(r, vals, 0, n - 1);
            }
            String sh = Nodes.shape(root);
            Check.eq(naiveHeight(root), maxDepth(root), "depth " + sh);
            Check.eq(naiveBalanced(root), isBalanced(root), "balanced " + sh);
            Check.eq(bruteDiameter(root), diameterOfBinaryTree(root), "diameter " + sh);
        }
        Check.eq(3, maxDepth(Nodes.tree(3, 9, 20, null, null, 15, 7)), "LC 104 sample 1");
        Check.eq(2, maxDepth(Nodes.tree(1, null, 2)), "LC 104 sample 2");
        Check.eq(true, isBalanced(Nodes.tree(3, 9, 20, null, null, 15, 7)), "LC 110 sample 1");
        Check.eq(false, isBalanced(Nodes.tree(1, 2, 2, 3, 3, null, null, 4, 4)), "LC 110 sample 2");
        Check.eq(true, isBalanced(null), "LC 110 sample 3");
        Check.eq(3, diameterOfBinaryTree(Nodes.tree(1, 2, 3, 4, 5)), "LC 543 sample 1");
        Check.eq(1, diameterOfBinaryTree(Nodes.tree(1, 2)), "LC 543 sample 2");
        Check.done("P08BottomUp");
    }
}
