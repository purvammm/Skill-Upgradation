import java.util.*;

public class P11Manacher {
    //@tpl radius
    // t = "#s0#s1#...#s(n-1)#" has 2n + 1 chars and only odd palindromes.
    // p[i] = radius of the longest palindrome of t centred at i. It equals the
    // length of the palindrome of s with the same centre, which starts at
    // s[(i - p[i]) / 2].
    static int[] radii(String s) {
        int m = 2 * s.length() + 1;
        char[] t = new char[m];
        for (int i = 0; i < m; i++) t[i] = i % 2 == 0 ? '#' : s.charAt(i / 2);
        int[] p = new int[m];
        for (int i = 0, c = 0, r = 0; i < m; i++) {     // t[2c-r..r] is a palindrome
            if (i < r) p[i] = Math.min(r - i, p[2 * c - i]);   // mirror rule
            while (i - p[i] - 1 >= 0 && i + p[i] + 1 < m
                    && t[i - p[i] - 1] == t[i + p[i] + 1]) p[i]++;
            if (i + p[i] > r) {                         // reaches further right
                c = i;
                r = i + p[i];
            }
        }
        return p;
    }
    //@end

    //@tpl use
    static String longestPalindrome(String s) {           // LC 5 in O(n)
        int[] p = radii(s);
        int best = 0;
        for (int i = 1; i < p.length; i++) if (p[i] > p[best]) best = i;
        int start = (best - p[best]) / 2;
        return s.substring(start, start + p[best]);
    }

    // LC 647 in O(n): centre i holds palindromes of length p[i], p[i] - 2,
    // ..., down to 1 or 2, which is (p[i] + 1) / 2 of them.
    static long countSubstrings(String s) {
        long count = 0;
        for (int r : radii(s)) count += (r + 1) / 2;
        return count;
    }
    //@end

