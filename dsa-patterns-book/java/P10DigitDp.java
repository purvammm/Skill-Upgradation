import java.util.*;

public class P10DigitDp {
    //@tpl distinct
    // Numbers with repeated digits (LC 1012) = n - (numbers in [1, n] whose
    // digits are all different). go() places the digits left to right.
    static char[] d;                                   // the digits of n
    static Integer[][][][] memo;                       // [pos][tight][started][mask]

    static int numDupDigitsAtMostN(int n) {
        d = String.valueOf(n).toCharArray();
        memo = new Integer[d.length][2][2][1 << 10];
        return n - go(0, 1, 0, 0);
    }

    // tight = 1: the prefix equals n's prefix, so this digit is at most d[pos].
    // started = 0: only leading zeros so far; they are not digits of the number.
    static int go(int pos, int tight, int started, int mask) {
        if (pos == d.length) return started;           // built one number (not 0)
        Integer m = memo[pos][tight][started][mask];
        if (m != null) return m;
        int lim = tight == 1 ? d[pos] - '0' : 9, res = 0;
        for (int dig = 0; dig <= lim; dig++) {
            int nt = tight == 1 && dig == lim ? 1 : 0;
            if (started == 0 && dig == 0)
                res += go(pos + 1, nt, 0, mask);           // still a leading zero
            else if ((mask >> dig & 1) == 0)
                res += go(pos + 1, nt, 1, mask | 1 << dig); // dig is new: use it
        }
        return memo[pos][tight][started][mask] = res;
    }
    //@end

    //@tpl range
    // Count of integers (LC 2719): x in [num1, num2], strings up to 10^22,
    // digit sum in [minSum, maxSum], mod 1e9+7.
    static final int MOD = 1_000_000_007;
    static String bound;                               // count x in [0, bound]
    static int minSum, maxSum;
    static long[][][] ways;                            // [pos][tight][sum]; -1 = new

    static int count(String num1, String num2, int min_sum, int max_sum) {
        minSum = min_sum;
        maxSum = max_sum;
        long r = upTo(num2) - upTo(minusOne(num1));    // f(hi) - f(lo - 1)
        return (int) ((r % MOD + MOD) % MOD);
    }

    static long upTo(String n) {
        bound = n;
        ways = new long[n.length()][2][maxSum + 1];
        for (long[][] a : ways) for (long[] b : a) Arrays.fill(b, -1);
        return walk(0, 1, 0);
    }

    // Leading zeros add 0 to the sum, so this rule needs no 'started' flag.
    static long walk(int pos, int tight, int sum) {
        if (sum > maxSum) return 0;                    // the sum only grows
        if (pos == bound.length()) return sum >= minSum ? 1 : 0;
        if (ways[pos][tight][sum] >= 0) return ways[pos][tight][sum];
        int lim = tight == 1 ? bound.charAt(pos) - '0' : 9;
        long res = 0;
        for (int dig = 0; dig <= lim; dig++)
            res += walk(pos + 1, tight == 1 && dig == lim ? 1 : 0, sum + dig);
        return ways[pos][tight][sum] = res % MOD;
    }

