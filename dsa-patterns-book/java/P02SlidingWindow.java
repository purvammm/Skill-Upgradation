import java.util.*;

public class P02SlidingWindow {
    //@tpl longest
    // Longest subarray with at most k distinct values.
    // Grow r; while the window breaks the rule, shrink l; then record.
    static int longestAtMostKDistinct(int[] a, int k) {
        Map<Integer, Integer> cnt = new HashMap<>();
        int best = 0;
        for (int l = 0, r = 0; r < a.length; r++) {
            cnt.merge(a[r], 1, Integer::sum);              // 1. add a[r]
            while (cnt.size() > k) {                       // 2. shrink while invalid
                if (cnt.merge(a[l], -1, Integer::sum) == 0) cnt.remove(a[l]);
                l++;
            }
            best = Math.max(best, r - l + 1);              // 3. [l, r] is valid
        }
        return best;
    }
    //@end

    //@tpl shortest
    // Shortest subarray with sum >= target (all values positive).
    // Record inside the loop, because a valid window gets shorter as l moves.
    static int shortestSumAtLeast(int[] a, long target) {
        int best = Integer.MAX_VALUE;
        long sum = 0;
        for (int l = 0, r = 0; r < a.length; r++) {
            sum += a[r];
            while (sum >= target) {
                best = Math.min(best, r - l + 1);
                sum -= a[l++];
            }
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int bruteLongest(int[] a, int k) {
        int best = 0;
        for (int i = 0; i < a.length; i++) {
            Set<Integer> s = new HashSet<>();
            for (int j = i; j < a.length; j++) {
                s.add(a[j]);
                if (s.size() <= k) best = Math.max(best, j - i + 1);
            }
        }
        return best;
    }

    static int bruteShortest(int[] a, long target) {
        int best = 0;
        for (int i = 0; i < a.length; i++) {
            long sum = 0;
            for (int j = i; j < a.length; j++) {
                sum += a[j];
                if (sum >= target && (best == 0 || j - i + 1 < best)) best = j - i + 1;
            }
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(7);
        for (int t = 0; t < 4000; t++) {
            int n = r.nextInt(14);
            int[] a = Check.ints(r, n, 0, 5);
            int k = r.nextInt(5);
            Check.eq(bruteLongest(a, k), longestAtMostKDistinct(a, k), "longest " + Arrays.toString(a) + " k=" + k);
            int[] p = Check.ints(r, n, 1, 9);
            long target = 1 + r.nextInt(40);
            Check.eq(bruteShortest(p, target), shortestSumAtLeast(p, target), "shortest " + Arrays.toString(p));
        }
        Check.eq(3, longestAtMostKDistinct(new int[] {1, 2, 1, 3, 3, 2}, 2), "doc example");
        Check.eq(2, shortestSumAtLeast(new int[] {2, 3, 1, 2, 4, 3}, 7), "LC 209 sample");
        Check.done("P02SlidingWindow");
    }
}
