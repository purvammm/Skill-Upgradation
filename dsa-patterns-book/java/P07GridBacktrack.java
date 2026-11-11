import java.util.*;

public class P07GridBacktrack {
    //@tpl wordsearch
    static final int[] DR = {-1, 1, 0, 0}, DC = {0, 0, -1, 1};

    static boolean exist(char[][] board, String word) {
        for (int r = 0; r < board.length; r++)
            for (int c = 0; c < board[0].length; c++)
                if (match(board, word, 0, r, c)) return true;
        return false;
    }

    // Can word[k..] be spelled starting at (r, c)? The cell is marked while in use.
    static boolean match(char[][] b, String w, int k, int r, int c) {
        if (r < 0 || c < 0 || r >= b.length || c >= b[0].length) return false;
        if (b[r][c] != w.charAt(k)) return false;   // a '#' (in use) never matches
        if (k == w.length() - 1) return true;
        char keep = b[r][c];
        b[r][c] = '#';                              // mark: on the current path
        boolean found = false;
        for (int d = 0; d < 4 && !found; d++)
            found = match(b, w, k + 1, r + DR[d], c + DC[d]);
        b[r][c] = keep;                             // restore for other paths
        return found;
    }
    //@end

    //@tpl gold
    // Path with maximum gold: start anywhere, never step on 0, never reuse a cell.
    static int getMaximumGold(int[][] g) {
        int best = 0;
        for (int r = 0; r < g.length; r++)
            for (int c = 0; c < g[0].length; c++)
                if (g[r][c] > 0) best = Math.max(best, collect(g, r, c));
        return best;
    }

    static int collect(int[][] g, int r, int c) {
        if (r < 0 || c < 0 || r >= g.length || c >= g[0].length || g[r][c] == 0)
            return 0;
        int gold = g[r][c], more = 0;
        g[r][c] = 0;                                // mark by emptying the cell
        for (int d = 0; d < 4; d++)
            more = Math.max(more, collect(g, r + DR[d], c + DC[d]));
        g[r][c] = gold;                             // restore
        return gold + more;
    }
    //@end

    //@tpl paths3
    // Unique paths III: walk from 1 to 2 over every 0 exactly once; -1 is a wall.
    static int uniquePathsIII(int[][] g) {
        int sr = 0, sc = 0, open = 0;
        for (int r = 0; r < g.length; r++)
            for (int c = 0; c < g[0].length; c++) {
                if (g[r][c] != -1) open++;          // cells the path must cover
                if (g[r][c] == 1) { sr = r; sc = c; }
            }
        return walk(g, sr, sc, open);
    }

