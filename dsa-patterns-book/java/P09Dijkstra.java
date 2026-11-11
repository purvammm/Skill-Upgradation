import java.util.*;

public class P09Dijkstra {
    //@tpl main
    // Dijkstra from src. g[u] holds edges {v, w} with w >= 0.
    // dist[v] = Long.MAX_VALUE means v cannot be reached.
    static long[] dijkstra(List<int[]>[] g, int src) {
        long[] dist = new long[g.length];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[src] = 0;
        PriorityQueue<long[]> pq =
            new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
        pq.add(new long[] {0, src});                   // {distance, node}
        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            long d = top[0];
            int u = (int) top[1];
            if (d > dist[u]) continue;                 // stale: u was settled cheaper
            for (int[] e : g[u]) {                     // d == dist[u] is final here
                int v = e[0];
                long nd = d + e[1];
                if (nd < dist[v]) {                    // relax u -> v
                    dist[v] = nd;
                    pq.add(new long[] {nd, v});
                }
            }
        }
        return dist;
    }

    // Network delay time (LC 743): nodes 1..n, times[i] = {u, v, w}.
    static int networkDelayTime(int[][] times, int n, int k) {
        List<int[]>[] g = new List[n + 1];
        for (int i = 0; i <= n; i++) g[i] = new ArrayList<>();
        for (int[] t : times) g[t[0]].add(new int[] {t[1], t[2]});
        long[] dist = dijkstra(g, k);
        long worst = 0;
        for (int v = 1; v <= n; v++) worst = Math.max(worst, dist[v]);
        return worst == Long.MAX_VALUE ? -1 : (int) worst;
    }
    //@end

    //@tpl ways
    // Number of ways to arrive at destination (LC 1976): count the shortest
    // paths 0 -> n-1, mod 1e9+7. Roads are undirected, times are >= 1.
    static int countPaths(int n, int[][] roads) {
        List<int[]>[] g = new List[n];
        for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
        for (int[] r : roads) {
            g[r[0]].add(new int[] {r[1], r[2]});
            g[r[1]].add(new int[] {r[0], r[2]});
        }
        long[] dist = new long[n], ways = new long[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[0] = 0;
        ways[0] = 1;
        PriorityQueue<long[]> pq =
            new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
        pq.add(new long[] {0, 0});
        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            long d = top[0];
            int u = (int) top[1];
            if (d > dist[u]) continue;                 // ways[u] is complete here
            for (int[] e : g[u]) {
                int v = e[0];
                long nd = d + e[1];
                if (nd < dist[v]) {                    // shorter: a new count
                    dist[v] = nd;
                    ways[v] = ways[u];
                    pq.add(new long[] {nd, v});
                } else if (nd == dist[v]) {            // as short: add u's ways
                    ways[v] = (ways[v] + ways[u]) % 1_000_000_007;
                }
            }
        }
        return (int) ways[n - 1];
    }
    //@end

    // ---------------- tests (not printed) ----------------

    // Path with minimum effort (LC 1631), described in a variant bullet (not printed):
    // the new cost is max(e, |step|) instead of d + w; return when the corner pops.
    static int minimumEffortPath(int[][] h) {
        int m = h.length, n = h[0].length;
        int[] best = new int[m * n];
        Arrays.fill(best, Integer.MAX_VALUE);
        best[0] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
        pq.add(new int[] {0, 0});
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        while (!pq.isEmpty()) {
            int[] top = pq.poll();
            int e = top[0], cell = top[1], r = cell / n, c = cell % n;
            if (e > best[cell]) continue;
            if (cell == m * n - 1) return e;
            for (int k = 0; k < 4; k++) {
                int nr = r + dr[k], nc = c + dc[k];
                if (nr < 0 || nr >= m || nc < 0 || nc >= n) continue;
                int ne = Math.max(e, Math.abs(h[nr][nc] - h[r][c]));
                if (ne < best[nr * n + nc]) {
                    best[nr * n + nc] = ne;
                    pq.add(new int[] {ne, nr * n + nc});
                }
            }
        }
        return best[m * n - 1];
    }
    static final long INF = Long.MAX_VALUE;

    static long[][] floyd(int n, List<int[]>[] g) {
        long[][] d = new long[n][n];
        for (long[] row : d) Arrays.fill(row, INF);
        for (int i = 0; i < n; i++) d[i][i] = 0;
        for (int u = 0; u < n; u++) for (int[] e : g[u]) d[u][e[0]] = Math.min(d[u][e[0]], e[1]);
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (d[i][k] != INF && d[k][j] != INF) d[i][j] = Math.min(d[i][j], d[i][k] + d[k][j]);
        return d;
    }

    static List<int[]>[] randomGraph(Random r, int n, int m, int maxW) {
        List<int[]>[] g = new List[n];
        for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
        for (int i = 0; i < m; i++) g[r.nextInt(n)].add(new int[] {r.nextInt(n), r.nextInt(maxW + 1)});
        return g;
    }

    // smallest threshold t such that the corner is reachable using steps <= t
    static int bruteEffort(int[][] h) {
        int m = h.length, n = h[0].length;
        for (int t = 0; ; t++) {
            boolean[][] seen = new boolean[m][n];
            Deque<int[]> q = new ArrayDeque<>();
            q.add(new int[] {0, 0});
            seen[0][0] = true;
            int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
            while (!q.isEmpty()) {
                int[] c = q.poll();
                for (int k = 0; k < 4; k++) {
                    int nr = c[0] + dr[k], nc = c[1] + dc[k];
                    if (nr < 0 || nr >= m || nc < 0 || nc >= n || seen[nr][nc]) continue;
                    if (Math.abs(h[nr][nc] - h[c[0]][c[1]]) > t) continue;
                    seen[nr][nc] = true;
                    q.add(new int[] {nr, nc});
                }
            }
            if (seen[m - 1][n - 1]) return t;
        }
    }

    static long bestLen;
    static long bestCount;

    static void dfsPaths(int u, int n, int[][] w, boolean[] on, long len) {
        if (u == n - 1) {
            if (len < bestLen) { bestLen = len; bestCount = 1; }
            else if (len == bestLen) bestCount++;
            return;
        }
        for (int v = 0; v < n; v++) {
            if (w[u][v] == 0 || on[v]) continue;
            on[v] = true;
            dfsPaths(v, n, w, on, len + w[u][v]);
            on[v] = false;
        }
    }

    static long bruteWays(int n, int[][] roads) {
        int[][] w = new int[n][n];
        for (int[] r : roads) { w[r[0]][r[1]] = r[2]; w[r[1]][r[0]] = r[2]; }
        bestLen = Long.MAX_VALUE;
        bestCount = 0;
        boolean[] on = new boolean[n];
        on[0] = true;
        dfsPaths(0, n, w, on, 0);
        return bestCount;
    }

    public static void main(String[] args) {
        Random r = new Random(9);
        // dijkstra vs Floyd on random directed graphs (zero weights, loops, parallel edges)
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(7), m = r.nextInt(16);
            List<int[]>[] g = randomGraph(r, n, m, t % 3 == 0 ? 1 : 20);
            long[][] fw = floyd(n, g);
            int src = r.nextInt(n);
            Check.eq(fw[src], dijkstra(g, src), "dijkstra n=" + n + " t=" + t);
        }
        // large weights: distances pass 2^31
        for (int t = 0; t < 300; t++) {
            int n = 2 + r.nextInt(6), m = r.nextInt(14);
            List<int[]>[] g = new List[n];
            for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
            for (int i = 0; i < m; i++)
                g[r.nextInt(n)].add(new int[] {r.nextInt(n), 1_500_000_000 + r.nextInt(600_000_000)});
            Check.eq(floyd(n, g)[0], dijkstra(g, 0), "big weights");
        }
        // network delay time vs Floyd
        for (int t = 0; t < 1000; t++) {
            int n = 1 + r.nextInt(6), m = r.nextInt(12);
            int[][] times = new int[m][];
            List<int[]>[] g = new List[n];
            for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                int u = r.nextInt(n), v = r.nextInt(n), w = 1 + r.nextInt(9);
                times[i] = new int[] {u + 1, v + 1, w};
                g[u].add(new int[] {v, w});
            }
            int k = 1 + r.nextInt(n);
            long[] d = floyd(n, g)[k - 1];
            long worst = 0;
            for (long x : d) worst = Math.max(worst, x);
            Check.eq(worst == INF ? -1 : worst, networkDelayTime(times, n, k), "network delay");
        }
        // minimum effort vs threshold BFS
        for (int t = 0; t < 2000; t++) {
            int m = 1 + r.nextInt(4), n = 1 + r.nextInt(4);
            int[][] h = new int[m][];
            for (int i = 0; i < m; i++) h[i] = Check.ints(r, n, 1, t % 2 == 0 ? 5 : 30);
            Check.eq(bruteEffort(h), minimumEffortPath(h), "effort " + Arrays.deepToString(h));
        }
        // count shortest paths vs enumerating all simple paths
        for (int t = 0; t < 2000; t++) {
            int n = 2 + r.nextInt(6);
            List<int[]> roads = new ArrayList<>();
            for (int u = 0; u < n; u++)
                for (int v = u + 1; v < n; v++)
                    if (r.nextInt(3) > 0) roads.add(new int[] {u, v, 1 + r.nextInt(t % 2 == 0 ? 2 : 6)});
            int[][] rs = roads.toArray(new int[0][]);
            long want = bruteWays(n, rs);
            if (want == 0) continue;                    // LC guarantees a path
            Check.eq(want, countPaths(n, rs), "ways n=" + n);
        }
        // many shortest paths: the count needs the mod
        int layers = 40;
        List<int[]> diamond = new ArrayList<>();
        for (int i = 0; i < layers; i++) {
            int a = 3 * i, b = a + 1, c = a + 2, d = a + 3;
            diamond.add(new int[] {a, b, 1});
            diamond.add(new int[] {a, c, 1});
            diamond.add(new int[] {b, d, 1});
            diamond.add(new int[] {c, d, 1});
        }
        long pow = 1;
        for (int i = 0; i < layers; i++) pow = pow * 2 % 1_000_000_007;
        Check.eq(pow, countPaths(3 * layers + 1, diamond.toArray(new int[0][])), "2^40 mod p");

        Check.eq(2, networkDelayTime(new int[][] {{2, 1, 1}, {2, 3, 1}, {3, 4, 1}}, 4, 2), "LC 743 sample 1");
        Check.eq(1, networkDelayTime(new int[][] {{1, 2, 1}}, 2, 1), "LC 743 sample 2");
        Check.eq(-1, networkDelayTime(new int[][] {{1, 2, 1}}, 2, 2), "LC 743 sample 3");
        Check.eq(2, minimumEffortPath(new int[][] {{1, 2, 2}, {3, 8, 2}, {5, 3, 5}}), "LC 1631 sample 1");
        Check.eq(1, minimumEffortPath(new int[][] {{1, 2, 3}, {3, 8, 4}, {5, 3, 5}}), "LC 1631 sample 2");
        Check.eq(0, minimumEffortPath(new int[][] {{1, 2, 1, 1, 1}, {1, 2, 1, 2, 1}, {1, 2, 1, 2, 1},
            {1, 2, 1, 2, 1}, {1, 1, 1, 2, 1}}), "LC 1631 sample 3");
        Check.eq(4, countPaths(7, new int[][] {{0, 6, 7}, {0, 1, 2}, {1, 2, 3}, {1, 3, 3}, {6, 3, 3},
            {3, 5, 1}, {6, 5, 1}, {2, 5, 1}, {0, 4, 5}, {4, 6, 2}}), "LC 1976 sample 1");
        Check.eq(1, countPaths(2, new int[][] {{1, 0, 10}}), "LC 1976 sample 2");
        Check.done("P09Dijkstra");
    }
}
