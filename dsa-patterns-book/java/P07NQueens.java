import java.util.*;

public class P07NQueens {
    //@tpl nqueens
    static int n;
    static int[] queenAt;                   // queenAt[r] = column of row r's queen
    static boolean[] cols, diag, anti;      // diag: r - c + n - 1, anti: r + c
    static List<List<String>> boards;

    static List<List<String>> solveNQueens(int size) {
        n = size;                           // reset every field on each call
        queenAt = new int[n];
        cols = new boolean[n];
        diag = new boolean[2 * n - 1];
        anti = new boolean[2 * n - 1];
        boards = new ArrayList<>();
        place(0);
        return boards;
    }

    // One queen per row, so rows never clash; check the column and both diagonals.
    static void place(int r) {
        if (r == n) { boards.add(draw()); return; }
        for (int c = 0; c < n; c++) {
            if (cols[c] || diag[r - c + n - 1] || anti[r + c]) continue;
            queenAt[r] = c;
            cols[c] = diag[r - c + n - 1] = anti[r + c] = true;     // mark
            place(r + 1);
            cols[c] = diag[r - c + n - 1] = anti[r + c] = false;    // restore
        }
    }

    static List<String> draw() {
        List<String> rows = new ArrayList<>();
        for (int r = 0; r < n; r++) {
            char[] row = new char[n];
            Arrays.fill(row, '.');
            row[queenAt[r]] = 'Q';
            rows.add(new String(row));
        }
        return rows;
    }
    //@end

    //@tpl sudoku
    static boolean[][] rowHas, colHas, boxHas;     // [row, col or box][digit 1..9]

    static void solveSudoku(char[][] b) {
        rowHas = new boolean[9][10];
        colHas = new boolean[9][10];
        boxHas = new boolean[9][10];
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (b[r][c] != '.') mark(r, c, b[r][c] - '0', true);
        fill(b, 0);
    }

    // Visit cells in row-major order; true as soon as the board is full.
    static boolean fill(char[][] b, int cell) {
        if (cell == 81) return true;
        int r = cell / 9, c = cell % 9, box = r / 3 * 3 + c / 3;
        if (b[r][c] != '.') return fill(b, cell + 1);
        for (int d = 1; d <= 9; d++) {
            if (rowHas[r][d] || colHas[c][d] || boxHas[box][d]) continue;
            b[r][c] = (char) ('0' + d);
            mark(r, c, d, true);
            if (fill(b, cell + 1)) return true;    // keep it: stop searching
            mark(r, c, d, false);
            b[r][c] = '.';
        }
        return false;                              // no digit fits: back up
    }

    static void mark(int r, int c, int d, boolean on) {
        rowHas[r][d] = colHas[c][d] = boxHas[r / 3 * 3 + c / 3][d] = on;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Every permutation of columns (one queen per row and column), keep the safe ones.
    static void brutePerm(int[] p, int k, List<List<String>> out) {
        int m = p.length;
        if (k == m) {
            for (int i = 0; i < m; i++)
                for (int j = i + 1; j < m; j++)
                    if (Math.abs(p[i] - p[j]) == j - i) return;
            List<String> rows = new ArrayList<>();
            for (int r = 0; r < m; r++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < m; c++) sb.append(c == p[r] ? 'Q' : '.');
                rows.add(sb.toString());
            }
            out.add(rows);
            return;
        }
        for (int i = k; i < m; i++) {
            int t = p[k]; p[k] = p[i]; p[i] = t;
            brutePerm(p, k + 1, out);
            t = p[k]; p[k] = p[i]; p[i] = t;
        }
    }

    static String canonBoards(List<List<String>> bs) {
        List<String> s = new ArrayList<>();
        for (List<String> b : bs) s.add(b.toString());
        Collections.sort(s);
        return s.toString();
    }

