import java.util.*;

public class P04HeapGreedy {
    //@tpl ipo
    // IPO: finish at most k projects, starting with capital w.
    // Sort by the gate (capital needed); a max-heap ranks what you can afford.
    static int findMaximizedCapital(int k, int w, int[] profits, int[] capital) {
        int n = profits.length;
        int[][] p = new int[n][];
        for (int i = 0; i < n; i++) p[i] = new int[] {capital[i], profits[i]};
        Arrays.sort(p, (x, y) -> Integer.compare(x[0], y[0]));
        PriorityQueue<Integer> heap = new PriorityQueue<>(Collections.reverseOrder());
        int next = 0;
        for (int round = 0; round < k; round++) {
            while (next < n && p[next][0] <= w)
                heap.offer(p[next++][1]);            // unlocked: wait in the heap
            if (heap.isEmpty()) break;               // nothing affordable
            w += heap.poll();                        // best profit available now
        }
        return w;
    }
    //@end

    //@tpl regret
    // Course schedule III: {duration, lastDay}. Take courses by deadline; if
    // the total time passes it, drop the longest course taken (max-heap).
    static int scheduleCourse(int[][] courses) {
        Arrays.sort(courses, (x, y) -> Integer.compare(x[1], y[1]));
        PriorityQueue<Integer> taken =
            new PriorityQueue<>(Collections.reverseOrder());   // durations
        int time = 0;
        for (int[] c : courses) {
            taken.offer(c[0]);
            time += c[0];
            if (time > c[1]) time -= taken.poll();   // same count, less time
        }
        return taken.size();
    }

    // Minimum refuelling stops: drive past stations and remember their fuel;
    // when you cannot go further, refuel (in hindsight) at the biggest one.
    static int minRefuelStops(int target, int fuel, int[][] stations) {
        PriorityQueue<Integer> passed =
            new PriorityQueue<>(Collections.reverseOrder());   // fuel
        long reach = fuel;
        int stops = 0, i = 0;
        while (reach < target) {
            while (i < stations.length && stations[i][0] <= reach)
                passed.offer(stations[i++][1]);
            if (passed.isEmpty()) return -1;         // stuck before the target
            reach += passed.poll();
            stops++;
        }
        return stops;
    }
    //@end

