import java.util.*;

public class P03MergeSort {
    //@tpl sort
    // Sorts a[lo, hi) and returns its inversions: pairs i < j with a[i] > a[j].
    // Call sortCount(a, new int[a.length], 0, a.length). LC 912 is the sort alone.
    static long sortCount(int[] a, int[] tmp, int lo, int hi) {
        if (hi - lo < 2) return 0;
        int mid = lo + (hi - lo) / 2;
        long inv = sortCount(a, tmp, lo, mid) + sortCount(a, tmp, mid, hi);
        int i = lo, j = mid, k = lo;              // both halves are sorted now
        while (i < mid || j < hi) {
            if (j == hi || (i < mid && a[i] <= a[j])) tmp[k++] = a[i++];
            else {
                inv += mid - i;                   // a[j] < every a[i..mid)
                tmp[k++] = a[j++];
            }
        }
        System.arraycopy(tmp, lo, a, lo, hi - lo);
        return inv;
    }
    //@end

    //@tpl smaller
    // LC 315: for each i, count j > i with a[j] < a[i]. Merge-sort the indices by
    // value; right indices placed before a left one are smaller and to its right.
    static List<Integer> countSmaller(int[] a) {
        int n = a.length;
        int[] idx = new int[n], tmp = new int[n], cnt = new int[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        sortIdx(a, idx, tmp, cnt, 0, n);
        List<Integer> out = new ArrayList<>();
        for (int c : cnt) out.add(c);
        return out;
    }

    static void sortIdx(int[] a, int[] idx, int[] tmp, int[] cnt, int lo, int hi) {
        if (hi - lo < 2) return;
        int mid = lo + (hi - lo) / 2;
        sortIdx(a, idx, tmp, cnt, lo, mid); sortIdx(a, idx, tmp, cnt, mid, hi);
        int i = lo, j = mid, k = lo;
        while (i < mid || j < hi) {
            if (j == hi || (i < mid && a[idx[i]] <= a[idx[j]])) {
                cnt[idx[i]] += j - mid;           // right items already placed
                tmp[k++] = idx[i++];
            } else tmp[k++] = idx[j++];
        }
        System.arraycopy(tmp, lo, idx, lo, hi - lo);
    }
    //@end

    //@tpl reverse
    // LC 493: pairs i < j with a[i] > 2 * a[j]. Both halves are sorted, so count
    // across them with two pointers first, then merge as usual.
    static int reversePairs(int[] a, int[] tmp, int lo, int hi) {
        if (hi - lo < 2) return 0;
        int mid = lo + (hi - lo) / 2;
        int count = reversePairs(a, tmp, lo, mid) + reversePairs(a, tmp, mid, hi);
        for (int i = lo, j = mid; i < mid; i++) {
            while (j < hi && a[i] > 2L * a[j]) j++;   // 2L: 2 * a[j] overflows int
            count += j - mid;
        }
        merge(a, tmp, lo, mid, hi);
        return count;
    }

    static void merge(int[] a, int[] tmp, int lo, int mid, int hi) {
        int i = lo, j = mid, k = lo;
        while (i < mid || j < hi)
            tmp[k++] = (j == hi || (i < mid && a[i] <= a[j])) ? a[i++] : a[j++];
        System.arraycopy(tmp, lo, a, lo, hi - lo);
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static long bruteInversions(int[] a) {
        long c = 0;
        for (int i = 0; i < a.length; i++) for (int j = i + 1; j < a.length; j++) if (a[i] > a[j]) c++;
        return c;
    }

    static List<Integer> bruteSmaller(int[] a) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < a.length; i++) {
            int c = 0;
            for (int j = i + 1; j < a.length; j++) if (a[j] < a[i]) c++;
            out.add(c);
        }
        return out;
    }

    static int bruteReverse(int[] a) {
        int c = 0;
        for (int i = 0; i < a.length; i++) for (int j = i + 1; j < a.length; j++) if (a[i] > 2L * a[j]) c++;
        return c;
    }

    // The trap in the book: < instead of <= takes equal right items first.
    static long sortCountStrict(int[] a, int[] tmp, int lo, int hi) {
        if (hi - lo < 2) return 0;
        int mid = lo + (hi - lo) / 2;
        long inv = sortCountStrict(a, tmp, lo, mid) + sortCountStrict(a, tmp, mid, hi);
        int i = lo, j = mid, k = lo;
        while (i < mid || j < hi) {
            if (j == hi || (i < mid && a[i] < a[j])) tmp[k++] = a[i++];
            else { inv += mid - i; tmp[k++] = a[j++]; }
        }
        System.arraycopy(tmp, lo, a, lo, hi - lo);
        return inv;
    }

    public static void main(String[] args) {
        Random r = new Random(47);
        for (int t = 0; t < 5000; t++) {
            int n = r.nextInt(16);
            int[] a = Check.ints(r, n, -6, 6);
            int[] s = a.clone(), sorted = a.clone();
            Arrays.sort(sorted);
            Check.eq(bruteInversions(a), sortCount(s, new int[n], 0, n), "inv " + Arrays.toString(a));
            Check.eq(sorted, s, "sorted " + Arrays.toString(a));
            Check.eq(bruteSmaller(a), countSmaller(a), "smaller " + Arrays.toString(a));
            int[] big = new int[n];
            for (int i = 0; i < n; i++) big[i] = r.nextInt(3) == 0 ? r.nextInt() : r.nextInt(13) - 6;
            int[] b2 = big.clone();
            Check.eq(bruteReverse(big), reversePairs(b2, new int[n], 0, n), "reverse " + Arrays.toString(big));
            Arrays.sort(big);
            Check.eq(big, b2, "reverse sorts");
        }
        // size and speed: 2e5 values, inversions past 2^31
        int n = 200000;
        int[] desc = new int[n];
        for (int i = 0; i < n; i++) desc[i] = n - i;
        Check.eq((long) n * (n - 1) / 2, sortCount(desc, new int[n], 0, n), "descending");
        Check.isTrue(sortCountStrict(new int[] {2, 2}, new int[2], 0, 2) == 1, "< counts equal pairs");
        // LeetCode samples
        int[] s912 = {5, 2, 3, 1};
        sortCount(s912, new int[4], 0, 4);
        Check.eq(new int[] {1, 2, 3, 5}, s912, "LC 912 sample 1");
        int[] s912b = {5, 1, 1, 2, 0, 0};
        sortCount(s912b, new int[6], 0, 6);
        Check.eq(new int[] {0, 0, 1, 1, 2, 5}, s912b, "LC 912 sample 2");
        Check.eq(Arrays.asList(2, 1, 1, 0), countSmaller(new int[] {5, 2, 6, 1}), "LC 315 sample 1");
        Check.eq(Arrays.asList(0), countSmaller(new int[] {-1}), "LC 315 sample 2");
        Check.eq(Arrays.asList(0, 0), countSmaller(new int[] {-1, -1}), "LC 315 sample 3");
        Check.eq(2, reversePairs(new int[] {1, 3, 2, 3, 1}, new int[5], 0, 5), "LC 493 sample 1");
        Check.eq(3, reversePairs(new int[] {2, 4, 3, 5, 1}, new int[5], 0, 5), "LC 493 sample 2");
        Check.done("P03MergeSort");
    }
}
