import java.util.*;

public class P10Knapsack01 {
    //@tpl core
    // 0/1 knapsack: each item at most once. best[c] = most value in weight <= c.
    static int knapsack(int[] w, int[] v, int cap) {
        int[] best = new int[cap + 1];
        for (int i = 0; i < w.length; i++)
            // reverse: best[c - w[i]] is not updated yet, so it means "without i"
            for (int c = cap; c >= w[i]; c--)
                best[c] = Math.max(best[c], best[c - w[i]] + v[i]);
        return best[cap];
    }

    // Partition equal subset sum: can some subset reach half the total?
    static boolean canPartition(int[] nums) {
        int total = 0;
        for (int x : nums) total += x;
        if (total % 2 == 1) return false;
        int half = total / 2;
        boolean[] can = new boolean[half + 1];         // can[s]: a subset sums to s
        can[0] = true;
        for (int x : nums)
            for (int s = half; s >= x; s--)
                if (can[s - x]) can[s] = true;
        return can[half];
    }
    //@end

    //@tpl bits
    // The same with bits: bit s of the long words = "sum s is reachable".
    // java.util.BitSet has no shift, so do bits |= bits << x by hand.
    static boolean canPartitionBits(int[] nums) {
        int total = 0;
        for (int x : nums) total += x;
        if (total % 2 == 1) return false;
        int half = total / 2;
        long[] bits = new long[half / 64 + 1];
        bits[0] = 1;                                   // sum 0
        for (int x : nums) {
            int ws = x / 64, bs = x % 64;              // whole words, then bits
            for (int i = bits.length - 1; i >= ws; i--) {     // high words first
                bits[i] |= bits[i - ws] << bs;
                if (bs > 0 && i > ws) bits[i] |= bits[i - ws - 1] >>> (64 - bs);
            }
        }
        return (bits[half / 64] >>> (half % 64) & 1) == 1;
    }
    //@end

    //@tpl count
    // Target sum: the '+' numbers P and the '-' numbers N give P - N = target and
    // P + N = total, so P = (total + target) / 2. Count subsets with sum P.
    static int findTargetSumWays(int[] nums, int target) {
        int total = 0;
        for (int x : nums) total += x;
        if (Math.abs(target) > total || (total + target) % 2 != 0) return 0;
        int p = (total + target) / 2;
        int[] ways = new int[p + 1];
        ways[0] = 1;
        for (int x : nums)                             // x = 0 doubles every count
            for (int s = p; s >= x; s--) ways[s] += ways[s - x];
        return ways[p];
    }

    // Ones and zeroes: two capacities (m zeros, n ones), both loops reversed.
    static int findMaxForm(String[] strs, int m, int n) {
        int[][] best = new int[m + 1][n + 1];
        for (String s : strs) {
            int zeros = 0, ones = 0;
            for (char ch : s.toCharArray()) if (ch == '0') zeros++; else ones++;
            for (int i = m; i >= zeros; i--)
                for (int j = n; j >= ones; j--)
                    best[i][j] = Math.max(best[i][j], best[i - zeros][j - ones] + 1);
        }
        return best[m][n];
    }
    //@end

    // Not printed: last stone weight II, as the variant bullet describes it.
    static int lastStoneWeightII(int[] stones) {
        int total = 0;
        for (int x : stones) total += x;
        boolean[] can = new boolean[total / 2 + 1];
        can[0] = true;
        for (int x : stones)
            for (int s = total / 2; s >= x; s--) if (can[s - x]) can[s] = true;
        for (int s = total / 2; ; s--) if (can[s]) return total - 2 * s;
    }

    // Forward inner loop, to show the bug in the text.
    static int knapsackForward(int[] w, int[] v, int cap) {
        int[] best = new int[cap + 1];
        for (int i = 0; i < w.length; i++)
            for (int c = w[i]; c <= cap; c++) best[c] = Math.max(best[c], best[c - w[i]] + v[i]);
        return best[cap];
    }

    // ---------------- tests (not printed) ----------------
    static int bruteKnap(int[] w, int[] v, int cap) {
        int best = 0;
        for (int mask = 0; mask < (1 << w.length); mask++) {
            int sw = 0, sv = 0;
            for (int i = 0; i < w.length; i++) if ((mask >> i & 1) == 1) { sw += w[i]; sv += v[i]; }
            if (sw <= cap) best = Math.max(best, sv);
        }
        return best;
    }

    static boolean brutePartition(int[] a) {
        int total = 0;
        for (int x : a) total += x;
        for (int mask = 0; mask < (1 << a.length); mask++) {
            int s = 0;
            for (int i = 0; i < a.length; i++) if ((mask >> i & 1) == 1) s += a[i];
            if (2 * s == total) return true;
        }
        return false;
    }

    static int bruteSigns(int[] a, int i, int sum, int target) {
        if (i == a.length) return sum == target ? 1 : 0;
        return bruteSigns(a, i + 1, sum + a[i], target) + bruteSigns(a, i + 1, sum - a[i], target);
    }

