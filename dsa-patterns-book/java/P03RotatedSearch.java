import java.util.*;

public class P03RotatedSearch {
    //@tpl search
    // LC 33: index of target in a rotated sorted array of distinct values, or -1.
    // One of [lo, mid] and [mid, hi] is always sorted; test the target on that one.
    static int search(int[] a, int target) {
        int lo = 0, hi = a.length - 1;            // closed: target in [lo, hi]
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] == target) return mid;
            if (a[lo] <= a[mid]) {                // left half [lo, mid] is sorted
                if (a[lo] <= target && target < a[mid]) hi = mid - 1;
                else lo = mid + 1;
            } else {                              // right half [mid, hi] is sorted
                if (a[mid] < target && target <= a[hi]) lo = mid + 1;
                else hi = mid - 1;
            }
        }
        return -1;
    }
    //@end

    //@tpl min
    // LC 153: minimum of a rotated sorted array of distinct values.
    // The minimum sits at the first index with a[i] <= a[n-1]: a lower bound.
    static int findMin(int[] a) {
        int last = a[a.length - 1];
        int lo = 0, hi = a.length - 1;            // index n-1 passes the test
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] > last) lo = mid + 1;      // mid in the upper (left) run
            else hi = mid;                        // mid in the lower run: min <= mid
        }
        return a[lo];
    }
    //@end

    //@tpl dup
    // LC 81: the same search when values repeat. If a[lo], a[mid] and a[hi]
    // are equal you cannot tell which half is sorted, so drop both ends.
    static boolean searchDup(int[] a, int target) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] == target) return true;
            if (a[lo] == a[mid] && a[mid] == a[hi]) { lo++; hi--; continue; }
            if (a[lo] <= a[mid]) {
                if (a[lo] <= target && target < a[mid]) hi = mid - 1;
                else lo = mid + 1;
            } else {
                if (a[mid] < target && target <= a[hi]) lo = mid + 1;
                else hi = mid - 1;
            }
        }
        return false;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // LC 154, quoted in the variants: compare with the moving a[hi]; on a tie drop a[hi].
    static int findMinDup(int[] a) {
        int lo = 0, hi = a.length - 1;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] > a[hi]) lo = mid + 1;
            else if (a[mid] < a[hi]) hi = mid;
            else hi--;
        }
        return a[lo];
    }

    // The trap: a[lo] < a[mid] instead of <=, wrong when lo == mid.
    static int searchStrictLeft(int[] a, int target) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] == target) return mid;
            if (a[lo] < a[mid]) {
                if (a[lo] <= target && target < a[mid]) hi = mid - 1;
                else lo = mid + 1;
            } else {
                if (a[mid] < target && target <= a[hi]) lo = mid + 1;
                else hi = mid - 1;
            }
        }
        return -1;
    }

    // The trap: a[lo] < target instead of <=, which skips a target sitting at a[lo].
    static int searchStrictRange(int[] a, int target) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] == target) return mid;
            if (a[lo] <= a[mid]) {
                if (a[lo] < target && target < a[mid]) hi = mid - 1;
                else lo = mid + 1;
            } else {
                if (a[mid] < target && target <= a[hi]) lo = mid + 1;
                else hi = mid - 1;
            }
        }
        return -1;
    }

    static int[] rotate(int[] s, int k) {
        int n = s.length;
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = s[(i + k) % n];
        return a;
    }

    static int[] distinctSorted(Random r, int n) {
        TreeSet<Integer> set = new TreeSet<>();
        while (set.size() < n) set.add(r.nextInt(41) - 20);
        int[] a = new int[n];
        int i = 0;
        for (int v : set) a[i++] = v;
        return a;
    }

    public static void main(String[] args) {
        Random r = new Random(33);
        for (int t = 0; t < 4000; t++) {
            int n = 1 + r.nextInt(14);
            int[] a = rotate(distinctSorted(r, n), r.nextInt(n));
            String ctx = Arrays.toString(a);
            int min = Integer.MAX_VALUE;
            for (int v : a) min = Math.min(min, v);
            Check.eq(min, findMin(a), "min " + ctx);
            for (int x = -22; x <= 22; x++) {
                int idx = -1;
                for (int i = 0; i < n; i++) if (a[i] == x) idx = i;
                Check.eq(idx, search(a, x), "search " + ctx + " x=" + x);
            }
            int[] d = rotate(Check.sortedInts(r, n, 0, 4), r.nextInt(n));
            int dmin = Integer.MAX_VALUE;
            for (int v : d) dmin = Math.min(dmin, v);
            Check.eq(dmin, findMinDup(d), "minDup " + Arrays.toString(d));
            for (int x = -1; x <= 5; x++) {
                boolean has = false;
                for (int v : d) has |= v == x;
                Check.eq(has, searchDup(d, x), "dup " + Arrays.toString(d) + " x=" + x);
            }
        }
        int[] s1 = {4, 5, 6, 7, 0, 1, 2};
        Check.eq(4, search(s1, 0), "LC 33 sample 1");
        Check.eq(-1, search(s1, 3), "LC 33 sample 2");
        Check.eq(-1, search(new int[] {1}, 0), "LC 33 sample 3");
        Check.eq(1, findMin(new int[] {3, 4, 5, 1, 2}), "LC 153 sample 1");
        Check.eq(0, findMin(s1), "LC 153 sample 2");
        Check.eq(11, findMin(new int[] {11, 13, 15, 17}), "LC 153 sample 3");
        Check.eq(true, searchDup(new int[] {2, 5, 6, 0, 0, 1, 2}, 0), "LC 81 sample 1");
        Check.eq(false, searchDup(new int[] {2, 5, 6, 0, 0, 1, 2}, 3), "LC 81 sample 2");
        // traps quoted in the book
        Check.eq(-1, searchStrictLeft(new int[] {3, 1}, 1), "a[lo] < a[mid] misses 1 in [3, 1]");
        Check.eq(-1, search(new int[] {1, 0, 1, 1, 1}, 0), "distinct-value code fails on duplicates");
        Check.eq(-1, searchStrictRange(new int[] {4, 5, 6, 7, 0, 1, 2}, 4), "a[lo] < target misses a[lo]");
        Check.eq(1, findMinDup(new int[] {1, 3, 5}), "LC 154 sample 1");
        Check.eq(0, findMinDup(new int[] {2, 2, 2, 0, 1}), "LC 154 sample 2");
        Check.done("P03RotatedSearch");
    }
}
