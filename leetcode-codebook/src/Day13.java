import java.util.*;

/*@core LIS with tails + binary search
note: `tails[k]` = the smallest possible last value of an increasing run of length k + 1. For each x, find the first tail that x can replace. Strictly increasing → lower bound (first tail ≥ x). Non-decreasing → upper bound (first tail > x). The position + 1 is the LIS ending at x.
@*/
class Core13 {
    static int[] lisEndingAt(int[] a, boolean strict) {
        int[] tails = new int[a.length], res = new int[a.length];
        int len = 0;
        for (int i = 0; i < a.length; i++) {
            int lo = 0, hi = len;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (strict ? tails[mid] < a[i] : tails[mid] <= a[i]) lo = mid + 1; else hi = mid;
            }
            tails[lo] = a[i];
            if (lo == len) len++;
            res[i] = lo + 1;
        }
        return res;
    }
}

/*@LC 300
tag: LIS · O(n log n)
cue: Length of the longest strictly increasing subsequence.
hook: Keep the smallest tail for each length; x replaces the first tail ≥ x, or extends.
time: O(n log n) time · O(n) space
trap: `tails` is NOT a real subsequence; only its length is the answer.
var: Longest non-decreasing | Change `<` to `<=` in the search (LC 1964).
var: Longest decreasing | Run it on the negated values.
@*/
class LC300 {
    public int lengthOfLIS(int[] nums) {
        int[] tails = new int[nums.length];
        int len = 0;
        for (int x : nums) {
            int lo = 0, hi = len;
            while (lo < hi) {                                 //* first tail >= x
                int mid = (lo + hi) >>> 1;
                if (tails[mid] < x) lo = mid + 1; else hi = mid;
            }
            tails[lo] = x;                                    //* replace or extend
            if (lo == len) len++;                             //*
        }
        return len;
    }
}

/*@var O(n²) DP: `dp[i]` = LIS ending at i
note: Slower, but it is the template for LC 368, 673, 1626 and any "chain" with a custom rule.
@*/
class LC300v1 {
    public int lengthOfLIS(int[] nums) {
        int n = nums.length, best = 0;
        int[] dp = new int[n];
        for (int i = 0; i < n; i++) {
            dp[i] = 1;
            for (int j = 0; j < i; j++) if (nums[j] < nums[i]) dp[i] = Math.max(dp[i], dp[j] + 1);
            best = Math.max(best, dp[i]);
        }
        return best;
    }
}

/*@LC 1964
tag: Non-decreasing LIS at every index
cue: For each i, the longest non-decreasing course ending exactly at i.
hook: Upper bound (first tail > x); the answer at i is position + 1.
time: O(n log n) time · O(n) space
trap: Equal heights are allowed, so search with `<=`. Using `<` undercounts.
@*/
class LC1964 {
    public int[] longestObstacleCourseAtEachPosition(int[] obstacles) {
        int n = obstacles.length, len = 0;
        int[] tails = new int[n], ans = new int[n];
        for (int i = 0; i < n; i++) {
            int x = obstacles[i], lo = 0, hi = len;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (tails[mid] <= x) lo = mid + 1; else hi = mid;   //* upper bound
            }
            tails[lo] = x;
            if (lo == len) len++;
            ans[i] = lo + 1;                                         //*
        }
        return ans;
    }
}

/*@LC 1671
tag: LIS from both sides
cue: Fewest removals to leave a mountain (strictly up, then strictly down).
hook: Up-run ending at i plus down-run starting at i minus one; the peak needs both sides.
time: O(n log n) time · O(n) space
trap: A peak needs `up ≥ 2` AND `down ≥ 2`; a pure slope is not a mountain.
@*/
class LC1671 {
    public int minimumMountainRemovals(int[] nums) {
        int n = nums.length;
        int[] up = lisEndingAt(nums);
        int[] rev = new int[n];
        for (int i = 0; i < n; i++) rev[i] = nums[n - 1 - i];
        int[] r = lisEndingAt(rev);
        int best = 0;
        for (int i = 0; i < n; i++) {
            int down = r[n - 1 - i];                                  //* LIS of the reversed array
            if (up[i] >= 2 && down >= 2) best = Math.max(best, up[i] + down - 1);   //*
        }
        return n - best;
    }

