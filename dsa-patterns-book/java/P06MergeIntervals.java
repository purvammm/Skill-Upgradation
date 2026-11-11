import java.util.*;

public class P06MergeIntervals {
    //@tpl merge
    // Merge closed intervals [start, end]. Touching ones ([1, 4], [4, 5]) merge.
    static int[][] merge(int[][] intervals) {
        Arrays.sort(intervals, (x, y) -> Integer.compare(x[0], y[0]));  // 1. by start
        List<int[]> out = new ArrayList<>();
        for (int[] cur : intervals) {
            int[] last = out.isEmpty() ? null : out.get(out.size() - 1);
            if (last != null && cur[0] <= last[1]) {
                last[1] = Math.max(last[1], cur[1]);     // 2. overlaps: extend
            } else {
                out.add(new int[] {cur[0], cur[1]});     // 3. gap: start a new one
            }
        }
        return out.toArray(new int[0][]);
    }
    //@end

    //@tpl covered
    // Remove covered intervals: count the ones not inside another interval.
    // Equal starts: longer first, so a container is seen before what it covers.
    static int removeCoveredIntervals(int[][] iv) {
        Arrays.sort(iv, (x, y) -> x[0] != y[0] ? Integer.compare(x[0], y[0])
                                               : Integer.compare(y[1], x[1]));
        int kept = 0, maxEnd = Integer.MIN_VALUE;
        for (int[] cur : iv) {
            if (cur[1] > maxEnd) {          // no earlier interval reaches this far
                kept++;
                maxEnd = cur[1];
            }
        }
        return kept;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int[][] copy(int[][] a) {
        int[][] c = new int[a.length][];
        for (int i = 0; i < a.length; i++) c[i] = a[i].clone();
        return c;
    }

    // Mark doubled coordinates 2s..2e, then read off the runs. Closed intervals
    // [1, 3] and [4, 5] stay apart (point 7 is empty); [1, 4] and [4, 5] join.
    static int[][] bruteMerge(int[][] iv, int maxCoord) {
        boolean[] on = new boolean[2 * maxCoord + 3];
        for (int[] x : iv) for (int p = 2 * x[0]; p <= 2 * x[1]; p++) on[p] = true;
        List<int[]> out = new ArrayList<>();
        for (int p = 0; p < on.length; p++) {
            if (!on[p]) continue;
            int q = p;
            while (q + 1 < on.length && on[q + 1]) q++;
            out.add(new int[] {p / 2, q / 2});
            p = q;
        }
        return out.toArray(new int[0][]);
    }

    static int bruteCovered(int[][] iv) {
        int kept = 0;
        for (int i = 0; i < iv.length; i++) {
            boolean covered = false;
            for (int j = 0; j < iv.length; j++)
                if (i != j && iv[j][0] <= iv[i][0] && iv[i][1] <= iv[j][1]) covered = true;
            if (!covered) kept++;
        }
        return kept;
    }

    public static void main(String[] args) {
        Random r = new Random(61);
        for (int t = 0; t < 5000; t++) {
            int n = r.nextInt(9);
            int[][] iv = new int[n][];
            for (int i = 0; i < n; i++) {
                int s = r.nextInt(16);
                iv[i] = new int[] {s, s + r.nextInt(5)};
            }
            Check.eq(bruteMerge(iv, 20), merge(copy(iv)), "merge " + Check.str(iv));
            // LC 1288 promises distinct intervals
            Set<String> seen = new HashSet<>();
            List<int[]> uniq = new ArrayList<>();
            for (int[] x : iv) if (seen.add(x[0] + "," + x[1])) uniq.add(x);
            int[][] u = uniq.toArray(new int[0][]);
            Check.eq(bruteCovered(u), removeCoveredIntervals(copy(u)), "covered " + Check.str(u));
        }
        Check.eq(new int[][] {{1, 6}, {8, 10}, {15, 18}},
                 merge(new int[][] {{1, 3}, {2, 6}, {8, 10}, {15, 18}}), "LC 56 sample 1");
        Check.eq(new int[][] {{1, 5}}, merge(new int[][] {{1, 4}, {4, 5}}), "LC 56 sample 2");
        Check.eq(new int[][] {{1, 7}, {8, 10}, {15, 18}},
                 merge(new int[][] {{8, 10}, {1, 3}, {2, 6}, {15, 18}, {6, 7}}), "doc example");
        Check.eq(2, removeCoveredIntervals(new int[][] {{1, 4}, {3, 6}, {2, 8}}), "LC 1288 sample 1");
        Check.eq(1, removeCoveredIntervals(new int[][] {{1, 4}, {2, 3}}), "LC 1288 sample 2");
        Check.done("P06MergeIntervals");
    }
}
