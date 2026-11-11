import java.util.*;

public class P02MinWindow {
    //@tpl counts
    // Shortest substring of s that contains every char of t, counts included (LC 76).
    static String minWindow(String s, String t) {
        int[] need = new int[128], have = new int[128];
        int required = 0;                     // distinct chars in t
        for (char c : t.toCharArray()) if (need[c]++ == 0) required++;
        int formed = 0;                       // chars c with have[c] >= need[c]
        int bestLen = Integer.MAX_VALUE, bestL = 0;
        for (int l = 0, r = 0; r < s.length(); r++) {
            char c = s.charAt(r);
            if (++have[c] == need[c]) formed++;       // 1. c is now covered
            while (formed == required) {              // 2. valid: record, then shrink
                if (r - l + 1 < bestLen) { bestLen = r - l + 1; bestL = l; }
                char d = s.charAt(l++);
                if (have[d]-- == need[d]) formed--;   // d drops below what t needs
            }
        }
        if (bestLen == Integer.MAX_VALUE) return "";
        return s.substring(bestL, bestL + bestLen);
    }
    //@end

    //@tpl missing
    // Same window with one array: cnt[c] = copies of c still needed (< 0 = spare).
    static String minWindowShort(String s, String t) {
        int[] cnt = new int[128];
        for (char c : t.toCharArray()) cnt[c]++;
        int missing = t.length(), bestL = 0, bestLen = Integer.MAX_VALUE;
        for (int l = 0, r = 0; r < s.length(); r++) {
            if (cnt[s.charAt(r)]-- > 0) missing--;       // a needed copy came in
            while (missing == 0) {
                if (r - l + 1 < bestLen) { bestLen = r - l + 1; bestL = l; }
                if (++cnt[s.charAt(l++)] > 0) missing++; // a needed copy went out
            }
        }
        if (bestLen == Integer.MAX_VALUE) return "";
        return s.substring(bestL, bestL + bestLen);
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean covers(String w, String t) {
        int[] c = new int[128];
        for (char ch : w.toCharArray()) c[ch]++;
        for (char ch : t.toCharArray()) if (--c[ch] < 0) return false;
        return true;
    }

    static String brute(String s, String t) {
        for (int len = 1; len <= s.length(); len++)          // shortest, then leftmost
            for (int i = 0; i + len <= s.length(); i++)
                if (covers(s.substring(i, i + len), t)) return s.substring(i, i + len);
        return "";
    }

    public static void main(String[] args) {
        Random r = new Random(33);
        for (int i = 0; i < 6000; i++) {
            String s = Check.str(r, r.nextInt(15), "ABCD");
            String t = Check.str(r, 1 + r.nextInt(4), "ABC");
            String want = brute(s, t);
            Check.eq(want, minWindow(s, t), "minWindow " + s + " / " + t);
            Check.eq(want, minWindowShort(s, t), "minWindowShort " + s + " / " + t);
        }
        Check.eq("BANC", minWindow("ADOBECODEBANC", "ABC"), "LC 76 sample 1");
        Check.eq("a", minWindow("a", "a"), "LC 76 sample 2");
        Check.eq("", minWindow("a", "aa"), "LC 76 sample 3");
        Check.eq("BANC", minWindowShort("ADOBECODEBANC", "ABC"), "LC 76 sample 1 short");
        Check.eq("a", minWindowShort("a", "a"), "LC 76 sample 2 short");
        Check.eq("", minWindowShort("a", "aa"), "LC 76 sample 3 short");
        Check.done("P02MinWindow");
    }
}
