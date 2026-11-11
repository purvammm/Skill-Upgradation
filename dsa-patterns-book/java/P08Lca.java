import java.util.*;

public class P08Lca {
    //@tpl binary
    // LCA: return p or q if found in t's subtree, else null. Both sides: t.
    static TreeNode lowestCommonAncestor(TreeNode t, TreeNode p, TreeNode q) {
        if (t == null || t == p || t == q) return t;
        TreeNode l = lowestCommonAncestor(t.left, p, q);
        TreeNode r = lowestCommonAncestor(t.right, p, q);
        if (l != null && r != null) return t;      // p and q on different sides
        return l != null ? l : r;                  // pass up what was found
    }
    //@end

    //@tpl bst
    // LCA in a BST: walk down while p and q are on the same side of t.
    static TreeNode lcaBst(TreeNode t, TreeNode p, TreeNode q) {
        while (true) {                             // p and q are in the tree
            if (p.val < t.val && q.val < t.val) t = t.left;
            else if (p.val > t.val && q.val > t.val) t = t.right;
            else return t;                         // they split here, or t is one
        }
    }
    //@end

    //@tpl directions
    // Step-by-step directions: root-to-node paths as strings of L and R.
    // Their common prefix leads to the LCA; climb from s to it, then go down.
    static String getDirections(TreeNode root, int s, int d) {
        StringBuilder a = new StringBuilder(), b = new StringBuilder();
        find(root, s, a);
        find(root, d, b);
        int i = 0;
        while (i < a.length() && i < b.length() && a.charAt(i) == b.charAt(i)) i++;
        for (int j = i; j < a.length(); j++) a.setCharAt(j, 'U');    // climb
        return a.substring(i) + b.substring(i);                      // descend
    }

    static boolean find(TreeNode t, int target, StringBuilder path) {
        if (t == null) return false;
        if (t.val == target) return true;
        path.append('L');
        if (find(t.left, target, path)) return true;
        path.setCharAt(path.length() - 1, 'R');
        if (find(t.right, target, path)) return true;
        path.deleteCharAt(path.length() - 1);      // not below t: undo
        return false;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static void parents(TreeNode t, TreeNode par, Map<TreeNode, TreeNode> out, List<TreeNode> nodes) {
        if (t == null) return;
        out.put(t, par);
        nodes.add(t);
        parents(t.left, t, out, nodes);
        parents(t.right, t, out, nodes);
    }

    static TreeNode naiveLca(Map<TreeNode, TreeNode> par, TreeNode p, TreeNode q) {
        Set<TreeNode> anc = Collections.newSetFromMap(new IdentityHashMap<>());
        for (TreeNode x = p; x != null; x = par.get(x)) anc.add(x);
        for (TreeNode y = q; y != null; y = par.get(y)) if (anc.contains(y)) return y;
        return null;
    }

    // BFS over the tree as a graph with labelled moves U, L, R
    static String bfsDirections(TreeNode root, Map<TreeNode, TreeNode> par, TreeNode s, int d) {
        Map<TreeNode, String> seen = new IdentityHashMap<>();
        Deque<TreeNode> q = new ArrayDeque<>();
        seen.put(s, "");
        q.add(s);
        while (!q.isEmpty()) {
            TreeNode t = q.poll();
            String w = seen.get(t);
            if (t.val == d) return w;
            TreeNode[] next = {par.get(t), t.left, t.right};
            String[] mv = {"U", "L", "R"};
            for (int i = 0; i < 3; i++)
                if (next[i] != null && !seen.containsKey(next[i])) { seen.put(next[i], w + mv[i]); q.add(next[i]); }
        }
        return null;
    }

    static TreeNode byVal(TreeNode t, int v) {
        if (t == null) return null;
        if (t.val == v) return t;
        TreeNode l = byVal(t.left, v);
        return l != null ? l : byVal(t.right, v);
    }

    static TreeNode labelled(Random r, int n) {   // values 1..n in random order
        TreeNode root = Nodes.randomTree(r, n, 0, 0);
        List<Integer> ids = new ArrayList<>();
        for (int i = 1; i <= n; i++) ids.add(i);
        Collections.shuffle(ids, r);
        Deque<TreeNode> st = new ArrayDeque<>();
        if (root != null) st.push(root);
        int k = 0;
        while (!st.isEmpty()) {
            TreeNode t = st.pop();
            t.val = ids.get(k++);
            if (t.left != null) st.push(t.left);
            if (t.right != null) st.push(t.right);
        }
        return root;
    }

    public static void main(String[] args) {
        Random r = new Random(87);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(30);
            TreeNode root = labelled(r, n);
            Map<TreeNode, TreeNode> par = new IdentityHashMap<>();
            List<TreeNode> nodes = new ArrayList<>();
            parents(root, null, par, nodes);
            for (int k = 0; k < 10; k++) {
                TreeNode p = nodes.get(r.nextInt(n)), q = nodes.get(r.nextInt(n));
                Check.isTrue(naiveLca(par, p, q) == lowestCommonAncestor(root, p, q), "lca " + Nodes.shape(root) + " " + p.val + "," + q.val);
                Check.eq(bfsDirections(root, par, p, q.val), getDirections(root, p.val, q.val), "directions " + p.val + "->" + q.val);
            }
            int[] vals = new int[n];
            for (int i = 0; i < n; i++) vals[i] = 3 * i - 20;
            TreeNode bst = Nodes.randomBst(r, vals, 0, n - 1);
            Map<TreeNode, TreeNode> bpar = new IdentityHashMap<>();
            List<TreeNode> bnodes = new ArrayList<>();
            parents(bst, null, bpar, bnodes);
            for (int k = 0; k < 10; k++) {
                TreeNode p = bnodes.get(r.nextInt(n)), q = bnodes.get(r.nextInt(n));
                Check.isTrue(naiveLca(bpar, p, q) == lcaBst(bst, p, q), "lca bst");
            }
        }
        TreeNode s = Nodes.tree(3, 5, 1, 6, 2, 0, 8, null, null, 7, 4);
        Check.eq(3, lowestCommonAncestor(s, byVal(s, 5), byVal(s, 1)).val, "LC 236 sample 1");
        Check.eq(5, lowestCommonAncestor(s, byVal(s, 5), byVal(s, 4)).val, "LC 236 sample 2");
        TreeNode s2 = Nodes.tree(1, 2);
        Check.eq(1, lowestCommonAncestor(s2, byVal(s2, 1), byVal(s2, 2)).val, "LC 236 sample 3");
        TreeNode b = Nodes.tree(6, 2, 8, 0, 4, 7, 9, null, null, 3, 5);
        Check.eq(6, lcaBst(b, byVal(b, 2), byVal(b, 8)).val, "LC 235 sample 1");
        Check.eq(2, lcaBst(b, byVal(b, 2), byVal(b, 4)).val, "LC 235 sample 2");
        Check.eq("UURL", getDirections(Nodes.tree(5, 1, 2, 3, null, 6, 4), 3, 6), "LC 2096 sample 1");
        Check.eq("L", getDirections(Nodes.tree(2, 1), 2, 1), "LC 2096 sample 2");
        Check.done("P08Lca");
    }
}
