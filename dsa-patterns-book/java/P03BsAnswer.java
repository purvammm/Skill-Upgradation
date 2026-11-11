import java.util.*;

public class P03BsAnswer {
    //@tpl shape
    interface Ok { boolean test(long x); }

    // Smallest x in [lo, hi] with ok(x) true. ok must be monotone
    // (false ... false true ... true) and ok(hi) must be true.
    static long smallestOk(long lo, long hi, Ok ok) {
        while (lo < hi) {
            long mid = lo + (hi - lo) / 2;
            if (ok.test(mid)) hi = mid;       // mid works: answer is mid or less
            else lo = mid + 1;                // mid fails, and so does all below
        }
        return lo;
    }
    //@end

    //@tpl koko
    // LC 875: slowest speed k that eats every pile within h hours.
    // k = biggest pile always works, because h >= number of piles.
    static int minEatingSpeed(int[] piles, int h) {
        int max = 0;
        for (int p : piles) max = Math.max(max, p);
        return (int) smallestOk(1, max, k -> hoursAt(piles, k) <= h);
    }

    static long hoursAt(int[] piles, long k) {
        long hours = 0;
        for (int p : piles) hours += (p + k - 1) / k;   // ceil(p / k) in integers
        return hours;
    }

    // LC 1482: fewest days until m bouquets of k adjacent bloomed flowers exist.
    static int minDays(int[] bloom, int m, int k) {
        if ((long) m * k > bloom.length) return -1;       // not enough flowers ever
        int max = 0;
        for (int b : bloom) max = Math.max(max, b);
        return (int) smallestOk(1, max, day -> bouquets(bloom, day, k) >= m);
    }

    static int bouquets(int[] bloom, long day, int k) {
        int made = 0, run = 0;
        for (int b : bloom) {
            run = b <= day ? run + 1 : 0;                 // bloomed flowers in a row
            if (run == k) { made++; run = 0; }
        }
        return made;
    }
    //@end

    //@tpl ship
    // LC 1011: least capacity that ships the packages, in order, within d days.
    // LC 410 (split array largest sum into k parts) is this call with d = k.
    static int shipWithinDays(int[] w, int d) {
        long max = 0, sum = 0;
        for (int x : w) { max = Math.max(max, x); sum += x; }
        return (int) smallestOk(max, sum, cap -> daysNeeded(w, cap) <= d);
    }

