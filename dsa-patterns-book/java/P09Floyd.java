import java.util.*;

public class P09Floyd {
    //@tpl main
    static final long INF = Long.MAX_VALUE / 4;       // INF + INF does not overflow

    // All pairs, in place: d[i][i] = 0, d[i][j] = edge weight or INF (w >= 0).
    // After round k, d[i][j] is the best path whose middle nodes are all <= k.
    static void floyd(long[][] d) {
        int n = d.length;
        for (int k = 0; k < n; k++)                     // k must be the outer loop
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    d[i][j] = Math.min(d[i][j], d[i][k] + d[k][j]);
    }

    // LC 1334: the city with the fewest cities within the threshold.
    // Ties go to the largest index, hence <= below. Edges are undirected.
    static int findTheCity(int n, int[][] edges, int threshold) {
        long[][] d = new long[n][n];
        for (long[] row : d) Arrays.fill(row, INF);
        for (int i = 0; i < n; i++) d[i][i] = 0;
        for (int[] e : edges) {
            d[e[0]][e[1]] = Math.min(d[e[0]][e[1]], e[2]);
            d[e[1]][e[0]] = Math.min(d[e[1]][e[0]], e[2]);
        }
        floyd(d);
        int best = -1, bestCount = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            int count = 0;
            for (int j = 0; j < n; j++) if (j != i && d[i][j] <= threshold) count++;
            if (count <= bestCount) { bestCount = count; best = i; }
        }
        return best;
    }
    //@end

    //@tpl closure
    // Course schedule IV (LC 1462): is a a prerequisite of b, directly or not?
    // Same three loops with OR instead of min: the transitive closure.
    static List<Boolean> checkIfPrerequisite(int n, int[][] pre, int[][] queries) {
        boolean[][] reach = new boolean[n][n];
        for (int[] p : pre) reach[p[0]][p[1]] = true;
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                if (reach[i][k])
                    for (int j = 0; j < n; j++)
                        if (reach[k][j]) reach[i][j] = true;
        List<Boolean> ans = new ArrayList<>();
        for (int[] q : queries) ans.add(reach[q[0]][q[1]]);
        return ans;
    }
    //@end

    //@tpl convert
    // Minimum cost to convert string I (LC 2976): Floyd on the 26 letters,
    // then pay d[s][t] for every position.
    static long minimumCost(String source, String target, char[] original,
                            char[] changed, int[] cost) {
        long[][] d = new long[26][26];
        for (long[] row : d) Arrays.fill(row, INF);
        for (int i = 0; i < 26; i++) d[i][i] = 0;
        for (int i = 0; i < cost.length; i++) {
            int a = original[i] - 'a', b = changed[i] - 'a';
            d[a][b] = Math.min(d[a][b], cost[i]);       // keep the cheapest rule
        }
        floyd(d);
        long total = 0;
        for (int i = 0; i < source.length(); i++) {
            long c = d[source.charAt(i) - 'a'][target.charAt(i) - 'a'];
            if (c >= INF) return -1;
            total += c;
        }
        return total;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // shortest simple path between every pair (weights >= 0)
    static void simple(int u, int[][] w, boolean[] on, long len, long[] best) {
        best[u] = Math.min(best[u], len);
        for (int v = 0; v < w.length; v++) {
            if (w[u][v] < 0 || on[v]) continue;
            on[v] = true;
            simple(v, w, on, len + w[u][v], best);
            on[v] = false;
        }
    }

    static long[][] brute(int[][] w) {
        int n = w.length;
        long[][] d = new long[n][n];
        for (int s = 0; s < n; s++) {
            Arrays.fill(d[s], INF);
            boolean[] on = new boolean[n];
            on[s] = true;
            simple(s, w, on, 0, d[s]);
        }
        return d;
    }

    // relax every rule until nothing changes (Bellman-Ford style)
    static long[][] bruteLetters(char[] o, char[] c, int[] cost) {
        long[][] d = new long[26][26];
        for (int s = 0; s < 26; s++) {
            Arrays.fill(d[s], INF);
            d[s][s] = 0;
            boolean changed = true;
            while (changed) {
                changed = false;
                for (int i = 0; i < cost.length; i++) {
                    int a = o[i] - 'a', b = c[i] - 'a';
                    if (d[s][a] < INF && d[s][a] + cost[i] < d[s][b]) { d[s][b] = d[s][a] + cost[i]; changed = true; }
                }
            }
        }
        return d;
    }

    public static void main(String[] args) {
        Random r = new Random(13);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(6);
            int[][] w = new int[n][n];
            for (int[] row : w) Arrays.fill(row, -1);
            long[][] d = new long[n][n];
            for (long[] row : d) Arrays.fill(row, INF);
            for (int i = 0; i < n; i++) d[i][i] = 0;
            int m = r.nextInt(14);
            for (int i = 0; i < m; i++) {
                int u = r.nextInt(n), v = r.nextInt(n), x = r.nextInt(t % 3 == 0 ? 2 : 30);
                if (u == v) continue;
                if (w[u][v] < 0 || x < w[u][v]) w[u][v] = x;
                d[u][v] = w[u][v];
            }
            floyd(d);
            Check.eq(brute(w), d, "floyd " + Arrays.deepToString(w));
        }
        for (int t = 0; t < 2000; t++) {
            int n = 2 + r.nextInt(6);
            List<int[]> es = new ArrayList<>();
            int[][] w = new int[n][n];
            for (int[] row : w) Arrays.fill(row, -1);
            for (int u = 0; u < n; u++)
                for (int v = u + 1; v < n; v++)
                    if (r.nextInt(2) == 0) {
                        int x = 1 + r.nextInt(8);
                        es.add(new int[] {u, v, x});
                        w[u][v] = w[v][u] = x;
                    }
            int th = 1 + r.nextInt(15);
            long[][] d = brute(w);
            int best = -1, bc = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                int c = 0;
                for (int j = 0; j < n; j++) if (j != i && d[i][j] <= th) c++;
                if (c < bc || (c == bc && i > best)) { bc = c; best = i; }
            }
            Check.eq(best, findTheCity(n, es.toArray(new int[0][]), th), "city");
        }
        for (int t = 0; t < 2000; t++) {
            int n = 2 + r.nextInt(7);
            List<int[]> pre = new ArrayList<>();
            int[] perm = new int[n];
            for (int i = 0; i < n; i++) perm[i] = i;
            for (int i = n - 1; i > 0; i--) { int j = r.nextInt(i + 1); int x = perm[i]; perm[i] = perm[j]; perm[j] = x; }
            for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++) if (r.nextInt(4) == 0) pre.add(new int[] {perm[i], perm[j]});
            int[][] qs = new int[10][];
            for (int i = 0; i < 10; i++) {
                int a = r.nextInt(n), b = r.nextInt(n - 1);
                if (b >= a) b++;
                qs[i] = new int[] {a, b};
            }
            // reachability by DFS
            List<Integer>[] g = new List[n];
            for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
            for (int[] p : pre) g[p[0]].add(p[1]);
            List<Boolean> want = new ArrayList<>();
            for (int[] q : qs) {
                boolean[] seen = new boolean[n];
                Deque<Integer> st = new ArrayDeque<>();
                st.push(q[0]);
                seen[q[0]] = true;
                while (!st.isEmpty()) for (int v : g[st.pop()]) if (!seen[v]) { seen[v] = true; st.push(v); }
                want.add(seen[q[1]]);
            }
            Check.eq(want, checkIfPrerequisite(n, pre.toArray(new int[0][]), qs), "closure");
        }
        for (int t = 0; t < 2000; t++) {
            int m = r.nextInt(10);
            char[] o = new char[m], c = new char[m];
            int[] cost = new int[m];
            for (int i = 0; i < m; i++) {
                o[i] = (char) ('a' + r.nextInt(5));
                do c[i] = (char) ('a' + r.nextInt(5)); while (c[i] == o[i]);
                cost[i] = 1 + r.nextInt(t % 2 == 0 ? 3 : 1_000_000);
            }
            String s = Check.str(r, 1 + r.nextInt(8), "abcde"), tg = Check.str(r, s.length(), "abcde");
            long[][] d = bruteLetters(o, c, cost);
            long want = 0;
            for (int i = 0; i < s.length(); i++) {
                long x = d[s.charAt(i) - 'a'][tg.charAt(i) - 'a'];
                if (x >= INF) { want = -1; break; }
                want += x;
            }
            Check.eq(want, minimumCost(s, tg, o, c, cost), "convert " + s + " -> " + tg);
        }
        // 10^5 positions at 10^6 * 25 each: the total needs a long
        char[] o = new char[25], c = new char[25];
        int[] cost = new int[25];
        for (int i = 0; i < 25; i++) { o[i] = (char) ('a' + i); c[i] = (char) ('b' + i); cost[i] = 1_000_000; }
        StringBuilder sa = new StringBuilder(), sz = new StringBuilder();
        for (int i = 0; i < 100_000; i++) { sa.append('a'); sz.append('z'); }
        Check.eq(25L * 1_000_000 * 100_000, minimumCost(sa.toString(), sz.toString(), o, c, cost), "long total");

        Check.eq(3, findTheCity(4, new int[][] {{0, 1, 3}, {1, 2, 1}, {1, 3, 4}, {2, 3, 1}}, 4), "LC 1334 sample 1");
        Check.eq(0, findTheCity(5, new int[][] {{0, 1, 2}, {0, 4, 8}, {1, 2, 3}, {1, 4, 2}, {2, 3, 1}, {3, 4, 1}}, 2),
            "LC 1334 sample 2");
        Check.eq(Arrays.asList(false, true), checkIfPrerequisite(2, new int[][] {{1, 0}}, new int[][] {{0, 1}, {1, 0}}),
            "LC 1462 sample 1");
        Check.eq(Arrays.asList(false, false), checkIfPrerequisite(2, new int[0][], new int[][] {{1, 0}, {0, 1}}),
            "LC 1462 sample 2");
        Check.eq(Arrays.asList(true, true), checkIfPrerequisite(3, new int[][] {{1, 2}, {1, 0}, {2, 0}},
            new int[][] {{1, 0}, {1, 2}}), "LC 1462 sample 3");
        Check.eq(28L, minimumCost("abcd", "acbe", "abcced".toCharArray(), "bcbebe".toCharArray(),
            new int[] {2, 5, 5, 1, 2, 20}), "LC 2976 sample 1");
        Check.eq(12L, minimumCost("aaaa", "bbbb", "ac".toCharArray(), "cb".toCharArray(), new int[] {1, 2}),
            "LC 2976 sample 2");
        Check.eq(-1L, minimumCost("abcd", "abce", "a".toCharArray(), "e".toCharArray(), new int[] {10000}),
            "LC 2976 sample 3");
        Check.done("P09Floyd");
    }
}