    // left = cells still to visit, counting (r, c) itself
    static int walk(int[][] g, int r, int c, int left) {
        if (r < 0 || c < 0 || r >= g.length || c >= g[0].length || g[r][c] == -1)
            return 0;
        if (g[r][c] == 2) return left == 1 ? 1 : 0; // at the end: all covered?
        int keep = g[r][c], ways = 0;
        g[r][c] = -1;                               // mark: a wall while on the path
        for (int d = 0; d < 4; d++)
            ways += walk(g, r + DR[d], c + DC[d], left - 1);
        g[r][c] = keep;
        return ways;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Brute force without the template's recursion: dynamic programming over
    // (set of visited cells, current cell) for grids with at most 12 cells.
    static boolean bruteExist(char[][] b, String w) {
        int R = b.length, C = b[0].length, N = R * C;
        Set<Long> layer = new HashSet<>();
        for (int v = 0; v < N; v++) if (b[v / C][v % C] == w.charAt(0)) layer.add(((long) (1 << v) << 4) | v);
        for (int k = 1; k < w.length() && !layer.isEmpty(); k++) {
            Set<Long> next = new HashSet<>();
            for (long s : layer) {
                int mask = (int) (s >> 4), v = (int) (s & 15);
                for (int u = 0; u < N; u++) {
                    if ((mask >> u & 1) == 1 || Math.abs(u / C - v / C) + Math.abs(u % C - v % C) != 1) continue;
                    if (b[u / C][u % C] == w.charAt(k)) next.add(((long) (mask | 1 << u) << 4) | u);
                }
            }
            layer = next;
        }
        return !layer.isEmpty();
    }

    static boolean adj(int u, int v, int C) {
        return Math.abs(u / C - v / C) + Math.abs(u % C - v % C) == 1;
    }

    static int bruteGold(int[][] g) {
        int R = g.length, C = g[0].length, N = R * C, best = 0;
        boolean[][] seen = new boolean[1 << N][N];
        for (int v = 0; v < N; v++) if (g[v / C][v % C] > 0) seen[1 << v][v] = true;
        for (int mask = 1; mask < (1 << N); mask++)
            for (int v = 0; v < N; v++) {
                if (!seen[mask][v]) continue;
                int sum = 0;
                for (int u = 0; u < N; u++) if ((mask >> u & 1) == 1) sum += g[u / C][u % C];
                best = Math.max(best, sum);
                for (int u = 0; u < N; u++)
                    if ((mask >> u & 1) == 0 && adj(u, v, C) && g[u / C][u % C] > 0) seen[mask | 1 << u][u] = true;
            }
        return best;
    }

    static int brutePaths(int[][] g) {
        int R = g.length, C = g[0].length, N = R * C, start = -1, end = -1, full = 0;
        for (int v = 0; v < N; v++) {
            int x = g[v / C][v % C];
            if (x != -1) full |= 1 << v;
            if (x == 1) start = v;
            if (x == 2) end = v;
        }
        long[][] ways = new long[1 << N][N];
        ways[1 << start][start] = 1;
        for (int mask = 1; mask < (1 << N); mask++)
            for (int v = 0; v < N; v++) {
                if (ways[mask][v] == 0 || v == end) continue;
                for (int u = 0; u < N; u++)
                    if ((full >> u & 1) == 1 && (mask >> u & 1) == 0 && adj(u, v, C))
                        ways[mask | 1 << u][u] += ways[mask][v];
            }
        return (int) ways[full][end];
    }

    public static void main(String[] args) {
        Random r = new Random(76);
        for (int t = 0; t < 1500; t++) {
            int R = 1 + r.nextInt(3), C = 1 + r.nextInt(4);
            char[][] b = new char[R][C];
            for (int i = 0; i < R; i++) b[i] = Check.str(r, C, "ABC").toCharArray();
            String w = Check.str(r, 1 + r.nextInt(Math.min(6, R * C)), "ABC");
            Check.eq(bruteExist(b, w), exist(b, w), "exist " + Check.str(b) + " " + w);
            int[][] g = new int[R][C];
            for (int i = 0; i < R; i++) for (int j = 0; j < C; j++) g[i][j] = r.nextInt(3) == 0 ? 0 : 1 + r.nextInt(9);
            Check.eq(bruteGold(g), getMaximumGold(g), "gold " + Check.str(g));
            if (R * C >= 2) {
                int[][] p = new int[R][C];
                for (int i = 0; i < R; i++) for (int j = 0; j < C; j++) p[i][j] = r.nextInt(4) == 0 ? -1 : 0;
                int s = r.nextInt(R * C), e;
                do e = r.nextInt(R * C); while (e == s);
                p[s / C][s % C] = 1;
                p[e / C][e % C] = 2;
                Check.eq(brutePaths(p), uniquePathsIII(p), "paths3 " + Check.str(p));
            }
        }
        char[][] board = {"ABCE".toCharArray(), "SFCS".toCharArray(), "ADEE".toCharArray()};
        Check.eq(true, exist(board, "ABCCED"), "LC 79 sample 1");
        Check.eq(true, exist(board, "SEE"), "LC 79 sample 2");
        Check.eq(false, exist(board, "ABCB"), "LC 79 sample 3");
        Check.eq("[ABCE, SFCS, ADEE]", Arrays.asList(new String(board[0]), new String(board[1]),
                 new String(board[2])).toString(), "board restored");
        Check.eq(24, getMaximumGold(new int[][] {{0, 6, 0}, {5, 8, 7}, {0, 9, 0}}), "LC 1219 sample 1");
        Check.eq(28, getMaximumGold(new int[][] {{1, 0, 7}, {2, 0, 6}, {3, 4, 5}, {0, 3, 0}, {9, 0, 20}}),
                 "LC 1219 sample 2");
        Check.eq(2, uniquePathsIII(new int[][] {{1, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 2, -1}}), "LC 980 sample 1");
        Check.eq(4, uniquePathsIII(new int[][] {{1, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 2}}), "LC 980 sample 2");
        Check.eq(0, uniquePathsIII(new int[][] {{0, 1}, {2, 0}}), "LC 980 sample 3");
        Check.done("P07GridBacktrack");
    }
}
