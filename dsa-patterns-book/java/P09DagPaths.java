import java.util.*;

public class P09DagPaths {
    //@tpl finish
    // Parallel courses III: {a, b} (1-based) means a before b; course v takes
    // time[v - 1]. Earliest finish of v = time + latest finish of a prerequisite.
    static int minimumTime(int n, int[][] relations, int[] time) {
        List<Integer>[] g = new List[n];
        for (int v = 0; v < n; v++) g[v] = new ArrayList<>();
        int[] indeg = new int[n], start = new int[n];  // start = all prereqs done
        for (int[] e : relations) { g[e[0] - 1].add(e[1] - 1); indeg[e[1] - 1]++; }
        Deque<Integer> q = new ArrayDeque<>();
        for (int v = 0; v < n; v++) if (indeg[v] == 0) q.add(v);
        int best = 0;
        while (!q.isEmpty()) {
            int u = q.poll();                         // start[u] is final now
            int finish = start[u] + time[u];
            best = Math.max(best, finish);
            for (int v : g[u]) {
                start[v] = Math.max(start[v], finish);   // relax u -> v
                if (--indeg[v] == 0) q.add(v);
            }
        }
        return best;
    }
    //@end

    //@tpl colours
    // Largest colour value: cnt[v][c] = most nodes of colour c on a path ending
    // at v. Push the counts along each edge in Kahn order; a cycle gives -1.
    static int largestPathValue(String colors, int[][] edges) {
        int n = colors.length();
        List<Integer>[] g = new List[n];
        for (int v = 0; v < n; v++) g[v] = new ArrayList<>();
        int[] indeg = new int[n];
        for (int[] e : edges) { g[e[0]].add(e[1]); indeg[e[1]]++; }
        int[][] cnt = new int[n][26];
        Deque<Integer> q = new ArrayDeque<>();
        for (int v = 0; v < n; v++) if (indeg[v] == 0) q.add(v);
        int done = 0, best = 0;
        while (!q.isEmpty()) {
            int u = q.poll();
            done++;
            best = Math.max(best, ++cnt[u][colors.charAt(u) - 'a']);  // add u
            for (int v : g[u]) {
                for (int c = 0; c < 26; c++)
                    cnt[v][c] = Math.max(cnt[v][c], cnt[u][c]);
                if (--indeg[v] == 0) q.add(v);
            }
        }
        return done < n ? -1 : best;
    }
    //@end

    //@tpl matrix
    static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    // Longest increasing path in a matrix: "step to a bigger neighbour" is a DAG,
    // so a memoised DFS computes each cell's answer once.
    static int longestIncreasingPath(int[][] m) {
        int[][] memo = new int[m.length][m[0].length];    // 0 = not computed
        int best = 0;
        for (int r = 0; r < m.length; r++)
            for (int c = 0; c < m[0].length; c++)
                best = Math.max(best, longest(m, r, c, memo));
        return best;
    }

