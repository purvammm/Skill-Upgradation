import java.util.*;

public class P07Subsets {
    //@tpl subsets
    static List<List<Integer>> subsets(int[] a) {
        List<List<Integer>> out = new ArrayList<>();
        pick(a, 0, new ArrayList<>(), out);
        return out;
    }

    // Decide a[i]: leave it out, or put it in. At i == n, path is one subset.
    static void pick(int[] a, int i, List<Integer> path, List<List<Integer>> out) {
        if (i == a.length) {
            out.add(new ArrayList<>(path));        // copy: path keeps changing
            return;
        }
        pick(a, i + 1, path, out);                 // 1. without a[i]
        path.add(a[i]);                            // 2. with a[i] ...
        pick(a, i + 1, path, out);
        path.remove(path.size() - 1);              // ... then undo the choice
    }
    //@end

    //@tpl bitmask
    // The same 2^n subsets without recursion: bit i of mask means "a[i] is in".
    static List<List<Integer>> subsetsBitmask(int[] a) {
        int n = a.length;
        List<List<Integer>> out = new ArrayList<>();
        for (int mask = 0; mask < (1 << n); mask++) {
            List<Integer> s = new ArrayList<>();
            for (int i = 0; i < n; i++)
                if ((mask >> i & 1) == 1) s.add(a[i]);
            out.add(s);
        }
        return out;
    }
    //@end

    //@tpl lettercase
    // Letter case permutation: a digit has one choice, a letter has two.
    static List<String> letterCasePermutation(String s) {
        List<String> out = new ArrayList<>();
        flip(s.toCharArray(), 0, out);
        return out;
    }

    static void flip(char[] c, int i, List<String> out) {
        if (i == c.length) {
            out.add(new String(c));
            return;
        }
        flip(c, i + 1, out);                       // keep c[i] as it is
        if (Character.isLetter(c[i])) {
            c[i] ^= 32;                            // swap case: 'a' (97) <-> 'A' (65)
            flip(c, i + 1, out);
            c[i] ^= 32;                            // restore
        }
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Start from {[]} and, for each element, add a copy of every subset with it.
    static List<List<Integer>> bruteSubsets(int[] a) {
        List<List<Integer>> all = new ArrayList<>();
        all.add(new ArrayList<>());
        for (int x : a) {
            int size = all.size();
            for (int k = 0; k < size; k++) {
                List<Integer> c = new ArrayList<>(all.get(k));
                c.add(x);
                all.add(c);
            }
        }
        return all;
    }

    static List<String> bruteCase(String s) {
        List<String> all = new ArrayList<>();
        all.add("");
        for (char ch : s.toCharArray()) {
            List<String> next = new ArrayList<>();
            for (String p : all) {
                if (Character.isLetter(ch)) {
                    next.add(p + Character.toLowerCase(ch));
                    next.add(p + Character.toUpperCase(ch));
                } else next.add(p + ch);
            }
            all = next;
        }
        return all;
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
        Random r = new Random(71);
        for (int t = 0; t < 1500; t++) {
            int n = r.nextInt(9);
            int[] a = Check.ints(r, n, -5, 5);
            List<List<Integer>> want = bruteSubsets(a);
            List<List<Integer>> got = subsets(a), got2 = subsetsBitmask(a);
            Check.eq(1 << n, got.size(), "count " + Arrays.toString(a));
            Check.eq(1 << n, got2.size(), "count bitmask " + Arrays.toString(a));
            Check.eq(Check.canon(want, false), Check.canon(got, false), "subsets " + Arrays.toString(a));
            Check.eq(Check.canon(want, false), Check.canon(got2, false), "bitmask " + Arrays.toString(a));
            String s = Check.str(r, r.nextInt(8), "aB1zZ9");
            Check.eq(Check.sortedCopy(bruteCase(s)), Check.sortedCopy(letterCasePermutation(s)), "case " + s);
        }
        Check.eq(Check.canon(lists(new int[] {}, new int[] {1}, new int[] {1, 2}, new int[] {1, 2, 3}, new int[] {1, 3}, new int[] {2}, new int[] {2, 3}, new int[] {3}), false),
                 Check.canon(subsets(new int[] {1, 2, 3}), false), "LC 78 sample 1");
        Check.eq(Check.canon(lists(new int[] {}, new int[] {0}), false),
                 Check.canon(subsets(new int[] {0}), false), "LC 78 sample 2");
        Check.eq("[A1B2, A1b2, a1B2, a1b2]", Check.sortedCopy(letterCasePermutation("a1b2")), "LC 784 sample 1");
        Check.eq("[3Z4, 3z4]", Check.sortedCopy(letterCasePermutation("3z4")), "LC 784 sample 2");
        Check.done("P07Subsets");
    }
}
