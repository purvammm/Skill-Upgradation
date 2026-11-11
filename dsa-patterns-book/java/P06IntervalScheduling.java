import java.util.*;

public class P06IntervalScheduling {
    //@tpl erase
    // Fewest intervals to remove so the rest do not overlap (LC 435).
    // Here [1, 2] and [2, 3] do not overlap, so a start equal to lastEnd is fine.
    static int eraseOverlapIntervals(int[][] iv) {
        Arrays.sort(iv, (x, y) -> Integer.compare(x[1], y[1]));   // by END
        int kept = 0;
        long lastEnd = Long.MIN_VALUE;
        for (int[] cur : iv) {
            if (cur[0] >= lastEnd) {     // compatible with everything kept: keep it
                kept++;
                lastEnd = cur[1];
            }                            // else drop it: it ends no earlier
        }
        return iv.length - kept;
    }
    //@end

    //@tpl arrows
    // Fewest arrows so every balloon [s, e] contains one (LC 452).
    // Balloons that touch share a point, so a new arrow is needed only if s > arrow.
    static int findMinArrowShots(int[][] b) {
        Arrays.sort(b, (x, y) -> Integer.compare(x[1], y[1]));    // not x[1] - y[1]
        int arrows = 0;
        long arrow = Long.MIN_VALUE;
        for (int[] cur : b) {
            if (cur[0] > arrow) {        // not burst yet: shoot at its end
                arrows++;
                arrow = cur[1];
            }
        }
        return arrows;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int[][] copy(int[][] a) {
        int[][] c = new int[a.length][];
        for (int i = 0; i < a.length; i++) c[i] = a[i].clone();
        return c;
    }

    // Most intervals with no two overlapping (touching allowed), over all subsets.
    static int bruteErase(int[][] iv) {
        int n = iv.length, best = 0;
        for (int mask = 0; mask < (1 << n); mask++) {
            boolean ok = true;
            for (int i = 0; i < n && ok; i++)
                for (int j = i + 1; j < n && ok; j++)
                    if ((mask >> i & 1) == 1 && (mask >> j & 1) == 1
                            && Math.max(iv[i][0], iv[j][0]) < Math.min(iv[i][1], iv[j][1])) ok = false;
            if (ok) best = Math.max(best, Integer.bitCount(mask));
        }
        return n - best;
    }

    // Fewest integer points hitting every closed interval (integer points suffice).
    static int bruteArrows(int[][] b, int maxC) {
        int m = maxC + 1, best = Integer.MAX_VALUE;
        for (int mask = 0; mask < (1 << m); mask++) {
            int k = Integer.bitCount(mask);
            if (k >= best) continue;
            boolean ok = true;
            for (int[] x : b) {
                boolean hit = false;
                for (int p = x[0]; p <= x[1] && !hit; p++) if ((mask >> p & 1) == 1) hit = true;
                if (!hit) { ok = false; break; }
            }
            if (ok) best = k;
        }
        return best;
    }

    // Longest chain with c > b for [a, b] -> [c, d], by DP over pairs sorted by start.
    static int bruteChain(int[][] p) {
        int[][] s = copy(p);
        Arrays.sort(s, (x, y) -> Integer.compare(x[0], y[0]));
        int n = s.length, best = 0;
        int[] dp = new int[n];
        for (int i = 0; i < n; i++) {
            dp[i] = 1;
            for (int j = 0; j < i; j++) if (s[j][1] < s[i][0]) dp[i] = Math.max(dp[i], dp[j] + 1);
            best = Math.max(best, dp[i]);
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(63);
        for (int t = 0; t < 3000; t++) {
            int n = r.nextInt(9);
            int[][] iv = new int[n][];
            for (int i = 0; i < n; i++) {
                int s = r.nextInt(10);
                iv[i] = new int[] {s, s + 1 + r.nextInt(4)};    // LC 435: start < end
            }
            Check.eq(bruteErase(iv), eraseOverlapIntervals(copy(iv)), "erase " + Check.str(iv));
            int m = r.nextInt(7);
            int[][] b = new int[m][];
            for (int i = 0; i < m; i++) {
                int s = r.nextInt(9);
                b[i] = new int[] {s, Math.min(10, s + r.nextInt(4))};
            }
            Check.eq(bruteArrows(b, 10), findMinArrowShots(copy(b)), "arrows " + Check.str(b));
            // the variant in the text: pair chain is the arrows loop (strict >)
            if (m > 0) Check.eq(bruteChain(b), findMinArrowShots(copy(b)), "chain " + Check.str(b));
        }
        Check.eq(1, eraseOverlapIntervals(new int[][] {{1, 2}, {2, 3}, {3, 4}, {1, 3}}), "LC 435 sample 1");
        Check.eq(2, eraseOverlapIntervals(new int[][] {{1, 2}, {1, 2}, {1, 2}}), "LC 435 sample 2");
        Check.eq(0, eraseOverlapIntervals(new int[][] {{1, 2}, {2, 3}}), "LC 435 sample 3");
        Check.eq(2, findMinArrowShots(new int[][] {{10, 16}, {2, 8}, {1, 6}, {7, 12}}), "LC 452 sample 1");
        Check.eq(4, findMinArrowShots(new int[][] {{1, 2}, {3, 4}, {5, 6}, {7, 8}}), "LC 452 sample 2");
        Check.eq(2, findMinArrowShots(new int[][] {{1, 2}, {2, 3}, {3, 4}, {4, 5}}), "LC 452 sample 3");
        Check.eq(2, findMinArrowShots(new int[][] {{-2147483646, -2147483645}, {2147483646, 2147483647}}),
                 "LC 452 overflow case");
        Check.eq(1, findMinArrowShots(new int[][] {{-2147483648, 2147483647}}), "full int range");
        Check.eq(2, findMinArrowShots(new int[][] {{1, 2}, {2, 3}, {3, 4}}), "LC 646 sample 1 (chain)");
        Check.eq(3, findMinArrowShots(new int[][] {{1, 2}, {7, 8}, {4, 5}}), "LC 646 sample 2 (chain)");
        Check.done("P06IntervalScheduling");
    }
}
