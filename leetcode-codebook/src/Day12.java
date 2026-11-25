import java.util.*;

/*@core Two-string DP table
note: `dp[i][j]` = the answer for the first i chars of s and the first j chars of t. Row 0 and column 0 are the empty prefixes. Each cell looks at most at three neighbours: diagonal (use both last chars), up (drop s's last), left (drop t's last).
@*/
class Core12 {
    static String lcsString(String s, String t) {      // also prints one LCS by walking back
        int n = s.length(), m = t.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++)
            for (int j = 1; j <= m; j++)
                dp[i][j] = s.charAt(i - 1) == t.charAt(j - 1) ? dp[i - 1][j - 1] + 1
                        : Math.max(dp[i - 1][j], dp[i][j - 1]);
        StringBuilder sb = new StringBuilder();
        for (int i = n, j = m; i > 0 && j > 0; ) {
            if (s.charAt(i - 1) == t.charAt(j - 1)) { sb.append(s.charAt(i - 1)); i--; j--; }
            else if (dp[i - 1][j] >= dp[i][j - 1]) i--;
            else j--;
        }
        return sb.reverse().toString();
    }
}

/*@LC 1143
tag: LCS
cue: Longest common subsequence of two strings.
hook: Same last letter: diagonal plus one. Different: the better of dropping either.
time: O(n·m) time · O(n·m) space
trap: The table is (n+1)×(m+1); `charAt(i − 1)` is the i-th character.
var: LC 583 Delete Operation for Two Strings | `n + m − 2·LCS`.
var: LC 1035 Uncrossed Lines | LCS on two int arrays.
@*/
class LC1143 {
    public int longestCommonSubsequence(String text1, String text2) {
        int n = text1.length(), m = text2.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++)
            for (int j = 1; j <= m; j++)
                dp[i][j] = text1.charAt(i - 1) == text2.charAt(j - 1)
                        ? dp[i - 1][j - 1] + 1                         //* match: diagonal + 1
                        : Math.max(dp[i - 1][j], dp[i][j - 1]);        //* drop one char
        return dp[n][m];
    }
}

/*@var LC 718 · Maximum Length of Repeated Subarray (contiguous!)
note: Substring, not subsequence: `dp[i][j]` = common run ENDING at both positions; a mismatch resets to 0. Answer = the max cell.
@*/
class LC1143v1 {
    public int findLength(int[] nums1, int[] nums2) {
        int[][] dp = new int[nums1.length + 1][nums2.length + 1];
        int best = 0;
        for (int i = 1; i <= nums1.length; i++)
            for (int j = 1; j <= nums2.length; j++)
                if (nums1[i - 1] == nums2[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    best = Math.max(best, dp[i][j]);
                }
        return best;
    }
}

/*@LC 72
tag: Edit distance
cue: Fewest insert / delete / replace operations to turn word1 into word2.
hook: Match is free; otherwise one plus the best of replace (diagonal), delete (up), insert (left).
time: O(n·m) time · O(n·m) space
trap: Base cases: `dp[i][0] = i` and `dp[0][j] = j`, not zeros.
@*/
class LC72 {
    public int minDistance(String word1, String word2) {
        int n = word1.length(), m = word2.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = i;                    //* delete all
        for (int j = 0; j <= m; j++) dp[0][j] = j;                    //* insert all
        for (int i = 1; i <= n; i++)
            for (int j = 1; j <= m; j++)
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) dp[i][j] = dp[i - 1][j - 1];   //*
                else dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],                                 //* replace
                                    Math.min(dp[i - 1][j], dp[i][j - 1]));                    //* delete, insert
        return dp[n][m];
    }
}

/*@var LC 712 · Minimum ASCII Delete Sum for Two Strings
note: Only deletions, and each costs the character's ASCII value. Base rows are running ASCII sums.
@*/
class LC72v1 {
    public int minimumDeleteSum(String s1, String s2) {
        int n = s1.length(), m = s2.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) dp[i][0] = dp[i - 1][0] + s1.charAt(i - 1);
        for (int j = 1; j <= m; j++) dp[0][j] = dp[0][j - 1] + s2.charAt(j - 1);
        for (int i = 1; i <= n; i++)
            for (int j = 1; j <= m; j++)
                dp[i][j] = s1.charAt(i - 1) == s2.charAt(j - 1) ? dp[i - 1][j - 1]
                        : Math.min(dp[i - 1][j] + s1.charAt(i - 1), dp[i][j - 1] + s2.charAt(j - 1));
        return dp[n][m];
    }
}

