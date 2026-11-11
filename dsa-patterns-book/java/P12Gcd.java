import java.util.*;

public class P12Gcd {
    //@tpl gcd
    // Euclid for a, b >= 0: gcd(a, b) = gcd(b, a % b), and gcd(a, 0) = a.
    static long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    static long lcm(long a, long b) { return a / gcd(a, b) * b; }   // divide first

    // Extended Euclid: returns {g, x, y} with a*x + b*y == g == gcd(a, b).
    static long[] extGcd(long a, long b) {
        if (b == 0) return new long[] {a, 1, 0};
        long[] r = extGcd(b, a % b);            // b*x1 + (a % b)*y1 == g
        long x = r[2], y = r[1] - (a / b) * r[2];
        return new long[] {r[0], x, y};
    }
    //@end

    //@tpl uses
    // GCD of an array (LC 1979 wants gcd(min, max)); gcd(0, v) == v starts the fold.
    static long gcdAll(int[] a) {
        long g = 0;
        for (int v : a) g = gcd(g, v);
        return g;
    }

    // GCD of strings: some string divides both iff s + t equals t + s.
    static String gcdOfStrings(String s, String t) {
        if (!(s + t).equals(t + s)) return "";
        return s.substring(0, (int) gcd(s.length(), t.length()));
    }

    // Deck of cards: groups of equal size X >= 2 exist iff gcd of the counts >= 2.
    static boolean hasGroupsSizeX(int[] deck) {
        Map<Integer, Integer> cnt = new HashMap<>();
        for (int c : deck) cnt.merge(c, 1, Integer::sum);
        long g = 0;
        for (int c : cnt.values()) g = gcd(g, c);
        return g >= 2;
    }

