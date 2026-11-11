import java.util.*;

public class P08Rerooting {
    //@tpl distances
    // Pass 1, walk() from 8.9 at root 0: the sum of depths is ans[0].
    // Pass 2, parents first: root p -> child v, ans[v] from ans[p] in O(1).
    static int[] sumOfDistancesInTree(int n, int[][] edges) {
        walk(adjacency(n, edges), 0);
        int[] ans = new int[n];
        for (int v = 0; v < n; v++) ans[0] += depth[v];
        for (int i = 1; i < n; i++) {              // order[0] is the root
            int v = order[i];
            ans[v] = ans[parent[v]] - size[v] + (n - size[v]);
        }
        return ans;
    }
    //@end

    //@tpl reversals
    // Min edge reversals to reach every node from v. For root 0, flip every
    // edge that points up; moving the root across (p, v) changes only that one.
    static int[] minEdgeReversals(int n, int[][] edges) {
        walk(adjacency(n, edges), 0);
        boolean[] down = new boolean[n];           // edge parent[v] -> v exists
        for (int[] e : edges) if (parent[e[1]] == e[0]) down[e[1]] = true;
        int[] ans = new int[n];
        for (int v = 1; v < n; v++) if (!down[v]) ans[0]++;
        for (int i = 1; i < n; i++) {
            int v = order[i];                      // root moves parent[v] -> v
            ans[v] = ans[parent[v]] + (down[v] ? 1 : -1);
        }
        return ans;
    }
    //@end

    //@tpl guesses
    // Count possible roots: guess [u, v] claims parent(v) == u. Moving the
    // root from p to v makes [p, v] false and [v, p] true; nothing else changes.
    static int rootCount(int[][] edges, int[][] guesses, int k) {
        int n = edges.length + 1;
        walk(adjacency(n, edges), 0);
        Set<Long> g = new HashSet<>();
        for (int[] q : guesses) g.add((long) q[0] * n + q[1]);
        int[] right = new int[n];
        for (int v = 1; v < n; v++)
            if (g.contains((long) parent[v] * n + v)) right[0]++;
        int roots = right[0] >= k ? 1 : 0;
        for (int i = 1; i < n; i++) {
            int v = order[i], p = parent[v];
            right[v] = right[p];
            if (g.contains((long) p * n + v)) right[v]--;   // p is not v's parent
            if (g.contains((long) v * n + p)) right[v]++;   // v is now p's parent
            if (right[v] >= k) roots++;
        }
        return roots;
    }
    //@end

    // ---------------- walk() from P08TreeEdges (8.9), not printed again ----------------
    static int[] parent, depth, order, size;

    static List<Integer>[] adjacency(int n, int[][] edges) {
        List<Integer>[] g = new List[n];
        for (int v = 0; v < n; v++) g[v] = new ArrayList<>();
        for (int[] e : edges) { g[e[0]].add(e[1]); g[e[1]].add(e[0]); }
        return g;
    }

    static void walk(List<Integer>[] g, int root) {
        int n = g.length, k = 0;
        parent = new int[n]; depth = new int[n]; order = new int[n];
        Deque<Integer> st = new ArrayDeque<>();
        st.push(root);
        parent[root] = -1;
        while (!st.isEmpty()) {
            int v = st.pop();
            order[k++] = v;
            for (int u : g[v]) {
                if (u == parent[v]) continue;      // the edge back up
                parent[u] = v;
                depth[u] = depth[v] + 1;
                st.push(u);
            }
        }
        size = new int[n];
        for (int i = n - 1; i >= 0; i--) {         // children before parents
            int v = order[i];
            size[v] += 1;
            if (parent[v] >= 0) size[parent[v]] += size[v];
        }
    }

    // ---------------- tests (not printed) ----------------
    static int[][] shuffledTree(Random r, int n) {
        int[][] e = Check.randomTree(r, n);
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;
        for (int i = n - 1; i > 0; i--) { int j = r.nextInt(i + 1); int t = perm[i]; perm[i] = perm[j]; perm[j] = t; }
        for (int[] x : e) {
            x[0] = perm[x[0]];
            x[1] = perm[x[1]];
            if (r.nextBoolean()) { int t = x[0]; x[0] = x[1]; x[1] = t; }
        }
        return e;
    }