/*@LC 516
tag: Interval DP on one string
cue: Longest palindromic subsequence.
hook: Equal ends: inside plus two. Different ends: drop one end.
time: O(n²) time · O(n²) space
trap: Loop l from n−1 DOWN to 0 and r from l+1 UP, so `dp[l+1][…]` is ready.
var: Same answer another way | `LCS(s, reverse(s))`.
@*/
class LC516 {
    public int longestPalindromeSubseq(String s) {
        int n = s.length();
        int[][] dp = new int[n][n];
        for (int l = n - 1; l >= 0; l--) {                              //* l goes down
            dp[l][l] = 1;
            for (int r = l + 1; r < n; r++)
                dp[l][r] = s.charAt(l) == s.charAt(r)
                        ? dp[l + 1][r - 1] + 2                          //* both ends
                        : Math.max(dp[l + 1][r], dp[l][r - 1]);         //* drop one end
        }
        return dp[0][n - 1];
    }
}

/*@var LC 647 · Palindromic Substrings (contiguous)
note: Substrings: expand around each of the 2n − 1 centres (letters and gaps). No table needed.
@*/
class LC516v1 {
    public int countSubstrings(String s) {
        int count = 0;
        for (int c = 0; c < 2 * s.length() - 1; c++) {
            int l = c / 2, r = l + c % 2;
            while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) { count++; l--; r++; }
        }
        return count;
    }
}

/*@LC 1312
tag: Interval DP · insertions
cue: Fewest insertions to make a string a palindrome.
hook: Equal ends cost nothing; otherwise insert a copy of one end and pay one.
time: O(n²) time · O(n²) space
trap: It equals `n − longestPalindromeSubseq(s)`; use that if you remember LC 516 better.
var: LC 1216 Valid Palindrome III (premium) | Possible with ≤ k removals iff `n − LPS ≤ k`.
@*/
class LC1312 {
    public int minInsertions(String s) {
        int n = s.length();
        int[][] dp = new int[n][n];
        for (int l = n - 1; l >= 0; l--)
            for (int r = l + 1; r < n; r++)
                dp[l][r] = s.charAt(l) == s.charAt(r)
                        ? dp[l + 1][r - 1]                              //*
                        : 1 + Math.min(dp[l + 1][r], dp[l][r - 1]);     //*
        return dp[0][n - 1];
    }
}

/*@LC 115
tag: Counting subsequences
cue: How many ways does t appear in s as a subsequence?
hook: Each s-char can skip, or match t's j-th char and add the ways for j − 1.
time: O(n·m) time · O(m) space
trap: With one row, loop j from RIGHT to LEFT so `dp[j − 1]` is still from the previous s-char.
var: LC 392 Is Subsequence | Two pointers: walk s, advance in t on a match.
@*/
class LC115 {
    public int numDistinct(String s, String t) {
        int m = t.length();
        long[] dp = new long[m + 1];                          // ways to form t[0..j) so far
        dp[0] = 1;                                            //*
        for (int i = 0; i < s.length(); i++)
            for (int j = m; j >= 1; j--)                      //* right to left
                if (s.charAt(i) == t.charAt(j - 1)) dp[j] += dp[j - 1];   //*
        return (int) dp[m];
    }
}

/*@var LC 940 · Distinct Subsequences II (count distinct subsequences of s)
note: Keep the count of distinct subsequences ending with each letter. A new c replaces that letter's count with (all so far + 1).
@*/
class LC115v1 {
    public int distinctSubseqII(String s) {
        final int MOD = 1_000_000_007;
        long[] endsWith = new long[26];
        for (char c : s.toCharArray()) {
            long total = 0;
            for (long v : endsWith) total += v;
            endsWith[c - 'a'] = (total + 1) % MOD;
        }
        long ans = 0;
        for (long v : endsWith) ans += v;
        return (int) (ans % MOD);
    }
}

