import java.util.*;

public class P03CoordCompress {
    //@tpl compress
    // Replace each value by its rank 0..m-1 among the distinct values.
    // Order is kept: a[i] < a[j] exactly when rank[i] < rank[j].
    static int[] compress(int[] a) {
        int[] vals = a.clone();
        Arrays.sort(vals);                                 // 1. sort
        int m = 0;
        for (int i = 0; i < vals.length; i++)              // 2. unique, in place
            if (i == 0 || vals[i] != vals[i - 1]) vals[m++] = vals[i];
        int[] rank = new int[a.length];
        for (int i = 0; i < a.length; i++)                 // 3. binary search
            rank[i] = Arrays.binarySearch(vals, 0, m, a[i]);   // present, so >= 0
        return rank;
    }
    //@end

    //@tpl fenwick
    // Inversions with a Fenwick tree indexed by rank. Values may be up to 1e9
    // or negative; ranks are < n, so the tree needs n + 1 slots, not 1e9.
    static long inversions(int[] a) {
        int[] rank = compress(a);
        int n = a.length;
        int[] bit = new int[n + 1];               // slot r + 1 counts rank r
        long inv = 0;
        for (int i = n - 1; i >= 0; i--) {        // right to left
            // how many ranks below rank[i] are in the tree; then add rank[i]
            for (int p = rank[i]; p > 0; p -= p & -p) inv += bit[p];
            for (int p = rank[i] + 1; p <= n; p += p & -p) bit[p]++;
        }
        return inv;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // LC 1331, quoted in the variants: ranks start at 1.
    static int[] arrayRankTransform(int[] arr) {
        int[] r = compress(arr);
        for (int i = 0; i < r.length; i++) r[i]++;
        return r;
    }

    public static void main(String[] args) {
        Random r = new Random(61);
        for (int t = 0; t < 5000; t++) {
            int n = r.nextInt(15);
            int[] a = r.nextBoolean() ? Check.ints(r, n, -5, 5) : Check.ints(r, n, -1000000000, 1000000000);
            int[] got = compress(a);
            for (int i = 0; i < n; i++) {
                Set<Integer> smaller = new HashSet<>();
                for (int v : a) if (v < a[i]) smaller.add(v);
                Check.eq(smaller.size(), got[i], "rank " + Arrays.toString(a));
            }
            long inv = 0;
            for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++) if (a[i] > a[j]) inv++;
            Check.eq(inv, inversions(a), "inversions " + Arrays.toString(a));
        }
        int n = 200000;
        int[] desc = new int[n];
        for (int i = 0; i < n; i++) desc[i] = 1000000000 - 5000 * i;
        Check.eq((long) n * (n - 1) / 2, inversions(desc), "big descending");
        int[] ext = {Integer.MAX_VALUE, Integer.MIN_VALUE, 0, Integer.MIN_VALUE};
        Check.eq(new int[] {2, 0, 1, 0}, compress(ext), "extremes");
        // LeetCode samples
        Check.eq(new int[] {4, 1, 2, 3}, arrayRankTransform(new int[] {40, 10, 20, 30}), "LC 1331 sample 1");
        Check.eq(new int[] {1, 1, 1}, arrayRankTransform(new int[] {100, 100, 100}), "LC 1331 sample 2");
        Check.eq(new int[] {5, 3, 4, 2, 8, 6, 7, 1, 3},
                arrayRankTransform(new int[] {37, 12, 28, 9, 100, 56, 80, 5, 12}), "LC 1331 sample 3");
        // the trap: no unique step leaves gaps in the ranks
        int[] q = {40, 10, 10, 30}, sortedQ = q.clone(), gappy = new int[4];
        Arrays.sort(sortedQ);
        for (int i = 0; i < 4; i++) gappy[i] = Arrays.binarySearch(sortedQ, q[i]) + 1;
        Check.eq(new int[] {4, 2, 2, 3}, gappy, "no unique step");
        Check.done("P03CoordCompress");
    }
}
