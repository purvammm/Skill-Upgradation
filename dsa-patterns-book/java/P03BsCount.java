import java.util.*;

public class P03BsCount {
    //@tpl matrix
    // LC 378: k-th smallest (1-based) in an n x n matrix with sorted rows and
    // columns. Answer = smallest value v with countAtMost(v) >= k.
    static int kthSmallest(int[][] m, int k) {
        int n = m.length;
        int lo = m[0][0], hi = m[n - 1][n - 1];   // count(hi) = n*n >= k
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;         // rounds down, also for negatives
            if (countAtMost(m, mid) >= k) hi = mid;
            else lo = mid + 1;
        }
        return lo;
    }

    // Staircase from the bottom-left corner: O(n) per count.
    static int countAtMost(int[][] m, int v) {
        int n = m.length, count = 0, row = n - 1, col = 0;
        while (row >= 0 && col < n) {
            if (m[row][col] <= v) { count += row + 1; col++; }  // rows 0..row of col
            else row--;                                         // too big: go up
        }
        return count;
    }
    //@end

    //@tpl pairs
    // LC 719: k-th smallest |a[i] - a[j]| over all pairs i < j.
    static int smallestDistancePair(int[] a, int k) {
        Arrays.sort(a);
        int lo = 0, hi = a[a.length - 1] - a[0];  // every pair distance is <= hi
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (pairsAtMost(a, mid) >= k) hi = mid;
            else lo = mid + 1;
        }
        return lo;
    }

    // Pairs with a[j] - a[i] <= d: for each j, i is the first index close enough.
    static long pairsAtMost(int[] a, int d) {
        long count = 0;
        for (int i = 0, j = 0; j < a.length; j++) {
            while (a[j] - a[i] > d) i++;          // i only moves right: O(n) total
            count += j - i;
        }
        return count;
    }
    //@end

    //@tpl median
    // LC 4: median of two sorted arrays in O(log min(n, m)). Binary search the cut
    // i in the shorter array a; the cut in b is j = half - i. The cut is right
    // when a[i-1] <= b[j] and b[j-1] <= a[i].
    static double findMedianSortedArrays(int[] a, int[] b) {
        if (a.length > b.length) return findMedianSortedArrays(b, a);
        int n = a.length, m = b.length, half = (n + m + 1) / 2;
        int lo = 0, hi = n;                       // i is in [lo, hi]
        while (true) {
            int i = lo + (hi - lo) / 2, j = half - i;
            int aL = i == 0 ? Integer.MIN_VALUE : a[i - 1];   // left of the cut in a
            int aR = i == n ? Integer.MAX_VALUE : a[i];       // right of the cut in a
            int bL = j == 0 ? Integer.MIN_VALUE : b[j - 1];
            int bR = j == m ? Integer.MAX_VALUE : b[j];
            if (aL > bR) hi = i - 1;              // took too many from a
            else if (bL > aR) lo = i + 1;         // took too few from a
            else if ((n + m) % 2 == 1) return Math.max(aL, bL);
            else return (Math.max(aL, bL) + (long) Math.min(aR, bR)) / 2.0;
        }
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // LC 668, quoted in the variants: entries <= v in an m x n multiplication table.
    static int findKthNumber(int m, int n, int k) {
        int lo = 1, hi = m * n;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            int count = 0;
            for (int i = 1; i <= m; i++) count += Math.min(mid / i, n);
            if (count >= k) hi = mid;
            else lo = mid + 1;
        }
        return lo;
    }

    // LC 287, quoted in the variants: smallest v with more than v values <= v.
    static int findDuplicate(int[] a) {
        int lo = 1, hi = a.length - 1;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2, count = 0;
            for (int x : a) if (x <= mid) count++;
            if (count > mid) hi = mid;
            else lo = mid + 1;
        }
        return lo;
    }

    static int[][] sortedMatrix(Random r, int n, int lo, int step) {
        int[][] m = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                int base = lo;
                if (i > 0) base = Math.max(base, m[i - 1][j]);
                if (j > 0) base = Math.max(base, m[i][j - 1]);
                m[i][j] = (i == 0 && j == 0 ? lo : base) + r.nextInt(step);
            }
        return m;
    }

    public static void main(String[] args) {
        Random r = new Random(41);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(6);
            int[][] m = sortedMatrix(r, n, -10 + r.nextInt(5), 4);
            int[] flat = new int[n * n];
            for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) flat[i * n + j] = m[i][j];
            Arrays.sort(flat);
            for (int k = 1; k <= n * n; k++)
                Check.eq(flat[k - 1], kthSmallest(m, k), "matrix " + Arrays.deepToString(m) + " k=" + k);
            int len = 2 + r.nextInt(9);
            int[] a = Check.ints(r, len, 0, 20);
            List<Integer> d = new ArrayList<>();
            for (int i = 0; i < len; i++) for (int j = i + 1; j < len; j++) d.add(Math.abs(a[i] - a[j]));
            Collections.sort(d);
            int k = 1 + r.nextInt(d.size());
            Check.eq((long) d.get(k - 1), smallestDistancePair(a.clone(), k), "pairs " + Arrays.toString(a) + " k=" + k);
            int[] x = Check.sortedInts(r, r.nextInt(7), -20, 20);
            int[] y = Check.sortedInts(r, (x.length == 0 ? 1 : 0) + r.nextInt(7), -20, 20);
            int[] all = new int[x.length + y.length];
            System.arraycopy(x, 0, all, 0, x.length);
            System.arraycopy(y, 0, all, x.length, y.length);
            Arrays.sort(all);
            int L = all.length;
            double med = L % 2 == 1 ? all[L / 2] : (all[L / 2 - 1] + all[L / 2]) / 2.0;
            Check.eq(med, findMedianSortedArrays(x, y), 0, "median " + Arrays.toString(x) + Arrays.toString(y));
            int mm = 1 + r.nextInt(6), nn = 1 + r.nextInt(6);
            List<Integer> tab = new ArrayList<>();
            for (int i = 1; i <= mm; i++) for (int j = 1; j <= nn; j++) tab.add(i * j);
            Collections.sort(tab);
            int kk = 1 + r.nextInt(mm * nn);
            Check.eq((long) tab.get(kk - 1), findKthNumber(mm, nn, kk), "table " + mm + "x" + nn + " k=" + kk);
        }
        // LC 287: n + 1 values in 1..n, one value repeated (maybe many times)
        for (int t = 0; t < 2000; t++) {
            int n = 1 + r.nextInt(9), dup = 1 + r.nextInt(n);
            List<Integer> vals = new ArrayList<>();
            for (int v = 1; v <= n; v++) if (r.nextBoolean() || v == dup) vals.add(v);
            while (vals.size() < n + 1) vals.add(dup);
            Collections.shuffle(vals, r);
            int[] a = new int[n + 1];
            for (int i = 0; i <= n; i++) a[i] = vals.get(i);
            Check.eq(dup, findDuplicate(a), "LC 287 " + Arrays.toString(a));
        }
        Check.eq(2, findDuplicate(new int[] {1, 3, 4, 2, 2}), "LC 287 sample 1");
        Check.eq(3, findDuplicate(new int[] {3, 1, 3, 4, 2}), "LC 287 sample 2");
        Check.eq(3, findDuplicate(new int[] {3, 3, 3, 3, 3}), "LC 287 sample 3");
        // extremes: values across the whole LC 378 range, medians near the int limits
        int[][] wide = {{-1000000000, 0}, {5, 1000000000}};
        Check.eq(1000000000, kthSmallest(wide, 4), "wide k=4");
        Check.eq(-1000000000, kthSmallest(wide, 1), "wide k=1");
        Check.eq((double) Integer.MAX_VALUE, findMedianSortedArrays(new int[] {Integer.MAX_VALUE},
                new int[] {Integer.MAX_VALUE}), 0, "median max");
        // the (lo + hi) / 2 trap: with lo = -3, hi = -2 it gives -2 = hi, not -3
        Check.eq(-2, (-3 + -2) / 2, "truncation toward zero");
        Check.eq(-3, -3 + (-2 - -3) / 2, "lo + (hi - lo) / 2 rounds down");
        // LeetCode samples
        Check.eq(13, kthSmallest(new int[][] {{1, 5, 9}, {10, 11, 13}, {12, 13, 15}}, 8), "LC 378 sample 1");
        Check.eq(-5, kthSmallest(new int[][] {{-5}}, 1), "LC 378 sample 2");
        Check.eq(0, smallestDistancePair(new int[] {1, 3, 1}, 1), "LC 719 sample 1");
        Check.eq(0, smallestDistancePair(new int[] {1, 1, 1}, 2), "LC 719 sample 2");
        Check.eq(5, smallestDistancePair(new int[] {1, 6, 1}, 3), "LC 719 sample 3");
        Check.eq(2.0, findMedianSortedArrays(new int[] {1, 3}, new int[] {2}), 0, "LC 4 sample 1");
        Check.eq(2.5, findMedianSortedArrays(new int[] {1, 2}, new int[] {3, 4}), 0, "LC 4 sample 2");
        Check.eq(3, findKthNumber(3, 3, 5), "LC 668 sample 1");
        Check.eq(6, findKthNumber(2, 3, 6), "LC 668 sample 2");
        Check.done("P03BsCount");
    }
}
