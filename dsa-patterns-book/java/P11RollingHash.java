import java.util.*;

public class P11RollingHash {
    //@tpl hash
    // Two polynomial hashes, mod two primes below 2^30, so every product of
    // two residues is below 2^60 and fits in a long. Chars are >= 1.
    static final long M1 = 1_000_000_007L, M2 = 1_000_000_009L, B = 131;

    static class Hasher {
        final long[] h1, h2, p1, p2;     // prefix hashes and powers of B

        Hasher(String s) {
            int n = s.length();
            h1 = new long[n + 1]; h2 = new long[n + 1];
            p1 = new long[n + 1]; p2 = new long[n + 1];
            p1[0] = p2[0] = 1;
            for (int i = 0; i < n; i++) {      // h[i+1] = h[i] * B + s[i]
                h1[i + 1] = (h1[i] * B + s.charAt(i)) % M1;
                h2[i + 1] = (h2[i] * B + s.charAt(i)) % M2;
                p1[i + 1] = p1[i] * B % M1;
                p2[i + 1] = p2[i] * B % M2;
            }
        }

        // Hash of s[l..r) in O(1): h[r] - h[l] * B^(r-l), both mods in one key.
        long get(int l, int r) {
            long a = ((h1[r] - h1[l] * p1[r - l]) % M1 + M1) % M1;
            long b = ((h2[r] - h2[l] * p2[r - l]) % M2 + M2) % M2;
            return a * M2 + b;               // < 2^63, and a different (a, b)
        }                                    // always gives a different key
    }
    //@end

