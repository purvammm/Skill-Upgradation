import java.util.*;

public class P13LazySegTree {
    //@tpl lazy
    // Range add, range sum. sm[x] is the true sum of x's range; lz[x] is an
    // add that x's children have not received yet.
    static class LazySegTree {
        final int n;
        final long[] sm, lz;
        LazySegTree(int n) { this.n = n; sm = new long[4 * n]; lz = new long[4 * n]; }

        void apply(int x, int lo, int hi, long v) {      // add v to all of [lo, hi]
            sm[x] += v * (hi - lo + 1);
            lz[x] += v;
        }

        void push(int x, int lo, int hi) {               // hand the tag down
            if (lz[x] == 0) return;
            int mid = (lo + hi) / 2;
            apply(2 * x, lo, mid, lz[x]);
            apply(2 * x + 1, mid + 1, hi, lz[x]);
            lz[x] = 0;
        }

        void add(int l, int r, long v) { add(1, 0, n - 1, l, r, v); }
        void add(int x, int lo, int hi, int l, int r, long v) {
            if (r < lo || hi < l) return;
            if (l <= lo && hi <= r) { apply(x, lo, hi, v); return; }   // stop here
            push(x, lo, hi);                             // 1. push before going down
            int mid = (lo + hi) / 2;
            add(2 * x, lo, mid, l, r, v);
            add(2 * x + 1, mid + 1, hi, l, r, v);
            sm[x] = sm[2 * x] + sm[2 * x + 1];           // 2. pull after
        }

        long sum(int l, int r) { return sum(1, 0, n - 1, l, r); }
        long sum(int x, int lo, int hi, int l, int r) {
            if (r < lo || hi < l) return 0;
            if (l <= lo && hi <= r) return sm[x];
            push(x, lo, hi);
            int mid = (lo + hi) / 2;
            return sum(2 * x, lo, mid, l, r) + sum(2 * x + 1, mid + 1, hi, l, r);
        }
    }
    //@end

    //@tpl flip
    // LC 2569: flip the bits of nums1[l..r] and count its ones. A flip turns a
    // node's ones into len - ones, and two flips cancel: the tag is a boolean.
    static class FlipTree {
        final int n;
        final int[] ones;
        final boolean[] flip;
        FlipTree(int n) {
            this.n = n;
            ones = new int[4 * n];
            flip = new boolean[4 * n];
        }

        void apply(int x, int lo, int hi) {          // flip all of [lo, hi]
            ones[x] = hi - lo + 1 - ones[x];
            flip[x] = !flip[x];
        }

        void flip(int l, int r) { flip(1, 0, n - 1, l, r); }
        void flip(int x, int lo, int hi, int l, int r) {
            if (r < lo || hi < l) return;
            if (l <= lo && hi <= r) { apply(x, lo, hi); return; }
            int mid = (lo + hi) / 2;
            if (flip[x]) {                           // push
                apply(2 * x, lo, mid);
                apply(2 * x + 1, mid + 1, hi);
                flip[x] = false;
            }
            flip(2 * x, lo, mid, l, r);
            flip(2 * x + 1, mid + 1, hi, l, r);
            ones[x] = ones[2 * x] + ones[2 * x + 1]; // pull
        }
    }
    //@end

    // LC 2569 (variant): ones[1] is the number of ones in all of nums1
    static long[] handleQuery(int[] nums1, int[] nums2, int[][] queries) {
        int n = nums1.length;
        FlipTree t = new FlipTree(n);
        for (int i = 0; i < n; i++) if (nums1[i] == 1) t.flip(i, i);
        long total = 0;                                  // sum of nums2
        for (int v : nums2) total += v;
        List<Long> out = new ArrayList<>();
        for (int[] q : queries) {
            if (q[0] == 1) t.flip(q[1], q[2]);
            else if (q[0] == 2) total += (long) q[1] * t.ones[1];
            else out.add(total);
        }
        long[] res = new long[out.size()];
        for (int i = 0; i < res.length; i++) res[i] = out.get(i);
        return res;
    }

