import java.util.*;

public class P09Bridges {
    //@tpl bridges
    // Critical connections (LC 1192) = bridges of an undirected graph.
    // tin[u]: DFS entry time. low[u]: smallest tin that u's subtree reaches
    // with tree edges down and one back edge up.
    static int timer;
    static int[] tin, low;
    static List<int[]>[] g;                             // g[u] holds {v, edge id}
    static List<List<Integer>> bridges;

    static List<List<Integer>> criticalConnections(int n, List<List<Integer>> conn) {
        g = new List[n];
        for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
        for (int id = 0; id < conn.size(); id++) {
            int a = conn.get(id).get(0), b = conn.get(id).get(1);
            g[a].add(new int[] {b, id});
            g[b].add(new int[] {a, id});
        }
        tin = new int[n];
        low = new int[n];
        Arrays.fill(tin, -1);                           // -1 = not visited
        timer = 0;
        bridges = new ArrayList<>();
        for (int s = 0; s < n; s++) if (tin[s] == -1) dfs(s, -1);
        return bridges;
    }

    static void dfs(int u, int parentEdge) {
        tin[u] = low[u] = timer++;
        for (int[] e : g[u]) {
            int v = e[0];
            if (e[1] == parentEdge) continue;           // not the edge we came by
            if (tin[v] == -1) {                         // tree edge
                dfs(v, e[1]);
                low[u] = Math.min(low[u], low[v]);
                if (low[v] > tin[u]) bridges.add(Arrays.asList(u, v));
            } else {                                    // back edge
                low[u] = Math.min(low[u], tin[v]);
            }
        }
    }
    //@end

    //@tpl cut
    // Articulation points: removing u disconnects its component.
    // Uses tin, low and timer from above; adj[u] lists the neighbours of u.
    static boolean[] isCut;

    static boolean[] articulationPoints(List<Integer>[] adj) {
        int n = adj.length;
        tin = new int[n];
        low = new int[n];
        isCut = new boolean[n];
        Arrays.fill(tin, -1);
        timer = 0;
        for (int s = 0; s < n; s++) if (tin[s] == -1) dfsCut(adj, s, -1);
        return isCut;
    }

