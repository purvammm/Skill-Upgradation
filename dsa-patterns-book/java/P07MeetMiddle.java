import java.util.*;

public class P07MeetMiddle {
    //@tpl sums
    // All 2^k subset sums of a[from..to). sums[mask] extends the sum of mask
    // without its lowest bit, so each sum costs O(1).
    static long[] subsetSums(int[] a, int from, int to) {
        int k = to - from;
        long[] sums = new long[1 << k];
        for (int mask = 1; mask < (1 << k); mask++) {
            int low = Integer.numberOfTrailingZeros(mask);
            sums[mask] = sums[mask & (mask - 1)] + a[from + low];
        }
        return sums;
    }
    //@end

    //@tpl closest
    // Closest subsequence sum (n <= 40): smallest |sum - goal| over all subsets.
    static int minAbsDifference(int[] a, int goal) {
        int h = a.length / 2;
        long[] left = subsetSums(a, 0, h), right = subsetSums(a, h, a.length);
        Arrays.sort(left);
        Arrays.sort(right);
        long best = Long.MAX_VALUE;
        int i = 0, j = right.length - 1;        // left goes up, right comes down
        while (i < left.length && j >= 0) {
            long sum = left[i] + right[j];
            best = Math.min(best, Math.abs(sum - goal));
            if (sum < goal) i++;                // need a bigger sum
            else if (sum > goal) j--;           // need a smaller sum
            else return 0;
        }
        return (int) best;
    }
    //@end

    //@tpl partition
    // Split 2n numbers into two groups of n with the smallest |sumA - sumB|.
    // Group A takes k numbers from the left half and n - k from the right half.
    static int minimumDifference(int[] a) {
        int n = a.length / 2;
        long total = 0;
        for (int v : a) total += v;
        List<List<Long>> left = byCount(a, 0, n), right = byCount(a, n, n);
        long best = Long.MAX_VALUE;                // |sumA - sumB| = |total - 2 sumA|
        for (int k = 0; k <= n; k++) {
            List<Long> r = right.get(n - k);
            for (long x : left.get(k)) {
                int lo = 0, hi = r.size();         // first y with 2(x + y) >= total
                while (lo < hi) {
                    int mid = (lo + hi) >>> 1;
                    if (2 * (x + r.get(mid)) >= total) hi = mid;
                    else lo = mid + 1;
                }
                if (lo < r.size()) best = Math.min(best, 2 * (x + r.get(lo)) - total);
                if (lo > 0) best = Math.min(best, total - 2 * (x + r.get(lo - 1)));
            }
        }
        return (int) best;
    }

    // Sums of a[from..from+k), grouped by how many numbers they use, each sorted.
    static List<List<Long>> byCount(int[] a, int from, int k) {
        long[] sums = subsetSums(a, from, from + k);
        List<List<Long>> g = new ArrayList<>();
        for (int c = 0; c <= k; c++) g.add(new ArrayList<>());
        for (int mask = 0; mask < sums.length; mask++)
            g.get(Integer.bitCount(mask)).add(sums[mask]);
        for (List<Long> list : g) Collections.sort(list);
        return g;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static long bruteClosest(int[] a, int goal) {
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << a.length); mask++) {
            long s = 0;
            for (int i = 0; i < a.length; i++) if ((mask >> i & 1) == 1) s += a[i];
            best = Math.min(best, Math.abs(s - goal));
        }
        return best;
    }

    static long brutePartition(int[] a) {
        int m = a.length;
        long total = 0, best = Long.MAX_VALUE;
        for (int v : a) total += v;
        for (int mask = 0; mask < (1 << m); mask++) {
            if (Integer.bitCount(mask) != m / 2) continue;
            long s = 0;
            for (int i = 0; i < m; i++) if ((mask >> i & 1) == 1) s += a[i];
            best = Math.min(best, Math.abs(total - 2 * s));
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(78);
        for (int t = 0; t < 3000; t++) {
            int n = r.nextInt(15);
            int[] a = Check.ints(r, n, -30, 30);
            int goal = r.nextInt(201) - 100;
            Check.eq(bruteClosest(a, goal), minAbsDifference(a, goal), "closest " + Arrays.toString(a) + " g=" + goal);
            int half = 1 + r.nextInt(6);
            int[] b = Check.ints(r, 2 * half, -50, 50);
            Check.eq(brutePartition(b), minimumDifference(b), "partition " + Arrays.toString(b));
        }
        // large values: sums need long
        for (int t = 0; t < 200; t++) {
            int[] a = Check.ints(r, 1 + r.nextInt(14), -10000000, 10000000);
            int goal = r.nextInt(2000000001) - 1000000000;
            Check.eq(bruteClosest(a, goal), minAbsDifference(a, goal), "closest big " + Arrays.toString(a));
            int[] b = Check.ints(r, 2 * (1 + r.nextInt(6)), -10000000, 10000000);
            Check.eq(brutePartition(b), minimumDifference(b), "partition big " + Arrays.toString(b));
        }
        // full size: 40 numbers and 30 numbers must run fast
        int[] big = Check.ints(r, 40, -10000000, 10000000);
        long t0 = System.nanoTime();
        int got = minAbsDifference(big, 123456789);
        Check.isTrue(got >= 0, "n = 40 runs");
        int[] big2 = Check.ints(r, 30, -10000000, 10000000);
        Check.isTrue(minimumDifference(big2) >= 0, "2n = 30 runs");
        Check.isTrue(System.nanoTime() - t0 < 5_000_000_000L, "full size under 5 s");
        Check.eq(0, minAbsDifference(new int[] {5, -7, 3, 5}, 6), "LC 1755 sample 1");
        Check.eq(1, minAbsDifference(new int[] {7, -9, 15, -2}, -5), "LC 1755 sample 2");
        Check.eq(7, minAbsDifference(new int[] {1, 2, 3}, -7), "LC 1755 sample 3");
        Check.eq(2, minimumDifference(new int[] {3, 9, 7, 3}), "LC 2035 sample 1");
        Check.eq(72, minimumDifference(new int[] {-36, 36}), "LC 2035 sample 2");
        Check.eq(0, minimumDifference(new int[] {2, -1, 0, 4, -2, -9}), "LC 2035 sample 3");
        Check.done("P07MeetMiddle");
    }
}
