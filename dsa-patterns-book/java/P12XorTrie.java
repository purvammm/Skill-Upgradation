import java.util.*;

public class P12XorTrie {
    //@tpl trie
    // Binary trie over bits 30..0 (values 0 <= v < 2^31), for max XOR queries.
    static class XorTrie {
        static final int HIGH = 30;
        final int[][] child;          // child[bit][node]; 0 means none
        int nodes = 1;                // node 0 is the root

        XorTrie(int maxValues) {
            child = new int[2][maxValues * (HIGH + 1) + 1];
        }

        void insert(int v) {
            int cur = 0;
            for (int b = HIGH; b >= 0; b--) {
                int bit = (v >> b) & 1;
                if (child[bit][cur] == 0) child[bit][cur] = nodes++;
                cur = child[bit][cur];
            }
        }

        // max of x ^ v over the inserted v (the trie must not be empty)
        int maxXor(int x) {
            int cur = 0, best = 0;
            for (int b = HIGH; b >= 0; b--) {
                int want = ((x >> b) & 1) ^ 1;          // the opposite bit
                if (child[want][cur] != 0) {
                    best |= 1 << b;                     // bit b of x ^ v is 1
                    cur = child[want][cur];
                } else {
                    cur = child[want ^ 1][cur];
                }
            }
            return best;
        }
    }

    static int findMaximumXOR(int[] a) {
        XorTrie t = new XorTrie(a.length);
        int best = 0;
        for (int v : a) {
            t.insert(v);                                // insert first: never empty
            best = Math.max(best, t.maxXor(v));
        }
        return best;
    }
    //@end

    //@tpl offline
    // queries[i] = {x, m}: max x ^ v over nums v <= m, or -1 if no v <= m.
    // Offline: answer queries by increasing m, inserting nums as they qualify.
    static int[] maximizeXor(int[] nums, int[][] queries) {
        Arrays.sort(nums);
        Integer[] order = new Integer[queries.length];
        for (int i = 0; i < order.length; i++) order[i] = i;
        Arrays.sort(order, (p, q) -> Integer.compare(queries[p][1], queries[q][1]));
        XorTrie t = new XorTrie(nums.length);
        int[] ans = new int[queries.length];
        int j = 0;                                      // nums[0..j) are inserted
        for (int qi : order) {
            while (j < nums.length && nums[j] <= queries[qi][1]) t.insert(nums[j++]);
            ans[qi] = j == 0 ? -1 : t.maxXor(queries[qi][0]);
        }
        return ans;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int brutePairs(int[] a) {
        int best = 0;
        for (int i = 0; i < a.length; i++)
            for (int j = i; j < a.length; j++) best = Math.max(best, a[i] ^ a[j]);
        return best;
    }

    static int randVal(Random r) {
        int k = r.nextInt(3);
        return k == 0 ? r.nextInt(32) : k == 1 ? r.nextInt(Integer.MAX_VALUE) : r.nextInt(1 << 12);
    }

    // trap: bits from low to high
    static int lowToHigh(int[] vals, int x) {
        // a trie keyed from bit 0 upwards, stored as the set of its paths
        Set<String> paths = new HashSet<>();
        for (int v : vals) {
            StringBuilder sb = new StringBuilder();
            for (int b = 0; b <= 30; b++) { sb.append((v >> b) & 1); paths.add(sb.toString()); }
        }
        StringBuilder cur = new StringBuilder();
        int best = 0;
        for (int b = 0; b <= 30; b++) {
            int want = ((x >> b) & 1) ^ 1;
            if (paths.contains(cur.toString() + want)) { best |= 1 << b; cur.append(want); }
            else cur.append(want ^ 1);
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(4212);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(15);
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = randVal(r);
            Check.eq(brutePairs(a), findMaximumXOR(a), "pairs " + Arrays.toString(a));
            // single queries against all inserted values
            XorTrie tr = new XorTrie(n);
            for (int v : a) tr.insert(v);
            for (int q = 0; q < 5; q++) {
                int x = randVal(r), best = 0;
                for (int v : a) best = Math.max(best, x ^ v);
                Check.eq(best, tr.maxXor(x), "maxXor");
            }
            // offline queries
            int m = 1 + r.nextInt(10);
            int[][] qs = new int[m][];
            int[] want = new int[m];
            for (int q = 0; q < m; q++) {
                int x = randVal(r), lim = r.nextInt(4) == 0 ? r.nextInt(8) : randVal(r);
                qs[q] = new int[] {x, lim};
                want[q] = -1;
                for (int v : a) if (v <= lim) want[q] = Math.max(want[q], x ^ v);
            }
            Check.eq(want, maximizeXor(a.clone(), qs), "offline");
        }
        // trap: low-to-high greedy is wrong
        Check.eq(1, lowToHigh(new int[] {1, 2}, 0), "low to high gives 1");
        // trap: HIGH = 29 keeps only bits 29..0, so 2^30 looks like 0
        int low30 = (1 << 30) - 1;
        Check.eq(0, findMaximumXOR(new int[] {0 & low30, (1 << 30) & low30}), "HIGH = 29");
        Check.eq(1 << 30, findMaximumXOR(new int[] {0, 1 << 30}), "HIGH = 30");
        // empty trie returns 0, not -1
        Check.eq(0, new XorTrie(1).maxXor(5), "empty trie");
        // LeetCode samples
        Check.eq(28, findMaximumXOR(new int[] {3, 10, 5, 25, 2, 8}), "LC 421 sample 1");
        Check.eq(127, findMaximumXOR(new int[] {14, 70, 53, 83, 49, 91, 36, 80, 92, 51, 66, 70}), "LC 421 sample 2");
        Check.eq(new int[] {3, 3, 7}, maximizeXor(new int[] {0, 1, 2, 3, 4},
                new int[][] {{3, 1}, {1, 3}, {5, 6}}), "LC 1707 sample 1");
        Check.eq(new int[] {15, -1, 5}, maximizeXor(new int[] {5, 2, 4, 6, 6, 3},
                new int[][] {{12, 4}, {8, 1}, {6, 3}}), "LC 1707 sample 2");
        // big input runs fast
        int[] big = new int[200000];
        for (int i = 0; i < big.length; i++) big[i] = r.nextInt(Integer.MAX_VALUE);
        int got = findMaximumXOR(big);
        Check.isTrue(got > 0, "big");
        Check.done("P12XorTrie");
    }
}
