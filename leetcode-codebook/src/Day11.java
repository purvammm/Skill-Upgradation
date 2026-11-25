import java.util.*;

/*@core DP recipe + the two knapsack loops
note: Write four things before coding: STATE (what dp[i] means), TRANSITION, BASE, ANSWER. Each item once (0/1) → loop the capacity DOWN. Items reusable (unbounded) → loop UP. Counting combinations → items outside, amount inside.
@*/
class Core11 {
    static int knapsack01(int[] w, int[] v, int cap) {
        int[] dp = new int[cap + 1];                  // dp[c] = best value with capacity c
        for (int i = 0; i < w.length; i++)
            for (int c = cap; c >= w[i]; c--)         // DOWN: item i used at most once
                dp[c] = Math.max(dp[c], dp[c - w[i]] + v[i]);
        return dp[cap];
    }

    static int knapsackUnbounded(int[] w, int[] v, int cap) {
        int[] dp = new int[cap + 1];
        for (int i = 0; i < w.length; i++)
            for (int c = w[i]; c <= cap; c++)         // UP: item i can be used again
                dp[c] = Math.max(dp[c], dp[c - w[i]] + v[i]);
        return dp[cap];
    }
}

/*@LC 198
tag: Take or skip
cue: Max sum of non-adjacent elements.
hook: Skip me and keep the best so far, or take me plus the best two back.
time: O(n) time · O(1) space
trap: Update `prev2` before `prev1`, using the OLD `prev1`.
@*/
class LC198 {
    public int rob(int[] nums) {
        int prev2 = 0, prev1 = 0;                     // best up to i−2, best up to i−1
        for (int x : nums) {
            int cur = Math.max(prev1, prev2 + x);     //* skip x, or take x
            prev2 = prev1;
            prev1 = cur;
        }
        return prev1;
    }
}

/*@var LC 213 · House Robber II (houses in a circle)
note: First and last are neighbours, so run the line version twice: without the last house, and without the first.
@*/
class LC198v1 {
    public int rob(int[] nums) {
        int n = nums.length;
        if (n == 1) return nums[0];
        return Math.max(line(nums, 0, n - 2), line(nums, 1, n - 1));
    }

    private int line(int[] a, int lo, int hi) {
        int prev2 = 0, prev1 = 0;
        for (int i = lo; i <= hi; i++) {
            int cur = Math.max(prev1, prev2 + a[i]);
            prev2 = prev1;
            prev1 = cur;
        }
        return prev1;
    }
}

/*@LC 322
tag: Unbounded knapsack · minimum
cue: Fewest coins to make an amount (each coin unlimited).
hook: The best for a equals one coin plus the best for a minus that coin.
time: O(amount · coins) time · O(amount) space
trap: Use `INF = amount + 1`, not `Integer.MAX_VALUE`: `MAX_VALUE + 1` overflows to a negative.
@*/
class LC322 {
    public int coinChange(int[] coins, int amount) {
        final int INF = amount + 1;                                    //*
        int[] dp = new int[amount + 1];                                // fewest coins for each amount
        Arrays.fill(dp, INF);
        dp[0] = 0;                                                     //*
        for (int a = 1; a <= amount; a++)
            for (int c : coins)
                if (c <= a) dp[a] = Math.min(dp[a], dp[a - c] + 1);    //*
        return dp[amount] >= INF ? -1 : dp[amount];
    }
}

/*@var LC 279 · Perfect Squares
note: Coin change where the coins are 1, 4, 9, 16 …
@*/
class LC322v1 {
    public int numSquares(int n) {
        int[] dp = new int[n + 1];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[0] = 0;
        for (int a = 1; a <= n; a++)
            for (int s = 1; s * s <= a; s++) dp[a] = Math.min(dp[a], dp[a - s * s] + 1);
        return dp[n];
    }
}

/*@LC 518
tag: Counting combinations
cue: Number of ways to make an amount; order does not matter.
hook: Coins outside, amounts inside, so each combination is built in one order only.
time: O(amount · coins) time · O(amount) space
trap: Swapping the loops counts permutations (that is LC 377 below).
var: LC 70 Climbing Stairs | LC 377 with steps {1, 2}.
@*/
class LC518 {
    public int change(int amount, int[] coins) {
        int[] dp = new int[amount + 1];
        dp[0] = 1;                                     //* one way to make 0
        for (int c : coins)                            //* coins OUTSIDE
            for (int a = c; a <= amount; a++)          //* amounts inside, upwards (reuse)
                dp[a] += dp[a - c];
        return dp[amount];
    }
}

