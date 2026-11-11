import java.util.*;

public class P02FixedWindow {
    //@tpl sum
    // Max sum of k consecutive elements (LC 643 returns this / k as a double).
    static long maxSumOfK(int[] a, int k) {
        long sum = 0, best = Long.MIN_VALUE;
        for (int r = 0; r < a.length; r++) {
            sum += a[r];                                 // 1. add the right end
            if (r >= k) sum -= a[r - k];                 // 2. drop the one that left
            if (r >= k - 1) best = Math.max(best, sum);  // 3. window is full
        }
        return best;
    }
    //@end

    //@tpl anagrams
    // Start of every anagram of p in s (LC 438). Lowercase letters only.
    static List<Integer> findAnagrams(String s, String p) {
        List<Integer> out = new ArrayList<>();
        int k = p.length();
        int[] need = new int[26], have = new int[26];
        for (char c : p.toCharArray()) need[c - 'a']++;
        int match = 0;                       // letters c with have[c] == need[c]
        for (int c = 0; c < 26; c++) if (need[c] == 0) match++;
        for (int r = 0; r < s.length(); r++) {
            match += change(have, need, s.charAt(r) - 'a', +1);
            if (r >= k) match += change(have, need, s.charAt(r - k) - 'a', -1);
            if (r >= k - 1 && match == 26) out.add(r - k + 1);
        }
        return out;
    }

    // have[c] += d; returns how the number of matching letters changed (-1, 0, +1).
    static int change(int[] have, int[] need, int c, int d) {
        int before = have[c] == need[c] ? 1 : 0;
        have[c] += d;
        return (have[c] == need[c] ? 1 : 0) - before;
    }
    //@end

    //@tpl vowels
    // Most vowels in any substring of length k (LC 1456).
    static int maxVowels(String s, int k) {
        int cnt = 0, best = 0;
        for (int r = 0; r < s.length(); r++) {
            if (isVowel(s.charAt(r))) cnt++;
            if (r >= k && isVowel(s.charAt(r - k))) cnt--;
            if (r >= k - 1) best = Math.max(best, cnt);
        }
        return best;
    }

    static boolean isVowel(char c) { return "aeiou".indexOf(c) >= 0; }
    //@end

    // ---------------- tests (not printed) ----------------
    static long bruteMaxSum(int[] a, int k) {
        long best = Long.MIN_VALUE;
        for (int i = 0; i + k <= a.length; i++) {
            long s = 0;
            for (int j = i; j < i + k; j++) s += a[j];
            best = Math.max(best, s);
        }
        return best;
    }

    static List<Integer> bruteAnagrams(String s, String p) {
        List<Integer> out = new ArrayList<>();
        char[] want = p.toCharArray();
        Arrays.sort(want);
        for (int i = 0; i + p.length() <= s.length(); i++) {
            char[] w = s.substring(i, i + p.length()).toCharArray();
            Arrays.sort(w);
            if (Arrays.equals(want, w)) out.add(i);
        }
        return out;
    }

    static int bruteVowels(String s, int k) {
        int best = 0;
        for (int i = 0; i + k <= s.length(); i++) {
            int c = 0;
            for (int j = i; j < i + k; j++) if (isVowel(s.charAt(j))) c++;
            best = Math.max(best, c);
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(29);
        for (int t = 0; t < 5000; t++) {
            int n = 1 + r.nextInt(14);
            int[] a = Check.ints(r, n, -10, 10);
            int k = 1 + r.nextInt(n);
            Check.eq(bruteMaxSum(a, k), maxSumOfK(a, k), "sum " + Arrays.toString(a) + " k=" + k);
            String s = Check.str(r, r.nextInt(16), "abc");
            String p = Check.str(r, 1 + r.nextInt(4), "abc");
            Check.eq(bruteAnagrams(s, p).toString(), findAnagrams(s, p).toString(), "anagrams " + s + " " + p);
            String v = Check.str(r, 1 + r.nextInt(15), "abeiz");
            int kv = 1 + r.nextInt(v.length());
            Check.eq(bruteVowels(v, kv), maxVowels(v, kv), "vowels " + v + " k=" + kv);
        }
        Check.eq(12.75, maxSumOfK(new int[] {1, 12, -5, -6, 50, 3}, 4) / 4.0, 1e-9, "LC 643 sample 1");
        Check.eq(5.0, maxSumOfK(new int[] {5}, 1) / 1.0, 1e-9, "LC 643 sample 2");
        Check.eq("[0, 6]", findAnagrams("cbaebabacd", "abc").toString(), "LC 438 sample 1");
        Check.eq("[0, 1, 2]", findAnagrams("abab", "ab").toString(), "LC 438 sample 2");
        Check.eq(true, !findAnagrams("eidbaooo", "ab").isEmpty(), "LC 567 sample 1");
        Check.eq(false, !findAnagrams("eidboaoo", "ab").isEmpty(), "LC 567 sample 2");
        Check.eq(3, maxVowels("abciiidef", 3), "LC 1456 sample 1");
        Check.eq(2, maxVowels("aeiou", 2), "LC 1456 sample 2");
        Check.eq(2, maxVowels("leetcode", 3), "LC 1456 sample 3");
        Check.done("P02FixedWindow");
    }
}
