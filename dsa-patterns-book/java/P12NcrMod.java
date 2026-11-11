import java.math.BigInteger;
import java.util.*;

public class P12NcrMod {
    static final long MOD = 1_000_000_007L;

    //@tpl tables
    // nCr mod a prime p for 0 <= r <= n <= max, after O(max) setup (max < p).
    static class Comb {
        final long[] fact, inv;                     // inv[i] = 1 / i! mod p
        Comb(int max) {
            fact = new long[max + 1];
            inv = new long[max + 1];
            fact[0] = 1;
            for (int i = 1; i <= max; i++) fact[i] = fact[i - 1] * i % MOD;
            inv[max] = modPow(fact[max], MOD - 2);  // the only Fermat inverse
            for (int i = max; i > 0; i--)
                inv[i - 1] = inv[i] * i % MOD;      // 1/(i-1)! = i * 1/i!
        }

        long nCr(int n, int r) {
            if (r < 0 || r > n) return 0;
            return fact[n] * inv[r] % MOD * inv[n - r] % MOD;
        }
    }
    //@end

    //@tpl pascal
    // Pascal: the last item is in or out. Exact in long up to n = 66.
    static long[][] pascal(int n) {
        long[][] c = new long[n + 1][n + 1];
        for (int i = 0; i <= n; i++) {
            c[i][0] = 1;
            for (int j = 1; j <= i; j++) c[i][j] = c[i - 1][j - 1] + c[i - 1][j];
        }
        return c;
    }

    // Unique paths in an m x n grid: pick which m - 1 of the m + n - 2 moves
    // go down. After step i, r = C(n - 1 + i, i), so each division is exact.
    static long uniquePaths(int m, int n) {
        long r = 1;
        for (int i = 1; i <= m - 1; i++) r = r * (n - 1 + i) / i;
        return r;
    }
    //@end

    //@tpl uses
    // Count anagrams (LC 2514): a word of length L with letter counts c1, c2, ...
    // has L! / (c1! c2! ...) orders; multiply over the words.
    static long countAnagrams(String s, Comb cb) {
        long ans = 1;
        for (String w : s.split(" ")) {
            int[] cnt = new int[26];
            for (char ch : w.toCharArray()) cnt[ch - 'a']++;
            ans = ans * cb.fact[w.length()] % MOD;
            for (int c : cnt) ans = ans * cb.inv[c] % MOD;
        }
        return ans;
    }

    // Exactly k unit steps from start to end (LC 2400): (k + d) / 2 of them go
    // toward end, so choose which ones. Needs k - d >= 0 and even.
    static long numberOfWays(int start, int end, int k, Comb cb) {
        int d = Math.abs(end - start);
        if (d > k || (k - d) % 2 != 0) return 0;
        return cb.nCr(k, (k + d) / 2);
    }
    //@end

    static long modPow(long b, long e) {
        long r = 1;
        b %= MOD;
        while (e > 0) {
            if ((e & 1) == 1) r = r * b % MOD;
            b = b * b % MOD;
            e >>= 1;
        }
        return r;
    }

    // ---------------- tests (not printed) ----------------
    static BigInteger bigC(int n, int r) {
        if (r < 0 || r > n) return BigInteger.ZERO;
        BigInteger x = BigInteger.ONE;
        for (int i = 1; i <= r; i++)
            x = x.multiply(BigInteger.valueOf(n - r + i)).divide(BigInteger.valueOf(i));
        return x;
    }

    static long bruteAnagrams(String s) {
        long ans = 1;
        for (String w : s.split(" ")) {
            Set<String> seen = new HashSet<>();
            permute(w.toCharArray(), 0, seen);
            ans *= seen.size();
        }
        return ans % MOD;
    }

