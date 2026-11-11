import java.util.*;

public class P01MatrixMoves {
    //@tpl rotate
    // Rotate an n x n matrix 90 degrees clockwise, in place:
    // transpose (swap across the main diagonal), then reverse each row.
    static void rotate(int[][] m) {
        int n = m.length;
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++) {          // j > i: each pair once
                int t = m[i][j]; m[i][j] = m[j][i]; m[j][i] = t;
            }
        for (int[] row : m)
            for (int l = 0, r = n - 1; l < r; l++, r--) {
                int t = row[l]; row[l] = row[r]; row[r] = t;
            }
    }
    //@end

    //@tpl spiral
    // Spiral order: walk the outer ring, moving each boundary in after
    // its side is done. The ifs stop a leftover row or column being walked back.
    static List<Integer> spiralOrder(int[][] m) {
        List<Integer> out = new ArrayList<>();
        int top = 0, bottom = m.length - 1, left = 0, right = m[0].length - 1;
        while (top <= bottom && left <= right) {
            for (int j = left; j <= right; j++) out.add(m[top][j]);         // ->
            top++;
            for (int i = top; i <= bottom; i++) out.add(m[i][right]);       // down
            right--;
            if (top <= bottom) {
                for (int j = right; j >= left; j--) out.add(m[bottom][j]);  // <-
                bottom--;
            }
            if (left <= right) {
                for (int i = bottom; i >= top; i--) out.add(m[i][left]);    // up
                left++;
            }
        }
        return out;
    }
    //@end

    //@tpl zeroes
    // Set matrix zeroes with O(1) extra space: row 0 and column 0 hold the
    // marks, and two booleans remember whether they had a zero themselves.
    static void setZeroes(int[][] m) {
        int rows = m.length, cols = m[0].length;
        boolean row0 = false, col0 = false;
        for (int j = 0; j < cols; j++) if (m[0][j] == 0) row0 = true;
        for (int i = 0; i < rows; i++) if (m[i][0] == 0) col0 = true;
        for (int i = 1; i < rows; i++)                         // 1. mark
            for (int j = 1; j < cols; j++)
                if (m[i][j] == 0) { m[i][0] = 0; m[0][j] = 0; }
        for (int i = 1; i < rows; i++)                         // 2. clear the inside
            for (int j = 1; j < cols; j++)
                if (m[i][0] == 0 || m[0][j] == 0) m[i][j] = 0;
        if (row0) for (int j = 0; j < cols; j++) m[0][j] = 0;  // 3. marks last
        if (col0) for (int i = 0; i < rows; i++) m[i][0] = 0;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static final int[][] DIRS = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};   // right, down, left, up

    static int[][] bruteRotate(int[][] m) {
        int n = m.length;
        int[][] b = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) b[j][n - 1 - i] = m[i][j];
        return b;
    }

    // Walk with a direction array and turn right when blocked.
    static List<Integer> bruteSpiral(int[][] m) {
        int rows = m.length, cols = m[0].length;
        boolean[][] seen = new boolean[rows][cols];
        List<Integer> out = new ArrayList<>();
        int r = 0, c = 0, d = 0;
        for (int k = 0; k < rows * cols; k++) {
            out.add(m[r][c]);
            seen[r][c] = true;
            int nr = r + DIRS[d][0], nc = c + DIRS[d][1];
            if (nr < 0 || nr >= rows || nc < 0 || nc >= cols || seen[nr][nc]) {
                d = (d + 1) % 4;
                nr = r + DIRS[d][0];
                nc = c + DIRS[d][1];
            }
            r = nr;
            c = nc;
        }
        return out;
    }

    static int[][] bruteZeroes(int[][] m) {
        int rows = m.length, cols = m[0].length;
        boolean[] zr = new boolean[rows], zc = new boolean[cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                if (m[i][j] == 0) { zr[i] = true; zc[j] = true; }
        int[][] b = new int[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++) b[i][j] = zr[i] || zc[j] ? 0 : m[i][j];
        return b;
    }

    static int[][] randomMatrix(Random r, int rows, int cols, int lo, int hi) {
        int[][] m = new int[rows][];
        for (int i = 0; i < rows; i++) m[i] = Check.ints(r, cols, lo, hi);
        return m;
    }

    static int[][] copy(int[][] m) {
        int[][] b = new int[m.length][];
        for (int i = 0; i < m.length; i++) b[i] = m[i].clone();
        return b;
    }

    public static void main(String[] args) {
        Random r = new Random(1010);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(6);
            int[][] sq = randomMatrix(r, n, n, -9, 9);
            int[][] rot = copy(sq);
            rotate(rot);
            Check.eq(bruteRotate(sq), rot, "rotate " + Arrays.deepToString(sq));

            int rows = 1 + r.nextInt(6), cols = 1 + r.nextInt(6);
            int[][] m = randomMatrix(r, rows, cols, 0, 99);
            Check.eq(bruteSpiral(m), spiralOrder(m), "spiral " + Arrays.deepToString(m));

            int[][] z = randomMatrix(r, rows, cols, 0, 4 + r.nextInt(8));
            int[][] got = copy(z);
            setZeroes(got);
            Check.eq(bruteZeroes(z), got, "zeroes " + Arrays.deepToString(z));
        }
        int[][] a = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        rotate(a);
        Check.eq(new int[][] {{7, 4, 1}, {8, 5, 2}, {9, 6, 3}}, a, "LC 48 sample 1");
        int[][] b = {{5, 1, 9, 11}, {2, 4, 8, 10}, {13, 3, 6, 7}, {15, 14, 12, 16}};
        rotate(b);
        Check.eq(new int[][] {{15, 13, 2, 5}, {14, 3, 4, 1}, {12, 6, 8, 9}, {16, 7, 10, 11}}, b, "LC 48 sample 2");
        Check.eq(Arrays.asList(1, 2, 3, 6, 9, 8, 7, 4, 5),
                spiralOrder(new int[][] {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}}), "LC 54 sample 1");
        Check.eq(Arrays.asList(1, 2, 3, 4, 8, 12, 11, 10, 9, 5, 6, 7),
                spiralOrder(new int[][] {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}}), "LC 54 sample 2");
        int[][] z1 = {{1, 1, 1}, {1, 0, 1}, {1, 1, 1}};
        setZeroes(z1);
        Check.eq(new int[][] {{1, 0, 1}, {0, 0, 0}, {1, 0, 1}}, z1, "LC 73 sample 1");
        int[][] z2 = {{0, 1, 2, 0}, {3, 4, 5, 2}, {1, 3, 1, 5}};
        setZeroes(z2);
        Check.eq(new int[][] {{0, 0, 0, 0}, {0, 4, 5, 0}, {0, 3, 1, 0}}, z2, "LC 73 sample 2");
        Check.done("P01MatrixMoves");
    }
}
