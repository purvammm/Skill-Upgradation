import java.util.*;

public class P09Kruskal {
    // DSU with the same interface as the one printed in pattern dsu (P09Dsu):
    // union(a, b) returns false when a and b are already in one set.
    // Not printed here; iterative path halving plus union by size.
    static class DSU {
        int[] parent, size;

        DSU(int n) {
            parent = new int[n];
            size = new int[n];
            for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
        }

        int find(int x) {
            while (parent[x] != x) x = parent[x] = parent[parent[x]];
            return x;
        }

        boolean union(int a, int b) {
            a = find(a);
            b = find(b);
            if (a == b) return false;
            if (size[a] < size[b]) { int t = a; a = b; b = t; }
            parent[b] = a;
            size[a] += size[b];
            return true;
        }
    }

    //@tpl main
    // Kruskal: MST weight of nodes 0..n-1, edges[i] = {u, v, w}; -1 if disconnected.
    static long kruskal(int n, int[][] edges) {
        Arrays.sort(edges, (a, b) -> Integer.compare(a[2], b[2]));   // in place
        DSU dsu = new DSU(n);
        long total = 0;
        int used = 0;
        for (int[] e : edges) {
            if (used == n - 1) break;                   // the tree is complete
            if (dsu.union(e[0], e[1])) {                // joins two parts: take it
                total += e[2];
                used++;
            }
        }
        return used == n - 1 ? total : -1;
    }
    //@end

    //@tpl critical
    // LC 1489. Critical: without edge i the MST is heavier or impossible.
    // Pseudo-critical: not critical, but some MST uses it (force it in first).
    static List<List<Integer>> findCriticalAndPseudoCriticalEdges(
            int n, int[][] edges) {
        Integer[] order = new Integer[edges.length];
        for (int i = 0; i < order.length; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> Integer.compare(edges[a][2], edges[b][2]));
        long best = mst(n, edges, order, -1, -1);
        List<Integer> critical = new ArrayList<>(), pseudo = new ArrayList<>();
        for (int i = 0; i < edges.length; i++) {
            if (mst(n, edges, order, i, -1) > best) critical.add(i);
            else if (mst(n, edges, order, -1, i) == best) pseudo.add(i);
        }
        return Arrays.asList(critical, pseudo);
    }

    // Kruskal that skips edge `skip` and takes edge `force` first (-1: none).
    static long mst(int n, int[][] edges, Integer[] order, int skip, int force) {
        DSU dsu = new DSU(n);
        long total = 0;
        int used = 0;
        if (force >= 0 && dsu.union(edges[force][0], edges[force][1])) {
            total += edges[force][2];
            used++;
        }
        for (int i : order)
            if (i != skip && dsu.union(edges[i][0], edges[i][1])) {
                total += edges[i][2];
                used++;
            }
        return used == n - 1 ? total : Long.MAX_VALUE;  // MAX: no spanning tree
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean isSpanningTree(int n, int[][] edges, int mask) {
        DSU d = new DSU(n);
        int cnt = 0;
        for (int i = 0; i < edges.length; i++)
            if ((mask >> i & 1) == 1) {
                if (!d.union(edges[i][0], edges[i][1])) return false;
                cnt++;
            }
        return cnt == n - 1;
    }

    static long weight(int[][] edges, int mask) {
        long w = 0;
        for (int i = 0; i < edges.length; i++) if ((mask >> i & 1) == 1) w += edges[i][2];
        return w;
    }

    static long bruteMst(int n, int[][] edges) {
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << edges.length); mask++)
            if (Integer.bitCount(mask) == n - 1 && isSpanningTree(n, edges, mask)) best = Math.min(best, weight(edges, mask));
        return best == Long.MAX_VALUE ? -1 : best;
    }

    static int[][] randomEdges(Random r, int n, int m, int maxW, boolean simple) {
        List<int[]> es = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < m; i++) {
            int u = r.nextInt(n), v = r.nextInt(n);
            if (simple) {
                if (u == v || !seen.add(Math.min(u, v) * 100 + Math.max(u, v))) continue;
            }
            es.add(new int[] {u, v, 1 + r.nextInt(maxW)});
        }
        return es.toArray(new int[0][]);
    }

    public static void main(String[] args) {
        Random r = new Random(17);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(5);
            int[][] edges = randomEdges(r, n, r.nextInt(10), t % 2 == 0 ? 3 : 20, false);  // loops, parallel edges
            Check.eq(bruteMst(n, edges), kruskal(n, edges), "kruskal " + Arrays.deepToString(edges));
        }
        // weights near 10^9: the total needs a long
        int[][] heavy = new int[9][];
        for (int i = 0; i < 9; i++) heavy[i] = new int[] {i, i + 1, 1_000_000_000};
        Check.eq(9_000_000_000L, kruskal(10, heavy), "long total");

        for (int t = 0; t < 2000; t++) {
            int n = 2 + r.nextInt(5);
            int[][] edges = randomEdges(r, n, 2 + r.nextInt(10), t % 2 == 0 ? 2 : 6, true);
            long best = bruteMst(n, edges);
            if (best < 0) continue;                       // LC: the graph is connected
            int all = 0, inAll = ~0, inSome = 0;
            for (int mask = 0; mask < (1 << edges.length); mask++)
                if (Integer.bitCount(mask) == n - 1 && isSpanningTree(n, edges, mask) && weight(edges, mask) == best) {
                    inAll &= mask;
                    inSome |= mask;
                    all++;
                }
            List<Integer> crit = new ArrayList<>(), pseudo = new ArrayList<>();
            for (int i = 0; i < edges.length; i++) {
                if ((inAll >> i & 1) == 1) crit.add(i);
                else if ((inSome >> i & 1) == 1) pseudo.add(i);
            }
            Check.eq(Arrays.asList(crit, pseudo), findCriticalAndPseudoCriticalEdges(n, edges), "critical " + Arrays.deepToString(edges));
        }
        Check.eq(Arrays.asList(Arrays.asList(0, 1), Arrays.asList(2, 3, 4, 5)), findCriticalAndPseudoCriticalEdges(5,
            new int[][] {{0, 1, 1}, {1, 2, 1}, {2, 3, 2}, {0, 3, 2}, {0, 4, 3}, {3, 4, 3}, {1, 4, 6}}), "LC 1489 sample 1");
        Check.eq(Arrays.asList(Arrays.<Integer>asList(), Arrays.asList(0, 1, 2, 3)), findCriticalAndPseudoCriticalEdges(4,
            new int[][] {{0, 1, 1}, {1, 2, 1}, {2, 3, 1}, {0, 3, 1}}), "LC 1489 sample 2");
        // LC 1584 through kruskal: all pairs with Manhattan weights
        int[][] pts = {{0, 0}, {2, 2}, {3, 10}, {5, 2}, {7, 0}};
        List<int[]> es = new ArrayList<>();
        for (int i = 0; i < pts.length; i++)
            for (int j = i + 1; j < pts.length; j++)
                es.add(new int[] {i, j, Math.abs(pts[i][0] - pts[j][0]) + Math.abs(pts[i][1] - pts[j][1])});
        Check.eq(20L, kruskal(pts.length, es.toArray(new int[0][])), "LC 1584 sample 1");
        Check.done("P09Kruskal");
    }
}