    // Water jugs (x, y >= 1): every reachable amount is a*x + b*y (Bezout),
    // so z works iff it fits in both jugs and gcd(x, y) divides it.
    static boolean canMeasureWater(int x, int y, int z) {
        return z <= (long) x + y && z % gcd(x, y) == 0;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static long bruteGcd(long a, long b) {
        long best = 0;
        for (long d = 1; d <= Math.max(a, b); d++) if (a % d == 0 && b % d == 0) best = d;
        return Math.max(a, b) == 0 ? 0 : best;
    }

    static long bruteLcm(long a, long b) {
        for (long m = Math.max(a, b); ; m++) if (m % a == 0 && m % b == 0) return m;
    }

    static String bruteGcdStrings(String s, String t) {
        String best = "";
        for (int len = 1; len <= Math.min(s.length(), t.length()); len++) {
            String d = s.substring(0, len);
            if (divides(d, s) && divides(d, t)) best = d;
        }
        return best;
    }

    static boolean divides(String d, String s) {
        if (s.length() % d.length() != 0) return false;
        StringBuilder sb = new StringBuilder();
        while (sb.length() < s.length()) sb.append(d);
        return sb.toString().equals(s);
    }

    static boolean bruteGroups(int[] deck) {
        Map<Integer, Integer> cnt = new HashMap<>();
        for (int c : deck) cnt.merge(c, 1, Integer::sum);
        for (int x = 2; x <= deck.length; x++) {
            boolean ok = true;
            for (int c : cnt.values()) if (c % x != 0) ok = false;
            if (ok) return true;
        }
        return false;
    }

    static boolean bruteJugs(int x, int y, int z) {
        boolean[][] seen = new boolean[x + 1][y + 1];
        Deque<int[]> q = new ArrayDeque<>();
        q.add(new int[] {0, 0});
        seen[0][0] = true;
        while (!q.isEmpty()) {
            int[] s = q.poll();
            int a = s[0], b = s[1];
            if (a + b == z || a == z || b == z) return true;
            int pourAB = Math.min(a, y - b), pourBA = Math.min(b, x - a);
            int[][] next = {{x, b}, {a, y}, {0, b}, {a, 0}, {a - pourAB, b + pourAB}, {a + pourBA, b - pourBA}};
            for (int[] nx : next)
                if (!seen[nx[0]][nx[1]]) { seen[nx[0]][nx[1]] = true; q.add(nx); }
        }
        return false;
    }

    public static void main(String[] args) {
        Random r = new Random(1209);
        for (int t = 0; t < 4000; t++) {
            long a = r.nextInt(300), b = r.nextInt(300);
            Check.eq(bruteGcd(a, b), gcd(a, b), "gcd " + a + " " + b);
            if (a > 0 && b > 0) Check.eq(bruteLcm(a, b), lcm(a, b), "lcm " + a + " " + b);
            long[] e = extGcd(a, b);
            Check.eq(gcd(a, b), e[0], "ext g");
            Check.eq(e[0], a * e[1] + b * e[2], "bezout " + a + " " + b);
            long A = Math.abs(r.nextLong() % 1_000_000_000_000L), B = Math.abs(r.nextLong() % 1_000_000_000_000L);
            long[] E = extGcd(A, B);
            Check.eq(java.math.BigInteger.valueOf(A).gcd(java.math.BigInteger.valueOf(B)).longValue(), gcd(A, B), "big gcd");
            Check.eq(java.math.BigInteger.valueOf(E[0]),
                    java.math.BigInteger.valueOf(A).multiply(java.math.BigInteger.valueOf(E[1]))
                            .add(java.math.BigInteger.valueOf(B).multiply(java.math.BigInteger.valueOf(E[2]))), "big bezout");
            // strings over a small alphabet, often built from a common root
            String root = Check.str(r, 1 + r.nextInt(3), "ab");
            String s = r.nextBoolean() ? rep(root, 1 + r.nextInt(4)) : Check.str(r, 1 + r.nextInt(8), "ab");
            String u = r.nextBoolean() ? rep(root, 1 + r.nextInt(4)) : Check.str(r, 1 + r.nextInt(8), "ab");
            Check.eq(bruteGcdStrings(s, u), gcdOfStrings(s, u), "gcdStrings " + s + " " + u);
            int[] deck = Check.ints(r, 1 + r.nextInt(12), 0, 3);
            Check.eq(bruteGroups(deck), hasGroupsSizeX(deck), "deck " + Arrays.toString(deck));
            int x = 1 + r.nextInt(9), y = 1 + r.nextInt(9), z = r.nextInt(20);
            Check.eq(bruteJugs(x, y, z), canMeasureWater(x, y, z), "jugs " + x + " " + y + " " + z);
            int[] arr = Check.ints(r, 1 + r.nextInt(6), 1, 60);
            long g = 0;
            for (int v : arr) g = bruteGcd(g, v);
            Check.eq(g, gcdAll(arr), "gcdAll");
        }
        // worst case: consecutive Fibonacci numbers
        Check.eq(1, gcd(1134903170L, 701408733L), "fib gcd");
        // traps quoted in the book
        int ia = 60000, ib = 90000;
        Check.eq(36834, ia * ib / (int) gcd(ia, ib), "int a*b/gcd overflows");
        Check.eq(180000, lcm(ia, ib), "lcm");
        Check.eq(-2, gcd(4, -6), "negative input");
        Check.isTrue(9 % gcd(3, 5) == 0, "z > x + y needs its own test");
        Check.eq(false, canMeasureWater(3, 5, 9), "z > x + y");
        long[] tr = extGcd(252, 105);
        Check.eq(new long[] {21, -2, 5}, tr, "trace");
        // LeetCode samples
        Check.eq("ABC", gcdOfStrings("ABCABC", "ABC"), "LC 1071 sample 1");
        Check.eq("AB", gcdOfStrings("ABABAB", "ABAB"), "LC 1071 sample 2");
        Check.eq("", gcdOfStrings("LEET", "CODE"), "LC 1071 sample 3");
        Check.eq(2, gcd(2, 10), "LC 1979 sample 1 (min 2, max 10)");
        Check.eq(1, gcd(3, 8), "LC 1979 sample 2 (min 3, max 8)");
        Check.eq(true, hasGroupsSizeX(new int[] {1, 2, 3, 4, 4, 3, 2, 1}), "LC 914 sample 1");
        Check.eq(false, hasGroupsSizeX(new int[] {1, 1, 1, 2, 2, 2, 3, 3}), "LC 914 sample 2");
        Check.eq(true, canMeasureWater(3, 5, 4), "LC 365 sample 1");
        Check.eq(false, canMeasureWater(2, 6, 5), "LC 365 sample 2");
        Check.eq(true, canMeasureWater(1, 2, 3), "LC 365 sample 3");
        Check.eq(1, gcdAll(new int[] {12, 5, 7, 23}), "LC 1250 sample 1 (good)");
        Check.eq(3, gcdAll(new int[] {3, 6}), "LC 1250 sample 3 (not good)");
        Check.done("P12Gcd");
    }

    static String rep(String s, int k) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < k; i++) sb.append(s);
        return sb.toString();
    }
}
