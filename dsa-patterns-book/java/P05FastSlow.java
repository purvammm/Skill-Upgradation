import java.util.*;

public class P05FastSlow {
    //@tpl floyd
    // Middle node (LC 876). Even length: the second of the two middles.
    static ListNode middleNode(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }

    // First node of the cycle, or null (LC 142). Phase 1 alone answers LC 141.
    static ListNode detectCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {               // 1. they meet inside the cycle
                ListNode p = head;            // 2. head and meeting point are the
                while (p != slow) {           //    same distance from the start
                    p = p.next;
                    slow = slow.next;
                }
                return p;
            }
        }
        return null;                          // fast fell off the end: no cycle
    }
    //@end

    //@tpl halves
    // Palindrome list (LC 234): reverse the back half, walk both halves.
    static boolean isPalindrome(ListNode head) {
        ListNode q = reverse(middleNode(head));
        for (ListNode p = head; q != null; p = p.next, q = q.next)
            if (p.val != q.val) return false;
        return true;
    }

    // Reorder to L0, Ln, L1, Ln-1, ... (LC 143): cut, reverse the back, zip.
    static void reorderList(ListNode head) {
        if (head == null) return;
        ListNode mid = middleNode(head);
        ListNode q = reverse(mid.next);           // back half, reversed
        mid.next = null;                          // front half ends at mid
        for (ListNode p = head; q != null; ) {
            ListNode pNext = p.next, qNext = q.next;
            p.next = q; q.next = pNext;           // p -> q -> old p.next
            p = pNext; q = qNext;
        }
    }
    //@end

    static ListNode reverse(ListNode cur) {       // reverseList from 5.2
        ListNode prev = null;
        while (cur != null) {
            ListNode next = cur.next;
            cur.next = prev;
            prev = cur;
            cur = next;
        }
        return prev;
    }

    //@tpl function
    // LC 287: walk i -> a[i] from index 0; the cycle starts at the duplicate value.
    static int findDuplicate(int[] a) {
        int slow = 0, fast = 0;
        do { slow = a[slow]; fast = a[a[fast]]; } while (slow != fast);
        int p = 0;
        while (p != slow) { p = a[p]; slow = a[slow]; }
        return p;
    }

    // Happy number (LC 202): f(n) = sum of squared digits; happy if the loop is {1}.
    static boolean isHappy(int n) {
        int slow = n, fast = n;
        do { slow = f(slow); fast = f(f(fast)); } while (slow != fast);
        return slow == 1;
    }

    static int f(int n) {
        int s = 0;
        for (; n > 0; n /= 10) s += (n % 10) * (n % 10);
        return s;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static ListNode[] build(int[] a, int pos) {         // nodes; tail -> nodes[pos] if pos >= 0
        ListNode[] nodes = new ListNode[a.length];
        for (int i = a.length - 1; i >= 0; i--) nodes[i] = new ListNode(a[i], i + 1 < a.length ? nodes[i + 1] : null);
        if (pos >= 0) nodes[a.length - 1].next = nodes[pos];
        return nodes;
    }

    static int[] bruteReorder(int[] a) {
        int[] out = new int[a.length];
        int i = 0, j = a.length - 1, k = 0;
        while (i <= j) {
            out[k++] = a[i++];
            if (i <= j) out[k++] = a[j--];
        }
        return out;
    }

    static boolean brutePal(int[] a) {
        for (int i = 0, j = a.length - 1; i < j; i++, j--) if (a[i] != a[j]) return false;
        return true;
    }

    static boolean bruteHappy(int n) {
        Set<Integer> seen = new HashSet<>();
        while (n != 1 && seen.add(n)) {
            int s = 0;
            for (char ch : String.valueOf(n).toCharArray()) s += (ch - '0') * (ch - '0');
            n = s;
        }
        return n == 1;
    }

    public static void main(String[] args) {
        Random r = new Random(55);
        for (int t = 0; t < 5000; t++) {
            int n = r.nextInt(12);
            int[] a = Check.ints(r, n, 0, 2);
            ListNode[] nodes = build(a, -1);
            ListNode head = n == 0 ? null : nodes[0];
            Check.isTrue(n == 0 ? middleNode(head) == null : middleNode(head) == nodes[n / 2], "middle n=" + n);
            Check.isTrue(detectCycle(head) == null, "no cycle n=" + n);
            if (n > 0) {
                int pos = r.nextInt(n);
                ListNode[] cyc = build(a, pos);
                Check.isTrue(detectCycle(cyc[0]) == cyc[pos], "cycle start n=" + n + " pos=" + pos);
            }
            Check.eq(brutePal(a), isPalindrome(Nodes.list(a)), "palindrome " + Arrays.toString(a));
            int[] b = Check.ints(r, n, 0, 9);
            ListNode hb = Nodes.list(b);
            reorderList(hb);
            Check.eq(bruteReorder(b), Nodes.toArray(hb), "reorder " + Arrays.toString(b));

            // findDuplicate: n+1 values in [1, m], only one value repeats (maybe many times)
            int m = 1 + r.nextInt(10);
            List<Integer> vals = new ArrayList<>();
            for (int v = 1; v <= m; v++) vals.add(v);
            Collections.shuffle(vals, r);
            int dup = vals.get(0);
            int copies = 2 + r.nextInt(m);                   // dup appears 'copies' times
            List<Integer> arr = new ArrayList<>();
            for (int i = 0; i < copies; i++) arr.add(dup);
            for (int i = 1; arr.size() < m + 1; i++) arr.add(vals.get(i));
            Collections.shuffle(arr, r);
            int[] d = new int[m + 1];
            for (int i = 0; i <= m; i++) d[i] = arr.get(i);
            Check.eq(dup, findDuplicate(d), "duplicate " + Arrays.toString(d));
        }
        for (int x = 1; x <= 20000; x++) Check.eq(bruteHappy(x), isHappy(x), "happy " + x);
        Check.eq(Integer.MAX_VALUE == 2147483647, true, "sanity");
        Check.eq(bruteHappy(Integer.MAX_VALUE), isHappy(Integer.MAX_VALUE), "happy max int");
        Check.eq(new int[] {3, 4, 5}, Nodes.toArray(middleNode(Nodes.list(1, 2, 3, 4, 5))), "LC 876 sample 1");
        Check.eq(new int[] {4, 5, 6}, Nodes.toArray(middleNode(Nodes.list(1, 2, 3, 4, 5, 6))), "LC 876 sample 2");
        ListNode[] s = build(new int[] {3, 2, 0, -4}, 1);
        Check.isTrue(detectCycle(s[0]) == s[1], "LC 142 sample 1");
        ListNode[] s2 = build(new int[] {1, 2}, 0);
        Check.isTrue(detectCycle(s2[0]) == s2[0], "LC 142 sample 2");
        Check.isTrue(detectCycle(Nodes.list(1)) == null, "LC 142 sample 3");
        Check.eq(true, isPalindrome(Nodes.list(1, 2, 2, 1)), "LC 234 sample 1");
        Check.eq(false, isPalindrome(Nodes.list(1, 2)), "LC 234 sample 2");
        ListNode h1 = Nodes.list(1, 2, 3, 4);
        reorderList(h1);
        Check.eq(new int[] {1, 4, 2, 3}, Nodes.toArray(h1), "LC 143 sample 1");
        ListNode h2 = Nodes.list(1, 2, 3, 4, 5);
        reorderList(h2);
        Check.eq(new int[] {1, 5, 2, 4, 3}, Nodes.toArray(h2), "LC 143 sample 2");
        Check.eq(2, findDuplicate(new int[] {1, 3, 4, 2, 2}), "LC 287 sample 1");
        Check.eq(3, findDuplicate(new int[] {3, 1, 3, 4, 2}), "LC 287 sample 2");
        Check.eq(3, findDuplicate(new int[] {3, 3, 3, 3, 3}), "LC 287 sample 3");
        Check.eq(true, isHappy(19), "LC 202 sample 1");
        Check.eq(false, isHappy(2), "LC 202 sample 2");
        Check.done("P05FastSlow");
    }
}
