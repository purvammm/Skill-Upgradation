import java.util.*;

public class P01Prefix2D {
    //@tpl matrix
    // Range sum query 2D. pre[i][j] = sum of rows 0..i-1, columns 0..j-1.
    // The extra row and column of zeros remove every bounds check.
    static class NumMatrix {
        private final long[][] pre;

        NumMatrix(int[][] m) {
            int rows = m.length, cols = m[0].length;
            pre = new long[rows + 1][cols + 1];
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++)
                    pre[i + 1][j + 1] = pre[i][j + 1] + pre[i + 1][j]
                                      - pre[i][j] + m[i][j];
        }

        // Inclusive corners (r1, c1) top-left and (r2, c2) bottom-right.
        long sumRegion(int r1, int c1, int r2, int c2) {
            return pre[r2 + 1][c2 + 1] - pre[r1][c2 + 1]
                 - pre[r2 + 1][c1] + pre[r1][c1];
        }
    }
    //@end

    //@tpl block
    // Matrix block sum: clip the (2k+1) x (2k+1) block to the grid,
    // then answer each cell with one sumRegion call.
    static int[][] matrixBlockSum(int[][] m, int k) {
        int rows = m.length, cols = m[0].length;
        NumMatrix ps = new NumMatrix(m);
        int[][] ans = new int[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++) {
                int r1 = Math.max(0, i - k), c1 = Math.max(0, j - k);
                int r2 = Math.min(rows - 1, i + k), c2 = Math.min(cols - 1, j + k);
                ans[i][j] = (int) ps.sumRegion(r1, c1, r2, c2);
            }
        return ans;
    }
    //@end

    //@tpl target
    // Submatrices that sum to target in O(rows^2 * cols): fix the top and
    // bottom rows, squash them into column sums, then count 1D subarrays
    // with sum target using a prefix sum + hash map.
    static int numSubmatrixSumTarget(int[][] m, int target) {
        int rows = m.length, cols = m[0].length, count = 0;
        for (int top = 0; top < rows; top++) {
            int[] col = new int[cols];             // col[j] = m[top..bot][j]
            for (int bot = top; bot < rows; bot++) {
                for (int j = 0; j < cols; j++) col[j] += m[bot][j];
                Map<Integer, Integer> seen = new HashMap<>();
                seen.put(0, 1);
                int pre = 0;
                for (int j = 0; j < cols; j++) {
                    pre += col[j];
                    count += seen.getOrDefault(pre - target, 0);
                    seen.merge(pre, 1, Integer::sum);
                }
            }
        }
        return count;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static long bruteRegion(int[][] m, int r1, int c1, int r2, int c2) {
        long s = 0;
        for (int i = r1; i <= r2; i++)
            for (int j = c1; j <= c2; j++) s += m[i][j];
        return s;
    }

    static int[][] bruteBlock(int[][] m, int k) {
        int rows = m.length, cols = m[0].length;
        int[][] ans = new int[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                for (int a = 0; a < rows; a++)
                    for (int b = 0; b < cols; b++)
                        if (Math.abs(a - i) <= k && Math.abs(b - j) <= k) ans[i][j] += m[a][b];
        return ans;
    }

    static int bruteTarget(int[][] m, int target) {
        int rows = m.length, cols = m[0].length, c = 0;
        for (int r1 = 0; r1 < rows; r1++)
            for (int r2 = r1; r2 < rows; r2++)
                for (int c1 = 0; c1 < cols; c1++)
                    for (int c2 = c1; c2 < cols; c2++)
                        if (bruteRegion(m, r1, c1, r2, c2) == target) c++;
        return c;
    }

    static int[][] randomMatrix(Random r, int rows, int cols, int lo, int hi) {
        int[][] m = new int[rows][];
        for (int i = 0; i < rows; i++) m[i] = Check.ints(r, cols, lo, hi);
        return m;
    }

    public static void main(String[] args) {
        Random r = new Random(505);
        for (int t = 0; t < 1500; t++) {
            int rows = 1 + r.nextInt(5), cols = 1 + r.nextInt(5);
            int[][] m = randomMatrix(r, rows, cols, -5, 9);
            NumMatrix nm = new NumMatrix(m);
            for (int q = 0; q < 10; q++) {
                int r1 = r.nextInt(rows), r2 = r1 + r.nextInt(rows - r1);
                int c1 = r.nextInt(cols), c2 = c1 + r.nextInt(cols - c1);
                Check.eq(bruteRegion(m, r1, c1, r2, c2), nm.sumRegion(r1, c1, r2, c2),
                        "sumRegion " + Arrays.deepToString(m));
            }
            int k = r.nextInt(4);
            Check.eq(bruteBlock(m, k), matrixBlockSum(m, k), "block " + Arrays.deepToString(m) + " k=" + k);
            int[][] s = randomMatrix(r, rows, cols, -2, 2);
            int target = r.nextInt(5) - 2;
            Check.eq(bruteTarget(s, target), numSubmatrixSumTarget(s, target),
                    "target " + Arrays.deepToString(s) + " t=" + target);
        }
        int[][] lc = {{3, 0, 1, 4, 2}, {5, 6, 3, 2, 1}, {1, 2, 0, 1, 5}, {4, 1, 0, 1, 7}, {1, 0, 3, 0, 5}};
        NumMatrix nm = new NumMatrix(lc);
        Check.eq(8, nm.sumRegion(2, 1, 4, 3), "LC 304 sample a");
        Check.eq(11, nm.sumRegion(1, 1, 2, 2), "LC 304 sample b");
        Check.eq(12, nm.sumRegion(1, 2, 2, 4), "LC 304 sample c");
        Check.eq(new int[][] {{12, 21, 16}, {27, 45, 33}, {24, 39, 28}},
                matrixBlockSum(new int[][] {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}}, 1), "LC 1314 sample 1");
        Check.eq(new int[][] {{45, 45, 45}, {45, 45, 45}, {45, 45, 45}},
                matrixBlockSum(new int[][] {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}}, 2), "LC 1314 sample 2");
        Check.eq(4, numSubmatrixSumTarget(new int[][] {{0, 1, 0}, {1, 1, 1}, {0, 1, 0}}, 0), "LC 1074 sample 1");
        Check.eq(5, numSubmatrixSumTarget(new int[][] {{1, -1}, {-1, 1}}, 0), "LC 1074 sample 2");
        Check.eq(0, numSubmatrixSumTarget(new int[][] {{904}}, 0), "LC 1074 sample 3");
        Check.done("P01Prefix2D");
    }
}
