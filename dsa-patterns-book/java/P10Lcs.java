import java.util.*;

public class P10Lcs {
    //@tpl table
    // dp[i][j] = LCS length of a[0..i) and b[0..j). Row 0 and column 0 stand
    // for an empty prefix, so they stay 0 and need no special case.
    static int[][] lcsTable(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                dp[i + 1][j + 1] = a.charAt(i) == b.charAt(j)
                        ? dp[i][j] + 1                               // use both
                        : Math.max(dp[i][j + 1], dp[i + 1][j]);      // drop one
        return dp;
    }
    //@end

    //@tpl walks
    // One LCS: walk back from (m, n). Equal chars are in it; otherwise step
    // to the neighbour that kept the same length.
    static String oneLcs(String a, String b) {
        int[][] dp = lcsTable(a, b);
        StringBuilder sb = new StringBuilder();
        int i = a.length(), j = b.length();
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                sb.append(a.charAt(--i));              // in the LCS
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) i--;
            else j--;
        }
        return sb.reverse().toString();
    }

    // Shortest common supersequence: the same walk back, but every char is
    // written; a shared (LCS) char is written once.
    static String shortestCommonSupersequence(String a, String b) {
        int[][] dp = lcsTable(a, b);
        StringBuilder sb = new StringBuilder();
        int i = a.length(), j = b.length();
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                sb.append(a.charAt(--i));              // shared: written once
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) sb.append(a.charAt(--i));
            else sb.append(b.charAt(--j));
        }
        while (i > 0) sb.append(a.charAt(--i));        // one side is left over
        while (j > 0) sb.append(b.charAt(--j));
        return sb.reverse().toString();
    }
    //@end

    //@tpl rolling
    // Uncrossed lines = LCS of two int arrays, with one row of the table.
    // diag saves dp[i][j], the old row[j] that the step before overwrote.
    static int maxUncrossedLines(int[] a, int[] b) {
        int[] row = new int[b.length + 1];             // row i of the table
        for (int x : a) {
            int diag = 0;                              // dp[i][0]
            for (int j = 0; j < b.length; j++) {
                int up = row[j + 1];                   // dp[i][j + 1]
                row[j + 1] = x == b[j] ? diag + 1 : Math.max(up, row[j]);
                diag = up;
            }
        }
        return row[b.length];
    }
    //@end

    // Not printed: delete operation for two strings, m + n - 2 * LCS as the text says.
    static int minDistance(String a, String b) {
        int lcs = lcsTable(a, b)[a.length()][b.length()];
        return a.length() + b.length() - 2 * lcs;
    }

    // ---------------- tests (not printed) ----------------
    static boolean isSub(String s, String t) {           // s is a subsequence of t
        int i = 0;
        for (int j = 0; j < t.length() && i < s.length(); j++) if (s.charAt(i) == t.charAt(j)) i++;
        return i == s.length();
    }

    // Every subsequence of a: the longest that is also a subsequence of b.
    static int bruteLcs(String a, String b) {
        int best = 0;
        for (int mask = 0; mask < (1 << a.length()); mask++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < a.length(); i++) if ((mask >> i & 1) == 1) sb.append(a.charAt(i));
            if (sb.length() > best && isSub(sb.toString(), b)) best = sb.length();
        }
        return best;
    }

    static int bruteLcs(int[] a, int[] b) {
        int best = 0;
        for (int mask = 0; mask < (1 << a.length); mask++) {
            List<Integer> s = new ArrayList<>();
            for (int i = 0; i < a.length; i++) if ((mask >> i & 1) == 1) s.add(a[i]);
            int k = 0;
            for (int j = 0; j < b.length && k < s.size(); j++) if (s.get(k) == b[j]) k++;
            if (k == s.size()) best = Math.max(best, k);
        }
        return best;
    }

    // Shortest string over the alphabet that contains both: try lengths in order.
    static int bruteScsLen(String a, String b, String alphabet) {
        for (int len = Math.max(a.length(), b.length()); ; len++) {
            int total = 1;
            for (int i = 0; i < len; i++) total *= alphabet.length();
            for (int code = 0; code < total; code++) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0, c = code; i < len; i++, c /= alphabet.length()) sb.append(alphabet.charAt(c % alphabet.length()));
                String s = sb.toString();
                if (isSub(a, s) && isSub(b, s)) return len;
            }
        }
    }

    public static void main(String[] args) {
        Random r = new Random(1007);
        for (int t = 0; t < 3000; t++) {
            String abc = t % 2 == 0 ? "ab" : "abcd";
            String a = Check.str(r, r.nextInt(10), abc), b = Check.str(r, r.nextInt(10), abc);
            int want = bruteLcs(a, b);
            Check.eq(want, lcsTable(a, b)[a.length()][b.length()], "lcs " + a + " " + b);
            String one = oneLcs(a, b);
            Check.isTrue(one.length() == want && isSub(one, a) && isSub(one, b), "oneLcs " + a + " " + b + " -> " + one);
            Check.eq(a.length() + b.length() - 2 * want, minDistance(a, b), "delete " + a + " " + b);
            String scs = shortestCommonSupersequence(a, b);
            Check.isTrue(isSub(a, scs) && isSub(b, scs), "scs contains both " + a + " " + b + " -> " + scs);
            Check.eq(a.length() + b.length() - want, scs.length(), "scs length " + a + " " + b);
            int[] x = Check.ints(r, r.nextInt(10), 1, 4), y = Check.ints(r, r.nextInt(10), 1, 4);
            Check.eq(bruteLcs(x, y), maxUncrossedLines(x, y), "lines " + Arrays.toString(x) + Arrays.toString(y));
        }
        for (int t = 0; t < 300; t++) {                     // supersequence length by exhaustive search
            String a = Check.str(r, r.nextInt(5), "ab"), b = Check.str(r, r.nextInt(5), "ab");
            Check.eq(bruteScsLen(a, b, "ab"), shortestCommonSupersequence(a, b).length(), "scs brute " + a + " " + b);
        }
        Check.eq(3, lcsTable("abcde", "ace")[5][3], "LC 1143 sample 1");
        Check.eq(3, lcsTable("abc", "abc")[3][3], "LC 1143 sample 2");
        Check.eq(0, lcsTable("abc", "def")[3][3], "LC 1143 sample 3");
        Check.eq("ace", oneLcs("abcde", "ace"), "one LCS of the sample");
        Check.eq(2, minDistance("sea", "eat"), "LC 583 sample 1");
        Check.eq(4, minDistance("leetcode", "etco"), "LC 583 sample 2");
        String s1 = shortestCommonSupersequence("abac", "cab");
        Check.isTrue(s1.length() == 5 && isSub("abac", s1) && isSub("cab", s1), "LC 1092 sample 1: " + s1);
        Check.eq(8, shortestCommonSupersequence("aaaaaaaa", "aaaaaaaa").length(), "LC 1092 sample 2");
        Check.eq(2, maxUncrossedLines(new int[] {1, 4, 2}, new int[] {1, 2, 4}), "LC 1035 sample 1");
        Check.eq(3, maxUncrossedLines(new int[] {2, 5, 1, 2, 5}, new int[] {10, 5, 2, 1, 5, 2}), "LC 1035 sample 2");
        Check.eq(2, maxUncrossedLines(new int[] {1, 3, 7, 1, 7, 5}, new int[] {1, 9, 2, 5, 1}), "LC 1035 sample 3");
        Check.done("P10Lcs");
    }
}