    private int[] lisEndingAt(int[] a) {
        int[] tails = new int[a.length], res = new int[a.length];
        int len = 0;
        for (int i = 0; i < a.length; i++) {
            int lo = 0, hi = len;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (tails[mid] < a[i]) lo = mid + 1; else hi = mid;
            }
            tails[lo] = a[i];
            if (lo == len) len++;
            res[i] = lo + 1;
        }
        return res;
    }
}

/*@var LC 845 · Longest Mountain in Array (contiguous)
note: Subarray, not subsequence: count the climb into i and the fall out of i with two simple passes.
@*/
class LC1671v1 {
    public int longestMountain(int[] arr) {
        int n = arr.length, best = 0;
        int[] up = new int[n], down = new int[n];
        for (int i = 1; i < n; i++) if (arr[i] > arr[i - 1]) up[i] = up[i - 1] + 1;
        for (int i = n - 2; i >= 0; i--) if (arr[i] > arr[i + 1]) down[i] = down[i + 1] + 1;
        for (int i = 0; i < n; i++) if (up[i] > 0 && down[i] > 0) best = Math.max(best, up[i] + down[i] + 1);
        return best;
    }
}

/*@LC 2370
tag: DP over the last letter
cue: Longest subsequence where neighbouring letters differ by at most k.
hook: Only the last letter matters: 26 states.
time: O(26 · n) time · O(26) space
trap: Read the best of the close letters BEFORE writing `best[c]`.
@*/
class LC2370 {
    public int longestIdealString(String s, int k) {
        int[] best = new int[26];                       //* best[c] = longest ending with letter c
        int ans = 0;
        for (char ch : s.toCharArray()) {
            int c = ch - 'a', len = 0;
            for (int p = Math.max(0, c - k); p <= Math.min(25, c + k); p++) len = Math.max(len, best[p]);   //*
            best[c] = len + 1;
            ans = Math.max(ans, best[c]);
        }
        return ans;
    }
}

/*@var LC 1218 · Longest Arithmetic Subsequence of Given Difference
note: The same "best ending with this value" idea, with a HashMap because the values are big.
@*/
class LC2370v1 {
    public int longestSubsequence(int[] arr, int difference) {
        Map<Integer, Integer> best = new HashMap<>();
        int ans = 0;
        for (int x : arr) {
            int len = best.getOrDefault(x - difference, 0) + 1;
            best.put(x, len);
            ans = Math.max(ans, len);
        }
        return ans;
    }
}

/*@LC 354
tag: Sort trick + LIS
cue: Most envelopes nested inside each other (both sides strictly bigger).
hook: Width up, height DOWN on ties, then LIS on heights.
time: O(n log n) time · O(n) space
trap: Without the "height descending on equal width" rule, two envelopes of the same width get nested.
@*/
class LC354 {
    public int maxEnvelopes(int[][] envelopes) {
        Arrays.sort(envelopes, (a, b) -> a[0] != b[0] ? Integer.compare(a[0], b[0])
                                                      : Integer.compare(b[1], a[1]));   //*
        int[] tails = new int[envelopes.length];
        int len = 0;
        for (int[] e : envelopes) {                                  //* strict LIS on heights
            int lo = 0, hi = len;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (tails[mid] < e[1]) lo = mid + 1; else hi = mid;
            }
            tails[lo] = e[1];
            if (lo == len) len++;
        }
        return len;
    }
}