    //@tpl reorganize
    // Reorganize string: place the most frequent letter other than the last one.
    // The last letter waits outside the heap, where its count can change safely.
    static String reorganizeString(String s) {
        int[] cnt = new int[26];
        for (char c : s.toCharArray()) cnt[c - 'a']++;
        PriorityQueue<Integer> heap = new PriorityQueue<>((x, y) -> cnt[y] - cnt[x]);
        for (int c = 0; c < 26; c++) if (cnt[c] > 0) heap.offer(c);
        StringBuilder sb = new StringBuilder();
        int held = -1;                               // letter placed last
        while (!heap.isEmpty()) {
            int c = heap.poll();
            sb.append((char) ('a' + c));
            cnt[c]--;
            if (held >= 0 && cnt[held] > 0) heap.offer(held);
            held = c;
        }
        return sb.length() == s.length() ? sb.toString() : "";
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int bruteIpo(int k, int w, int[] profits, int[] capital, boolean[] used) {
        int best = w;
        if (k == 0) return best;
        for (int i = 0; i < profits.length; i++) {
            if (used[i] || capital[i] > w) continue;
            used[i] = true;
            best = Math.max(best, bruteIpo(k - 1, w + profits[i], profits, capital, used));
            used[i] = false;
        }
        return best;
    }

    static int bruteCourses(int[][] c, boolean[] used, int time) {
        int best = 0;
        for (int i = 0; i < c.length; i++) {
            if (used[i] || time + c[i][0] > c[i][1]) continue;
            used[i] = true;
            best = Math.max(best, 1 + bruteCourses(c, used, time + c[i][0]));
            used[i] = false;
        }
        return best;
    }

    static int bruteRefuel(int target, int fuel, int[][] st) {
        int n = st.length, best = -1;
        for (int mask = 0; mask < 1 << n; mask++) {
            long tank = fuel;
            int pos = 0;
            boolean ok = true;
            for (int i = 0; i < n && ok; i++) {
                if ((mask >> i & 1) == 0) continue;
                tank -= st[i][0] - pos;
                pos = st[i][0];
                if (tank < 0) ok = false;
                tank += st[i][1];
            }
            if (!ok || tank < target - pos) continue;
            int stops = Integer.bitCount(mask);
            if (best < 0 || stops < best) best = stops;
        }
        return best;
    }

    static boolean canArrange(int[] cnt, int last, int left) {
        if (left == 0) return true;
        for (int c = 0; c < 26; c++) {
            if (c == last || cnt[c] == 0) continue;
            cnt[c]--;
            boolean ok = canArrange(cnt, c, left - 1);
            cnt[c]++;
            if (ok) return true;
        }
        return false;
    }

    static int[][] copy(int[][] a) {
        int[][] b = new int[a.length][];
        for (int i = 0; i < a.length; i++) b[i] = a[i].clone();
        return b;
    }

    public static void main(String[] args) {
        Random r = new Random(50);
        for (int t = 0; t < 1500; t++) {
            int n = 1 + r.nextInt(6);
            int[] profits = Check.ints(r, n, 0, 9), capital = Check.ints(r, n, 0, 12);
            int k = 1 + r.nextInt(4), w = r.nextInt(5);
            Check.eq(bruteIpo(k, w, profits, capital, new boolean[n]), findMaximizedCapital(k, w, profits, capital),
                     "ipo k=" + k + " w=" + w + " p=" + Arrays.toString(profits) + " c=" + Arrays.toString(capital));

            int m = r.nextInt(7);
            int[][] courses = new int[m][];
            for (int i = 0; i < m; i++) { int d = 1 + r.nextInt(6); courses[i] = new int[] {d, d + r.nextInt(12)}; }
            Check.eq(bruteCourses(courses, new boolean[m], 0), scheduleCourse(copy(courses)), "courses " + Arrays.deepToString(courses));

            int s = r.nextInt(7), target = 1 + r.nextInt(40), fuel = 1 + r.nextInt(15);
            int[] pos = new int[s];
            for (int i = 0; i < s; i++) pos[i] = 1 + r.nextInt(target);
            Arrays.sort(pos);
            int[][] st = new int[s][];
            for (int i = 0; i < s; i++) st[i] = new int[] {pos[i], 1 + r.nextInt(15)};
            Check.eq(bruteRefuel(target, fuel, st), minRefuelStops(target, fuel, st),
                     "refuel t=" + target + " f=" + fuel + " " + Arrays.deepToString(st));

            String str = Check.str(r, 1 + r.nextInt(8), r.nextBoolean() ? "ab" : "abc");
            String got = reorganizeString(str);
            int[] cnt = new int[26];
            for (char c : str.toCharArray()) cnt[c - 'a']++;
            boolean possible = canArrange(cnt.clone(), -1, str.length());
            Check.eq(possible, !got.isEmpty(), "reorganize possible " + str);
            if (possible) {
                char[] x = got.toCharArray(), y = str.toCharArray();
                Arrays.sort(x);
                Arrays.sort(y);
                Check.isTrue(Arrays.equals(x, y), "reorganize same letters " + str + " -> " + got);
                for (int i = 1; i < got.length(); i++) Check.isTrue(got.charAt(i) != got.charAt(i - 1), "no equal neighbours " + got);
            }
        }
        Check.eq(4, findMaximizedCapital(2, 0, new int[] {1, 2, 3}, new int[] {0, 1, 1}), "LC 502 sample 1");
        Check.eq(6, findMaximizedCapital(3, 0, new int[] {1, 2, 3}, new int[] {0, 1, 2}), "LC 502 sample 2");
        Check.eq(3, scheduleCourse(new int[][] {{100, 200}, {200, 1300}, {1000, 1250}, {2000, 3200}}), "LC 630 sample 1");
        Check.eq(1, scheduleCourse(new int[][] {{1, 2}}), "LC 630 sample 2");
        Check.eq(0, scheduleCourse(new int[][] {{3, 2}, {4, 3}}), "LC 630 sample 3");
        Check.eq(0, minRefuelStops(1, 1, new int[0][]), "LC 871 sample 1");
        Check.eq(-1, minRefuelStops(100, 1, new int[][] {{10, 100}}), "LC 871 sample 2");
        Check.eq(2, minRefuelStops(100, 10, new int[][] {{10, 60}, {20, 30}, {30, 30}, {60, 40}}), "LC 871 sample 3");
        int[][] many = new int[500][];
        for (int i = 0; i < 500; i++) many[i] = new int[] {i + 1, 1_000_000_000};
        Check.eq(1, minRefuelStops(1_000_000_000, 1, many), "limits: big fuel values");
        String ab = reorganizeString("aab");
        Check.eq("aba", ab, "LC 767 sample 1");
        Check.eq("", reorganizeString("aaab"), "LC 767 sample 2");
        Check.done("P04HeapGreedy");
    }
}