    static int bruteForm(String[] strs, int m, int n) {
        int best = 0;
        for (int mask = 0; mask < (1 << strs.length); mask++) {
            int z = 0, o = 0, k = 0;
            for (int i = 0; i < strs.length; i++)
                if ((mask >> i & 1) == 1) {
                    k++;
                    for (char ch : strs[i].toCharArray()) if (ch == '0') z++; else o++;
                }
            if (z <= m && o <= n) best = Math.max(best, k);
        }
        return best;
    }

    // Play the stone game: smash every pair in every order.
    static int bruteStones(List<Integer> st) {
        if (st.size() <= 1) return st.isEmpty() ? 0 : st.get(0);
        int best = Integer.MAX_VALUE;
        for (int i = 0; i < st.size(); i++)
            for (int j = i + 1; j < st.size(); j++) {
                List<Integer> next = new ArrayList<>();
                for (int k = 0; k < st.size(); k++) if (k != i && k != j) next.add(st.get(k));
                int d = Math.abs(st.get(i) - st.get(j));
                if (d > 0) next.add(d);
                best = Math.min(best, bruteStones(next));
            }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(1004);
        for (int t = 0; t < 3000; t++) {
            int n = r.nextInt(11);
            int[] w = Check.ints(r, n, 1, 9), v = Check.ints(r, n, 0, 20);
            int cap = r.nextInt(30);
            Check.eq(bruteKnap(w, v, cap), knapsack(w, v, cap), "knap " + Arrays.toString(w) + Arrays.toString(v) + cap);

            int[] a = Check.ints(r, 1 + r.nextInt(11), 1, t % 3 == 0 ? 200 : 12);
            boolean want = brutePartition(a);
            Check.eq(want, canPartition(a), "partition " + Arrays.toString(a));
            Check.eq(want, canPartitionBits(a), "bits " + Arrays.toString(a));

            int[] s = Check.ints(r, 1 + r.nextInt(10), 0, 6);
            int target = r.nextInt(31) - 15;
            Check.eq(bruteSigns(s, 0, 0, target), findTargetSumWays(s, target), "target " + Arrays.toString(s) + " " + target);

            String[] strs = new String[r.nextInt(9)];
            for (int i = 0; i < strs.length; i++) strs[i] = Check.str(r, 1 + r.nextInt(4), "01");
            int zm = r.nextInt(8), on = r.nextInt(8);
            Check.eq(bruteForm(strs, zm, on), findMaxForm(strs, zm, on), "form " + Arrays.toString(strs));

            if (t % 6 == 0) {
                int[] st = Check.ints(r, 1 + r.nextInt(6), 1, 15);
                List<Integer> list = new ArrayList<>();
                for (int x : st) list.add(x);
                Check.eq(bruteStones(list), lastStoneWeightII(st), "stones " + Arrays.toString(st));
            }
        }
        // big values for the word-shift path (x >= 64)
        for (int t = 0; t < 300; t++) {
            int[] a = Check.ints(r, 1 + r.nextInt(12), 1, 1000);
            Check.eq(brutePartition(a), canPartitionBits(a), "bits big " + Arrays.toString(a));
            Check.eq(brutePartition(a), canPartition(a), "partition big " + Arrays.toString(a));
        }
        Check.eq(9, knapsack(new int[] {1, 3, 4, 5}, new int[] {1, 4, 5, 7}, 7), "doc example");
        Check.eq(true, canPartition(new int[] {1, 5, 11, 5}), "LC 416 sample 1");
        Check.eq(false, canPartition(new int[] {1, 2, 3, 5}), "LC 416 sample 2");
        Check.eq(true, canPartitionBits(new int[] {1, 5, 11, 5}), "LC 416 sample 1 bits");
        Check.eq(false, canPartitionBits(new int[] {1, 2, 3, 5}), "LC 416 sample 2 bits");
        Check.eq(5, findTargetSumWays(new int[] {1, 1, 1, 1, 1}, 3), "LC 494 sample 1");
        Check.eq(1, findTargetSumWays(new int[] {1}, 1), "LC 494 sample 2");
        Check.eq(4, findMaxForm(new String[] {"10", "0001", "111001", "1", "0"}, 5, 3), "LC 474 sample 1");
        Check.eq(2, findMaxForm(new String[] {"10", "0", "1"}, 1, 1), "LC 474 sample 2");
        Check.eq(1, lastStoneWeightII(new int[] {2, 7, 4, 1, 8, 1}), "LC 1049 sample 1");
        Check.eq(5, lastStoneWeightII(new int[] {31, 26, 33, 21, 40}), "LC 1049 sample 2");
        Check.eq(7, knapsackForward(new int[] {1}, new int[] {1}, 7), "forward loop reuses item");
        Check.done("P10Knapsack01");
    }
}
