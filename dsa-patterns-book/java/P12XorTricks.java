import java.util.*;

public class P12XorTricks {
    //@tpl cancel
    // Every value appears twice except one: the pairs cancel (a ^ a == 0).
    static int singleNumber(int[] a) {
        int x = 0;
        for (int v : a) x ^= v;
        return x;
    }

    // a holds n distinct values from 0..n: XOR every index and every value.
    static int missingNumber(int[] a) {
        int x = a.length;
        for (int i = 0; i < a.length; i++) x ^= i ^ a[i];
        return x;
    }

    // Two values x != y appear once, all others twice.
    static int[] singleNumberIII(int[] a) {
        int xy = 0;
        for (int v : a) xy ^= v;                 // = x ^ y, not 0
        int bit = xy & -xy;                      // a bit where x and y differ
        int x = 0;
        for (int v : a) if ((v & bit) != 0) x ^= v;   // only x's group
        return new int[] {x, xy ^ x};
    }
    //@end

    //@tpl mod3
    // Every value appears three times except one: count each bit mod 3.
    static int singleNumberII(int[] a) {
        int ans = 0;
        for (int i = 0; i < 32; i++) {
            int ones = 0;
            for (int v : a) ones += (v >> i) & 1;
            if (ones % 3 != 0) ans |= 1 << i;    // i = 31 sets the sign bit
        }
        return ans;
    }
    //@end

    //@tpl prefix
    // p[i] = a[0] ^ ... ^ a[i-1]; the XOR of a[l..r] is p[r + 1] ^ p[l].
    static int[] xorQueries(int[] a, int[][] queries) {
        int[] p = new int[a.length + 1];
        for (int i = 0; i < a.length; i++) p[i + 1] = p[i] ^ a[i];
        int[] ans = new int[queries.length];
        for (int q = 0; q < queries.length; q++)
            ans[q] = p[queries[q][1] + 1] ^ p[queries[q][0]];
        return ans;
    }

    // encoded[i] = arr[i] ^ arr[i + 1], so arr[i + 1] = encoded[i] ^ arr[i].
    static int[] decode(int[] encoded, int first) {
        int[] arr = new int[encoded.length + 1];
        arr[0] = first;
        for (int i = 0; i < encoded.length; i++) arr[i + 1] = encoded[i] ^ arr[i];
        return arr;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int[] shuffled(Random r, List<Integer> vals) {
        Collections.shuffle(vals, r);
        int[] a = new int[vals.size()];
        for (int i = 0; i < a.length; i++) a[i] = vals.get(i);
        return a;
    }

    static int randVal(Random r) {
        int k = r.nextInt(3);
        return k == 0 ? r.nextInt(10) - 5 : k == 1 ? r.nextInt() : r.nextInt(1000);
    }

    // build: `times` copies of distinct values plus the given singles
    static int[] build(Random r, int distinct, int times, int[] singles) {
        Set<Integer> used = new HashSet<>();
        for (int s : singles) used.add(s);
        List<Integer> vals = new ArrayList<>();
        for (int s : singles) vals.add(s);
        while (used.size() < singles.length + distinct) {
            int v = randVal(r);
            if (used.add(v)) for (int k = 0; k < times; k++) vals.add(v);
        }
        return shuffled(r, vals);
    }

    public static void main(String[] args) {
        Random r = new Random(1212);
        for (int t = 0; t < 3000; t++) {
            int d = r.nextInt(8);
            int x = randVal(r);
            Check.eq(x, singleNumber(build(r, d, 2, new int[] {x})), "single");
            Check.eq(x, singleNumberII(build(r, d, 3, new int[] {x})), "single II");
            int y = randVal(r);
            while (y == x) y = randVal(r);
            int[] got = singleNumberIII(build(r, d, 2, new int[] {x, y}));
            Arrays.sort(got);
            int[] exp = {Math.min(x, y), Math.max(x, y)};
            Check.eq(exp, got, "single III " + x + " " + y);
            // missing number
            int n = 1 + r.nextInt(12);
            int miss = r.nextInt(n + 1);
            List<Integer> vals = new ArrayList<>();
            for (int v = 0; v <= n; v++) if (v != miss) vals.add(v);
            Check.eq(miss, missingNumber(shuffled(r, vals)), "missing");
            // prefix xor queries against a direct loop
            int[] a = Check.ints(r, 1 + r.nextInt(10), 0, 1000);
            int[][] qs = new int[5][];
            int[] want = new int[5];
            for (int q = 0; q < 5; q++) {
                int lo = r.nextInt(a.length), hi = lo + r.nextInt(a.length - lo);
                qs[q] = new int[] {lo, hi};
                for (int i = lo; i <= hi; i++) want[q] ^= a[i];
            }
            Check.eq(want, xorQueries(a, qs), "xorQueries");
            int[] enc = new int[a.length - 1];
            for (int i = 0; i + 1 < a.length; i++) enc[i] = a[i] ^ a[i + 1];
            Check.eq(a, decode(enc, a[0]), "decode");
        }
        Check.eq(Integer.MIN_VALUE, singleNumberII(new int[] {Integer.MIN_VALUE, 5, 5, 5}), "sign bit");
        int[] mm = singleNumberIII(new int[] {Integer.MIN_VALUE, 0, 7, 7});
        Arrays.sort(mm);
        Check.eq(new int[] {Integer.MIN_VALUE, 0}, mm, "single III with MIN and 0");
        // traps quoted in the book
        Check.eq(1, 2 ^ 2 ^ 3 ^ 2, "xor with triples");
        int ansNoSign = 0;
        int[] neg = {-4, 7, 7, 7};
        for (int i = 0; i < 31; i++) {
            int ones = 0;
            for (int v : neg) ones += (v >> i) & 1;
            if (ones % 3 != 0) ansNoSign |= 1 << i;
        }
        Check.eq(2147483644, ansNoSign, "i < 31 loses the sign");
        // LeetCode samples
        Check.eq(1, singleNumber(new int[] {2, 2, 1}), "LC 136 sample 1");
        Check.eq(4, singleNumber(new int[] {4, 1, 2, 1, 2}), "LC 136 sample 2");
        Check.eq(2, missingNumber(new int[] {3, 0, 1}), "LC 268 sample 1");
        Check.eq(8, missingNumber(new int[] {9, 6, 4, 2, 3, 5, 7, 0, 1}), "LC 268 sample 3");
        int[] s3 = singleNumberIII(new int[] {1, 2, 1, 3, 2, 5});
        Arrays.sort(s3);
        Check.eq(new int[] {3, 5}, s3, "LC 260 sample 1");
        Check.eq(3, singleNumberII(new int[] {2, 2, 3, 2}), "LC 137 sample 1");
        Check.eq(99, singleNumberII(new int[] {0, 1, 0, 1, 0, 1, 99}), "LC 137 sample 2");
        Check.eq(new int[] {2, 7, 14, 8}, xorQueries(new int[] {1, 3, 4, 8},
                new int[][] {{0, 1}, {1, 2}, {0, 3}, {3, 3}}), "LC 1310 sample 1");
        Check.eq(new int[] {1, 0, 2, 1}, decode(new int[] {1, 2, 3}, 1), "LC 1720 sample 1");
        Check.done("P12XorTricks");
    }
}