    // one BFS per root: distances, and the parent of every node for that root
    static int[][] bfs(List<Integer>[] g, int s) {
        int n = g.length;
        int[] dist = new int[n], par = new int[n];
        Arrays.fill(dist, -1);
        dist[s] = 0;
        par[s] = -1;
        Deque<Integer> q = new ArrayDeque<>();
        q.add(s);
        while (!q.isEmpty()) {
            int v = q.poll();
            for (int u : g[v]) if (dist[u] < 0) { dist[u] = dist[v] + 1; par[u] = v; q.add(u); }
        }
        return new int[][] {dist, par};
    }

    public static void main(String[] args) {
        Random r = new Random(812);
        for (int t = 0; t < 2000; t++) {
            int n = 1 + r.nextInt(30);
            int[][] e = shuffledTree(r, n);
            List<Integer>[] g = adjacency(n, e);
            int[] wantDist = new int[n], wantRev = new int[n];
            int[][] parFor = new int[n][];
            for (int s = 0; s < n; s++) {
                int[][] b = bfs(g, s);
                for (int v = 0; v < n; v++) wantDist[s] += b[0][v];
                parFor[s] = b[1];
                // with root s, edge [x, y] must point down: x must be y's parent
                for (int[] x : e) if (b[1][x[1]] != x[0]) wantRev[s]++;
            }
            Check.eq(wantDist, sumOfDistancesInTree(n, e), "distances");
            Check.eq(wantRev, minEdgeReversals(n, e), "reversals");
            int m = r.nextInt(2 * n + 1);
            int[][] gs = new int[m][];
            Set<Long> used = new HashSet<>();
            int filled = 0;
            for (int i = 0; i < m && n > 1; i++) {
                int[] x = e[r.nextInt(n - 1)];
                int[] gu = r.nextBoolean() ? new int[] {x[0], x[1]} : new int[] {x[1], x[0]};
                if (!used.add((long) gu[0] * n + gu[1])) continue;   // guesses are distinct
                gs[filled++] = gu;
            }
            gs = Arrays.copyOf(gs, filled);
            int k = r.nextInt(filled + 1);
            int want = 0;
            for (int s = 0; s < n; s++) {
                int ok = 0;
                for (int[] gu : gs) if (parFor[s][gu[1]] == gu[0]) ok++;
                if (ok >= k) want++;
            }
            Check.eq(want, rootCount(e, gs, k), "root count");
        }
        Check.eq(new int[] {8, 12, 6, 10, 10, 10}, sumOfDistancesInTree(6, new int[][] {{0, 1}, {0, 2}, {2, 3}, {2, 4}, {2, 5}}), "LC 834 sample 1");
        Check.eq(new int[] {0}, sumOfDistancesInTree(1, new int[][] {}), "LC 834 sample 2");
        Check.eq(new int[] {1, 1}, sumOfDistancesInTree(2, new int[][] {{1, 0}}), "LC 834 sample 3");
        Check.eq(new int[] {1, 1, 0, 2}, minEdgeReversals(4, new int[][] {{2, 0}, {2, 1}, {1, 3}}), "LC 2858 sample 1");
        Check.eq(new int[] {2, 0, 1}, minEdgeReversals(3, new int[][] {{1, 2}, {2, 0}}), "LC 2858 sample 2");
        Check.eq(3, rootCount(new int[][] {{0, 1}, {1, 2}, {1, 3}, {4, 2}}, new int[][] {{1, 3}, {0, 1}, {1, 0}, {2, 4}}, 3), "LC 2581 sample 1");
        Check.eq(5, rootCount(new int[][] {{0, 1}, {1, 2}, {2, 3}, {3, 4}}, new int[][] {{1, 0}, {3, 4}, {2, 1}, {3, 2}}, 1), "LC 2581 sample 2");
        // n = 30000 chain: answer for node 0 is 0 + 1 + ... + 29999 = 449985000 (fits int)
        int big = 30000;
        int[][] chain = new int[big - 1][];
        for (int i = 0; i + 1 < big; i++) chain[i] = new int[] {i, i + 1};
        Check.eq(449985000, sumOfDistancesInTree(big, chain)[0], "big chain");
        Check.done("P08Rerooting");
    }
}
