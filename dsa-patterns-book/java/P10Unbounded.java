import java.util.*;

public class P10Unbounded {
    //@tpl coins
    // Coin change: fewest coins that make amount; every coin can be used again.
    static int coinChange(int[] coins, int amount) {
        final int INF = amount + 1;                    // more coins than any answer
        int[] dp = new int[amount + 1];                // dp[a] = fewest coins for a
        Arrays.fill(dp, INF);
        dp[0] = 0;
        for (int coin : coins)
            for (int a = coin; a <= amount; a++)       // forward: reuse allowed
                dp[a] = Math.min(dp[a], dp[a - coin] + 1);
        return dp[amount] == INF ? -1 : dp[amount];
    }
    //@end

    //@tpl count
    // Coin change II: number of combinations ({1, 2} and {2, 1} are the same).
    // Coins outer: each combination is built in coin order, so it counts once.
    static int change(int amount, int[] coins) {
        int[] ways = new int[amount + 1];
        ways[0] = 1;
        for (int coin : coins)
            for (int a = coin; a <= amount; a++) ways[a] += ways[a - coin];
        return ways[amount];
    }

    // Combination sum IV: number of sequences ((1, 2) and (2, 1) differ).
    // Amount outer: for each total, try every number as the last one.
    static int combinationSum4(int[] nums, int target) {
        int[] ways = new int[target + 1];
        ways[0] = 1;
        for (int a = 1; a <= target; a++)
            for (int x : nums)
                if (x <= a) ways[a] += ways[a - x];
        return ways[target];
    }
    //@end

    //@tpl squares
    // Perfect squares: coin change with coins 1, 4, 9, ... up to n.
    static int numSquares(int n) {
        int[] dp = new int[n + 1];
        for (int a = 0; a <= n; a++) dp[a] = a;        // a ones: always possible
        for (int s = 2; s * s <= n; s++)
            for (int a = s * s; a <= n; a++)
                dp[a] = Math.min(dp[a], dp[a - s * s] + 1);
        return dp[n];
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Exhaustive: try every coin as the next one (no memo).
    static int bruteMin(int[] coins, int left) {
        if (left == 0) return 0;
        int best = Integer.MAX_VALUE;
        for (int c : coins)
            if (c <= left) {
                int sub = bruteMin(coins, left - c);
                if (sub != Integer.MAX_VALUE) best = Math.min(best, sub + 1);
            }
        return best;
    }

    // Combinations: choose how many of coin i, then move to coin i + 1.
    static int bruteComb(int[] coins, int i, int left) {
        if (i == coins.length) return left == 0 ? 1 : 0;
        int ways = 0;
        for (int k = 0; k * coins[i] <= left; k++) ways += bruteComb(coins, i + 1, left - k * coins[i]);
        return ways;
    }

    // Sequences: every number can be next.
    static int bruteSeq(int[] nums, int left) {
        if (left == 0) return 1;
        int ways = 0;
        for (int x : nums) if (x <= left) ways += bruteSeq(nums, left - x);
        return ways;
    }

    static int bruteSquares(int left, int maxRoot) {
        if (left == 0) return 0;
        int best = Integer.MAX_VALUE;
        for (int s = 1; s <= maxRoot && s * s <= left; s++) best = Math.min(best, 1 + bruteSquares(left - s * s, s));
        return best;
    }

    static int[] distinct(Random r, int k, int lo, int hi) {
        TreeSet<Integer> s = new TreeSet<>();
        while (s.size() < k) s.add(lo + r.nextInt(hi - lo + 1));
        List<Integer> l = new ArrayList<>(s);
        Collections.shuffle(l, r);
        int[] a = new int[k];
        for (int i = 0; i < k; i++) a[i] = l.get(i);
        return a;
    }

    public static void main(String[] args) {
        Random r = new Random(1005);
        for (int t = 0; t < 2000; t++) {
            int[] coins = distinct(r, 1 + r.nextInt(4), 1, 12);
            int amount = r.nextInt(26);
            int bm = bruteMin(coins, amount);
            Check.eq(bm == Integer.MAX_VALUE ? -1 : bm, coinChange(coins, amount), "min " + Arrays.toString(coins) + " " + amount);
            Check.eq(bruteComb(coins, 0, amount), change(amount, coins), "comb " + Arrays.toString(coins) + " " + amount);
            int target = r.nextInt(18);
            Check.eq(bruteSeq(coins, target), combinationSum4(coins, target), "seq " + Arrays.toString(coins) + " " + target);
        }
        for (int n = 1; n <= 120; n++) Check.eq(bruteSquares(n, 11), numSquares(n), "squares " + n);
        Check.eq(3, coinChange(new int[] {1, 2, 5}, 11), "LC 322 sample 1");
        Check.eq(-1, coinChange(new int[] {2}, 3), "LC 322 sample 2");
        Check.eq(0, coinChange(new int[] {1}, 0), "LC 322 sample 3");
        Check.eq(4, change(5, new int[] {1, 2, 5}), "LC 518 sample 1");
        Check.eq(0, change(3, new int[] {2}), "LC 518 sample 2");
        Check.eq(1, change(10, new int[] {10}), "LC 518 sample 3");
        Check.eq(7, combinationSum4(new int[] {1, 2, 3}, 4), "LC 377 sample 1");
        Check.eq(0, combinationSum4(new int[] {9}, 3), "LC 377 sample 2");
        Check.eq(3, numSquares(12), "LC 279 sample 1");
        Check.eq(2, numSquares(13), "LC 279 sample 2");
        Check.eq(4, change(4, new int[] {1, 2, 3}), "doc example: combinations");
        Check.done("P10Unbounded");
    }
}
