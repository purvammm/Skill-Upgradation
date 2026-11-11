import java.util.*;

public class P04TwoHeaps {
    //@tpl median
    // Running median. low: max-heap, smaller half. high: min-heap, larger half.
    // Invariant: every value in low <= every value in high, and
    // low.size() == high.size() or low.size() == high.size() + 1.
    static class MedianFinder {
        private final PriorityQueue<Integer> low =
            new PriorityQueue<>(Collections.reverseOrder());
        private final PriorityQueue<Integer> high = new PriorityQueue<>();

        void addNum(int x) {
            low.offer(x);                        // 1. x joins the low half
            high.offer(low.poll());              // 2. low's largest moves up
            if (high.size() > low.size())        // 3. low may be one bigger,
                low.offer(high.poll());          //    never smaller
        }

        double findMedian() {
            if (low.size() > high.size()) return low.peek();
            return ((long) low.peek() + high.peek()) / 2.0;
        }
    }
    //@end

    //@tpl window
    // Sliding window median. A PriorityQueue cannot delete in O(log k), so use
    // two TreeSets of INDICES ordered by value, then index (keeps duplicates).
    static double[] medianSlidingWindow(int[] a, int k) {
        Comparator<Integer> byValue = (i, j) ->
            a[i] != a[j] ? Integer.compare(a[i], a[j]) : Integer.compare(i, j);
        TreeSet<Integer> low = new TreeSet<>(byValue), high = new TreeSet<>(byValue);
        double[] out = new double[a.length - k + 1];
        for (int i = 0; i < a.length; i++) {
            low.add(i);                                   // the same 3 steps
            high.add(low.pollLast());
            if (high.size() > low.size()) low.add(high.pollFirst());
            if (i < k - 1) continue;
            out[i - k + 1] = k % 2 == 1 ? a[low.last()]
                           : ((long) a[low.last()] + a[high.first()]) / 2.0;
            int old = i - k + 1;                          // leaves the window
            if (!low.remove(old)) high.remove(old);       // next add rebalances
        }
        return out;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static double median(List<Integer> xs) {
        List<Integer> s = new ArrayList<>(xs);
        Collections.sort(s);
        int n = s.size();
        return n % 2 == 1 ? s.get(n / 2) : ((long) s.get(n / 2 - 1) + s.get(n / 2)) / 2.0;
    }

    public static void main(String[] args) {
        Random r = new Random(48);
        for (int t = 0; t < 1500; t++) {
            MedianFinder mf = new MedianFinder();
            List<Integer> seen = new ArrayList<>();
            boolean huge = r.nextInt(4) == 0;
            int steps = 1 + r.nextInt(25);
            for (int s = 0; s < steps; s++) {
                int x = huge ? (r.nextBoolean() ? Integer.MAX_VALUE - r.nextInt(3) : Integer.MIN_VALUE + r.nextInt(3))
                             : r.nextInt(21) - 10;
                mf.addNum(x);
                seen.add(x);
                Check.eq(median(seen), mf.findMedian(), 1e-9, "median " + seen);
            }

            int n = 1 + r.nextInt(14);
            int[] a = huge ? new int[n] : Check.ints(r, n, -5, 5);
            if (huge) for (int i = 0; i < n; i++) a[i] = r.nextBoolean() ? Integer.MAX_VALUE : Integer.MIN_VALUE + r.nextInt(2);
            int k = 1 + r.nextInt(n);
            double[] got = medianSlidingWindow(a, k);
            Check.eq(n - k + 1, got.length, "window count");
            for (int i = 0; i + k <= n; i++) {
                List<Integer> w = new ArrayList<>();
                for (int j = i; j < i + k; j++) w.add(a[j]);
                Check.eq(median(w), got[i], 1e-9, "window " + Arrays.toString(a) + " k=" + k + " at " + i);
            }
        }
        MedianFinder mf = new MedianFinder();
        mf.addNum(1);
        mf.addNum(2);
        Check.eq(1.5, mf.findMedian(), 1e-9, "LC 295 sample");
        mf.addNum(3);
        Check.eq(2.0, mf.findMedian(), 1e-9, "LC 295 sample");
        Check.eq(new double[] {1, -1, -1, 3, 5, 6}, medianSlidingWindow(new int[] {1, 3, -1, -3, 5, 3, 6, 7}, 3), "LC 480 sample 1");
        Check.eq(new double[] {2, 3, 3, 3, 2, 3, 2}, medianSlidingWindow(new int[] {1, 2, 3, 4, 2, 3, 1, 4, 2}, 3), "LC 480 sample 2");
        Check.eq(new double[] {2147483647.0}, medianSlidingWindow(new int[] {2147483647, 2147483647}, 2), "LC 480 overflow case");
        Check.done("P04TwoHeaps");
    }
}
