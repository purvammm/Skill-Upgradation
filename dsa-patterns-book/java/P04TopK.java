import java.util.*;

public class P04TopK {
    //@tpl stream
    // Kth largest in a stream: a MIN-heap holding the k largest values so far.
    // Its root is the smallest of them, which is the kth largest overall.
    static class KthLargest {
        private final PriorityQueue<Integer> heap = new PriorityQueue<>();
        private final int k;

        KthLargest(int k, int[] nums) {
            this.k = k;
            for (int x : nums) add(x);
        }

        int add(int x) {
            heap.offer(x);
            if (heap.size() > k) heap.poll();    // drop the smallest of k + 1
            return heap.peek();
        }
    }
    //@end

    //@tpl closest
    // K closest points to the origin: keep the k smallest distances, so use a
    // MAX-heap of size k; its root is the worst point kept, the one to evict.
    static int[][] kClosest(int[][] points, int k) {
        PriorityQueue<int[]> heap = new PriorityQueue<>(
            (p, q) -> Long.compare(dist(q), dist(p)));   // farthest on top
        for (int[] p : points) {
            heap.offer(p);
            if (heap.size() > k) heap.poll();
        }
        return heap.toArray(new int[0][]);               // any order is fine
    }

    static long dist(int[] p) { return (long) p[0] * p[0] + (long) p[1] * p[1]; }
    //@end

    //@tpl words
    // Top k frequent words, best first; equal counts in alphabetical order.
    // The root must be the WORST candidate: lowest count, and on a tie the
    // word that comes last alphabetically. Poll all, then reverse.
    static List<String> topKFrequent(String[] words, int k) {
        Map<String, Integer> cnt = new HashMap<>();
        for (String w : words) cnt.merge(w, 1, Integer::sum);
        PriorityQueue<String> heap = new PriorityQueue<>((a, b) -> {
            int ca = cnt.get(a), cb = cnt.get(b);
            return ca != cb ? Integer.compare(ca, cb) : b.compareTo(a);
        });
        for (String w : cnt.keySet()) {
            heap.offer(w);
            if (heap.size() > k) heap.poll();
        }
        List<String> out = new ArrayList<>();
        while (!heap.isEmpty()) out.add(heap.poll());    // worst to best
        Collections.reverse(out);
        return out;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static List<String> bruteWords(String[] words, int k) {
        final Map<String, Integer> cnt = new HashMap<>();
        for (String w : words) cnt.merge(w, 1, Integer::sum);
        List<String> keys = new ArrayList<>(cnt.keySet());
        Collections.sort(keys, (a, b) -> !cnt.get(a).equals(cnt.get(b)) ? cnt.get(b) - cnt.get(a) : a.compareTo(b));
        return new ArrayList<>(keys.subList(0, k));
    }

    public static void main(String[] args) {
        Random r = new Random(47);
        for (int t = 0; t < 2000; t++) {
            int k = 1 + r.nextInt(5);
            int n0 = k - 1 + r.nextInt(3);           // LeetCode: at least k - 1 initial values
            int[] init = Check.ints(r, n0, -20, 20);
            KthLargest kl = new KthLargest(k, init);
            List<Integer> all = new ArrayList<>();
            for (int x : init) all.add(x);
            for (int step = 0; step < 15; step++) {
                int x = r.nextInt(41) - 20;
                all.add(x);
                List<Integer> s = new ArrayList<>(all);
                Collections.sort(s, Collections.reverseOrder());
                Check.eq(s.get(k - 1), (Object) kl.add(x), "kth largest k=" + k + " " + all);
            }

            int n = 1 + r.nextInt(10);
            int[][] pts = new int[n][];
            for (int i = 0; i < n; i++) pts[i] = new int[] {r.nextInt(11) - 5, r.nextInt(11) - 5};
            int kk = 1 + r.nextInt(n);
            int[][] got = kClosest(pts, kk);
            long[] want = new long[n];
            for (int i = 0; i < n; i++) want[i] = dist(pts[i]);
            Arrays.sort(want);
            long[] gotD = new long[got.length];
            for (int i = 0; i < got.length; i++) gotD[i] = dist(got[i]);
            Arrays.sort(gotD);
            Check.eq(Arrays.copyOf(want, kk), gotD, "closest distances");
            Set<int[]> ids = Collections.newSetFromMap(new IdentityHashMap<int[], Boolean>());
            for (int[] p : got) ids.add(p);
            Check.eq(kk, ids.size(), "closest distinct points");

            int m = 1 + r.nextInt(12);
            String[] words = new String[m];
            for (int i = 0; i < m; i++) words[i] = Check.str(r, 1 + r.nextInt(2), "abc");
            int distinct = new HashSet<>(Arrays.asList(words)).size();
            int kw = 1 + r.nextInt(distinct);
            Check.eq(bruteWords(words, kw), topKFrequent(words, kw), "words " + Arrays.toString(words) + " k=" + kw);
        }
        KthLargest kl = new KthLargest(3, new int[] {4, 5, 8, 2});
        int[] adds = {3, 5, 10, 9, 4}, outs = {4, 5, 5, 8, 8};
        for (int i = 0; i < adds.length; i++) Check.eq(outs[i], kl.add(adds[i]), "LC 703 sample 1 step " + i);
        kl = new KthLargest(4, new int[] {7, 7, 7, 7, 8, 3});
        int[] adds2 = {2, 10, 9, 9}, outs2 = {7, 7, 7, 8};
        for (int i = 0; i < adds2.length; i++) Check.eq(outs2[i], kl.add(adds2[i]), "LC 703 sample 2 step " + i);
        Check.eq(new int[][] {{-2, 2}}, kClosest(new int[][] {{1, 3}, {-2, 2}}, 1), "LC 973 sample 1");
        int[][] two = kClosest(new int[][] {{3, 3}, {5, -1}, {-2, 4}}, 2);
        Arrays.sort(two, (p, q) -> p[0] - q[0]);
        Check.eq(new int[][] {{-2, 4}, {3, 3}}, two, "LC 973 sample 2");
        Check.eq(Arrays.asList("i", "love"),
                 topKFrequent(new String[] {"i", "love", "leetcode", "i", "love", "coding"}, 2), "LC 692 sample 1");
        Check.eq(Arrays.asList("the", "is", "sunny", "day"),
                 topKFrequent(new String[] {"the", "day", "is", "sunny", "the", "the", "the", "sunny", "is", "is"}, 4),
                 "LC 692 sample 2");
        Check.done("P04TopK");
    }
}
