import java.util.*;

public class P02ExactlyK {
    //@tpl distinct
    // Subarrays with exactly k distinct values (LC 992).
    static long subarraysWithKDistinct(int[] a, int k) {
        return atMostK(a, k) - atMostK(a, k - 1);
    }

    // At most k distinct: the 2.7 window, but count every window ending at r.
    static long atMostK(int[] a, int k) {
        if (k < 0) return 0;
        Map<Integer, Integer> cnt = new HashMap<>();
        long count = 0;
        for (int l = 0, r = 0; r < a.length; r++) {
            cnt.merge(a[r], 1, Integer::sum);
            while (cnt.size() > k) {
                if (cnt.merge(a[l], -1, Integer::sum) == 0) cnt.remove(a[l]);
                l++;
            }
            count += r - l + 1;            // [l..r], [l+1..r], ..., [r..r]
        }
        return count;
    }
    //@end

    //@tpl sum
    // Binary array: subarrays with sum exactly goal (LC 930).
    static long numSubarraysWithSum(int[] a, int goal) {
        return atMostSum(a, goal) - atMostSum(a, goal - 1);
    }

    static long atMostSum(int[] a, int s) {         // needs a[i] >= 0
        if (s < 0) return 0;                         // goal = 0 asks for atMost(-1)
        long count = 0, sum = 0;
        for (int l = 0, r = 0; r < a.length; r++) {
            sum += a[r];
            while (sum > s) sum -= a[l++];
            count += r - l + 1;
        }
        return count;
    }

    // Subarrays with exactly k odd numbers (LC 1248): map x -> x & 1, reuse.
    static long numberOfNiceSubarrays(int[] a, int k) {
        int[] odd = new int[a.length];
        for (int i = 0; i < a.length; i++) odd[i] = a[i] & 1;
        return numSubarraysWithSum(odd, k);
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static long bruteDistinct(int[] a, int k) {
        long c = 0;
        for (int i = 0; i < a.length; i++) {
            Set<Integer> s = new HashSet<>();
            for (int j = i; j < a.length; j++) {
                s.add(a[j]);
                if (s.size() == k) c++;
            }
        }
        return c;
    }

    static long bruteSum(int[] a, int goal) {
        long c = 0;
        for (int i = 0; i < a.length; i++) {
            int s = 0;
            for (int j = i; j < a.length; j++) {
                s += a[j];
                if (s == goal) c++;
            }
        }
        return c;
    }

    static long bruteOdd(int[] a, int k) {
        long c = 0;
        for (int i = 0; i < a.length; i++) {
            int s = 0;
            for (int j = i; j < a.length; j++) {
                if (a[j] % 2 != 0) s++;
                if (s == k) c++;
            }
        }
        return c;
    }

    public static void main(String[] args) {
        Random r = new Random(31);
        for (int t = 0; t < 5000; t++) {
            int n = r.nextInt(15);
            int[] a = Check.ints(r, n, 1, 4);
            int k = r.nextInt(5);
            Check.eq(bruteDistinct(a, k), subarraysWithKDistinct(a, k), "distinct " + Arrays.toString(a) + " k=" + k);
            int[] b = Check.ints(r, n, 0, 1);
            int goal = r.nextInt(n + 1);
            Check.eq(bruteSum(b, goal), numSubarraysWithSum(b, goal), "binary " + Arrays.toString(b) + " g=" + goal);
            int[] c = Check.ints(r, n, 1, 9);
            int kk = r.nextInt(5);
            Check.eq(bruteOdd(c, kk), numberOfNiceSubarrays(c, kk), "nice " + Arrays.toString(c) + " k=" + kk);
        }
        Check.eq(7, subarraysWithKDistinct(new int[] {1, 2, 1, 2, 3}, 2), "LC 992 sample 1");
        Check.eq(3, subarraysWithKDistinct(new int[] {1, 2, 1, 3, 4}, 3), "LC 992 sample 2");
        Check.eq(4, numSubarraysWithSum(new int[] {1, 0, 1, 0, 1}, 2), "LC 930 sample 1");
        Check.eq(15, numSubarraysWithSum(new int[] {0, 0, 0, 0, 0}, 0), "LC 930 sample 2");
        Check.eq(2, numberOfNiceSubarrays(new int[] {1, 1, 2, 1, 1}, 3), "LC 1248 sample 1");
        Check.eq(0, numberOfNiceSubarrays(new int[] {2, 4, 6}, 1), "LC 1248 sample 2");
        Check.eq(16, numberOfNiceSubarrays(new int[] {2, 2, 2, 1, 2, 2, 1, 2, 2, 2}, 2), "LC 1248 sample 3");
        Check.done("P02ExactlyK");
    }
}
