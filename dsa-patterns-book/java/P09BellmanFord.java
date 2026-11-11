import java.util.*;

public class P09BellmanFord {
    //@tpl main
    static final long INF = Long.MAX_VALUE / 4;

    // Shortest paths from src with any edge weights; edges[i] = {u, v, w}.
    // Returns null if a negative cycle can be reached from src.
    static long[] bellmanFord(int n, int[][] edges, int src) {
        long[] dist = new long[n];
        Arrays.fill(dist, INF);
        dist[src] = 0;
        for (int round = 1; round < n; round++) {      // n - 1 rounds
            boolean changed = false;
            for (int[] e : edges) {
                if (dist[e[0]] == INF) continue;          // u not reached yet
                if (dist[e[0]] + e[2] < dist[e[1]]) {
                    dist[e[1]] = dist[e[0]] + e[2];
                    changed = true;
                }
            }
            if (!changed) return dist;                    // stable: no cycle either
        }
        for (int[] e : edges)                             // round n: still improves?
            if (dist[e[0]] != INF && dist[e[0]] + e[2] < dist[e[1]]) return null;
        return dist;
    }
    //@end

    //@tpl kstops
    // Cheapest flights within k stops (LC 787): at most k + 1 edges.
    // Each round reads last round's copy, so after round i every path
    // in dist uses at most i edges.
    static int findCheapestPrice(int n, int[][] flights, int src, int dst, int k) {
        long[] dist = new long[n];
        Arrays.fill(dist, INF);
        dist[src] = 0;
        for (int round = 1; round <= k + 1; round++) {
            long[] prev = dist.clone();                   // paths with < round edges
            for (int[] f : flights)
                if (prev[f[0]] != INF && prev[f[0]] + f[2] < dist[f[1]])
                    dist[f[1]] = prev[f[0]] + f[2];
        }
        return dist[dst] == INF ? -1 : (int) dist[dst];
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static long bestPath;

    // shortest simple path from u to every node (no negative cycle: shortest paths are simple)
    static void simple(int u, List<int[]>[] g, boolean[] on, long len, long[] best) {
        best[u] = Math.min(best[u], len);
        for (int[] e : g[u]) {
            if (on[e[0]]) continue;
            on[e[0]] = true;
            simple(e[0], g, on, len + e[1], best);
            on[e[0]] = false;
        }
    }

    // is there a negative simple cycle through a node reachable from src?
    static boolean negCycleFrom(int start, int u, List<int[]>[] g, boolean[] on, long len) {
        for (int[] e : g[u]) {
            if (e[0] == start && len + e[1] < 0) return true;
            if (on[e[0]]) continue;
            on[e[0]] = true;
            boolean f = negCycleFrom(start, e[0], g, on, len + e[1]);
            on[e[0]] = false;
            if (f) return true;
        }
        return false;
    }

    static long[] bruteDist(int n, int[][] edges, int src) {
        List<int[]>[] g = new List[n];
        for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
        for (int[] e : edges) g[e[0]].add(new int[] {e[1], e[2]});
        long[] best = new long[n];
        Arrays.fill(best, INF);
        boolean[] on = new boolean[n];
        on[src] = true;
        simple(src, g, on, 0, best);
        return best;
    }

    static boolean bruteNegCycle(int n, int[][] edges, int src) {
        List<int[]>[] g = new List[n];
        for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
        for (int[] e : edges) g[e[0]].add(new int[] {e[1], e[2]});
        long[] reach = bruteDist(n, edges, src);       // only reachability is used
        for (int v = 0; v < n; v++) {
            if (reach[v] == INF) continue;
            boolean[] on = new boolean[n];
            on[v] = true;
            if (negCycleFrom(v, v, g, on, 0)) return true;
        }
        return false;
    }

    // every walk from src with at most maxEdges edges
    static void walks(int u, int left, List<int[]>[] g, long len, long[] best) {
        best[u] = Math.min(best[u], len);
        if (left == 0) return;
        for (int[] e : g[u]) walks(e[0], left - 1, g, len + e[1], best);
    }

    public static void main(String[] args) {
        Random r = new Random(5);
        int cycles = 0;
        for (int t = 0; t < 4000; t++) {
            int n = 1 + r.nextInt(6), m = r.nextInt(11);
            int[][] edges = new int[m][];
            int lo = t % 2 == 0 ? -3 : -8;
            for (int i = 0; i < m; i++) edges[i] = new int[] {r.nextInt(n), r.nextInt(n), lo + r.nextInt(15)};
            int src = r.nextInt(n);
            long[] got = bellmanFord(n, edges, src);
            boolean neg = bruteNegCycle(n, edges, src);
            Check.eq(neg, got == null, "negative cycle " + Arrays.deepToString(edges) + " src=" + src);
            if (neg) { cycles++; continue; }
            Check.eq(bruteDist(n, edges, src), got, "dist " + Arrays.deepToString(edges));
        }
        Check.isTrue(cycles > 300, "enough graphs with negative cycles: " + cycles);
        // worst case: a path given in reverse order needs all n - 1 rounds
        int n0 = 50;
        int[][] chain = new int[n0 - 1][];
        for (int i = 0; i < n0 - 1; i++) chain[i] = new int[] {n0 - 2 - i, n0 - 1 - i, -1};
        Check.eq(-(long) (n0 - 1), bellmanFord(n0, chain, 0)[n0 - 1], "reversed chain");
        int[][] loop = new int[n0][];
        for (int i = 0; i < n0; i++) loop[i] = new int[] {i, (i + 1) % n0, i == 0 ? -1 : 0};
        Check.isTrue(bellmanFord(n0, loop, 3) == null, "long negative cycle");

        for (int t = 0; t < 4000; t++) {
            int n = 2 + r.nextInt(5), m = r.nextInt(10);
            Set<Long> pairs = new HashSet<>();
            List<int[]> fl = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                int u = r.nextInt(n), v = r.nextInt(n);
                if (u == v || !pairs.add((long) u * 100 + v)) continue;   // LC: no loops, no repeats
                fl.add(new int[] {u, v, 1 + r.nextInt(t % 2 == 0 ? 3 : 50)});
            }
            int[][] flights = fl.toArray(new int[0][]);
            int src = r.nextInt(n), dst = r.nextInt(n), k = r.nextInt(n);
            if (src == dst) continue;
            List<int[]>[] g = new List[n];
            for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
            for (int[] f : flights) g[f[0]].add(new int[] {f[1], f[2]});
            long[] best = new long[n];
            Arrays.fill(best, INF);
            walks(src, k + 1, g, 0, best);
            long want = best[dst] == INF ? -1 : best[dst];
            Check.eq(want, findCheapestPrice(n, flights, src, dst, k), "flights " + Arrays.deepToString(flights) + " k=" + k);
        }
        Check.eq(700, findCheapestPrice(4, new int[][] {{0, 1, 100}, {1, 2, 100}, {2, 0, 100}, {1, 3, 600},
            {2, 3, 200}}, 0, 3, 1), "LC 787 sample 1");
        Check.eq(200, findCheapestPrice(3, new int[][] {{0, 1, 100}, {1, 2, 100}, {0, 2, 500}}, 0, 2, 1), "LC 787 sample 2");
        Check.eq(500, findCheapestPrice(3, new int[][] {{0, 1, 100}, {1, 2, 100}, {0, 2, 500}}, 0, 2, 0), "LC 787 sample 3");
        Check.done("P09BellmanFord");
    }
}