/*@var LC 1626 · Best Team With No Conflicts
note: Sort by (age, score); then a non-decreasing chain on scores where dp stores the best SUM, not the length.
@*/
class LC354v1 {
    public int bestTeamScore(int[] scores, int[] ages) {
        int n = scores.length;
        int[][] p = new int[n][];
        for (int i = 0; i < n; i++) p[i] = new int[]{ages[i], scores[i]};
        Arrays.sort(p, (a, b) -> a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));
        int[] dp = new int[n];
        int best = 0;
        for (int i = 0; i < n; i++) {
            dp[i] = p[i][1];
            for (int j = 0; j < i; j++) if (p[j][1] <= p[i][1]) dp[i] = Math.max(dp[i], dp[j] + p[i][1]);
            best = Math.max(best, dp[i]);
        }
        return best;
    }
}

/*@LC 673
tag: LIS with counts
cue: How many longest increasing subsequences are there?
hook: Each index keeps (length, count): longer replaces the count, a tie adds to it.
time: O(n²) time · O(n) space
trap: Sum the counts of EVERY index whose length equals the best length.
@*/
class LC673 {
    public int findNumberOfLIS(int[] nums) {
        int n = nums.length, bestLen = 0, total = 0;
        int[] len = new int[n], cnt = new int[n];
        for (int i = 0; i < n; i++) {
            len[i] = 1;
            cnt[i] = 1;
            for (int j = 0; j < i; j++) {
                if (nums[j] >= nums[i]) continue;
                if (len[j] + 1 > len[i]) { len[i] = len[j] + 1; cnt[i] = cnt[j]; }   //* longer: replace
                else if (len[j] + 1 == len[i]) cnt[i] += cnt[j];                    //* tie: add
            }
            if (len[i] > bestLen) { bestLen = len[i]; total = cnt[i]; }
            else if (len[i] == bestLen) total += cnt[i];                            //*
        }
        return total;
    }
}