    //@tpl longest
    // LC 1044: if some substring of length L occurs twice, so does its first
    // L - 1 chars. "A repeat of length L exists" is monotone: binary search L.
    static String longestDupSubstring(String s) {
        Hasher h = new Hasher(s);
        int lo = 0, hi = s.length() - 1, start = 0;    // answer length in [lo, hi]
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            int at = repeatAt(h, s.length(), mid);
            if (at >= 0) { lo = mid; start = at; }
            else hi = mid - 1;
        }
        return s.substring(start, start + lo);
    }

    // Start of the second copy of some length-len substring, or -1.
    static int repeatAt(Hasher h, int n, int len) {
        Set<Long> seen = new HashSet<>();
        for (int i = 0; i + len <= n; i++)
            if (!seen.add(h.get(i, i + len))) return i;
        return -1;
    }
    //@end

    // LC 187 (variant): 2 bits per letter, so a window of 10 letters is a 20-bit number.
    // This rolling code is exact: different windows never share a code.
    static List<String> findRepeatedDnaSequences(String s) {
        int[] code = new int[128];
        code['C'] = 1; code['G'] = 2; code['T'] = 3;
        Set<Integer> seen = new HashSet<>(), added = new HashSet<>();
        List<String> out = new ArrayList<>();
        for (int i = 0, x = 0; i < s.length(); i++) {
            x = ((x << 2) | code[s.charAt(i)]) & 0xFFFFF;   // drop the oldest letter
            if (i >= 9 && !seen.add(x) && added.add(x))     // second time seen
                out.add(s.substring(i - 9, i + 1));
        }
        return out;
    }

    //@tpl echo
    // LC 1316: count distinct a + a. For each half length, compare the two
    // halves in O(1) and collect the hashes of the first half.
    static int distinctEchoSubstrings(String text) {
        Hasher h = new Hasher(text);
        int n = text.length(), count = 0;
        for (int len = 1; 2 * len <= n; len++) {
            Set<Long> halves = new HashSet<>();
            for (int i = 0; i + 2 * len <= n; i++)
                if (h.get(i, i + len) == h.get(i + len, i + 2 * len))
                    halves.add(h.get(i, i + len));
            count += halves.size();
        }
        return count;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static String bruteLongestDup(String s) {
        String best = "";
        for (int i = 0; i < s.length(); i++)
            for (int j = i + 1; j < s.length(); j++) {
                int k = 0;
                while (j + k < s.length() && s.charAt(i + k) == s.charAt(j + k)) k++;
                if (k > best.length()) best = s.substring(i, i + k);
            }
        return best;
    }

    static boolean occursTwice(String s, String sub) {
        int i = s.indexOf(sub);
        return i >= 0 && s.indexOf(sub, i + 1) >= 0;
    }

    static List<String> bruteDna(String s) {
        Map<String, Integer> cnt = new LinkedHashMap<>();
        for (int i = 0; i + 10 <= s.length(); i++) cnt.merge(s.substring(i, i + 10), 1, Integer::sum);
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, Integer> e : cnt.entrySet()) if (e.getValue() > 1) out.add(e.getKey());
        return out;
    }

    static int bruteEcho(String t) {
        Set<String> s = new HashSet<>();
        for (int i = 0; i < t.length(); i++)
            for (int len = 1; i + 2 * len <= t.length(); len++)
                if (t.substring(i, i + len).equals(t.substring(i + len, i + 2 * len)))
                    s.add(t.substring(i, i + 2 * len));
        return s.size();
    }

    // Rabin-Karp (variant): hash p once, compare windows, confirm with regionMatches
    static int rabinKarp(String t, String p) {
        int m = p.length();
        if (m > t.length()) return -1;
        long hp = new Hasher(p).get(0, m);
        Hasher h = new Hasher(t);
        for (int i = 0; i + m <= t.length(); i++)
            if (h.get(i, i + m) == hp && t.regionMatches(i, p, 0, m)) return i;
        return -1;
    }

    public static void main(String[] args) {
        Random r = new Random(1104);
        for (int t = 0; t < 3000; t++) {
            String alpha = t % 3 == 0 ? "ab" : (t % 3 == 1 ? "abc" : "a");
            String s = Check.str(r, 1 + r.nextInt(16), alpha);
            // substring hash equality matches string equality on all pairs
            Hasher h = new Hasher(s);
            for (int k = 0; k < 30; k++) {
                int l1 = r.nextInt(s.length()), len = r.nextInt(s.length() - l1 + 1);
                int l2 = r.nextInt(s.length() - len + 1);
                boolean same = s.regionMatches(l1, s, l2, len);
                Check.eq(same, h.get(l1, l1 + len) == h.get(l2, l2 + len), "get " + s);
            }
            String got = longestDupSubstring(s), want = bruteLongestDup(s);
            Check.eq(want.length(), got.length(), "dup len " + s);
            Check.isTrue(got.isEmpty() || occursTwice(s, got), "dup valid " + s);
            Check.eq(bruteEcho(s), distinctEchoSubstrings(s), "echo " + s);
            String p = Check.str(r, 1 + r.nextInt(4), alpha);
            Check.eq(s.indexOf(p), rabinKarp(s, p), "rk " + s + " / " + p);
            String dna = Check.str(r, r.nextInt(40), t % 2 == 0 ? "AC" : "ACGT");
            Check.eq(Check.sortedCopy(bruteDna(dna)), Check.sortedCopy(findRepeatedDnaSequences(dna)), "dna " + dna);
        }
        // big: n = 3e4 random, compare the length with a suffix-array-free check
        StringBuilder sb = new StringBuilder();
        Random rb = new Random(9);
        for (int i = 0; i < 30000; i++) sb.append((char) ('a' + rb.nextInt(2)));
        String big = sb.toString();
        String d = longestDupSubstring(big);
        Check.isTrue(occursTwice(big, d), "big dup valid");
        Check.eq(-1, repeatAt(new Hasher(big), big.length(), d.length() + 1), "big dup longest");
        // the trap: one mod collides among 1e5 random windows, two mods do not
        Random rc = new Random(3);
        StringBuilder sc = new StringBuilder();
        for (int i = 0; i < 100009; i++) sc.append((char) ('a' + rc.nextInt(26)));
        String rnd = sc.toString();
        Hasher hc = new Hasher(rnd);
        Map<Long, String> one = new HashMap<>(), two = new HashMap<>();
        int c1 = 0, c2 = 0;
        for (int i = 0; i + 10 <= rnd.length(); i++) {
            String w = rnd.substring(i, i + 10);
            long key = hc.get(i, i + 10);
            String o1 = one.put(key / M2, w), o2 = two.put(key, w);
            if (o1 != null && !o1.equals(w)) c1++;
            if (o2 != null && !o2.equals(w)) c2++;
        }
        Check.eq(6, c1, "single mod collisions");
        Check.eq(0, c2, "double mod collisions");
        // LeetCode samples
        Check.eq("ana", longestDupSubstring("banana"), "LC 1044 sample 1");
        Check.eq("", longestDupSubstring("abcd"), "LC 1044 sample 2");
        Check.eq(Arrays.asList("AAAAACCCCC", "CCCCCAAAAA"),
                findRepeatedDnaSequences("AAAAACCCCCAAAAACCCCCCAAAAAGGGTTT"), "LC 187 sample 1");
        Check.eq(Arrays.asList("AAAAAAAAAA"), findRepeatedDnaSequences("AAAAAAAAAAAAA"), "LC 187 sample 2");
        Check.eq(3, distinctEchoSubstrings("abcabcabc"), "LC 1316 sample 1");
        Check.eq(2, distinctEchoSubstrings("leetcodeleetcode"), "LC 1316 sample 2");
        Check.done("P11RollingHash");
    }
}
