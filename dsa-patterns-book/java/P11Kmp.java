import java.util.*;

public class P11Kmp {
    //@tpl pi
    // pi[i] = length of the longest proper prefix of s[0..i] that is also a
    // suffix of s[0..i] (its longest "border").
    static int[] prefixFunction(String s) {
        int n = s.length();
        int[] pi = new int[n];
        for (int i = 1; i < n; i++) {
            int k = pi[i - 1];                        // longest border of s[0..i-1]
            while (k > 0 && s.charAt(i) != s.charAt(k))
                k = pi[k - 1];                        // next shorter border
            if (s.charAt(i) == s.charAt(k)) k++;      // extend it by s[i]
            pi[i] = k;
        }
        return pi;
    }
    //@end

    //@tpl search
    // First index of p in t, or -1. i never moves back; on a mismatch only k
    // (the number of chars of p matched so far) falls back through pi.
    static int strStr(String t, String p) {
        int m = p.length();
        if (m == 0) return 0;
        int[] pi = prefixFunction(p);
        for (int i = 0, k = 0; i < t.length(); i++) {
            while (k > 0 && t.charAt(i) != p.charAt(k)) k = pi[k - 1];
            if (t.charAt(i) == p.charAt(k)) k++;
            if (k == m) return i - m + 1;    // all matches: record, k = pi[k - 1]
        }
        return -1;
    }
    //@end

    //@tpl borders
    // LC 459: the shortest period of s is n - pi[n-1]. s is a block repeated
    // at least twice exactly when that period is shorter than n and divides n.
    static boolean repeatedSubstringPattern(String s) {
        int n = s.length(), period = n - prefixFunction(s)[n - 1];
        return period < n && n % period == 0;
    }

    // LC 1392: the longest happy prefix is the longest border of s.
    static String longestPrefix(String s) {
        return s.substring(0, prefixFunction(s)[s.length() - 1]);
    }

    // LC 214: keep the longest palindromic prefix of s, then put the reverse
    // of the rest in front. That prefix is the longest border of
    // s + '#' + reverse(s); the '#' stops a border from passing n.
    static String shortestPalindrome(String s) {
        String rev = new StringBuilder(s).reverse().toString();
        int[] pi = prefixFunction(s + "#" + rev);
        int keep = pi[pi.length - 1];                 // s[0..keep) is a palindrome
        return rev.substring(0, s.length() - keep) + s;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int[] brutePi(String s) {
        int[] pi = new int[s.length()];
        for (int i = 0; i < s.length(); i++)
            for (int k = i; k > 0; k--)
                if (s.substring(0, k).equals(s.substring(i - k + 1, i + 1))) { pi[i] = k; break; }
        return pi;
    }

    static boolean bruteRepeated(String s) {
        int n = s.length();
        for (int d = 1; d < n; d++) {
            if (n % d != 0) continue;
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n / d; j++) sb.append(s, 0, d);
            if (sb.toString().equals(s)) return true;
        }
        return false;
    }

    static String bruteHappy(String s) {
        for (int k = s.length() - 1; k > 0; k--)
            if (s.startsWith(s.substring(s.length() - k))) return s.substring(0, k);
        return "";
    }

    static boolean isPal(String s) {
        return new StringBuilder(s).reverse().toString().equals(s);
    }

    static String bruteShortestPal(String s) {
        for (int k = s.length(); k >= 0; k--)
            if (isPal(s.substring(0, k)))
                return new StringBuilder(s.substring(k)).reverse().toString() + s;
        return s;
    }

    // all matches (variant): keep going after a match
    static List<Integer> allMatches(String t, String p) {
        int[] pi = prefixFunction(p);
        List<Integer> out = new ArrayList<>();
        for (int i = 0, k = 0; i < t.length(); i++) {
            while (k > 0 && t.charAt(i) != p.charAt(k)) k = pi[k - 1];
            if (t.charAt(i) == p.charAt(k)) k++;
            if (k == p.length()) { out.add(i - k + 1); k = pi[k - 1]; }
        }
        return out;
    }

    // LC 1910 (variant): stack of kept chars plus the KMP state after each one
    static String removeOccurrences(String s, String part) {
        int m = part.length();
        int[] pi = prefixFunction(part);
        char[] st = new char[s.length()];
        int[] state = new int[s.length() + 1];
        int top = 0;
        for (char c : s.toCharArray()) {
            int k = state[top];
            while (k > 0 && c != part.charAt(k)) k = pi[k - 1];
            if (c == part.charAt(k)) k++;
            st[top++] = c;
            state[top] = k;
            if (k == m) top -= m;
        }
        return new String(st, 0, top);
    }

    static String bruteRemove(String s, String part) {
        int i;
        while ((i = s.indexOf(part)) >= 0) s = s.substring(0, i) + s.substring(i + part.length());
        return s;
    }

