import java.util.*;

/*@core Fenwick tree and iterative segment tree
note: Fenwick (1-indexed): `add` climbs with `i += i & -i`, `prefix` falls with `i -= i & -i`. Segment tree (iterative, size n): leaves at `n + i`, parent of p is `p / 2`; query walks l and r inward on a half-open range [l, r).
@*/
class Core20 {
    static final class Fenwick {
        final long[] t;

        Fenwick(int n) { t = new long[n + 1]; }

        void add(int i, long v) { for (i++; i < t.length; i += i & -i) t[i] += v; }   // 0-indexed i

        long prefix(int i) { long s = 0; for (; i > 0; i -= i & -i) s += t[i]; return s; }   // sum of [0, i)

        long range(int l, int r) { return prefix(r + 1) - prefix(l); }                 // sum of [l, r]
    }

    static final class MaxSegTree {
        final int n;
        final int[] t;

        MaxSegTree(int n) { this.n = n; t = new int[2 * n]; }

        void set(int i, int v) {
            for (t[i += n] = v; i > 1; i >>= 1) t[i >> 1] = Math.max(t[i], t[i ^ 1]);
        }

        int max(int l, int r) {                         // max on [l, r), 0 if empty
            int res = 0;
            for (l += n, r += n; l < r; l >>= 1, r >>= 1) {
                if ((l & 1) == 1) res = Math.max(res, t[l++]);
                if ((r & 1) == 1) res = Math.max(res, t[--r]);
            }
            return res;
        }
    }
}

/*@LC 307
class: NumArray
tag: Fenwick tree
cue: Point updates and range sums, mixed together.
hook: Store the difference on update; a range is prefix(r + 1) − prefix(l).
time: O(log n) per operation · O(n) space
trap: `update` SETS a value: add `val − nums[index]` and remember the new value.
@*/
class LC307 {
    private final int n;
    private final int[] nums, tree;                       // tree is 1-indexed

    public LC307(int[] nums) {
        n = nums.length;
        this.nums = new int[n];
        tree = new int[n + 1];
        for (int i = 0; i < n; i++) update(i, nums[i]);
    }

    public void update(int index, int val) {
        int delta = val - nums[index];                    //*
        nums[index] = val;
        for (int i = index + 1; i <= n; i += i & -i) tree[i] += delta;   //* climb
    }

    public int sumRange(int left, int right) {
        return prefix(right + 1) - prefix(left);          //*
    }

    private int prefix(int i) {                           // sum of the first i numbers
        int s = 0;
        for (; i > 0; i -= i & -i) s += tree[i];          //* fall
        return s;
    }
}

/*@var Segment tree version (iterative, also gives min / max / gcd)
class: NumArray
note: Swap `+` for `Math.max`, `Math.min` or `gcd` and the same code answers range max / min / gcd (CF 914D uses gcd).
@*/
class LC307v1 {
    private final int n;
    private final int[] t;

    public LC307v1(int[] nums) {
        n = nums.length;
        t = new int[2 * n];
        for (int i = 0; i < n; i++) t[n + i] = nums[i];
        for (int i = n - 1; i >= 1; i--) t[i] = t[2 * i] + t[2 * i + 1];
    }

    public void update(int index, int val) {
        int p = index + n;
        t[p] = val;
        for (p >>= 1; p >= 1; p >>= 1) t[p] = t[2 * p] + t[2 * p + 1];
    }

    public int sumRange(int left, int right) {
        int s = 0;
        for (int l = left + n, r = right + n + 1; l < r; l >>= 1, r >>= 1) {
            if ((l & 1) == 1) s += t[l++];
            if ((r & 1) == 1) s += t[--r];
        }
        return s;
    }
}

/*@LC 315
tag: Fenwick over compressed values
cue: For each element, how many smaller elements are to its right?
hook: Walk from the right; ask the tree how many smaller values it has seen, then add yourself.
time: O(n log n) time · O(n) space
trap: Compress values to ranks 1..m first (they can be negative); query `rank − 1` for STRICTLY smaller.
@*/
class LC315 {
    public List<Integer> countSmaller(int[] nums) {
        int n = nums.length;
        int[] sorted = nums.clone();
        Arrays.sort(sorted);
        int[] tree = new int[n + 1];
        Integer[] ans = new Integer[n];
        for (int i = n - 1; i >= 0; i--) {
            int rank = Arrays.binarySearch(sorted, nums[i]);         //* some index of nums[i]
            while (rank > 0 && sorted[rank - 1] == nums[i]) rank--;  //* first index = count of smaller values
            int c = 0;
            for (int j = rank; j > 0; j -= j & -j) c += tree[j];     //* seen values with rank < mine
            ans[i] = c;
            for (int j = rank + 1; j <= n; j += j & -j) tree[j]++;   //* add myself at rank + 1
        }
        return Arrays.asList(ans);
    }
}

