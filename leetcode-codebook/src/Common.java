import java.util.*;

// LeetCode's node classes (LeetCode supplies these, so the book never prints them).
class ListNode {
    int val;
    ListNode next;
    ListNode() {}
    ListNode(int val) { this.val = val; }
    ListNode(int val, ListNode next) { this.val = val; this.next = next; }
}

class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode() {}
    TreeNode(int val) { this.val = val; }
    TreeNode(int val, TreeNode left, TreeNode right) { this.val = val; this.left = left; this.right = right; }
}

// Tiny test kit shared by every DayNN.java file.
final class T {
    static int pass = 0, fail = 0;
    static final Random R = new Random(20261125);

    static String show(Object o) {
        if (o == null) return "null";
        if (o instanceof int[] a) return Arrays.toString(a);
        if (o instanceof long[] a) return Arrays.toString(a);
        if (o instanceof double[] a) return Arrays.toString(a);
        if (o instanceof boolean[] a) return Arrays.toString(a);
        if (o instanceof char[] a) return new String(a);
        if (o instanceof Object[] a) return Arrays.deepToString(a);
        return String.valueOf(o);
    }

    static void eq(String name, Object got, Object want) {
        String g = show(got), w = show(want);
        if (g.equals(w)) pass++;
        else { fail++; System.out.println("FAIL " + name + ": got " + g + "  want " + w); }
    }

    static void ok(String name, boolean cond) {
        if (cond) pass++;
        else { fail++; System.out.println("FAIL " + name); }
    }

    static void close(String name, double got, double want) {
        if (Math.abs(got - want) < 1e-5) pass++;
        else { fail++; System.out.println("FAIL " + name + ": got " + got + "  want " + want); }
    }

    // Order-free comparison for answers such as 3Sum, subsets, group anagrams.
    static <E> String canon(Collection<? extends Collection<E>> groups) {
        List<String> parts = new ArrayList<>();
        for (Collection<E> g : groups) {
            List<String> inner = new ArrayList<>();
            for (E e : g) inner.add(String.valueOf(e));
            Collections.sort(inner);
            parts.add(inner.toString());
        }
        Collections.sort(parts);
        return parts.toString();
    }

    static String sorted(int[] a) { int[] b = a.clone(); Arrays.sort(b); return Arrays.toString(b); }

    // "[1,2,3]" -> int[]
    static int[] ints(String s) {
        s = s.trim();
        s = s.substring(1, s.length() - 1).trim();
        if (s.isEmpty()) return new int[0];
        String[] p = s.split(",");
        int[] a = new int[p.length];
        for (int i = 0; i < p.length; i++) a[i] = Integer.parseInt(p[i].trim());
        return a;
    }

    // "[[1,2],[3,4]]" -> int[][]
    static int[][] grid(String s) {
        s = s.trim();
        s = s.substring(1, s.length() - 1).trim();
        List<int[]> rows = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            int open = s.indexOf('[', i);
            if (open < 0) break;
            int close = s.indexOf(']', open);
            rows.add(ints(s.substring(open, close + 1)));
            i = close + 1;
        }
        return rows.toArray(new int[0][]);
    }

    static char[][] chars(String... rows) {
        char[][] g = new char[rows.length][];
        for (int i = 0; i < rows.length; i++) g[i] = rows[i].toCharArray();
        return g;
    }

    static ListNode list(int... v) {
        ListNode dummy = new ListNode(0), t = dummy;
        for (int x : v) { t.next = new ListNode(x); t = t.next; }
        return dummy.next;
    }

    // List whose tail links back to index pos (pos = -1: no cycle).
    static ListNode cycle(int[] v, int pos) {
        ListNode head = list(v);
        if (pos < 0 || head == null) return head;
        ListNode tail = head, target = null;
        for (int i = 0; ; i++) {
            if (i == pos) target = tail;
            if (tail.next == null) break;
            tail = tail.next;
        }
        tail.next = target;
        return head;
    }

    static int[] arr(ListNode h) {
        List<Integer> out = new ArrayList<>();
        for (; h != null; h = h.next) out.add(h.val);
        return out.stream().mapToInt(Integer::intValue).toArray();
    }

    // Level-order with nulls, exactly like LeetCode's tree notation.
    static TreeNode tree(Integer... v) {
        if (v.length == 0 || v[0] == null) return null;
        TreeNode root = new TreeNode(v[0]);
        Deque<TreeNode> q = new ArrayDeque<>();
        q.add(root);
        int i = 1;
        while (!q.isEmpty() && i < v.length) {
            TreeNode n = q.poll();
            if (i < v.length && v[i] != null) { n.left = new TreeNode(v[i]); q.add(n.left); }
            i++;
            if (i < v.length && v[i] != null) { n.right = new TreeNode(v[i]); q.add(n.right); }
            i++;
        }
        return root;
    }

    static TreeNode find(TreeNode r, int val) {
        if (r == null) return null;
        if (r.val == val) return r;
        TreeNode l = find(r.left, val);
        return l != null ? l : find(r.right, val);
    }

    static int[] rand(int n, int lo, int hi) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = lo + R.nextInt(hi - lo + 1);
        return a;
    }

    static int rnd(int lo, int hi) { return lo + R.nextInt(hi - lo + 1); }

    // Random tree on n nodes as an edge list (node i > 0 hangs under a random earlier node).
    static int[][] randTreeEdges(int n) {
        int[][] e = new int[Math.max(0, n - 1)][];
        for (int i = 1; i < n; i++) {
            int p = R.nextInt(i);
            e[i - 1] = R.nextBoolean() ? new int[]{p, i} : new int[]{i, p};
        }
        return e;
    }

    static void summary(String who) {
        System.out.println(who + ": " + pass + " passed, " + fail + " failed");
        if (fail > 0) System.exit(1);
    }
}