    // ---------------- tests (not printed) ----------------
    // Falling squares (LC 699, variant): compress edges, range chmax + range max
    static class MaxTree {
        final int n;
        final long[] mx, tag;
        MaxTree(int n) { this.n = n; mx = new long[4 * n]; tag = new long[4 * n]; }
        void apply(int x, long v) { mx[x] = Math.max(mx[x], v); tag[x] = Math.max(tag[x], v); }
        void push(int x) { if (tag[x] != 0) { apply(2 * x, tag[x]); apply(2 * x + 1, tag[x]); tag[x] = 0; } }
        void raise(int x, int lo, int hi, int l, int r, long v) {
            if (r < lo || hi < l) return;
            if (l <= lo && hi <= r) { apply(x, v); return; }
            push(x);
            int mid = (lo + hi) / 2;
            raise(2 * x, lo, mid, l, r, v);
            raise(2 * x + 1, mid + 1, hi, l, r, v);
            mx[x] = Math.max(mx[2 * x], mx[2 * x + 1]);
        }
        long max(int x, int lo, int hi, int l, int r) {
            if (r < lo || hi < l) return 0;
            if (l <= lo && hi <= r) return mx[x];
            push(x);
            int mid = (lo + hi) / 2;
            return Math.max(max(2 * x, lo, mid, l, r), max(2 * x + 1, mid + 1, hi, l, r));
        }
    }

    static List<Integer> fallingSquares(int[][] positions) {
        TreeSet<Integer> pts = new TreeSet<>();
        for (int[] p : positions) { pts.add(p[0]); pts.add(p[0] + p[1]); }
        List<Integer> xs = new ArrayList<>(pts);
        int m = xs.size() - 1;                     // unit cells [xs[i], xs[i+1])
        MaxTree t = new MaxTree(Math.max(1, m));
        List<Integer> out = new ArrayList<>();
        long best = 0;
        for (int[] p : positions) {
            int l = Collections.binarySearch(xs, p[0]), r = Collections.binarySearch(xs, p[0] + p[1]) - 1;
            long h = t.max(1, 0, t.n - 1, l, r) + p[1];
            t.raise(1, 0, t.n - 1, l, r, h);
            best = Math.max(best, h);
            out.add((int) best);
        }
        return out;
    }

    static List<Integer> bruteFalling(int[][] positions) {
        List<int[]> placed = new ArrayList<>();          // l, r (exclusive), height
        List<Integer> out = new ArrayList<>();
        int best = 0;
        for (int[] p : positions) {
            int l = p[0], r = p[0] + p[1], base = 0;
            for (int[] q : placed) if (q[0] < r && l < q[1]) base = Math.max(base, q[2]);
            placed.add(new int[] {l, r, base + p[1]});
            best = Math.max(best, base + p[1]);
            out.add(best);
        }
        return out;
    }

    // the trap: sum without push reads stale children
    static long sumNoPush(LazySegTree t, int x, int lo, int hi, int l, int r) {
        if (r < lo || hi < l) return 0;
        if (l <= lo && hi <= r) return t.sm[x];
        int mid = (lo + hi) / 2;
        return sumNoPush(t, 2 * x, lo, mid, l, r) + sumNoPush(t, 2 * x + 1, mid + 1, hi, l, r);
    }

