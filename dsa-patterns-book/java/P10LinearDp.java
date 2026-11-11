import java.util.*;

public class P10LinearDp {
    //@tpl decode
    // Decode ways ('1' -> A ... '26' -> Z).
    // dp[i] = number of ways to decode the first i characters.
    static int numDecodings(String s) {
        int n = s.length();
        int[] dp = new int[n + 1];
        dp[0] = 1;                                     // empty prefix: one way
        for (int i = 1; i <= n; i++) {
            char c = s.charAt(i - 1);
            if (c != '0') dp[i] += dp[i - 1];          // last letter is one digit
            if (i >= 2) {
                int two = (s.charAt(i - 2) - '0') * 10 + (c - '0');
                if (two >= 10 && two <= 26) dp[i] += dp[i - 2];   // two digits
            }
        }
        return dp[n];
    }
    //@end

    //@tpl wordbreak
    // Word break: ok[i] = the first i characters split into dictionary words.
    static boolean wordBreak(String s, List<String> words) {
        Set<String> dict = new HashSet<>(words);
        int maxLen = 0;
        for (String w : words) maxLen = Math.max(maxLen, w.length());
        int n = s.length();
        boolean[] ok = new boolean[n + 1];
        ok[0] = true;
        for (int i = 1; i <= n; i++)                   // last word is s[j..i)
            for (int j = i - 1; j >= Math.max(0, i - maxLen) && !ok[i]; j--)
                if (ok[j] && dict.contains(s.substring(j, i))) ok[i] = true;
        return ok[n];
    }
    //@end

    //@tpl reduce
    // House robber II: houses in a circle, so house 0 and house n-1 touch.
    // An answer skips house 0 or skips house n-1: run the line version twice.
    static int robCircle(int[] a) {
        int n = a.length;
        if (n == 1) return a[0];
        return Math.max(robLine(a, 0, n - 2), robLine(a, 1, n - 1));
    }

    static int robLine(int[] a, int lo, int hi) {     // house robber on a[lo..hi]
        int prev2 = 0, prev1 = 0;
        for (int i = lo; i <= hi; i++) {
            int cur = Math.max(prev1, prev2 + a[i]);
            prev2 = prev1;
            prev1 = cur;
        }
        return prev1;
    }

    // Delete and earn: taking value v deletes every v-1 and v+1. Total the
    // points per value; then values are houses in a row.
    static int deleteAndEarn(int[] nums) {
        int max = 0;
        for (int x : nums) max = Math.max(max, x);
        int[] points = new int[max + 1];
        for (int x : nums) points[x] += x;
        return robLine(points, 0, max);
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int bruteDecode(String s, int i) {
        if (i == s.length()) return 1;
        int ways = 0;
        if (s.charAt(i) != '0') ways += bruteDecode(s, i + 1);
        if (i + 1 < s.length()) {
            int two = Integer.parseInt(s.substring(i, i + 2));
            if (two >= 10 && two <= 26) ways += bruteDecode(s, i + 2);
        }
        return ways;
    }

    static boolean bruteBreak(String s, Set<String> dict) {
        if (s.isEmpty()) return true;
        for (int k = 1; k <= s.length(); k++)
            if (dict.contains(s.substring(0, k)) && bruteBreak(s.substring(k), dict)) return true;
        return false;
    }

    static int bruteCircle(int[] a) {
        int n = a.length, best = 0;
        for (int mask = 0; mask < (1 << n); mask++) {
            boolean okm = true;
            for (int i = 0; i < n && okm; i++) {
                int j = (i + 1) % n;
                if (j != i && (mask >> i & 1) == 1 && (mask >> j & 1) == 1) okm = false;
            }
            if (!okm) continue;
            int s = 0;
            for (int i = 0; i < n; i++) if ((mask >> i & 1) == 1) s += a[i];
            best = Math.max(best, s);
        }
        return best;
    }

    // Play the game itself: pick any remaining element, delete it and every +-1.
    static int bruteEarn(int[] nums, int mask, Map<Integer, Integer> memo) {
        if (mask == 0) return 0;
        Integer got = memo.get(mask);
        if (got != null) return got;
        int best = 0;
        for (int i = 0; i < nums.length; i++) {
            if ((mask >> i & 1) == 0) continue;
            int rest = mask & ~(1 << i);
            for (int j = 0; j < nums.length; j++)
                if (Math.abs(nums[j] - nums[i]) == 1) rest &= ~(1 << j);
            best = Math.max(best, nums[i] + bruteEarn(nums, rest, memo));
        }
        memo.put(mask, best);
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(1002);
        for (int t = 0; t < 3000; t++) {
            String s = Check.str(r, r.nextInt(13), r.nextBoolean() ? "0123" : "0126789");
            Check.eq(bruteDecode(s, 0), numDecodings(s), "decode " + s);

            List<String> words = new ArrayList<>();
            int k = 1 + r.nextInt(4);
            for (int i = 0; i < k; i++) words.add(Check.str(r, 1 + r.nextInt(3), "ab"));
            String w = Check.str(r, r.nextInt(12), "ab");
            Check.eq(bruteBreak(w, new HashSet<>(words)), wordBreak(w, words), "break " + w + " " + words);

            int[] a = Check.ints(r, 1 + r.nextInt(12), 0, 20);
            Check.eq(bruteCircle(a), robCircle(a), "circle " + Arrays.toString(a));

            int[] nums = Check.ints(r, 1 + r.nextInt(10), 1, 8);
            Check.eq(bruteEarn(nums, (1 << nums.length) - 1, new HashMap<Integer, Integer>()),
                     deleteAndEarn(nums), "earn " + Arrays.toString(nums));
        }
        Check.eq(2, numDecodings("12"), "LC 91 sample 1");
        Check.eq(3, numDecodings("226"), "LC 91 sample 2");
        Check.eq(0, numDecodings("06"), "LC 91 sample 3");
        Check.eq(2, numDecodings("11106"), "LC 91 statement example");
        Check.eq(true, wordBreak("leetcode", Arrays.asList("leet", "code")), "LC 139 sample 1");
        Check.eq(true, wordBreak("applepenapple", Arrays.asList("apple", "pen")), "LC 139 sample 2");
        Check.eq(false, wordBreak("catsandog", Arrays.asList("cats", "dog", "sand", "and", "cat")), "LC 139 sample 3");
        Check.eq(3, robCircle(new int[] {2, 3, 2}), "LC 213 sample 1");
        Check.eq(4, robCircle(new int[] {1, 2, 3, 1}), "LC 213 sample 2");
        Check.eq(3, robCircle(new int[] {1, 2, 3}), "LC 213 sample 3");
        Check.eq(6, deleteAndEarn(new int[] {3, 4, 2}), "LC 740 sample 1");
        Check.eq(9, deleteAndEarn(new int[] {2, 2, 3, 3, 3, 4}), "LC 740 sample 2");
        Check.done("P10LinearDp");
    }
}
