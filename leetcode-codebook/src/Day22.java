import java.util.*;

/*@core Bitmask loops and the digit-DP skeleton
note: n ≤ 20 → think bitmask. Subsets of `mask`: `for (s = mask; s > 0; s = (s − 1) & mask)`. Digit DP: walk the digits of N left to right with a `tight` flag (still equal to N's prefix); memoise only the non-tight states. Count in [lo, hi] as `f(hi) − f(lo − 1)`.
@*/
class Core22 {
    static int countSubmasks(int mask) {
        int c = 1;                                        // the empty submask
        for (int s = mask; s > 0; s = (s - 1) & mask) c++;
        return c;
    }

    private static char[] dig;
    private static long[][] memo;
    private static int mod;

    static long countDigitSumDivisible(long n, int d) {   // x in [0, n] with digit sum % d == 0
        dig = Long.toString(n).toCharArray();
        mod = d;
        memo = new long[dig.length][d];
        for (long[] row : memo) Arrays.fill(row, -1);
        return go(0, 0, true);
    }

    private static long go(int pos, int rem, boolean tight) {
        if (pos == dig.length) return rem == 0 ? 1 : 0;
        if (!tight && memo[pos][rem] != -1) return memo[pos][rem];
        int limit = tight ? dig[pos] - '0' : 9;
        long res = 0;
        for (int x = 0; x <= limit; x++) res += go(pos + 1, (rem + x) % mod, tight && x == limit);
        if (!tight) memo[pos][rem] = res;
        return res;
    }
}

/*@LC 78
tag: Bitmask enumeration
cue: All subsets of distinct numbers.
hook: Every mask from 0 to 2ⁿ − 1 is one subset: bit i on means take nums[i].
time: O(n · 2ⁿ) time · O(n · 2ⁿ) output
trap: Use `1 << n` subsets, including the empty one.
@*/
class LC78 {
    public List<List<Integer>> subsets(int[] nums) {
        int n = nums.length;
        List<List<Integer>> res = new ArrayList<>();
        for (int mask = 0; mask < 1 << n; mask++) {                 //*
            List<Integer> cur = new ArrayList<>();
            for (int i = 0; i < n; i++) if (((mask >> i) & 1) == 1) cur.add(nums[i]);   //*
            res.add(cur);
        }
        return res;
    }
}

/*@var LC 90 · Subsets II (with duplicates): backtracking
note: Choose / skip recursion. Sort first; at one depth, skip a value equal to the previous one so each subset appears once.
@*/
class LC78v1 {
    public List<List<Integer>> subsetsWithDup(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> res = new ArrayList<>();
        build(nums, 0, new ArrayList<>(), res);
        return res;
    }

    private void build(int[] a, int start, List<Integer> cur, List<List<Integer>> res) {
        res.add(new ArrayList<>(cur));                    // copy: cur keeps changing
        for (int i = start; i < a.length; i++) {
            if (i > start && a[i] == a[i - 1]) continue;  // same value at the same depth
            cur.add(a[i]);
            build(a, i + 1, cur, res);
            cur.remove(cur.size() - 1);                   // undo
        }
    }
}

/*@var LC 46 · Permutations
note: Backtracking with a `used[]` array: try every unused number in the next position, then undo.
@*/
class LC78v2 {
    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        build(nums, new boolean[nums.length], new ArrayList<>(), res);
        return res;
    }

    private void build(int[] a, boolean[] used, List<Integer> cur, List<List<Integer>> res) {
        if (cur.size() == a.length) { res.add(new ArrayList<>(cur)); return; }
        for (int i = 0; i < a.length; i++) {
            if (used[i]) continue;
            used[i] = true;
            cur.add(a[i]);
            build(a, used, cur, res);
            cur.remove(cur.size() - 1);
            used[i] = false;
        }
    }
}

/*@var LC 39 · Combination Sum (reuse allowed)
note: Same skeleton; recurse with `i` (not `i + 1`) to allow reuse, and stop when the remainder goes negative.
@*/
class LC78v3 {
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        List<List<Integer>> res = new ArrayList<>();
        build(candidates, 0, target, new ArrayList<>(), res);
        return res;
    }

    private void build(int[] c, int start, int remain, List<Integer> cur, List<List<Integer>> res) {
        if (remain == 0) { res.add(new ArrayList<>(cur)); return; }
        for (int i = start; i < c.length; i++) {
            if (c[i] > remain) continue;
            cur.add(c[i]);
            build(c, i, remain - c[i], cur, res);         // i again: reuse
            cur.remove(cur.size() - 1);
        }
    }
}

