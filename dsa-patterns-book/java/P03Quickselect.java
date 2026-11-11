import java.util.*;

public class P03Quickselect {
    //@tpl select
    static final Random RND = new Random();

    // k-th smallest (0-based) in O(n) expected time; reorders a.
    // LC 215 (k-th largest, 1-based) is kthSmallest(a, a.length - k).
    static int kthSmallest(int[] a, int k) {
        int lo = 0, hi = a.length - 1;            // index k lies in [lo, hi]
        while (lo < hi) {
            int pivot = a[lo + RND.nextInt(hi - lo + 1)];
            int i = lo, j = hi;                   // Hoare-style partition
            while (i <= j) {
                while (a[i] < pivot) i++;         // stops at a value >= pivot
                while (a[j] > pivot) j--;         // stops at a value <= pivot
                if (i <= j) { int t = a[i]; a[i] = a[j]; a[j] = t; i++; j--; }
            }
            // now a[lo..j] <= pivot, a[i..hi] >= pivot, a[j+1..i-1] == pivot
            if (k <= j) hi = j;
            else if (k >= i) lo = i;
            else return a[k];
        }
        return a[k];
    }
    //@end

    //@tpl closest
    // LC 973: the k points nearest (0, 0), any order. The same loop, keyed on
    // squared distance; afterwards p[0..k-1] are the k smallest.
    static int[][] kClosest(int[][] p, int k) {
        int lo = 0, hi = p.length - 1, target = k - 1;
        while (lo < hi) {
            long pivot = dist(p[lo + RND.nextInt(hi - lo + 1)]);
            int i = lo, j = hi;
            while (i <= j) {
                while (dist(p[i]) < pivot) i++;
                while (dist(p[j]) > pivot) j--;
                if (i <= j) { int[] t = p[i]; p[i] = p[j]; p[j] = t; i++; j--; }
            }
            if (target <= j) hi = j;
            else if (target >= i) lo = i;
            else break;
        }
        return Arrays.copyOf(p, k);
    }

    static long dist(int[] q) { return (long) q[0] * q[0] + (long) q[1] * q[1]; }
    //@end

    // ---------------- tests (not printed) ----------------
    // Lomuto partition, used only to measure the all-equal worst case quoted in the book.
    static long lomutoSteps;

    static int lomutoSelect(int[] a, int k) {
        int lo = 0, hi = a.length - 1;
        while (lo < hi) {
            int p = lo + RND.nextInt(hi - lo + 1);
            int t = a[p]; a[p] = a[hi]; a[hi] = t;
            int pivot = a[hi], store = lo;
            for (int i = lo; i < hi; i++) {
                lomutoSteps++;
                if (a[i] < pivot) { t = a[i]; a[i] = a[store]; a[store] = t; store++; }
            }
            t = a[store]; a[store] = a[hi]; a[hi] = t;
            if (k == store) return a[k];
            if (k < store) hi = store - 1;
            else lo = store + 1;
        }
        return a[k];
    }

    public static void main(String[] args) {
        RND.setSeed(53);
        Random r = new Random(53);
        for (int t = 0; t < 4000; t++) {
            int n = 1 + r.nextInt(15);
            int[] a = Check.ints(r, n, -4, 4);
            int[] s = a.clone();
            Arrays.sort(s);
            for (int k = 0; k < n; k++) {
                int[] c = a.clone();
                Check.eq(s[k], kthSmallest(c, k), "kth " + Arrays.toString(a) + " k=" + k);
                Arrays.sort(c);
                Check.eq(s, c, "permutation");
            }
            int[][] p = new int[n][];
            for (int i = 0; i < n; i++) p[i] = new int[] {r.nextInt(7) - 3, r.nextInt(7) - 3};
            long[] d = new long[n];
            for (int i = 0; i < n; i++) d[i] = dist(p[i]);
            Arrays.sort(d);
            int k = 1 + r.nextInt(n);
            int[][] got = kClosest(p.clone(), k);
            long[] gd = new long[k];
            for (int i = 0; i < k; i++) gd[i] = dist(got[i]);
            Arrays.sort(gd);
            Check.eq(Arrays.copyOf(d, k), gd, "closest k=" + k);
        }
        // all equal and big: Hoare-style splits evenly, so this is fast
        int[] same = new int[1000000];
        Arrays.fill(same, 7);
        Check.eq(7, kthSmallest(same, 123456), "all equal 1e6");
        int[] big = Check.ints(r, 1000000, -1000000000, 1000000000);
        int[] bs = big.clone();
        Arrays.sort(bs);
        Check.eq(bs[500000], kthSmallest(big, 500000), "median 1e6");
        // Lomuto on 2000 equal values does about n^2 / 2 steps
        int[] eq = new int[2000];
        lomutoSteps = 0;
        lomutoSelect(eq, 1000);
        Check.isTrue(lomutoSteps > 1000000, "lomuto quadratic: " + lomutoSteps);
        // LeetCode samples
        int[] s1 = {3, 2, 1, 5, 6, 4};
        Check.eq(5, kthSmallest(s1, s1.length - 2), "LC 215 sample 1");
        int[] s2 = {3, 2, 3, 1, 2, 4, 5, 5, 6};
        Check.eq(4, kthSmallest(s2, s2.length - 4), "LC 215 sample 2");
        Check.eq(new int[][] {{-2, 2}}, kClosest(new int[][] {{1, 3}, {-2, 2}}, 1), "LC 973 sample 1");
        int[][] c2 = kClosest(new int[][] {{3, 3}, {5, -1}, {-2, 4}}, 2);
        Check.isTrue(Arrays.deepEquals(c2, new int[][] {{3, 3}, {-2, 4}})
                || Arrays.deepEquals(c2, new int[][] {{-2, 4}, {3, 3}}), "LC 973 sample 2");
        Check.done("P03Quickselect");
    }
}
