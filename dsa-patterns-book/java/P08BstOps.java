import java.util.*;

public class P08BstOps {
    //@tpl ops
    // Search and insert follow one root-to-leaf path: O(h).
    static TreeNode search(TreeNode t, int key) {
        while (t != null && t.val != key) t = key < t.val ? t.left : t.right;
        return t;
    }

    static TreeNode insert(TreeNode t, int key) {
        if (t == null) return new TreeNode(key);
        if (key < t.val) t.left = insert(t.left, key);
        else if (key > t.val) t.right = insert(t.right, key);
        return t;                                  // key already there: no change
    }
    //@end

    //@tpl delete
    // Delete: find the key, then lift a child up or copy in the successor.
    static TreeNode delete(TreeNode t, int key) {
        if (t == null) return null;                // key not in the tree
        if (key < t.val) t.left = delete(t.left, key);
        else if (key > t.val) t.right = delete(t.right, key);
        else {
            if (t.left == null) return t.right;    // 0 or 1 child: lift it up
            if (t.right == null) return t.left;
            TreeNode s = t.right;                  // 2 children: the successor
            while (s.left != null) s = s.left;     // is leftmost on the right
            t.val = s.val;                         // copy it here, then
            t.right = delete(t.right, s.val);      // delete it from the right
        }
        return t;
    }
    //@end

