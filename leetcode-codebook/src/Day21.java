import java.util.*;

/*@core Lazy segment tree: range add, range max
note: A node's `lazy` is an add that its children have not received yet. Push it down before you go into the children; pull the max back up after. This is the engine behind CF 833B "The Bakery" (the Infosys gift boxes) and CF 1527E.
@*/
class Core21 {
    static final class LazyMax {
        final int n;
        final long[] mx, lz;

        LazyMax(int n) { this.n = n; mx = new long[4 * n]; lz = new long[4 * n]; }

        void add(int l, int r, long v) { add(1, 0, n - 1, l, r, v); }       // add v to [l, r]

        long max(int l, int r) { return max(1, 0, n - 1, l, r); }           // max on [l, r]

        private void push(int node) {
            for (int c = 2 * node; c <= 2 * node + 1; c++) { mx[c] += lz[node]; lz[c] += lz[node]; }
            lz[node] = 0;
        }

        private void add(int node, int lo, int hi, int l, int r, long v) {
            if (r < lo || hi < l) return;
            if (l <= lo && hi <= r) { mx[node] += v; lz[node] += v; return; }
            push(node);
            int mid = (lo + hi) / 2;
            add(2 * node, lo, mid, l, r, v);
            add(2 * node + 1, mid + 1, hi, l, r, v);
            mx[node] = Math.max(mx[2 * node], mx[2 * node + 1]);
        }

        private long max(int node, int lo, int hi, int l, int r) {
            if (r < lo || hi < l) return Long.MIN_VALUE;
            if (l <= lo && hi <= r) return mx[node];
            push(node);
            int mid = (lo + hi) / 2;
            return Math.max(max(2 * node, lo, mid, l, r), max(2 * node + 1, mid + 1, hi, l, r));
        }
    }
}

/*@LC 1335
tag: Split into k parts · dp[day][i]
cue: Schedule jobs in order over d days (at least one per day); a day costs its hardest job; minimise the total.
hook: The last day starts at some j: best for j jobs in d − 1 days plus max(j..i−1).
time: O(d · n²) time · O(d · n) space
trap: Walk j DOWNWARDS from i − 1 so the max of the last day grows as you go; fewer jobs than days → −1.
var: Same frame, other part costs | LC 410 (sum), LC 813 (average), CF 833B (distinct count, needs the lazy tree).
@*/
class LC1335 {
    public int minDifficulty(int[] jobDifficulty, int d) {
        int n = jobDifficulty.length;
        if (n < d) return -1;                                            //*
        final int INF = 1_000_000_000;
        int[][] dp = new int[d + 1][n + 1];                              // dp[day][i] = first i jobs in day days
        for (int[] row : dp) Arrays.fill(row, INF);
        dp[0][0] = 0;
        for (int day = 1; day <= d; day++)
            for (int i = day; i <= n; i++) {
                int mx = 0;
                for (int j = i - 1; j >= day - 1; j--) {                 //* last day = jobs j..i-1
                    mx = Math.max(mx, jobDifficulty[j]);
                    if (dp[day - 1][j] < INF) dp[day][i] = Math.min(dp[day][i], dp[day - 1][j] + mx);   //*
                }
            }
        return dp[d][n];
    }
}

/*@LC 813
tag: Split into k parts · averages
cue: Split into at most k groups and maximise the sum of the group averages.
hook: Same k-parts DP; a part's value is its average from prefix sums.
time: O(k · n²) time · O(k · n) space
trap: Use `double` all the way; more groups never hurt here, so `dp[k][n]` is the answer.
@*/
class LC813 {
    public double largestSumOfAverages(int[] nums, int k) {
        int n = nums.length;
        double[] P = new double[n + 1];
        for (int i = 0; i < n; i++) P[i + 1] = P[i] + nums[i];
        double[][] dp = new double[k + 1][n + 1];
        for (int i = 1; i <= n; i++) dp[1][i] = P[i] / i;                       //* one group
        for (int j = 2; j <= k; j++)
            for (int i = j; i <= n; i++)
                for (int p = j - 1; p < i; p++)                                 //* last group = p..i-1
                    dp[j][i] = Math.max(dp[j][i], dp[j - 1][p] + (P[i] - P[p]) / (i - p));   //*
        return dp[k][n];
    }
}

