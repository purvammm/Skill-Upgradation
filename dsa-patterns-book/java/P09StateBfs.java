import java.util.*;

public class P09StateBfs {
    //@tpl lock
    // Open the lock: a state is the 4-digit string; a move turns one wheel by 1.
    static int openLock(String[] deadends, String target) {
        Set<String> seen = new HashSet<>(Arrays.asList(deadends));
        if (!seen.add("0000")) return -1;            // the start is a dead end
        Deque<String> q = new ArrayDeque<>();
        q.add("0000");
        for (int moves = 0; !q.isEmpty(); moves++) {
            for (int size = q.size(); size > 0; size--) {
                String s = q.poll();
                if (s.equals(target)) return moves;
                char[] w = s.toCharArray();
                for (int i = 0; i < 4; i++) {
                    char keep = w[i];
                    for (int turn = 1; turn <= 9; turn += 8) {  // +1, and +9 = -1
                        w[i] = (char) ('0' + (keep - '0' + turn) % 10);
                        String next = new String(w);
                        if (seen.add(next)) q.add(next);   // dead ends start seen
                    }
                    w[i] = keep;
                }
            }
        }
        return -1;
    }
    //@end

    //@tpl obstacles
    static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    // Grid with obstacles elimination: state = (row, col, eliminations left).
    static int shortestPath(int[][] grid, int k) {
        int m = grid.length, n = grid[0].length;
        boolean[][][] seen = new boolean[m][n][k + 1];
        Deque<int[]> q = new ArrayDeque<>();
        q.add(new int[] {0, 0, k, 0});                   // row, col, left, steps
        seen[0][0][k] = true;
        while (!q.isEmpty()) {
            int[] s = q.poll();
            if (s[0] == m - 1 && s[1] == n - 1) return s[3];
            for (int[] d : DIRS) {
                int r = s[0] + d[0], c = s[1] + d[1];
                if (r < 0 || c < 0 || r >= m || c >= n) continue;
                int left = s[2] - grid[r][c];            // a wall costs one
                if (left < 0 || seen[r][c][left]) continue;
                seen[r][c][left] = true;
                q.add(new int[] {r, c, left, s[3] + 1});
            }
        }
        return -1;
    }
    //@end

