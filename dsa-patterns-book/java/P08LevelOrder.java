import java.util.*;

public class P08LevelOrder {
    //@tpl levels
    // BFS one level at a time. When a level starts, the queue holds exactly
    // that level, so its size is fixed before any child is added.
    static List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> out = new ArrayList<>();
        Deque<TreeNode> q = new ArrayDeque<>();
        if (root != null) q.add(root);
        while (!q.isEmpty()) {
            int size = q.size();                   // read once, before the loop
            List<Integer> level = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                TreeNode t = q.poll();
                level.add(t.val);
                if (t.left != null) q.add(t.left);
                if (t.right != null) q.add(t.right);
            }
            out.add(level);
        }
        return out;
    }
    //@end

    //@tpl views
    // Right side view: the last value of each level.
    static List<Integer> rightSideView(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        for (List<Integer> lv : levelOrder(root)) out.add(lv.get(lv.size() - 1));
        return out;
    }

    // Zigzag: the BFS order never changes; every second level is reversed.
    static List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        List<List<Integer>> levels = levelOrder(root);
        for (int d = 1; d < levels.size(); d += 2) Collections.reverse(levels.get(d));
        return levels;
    }
    //@end

    //@tpl width
    // Maximum width: number the nodes like a heap (children 2i+1 and 2i+2).
    // Rebase every level to start at 0, so an index is below 2 * width.
    static int widthOfBinaryTree(TreeNode root) {
        if (root == null) return 0;
        Deque<TreeNode> q = new ArrayDeque<>();
        Deque<Long> idx = new ArrayDeque<>();
        q.add(root);
        idx.add(0L);
        long best = 0;
        while (!q.isEmpty()) {
            long first = idx.peek(), last = 0;
            for (int i = 0, size = q.size(); i < size; i++) {
                TreeNode t = q.poll();
                last = idx.poll() - first;             // rebased index
                if (t.left != null) { q.add(t.left); idx.add(2 * last + 1); }
                if (t.right != null) { q.add(t.right); idx.add(2 * last + 2); }
            }
            best = Math.max(best, last + 1);           // first is 0 after rebase
        }
        return (int) best;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static void byDepth(TreeNode t, int d, List<List<Integer>> out) {
        if (t == null) return;
        if (out.size() == d) out.add(new ArrayList<>());
        out.get(d).add(t.val);
        byDepth(t.left, d + 1, out);
        byDepth(t.right, d + 1, out);
    }

    // exact heap positions with BigInteger, min and max per depth
    static void positions(TreeNode t, int d, java.math.BigInteger pos,
                          List<java.math.BigInteger[]> span) {
        if (t == null) return;
        if (span.size() == d) span.add(new java.math.BigInteger[] {pos, pos});
        java.math.BigInteger[] s = span.get(d);
        if (pos.compareTo(s[0]) < 0) s[0] = pos;
        if (pos.compareTo(s[1]) > 0) s[1] = pos;
        java.math.BigInteger two = java.math.BigInteger.valueOf(2);
        positions(t.left, d + 1, pos.multiply(two).add(java.math.BigInteger.ONE), span);
        positions(t.right, d + 1, pos.multiply(two).add(two), span);
    }

    static int bruteWidth(TreeNode root) {
        List<java.math.BigInteger[]> span = new ArrayList<>();
        positions(root, 0, java.math.BigInteger.ZERO, span);
        java.math.BigInteger best = java.math.BigInteger.ZERO;
        for (java.math.BigInteger[] s : span) best = best.max(s[1].subtract(s[0]).add(java.math.BigInteger.ONE));
        return best.intValueExact();
    }

    static List<Integer> list(int... v) {
        List<Integer> l = new ArrayList<>();
        for (int x : v) l.add(x);
        return l;
    }

    public static void main(String[] args) {
        Random r = new Random(82);
        for (int t = 0; t < 3000; t++) {
            TreeNode root = Nodes.randomTree(r, r.nextInt(40), -20, 20);
            List<List<Integer>> ref = new ArrayList<>();
            byDepth(root, 0, ref);
            Check.eq(ref, levelOrder(root), "levels " + Nodes.shape(root));
            List<Integer> right = new ArrayList<>();
            List<List<Integer>> zz = new ArrayList<>();
            for (int d = 0; d < ref.size(); d++) {
                List<Integer> lv = new ArrayList<>(ref.get(d));
                right.add(lv.get(lv.size() - 1));
                if (d % 2 == 1) Collections.reverse(lv);
                zz.add(lv);
            }
            Check.eq(right, rightSideView(root), "right view");
            Check.eq(zz, zigzagLevelOrder(root), "zigzag");
            Check.eq(bruteWidth(root), widthOfBinaryTree(root), "width " + Nodes.shape(root));
        }
        // deep, thin trees: true indices have hundreds of bits, widths stay small.
        // A random chain; at a random depth it splits into two chains that hug
        // each other (left one keeps going right, right one keeps going left).
        for (int t = 0; t < 200; t++) {
            int depth = 100 + r.nextInt(300), split = r.nextInt(depth);
            TreeNode root = new TreeNode(0), a = root, b = null;
            for (int d = 1; d < depth; d++) {
                if (b == null && d == split) {
                    a.left = new TreeNode(d);
                    a.right = new TreeNode(d);
                    b = a.right;
                    a = a.left;
                    continue;
                }
                TreeNode na = new TreeNode(d);
                if (b == null) {
                    if (r.nextBoolean()) a.left = na; else a.right = na;
                    if (r.nextInt(8) == 0) { if (a.left == null) a.left = new TreeNode(-1); else a.right = new TreeNode(-1); }
                } else {
                    a.right = na;
                    TreeNode nb = new TreeNode(d);
                    b.left = nb;
                    if (r.nextInt(8) == 0) b.right = new TreeNode(-1);
                    b = nb;
                }
                a = na;
            }
            Check.isTrue(bruteWidth(root) <= 4, "thin tree");
            Check.eq(bruteWidth(root), widthOfBinaryTree(root), "deep width");
        }
        TreeNode s = Nodes.tree(3, 9, 20, null, null, 15, 7);
        Check.eq(Arrays.asList(list(3), list(9, 20), list(15, 7)), levelOrder(s), "LC 102 sample 1");
        Check.eq(Arrays.asList(list(1)), levelOrder(Nodes.tree(1)), "LC 102 sample 2");
        Check.eq(new ArrayList<List<Integer>>(), levelOrder(null), "LC 102 sample 3");
        Check.eq(Arrays.asList(list(3), list(20, 9), list(15, 7)), zigzagLevelOrder(s), "LC 103 sample 1");
        Check.eq(list(1, 3, 4), rightSideView(Nodes.tree(1, 2, 3, null, 5, null, 4)), "LC 199 sample 1");
        Check.eq(list(1, 3, 4, 5), rightSideView(Nodes.tree(1, 2, 3, 4, null, null, null, 5)), "LC 199 sample 2");
        Check.eq(list(1, 3, 4), rightSideView(Nodes.tree(1, 2, 3, 4)), "right view, left sticks out");
        Check.eq(4, widthOfBinaryTree(Nodes.tree(1, 3, 2, 5, 3, null, 9)), "LC 662 sample 1");
        Check.eq(7, widthOfBinaryTree(Nodes.tree(1, 3, 2, 5, null, null, 9, 6, null, 7)), "LC 662 sample 2");
        Check.eq(2, widthOfBinaryTree(Nodes.tree(1, 3, 2, 5)), "LC 662 sample 3");
        Check.done("P08LevelOrder");
    }
}
