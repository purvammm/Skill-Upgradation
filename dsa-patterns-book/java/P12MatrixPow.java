import java.util.*;

public class P12MatrixPow {
    static final long MOD = 1_000_000_007L;

    //@tpl matpow
    static long[][] mul(long[][] a, long[][] b) {           // (n x k) * (k x m)
        int n = a.length, k = b.length, m = b[0].length;
        long[][] c = new long[n][m];
        for (int i = 0; i < n; i++)
            for (int t = 0; t < k; t++) {
                if (a[i][t] == 0) continue;                 // sparse rows are cheap
                for (int j = 0; j < m; j++)
                    c[i][j] = (c[i][j] + a[i][t] * b[t][j]) % MOD;
            }
        return c;
    }

    // M^e by squaring: the modPow loop with matrices. O(k^3 log e).
    static long[][] matPow(long[][] m, long e) {
        int k = m.length;
        long[][] r = new long[k][k];
        for (int i = 0; i < k; i++) r[i][i] = 1;            // identity
        while (e > 0) {
            if ((e & 1) == 1) r = mul(r, m);
            m = mul(m, m);
            e >>= 1;
        }
        return r;
    }
    //@end

    //@tpl recur
    // [F(n+1), F(n)] = M * [F(n), F(n-1)] with M = [[1, 1], [1, 0]],
    // so M^n = [[F(n+1), F(n)], [F(n), F(n-1)]].
    static long fib(long n) {
        return matPow(new long[][] {{1, 1}, {1, 0}}, n)[1][0];
    }

    // Tribonacci: state [T(i+2), T(i+1), T(i)]; T(0) = 0, T(1) = T(2) = 1.
    static long tribonacci(long n) {
        long[][] m = {{1, 1, 1}, {1, 0, 0}, {0, 1, 0}};
        long[][] p = matPow(m, n);
        return (p[2][0] + p[2][1]) % MOD;                   // M^n * [1, 1, 0]
    }
    //@end

    //@tpl walks
    // Strings of n symbols where symbol b may be followed by any a in next[b]:
    // T[a][b] = 1, and T^(n-1)[a][b] counts the strings from b to a.
    static long countStrings(int[][] next, long n) {
        int k = next.length;
        long[][] t = new long[k][k];
        for (int b = 0; b < k; b++) for (int a : next[b]) t[a][b] = 1;
        long[][] p = matPow(t, n - 1);
        long total = 0;
        for (long[] row : p) for (long x : row) total = (total + x) % MOD;
        return total;
    }

    // Knight dialer (LC 935): the digits a knight can jump to from each digit.
    static final int[][] JUMPS = {{4, 6}, {6, 8}, {7, 9}, {4, 8}, {0, 3, 9},
                                  {}, {0, 1, 7}, {2, 6}, {1, 3}, {2, 4}};
    // Count vowels permutation (LC 1220): a e i o u and the vowels allowed next.
    static final int[][] FOLLOW = {{1}, {0, 2}, {0, 1, 3, 4}, {2, 4}, {0}};

    static long knightDialer(int n)          { return countStrings(JUMPS, n); }
    static long countVowelPermutation(int n) { return countStrings(FOLLOW, n); }
    //@end

    // ---------------- tests (not printed) ----------------
    static long dpStrings(int[][] next, int n) {
        int k = next.length;
        long[] cur = new long[k];
        Arrays.fill(cur, 1);
        for (int s = 1; s < n; s++) {
            long[] nx = new long[k];
            for (int b = 0; b < k; b++) for (int a : next[b]) nx[a] = (nx[a] + cur[b]) % MOD;
            cur = nx;
        }
        long total = 0;
        for (long x : cur) total = (total + x) % MOD;
        return total;
    }

    static long[] fastDoubling(long n) {                     // {F(n), F(n+1)} mod MOD
        if (n == 0) return new long[] {0, 1};
        long[] h = fastDoubling(n >> 1);
        long a = h[0], b = h[1];
        long c = a * ((2 * b - a + MOD) % MOD) % MOD;
        long d = (a * a + b * b) % MOD;
        return (n & 1) == 0 ? new long[] {c, d} : new long[] {d, (c + d) % MOD};
    }

