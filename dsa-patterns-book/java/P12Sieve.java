import java.util.*;

public class P12Sieve {
    //@tpl sieve
    // isPrime[x] for 0 <= x <= n. Each prime p crosses out p*p, p*p + p, ...
    static boolean[] sieve(int n) {
        boolean[] isPrime = new boolean[n + 1];
        if (n >= 2) Arrays.fill(isPrime, 2, n + 1, true);
        for (int p = 2; (long) p * p <= n; p++)
            if (isPrime[p])
                for (int m = p * p; m <= n; m += p) isPrime[m] = false;
        return isPrime;
    }

    static int countPrimes(int n) {                 // primes strictly below n
        int c = 0;
        for (boolean b : sieve(n - 1)) if (b) c++;
        return c;
    }
    //@end

    //@tpl spf
    // spf[x] = smallest prime factor of x, for 2 <= x <= n.
    static int[] spfSieve(int n) {
        int[] spf = new int[n + 1];
        for (int p = 2; p <= n; p++)
            if (spf[p] == 0)                        // no smaller prime divides p
                for (int m = p; m <= n; m += p)
                    if (spf[m] == 0) spf[m] = p;
        return spf;
    }

    // Distinct prime factors of x, smallest first, in O(log x) divisions.
    static List<Integer> distinctPrimes(int x, int[] spf) {
        List<Integer> out = new ArrayList<>();
        while (x > 1) {
            int p = spf[x];
            out.add(p);
            while (x % p == 0) x /= p;              // remove every copy of p
        }
        return out;
    }
    //@end

    //@tpl trial
    // One number: if x = a * b then min(a, b) <= sqrt(x). O(sqrt x) steps.
    static List<Long> primeFactors(long x) {        // with repeats, ascending
        List<Long> out = new ArrayList<>();
        for (long d = 2; d * d <= x; d++)
            while (x % d == 0) { out.add(d); x /= d; }
        if (x > 1) out.add(x);                      // what is left is a prime
        return out;
    }

    static boolean isPrime(long x) {
        if (x < 2) return false;
        for (long d = 2; d * d <= x; d++) if (x % d == 0) return false;
        return true;
    }
    //@end

    //@tpl component
    // Largest component by common factor (LC 952): join every value with each
    // of its primes, so two values that share a prime end up in one set.
    static int largestComponentSize(int[] nums) {
        int max = 0;
        for (int v : nums) max = Math.max(max, v);
        int[] spf = spfSieve(max);
        DSU d = new DSU(max + 1);                   // nodes: values and primes
        for (int v : nums)
            for (int p : distinctPrimes(v, spf)) d.union(v, p);
        Map<Integer, Integer> size = new HashMap<>();      // root -> values
        int best = 0;
        for (int v : nums)
            best = Math.max(best, size.merge(d.find(v), 1, Integer::sum));
        return best;
    }
    //@end

