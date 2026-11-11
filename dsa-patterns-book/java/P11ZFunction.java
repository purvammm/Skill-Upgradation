import java.util.*;

public class P11ZFunction {
    //@tpl z
    // z[i] = length of the longest common prefix of s and s[i..]; z[0] = n.
    // [l, r) is the match s[l..r) == s[0..r-l) that reaches furthest right.
    static int[] zFunction(String s) {
        int n = s.length();
        int[] z = new int[n];
        if (n > 0) z[0] = n;
        for (int i = 1, l = 0, r = 0; i < n; i++) {
            if (i < r) z[i] = Math.min(r - i, z[i - l]);   // copy from the box
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i]))
                z[i]++;                                   // compare past r
            if (i + z[i] > r) {                           // box moves right
                l = i;
                r = i + z[i];
            }
        }
        return z;
    }
    //@end

    //@tpl search
    // Every start of p in t. '$' is in neither string, so no z-value of
    // p + '$' + t can pass m, and z[i] == m marks a match.
    static List<Integer> occurrences(String t, String p) {
        int m = p.length();
        int[] z = zFunction(p + "$" + t);
        List<Integer> out = new ArrayList<>();
        for (int i = m + 1; i < z.length; i++)
            if (z[i] == m) out.add(i - m - 1);
        return out;
    }
    //@end

    //@tpl uses
    // LC 2223: the score of the suffix s[i..] is z[i]. The sum reaches
    // n(n+1)/2, about 5e9 for n = 1e5, so add in a long.
    static long sumScores(String s) {
        long sum = 0;
        for (int v : zFunction(s)) sum += v;
        return sum;
    }

    // LC 3029 / 3031: after t seconds the word starts with word[t*k..] and
    // the rest is free. It is back once that suffix is a prefix of word
    // (z[t*k] covers it), or once nothing of the old word is left.
    static int minimumTimeToInitialState(String word, int k) {
        int n = word.length();
        int[] z = zFunction(word);
        int t = 1;
        while (t * k < n && z[t * k] < n - t * k) t++;
        return t;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int[] bruteZ(String s) {
        int n = s.length();
        int[] z = new int[n];
        for (int i = 0; i < n; i++) while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) z[i]++;
        return z;
    }

    // BFS over all words of length n on alphabet {a, b}: remove k from the
    // front, add any k letters at the back; fewest seconds (> 0) back to word.
    static int bruteRevert(String word, int k) {
        int n = word.length();
        int start = 0;
        for (int i = 0; i < n; i++) if (word.charAt(i) == 'b') start |= 1 << i;
        int[] dist = new int[1 << n];
        Arrays.fill(dist, -1);
        Deque<Integer> q = new ArrayDeque<>();
        // step 1 from start
        for (int add = 0; add < (1 << k); add++) {
            int nx = next(start, add, n, k);
            if (dist[nx] < 0) { dist[nx] = 1; q.add(nx); }
        }
        while (!q.isEmpty()) {
            int x = q.poll();
            if (x == start) return dist[x];
            for (int add = 0; add < (1 << k); add++) {
                int nx = next(x, add, n, k);
                if (dist[nx] < 0) { dist[nx] = dist[x] + 1; q.add(nx); }
            }
        }
        return -1;
    }

    static int next(int x, int add, int n, int k) {
        if (k >= n) return add & ((1 << n) - 1);
        return (x >>> k) | (add << (n - k)) & ((1 << n) - 1);
    }

    // trap versions
    static int[] zNoMin(String s) {
        int n = s.length();
        int[] z = new int[n];
        if (n > 0) z[0] = n;
        for (int i = 1, l = 0, r = 0; i < n; i++) {
            if (i < r) z[i] = z[i - l];
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) z[i]++;
            if (i + z[i] > r) { l = i; r = i + z[i]; }
        }
        return z;
    }

    // LC 3303 (variant): first start where p matches with at most one change
    static int minStartingIndex(String s, String p) {
        int n = s.length(), m = p.length();
        int[] pre = zFunction(p + "$" + s);
        String rs = new StringBuilder(s).reverse().toString(), rp = new StringBuilder(p).reverse().toString();
        int[] suf = zFunction(rp + "$" + rs);
        for (int i = 0; i + m <= n; i++) {
            int a = pre[m + 1 + i];
            int end = i + m - 1;                       // last index of the window in s
            int b = suf[m + 1 + (n - 1 - end)];
            if (a + b >= m - 1) return i;
        }
        return -1;
    }

    static int bruteMinStart(String s, String p) {
        for (int i = 0; i + p.length() <= s.length(); i++) {
            int diff = 0;
            for (int j = 0; j < p.length(); j++) if (s.charAt(i + j) != p.charAt(j)) diff++;
            if (diff <= 1) return i;
        }
        return -1;
    }

    // longest happy prefix with Z (variant): first i with i + z[i] == n
    static String happyByZ(String s) {
        int[] z = zFunction(s);
        for (int i = 1; i < s.length(); i++) if (i + z[i] == s.length()) return s.substring(i);
        return "";
    }

    public static void main(String[] args) {
        Random r = new Random(1103);
        for (int t = 0; t < 4000; t++) {
            String alpha = t % 3 == 0 ? "ab" : (t % 3 == 1 ? "abc" : "a");
            String s = Check.str(r, r.nextInt(16), alpha);
            int[] bz = bruteZ(s);
            Check.eq(bz, zFunction(s), "z " + s);
            long sum = 0;
            for (int v : bz) sum += v;
            Check.eq(sum, sumScores(s), "sum " + s);
            String happy = "";
            for (int k = s.length() - 1; k > 0 && happy.isEmpty(); k--)
                if (s.startsWith(s.substring(s.length() - k))) happy = s.substring(0, k);
            Check.eq(happy, happyByZ(s), "happy by z " + s);
            String txt = Check.str(r, r.nextInt(20), alpha);
            String p = Check.str(r, 1 + r.nextInt(4), alpha);
            List<Integer> all = new ArrayList<>();
            for (int i = 0; i + p.length() <= txt.length(); i++) if (txt.startsWith(p, i)) all.add(i);
            Check.eq(all, occurrences(txt, p), "occ " + txt + " / " + p);
            Check.eq(bruteMinStart(txt, p), minStartingIndex(txt, p), "almost " + txt + " / " + p);
        }
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(8), k = 1 + r.nextInt(n);
            String w = Check.str(r, n, "ab");
            Check.eq(bruteRevert(w, k), minimumTimeToInitialState(w, k), "revert " + w + " k=" + k);
        }
        // big inputs: linear time
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100000; i++) sb.append('a');
        Check.eq(100000L * 100001 / 2, sumScores(sb.toString()), "sum all a, long");
        Check.eq(1, minimumTimeToInitialState(sb.toString(), 3), "revert all a");
        // LeetCode samples
        Check.eq(9, sumScores("babab"), "LC 2223 sample 1");
        Check.eq(14, sumScores("azbazbzaz"), "LC 2223 sample 2");
        Check.eq(2, minimumTimeToInitialState("abacaba", 3), "LC 3029 sample 1");
        Check.eq(1, minimumTimeToInitialState("abacaba", 4), "LC 3029 sample 2");
        Check.eq(4, minimumTimeToInitialState("abcbabcd", 2), "LC 3029 sample 3");
        Check.eq(1, minStartingIndex("abcdefg", "bcdffg"), "LC 3303 sample 1");
        Check.eq(4, minStartingIndex("ababbababa", "bacaba"), "LC 3303 sample 2");
        Check.eq(-1, minStartingIndex("abcd", "dba"), "LC 3303 sample 3");
        Check.eq(0, minStartingIndex("dde", "d"), "LC 3303 sample 4");
        // traps
        Check.eq(new int[] {5, 4, 3, 2, 1}, zFunction("aaaaa"), "no separator: p + t = aa + aaa");
        int z0 = 0;
        int[] zb = zFunction("babab");
        for (int i = 1; i < zb.length; i++) z0 += zb[i];
        Check.eq(4, z0, "sum without z[0]");
        Check.done("P11ZFunction");
    }
}
