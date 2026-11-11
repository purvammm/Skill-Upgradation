import java.util.*;

public class P13SparseTable {
    //@tpl table
    // st[k][i] = op of a[i .. i + 2^k), built from two halves of size 2^(k-1).
    // op must be idempotent (op(x, x) == x): min, max, gcd, &, |.
    static class SparseTable {
        final int[][] st;
        final int[] lg;                     // lg[x] = floor(log2 x)
        final boolean max;                  // false: range min, true: range max

        SparseTable(int[] a, boolean max) {
            this.max = max;
            int n = a.length;
            lg = new int[n + 1];
            for (int x = 2; x <= n; x++) lg[x] = lg[x / 2] + 1;
            st = new int[lg[n] + 1][];
            st[0] = a.clone();
            for (int k = 1; k < st.length; k++) {      // row k needs all of row k-1
                int half = 1 << (k - 1);
                st[k] = new int[n - (1 << k) + 1];
                for (int i = 0; i < st[k].length; i++)
                    st[k][i] = op(st[k - 1][i], st[k - 1][i + half]);
            }
        }

        int op(int x, int y) { return max ? Math.max(x, y) : Math.min(x, y); }

        // op of a[l..r], inclusive: two blocks of length 2^k that may overlap.
        int query(int l, int r) {
            int k = lg[r - l + 1];
            return op(st[k][l], st[k][r - (1 << k) + 1]);
        }
    }
    //@end

    //@tpl limit
    // LC 1438: longest subarray with max - min <= limit. Both ends only move
    // right, and each check is two O(1) queries.
    static int longestSubarray(int[] a, int limit) {
        SparseTable mn = new SparseTable(a, false), mx = new SparseTable(a, true);
        int best = 0;
        for (int l = 0, r = 0; r < a.length; r++) {
            while (mx.query(l, r) - mn.query(l, r) > limit) l++;
            best = Math.max(best, r - l + 1);
        }
        return best;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int bruteLongest(int[] a, int limit) {
        int best = 0;
        for (int i = 0; i < a.length; i++) {
            int mn = a[i], mx = a[i];
            for (int j = i; j < a.length; j++) {
                mn = Math.min(mn, a[j]);
                mx = Math.max(mx, a[j]);
                if (mx - mn <= limit) best = Math.max(best, j - i + 1);
            }
        }
        return best;
    }

    // OR table (variant): LC 2411 smallest subarrays with maximum OR
    static int[] smallestSubarrays(int[] a) {
        int n = a.length;
        int[] lg = new int[n + 1];
        for (int x = 2; x <= n; x++) lg[x] = lg[x / 2] + 1;
        int[][] st = new int[lg[n] + 1][];
        st[0] = a.clone();
        for (int k = 1; k < st.length; k++) {
            st[k] = new int[n - (1 << k) + 1];
            for (int i = 0; i < st[k].length; i++) st[k][i] = st[k - 1][i] | st[k - 1][i + (1 << (k - 1))];
        }
        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            int full = orq(st, lg, i, n - 1);
            int lo = i, hi = n - 1;                  // first r with OR(i..r) == full
            while (lo < hi) {
                int mid = (lo + hi) / 2;
                if (orq(st, lg, i, mid) == full) hi = mid; else lo = mid + 1;
            }
            out[i] = lo - i + 1;
        }
        return out;
    }

    static int orq(int[][] st, int[] lg, int l, int r) {
        int k = lg[r - l + 1];
        return st[k][l] | st[k][r - (1 << k) + 1];
    }

    // the trap: i in the outer loop reads cells of row k-1 not built yet
    static int[][] buildWrongOrder(int[] a) {
        int n = a.length;
        int[] lg = new int[n + 1];
        for (int x = 2; x <= n; x++) lg[x] = lg[x / 2] + 1;
        int[][] st = new int[lg[n] + 1][n];
        for (int i = 0; i < n; i++) st[0][i] = a[i];
        for (int i = 0; i < n; i++)
            for (int k = 1; i + (1 << k) <= n; k++)
                st[k][i] = Math.min(st[k - 1][i], st[k - 1][i + (1 << (k - 1))]);
        return st;
    }