/*@LC 1986
tag: Bitmask DP with a pair state
cue: Fewest work sessions of length sessionTime to finish all tasks (n ≤ 14).
hook: For each set of done tasks, keep (sessions used, time in the last one) and compare in that order.
time: O(n · 2ⁿ) time · O(2ⁿ) space
trap: Start from `{1, 0}`: one open, empty session.
var: LC 698 Partition to K Equal Sum Subsets | The same DP with sessionTime = total / k; answer is whether it fits in k.
@*/
class LC1986 {
    public int minSessions(int[] tasks, int sessionTime) {
        int n = tasks.length;
        int[][] dp = new int[1 << n][];                                    // {sessions, time used in the last}
        dp[0] = new int[]{1, 0};                                           //*
        for (int mask = 1; mask < 1 << n; mask++) {
            int[] best = null;
            for (int i = 0; i < n; i++) {
                if (((mask >> i) & 1) == 0) continue;
                int[] prev = dp[mask ^ (1 << i)];                           //* task i was the last one added
                int[] cand = prev[1] + tasks[i] <= sessionTime
                        ? new int[]{prev[0], prev[1] + tasks[i]}            //* fits in the open session
                        : new int[]{prev[0] + 1, tasks[i]};                 //* open a new session
                if (best == null || cand[0] < best[0] || (cand[0] == best[0] && cand[1] < best[1])) best = cand;
            }
            dp[mask] = best;
        }
        return dp[(1 << n) - 1][0];
    }
}

/*@LC 1879
tag: Assignment bitmask DP
cue: Pair every nums1[i] with a distinct nums2[j]; minimise the sum of XORs (n ≤ 14).
hook: The mask says which nums2 are taken; its bit count says which nums1 is next.
time: O(n · 2ⁿ) time · O(2ⁿ) space
trap: Do not loop over i separately: `i = Integer.bitCount(mask)` is forced.
var: LC 1947 Maximum Compatibility Score Sum | The same DP with max and a score table.
@*/
class LC1879 {
    public int minimumXORSum(int[] nums1, int[] nums2) {
        int n = nums1.length;
        int[] dp = new int[1 << n];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[0] = 0;
        for (int mask = 0; mask < 1 << n; mask++) {
            if (dp[mask] == Integer.MAX_VALUE) continue;
            int i = Integer.bitCount(mask);                                   //* next nums1 index
            if (i == n) continue;
            for (int j = 0; j < n; j++)
                if (((mask >> j) & 1) == 0)
                    dp[mask | 1 << j] = Math.min(dp[mask | 1 << j], dp[mask] + (nums1[i] ^ nums2[j]));   //*
        }
        return dp[(1 << n) - 1];
    }
}

/*@LC 902
tag: Digit counting (tight walk)
cue: How many numbers ≤ n can be written with the given digits?
hook: Shorter lengths are free: D^len. Same length: at each position count smaller digits × D^rest, continue only on an equal digit.
time: O(len · D) time · O(1) space
trap: If every digit of n is available, n itself counts: add 1 at the end.
@*/
class LC902 {
    public int atMostNGivenDigitSet(String[] digits, int n) {
        String s = String.valueOf(n);
        int L = s.length(), D = digits.length, count = 0;
        int[] pow = new int[L + 1];
        pow[0] = 1;
        for (int i = 1; i <= L; i++) pow[i] = pow[i - 1] * D;
        for (int len = 1; len < L; len++) count += pow[len];                //* all shorter numbers
        for (int i = 0; i < L; i++) {
            char c = s.charAt(i);
            int smaller = 0;
            boolean equal = false;
            for (String d : digits) {
                if (d.charAt(0) < c) smaller++;
                else if (d.charAt(0) == c) equal = true;
            }
            count += smaller * pow[L - i - 1];                              //* smaller digit here: rest is free
            if (!equal) return count;                                       //* cannot stay tight
        }
        return count + 1;                                                   //* n itself
    }
}

