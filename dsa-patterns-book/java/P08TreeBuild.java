import java.util.*;

public class P08TreeBuild {
    //@tpl build
    // Build from preorder + inorder (distinct values). pre[next] is the root of
    // the current inorder range; its position there splits left from right.
    static int next;
    static Map<Integer, Integer> pos;

    static TreeNode buildTree(int[] pre, int[] in) {
        next = 0;
        pos = new HashMap<>();
        for (int i = 0; i < in.length; i++) pos.put(in[i], i);  // O(1) lookups
        return build(pre, 0, in.length - 1);
    }

    static TreeNode build(int[] pre, int lo, int hi) {     // inorder [lo, hi]
        if (lo > hi) return null;
        TreeNode t = new TreeNode(pre[next++]);
        int mid = pos.get(t.val);
        t.left = build(pre, lo, mid - 1);          // left first: preorder is
        t.right = build(pre, mid + 1, hi);         // root, left, right
        return t;
    }
    //@end

    //@tpl serialize
    // Serialize: preorder, with "#" for every null child.
    static String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        write(root, sb);
        return sb.toString();
    }

    static void write(TreeNode t, StringBuilder sb) {
        if (t == null) { sb.append("#,"); return; }
        sb.append(t.val).append(',');
        write(t.left, sb);
        write(t.right, sb);
    }
    //@end

    //@tpl deserialize
    // Deserialize: read the tokens in the same order write() produced them.
    static TreeNode deserialize(String data) {
        return read(new ArrayDeque<>(Arrays.asList(data.split(","))));
    }

    static TreeNode read(Deque<String> tokens) {
        String s = tokens.poll();
        if (s.equals("#")) return null;
        TreeNode t = new TreeNode(Integer.parseInt(s));
        t.left = read(tokens);                     // same order as write()
        t.right = read(tokens);
        return t;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static void pre(TreeNode t, List<Integer> out) {
        if (t == null) return;
        out.add(t.val);
        pre(t.left, out);
        pre(t.right, out);
    }

    static int[] arr(List<Integer> l) {
        int[] a = new int[l.size()];
        for (int i = 0; i < a.length; i++) a[i] = l.get(i);
        return a;
    }

    static TreeNode distinctTree(Random r, int n) {
        TreeNode root = Nodes.randomTree(r, n, 0, 0);
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < n; i++) ids.add(r.nextInt(7) - 3000 + 7 * i);   // distinct, may be negative
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
        Random r = new Random(88);
        for (int t = 0; t < 3000; t++) {
            int n = r.nextInt(30);
            TreeNode root = distinctTree(r, n);
            List<Integer> p = new ArrayList<>(), in = new ArrayList<>();
            pre(root, p);
            Nodes.inorder(root, in);
            Check.eq(Nodes.shape(root), Nodes.shape(buildTree(arr(p), arr(in))), "build " + Nodes.shape(root));
            TreeNode any = Nodes.randomTree(r, n, -1000, 1000);
            Check.eq(Nodes.shape(any), Nodes.shape(deserialize(serialize(any))), "codec " + Nodes.shape(any));
        }
        Check.eq(Nodes.shape(Nodes.tree(3, 9, 20, null, null, 15, 7)),
                 Nodes.shape(buildTree(new int[] {3, 9, 20, 15, 7}, new int[] {9, 3, 15, 20, 7})), "LC 105 sample 1");
        Check.eq(Nodes.shape(Nodes.tree(-1)), Nodes.shape(buildTree(new int[] {-1}, new int[] {-1})), "LC 105 sample 2");
        TreeNode s = Nodes.tree(1, 2, 3, null, null, 4, 5);
        Check.eq("1,2,#,#,3,4,#,#,5,#,#,", serialize(s), "LC 297 sample 1 string");
        Check.eq(Nodes.shape(s), Nodes.shape(deserialize(serialize(s))), "LC 297 sample 1");
        Check.eq("#,", serialize(null), "LC 297 sample 2 string");
        Check.isTrue(deserialize(serialize(null)) == null, "LC 297 sample 2");
        Check.eq(Nodes.shape(Nodes.tree(Integer.MIN_VALUE, Integer.MAX_VALUE)), Nodes.shape(deserialize(serialize(Nodes.tree(Integer.MIN_VALUE, Integer.MAX_VALUE)))), "extremes");
        Check.done("P08TreeBuild");
    }
}