    public static void main(String[] args) {
        Random r = new Random(1204);
        long[] f = new long[5001], tr = new long[5001];
        f[1] = 1;
        tr[1] = tr[2] = 1;
        for (int i = 2; i <= 5000; i++) f[i] = (f[i - 1] + f[i - 2]) % MOD;
        for (int i = 3; i <= 5000; i++) tr[i] = (tr[i - 1] + tr[i - 2] + tr[i - 3]) % MOD;
        for (int i = 0; i <= 5000; i += (i < 100 ? 1 : 37)) {
            Check.eq(f[i], fib(i), "fib " + i);
            Check.eq(tr[i], tribonacci(i), "trib " + i);
        }
        for (int t = 0; t < 300; t++) {
            long n = Math.abs(r.nextLong() % 1_000_000_000_000_000_000L);
            Check.eq(fastDoubling(n)[0], fib(n), "fib big " + n);
        }
        for (int t = 0; t < 300; t++) {
            int k = 1 + r.nextInt(6);
            int[][] next = new int[k][];
            for (int b = 0; b < k; b++) {
                List<Integer> l = new ArrayList<>();
                for (int a = 0; a < k; a++) if (r.nextInt(3) > 0) l.add(a);
                next[b] = new int[l.size()];
                for (int i = 0; i < l.size(); i++) next[b][i] = l.get(i);
            }
            int n = 1 + r.nextInt(60);
            Check.eq(dpStrings(next, n), countStrings(next, n), "strings k=" + k + " n=" + n);
        }
        for (int n = 1; n <= 300; n++) {
            Check.eq(dpStrings(JUMPS, n), knightDialer(n), "knight " + n);
            Check.eq(dpStrings(FOLLOW, n), countVowelPermutation(n), "vowels " + n);
        }
        // matrix product against a plain triple loop on rectangular shapes
        for (int t = 0; t < 300; t++) {
            int n = 1 + r.nextInt(4), k = 1 + r.nextInt(4), m = 1 + r.nextInt(4);
            long[][] a = new long[n][k], b = new long[k][m];
            for (long[] row : a) for (int j = 0; j < k; j++) row[j] = r.nextInt(3) == 0 ? 0 : Math.abs(r.nextLong() % MOD);
            for (long[] row : b) for (int j = 0; j < m; j++) row[j] = Math.abs(r.nextLong() % MOD);
            long[][] c = mul(a, b);
            for (int i = 0; i < n; i++)
                for (int j = 0; j < m; j++) {
                    java.math.BigInteger s = java.math.BigInteger.ZERO;
                    for (int x = 0; x < k; x++)
                        s = s.add(java.math.BigInteger.valueOf(a[i][x]).multiply(java.math.BigInteger.valueOf(b[x][j])));
                    Check.eq(s.mod(java.math.BigInteger.valueOf(MOD)).longValue(), c[i][j], "mul");
                }
        }
        // trap quoted in the book: T(37) exceeds MOD
        long exact = 0, a0 = 0, a1 = 1, a2 = 1;
        for (int i = 3; i <= 37; i++) { long nx = a0 + a1 + a2; a0 = a1; a1 = a2; a2 = nx; }
        exact = a2;
        Check.eq(2082876103L, exact, "T(37) exact");
        Check.eq(exact % MOD, tribonacci(37), "T(37) mod");
        Check.eq(82876089L, tribonacci(37), "T(37) mod value");
        // trap: n transitions instead of n - 1
        long[][] tk = new long[10][10];
        for (int b = 0; b < 10; b++) for (int a : JUMPS[b]) tk[a][b] = 1;
        long wrong = 0;
        for (long[] row : matPow(tk, 1)) for (long x : row) wrong += x;
        Check.eq(20, wrong, "matPow(t, n) for n = 1");
        Check.eq(89, matPow(new long[][] {{1, 1}, {1, 0}}, 10)[0][0], "[0][0] is F(n+1)");
        // LeetCode samples
        Check.eq(1, fib(2), "LC 509 sample 1");
        Check.eq(2, fib(3), "LC 509 sample 2");
        Check.eq(3, fib(4), "LC 509 sample 3");
        Check.eq(4, tribonacci(4), "LC 1137 sample 1");
        Check.eq(1389537, tribonacci(25), "LC 1137 sample 2");
        Check.eq(10, knightDialer(1), "LC 935 sample 1");
        Check.eq(20, knightDialer(2), "LC 935 sample 2");
        Check.eq(136006598, knightDialer(3131), "LC 935 sample 3");
        Check.eq(5, countVowelPermutation(1), "LC 1220 sample 1");
        Check.eq(10, countVowelPermutation(2), "LC 1220 sample 2");
        Check.eq(68, countVowelPermutation(5), "LC 1220 sample 3");
        Check.done("P12MatrixPow");
    }
}