/*@LC 51
tag: Backtracking with flags
cue: Place n queens so none attack each other; list all boards.
hook: One queen per row; columns and both diagonals each get a "used" flag.
time: O(n!) time · O(n) space
trap: Diagonals: `r + c` for one direction, `r − c + n − 1` for the other (shifted to stay ≥ 0).
var: LC 52 N-Queens II | Count only, no boards (code below with bitmasks).
@*/
class LC51 {
    public List<List<String>> solveNQueens(int n) {
        List<List<String>> res = new ArrayList<>();
        place(0, n, new int[n], new boolean[n], new boolean[2 * n], new boolean[2 * n], res);
        return res;
    }

    private void place(int r, int n, int[] colOf, boolean[] col, boolean[] d1, boolean[] d2, List<List<String>> res) {
        if (r == n) {
            List<String> board = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                char[] row = new char[n];
                Arrays.fill(row, '.');
                row[colOf[i]] = 'Q';
                board.add(new String(row));
            }
            res.add(board);
            return;
        }
        for (int c = 0; c < n; c++) {
            if (col[c] || d1[r + c] || d2[r - c + n - 1]) continue;                     //*
            col[c] = d1[r + c] = d2[r - c + n - 1] = true;                              //* place
            colOf[r] = c;
            place(r + 1, n, colOf, col, d1, d2, res);
            col[c] = d1[r + c] = d2[r - c + n - 1] = false;                             //* undo
        }
    }
}

/*@var LC 52 · N-Queens II with bitmasks
note: Three masks of attacked columns; shift the diagonal masks by one each row. `free & -free` picks the lowest free column.
@*/
class LC51v1 {
    public int totalNQueens(int n) {
        return count(n, 0, 0, 0, 0);
    }

    private int count(int n, int r, int cols, int d1, int d2) {
        if (r == n) return 1;
        int total = 0, free = ~(cols | d1 | d2) & ((1 << n) - 1);
        while (free != 0) {
            int bit = free & -free;
            free -= bit;
            total += count(n, r + 1, cols | bit, (d1 | bit) << 1, (d2 | bit) >> 1);
        }
        return total;
    }
}

/*@LC 847
tag: BFS over (node, visited mask)
cue: Shortest walk that visits every node (revisits allowed), n ≤ 12.
hook: The state is where you are plus who you have seen; start from every node at once.
time: O(n² · 2ⁿ) time · O(n · 2ⁿ) space
trap: Revisits are allowed, so a plain node BFS is wrong; the mask makes states unique.
@*/
class LC847 {
    public int shortestPathLength(int[][] graph) {
        int n = graph.length, full = (1 << n) - 1;
        boolean[][] seen = new boolean[n][1 << n];
        Deque<int[]> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) { q.offer(new int[]{i, 1 << i}); seen[i][1 << i] = true; }   //* all starts
        for (int steps = 0; !q.isEmpty(); steps++) {
            for (int size = q.size(); size > 0; size--) {
                int[] s = q.poll();
                if (s[1] == full) return steps;
                for (int v : graph[s[0]]) {
                    int m = s[1] | 1 << v;                                                    //*
                    if (!seen[v][m]) { seen[v][m] = true; q.offer(new int[]{v, m}); }
                }
            }
        }
        return 0;
    }
}

/*@var Shortest Hamiltonian path with weights (bitmask DP, TSP style)
note: `dp[mask][v]` = cheapest way to visit exactly `mask` and stand at v. Extend by one unvisited node at a time.
@*/
class LC847v1 {
    static long shortestHamiltonian(long[][] dist) {
        int n = dist.length;
        long INF = Long.MAX_VALUE / 4;
        long[][] dp = new long[1 << n][n];
        for (long[] row : dp) Arrays.fill(row, INF);
        for (int v = 0; v < n; v++) dp[1 << v][v] = 0;
        for (int mask = 1; mask < 1 << n; mask++)
            for (int v = 0; v < n; v++) {
                if (dp[mask][v] >= INF) continue;
                for (int w = 0; w < n; w++)
                    if (((mask >> w) & 1) == 0)
                        dp[mask | 1 << w][w] = Math.min(dp[mask | 1 << w][w], dp[mask][v] + dist[v][w]);
            }
        long best = INF;
        for (int v = 0; v < n; v++) best = Math.min(best, dp[(1 << n) - 1][v]);
        return best;
    }
}

