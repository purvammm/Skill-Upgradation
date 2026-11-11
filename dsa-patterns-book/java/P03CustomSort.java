import java.util.*;

public class P03CustomSort {
    //@tpl largest
    // LC 179: arrange the numbers to form the largest number.
    // Exchange argument: a goes before b exactly when a+b > b+a as strings.
    static String largestNumber(int[] nums) {
        String[] s = new String[nums.length];
        for (int i = 0; i < nums.length; i++) s[i] = String.valueOf(nums[i]);
        Arrays.sort(s, (a, b) -> (b + a).compareTo(a + b));  // bigger a+b first
        if (s[0].equals("0")) return "0";                     // all were 0
        StringBuilder sb = new StringBuilder();
        for (String x : s) sb.append(x);
        return sb.toString();
    }
    //@end

    //@tpl keys
    // LC 1636: sort by frequency (rare first), ties by value (big first).
    static int[] frequencySort(int[] nums) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int x : nums) freq.merge(x, 1, Integer::sum);
        Integer[] b = new Integer[nums.length];       // comparators need objects
        for (int i = 0; i < nums.length; i++) b[i] = nums[i];
        Arrays.sort(b, (x, y) -> {
            int fx = freq.get(x), fy = freq.get(y);
            if (fx != fy) return Integer.compare(fx, fy);   // key 1: frequency up
            return Integer.compare(y, x);                   // key 2: value down
        });
        for (int i = 0; i < nums.length; i++) nums[i] = b[i];
        return nums;
    }
    //@end

    //@tpl rows
    // int[][] by column 0 up, ties by column 1 down. Compare; never subtract.
    static void sortRows(int[][] rows) {
        Arrays.sort(rows, (x, y) -> x[0] != y[0] ? Integer.compare(x[0], y[0])
                                                 : Integer.compare(y[1], x[1]));
    }

    // Comparator helpers: the same row order, and strings by length, then A-Z.
    static final Comparator<int[]> BY_COLUMNS =
            Comparator.<int[]>comparingInt(r -> r[0])
                      .thenComparing((x, y) -> Integer.compare(y[1], x[1]));
    static final Comparator<String> BY_LENGTH = Comparator
            .comparingInt(String::length).thenComparing(Comparator.naturalOrder());

    // Indices of a in sorted order, so the original positions survive.
    static Integer[] sortedIndices(int[] a) {
        Integer[] idx = new Integer[a.length];
        for (int k = 0; k < a.length; k++) idx[k] = k;
        Arrays.sort(idx, (i, j) -> Integer.compare(a[i], a[j]));  // ties: i < j
        return idx;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static String best;

    static void permute(String[] s, int k) {
        if (k == s.length) {
            StringBuilder sb = new StringBuilder();
            for (String x : s) sb.append(x);
            String t = sb.toString().replaceFirst("^0+(?=.)", "");
            if (best == null || t.length() > best.length()
                    || (t.length() == best.length() && t.compareTo(best) > 0)) best = t;
            return;
        }
        for (int i = k; i < s.length; i++) {
            String t = s[k]; s[k] = s[i]; s[i] = t;
            permute(s, k + 1);
            t = s[k]; s[k] = s[i]; s[i] = t;
        }
    }

    static String bruteLargest(int[] nums) {
        String[] s = new String[nums.length];
        for (int i = 0; i < nums.length; i++) s[i] = String.valueOf(nums[i]);
        best = null;
        permute(s, 0);
        return best;
    }

    static int[] bruteFrequency(int[] nums) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int x : nums) freq.merge(x, 1, Integer::sum);
        int[] out = new int[nums.length];
        int n = 0;
        for (int f = 1; f <= nums.length; f++)
            for (int v = 100; v >= -100; v--)
                if (freq.getOrDefault(v, 0) == f) for (int c = 0; c < f; c++) out[n++] = v;
        return out;
    }

    static boolean before(int[] x, int[] y) {   // x strictly before y in sortRows order
        return x[0] < y[0] || (x[0] == y[0] && x[1] > y[1]);
    }

    public static void main(String[] args) {
        Random r = new Random(43);
        for (int t = 0; t < 1500; t++) {
            int n = 1 + r.nextInt(6);
            int[] nums = new int[n];
            for (int i = 0; i < n; i++) nums[i] = r.nextInt(3) == 0 ? r.nextInt(3) : r.nextInt(400);
            Check.eq(bruteLargest(nums), largestNumber(nums), "largest " + Arrays.toString(nums));
            int[] f = Check.ints(r, r.nextInt(12), -5, 5);
            Check.eq(bruteFrequency(f), frequencySort(f.clone()), "freq " + Arrays.toString(f));
            int m = r.nextInt(10);
            int[][] rows = new int[m][];
            for (int i = 0; i < m; i++) rows[i] = new int[] {r.nextInt(4), r.nextInt(4)};
            int[][] c = rows.clone();
            sortRows(rows);
            for (int i = 0; i + 1 < m; i++) Check.isTrue(!before(rows[i + 1], rows[i]), "rows order");
            Arrays.sort(c, BY_COLUMNS);
            Check.eq(rows, c, "comparator helpers agree");
            int[] a = Check.ints(r, m, 0, 4);
            Integer[] idx = sortedIndices(a);
            for (int i = 0; i + 1 < m; i++) {
                int p = idx[i], q = idx[i + 1];
                Check.isTrue(a[p] < a[q] || (a[p] == a[q] && p < q), "indices stable");
            }
            String[] w = new String[m];
            for (int i = 0; i < m; i++) w[i] = Check.str(r, 1 + r.nextInt(3), "ab");
            Arrays.sort(w, BY_LENGTH);
            for (int i = 0; i + 1 < m; i++)
                Check.isTrue(w[i].length() < w[i + 1].length()
                        || (w[i].length() == w[i + 1].length() && w[i].compareTo(w[i + 1]) <= 0), "words");
        }
        // big values and the zero case
        Check.eq("0", largestNumber(new int[] {0, 0, 0}), "all zeros");
        Check.eq("9999999991000000000", largestNumber(new int[] {1000000000, 999999999}), "big");
        // the traps quoted in the book
        Integer[] sub = {1, Integer.MIN_VALUE};
        Arrays.sort(sub, (x, y) -> x - y);
        Check.eq(1, (long) sub[0], "x - y puts 1 before MIN_VALUE");
        int thrown = 0;
        for (int t = 0; t < 200; t++) {
            Integer[] v = new Integer[100];
            for (int i = 0; i < v.length; i++) v[i] = r.nextInt(5);
            try { Arrays.sort(v, (x, y) -> x < y ? -1 : 1); } catch (IllegalArgumentException e) { thrown++; }
        }
        Check.isTrue(thrown > 0, "a comparator that never returns 0 can throw");
        // LeetCode samples
        Check.eq("210", largestNumber(new int[] {10, 2}), "LC 179 sample 1");
        Check.eq("9534330", largestNumber(new int[] {3, 30, 34, 5, 9}), "LC 179 sample 2");
        Check.eq(new int[] {3, 1, 1, 2, 2, 2}, frequencySort(new int[] {1, 1, 2, 2, 2, 3}), "LC 1636 sample 1");
        Check.eq(new int[] {1, 3, 3, 2, 2}, frequencySort(new int[] {2, 3, 1, 3, 2}), "LC 1636 sample 2");
        Check.eq(new int[] {5, -1, 4, 4, -6, -6, 1, 1, 1},
                frequencySort(new int[] {-1, 1, -6, 4, 5, -6, 1, 4, 1}), "LC 1636 sample 3");
        Check.done("P03CustomSort");
    }
}