    //@tpl product
    // LC 1960: two non-overlapping odd palindromes, largest product of lengths.
    // left[i] = longest one inside s[0..i], right[i] = inside s[i..n-1].
    static long maxProduct(String s) {
        int n = s.length();
        int[] p = radii(s), left = new int[n], right = new int[n];
        for (int j = 0; j < n; j++) {          // odd centre s[j] is t[2j + 1]
            int len = p[2 * j + 1], h = len / 2;
            left[j + h] = Math.max(left[j + h], len);     // ends at j + h
            right[j - h] = Math.max(right[j - h], len);   // starts at j - h
        }
        // Drop one char from each end: a palindrome ending at i + 1 gives a
        // palindrome two shorter ending at i (and the same for starts).
        for (int i = n - 2; i >= 0; i--) left[i] = Math.max(left[i], left[i + 1] - 2);
        for (int i = 1; i < n; i++) right[i] = Math.max(right[i], right[i - 1] - 2);
        for (int i = 1; i < n; i++) left[i] = Math.max(left[i], left[i - 1]);
        for (int i = n - 2; i >= 0; i--) right[i] = Math.max(right[i], right[i + 1]);
        long best = 0;
        for (int i = 0; i + 1 < n; i++)                  // split after i
            best = Math.max(best, (long) left[i] * right[i + 1]);
        return best;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean isPal(String s, int i, int j) {
        while (i < j) if (s.charAt(i++) != s.charAt(j--)) return false;
        return true;
    }

    static int[] bruteRadii(String s) {
        int m = 2 * s.length() + 1;
        char[] t = new char[m];
        for (int i = 0; i < m; i++) t[i] = i % 2 == 0 ? '#' : s.charAt(i / 2);
        int[] p = new int[m];
        for (int i = 0; i < m; i++) while (i - p[i] - 1 >= 0 && i + p[i] + 1 < m && t[i - p[i] - 1] == t[i + p[i] + 1]) p[i]++;
        return p;
    }

    static long bruteProduct(String s) {
        int n = s.length();
        long best = 0;
        for (int i = 0; i < n; i++)
            for (int j = i; j < n; j += 2)
                if (isPal(s, i, j))
                    for (int k = j + 1; k < n; k++)
                        for (int l = k; l < n; l += 2)
                            if (isPal(s, k, l)) best = Math.max(best, (long) (j - i + 1) * (l - k + 1));
        return best;
    }

    static int[] radiiNoMin(String s) {
        int m = 2 * s.length() + 1;
        char[] t = new char[m];
        for (int i = 0; i < m; i++) t[i] = i % 2 == 0 ? '#' : s.charAt(i / 2);
        int[] p = new int[m];
        for (int i = 0, c = 0, r = 0; i < m; i++) {
            if (i < r) p[i] = p[2 * c - i];
            while (i - p[i] - 1 >= 0 && i + p[i] + 1 < m && t[i - p[i] - 1] == t[i + p[i] + 1]) p[i]++;
            if (i + p[i] > r) { c = i; r = i + p[i]; }
        }
        return p;
    }

    // variant: O(1) palindrome test of s[l..r] from p
    static boolean isPalindrome(int[] p, int l, int r) { return p[l + r + 1] >= r - l + 1; }

    // variant: longest palindromic prefix -> shortest palindrome (LC 214)
    static String shortestPalindrome(String s) {
        int[] p = radii(s);
        int keep = 0;
        for (int i = 0; i < p.length; i++) if (i - p[i] == 0) keep = Math.max(keep, p[i]);
        return new StringBuilder(s.substring(keep)).reverse().toString() + s;
    }

    public static void main(String[] args) {
        Random r = new Random(1106);
        for (int t = 0; t < 4000; t++) {
            String alpha = t % 3 == 0 ? "ab" : (t % 3 == 1 ? "abc" : "a#");
            String s = Check.str(r, r.nextInt(16), alpha);
            Check.eq(bruteRadii(s), radii(s), "radii " + s);
            int bestLen = 0;
            long cnt = 0;
            for (int i = 0; i < s.length(); i++)
                for (int j = i; j < s.length(); j++)
                    if (isPal(s, i, j)) { cnt++; bestLen = Math.max(bestLen, j - i + 1); }
            String got = longestPalindrome(s);
            Check.eq(bestLen, got.length(), "longest " + s);
            Check.isTrue(s.contains(got) && isPal(got, 0, got.length() - 1), "longest valid " + s);
            Check.eq(cnt, countSubstrings(s), "count " + s);
            if (s.length() >= 2) Check.eq(bruteProduct(s), maxProduct(s), "product " + s);
            int[] rad = radii(s);
            for (int i = 0; i < s.length(); i++)
                for (int j = i; j < s.length(); j++)
                    Check.eq(isPal(s, i, j), isPalindrome(rad, i, j), "O(1) test " + s);
            int keep = 0;
            for (int k = s.length(); k > 0 && keep == 0; k--) if (isPal(s, 0, k - 1)) keep = k;
            Check.eq(new StringBuilder(s.substring(keep)).reverse() + s, shortestPalindrome(s), "LC 214 " + s);
        }
        // big: n = 1e5, linear time, and the count needs a long
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100000; i++) sb.append('a');
        String big = sb.toString();
        Check.eq(100000L * 100001 / 2, countSubstrings(big), "count all a, long");
        Check.eq(49999L * 50001, maxProduct(big), "product all a, long");
        Check.isTrue(49999L * 50001 > Integer.MAX_VALUE, "product passes int");
        Random rb = new Random(4);
        StringBuilder sr = new StringBuilder();
        for (int i = 0; i < 100000; i++) sr.append((char) ('a' + rb.nextInt(2)));
        String rnd = sr.toString();
        String lp = longestPalindrome(rnd);
        Check.isTrue(rnd.contains(lp) && isPal(lp, 0, lp.length() - 1), "big random longest valid");
        // LeetCode samples
        String g = longestPalindrome("babad");
        Check.isTrue(g.equals("bab") || g.equals("aba"), "LC 5 sample 1");
        Check.eq("bb", longestPalindrome("cbbd"), "LC 5 sample 2");
        Check.eq(3, countSubstrings("abc"), "LC 647 sample 1");
        Check.eq(6, countSubstrings("aaa"), "LC 647 sample 2");
        Check.eq(9, maxProduct("ababbb"), "LC 1960 sample 1");
        Check.eq(9, maxProduct("zaaaxbbby"), "LC 1960 sample 2");
        // traps
        Check.eq(new int[] {0, 1, 2, 3, 2, 1, 0}, radii("aaa"), "radii aaa");
        Check.eq(new int[] {0, 1, 0, 3, 0, 1, 0}, radii("aba"), "radii aba");
        Check.eq(new int[] {0, 1, 0, 3, 0, 1, 0, 7, 0, 1, 0, 5, 0, 1, 0, 1, 0}, radii("babcbabc"), "trace");
        Check.eq(14, countSubstrings("babcbabc"), "trace count");
        int[] bad = radiiNoMin("baba");
        long badCount = 0;
        for (int x : bad) badCount += (x + 1) / 2;
        Check.eq(3, bad[7], "no min: p[7]");
        Check.eq(7, badCount, "no min: count");
        Check.eq(6, countSubstrings("baba"), "baba count");
        Check.done("P11Manacher");
    }
}