/*@LC 2827
tag: Digit DP with several counters
cue: Count numbers in [low, high] with as many even digits as odd digits and divisible by k.
hook: State = (position, even − odd, remainder, tight, started); answer f(high) − f(low − 1).
time: O(10 · 21 · k · 10) states × work · small
trap: Leading zeros are not digits: only count parity once `started` is true.
@*/
class LC2827 {
    private char[] d;
    private int k;
    private Integer[][][][] memo;

    public int numberOfBeautifulIntegers(int low, int high, int k) {
        return count(high, k) - count(low - 1, k);                         //*
    }

    private int count(int n, int k) {
        if (n <= 0) return 0;
        d = String.valueOf(n).toCharArray();
        this.k = k;
        memo = new Integer[d.length][21][k][2];
        return go(0, 10, 0, true, false);                                   // diff offset by 10
    }

    private int go(int pos, int diff, int rem, boolean tight, boolean started) {
        if (pos == d.length) return started && diff == 10 && rem == 0 ? 1 : 0;
        int s = started ? 1 : 0;
        if (!tight && memo[pos][diff][rem][s] != null) return memo[pos][diff][rem][s];   //* memo only when free
        int limit = tight ? d[pos] - '0' : 9, res = 0;
        for (int x = 0; x <= limit; x++) {
            boolean st = started || x != 0;
            int nd = st ? diff + (x % 2 == 0 ? 1 : -1) : diff;                         //* leading zeros don't count
            res += go(pos + 1, nd, (rem * 10 + x) % k, tight && x == limit, st);
        }
        if (!tight) memo[pos][diff][rem][s] = res;
        return res;
    }
}

public class Day22 {
    static int sessionsBrute(int[] t, int cap) {
        for (int k = 1; k <= t.length; k++) if (fits(t, 0, new int[k], cap)) return k;
        return t.length;
    }

    static boolean fits(int[] t, int i, int[] load, int cap) {
        if (i == t.length) return true;
        for (int b = 0; b < load.length; b++) {
            if (load[b] + t[i] > cap) continue;
            load[b] += t[i];
            if (fits(t, i + 1, load, cap)) { load[b] -= t[i]; return true; }
            load[b] -= t[i];
            if (load[b] == 0) break;
        }
        return false;
    }

