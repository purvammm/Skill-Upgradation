import java.util.*;

public class P10PalindromeDp {
    //@tpl lps
    // Longest palindromic subsequence. dp[i][j] = answer for s[i..j].
    // i goes down, so row i + 1 (shorter ranges) is ready when row i reads it.
    static int longestPalindromeSubseq(String s) {
        int n = s.length();
        int[][] dp = new int[n][n];
        for (int i = n - 1; i >= 0; i--) {
            dp[i][i] = 1;
            for (int j = i + 1; j < n; j++) {
                if (s.charAt(i) == s.charAt(j)) dp[i][j] = 2 + dp[i + 1][j - 1];
                else dp[i][j] = Math.max(dp[i + 1][j], dp[i][j - 1]);
            }
        }
        return dp[0][n - 1];
    }
    //@end

    //@tpl count
    // isPal[i][j]: s[i..j] is a palindrome. Count the true cells (LC 647).
    static int countSubstrings(String s) {
        int n = s.length(), count = 0;
        boolean[][] isPal = new boolean[n][n];
        for (int i = n - 1; i >= 0; i--)
            for (int j = i; j < n; j++) {
                isPal[i][j] = s.charAt(i) == s.charAt(j)
                              && (j - i < 2 || isPal[i + 1][j - 1]);
                if (isPal[i][j]) count++;
            }
        return count;
    }
    //@end

    //@tpl cuts
    // Palindrome partitioning II. cuts[j] = fewest cuts for s[0..j].
    // Column j of isPal only reads column j - 1, so both fill left to right.
    static int minCut(String s) {
        int n = s.length();
        boolean[][] isPal = new boolean[n][n];
        int[] cuts = new int[n];
        for (int j = 0; j < n; j++) {
            cuts[j] = j;                                 // one char per piece
            for (int i = 0; i <= j; i++) {
                isPal[i][j] = s.charAt(i) == s.charAt(j)
                              && (j - i < 2 || isPal[i + 1][j - 1]);
                if (isPal[i][j])                         // last piece is s[i..j]
                    cuts[j] = Math.min(cuts[j], i == 0 ? 0 : cuts[i - 1] + 1);
            }
        }
        return cuts[n - 1];
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean pal(String s) {
        for (int i = 0, j = s.length() - 1; i < j; i++, j--) if (s.charAt(i) != s.charAt(j)) return false;
        return true;
    }

    static int bruteLps(String s) {
        int n = s.length(), best = 0;
        for (int mask = 1; mask < 1 << n; mask++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) if ((mask >> i & 1) == 1) sb.append(s.charAt(i));
            if (pal(sb.toString())) best = Math.max(best, sb.length());
        }
        return best;
    }

    static int bruteCount(String s) {
        int c = 0;
        for (int i = 0; i < s.length(); i++)
            for (int j = i + 1; j <= s.length(); j++) if (pal(s.substring(i, j))) c++;
        return c;
    }

    // every partition: try each first piece, recurse (exponential)
    static int bruteCut(String s) {
        if (pal(s)) return 0;
        int best = Integer.MAX_VALUE;
        for (int e = 1; e < s.length(); e++)
            if (pal(s.substring(0, e))) best = Math.min(best, 1 + bruteCut(s.substring(e)));
        return best;
    }

    // trap: i ascending
    static int lpsWrongOrder(String s) {
        int n = s.length();
        int[][] dp = new int[n][n];
        for (int i = 0; i < n; i++) {
            dp[i][i] = 1;
            for (int j = i + 1; j < n; j++) {
                if (s.charAt(i) == s.charAt(j)) dp[i][j] = 2 + dp[i + 1][j - 1];
                else dp[i][j] = Math.max(dp[i + 1][j], dp[i][j - 1]);
            }
        }
        return dp[0][n - 1];
    }

    static int countNoGuard(String s) {
        int n = s.length(), count = 0;
        boolean[][] isPal = new boolean[n + 1][n + 1];
        for (int i = n - 1; i >= 0; i--)
            for (int j = i; j < n; j++) {
                isPal[i][j] = s.charAt(i) == s.charAt(j) && (j == i || isPal[i + 1][j - 1]);
                if (isPal[i][j]) count++;
            }
        return count;
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            String s = "bbbab";
            int n = s.length();
            int[][] dp = new int[n][n];
            for (int i = 0; i < n; i++)
                for (int j = i; j < n; j++) dp[i][j] = longestPalindromeSubseq(s.substring(i, j + 1));
            StringBuilder sb = new StringBuilder("        ");
            for (int j = 0; j < n; j++) sb.append(String.format("%3s", "" + j + s.charAt(j)));
            System.out.println(sb);
            for (int i = 0; i < n; i++) {
                sb = new StringBuilder(String.format("  %d %c   ", i, s.charAt(i)));
                for (int j = 0; j < n; j++) sb.append(j < i ? "  ." : String.format("%3d", dp[i][j]));
                System.out.println(sb);
            }
            System.out.println("wrong order bbbab: " + lpsWrongOrder("bbbab") + " cbbd: " + lpsWrongOrder("cbbd"));
            System.out.println("no guard aa: " + countNoGuard("aa") + " aaa " + countNoGuard("aaa"));
            String c = "aabba";
            for (int j = 0; j < c.length(); j++) System.out.print(minCut(c.substring(0, j + 1)) + " ");
            System.out.println();
            return;
        }
        Random r = new Random(20);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(11);
            String s = Check.str(r, n, r.nextBoolean() ? "ab" : "abc");
            Check.eq(bruteLps(s), longestPalindromeSubseq(s), "lps " + s);
            Check.eq(bruteCount(s), countSubstrings(s), "count " + s);
            Check.eq(bruteCut(s), minCut(s), "cut " + s);
        }
        Check.eq(4, longestPalindromeSubseq("bbbab"), "LC 516 sample 1");
        Check.eq(2, longestPalindromeSubseq("cbbd"), "LC 516 sample 2");
        Check.eq(3, countSubstrings("abc"), "LC 647 sample 1");
        Check.eq(6, countSubstrings("aaa"), "LC 647 sample 2");
        Check.eq(1, minCut("aab"), "LC 132 sample 1");
        Check.eq(0, minCut("a"), "LC 132 sample 2");
        Check.eq(1, minCut("ab"), "LC 132 sample 3");
        Check.done("P10PalindromeDp");
    }
}