    // LC 3171 (practice): OR table, binary search the first r with OR >= k
    static int minimumDifference(int[] a, int k) {
        int n = a.length;
        int[] lg = new int[n + 1];
        for (int x = 2; x <= n; x++) lg[x] = lg[x / 2] + 1;
        int[][] st = new int[lg[n] + 1][];
        st[0] = a.clone();
        for (int j = 1; j < st.length; j++) {
            st[j] = new int[n - (1 << j) + 1];
            for (int i = 0; i < st[j].length; i++) st[j][i] = st[j - 1][i] | st[j - 1][i + (1 << (j - 1))];
        }
        int best = Integer.MAX_VALUE;
        for (int l = 0; l < n; l++) {
            int lo = l, hi = n - 1;
            while (lo < hi) {
                int mid = (lo + hi) / 2;
                if (orq(st, lg, l, mid) >= k) hi = mid; else lo = mid + 1;
            }
            best = Math.min(best, Math.abs(orq(st, lg, l, lo) - k));
            if (lo > l) best = Math.min(best, Math.abs(orq(st, lg, l, lo - 1) - k));
        }
        return best;
    }

    // LC 2398 (practice): window with max(charge) + len * sum(running) <= budget
    static int maximumRobots(int[] charge, int[] running, long budget) {
        SparseTable mx = new SparseTable(charge, true);
        int n = charge.length, best = 0;
        long sum = 0;
        for (int l = 0, r = 0; r < n; r++) {
            sum += running[r];
            while (l <= r && mx.query(l, r) + (long) (r - l + 1) * sum > budget) sum -= running[l++];
            best = Math.max(best, r - l + 1);
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(1301);
        for (int t = 0; t < 2000; t++) {
            int n = 1 + r.nextInt(40);
            int[] a = Check.ints(r, n, -1000000000, 1000000000);
            if (t % 2 == 0) a = Check.ints(r, n, 0, 9);
            SparseTable mn = new SparseTable(a, false), mx = new SparseTable(a, true);
            for (int l = 0; l < n; l++) {
                int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
                for (int rr = l; rr < n; rr++) {
                    lo = Math.min(lo, a[rr]);
                    hi = Math.max(hi, a[rr]);
                    Check.eq(lo, mn.query(l, rr), "min " + Arrays.toString(a) + " " + l + ".." + rr);
                    Check.eq(hi, mx.query(l, rr), "max");
                }
            }
            int[] b = Check.ints(r, n, 1, 20);
            int limit = r.nextInt(10);
            Check.eq(bruteLongest(b, limit), longestSubarray(b, limit), "LC 1438 " + Arrays.toString(b));
            int[] c = Check.ints(r, n, 0, 15);
            int[] want = new int[n];
            for (int i = 0; i < n; i++) {
                int full = 0;
                for (int j = i; j < n; j++) full |= c[j];
                int cur = 0, j = i;
                while (true) { cur |= c[j]; if (cur == full) break; j++; }
                want[i] = j - i + 1;
            }
            Check.eq(want, smallestSubarrays(c), "LC 2411 " + Arrays.toString(c));
            int kk = r.nextInt(20);
            int bestOr = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++) { int o = 0; for (int j = i; j < n; j++) { o |= c[j]; bestOr = Math.min(bestOr, Math.abs(o - kk)); } }
            Check.eq(bestOr, minimumDifference(c, kk), "LC 3171");
            int[] ch = Check.ints(r, n, 1, 20), ru = Check.ints(r, n, 1, 10);
            long budget = r.nextInt(200);
            int bestR = 0;
            for (int i = 0; i < n; i++) for (int j = i; j < n; j++) {
                int mxc = 0; long sm = 0;
                for (int q = i; q <= j; q++) { mxc = Math.max(mxc, ch[q]); sm += ru[q]; }
                if (mxc + (long) (j - i + 1) * sm <= budget) bestR = Math.max(bestR, j - i + 1);
            }
            Check.eq(bestR, maximumRobots(ch, ru, budget), "LC 2398");
        }
        // big: n = 1e5 with 1e5 random queries
        int n = 100000;
        int[] a = Check.ints(r, n, 1, 1000000000);
        SparseTable mn = new SparseTable(a, false);
        long[] pre = new long[n + 1];
        for (int i = 0; i < n; i++) pre[i + 1] = pre[i] + a[i];
        for (int q = 0; q < 100000; q++) {
            int l = r.nextInt(n), len = 1 + r.nextInt(Math.min(64, n - l));
            int want = Integer.MAX_VALUE;
            for (int i = l; i < l + len; i++) want = Math.min(want, a[i]);
            Check.eq(want, mn.query(l, l + len - 1), "big min");
        }
        Check.eq(bruteLongest(Arrays.copyOf(a, 3000), 500000000), longestSubarray(Arrays.copyOf(a, 3000), 500000000), "big 1438");
        // LeetCode samples
        Check.eq(2, longestSubarray(new int[] {8, 2, 4, 7}, 4), "LC 1438 sample 1");
        Check.eq(4, longestSubarray(new int[] {10, 1, 2, 4, 7, 2}, 5), "LC 1438 sample 2");
        Check.eq(3, longestSubarray(new int[] {4, 2, 2, 2, 4, 4, 2, 2}, 0), "LC 1438 sample 3");
        Check.eq(new int[] {3, 3, 2, 2, 1}, smallestSubarrays(new int[] {1, 0, 2, 1, 3}), "LC 2411 sample 1");
        Check.eq(new int[] {2, 1}, smallestSubarrays(new int[] {1, 2}), "LC 2411 sample 2");
        Check.eq(0, minimumDifference(new int[] {1, 2, 4, 5}, 3), "LC 3171 sample 1");
        Check.eq(1, minimumDifference(new int[] {1, 3, 1, 3}, 2), "LC 3171 sample 2");
        Check.eq(9, minimumDifference(new int[] {1}, 10), "LC 3171 sample 3");
        Check.eq(3, maximumRobots(new int[] {3, 6, 1, 3, 4}, new int[] {2, 1, 3, 4, 5}, 25), "LC 2398 sample 1");
        Check.eq(0, maximumRobots(new int[] {11, 12, 19}, new int[] {10, 8, 7}, 19), "LC 2398 sample 2");
        // trace numbers and traps
        int[] tr = {5, 2, 4, 7, 1, 3, 6, 8};
        SparseTable ts = new SparseTable(tr, false);
        Check.eq(1, ts.query(2, 6), "trace min(2, 6)");
        int k = ts.lg[5];
        int overlapSum = 0;
        for (int i = 2; i < 2 + (1 << k); i++) overlapSum += tr[i];
        for (int i = 6 - (1 << k) + 1; i <= 6; i++) overlapSum += tr[i];
        Check.eq(32, overlapSum, "two blocks for a sum");
        Check.eq(21, 4 + 7 + 1 + 3 + 6, "true sum");
        int[][] bad = buildWrongOrder(tr);
        Check.eq(new int[] {2, 1, 1, 1, 1}, ts.st[2], "row 2");
        int kq = ts.lg[4];                                 // query(0, 3) with r - k + 1
        Check.eq(2, ts.query(0, 3), "right min(0, 3)");
        Check.eq(1, Math.min(ts.st[kq][0], ts.st[kq][3 - kq + 1]), "r - k + 1 instead of r - 2^k + 1");
        Check.eq(new int[] {0, 0, 0, 0, 0}, Arrays.copyOf(bad[2], 5), "wrong order: row 2 is all 0");
        Check.done("P13SparseTable");
    }
}
