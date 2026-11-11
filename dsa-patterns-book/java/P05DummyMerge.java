import java.util.*;

public class P05DummyMerge {
    //@tpl merge
    // Merge two sorted lists (LC 21). With a dummy, "append" never needs an if.
    static ListNode mergeTwoLists(ListNode a, ListNode b) {
        ListNode dummy = new ListNode(0), tail = dummy;
        while (a != null && b != null) {
            if (a.val <= b.val) { tail.next = a; a = a.next; }
            else { tail.next = b; b = b.next; }
            tail = tail.next;
        }
        tail.next = (a != null) ? a : b;      // link the rest of the other list whole
        return dummy.next;
    }
    //@end

    //@tpl remove
    // Remove every node with this val (LC 203). The head itself may go.
    static ListNode removeElements(ListNode head, int val) {
        ListNode dummy = new ListNode(0, head), prev = dummy;
        while (prev.next != null) {
            if (prev.next.val == val) prev.next = prev.next.next;  // stay on prev
            else prev = prev.next;
        }
        return dummy.next;
    }

    // Partition (LC 86): nodes < x first; both groups keep their order.
    static ListNode partition(ListNode head, int x) {
        ListNode lessHead = new ListNode(0), less = lessHead;
        ListNode moreHead = new ListNode(0), more = moreHead;
        for (ListNode p = head; p != null; p = p.next) {
            if (p.val < x) { less.next = p; less = p; }
            else { more.next = p; more = p; }
        }
        more.next = null;                     // cut the old link, or you make a cycle
        less.next = moreHead.next;
        return lessHead.next;
    }
    //@end

    //@tpl add
    // Add two numbers stored least significant digit first (LC 2).
    static ListNode addTwoNumbers(ListNode a, ListNode b) {
        ListNode dummy = new ListNode(0), tail = dummy;
        int carry = 0;
        while (a != null || b != null || carry != 0) {   // carry can add a digit
            int s = carry;
            if (a != null) { s += a.val; a = a.next; }
            if (b != null) { s += b.val; b = b.next; }
            tail.next = new ListNode(s % 10);
            tail = tail.next;
            carry = s / 10;
        }
        return dummy.next;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int[] filter(int[] a, int val) {
        int k = 0;
        int[] out = new int[a.length];
        for (int x : a) if (x != val) out[k++] = x;
        return Arrays.copyOf(out, k);
    }

    static int[] stablePartition(int[] a, int x) {
        int[] out = new int[a.length];
        int k = 0;
        for (int v : a) if (v < x) out[k++] = v;
        for (int v : a) if (v >= x) out[k++] = v;
        return out;
    }

    static int[] randomNumber(Random r) {        // digits, least significant first
        int len = 1 + r.nextInt(8);
        int[] d = Check.ints(r, len, 0, 9);
        if (len > 1 && d[len - 1] == 0) d[len - 1] = 1 + r.nextInt(9);
        return d;
    }

    static long value(int[] d) {
        long v = 0;
        for (int i = d.length - 1; i >= 0; i--) v = v * 10 + d[i];
        return v;
    }

    static int[] digits(long v) {
        if (v == 0) return new int[] {0};
        List<Integer> out = new ArrayList<>();
        for (; v > 0; v /= 10) out.add((int) (v % 10));
        int[] d = new int[out.size()];
        for (int i = 0; i < d.length; i++) d[i] = out.get(i);
        return d;
    }

    public static void main(String[] args) {
        Random r = new Random(51);
        for (int t = 0; t < 5000; t++) {
            int[] a = Check.sortedInts(r, r.nextInt(8), -5, 5);
            int[] b = Check.sortedInts(r, r.nextInt(8), -5, 5);
            int[] want = Arrays.copyOf(a, a.length + b.length);
            System.arraycopy(b, 0, want, a.length, b.length);
            Arrays.sort(want);
            Check.eq(want, Nodes.toArray(mergeTwoLists(Nodes.list(a), Nodes.list(b))),
                    "merge " + Arrays.toString(a) + " " + Arrays.toString(b));

            int[] c = Check.ints(r, r.nextInt(10), 0, 3);
            int val = r.nextInt(4);
            Check.eq(filter(c, val), Nodes.toArray(removeElements(Nodes.list(c), val)),
                    "remove " + Arrays.toString(c) + " val=" + val);
            int x = r.nextInt(5);
            Check.eq(stablePartition(c, x), Nodes.toArray(partition(Nodes.list(c), x)),
                    "partition " + Arrays.toString(c) + " x=" + x);

            int[] p = randomNumber(r), q = randomNumber(r);
            Check.eq(digits(value(p) + value(q)), Nodes.toArray(addTwoNumbers(Nodes.list(p), Nodes.list(q))),
                    "add " + Arrays.toString(p) + " + " + Arrays.toString(q));
        }
        Check.eq(new int[] {1, 1, 2, 3, 4, 4},
                Nodes.toArray(mergeTwoLists(Nodes.list(1, 2, 4), Nodes.list(1, 3, 4))), "LC 21 sample 1");
        Check.eq(new int[] {}, Nodes.toArray(mergeTwoLists(null, null)), "LC 21 sample 2");
        Check.eq(new int[] {0}, Nodes.toArray(mergeTwoLists(null, Nodes.list(0))), "LC 21 sample 3");
        Check.eq(new int[] {1, 2, 3, 4, 5},
                Nodes.toArray(removeElements(Nodes.list(1, 2, 6, 3, 4, 5, 6), 6)), "LC 203 sample 1");
        Check.eq(new int[] {}, Nodes.toArray(removeElements(Nodes.list(7, 7, 7, 7), 7)), "LC 203 sample 3");
        Check.eq(new int[] {1, 2, 2, 4, 3, 5},
                Nodes.toArray(partition(Nodes.list(1, 4, 3, 2, 5, 2), 3)), "LC 86 sample 1");
        Check.eq(new int[] {1, 2}, Nodes.toArray(partition(Nodes.list(2, 1), 2)), "LC 86 sample 2");
        Check.eq(new int[] {7, 0, 8},
                Nodes.toArray(addTwoNumbers(Nodes.list(2, 4, 3), Nodes.list(5, 6, 4))), "LC 2 sample 1");
        Check.eq(new int[] {8, 9, 9, 9, 0, 0, 0, 1},
                Nodes.toArray(addTwoNumbers(Nodes.list(9, 9, 9, 9, 9, 9, 9), Nodes.list(9, 9, 9, 9))),
                "LC 2 sample 3");
        Check.done("P05DummyMerge");
    }
}
