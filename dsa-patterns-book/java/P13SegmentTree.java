import java.util.*;

public class P13SegmentTree {
    //@tpl tree
    // Recursive segment tree over a[0..n-1], all 0 at the start. Node x covers
    // [lo, hi]; children 2x and 2x+1 split it at mid. Each node keeps the max
    // and the sum of its range. 4n slots are always enough.
    static class SegTree {
        final int n;
        final long[] mx, sm;
        SegTree(int n) { this.n = n; mx = new long[4 * n]; sm = new long[4 * n]; }

        void set(int i, long v) { set(1, 0, n - 1, i, v); }          // a[i] = v
        void set(int x, int lo, int hi, int i, long v) {
            if (lo == hi) { mx[x] = sm[x] = v; return; }
            int mid = (lo + hi) / 2;
            if (i <= mid) set(2 * x, lo, mid, i, v);
            else set(2 * x + 1, mid + 1, hi, i, v);
            mx[x] = Math.max(mx[2 * x], mx[2 * x + 1]);                 // pull up
            sm[x] = sm[2 * x] + sm[2 * x + 1];
        }

        long max(int l, int r) { return max(1, 0, n - 1, l, r); }      // a[l..r]
        long max(int x, int lo, int hi, int l, int r) {
            if (r < lo || hi < l) return Long.MIN_VALUE;                // outside
            if (l <= lo && hi <= r) return mx[x];                       // inside
            int mid = (lo + hi) / 2;
            return Math.max(max(2 * x, lo, mid, l, r),
                            max(2 * x + 1, mid + 1, hi, l, r));
        }

        long sum(int l, int r) { return sum(1, 0, n - 1, l, r); }
        long sum(int x, int lo, int hi, int l, int r) {
            if (r < lo || hi < l) return 0;
            if (l <= lo && hi <= r) return sm[x];
            int mid = (lo + hi) / 2;
            return sum(2 * x, lo, mid, l, r) + sum(2 * x + 1, mid + 1, hi, l, r);
        }
    }
    //@end

    //@tpl descent
    // Smallest i <= r with a[i] >= k, or -1: call first(t, 1, 0, t.n - 1, k, r).
    // A node whose max is below k cannot hold the answer, so skip it whole.
    static int first(SegTree t, int x, int lo, int hi, long k, int r) {
        if (lo > r || t.mx[x] < k) return -1;
        if (lo == hi) return lo;
        int mid = (lo + hi) / 2;
        int i = first(t, 2 * x, lo, mid, k, r);              // leftmost first
        return i >= 0 ? i : first(t, 2 * x + 1, mid + 1, hi, k, r);
    }
    //@end

    //@tpl lis
    // LC 2407: best[v] = longest valid subsequence ending in value v. The value
    // before v lies in [v - k, v - 1]: one range max, one point set per value.
    static int lengthOfLIS(int[] nums, int k) {
        int maxV = 0;
        for (int v : nums) maxV = Math.max(maxV, v);
        SegTree t = new SegTree(maxV + 1);
        long best = 0;
        for (int v : nums) {                         // values are >= 1
            long len = 1 + Math.max(0, t.max(Math.max(0, v - k), v - 1));
            t.set(v, len);                           // never smaller than before
            best = Math.max(best, len);
        }
        return (int) best;
    }
    //@end

    // LC 2286 (variant): n rows of m seats; the tree holds the free seats of each row.
    static class BookMyShow {
        final SegTree t;
        final int m;
        int open = 0;                                // rows before it are full
        BookMyShow(int n, int m) {
            this.m = m;
            t = new SegTree(n);
            for (int r = 0; r < n; r++) t.set(r, m);
        }

        int[] gather(int k, int maxRow) {            // k seats in one row
            int r = first(t, 1, 0, t.n - 1, k, maxRow);
            if (r < 0) return new int[0];
            long free = t.sum(r, r);
            t.set(r, free - k);
            return new int[] {r, (int) (m - free)};
        }

        boolean scatter(int k, int maxRow) {         // k seats, lowest rows first
            if (t.sum(0, maxRow) < k) return false;
            long need = k;
            while (need > 0) {
                long free = t.sum(open, open), take = Math.min(free, need);
                t.set(open, free - take);
                need -= take;
                if (take == free) open++;            // this row is full now
            }
            return true;
        }
    }