    static void dfsCut(List<Integer>[] adj, int u, int parent) {
        tin[u] = low[u] = timer++;
        int children = 0;
        for (int v : adj[u]) {
            if (v == parent) continue;
            if (tin[v] == -1) {
                dfsCut(adj, v, u);
                low[u] = Math.min(low[u], low[v]);
                children++;
                if (parent != -1 && low[v] >= tin[u]) isCut[u] = true;   // >=, not >
            } else {
                low[u] = Math.min(low[u], tin[v]);
            }
        }
        if (parent == -1 && children >= 2) isCut[u] = true;   // the root rule
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // LC 1568 with articulation points: 0 if not exactly one island, 1 if one land
    // cell or a cut cell exists, else 2.
    static int minDays(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        List<Integer>[] adj = new List[m * n];
        int land = 0;
        for (int i = 0; i < m * n; i++) adj[i] = new ArrayList<>();
        for (int r = 0; r < m; r++)
            for (int c = 0; c < n; c++) {
                if (grid[r][c] == 0) continue;
                land++;
                if (r + 1 < m && grid[r + 1][c] == 1) { adj[r * n + c].add((r + 1) * n + c); adj[(r + 1) * n + c].add(r * n + c); }
                if (c + 1 < n && grid[r][c + 1] == 1) { adj[r * n + c].add(r * n + c + 1); adj[r * n + c + 1].add(r * n + c); }
            }
        if (islands(grid) != 1) return 0;
        if (land == 1) return 1;
        boolean[] cut = articulationPoints(adj);
        for (int r = 0; r < m; r++) for (int c = 0; c < n; c++) if (grid[r][c] == 1 && cut[r * n + c]) return 1;
        return 2;
    }

    static int islands(int[][] grid) {
        int m = grid.length, n = grid[0].length, cnt = 0;
        boolean[][] seen = new boolean[m][n];
        for (int r = 0; r < m; r++)
            for (int c = 0; c < n; c++) {
                if (grid[r][c] == 0 || seen[r][c]) continue;
                cnt++;
                Deque<int[]> st = new ArrayDeque<>();
                st.push(new int[] {r, c});
                seen[r][c] = true;
                while (!st.isEmpty()) {
                    int[] x = st.pop();
                    int[][] d = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                    for (int[] dd : d) {
                        int nr = x[0] + dd[0], nc = x[1] + dd[1];
                        if (nr < 0 || nr >= m || nc < 0 || nc >= n || grid[nr][nc] == 0 || seen[nr][nc]) continue;
                        seen[nr][nc] = true;
                        st.push(new int[] {nr, nc});
                    }
                }
            }
        return cnt;
    }

    static int bruteDays(int[][] grid) {
        if (islands(grid) != 1) return 0;
        int m = grid.length, n = grid[0].length;
        for (int r = 0; r < m; r++)
            for (int c = 0; c < n; c++) {
                if (grid[r][c] == 0) continue;
                grid[r][c] = 0;
                int k = islands(grid);
                grid[r][c] = 1;
                if (k != 1) return 1;
            }
        return 2;
    }

    static int components(int n, List<int[]> edges, int skipEdge, int skipNode) {
        int[] p = new int[n];
        for (int i = 0; i < n; i++) p[i] = i;
        int comps = 0;
        for (int i = 0; i < n; i++) if (i != skipNode) comps++;
        for (int i = 0; i < edges.size(); i++) {
            int[] e = edges.get(i);
            if (i == skipEdge || e[0] == skipNode || e[1] == skipNode) continue;
            int a = find(p, e[0]), b = find(p, e[1]);
            if (a != b) { p[a] = b; comps--; }
        }
        return comps;
    }

    static int find(int[] p, int x) { return p[x] == x ? x : (p[x] = find(p, p[x])); }

    public static void main(String[] args) {
        Random r = new Random(29);
        for (int t = 0; t < 4000; t++) {
            int n = 1 + r.nextInt(8), m = r.nextInt(12);
            List<int[]> edges = new ArrayList<>();
            List<List<Integer>> conn = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                int a = r.nextInt(n), b = r.nextInt(n);
                if (a == b) continue;                      // parallel edges allowed
                edges.add(new int[] {a, b});
                conn.add(Arrays.asList(a, b));
            }
            int base = components(n, edges, -1, -1);
            List<List<Integer>> want = new ArrayList<>();
            for (int i = 0; i < edges.size(); i++)
                if (components(n, edges, i, -1) > base) want.add(Arrays.asList(edges.get(i)[0], edges.get(i)[1]));
            Check.eq(Check.canon(want, true), Check.canon(criticalConnections(n, conn), true), "bridges " + conn);
            List<Integer>[] adj = new List[n];
            for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
            for (int[] e : edges) { adj[e[0]].add(e[1]); adj[e[1]].add(e[0]); }
            boolean[] cut = articulationPoints(adj);
            for (int u = 0; u < n; u++)
                Check.eq(components(n, edges, -1, u) > base, cut[u], "cut vertex " + u + " in " + conn);
        }
        for (int t = 0; t < 3000; t++) {
            int m = 1 + r.nextInt(4), n = 1 + r.nextInt(5);
            int[][] grid = new int[m][];
            for (int i = 0; i < m; i++) grid[i] = Check.ints(r, n, t % 3 == 0 ? 0 : 1, 1);
            if (t % 3 != 0) grid[r.nextInt(m)][r.nextInt(n)] = r.nextInt(2);
            for (int k = 0; k < r.nextInt(4); k++) grid[r.nextInt(m)][r.nextInt(n)] = 0;
            Check.eq(bruteDays(grid), minDays(grid), "days " + Arrays.deepToString(grid));
        }
        Check.eq(Check.canon(Arrays.asList(Arrays.asList(1, 3)), true),
            Check.canon(criticalConnections(4, Arrays.asList(Arrays.asList(0, 1), Arrays.asList(1, 2),
                Arrays.asList(2, 0), Arrays.asList(1, 3))), true), "LC 1192 sample 1");
        Check.eq(Check.canon(Arrays.asList(Arrays.asList(0, 1)), true),
            Check.canon(criticalConnections(2, Arrays.asList(Arrays.asList(0, 1))), true), "LC 1192 sample 2");
        Check.eq(2, minDays(new int[][] {{0, 1, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}}), "LC 1568 sample 1");
        Check.eq(2, minDays(new int[][] {{1, 1}}), "LC 1568 sample 2");
        // a path of 100000 nodes recurses 100000 deep (check_java runs with -Xss64m)
        int big = 100_000;
        List<List<Integer>> path = new ArrayList<>();
        for (int i = 0; i + 1 < big; i++) path.add(Arrays.asList(i, i + 1));
        Check.eq(big - 1, criticalConnections(big, path).size(), "deep path");
        Check.done("P09Bridges");
    }
}
