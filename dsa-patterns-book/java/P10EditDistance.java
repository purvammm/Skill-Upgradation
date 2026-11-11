import java.util.*;

public class P10EditDistance {
    //@tpl edit
    // dp[i][j] = fewest edits that turn a[0..i) into b[0..j).
    static int minDistance(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;       // delete all i chars
        for (int j = 0; j <= n; j++) dp[0][j] = j;       // insert all j chars
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];         // last chars match: free
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],   // replace
                                   Math.min(dp[i - 1][j],       // delete a[i-1]
                                            dp[i][j - 1]));     // insert b[j-1]
                }
            }
        return dp[m][n];
    }
    //@end

    //@tpl count
    // Distinct subsequences: ways[i][j] = ways to pick t[0..j) out of s[0..i).
    static int numDistinct(String s, String t) {
        int m = s.length(), n = t.length();
        int[][] ways = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) ways[i][0] = 1;     // empty t: one way
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++) {
                ways[i][j] = ways[i - 1][j];             // do not use s[i-1]
                if (s.charAt(i - 1) == t.charAt(j - 1))
                    ways[i][j] += ways[i - 1][j - 1];    // use it as t[j-1]
            }
        return ways[m][n];
    }

    // Interleaving string: ok[i][j] = s3[0..i+j) interleaves s1[0..i), s2[0..j).
    static boolean isInterleave(String s1, String s2, String s3) {
        int m = s1.length(), n = s2.length();
        if (m + n != s3.length()) return false;
        boolean[][] ok = new boolean[m + 1][n + 1];
        ok[0][0] = true;
        for (int i = 0; i <= m; i++)
            for (int j = 0; j <= n; j++) {
                if (i == 0 && j == 0) continue;
                char c = s3.charAt(i + j - 1);           // the char placed last
                ok[i][j] = (i > 0 && ok[i - 1][j] && s1.charAt(i - 1) == c)
                        || (j > 0 && ok[i][j - 1] && s2.charAt(j - 1) == c);
            }
        return ok[m][n];
    }
    //@end

    //@tpl wildcard
    // Wildcard matching: '?' is any one char, '*' is any run, also empty.
    static boolean isMatch(String s, String p) {
        int m = s.length(), n = p.length();
        boolean[][] dp = new boolean[m + 1][n + 1];      // s[0..i) matches p[0..j)
        dp[0][0] = true;
        for (int j = 1; j <= n; j++)                     // only stars match ""
            dp[0][j] = dp[0][j - 1] && p.charAt(j - 1) == '*';
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++) {
                char c = p.charAt(j - 1);
                if (c == '*') dp[i][j] = dp[i][j - 1]    // '*' matches nothing
                                      || dp[i - 1][j];   // '*' also takes s[i-1]
                else dp[i][j] = dp[i - 1][j - 1]
                                && (c == '?' || c == s.charAt(i - 1));
            }
        return dp[m][n];
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Edit distance by BFS over every string reachable with single edits.
    static int bruteEdit(String a, String b, String alpha) {
        int maxLen = Math.max(a.length(), b.length());
        Map<String, Integer> dist = new HashMap<>();
        Deque<String> q = new ArrayDeque<>();
        dist.put(a, 0);
        q.add(a);
        while (!q.isEmpty()) {
            String x = q.poll();
            int d = dist.get(x);
            if (x.equals(b)) return d;
            List<String> next = new ArrayList<>();
            for (int i = 0; i < x.length(); i++) {
                next.add(x.substring(0, i) + x.substring(i + 1));
                for (char c : alpha.toCharArray()) next.add(x.substring(0, i) + c + x.substring(i + 1));
            }
            if (x.length() < maxLen)
                for (int i = 0; i <= x.length(); i++)
                    for (char c : alpha.toCharArray()) next.add(x.substring(0, i) + c + x.substring(i));
            for (String y : next)
                if (!dist.containsKey(y)) { dist.put(y, d + 1); q.add(y); }
        }
        return -1;
    }

    static int bruteDistinct(String s, String t) {
        int m = s.length(), cnt = 0;
        for (int mask = 0; mask < 1 << m; mask++) {
            if (Integer.bitCount(mask) != t.length()) continue;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < m; i++) if ((mask >> i & 1) == 1) sb.append(s.charAt(i));
            if (sb.toString().equals(t)) cnt++;
        }
        return cnt;
    }

    static boolean bruteInterleave(String s1, String s2, String s3) {
        int L = s3.length();
        if (s1.length() + s2.length() != L) return false;
        for (int mask = 0; mask < 1 << L; mask++) {
            if (Integer.bitCount(mask) != s1.length()) continue;
            StringBuilder a = new StringBuilder(), b = new StringBuilder();
            for (int i = 0; i < L; i++) {
                if ((mask >> i & 1) == 1) a.append(s3.charAt(i));
                else b.append(s3.charAt(i));
            }
            if (a.toString().equals(s1) && b.toString().equals(s2)) return true;
        }
        return false;
    }

    static boolean bruteWildcard(String s, String p) {
        String re = p.replace("?", ".").replace("*", ".*");
        return s.matches(re);
    }

    static String table(String a, String b) {
        int m = a.length(), n = b.length();
        StringBuilder sb = new StringBuilder("       \"\"");
        for (int j = 0; j < n; j++) sb.append("  ").append(b.charAt(j));
        sb.append('\n');
        for (int i = 0; i <= m; i++) {
            sb.append(i == 0 ? "  \"\"  " : "  " + a.charAt(i - 1) + "   ");
            for (int j = 0; j <= n; j++)
                sb.append(String.format("%3d", minDistance(a.substring(0, i), b.substring(0, j))));
            sb.append('\n');
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            System.out.print(table("horse", "ros"));
            System.out.println("wrong base row: " + isMatchWrongBase("", "a*"));
            return;
        }
        Random r = new Random(10);
        for (int t = 0; t < 600; t++) {
            String a = Check.str(r, r.nextInt(5), "abc"), b = Check.str(r, r.nextInt(5), "abc");
            Check.eq(bruteEdit(a, b, "abc"), minDistance(a, b), "edit " + a + " " + b);
        }
        for (int t = 0; t < 2000; t++) {
            String s = Check.str(r, r.nextInt(11), "ab"), u = Check.str(r, r.nextInt(4), "ab");
            Check.eq(bruteDistinct(s, u), numDistinct(s, u), "distinct " + s + " " + u);
            String s1 = Check.str(r, r.nextInt(5), "ab"), s2 = Check.str(r, r.nextInt(5), "ab");
            String s3 = r.nextInt(3) == 0 ? Check.str(r, s1.length() + s2.length() + r.nextInt(2), "ab")
                                          : shuffleMerge(r, s1, s2);
            Check.eq(bruteInterleave(s1, s2, s3), isInterleave(s1, s2, s3), "inter " + s1 + " " + s2 + " " + s3);
            String w = Check.str(r, r.nextInt(7), "ab"), p = Check.str(r, r.nextInt(6), "ab?*");
            Check.eq(bruteWildcard(w, p), isMatch(w, p), "wild " + w + " " + p);
        }
        // int overflow in unused cells: compare with exact BigInteger counts
        int overflowCases = 0;
        for (int t = 0; t < 400 && overflowCases < 20; t++) {
            String s = Check.str(r, 50 + r.nextInt(30), "ab");
            String u = Check.str(r, 8 + r.nextInt(25), "ab");
            java.math.BigInteger[] row = new java.math.BigInteger[u.length() + 1];
            Arrays.fill(row, java.math.BigInteger.ZERO);
            row[0] = java.math.BigInteger.ONE;
            java.math.BigInteger biggest = java.math.BigInteger.ZERO;
            for (int i = 1; i <= s.length(); i++)
                for (int j = u.length(); j >= 1; j--) {
                    if (s.charAt(i - 1) == u.charAt(j - 1)) row[j] = row[j].add(row[j - 1]);
                    if (row[j].compareTo(biggest) > 0) biggest = row[j];
                }
            java.math.BigInteger lim = java.math.BigInteger.valueOf(Integer.MAX_VALUE);
            if (row[u.length()].compareTo(lim) > 0 || biggest.compareTo(lim) <= 0) continue;
            overflowCases++;
            Check.eq(row[u.length()].longValue(), numDistinct(s, u), "distinct with overflow " + s + " " + u);
        }
        Check.isTrue(overflowCases > 0, "found overflow cases");
        Check.eq(3, minDistance("horse", "ros"), "LC 72 sample 1");
        Check.eq(5, minDistance("intention", "execution"), "LC 72 sample 2");
        Check.eq(3, numDistinct("rabbbit", "rabbit"), "LC 115 sample 1");
        Check.eq(5, numDistinct("babgbag", "bag"), "LC 115 sample 2");
        Check.eq(true, isInterleave("aabcc", "dbbca", "aadbbcbcac"), "LC 97 sample 1");
        Check.eq(false, isInterleave("aabcc", "dbbca", "aadbbbaccc"), "LC 97 sample 2");
        Check.eq(true, isInterleave("", "", ""), "LC 97 sample 3");
        Check.eq(false, isMatch("aa", "a"), "LC 44 sample 1");
        Check.eq(true, isMatch("aa", "*"), "LC 44 sample 2");
        Check.eq(false, isMatch("cb", "?a"), "LC 44 sample 3");
        Check.eq(true, isMatch("adceb", "*a*b"), "LC 44 extra");
        Check.eq(false, isMatch("acdcb", "a*c?b"), "LC 44 extra 2");
        Check.done("P10EditDistance");
    }

    static String shuffleMerge(Random r, String a, String b) {
        StringBuilder sb = new StringBuilder();
        int i = 0, j = 0;
        while (i < a.length() || j < b.length()) {
            if (j == b.length() || (i < a.length() && r.nextBoolean())) sb.append(a.charAt(i++));
            else sb.append(b.charAt(j++));
        }
        return sb.toString();
    }

    // The trap from the book: base row without "&& dp[0][j-1]".
    static boolean isMatchWrongBase(String s, String p) {
        int m = s.length(), n = p.length();
        boolean[][] dp = new boolean[m + 1][n + 1];
        dp[0][0] = true;
        for (int j = 1; j <= n; j++) dp[0][j] = p.charAt(j - 1) == '*';
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++) {
                char c = p.charAt(j - 1);
                if (c == '*') dp[i][j] = dp[i][j - 1] || dp[i - 1][j];
                else dp[i][j] = dp[i - 1][j - 1] && (c == '?' || c == s.charAt(i - 1));
            }
        return dp[m][n];
    }
}