    // Greedy check: fill each day until the next package does not fit.
    static int daysNeeded(int[] w, long cap) {
        int days = 1;
        long load = 0;
        for (int x : w) {
            if (load + x > cap) { days++; load = 0; }
            load += x;
        }
        return days;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int bruteKoko(int[] piles, int h) {
        for (int k = 1; ; k++) {
            long hours = 0;
            for (int p : piles) hours += (p + k - 1) / k;
            if (hours <= h) return k;
        }
    }

    // DP: least possible largest part sum when a is cut into at most d contiguous parts.
    static long bruteSplit(int[] a, int d) {
        int n = a.length;
        long[][] f = new long[d + 1][n + 1];
        for (long[] row : f) Arrays.fill(row, Long.MAX_VALUE);
        f[0][0] = 0;
        for (int parts = 1; parts <= d; parts++) {
            for (int i = 0; i <= n; i++) {
                f[parts][i] = f[parts - 1][i];
                long s = 0;
                for (int j = i - 1; j >= 0; j--) {
                    s += a[j];
                    if (f[parts - 1][j] != Long.MAX_VALUE)
                        f[parts][i] = Math.min(f[parts][i], Math.max(f[parts - 1][j], s));
                }
            }
        }
        return f[d][n];
    }

    // DP over positions: most disjoint runs of k bloomed flowers on a given day.
    static int bruteBouquets(int[] bloom, int day, int k) {
        int n = bloom.length;
        int[] f = new int[n + 1];
        for (int i = 1; i <= n; i++) {
            f[i] = f[i - 1];
            if (i >= k) {
                boolean ok = true;
                for (int j = i - k; j < i; j++) ok &= bloom[j] <= day;
                if (ok) f[i] = Math.max(f[i], f[i - k] + 1);
            }
        }
        return f[n];
    }

    static int bruteMinDays(int[] bloom, int m, int k) {
        int max = 0;
        for (int b : bloom) max = Math.max(max, b);
        for (int day = 1; day <= max; day++) if (bruteBouquets(bloom, day, k) >= m) return day;
        return -1;
    }

    // LC 1552, quoted in the variants: largest x with ok(x) = smallest failing x, minus 1.
    static int maxDistance(int[] pos, int m) {
        int[] p = pos.clone();
        Arrays.sort(p);
        long span = p[p.length - 1] - p[0];
        return (int) smallestOk(1, span + 1, gap -> !fits(p, gap, m)) - 1;   // span+1 fails
    }

    static boolean fits(int[] p, long gap, int m) {
        int placed = 1;
        long last = p[0];
        for (int x : p) if (x - last >= gap) { placed++; last = x; }
        return placed >= m;
    }

    static int bruteMaxDistance(int[] pos, int m) {   // try every subset of size m
        int n = pos.length, best = 0;
        for (int mask = 0; mask < (1 << n); mask++) {
            if (Integer.bitCount(mask) != m) continue;
            List<Integer> v = new ArrayList<>();
            for (int i = 0; i < n; i++) if ((mask >> i & 1) == 1) v.add(pos[i]);
            Collections.sort(v);
            int g = Integer.MAX_VALUE;
            for (int i = 1; i < v.size(); i++) g = Math.min(g, v.get(i) - v.get(i - 1));
            best = Math.max(best, g);
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(35);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(9);
            int[] piles = Check.ints(r, n, 1, 30);
            int h = n + r.nextInt(3 * n);
            String ctx = Arrays.toString(piles) + " h=" + h;
            Check.eq(bruteKoko(piles, h), minEatingSpeed(piles, h), "koko " + ctx);
            int d = 1 + r.nextInt(n);
            Check.eq(bruteSplit(piles, d), shipWithinDays(piles, d), "ship " + ctx + " d=" + d);
            int[] bloom = Check.ints(r, n, 1, 12);
            int m = 1 + r.nextInt(4), k = 1 + r.nextInt(4);
            Check.eq(bruteMinDays(bloom, m, k), minDays(bloom, m, k),
                    "bloom " + Arrays.toString(bloom) + " m=" + m + " k=" + k);
            // generic shape against a linear scan
            long lo = r.nextInt(20), hi = lo + r.nextInt(20);
            final long cut = lo + r.nextInt((int) (hi - lo + 1));
            Check.eq(cut, smallestOk(lo, hi, x -> x >= cut), "shape");
            if (n >= 2) {
                Set<Integer> s = new HashSet<>();
                while (s.size() < n) s.add(1 + r.nextInt(40));
                int[] pos = new int[n];
                int i = 0;
                for (int v : s) pos[i++] = v;
                int balls = 2 + r.nextInt(n - 1);
                Check.eq(bruteMaxDistance(pos, balls), maxDistance(pos, balls),
                        "magnetic " + Arrays.toString(pos) + " m=" + balls);
            }
        }
        Check.eq(3, maxDistance(new int[] {1, 2, 3, 4, 7}, 3), "LC 1552 sample 1");
        Check.eq(999999999, maxDistance(new int[] {5, 4, 3, 2, 1, 1000000000}, 2), "LC 1552 sample 2");
        // big ranges: values up to 1e9 and sums past 2^31
        int[] heavy = new int[50000];
        Arrays.fill(heavy, 1000000000);
        Check.eq(1000000000, minEatingSpeed(heavy, 50000), "koko big");
        Check.eq(50000, minEatingSpeed(heavy, 1000000000), "koko big h");   // hours pass 2^31
        Check.eq(25000L * 1000000000L, smallestOk(1000000000L, 50000L * 1000000000L,
                cap -> daysNeeded(heavy, cap) <= 2), "ship big");
        // LeetCode samples
        Check.eq(4, minEatingSpeed(new int[] {3, 6, 7, 11}, 8), "LC 875 sample 1");
        Check.eq(30, minEatingSpeed(new int[] {30, 11, 23, 4, 20}, 5), "LC 875 sample 2");
        Check.eq(23, minEatingSpeed(new int[] {30, 11, 23, 4, 20}, 6), "LC 875 sample 3");
        Check.eq(15, shipWithinDays(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 5), "LC 1011 sample 1");
        Check.eq(6, shipWithinDays(new int[] {3, 2, 2, 4, 1, 4}, 3), "LC 1011 sample 2");
        Check.eq(3, shipWithinDays(new int[] {1, 2, 3, 1, 1}, 4), "LC 1011 sample 3");
        Check.eq(18, shipWithinDays(new int[] {7, 2, 5, 10, 8}, 2), "LC 410 sample 1");
        Check.eq(9, shipWithinDays(new int[] {1, 2, 3, 4, 5}, 2), "LC 410 sample 2");
        Check.eq(3, minDays(new int[] {1, 10, 3, 10, 2}, 3, 1), "LC 1482 sample 1");
        Check.eq(-1, minDays(new int[] {1, 10, 3, 10, 2}, 3, 2), "LC 1482 sample 2");
        Check.eq(12, minDays(new int[] {7, 7, 7, 7, 12, 7, 7}, 2, 3), "LC 1482 sample 3");
        // the trap: starting the ship search below the heaviest package
        Check.isTrue(daysNeeded(new int[] {1, 9, 1}, 5) <= 3, "cap < max passes the check wrongly");
        Check.done("P03BsAnswer");
    }
}
