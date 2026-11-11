import java.util.*;

public class P11ExpandCenter {
    //@tpl longest
    // Longest palindromic substring. Centre c runs over 0..2n-2: an even c is
    // the char c/2 (odd lengths), an odd c is the gap after it (even lengths).
    static String longestPalindrome(String s) {
        int n = s.length(), bestL = 0, bestLen = 0;
        for (int c = 0; c < 2 * n - 1; c++) {
            int l = c / 2, r = l + c % 2;
            while (l >= 0 && r < n && s.charAt(l) == s.charAt(r)) {
                l--;
                r++;
            }
            // the loop stopped one step too far: s[l+1..r-1] is the palindrome
            if (r - l - 1 > bestLen) {
                bestL = l + 1;
                bestLen = r - l - 1;
            }
        }
        return s.substring(bestL, bestL + bestLen);
    }
    //@end

    //@tpl count
    // Palindromic substrings: every step of the inner loop finds one more
    // palindrome with this centre (the next one is two chars longer).
    static int countSubstrings(String s) {
        int n = s.length(), count = 0;
        for (int c = 0; c < 2 * n - 1; c++) {
            int l = c / 2, r = l + c % 2;
            while (l >= 0 && r < n && s.charAt(l) == s.charAt(r)) {
                count++;
                l--;
                r++;
            }
        }
        return count;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean isPal(String s, int i, int j) {     // s[i..j]
        while (i < j) if (s.charAt(i++) != s.charAt(j--)) return false;
        return true;
    }

    static int bruteLongestLen(String s) {
        int best = 0;
        for (int i = 0; i < s.length(); i++)
            for (int j = i; j < s.length(); j++)
                if (isPal(s, i, j)) best = Math.max(best, j - i + 1);
        return best;
    }

    static int bruteCount(String s) {
        int c = 0;
        for (int i = 0; i < s.length(); i++)
            for (int j = i; j < s.length(); j++)
                if (isPal(s, i, j)) c++;
        return c;
    }

    // the trap: odd centres only
    static String oddOnly(String s) {
        int n = s.length(), bestL = 0, bestLen = 0;
        for (int c = 0; c < n; c++) {
            int l = c, r = c;
            while (l >= 0 && r < n && s.charAt(l) == s.charAt(r)) { l--; r++; }
            if (r - l - 1 > bestLen) { bestL = l + 1; bestLen = r - l - 1; }
        }
        return s.substring(bestL, bestL + bestLen);
    }

    // LC 2472 (variant): shortest palindrome of length >= k at each centre,
    // then interval scheduling by right end
    static int maxPalindromes(String s, int k) {
        int n = s.length();
        List<int[]> iv = new ArrayList<>();
        for (int c = 0; c < 2 * n - 1; c++) {
            int l = c / 2, r = l + c % 2;
            while (l >= 0 && r < n && s.charAt(l) == s.charAt(r)) {
                if (r - l + 1 >= k) { iv.add(new int[] {l, r}); break; }
                l--; r++;
            }
        }
        iv.sort((a, b) -> Integer.compare(a[1], b[1]));
        int count = 0, lastEnd = -1;
        for (int[] x : iv) if (x[0] > lastEnd) { count++; lastEnd = x[1]; }
        return count;
    }

    static int bruteMaxPal(String s, int k) {
        int n = s.length();
        int[] dp = new int[n + 1];
        for (int i = 1; i <= n; i++) {
            dp[i] = dp[i - 1];
            for (int j = 0; j + k <= i; j++) if (isPal(s, j, i - 1)) dp[i] = Math.max(dp[i], dp[j] + 1);
        }
        return dp[n];
    }

    public static void main(String[] args) {
        Random r = new Random(1101);
        for (int t = 0; t < 4000; t++) {
            int n = r.nextInt(14);
            String s = Check.str(r, n, t % 2 == 0 ? "ab" : "abc");
            String got = longestPalindrome(s);
            Check.eq(bruteLongestLen(s), got.length(), "longest len " + s);
            Check.isTrue(s.contains(got) && isPal(got, 0, got.length() - 1), "longest valid " + s);
            Check.eq(bruteCount(s), countSubstrings(s), "count " + s);
            int k = 1 + r.nextInt(4);
            Check.eq(bruteMaxPal(s, k), maxPalindromes(s, k), "LC 2472 " + s + " k=" + k);
        }
        // large input runs fast enough: n = 1000, all 'a'
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) sb.append('a');
        Check.eq(1000, longestPalindrome(sb.toString()).length(), "all a");
        Check.eq(500500, countSubstrings(sb.toString()), "all a count");
        // LeetCode samples
        String g = longestPalindrome("babad");
        Check.isTrue(g.equals("bab") || g.equals("aba"), "LC 5 sample 1");
        Check.eq("bb", longestPalindrome("cbbd"), "LC 5 sample 2");
        Check.eq(3, countSubstrings("abc"), "LC 647 sample 1");
        Check.eq(6, countSubstrings("aaa"), "LC 647 sample 2");
        Check.eq(2, maxPalindromes("abaccdbbd", 3), "LC 2472 sample 1");
        Check.eq(0, maxPalindromes("adbcda", 2), "LC 2472 sample 2");
        // trap: odd centres only miss "bb"
        Check.eq("c", oddOnly("cbbd"), "odd centres only");
        // trap: bounds test after charAt throws
        boolean threw = false;
        try {
            String s = "aba";
            int l = 1, rr = 1;
            while (s.charAt(l) == s.charAt(rr) && l >= 0 && rr < s.length()) { l--; rr++; }
        } catch (StringIndexOutOfBoundsException e) {
            threw = true;
        }
        Check.isTrue(threw, "charAt before the bounds test throws");
        Check.done("P11ExpandCenter");
    }
}