/*@LC 97
tag: Two-string reachability
cue: Is s3 an interleaving of s1 and s2?
hook: The last char of s3's prefix came from s1 or from s2.
time: O(n·m) time · O(n·m) space
trap: Check `n + m == s3.length()` first.
@*/
class LC97 {
    public boolean isInterleave(String s1, String s2, String s3) {
        int n = s1.length(), m = s2.length();
        if (n + m != s3.length()) return false;                                         //*
        boolean[][] dp = new boolean[n + 1][m + 1];
        dp[0][0] = true;
        for (int i = 0; i <= n; i++)
            for (int j = 0; j <= m; j++) {
                if (i == 0 && j == 0) continue;
                char c = s3.charAt(i + j - 1);
                if (i > 0 && s1.charAt(i - 1) == c && dp[i - 1][j]) dp[i][j] = true;     //* from s1
                if (j > 0 && s2.charAt(j - 1) == c && dp[i][j - 1]) dp[i][j] = true;     //* from s2
            }
        return dp[n][m];
    }
}

/*@LC 1092
tag: LCS table + walk back
cue: Shortest string that has both strings as subsequences.
hook: Build the LCS table, walk back from the corner: shared chars once, the rest from whichever side you drop.
time: O(n·m) time · O(n·m) space
trap: After the loop, copy whatever is left of either string, then reverse.
var: Print the LCS itself | Same walk, but append only on a match (see the pattern core).
@*/
class LC1092 {
    public String shortestCommonSupersequence(String str1, String str2) {
        int n = str1.length(), m = str2.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++)
            for (int j = 1; j <= m; j++)
                dp[i][j] = str1.charAt(i - 1) == str2.charAt(j - 1) ? dp[i - 1][j - 1] + 1
                        : Math.max(dp[i - 1][j], dp[i][j - 1]);
        StringBuilder sb = new StringBuilder();
        int i = n, j = m;
        while (i > 0 && j > 0) {                                                  //* walk back
            if (str1.charAt(i - 1) == str2.charAt(j - 1)) { sb.append(str1.charAt(i - 1)); i--; j--; }   //* once
            else if (dp[i - 1][j] >= dp[i][j - 1]) sb.append(str1.charAt(--i));
            else sb.append(str2.charAt(--j));
        }
        while (i > 0) sb.append(str1.charAt(--i));                                //* leftovers
        while (j > 0) sb.append(str2.charAt(--j));
        return sb.reverse().toString();
    }
}

public class Day12 {
    static boolean isSub(String small, String big) {
        int i = 0;
        for (int j = 0; j < big.length() && i < small.length(); j++) if (small.charAt(i) == big.charAt(j)) i++;
        return i == small.length();
    }

