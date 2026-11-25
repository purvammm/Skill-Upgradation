import java.util.*;

/*@LC 5 · Longest Palindromic Substring · Medium
group: B
tag: Expand around centres
cue: The longest contiguous palindrome inside s.
hook: Every palindrome has a centre: a letter or a gap. Grow from all 2n − 1 centres.
time: O(n²) time · O(1) space
trap: After the while loop the palindrome is `s[l+1 .. r−1]`: the last step went one too far.
var: LC 647 Palindromic Substrings | Same expansion, count instead of keeping the longest (Day 12).
@*/
class LC5 {
    public String longestPalindrome(String s) {
        int bestL = 0, bestLen = 0;
        for (int c = 0; c < 2 * s.length() - 1; c++) {
            int l = c / 2, r = l + c % 2;                                    //* letter or gap centre
            while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) { l--; r++; }   //*
            if (r - l - 1 > bestLen) { bestLen = r - l - 1; bestL = l + 1; }  //* one step too far
        }
        return s.substring(bestL, bestL + bestLen);
    }
}

/*@LC 139 · Word Break · Medium
group: B
tag: Prefix DP over cut points
cue: Can s be split into dictionary words?
hook: `ok[i]` is true if some earlier `ok[j]` is true and s[j..i) is a word.
time: O(n² · L) time · O(n) space
trap: `ok[0] = true` (the empty prefix); put the words in a HashSet.
var: LC 140 Word Break II | Same DP, then backtrack through the true cut points to list sentences.
@*/
class LC139 {
    public boolean wordBreak(String s, List<String> wordDict) {
        Set<String> dict = new HashSet<>(wordDict);
        int n = s.length();
        boolean[] ok = new boolean[n + 1];
        ok[0] = true;                                                        //*
        for (int i = 1; i <= n; i++)
            for (int j = 0; j < i && !ok[i]; j++)
                if (ok[j] && dict.contains(s.substring(j, i))) ok[i] = true; //*
        return ok[n];
    }
}

/*@LC 62 · Unique Paths · Medium
group: B
tag: Grid DP
cue: Paths from top-left to bottom-right moving only right or down.
hook: Ways into a cell = ways from above + ways from the left.
time: O(m·n) time · O(n) space
trap: With one row array, `dp[j] += dp[j − 1]` already holds "above" in `dp[j]` before the add.
@*/
class LC62 {
    public int uniquePaths(int m, int n) {
        int[] dp = new int[n];
        Arrays.fill(dp, 1);                                  // first row: one way each
        for (int i = 1; i < m; i++)
            for (int j = 1; j < n; j++) dp[j] += dp[j - 1];  //* above + left
        return dp[n - 1];
    }
}

/*@var LC 63 · Unique Paths II (obstacles)
note: Same row DP; an obstacle cell has 0 ways.
@*/
class LC62v1 {
    public int uniquePathsWithObstacles(int[][] grid) {
        int n = grid[0].length;
        int[] dp = new int[n];
        dp[0] = 1;
        for (int[] row : grid)
            for (int j = 0; j < n; j++) {
                if (row[j] == 1) dp[j] = 0;
                else if (j > 0) dp[j] += dp[j - 1];
            }
        return dp[n - 1];
    }
}

/*@var LC 64 · Minimum Path Sum
note: The same grid walk with min instead of plus.
@*/
class LC62v2 {
    public int minPathSum(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[] dp = new int[n];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[0] = 0;
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                dp[j] = grid[i][j] + (j > 0 ? Math.min(dp[j], dp[j - 1]) : dp[j]);
        return dp[n - 1];
    }
}

/*@LC 309 · Best Time to Buy and Sell Stock with Cooldown · Medium
group: B
tag: State machine DP
cue: Buy and sell many times, but after selling you must rest one day.
hook: Three states each day: holding, just sold, resting; move between them.
time: O(n) time · O(1) space
trap: Compute the new values from YESTERDAY's three values (save them first).
var: LC 121 (one trade) | `best = max(best, price − minSoFar)`.
var: LC 122 (unlimited, no cooldown) | Add every positive day-to-day rise.
var: LC 714 (with a fee) | Two states: `hold = max(hold, cash − p)`, `cash = max(cash, hold + p − fee)`.
@*/
class LC309 {
    public int maxProfit(int[] prices) {
        int hold = Integer.MIN_VALUE / 2, sold = 0, rest = 0;
        for (int p : prices) {
            int h = hold, s = sold, r = rest;
            hold = Math.max(h, r - p);                       //* keep holding, or buy after resting
            sold = h + p;                                    //* sell today
            rest = Math.max(r, s);                           //* rest (cooldown ends)
        }
        return Math.max(sold, rest);
    }
}

