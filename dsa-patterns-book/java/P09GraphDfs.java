import java.util.*;

public class P09GraphDfs {
    //@tpl components
    // Adjacency lists for an undirected graph on nodes 0..n-1.
    static List<Integer>[] build(int n, int[][] edges) {
        List<Integer>[] g = new List[n];
        for (int v = 0; v < n; v++) g[v] = new ArrayList<>();
        for (int[] e : edges) {
            g[e[0]].add(e[1]);
            g[e[1]].add(e[0]);                // leave out for a directed graph
        }
        return g;
    }

    // Connected components: one DFS from every node not seen yet.
    static int countComponents(int n, int[][] edges) {
        List<Integer>[] g = build(n, edges);
        boolean[] seen = new boolean[n];
        int count = 0;
        for (int v = 0; v < n; v++)
            if (!seen[v]) { count++; dfs(g, v, seen); }
        return count;
    }

    static void dfs(List<Integer>[] g, int u, boolean[] seen) {
        seen[u] = true;                       // mark on entry, never unmark
        for (int v : g[u])
            if (!seen[v]) dfs(g, v, seen);
    }
    //@end

    //@tpl iterative
    // The same reach with an explicit stack: no recursion depth limit.
    static void dfsIter(List<Integer>[] g, int src, boolean[] seen) {
        Deque<Integer> stack = new ArrayDeque<>();
        seen[src] = true;
        stack.push(src);
        while (!stack.isEmpty()) {
            int u = stack.pop();
            for (int v : g[u])
                if (!seen[v]) { seen[v] = true; stack.push(v); }  // mark on push
        }
    }
    //@end

    //@tpl islands
    static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    // Number of islands: each DFS sinks one whole island, '1' -> '0'.
    static int numIslands(char[][] grid) {
        int count = 0;
        for (int r = 0; r < grid.length; r++)
            for (int c = 0; c < grid[0].length; c++)
                if (grid[r][c] == '1') { count++; sink(grid, r, c); }
        return count;
    }

    static void sink(char[][] g, int r, int c) {
        if (r < 0 || c < 0 || r >= g.length || c >= g[0].length || g[r][c] != '1')
            return;                           // off the grid, water or done
        g[r][c] = '0';                        // mark by overwriting
        for (int[] d : DIRS) sink(g, r + d[0], c + d[1]);
    }

