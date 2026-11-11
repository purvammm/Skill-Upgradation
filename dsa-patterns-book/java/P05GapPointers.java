import java.util.*;

public class P05GapPointers {
    //@tpl nth
    // Remove the n-th node from the end (LC 19), one pass, 1 <= n <= length.
    static ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode dummy = new ListNode(0, head), fast = dummy, slow = dummy;
        for (int i = 0; i <= n; i++) fast = fast.next;   // 1. gap of n + 1 links
        while (fast != null) {                           // 2. keep the gap
            fast = fast.next;
            slow = slow.next;
        }
        slow.next = slow.next.next;                      // 3. slow is just before it
        return dummy.next;
    }
    //@end

    //@tpl rotate
    // Rotate right by k (LC 61): the last k % len nodes move to the front.
    static ListNode rotateRight(ListNode head, int k) {
        if (head == null) return null;
        int len = 0;
        for (ListNode p = head; p != null; p = p.next) len++;
        k %= len;
        if (k == 0) return head;
        ListNode fast = head, slow = head;
        for (int i = 0; i < k; i++) fast = fast.next;    // gap of k
        while (fast.next != null) { fast = fast.next; slow = slow.next; }
        ListNode newHead = slow.next;      // slow = new tail, fast = old tail
        slow.next = null;
        fast.next = head;
        return newHead;
    }
    //@end

    //@tpl intersect
    // First shared node of two lists, or null (LC 160). Each pointer walks its
    // own list, then the other one: both cover a + c + b and meet at the join.
    static ListNode getIntersectionNode(ListNode headA, ListNode headB) {
        ListNode p = headA, q = headB;
        while (p != q) {
            p = (p == null) ? headB : p.next;
            q = (q == null) ? headA : q.next;
        }
        return p;                                        // join, or null if none
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int[] removeAt(int[] a, int idx) {
        int[] b = new int[a.length - 1];
        for (int i = 0, k = 0; i < a.length; i++) if (i != idx) b[k++] = a[i];
        return b;
    }

    static int[] rotate(int[] a, int k) {
        int n = a.length;
        int[] b = new int[n];
        for (int i = 0; i < n; i++) b[(int) ((i + (long) k) % n)] = a[i];
        return b;
    }

    static ListNode bruteIntersect(ListNode a, ListNode b) {
        Set<ListNode> seen = Collections.newSetFromMap(new IdentityHashMap<ListNode, Boolean>());
        for (ListNode p = a; p != null; p = p.next) seen.add(p);
        for (ListNode q = b; q != null; q = q.next) if (seen.contains(q)) return q;
        return null;
    }

    static ListNode append(ListNode front, ListNode tail) {
        if (front == null) return tail;
        ListNode p = front;
        while (p.next != null) p = p.next;
        p.next = tail;
        return front;
    }

    public static void main(String[] args) {
        Random r = new Random(57);
        for (int t = 0; t < 5000; t++) {
            int len = 1 + r.nextInt(10);
            int[] a = Check.ints(r, len, 0, 9);
            int n = 1 + r.nextInt(len);
            Check.eq(removeAt(a, len - n), Nodes.toArray(removeNthFromEnd(Nodes.list(a), n)),
                    "nth " + Arrays.toString(a) + " n=" + n);
            int k = r.nextInt(25);
            Check.eq(rotate(a, k), Nodes.toArray(rotateRight(Nodes.list(a), k)), "rotate " + Arrays.toString(a) + " k=" + k);

            // two lists with a random (maybe empty) shared tail; values repeat on purpose
            ListNode shared = Nodes.list(Check.ints(r, r.nextInt(5), 0, 2));
            ListNode ha = append(Nodes.list(Check.ints(r, r.nextInt(5), 0, 2)), shared);
            ListNode hb = append(Nodes.list(Check.ints(r, r.nextInt(5), 0, 2)), shared);
            Check.isTrue(bruteIntersect(ha, hb) == getIntersectionNode(ha, hb), "intersect t=" + t);
        }
        Check.eq(new int[] {}, Nodes.toArray(rotateRight(null, 3)), "rotate empty");
        Check.eq(new int[] {1, 2, 3, 5}, Nodes.toArray(removeNthFromEnd(Nodes.list(1, 2, 3, 4, 5), 2)), "LC 19 sample 1");
        Check.eq(new int[] {}, Nodes.toArray(removeNthFromEnd(Nodes.list(1), 1)), "LC 19 sample 2");
        Check.eq(new int[] {1}, Nodes.toArray(removeNthFromEnd(Nodes.list(1, 2), 1)), "LC 19 sample 3");
        Check.eq(new int[] {4, 5, 1, 2, 3}, Nodes.toArray(rotateRight(Nodes.list(1, 2, 3, 4, 5), 2)), "LC 61 sample 1");
        Check.eq(new int[] {2, 0, 1}, Nodes.toArray(rotateRight(Nodes.list(0, 1, 2), 4)), "LC 61 sample 2");
        Check.eq(new int[] {1, 2}, Nodes.toArray(rotateRight(Nodes.list(1, 2), 2000000000)), "LC 61 large k");
        ListNode common = Nodes.list(8, 4, 5);
        ListNode la = append(Nodes.list(4, 1), common), lb = append(Nodes.list(5, 6, 1), common);
        Check.isTrue(getIntersectionNode(la, lb) == common, "LC 160 sample 1");
        Check.isTrue(getIntersectionNode(Nodes.list(2, 6, 4), Nodes.list(1, 5)) == null, "LC 160 sample 3");
        Check.done("P05GapPointers");
    }
}