/*@LC 2916
tag: Lazy segment tree over start positions
cue: Sum over all subarrays of (number of distinct values)², n up to 10⁵.
hook: Extending r adds 1 to the distinct count of every start after the previous copy of nums[r]; (f + 1)² − f² = 2f + 1.
time: O(n log n) time · O(n) space
trap: Read the range SUM before adding 1 to it; keep sums in `long` and take the mod only on the total.
@*/
class LC2916 {
    private long[] sum, lz;

    public int sumCounts(int[] nums) {
        int n = nums.length;
        sum = new long[4 * n];
        lz = new long[4 * n];
        Map<Integer, Integer> last = new HashMap<>();
        long squares = 0, total = 0;                     // squares = Σ f(l, r)² over l <= r
        for (int r = 0; r < n; r++) {
            int p = last.getOrDefault(nums[r], -1);
            squares += 2 * query(1, 0, n - 1, p + 1, r) + (r - p);     //* Σ (2f + 1) over starts p+1..r
            add(1, 0, n - 1, p + 1, r);                                //* those starts gain one value
            last.put(nums[r], r);
            total = (total + squares) % 1_000_000_007L;
        }
        return (int) total;
    }

    private void push(int node, int lo, int mid, int hi) {
        if (lz[node] == 0) return;
        sum[2 * node] += lz[node] * (mid - lo + 1);
        lz[2 * node] += lz[node];
        sum[2 * node + 1] += lz[node] * (hi - mid);
        lz[2 * node + 1] += lz[node];
        lz[node] = 0;
    }

    private void add(int node, int lo, int hi, int l, int r) {
        if (r < lo || hi < l) return;
        if (l <= lo && hi <= r) { sum[node] += hi - lo + 1; lz[node]++; return; }
        int mid = (lo + hi) / 2;
        push(node, lo, mid, hi);
        add(2 * node, lo, mid, l, r);
        add(2 * node + 1, mid + 1, hi, l, r);
        sum[node] = sum[2 * node] + sum[2 * node + 1];
    }

    private long query(int node, int lo, int hi, int l, int r) {
        if (r < lo || hi < l) return 0;
        if (l <= lo && hi <= r) return sum[node];
        int mid = (lo + hi) / 2;
        push(node, lo, mid, hi);
        return query(2 * node, lo, mid, l, r) + query(2 * node + 1, mid + 1, hi, l, r);
    }
}

/*@LC 732
class: MyCalendarThree
tag: Sweep line on a TreeMap
cue: After each booking, the largest number of bookings that overlap at one moment.
hook: +1 at start, −1 at end, walk in time order, remember the highest running count.
time: O(n) per booking (n ≤ 400) · O(n) space
trap: Bookings are half-open: an end and a start at the same time do not overlap, and the TreeMap merge handles that.
@*/
class LC732 {
    private final TreeMap<Integer, Integer> diff = new TreeMap<>();

    public LC732() {}

    public int book(int startTime, int endTime) {
        diff.merge(startTime, 1, Integer::sum);                     //*
        diff.merge(endTime, -1, Integer::sum);                      //*
        int active = 0, best = 0;
        for (int v : diff.values()) best = Math.max(best, active += v);   //*
        return best;
    }
}

/*@var Dynamic lazy segment tree over 0..10⁹ (O(log C) per booking)
class: MyCalendarThree
note: Nodes are created only when touched (a HashMap keyed by node id). `max = own add + best child`, so no push-down is needed for "range add, global max".
@*/
class LC732v1 {
    private final Map<Long, Integer> max = new HashMap<>(), add = new HashMap<>();

    public LC732v1() {}

    public int book(int startTime, int endTime) {
        update(1, 0, 1_000_000_000, startTime, endTime - 1);
        return max.getOrDefault(1L, 0);
    }

    private void update(long node, int lo, int hi, int l, int r) {
        if (r < lo || hi < l) return;
        if (l <= lo && hi <= r) {
            add.merge(node, 1, Integer::sum);
            max.merge(node, 1, Integer::sum);
            return;
        }
        int mid = (lo + hi) >>> 1;
        update(2 * node, lo, mid, l, r);
        update(2 * node + 1, mid + 1, hi, l, r);
        max.put(node, add.getOrDefault(node, 0) + Math.max(max.getOrDefault(2 * node, 0), max.getOrDefault(2 * node + 1, 0)));
    }
}