    static void permute(char[] a, int i, Set<String> seen) {
        if (i == a.length) { seen.add(new String(a)); return; }
        for (int j = i; j < a.length; j++) {
            char t = a[i]; a[i] = a[j]; a[j] = t;
            permute(a, i + 1, seen);
            t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }

    static long bruteWays(int start, int end, int k) {
        Map<Integer, Long> cur = new HashMap<>();
        cur.put(start, 1L);
        for (int s = 0; s < k; s++) {
            Map<Integer, Long> nx = new HashMap<>();
            for (Map.Entry<Integer, Long> e : cur.entrySet()) {
                nx.merge(e.getKey() - 1, e.getValue(), (a, b) -> (a + b) % MOD);
                nx.merge(e.getKey() + 1, e.getValue(), (a, b) -> (a + b) % MOD);
            }
            cur = nx;
        }
        return cur.getOrDefault(end, 0L);
    }

    static long bruteGrid(int m, int n) {
        long[][] dp = new long[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                dp[i][j] = (i == 0 || j == 0) ? 1 : dp[i - 1][j] + dp[i][j - 1];
        return dp[m - 1][n - 1];
    }

    public static void main(String[] args) {
        Random r = new Random(1205);
        Comb cb = new Comb(2000);
        BigInteger P = BigInteger.valueOf(MOD);
        for (int i = 0; i <= 2000; i++) Check.eq(1, cb.fact[i] * cb.inv[i] % MOD, "fact * inv " + i);
        for (int t = 0; t < 3000; t++) {
            int n = r.nextInt(2001), k = r.nextInt(n + 3) - 1;
            Check.eq(bigC(n, k).mod(P).longValue(), cb.nCr(n, k), "nCr " + n + " " + k);
        }
        long[][] pc = pascal(66);
        for (int n = 0; n <= 66; n++)
            for (int k = 0; k <= n; k++) Check.eq(bigC(n, k).longValue(), pc[n][k], "pascal " + n + " " + k);
        Check.isTrue(bigC(66, 33).bitLength() < 64 && bigC(67, 33).bitLength() >= 64, "66 is the limit");
        Check.isTrue(pascal(67)[67][33] < 0, "pascal 67 wraps negative");
        for (int m = 1; m <= 17; m++)
            for (int n = 1; n <= 17; n++) Check.eq(bruteGrid(m, n), uniquePaths(m, n), "paths " + m + " " + n);
        for (int m = 1; m <= 100; m++)
            for (int n = 1; n <= 100; n++) {
                BigInteger w = bigC(m + n - 2, m - 1);
                if (w.compareTo(BigInteger.valueOf(2_000_000_000L)) <= 0)
                    Check.eq(w.longValue(), uniquePaths(m, n), "paths big " + m + " " + n);
            }
        for (int t = 0; t < 600; t++) {
            int words = 1 + r.nextInt(3);
            StringBuilder sb = new StringBuilder();
            for (int w = 0; w < words; w++) {
                if (w > 0) sb.append(' ');
                sb.append(Check.str(r, 1 + r.nextInt(6), "abc"));
            }
            Check.eq(bruteAnagrams(sb.toString()), countAnagrams(sb.toString(), cb), "anagrams " + sb);
            int st = r.nextInt(20), en = r.nextInt(20), k = 1 + r.nextInt(25);
            Check.eq(bruteWays(st, en, k), numberOfWays(st, en, k, cb), "ways " + st + " " + en + " " + k);
        }
        // traps quoted in the book
        long naive = cb.fact[20] / (cb.fact[10] * cb.fact[10] % MOD) % MOD;
        Check.isTrue(naive != 184756, "dividing residues is wrong");
        Check.eq(0, numberOfWays(0, 2, 3, cb), "parity");
        Check.eq(3, cb.nCr(3, (3 + 2) / 2), "without parity check");
        // LeetCode samples
        Check.eq(28, uniquePaths(3, 7), "LC 62 sample 1");
        Check.eq(3, uniquePaths(3, 2), "LC 62 sample 2");
        Comb big = new Comb(100000);
        Check.eq(18, countAnagrams("too hot", big), "LC 2514 sample 1");
        Check.eq(1, countAnagrams("aa", big), "LC 2514 sample 2");
        Check.eq(3, numberOfWays(1, 2, 3, cb), "LC 2400 sample 1");
        Check.eq(0, numberOfWays(2, 5, 10, cb), "LC 2400 sample 2");
        Check.eq(new long[] {1, 4, 6, 4, 1}, Arrays.copyOf(pascal(4)[4], 5), "LC 118 row 4");
        Check.done("P12NcrMod");
    }
}