    public static void main(String[] args) {
        T.eq("core submask", Core22.countSubmasks(0b1011), 8);
        for (int t = 0; t < 100; t++) {
            long n = T.rnd(0, 3000);
            int d = T.rnd(1, 9), c = 0;
            for (long x = 0; x <= n; x++) { int s = 0; for (char ch : Long.toString(x).toCharArray()) s += ch - '0'; if (s % d == 0) c++; }
            T.eq("core digit", Core22.countDigitSumDivisible(n, d), (long) c);
        }

        T.eq("78a", T.canon(new LC78().subsets(T.ints("[1,2,3]"))), T.canon(List.of(List.of(), List.of(1), List.of(2), List.of(1, 2), List.of(3), List.of(1, 3), List.of(2, 3), List.of(1, 2, 3))));
        T.eq("78b", new LC78().subsets(T.ints("[0]")).size(), 2);
        T.eq("90a", T.canon(new LC78v1().subsetsWithDup(T.ints("[1,2,2]"))), T.canon(List.of(List.of(), List.of(1), List.of(1, 2), List.of(1, 2, 2), List.of(2), List.of(2, 2))));
        T.eq("46a", new LC78v2().permute(T.ints("[1,2,3]")).size(), 6);
        T.eq("46b", new HashSet<>(new LC78v2().permute(T.ints("[1,2,3]"))).size(), 6);
        T.eq("39a", T.canon(new LC78v3().combinationSum(T.ints("[2,3,6,7]"), 7)), T.canon(List.of(List.of(2, 2, 3), List.of(7))));
        T.eq("39b", T.canon(new LC78v3().combinationSum(T.ints("[2,3,5]"), 8)), T.canon(List.of(List.of(2, 2, 2, 2), List.of(2, 3, 3), List.of(3, 5))));
        T.eq("39c", new LC78v3().combinationSum(T.ints("[2]"), 1).size(), 0);

        T.eq("1986a", new LC1986().minSessions(T.ints("[1,2,3]"), 3), 2);
        T.eq("1986b", new LC1986().minSessions(T.ints("[3,1,3,1,1]"), 8), 2);
        T.eq("1986c", new LC1986().minSessions(T.ints("[1,2,3,4,5]"), 15), 1);
        for (int t = 0; t < 200; t++) {
            int cap = T.rnd(3, 10);
            int[] a = T.rand(T.rnd(1, 8), 1, cap);
            T.eq("1986 rand", new LC1986().minSessions(a, cap), sessionsBrute(a, cap));
        }

        T.eq("1879a", new LC1879().minimumXORSum(T.ints("[1,2]"), T.ints("[2,3]")), 2);
        T.eq("1879b", new LC1879().minimumXORSum(T.ints("[1,0,3]"), T.ints("[5,3,4]")), 8);
        for (int t = 0; t < 100; t++) {
            int n = T.rnd(1, 6);
            int[] a = T.rand(n, 0, 20), b = T.rand(n, 0, 20);
            int best = Integer.MAX_VALUE;
            for (List<Integer> p : new LC78v2().permute(java.util.stream.IntStream.range(0, n).toArray())) {
                int s = 0;
                for (int i = 0; i < n; i++) s += a[i] ^ b[p.get(i)];
                best = Math.min(best, s);
            }
            T.eq("1879 rand", new LC1879().minimumXORSum(a, b), best);
        }

        T.eq("902a", new LC902().atMostNGivenDigitSet(new String[]{"1", "3", "5", "7"}, 100), 20);
        T.eq("902b", new LC902().atMostNGivenDigitSet(new String[]{"1", "4", "9"}, 1000000000), 29523);
        T.eq("902c", new LC902().atMostNGivenDigitSet(new String[]{"7"}, 8), 1);
        for (int t = 0; t < 100; t++) {
            TreeSet<Integer> ds = new TreeSet<>();
            int m = T.rnd(1, 4);
            while (ds.size() < m) ds.add(T.rnd(1, 9));
            String[] digs = ds.stream().map(String::valueOf).toArray(String[]::new);
            int n = T.rnd(1, 5000), c = 0;
            for (int x = 1; x <= n; x++) { boolean ok = true; for (char ch : String.valueOf(x).toCharArray()) if (!ds.contains(ch - '0')) ok = false; if (ok) c++; }
            T.eq("902 rand", new LC902().atMostNGivenDigitSet(digs, n), c);
        }

        T.eq("51a", T.canon(new LC51().solveNQueens(4)), T.canon(List.of(List.of(".Q..", "...Q", "Q...", "..Q."), List.of("..Q.", "Q...", "...Q", ".Q.."))));
        T.eq("51b", new LC51().solveNQueens(1).toString(), "[[Q]]");
        T.eq("51c", new LC51().solveNQueens(8).size(), 92);
        T.eq("52", new int[]{new LC51v1().totalNQueens(1), new LC51v1().totalNQueens(4), new LC51v1().totalNQueens(8)}, T.ints("[1,2,92]"));

        T.eq("847a", new LC847().shortestPathLength(new int[][]{{1, 2, 3}, {0}, {0}, {0}}), 4);
        T.eq("847b", new LC847().shortestPathLength(new int[][]{{1}, {0, 2, 4}, {1, 3, 4}, {2}, {1, 2}}), 4);
        T.eq("847c", new LC847().shortestPathLength(new int[][]{{}}), 0);
        long[][] dist = {{0, 1, 10}, {1, 0, 2}, {10, 2, 0}};
        T.eq("tsp", LC847v1.shortestHamiltonian(dist), 3L);

        T.eq("2827a", new LC2827().numberOfBeautifulIntegers(10, 20, 3), 2);
        T.eq("2827b", new LC2827().numberOfBeautifulIntegers(1, 10, 1), 1);
        T.eq("2827c", new LC2827().numberOfBeautifulIntegers(5, 5, 2), 0);
        for (int t = 0; t < 100; t++) {
            int lo = T.rnd(1, 3000), hi = lo + T.rnd(0, 3000), k = T.rnd(1, 20), c = 0;
            for (int x = lo; x <= hi; x++) {
                if (x % k != 0) continue;
                int e = 0, o = 0;
                for (char ch : String.valueOf(x).toCharArray()) if ((ch - '0') % 2 == 0) e++; else o++;
                if (e == o) c++;
            }
            T.eq("2827 rand", new LC2827().numberOfBeautifulIntegers(lo, hi, k), c);
        }
        T.summary("Day22");
    }
}