/*@var LC 377 · Combination Sum IV (order matters)
note: Amount outside, numbers inside: the same multiset in different orders is counted separately.
@*/
class LC518v1 {
    public int combinationSum4(int[] nums, int target) {
        int[] dp = new int[target + 1];
        dp[0] = 1;
        for (int a = 1; a <= target; a++)
            for (int x : nums)
                if (x <= a) dp[a] += dp[a - x];
        return dp[target];
    }
}

/*@LC 416
tag: 0/1 subset sum
cue: Can the array be split into two parts with equal sums?
hook: Odd total is a no; otherwise can a subset hit half?
time: O(n · sum) time · O(sum) space
trap: Loop the sum DOWNWARDS, or one number gets used twice.
var: Any target T | The same loop with `half` replaced by T.
@*/
class LC416 {
    public boolean canPartition(int[] nums) {
        int total = 0;
        for (int x : nums) total += x;
        if (total % 2 == 1) return false;              //*
        int half = total / 2;
        boolean[] can = new boolean[half + 1];
        can[0] = true;
        for (int x : nums)
            for (int s = half; s >= x; s--)            //* downwards
                can[s] |= can[s - x];                  //*
        return can[half];
    }
}

/*@LC 494
tag: Count subsets with a sum
cue: Put + or − before each number; how many ways reach target?
hook: Plus-group minus minus-group is target, together they are total: plus = (total + target) / 2.
time: O(n · sum) time · O(sum) space
trap: Return 0 when `|target| > total` or `total + target` is odd. Zeros double the count: the loop handles that.
@*/
class LC494 {
    public int findTargetSumWays(int[] nums, int target) {
        int total = 0;
        for (int x : nums) total += x;
        if (Math.abs(target) > total || (total + target) % 2 != 0) return 0;    //*
        int want = (total + target) / 2;                                         //*
        int[] ways = new int[want + 1];
        ways[0] = 1;
        for (int x : nums)
            for (int s = want; s >= x; s--) ways[s] += ways[s - x];             //* 0/1 counting
        return ways[want];
    }
}

/*@LC 1049
tag: Subset sum closest to half
cue: Smash any two stones (x ≤ y → y − x); smallest possible last stone.
hook: Every smash is a + or −: split into two piles as equal as possible.
time: O(n · sum) time · O(sum) space
trap: This is NOT LC 1046: the heap greedy gives wrong answers here.
@*/
class LC1049 {
    public int lastStoneWeightII(int[] stones) {
        int total = 0;
        for (int s : stones) total += s;
        boolean[] can = new boolean[total / 2 + 1];
        can[0] = true;
        for (int s : stones)
            for (int t = total / 2; t >= s; t--) can[t] |= can[t - s];
        for (int t = total / 2; t >= 0; t--)
            if (can[t]) return total - 2 * t;          //* piles t and total − t
        return total;
    }
}

/*@LC 740
tag: Reduce to House Robber
cue: Taking value x deletes every x − 1 and x + 1; maximise points.
hook: Add up each value's points, then rob houses where neighbours are x and x + 1.
time: O(n + maxValue) time · O(maxValue) space
trap: Take ALL copies of x together: `points[x] += x` for each copy.
@*/
class LC740 {
    public int deleteAndEarn(int[] nums) {
        int max = 0;
        for (int x : nums) max = Math.max(max, x);
        int[] points = new int[max + 1];
        for (int x : nums) points[x] += x;                 //*
        int prev2 = 0, prev1 = 0;
        for (int v = 0; v <= max; v++) {                   //* House Robber over values
            int cur = Math.max(prev1, prev2 + points[v]);
            prev2 = prev1;
            prev1 = cur;
        }
        return prev1;
    }
}

/*@var LC 2140 · Solving Questions With Brainpower
note: Take-or-skip where taking jumps ahead: fill dp from the right. `dp[i]` = best from question i on.
@*/
class LC740v1 {
    public long mostPoints(int[][] questions) {
        int n = questions.length;
        long[] dp = new long[n + 1];
        for (int i = n - 1; i >= 0; i--) {
            int next = Math.min(n, i + questions[i][1] + 1);
            dp[i] = Math.max(dp[i + 1], questions[i][0] + dp[next]);   // skip, or solve and jump
        }
        return dp[0];
    }
}

