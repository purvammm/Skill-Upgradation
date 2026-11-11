import java.util.*;

public class P04Histogram {
    //@tpl largest
    // Largest rectangle in a histogram. The stack holds indices with rising
    // heights. When h[i] is not higher than the top bar, that bar cannot reach
    // past i: pop it and measure the widest rectangle of its height.
    static int largestRectangle(int[] h) {
        int n = h.length, best = 0;
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i <= n; i++) {
            int cur = i == n ? 0 : h[i];             // sentinel 0 empties the stack
            while (!st.isEmpty() && h[st.peek()] >= cur) {
                int height = h[st.pop()];
                int left = st.isEmpty() ? -1 : st.peek();     // previous smaller
                best = Math.max(best, height * (i - left - 1)); // i: next smaller
            }
            st.push(i);
        }
        return best;
    }
    //@end

    //@tpl twopass
    // The same answer in two clear passes: the nearest lower bar on each side.
    static int largestRectangleTwoPass(int[] h) {
        int n = h.length, best = 0;
        int[] left = new int[n], right = new int[n];
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {                // previous smaller
            while (!st.isEmpty() && h[st.peek()] >= h[i]) st.pop();
            left[i] = st.isEmpty() ? -1 : st.peek();
            st.push(i);
        }
        st.clear();
        for (int i = n - 1; i >= 0; i--) {           // next smaller
            while (!st.isEmpty() && h[st.peek()] >= h[i]) st.pop();
            right[i] = st.isEmpty() ? n : st.peek();
            st.push(i);
        }
        for (int i = 0; i < n; i++)                  // bar i spans (left, right)
            best = Math.max(best, h[i] * (right[i] - left[i] - 1));
        return best;
    }
    //@end

    //@tpl maximal
    // Maximal rectangle of '1's: each row is the floor of a histogram.
    static int maximalRectangle(char[][] m) {
        if (m.length == 0) return 0;
        int[] h = new int[m[0].length];
        int best = 0;
        for (char[] row : m) {
            for (int j = 0; j < row.length; j++)
                h[j] = row[j] == '1' ? h[j] + 1 : 0;     // ones straight above
            best = Math.max(best, largestRectangle(h));
        }
        return best;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int bruteHist(int[] h) {
        int best = 0;
        for (int i = 0; i < h.length; i++) {
            int min = Integer.MAX_VALUE;
            for (int j = i; j < h.length; j++) {
                min = Math.min(min, h[j]);
                best = Math.max(best, min * (j - i + 1));
            }
        }
        return best;
    }

    static int bruteMatrix(char[][] m) {
        int R = m.length, C = R == 0 ? 0 : m[0].length, best = 0;
        for (int r1 = 0; r1 < R; r1++)
            for (int c1 = 0; c1 < C; c1++)
                for (int r2 = r1; r2 < R; r2++)
                    for (int c2 = c1; c2 < C; c2++) {
                        boolean ok = true;
                        for (int i = r1; i <= r2 && ok; i++)
                            for (int j = c1; j <= c2 && ok; j++)
                                if (m[i][j] != '1') ok = false;
                        if (ok) best = Math.max(best, (r2 - r1 + 1) * (c2 - c1 + 1));
                    }
        return best;
    }

    static char[][] grid(String... rows) {
        char[][] g = new char[rows.length][];
        for (int i = 0; i < rows.length; i++) g[i] = rows[i].toCharArray();
        return g;
    }

    public static void main(String[] args) {
        Random r = new Random(44);
        for (int t = 0; t < 4000; t++) {
            int n = r.nextInt(13);
            int[] h = Check.ints(r, n, 0, r.nextBoolean() ? 3 : 9);
            int want = bruteHist(h);
            Check.eq(want, largestRectangle(h), "hist " + Arrays.toString(h));
            Check.eq(want, largestRectangleTwoPass(h), "two-pass " + Arrays.toString(h));
        }
        for (int t = 0; t < 1500; t++) {
            int R = 1 + r.nextInt(5), C = 1 + r.nextInt(5);
            int ones = 1 + r.nextInt(9);
            char[][] m = new char[R][C];
            for (int i = 0; i < R; i++) for (int j = 0; j < C; j++) m[i][j] = r.nextInt(10) < ones ? '1' : '0';
            Check.eq(bruteMatrix(m), maximalRectangle(m), "matrix " + Arrays.deepToString(m));
        }
        int[] big = new int[100000];
        Arrays.fill(big, 10000);
        Check.eq(1000000000, largestRectangle(big), "limits: 1e4 x 1e5 fits in int");
        Check.eq(10, largestRectangle(new int[] {2, 1, 5, 6, 2, 3}), "LC 84 sample 1");
        Check.eq(4, largestRectangle(new int[] {2, 4}), "LC 84 sample 2");
        Check.eq(10, largestRectangleTwoPass(new int[] {2, 1, 5, 6, 2, 3}), "LC 84 sample 1 two-pass");
        Check.eq(6, maximalRectangle(grid("10100", "10111", "11111", "10010")), "LC 85 sample 1");
        Check.eq(0, maximalRectangle(grid("0")), "LC 85 sample 2");
        Check.eq(1, maximalRectangle(grid("1")), "LC 85 sample 3");
        Check.done("P04Histogram");
    }
}
