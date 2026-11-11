import java.util.*;

public class P10PartitionDp {
    //@tpl split
    // Split array largest sum: cut a into k contiguous groups and minimise the
    // largest group sum. dp[g][i] = best for the first i elements in g groups;
    // the last group is a[j..i-1].
    static int splitArray(int[] a, int k) {
        int n = a.length, INF = Integer.MAX_VALUE;
        int[][] dp = new int[k + 1][n + 1];
        for (int[] row : dp) Arrays.fill(row, INF);
        dp[0][0] = 0;
        for (int g = 1; g <= k; g++)
            for (int i = g; i <= n; i++) {
                int last = 0;                            // sum of a[j..i-1]
                for (int j = i - 1; j >= g - 1; j--) {   // g-1 groups need j >= g-1
                    last += a[j];
                    dp[g][i] = Math.min(dp[g][i], Math.max(dp[g - 1][j], last));
                }
            }
        return dp[k][n];
    }
    //@end

    //@tpl difficulty
    // Minimum difficulty of a job schedule: d days, jobs in order, at least one
    // a day; a day costs its hardest job. Same table; groups add up.
    static int minDifficulty(int[] jobs, int d) {
        int n = jobs.length, INF = Integer.MAX_VALUE / 2;   // INF + cost: no overflow
        if (n < d) return -1;
        int[][] dp = new int[d + 1][n + 1];
        for (int[] row : dp) Arrays.fill(row, INF);
        dp[0][0] = 0;
        for (int g = 1; g <= d; g++)
            for (int i = g; i <= n; i++) {
                int hardest = 0;                         // max of jobs[j..i-1]
                for (int j = i - 1; j >= g - 1; j--) {
                    hardest = Math.max(hardest, jobs[j]);
                    dp[g][i] = Math.min(dp[g][i], dp[g - 1][j] + hardest);
                }
            }
        return dp[d][n];
    }
    //@end

    //@tpl palk
    // Palindrome partitioning III: change the fewest chars so that s splits
    // into k palindromes. cost[j][i] = changes that make s[j..i] a palindrome.
    static int palindromePartition(String s, int k) {
        int n = s.length(), INF = Integer.MAX_VALUE / 2;
        int[][] cost = new int[n][n];
        for (int j = n - 1; j >= 0; j--)
            for (int i = j + 1; i < n; i++)
                cost[j][i] = cost[j + 1][i - 1]
                             + (s.charAt(j) == s.charAt(i) ? 0 : 1);
        int[][] dp = new int[k + 1][n + 1];
        for (int[] row : dp) Arrays.fill(row, INF);
        dp[0][0] = 0;
        for (int g = 1; g <= k; g++)
            for (int i = g; i <= n; i++)
                for (int j = g - 1; j < i; j++)          // last piece s[j..i-1]
                    dp[g][i] = Math.min(dp[g][i], dp[g - 1][j] + cost[j][i - 1]);
        return dp[k][n];
    }
    //@end

    // ---------------- tests (not printed) ----------------
    interface Cost { long of(int lo, int hi); }        // piece [lo, hi)

    // every way to place k - 1 cuts; combine = max or sum
    static long brute(int n, int k, Cost c, boolean useMax) {
        long[] best = {Long.MAX_VALUE};
        cuts(n, k, 0, c, useMax, 0, best);
        return best[0];
    }

    static void cuts(int n, int left, int start, Cost c, boolean useMax, long acc, long[] best) {
        if (left == 1) {
            if (start >= n) return;
            long v = useMax ? Math.max(acc, c.of(start, n)) : acc + c.of(start, n);
            best[0] = Math.min(best[0], v);
            return;
        }
        for (int end = start + 1; end < n; end++) {
            long piece = c.of(start, end);
            cuts(n, left - 1, end, c, useMax, useMax ? Math.max(acc, piece) : acc + piece, best);
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            int[] a = {7, 2, 5, 10, 8};
            for (int g = 1; g <= 2; g++) {
                StringBuilder sb = new StringBuilder("g=" + g + " ");
                for (int i = 1; i <= a.length; i++)
                    sb.append(i < g ? "   -" : String.format("%4d", splitArray(Arrays.copyOf(a, i), g)));
                System.out.println(sb);
            }
            int run = 0;
            for (int j = 4; j >= 1; j--) {
                run += a[j];
                System.out.println("j=" + j + " max(dp[1][" + j + "]=" + splitArray(Arrays.copyOf(a, j), 1) + ", " + run + ")");
            }
            return;
        }
        Random r = new Random(70);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(9), k = 1 + r.nextInt(n);
            final int[] a = Check.ints(r, n, 0, 20);
            Check.eq(brute(n, k, (lo, hi) -> { long s = 0; for (int i = lo; i < hi; i++) s += a[i]; return s; }, true),
                     splitArray(a, k), "split " + Arrays.toString(a) + " k=" + k);
            int d = 1 + r.nextInt(n + 1);
            long want = d > n ? -1 : brute(n, d, (lo, hi) -> { long m = 0; for (int i = lo; i < hi; i++) m = Math.max(m, a[i]); return m; }, false);
            Check.eq(want, minDifficulty(a, d), "jobs " + Arrays.toString(a) + " d=" + d);
            final String s = Check.str(r, n, r.nextBoolean() ? "ab" : "abc");
            Check.eq(brute(n, k, (lo, hi) -> { long c = 0; for (int i = lo, j = hi - 1; i < j; i++, j--) if (s.charAt(i) != s.charAt(j)) c++; return c; }, false),
                     palindromePartition(s, k), "palk " + s + " k=" + k);
        }
        Check.eq(18, splitArray(new int[] {7, 2, 5, 10, 8}, 2), "LC 410 sample 1");
        Check.eq(9, splitArray(new int[] {1, 2, 3, 4, 5}, 2), "LC 410 sample 2");
        Check.eq(7, minDifficulty(new int[] {6, 5, 4, 3, 2, 1}, 2), "LC 1335 sample 1");
        Check.eq(-1, minDifficulty(new int[] {9, 9, 9}, 4), "LC 1335 sample 2");
        Check.eq(3, minDifficulty(new int[] {1, 1, 1}, 3), "LC 1335 sample 3");
        Check.eq(1, palindromePartition("abc", 2), "LC 1278 sample 1");
        Check.eq(0, palindromePartition("aabbc", 3), "LC 1278 sample 2");
        Check.eq(0, palindromePartition("leetcode", 8), "LC 1278 sample 3");
        Check.done("P10PartitionDp");
    }
}