/*@var Count inversions with merge sort (GfG, also LC 493 Reverse Pairs)
note: While merging, every time the right half wins, all remaining left elements form inversions with it. For LC 493 count `a[i] > 2L·a[j]` with a separate pointer before merging.
@*/
class LC315v1 {
    static long inversions(int[] a) {
        return sort(a.clone(), new int[a.length], 0, a.length - 1);
    }

    private static long sort(int[] a, int[] tmp, int lo, int hi) {
        if (lo >= hi) return 0;
        int mid = (lo + hi) >>> 1;
        long cnt = sort(a, tmp, lo, mid) + sort(a, tmp, mid + 1, hi);
        int i = lo, j = mid + 1, k = lo;
        while (i <= mid && j <= hi) {
            if (a[i] <= a[j]) tmp[k++] = a[i++];
            else { cnt += mid - i + 1; tmp[k++] = a[j++]; }   // a[i..mid] are all > a[j]
        }
        while (i <= mid) tmp[k++] = a[i++];
        while (j <= hi) tmp[k++] = a[j++];
        System.arraycopy(tmp, lo, a, lo, hi - lo + 1);
        return cnt;
    }
}

/*@LC 2407
tag: LIS with a segment tree over values
cue: Longest strictly increasing subsequence where neighbours differ by at most k.
hook: Best chain ending at value x = 1 + max over values in [x − k, x − 1]; a max segment tree answers that.
time: O(n log V) time · O(V) space
trap: Query BEFORE updating x, and the range is half-open in this tree: `[x − k, x)`.
@*/
class LC2407 {
    public int lengthOfLIS(int[] nums, int k) {
        int size = 100_001;                                      // values are 1..10^5
        int[] t = new int[2 * size];
        int best = 0;
        for (int x : nums) {
            int res = 0;
            for (int l = Math.max(0, x - k) + size, r = x + size; l < r; l >>= 1, r >>= 1) {   //* max on [x-k, x)
                if ((l & 1) == 1) res = Math.max(res, t[l++]);
                if ((r & 1) == 1) res = Math.max(res, t[--r]);
            }
            int cur = res + 1;                                   //* extend the best chain
            best = Math.max(best, cur);
            int p = x + size;
            if (cur > t[p]) {
                for (t[p] = cur; p > 1; p >>= 1) t[p >> 1] = Math.max(t[p], t[p ^ 1]);   //* point update
            }
        }
        return best;
    }
}

/*@LC 2286
class: BookMyShow
tag: Segment tree (max + sum) with descent
cue: n rows of m seats; gather k together in the first row ≤ maxRow that fits, or scatter k anywhere in rows ≤ maxRow.
hook: Max finds the first row with room by walking down the tree; sum answers "enough seats in total?".
time: O(log n) per gather, amortised O(log n) per scatter
trap: Scatter fills from the first non-full row; keep a pointer to it so full rows are never visited again.
@*/
class LC2286 {
    private final int n, m;
    private final long[] sum;
    private final int[] max, free;
    private int firstFree = 0;

    public LC2286(int n, int m) {
        this.n = n;
        this.m = m;
        sum = new long[4 * n];
        max = new int[4 * n];
        free = new int[n];
        Arrays.fill(free, m);
        build(1, 0, n - 1);
    }

    public int[] gather(int k, int maxRow) {
        int row = firstRow(1, 0, n - 1, k, maxRow);                    //*
        if (row == -1) return new int[0];
        int seat = m - free[row];
        set(1, 0, n - 1, row, free[row] - k);
        return new int[]{row, seat};
    }

    public boolean scatter(int k, int maxRow) {
        if (total(1, 0, n - 1, maxRow) < k) return false;              //*
        while (k > 0) {
            int take = Math.min(free[firstFree], k);
            set(1, 0, n - 1, firstFree, free[firstFree] - take);
            k -= take;
            if (free[firstFree] == 0) firstFree++;                     //* never look back
        }
        return true;
    }

