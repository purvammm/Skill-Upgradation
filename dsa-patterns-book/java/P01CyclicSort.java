import java.util.*;

public class P01CyclicSort {
    //@tpl missing
    // Cyclic sort: put each value v in 1..n at index v - 1 by swapping.
    // Values outside 1..n and extra copies stay wherever they land.
    static void cyclicSort(int[] a) {
        for (int i = 0; i < a.length; i++)
            while (a[i] >= 1 && a[i] <= a.length && a[a[i] - 1] != a[i]) {
                int j = a[i] - 1;                     // home of a[i]
                int t = a[i]; a[i] = a[j]; a[j] = t;  // one more value home
            }
    }

    // First missing positive in O(n) time, O(1) extra space.
    static int firstMissingPositive(int[] a) {
        cyclicSort(a);
        for (int i = 0; i < a.length; i++)
            if (a[i] != i + 1) return i + 1;          // first empty home
        return a.length + 1;                          // 1..n all present
    }
    //@end

    //@tpl mismatch
    // Set mismatch: 1..n with one value doubled and one missing.
    // After cyclic sort the only wrong cell is the missing value's home,
    // and it holds the extra copy.
    static int[] findErrorNums(int[] a) {
        cyclicSort(a);
        for (int i = 0; i < a.length; i++)
            if (a[i] != i + 1) return new int[] {a[i], i + 1};  // {dup, missing}
        return new int[] {-1, -1};
    }
    //@end

    //@tpl dups
    // Find all duplicates (each value 1..n appears once or twice).
    // Mark "v seen" by making a[v - 1] negative; the sign is the flag.
    static List<Integer> findDuplicates(int[] a) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < a.length; i++) {
            int v = Math.abs(a[i]);        // a[i] may be marked
            if (a[v - 1] < 0) out.add(v);  // second time we see v
            else a[v - 1] = -a[v - 1];
        }
        return out;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int bruteMissing(int[] a) {
        Set<Integer> s = new HashSet<>();
        for (int x : a) s.add(x);
        int k = 1;
        while (s.contains(k)) k++;
        return k;
    }

    static void shuffle(Random r, int[] a) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = r.nextInt(i + 1), t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
    }

    public static void main(String[] args) {
        Random r = new Random(909);
        for (int t = 0; t < 5000; t++) {
            int n = r.nextInt(12);
            int[] a = Check.ints(r, n, -3, n + 2);
            Check.eq(bruteMissing(a), firstMissingPositive(a.clone()), "missing " + Arrays.toString(a));
            int[] sorted = a.clone();
            cyclicSort(sorted);
            for (int i = 0; i < n; i++) {
                boolean present = false;
                for (int x : a) if (x == i + 1) present = true;
                if (present) Check.eq(i + 1, sorted[i], "home " + Arrays.toString(a));
            }

            // 1..n with d values doubled and d others removed
            if (n == 0) continue;
            int[] perm = new int[n];
            for (int i = 0; i < n; i++) perm[i] = i + 1;
            shuffle(r, perm);
            int d = r.nextInt(n / 2 + 1);
            int[] b = new int[n];
            int idx = 0;
            for (int i = 0; i < d; i++) { b[idx++] = perm[i]; b[idx++] = perm[i]; }
            for (int i = 2 * d; i < n; i++) b[idx++] = perm[i];
            shuffle(r, b);
            List<Integer> want = new ArrayList<>();
            for (int i = 0; i < d; i++) want.add(perm[i]);
            Collections.sort(want);
            List<Integer> got = findDuplicates(b.clone());
            Collections.sort(got);
            Check.eq(want, got, "dups " + Arrays.toString(b));

            if (n < 2) continue;
            int[] c = new int[n];
            for (int i = 0; i < n; i++) c[i] = perm[i];
            int dup = perm[0], miss = perm[1];
            for (int i = 0; i < n; i++) if (c[i] == miss) c[i] = dup;
            shuffle(r, c);
            Check.eq(new int[] {dup, miss}, findErrorNums(c.clone()), "mismatch " + Arrays.toString(c));
        }
        Check.eq(3, firstMissingPositive(new int[] {1, 2, 0}), "LC 41 sample 1");
        Check.eq(2, firstMissingPositive(new int[] {3, 4, -1, 1}), "LC 41 sample 2");
        Check.eq(1, firstMissingPositive(new int[] {7, 8, 9, 11, 12}), "LC 41 sample 3");
        List<Integer> d1 = findDuplicates(new int[] {4, 3, 2, 7, 8, 2, 3, 1});
        Collections.sort(d1);
        Check.eq(Arrays.asList(2, 3), d1, "LC 442 sample 1");
        Check.eq(Arrays.asList(1), findDuplicates(new int[] {1, 1, 2}), "LC 442 sample 2");
        Check.eq(new ArrayList<Integer>(), findDuplicates(new int[] {1}), "LC 442 sample 3");
        Check.eq(new int[] {2, 3}, findErrorNums(new int[] {1, 2, 2, 4}), "LC 645 sample 1");
        Check.eq(new int[] {1, 2}, findErrorNums(new int[] {1, 1}), "LC 645 sample 2");
        Check.done("P01CyclicSort");
    }
}