    static int longest(int[][] m, int r, int c, int[][] memo) {
        if (memo[r][c] != 0) return memo[r][c];
        int best = 1;                                     // the cell alone
        for (int[] d : DIRS) {
            int nr = r + d[0], nc = c + d[1];
            if (nr < 0 || nc < 0 || nr >= m.length || nc >= m[0].length) continue;
            if (m[nr][nc] > m[r][c])                      // an edge of the DAG
                best = Math.max(best, 1 + longest(m, nr, nc, memo));
        }
        return memo[r][c] = best;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Earliest finish by plain recursion over prerequisites, no memo.
    static int bruteFinish(int v, List<List<Integer>> pre, int[] time) {
        int start = 0;
        for (int u : pre.get(v)) start = Math.max(start, bruteFinish(u, pre, time));
        return start + time[v];
    }

    static boolean hasCycle(int n, int[][] edges) {
        boolean[][] reach = new boolean[n][n];
        for (int[] e : edges) reach[e[0]][e[1]] = true;
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                if (reach[i][k]) for (int j = 0; j < n; j++) if (reach[k][j]) reach[i][j] = true;
        for (int i = 0; i < n; i++) if (reach[i][i]) return true;
        return false;
    }

    // Walk every path from every node (acyclic only), counting colours.
    static int brutePaths(int u, List<List<Integer>> out, String colors, int[] cnt) {
        cnt[colors.charAt(u) - 'a']++;
        int best = 0;
        for (int x : cnt) best = Math.max(best, x);
        for (int v : out.get(u)) best = Math.max(best, brutePaths(v, out, colors, cnt));
        cnt[colors.charAt(u) - 'a']--;
        return best;
    }

    static int bruteLip(int[][] m, int r, int c) {
        int best = 1;
        for (int[] d : DIRS) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nc >= 0 && nr < m.length && nc < m[0].length && m[nr][nc] > m[r][c])
                best = Math.max(best, 1 + bruteLip(m, nr, nc));
        }
        return best;
    }

    static int[][] randomDag(Random r, int n, int m) {
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;
        for (int i = n - 1; i > 0; i--) { int j = r.nextInt(i + 1), t = perm[i]; perm[i] = perm[j]; perm[j] = t; }
        List<int[]> e = new ArrayList<>();
        for (int i = 0; i < m && n > 1; i++) {
            int a = r.nextInt(n), b = r.nextInt(n);
            if (a == b) continue;
            if (a > b) { int t = a; a = b; b = t; }
            e.add(new int[] {perm[a], perm[b]});
        }
        return e.toArray(new int[0][]);
    }

    public static void main(String[] args) {
        Random r = new Random(98);
        int cyc = 0;
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(9);
            int[][] dag = randomDag(r, n, r.nextInt(2 * n + 1));
            int[][] rel = new int[dag.length][];
            List<List<Integer>> pre = new ArrayList<>();
            for (int i = 0; i < n; i++) pre.add(new ArrayList<>());
            for (int i = 0; i < dag.length; i++) {
                rel[i] = new int[] {dag[i][0] + 1, dag[i][1] + 1};
                pre.get(dag[i][1]).add(dag[i][0]);
            }
            int[] time = Check.ints(r, n, 1, 10);
            int exp = 0;
            for (int v = 0; v < n; v++) exp = Math.max(exp, bruteFinish(v, pre, time));
            Check.eq(exp, minimumTime(n, rel, time), "parallel " + Check.str(rel) + " " + Arrays.toString(time));
            // colours: a DAG, sometimes with extra random edges that may close a cycle
            int[][] edges = dag;
            if (r.nextInt(3) == 0) {
                edges = Arrays.copyOf(dag, dag.length + 1);
                edges[dag.length] = new int[] {r.nextInt(n), r.nextInt(n)};
            }
            String colors = Check.str(r, n, "abc");
            int want;
            if (hasCycle(n, edges)) { want = -1; cyc++; }
            else {
                List<List<Integer>> out = new ArrayList<>();
                for (int i = 0; i < n; i++) out.add(new ArrayList<>());
                for (int[] e : edges) out.get(e[0]).add(e[1]);
                want = 0;
                for (int v = 0; v < n; v++) want = Math.max(want, brutePaths(v, out, colors, new int[26]));
            }
            Check.eq(want, largestPathValue(colors, edges), "colours " + colors + " " + Check.str(edges));
            // matrix
            int R = 1 + r.nextInt(4), C = 1 + r.nextInt(4);
            int[][] mat = new int[R][C];
            for (int i = 0; i < R; i++) mat[i] = Check.ints(r, C, 0, 6);
            int lip = 0;
            for (int i = 0; i < R; i++) for (int j = 0; j < C; j++) lip = Math.max(lip, bruteLip(mat, i, j));
            Check.eq(lip, longestIncreasingPath(mat), "lip " + Check.str(mat));
        }
        Check.isTrue(cyc > 200, "cycles tested: " + cyc);
        Check.eq(8, minimumTime(3, new int[][] {{1, 3}, {2, 3}}, new int[] {3, 2, 5}), "LC 2050 sample 1");
        Check.eq(12, minimumTime(5, new int[][] {{1, 5}, {2, 5}, {3, 5}, {3, 4}, {4, 5}}, new int[] {1, 2, 3, 4, 5}),
                 "LC 2050 sample 2");
        Check.eq(3, largestPathValue("abaca", new int[][] {{0, 1}, {0, 2}, {2, 3}, {3, 4}}), "LC 1857 sample 1");
        Check.eq(-1, largestPathValue("a", new int[][] {{0, 0}}), "LC 1857 sample 2");
        Check.eq(4, longestIncreasingPath(new int[][] {{9, 9, 4}, {6, 6, 8}, {2, 1, 1}}), "LC 329 sample 1");
        Check.eq(4, longestIncreasingPath(new int[][] {{3, 4, 5}, {3, 2, 6}, {2, 2, 1}}), "LC 329 sample 2");
        Check.eq(1, longestIncreasingPath(new int[][] {{1}}), "LC 329 sample 3");
        Check.done("P09DagPaths");
    }
}