    private void build(int node, int lo, int hi) {
        if (lo == hi) { sum[node] = m; max[node] = m; return; }
        int mid = (lo + hi) / 2;
        build(2 * node, lo, mid);
        build(2 * node + 1, mid + 1, hi);
        sum[node] = sum[2 * node] + sum[2 * node + 1];
        max[node] = Math.max(max[2 * node], max[2 * node + 1]);
    }

    private void set(int node, int lo, int hi, int pos, int val) {
        if (lo == hi) { free[pos] = val; sum[node] = val; max[node] = val; return; }
        int mid = (lo + hi) / 2;
        if (pos <= mid) set(2 * node, lo, mid, pos, val);
        else set(2 * node + 1, mid + 1, hi, pos, val);
        sum[node] = sum[2 * node] + sum[2 * node + 1];
        max[node] = Math.max(max[2 * node], max[2 * node + 1]);
    }

    private int firstRow(int node, int lo, int hi, int k, int maxRow) {   // first row <= maxRow with >= k free
        if (lo > maxRow || max[node] < k) return -1;                     //* prune
        if (lo == hi) return lo;
        int mid = (lo + hi) / 2;
        int r = firstRow(2 * node, lo, mid, k, maxRow);                  //* try the left first
        return r != -1 ? r : firstRow(2 * node + 1, mid + 1, hi, k, maxRow);
    }

    private long total(int node, int lo, int hi, int maxRow) {           // free seats in rows 0..maxRow
        if (lo > maxRow) return 0;
        if (hi <= maxRow) return sum[node];
        int mid = (lo + hi) / 2;
        return total(2 * node, lo, mid, maxRow) + total(2 * node + 1, mid + 1, hi, maxRow);
    }
}

/*@LC 3165
tag: Segment tree with 4 states per node
cue: Point updates, and after each one the best sum of non-adjacent elements (sum of answers mod 10⁹+7).
hook: Each node keeps the best for "may I use my first element / my last element"; joining needs one side of the seam to be unused.
time: O((n + q) log n) time · O(n) space
trap: An empty choice is allowed, so a negative leaf is worth 0, and sums need `long`.
@*/
class LC3165 {
    private long[][] t;                                   // t[node][f * 2 + l]: f / l = first / last element allowed

    public int maximumSumSubsequence(int[] nums, int[][] queries) {
        int n = nums.length;
        t = new long[4 * n][4];
        build(1, 0, n - 1, nums);
        long total = 0;
        for (int[] q : queries) {
            update(1, 0, n - 1, q[0], q[1]);
            total = (total + t[1][3]) % 1_000_000_007L;   //* root, both ends allowed
        }
        return (int) total;
    }

    private void leaf(int node, int x) {
        t[node][0] = t[node][1] = t[node][2] = 0;
        t[node][3] = Math.max(0, x);                      //* only "both allowed" may take it
    }

    private void pull(int node) {
        long[] L = t[2 * node], R = t[2 * node + 1];
        for (int f = 0; f < 2; f++)
            for (int l = 0; l < 2; l++)
                t[node][f * 2 + l] = Math.max(L[f * 2] + R[2 + l], L[f * 2 + 1] + R[l]);   //* seam: one side unused
    }

    private void build(int node, int lo, int hi, int[] a) {
        if (lo == hi) { leaf(node, a[lo]); return; }
        int mid = (lo + hi) / 2;
        build(2 * node, lo, mid, a);
        build(2 * node + 1, mid + 1, hi, a);
        pull(node);
    }

    private void update(int node, int lo, int hi, int pos, int x) {
        if (lo == hi) { leaf(node, x); return; }
        int mid = (lo + hi) / 2;
        if (pos <= mid) update(2 * node, lo, mid, pos, x);
        else update(2 * node + 1, mid + 1, hi, pos, x);
        pull(node);
    }
}

/*@LC 2322
tag: Euler tour · subtree inside subtree
cue: Remove two edges from a tree; minimise (largest XOR − smallest XOR) of the three parts.
hook: An edge = the subtree under it. With entry/exit times you can tell if one subtree holds the other.
time: O(n²) time · O(n) space
trap: If v's subtree is inside u's, the three parts are `x[v]`, `x[u] ^ x[v]` and `total ^ x[u]`.
@*/
class LC2322 {
    private List<List<Integer>> g;
    private int[] xr, tin, tout, nums;
    private int timer;

