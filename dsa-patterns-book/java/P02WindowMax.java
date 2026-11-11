import java.util.*;

public class P02WindowMax {
    //@tpl max
    // Max of every window of size k (LC 239). The deque holds indices of the
    // window whose values strictly decrease from front to back: front = max.
    static int[] maxSlidingWindow(int[] a, int k) {
        int[] out = new int[a.length - k + 1];
        Deque<Integer> dq = new ArrayDeque<>();
        for (int r = 0; r < a.length; r++) {
            if (!dq.isEmpty() && dq.peekFirst() <= r - k)
                dq.pollFirst();                     // 1. front left the window
            while (!dq.isEmpty() && a[dq.peekLast()] <= a[r])
                dq.pollLast();                      // 2. can never be a max again
            dq.addLast(r);                          // 3. r joins at the back
            if (r >= k - 1) out[r - k + 1] = a[dq.peekFirst()];
        }
        return out;
    }
    //@end

    //@tpl shortest
    // Shortest subarray with sum >= k; values may be negative (LC 862).
    static int shortestSubarray(int[] a, long k) {
        int n = a.length, best = n + 1;
        long[] p = new long[n + 1];                   // p[i] = a[0] + ... + a[i-1]
        for (int i = 0; i < n; i++) p[i + 1] = p[i] + a[i];
        Deque<Integer> dq = new ArrayDeque<>();       // start indices, p increasing
        for (int j = 0; j <= n; j++) {
            while (!dq.isEmpty() && p[j] - p[dq.peekFirst()] >= k)
                best = Math.min(best, j - dq.pollFirst()); // a later j is only longer
            while (!dq.isEmpty() && p[dq.peekLast()] >= p[j])
                dq.pollLast();                        // j is a later and lower start
            dq.addLast(j);
        }
        return best <= n ? best : -1;
    }
    //@end

    //@tpl dp
    // Max sum of a non-empty subsequence whose chosen neighbours are at most
    // k apart (LC 1425). dp[i] = a[i] + max(0, max of dp[i-k..i-1]).
    static int constrainedSubsetSum(int[] a, int k) {
        int[] dp = new int[a.length];
        Deque<Integer> dq = new ArrayDeque<>();       // indices, dp decreasing
        int best = Integer.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            if (!dq.isEmpty() && dq.peekFirst() < i - k) dq.pollFirst();
            dp[i] = a[i] + (dq.isEmpty() ? 0 : Math.max(0, dp[dq.peekFirst()]));
            while (!dq.isEmpty() && dp[dq.peekLast()] <= dp[i]) dq.pollLast();
            dq.addLast(i);
            best = Math.max(best, dp[i]);
        }
        return best;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int[] bruteMax(int[] a, int k) {
        int[] out = new int[a.length - k + 1];
        for (int i = 0; i + k <= a.length; i++) {
            int m = Integer.MIN_VALUE;
            for (int j = i; j < i + k; j++) m = Math.max(m, a[j]);
            out[i] = m;
        }
        return out;
    }

    static int bruteShortest(int[] a, long k) {
        int best = -1;
        for (int i = 0; i < a.length; i++) {
            long s = 0;
            for (int j = i; j < a.length; j++) {
                s += a[j];
                if (s >= k && (best < 0 || j - i + 1 < best)) best = j - i + 1;
            }
        }
        return best;
    }

    static int bruteConstrained(int[] a, int k) {     // every non-empty subset
        int n = a.length, best = Integer.MIN_VALUE;
        for (int mask = 1; mask < (1 << n); mask++) {
            int last = -1, sum = 0;
            boolean ok = true;
            for (int i = 0; i < n; i++) {
                if ((mask >> i & 1) == 0) continue;
                if (last >= 0 && i - last > k) { ok = false; break; }
                sum += a[i];
                last = i;
            }
            if (ok) best = Math.max(best, sum);
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(35);
        for (int t = 0; t < 4000; t++) {
            int n = 1 + r.nextInt(12);
            int[] a = Check.ints(r, n, -6, 6);
            int k = 1 + r.nextInt(n);
            Check.eq(bruteMax(a, k), maxSlidingWindow(a, k), "max " + Arrays.toString(a) + " k=" + k);
            long target = r.nextInt(25) - 4;
            Check.eq(bruteShortest(a, target), shortestSubarray(a, target), "shortest " + Arrays.toString(a) + " k=" + target);
            Check.eq(bruteConstrained(a, k), constrainedSubsetSum(a, k), "constrained " + Arrays.toString(a) + " k=" + k);
        }
        Check.eq(new int[] {3, 3, 5, 5, 6, 7}, maxSlidingWindow(new int[] {1, 3, -1, -3, 5, 3, 6, 7}, 3), "LC 239 sample 1");
        Check.eq(new int[] {1}, maxSlidingWindow(new int[] {1}, 1), "LC 239 sample 2");
        Check.eq(1, shortestSubarray(new int[] {1}, 1), "LC 862 sample 1");
        Check.eq(-1, shortestSubarray(new int[] {1, 2}, 4), "LC 862 sample 2");
        Check.eq(3, shortestSubarray(new int[] {2, -1, 2}, 3), "LC 862 sample 3");
        Check.eq(3, shortestSubarray(new int[] {84, -37, 32, 40, 95}, 167), "LC 862 negatives");
        Check.eq(37, constrainedSubsetSum(new int[] {10, 2, -10, 5, 20}, 2), "LC 1425 sample 1");
        Check.eq(-1, constrainedSubsetSum(new int[] {-1, -2, -3}, 1), "LC 1425 sample 2");
        Check.eq(23, constrainedSubsetSum(new int[] {10, -2, -10, -5, 20}, 2), "LC 1425 sample 3");
        Check.done("P02WindowMax");
    }
}
