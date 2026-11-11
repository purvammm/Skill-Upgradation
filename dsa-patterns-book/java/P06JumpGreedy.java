import java.util.*;

public class P06JumpGreedy {
    //@tpl canjump
    // Can you reach the last index? Invariant: every index <= far is reachable.
    static boolean canJump(int[] a) {
        int far = 0;
        for (int i = 0; i < a.length; i++) {
            if (i > far) return false;          // i is past every reachable index
            far = Math.max(far, i + a[i]);
        }
        return true;
    }
    //@end

    //@tpl jumps
    // Fewest jumps to the last index (LC 45 promises it is reachable).
    // Indices up to `end` need at most `jumps` jumps; `far` is the next layer's end.
    static int jump(int[] a) {
        int jumps = 0, end = 0, far = 0;
        for (int i = 0; i < a.length - 1; i++) {   // never jump from the last index
            far = Math.max(far, i + a[i]);
            if (i == end) {                        // layer finished: one more jump
                jumps++;
                end = far;
            }
        }
        return jumps;
    }
    //@end

    //@tpl taps
    // Fewest taps to water [0, n]; tap i covers [i - r[i], i + r[i]] (LC 1326).
    // reach[x] = farthest point a tap starting at x covers. Then layer as in jump.
    static int minTaps(int n, int[] r) {
        int[] reach = new int[n + 1];
        for (int i = 0; i <= n; i++) {
            int lo = Math.max(0, i - r[i]);
            reach[lo] = Math.max(reach[lo], Math.min(n, i + r[i]));
        }
        int taps = 0, end = 0, far = 0;
        for (int x = 0; x < n; x++) {
            far = Math.max(far, reach[x]);
            if (x == end) {                 // [0, end] is watered; extend it
                if (far <= x) return -1;    // nothing covers [x, x + 1]
                taps++;
                end = far;
            }
        }
        return taps;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean bruteCan(int[] a) {
        boolean[] ok = new boolean[a.length];
        ok[0] = true;
        for (int i = 0; i < a.length; i++)
            if (ok[i]) for (int j = i + 1; j <= i + a[i] && j < a.length; j++) ok[j] = true;
        return ok[a.length - 1];
    }

    static int bruteJump(int[] a) {
        int n = a.length;
        int[] dp = new int[n];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[0] = 0;
        for (int j = 1; j < n; j++)
            for (int i = 0; i < j; i++)
                if (dp[i] != Integer.MAX_VALUE && i + a[i] >= j) dp[j] = Math.min(dp[j], dp[i] + 1);
        return dp[n - 1];
    }

    // Fewest taps over all subsets. [0, n] is covered iff every [x, x + 1] lies
    // inside one tap's range (all endpoints are integers).
    static int bruteTaps(int n, int[] r) {
        int best = Integer.MAX_VALUE;
        for (int mask = 0; mask < (1 << (n + 1)); mask++) {
            boolean ok = true;
            for (int x = 0; x < n && ok; x++) {
                boolean cov = false;
                for (int i = 0; i <= n && !cov; i++)
                    if ((mask >> i & 1) == 1 && i - r[i] <= x && x + 1 <= i + r[i]) cov = true;
                ok = cov;
            }
            if (ok) best = Math.min(best, Integer.bitCount(mask));
        }
        return best == Integer.MAX_VALUE ? -1 : best;
    }

    public static void main(String[] args) {
        Random rnd = new Random(65);
        int reachable = 0;
        for (int t = 0; t < 5000; t++) {
            int n = 1 + rnd.nextInt(14);
            int[] a = Check.ints(rnd, n, 0, 4);
            boolean can = bruteCan(a);
            Check.eq(can, canJump(a), "canJump " + Arrays.toString(a));
            if (can) {
                reachable++;
                Check.eq(bruteJump(a), jump(a), "jump " + Arrays.toString(a));
            }
            int m = 1 + rnd.nextInt(9);
            int[] taps = Check.ints(rnd, m + 1, 0, 3);
            Check.eq(bruteTaps(m, taps), minTaps(m, taps), "taps n=" + m + " " + Arrays.toString(taps));
        }
        Check.isTrue(reachable > 1000, "enough reachable cases");
        Check.eq(true, canJump(new int[] {2, 3, 1, 1, 4}), "LC 55 sample 1");
        Check.eq(false, canJump(new int[] {3, 2, 1, 0, 4}), "LC 55 sample 2");
        Check.eq(2, jump(new int[] {2, 3, 1, 1, 4}), "LC 45 sample 1");
        Check.eq(2, jump(new int[] {2, 3, 0, 1, 4}), "LC 45 sample 2");
        Check.eq(0, jump(new int[] {0}), "single index");
        Check.eq(1, minTaps(5, new int[] {3, 4, 1, 1, 0, 0}), "LC 1326 sample 1");
        Check.eq(-1, minTaps(3, new int[] {0, 0, 0, 0}), "LC 1326 sample 2");
        Check.done("P06JumpGreedy");
    }
}
