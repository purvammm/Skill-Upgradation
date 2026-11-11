import java.util.*;

public class P05ListReverse {
    //@tpl reverse
    // Reverse a whole list (LC 206).
    // Invariant: prev = reversed part, cur = first node not yet reversed.
    static ListNode reverseList(ListNode head) {
        ListNode prev = null, cur = head;
        while (cur != null) {
            ListNode next = cur.next;   // 1. save the rest
            cur.next = prev;            // 2. turn the arrow
            prev = cur;                 // 3. step both forward
            cur = next;
        }
        return prev;
    }
    //@end

    //@tpl recursive
    // Same, recursively: reverse the tail, then hang head after its old next.
    static ListNode reverseRec(ListNode head) {
        if (head == null || head.next == null) return head;
        ListNode newHead = reverseRec(head.next);
        head.next.next = head;          // old next is now the tail: append head
        head.next = null;
        return newHead;
    }
    //@end

    //@tpl segment
    // Reverse the k nodes after pre and reconnect both ends.
    // Returns the segment's new last node: the pre for the next segment.
    static ListNode reverseAfter(ListNode pre, int k) {
        ListNode first = pre.next, prev = null, cur = first;
        for (int i = 0; i < k; i++) {
            ListNode next = cur.next;
            cur.next = prev;
            prev = cur;
            cur = next;
        }
        pre.next = prev;                // pre -> old k-th node
        first.next = cur;               // old first -> node after the segment
        return first;
    }

    // Reverse positions left..right, 1-based (LC 92).
    static ListNode reverseBetween(ListNode head, int left, int right) {
        ListNode dummy = new ListNode(0, head), pre = dummy;
        for (int i = 1; i < left; i++) pre = pre.next;
        reverseAfter(pre, right - left + 1);
        return dummy.next;
    }

    // Reverse every full group of k; a short last group stays (LC 25).
    static ListNode reverseKGroup(ListNode head, int k) {
        ListNode dummy = new ListNode(0, head), pre = dummy;
        while (true) {
            ListNode probe = pre;
            for (int i = 0; i < k && probe != null; i++) probe = probe.next;
            if (probe == null) break;   // fewer than k nodes left
            pre = reverseAfter(pre, k);
        }
        return dummy.next;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int[] rev(int[] a, int i, int j) {          // reverse a[i..j] of a copy
        int[] b = a.clone();
        for (; i < j; i++, j--) { int t = b[i]; b[i] = b[j]; b[j] = t; }
        return b;
    }

    static int[] bruteK(int[] a, int k) {
        int[] b = a.clone();
        for (int s = 0; s + k <= b.length; s += k) b = rev(b, s, s + k - 1);
        return b;
    }

    public static void main(String[] args) {
        Random r = new Random(53);
        for (int t = 0; t < 5000; t++) {
            int n = r.nextInt(12);
            int[] a = Check.ints(r, n, 0, 9);
            Check.eq(rev(a, 0, n - 1), Nodes.toArray(reverseList(Nodes.list(a))), "reverse " + Arrays.toString(a));
            Check.eq(rev(a, 0, n - 1), Nodes.toArray(reverseRec(Nodes.list(a))), "reverseRec " + Arrays.toString(a));
            if (n >= 1) {
                int left = 1 + r.nextInt(n), right = left + r.nextInt(n - left + 1);
                Check.eq(rev(a, left - 1, right - 1), Nodes.toArray(reverseBetween(Nodes.list(a), left, right)),
                        "between " + Arrays.toString(a) + " " + left + ".." + right);
                int k = 1 + r.nextInt(n);
                Check.eq(bruteK(a, k), Nodes.toArray(reverseKGroup(Nodes.list(a), k)),
                        "kgroup " + Arrays.toString(a) + " k=" + k);
            }
        }
        int[] big = new int[100000];
        for (int i = 0; i < big.length; i++) big[i] = i;
        Check.eq(rev(big, 0, big.length - 1), Nodes.toArray(reverseList(Nodes.list(big))), "long list");
        Check.eq(new int[] {5, 4, 3, 2, 1}, Nodes.toArray(reverseList(Nodes.list(1, 2, 3, 4, 5))), "LC 206 sample 1");
        Check.eq(new int[] {2, 1}, Nodes.toArray(reverseRec(Nodes.list(1, 2))), "LC 206 sample 2");
        Check.eq(new int[] {}, Nodes.toArray(reverseList(null)), "LC 206 sample 3");
        Check.eq(new int[] {1, 4, 3, 2, 5},
                Nodes.toArray(reverseBetween(Nodes.list(1, 2, 3, 4, 5), 2, 4)), "LC 92 sample 1");
        Check.eq(new int[] {5}, Nodes.toArray(reverseBetween(Nodes.list(5), 1, 1)), "LC 92 sample 2");
        Check.eq(new int[] {2, 1, 4, 3, 5},
                Nodes.toArray(reverseKGroup(Nodes.list(1, 2, 3, 4, 5), 2)), "LC 25 sample 1");
        Check.eq(new int[] {3, 2, 1, 4, 5},
                Nodes.toArray(reverseKGroup(Nodes.list(1, 2, 3, 4, 5), 3)), "LC 25 sample 2");
        Check.done("P05ListReverse");
    }
}