    static String rs(int n, int letters) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append((char) ('a' + T.rnd(0, letters - 1)));
        return sb.toString();
    }

    static long countSub(String s, String t, int i, int j) {
        if (j == t.length()) return 1;
        if (i == s.length()) return 0;
        return countSub(s, t, i + 1, j) + (s.charAt(i) == t.charAt(j) ? countSub(s, t, i + 1, j + 1) : 0);
    }

    static boolean inter(String a, String b, String c) {
        if (c.isEmpty()) return a.isEmpty() && b.isEmpty();
        return (!a.isEmpty() && a.charAt(0) == c.charAt(0) && inter(a.substring(1), b, c.substring(1)))
            || (!b.isEmpty() && b.charAt(0) == c.charAt(0) && inter(a, b.substring(1), c.substring(1)));
    }

    public static void main(String[] args) {
        T.eq("core lcs", Core12.lcsString("abcde", "ace"), "ace");

        T.eq("1143a", new LC1143().longestCommonSubsequence("abcde", "ace"), 3);
        T.eq("1143b", new LC1143().longestCommonSubsequence("abc", "abc"), 3);
        T.eq("1143c", new LC1143().longestCommonSubsequence("abc", "def"), 0);
        T.eq("718a", new LC1143v1().findLength(T.ints("[1,2,3,2,1]"), T.ints("[3,2,1,4,7]")), 3);
        T.eq("718b", new LC1143v1().findLength(T.ints("[0,0,0,0,0]"), T.ints("[0,0,0,0,0]")), 5);

        T.eq("72a", new LC72().minDistance("horse", "ros"), 3);
        T.eq("72b", new LC72().minDistance("intention", "execution"), 5);
        T.eq("72c", new LC72().minDistance("", "a"), 1);
        T.eq("712a", new LC72v1().minimumDeleteSum("sea", "eat"), 231);
        T.eq("712b", new LC72v1().minimumDeleteSum("delete", "leet"), 403);

        T.eq("516a", new LC516().longestPalindromeSubseq("bbbab"), 4);
        T.eq("516b", new LC516().longestPalindromeSubseq("cbbd"), 2);
        T.eq("647a", new LC516v1().countSubstrings("abc"), 3);
        T.eq("647b", new LC516v1().countSubstrings("aaa"), 6);
        T.eq("1312a", new LC1312().minInsertions("zzazz"), 0);
        T.eq("1312b", new LC1312().minInsertions("mbadm"), 2);
        T.eq("1312c", new LC1312().minInsertions("leetcode"), 5);
        for (int t = 0; t < 200; t++) {
            String s = rs(T.rnd(1, 10), 3), u = rs(T.rnd(1, 8), 3);
            String rev = new StringBuilder(s).reverse().toString();
            int lps = new LC1143().longestCommonSubsequence(s, rev);
            T.eq("516 rand", new LC516().longestPalindromeSubseq(s), lps);
            T.eq("1312 rand", new LC1312().minInsertions(s), s.length() - lps);
            int best = 0;
            for (int m = 0; m < 1 << s.length(); m++) {
                StringBuilder c = new StringBuilder();
                for (int i = 0; i < s.length(); i++) if ((m >> i & 1) == 1) c.append(s.charAt(i));
                if (isSub(c.toString(), u)) best = Math.max(best, c.length());
            }
            T.eq("1143 rand", new LC1143().longestCommonSubsequence(s, u), best);
            int pal = 0;
            for (int i = 0; i < s.length(); i++) for (int j = i + 1; j <= s.length(); j++) { String x = s.substring(i, j); if (x.equals(new StringBuilder(x).reverse().toString())) pal++; }
            T.eq("647 rand", new LC516v1().countSubstrings(s), pal);
            T.eq("115 rand", new LC115().numDistinct(s, u.substring(0, Math.min(3, u.length()))), (int) countSub(s, u.substring(0, Math.min(3, u.length())), 0, 0));
            String sc = new LC1092().shortestCommonSupersequence(s, u);
            T.ok("1092 rand", isSub(s, sc) && isSub(u, sc) && sc.length() == s.length() + u.length() - best);
            String a = rs(T.rnd(0, 4), 2), b = rs(T.rnd(0, 4), 2), c = rs(a.length() + b.length() + T.rnd(-1, 1) * (T.rnd(0, 3) == 0 ? 1 : 0), 2);
            T.eq("97 rand", new LC97().isInterleave(a, b, c), a.length() + b.length() == c.length() && inter(a, b, c));
        }

        T.eq("115a", new LC115().numDistinct("rabbbit", "rabbit"), 3);
        T.eq("115b", new LC115().numDistinct("babgbag", "bag"), 5);
        T.eq("940a", new LC115v1().distinctSubseqII("abc"), 7);
        T.eq("940b", new LC115v1().distinctSubseqII("aba"), 6);
        T.eq("940c", new LC115v1().distinctSubseqII("aaa"), 3);

        T.eq("97a", new LC97().isInterleave("aabcc", "dbbca", "aadbbcbcac"), true);
        T.eq("97b", new LC97().isInterleave("aabcc", "dbbca", "aadbbbaccc"), false);
        T.eq("97c", new LC97().isInterleave("", "", ""), true);

        String sc = new LC1092().shortestCommonSupersequence("abac", "cab");
        T.ok("1092a", sc.length() == 5 && isSub("abac", sc) && isSub("cab", sc));
        T.eq("1092b", new LC1092().shortestCommonSupersequence("aaaaaaaa", "aaaaaaaa"), "aaaaaaaa");
        T.summary("Day12");
    }
}
