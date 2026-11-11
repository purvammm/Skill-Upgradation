import java.util.*;

public class P06InsertInterval {
    //@tpl insert
    // intervals: sorted by start and disjoint. Insert [s, e], merging overlaps.
    static int[][] insert(int[][] intervals, int[] add) {
        List<int[]> out = new ArrayList<>();
        int i = 0, n = intervals.length, s = add[0], e = add[1];
        while (i < n && intervals[i][1] < s)        // 1. ends before the new one
            out.add(intervals[i++]);
        while (i < n && intervals[i][0] <= e) {     // 2. overlaps: absorb it
            s = Math.min(s, intervals[i][0]);
            e = Math.max(e, intervals[i][1]);
            i++;
        }
        out.add(new int[] {s, e});
        while (i < n)                               // 3. starts after the new one
            out.add(intervals[i++]);
        return out.toArray(new int[0][]);
    }
    //@end

    //@tpl intersect
    // Intersections of two sorted, disjoint lists of closed intervals.
    static int[][] intervalIntersection(int[][] a, int[][] b) {
        List<int[]> out = new ArrayList<>();
        int i = 0, j = 0;
        while (i < a.length && j < b.length) {
            int lo = Math.max(a[i][0], b[j][0]);
            int hi = Math.min(a[i][1], b[j][1]);
            if (lo <= hi) out.add(new int[] {lo, hi});
            if (a[i][1] < b[j][1]) i++;    // a[i] ends first: it cannot meet b[j+1]
            else j++;
        }
        return out.toArray(new int[0][]);
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static final int MAXC = 60;

    // Sorted, pairwise disjoint closed intervals with a gap of at least 1.
    static int[][] randomList(Random r) {
        int n = r.nextInt(6);
        List<int[]> out = new ArrayList<>();
        int pos = r.nextInt(3) - 1;
        for (int k = 0; k < n; k++) {
            int s = pos + 1 + r.nextInt(4);
            int e = s + r.nextInt(5);
            out.add(new int[] {s, e});
            pos = e;
        }
        return out.toArray(new int[0][]);
    }

    static boolean[] mark(int[][] iv) {
        boolean[] on = new boolean[2 * MAXC + 3];
        for (int[] x : iv) for (int p = 2 * x[0]; p <= 2 * x[1]; p++) on[p] = true;
        return on;
    }

    static int[][] runs(boolean[] on) {
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

    public static void main(String[] args) {
        Random r = new Random(62);
        for (int t = 0; t < 5000; t++) {
            int[][] a = randomList(r), b = randomList(r);
            int s = r.nextInt(30);
            int[] add = {s, s + r.nextInt(8)};
            int[][] all = Arrays.copyOf(a, a.length + 1);
            all[a.length] = add;
            Check.eq(runs(mark(all)), insert(a, add.clone()), "insert " + Check.str(a) + " + " + Check.str(add));
            boolean[] ma = mark(a), mb = mark(b), both = new boolean[ma.length];
            for (int p = 0; p < ma.length; p++) both[p] = ma[p] && mb[p];
            Check.eq(runs(both), intervalIntersection(a, b), "intersect " + Check.str(a) + " " + Check.str(b));
        }
        Check.eq(new int[][] {{1, 5}, {6, 9}},
                 insert(new int[][] {{1, 3}, {6, 9}}, new int[] {2, 5}), "LC 57 sample 1");
        Check.eq(new int[][] {{1, 2}, {3, 10}, {12, 16}},
                 insert(new int[][] {{1, 2}, {3, 5}, {6, 7}, {8, 10}, {12, 16}}, new int[] {4, 8}),
                 "LC 57 sample 2");
        Check.eq(new int[][] {{5, 7}}, insert(new int[0][], new int[] {5, 7}), "empty list");
        Check.eq(new int[][] {{1, 2}, {5, 5}, {8, 10}, {15, 23}, {24, 24}, {25, 25}},
                 intervalIntersection(new int[][] {{0, 2}, {5, 10}, {13, 23}, {24, 25}},
                                      new int[][] {{1, 5}, {8, 12}, {15, 24}, {25, 26}}),
                 "LC 986 sample 1");
        Check.eq(new int[0][], intervalIntersection(new int[][] {{1, 3}, {5, 9}}, new int[0][]),
                 "LC 986 sample 2");
        Check.done("P06InsertInterval");
    }
}
