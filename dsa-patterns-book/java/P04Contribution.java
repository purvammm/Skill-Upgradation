import java.util.*;

public class P04Contribution {
    //@tpl mins
    // Sum of min(sub) over all subarrays, modulo 1e9+7.
    // a[i] is the minimum of every subarray that starts in (left, i] and ends
    // in [i, right): left = previous value <= a[i], right = next value < a[i].
    static int sumSubarrayMins(int[] a) {
        final long MOD = 1_000_000_007L;
        int n = a.length;
        int[] left = new int[n], right = new int[n];
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            while (!st.isEmpty() && a[st.peek()] > a[i])
                right[st.pop()] = i;                 // strictly smaller on the right
            left[i] = st.isEmpty() ? -1 : st.peek(); // smaller or equal on the left
            st.push(i);
        }
        while (!st.isEmpty()) right[st.pop()] = n;   // no smaller value after it
        long sum = 0;
        for (int i = 0; i < n; i++)
            sum = (sum + (long) a[i] * (i - left[i]) * (right[i] - i)) % MOD;
        return (int) sum;
    }
    //@end

    //@tpl ranges
    // Sum of subarray ranges = sum of maxima - sum of minima. No modulo here,
    // so return long. max(sub) = -min(-sub), so one helper does both.
    static long subArrayRanges(int[] a) {
        int[] neg = new int[a.length];
        for (int i = 0; i < a.length; i++) neg[i] = -a[i];
        return -sumMins(neg) - sumMins(a);
    }

    // Same tie rule as above, but each index is counted when it is popped:
    // i is its next smaller, the new top is its previous smaller-or-equal.
    static long sumMins(int[] a) {
        int n = a.length;
        long sum = 0;
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i <= n; i++) {               // i == n pops everything
            while (!st.isEmpty() && (i == n || a[st.peek()] > a[i])) {
                int j = st.pop();
                int left = st.isEmpty() ? -1 : st.peek();
                sum += (long) a[j] * (j - left) * (i - j);
            }
            st.push(i);
        }
        return sum;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static long bruteMins(int[] a, long mod) {
        long sum = 0;
        for (int i = 0; i < a.length; i++) {
            int min = Integer.MAX_VALUE;
            for (int j = i; j < a.length; j++) {
                min = Math.min(min, a[j]);
                sum += min;
                if (mod > 0) sum %= mod;
            }
        }
        return sum;
    }

    static long bruteRanges(int[] a) {
        long sum = 0;
        for (int i = 0; i < a.length; i++) {
            int min = a[i], max = a[i];
            for (int j = i; j < a.length; j++) {
                min = Math.min(min, a[j]);
                max = Math.max(max, a[j]);
                sum += (long) max - min;
            }
        }
        return sum;
    }

    public static void main(String[] args) {
        Random r = new Random(45);
        for (int t = 0; t < 4000; t++) {
            int n = r.nextInt(13);
            int[] a = Check.ints(r, n, 1, r.nextBoolean() ? 4 : 30000);
            Check.eq(bruteMins(a, 1_000_000_007L), sumSubarrayMins(a), "mins " + Arrays.toString(a));
            Check.eq(bruteMins(a, 0), sumMins(a), "sumMins " + Arrays.toString(a));
            int[] b = Check.ints(r, n, -5, 5);
            if (r.nextInt(3) == 0) for (int i = 0; i < n; i++) b[i] = r.nextInt(2_000_000_001) - 1_000_000_000;
            Check.eq(bruteRanges(b), subArrayRanges(b), "ranges " + Arrays.toString(b));
        }
        for (int t = 0; t < 5; t++) {    // big enough that the modulo matters
            int[] a = Check.ints(r, 3000, 1, 30000);
            Check.eq(bruteMins(a, 1_000_000_007L), sumSubarrayMins(a), "mins n=3000");
        }
        int[] worst = new int[30000];
        Arrays.fill(worst, 30000);
        long pairs = 30000L * 30001 / 2;              // every subarray has min 30000
        Check.eq(30000L * pairs % 1_000_000_007L, sumSubarrayMins(worst), "limits: all equal, n=3e4");
        Check.eq(17, sumSubarrayMins(new int[] {3, 1, 2, 4}), "LC 907 sample 1");
        Check.eq(444, sumSubarrayMins(new int[] {11, 81, 94, 43, 3}), "LC 907 sample 2");
        Check.eq(13, sumSubarrayMins(new int[] {3, 1, 2, 1}), "trace example");
        Check.eq(4L, subArrayRanges(new int[] {1, 2, 3}), "LC 2104 sample 1");
        Check.eq(4L, subArrayRanges(new int[] {1, 3, 3}), "LC 2104 sample 2");
        Check.eq(59L, subArrayRanges(new int[] {4, -2, -3, 4, 1}), "LC 2104 sample 3");
        Check.done("P04Contribution");
    }
}
