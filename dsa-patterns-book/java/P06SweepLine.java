import java.util.*;

public class P06SweepLine {
    //@tpl overlap
    // Most closed intervals [s, e] covering one point = fewest groups (LC 2406).
    static int maxOverlap(int[][] iv) {
        int n = iv.length;
        int[][] ev = new int[2 * n][];
        for (int i = 0; i < n; i++) {
            ev[2 * i] = new int[] {iv[i][0], +1};       // starts here
            ev[2 * i + 1] = new int[] {iv[i][1], -1};   // ends here
        }
        // By position. Ties: +1 first, because [1, 5] and [5, 8] share point 5.
        // For half-open [s, e) put -1 first instead.
        Arrays.sort(ev, (x, y) -> x[0] != y[0] ? Integer.compare(x[0], y[0])
                                               : Integer.compare(y[1], x[1]));
        int open = 0, best = 0;
        for (int[] e : ev) {
            open += e[1];                               // intervals covering e[0]
            best = Math.max(best, open);
        }
        return best;
    }
    //@end

    //@tpl carpool
    // Car pooling: trip = {people, from, to}; they leave the car at `to`.
    // Summing all changes at one stop puts drop-offs before pick-ups for free.
    static boolean carPooling(int[][] trips, int capacity) {
        TreeMap<Integer, Integer> change = new TreeMap<>();
        for (int[] t : trips) {
            change.merge(t[1], t[0], Integer::sum);
            change.merge(t[2], -t[0], Integer::sum);
        }
        int load = 0;
        for (int d : change.values()) {                 // stops in increasing order
            load += d;
            if (load > capacity) return false;
        }
        return true;
    }
    //@end

    //@tpl skyline
    // Skyline: building = {left, right, height}. Live heights in a TreeMap
    // (height -> count). After all events at x, the top changed => key point.
    static List<List<Integer>> getSkyline(int[][] buildings) {
        List<int[]> ev = new ArrayList<>();
        for (int[] b : buildings) {
            ev.add(new int[] {b[0], b[2], +1});         // height starts at left
            ev.add(new int[] {b[1], b[2], -1});         // and stops at right
        }
        ev.sort((p, q) -> Integer.compare(p[0], q[0]));
        TreeMap<Integer, Integer> live = new TreeMap<>();
        live.put(0, 1);                                 // the ground, never removed
        List<List<Integer>> out = new ArrayList<>();
        int prev = 0;
        for (int i = 0; i < ev.size(); ) {
            int x = ev.get(i)[0];
            for (; i < ev.size() && ev.get(i)[0] == x; i++) {    // every event at x
                int h = ev.get(i)[1];
                if (ev.get(i)[2] == 1) live.merge(h, 1, Integer::sum);
                else if (live.merge(h, -1, Integer::sum) == 0) live.remove(h);
            }
            int top = live.lastKey();
            if (top != prev) {
                out.add(Arrays.asList(x, top));
                prev = top;
            }
        }
        return out;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int bruteOverlap(int[][] iv) {
        int best = 0;
        for (int x = -1; x <= 25; x++) {
            int c = 0;
            for (int[] v : iv) if (v[0] <= x && x <= v[1]) c++;
            best = Math.max(best, c);
        }
        return best;
    }

    static boolean bruteCar(int[][] trips, int cap) {
        for (int x = 0; x <= 25; x++) {
            int load = 0;
            for (int[] t : trips) if (t[1] <= x && x < t[2]) load += t[0];
            if (load > cap) return false;
        }
        return true;
    }

    // Height on [x, x + 1) for every integer x; a key point wherever it changes.
    static List<List<Integer>> bruteSkyline(int[][] bs) {
        List<List<Integer>> out = new ArrayList<>();
        int prev = 0;
        for (int x = 0; x <= 26; x++) {
            int h = 0;
            for (int[] b : bs) if (b[0] <= x && x < b[1]) h = Math.max(h, b[2]);
            if (h != prev) {
                out.add(Arrays.asList(x, h));
                prev = h;
            }
        }
        return out;
    }

    public static void main(String[] args) {
        Random r = new Random(64);
        for (int t = 0; t < 4000; t++) {
            int n = r.nextInt(9);
            int[][] iv = new int[n][];
            for (int i = 0; i < n; i++) {
                int s = r.nextInt(15);
                iv[i] = new int[] {s, s + r.nextInt(6)};
            }
            Check.eq(bruteOverlap(iv), maxOverlap(iv), "overlap " + Check.str(iv));
            int[][] trips = new int[n][];
            for (int i = 0; i < n; i++) {
                int from = r.nextInt(15);
                trips[i] = new int[] {1 + r.nextInt(4), from, from + 1 + r.nextInt(6)};
            }
            int cap = 1 + r.nextInt(10);
            Check.eq(bruteCar(trips, cap), carPooling(trips, cap), "car " + Check.str(trips) + " cap=" + cap);
            int[][] bs = new int[n][];
            for (int i = 0; i < n; i++) {
                int left = r.nextInt(15);
                bs[i] = new int[] {left, left + 1 + r.nextInt(8), 1 + r.nextInt(6)};
            }
            Arrays.sort(bs, (p, q) -> Integer.compare(p[0], q[0]));   // LC sorts by left
            Check.eq(bruteSkyline(bs), getSkyline(bs), "skyline " + Check.str(bs));
        }
        Check.eq(3, maxOverlap(new int[][] {{5, 10}, {6, 8}, {1, 5}, {2, 3}, {1, 10}}), "LC 2406 sample 1");
        Check.eq(1, maxOverlap(new int[][] {{1, 3}, {5, 6}, {8, 10}, {11, 13}}), "LC 2406 sample 2");
        Check.eq(false, carPooling(new int[][] {{2, 1, 5}, {3, 3, 7}}, 4), "LC 1094 sample 1");
        Check.eq(true, carPooling(new int[][] {{2, 1, 5}, {3, 3, 7}}, 5), "LC 1094 sample 2");
        List<List<Integer>> want = new ArrayList<>();
        int[][] pts = {{2, 10}, {3, 15}, {7, 12}, {12, 0}, {15, 10}, {20, 8}, {24, 0}};
        for (int[] p : pts) want.add(Arrays.asList(p[0], p[1]));
        Check.eq(want, getSkyline(new int[][] {{2, 9, 10}, {3, 7, 15}, {5, 12, 12}, {15, 20, 10}, {19, 24, 8}}),
                 "LC 218 sample 1");
        want = new ArrayList<>();
        want.add(Arrays.asList(0, 3));
        want.add(Arrays.asList(5, 0));
        Check.eq(want, getSkyline(new int[][] {{0, 2, 3}, {2, 5, 3}}), "LC 218 sample 2");
        Check.done("P06SweepLine");
    }
}
