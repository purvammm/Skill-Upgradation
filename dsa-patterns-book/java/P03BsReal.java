import java.util.*;

public class P03BsReal {
    //@tpl sqrt
    // Square root of x >= 0 by halving. A fixed count of steps replaces an
    // epsilon test: 100 halvings make the range 2^100 times smaller.
    static double sqrt(double x) {
        double lo = 0, hi = Math.max(1, x);       // sqrt(x) is in [lo, hi]
        for (int it = 0; it < 100; it++) {
            double mid = (lo + hi) / 2;
            if (mid * mid < x) lo = mid;          // keep the answer inside [lo, hi]
            else hi = mid;
        }
        return hi;
    }
    //@end

    //@tpl squares
    // LC 3453: lowest y where the square area below the line is half the total.
    // below(y) never decreases as y grows: find the first y with 2*below >= total.
    static double separateSquares(int[][] sq) {
        long total = 0;
        for (int[] s : sq) total += (long) s[2] * s[2];
        double lo = 0, hi = 2e9;                  // all squares lie in y <= 2e9
        for (int it = 0; it < 100; it++) {
            double mid = (lo + hi) / 2;
            if (lessThanHalfBelow(sq, mid, total)) lo = mid;    // go up
            else hi = mid;
        }
        return hi;
    }

    // Is 2 * (area below y) < total? Squares wholly below y are added exactly
    // in a long, so a total near 1e12 does not eat the decimals of the cut part.
    static boolean lessThanHalfBelow(int[][] sq, double y, long total) {
        long whole = 0;
        double cut = 0;
        for (int[] s : sq) {                      // s = {x, y, side}
            if (y >= (long) s[1] + s[2]) whole += (long) s[2] * s[2];
            else if (y > s[1]) cut += (y - s[1]) * s[2];
        }
        return 2 * cut < total - 2 * whole;
    }
    //@end

    //@tpl ternary
    interface Fn { double at(double x); }

    // Minimum point of f on [lo, hi] when f falls and then rises (unimodal).
    // Each step drops the outer third that cannot hold the minimum.
    static double ternaryMin(Fn f, double lo, double hi) {
        for (int it = 0; it < 200; it++) {        // (2/3)^200 of the range is left
            double m1 = lo + (hi - lo) / 3, m2 = hi - (hi - lo) / 3;
            if (f.at(m1) < f.at(m2)) hi = m2;     // the minimum is left of m2
            else lo = m1;                         // the minimum is right of m1
        }
        return (lo + hi) / 2;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Sweep: below(y) is piecewise linear; its slope changes by +side at y_i and
    // by -side at y_i + side. Walk the events until 2*below reaches the total.
    static double sweepSquares(int[][] sq) {
        TreeMap<Long, Long> slopeChange = new TreeMap<>();
        long total = 0;
        for (int[] s : sq) {
            slopeChange.merge((long) s[1], (long) s[2], Long::sum);
            slopeChange.merge((long) s[1] + s[2], (long) -s[2], Long::sum);
            total += (long) s[2] * s[2];
        }
        long slope = 0, below = 0, prev = 0;     // below is exact at event points (integers)
        for (Map.Entry<Long, Long> e : slopeChange.entrySet()) {
            long y = e.getKey();
            long next = below + slope * (y - prev);
            if (2 * next >= total) return prev + (total / 2.0 - below) / slope;
            below = next;
            prev = y;
            slope += e.getValue();
        }
        return prev;
    }

    // LC 786, quoted in the variants: binary search a real v, count fractions <= v.
    static int[] kthSmallestPrimeFraction(int[] arr, int k) {
        double lo = 0, hi = 1;
        for (int it = 0; it < 100; it++) {
            double mid = (lo + hi) / 2;
            int count = 0, p = 0, q = 1;
            for (int i = 0, j = 1; j < arr.length; j++) {   // fractions arr[i] / arr[j]
                while (i < j && arr[i] <= mid * arr[j]) i++;
                count += i;
                if (i > 0 && (long) arr[i - 1] * q > (long) p * arr[j]) { p = arr[i - 1]; q = arr[j]; }
            }
            if (count == k) return new int[] {p, q};
            if (count < k) lo = mid;
            else hi = mid;
        }
        return null;
    }

    // LC 2448, quoted in the variants: f convex on integers; first x with f(x) <= f(x+1).
    static long minCost(int[] nums, int[] cost) {
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
        for (int x : nums) { lo = Math.min(lo, x); hi = Math.max(hi, x); }
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (costAt(nums, cost, mid) <= costAt(nums, cost, mid + 1)) hi = mid;
            else lo = mid + 1;
        }
        return costAt(nums, cost, lo);
    }