    //@tpl allnodes
    // Shortest path visiting all nodes: state = (node, bitmask of visited).
    static int shortestPathLength(int[][] graph) {
        int n = graph.length, full = (1 << n) - 1;
        boolean[][] seen = new boolean[n][1 << n];
        Deque<int[]> q = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {                // any start: all at step 0
            q.add(new int[] {v, 1 << v});
            seen[v][1 << v] = true;
        }
        for (int steps = 0; !q.isEmpty(); steps++) {
            for (int size = q.size(); size > 0; size--) {
                int[] s = q.poll();
                if (s[1] == full) return steps;
                for (int v : graph[s[0]]) {
                    int mask = s[1] | 1 << v;
                    if (seen[v][mask]) continue;
                    seen[v][mask] = true;
                    q.add(new int[] {v, mask});
                }
            }
        }
        return -1;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static final int INF = 1 << 29;

    // Lock: Bellman-Ford style relaxation over all 10^4 codes until nothing changes.
    static int bruteLock(String[] dead, String target) {
        boolean[] bad = new boolean[10000];
        for (String s : dead) bad[Integer.parseInt(s)] = true;
        if (bad[0]) return -1;
        int[] d = new int[10000];
        Arrays.fill(d, INF);
        d[0] = 0;
        int[] pw = {1000, 100, 10, 1};
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int v = 0; v < 10000; v++) {
                if (d[v] == INF) continue;
                for (int i = 0; i < 4; i++) {
                    int digit = v / pw[i] % 10;
                    for (int delta : new int[] {1, 9}) {
                        int u = v + ((digit + delta) % 10 - digit) * pw[i];
                        if (!bad[u] && d[v] + 1 < d[u]) { d[u] = d[v] + 1; changed = true; }
                    }
                }
            }
        }
        int t = Integer.parseInt(target);
        return d[t] == INF ? -1 : d[t];
    }

    // Obstacles: try every set of at most k walls to knock down, then plain
    // shortest path by relaxation on the grid with those walls removed.
    static int bruteObstacles(int[][] g, int k) {
        int m = g.length, n = g[0].length, N = m * n;
        List<Integer> walls = new ArrayList<>();
        for (int v = 0; v < N; v++) if (g[v / n][v % n] == 1) walls.add(v);
        int best = INF;
        for (int mask = 0; mask < (1 << walls.size()); mask++) {
            if (Integer.bitCount(mask) > k) continue;
            boolean[] open = new boolean[N];
            for (int v = 0; v < N; v++) open[v] = g[v / n][v % n] == 0;
            for (int i = 0; i < walls.size(); i++) if ((mask >> i & 1) == 1) open[walls.get(i)] = true;
            int[] d = new int[N];
            Arrays.fill(d, INF);
            d[0] = 0;
            boolean changed = true;
            while (changed) {
                changed = false;
                for (int v = 0; v < N; v++) {
                    if (d[v] == INF) continue;
                    for (int[] dd : DIRS) {
                        int r = v / n + dd[0], c = v % n + dd[1];
                        if (r < 0 || c < 0 || r >= m || c >= n || !open[r * n + c]) continue;
                        if (d[v] + 1 < d[r * n + c]) { d[r * n + c] = d[v] + 1; changed = true; }
                    }
                }
            }
            best = Math.min(best, d[N - 1]);
        }
        return best == INF ? -1 : best;
    }

    // All nodes: Floyd for pairwise distances, then try every visiting order.
    static int bruteAllNodes(int[][] graph) {
        int n = graph.length;
        int[][] d = new int[n][n];
        for (int[] row : d) Arrays.fill(row, INF);
        for (int i = 0; i < n; i++) { d[i][i] = 0; for (int j : graph[i]) d[i][j] = 1; }
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++) d[i][j] = Math.min(d[i][j], d[i][k] + d[k][j]);
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;
        return bestOrder(perm, 0, d);
    }

    static int bestOrder(int[] p, int i, int[][] d) {
        if (i == p.length) {
            int s = 0;
            for (int j = 0; j + 1 < p.length; j++) s += d[p[j]][p[j + 1]];
            return s;
        }
        int best = INF;
        for (int j = i; j < p.length; j++) {
            int t = p[i]; p[i] = p[j]; p[j] = t;
            best = Math.min(best, bestOrder(p, i + 1, d));
            t = p[i]; p[i] = p[j]; p[j] = t;
        }
        return best;
    }

    static int[][] randomConnected(Random r, int n) {
        Set<Long> es = new HashSet<>();
        for (int[] e : Check.randomTree(r, n)) es.add((long) Math.min(e[0], e[1]) * 64 + Math.max(e[0], e[1]));
        int extra = r.nextInt(n + 1);
        for (int i = 0; i < extra; i++) {
            int a = r.nextInt(n), b = r.nextInt(n);
            if (a != b) es.add((long) Math.min(a, b) * 64 + Math.max(a, b));
        }
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (long e : es) { int a = (int) (e / 64), b = (int) (e % 64); adj.get(a).add(b); adj.get(b).add(a); }
        int[][] g = new int[n][];
        for (int i = 0; i < n; i++) {
            Collections.shuffle(adj.get(i), r);
            g[i] = new int[adj.get(i).size()];
            for (int j = 0; j < g[i].length; j++) g[i][j] = adj.get(i).get(j);
        }
        return g;
    }

    static String code(int v) {
        return String.valueOf(10000 + v).substring(1);
    }

    public static void main(String[] args) {
        Random r = new Random(94);
        for (int t = 0; t < 40; t++) {
            int density = r.nextInt(60);
            List<String> dead = new ArrayList<>();
            for (int v = 0; v < 10000; v++) if (r.nextInt(100) < density) dead.add(code(v));
            if (r.nextBoolean()) dead.remove("0000");
            String[] da = dead.toArray(new String[0]);
            String target = code(r.nextInt(10000));
            Check.eq(bruteLock(da, target), openLock(da, target), "lock t=" + t);
        }
        for (int t = 0; t < 1500; t++) {
            int m = 1 + r.nextInt(4), n = 1 + r.nextInt(4);
            int[][] g = new int[m][n];
            int walls = 0;
            for (int i = 0; i < m; i++)
                for (int j = 0; j < n; j++)
                    if (walls < 7 && r.nextInt(100) < 45) { g[i][j] = 1; walls++; }
            g[0][0] = 0;
            g[m - 1][n - 1] = 0;
            int k = r.nextInt(4);
            Check.eq(bruteObstacles(g, k), shortestPath(g, k), "obstacles k=" + k + " " + Check.str(g));
        }
        for (int t = 0; t < 600; t++) {
            int n = 1 + r.nextInt(7);
            int[][] g = randomConnected(r, n);
            Check.eq(bruteAllNodes(g), shortestPathLength(g), "all nodes " + Check.str(g));
        }
        Check.eq(0, openLock(new String[0], "0000"), "lock already open");
        Check.eq(-1, openLock(new String[] {"0000"}, "0000"), "lock start is dead");
        Check.eq(-1, openLock(new String[] {"0202"}, "0202"), "lock target is dead");
        Check.eq(20, bruteLock(new String[0], "5555"), "brute lock farthest");
        Check.eq(6, openLock(new String[] {"0201", "0101", "0102", "1212", "2002"}, "0202"), "LC 752 sample 1");
        Check.eq(1, openLock(new String[] {"8888"}, "0009"), "LC 752 sample 2");
        Check.eq(-1, openLock(new String[] {"8887", "8889", "8878", "8898", "8788", "8988", "7888", "9888"}, "8888"),
                 "LC 752 sample 3");
        Check.eq(6, shortestPath(new int[][] {{0, 0, 0}, {1, 1, 0}, {0, 0, 0}, {0, 1, 1}, {0, 0, 0}}, 1), "LC 1293 sample 1");
        Check.eq(-1, shortestPath(new int[][] {{0, 1, 1}, {1, 1, 1}, {1, 0, 0}}, 1), "LC 1293 sample 2");
        Check.eq(4, shortestPathLength(new int[][] {{1, 2, 3}, {0}, {0}, {0}}), "LC 847 sample 1");
        Check.eq(4, shortestPathLength(new int[][] {{1}, {0, 2, 4}, {1, 3, 4}, {2}, {1, 2}}), "LC 847 sample 2");
        Check.done("P09StateBfs");
    }
}
