import java.util.*;

/** The node classes LeetCode gives you. Shared by the linked list and tree patterns. */
//@tpl nodes
class ListNode {
    int val;
    ListNode next;
    ListNode(int val) { this.val = val; }
    ListNode(int val, ListNode next) { this.val = val; this.next = next; }
}

class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
    TreeNode(int val, TreeNode left, TreeNode right) {
        this.val = val; this.left = left; this.right = right;
    }
}
//@end

/** Helpers for tests only (not printed). */
final class Nodes {
    private Nodes() {}

    static ListNode list(int... vals) {
        ListNode dummy = new ListNode(0), t = dummy;
        for (int v : vals) { t.next = new ListNode(v); t = t.next; }
        return dummy.next;
    }

    static int[] toArray(ListNode h) {
        List<Integer> out = new ArrayList<>();
        for (int guard = 0; h != null && guard < 1_000_000; h = h.next, guard++) out.add(h.val);
        int[] a = new int[out.size()];
        for (int i = 0; i < a.length; i++) a[i] = out.get(i);
        return a;
    }

    /** Builds a tree from LeetCode's level-order form; null means "no node". */
    static TreeNode tree(Integer... level) {
        if (level.length == 0 || level[0] == null) return null;
        TreeNode root = new TreeNode(level[0]);
        Deque<TreeNode> q = new ArrayDeque<>();
        q.add(root);
        int i = 1;
        while (!q.isEmpty() && i < level.length) {
            TreeNode cur = q.poll();
            if (i < level.length && level[i] != null) { cur.left = new TreeNode(level[i]); q.add(cur.left); }
            i++;
            if (i < level.length && level[i] != null) { cur.right = new TreeNode(level[i]); q.add(cur.right); }
            i++;
        }
        return root;
    }

    /** Random binary tree with n nodes and values in [lo, hi]. */
    static TreeNode randomTree(Random r, int n, int lo, int hi) {
        if (n == 0) return null;
        int leftSize = r.nextInt(n);
        TreeNode t = new TreeNode(lo + r.nextInt(hi - lo + 1));
        t.left = randomTree(r, leftSize, lo, hi);
        t.right = randomTree(r, n - 1 - leftSize, lo, hi);
        return t;
    }

    /** Random BST holding the given distinct sorted values. */
    static TreeNode randomBst(Random r, int[] sorted, int lo, int hi) {
        if (lo > hi) return null;
        int mid = lo + r.nextInt(hi - lo + 1);
        TreeNode t = new TreeNode(sorted[mid]);
        t.left = randomBst(r, sorted, lo, mid - 1);
        t.right = randomBst(r, sorted, mid + 1, hi);
        return t;
    }

    static void inorder(TreeNode t, List<Integer> out) {
        if (t == null) return;
        inorder(t.left, out);
        out.add(t.val);
        inorder(t.right, out);
    }

    static String shape(TreeNode t) {
        if (t == null) return "#";
        return "(" + t.val + " " + shape(t.left) + " " + shape(t.right) + ")";
    }

    static int size(TreeNode t) { return t == null ? 0 : 1 + size(t.left) + size(t.right); }
}
