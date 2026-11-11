import java.util.*;

public class P08MaxPathSum {
    //@tpl maxpath
    // gain(t) = best downward path from t; each node is tried as the bend.
    static int best;

    static int maxPathSum(TreeNode root) {
        best = Integer.MIN_VALUE;                  // a path has at least 1 node
        gain(root);
        return best;
    }

    static int gain(TreeNode t) {
        if (t == null) return 0;
        int l = Math.max(0, gain(t.left));         // a negative arm is dropped
        int r = Math.max(0, gain(t.right));
        best = Math.max(best, t.val + l + r);      // both arms meet at t
        return t.val + Math.max(l, r);             // only one arm goes up
    }
    //@end

    //@tpl univalue
    // Longest univalue path (edges): an arm goes on only into an equal child.
    static int longest;

    static int longestUnivaluePath(TreeNode root) {
        longest = 0;
        arm(root);
        return longest;
    }

    static int arm(TreeNode t) {
        if (t == null) return 0;
        int l = arm(t.left), r = arm(t.right);     // always visit both children
        int dl = t.left != null && t.left.val == t.val ? l + 1 : 0;
        int dr = t.right != null && t.right.val == t.val ? r + 1 : 0;
        longest = Math.max(longest, dl + dr);
        return Math.max(dl, dr);
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static void collect(TreeNode t, List<TreeNode> nodes, Map<TreeNode, List<TreeNode>> g) {
        if (t == null) return;
        nodes.add(t);
        g.putIfAbsent(t, new ArrayList<>());
        for (TreeNode c : new TreeNode[] {t.left, t.right}) {
            if (c == null) continue;
            g.putIfAbsent(c, new ArrayList<>());
            g.get(t).add(c);
            g.get(c).add(t);
            collect(c, nodes, g);
        }
    }

    // every simple path = every (start, end) pair; DFS from each start
    static void spread(TreeNode v, TreeNode from, long sum, Map<TreeNode, List<TreeNode>> g, long[] best) {
        sum += v.val;
        best[0] = Math.max(best[0], sum);
        for (TreeNode u : g.get(v)) if (u != from) spread(u, v, sum, g, best);
    }

    static int bruteMaxPath(TreeNode root) {
        List<TreeNode> nodes = new ArrayList<>();
        Map<TreeNode, List<TreeNode>> g = new IdentityHashMap<>();
        collect(root, nodes, g);
        long[] best = {Long.MIN_VALUE};
        for (TreeNode s : nodes) spread(s, null, 0, g, best);
        return (int) best[0];
    }

    static void sameSpread(TreeNode v, TreeNode from, int len, Map<TreeNode, List<TreeNode>> g, int[] best) {
        best[0] = Math.max(best[0], len);
        for (TreeNode u : g.get(v)) if (u != from && u.val == v.val) sameSpread(u, v, len + 1, g, best);
    }

    static int bruteUnivalue(TreeNode root) {
        List<TreeNode> nodes = new ArrayList<>();
        Map<TreeNode, List<TreeNode>> g = new IdentityHashMap<>();
        collect(root, nodes, g);
        int[] best = {0};
        for (TreeNode s : nodes) sameSpread(s, null, 0, g, best);
        return best[0];
    }

    // the two wrong versions quoted in the traps
    static int bothArmsUp(TreeNode t, int[] b) {
        if (t == null) return 0;
        int l = Math.max(0, bothArmsUp(t.left, b)), r = Math.max(0, bothArmsUp(t.right, b));
        b[0] = Math.max(b[0], t.val + l + r);
        return t.val + l + r;
    }

    static int stopEarly(TreeNode t, int[] b) {
        if (t == null) return 0;
        int dl = 0, dr = 0;
        if (t.left != null && t.left.val == t.val) dl = stopEarly(t.left, b) + 1;
        if (t.right != null && t.right.val == t.val) dr = stopEarly(t.right, b) + 1;
        b[0] = Math.max(b[0], dl + dr);
        return Math.max(dl, dr);
    }

    public static void main(String[] args) {
        Random r = new Random(84);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(22);
            TreeNode root = Nodes.randomTree(r, n, -10, 10);
            Check.eq(bruteMaxPath(root), maxPathSum(root), "maxpath " + Nodes.shape(root));
            TreeNode u = Nodes.randomTree(r, n, 1, 2 + r.nextInt(2));
            Check.eq(bruteUnivalue(u), longestUnivaluePath(u), "univalue " + Nodes.shape(u));
        }
        Check.eq(6, maxPathSum(Nodes.tree(1, 2, 3)), "LC 124 sample 1");
        Check.eq(42, maxPathSum(Nodes.tree(-10, 9, 20, null, null, 15, 7)), "LC 124 sample 2");
        Check.eq(-3, maxPathSum(Nodes.tree(-3)), "all negative");
        Check.eq(2, longestUnivaluePath(Nodes.tree(5, 4, 5, 1, 1, null, 5)), "LC 687 sample 1");
        Check.eq(2, longestUnivaluePath(Nodes.tree(1, 4, 5, 4, 4, null, 5)), "LC 687 sample 2");
        Check.eq(0, longestUnivaluePath(null), "LC 687 empty");
        // trap numbers
        TreeNode trap = Nodes.tree(1, 2, 3, 4, 5);
        int[] b = {Integer.MIN_VALUE};
        bothArmsUp(trap, b);
        Check.eq(11, maxPathSum(trap), "trap tree right answer");
        Check.eq(15, b[0], "trap: both arms returned up");
        TreeNode trap2 = Nodes.tree(1, 2, null, 2, null, 2);
        int[] b2 = {0};
        stopEarly(trap2, b2);
        Check.eq(2, longestUnivaluePath(trap2), "trap2 right answer");
        Check.eq(0, b2[0], "trap2: stop at a different value");
        Check.done("P08MaxPathSum");
    }
}