public class Day11 {
    public static void main(String[] args) {
        T.eq("core 01", Core11.knapsack01(T.ints("[1,3,4]"), T.ints("[15,20,30]"), 4), 35);
        T.eq("core unb", Core11.knapsackUnbounded(T.ints("[1,3,4]"), T.ints("[15,20,30]"), 4), 60);

        T.eq("198a", new LC198().rob(T.ints("[1,2,3,1]")), 4);
        T.eq("198b", new LC198().rob(T.ints("[2,7,9,3,1]")), 12);
        T.eq("213a", new LC198v1().rob(T.ints("[2,3,2]")), 3);
        T.eq("213b", new LC198v1().rob(T.ints("[1,2,3,1]")), 4);
        T.eq("213c", new LC198v1().rob(T.ints("[1,2,3]")), 3);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 10), 0, 9);
            int n = a.length, best = 0, bestC = 0;
            for (int m = 0; m < 1 << n; m++) {
                if ((m & (m >> 1)) != 0) continue;
                int s = 0;
                for (int i = 0; i < n; i++) if ((m >> i & 1) == 1) s += a[i];
                best = Math.max(best, s);
                if (n == 1 || !((m & 1) == 1 && (m >> (n - 1) & 1) == 1)) bestC = Math.max(bestC, s);
            }
            T.eq("198 rand", new LC198().rob(a), best);
            T.eq("213 rand", new LC198v1().rob(a), bestC);
        }

        T.eq("322a", new LC322().coinChange(T.ints("[1,2,5]"), 11), 3);
        T.eq("322b", new LC322().coinChange(T.ints("[2]"), 3), -1);
        T.eq("322c", new LC322().coinChange(T.ints("[1]"), 0), 0);
        T.eq("279a", new LC322v1().numSquares(12), 3);
        T.eq("279b", new LC322v1().numSquares(13), 2);

        T.eq("518a", new LC518().change(5, T.ints("[1,2,5]")), 4);
        T.eq("518b", new LC518().change(3, T.ints("[2]")), 0);
        T.eq("518c", new LC518().change(10, T.ints("[10]")), 1);
        T.eq("377a", new LC518v1().combinationSum4(T.ints("[1,2,3]"), 4), 7);
        T.eq("377b", new LC518v1().combinationSum4(T.ints("[9]"), 3), 0);

        T.eq("416a", new LC416().canPartition(T.ints("[1,5,11,5]")), true);
        T.eq("416b", new LC416().canPartition(T.ints("[1,2,3,5]")), false);
        T.eq("494a", new LC494().findTargetSumWays(T.ints("[1,1,1,1,1]"), 3), 5);
        T.eq("494b", new LC494().findTargetSumWays(T.ints("[1]"), 1), 1);
        T.eq("494c", new LC494().findTargetSumWays(T.ints("[0,0,0,0,0,0,0,0,1]"), 1), 256);
        T.eq("494d", new LC494().findTargetSumWays(T.ints("[100]"), -200), 0);
        T.eq("1049a", new LC1049().lastStoneWeightII(T.ints("[2,7,4,1,8,1]")), 1);
        T.eq("1049b", new LC1049().lastStoneWeightII(T.ints("[31,26,33,21,40]")), 5);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 10), 0, 9);
            int total = 0;
            for (int x : a) total += x;
            int tgt = T.rnd(-total - 2, total + 2), ways = 0, bestDiff = Integer.MAX_VALUE;
            boolean half = false;
            for (int m = 0; m < 1 << a.length; m++) {
                int s = 0;
                for (int i = 0; i < a.length; i++) s += (m >> i & 1) == 1 ? a[i] : -a[i];
                if (s == tgt) ways++;
                if (s == 0) half = true;
                bestDiff = Math.min(bestDiff, Math.abs(s));
            }
            T.eq("494 rand", new LC494().findTargetSumWays(a, tgt), ways);
            T.eq("416 rand", new LC416().canPartition(a), half);
            T.eq("1049 rand", new LC1049().lastStoneWeightII(a), bestDiff);
        }

        T.eq("740a", new LC740().deleteAndEarn(T.ints("[3,4,2]")), 6);
        T.eq("740b", new LC740().deleteAndEarn(T.ints("[2,2,3,3,3,4]")), 9);
        T.eq("2140a", new LC740v1().mostPoints(T.grid("[[3,2],[4,3],[4,4],[2,5]]")), 5L);
        T.eq("2140b", new LC740v1().mostPoints(T.grid("[[1,1],[2,2],[3,3],[4,4],[5,5]]")), 7L);
        T.summary("Day11");
    }
}
