import java.util.*;

public class P01DiffArray {
    //@tpl adds
    // Many range adds, then read every value once.
    // diff[l] += v starts the add at l; diff[r + 1] -= v stops it after r.
    static int[] rangeAdds(int n, int[][] updates) {  // each {l, r, v}
        int[] diff = new int[n + 1];                  // +1 so r + 1 fits
        for (int[] u : updates) {
            diff[u[0]] += u[2];
            diff[u[1] + 1] -= u[2];
        }
        int[] a = new int[n];
        int run = 0;
        for (int i = 0; i < n; i++) {
            run += diff[i];                           // prefix sum = a[i]
            a[i] = run;
        }
        return a;
    }
    //@end

    //@tpl carpool
    // Car pooling: t = {people, from, to}. They ride stops from..to-1,
    // so add at from and remove at to (they are out before others get in).
    static boolean carPooling(int[][] trips, int capacity) {
        int[] diff = new int[1001];                      // stops 0..1000
        for (int[] t : trips) {
            diff[t[1]] += t[0];
            diff[t[2]] -= t[0];
        }
        int load = 0;
        for (int d : diff) {
            load += d;
            if (load > capacity) return false;
        }
        return true;
    }
    //@end

    //@tpl rect
    // 2D difference: add v to rectangle (r1, c1)..(r2, c2) with four marks,
    // then a 2D prefix sum turns the marks back into values.
    // Each update is {r1, c1, r2, c2, v}, corners inclusive.
    static int[][] rectAdds(int rows, int cols, int[][] ups) {
        int[][] d = new int[rows + 1][cols + 1];
        for (int[] u : ups) {
            d[u[0]][u[1]] += u[4];
            d[u[0]][u[3] + 1] -= u[4];
            d[u[2] + 1][u[1]] -= u[4];
            d[u[2] + 1][u[3] + 1] += u[4];
        }
        int[][] a = new int[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++) {
                a[i][j] = d[i][j];
                if (i > 0) a[i][j] += a[i - 1][j];
                if (j > 0) a[i][j] += a[i][j - 1];
                if (i > 0 && j > 0) a[i][j] -= a[i - 1][j - 1];
            }
        return a;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int[] bruteAdds(int n, int[][] ups) {
        int[] a = new int[n];
        for (int[] u : ups)
            for (int i = u[0]; i <= u[1]; i++) a[i] += u[2];
        return a;
    }

    static boolean brutePool(int[][] trips, int cap) {
        for (int stop = 0; stop <= 1000; stop++) {
            int load = 0;
            for (int[] t : trips) if (t[1] <= stop && stop < t[2]) load += t[0];
            if (load > cap) return false;
        }
        return true;
    }

    static int[][] bruteRect(int rows, int cols, int[][] ups) {
        int[][] a = new int[rows][cols];
        for (int[] u : ups)
            for (int i = u[0]; i <= u[2]; i++)
                for (int j = u[1]; j <= u[3]; j++) a[i][j] += u[4];
        return a;
    }

    // LeetCode 1109 is 1-indexed: bookings {first, last, seats}.
    static int[] corpFlightBookings(int[][] bookings, int n) {
        int[][] ups = new int[bookings.length][];
        for (int i = 0; i < bookings.length; i++)
            ups[i] = new int[] {bookings[i][0] - 1, bookings[i][1] - 1, bookings[i][2]};
        return rangeAdds(n, ups);
    }

    public static void main(String[] args) {
        Random r = new Random(606);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(10);
            int[][] ups = new int[r.nextInt(6)][];
            for (int i = 0; i < ups.length; i++) {
                int l = r.nextInt(n), rr = l + r.nextInt(n - l);
                ups[i] = new int[] {l, rr, r.nextInt(21) - 10};
            }
            Check.eq(bruteAdds(n, ups), rangeAdds(n, ups), "adds " + Arrays.deepToString(ups));

            int[][] trips = new int[1 + r.nextInt(6)][];
            for (int i = 0; i < trips.length; i++) {
                int from = r.nextInt(10);
                trips[i] = new int[] {1 + r.nextInt(5), from, from + 1 + r.nextInt(10 - from)};
            }
            if (t % 7 == 0) trips[0] = new int[] {1 + r.nextInt(5), 990 + r.nextInt(5), 1000};
            int cap = 1 + r.nextInt(12);
            Check.eq(brutePool(trips, cap), carPooling(trips, cap), "pool " + Arrays.deepToString(trips));

            int rows = 1 + r.nextInt(5), cols = 1 + r.nextInt(5);
            int[][] rect = new int[r.nextInt(5)][];
            for (int i = 0; i < rect.length; i++) {
                int r1 = r.nextInt(rows), r2 = r1 + r.nextInt(rows - r1);
                int c1 = r.nextInt(cols), c2 = c1 + r.nextInt(cols - c1);
                rect[i] = new int[] {r1, c1, r2, c2, r.nextInt(11) - 5};
            }
            Check.eq(bruteRect(rows, cols, rect), rectAdds(rows, cols, rect), "rect " + Arrays.deepToString(rect));
        }
        Check.eq(new int[] {10, 55, 45, 25, 25},
                corpFlightBookings(new int[][] {{1, 2, 10}, {2, 3, 20}, {2, 5, 25}}, 5), "LC 1109 sample 1");
        Check.eq(new int[] {10, 25}, corpFlightBookings(new int[][] {{1, 2, 10}, {2, 2, 15}}, 2), "LC 1109 sample 2");
        Check.eq(false, carPooling(new int[][] {{2, 1, 5}, {3, 3, 7}}, 4), "LC 1094 sample 1");
        Check.eq(true, carPooling(new int[][] {{2, 1, 5}, {3, 3, 7}}, 5), "LC 1094 sample 2");
        // LC 2536 (increment submatrices by one): n = 3, queries {r1, c1, r2, c2}
        Check.eq(new int[][] {{1, 1, 0}, {1, 2, 1}, {0, 1, 1}},
                rectAdds(3, 3, new int[][] {{1, 1, 2, 2, 1}, {0, 0, 1, 1, 1}}), "LC 2536 sample 1");
        Check.done("P01DiffArray");
    }
}
