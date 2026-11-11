import java.util.*;

public class P08TreeTraversal {
    //@tpl recursive
    // One walk, three orders: only the position of the visit line changes.
    static void walk(TreeNode t, List<Integer> pre, List<Integer> in,
                     List<Integer> post) {
        if (t == null) return;
        pre.add(t.val);                        // before both subtrees
        walk(t.left, pre, in, post);
        in.add(t.val);                         // between them
        walk(t.right, pre, in, post);
        post.add(t.val);                       // after both
    }
    //@end

    //@tpl inorder
    // Iterative inorder: push the whole left spine, pop, visit, go right.
    // The stack holds the ancestors whose left side is still being visited.
    static List<Integer> inorder(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        Deque<TreeNode> st = new ArrayDeque<>();
        TreeNode cur = root;
        while (cur != null || !st.isEmpty()) {
            while (cur != null) {              // 1. go left as far as possible
                st.push(cur);
                cur = cur.left;
            }
            cur = st.pop();                    // 2. leftmost unvisited node
            out.add(cur.val);
            cur = cur.right;                   // 3. then its right subtree
        }
        return out;
    }
    //@end

    //@tpl prepost
    // Preorder: pop, visit, push right then left, so left is popped first.
    static List<Integer> preorder(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        Deque<TreeNode> st = new ArrayDeque<>();
        if (root != null) st.push(root);
        while (!st.isEmpty()) {
            TreeNode t = st.pop();
            out.add(t.val);
            if (t.right != null) st.push(t.right);
            if (t.left != null) st.push(t.left);
        }
        return out;
    }

    // Postorder: walk like inorder, but record a node only after its right
    // subtree is done. last = the node recorded most recently.
    static List<Integer> postorderLast(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        Deque<TreeNode> st = new ArrayDeque<>();
        TreeNode cur = root, last = null;
        while (cur != null || !st.isEmpty()) {
            while (cur != null) { st.push(cur); cur = cur.left; }
            TreeNode top = st.peek();
            if (top.right != null && top.right != last) {
                cur = top.right;               // right subtree not done yet
            } else {
                out.add(top.val);              // both subtrees done
                last = st.pop();
            }
        }
        return out;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // The variant described in the text: postorder = root-right-left reversed.
    static List<Integer> postorder(TreeNode root) {
        LinkedList<Integer> out = new LinkedList<>();
        Deque<TreeNode> st = new ArrayDeque<>();
        if (root != null) st.push(root);
        while (!st.isEmpty()) {
            TreeNode t = st.pop();
            out.addFirst(t.val);               // prepend = reverse at the end
            if (t.left != null) st.push(t.left);
            if (t.right != null) st.push(t.right);
        }
        return out;
    }

    static List<Integer> list(int... v) {
        List<Integer> l = new ArrayList<>();
        for (int x : v) l.add(x);
        return l;
    }

    public static void main(String[] args) {
        Random r = new Random(81);
        for (int t = 0; t < 3000; t++) {
            TreeNode root = Nodes.randomTree(r, r.nextInt(30), -50, 50);
            List<Integer> pre = new ArrayList<>(), in = new ArrayList<>(), post = new ArrayList<>();
            walk(root, pre, in, post);
            List<Integer> ref = new ArrayList<>();
            Nodes.inorder(root, ref);
            Check.eq(ref, in, "recursive inorder vs Nodes.inorder");
            String shape = Nodes.shape(root);
            Check.eq(in, inorder(root), "inorder " + shape);
            Check.eq(pre, preorder(root), "preorder " + shape);
            Check.eq(post, postorder(root), "postorder " + shape);
            Check.eq(post, postorderLast(root), "postorderLast " + shape);
        }
        // a 100000-node chain: the iterative versions need no deep recursion
        TreeNode chain = null;
        for (int i = 0; i < 100000; i++) chain = (i % 2 == 0) ? new TreeNode(i, chain, null) : new TreeNode(i, null, chain);
        Check.eq(100000, inorder(chain).size(), "deep inorder");
        Check.eq(100000, postorderLast(chain).size(), "deep postorder");
        Check.eq(99999, (int) postorderLast(chain).get(99999), "deep postorder ends at root");
        TreeNode s = Nodes.tree(1, null, 2, 3);
        Check.eq(list(1, 3, 2), inorder(s), "LC 94 sample 1");
        Check.eq(list(1, 2, 3), preorder(s), "LC 144 sample 1");
        Check.eq(list(3, 2, 1), postorder(s), "LC 145 sample 1");
        Check.eq(list(3, 2, 1), postorderLast(s), "LC 145 sample 1 (last)");
        TreeNode s2 = Nodes.tree(1, 2, 3, 4, 5, null, 8, null, null, 6, 7, 9);
        Check.eq(list(4, 2, 6, 5, 7, 1, 3, 9, 8), inorder(s2), "LC 94 sample 2");
        Check.eq(list(1, 2, 4, 5, 6, 7, 3, 8, 9), preorder(s2), "LC 144 sample 2");
        Check.eq(list(4, 6, 7, 5, 2, 9, 8, 3, 1), postorder(s2), "LC 145 sample 2");
        Check.eq(list(4, 6, 7, 5, 2, 9, 8, 3, 1), postorderLast(s2), "LC 145 sample 2 (last)");
        Check.eq(list(), inorder(null), "empty");
        Check.done("P08TreeTraversal");
    }
}