    // trap versions
    static int[] piIf(String s) {               // `if` instead of `while`
        int[] pi = new int[s.length()];
        for (int i = 1; i < s.length(); i++) {
            int k = pi[i - 1];
            if (k > 0 && s.charAt(i) != s.charAt(k)) k = pi[k - 1];
            if (s.charAt(i) == s.charAt(k)) k++;
            pi[i] = k;
        }
        return pi;
    }

    // trap: k = pi[k] instead of pi[k - 1]; returns null if it would never stop
    static int[] piWrongIndex(String s) {
        int[] pi = new int[s.length()];
        for (int i = 1; i < s.length(); i++) {
            int k = pi[i - 1], guard = 0;
            while (k > 0 && s.charAt(i) != s.charAt(k)) {
                k = pi[k];
                if (++guard > 1000) return null;
            }
            if (s.charAt(i) == s.charAt(k)) k++;
            pi[i] = k;
        }
        return pi;
    }

    // LC 796 (variant)
    static boolean rotateString(String s, String goal) {
        return s.length() == goal.length() && strStr(s + s, goal) >= 0;
    }

    public static void main(String[] args) {
        Random r = new Random(1102);
        for (int t = 0; t < 4000; t++) {
            String alpha = t % 3 == 0 ? "ab" : (t % 3 == 1 ? "abc" : "a");
            String s = Check.str(r, 1 + r.nextInt(14), alpha);
            Check.eq(brutePi(s), prefixFunction(s), "pi " + s);
            String txt = Check.str(r, r.nextInt(20), alpha);
            String p = Check.str(r, 1 + r.nextInt(4), alpha);
            Check.eq(txt.indexOf(p), strStr(txt, p), "strStr " + txt + " / " + p);
            List<Integer> all = new ArrayList<>();
            for (int i = 0; i + p.length() <= txt.length(); i++) if (txt.startsWith(p, i)) all.add(i);
            Check.eq(all, allMatches(txt, p), "all " + txt + " / " + p);
            Check.eq(bruteRepeated(s), repeatedSubstringPattern(s), "repeated " + s);
            Check.eq(bruteHappy(s), longestPrefix(s), "happy " + s);
            Check.eq(bruteShortestPal(s), shortestPalindrome(s), "shortest pal " + s);
            Check.eq(bruteRemove(txt, p), removeOccurrences(txt, p), "remove " + txt + " / " + p);
            boolean rot = false;
            for (int i = 0; i < Math.max(1, s.length()); i++) if ((s.substring(i) + s.substring(0, i)).equals(p)) rot = true;
            Check.eq(rot, rotateString(s, p), "rotate " + s + " / " + p);
        }
        // big: t = a^50000, p = a^999 b would be O(n m) for the naive restart
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50000; i++) sb.append('a');
        String big = sb.toString();
        Check.eq(-1, strStr(big, big.substring(0, 999) + "b"), "big no match");
        Check.eq(big.length() + 1, shortestPalindrome(big.substring(1) + "b").length(), "big shortest");
        // LeetCode samples
        Check.eq(0, strStr("sadbutsad", "sad"), "LC 28 sample 1");
        Check.eq(-1, strStr("leetcode", "leeto"), "LC 28 sample 2");
        Check.isTrue(repeatedSubstringPattern("abab"), "LC 459 sample 1");
        Check.isTrue(!repeatedSubstringPattern("aba"), "LC 459 sample 2");
        Check.isTrue(repeatedSubstringPattern("abcabcabcabc"), "LC 459 sample 3");
        Check.eq("l", longestPrefix("level"), "LC 1392 sample 1");
        Check.eq("abab", longestPrefix("ababab"), "LC 1392 sample 2");
        Check.eq("aaacecaaa", shortestPalindrome("aacecaaa"), "LC 214 sample 1");
        Check.eq("dcbabcd", shortestPalindrome("abcd"), "LC 214 sample 2");
        Check.eq("", shortestPalindrome(""), "LC 214 empty");
        Check.eq("dab", removeOccurrences("daabcbaabcbc", "abc"), "LC 1910 sample 1");
        Check.eq("ab", removeOccurrences("axxxxyyyyb", "xy"), "LC 1910 sample 2");
        // traps
        Check.eq(new int[] {0, 1, 2, 0}, prefixFunction("aaab"), "pi aaab");
        Check.eq(new int[] {0, 1, 2, 1}, piIf("aaab"), "if instead of while");
        boolean threw = false;
        try {
            String s = "aa", rev = "aa";
            int[] pi = prefixFunction(s + rev);
            rev.substring(0, s.length() - pi[pi.length - 1]);
        } catch (StringIndexOutOfBoundsException e) {
            threw = true;
        }
        Check.isTrue(threw, "no separator: border passes n");
        Check.isTrue(piWrongIndex("aab") == null, "k = pi[k] loops on aab");
        Check.done("P11Kmp");
    }
}