    static boolean validFull(char[][] b) {
        for (int i = 0; i < 9; i++) {
            boolean[] row = new boolean[10], col = new boolean[10], box = new boolean[10];
            for (int j = 0; j < 9; j++) {
                int x = b[i][j] - '0', y = b[j][i] - '0';
                int z = b[i / 3 * 3 + j / 3][i % 3 * 3 + j % 3] - '0';
                if (x < 1 || x > 9 || y < 1 || y > 9 || z < 1 || z > 9) return false;
                if (row[x] || col[y] || box[z]) return false;
                row[x] = col[y] = box[z] = true;
            }
        }
        return true;
    }

    // A random full grid: a fixed valid pattern with digits, rows and columns shuffled
    // inside their bands and stacks, and bands and stacks shuffled.
    static int[][] randomGrid(Random r) {
        int[] digit = shuffled(r, 9);
        int[] rows = bandOrder(r), cols = bandOrder(r);
        int[][] g = new int[9][9];
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++) {
                int ri = rows[i], cj = cols[j];
                g[i][j] = digit[(ri % 3 * 3 + ri / 3 + cj) % 9] + 1;
            }
        return g;
    }

    static int[] shuffled(Random r, int n) {
        List<Integer> l = new ArrayList<>();
        for (int i = 0; i < n; i++) l.add(i);
        Collections.shuffle(l, r);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = l.get(i);
        return a;
    }

    static int[] bandOrder(Random r) {
        int[] band = shuffled(r, 3), out = new int[9];
        for (int b = 0; b < 3; b++) {
            int[] inner = shuffled(r, 3);
            for (int k = 0; k < 3; k++) out[b * 3 + k] = band[b] * 3 + inner[k];
        }
        return out;
    }

    public static void main(String[] args) {
        int[] known = {1, 0, 0, 2, 10, 4, 40, 92, 352};
        for (int size = 1; size <= 9; size++) {
            List<List<String>> got = solveNQueens(size);
            Check.eq(known[size - 1], got.size(), "count n=" + size);
            if (size <= 8) {
                int[] p = new int[size];
                for (int i = 0; i < size; i++) p[i] = i;
                List<List<String>> want = new ArrayList<>();
                brutePerm(p, 0, want);
                Check.eq(canonBoards(want), canonBoards(got), "boards n=" + size);
            }
        }
        Check.eq("[[..Q., Q..., ...Q, .Q..], [.Q.., ...Q, Q..., ..Q.]]", canonBoards(solveNQueens(4)),
                 "LC 51 sample 1");
        Check.eq("[[Q]]", canonBoards(solveNQueens(1)), "LC 51 sample 2");

        Random r = new Random(75);
        for (int t = 0; t < 300; t++) {
            int[][] g = randomGrid(r);
            char[][] b = new char[9][9];
            for (int i = 0; i < 9; i++) for (int j = 0; j < 9; j++) b[i][j] = (char) ('0' + g[i][j]);
            Check.isTrue(validFull(b), "generator gives a valid grid");
            int holes = 30 + r.nextInt(26);
            char[][] puzzle = new char[9][];
            for (int i = 0; i < 9; i++) puzzle[i] = b[i].clone();
            for (int h = 0; h < holes; h++) puzzle[r.nextInt(9)][r.nextInt(9)] = '.';
            char[][] work = new char[9][];
            for (int i = 0; i < 9; i++) work[i] = puzzle[i].clone();
            solveSudoku(work);
            Check.isTrue(validFull(work), "solved grid is valid, t=" + t);
            for (int i = 0; i < 9; i++)
                for (int j = 0; j < 9; j++)
                    if (puzzle[i][j] != '.') Check.isTrue(work[i][j] == puzzle[i][j], "clue kept");
        }
        String[] lc = {"53..7....", "6..195...", ".98....6.", "8...6...3", "4..8.3..1",
                       "7...2...6", ".6....28.", "...419..5", "....8..79"};
        String[] lcAns = {"534678912", "672195348", "198342567", "859761423", "426853791",
                          "713924856", "961537284", "287419635", "345286179"};
        char[][] b = new char[9][];
        for (int i = 0; i < 9; i++) b[i] = lc[i].toCharArray();
        solveSudoku(b);
        for (int i = 0; i < 9; i++) Check.eq(lcAns[i], new String(b[i]), "LC 37 sample row " + i);
        Check.done("P07NQueens");
    }
}