    // DSU as printed in part 9 (not printed here)
    static class DSU {
        int[] parent, size;
        DSU(int n) {
            parent = new int[n];
            size = new int[n];
            for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
        }
        int find(int x) { return parent[x] == x ? x : (parent[x] = find(parent[x])); }
        boolean union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) return false;
            if (size[ra] < size[rb]) { int t = ra; ra = rb; rb = t; }
            parent[rb] = ra;
            size[ra] += size[rb];
            return true;
        }
    }

    // ---------------- tests (not printed) ----------------
    static boolean brutePrime(long x) {
        if (x < 2) return false;
        for (long d = 2; d < x; d++) if (x % d == 0) return false;
        return true;
    }

    static int bruteComponent(int[] a) {
        int n = a.length, best = 0;
        boolean[] seen = new boolean[n];
        for (int s = 0; s < n; s++) {
            if (seen[s]) continue;
            int cnt = 0;
            Deque<Integer> st = new ArrayDeque<>();
            st.push(s);
            seen[s] = true;
            while (!st.isEmpty()) {
                int u = st.pop();
                cnt++;
                for (int v = 0; v < n; v++)
                    if (!seen[v] && gcd(a[u], a[v]) > 1) { seen[v] = true; st.push(v); }
            }
            best = Math.max(best, cnt);
        }
        return best;
    }

    static int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }

    public static void main(String[] args) {
        Random r = new Random(1206);
        boolean[] s = sieve(3000);
        int[] spf = spfSieve(3000);
        for (int x = 0; x <= 3000; x++) {
            Check.eq(brutePrime(x), s[x], "sieve " + x);
            Check.eq(brutePrime(x), isPrime(x), "isPrime " + x);
            if (x >= 2) {
                int small = 2;
                while (x % small != 0) small++;
                Check.eq(small, spf[x], "spf " + x);
                TreeSet<Integer> want = new TreeSet<>();
                for (int d = 2; d <= x; d++) if (x % d == 0 && brutePrime(d)) want.add(d);
                Check.eq(new ArrayList<>(want).toString(), distinctPrimes(x, spf).toString(), "distinct " + x);
            }
        }
        for (int n = 0; n <= 300; n++) {
            int c = 0;
            for (int x = 0; x < n; x++) if (brutePrime(x)) c++;
            Check.eq(c, countPrimes(n), "countPrimes " + n);
            Check.eq(n + 1, sieve(n).length, "length");
        }
        for (int t = 0; t < 400; t++) {
            long x = t % 2 == 0 ? 1 + r.nextInt(100000) : 1 + Math.abs(r.nextLong() % 1_000_000_000_000L);
            List<Long> f = primeFactors(x);
            long prod = 1;
            for (int i = 0; i < f.size(); i++) {
                prod *= f.get(i);
                if (i > 0) Check.isTrue(f.get(i - 1) <= f.get(i), "ascending");
                Check.isTrue(java.math.BigInteger.valueOf(f.get(i)).isProbablePrime(40), "prime factor");
            }
            Check.eq(x, prod, "product " + x);
            Check.eq(java.math.BigInteger.valueOf(x).isProbablePrime(40), isPrime(x), "isPrime big " + x);
        }
        for (int t = 0; t < 1500; t++) {
            int n = 1 + r.nextInt(10);
            Set<Integer> vals = new LinkedHashSet<>();
            while (vals.size() < n) vals.add(1 + r.nextInt(t % 2 == 0 ? 40 : 1000));
            int[] a = new int[n];
            int i = 0;
            for (int v : vals) a[i++] = v;
            Check.eq(bruteComponent(a), largestComponentSize(a), "component " + Arrays.toString(a));
        }
        // size check: pi(5e6) = 348513
        Check.eq(348513, countPrimes(5_000_001), "pi(5e6)");
        // traps quoted in the book
        boolean threw = false;
        try {
            int n = 100000;
            boolean[] bad = new boolean[n + 1];
            Arrays.fill(bad, 2, n + 1, true);
            for (int p = 2; p <= n; p++)
                if (bad[p]) for (int m = p * p; m <= n; m += p) bad[m] = false;
        } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        Check.isTrue(threw, "p * p overflow without the sqrt bound");
        Check.isTrue(46349 * 46349 < 0, "46349^2 overflows");
        threw = false;
        try { Arrays.fill(new boolean[1], 2, 1, true); } catch (IllegalArgumentException e) { threw = true; }
        Check.isTrue(threw, "fill(2, 1) throws");
        // LeetCode samples
        Check.eq(4, countPrimes(10), "LC 204 sample 1");
        Check.eq(0, countPrimes(0), "LC 204 sample 2");
        Check.eq(0, countPrimes(1), "LC 204 sample 3");
        Check.eq(4, largestComponentSize(new int[] {4, 6, 15, 35}), "LC 952 sample 1");
        Check.eq(2, largestComponentSize(new int[] {20, 50, 9, 63}), "LC 952 sample 2");
        Check.eq(8, largestComponentSize(new int[] {2, 3, 6, 7, 4, 12, 21, 39}), "LC 952 sample 3");
        Check.done("P12Sieve");
    }
}