    //@tpl validate
    // Validate: every node must lie strictly inside the range its ancestors
    // allow. long bounds, so a node equal to Integer.MIN_VALUE still fits.
    static boolean isValidBST(TreeNode root) {
        return valid(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    static boolean valid(TreeNode t, long lo, long hi) {
        if (t == null) return true;
        if (t.val <= lo || t.val >= hi) return false;
        return valid(t.left, lo, t.val) && valid(t.right, t.val, hi);
    }

    // Kth smallest: inorder visits values in increasing order; stop at pop k.
    static int kthSmallest(TreeNode root, int k) {
        Deque<TreeNode> st = new ArrayDeque<>();
        TreeNode cur = root;
        while (true) {
            while (cur != null) { st.push(cur); cur = cur.left; }
            cur = st.pop();
            if (--k == 0) return cur.val;
            cur = cur.right;
        }
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // BST iterator, described as a variant in the text: the inorder loop,
    // paused after each pop. O(h) memory, O(1) amortised per next().
    static class BSTIterator {
        private final Deque<TreeNode> st = new ArrayDeque<>();

        BSTIterator(TreeNode root) { pushLeft(root); }

        int next() {
            TreeNode t = st.pop();
            pushLeft(t.right);                     // t's successors come next
            return t.val;
        }

        boolean hasNext() { return !st.isEmpty(); }

        private void pushLeft(TreeNode t) {
            for (; t != null; t = t.left) st.push(t);
        }
    }

    static boolean bruteValid(TreeNode root) {
        List<Integer> in = new ArrayList<>();
        Nodes.inorder(root, in);
        for (int i = 1; i < in.size(); i++) if (in.get(i - 1) >= in.get(i)) return false;
        return true;
    }

    static int[] distinctSorted(Random r, int n, int range) {
        TreeSet<Integer> s = new TreeSet<>();
        while (s.size() < n) s.add(r.nextInt(range) - range / 2);
        int[] a = new int[n];
        int i = 0;
        for (int x : s) a[i++] = x;
        return a;
    }

    static List<Integer> inorder(TreeNode t) {
        List<Integer> out = new ArrayList<>();
        Nodes.inorder(t, out);
        return out;
    }

    public static void main(String[] args) {
        Random r = new Random(86);
        for (int t = 0; t < 2000; t++) {
            int n = r.nextInt(20);
            int[] vals = distinctSorted(r, n, 60);
            TreeNode root = Nodes.randomBst(r, vals, 0, n - 1);
            TreeSet<Integer> set = new TreeSet<>();
            for (int v : vals) set.add(v);
            for (int op = 0; op < 30; op++) {
                int key = r.nextInt(60) - 30;
                int kind = r.nextInt(3);
                if (kind == 0) {
                    TreeNode f = search(root, key);
                    Check.eq(set.contains(key), f != null && f.val == key, "search " + key);
                } else if (kind == 1) {
                    root = insert(root, key);
                    set.add(key);
                } else {
                    root = delete(root, key);
                    set.remove(key);
                }
                Check.eq(new ArrayList<>(set), inorder(root), "inorder after op " + kind + " " + key);
                Check.isTrue(isValidBST(root), "still a BST");
            }
            if (!set.isEmpty()) {
                List<Integer> sorted = new ArrayList<>(set);
                int k = 1 + r.nextInt(sorted.size());
                Check.eq((int) sorted.get(k - 1), kthSmallest(root, k), "kth " + k);
            }
            BSTIterator it = new BSTIterator(root);
            List<Integer> got = new ArrayList<>();
            while (it.hasNext()) got.add(it.next());
            Check.eq(new ArrayList<>(set), got, "iterator");
            // validate on arbitrary trees, with repeated values and extremes
            TreeNode any = Nodes.randomTree(r, r.nextInt(8), 0, 6);
            if (r.nextInt(4) == 0) {
                int[] big = distinctSorted(r, 1 + r.nextInt(5), 20);
                for (int i = 0; i < big.length; i++) big[i] = big[i] < 0 ? Integer.MIN_VALUE + (big[i] + 10) : Integer.MAX_VALUE - (10 - big[i]);
                Arrays.sort(big);
                any = Nodes.randomBst(r, big, 0, big.length - 1);
            }
            Check.eq(bruteValid(any), isValidBST(any), "valid " + Nodes.shape(any));
        }
        Check.eq("(2 (1 # #) (3 # #))", Nodes.shape(search(Nodes.tree(4, 2, 7, 1, 3), 2)), "LC 700 sample 1");
        Check.isTrue(search(Nodes.tree(4, 2, 7, 1, 3), 5) == null, "LC 700 sample 2");
        Check.eq(Arrays.asList(1, 2, 3, 4, 5, 7), inorder(insert(Nodes.tree(4, 2, 7, 1, 3), 5)), "LC 701 sample 1");
        Check.eq(Nodes.shape(Nodes.tree(5, 4, 6, 2, null, null, 7)), Nodes.shape(delete(Nodes.tree(5, 3, 6, 2, 4, null, 7), 3)), "LC 450 sample 1");
        Check.eq(Nodes.shape(Nodes.tree(5, 3, 6, 2, 4, null, 7)), Nodes.shape(delete(Nodes.tree(5, 3, 6, 2, 4, null, 7), 0)), "LC 450 sample 2");
        Check.isTrue(delete(null, 0) == null, "LC 450 sample 3");
        Check.eq(true, isValidBST(Nodes.tree(2, 1, 3)), "LC 98 sample 1");
        Check.eq(false, isValidBST(Nodes.tree(5, 1, 4, null, null, 3, 6)), "LC 98 sample 2");
        Check.eq(false, isValidBST(Nodes.tree(5, 4, 6, null, null, 3, 7)), "grandchild breaks the bound");
        Check.eq(true, isValidBST(Nodes.tree(Integer.MIN_VALUE)), "MIN_VALUE root");
        Check.eq(true, isValidBST(Nodes.tree(Integer.MAX_VALUE)), "MAX_VALUE root");
        Check.eq(1, kthSmallest(Nodes.tree(3, 1, 4, null, 2), 1), "LC 230 sample 1");
        Check.eq(3, kthSmallest(Nodes.tree(5, 3, 6, 2, 4, null, null, 1), 3), "LC 230 sample 2");
        BSTIterator it = new BSTIterator(Nodes.tree(7, 3, 15, null, null, 9, 20));
        Check.eq(3, it.next(), "LC 173 next");
        Check.eq(7, it.next(), "LC 173 next");
        Check.eq(true, it.hasNext(), "LC 173 hasNext");
        Check.eq(9, it.next(), "LC 173 next");
        Check.eq(15, it.next(), "LC 173 next");
        Check.eq(20, it.next(), "LC 173 next");
        Check.eq(false, it.hasNext(), "LC 173 hasNext end");
        Check.done("P08BstOps");
    }
}