    static long costAt(int[] nums, int[] cost, long x) {
        long s = 0;
        for (int i = 0; i < nums.length; i++) s += Math.abs(nums[i] - x) * cost[i];
        return s;
    }

    // The version most people write first: every area in one double (quoted in a trap).
    static double naiveSquares(int[][] sq) {
        double total = 0;
        for (int[] s : sq) total += (double) s[2] * s[2];
        double lo = 0, hi = 2e9;
        for (int it = 0; it < 100; it++) {
            double mid = (lo + hi) / 2;
            if (2 * areaBelowSlow(sq, mid) < total) lo = mid;
            else hi = mid;
        }
        return hi;
    }

    // Exact answer: walk the breakpoints, find the segment where 2*below reaches total.
    static double bruteSquares(int[][] sq) {
        TreeSet<Double> ys = new TreeSet<>();
        double total = 0;
        for (int[] s : sq) { ys.add((double) s[1]); ys.add((double) s[1] + s[2]); total += (double) s[2] * s[2]; }
        Double prev = null;
        for (double y : ys) {
            if (2 * areaBelowSlow(sq, y) >= total) {
                if (prev == null) return y;
                // below is linear on [prev, y]
                double a = areaBelowSlow(sq, prev), b = areaBelowSlow(sq, y);
                return prev + (total / 2 - a) / (b - a) * (y - prev);
            }
            prev = y;
        }
        return ys.last();
    }

    static double areaBelowSlow(int[][] sq, double y) {
        double area = 0;
        for (int[] s : sq) {
            if (y >= s[1] + s[2]) area += (double) s[2] * s[2];
            else if (y > s[1]) area += (y - s[1]) * s[2];
        }
        return area;
    }

