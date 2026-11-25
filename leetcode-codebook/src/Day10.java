import java.util.*;

/*@core Number theory toolkit
note: `gcd` by Euclid, `lcm = a / gcd · b` (divide first). Fast power halves the exponent. Inverse mod a PRIME is `power(a, MOD − 2)`. The smallest-prime-factor sieve factorises any x ≤ N in O(log x).
@*/
class Core10 {
    static final long MOD = 1_000_000_007L;

    static long gcd(long a, long b) { return b == 0 ? a : gcd(b, a % b); }

    static long lcm(long a, long b) { return a / gcd(a, b) * b; }

    static long power(long base, long exp, long mod) {
        long r = 1;
        base %= mod;
        while (exp > 0) {
            if ((exp & 1) == 1) r = r * base % mod;
            base = base * base % mod;
            exp >>= 1;
        }
        return r;
    }

    static long inverse(long a) { return power(a, MOD - 2, MOD); }   // MOD must be prime

    static int[] smallestPrimeFactor(int n) {
        int[] spf = new int[n + 1];
        for (int i = 2; i <= n; i++)
            if (spf[i] == 0)
                for (int j = i; j <= n; j += i) if (spf[j] == 0) spf[j] = i;
        return spf;
    }

    static int squareFreeKernel(int x, int[] spf) {   // product of primes with an odd exponent
        int k = 1;
        while (x > 1) {
            int p = spf[x], e = 0;
            while (x % p == 0) { x /= p; e++; }
            if (e % 2 == 1) k *= p;
        }
        return k;
    }

    static long nCr(int n, int r, long[] fact, long[] invFact) {
        if (r < 0 || r > n) return 0;
        return fact[n] * invFact[r] % MOD * invFact[n - r] % MOD;
    }
}

/*@LC 204
tag: Sieve of Eratosthenes
cue: Count primes below n.
hook: Each prime crosses out its multiples, starting from its square.
time: O(n log log n) time · O(n) space
trap: Start at `i * i` computed in `long`: for i near 46 341 an int square overflows.
@*/
class LC204 {
    public int countPrimes(int n) {
        if (n < 3) return 0;
        boolean[] composite = new boolean[n];
        int count = 0;
        for (int i = 2; i < n; i++) {
            if (composite[i]) continue;
            count++;                                                      //* i is prime
            for (long j = (long) i * i; j < n; j += i) composite[(int) j] = true;   //* from i*i
        }
        return count;
    }
}

/*@var LC 2523 · Closest Prime Numbers in Range
note: Sieve up to `right`, then walk the primes in [left, right] and keep the closest neighbouring pair.
@*/
class LC204v1 {
    public int[] closestPrimes(int left, int right) {
        boolean[] comp = new boolean[right + 1];
        for (int i = 2; (long) i * i <= right; i++)
            if (!comp[i]) for (int j = i * i; j <= right; j += i) comp[j] = true;
        int prev = -1;
        int[] ans = {-1, -1};
        for (int x = Math.max(left, 2); x <= right; x++) {
            if (comp[x]) continue;
            if (prev != -1 && (ans[0] == -1 || x - prev < ans[1] - ans[0])) ans = new int[]{prev, x};
            prev = x;
        }
        return ans;
    }
}

/*@LC 1922
tag: Fast power mod p
cue: Count digit strings of length n (up to 10¹⁵) with even digits at even positions and primes at odd ones.
hook: Five choices on even spots, four on odd spots, multiply with fast power.
time: O(log n) time · O(1) space
trap: n is a `long`; there are `(n + 1) / 2` even positions (0, 2, 4 …) and `n / 2` odd ones.
@*/
class LC1922 {
    private static final long MOD = 1_000_000_007L;

    public int countGoodNumbers(long n) {
        long even = (n + 1) / 2, odd = n / 2;
        return (int) (power(5, even) * power(4, odd) % MOD);   //*
    }

    private long power(long base, long exp) {
        long result = 1;
        base %= MOD;
        while (exp > 0) {
            if ((exp & 1) == 1) result = result * base % MOD;   //* odd exponent: take one base
            base = base * base % MOD;                         //* square
            exp >>= 1;                                        //* halve
        }
        return result;
    }
}

/*@var LC 50 · Pow(x, n)
note: The same loop on doubles. Negative n → invert x. Copy n into a `long` first: −2³¹ cannot be negated as an int.
@*/
class LC1922v1 {
    public double myPow(double x, int n) {
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
}

/*@LC 2447
tag: gcd of a growing subarray
cue: Count subarrays whose gcd is exactly k.
hook: Extend from each start; the gcd only shrinks, so stop once it is not a multiple of k.
time: O(n² log V) worst case, fast in practice · O(1) space
trap: Start with `g = 0`, because `gcd(0, x) = x`.
var: Many queries / big n | Keep the list of distinct gcds of subarrays ending at i: at most log V values.
@*/
class LC2447 {
    public int subarrayGCD(int[] nums, int k) {
        int count = 0;
        for (int i = 0; i < nums.length; i++) {
            int g = 0;
            for (int j = i; j < nums.length; j++) {
                g = gcd(g, nums[j]);                      //*
                if (g % k != 0) break;                    //* can never come back to k
                if (g == k) count++;
            }
        }
        return count;
    }