    static String minusOne(String x) {                 // "1000" -> "0999"
        char[] c = x.toCharArray();
        int i = c.length - 1;
        while (c[i] == '0') c[i--] = '9';
        c[i]--;
        return new String(c);
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean distinctDigits(int x) {
        int seen = 0;
        for (; x > 0; x /= 10) {
            int b = 1 << (x % 10);
            if ((seen & b) != 0) return false;
            seen |= b;
        }
        return true;
    }

    static int digitSum(long x) {
        int s = 0;
        for (; x > 0; x /= 10) s += x % 10;
        return s;
    }

    // trap versions
    static int goNoStarted(int pos, int tight, int mask) {
        if (pos == d.length) return 1;
        int lim = tight == 1 ? d[pos] - '0' : 9, res = 0;
        for (int dig = 0; dig <= lim; dig++)
            if ((mask >> dig & 1) == 0) res += goNoStarted(pos + 1, tight == 1 && dig == lim ? 1 : 0, mask | 1 << dig);
        return res;
    }

    static Integer[][][] memoNoTight;
    static int goNoTightMemo(int pos, int tight, int started, int mask) {
        if (pos == d.length) return started;
        Integer m = memoNoTight[pos][started][mask];
        if (m != null) return m;
        int lim = tight == 1 ? d[pos] - '0' : 9, res = 0;
        for (int dig = 0; dig <= lim; dig++) {
            int nt = tight == 1 && dig == lim ? 1 : 0;
            if (started == 0 && dig == 0) res += goNoTightMemo(pos + 1, nt, 0, mask);
            else if ((mask >> dig & 1) == 0) res += goNoTightMemo(pos + 1, nt, 1, mask | 1 << dig);
        }
        return memoNoTight[pos][started][mask] = res;
    }

    static String setStr(int mask) {
        StringBuilder sb = new StringBuilder("{");
        for (int b = 0; b < 10; b++) if ((mask >> b & 1) == 1) sb.append(sb.length() > 1 ? "," : "").append(b);
        return sb.append("}").toString();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            int n = 235;
            numDupDigitsAtMostN(n);
            // walk the tight path and print each branch
            int pos = 0, tight = 1, started = 0, mask = 0;
            while (pos < d.length && tight == 1) {
                int lim = d[pos] - '0';
                for (int dig = 0; dig <= lim; dig++) {
                    int nt = dig == lim ? 1 : 0;
                    String what;
                    if (started == 0 && dig == 0) what = "go(" + (pos + 1) + ", " + (nt == 1 ? "tight" : "free") + ", not started) = " + go(pos + 1, nt, 0, mask);
                    else if ((mask >> dig & 1) == 0) what = "go(" + (pos + 1) + ", " + (nt == 1 ? "tight" : "free") + ", " + setStr(mask | 1 << dig) + ") = " + go(pos + 1, nt, 1, mask | 1 << dig);
                    else what = "repeat, skipped";
                    System.out.println("pos " + pos + " mask " + setStr(mask) + " digit " + dig + ": " + what);
                }
                int dig = lim;
                if (!(started == 0 && dig == 0)) { mask |= 1 << dig; started = 1; }
                pos++;
            }
            System.out.println("distinct = " + go(0, 1, 0, 0) + ", answer = " + numDupDigitsAtMostN(n));
            d = "100".toCharArray();
            System.out.println("no started (n=100): distinct " + goNoStarted(0, 1, 0) + " true " + (100 - numDupDigitsAtMostN(100)));
            for (int x : new int[] {20, 100, 235, 1000}) {
                d = String.valueOf(x).toCharArray();
                memoNoTight = new Integer[d.length][2][1 << 10];
                int bad = goNoTightMemo(0, 1, 0, 0);
                System.out.println("no tight memo n=" + x + ": distinct " + bad + " true " + (x - numDupDigitsAtMostN(x)));
            }
            System.out.println("minusOne 1000 = " + minusOne("1000") + ", 1 = " + minusOne("1"));
            return;
        }
        // brute prefix counts
        int N = 30000;
        int[] dup = new int[N + 1];
        for (int x = 1; x <= N; x++) dup[x] = dup[x - 1] + (distinctDigits(x) ? 0 : 1);
        Random r = new Random(60);
        for (int x = 1; x <= 1200; x++) Check.eq(dup[x], numDupDigitsAtMostN(x), "dup n=" + x);
        for (int t = 0; t < 800; t++) {
            int x = 1 + r.nextInt(N);
            Check.eq(dup[x], numDupDigitsAtMostN(x), "dup n=" + x);
        }
        int M = 3000;
        for (int t = 0; t < 1500; t++) {
            int lo = 1 + r.nextInt(M), hi = lo + r.nextInt(M);
            int mn = 1 + r.nextInt(30), mx = mn + r.nextInt(20);
            int want = 0;
            for (int x = lo; x <= hi; x++) { int s = digitSum(x); if (s >= mn && s <= mx) want++; }
            Check.eq(want, count(String.valueOf(lo), String.valueOf(hi), mn, mx), "range " + lo + " " + hi + " " + mn + " " + mx);
        }
        Check.eq(1, numDupDigitsAtMostN(20), "LC 1012 sample 1");
        Check.eq(10, numDupDigitsAtMostN(100), "LC 1012 sample 2");
        Check.eq(262, numDupDigitsAtMostN(1000), "LC 1012 sample 3");
        Check.eq(11, count("1", "12", 1, 8), "LC 2719 sample 1");
        Check.eq(5, count("1", "5", 1, 5), "LC 2719 sample 2");
        // every number in [1, 10^22 - 1] has digit sum in [1, 400]: count = (10^22 - 1) mod p
        String nines = "9999999999999999999999";
        long p10 = 1;
        for (int i = 0; i < 22; i++) p10 = p10 * 10 % MOD;
        Check.eq((p10 - 1 + MOD) % MOD, (long) count("1", nines, 1, 400), "22 nines");
        // split a big range in two and compare
        String a = "1000000000000000000000", b = "4876543210987654321098", c = "2345678901234567890123";
        long whole = count(a, b, 20, 90);
        long parts = (count(a, c, 20, 90) + (long) count(c, b, 20, 90)) % MOD;
        long cSum = digitSum(0);
        int cs = 0;
        for (char ch : c.toCharArray()) cs += ch - '0';
        if (cs >= 20 && cs <= 90) parts = (parts - 1 + MOD) % MOD;
        Check.eq(whole, parts, "split big range");
        Check.done("P10DigitDp");
    }
}