    public static void main(String[] args) {
        Random r = new Random(37);
        for (int t = 0; t < 3000; t++) {
            double x = r.nextInt(4) == 0 ? r.nextInt(100) : r.nextDouble() * Math.pow(10, r.nextInt(19) - 9);
            Check.eq(Math.sqrt(x), sqrt(x), Math.max(1e-12, Math.sqrt(x) * 1e-14), "sqrt " + x);
            int n = 1 + r.nextInt(6);
            int[][] sq = new int[n][];
            for (int i = 0; i < n; i++) sq[i] = new int[] {r.nextInt(10), r.nextInt(15), 1 + r.nextInt(8)};
            Check.eq(bruteSquares(sq), separateSquares(sq), 1e-6, "squares " + Arrays.deepToString(sq));
            Check.eq(bruteSquares(sq), sweepSquares(sq), 1e-9, "sweep " + Arrays.deepToString(sq));
            // ternary: weighted sum of |x - a_i| is convex; the minimum is at some a_i
            int m = 1 + r.nextInt(6);
            final int[] a = Check.ints(r, m, -50, 50), w = Check.ints(r, m, 1, 5);
            Fn f = v -> { double s = 0; for (int i = 0; i < a.length; i++) s += w[i] * Math.abs(v - a[i]); return s; };
            double best = Double.MAX_VALUE;
            for (int ai : a) best = Math.min(best, f.at(ai));
            Check.eq(best, f.at(ternaryMin(f, -100, 100)), 1e-6, "ternary " + Arrays.toString(a));
            final double c = r.nextDouble() * 200 - 100;
            Check.eq(c, ternaryMin(v -> (v - c) * (v - c) + 7, -100, 100), 1e-6, "parabola " + c);
        }
        // the variants: LC 786 and LC 2448 against brute force
        int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31};
        for (int t = 0; t < 500; t++) {
            TreeSet<Integer> set = new TreeSet<>();
            set.add(1);
            int len = 2 + r.nextInt(7);
            while (set.size() < len) set.add(primes[r.nextInt(primes.length)]);
            int[] arr = new int[len];
            int i = 0;
            for (int v : set) arr[i++] = v;
            List<int[]> fr = new ArrayList<>();
            for (int x = 0; x < len; x++) for (int y = x + 1; y < len; y++) fr.add(new int[] {arr[x], arr[y]});
            Collections.sort(fr, (u, v) -> Long.compare((long) u[0] * v[1], (long) v[0] * u[1]));
            int k = 1 + r.nextInt(fr.size());
            Check.eq(fr.get(k - 1), kthSmallestPrimeFraction(arr, k), "LC 786 " + Arrays.toString(arr) + " k=" + k);
            int n = 1 + r.nextInt(6);
            int[] nums = Check.ints(r, n, 1, 30), cost = Check.ints(r, n, 1, 9);
            long best = Long.MAX_VALUE;
            for (int x = 1; x <= 30; x++) best = Math.min(best, costAt(nums, cost, x));
            Check.eq(best, minCost(nums, cost), "LC 2448 " + Arrays.toString(nums));
        }
        Check.eq(new int[] {2, 5}, kthSmallestPrimeFraction(new int[] {1, 2, 3, 5}, 3), "LC 786 sample 1");
        Check.eq(new int[] {1, 7}, kthSmallestPrimeFraction(new int[] {1, 7}, 1), "LC 786 sample 2");
        Check.eq(8, minCost(new int[] {1, 3, 5, 2}, new int[] {2, 3, 1, 14}), "LC 2448 sample 1");
        Check.eq(0, minCost(new int[] {2, 2, 2, 2, 2}, new int[] {4, 2, 8, 1, 3}), "LC 2448 sample 2");
        // big squares near the limits (total area 1e12)
        int[][] big = {{0, 0, 1000000}, {5, 1000000000, 1}};
        Check.eq(bruteSquares(big), separateSquares(big), 1e-5, "big squares");
        for (int t = 0; t < 3; t++) {
            int[][] many = new int[50000][];
            for (int i = 0; i < many.length; i++)
                many[i] = new int[] {0, r.nextInt(1000000000), 1 + r.nextInt(4000)};
            Check.eq(sweepSquares(many), separateSquares(many), 1e-5, "many squares");
        }
        // precision: a thin square between two squares of area ~5e11 holds the answer.
        // Summing all areas in one double misses by ~3e-5; the long/double split does not.
        int[][] thin = {{0, 0, 707106}, {0, 707106, 1}, {0, 707107, 707106}};
        Check.eq(707106.5, separateSquares(thin), 1e-7, "thin square");
        Check.isTrue(Math.abs(naiveSquares(thin) - 707106.5) > 1e-5, "naive double misses");
        // LeetCode samples
        Check.eq(1.0, separateSquares(new int[][] {{0, 0, 1}, {2, 2, 1}}), 1e-5, "LC 3453 sample 1");
        Check.eq(7.0 / 6, separateSquares(new int[][] {{0, 0, 2}, {1, 1, 1}}), 1e-5, "LC 3453 sample 2");
        // the epsilon trap: at 1e9 neighbouring doubles are ~1.2e-7 apart, more than 1e-9
        Check.isTrue(Math.ulp(1e9) > 1e-9, "ulp(1e9) > 1e-9");
        Check.done("P03BsReal");
    }
}