    private int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }   //*
}

/*@var LC 2470 · Number of Subarrays With LCM Equal to K
note: Mirror image: the lcm only grows, so stop once k is not a multiple of it. Divide before multiplying.
@*/
class LC2447v1 {
    public int subarrayLCM(int[] nums, int k) {
        int count = 0;
        for (int i = 0; i < nums.length; i++) {
            long l = 1;
            for (int j = i; j < nums.length; j++) {
                l = l / gcd(l, nums[j]) * nums[j];
                if (k % l != 0) break;
                if (l == k) count++;
            }
        }
        return count;
    }

    private long gcd(long a, long b) { return b == 0 ? a : gcd(b, a % b); }
}

/*@LC 2862
tag: Square-free part of the index
cue: Pick indices whose pairwise products are perfect squares; maximise the sum (1-indexed).
hook: Same square-free part, same group: indices k·1², k·2², k·3² …
time: O(n) time (Σ √(n/k)) · O(1) space
trap: The indices are 1-based: read `nums.get(k·j² − 1)`.
var: Values (not indices) must multiply to squares | Group values by `squareFreeKernel(value)` with the SPF sieve (CF 1497E1 idea).
@*/
class LC2862 {
    public long maximumSum(List<Integer> nums) {
        int n = nums.size();
        long best = 0;
        for (int k = 1; k <= n; k++) {                       // every k gives a valid set; the best is square-free k
            long sum = 0;
            for (int j = 1; (long) k * j * j <= n; j++) sum += nums.get(k * j * j - 1);   //* k·1², k·2², …
            best = Math.max(best, sum);
        }
        return best;
    }
}

/*@LC 1819
tag: Walk the multiples of g
cue: How many different gcds can non-empty subsequences have?
hook: g is possible exactly when the gcd of all present multiples of g equals g.
time: O(V log V) time · O(V) space
trap: Loop over candidate g up to max value, not over the array.
@*/
class LC1819 {
    public int countDifferentSubsequenceGCDs(int[] nums) {
        int max = 0;
        for (int x : nums) max = Math.max(max, x);
        boolean[] present = new boolean[max + 1];
        for (int x : nums) present[x] = true;
        int count = 0;
        for (int g = 1; g <= max; g++) {
            int cur = 0;
            for (int m = g; m <= max && cur != g; m += g)    //* multiples of g
                if (present[m]) cur = gcd(cur, m);
            if (cur == g) count++;                           //*
        }
        return count;
    }

    private int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }
}

/*@LC 2572
tag: Prime bitmask + 0/1 knapsack over masks
cue: Count subsets whose product is square-free; values are 1..30.
hook: Only 10 primes up to 30: each number is a mask; masks in a subset must not overlap.
time: O(n · 2¹⁰) time · O(2¹⁰) space
trap: A number divisible by p² (4, 8, 9, 12 …) can never be used. Subtract the empty subset at the end.
var: LC 1994 The Number of Good Subsets | Same masks; handle 1s at the end with ×2^(count of 1s) and exclude the empty product.
@*/
class LC2572 {
    public int squareFreeSubsets(int[] nums) {
        final int MOD = 1_000_000_007;
        int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29};
        long[] dp = new long[1 << 10];                  // dp[mask] = subsets using exactly these primes
        dp[0] = 1;
        for (int x : nums) {
            int mask = 0;
            boolean squareFree = true;
            for (int i = 0; i < 10; i++) {
                if (x % (primes[i] * primes[i]) == 0) squareFree = false;   //*
                if (x % primes[i] == 0) mask |= 1 << i;
            }
            if (!squareFree) continue;
            for (int m = (1 << 10) - 1; m >= 0; m--)                          //* downwards: x used once
                if ((m & mask) == 0) dp[m | mask] = (dp[m | mask] + dp[m]) % MOD;   //* no shared prime
        }
        long total = 0;
        for (long v : dp) total += v;
        return (int) ((total - 1 + MOD) % MOD);                               //* minus the empty subset
    }
}