    // ---------------- tests (not printed) ----------------
    static int bruteLIS(int[] a, int k) {
        int[] dp = new int[a.length];
        int best = 0;
        for (int i = 0; i < a.length; i++) {
            dp[i] = 1;
            for (int j = 0; j < i; j++) if (a[j] < a[i] && a[i] - a[j] <= k) dp[i] = Math.max(dp[i], dp[j] + 1);
            best = Math.max(best, dp[i]);
        }
        return best;
    }

    static class BruteShow {
        final int[] used;
        final int m;
        BruteShow(int n, int m) { used = new int[n]; this.m = m; }
        int[] gather(int k, int maxRow) {
            for (int r = 0; r <= maxRow; r++)
                if (m - used[r] >= k) { int c = used[r]; used[r] += k; return new int[] {r, c}; }
            return new int[0];
        }
        boolean scatter(int k, int maxRow) {
            long free = 0;
            for (int r = 0; r <= maxRow; r++) free += m - used[r];
            if (free < k) return false;
            for (int r = 0; r <= maxRow && k > 0; r++) {
                int take = Math.min(k, m - used[r]);
                used[r] += take;
                k -= take;
            }
            return true;
        }
    }

    // LC 3479 (practice): first basket with capacity >= fruit, then use it up
    static int numOfUnplacedFruits(int[] fruits, int[] baskets) {
        int n = baskets.length;
        SegTree t = new SegTree(n);
        for (int i = 0; i < n; i++) t.set(i, baskets[i]);
        int unplaced = 0;
        for (int f : fruits) {
            int i = first(t, 1, 0, n - 1, f, n - 1);
            if (i < 0) unplaced++;
            else t.set(i, 0);
        }
        return unplaced;
    }

    // the trap: [v - k, v] lets a value follow itself
    static int lisWithSelf(int[] nums, int k) {
        int maxV = 0;
        for (int v : nums) maxV = Math.max(maxV, v);
        SegTree t = new SegTree(maxV + 1);
        long best = 0;
        for (int v : nums) {
            long len = 1 + Math.max(0, t.max(Math.max(0, v - k), v));
            t.set(v, len);
            best = Math.max(best, len);
        }
        return (int) best;
    }

    // the trap: max query that returns 0 outside the range
    static long maxZero(SegTree t, int x, int lo, int hi, int l, int r) {
        if (r < lo || hi < l) return 0;
        if (l <= lo && hi <= r) return t.mx[x];
        int mid = (lo + hi) / 2;
        return Math.max(maxZero(t, 2 * x, lo, mid, l, r), maxZero(t, 2 * x + 1, mid + 1, hi, l, r));
    }

    static int maxIndex(int x, int lo, int hi) {
        if (lo == hi) return x;
        int mid = (lo + hi) / 2;
        return Math.max(maxIndex(2 * x, lo, mid), maxIndex(2 * x + 1, mid + 1, hi));
    }

