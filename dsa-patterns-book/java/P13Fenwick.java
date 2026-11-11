import java.util.*;

public class P13Fenwick {
    //@tpl bit
    // Fenwick tree, 1-indexed: t[i] holds a(i - lowbit(i) .. i], where
    // lowbit(i) = i & -i is the lowest set bit of i.
    static class Fenwick {
        final long[] t;
        Fenwick(int n) { t = new long[n + 1]; }

        void add(int i, long delta) {              // a[i] += delta, 1 <= i <= n
            for (; i < t.length; i += i & -i) t[i] += delta;
        }

        long sum(int i) {                          // a[1] + ... + a[i]
            long s = 0;
            for (; i > 0; i -= i & -i) s += t[i];
            return s;
        }

        long sum(int l, int r) { return sum(r) - sum(l - 1); }    // a[l..r]
    }
    //@end

    //@tpl uses
    // LC 307: LeetCode indexes from 0, the tree from 1. update() sets a value,
    // so add the difference and remember the new value.
    static class NumArray {
        final int[] a;
        final Fenwick f;
        NumArray(int[] nums) {
            a = nums.clone();
            f = new Fenwick(a.length);
            for (int i = 0; i < a.length; i++) f.add(i + 1, a[i]);
        }
        void update(int i, int val) { f.add(i + 1, val - a[i]); a[i] = val; }
        int sumRange(int l, int r) { return (int) f.sum(l + 1, r + 1); }
    }

    // Range add, point query: keep d[i] = a[i] - a[i-1] in the tree, so a[i]
    // is the prefix sum d[1] + ... + d[i]. Adding v to a[l..r] changes two d's.
    static void rangeAdd(Fenwick f, int l, int r, long v) {
        f.add(l, v);
        f.add(r + 1, -v);                  // r + 1 = n + 1 is skipped by add
    }
    static long pointQuery(Fenwick f, int i) { return f.sum(i); }
    //@end

    //@tpl count
    // LC 315: walk from the right; the tree counts the values seen so far by
    // rank. ranks() is the compression of [coord-compress], shifted to 1..m.
    static List<Integer> countSmaller(int[] nums) {
        int[] rank = ranks(nums);
        Fenwick f = new Fenwick(nums.length);
        Integer[] out = new Integer[nums.length];
        for (int i = nums.length - 1; i >= 0; i--) {
            out[i] = (int) f.sum(rank[i] - 1);     // strictly smaller, to the right
            f.add(rank[i], 1);
        }
        return Arrays.asList(out);
    }

    // LC 1649: values are 1..1e5, so they index the tree directly.
    static int createSortedArray(int[] instructions) {
        Fenwick f = new Fenwick(100_000);
        long cost = 0;
        for (int i = 0; i < instructions.length; i++) {
            int v = instructions[i];
            long less = f.sum(v - 1), greater = i - f.sum(v);  // i values inserted
            cost += Math.min(less, greater);
            f.add(v, 1);
        }
        return (int) (cost % 1_000_000_007);
    }
    //@end

    // Ranks 1..m with equal values sharing a rank (sort, unique, binary search).
    static int[] ranks(int[] a) {
        int[] u = a.clone();
        Arrays.sort(u);
        int m = 0;
        for (int i = 0; i < u.length; i++) if (i == 0 || u[i] != u[i - 1]) u[m++] = u[i];
        int[] r = new int[a.length];
        for (int i = 0; i < a.length; i++) r[i] = Arrays.binarySearch(u, 0, m, a[i]) + 1;
        return r;
    }