    public int minimumScore(int[] nums, int[][] edges) {
        int n = nums.length;
        this.nums = nums;
        g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) { g.get(e[0]).add(e[1]); g.get(e[1]).add(e[0]); }
        xr = new int[n];
        tin = new int[n];
        tout = new int[n];
        timer = 0;
        dfs(0, -1);
        int best = Integer.MAX_VALUE;
        for (int u = 1; u < n; u++)                               // node u = the edge above u
            for (int v = u + 1; v < n; v++) {
                int a, b, c;
                if (inside(v, u)) { a = xr[v]; b = xr[u] ^ xr[v]; c = xr[0] ^ xr[u]; }        //* v under u
                else if (inside(u, v)) { a = xr[u]; b = xr[v] ^ xr[u]; c = xr[0] ^ xr[v]; }   //* u under v
                else { a = xr[u]; b = xr[v]; c = xr[0] ^ xr[u] ^ xr[v]; }                     //* separate
                best = Math.min(best, Math.max(a, Math.max(b, c)) - Math.min(a, Math.min(b, c)));
            }
        return best;
    }

    private boolean inside(int child, int anc) { return tin[anc] <= tin[child] && tout[child] <= tout[anc]; }   //*

    private void dfs(int v, int p) {
        tin[v] = timer++;
        xr[v] = nums[v];
        for (int w : g.get(v)) if (w != p) { dfs(w, v); xr[v] ^= xr[w]; }
        tout[v] = timer++;
    }
}

