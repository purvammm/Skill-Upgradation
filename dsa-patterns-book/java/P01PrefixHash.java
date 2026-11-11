import java.util.*;

public class P01PrefixHash {
    //@tpl count
    // Number of subarrays with sum k. The subarray after prefix j and up to
    // i has sum pre_i - pre_j, so at each i count earlier pre_j == pre_i - k.
    static int subarraySum(int[] a, int k) {
        Map<Integer, Integer> seen = new HashMap<>();  // prefix sum -> times seen
        seen.put(0, 1);                                // the empty prefix
        int pre = 0, count = 0;
        for (int x : a) {
            pre += x;
            count += seen.getOrDefault(pre - k, 0);    // 1. count first
            seen.merge(pre, 1, Integer::sum);          // 2. then add pre_i
        }
        return count;
    }
    //@end

    //@tpl modk
    // Continuous subarray sum: length >= 2 and sum a multiple of k (a[i] >= 0).
    // Two prefixes with the same remainder enclose a multiple of k.
    static boolean checkSubarraySum(int[] a, int k) {
        Map<Integer, Integer> first = new HashMap<>();  // remainder -> first index
        first.put(0, -1);                               // empty prefix "ends" at -1
        int rem = 0;
        for (int i = 0; i < a.length; i++) {
            rem = (int) ((rem + (long) a[i]) % k);      // long: may pass 2^31
            Integer j = first.putIfAbsent(rem, i);      // keeps the earliest index
            if (j != null && i - j >= 2) return true;
        }
        return false;
    }
    //@end

    //@tpl balance
    // Contiguous array: longest subarray with as many 0s as 1s.
    // Count 0 as -1; the subarray is balanced when two prefixes are equal.
    static int findMaxLength(int[] a) {
        Map<Integer, Integer> first = new HashMap<>();  // balance -> first index
        first.put(0, -1);
        int bal = 0, best = 0;
        for (int i = 0; i < a.length; i++) {
            bal += a[i] == 1 ? 1 : -1;
            Integer j = first.putIfAbsent(bal, i);
            if (j != null) best = Math.max(best, i - j);
        }
        return best;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int bruteCount(int[] a, int k) {
        int c = 0;
        for (int i = 0; i < a.length; i++) {
            int s = 0;
            for (int j = i; j < a.length; j++) {
                s += a[j];
                if (s == k) c++;
            }
        }
        return c;
    }

    static boolean bruteModK(int[] a, int k) {
        for (int i = 0; i < a.length; i++) {
            long s = a[i];
            for (int j = i + 1; j < a.length; j++) {
                s += a[j];
                if (s % k == 0) return true;
            }
        }
        return false;
    }

    static int bruteBalance(int[] a) {
        int best = 0;
        for (int i = 0; i < a.length; i++) {
            int z = 0, o = 0;
            for (int j = i; j < a.length; j++) {
                if (a[j] == 1) o++;
                else z++;
                if (z == o) best = Math.max(best, j - i + 1);
            }
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(404);
        for (int t = 0; t < 5000; t++) {
            int n = r.nextInt(14);
            int[] a = Check.ints(r, n, -4, 4);
            int k = r.nextInt(11) - 5;
            Check.eq(bruteCount(a, k), subarraySum(a, k), "subarraySum " + Arrays.toString(a) + " k=" + k);
            int[] p = Check.ints(r, n, 0, 15);
            int m = 1 + r.nextInt(12);
            Check.eq(bruteModK(p, m), checkSubarraySum(p, m), "modk " + Arrays.toString(p) + " k=" + m);
            int[] b = Check.ints(r, n, 0, 1);
            Check.eq(bruteBalance(b), findMaxLength(b), "balance " + Arrays.toString(b));
        }
        // large values and a large k: rem + a[i] passes 2^31
        for (int t = 0; t < 500; t++) {
            int[] p = Check.ints(r, 1 + r.nextInt(8), 0, 1_000_000_000);
            int m = Integer.MAX_VALUE - r.nextInt(1000);
            Check.eq(bruteModK(p, m), checkSubarraySum(p, m), "modk big " + Arrays.toString(p));
            // a block of three values that sums to exactly m, after a random start
            int[] q = new int[p.length + 3];
            System.arraycopy(p, 0, q, 0, p.length);
            int x = m / 3, y = m / 3, z = m - x - y;
            q[p.length] = x;
            q[p.length + 1] = y;
            q[p.length + 2] = z;
            Check.eq(true, bruteModK(q, m), "brute finds the block");
            Check.eq(true, checkSubarraySum(q, m), "modk block " + Arrays.toString(q));
        }
        Check.eq(2, subarraySum(new int[] {1, 1, 1}, 2), "LC 560 sample 1");
        Check.eq(2, subarraySum(new int[] {1, 2, 3}, 3), "LC 560 sample 2");
        Check.eq(true, checkSubarraySum(new int[] {23, 2, 4, 6, 7}, 6), "LC 523 sample 1");
        Check.eq(true, checkSubarraySum(new int[] {23, 2, 6, 4, 7}, 6), "LC 523 sample 2");
        Check.eq(false, checkSubarraySum(new int[] {23, 2, 6, 4, 7}, 13), "LC 523 sample 3");
        Check.eq(2, findMaxLength(new int[] {0, 1}), "LC 525 sample 1");
        Check.eq(2, findMaxLength(new int[] {0, 1, 0}), "LC 525 sample 2");
        Check.eq(6, findMaxLength(new int[] {0, 1, 1, 1, 1, 1, 0, 0, 0}), "LC 525 sample 3");
        Check.done("P01PrefixHash");
    }
}
