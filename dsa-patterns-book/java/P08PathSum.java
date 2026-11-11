import java.util.*;

public class P08PathSum {
    //@tpl paths
    // Path sum II: path is the root-to-t path; add t going down, remove after.
    static List<List<Integer>> pathSum(TreeNode root, int target) {
        List<List<Integer>> out = new ArrayList<>();
        walk(root, target, new ArrayList<>(), out);
        return out;
    }

    static void walk(TreeNode t, int remain, List<Integer> path,
                     List<List<Integer>> out) {
        if (t == null) return;
        path.add(t.val);
        remain -= t.val;
        if (t.left == null && t.right == null && remain == 0)
            out.add(new ArrayList<>(path));        // copy: path keeps changing
        walk(t.left, remain, path, out);
        walk(t.right, remain, path, out);
        path.remove(path.size() - 1);              // undo before going back up
    }
    //@end

    //@tpl numbers
    // Sum root to leaf numbers: carry the number so far down; add it at leaves.
    static int sumNumbers(TreeNode root) { return sumNumbers(root, 0); }

    static int sumNumbers(TreeNode t, int sofar) {
        if (t == null) return 0;
        sofar = sofar * 10 + t.val;
        if (t.left == null && t.right == null) return sofar;
        return sumNumbers(t.left, sofar) + sumNumbers(t.right, sofar);
    }
    //@end

    //@tpl prefix
    // Path sum III: count downward paths (any start, any end) with sum target.
    // seen holds the prefix sums of the ancestors of t (root path only).
    static int pathSumAny(TreeNode root, int target) {
        Map<Long, Integer> seen = new HashMap<>();
        seen.put(0L, 1);                           // empty prefix: start at root
        return count(root, 0L, target, seen);
    }

    static int count(TreeNode t, long cur, int target, Map<Long, Integer> seen) {
        if (t == null) return 0;
        cur += t.val;                              // sum from root to t
        int res = seen.getOrDefault(cur - target, 0);   // paths ending at t
        seen.merge(cur, 1, Integer::sum);          // visible to t's subtree
        res += count(t.left, cur, target, seen);
        res += count(t.right, cur, target, seen);
        seen.merge(cur, -1, Integer::sum);         // remove on the way back
        return res;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // every root-to-leaf path, built with explicit parent links
    static List<List<Integer>> allLeafPaths(TreeNode root) {
        List<List<Integer>> out = new ArrayList<>();
        if (root == null) return out;
        Map<TreeNode, TreeNode> par = new IdentityHashMap<>();
        Deque<TreeNode> st = new ArrayDeque<>();
        st.push(root);
        par.put(root, null);
        List<TreeNode> leaves = new ArrayList<>();
        while (!st.isEmpty()) {
            TreeNode t = st.pop();
            if (t.left == null && t.right == null) leaves.add(t);
            if (t.left != null) { par.put(t.left, t); st.push(t.left); }
            if (t.right != null) { par.put(t.right, t); st.push(t.right); }
        }
        for (TreeNode leaf : leaves) {
            LinkedList<Integer> p = new LinkedList<>();
            for (TreeNode x = leaf; x != null; x = par.get(x)) p.addFirst(x.val);
            out.add(p);
        }
        return out;
    }

    static int downFrom(TreeNode t, long sum, long target) {
        if (t == null) return 0;
        sum += t.val;
        return (sum == target ? 1 : 0) + downFrom(t.left, sum, target) + downFrom(t.right, sum, target);
    }

    static int bruteAny(TreeNode t, long target) {
        if (t == null) return 0;
        return downFrom(t, 0, target) + bruteAny(t.left, target) + bruteAny(t.right, target);
    }

    static List<List<Integer>> lists(int[]... rows) {
        List<List<Integer>> out = new ArrayList<>();
        for (int[] row : rows) {
            List<Integer> l = new ArrayList<>();
            for (int x : row) l.add(x);
            out.add(l);
        }
        return out;
    }

    public static void main(String[] args) {
        Random r = new Random(85);
        for (int t = 0; t < 3000; t++) {
            TreeNode root = Nodes.randomTree(r, r.nextInt(25), -4, 6);
            int target = r.nextInt(17) - 4;
            List<List<Integer>> want = new ArrayList<>();
            int sumNum = 0;
            for (List<Integer> p : allLeafPaths(root)) {
                int s = 0;
                for (int x : p) s += x;
                if (s == target) want.add(p);
            }
            Check.eq(Check.canon(want, false), Check.canon(pathSum(root, target), false), "pathSum " + Nodes.shape(root));
            Check.eq(bruteAny(root, target), pathSumAny(root, target), "any " + Nodes.shape(root) + " t=" + target);
            TreeNode d = Nodes.randomTree(r, r.nextInt(12), 0, 9);   // depth <= 9 digits fits int
            int expect = 0;
            for (List<Integer> p : allLeafPaths(d)) {
                if (p.size() > 9) { expect = -1; break; }
                StringBuilder sb = new StringBuilder();
                for (int x : p) sb.append(x);
                expect += Integer.parseInt(sb.toString());
            }
            if (expect >= 0) Check.eq(expect, sumNumbers(d), "sumNumbers " + Nodes.shape(d));
        }
        TreeNode s = Nodes.tree(5, 4, 8, 11, null, 13, 4, 7, 2, null, null, 5, 1);
        Check.eq(Check.canon(lists(new int[] {5, 4, 11, 2}, new int[] {5, 8, 4, 5}), false), Check.canon(pathSum(s, 22), false), "LC 113 sample 1");
        Check.eq(0, pathSum(Nodes.tree(1, 2, 3), 5).size(), "LC 113 sample 2");
        Check.eq(0, pathSum(Nodes.tree(1, 2), 0).size(), "LC 113 sample 3");
        Check.eq(25, sumNumbers(Nodes.tree(1, 2, 3)), "LC 129 sample 1");
        Check.eq(1026, sumNumbers(Nodes.tree(4, 9, 0, 5, 1)), "LC 129 sample 2");
        Check.eq(3, pathSumAny(Nodes.tree(10, 5, -3, 3, 2, null, 11, 3, -2, null, 1), 8), "LC 437 sample 1");
        Check.eq(3, pathSumAny(s, 22), "LC 437 sample 2");
        Check.eq(0, pathSumAny(Nodes.tree(1000000000, 1000000000, null, 294967296, null, 1000000000, null, 1000000000, null, 1000000000), 0), "LC 437 overflow case");
        Check.done("P08PathSum");
    }
}