public class Day13 {
    public static void main(String[] args) {
        T.eq("core strict", Core13.lisEndingAt(T.ints("[1,2,2,3]"), true), T.ints("[1,2,2,3]"));
        T.eq("core nondec", Core13.lisEndingAt(T.ints("[1,2,2,3]"), false), T.ints("[1,2,3,4]"));

        T.eq("300a", new LC300().lengthOfLIS(T.ints("[10,9,2,5,3,7,101,18]")), 4);
        T.eq("300b", new LC300().lengthOfLIS(T.ints("[0,1,0,3,2,3]")), 4);
        T.eq("300c", new LC300().lengthOfLIS(T.ints("[7,7,7,7]")), 1);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 10), 0, 6);
            int n = a.length, best = 0, cntBest = 0;
            int[] endNd = new int[n];
            for (int m = 1; m < 1 << n; m++) {
                int prev = Integer.MIN_VALUE, len = 0; boolean ok = true;
                for (int i = 0; i < n; i++) if ((m >> i & 1) == 1) { if (a[i] <= prev) ok = false; prev = a[i]; len++; }
                if (!ok) continue;
                if (len > best) { best = len; cntBest = 1; } else if (len == best) cntBest++;
            }
            for (int i = 0; i < n; i++) { endNd[i] = 1; for (int j = 0; j < i; j++) if (a[j] <= a[i]) endNd[i] = Math.max(endNd[i], endNd[j] + 1); }
            T.eq("300 rand", new LC300().lengthOfLIS(a), best);
            T.eq("300v rand", new LC300v1().lengthOfLIS(a), best);
            T.eq("673 rand", new LC673().findNumberOfLIS(a), cntBest);
            T.eq("1964 rand", new LC1964().longestObstacleCourseAtEachPosition(a), endNd);
            int mountain = 0;
            for (int m = 1; m < 1 << n; m++) {
                List<Integer> v = new ArrayList<>();
                for (int i = 0; i < n; i++) if ((m >> i & 1) == 1) v.add(a[i]);
                int p = 0;
                while (p + 1 < v.size() && v.get(p) < v.get(p + 1)) p++;
                int q = p;
                while (q + 1 < v.size() && v.get(q) > v.get(q + 1)) q++;
                if (p > 0 && q > p && q == v.size() - 1) mountain = Math.max(mountain, v.size());
            }
            if (mountain > 0) T.eq("1671 rand", new LC1671().minimumMountainRemovals(a), n - mountain);
        }

        T.eq("1964a", new LC1964().longestObstacleCourseAtEachPosition(T.ints("[1,2,3,2]")), T.ints("[1,2,3,3]"));
        T.eq("1964b", new LC1964().longestObstacleCourseAtEachPosition(T.ints("[2,2,1]")), T.ints("[1,2,1]"));
        T.eq("1964c", new LC1964().longestObstacleCourseAtEachPosition(T.ints("[3,1,5,6,4,2]")), T.ints("[1,1,2,3,2,2]"));

        T.eq("1671a", new LC1671().minimumMountainRemovals(T.ints("[1,3,1]")), 0);
        T.eq("1671b", new LC1671().minimumMountainRemovals(T.ints("[2,1,1,5,6,2,3,1]")), 3);
        T.eq("1671c", new LC1671().minimumMountainRemovals(T.ints("[100,92,89,77,74,66,64,66,64]")), 6);
        T.eq("845a", new LC1671v1().longestMountain(T.ints("[2,1,4,7,3,2,5]")), 5);
        T.eq("845b", new LC1671v1().longestMountain(T.ints("[2,2,2]")), 0);

        T.eq("2370a", new LC2370().longestIdealString("acfgbd", 2), 4);
        T.eq("2370b", new LC2370().longestIdealString("abcd", 3), 4);
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 10), k = T.rnd(0, 3);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) sb.append((char) ('a' + T.rnd(0, 6)));
            String s = sb.toString();
            int[] dp = new int[n];
            int best = 0;
            for (int i = 0; i < n; i++) { dp[i] = 1; for (int j = 0; j < i; j++) if (Math.abs(s.charAt(i) - s.charAt(j)) <= k) dp[i] = Math.max(dp[i], dp[j] + 1); best = Math.max(best, dp[i]); }
            T.eq("2370 rand", new LC2370().longestIdealString(s, k), best);
        }
        T.eq("1218a", new LC2370v1().longestSubsequence(T.ints("[1,2,3,4]"), 1), 4);
        T.eq("1218b", new LC2370v1().longestSubsequence(T.ints("[1,3,5,7]"), 1), 1);
        T.eq("1218c", new LC2370v1().longestSubsequence(T.ints("[1,5,7,8,5,3,4,2,1]"), -2), 4);

        T.eq("354a", new LC354().maxEnvelopes(T.grid("[[5,4],[6,4],[6,7],[2,3]]")), 3);
        T.eq("354b", new LC354().maxEnvelopes(T.grid("[[1,1],[1,1],[1,1]]")), 1);
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 9);
            int[][] e = new int[n][];
            for (int i = 0; i < n; i++) e[i] = new int[]{T.rnd(1, 5), T.rnd(1, 5)};
            int[][] s = e.clone();
            Arrays.sort(s, (a, b) -> Integer.compare(a[0] * 10 + a[1], b[0] * 10 + b[1]));
            int[] dp = new int[n];
            int best = 0;
            for (int i = 0; i < n; i++) { dp[i] = 1; for (int j = 0; j < i; j++) if (s[j][0] < s[i][0] && s[j][1] < s[i][1]) dp[i] = Math.max(dp[i], dp[j] + 1); best = Math.max(best, dp[i]); }
            T.eq("354 rand", new LC354().maxEnvelopes(e), best);
        }
        T.eq("1626a", new LC354v1().bestTeamScore(T.ints("[1,3,5,10,15]"), T.ints("[1,2,3,4,5]")), 34);
        T.eq("1626b", new LC354v1().bestTeamScore(T.ints("[4,5,6,5]"), T.ints("[2,1,2,1]")), 16);
        T.eq("1626c", new LC354v1().bestTeamScore(T.ints("[1,2,3,5]"), T.ints("[8,9,10,1]")), 6);

        T.eq("673a", new LC673().findNumberOfLIS(T.ints("[1,3,5,4,7]")), 2);
        T.eq("673b", new LC673().findNumberOfLIS(T.ints("[2,2,2,2,2]")), 5);
        T.summary("Day13");
    }
}
