import java.util.*;

public class P01PrefixSum {
    //@tpl range
    // Range sum query: pre[i] = a[0] + ... + a[i-1], so pre[0] = 0
    // and the sum of a[l..r] is pre[r + 1] - pre[l].
    static class NumArray {
        private final long[] pre;

        NumArray(int[] a) {
            pre = new long[a.length + 1];
            for (int i = 0; i < a.length; i++) pre[i + 1] = pre[i] + a[i];
        }

        long sumRange(int l, int r) {              // inclusive l..r
            return pre[r + 1] - pre[l];
        }
    }
    //@end

    //@tpl pivot
    // Pivot index: sum left of i == sum right of i.
    // right = total - left - a[i], so one running sum is enough.
    static int pivotIndex(int[] a) {
        long total = 0, left = 0;
        for (int x : a) total += x;
        for (int i = 0; i < a.length; i++) {
            if (left == total - left - a[i]) return i;
            left += a[i];
        }
        return -1;
    }
    //@end

    //@tpl product
    // Product of array except self, without division:
    // ans[i] = (product of a[0..i-1]) * (product of a[i+1..n-1]).
    static int[] productExceptSelf(int[] a) {
        int n = a.length;
        int[] ans = new int[n];
        ans[0] = 1;           // 1. left products
        for (int i = 1; i < n; i++) ans[i] = ans[i - 1] * a[i - 1];
        int right = 1;        // 2. times right products
        for (int i = n - 1; i >= 0; i--) {
            ans[i] *= right;  // right = product of a[i+1..]
            right *= a[i];
        }
        return ans;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int brutePivot(int[] a) {
        for (int i = 0; i < a.length; i++) {
            long l = 0, rr = 0;
            for (int j = 0; j < i; j++) l += a[j];
            for (int j = i + 1; j < a.length; j++) rr += a[j];
            if (l == rr) return i;
        }
        return -1;
    }

    static int[] bruteProduct(int[] a) {
        int[] out = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            int p = 1;
            for (int j = 0; j < a.length; j++) if (j != i) p *= a[j];
            out[i] = p;
        }
        return out;
    }

    public static void main(String[] args) {
        Random r = new Random(303);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(12);
            int[] a = Check.ints(r, n, -9, 9);
            NumArray na = new NumArray(a);
            for (int q = 0; q < 10; q++) {
                int l = r.nextInt(n), rr = l + r.nextInt(n - l);
                long s = 0;
                for (int i = l; i <= rr; i++) s += a[i];
                Check.eq(s, na.sumRange(l, rr), "sumRange " + Arrays.toString(a) + " " + l + ".." + rr);
            }
            int[] b = Check.ints(r, n, -2, 2);
            Check.eq(brutePivot(b), pivotIndex(b), "pivot " + Arrays.toString(b));
            int[] c = Check.ints(r, Math.min(n, 8), -3, 3);
            if (c.length == 0) continue;
            Check.eq(bruteProduct(c), productExceptSelf(c), "product " + Arrays.toString(c));
        }
        int[] big = new int[100000];
        Arrays.fill(big, 100000);
        Check.eq(10_000_000_000L, new NumArray(big).sumRange(0, 99999), "long sums");
        NumArray lc = new NumArray(new int[] {-2, 0, 3, -5, 2, -1});
        Check.eq(1, lc.sumRange(0, 2), "LC 303 sample a");
        Check.eq(-1, lc.sumRange(2, 5), "LC 303 sample b");
        Check.eq(-3, lc.sumRange(0, 5), "LC 303 sample c");
        Check.eq(3, pivotIndex(new int[] {1, 7, 3, 6, 5, 6}), "LC 724 sample 1");
        Check.eq(-1, pivotIndex(new int[] {1, 2, 3}), "LC 724 sample 2");
        Check.eq(0, pivotIndex(new int[] {2, 1, -1}), "LC 724 sample 3");
        Check.eq(new int[] {24, 12, 8, 6}, productExceptSelf(new int[] {1, 2, 3, 4}), "LC 238 sample 1");
        Check.eq(new int[] {0, 0, 9, 0, 0}, productExceptSelf(new int[] {-1, 1, 0, -3, 3}), "LC 238 sample 2");
        Check.done("P01PrefixSum");
    }
}
