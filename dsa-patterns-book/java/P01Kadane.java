import java.util.*;

public class P01Kadane {
    //@tpl max
    // Kadane: largest sum of a non-empty subarray.
    // cur = best sum of a subarray that ends exactly at i.
    static int maxSubArray(int[] a) {
        int cur = a[0], best = a[0];
        for (int i = 1; i < a.length; i++) {
            cur = Math.max(a[i], cur + a[i]);   // start fresh at i, or extend
            best = Math.max(best, cur);
        }
        return best;
    }
    //@end

    //@tpl circular
    // Max circular subarray. The best one either does not wrap (plain
    // Kadane) or wraps, and then it is total minus a minimum middle part.
    static int maxSubarraySumCircular(int[] a) {
        int total = 0, curMax = 0, bestMax = a[0], curMin = 0, bestMin = a[0];
        for (int x : a) {
            curMax = Math.max(x, curMax + x);
            bestMax = Math.max(bestMax, curMax);
            curMin = Math.min(x, curMin + x);
            bestMin = Math.min(bestMin, curMin);
            total += x;
        }
        // All negative: total - bestMin = 0 is the empty subarray, not allowed.
        return bestMax < 0 ? bestMax : Math.max(bestMax, total - bestMin);
    }
    //@end

    //@tpl product
    // Max product subarray. A negative x turns the smallest product into
    // the largest, so track both for subarrays ending at i.
    static int maxProduct(int[] a) {
        int hi = a[0], lo = a[0], best = a[0];
        for (int i = 1; i < a.length; i++) {
            int x = a[i];
            if (x < 0) { int t = hi; hi = lo; lo = t; }   // swap before use
            hi = Math.max(x, hi * x);
            lo = Math.min(x, lo * x);
            best = Math.max(best, hi);
        }
        return best;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int bruteMax(int[] a) {
        int best = Integer.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            int s = 0;
            for (int j = i; j < a.length; j++) {
                s += a[j];
                best = Math.max(best, s);
            }
        }
        return best;
    }

    static int bruteCircular(int[] a) {
        int n = a.length, best = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            int s = 0;
            for (int len = 1; len <= n; len++) {
                s += a[(i + len - 1) % n];
                best = Math.max(best, s);
            }
        }
        return best;
    }

    static long bruteProduct(int[] a) {
        long best = Long.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            long p = 1;
            for (int j = i; j < a.length; j++) {
                p *= a[j];
                best = Math.max(best, p);
            }
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(707);
        for (int t = 0; t < 5000; t++) {
            int n = 1 + r.nextInt(12);
            int[] a = Check.ints(r, n, -9, 9);
            if (t % 5 == 0) a = Check.ints(r, n, -9, -1);           // all negative
            Check.eq(bruteMax(a), maxSubArray(a), "max " + Arrays.toString(a));
            Check.eq(bruteCircular(a), maxSubarraySumCircular(a), "circular " + Arrays.toString(a));
            int[] p = Check.ints(r, Math.min(n, 10), -4, 4);
            Check.eq(bruteProduct(p), maxProduct(p), "product " + Arrays.toString(p));
        }
        Check.eq(6, maxSubArray(new int[] {-2, 1, -3, 4, -1, 2, 1, -5, 4}), "LC 53 sample 1");
        Check.eq(1, maxSubArray(new int[] {1}), "LC 53 sample 2");
        Check.eq(23, maxSubArray(new int[] {5, 4, -1, 7, 8}), "LC 53 sample 3");
        Check.eq(3, maxSubarraySumCircular(new int[] {1, -2, 3, -2}), "LC 918 sample 1");
        Check.eq(10, maxSubarraySumCircular(new int[] {5, -3, 5}), "LC 918 sample 2");
        Check.eq(-2, maxSubarraySumCircular(new int[] {-3, -2, -3}), "LC 918 sample 3");
        Check.eq(6, maxProduct(new int[] {2, 3, -2, 4}), "LC 152 sample 1");
        Check.eq(0, maxProduct(new int[] {-2, 0, -1}), "LC 152 sample 2");
        Check.done("P01Kadane");
    }
}
