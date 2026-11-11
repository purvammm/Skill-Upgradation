import java.util.*;

public class P09Bipartite {
    //@tpl bfs
    // graph[u] lists u's neighbours. color: 0 = none yet, 1 or -1 = a side.
    static boolean isBipartite(int[][] graph) {
        int n = graph.length;
        int[] color = new int[n];
        Deque<Integer> q = new ArrayDeque<>();
        for (int s = 0; s < n; s++) {
            if (color[s] != 0) continue;          // its component is done
            color[s] = 1;                         // new component: any side
            q.add(s);
            while (!q.isEmpty()) {
                int u = q.poll();
                for (int v : graph[u]) {
                    if (color[v] == 0) { color[v] = -color[u]; q.add(v); }
                    else if (color[v] == color[u]) return false;   // odd cycle
                }
            }
        }
        return true;
    }
    //@end

    //@tpl dfs
    // Possible bipartition: people 1..n, and each disliking pair must split.
    static boolean possibleBipartition(int n, int[][] dislikes) {
        List<Integer>[] g = new List[n + 1];
        for (int v = 0; v <= n; v++) g[v] = new ArrayList<>();
        for (int[] d : dislikes) { g[d[0]].add(d[1]); g[d[1]].add(d[0]); }
        int[] color = new int[n + 1];
        for (int v = 1; v <= n; v++)
            if (color[v] == 0 && !paint(g, v, 1, color)) return false;
        return true;
    }

    // DFS colouring: give u colour c, its neighbours -c. False on a clash.
    static boolean paint(List<Integer>[] g, int u, int c, int[] color) {
        color[u] = c;
        for (int v : g[u]) {
            if (color[v] == c) return false;
            if (color[v] == 0 && !paint(g, v, -c, color)) return false;
        }
        return true;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Try all 2^n side assignments.
    static boolean brute(int n, int[][] edges, int base) {
        for (int mask = 0; mask < (1 << n); mask++) {
            boolean ok = true;
            for (int[] e : edges) {
                int a = (mask >> (e[0] - base)) & 1, b = (mask >> (e[1] - base)) & 1;
                if (a == b) { ok = false; break; }
            }
            if (ok) return true;
        }
        return false;
    }

    public static void main(String[] args) {
        Random r = new Random(95);
        int yes = 0;
        for (int t = 0; t < 4000; t++) {
            int n = 1 + r.nextInt(10);
            // simple undirected graph: no self-loops, no parallel edges (as LC 785 and 886)
            List<int[]> es = new ArrayList<>();
            int p = r.nextInt(60);
            for (int a = 0; a < n; a++)
                for (int b = a + 1; b < n; b++)
                    if (r.nextInt(100) < p) es.add(r.nextBoolean() ? new int[] {a, b} : new int[] {b, a});
            Collections.shuffle(es, r);
            int[][] edges = es.toArray(new int[0][]);
            List<List<Integer>> adj = new ArrayList<>();
            for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
            for (int[] e : edges) { adj.get(e[0]).add(e[1]); adj.get(e[1]).add(e[0]); }
            int[][] graph = new int[n][];
            for (int i = 0; i < n; i++) {
                graph[i] = new int[adj.get(i).size()];
                for (int j = 0; j < graph[i].length; j++) graph[i][j] = adj.get(i).get(j);
            }
            boolean exp = brute(n, edges, 0);
            if (exp) yes++;
            Check.eq(exp, isBipartite(graph), "bipartite " + Check.str(graph));
            int[][] one = new int[edges.length][];
            for (int i = 0; i < edges.length; i++) one[i] = new int[] {edges[i][0] + 1, edges[i][1] + 1};
            Check.eq(exp, possibleBipartition(n, one), "bipartition n=" + n + " " + Check.str(one));
        }
        Check.isTrue(yes > 1000 && yes < 3000, "both answers are common: " + yes);
        Check.eq(false, isBipartite(new int[][] {{1, 2, 3}, {0, 2}, {0, 1, 3}, {0, 2}}), "LC 785 sample 1");
        Check.eq(true, isBipartite(new int[][] {{1, 3}, {0, 2}, {1, 3}, {0, 2}}), "LC 785 sample 2");
        Check.eq(true, possibleBipartition(4, new int[][] {{1, 2}, {1, 3}, {2, 4}}), "LC 886 sample 1");
        Check.eq(false, possibleBipartition(3, new int[][] {{1, 2}, {1, 3}, {2, 3}}), "LC 886 sample 2");
        Check.done("P09Bipartite");
    }
}