    // ---------------- tests (not printed) ----------------
    // LC 1409 (practice): positions of a permutation in a Fenwick tree
    static int[] processQueries(int[] queries, int m) {
        int n = queries.length;
        Fenwick f = new Fenwick(n + m);
        int[] pos = new int[m + 1];
        for (int v = 1; v <= m; v++) { pos[v] = n + v; f.add(n + v, 1); }
        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            int v = queries[i];
            out[i] = (int) f.sum(pos[v] - 1);
            f.add(pos[v], -1);
            pos[v] = n - i;
            f.add(pos[v], 1);
        }
        return out;
    }

    // O(n) build (variant)
    static long[] buildLinear(long[] a) {          // a[1..n]
        int n = a.length - 1;
        long[] t = a.clone();
        for (int i = 1; i <= n; i++) {
            int j = i + (i & -i);
            if (j <= n) t[j] += t[i];
        }
        return t;
    }

    // k-th smallest by walking powers of two (variant); counts >= 0, 1 <= k <= total
    static int kth(Fenwick f, long k) {
        int n = f.t.length - 1, pos = 0;
        for (int step = Integer.highestOneBit(Math.max(1, n)); step > 0; step >>= 1)
            if (pos + step <= n && f.t[pos + step] < k) { pos += step; k -= f.t[pos]; }
        return pos + 1;
    }

    // LC 2179 (variant): positions in nums2, then smaller-before and larger-after
    static long goodTriplets(int[] nums1, int[] nums2) {
        int n = nums1.length;
        int[] pos = new int[n];
        for (int i = 0; i < n; i++) pos[nums2[i]] = i;
        int[] p = new int[n];
        for (int i = 0; i < n; i++) p[i] = pos[nums1[i]] + 1;
        Fenwick f = new Fenwick(n);
        long total = 0;
        for (int i = 0; i < n; i++) {
            long before = f.sum(p[i] - 1);
            long after = (n - p[i]) - (i - before);
            total += before * after;
            f.add(p[i], 1);
        }
        return total;
    }

    // trap versions of LC 307
    static class AddValue {
        final Fenwick f;
        AddValue(int[] nums) { f = new Fenwick(nums.length); for (int i = 0; i < nums.length; i++) f.add(i + 1, nums[i]); }
        void update(int i, int val) { f.add(i + 1, val); }
        int sumRange(int l, int r) { return (int) f.sum(l + 1, r + 1); }
    }

    static class NoRemember {
        final int[] a;
        final Fenwick f;
        NoRemember(int[] nums) { a = nums.clone(); f = new Fenwick(a.length); for (int i = 0; i < a.length; i++) f.add(i + 1, a[i]); }
        void update(int i, int val) { f.add(i + 1, val - a[i]); }
        int sumRange(int l, int r) { return (int) f.sum(l + 1, r + 1); }
    }

    public static void main(String[] args) {
        Random r = new Random(1302);
        for (int t = 0; t < 1500; t++) {
            int n = 1 + r.nextInt(30);
            long[] a = new long[n + 1];
            Fenwick f = new Fenwick(n), d = new Fenwick(n);
            long[] b = new long[n + 2];
            for (int op = 0; op < 60; op++) {
                int l = 1 + r.nextInt(n), rr = l + r.nextInt(n - l + 1);
                long v = r.nextInt(2000000001) - 1000000000L;
                switch (r.nextInt(4)) {
                    case 0: f.add(l, v); a[l] += v; break;
                    case 1: {
                        long want = 0;
                        for (int i = l; i <= rr; i++) want += a[i];
                        Check.eq(want, f.sum(l, rr), "range sum");
                        break;
                    }
                    case 2: rangeAdd(d, l, rr, v); for (int i = l; i <= rr; i++) b[i] += v; break;
                    default: Check.eq(b[l], pointQuery(d, l), "point query");
                }
            }
            // NumArray against a plain array
            int[] nums = Check.ints(r, n, -100, 100);
            NumArray na = new NumArray(nums);
            int[] plain = nums.clone();
            for (int op = 0; op < 40; op++) {
                int i = r.nextInt(n), j = i + r.nextInt(n - i);
                if (r.nextBoolean()) { int v = r.nextInt(201) - 100; na.update(i, v); plain[i] = v; }
                else {
                    int want = 0;
                    for (int k = i; k <= j; k++) want += plain[k];
                    Check.eq(want, na.sumRange(i, j), "NumArray");
                }
            }
            // count smaller
            int[] c = r.nextBoolean() ? Check.ints(r, n, -3, 3) : Check.ints(r, n, -10000, 10000);
            List<Integer> want = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                int cnt = 0;
                for (int j = i + 1; j < n; j++) if (c[j] < c[i]) cnt++;
                want.add(cnt);
            }
            Check.eq(want, countSmaller(c), "LC 315 " + Arrays.toString(c));
            // create sorted array
            int[] ins = Check.ints(r, n, 1, r.nextBoolean() ? 5 : 100000);
            long cost = 0;
            for (int i = 0; i < n; i++) {
                int less = 0, greater = 0;
                for (int j = 0; j < i; j++) { if (ins[j] < ins[i]) less++; if (ins[j] > ins[i]) greater++; }
                cost += Math.min(less, greater);
            }
            Check.eq(cost % 1000000007, createSortedArray(ins), "LC 1649 " + Arrays.toString(ins));
            // LC 1409
            int m = 1 + r.nextInt(10);
            int[] qs = Check.ints(r, 1 + r.nextInt(10), 1, m);
            List<Integer> p = new ArrayList<>();
            for (int v = 1; v <= m; v++) p.add(v);
            int[] wq = new int[qs.length];
            for (int i = 0; i < qs.length; i++) {
                int idx = p.indexOf(qs[i]);
                wq[i] = idx;
                p.remove(idx);
                p.add(0, qs[i]);
            }
            Check.eq(wq, processQueries(qs, m), "LC 1409");
            long[] arr = new long[n + 1];
            Fenwick fb = new Fenwick(n);
            for (int i = 1; i <= n; i++) { arr[i] = r.nextInt(5); fb.add(i, arr[i]); }
            Check.eq(fb.t, buildLinear(arr), "O(n) build");
            long tot = fb.sum(n);
            for (long kk = 1; kk <= tot; kk++) {
                int want0 = 1;
                long acc = 0;
                for (int i = 1; i <= n; i++) { acc += arr[i]; if (acc >= kk) { want0 = i; break; } }
                Check.eq(want0, kth(fb, kk), "kth");
            }
            int[] p1 = new int[n], p2 = new int[n];
            List<Integer> perm = new ArrayList<>();
            for (int i = 0; i < n; i++) perm.add(i);
            Collections.shuffle(perm, r);
            for (int i = 0; i < n; i++) p1[i] = perm.get(i);
            Collections.shuffle(perm, r);
            for (int i = 0; i < n; i++) p2[i] = perm.get(i);
            int[] q2 = new int[n];
            for (int i = 0; i < n; i++) q2[p2[i]] = i;
            long gt = 0;
            for (int x = 0; x < n; x++) for (int y = x + 1; y < n; y++) for (int z = y + 1; z < n; z++)
                if (q2[p1[x]] < q2[p1[y]] && q2[p1[y]] < q2[p1[z]]) gt++;
            Check.eq(gt, goodTriplets(p1, p2), "LC 2179");
        }
        // big: n = 1e5 operations, sums need long
        Fenwick big = new Fenwick(100000);
        for (int i = 1; i <= 100000; i++) big.add(i, 1000000000);
        Check.eq(100000L * 1000000000, big.sum(100000), "big sum, long");
        int[] desc = new int[100000];
        for (int i = 0; i < desc.length; i++) desc[i] = 100000 - i;
        Check.eq(99999, (int) countSmaller(desc).get(0), "big LC 315");
        // LeetCode samples
        NumArray s1 = new NumArray(new int[] {1, 3, 5});
        Check.eq(9, s1.sumRange(0, 2), "LC 307 sample sumRange");
        s1.update(1, 2);
        Check.eq(8, s1.sumRange(0, 2), "LC 307 sample after update");
        Check.eq(Arrays.asList(2, 1, 1, 0), countSmaller(new int[] {5, 2, 6, 1}), "LC 315 sample 1");
        Check.eq(Arrays.asList(0), countSmaller(new int[] {-1}), "LC 315 sample 2");
        Check.eq(Arrays.asList(0, 0), countSmaller(new int[] {-1, -1}), "LC 315 sample 3");
        Check.eq(1, createSortedArray(new int[] {1, 5, 6, 2}), "LC 1649 sample 1");
        Check.eq(3, createSortedArray(new int[] {1, 2, 3, 6, 5, 4}), "LC 1649 sample 2");
        Check.eq(4, createSortedArray(new int[] {1, 3, 3, 3, 2, 4, 2, 1, 2}), "LC 1649 sample 3");
        Check.eq(new int[] {2, 1, 2, 1}, processQueries(new int[] {3, 1, 2, 1}, 5), "LC 1409 sample 1");
        Check.eq(1, goodTriplets(new int[] {2, 0, 1, 3}, new int[] {0, 1, 2, 3}), "LC 2179 sample 1");
        Check.eq(4, goodTriplets(new int[] {4, 0, 1, 3, 2}, new int[] {4, 1, 0, 2, 3}), "LC 2179 sample 2");
        // trace numbers
        Fenwick tr = new Fenwick(8);
        int[] ta = {3, 1, 4, 1, 5, 9, 2, 6};
        for (int i = 0; i < 8; i++) tr.add(i + 1, ta[i]);
        Check.eq(new long[] {0, 3, 4, 4, 9, 5, 14, 2, 31}, tr.t, "trace t");
        Check.eq(25, tr.sum(7), "trace sum(7)");
        // traps
        AddValue av = new AddValue(new int[] {1, 3, 5});
        av.update(1, 2);
        Check.eq(11, av.sumRange(0, 2), "update adds the value");
        NoRemember nr = new NoRemember(new int[] {1, 3, 5});
        nr.update(1, 2);
        nr.update(1, 2);
        Check.eq(7, nr.sumRange(0, 2), "update forgets a[i]");
        Fenwick wrong = new Fenwick(3);
        wrong.add(1, 5);
        wrong.add(2, -5);               // -v at r instead of r + 1
        Check.eq(0, pointQuery(wrong, 2), "range add ends one early");
        Check.isTrue((0 & -0) == 0, "add(0) never moves");
        Check.done("P13Fenwick");
    }
}
