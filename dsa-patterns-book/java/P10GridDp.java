import java.util.*;

public class P10GridDp {
    //@tpl paths
    // Unique paths II (moves: right or down; 1 = rock). ways[c] is one row:
    // before the update it holds the row above, after it the current row.
    static int uniquePathsWithObstacles(int[][] g) {
        int n = g[0].length;
        int[] ways = new int[n];
        ways[0] = 1;                                   // one way to stand on (0, 0)
        for (int[] row : g)
            for (int c = 0; c < n; c++) {
                if (row[c] == 1) ways[c] = 0;          // nobody passes a rock
                else if (c > 0) ways[c] += ways[c - 1];    // from above + from left
            }
        return ways[n - 1];
    }

    // Minimum path sum, in place: g[r][c] becomes the cheapest cost to reach it.
    static int minPathSum(int[][] g) {
        int m = g.length, n = g[0].length;
        for (int r = 0; r < m; r++)
            for (int c = 0; c < n; c++) {
                if (r == 0 && c == 0) continue;
                int up = r > 0 ? g[r - 1][c] : Integer.MAX_VALUE;
                int left = c > 0 ? g[r][c - 1] : Integer.MAX_VALUE;
                g[r][c] += Math.min(up, left);
            }
        return g[m - 1][n - 1];
    }
    //@end

    //@tpl square
    // Maximal square: side[r][c] = largest all-'1' square whose bottom-right
    // corner is cell (r-1, c-1). Row 0 and column 0 are padding.
    static int maximalSquare(char[][] g) {
        int m = g.length, n = g[0].length, best = 0;
        int[][] side = new int[m + 1][n + 1];
        for (int r = 1; r <= m; r++)
            for (int c = 1; c <= n; c++)
                if (g[r - 1][c - 1] == '1') {
                    side[r][c] = 1 + Math.min(side[r - 1][c - 1],
                                     Math.min(side[r - 1][c], side[r][c - 1]));
                    best = Math.max(best, side[r][c]);
                }
        return best * best;
    }
    //@end

    //@tpl dungeon
    // Dungeon game: need[r][c] = least health on entering (r, c) that still
    // reaches the end alive. It depends on the cells after, so fill backwards.
    static int calculateMinimumHP(int[][] d) {
        int m = d.length, n = d[0].length;
        int[][] need = new int[m + 1][n + 1];          // row m, column n: walls
        for (int[] row : need) Arrays.fill(row, Integer.MAX_VALUE);
        need[m][n - 1] = need[m - 1][n] = 1;           // leave the end with 1
        for (int r = m - 1; r >= 0; r--)
            for (int c = n - 1; c >= 0; c--) {
                int next = Math.min(need[r + 1][c], need[r][c + 1]);
                need[r][c] = Math.max(1, next - d[r][c]);
            }
        return need[0][0];
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int brutePaths(int[][] g, int r, int c) {
        int m = g.length, n = g[0].length;
        if (r >= m || c >= n || g[r][c] == 1) return 0;
        if (r == m - 1 && c == n - 1) return 1;
        return brutePaths(g, r + 1, c) + brutePaths(g, r, c + 1);
    }

    static int bruteMin(int[][] g, int r, int c) {
        int m = g.length, n = g[0].length;
        if (r >= m || c >= n) return Integer.MAX_VALUE / 2;
        if (r == m - 1 && c == n - 1) return g[r][c];
        return g[r][c] + Math.min(bruteMin(g, r + 1, c), bruteMin(g, r, c + 1));
    }

    static int bruteSquare(char[][] g) {
        int m = g.length, n = g[0].length, best = 0;
        for (int r = 0; r < m; r++)
            for (int c = 0; c < n; c++)
                for (int k = 1; r + k <= m && c + k <= n; k++) {
                    boolean all = true;
                    for (int i = r; i < r + k; i++)
                        for (int j = c; j < c + k; j++) if (g[i][j] != '1') all = false;
                    if (all) best = Math.max(best, k * k);
                }
        return best;
    }

    // Every right/down path; the start health must keep every prefix sum above 0.
    static int bruteHP(int[][] d, int r, int c, int sum, int minPrefix) {
        int m = d.length, n = d[0].length;
        if (r >= m || c >= n) return Integer.MAX_VALUE;
        sum += d[r][c];
        minPrefix = Math.min(minPrefix, sum);
        if (r == m - 1 && c == n - 1) return Math.max(1, 1 - minPrefix);
        return Math.min(bruteHP(d, r + 1, c, sum, minPrefix), bruteHP(d, r, c + 1, sum, minPrefix));
    }

    static int[][] copy(int[][] g) {
        int[][] h = new int[g.length][];
        for (int i = 0; i < g.length; i++) h[i] = g[i].clone();
        return h;
    }

    public static void main(String[] args) {
        Random r = new Random(1003);
        for (int t = 0; t < 3000; t++) {
            int m = 1 + r.nextInt(6), n = 1 + r.nextInt(6);
            int[][] rocks = new int[m][n];
            for (int[] row : rocks) for (int c = 0; c < n; c++) row[c] = r.nextInt(4) == 0 ? 1 : 0;
            Check.eq(brutePaths(rocks, 0, 0), uniquePathsWithObstacles(rocks), "paths " + Arrays.deepToString(rocks));

            int[][] g = new int[m][n];
            for (int[] row : g) for (int c = 0; c < n; c++) row[c] = r.nextInt(10);
            Check.eq(bruteMin(g, 0, 0), minPathSum(copy(g)), "min " + Arrays.deepToString(g));

            char[][] ch = new char[m][n];
            for (char[] row : ch) for (int c = 0; c < n; c++) row[c] = r.nextInt(4) == 0 ? '0' : '1';
            Check.eq(bruteSquare(ch), maximalSquare(ch), "square " + Arrays.deepToString(ch));

            int[][] d = new int[m][n];
            for (int[] row : d) for (int c = 0; c < n; c++) row[c] = r.nextInt(21) - 12;
            Check.eq(bruteHP(d, 0, 0, 0, 0), calculateMinimumHP(d), "dungeon " + Arrays.deepToString(d));
        }
        Check.eq(2, uniquePathsWithObstacles(new int[][] {{0, 0, 0}, {0, 1, 0}, {0, 0, 0}}), "LC 63 sample 1");
        Check.eq(1, uniquePathsWithObstacles(new int[][] {{0, 1}, {0, 0}}), "LC 63 sample 2");
        Check.eq(7, minPathSum(new int[][] {{1, 3, 1}, {1, 5, 1}, {4, 2, 1}}), "LC 64 sample 1");
        Check.eq(12, minPathSum(new int[][] {{1, 2, 3}, {4, 5, 6}}), "LC 64 sample 2");
        char[][] sq = {"10100".toCharArray(), "10111".toCharArray(), "11111".toCharArray(), "10010".toCharArray()};
        Check.eq(4, maximalSquare(sq), "LC 221 sample 1");
        Check.eq(1, maximalSquare(new char[][] {"01".toCharArray(), "10".toCharArray()}), "LC 221 sample 2");
        Check.eq(0, maximalSquare(new char[][] {"0".toCharArray()}), "LC 221 sample 3");
        Check.eq(7, calculateMinimumHP(new int[][] {{-2, -3, 3}, {-5, -10, 1}, {10, 30, -5}}), "LC 174 sample 1");
        Check.eq(1, calculateMinimumHP(new int[][] {{0}}), "LC 174 sample 2");
        Check.done("P10GridDp");
    }
}