    public static void main(String[] args) {
        Random r = new Random(1304);
        for (int t = 0; t < 1500; t++) {
            int n = 1 + r.nextInt(30);
            LazySegTree st = new LazySegTree(n);
            long[] a = new long[n];
            for (int op = 0; op < 80; op++) {
                int l = r.nextInt(n), rr = l + r.nextInt(n - l);
                if (r.nextBoolean()) {
                    long v = r.nextInt(2000000001) - 1000000000L;
                    st.add(l, rr, v);
                    for (int i = l; i <= rr; i++) a[i] += v;
                } else {
                    long want = 0;
                    for (int i = l; i <= rr; i++) want += a[i];
                    Check.eq(want, st.sum(l, rr), "range sum");
                }
            }
            // LC 2569 against plain arrays
            int[] n1 = Check.ints(r, n, 0, 1), n2 = Check.ints(r, n, 0, 1000000000);
            int q = 1 + r.nextInt(30);
            int[][] qs = new int[q][];
            for (int i = 0; i < q; i++) {
                int type = 1 + r.nextInt(3);
                int l = r.nextInt(n), rr = l + r.nextInt(n - l);
                qs[i] = type == 1 ? new int[] {1, l, rr} : type == 2 ? new int[] {2, r.nextInt(1000001), 0} : new int[] {3, 0, 0};
            }
            int[] b1 = n1.clone();
            long[] b2 = new long[n];
            for (int i = 0; i < n; i++) b2[i] = n2[i];
            List<Long> want = new ArrayList<>();
            for (int[] qq : qs) {
                if (qq[0] == 1) for (int i = qq[1]; i <= qq[2]; i++) b1[i] ^= 1;
                else if (qq[0] == 2) for (int i = 0; i < n; i++) b2[i] += (long) b1[i] * qq[1];
                else { long s = 0; for (long v : b2) s += v; want.add(s); }
            }
            long[] wa = new long[want.size()];
            for (int i = 0; i < wa.length; i++) wa[i] = want.get(i);
            Check.eq(wa, handleQuery(n1, n2, qs), "LC 2569");
            // falling squares
            int[][] pos = new int[1 + r.nextInt(8)][];
            for (int i = 0; i < pos.length; i++) pos[i] = new int[] {1 + r.nextInt(10), 1 + r.nextInt(5)};
            Check.eq(bruteFalling(pos), fallingSquares(pos), "LC 699");
        }
        // big: 1e5 adds and sums on 1e5 cells, values need long
        LazySegTree big = new LazySegTree(100000);
        for (int i = 0; i < 100000; i++) big.add(i % 50000, 99999 - i % 50000, 1000);
        long bw = 0;
        for (int i = 0; i < 100000; i++) bw += 1000L * (100000 - 2 * (i % 50000));
        Check.eq(bw, big.sum(0, 99999), "big sum, long");
        Check.isTrue(bw > Integer.MAX_VALUE, "big sum passes int");
        // LeetCode samples
        Check.eq(new long[] {3}, handleQuery(new int[] {1, 0, 1}, new int[] {0, 0, 0},
                new int[][] {{1, 1, 1}, {2, 1, 0}, {3, 0, 0}}), "LC 2569 sample 1");
        Check.eq(new long[] {5}, handleQuery(new int[] {1}, new int[] {5},
                new int[][] {{2, 0, 0}, {3, 0, 0}}), "LC 2569 sample 2");
        Check.eq(Arrays.asList(2, 5, 5), fallingSquares(new int[][] {{1, 2}, {2, 3}, {6, 1}}), "LC 699 sample 1");
        Check.eq(Arrays.asList(100, 100), fallingSquares(new int[][] {{100, 100}, {200, 100}}), "LC 699 sample 2");
        // trace and trap: n = 8, add(0, 5, +2), add(2, 3, +3), then sum(3, 3)
        LazySegTree tr = new LazySegTree(8);
        tr.add(0, 5, 2);
        tr.add(2, 3, 3);
        Check.eq(5, tr.sum(3, 3), "trace sum(3, 3)");
        LazySegTree tr2 = new LazySegTree(8);
        tr2.add(0, 5, 2);
        tr2.add(2, 3, 3);
        Check.eq(0, sumNoPush(tr2, 1, 0, 7, 3, 3), "no push in sum");
        LazySegTree noLen = new LazySegTree(4) {
            @Override void apply(int x, int lo, int hi, long v) { sm[x] += v; lz[x] += v; }
        };
        noLen.add(0, 3, 1);
        Check.eq(1, noLen.sum(0, 3), "no length factor");
        Check.done("P13LazySegTree");
    }
}
