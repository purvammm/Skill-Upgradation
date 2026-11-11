import java.util.*;

public class P09DirectedCycle {
    //@tpl colours
    // Course schedule: {a, b} means take b before a, so the edge is b -> a.
    // state: 0 = unvisited, 1 = on the current DFS path, 2 = finished.
    static boolean canFinish(int n, int[][] prerequisites) {
        List<Integer>[] g = new List[n];
        for (int v = 0; v < n; v++) g[v] = new ArrayList<>();
        for (int[] p : prerequisites) g[p[1]].add(p[0]);
        int[] state = new int[n];
        for (int v = 0; v < n; v++)
            if (state[v] == 0 && hasCycle(g, v, state)) return false;
        return true;
    }

    static boolean hasCycle(List<Integer>[] g, int u, int[] state) {
        state[u] = 1;                             // u is on the path
        for (int v : g[u]) {
            if (state[v] == 1) return true;       // back edge: a cycle
            if (state[v] == 0 && hasCycle(g, v, state)) return true;
            // state[v] == 2: explored before, no cycle that way
        }
        state[u] = 2;                             // finished
        return false;
    }
    //@end

    //@tpl safe
    // Eventual safe states: a node is safe when no path from it reaches a cycle.
    static List<Integer> eventualSafeNodes(int[][] graph) {
        int[] state = new int[graph.length];
        List<Integer> safe = new ArrayList<>();
        for (int v = 0; v < graph.length; v++)
            if (!reachesCycle(graph, v, state)) safe.add(v);
        return safe;
    }

    static boolean reachesCycle(int[][] g, int u, int[] state) {
        if (state[u] != 0) return state[u] == 1;  // 1: on a path into a cycle
        state[u] = 1;
        for (int v : g[u])
            if (reachesCycle(g, v, state)) return true;   // u stays 1: unsafe
        state[u] = 2;                             // 2: safe for good
        return false;
    }
    //@end

    //@tpl undirected
    // Undirected graph: a seen neighbour other than the parent closes a cycle.
    static boolean cycleUndirected(List<Integer>[] g, int u, int parent,
                                   boolean[] seen) {
        seen[u] = true;
        for (int v : g[u]) {
            if (!seen[v]) {
                if (cycleUndirected(g, v, u, seen)) return true;
            } else if (v != parent) return true;
        }
        return false;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // reach[i][j]: a path of length >= 1 from i to j (Floyd-Warshall closure).
    static boolean[][] closure(int n, int[][] edges) {
        boolean[][] reach = new boolean[n][n];
        for (int[] e : edges) reach[e[0]][e[1]] = true;
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                if (reach[i][k])
                    for (int j = 0; j < n; j++)
                        if (reach[k][j]) reach[i][j] = true;
        return reach;
    }

    static boolean bruteHasCycle(int n, int[][] edges) {
        boolean[][] reach = closure(n, edges);
        for (int i = 0; i < n; i++) if (reach[i][i]) return true;
        return false;
    }

    static List<Integer> bruteSafe(int n, int[][] edges) {
        boolean[][] reach = closure(n, edges);
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            boolean bad = reach[i][i];
            for (int j = 0; j < n && !bad; j++) if (reach[i][j] && reach[j][j]) bad = true;
            if (!bad) out.add(i);
        }
        return out;
    }

    // Simple undirected graph has a cycle iff E > V - components.
    static boolean bruteUndirected(int n, int[][] edges) {
        int[] lab = new int[n];
        for (int i = 0; i < n; i++) lab[i] = i;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int[] e : edges) {
                int m = Math.min(lab[e[0]], lab[e[1]]);
                if (lab[e[0]] != m || lab[e[1]] != m) { lab[e[0]] = m; lab[e[1]] = m; changed = true; }
            }
        }
        Set<Integer> comps = new HashSet<>();
        for (int x : lab) comps.add(x);
        return edges.length > n - comps.size();
    }

    @SuppressWarnings("unchecked")
    static List<Integer>[] adj(int n, int[][] edges, boolean directed) {
        List<Integer>[] g = new List[n];
        for (int v = 0; v < n; v++) g[v] = new ArrayList<>();
        for (int[] e : edges) { g[e[0]].add(e[1]); if (!directed) g[e[1]].add(e[0]); }
        return g;
    }

    public static void main(String[] args) {
        Random r = new Random(96);
        int cyc = 0;
        for (int t = 0; t < 4000; t++) {
            int n = 1 + r.nextInt(9);
            int m = r.nextInt(2 * n + 1);
            int[][] edges = new int[m][];                     // directed u -> v, self-loops allowed
            for (int i = 0; i < m; i++) edges[i] = new int[] {r.nextInt(n), r.nextInt(n)};
            int[][] pre = new int[m][];                        // {a, b}: edge b -> a
            for (int i = 0; i < m; i++) pre[i] = new int[] {edges[i][1], edges[i][0]};
            boolean has = bruteHasCycle(n, edges);
            if (has) cyc++;
            Check.eq(!has, canFinish(n, pre), "canFinish n=" + n + " " + Check.str(edges));
            List<Integer>[] g = adj(n, edges, true);
            int[][] graph = new int[n][];
            for (int i = 0; i < n; i++) {
                graph[i] = new int[g[i].size()];
                for (int j = 0; j < graph[i].length; j++) graph[i][j] = g[i].get(j);
            }
            Check.eq(bruteSafe(n, edges), eventualSafeNodes(graph), "safe " + Check.str(graph));
            // simple undirected graph
            List<int[]> es = new ArrayList<>();
            int p = r.nextInt(50);
            for (int a = 0; a < n; a++)
                for (int b = a + 1; b < n; b++)
                    if (r.nextInt(100) < p) es.add(new int[] {a, b});
            int[][] ue = es.toArray(new int[0][]);
            List<Integer>[] ug = adj(n, ue, false);
            boolean[] seen = new boolean[n];
            boolean got = false;
            for (int v = 0; v < n && !got; v++) if (!seen[v]) got = cycleUndirected(ug, v, -1, seen);
            Check.eq(bruteUndirected(n, ue), got, "undirected " + Check.str(ue));
        }
        Check.isTrue(cyc > 1000 && cyc < 3000, "both answers are common: " + cyc);
        Check.eq(true, canFinish(2, new int[][] {{1, 0}}), "LC 207 sample 1");
        Check.eq(false, canFinish(2, new int[][] {{1, 0}, {0, 1}}), "LC 207 sample 2");
        Check.eq(Arrays.asList(2, 4, 5, 6),
                 eventualSafeNodes(new int[][] {{1, 2}, {2, 3}, {5}, {0}, {5}, {}, {}}), "LC 802 sample 1");
        Check.eq(Arrays.asList(4),
                 eventualSafeNodes(new int[][] {{1, 2, 3, 4}, {1, 2}, {3, 4}, {0, 4}, {}}), "LC 802 sample 2");
        Check.done("P09DirectedCycle");
    }
}