    // Max area of island: the same DFS returns how many cells it sank.
    // Call it from every 1 cell and keep the maximum.
    static int area(int[][] g, int r, int c) {
        if (r < 0 || c < 0 || r >= g.length || c >= g[0].length || g[r][c] != 1)
            return 0;
        g[r][c] = 0;
        int size = 1;
        for (int[] d : DIRS) size += area(g, r + d[0], c + d[1]);
        return size;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int maxAreaOfIsland(int[][] g) {
        int best = 0;
        for (int r = 0; r < g.length; r++)
            for (int c = 0; c < g[0].length; c++)
                if (g[r][c] == 1) best = Math.max(best, area(g, r, c));
        return best;
    }

    // Brute force: label propagation. Every node starts with its own label; each
    // edge pulls both ends to the smaller label until nothing changes.
    static int[] labels(int n, int[][] edges) {
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
        return lab;
    }

    static int distinct(int[] a) {
        Set<Integer> s = new HashSet<>();
        for (int x : a) s.add(x);
        return s.size();
    }

    static int[][] gridEdges(int R, int C, boolean[][] land) {
        List<int[]> e = new ArrayList<>();
        for (int r = 0; r < R; r++)
            for (int c = 0; c < C; c++) {
                if (!land[r][c]) continue;
                if (r + 1 < R && land[r + 1][c]) e.add(new int[] {r * C + c, (r + 1) * C + c});
                if (c + 1 < C && land[r][c + 1]) e.add(new int[] {r * C + c, r * C + c + 1});
            }
        return e.toArray(new int[0][]);
    }

    static int[][] randomEdges(Random r, int n, int m) {
        int[][] e = new int[m][];
        for (int i = 0; i < m; i++) e[i] = new int[] {r.nextInt(n), r.nextInt(n)};
        return e;
    }

    public static void main(String[] args) {
        Random r = new Random(91);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(12);
            int[][] edges = randomEdges(r, n, r.nextInt(2 * n));
            int[] lab = labels(n, edges);
            Check.eq(distinct(lab), countComponents(n, edges), "components n=" + n + " " + Check.str(edges));
            List<Integer>[] g = build(n, edges);
            int src = r.nextInt(n);
            boolean[] a = new boolean[n], b = new boolean[n];
            dfs(g, src, a);
            dfsIter(g, src, b);
            for (int v = 0; v < n; v++) {
                Check.eq(lab[v] == lab[src], a[v], "dfs reach " + v);
                Check.eq(lab[v] == lab[src], b[v], "dfsIter reach " + v);
            }
            // grids
            int R = 1 + r.nextInt(5), C = 1 + r.nextInt(6);
            boolean[][] land = new boolean[R][C];
            char[][] cg = new char[R][C];
            int[][] ig = new int[R][C];
            for (int i = 0; i < R; i++)
                for (int j = 0; j < C; j++) {
                    land[i][j] = r.nextInt(100) < 55;
                    cg[i][j] = land[i][j] ? '1' : '0';
                    ig[i][j] = land[i][j] ? 1 : 0;
                }
            int[] gl = labels(R * C, gridEdges(R, C, land));
            Set<Integer> islands = new HashSet<>();
            Map<Integer, Integer> size = new HashMap<>();
            int best = 0;
            for (int v = 0; v < R * C; v++)
                if (land[v / C][v % C]) {
                    islands.add(gl[v]);
                    best = Math.max(best, size.merge(gl[v], 1, Integer::sum));
                }
            Check.eq(islands.size(), numIslands(cg), "islands " + Check.str(ig));
            Check.eq(best, maxAreaOfIsland(ig), "area " + Check.str(ig));
        }
        // a path of 10^6 nodes: the iterative version has no depth limit
        int big = 1_000_000;
        int[][] path = new int[big - 1][];
        for (int i = 0; i + 1 < big; i++) path[i] = new int[] {i, i + 1};
        boolean[] seen = new boolean[big];
        dfsIter(build(big, path), 0, seen);
        Check.isTrue(seen[big - 1], "deep path reached by dfsIter");

        char[][] s1 = {"11110".toCharArray(), "11010".toCharArray(), "11000".toCharArray(), "00000".toCharArray()};
        Check.eq(1, numIslands(s1), "LC 200 sample 1");
        char[][] s2 = {"11000".toCharArray(), "11000".toCharArray(), "00100".toCharArray(), "00011".toCharArray()};
        Check.eq(3, numIslands(s2), "LC 200 sample 2");
        int[][] a1 = {{0,0,1,0,0,0,0,1,0,0,0,0,0},{0,0,0,0,0,0,0,1,1,1,0,0,0},{0,1,1,0,1,0,0,0,0,0,0,0,0},
                      {0,1,0,0,1,1,0,0,1,0,1,0,0},{0,1,0,0,1,1,0,0,1,1,1,0,0},{0,0,0,0,0,0,0,0,0,0,1,0,0},
                      {0,0,0,0,0,0,0,1,1,1,0,0,0},{0,0,0,0,0,0,0,1,1,0,0,0,0}};
        Check.eq(6, maxAreaOfIsland(a1), "LC 695 sample 1");
        Check.eq(0, maxAreaOfIsland(new int[][] {{0, 0, 0, 0, 0, 0, 0, 0}}), "LC 695 sample 2");
        boolean[] p1 = new boolean[3];
        dfs(build(3, new int[][] {{0, 1}, {1, 2}, {2, 0}}), 0, p1);
        Check.isTrue(p1[2], "LC 1971 sample 1");
        boolean[] p2 = new boolean[6];
        dfsIter(build(6, new int[][] {{0, 1}, {0, 2}, {3, 5}, {5, 4}, {4, 3}}), 0, p2);
        Check.isTrue(!p2[5], "LC 1971 sample 2");
        // LC 547 as an edge list: isConnected[i][j] == 1 is an edge
        Check.eq(2, countComponents(3, new int[][] {{0, 1}}), "LC 547 sample 1");
        Check.eq(3, countComponents(3, new int[0][]), "LC 547 sample 2");
        Check.done("P09GraphDfs");
    }
}