    public static void main(String[] args) {
        Random r = new Random(1303);
        for (int t = 0; t < 1500; t++) {
            int n = 1 + r.nextInt(30);
            SegTree st = new SegTree(n);
            long[] a = new long[n];
            for (int op = 0; op < 80; op++) {
                int l = r.nextInt(n), rr = l + r.nextInt(n - l);
                switch (r.nextInt(4)) {
                    case 0: {
                        long v = r.nextInt(2000000001) - 1000000000L;
                        if (t % 2 == 0) v = r.nextInt(10);
                        st.set(l, v); a[l] = v; break;
                    }
                    case 1: {
                        long mx = Long.MIN_VALUE, sm = 0;
                        for (int i = l; i <= rr; i++) { mx = Math.max(mx, a[i]); sm += a[i]; }
                        Check.eq(mx, st.max(l, rr), "max");
                        Check.eq(sm, st.sum(l, rr), "sum");
                        break;
                    }
                    default: {
                        long k = t % 2 == 0 ? r.nextInt(12) : r.nextInt(2000000001) - 1000000000L;
                        int want = -1;
                        for (int i = 0; i <= rr; i++) if (a[i] >= k) { want = i; break; }
                        Check.eq(want, first(st, 1, 0, n - 1, k, rr), "first");
                    }
                }
            }
            int[] nums = Check.ints(r, n, 1, 20);
            int k = 1 + r.nextInt(6);
            Check.eq(bruteLIS(nums, k), lengthOfLIS(nums, k), "LC 2407 " + Arrays.toString(nums) + " k=" + k);
            int rows = 1 + r.nextInt(6), seats = 1 + r.nextInt(6);
            BookMyShow bs = new BookMyShow(rows, seats);
            BruteShow br = new BruteShow(rows, seats);
            for (int op = 0; op < 20; op++) {
                int kk = 1 + r.nextInt(seats + 2), mr = r.nextInt(rows);
                if (r.nextBoolean()) Check.eq(br.gather(kk, mr), bs.gather(kk, mr), "gather");
                else Check.eq(br.scatter(kk, mr), bs.scatter(kk, mr), "scatter");
            }
            int[] fr = Check.ints(r, n, 1, 10), bk = Check.ints(r, n, 1, 10);
            int un = 0;
            boolean[] usedB = new boolean[n];
            for (int f : fr) {
                boolean placed = false;
                for (int i = 0; i < n; i++) if (!usedB[i] && bk[i] >= f) { usedB[i] = true; placed = true; break; }
                if (!placed) un++;
            }
            Check.eq(un, numOfUnplacedFruits(fr, bk), "LC 3479");
        }
        // big: n = 1e5 values, 1e5 queries, in time
        int[] big = Check.ints(r, 100000, 1, 100000);
        Check.isTrue(lengthOfLIS(big, 100000) > 0, "big LIS");
        BookMyShow bb = new BookMyShow(50000, 1000000000);
        for (int i = 0; i < 50000; i++) bb.scatter(1000000000, 49999);
        Check.isTrue(!bb.scatter(1, 49999), "big scatter fills everything");
        // LeetCode samples
        Check.eq(5, lengthOfLIS(new int[] {4, 2, 1, 4, 3, 4, 5, 8, 15}, 3), "LC 2407 sample 1");
        Check.eq(4, lengthOfLIS(new int[] {7, 4, 5, 1, 8, 12, 4, 7}, 5), "LC 2407 sample 2");
        Check.eq(1, lengthOfLIS(new int[] {1, 5}, 1), "LC 2407 sample 3");
        BookMyShow s = new BookMyShow(2, 5);
        Check.eq(new int[] {0, 0}, s.gather(4, 0), "LC 2286 gather 1");
        Check.eq(new int[0], s.gather(2, 0), "LC 2286 gather 2");
        Check.isTrue(s.scatter(5, 1), "LC 2286 scatter 1");
        Check.isTrue(!s.scatter(5, 1), "LC 2286 scatter 2");
        Check.eq(1, numOfUnplacedFruits(new int[] {4, 2, 5}, new int[] {3, 5, 4}), "LC 3479 sample 1");
        Check.eq(0, numOfUnplacedFruits(new int[] {3, 6, 1}, new int[] {6, 4, 7}), "LC 3479 sample 2");
        // trace numbers
        SegTree tr = new SegTree(6);
        int[] ta = {5, 1, 4, 2, 6, 3};
        for (int i = 0; i < 6; i++) tr.set(i, ta[i]);
        Check.eq(13, tr.sum(1, 4), "trace sum(1, 4)");
        Check.eq(0, first(tr, 1, 0, 5, 5, 5), "trace first(5)");
        Check.eq(4, first(tr, 1, 0, 5, 6, 5), "trace first(6)");
        Check.eq(-1, first(tr, 1, 0, 5, 6, 3), "trace first(6, r=3)");
        // traps
        Check.eq(13, maxIndex(1, 0, 5), "n = 6 needs node 13 > 2n");
        Check.eq(1, lengthOfLIS(new int[] {1, 1}, 1), "strict");
        Check.eq(2, lisWithSelf(new int[] {1, 1}, 1), "[v - k, v] chains equal values");
        SegTree neg = new SegTree(2);
        neg.set(0, -3); neg.set(1, -5);
        Check.eq(-3, neg.max(0, 1), "negative max");
        Check.eq(-5, neg.max(1, 1), "max of [-5]");
        Check.eq(0, maxZero(neg, 1, 0, 1, 1, 1), "zero identity: max of [-5] is 0");
        Check.done("P13SegmentTree");
    }
}
