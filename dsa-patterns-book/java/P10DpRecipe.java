import java.util.*;

public class P10DpRecipe {
    //@tpl memo
    // House robber, top-down. State: f(i) = most money from the first i houses.
    // Transition: skip house i-1 -> f(i-1), or rob it -> f(i-2) + a[i-1].
    static int robMemo(int[] a) {
        Integer[] memo = new Integer[a.length + 1];    // null = not solved yet
        return f(a, a.length, memo);                   // answer: f(n)
    }

    static int f(int[] a, int i, Integer[] memo) {
        if (i <= 0) return 0;                          // base: no houses, no money
        if (memo[i] != null) return memo[i];
        int best = Math.max(f(a, i - 1, memo), f(a, i - 2, memo) + a[i - 1]);
        return memo[i] = best;
    }
    //@end

    //@tpl table
    // Bottom-up: the same recurrence, filled so dp[i-1] and dp[i-2] are ready.
    static int robTable(int[] a) {
        int n = a.length;
        int[] dp = new int[n + 1];                     // dp[i] = f(i); dp[0] = 0
        if (n > 0) dp[1] = a[0];
        for (int i = 2; i <= n; i++)
            dp[i] = Math.max(dp[i - 1], dp[i - 2] + a[i - 1]);
        return dp[n];
    }

    // Rolling: dp[i] reads only the two values before it, so keep two numbers.
    static int rob(int[] a) {
        int prev2 = 0, prev1 = 0;                      // dp[i-2], dp[i-1]
        for (int x : a) {
            int cur = Math.max(prev1, prev2 + x);
            prev2 = prev1;
            prev1 = cur;
        }
        return prev1;
    }
    //@end

    //@tpl stairs
    // Climbing stairs: ways(i) = ways(i-1) + ways(i-2); ways(0) = ways(1) = 1.
    static int climbStairs(int n) {
        int a = 1, b = 1;                              // ways(i-2), ways(i-1)
        for (int i = 2; i <= n; i++) {
            int c = a + b;
            a = b;
            b = c;
        }
        return b;
    }

    // Min cost climbing stairs: pay cost[i] to step up 1 or 2 from step i.
    // reach(i) = min(reach(i-1) + cost[i-1], reach(i-2) + cost[i-2]).
    static int minCostClimbingStairs(int[] cost) {
        int a = 0, b = 0;                              // reach(0) = reach(1) = 0
        for (int i = 2; i <= cost.length; i++) {
            int c = Math.min(b + cost[i - 1], a + cost[i - 2]);
            a = b;
            b = c;
        }
        return b;                                      // reach(n): the top
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Every subset of houses with no two neighbours.
    static int bruteRob(int[] a) {
        int n = a.length, best = 0;
        for (int mask = 0; mask < (1 << n); mask++) {
            if ((mask & (mask >> 1)) != 0) continue;
            int s = 0;
            for (int i = 0; i < n; i++) if ((mask >> i & 1) == 1) s += a[i];
            best = Math.max(best, s);
        }
        return best;
    }

    // Plain recursion over every sequence of 1- and 2-steps.
    static int bruteWays(int left) {
        if (left == 0) return 1;
        if (left < 0) return 0;
        return bruteWays(left - 1) + bruteWays(left - 2);
    }

    // Every path from step 0 or step 1 to the top.
    static int bruteCost(int[] cost, int i) {
        if (i >= cost.length) return 0;
        return cost[i] + Math.min(bruteCost(cost, i + 1), bruteCost(cost, i + 2));
    }

    // "Take the richest house left" greedy, to show it fails.
    static int greedyRob(int[] a) {
        int n = a.length, s = 0;
        boolean[] gone = new boolean[n];
        while (true) {
            int pick = -1;
            for (int i = 0; i < n; i++) if (!gone[i] && (pick < 0 || a[i] > a[pick])) pick = i;
            if (pick < 0) return s;
            s += a[pick];
            gone[pick] = true;
            if (pick > 0) gone[pick - 1] = true;
            if (pick + 1 < n) gone[pick + 1] = true;
        }
    }

    public static void main(String[] args) {
        Random r = new Random(1001);
        for (int t = 0; t < 3000; t++) {
            int n = r.nextInt(14);
            int[] a = Check.ints(r, n, 0, 20);
            int want = bruteRob(a);
            String ctx = Arrays.toString(a);
            Check.eq(want, robMemo(a), "memo " + ctx);
            Check.eq(want, robTable(a), "table " + ctx);
            Check.eq(want, rob(a), "rolling " + ctx);
            int[] cost = Check.ints(r, 2 + r.nextInt(12), 0, 30);
            int wantCost = Math.min(bruteCost(cost, 0), bruteCost(cost, 1));
            Check.eq(wantCost, minCostClimbingStairs(cost), "cost " + Arrays.toString(cost));
        }
        for (int n = 1; n <= 30; n++) Check.eq(bruteWays(n), climbStairs(n), "stairs n=" + n);
        Check.eq(1836311903, climbStairs(45), "stairs n=45 fits in int");
        Check.eq(4, rob(new int[] {1, 2, 3, 1}), "LC 198 sample 1");
        Check.eq(12, rob(new int[] {2, 7, 9, 3, 1}), "LC 198 sample 2");
        Check.eq(12, robMemo(new int[] {2, 7, 9, 3, 1}), "LC 198 sample 2 memo");
        Check.eq(2, climbStairs(2), "LC 70 sample 1");
        Check.eq(3, climbStairs(3), "LC 70 sample 2");
        Check.eq(15, minCostClimbingStairs(new int[] {10, 15, 20}), "LC 746 sample 1");
        Check.eq(6, minCostClimbingStairs(new int[] {1, 100, 1, 1, 1, 100, 1, 1, 100, 1}), "LC 746 sample 2");
        // greedy counterexample used in the text
        Check.eq(4, greedyRob(new int[] {3, 4, 3}), "greedy on [3, 4, 3]");
        Check.eq(6, rob(new int[] {3, 4, 3}), "dp on [3, 4, 3]");
        Check.done("P10DpRecipe");
    }
}
