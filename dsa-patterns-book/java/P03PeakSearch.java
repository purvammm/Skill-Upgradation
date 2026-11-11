import java.util.*;

public class P03PeakSearch {
    //@tpl peak
    // LC 162: index of any peak (bigger than both neighbours; a[-1] = a[n] = -inf,
    // neighbours never equal). LC 852 (peak of a mountain) is the same code.
    static int findPeak(int[] a) {
        int lo = 0, hi = a.length - 1;            // a peak exists in [lo, hi]
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;         // mid < hi, so a[mid + 1] exists
            if (a[mid] < a[mid + 1]) lo = mid + 1;    // uphill: a peak to the right
            else hi = mid;                            // downhill: mid or to the left
        }
        return lo;
    }
    //@end

    //@tpl mountain
    interface MountainArray { int get(int index); int length(); }   // LeetCode's

    // LC 1095: smallest index holding target, or -1, in about 4 log2(n) gets.
    static int findInMountainArray(int target, MountainArray m) {
        int n = m.length(), lo = 0, hi = n - 1;
        while (lo < hi) {                         // 1. the peak, as in findPeak
            int mid = lo + (hi - lo) / 2;
            if (m.get(mid) < m.get(mid + 1)) lo = mid + 1;
            else hi = mid;
        }
        int peak = lo;
        int i = firstEqual(m, target, 0, peak + 1, true);   // 2. rising side first
        if (i != -1) return i;
        return firstEqual(m, target, peak + 1, n, false);   // 3. then falling side
    }

    // Lower bound of x on [from, to), a run sorted up (asc) or down; -1 if absent.
    static int firstEqual(MountainArray m, int x, int from, int to, boolean asc) {
        int lo = from, hi = to;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            int v = m.get(mid);
            if (asc ? v < x : v > x) lo = mid + 1;    // x is further on
            else hi = mid;
        }
        return lo < to && m.get(lo) == x ? lo : -1;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean isPeak(int[] a, int i) {
        boolean left = i == 0 || a[i - 1] < a[i];
        boolean right = i == a.length - 1 || a[i + 1] < a[i];
        return left && right;
    }

    static class Counted implements MountainArray {
        final int[] a;
        int gets = 0;
        Counted(int[] a) { this.a = a; }
        public int get(int i) { gets++; return a[i]; }
        public int length() { return a.length; }
    }

    // Strictly rising to a peak at index p, then strictly falling.
    static int[] mountain(Random r, int n, int p, int step) {
        int[] a = new int[n];
        a[p] = n * step + r.nextInt(10);
        for (int i = p - 1; i >= 0; i--) a[i] = a[i + 1] - 1 - r.nextInt(step);
        for (int i = p + 1; i < n; i++) a[i] = a[i - 1] - 1 - r.nextInt(step);
        return a;
    }

    public static void main(String[] args) {
        Random r = new Random(39);
        for (int t = 0; t < 5000; t++) {
            int n = 1 + r.nextInt(12);
            int[] a = new int[n];
            a[0] = r.nextInt(10);
            for (int i = 1; i < n; i++) a[i] = a[i - 1] + (r.nextBoolean() ? 1 : -1) * (1 + r.nextInt(3));
            Check.isTrue(isPeak(a, findPeak(a)), "peak " + Arrays.toString(a));
            if (n >= 3) {
                int p = 1 + r.nextInt(n - 2);
                int[] m = mountain(r, n, p, 3);
                Check.eq(p, findPeak(m), "mountain peak " + Arrays.toString(m));
                for (int x = m[p] - 3 * n - 2; x <= m[p] + 1; x++) {
                    int want = -1;
                    for (int i = 0; i < n; i++) if (m[i] == x) { want = i; break; }
                    Check.eq(want, findInMountainArray(x, new Counted(m)), "find " + Arrays.toString(m) + " x=" + x);
                }
            }
        }
        // the 100-call limit at the largest size
        for (int t = 0; t < 200; t++) {
            int n = 10000, p = 1 + r.nextInt(n - 2);
            int[] m = mountain(r, n, p, 2);
            int x = r.nextBoolean() ? m[r.nextInt(n)] : m[p] - r.nextInt(3 * n);
            Counted c = new Counted(m);
            int got = findInMountainArray(x, c);
            int want = -1;
            for (int i = 0; i < n; i++) if (m[i] == x) { want = i; break; }
            Check.eq(want, got, "big find");
            Check.isTrue(c.gets <= 100, "gets " + c.gets);
        }
        // LeetCode samples
        Check.eq(2, findPeak(new int[] {1, 2, 3, 1}), "LC 162 sample 1");
        int p2 = findPeak(new int[] {1, 2, 1, 3, 5, 6, 4});
        Check.isTrue(p2 == 1 || p2 == 5, "LC 162 sample 2");
        Check.eq(1, findPeak(new int[] {0, 1, 0}), "LC 852 sample 1");
        Check.eq(1, findPeak(new int[] {0, 2, 1, 0}), "LC 852 sample 2");
        Check.eq(1, findPeak(new int[] {0, 10, 5, 2}), "LC 852 sample 3");
        Check.eq(2, findInMountainArray(3, new Counted(new int[] {1, 2, 3, 4, 5, 3, 1})), "LC 1095 sample 1");
        Check.eq(-1, findInMountainArray(3, new Counted(new int[] {0, 1, 2, 4, 2, 1})), "LC 1095 sample 2");
        // the trap: equal neighbours. On [1, 3, 3, 3, 4, 1] the loop returns 1, not a peak.
        int[] flat = {1, 3, 3, 3, 4, 1};
        Check.eq(1, findPeak(flat), "plateau answer");
        Check.isTrue(!isPeak(flat, findPeak(flat)), "plateau breaks findPeak");
        Check.done("P03PeakSearch");
    }
}
