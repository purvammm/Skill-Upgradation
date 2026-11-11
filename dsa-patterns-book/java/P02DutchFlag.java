import java.util.*;

public class P02DutchFlag {
    //@tpl colors
    // Sort 0s, 1s and 2s in one pass (LC 75).
    // Invariant: a[0..low) = 0, a[low..mid) = 1, a[mid..high] unseen, a(high..] = 2.
    static void sortColors(int[] a) {
        int low = 0, mid = 0, high = a.length - 1;
        while (mid <= high) {
            if (a[mid] == 0) swap(a, low++, mid++);      // a[low] was a 1: both move
            else if (a[mid] == 2) swap(a, mid, high--);  // new a[mid] unseen: stay
            else mid++;
        }
    }

    static void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }
    //@end

    //@tpl pivot
    // Three-way partition around p. Returns {lt, gt} with
    // a[0..lt) < p, a[lt..gt] == p, a(gt..] > p. Same loop, p plays the role of 1.
    static int[] partition3(int[] a, int p) {
        int lt = 0, i = 0, gt = a.length - 1;
        while (i <= gt) {
            if (a[i] < p) swap(a, lt++, i++);
            else if (a[i] > p) swap(a, i, gt--);
            else i++;
        }
        return new int[] {lt, gt};
    }
    //@end

    // ---------------- tests (not printed) ----------------
    public static void main(String[] args) {
        Random r = new Random(25);
        for (int t = 0; t < 5000; t++) {
            int n = r.nextInt(15);
            int[] a = Check.ints(r, n, 0, 2);
            int[] want = a.clone();
            Arrays.sort(want);
            int[] got = a.clone();
            sortColors(got);
            Check.eq(want, got, "colors " + Arrays.toString(a));

            int[] b = Check.ints(r, n, -4, 4);
            int p = r.nextInt(11) - 5;
            int[] c = b.clone();
            int[] lg = partition3(c, p);
            int less = 0, more = 0;
            for (int x : b) { if (x < p) less++; if (x > p) more++; }
            Check.eq(less, lg[0], "lt " + Arrays.toString(b) + " p=" + p);
            Check.eq(n - more - 1, lg[1], "gt " + Arrays.toString(b) + " p=" + p);
            for (int i = 0; i < n; i++) {
                int want3 = i < lg[0] ? -1 : (i <= lg[1] ? 0 : 1);
                Check.eq(want3, Integer.signum(Integer.compare(c[i], p)), "block " + Arrays.toString(c));
            }
            int[] s1 = b.clone(), s2 = c.clone();
            Arrays.sort(s1);
            Arrays.sort(s2);
            Check.eq(s1, s2, "permutation " + Arrays.toString(b));
        }
        int[] e1 = {2, 0, 2, 1, 1, 0};
        sortColors(e1);
        Check.eq(new int[] {0, 0, 1, 1, 2, 2}, e1, "LC 75 sample 1");
        int[] e2 = {2, 0, 1};
        sortColors(e2);
        Check.eq(new int[] {0, 1, 2}, e2, "LC 75 sample 2");
        Check.done("P02DutchFlag");
    }
}
