import java.math.BigInteger;
import java.util.*;

public class P12ModPow {
    //@tpl pow
    static final long MOD = 1_000_000_007L;

    // base^exp mod m by squaring. m < 3e9, so every product fits in a long.
    static long modPow(long base, long exp, long m) {
        long result = 1 % m;
        base %= m;
        if (base < 0) base += m;
        while (exp > 0) {
            if ((exp & 1) == 1) result = result * base % m;   // this bit is 1
            base = base * base % m;                            // a^1, a^2, a^4, ...
            exp >>= 1;
        }
        return result;
    }

    // Inverse of a modulo a prime p, for a % p != 0 (Fermat: a^(p-1) == 1).
    static long modInverse(long a, long p) { return modPow(a, p - 2, p); }
    //@end

    //@tpl double
    // x^n for doubles. Negate n as a long: -Integer.MIN_VALUE overflows an int.
    static double myPow(double x, int n) {
        long e = n;
        if (e < 0) { x = 1 / x; e = -e; }
        double r = 1;
        while (e > 0) {
            if ((e & 1) == 1) r *= x;
            x *= x;
            e >>= 1;
        }
        return r;
    }
    //@end

    //@tpl uses
    // Count good numbers (LC 1922): 5 even digits at the (n + 1) / 2 even
    // indexes, 4 prime digits at the n / 2 odd ones.
    static long countGoodNumbers(long n) {
        return modPow(5, (n + 1) / 2, MOD) * modPow(4, n / 2, MOD) % MOD;
    }

    // Super pow (LC 372): a^b mod 1337, b given as decimal digits.
    // Reading one more digit d: a^(10k + d) = (a^k)^10 * a^d.
    static int superPow(int a, int[] b) {
        long r = 1;
        for (int d : b) r = modPow(r, 10, 1337) * modPow(a, d, 1337) % 1337;
        return (int) r;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static long bruteGood(int n) {
        long count = 0, total = 1;
        for (int i = 0; i < n; i++) total *= 10;
        for (long v = 0; v < total; v++) {
            long x = v;
            boolean ok = true;
            for (int i = 0; i < n; i++) {           // digit i from the left
                int d = (int) (x % 10);
                x /= 10;
                int idx = n - 1 - i;
                if (idx % 2 == 0 && d % 2 != 0) ok = false;
                if (idx % 2 == 1 && !(d == 2 || d == 3 || d == 5 || d == 7)) ok = false;
            }
            if (ok) count++;
        }
        return count;
    }

    public static void main(String[] args) {
        Random r = new Random(1207);
        BigInteger P = BigInteger.valueOf(MOD);
        for (int t = 0; t < 20000; t++) {
            long base = t % 3 == 0 ? r.nextInt(20) - 10 : r.nextLong() % 4_000_000_000_000L;
            long exp = t % 2 == 0 ? r.nextInt(50) : Math.abs(r.nextLong() % 1_000_000_000_000_000L);
            long m = t % 4 == 0 ? 1 + r.nextInt(100) : t % 4 == 1 ? MOD : 1 + (Math.abs(r.nextLong()) % 3_000_000_000L);
            long exp2 = BigInteger.valueOf(base).modPow(BigInteger.valueOf(exp), BigInteger.valueOf(m)).longValue();
            Check.eq(exp2, modPow(base, exp, m), "modPow " + base + " " + exp + " " + m);
            long a = 1 + Math.abs(r.nextLong() % (MOD - 1));
            long inv = modInverse(a, MOD);
            Check.eq(BigInteger.valueOf(a).modInverse(P).longValue(), inv, "inverse");
            Check.eq(1, a * inv % MOD, "a * inv");
            double x = (r.nextDouble() * 4 - 2);
            int n = r.nextInt(61) - 30;
            double want = Math.pow(x, n), got = myPow(x, n);
            Check.eq(want, got, 1e-9 * Math.max(1, Math.abs(want)), "myPow " + x + " " + n);
        }
        for (int n = 1; n <= 6; n++) Check.eq(bruteGood(n), countGoodNumbers(n), "good " + n);
        for (int t = 0; t < 200; t++) {
            long n = 1 + Math.abs(r.nextLong() % 1_000_000_000_000_000L);
            BigInteger w = BigInteger.valueOf(5).modPow(BigInteger.valueOf((n + 1) / 2), P)
                    .multiply(BigInteger.valueOf(4).modPow(BigInteger.valueOf(n / 2), P)).mod(P);
            Check.eq(w.longValue(), countGoodNumbers(n), "good big");
            int a = 1 + r.nextInt(Integer.MAX_VALUE);
            int len = 1 + r.nextInt(30);
            int[] b = new int[len];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                b[i] = i == 0 ? 1 + r.nextInt(9) : r.nextInt(10);
                sb.append(b[i]);
            }
            long sp = BigInteger.valueOf(a).modPow(new BigInteger(sb.toString()), BigInteger.valueOf(1337)).longValue();
            Check.eq(sp, superPow(a, b), "superPow");
        }
        // variant: inverses of 1..n in O(n)
        long[] invs = new long[100001];
        invs[1] = 1;
        for (int i = 2; i <= 100000; i++) invs[i] = (MOD - MOD / i) * invs[(int) (MOD % i)] % MOD;
        for (int i = 1; i <= 100000; i++) Check.eq(modInverse(i, MOD), invs[i], "linear inverse " + i);
        // edge cases of myPow
        Check.eq(0.0, myPow(2.0, Integer.MIN_VALUE), 0, "MIN_VALUE");
        Check.eq(1.0, myPow(-1.0, Integer.MIN_VALUE), 0, "-1 ^ MIN");
        Check.eq(1.0, myPow(1.0, Integer.MIN_VALUE), 0, "1 ^ MIN");
        // traps quoted in the book: int negation of MIN_VALUE
        int bad = Integer.MIN_VALUE;
        double xx = 2.0, rr = 1;
        if (bad < 0) { xx = 1 / xx; bad = -bad; }
        while (bad > 0) { if ((bad & 1) == 1) rr *= xx; xx *= xx; bad >>= 1; }
        Check.eq(1.0, rr, 0, "trap: int negation returns 1.0");
        Check.eq(0, modInverse(0, MOD), "inverse of 0 is silently 0");
        Check.isTrue((3 - 5) % 7 == -2, "negative remainder");
        // LeetCode samples
        Check.eq(1024.0, myPow(2.0, 10), 1e-9, "LC 50 sample 1");
        Check.eq(9.26100, myPow(2.1, 3), 1e-5, "LC 50 sample 2");
        Check.eq(0.25, myPow(2.0, -2), 1e-12, "LC 50 sample 3");
        Check.eq(5, countGoodNumbers(1), "LC 1922 sample 1");
        Check.eq(400, countGoodNumbers(4), "LC 1922 sample 2");
        Check.eq(564908303, countGoodNumbers(50), "LC 1922 sample 3");
        Check.eq(8, superPow(2, new int[] {3}), "LC 372 sample 1");
        Check.eq(1024, superPow(2, new int[] {1, 0}), "LC 372 sample 2");
        Check.eq(1, superPow(1, new int[] {4, 3, 3, 8, 5, 2}), "LC 372 sample 3");
        Check.eq(1198, superPow(2147483647, new int[] {2, 0, 0}), "LC 372 sample 4");
        Check.done("P12ModPow");
    }
}
