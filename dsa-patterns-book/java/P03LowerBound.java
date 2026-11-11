import java.util.*;

public class P03LowerBound {
    //@tpl bounds
    // First index i with a[i] >= x, or a.length if there is none.
    // Invariant: a[0, lo) < x and a[hi, n) >= x, so the answer is in [lo, hi].
    static int lowerBound(int[] a, int x) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;     // lo <= mid < hi, so a[mid] exists
            if (a[mid] < x) lo = mid + 1;     // mid and all left of it are < x
            else hi = mid;                    // a[mid] >= x: mid may be the answer
        }
        return lo;                            // lo == hi: the boundary
    }

    // First index i with a[i] > x. Only the test changes: < becomes <=.
    static int upperBound(int[] a, int x) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] <= x) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }
    //@end

    //@tpl uses
    // LC 34: first and last index of x, or {-1, -1}.
    static int[] searchRange(int[] a, int x) {
        int first = lowerBound(a, x);
        if (first == a.length || a[first] != x) return new int[] {-1, -1};
        return new int[] {first, upperBound(a, x) - 1};
    }

    // LC 35: index of x if present, else the index where it would be inserted.
    static int searchInsert(int[] a, int x) {
        return lowerBound(a, x);
    }

    // How many elements satisfy lo <= a[i] <= hi.
    static int countInRange(int[] a, int lo, int hi) {
        return upperBound(a, hi) - lowerBound(a, lo);
    }

    // Last index with a[i] <= x (the "floor" of x), or -1 if there is none.
    static int lastAtMost(int[] a, int x) {
        return upperBound(a, x) - 1;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int bruteLower(int[] a, int x) {
        for (int i = 0; i < a.length; i++) if (a[i] >= x) return i;
        return a.length;
    }

    static int bruteUpper(int[] a, int x) {
        for (int i = 0; i < a.length; i++) if (a[i] > x) return i;
        return a.length;
    }

    public static void main(String[] args) {
        Random r = new Random(31);
        for (int t = 0; t < 5000; t++) {
            int n = r.nextInt(15);
            int[] a = Check.sortedInts(r, n, -6, 6);
            for (int x = -8; x <= 8; x++) {
                String ctx = Arrays.toString(a) + " x=" + x;
                Check.eq(bruteLower(a, x), lowerBound(a, x), "lower " + ctx);
                Check.eq(bruteUpper(a, x), upperBound(a, x), "upper " + ctx);
                int first = -1, last = -1;
                for (int i = 0; i < n; i++) if (a[i] == x) { if (first < 0) first = i; last = i; }
                Check.eq(new int[] {first, last}, searchRange(a, x), "range " + ctx);
                Check.eq(bruteLower(a, x), searchInsert(a, x), "insert " + ctx);
                int floor = -1;
                for (int i = 0; i < n; i++) if (a[i] <= x) floor = i;
                Check.eq(floor, lastAtMost(a, x), "floor " + ctx);
                int y = x + r.nextInt(5), cnt = 0;
                for (int v : a) if (x <= v && v <= y) cnt++;
                Check.eq(cnt, countInRange(a, x, y), "count " + ctx + " y=" + y);
            }
        }
        // values near the int limits
        int[] big = {Integer.MIN_VALUE, -5, 0, Integer.MAX_VALUE, Integer.MAX_VALUE};
        Check.eq(3, lowerBound(big, Integer.MAX_VALUE), "big lower");
        Check.eq(5, upperBound(big, Integer.MAX_VALUE), "big upper");
        Check.eq(0, lowerBound(big, Integer.MIN_VALUE), "big lower min");
        // LeetCode samples
        Check.eq(new int[] {3, 4}, searchRange(new int[] {5, 7, 7, 8, 8, 10}, 8), "LC 34 sample 1");
        Check.eq(new int[] {-1, -1}, searchRange(new int[] {5, 7, 7, 8, 8, 10}, 6), "LC 34 sample 2");
        Check.eq(new int[] {-1, -1}, searchRange(new int[] {}, 0), "LC 34 sample 3");
        Check.eq(2, searchInsert(new int[] {1, 3, 5, 6}, 5), "LC 35 sample 1");
        Check.eq(1, searchInsert(new int[] {1, 3, 5, 6}, 2), "LC 35 sample 2");
        Check.eq(4, searchInsert(new int[] {1, 3, 5, 6}, 7), "LC 35 sample 3");
        // the Arrays.binarySearch trap quoted in the book
        Check.eq(-3, Arrays.binarySearch(new int[] {1, 3, 5, 6}, 4), "binarySearch not found");
        Check.eq(2, Arrays.binarySearch(new int[] {3, 3, 3, 3, 3}, 3), "binarySearch dup");
        Check.done("P03LowerBound");
    }
}