/*@LC 1235 · Maximum Profit in Job Scheduling · Hard
group: B
tag: Weighted interval scheduling (DP + binary search)
cue: Non-overlapping jobs with profits; maximise the total.
hook: Sort by end. Best up to job i = max(skip it, its profit + best of jobs ending by its start).
time: O(n log n) time · O(n) space
trap: A job may start exactly when another ends: search for the last end `<= start`.
@*/
class LC1235 {
    public int jobScheduling(int[] startTime, int[] endTime, int[] profit) {
        int n = startTime.length;
        int[][] jobs = new int[n][];
        for (int i = 0; i < n; i++) jobs[i] = new int[]{startTime[i], endTime[i], profit[i]};
        Arrays.sort(jobs, (a, b) -> Integer.compare(a[1], b[1]));               //* by end
        int[] dp = new int[n + 1];                                              // dp[i] = best using the first i jobs
        for (int i = 1; i <= n; i++) {
            int start = jobs[i - 1][0], lo = 0, hi = i - 1;                     // count jobs with end <= start
            while (lo < hi) {
                int mid = (lo + hi + 1) >>> 1;
                if (jobs[mid - 1][1] <= start) lo = mid; else hi = mid - 1;     //*
            }
            dp[i] = Math.max(dp[i - 1], dp[lo] + jobs[i - 1][2]);               //* skip or take
        }
        return dp[n];
    }
}

/*@LC 91 · Decode Ways · Medium
group: B
tag: 1-D DP · one or two characters
cue: Count ways to decode a digit string where 1..26 map to letters.
hook: Take one digit (if not '0') or two digits (if 10..26).
time: O(n) time · O(1) space
trap: A '0' cannot stand alone: "06" has 0 ways.
@*/
class LC91 {
    public int numDecodings(String s) {
        int prev2 = 1, prev1 = s.charAt(0) == '0' ? 0 : 1;           // ways for "" and for s[0]
        for (int i = 1; i < s.length(); i++) {
            int cur = 0;
            if (s.charAt(i) != '0') cur += prev1;                     //* one digit
            int two = (s.charAt(i - 1) - '0') * 10 + (s.charAt(i) - '0');
            if (two >= 10 && two <= 26) cur += prev2;                 //* two digits
            prev2 = prev1;
            prev1 = cur;
        }
        return prev1;
    }
}

/*@LC 152 · Maximum Product Subarray · Medium
group: B
tag: Kadane with max and min
cue: Largest product of a contiguous subarray (negatives and zeros allowed).
hook: A negative number turns the smallest product into the largest, so carry both.
time: O(n) time · O(1) space
trap: Compute the new max and min from the OLD pair (save one first).
@*/
class LC152 {
    public int maxProduct(int[] nums) {
        int hi = nums[0], lo = nums[0], best = nums[0];
        for (int i = 1; i < nums.length; i++) {
            int x = nums[i], h = hi;
            hi = Math.max(x, Math.max(h * x, lo * x));               //*
            lo = Math.min(x, Math.min(h * x, lo * x));               //*
            best = Math.max(best, hi);
        }
        return best;
    }
}

public class BonusB {
    static int stockBrute(int[] p, int i, boolean holding) {
        if (i >= p.length) return 0;
        int skip = stockBrute(p, i + 1, holding);
        return holding ? Math.max(skip, p[i] + stockBrute(p, i + 2, false)) : Math.max(skip, -p[i] + stockBrute(p, i + 1, true));
    }

    static int decodeBrute(String s, int i) {
        if (i == s.length()) return 1;
        if (s.charAt(i) == '0') return 0;
        int r = decodeBrute(s, i + 1);
        if (i + 1 < s.length() && Integer.parseInt(s.substring(i, i + 2)) <= 26) r += decodeBrute(s, i + 2);
        return r;
    }