public class Day20 {
    public static void main(String[] args) {
        Core20.Fenwick fw = new Core20.Fenwick(5);
        for (int i = 0; i < 5; i++) fw.add(i, i + 1);
        T.eq("core fw", fw.range(1, 3), 9L);
        Core20.MaxSegTree st = new Core20.MaxSegTree(5);
        st.set(1, 7); st.set(3, 4);
        T.eq("core seg", new int[]{st.max(0, 5), st.max(2, 5), st.max(4, 5)}, T.ints("[7,4,0]"));

        LC307 na = new LC307(T.ints("[1,3,5]"));
        int a1 = na.sumRange(0, 2);
        na.update(1, 2);
        T.eq("307", new int[]{a1, na.sumRange(0, 2)}, T.ints("[9,8]"));
        for (int t = 0; t < 50; t++) {
            int n = T.rnd(1, 10);
            int[] a = T.rand(n, -5, 5);
            LC307 f = new LC307(a);
            LC307v1 s = new LC307v1(a);
            int[] b = a.clone();
            for (int op = 0; op < 20; op++) {
                if (T.rnd(0, 1) == 0) { int i = T.rnd(0, n - 1), v = T.rnd(-5, 5); f.update(i, v); s.update(i, v); b[i] = v; }
                else {
                    int l = T.rnd(0, n - 1), r = T.rnd(l, n - 1), want = 0;
                    for (int i = l; i <= r; i++) want += b[i];
                    T.eq("307 rand", f.sumRange(l, r), want);
                    T.eq("307v rand", s.sumRange(l, r), want);
                }
            }
        }

        T.eq("315a", new LC315().countSmaller(T.ints("[5,2,6,1]")), List.of(2, 1, 1, 0));
        T.eq("315b", new LC315().countSmaller(T.ints("[-1]")), List.of(0));
        T.eq("315c", new LC315().countSmaller(T.ints("[-1,-1]")), List.of(0, 0));
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 10), -4, 4);
            List<Integer> want = new ArrayList<>();
            long inv = 0;
            for (int i = 0; i < a.length; i++) { int c = 0; for (int j = i + 1; j < a.length; j++) if (a[j] < a[i]) c++; want.add(c); inv += c; }
            T.eq("315 rand", new LC315().countSmaller(a), want);
            T.eq("inv rand", LC315v1.inversions(a), inv);
        }

        T.eq("2407a", new LC2407().lengthOfLIS(T.ints("[4,2,1,4,3,4,5,8,15]"), 3), 5);
        T.eq("2407b", new LC2407().lengthOfLIS(T.ints("[7,4,5,1,8,12,4,7]"), 5), 4);
        T.eq("2407c", new LC2407().lengthOfLIS(T.ints("[1,5]"), 1), 1);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 10), 1, 12);
            int k = T.rnd(1, 4), best = 0;
            int[] dp = new int[a.length];
            for (int i = 0; i < a.length; i++) { dp[i] = 1; for (int j = 0; j < i; j++) if (a[j] < a[i] && a[i] - a[j] <= k) dp[i] = Math.max(dp[i], dp[j] + 1); best = Math.max(best, dp[i]); }
            T.eq("2407 rand", new LC2407().lengthOfLIS(a, k), best);
        }

        LC2286 bms = new LC2286(2, 5);
        T.eq("2286a", bms.gather(4, 0), T.ints("[0,0]"));
        T.eq("2286b", bms.gather(2, 0), new int[0]);
        T.eq("2286c", bms.scatter(5, 1), true);
        T.eq("2286d", bms.scatter(5, 1), false);
        for (int t = 0; t < 60; t++) {
            int n = T.rnd(1, 5), m = T.rnd(1, 6);
            LC2286 b = new LC2286(n, m);
            int[] used = new int[n];
            for (int op = 0; op < 25; op++) {
                int k = T.rnd(1, m + 1), maxRow = T.rnd(0, n - 1);
                if (T.rnd(0, 1) == 0) {
                    int[] want = new int[0];
                    for (int r = 0; r <= maxRow; r++) if (m - used[r] >= k) { want = new int[]{r, used[r]}; used[r] += k; break; }
                    T.eq("2286 gather", b.gather(k, maxRow), want);
                } else {
                    int freeSum = 0;
                    for (int r = 0; r <= maxRow; r++) freeSum += m - used[r];
                    boolean ok = freeSum >= k;
                    T.eq("2286 scatter", b.scatter(k, maxRow), ok);
                    if (ok) for (int r = 0, left = k; r <= maxRow && left > 0; r++) { int tk = Math.min(left, m - used[r]); used[r] += tk; left -= tk; }
                }
            }
        }

        T.eq("3165a", new LC3165().maximumSumSubsequence(T.ints("[3,5,9]"), T.grid("[[1,-2],[0,-3]]")), 21);
        T.eq("3165b", new LC3165().maximumSumSubsequence(T.ints("[0,-1]"), T.grid("[[0,-5]]")), 0);
        for (int t = 0; t < 100; t++) {
            int n = T.rnd(1, 8);
            int[] a = T.rand(n, -5, 9);
            int[][] qs = new int[T.rnd(1, 6)][];
            long want = 0;
            int[] b = a.clone();
            for (int i = 0; i < qs.length; i++) {
                qs[i] = new int[]{T.rnd(0, n - 1), T.rnd(-5, 9)};
                b[qs[i][0]] = qs[i][1];
                long take = 0, skip = 0;
                for (int x : b) { long nt = skip + x; skip = Math.max(skip, take); take = nt; }
                want += Math.max(0, Math.max(take, skip));
            }
            T.eq("3165 rand", new LC3165().maximumSumSubsequence(a, qs), (int) (want % 1_000_000_007L));
        }

        T.eq("2322a", new LC2322().minimumScore(T.ints("[1,5,5,4,11]"), T.grid("[[0,1],[1,2],[1,3],[3,4]]")), 9);
        T.eq("2322b", new LC2322().minimumScore(T.ints("[5,5,2,4,4,2]"), T.grid("[[0,1],[1,2],[5,2],[4,3],[1,3]]")), 0);
        for (int t = 0; t < 100; t++) {
            int n = T.rnd(3, 9);
            int[] v = T.rand(n, 1, 20);
            int[][] e = T.randTreeEdges(n);
            int best = Integer.MAX_VALUE;
            for (int i = 0; i < e.length; i++) for (int j = i + 1; j < e.length; j++) {
                int[] comp = new int[n];
                Arrays.fill(comp, -1);
                int c = 0;
                List<Integer> xs = new ArrayList<>();
                for (int s = 0; s < n; s++) {
                    if (comp[s] != -1) continue;
                    int x = 0;
                    Deque<Integer> q = new ArrayDeque<>();
                    q.add(s); comp[s] = c;
                    while (!q.isEmpty()) {
                        int u = q.poll(); x ^= v[u];
                        for (int k = 0; k < e.length; k++) {
                            if (k == i || k == j) continue;
                            int w = e[k][0] == u ? e[k][1] : e[k][1] == u ? e[k][0] : -1;
                            if (w != -1 && comp[w] == -1) { comp[w] = c; q.add(w); }
                        }
                    }
                    xs.add(x); c++;
                }
                best = Math.min(best, Collections.max(xs) - Collections.min(xs));
            }
            T.eq("2322 rand", new LC2322().minimumScore(v, e), best);
        }
        T.summary("Day20");
    }
}
