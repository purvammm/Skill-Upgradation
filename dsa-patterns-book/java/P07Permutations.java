import java.util.*;

public class P07Permutations {
    //@tpl permute
    static List<List<Integer>> permute(int[] a) {
        List<List<Integer>> out = new ArrayList<>();
        arrange(a, new boolean[a.length], new ArrayList<>(), out);
        return out;
    }

    // Fill the next position with every element not used yet.
    static void arrange(int[] a, boolean[] used, List<Integer> path,
                        List<List<Integer>> out) {
        if (path.size() == a.length) {
            out.add(new ArrayList<>(path));
            return;
        }
        for (int i = 0; i < a.length; i++) {
            if (used[i]) continue;
            used[i] = true; path.add(a[i]);                 // choose
            arrange(a, used, path, out);                    // explore
            used[i] = false; path.remove(path.size() - 1);  // undo
        }
    }
    //@end

    //@tpl permuteunique
    // Permutations II: sort, then use the copies of a value only left to right.
    static List<List<Integer>> permuteUnique(int[] a) {
        Arrays.sort(a);
        List<List<Integer>> out = new ArrayList<>();
        arrangeUnique(a, new boolean[a.length], new ArrayList<>(), out);
        return out;
    }

    static void arrangeUnique(int[] a, boolean[] used, List<Integer> path,
                              List<List<Integer>> out) {
        if (path.size() == a.length) {
            out.add(new ArrayList<>(path));
            return;
        }
        for (int i = 0; i < a.length; i++) {
            if (used[i]) continue;
            // left copy of this value still free: taking this copy first would
            // repeat a permutation that starts with the left copy
            if (i > 0 && a[i] == a[i - 1] && !used[i - 1]) continue;
            used[i] = true; path.add(a[i]);
            arrangeUnique(a, used, path, out);
            used[i] = false; path.remove(path.size() - 1);
        }
    }
    //@end

    //@tpl nextperm
    // Next permutation in lexicographic order, in place (the largest wraps round).
    static void nextPermutation(int[] a) {
        int i = a.length - 2;
        while (i >= 0 && a[i] >= a[i + 1]) i--;     // 1. pivot: last a[i] < a[i+1]
        if (i >= 0) {
            int j = a.length - 1;
            while (a[j] <= a[i]) j--;               // 2. rightmost a[j] > a[i]
            swap(a, i, j);
        }
        reverse(a, i + 1, a.length - 1);            // 3. suffix was descending
    }

    static void swap(int[] a, int i, int j) {
        int t = a[i]; a[i] = a[j]; a[j] = t;
    }

    static void reverse(int[] a, int l, int r) {
        while (l < r) swap(a, l++, r--);
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // All n^n index sequences, keep those that use every index once.
    static List<List<Integer>> brutePerms(int[] a) {
        int n = a.length;
        List<List<Integer>> out = new ArrayList<>();
        int total = 1;
        for (int k = 0; k < n; k++) total *= n;
        for (int code = 0; code < total; code++) {
            int c = code;
            int[] idx = new int[n];
            boolean[] seen = new boolean[n];
            boolean ok = true;
            for (int k = 0; k < n; k++) {
                idx[k] = c % n;
                c /= n;
                if (seen[idx[k]]) ok = false;
                seen[idx[k]] = true;
            }
            if (!ok) continue;
            List<Integer> p = new ArrayList<>();
            for (int k : idx) p.add(a[k]);
            out.add(p);
        }
        return out;
    }

    static List<List<Integer>> distinct(List<List<Integer>> all) {
        TreeMap<String, List<Integer>> m = new TreeMap<>();
        for (List<Integer> p : all) m.put(p.toString(), p);
        return new ArrayList<>(m.values());
    }

    static int cmp(List<Integer> x, List<Integer> y) {
        for (int k = 0; k < x.size(); k++) if (!x.get(k).equals(y.get(k))) return Integer.compare(x.get(k), y.get(k));
        return 0;
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
        Random r = new Random(73);
        for (int t = 0; t < 1500; t++) {
            int n = r.nextInt(7);
            int[] a = Check.ints(r, n, 1, 3);
            List<List<Integer>> all = brutePerms(a);
            Check.eq(Check.canon(all, false), Check.canon(permute(a.clone()), false), "permute " + Arrays.toString(a));
            List<List<Integer>> uniq = distinct(all);
            List<List<Integer>> got = permuteUnique(a.clone());
            Check.eq(uniq.size(), got.size(), "permuteUnique count " + Arrays.toString(a));
            Check.eq(Check.canon(uniq, false), Check.canon(got, false), "permuteUnique " + Arrays.toString(a));
            if (n == 0) continue;
            // next permutation: the next distinct arrangement in sorted order, or the first
            uniq.sort(P07Permutations::cmp);
            List<Integer> cur = uniq.get(r.nextInt(uniq.size()));
            int pos = uniq.indexOf(cur);
            List<Integer> want = uniq.get((pos + 1) % uniq.size());
            int[] b = new int[n];
            for (int k = 0; k < n; k++) b[k] = cur.get(k);
            nextPermutation(b);
            List<Integer> gotNext = new ArrayList<>();
            for (int x : b) gotNext.add(x);
            Check.eq(want, gotNext, "next of " + cur);
        }
        Check.eq(6, permute(new int[] {1, 2, 3}).size(), "LC 46 sample 1");
        Check.eq(Check.canon(lists(new int[] {0, 1}, new int[] {1, 0}), false),
                 Check.canon(permute(new int[] {0, 1}), false), "LC 46 sample 2");
        Check.eq(Check.canon(lists(new int[] {1, 1, 2}, new int[] {1, 2, 1}, new int[] {2, 1, 1}), false),
                 Check.canon(permuteUnique(new int[] {1, 1, 2}), false),
                 "LC 47 sample 1");
        int[] p = {1, 2, 3};
        nextPermutation(p);
        Check.eq(new int[] {1, 3, 2}, p, "LC 31 sample 1");
        p = new int[] {3, 2, 1};
        nextPermutation(p);
        Check.eq(new int[] {1, 2, 3}, p, "LC 31 sample 2");
        p = new int[] {1, 1, 5};
        nextPermutation(p);
        Check.eq(new int[] {1, 5, 1}, p, "LC 31 sample 3");
        p = new int[] {1, 3, 5, 4, 2};
        nextPermutation(p);
        Check.eq(new int[] {1, 4, 2, 3, 5}, p, "doc example");
        Check.done("P07Permutations");
    }
}
