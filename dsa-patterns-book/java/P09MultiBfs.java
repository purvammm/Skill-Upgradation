import java.util.*;

public class P09MultiBfs {
    //@tpl oranges
    static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    // Rotting oranges: every rotten orange is in the queue at minute 0.
    static int orangesRotting(int[][] grid) {
        int m = grid.length, n = grid[0].length, fresh = 0, minutes = 0;
        Deque<int[]> q = new ArrayDeque<>();
        for (int r = 0; r < m; r++)
            for (int c = 0; c < n; c++) {
                if (grid[r][c] == 2) q.add(new int[] {r, c});
                if (grid[r][c] == 1) fresh++;
            }
        while (fresh > 0 && !q.isEmpty()) {
            minutes++;                            // one level = one minute
            for (int size = q.size(); size > 0; size--) {
                int[] cur = q.poll();
                for (int[] d : DIRS) {
                    int r = cur[0] + d[0], c = cur[1] + d[1];
                    if (r < 0 || c < 0 || r >= m || c >= n) continue;
                    if (grid[r][c] != 1) continue;
                    grid[r][c] = 2;               // rots now: marked when pushed
                    fresh--;
                    q.add(new int[] {r, c});
                }
            }
        }
        return fresh == 0 ? minutes : -1;
    }
    //@end

    //@tpl nearest
    // dist[r][c] = steps from (r, c) to the nearest cell whose value is src.
    // All src cells start at 0; any other cell gets its dist once, when pushed.
    static int[][] nearest(int[][] grid, int src) {
        int m = grid.length, n = grid[0].length;
        int[][] dist = new int[m][n];
        Deque<int[]> q = new ArrayDeque<>();
        for (int r = 0; r < m; r++)
            for (int c = 0; c < n; c++) {
                dist[r][c] = grid[r][c] == src ? 0 : -1;
                if (dist[r][c] == 0) q.add(new int[] {r, c});
            }
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            for (int[] d : DIRS) {
                int r = cur[0] + d[0], c = cur[1] + d[1];
                if (r < 0 || c < 0 || r >= m || c >= n || dist[r][c] != -1) continue;
                dist[r][c] = dist[cur[0]][cur[1]] + 1;
                q.add(new int[] {r, c});
            }
        }
        return dist;
    }

    // 01 matrix: distance to the nearest 0.
    static int[][] updateMatrix(int[][] mat) { return nearest(mat, 0); }

    // As far from land as possible: the largest distance to the nearest 1.
    static int maxDistance(int[][] grid) {
        int best = -1;
        for (int[] row : nearest(grid, 1))
            for (int d : row) best = Math.max(best, d);
        return best <= 0 ? -1 : best;             // 0: no water, -1: no land
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Minute-by-minute simulation.
    static int bruteRot(int[][] g0) {
        int m = g0.length, n = g0[0].length;
        int[][] g = new int[m][];
        for (int i = 0; i < m; i++) g[i] = g0[i].clone();
        for (int minute = 0; ; minute++) {
            int fresh = 0;
            for (int[] row : g) for (int x : row) if (x == 1) fresh++;
            if (fresh == 0) return minute;
            List<int[]> rot = new ArrayList<>();
            for (int r = 0; r < m; r++)
                for (int c = 0; c < n; c++) {
                    if (g[r][c] != 1) continue;
                    for (int[] d : DIRS) {
                        int rr = r + d[0], cc = c + d[1];
                        if (rr >= 0 && cc >= 0 && rr < m && cc < n && g[rr][cc] == 2) { rot.add(new int[] {r, c}); break; }
                    }
                }
            if (rot.isEmpty()) return -1;
            for (int[] p : rot) g[p[0]][p[1]] = 2;
        }
    }

    // No walls, so the BFS distance is the Manhattan distance to the nearest src cell.
    static int[][] bruteNearest(int[][] g, int src) {
        int m = g.length, n = g[0].length;
        int[][] d = new int[m][n];
        for (int r = 0; r < m; r++)
            for (int c = 0; c < n; c++) {
                int best = -1;
                for (int i = 0; i < m; i++)
                    for (int j = 0; j < n; j++)
                        if (g[i][j] == src) {
                            int dd = Math.abs(i - r) + Math.abs(j - c);
                            if (best == -1 || dd < best) best = dd;
                        }
                d[r][c] = best;
            }
        return d;
    }

    static int bruteMaxDistance(int[][] g) {
        int m = g.length, n = g[0].length, best = -1;
        boolean land = false, water = false;
        for (int[] row : g) for (int x : row) { if (x == 1) land = true; else water = true; }
        if (!land || !water) return -1;
        for (int r = 0; r < m; r++)
            for (int c = 0; c < n; c++) {
                if (g[r][c] != 0) continue;
                int near = Integer.MAX_VALUE;
                for (int i = 0; i < m; i++)
                    for (int j = 0; j < n; j++)
                        if (g[i][j] == 1) near = Math.min(near, Math.abs(i - r) + Math.abs(j - c));
                best = Math.max(best, near);
            }
        return best;
    }

    static int[][] copy(int[][] a) {
        int[][] b = new int[a.length][];
        for (int i = 0; i < a.length; i++) b[i] = a[i].clone();
        return b;
    }

    public static void main(String[] args) {
        Random r = new Random(93);
        for (int t = 0; t < 4000; t++) {
            int m = 1 + r.nextInt(5), n = 1 + r.nextInt(6);
            int[][] g = new int[m][n];
            for (int i = 0; i < m; i++) for (int j = 0; j < n; j++) g[i][j] = r.nextInt(3);
            Check.eq(bruteRot(g), orangesRotting(copy(g)), "rot " + Check.str(g));
            int[][] b = new int[m][n];
            int p = r.nextInt(101);
            for (int i = 0; i < m; i++) for (int j = 0; j < n; j++) b[i][j] = r.nextInt(100) < p ? 1 : 0;
            Check.eq(bruteNearest(b, 0), updateMatrix(b), "01 " + Check.str(b));
            Check.eq(bruteNearest(b, 1), nearest(b, 1), "nearest 1 " + Check.str(b));
            Check.eq(bruteMaxDistance(b), maxDistance(b), "far " + Check.str(b));
        }
        Check.eq(4, orangesRotting(new int[][] {{2, 1, 1}, {1, 1, 0}, {0, 1, 1}}), "LC 994 sample 1");
        Check.eq(-1, orangesRotting(new int[][] {{2, 1, 1}, {0, 1, 1}, {1, 0, 1}}), "LC 994 sample 2");
        Check.eq(0, orangesRotting(new int[][] {{0, 2}}), "LC 994 sample 3");
        Check.eq(new int[][] {{0, 0, 0}, {0, 1, 0}, {1, 2, 1}},
                 updateMatrix(new int[][] {{0, 0, 0}, {0, 1, 0}, {1, 1, 1}}), "LC 542 sample 2");
        Check.eq(2, maxDistance(new int[][] {{1, 0, 1}, {0, 0, 0}, {1, 0, 1}}), "LC 1162 sample 1");
        Check.eq(4, maxDistance(new int[][] {{1, 0, 0}, {0, 0, 0}, {0, 0, 0}}), "LC 1162 sample 2");
        Check.done("P09MultiBfs");
    }
}
