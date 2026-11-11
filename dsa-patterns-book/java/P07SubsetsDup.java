import java.util.*;

public class P07SubsetsDup {
    //@tpl subsetsdup
    static List<List<Integer>> subsetsWithDup(int[] a) {
        Arrays.sort(a);                                  // equal values side by side
        List<List<Integer>> out = new ArrayList<>();
        build(a, 0, new ArrayList<>(), out);
        return out;
    }

    // Every path is a subset. The loop picks the NEXT element from a[start..].
    static void build(int[] a, int start, List<Integer> path,
                      List<List<Integer>> out) {
        out.add(new ArrayList<>(path));
        for (int i = start; i < a.length; i++) {
            if (i > start && a[i] == a[i - 1]) continue;   // same value, same depth
            path.add(a[i]);
            build(a, i + 1, path, out);
            path.remove(path.size() - 1);
        }
    }
    //@end

    //@tpl combsum2
    // Combination sum II: each a[i] used at most once, no repeated combination.
    static List<List<Integer>> combinationSum2(int[] a, int target) {
        Arrays.sort(a);
        List<List<Integer>> out = new ArrayList<>();
        choose(a, 0, target, new ArrayList<>(), out);
        return out;
    }

    static void choose(int[] a, int start, int remain, List<Integer> path,
                       List<List<Integer>> out) {
        if (remain == 0) {
            out.add(new ArrayList<>(path));
            return;
        }
        for (int i = start; i < a.length; i++) {
            if (a[i] > remain) break;                      // sorted: rest too big
            if (i > start && a[i] == a[i - 1]) continue;   // same value, same depth
            path.add(a[i]);
            choose(a, i + 1, remain - a[i], path, out);    // i + 1: no reuse
            path.remove(path.size() - 1);
        }
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Every one of the 2^n index subsets, sorted, de-duplicated with a TreeSet.
    static List<List<Integer>> bruteDistinct(int[] a, Integer target) {
        int n = a.length;
        TreeSet<String> seen = new TreeSet<>();
        List<List<Integer>> out = new ArrayList<>();
        for (int mask = 0; mask < (1 << n); mask++) {
            List<Integer> s = new ArrayList<>();
            int sum = 0;
            for (int i = 0; i < n; i++) if ((mask >> i & 1) == 1) { s.add(a[i]); sum += a[i]; }
            if (target != null && sum != target) continue;
            Collections.sort(s);
            if (seen.add(s.toString())) out.add(s);
        }
        return out;
    }

    static boolean noRepeats(List<List<Integer>> got) {
        Set<String> s = new HashSet<>();
        for (List<Integer> l : got) {
            List<Integer> c = new ArrayList<>(l);
            Collections.sort(c);
            if (!s.add(c.toString())) return false;
        }
        return true;
    }

    static List<List<Integer>> lists(int[]... rows) {
        List<List<Integer>> out = new ArrayList<>();
        for (int[] row : rows) {
            List<Integer> l = new ArrayList<>();
            for (int x : row) l.add(x);
            out.add(l);
        }
        return out;
    }

    public static void main(String[] args) {
        Random r = new Random(72);
        for (int t = 0; t < 2000; t++) {
            int n = r.nextInt(11);
            int[] a = Check.ints(r, n, 1, 4);
            List<List<Integer>> got = subsetsWithDup(a.clone());
            Check.isTrue(noRepeats(got), "subsets II repeats " + Arrays.toString(a));
            Check.eq(Check.canon(bruteDistinct(a, null), true), Check.canon(got, true), "subsets II " + Arrays.toString(a));
            int target = 1 + r.nextInt(12);
            List<List<Integer>> cs = combinationSum2(a.clone(), target);
            Check.isTrue(noRepeats(cs), "comb sum II repeats " + Arrays.toString(a));
            Check.eq(Check.canon(bruteDistinct(a, target), true), Check.canon(cs, true),
                     "comb sum II " + Arrays.toString(a) + " t=" + target);
        }
        Check.eq(Check.canon(lists(new int[] {}, new int[] {1}, new int[] {1, 2}, new int[] {1, 2, 2}, new int[] {2}, new int[] {2, 2}), true),
                 Check.canon(subsetsWithDup(new int[] {1, 2, 2}), true), "LC 90 sample 1");
        Check.eq(Check.canon(lists(new int[] {1, 1, 6}, new int[] {1, 2, 5}, new int[] {1, 7}, new int[] {2, 6}), true),
                 Check.canon(combinationSum2(new int[] {10, 1, 2, 7, 6, 1, 5}, 8), true), "LC 40 sample 1");
        Check.eq(Check.canon(lists(new int[] {1, 2, 2}, new int[] {5}), true),
                 Check.canon(combinationSum2(new int[] {2, 5, 2, 1, 2}, 5), true), "LC 40 sample 2");
        Check.done("P07SubsetsDup");
    }
}
