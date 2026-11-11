import java.util.*;

public class P09ZeroOneBfs {
    //@tpl valid
    // Minimum cost to make at least one valid path (LC 1368). Following the
    // cell's arrow costs 0; any other move costs 1 (change the arrow once).
    static int minCost(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[] dr = {0, 0, 0, 1, -1}, dc = {0, 1, -1, 0, 0};  // arrows 1..4: R L D U
        int[] dist = new int[m * n];                       // cell id = r * n + c
        Arrays.fill(dist, Integer.MAX_VALUE);
        Deque<Integer> dq = new ArrayDeque<>();
        dist[0] = 0;
        dq.addFirst(0);
        while (!dq.isEmpty()) {
            int cell = dq.pollFirst(), r = cell / n, c = cell % n;
            for (int k = 1; k <= 4; k++) {
                int nr = r + dr[k], nc = c + dc[k];
                if (nr < 0 || nr >= m || nc < 0 || nc >= n) continue;
                int w = grid[r][c] == k ? 0 : 1;
                int next = nr * n + nc;
                if (dist[cell] + w < dist[next]) {
                    dist[next] = dist[cell] + w;
                    if (w == 0) dq.addFirst(next);         // same distance: front
                    else dq.addLast(next);                 // one more: back
                }
            }
        }
        return dist[m * n - 1];
    }
    //@end

    //@tpl obstacles
    // Minimum obstacle removal to reach corner (LC 2290): stepping onto a
    // cell costs grid value (0 = empty, 1 = obstacle to remove).
    static int minimumObstacles(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[][] dist = new int[m][n];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        Deque<int[]> dq = new ArrayDeque<>();
        dist[0][0] = 0;
        dq.add(new int[] {0, 0});
        while (!dq.isEmpty()) {
            int[] cur = dq.pollFirst();
            int r = cur[0], c = cur[1];
            for (int k = 0; k < 4; k++) {
                int nr = r + dr[k], nc = c + dc[k];
                if (nr < 0 || nr >= m || nc < 0 || nc >= n) continue;
                int nd = dist[r][c] + grid[nr][nc];
                if (nd >= dist[nr][nc]) continue;
                dist[nr][nc] = nd;
                if (grid[nr][nc] == 0) dq.addFirst(new int[] {nr, nc});
                else dq.addLast(new int[] {nr, nc});
            }
        }
        return dist[m - 1][n - 1];
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Bellman-Ford style: relax every move until nothing changes.
    static int bruteValid(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[] dr = {0, 0, 0, 1, -1}, dc = {0, 1, -1, 0, 0};
        int[][] d = new int[m][n];
        for (int[] row : d) Arrays.fill(row, 1 << 29);
        d[0][0] = 0;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int r = 0; r < m; r++)
                for (int c = 0; c < n; c++)
                    for (int k = 1; k <= 4; k++) {
                        int nr = r + dr[k], nc = c + dc[k];
                        if (nr < 0 || nr >= m || nc < 0 || nc >= n) continue;
                        int nd = d[r][c] + (grid[r][c] == k ? 0 : 1);
                        if (nd < d[nr][nc]) { d[nr][nc] = nd; changed = true; }
                    }
        }
        return d[m - 1][n - 1];
    }

    // try every set of removed obstacles, smallest first
    static int bruteObstacles(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        List<int[]> obs = new ArrayList<>();
        for (int r = 0; r < m; r++) for (int c = 0; c < n; c++) if (grid[r][c] == 1) obs.add(new int[] {r, c});
        int best = Integer.MAX_VALUE;
        for (int mask = 0; mask < (1 << obs.size()); mask++) {
            int bits = Integer.bitCount(mask);
            if (bits >= best) continue;
            boolean[][] open = new boolean[m][n];
            for (int r = 0; r < m; r++) for (int c = 0; c < n; c++) open[r][c] = grid[r][c] == 0;
            for (int i = 0; i < obs.size(); i++) if ((mask >> i & 1) == 1) open[obs.get(i)[0]][obs.get(i)[1]] = true;
            boolean[][] seen = new boolean[m][n];
            Deque<int[]> q = new ArrayDeque<>();
            q.add(new int[] {0, 0});
            seen[0][0] = true;
            int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
            while (!q.isEmpty()) {
                int[] cur = q.poll();
                for (int k = 0; k < 4; k++) {
                    int nr = cur[0] + dr[k], nc = cur[1] + dc[k];
                    if (nr < 0 || nr >= m || nc < 0 || nc >= n || seen[nr][nc] || !open[nr][nc]) continue;
                    seen[nr][nc] = true;
                    q.add(new int[] {nr, nc});
                }
            }
            if (seen[m - 1][n - 1]) best = bits;
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(21);
        for (int t = 0; t < 3000; t++) {
            int m = 1 + r.nextInt(5), n = 1 + r.nextInt(5);
            int[][] g = new int[m][];
            for (int i = 0; i < m; i++) g[i] = Check.ints(r, n, 1, 4);
            Check.eq(bruteValid(g), minCost(g), "valid " + Arrays.deepToString(g));
        }
        for (int t = 0; t < 3000; t++) {
            int m = 1 + r.nextInt(4), n = 1 + r.nextInt(4);
            int[][] g = new int[m][];
            for (int i = 0; i < m; i++) g[i] = Check.ints(r, n, 0, 1);
            g[0][0] = 0;
            g[m - 1][n - 1] = 0;
            Check.eq(bruteObstacles(g), minimumObstacles(g), "obstacles " + Arrays.deepToString(g));
        }
        // bigger grids: the two templates agree with the slow relaxation
        for (int t = 0; t < 50; t++) {
            int m = 20 + r.nextInt(20), n = 20 + r.nextInt(20);
            int[][] g = new int[m][];
            for (int i = 0; i < m; i++) g[i] = Check.ints(r, n, 1, 4);
            Check.eq(bruteValid(g), minCost(g), "valid big");
        }
        Check.eq(3, minCost(new int[][] {{1, 1, 1, 1}, {2, 2, 2, 2}, {1, 1, 1, 1}, {2, 2, 2, 2}}), "LC 1368 sample 1");
        Check.eq(0, minCost(new int[][] {{1, 1, 3}, {3, 2, 2}, {1, 1, 4}}), "LC 1368 sample 2");
        Check.eq(1, minCost(new int[][] {{1, 2}, {4, 3}}), "LC 1368 sample 3");
        Check.eq(2, minimumObstacles(new int[][] {{0, 1, 1}, {1, 1, 0}, {1, 1, 0}}), "LC 2290 sample 1");
        Check.eq(0, minimumObstacles(new int[][] {{0, 1, 0, 0, 0}, {0, 1, 0, 1, 0}, {0, 0, 0, 1, 0}}), "LC 2290 sample 2");
        Check.done("P09ZeroOneBfs");
    }
}
