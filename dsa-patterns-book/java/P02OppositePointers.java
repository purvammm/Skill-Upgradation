import java.util.*;

public class P02OppositePointers {
    //@tpl pair
    // Sorted array: find indices i < j with a[i] + a[j] == target.
    static int[] pairWithSum(int[] a, int target) {
        int l = 0, r = a.length - 1;
        while (l < r) {
            long s = (long) a[l] + a[r];
            if (s == target) return new int[] {l, r};
            if (s < target) l++;          // need a bigger sum
            else r--;                     // need a smaller sum
        }
        return new int[] {-1, -1};
    }
    //@end

    //@tpl water
    // Container with most water: always move the shorter wall inward.
    static int maxArea(int[] h) {
        int l = 0, r = h.length - 1, best = 0;
        while (l < r) {
            best = Math.max(best, Math.min(h[l], h[r]) * (r - l));
            if (h[l] < h[r]) l++;
            else r--;
        }
        return best;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean brutePairExists(int[] a, int target) {
        for (int i = 0; i < a.length; i++)
            for (int j = i + 1; j < a.length; j++)
                if (a[i] + a[j] == target) return true;
        return false;
    }

    static int bruteArea(int[] h) {
        int best = 0;
        for (int i = 0; i < h.length; i++)
            for (int j = i + 1; j < h.length; j++)
                best = Math.max(best, Math.min(h[i], h[j]) * (j - i));
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(11);
        for (int t = 0; t < 5000; t++) {
            int n = r.nextInt(12);
            int[] a = Check.sortedInts(r, n, -10, 10);
            int target = r.nextInt(31) - 15;
            int[] got = pairWithSum(a, target);
            boolean exists = brutePairExists(a, target);
            Check.eq(exists, got[0] >= 0, "exists " + Arrays.toString(a) + " t=" + target);
            if (exists) Check.isTrue(got[0] < got[1] && a[got[0]] + a[got[1]] == target, "valid pair");
            int[] h = Check.ints(r, n, 0, 12);
            Check.eq(bruteArea(h), maxArea(h), "area " + Arrays.toString(h));
        }
        Check.eq(49, maxArea(new int[] {1, 8, 6, 2, 5, 4, 8, 3, 7}), "LC 11 sample");
        Check.done("P02OppositePointers");
    }
}
