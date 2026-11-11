import java.util.*;

public class P07CombinationSum {
    //@tpl combsum
    // Combination sum: distinct positive candidates, each usable any number of times.
    static List<List<Integer>> combinationSum(int[] a, int target) {
        Arrays.sort(a);                                  // lets the loop stop early
        List<List<Integer>> out = new ArrayList<>();
        grow(a, 0, target, new ArrayList<>(), out);
        return out;
    }

    static void grow(int[] a, int start, int remain, List<Integer> path,
                     List<List<Integer>> out) {
        if (remain == 0) {
            out.add(new ArrayList<>(path));
            return;
        }
        for (int i = start; i < a.length && a[i] <= remain; i++) {
            path.add(a[i]);
            grow(a, i, remain - a[i], path, out);        // i, not i + 1: reuse a[i]
            path.remove(path.size() - 1);
        }
    }
    //@end

    //@tpl combine
    // Combinations: every k-element subset of 1..n, each in increasing order.
    static List<List<Integer>> combine(int n, int k) {
        List<List<Integer>> out = new ArrayList<>();
        pickK(1, n, k, new ArrayList<>(), out);
        return out;
    }

    static void pickK(int start, int n, int k, List<Integer> path,
                      List<List<Integer>> out) {
        if (path.size() == k) {
            out.add(new ArrayList<>(path));
            return;
        }
        int need = k - path.size();
        for (int x = start; x <= n - need + 1; x++) {    // prune: leave `need` values
            path.add(x);
            pickK(x + 1, n, k, path, out);
            path.remove(path.size() - 1);
        }
    }
    //@end

    //@tpl phone
    static final String[] KEYS =
        {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};

    // One digit per depth; the choices at depth i are the letters of digit i.
    static List<String> letterCombinations(String digits) {
        List<String> out = new ArrayList<>();
        if (digits.isEmpty()) return out;                // [] for "", not [""]
        spell(digits, 0, new StringBuilder(), out);
        return out;
    }

    static void spell(String d, int i, StringBuilder sb, List<String> out) {
        if (i == d.length()) {
            out.add(sb.toString());
            return;
        }
        for (char c : KEYS[d.charAt(i) - '0'].toCharArray()) {
            sb.append(c);
            spell(d, i + 1, sb, out);
            sb.setLength(sb.length() - 1);               // undo
        }
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Choose a count 0..remain/a[i] for each candidate in turn.
    static void byCounts(int[] a, int i, int remain, List<Integer> cur, List<List<Integer>> out) {
        if (i == a.length) {
            if (remain == 0) out.add(new ArrayList<>(cur));
            return;
        }
        for (int c = 0; c * a[i] <= remain; c++) {
            for (int k = 0; k < c; k++) cur.add(a[i]);
            byCounts(a, i + 1, remain - c * a[i], cur, out);
            for (int k = 0; k < c; k++) cur.remove(cur.size() - 1);
        }
    }

    static List<List<Integer>> bruteCombine(int n, int k) {
        List<List<Integer>> out = new ArrayList<>();
        for (int mask = 0; mask < (1 << n); mask++) {
            if (Integer.bitCount(mask) != k) continue;
            List<Integer> s = new ArrayList<>();
            for (int b = 0; b < n; b++) if ((mask >> b & 1) == 1) s.add(b + 1);
            out.add(s);
        }
        return out;
    }

    static List<String> brutePhone(String d) {
        List<String> all = new ArrayList<>();
        if (d.isEmpty()) return all;
        all.add("");
        for (char ch : d.toCharArray()) {
            List<String> next = new ArrayList<>();
            for (String p : all) for (char c : KEYS[ch - '0'].toCharArray()) next.add(p + c);
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
        Random r = new Random(74);
        for (int t = 0; t < 1500; t++) {
            // distinct candidates in 2..12
            List<Integer> pool = new ArrayList<>();
            for (int v = 2; v <= 12; v++) pool.add(v);
            Collections.shuffle(pool, r);
            int m = 1 + r.nextInt(5);
            int[] a = new int[m];
            for (int i = 0; i < m; i++) a[i] = pool.get(i);
            int target = 1 + r.nextInt(25);
            List<List<Integer>> want = new ArrayList<>();
            byCounts(a, 0, target, new ArrayList<>(), want);
            Check.eq(Check.canon(want, true), Check.canon(combinationSum(a.clone(), target), true),
                     "combsum " + Arrays.toString(a) + " t=" + target);
            int n = 1 + r.nextInt(11), k = r.nextInt(n + 1);
            List<List<Integer>> got = combine(n, k);
            Check.eq(Check.canon(bruteCombine(n, k), false), Check.canon(got, false), "combine " + n + "," + k);
            String d = Check.str(r, r.nextInt(5), "23456789");
            Check.eq(Check.sortedCopy(brutePhone(d)), Check.sortedCopy(letterCombinations(d)), "phone " + d);
        }
        Check.eq(Check.canon(lists(new int[] {2, 2, 3}, new int[] {7}), true),
                 Check.canon(combinationSum(new int[] {2, 3, 6, 7}, 7), true), "LC 39 sample 1");
        Check.eq(Check.canon(lists(new int[] {2, 2, 2, 2}, new int[] {2, 3, 3}, new int[] {3, 5}), true),
                 Check.canon(combinationSum(new int[] {2, 3, 5}, 8), true),
                 "LC 39 sample 2");
        Check.eq(0, combinationSum(new int[] {2}, 1).size(), "LC 39 sample 3");
        Check.eq(Check.canon(lists(new int[] {1, 2}, new int[] {1, 3}, new int[] {1, 4}, new int[] {2, 3}, new int[] {2, 4}, new int[] {3, 4}), false),
                 Check.canon(combine(4, 2), false),
                 "LC 77 sample 1");
        Check.eq(Check.canon(lists(new int[] {1}), false),
                 Check.canon(combine(1, 1), false), "LC 77 sample 2");
        Check.eq("[ad, ae, af, bd, be, bf, cd, ce, cf]", Check.sortedCopy(letterCombinations("23")), "LC 17 sample 1");
        Check.eq(0, letterCombinations("").size(), "LC 17 sample 2");
        Check.eq("[a, b, c]", Check.sortedCopy(letterCombinations("2")), "LC 17 sample 3");
        Check.done("P07CombinationSum");
    }
}
