import java.util.*;

public class P09Prim {
    //@tpl dense
    // Min cost to connect all points (LC 1584), Manhattan distance, O(n^2).
    // best[v] = cheapest edge from the tree to v. No edge list is built.
    static int minCostConnectPoints(int[][] pts) {
        int n = pts.length, total = 0;
        int[] best = new int[n];
        boolean[] inTree = new boolean[n];
        Arrays.fill(best, Integer.MAX_VALUE);
        best[0] = 0;                                    // start the tree at point 0
        for (int step = 0; step < n; step++) {
            int u = -1;
            for (int v = 0; v < n; v++)                 // 1. closest point outside
                if (!inTree[v] && (u == -1 || best[v] < best[u])) u = v;
            inTree[u] = true;                           // 2. add it with its edge
            total += best[u];
            for (int v = 0; v < n; v++) {               // 3. u may be closer to v
                if (inTree[v]) continue;
                int d = Math.abs(pts[u][0] - pts[v][0])
                      + Math.abs(pts[u][1] - pts[v][1]);
                best[v] = Math.min(best[v], d);
            }
        }
        return total;
    }
    //@end

    //@tpl heap
    // Prim with a heap for sparse graphs: g[u] holds {v, w}. -1 if disconnected.
    // Like Dijkstra, but the key is the edge weight, not the path length.
    static long primHeap(List<int[]>[] g) {
        int n = g.length, taken = 0;
        boolean[] inTree = new boolean[n];
        PriorityQueue<int[]> pq =
            new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
        pq.add(new int[] {0, 0});                       // {edge weight, node}
        long total = 0;
        while (!pq.isEmpty() && taken < n) {
            int[] top = pq.poll();
            int u = top[1];
            if (inTree[u]) continue;                    // stale: u joined already
            inTree[u] = true;
            total += top[0];
            taken++;
            for (int[] e : g[u])
                if (!inTree[e[0]]) pq.add(new int[] {e[1], e[0]});
        }
        return taken == n ? total : -1;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int find(int[] p, int x) { return p[x] == x ? x : (p[x] = find(p, p[x])); }

    // minimum over all (n-1)-edge subsets that form a spanning tree
    static long bruteMst(int n, int[][] edges) {
        if (n == 1) return 0;
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << edges.length); mask++) {
            if (Integer.bitCount(mask) != n - 1) continue;
            int[] p = new int[n];
            for (int i = 0; i < n; i++) p[i] = i;
            boolean ok = true;
            long w = 0;
            for (int i = 0; i < edges.length && ok; i++)
                if ((mask >> i & 1) == 1) {
                    int a = find(p, edges[i][0]), b = find(p, edges[i][1]);
                    if (a == b) ok = false;
                    else { p[a] = b; w += edges[i][2]; }
                }
            if (ok) best = Math.min(best, w);
        }
        return best == Long.MAX_VALUE ? -1 : best;
    }

    static long kruskal(int n, List<int[]> es) {
        es.sort((a, b) -> Integer.compare(a[2], b[2]));
        int[] p = new int[n];
        for (int i = 0; i < n; i++) p[i] = i;
        long w = 0;
        for (int[] e : es) {
            int a = find(p, e[0]), b = find(p, e[1]);
            if (a != b) { p[a] = b; w += e[2]; }
        }
        return w;
    }

    static int[][] pointEdges(int[][] pts) {
        List<int[]> es = new ArrayList<>();
        for (int i = 0; i < pts.length; i++)
            for (int j = i + 1; j < pts.length; j++)
                es.add(new int[] {i, j, Math.abs(pts[i][0] - pts[j][0]) + Math.abs(pts[i][1] - pts[j][1])});
        return es.toArray(new int[0][]);
    }

    public static void main(String[] args) {
        Random r = new Random(23);
        for (int t = 0; t < 1500; t++) {
            int n = 1 + r.nextInt(6);
            int[][] pts = new int[n][];
            for (int i = 0; i < n; i++) pts[i] = Check.ints(r, 2, -6, 6);
            Check.eq(bruteMst(n, pointEdges(pts)), minCostConnectPoints(pts), "points " + Arrays.deepToString(pts));
        }
        for (int t = 0; t < 300; t++) {                    // bigger: against Kruskal on all pairs
            int n = 1 + r.nextInt(60);
            int[][] pts = new int[n][];
            for (int i = 0; i < n; i++) pts[i] = Check.ints(r, 2, -1_000_000, 1_000_000);
            Check.eq(kruskal(n, new ArrayList<>(Arrays.asList(pointEdges(pts)))), minCostConnectPoints(pts), "points big");
        }
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(6), m = r.nextInt(11);
            int[][] edges = new int[m][];
            List<int[]>[] g = new List[n];
            for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                int u = r.nextInt(n), v = r.nextInt(n), w = r.nextInt(t % 2 == 0 ? 3 : 1000);
                edges[i] = new int[] {u, v, w};
                g[u].add(new int[] {v, w});
                g[v].add(new int[] {u, w});
            }
            Check.eq(bruteMst(n, edges), primHeap(g), "heap " + Arrays.deepToString(edges));
        }
        Check.eq(20, minCostConnectPoints(new int[][] {{0, 0}, {2, 2}, {3, 10}, {5, 2}, {7, 0}}), "LC 1584 sample 1");
        Check.eq(18, minCostConnectPoints(new int[][] {{3, 12}, {-2, 5}, {-4, 1}}), "LC 1584 sample 2");
        // n = 1000 random points: the O(n^2) loop is fast
        int[][] pts = new int[1000][];
        for (int i = 0; i < 1000; i++) pts[i] = Check.ints(r, 2, -1_000_000, 1_000_000);
        Check.eq(kruskal(1000, new ArrayList<>(Arrays.asList(pointEdges(pts)))), minCostConnectPoints(pts), "n = 1000");
        Check.done("P09Prim");
    }
}