public class Day21 {
    static int jobs(int[] a, int from, int d) {
        if (d == 1) { int m = 0; for (int i = from; i < a.length; i++) m = Math.max(m, a[i]); return m; }
        int best = Integer.MAX_VALUE, m = 0;
        for (int i = from; i <= a.length - d; i++) { m = Math.max(m, a[i]); best = Math.min(best, m + jobs(a, i + 1, d - 1)); }
        return best;
    }

    static double avgs(int[] a, int from, int k) {
        double best = 0, s = 0;
        for (int i = from; i < a.length; i++) {
            s += a[i];
            double here = s / (i - from + 1);
            if (i == a.length - 1) best = Math.max(best, here);
            else if (k > 1) best = Math.max(best, here + avgs(a, i + 1, k - 1));
        }
        return best;
    }

    public static void main(String[] args) {
        for (int t = 0; t < 50; t++) {
            int n = T.rnd(1, 10);
            Core21.LazyMax lm = new Core21.LazyMax(n);
            long[] b = new long[n];
            for (int op = 0; op < 20; op++) {
                int l = T.rnd(0, n - 1), r = T.rnd(l, n - 1);
                if (T.rnd(0, 1) == 0) { int v = T.rnd(-3, 5); lm.add(l, r, v); for (int i = l; i <= r; i++) b[i] += v; }
                else { long m = Long.MIN_VALUE; for (int i = l; i <= r; i++) m = Math.max(m, b[i]); T.eq("core lazy", lm.max(l, r), m); }
            }
        }

        T.eq("1335a", new LC1335().minDifficulty(T.ints("[6,5,4,3,2,1]"), 2), 7);
        T.eq("1335b", new LC1335().minDifficulty(T.ints("[9,9,9]"), 4), -1);
        T.eq("1335c", new LC1335().minDifficulty(T.ints("[1,1,1]"), 3), 3);
        T.eq("1335d", new LC1335().minDifficulty(T.ints("[7,1,7,1,7,1]"), 3), 15);
        T.eq("1335e", new LC1335().minDifficulty(T.ints("[11,111,22,222,33,333,44,444]"), 6), 843);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 8), 0, 9);
            int d = T.rnd(1, a.length);
            T.eq("1335 rand", new LC1335().minDifficulty(a, d), jobs(a, 0, d));
        }

        T.close("813a", new LC813().largestSumOfAverages(T.ints("[9,1,2,3,9]"), 3), 20.0);
        T.close("813b", new LC813().largestSumOfAverages(T.ints("[1,2,3,4,5,6,7]"), 4), 20.5);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 8), 1, 9);
            int k = T.rnd(1, a.length);
            T.close("813 rand", new LC813().largestSumOfAverages(a, k), avgs(a, 0, k));
        }

        T.eq("2916a", new LC2916().sumCounts(T.ints("[1,2,1]")), 15);
        T.eq("2916b", new LC2916().sumCounts(T.ints("[2,2]")), 3);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 12), 1, 4);
            long s = 0;
            for (int i = 0; i < a.length; i++) { Set<Integer> h = new HashSet<>(); for (int j = i; j < a.length; j++) { h.add(a[j]); s += (long) h.size() * h.size(); } }
            T.eq("2916 rand", new LC2916().sumCounts(a), (int) s);
        }

        LC732 c3 = new LC732();
        LC732v1 c3v = new LC732v1();
        int[][] bk = T.grid("[[10,20],[50,60],[10,40],[5,15],[5,10],[25,55]]");
        int[] got = new int[6], gotV = new int[6];
        for (int i = 0; i < 6; i++) { got[i] = c3.book(bk[i][0], bk[i][1]); gotV[i] = c3v.book(bk[i][0], bk[i][1]); }
        T.eq("732", got, T.ints("[1,1,2,3,3,3]"));
        T.eq("732v", gotV, T.ints("[1,1,2,3,3,3]"));
        for (int t = 0; t < 50; t++) {
            LC732 a = new LC732();
            LC732v1 b = new LC732v1();
            for (int op = 0; op < 15; op++) {
                int s = T.rnd(0, 30), e = s + T.rnd(1, 10);
                T.eq("732 rand", b.book(s, e), a.book(s, e));
            }
        }
        T.summary("Day21");
    }
}