public class Day10 {
    static int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }

    public static void main(String[] args) {
        T.eq("core gcd", new long[]{Core10.gcd(12, 18), Core10.lcm(4, 6), Core10.power(2, 10, 1000), Core10.inverse(2) * 2 % Core10.MOD}, new long[]{6, 12, 24, 1});
        int[] spf = Core10.smallestPrimeFactor(100);
        T.eq("core kernel", new int[]{Core10.squareFreeKernel(12, spf), Core10.squareFreeKernel(50, spf), Core10.squareFreeKernel(36, spf)}, T.ints("[3,2,1]"));
        long[] f = {1, 1, 2, 6, 24, 120}, inv = new long[6];
        for (int i = 0; i < 6; i++) inv[i] = Core10.inverse(f[i]);
        T.eq("core nCr", Core10.nCr(5, 2, f, inv), 10L);

        int[] ns = {10, 0, 1, 2, 3, 100, 5000000};
        int[] pw = {4, 0, 0, 0, 1, 25, 348513};
        for (int i = 0; i < ns.length; i++) T.eq("204 " + ns[i], new LC204().countPrimes(ns[i]), pw[i]);
        T.eq("2523a", new LC204v1().closestPrimes(10, 19), T.ints("[11,13]"));
        T.eq("2523b", new LC204v1().closestPrimes(4, 6), T.ints("[-1,-1]"));
        T.eq("2523c", new LC204v1().closestPrimes(1, 1), T.ints("[-1,-1]"));

        T.eq("1922a", new LC1922().countGoodNumbers(1), 5);
        T.eq("1922b", new LC1922().countGoodNumbers(4), 400);
        T.eq("1922c", new LC1922().countGoodNumbers(50), 564908303);
        T.close("50a", new LC1922v1().myPow(2.0, 10), 1024.0);
        T.close("50b", new LC1922v1().myPow(2.1, 3), 9.261);
        T.close("50c", new LC1922v1().myPow(2.0, -2), 0.25);
        T.close("50d", new LC1922v1().myPow(1.0, Integer.MIN_VALUE), 1.0);

        T.eq("2447a", new LC2447().subarrayGCD(T.ints("[9,3,1,2,6,3]"), 3), 4);
        T.eq("2447b", new LC2447().subarrayGCD(T.ints("[4]"), 7), 0);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 9), 1, 12);
            int k = T.rnd(1, 6), c = 0;
            for (int i = 0; i < a.length; i++) { int g = 0; for (int j = i; j < a.length; j++) if ((g = gcd(g, a[j])) == k) c++; }
            T.eq("2447 rand", new LC2447().subarrayGCD(a, k), c);
        }
        T.eq("2470a", new LC2447v1().subarrayLCM(T.ints("[3,6,2,7,1]"), 6), 4);
        T.eq("2470b", new LC2447v1().subarrayLCM(T.ints("[3]"), 2), 0);

        T.eq("2862a", new LC2862().maximumSum(List.of(8, 7, 3, 5, 7, 2, 4, 9)), 16L);
        T.eq("2862b", new LC2862().maximumSum(List.of(8, 10, 3, 8, 1, 13, 7, 9, 4)), 20L);
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 10);
            List<Integer> a = new ArrayList<>();
            for (int i = 0; i < n; i++) a.add(T.rnd(1, 20));
            long best = 0;
            for (int mask = 1; mask < 1 << n; mask++) {
                boolean ok = true; long s = 0;
                for (int i = 0; i < n; i++) if ((mask >> i & 1) == 1) {
                    s += a.get(i);
                    for (int j = i + 1; j < n; j++) if ((mask >> j & 1) == 1) { long p = (long) (i + 1) * (j + 1), r = (long) Math.sqrt(p); if (r * r != p) ok = false; }
                }
                if (ok) best = Math.max(best, s);
            }
            T.eq("2862 rand", new LC2862().maximumSum(a), best);
        }

        T.eq("1819a", new LC1819().countDifferentSubsequenceGCDs(T.ints("[6,10,3]")), 5);
        T.eq("1819b", new LC1819().countDifferentSubsequenceGCDs(T.ints("[5,15,40,5,6]")), 7);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 8), 1, 30);
            Set<Integer> g = new HashSet<>();
            for (int mask = 1; mask < 1 << a.length; mask++) { int x = 0; for (int i = 0; i < a.length; i++) if ((mask >> i & 1) == 1) x = gcd(x, a[i]); g.add(x); }
            T.eq("1819 rand", new LC1819().countDifferentSubsequenceGCDs(a), g.size());
        }

        T.eq("2572a", new LC2572().squareFreeSubsets(T.ints("[3,4,4,5]")), 3);
        T.eq("2572b", new LC2572().squareFreeSubsets(T.ints("[1]")), 1);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 10), 1, 30);
            int c = 0;
            for (int mask = 1; mask < 1 << a.length; mask++) {
                long p = 1;
                for (int i = 0; i < a.length; i++) if ((mask >> i & 1) == 1) p *= a[i];
                boolean sf = true;
                for (long q = 2; q * q <= p; q++) if (p % (q * q) == 0) { sf = false; break; }
                if (sf) c++;
            }
            T.eq("2572 rand", new LC2572().squareFreeSubsets(a), c);
        }
        T.summary("Day10");
    }
}
