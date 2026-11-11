import java.util.*;

public class P02ReadWrite {
    //@tpl dedup
    // Sorted array: keep each value at most k times, in place; return the new length.
    // k = 1 is LC 26 (remove duplicates), k = 2 is LC 80.
    static int keepAtMostK(int[] a, int k) {
        int w = 0;                          // a[0..w) is the answer for a[0..r)
        for (int r = 0; r < a.length; r++) {
            if (w < k || a[w - k] != a[r])  // fewer than k copies of a[r] kept so far
                a[w++] = a[r];
        }
        return w;
    }
    //@end

    //@tpl filter
    // Remove every val (LC 27); the kept values stay in order.
    static int removeElement(int[] a, int val) {
        int w = 0;
        for (int r = 0; r < a.length; r++)
            if (a[r] != val) a[w++] = a[r];
        return w;
    }

    // Move zeroes to the end, keep the order of the rest (LC 283).
    // Invariant: a[0..w) = non-zeros in order, a[w..r) = zeros.
    static void moveZeroes(int[] a) {
        int w = 0;
        for (int r = 0; r < a.length; r++) {
            if (a[r] != 0) {
                int t = a[w]; a[w] = a[r]; a[r] = t;
                w++;
            }
        }
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int[] bruteKeep(int[] a, int k) {
        List<Integer> out = new ArrayList<>();
        Map<Integer, Integer> cnt = new HashMap<>();
        for (int x : a) if (cnt.merge(x, 1, Integer::sum) <= k) out.add(x);
        int[] res = new int[out.size()];
        for (int i = 0; i < res.length; i++) res[i] = out.get(i);
        return res;
    }

    static int[] bruteRemove(int[] a, int val) {
        List<Integer> out = new ArrayList<>();
        for (int x : a) if (x != val) out.add(x);
        int[] res = new int[out.size()];
        for (int i = 0; i < res.length; i++) res[i] = out.get(i);
        return res;
    }

    static int[] bruteZeroes(int[] a) {
        int[] nz = bruteRemove(a, 0);
        return Arrays.copyOf(nz, a.length);
    }

    public static void main(String[] args) {
        Random r = new Random(23);
        for (int t = 0; t < 5000; t++) {
            int n = r.nextInt(14);
            int[] a = Check.sortedInts(r, n, -3, 3);
            int k = 1 + r.nextInt(3);
            int[] b = a.clone();
            int w = keepAtMostK(b, k);
            Check.eq(bruteKeep(a, k), Arrays.copyOf(b, w), "keep " + Arrays.toString(a) + " k=" + k);
            int[] c = Check.ints(r, n, 0, 3);
            int val = r.nextInt(4);
            int[] d = c.clone();
            int w2 = removeElement(d, val);
            Check.eq(bruteRemove(c, val), Arrays.copyOf(d, w2), "remove " + Arrays.toString(c));
            int[] e = c.clone();
            moveZeroes(e);
            Check.eq(bruteZeroes(c), e, "zeroes " + Arrays.toString(c));
        }
        int[] s1 = {1, 1, 2};
        Check.eq(2, keepAtMostK(s1, 1), "LC 26 sample 1");
        int[] s2 = {0, 0, 1, 1, 1, 2, 2, 3, 3, 4};
        Check.eq(new int[] {0, 1, 2, 3, 4}, Arrays.copyOf(s2, keepAtMostK(s2, 1)), "LC 26 sample 2");
        int[] s3 = {1, 1, 1, 2, 2, 3};
        Check.eq(new int[] {1, 1, 2, 2, 3}, Arrays.copyOf(s3, keepAtMostK(s3, 2)), "LC 80 sample 1");
        int[] s4 = {0, 0, 1, 1, 1, 1, 2, 3, 3};
        Check.eq(new int[] {0, 0, 1, 1, 2, 3, 3}, Arrays.copyOf(s4, keepAtMostK(s4, 2)), "LC 80 sample 2");
        int[] s5 = {3, 2, 2, 3};
        Check.eq(new int[] {2, 2}, Arrays.copyOf(s5, removeElement(s5, 3)), "LC 27 sample 1");
        int[] s6 = {0, 1, 2, 2, 3, 0, 4, 2};
        Check.eq(5, removeElement(s6, 2), "LC 27 sample 2");
        int[] s7 = {0, 1, 0, 3, 12};
        moveZeroes(s7);
        Check.eq(new int[] {1, 3, 12, 0, 0}, s7, "LC 283 sample 1");
        int[] s8 = {0};
        moveZeroes(s8);
        Check.eq(new int[] {0}, s8, "LC 283 sample 2");
        Check.done("P02ReadWrite");
    }
}
