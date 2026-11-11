import java.util.*;

public class P04KWayMerge {
    //@tpl lists
    // Merge k sorted linked lists. The heap holds the current head of each
    // list, so its root is the smallest value not yet written out.
    static ListNode mergeKLists(ListNode[] lists) {
        PriorityQueue<ListNode> heap =
            new PriorityQueue<>((x, y) -> Integer.compare(x.val, y.val));
        for (ListNode head : lists) if (head != null) heap.offer(head);
        ListNode dummy = new ListNode(0), tail = dummy;
        while (!heap.isEmpty()) {
            ListNode node = heap.poll();                 // smallest remaining
            tail.next = node;
            tail = node;
            if (node.next != null) heap.offer(node.next);    // same list, next
        }
        return dummy.next;
    }
    //@end

    //@tpl pairs
    // K pairs with the smallest sums (a and b sorted). Row i is the sorted
    // list a[i]+b[0], a[i]+b[1], ...; merge the rows, creating entries lazily.
    // Heap entry {i, j}; compare sums as long (values reach +-1e9).
    static List<List<Integer>> kSmallestPairs(int[] a, int[] b, int k) {
        PriorityQueue<int[]> heap = new PriorityQueue<>((x, y) ->
            Long.compare((long) a[x[0]] + b[x[1]], (long) a[y[0]] + b[y[1]]));
        for (int i = 0; i < Math.min(a.length, k); i++) heap.offer(new int[] {i, 0});
        List<List<Integer>> out = new ArrayList<>();
        while (out.size() < k && !heap.isEmpty()) {
            int[] e = heap.poll();
            int i = e[0], j = e[1];
            out.add(Arrays.asList(a[i], b[j]));
            if (j + 1 < b.length) heap.offer(new int[] {i, j + 1});
        }
        return out;
    }
    //@end

    //@tpl range
    // Smallest range that has an element from each of k sorted lists.
    // Heap entry {value, list, index}: one per list. The range is
    // [heap root, max]; only advancing the root's list can make it narrower.
    static int[] smallestRange(List<List<Integer>> lists) {
        PriorityQueue<int[]> heap =
            new PriorityQueue<>((x, y) -> Integer.compare(x[0], y[0]));
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < lists.size(); i++) {
            int v = lists.get(i).get(0);
            heap.offer(new int[] {v, i, 0});
            max = Math.max(max, v);
        }
        int[] best = {heap.peek()[0], max};
        while (true) {
            int[] e = heap.poll();                       // current minimum
            if (max - e[0] < best[1] - best[0]) best = new int[] {e[0], max};
            List<Integer> list = lists.get(e[1]);
            if (e[2] + 1 == list.size()) return best;    // that list is used up
            int v = list.get(e[2] + 1);
            heap.offer(new int[] {v, e[1], e[2] + 1});
            max = Math.max(max, v);
        }
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int[] bruteRange(List<List<Integer>> lists) {
        List<Integer> vals = new ArrayList<>();
        for (List<Integer> l : lists) vals.addAll(l);
        Collections.sort(vals);
        int[] best = null;
        for (int lo : vals)
            for (int hi : vals) {
                if (hi < lo) continue;
                boolean ok = true;
                for (List<Integer> l : lists) {
                    boolean has = false;
                    for (int x : l) if (lo <= x && x <= hi) has = true;
                    if (!has) { ok = false; break; }
                }
                if (!ok) continue;
                if (best == null || hi - lo < best[1] - best[0] || (hi - lo == best[1] - best[0] && lo < best[0]))
                    best = new int[] {lo, hi};
            }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(49);
        for (int t = 0; t < 2000; t++) {
            int k = r.nextInt(6);
            ListNode[] lists = new ListNode[k];
            List<Integer> all = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                int[] vals = Check.sortedInts(r, r.nextInt(6), -10, 10);
                for (int v : vals) all.add(v);
                lists[i] = Nodes.list(vals);
            }
            Collections.sort(all);
            int[] want = new int[all.size()];
            for (int i = 0; i < want.length; i++) want[i] = all.get(i);
            Check.eq(want, Nodes.toArray(mergeKLists(lists)), "merge k=" + k);

            boolean big = r.nextInt(4) == 0;
            int[] a = big ? Check.sortedInts(r, 1 + r.nextInt(6), -1_000_000_000, 1_000_000_000) : Check.sortedInts(r, 1 + r.nextInt(6), -5, 5);
            int[] b = big ? Check.sortedInts(r, 1 + r.nextInt(6), -1_000_000_000, 1_000_000_000) : Check.sortedInts(r, 1 + r.nextInt(6), -5, 5);
            int kp = 1 + r.nextInt(a.length * b.length + 2);
            List<Long> sums = new ArrayList<>();
            Map<String, Integer> pairCount = new HashMap<>();
            for (int x : a) for (int y : b) { sums.add((long) x + y); pairCount.merge(x + "," + y, 1, Integer::sum); }
            Collections.sort(sums);
            List<List<Integer>> got = kSmallestPairs(a, b, kp);
            Check.eq(Math.min(kp, a.length * b.length), got.size(), "pairs size");
            List<Long> gotSums = new ArrayList<>();
            for (List<Integer> p : got) {
                gotSums.add((long) p.get(0) + p.get(1));
                Integer c = pairCount.merge(p.get(0) + "," + p.get(1), -1, Integer::sum);
                Check.isTrue(c != null && c >= 0, "pair is real " + p);
            }
            Collections.sort(gotSums);
            Check.eq(sums.subList(0, got.size()), gotSums, "pair sums " + Arrays.toString(a) + Arrays.toString(b) + " k=" + kp);

            int kl = 1 + r.nextInt(4);
            List<List<Integer>> ls = new ArrayList<>();
            for (int i = 0; i < kl; i++) {
                List<Integer> l = new ArrayList<>();
                for (int v : Check.sortedInts(r, 1 + r.nextInt(5), -10, 10)) l.add(v);
                ls.add(l);
            }
            Check.eq(bruteRange(ls), smallestRange(ls), "range " + ls);
        }
        Check.eq(new int[] {1, 1, 2, 3, 4, 4, 5, 6},
                 Nodes.toArray(mergeKLists(new ListNode[] {Nodes.list(1, 4, 5), Nodes.list(1, 3, 4), Nodes.list(2, 6)})), "LC 23 sample 1");
        Check.isTrue(mergeKLists(new ListNode[0]) == null, "LC 23 sample 2");
        Check.isTrue(mergeKLists(new ListNode[] {null}) == null, "LC 23 sample 3");
        Check.eq("[[1, 2], [1, 4], [1, 6]]", kSmallestPairs(new int[] {1, 7, 11}, new int[] {2, 4, 6}, 3).toString(), "LC 373 sample 1");
        Check.eq("[[1, 1], [1, 1]]", kSmallestPairs(new int[] {1, 1, 2}, new int[] {1, 2, 3}, 2).toString(), "LC 373 sample 2");
        List<List<Integer>> s1 = Arrays.asList(Arrays.asList(4, 10, 15, 24, 26), Arrays.asList(0, 9, 12, 20), Arrays.asList(5, 18, 22, 30));
        Check.eq(new int[] {20, 24}, smallestRange(s1), "LC 632 sample 1");
        List<List<Integer>> s2 = Arrays.asList(Arrays.asList(1, 2, 3), Arrays.asList(1, 2, 3), Arrays.asList(1, 2, 3));
        Check.eq(new int[] {1, 1}, smallestRange(s2), "LC 632 sample 2");
        Check.done("P04KWayMerge");
    }
}
