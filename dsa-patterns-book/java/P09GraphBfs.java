import java.util.*;

public class P09GraphBfs {
    //@tpl bfs
    // dist[v] = fewest edges from src (-1: unreachable); parent[v] = node before v.
    static int[] bfs(List<Integer>[] g, int src, int[] parent) {
        int[] dist = new int[g.length];
        Arrays.fill(dist, -1);
        Arrays.fill(parent, -1);
        Deque<Integer> q = new ArrayDeque<>();
        dist[src] = 0;
        q.add(src);
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v : g[u])
                if (dist[v] == -1) {              // set on push: pushed once
                    dist[v] = dist[u] + 1;
                    parent[v] = u;
                    q.add(v);
                }
        }
        return dist;
    }

    // Path src -> dst: walk parent[] back from dst, adding at the front.
    static List<Integer> path(int[] parent, int[] dist, int dst) {
        LinkedList<Integer> p = new LinkedList<>();
        if (dist[dst] == -1) return p;            // unreachable: empty path
        for (int v = dst; v != -1; v = parent[v]) p.addFirst(v);
        return p;
    }
    //@end

    //@tpl matrix
    // Shortest clear path (0,0) -> (n-1,n-1) in 8 directions, counted in cells.
    static int shortestPathBinaryMatrix(int[][] grid) {
        int n = grid.length;
        if (grid[0][0] == 1 || grid[n - 1][n - 1] == 1) return -1;
        int[][] dist = new int[n][n];             // 0 = not reached
        Deque<int[]> q = new ArrayDeque<>();
        dist[0][0] = 1;
        q.add(new int[] {0, 0});
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            for (int dr = -1; dr <= 1; dr++)
                for (int dc = -1; dc <= 1; dc++) {    // dr = dc = 0: reached
                    int r = cur[0] + dr, c = cur[1] + dc;
                    if (r < 0 || c < 0 || r >= n || c >= n) continue;
                    if (grid[r][c] == 1 || dist[r][c] != 0) continue;
                    dist[r][c] = dist[cur[0]][cur[1]] + 1;
                    q.add(new int[] {r, c});
                }
        }
        return dist[n - 1][n - 1] == 0 ? -1 : dist[n - 1][n - 1];
    }
    //@end

    //@tpl maze
    static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    // Nearest exit from the entrance: BFS level by level, one level per step.
    static int nearestExit(char[][] maze, int[] entrance) {
        int m = maze.length, n = maze[0].length;
        Deque<int[]> q = new ArrayDeque<>();
        q.add(entrance);
        maze[entrance[0]][entrance[1]] = '+';     // mark as a wall when pushed
        for (int steps = 1; !q.isEmpty(); steps++) {
            for (int size = q.size(); size > 0; size--) {
                int[] cur = q.poll();
                for (int[] d : DIRS) {
                    int r = cur[0] + d[0], c = cur[1] + d[1];
                    if (r < 0 || c < 0 || r >= m || c >= n) continue;
                    if (maze[r][c] == '+') continue;
                    if (r == 0 || c == 0 || r == m - 1 || c == n - 1) return steps;
                    maze[r][c] = '+';
                    q.add(new int[] {r, c});
                }
            }
        }
        return -1;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static final int INF = 1 << 29;

    // Floyd-Warshall on unit weights.
    static int[][] floyd(int n, int[][] edges, boolean directed) {
        int[][] d = new int[n][n];
        for (int[] row : d) Arrays.fill(row, INF);
        for (int i = 0; i < n; i++) d[i][i] = 0;
        for (int[] e : edges) {
            if (e[0] != e[1]) d[e[0]][e[1]] = 1;
            if (!directed && e[0] != e[1]) d[e[1]][e[0]] = 1;
        }
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    d[i][j] = Math.min(d[i][j], d[i][k] + d[k][j]);
        return d;
    }

    @SuppressWarnings("unchecked")
    static List<Integer>[] graph(int n, int[][] edges, boolean directed) {
        List<Integer>[] g = new List[n];
        for (int v = 0; v < n; v++) g[v] = new ArrayList<>();
        for (int[] e : edges) {
            g[e[0]].add(e[1]);
            if (!directed) g[e[1]].add(e[0]);
        }
        return g;
    }

    // Cell graph distances by repeated relaxation (Bellman-Ford with unit weights).
    static int[] relaxCells(boolean[] open, int R, int C, int src, int[][] moves) {
        int N = R * C;
        int[] d = new int[N];
        Arrays.fill(d, INF);
        d[src] = 0;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int v = 0; v < N; v++) {
                if (d[v] == INF) continue;
                for (int[] mv : moves) {
                    int r = v / C + mv[0], c = v % C + mv[1];
                    if (r < 0 || c < 0 || r >= R || c >= C || !open[r * C + c]) continue;
                    if (d[v] + 1 < d[r * C + c]) { d[r * C + c] = d[v] + 1; changed = true; }
                }
            }
        }
        return d;
    }

    static final int[][] EIGHT = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    static int bruteBinary(int[][] grid) {
        int n = grid.length;
        boolean[] open = new boolean[n * n];
        for (int v = 0; v < n * n; v++) open[v] = grid[v / n][v % n] == 0;
        if (!open[0] || !open[n * n - 1]) return -1;
        int[] d = relaxCells(open, n, n, 0, EIGHT);
        return d[n * n - 1] == INF ? -1 : d[n * n - 1] + 1;
    }

    static int bruteExit(char[][] maze, int[] e) {
        int R = maze.length, C = maze[0].length;
        boolean[] open = new boolean[R * C];
        for (int v = 0; v < R * C; v++) open[v] = maze[v / C][v % C] == '.';
        int src = e[0] * C + e[1];
        int[] d = relaxCells(open, R, C, src, DIRS);
        int best = INF;
        for (int v = 0; v < R * C; v++) {
            int r = v / C, c = v % C;
            boolean border = r == 0 || c == 0 || r == R - 1 || c == C - 1;
            if (v != src && open[v] && border) best = Math.min(best, d[v]);
        }
        return best == INF ? -1 : best;
    }

    static char[][] copy(char[][] a) {
        char[][] b = new char[a.length][];
        for (int i = 0; i < a.length; i++) b[i] = a[i].clone();
        return b;
    }

    public static void main(String[] args) {
        Random r = new Random(92);
        for (int t = 0; t < 2000; t++) {
            int n = 1 + r.nextInt(10);
            int m = r.nextInt(2 * n + 1);
            int[][] edges = new int[m][];
            for (int i = 0; i < m; i++) edges[i] = new int[] {r.nextInt(n), r.nextInt(n)};
            boolean directed = r.nextBoolean();
            int[][] fl = floyd(n, edges, directed);
            List<Integer>[] g = graph(n, edges, directed);
            Set<Long> edgeSet = new HashSet<>();
            for (int[] e : edges) {
                edgeSet.add((long) e[0] * 100 + e[1]);
                if (!directed) edgeSet.add((long) e[1] * 100 + e[0]);
            }
            int src = r.nextInt(n);
            int[] parent = new int[n];
            int[] dist = bfs(g, src, parent);
            for (int v = 0; v < n; v++) {
                int exp = fl[src][v] >= INF ? -1 : fl[src][v];
                Check.eq(exp, dist[v], "bfs dist " + src + "->" + v + " " + Check.str(edges));
                List<Integer> p = path(parent, dist, v);
                if (exp == -1) Check.eq(0, p.size(), "no path");
                else {
                    Check.eq(exp + 1, p.size(), "path length");
                    Check.eq(src, (int) p.get(0), "path start");
                    Check.eq(v, (int) p.get(p.size() - 1), "path end");
                    for (int i = 0; i + 1 < p.size(); i++)
                        Check.isTrue(edgeSet.contains((long) p.get(i) * 100 + p.get(i + 1)), "path edge");
                }
            }
            // binary matrix
            int k = 1 + r.nextInt(5);
            int[][] grid = new int[k][k];
            for (int i = 0; i < k; i++) for (int j = 0; j < k; j++) grid[i][j] = r.nextInt(100) < 35 ? 1 : 0;
            Check.eq(bruteBinary(grid), shortestPathBinaryMatrix(grid), "binary " + Check.str(grid));
            // maze
            int R = 1 + r.nextInt(5), C = 1 + r.nextInt(5);
            char[][] maze = new char[R][C];
            for (int i = 0; i < R; i++) for (int j = 0; j < C; j++) maze[i][j] = r.nextInt(100) < 35 ? '+' : '.';
            int[] ent = {r.nextInt(R), r.nextInt(C)};
            maze[ent[0]][ent[1]] = '.';
            Check.eq(bruteExit(maze, ent), nearestExit(copy(maze), ent.clone()), "maze " + Check.str(maze));
        }
        Check.eq(2, shortestPathBinaryMatrix(new int[][] {{0, 1}, {1, 0}}), "LC 1091 sample 1");
        Check.eq(4, shortestPathBinaryMatrix(new int[][] {{0, 0, 0}, {1, 1, 0}, {1, 1, 0}}), "LC 1091 sample 2");
        Check.eq(-1, shortestPathBinaryMatrix(new int[][] {{1, 0, 0}, {1, 1, 0}, {1, 1, 0}}), "LC 1091 sample 3");
        char[][] m1 = {"++.+".toCharArray(), "...+".toCharArray(), "+++.".toCharArray()};
        Check.eq(1, nearestExit(m1, new int[] {1, 2}), "LC 1926 sample 1");
        char[][] m2 = {"+++".toCharArray(), "...".toCharArray(), "+++".toCharArray()};
        Check.eq(2, nearestExit(m2, new int[] {1, 0}), "LC 1926 sample 2");
        char[][] m3 = {".+".toCharArray()};
        Check.eq(-1, nearestExit(m3, new int[] {0, 0}), "LC 1926 sample 3");
        Check.done("P09GraphBfs");
    }
}