    public static void main(String[] args) {
        T.eq("5a", new LC5().longestPalindrome("babad").length(), 3);
        T.eq("5b", new LC5().longestPalindrome("cbbd"), "bb");
        T.eq("5c", new LC5().longestPalindrome("a"), "a");
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 10);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) sb.append((char) ('a' + T.rnd(0, 2)));
            String s = sb.toString();
            int best = 0;
            for (int i = 0; i < n; i++) for (int j = i + 1; j <= n; j++) { String x = s.substring(i, j); if (x.equals(new StringBuilder(x).reverse().toString())) best = Math.max(best, x.length()); }
            String got = new LC5().longestPalindrome(s);
            T.ok("5 rand", got.length() == best && s.contains(got) && got.equals(new StringBuilder(got).reverse().toString()));
            T.eq("91 rand", new LC91().numDecodings(s.replace('a', '1').replace('b', '2').replace('c', '0')), decodeBrute(s.replace('a', '1').replace('b', '2').replace('c', '0'), 0));
        }

        T.eq("139a", new LC139().wordBreak("leetcode", List.of("leet", "code")), true);
        T.eq("139b", new LC139().wordBreak("applepenapple", List.of("apple", "pen")), true);
        T.eq("139c", new LC139().wordBreak("catsandog", List.of("cats", "dog", "sand", "and", "cat")), false);

        T.eq("62a", new LC62().uniquePaths(3, 7), 28);
        T.eq("62b", new LC62().uniquePaths(3, 2), 3);
        T.eq("62c", new LC62().uniquePaths(1, 1), 1);
        T.eq("63a", new LC62v1().uniquePathsWithObstacles(T.grid("[[0,0,0],[0,1,0],[0,0,0]]")), 2);
        T.eq("63b", new LC62v1().uniquePathsWithObstacles(T.grid("[[0,1],[0,0]]")), 1);
        T.eq("63c", new LC62v1().uniquePathsWithObstacles(T.grid("[[1]]")), 0);
        T.eq("64a", new LC62v2().minPathSum(T.grid("[[1,3,1],[1,5,1],[4,2,1]]")), 7);
        T.eq("64b", new LC62v2().minPathSum(T.grid("[[1,2,3],[4,5,6]]")), 12);

        T.eq("309a", new LC309().maxProfit(T.ints("[1,2,3,0,2]")), 3);
        T.eq("309b", new LC309().maxProfit(T.ints("[1]")), 0);
        for (int t = 0; t < 200; t++) {
            int[] p = T.rand(T.rnd(1, 10), 0, 9);
            T.eq("309 rand", new LC309().maxProfit(p), stockBrute(p, 0, false));
        }

        T.eq("1235a", new LC1235().jobScheduling(T.ints("[1,2,3,3]"), T.ints("[3,4,5,6]"), T.ints("[50,10,40,70]")), 120);
        T.eq("1235b", new LC1235().jobScheduling(T.ints("[1,2,3,4,6]"), T.ints("[3,5,10,6,9]"), T.ints("[20,20,100,70,60]")), 150);
        T.eq("1235c", new LC1235().jobScheduling(T.ints("[1,1,1]"), T.ints("[2,3,4]"), T.ints("[5,6,4]")), 6);
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 8);
            int[] s = new int[n], e = new int[n], p = T.rand(n, 1, 20);
            for (int i = 0; i < n; i++) { s[i] = T.rnd(1, 10); e[i] = s[i] + T.rnd(1, 5); }
            int best = 0;
            for (int m = 0; m < 1 << n; m++) {
                boolean ok = true; int sum = 0;
                for (int i = 0; i < n; i++) if ((m >> i & 1) == 1) { sum += p[i]; for (int j = i + 1; j < n; j++) if ((m >> j & 1) == 1 && s[i] < e[j] && s[j] < e[i]) ok = false; }
                if (ok) best = Math.max(best, sum);
            }
            T.eq("1235 rand", new LC1235().jobScheduling(s, e, p), best);
        }

        T.eq("91a", new LC91().numDecodings("12"), 2);
        T.eq("91b", new LC91().numDecodings("226"), 3);
        T.eq("91c", new LC91().numDecodings("06"), 0);

        T.eq("152a", new LC152().maxProduct(T.ints("[2,3,-2,4]")), 6);
        T.eq("152b", new LC152().maxProduct(T.ints("[-2,0,-1]")), 0);
        T.eq("152c", new LC152().maxProduct(T.ints("[-2,3,-4]")), 24);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 8), -3, 3);
            int best = Integer.MIN_VALUE;
            for (int i = 0; i < a.length; i++) { int pr = 1; for (int j = i; j < a.length; j++) best = Math.max(best, pr *= a[j]); }
            T.eq("152 rand", new LC152().maxProduct(a), best);
        }
        T.summary("BonusB");
    }
}
