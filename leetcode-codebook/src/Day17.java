import java.util.*;

/*@core Dijkstra and DSU
note: Dijkstra: pop the closest node; if the popped distance is bigger than `dist[u]`, it is a stale copy, skip it. Only for non-negative weights. DSU: `find` with path halving, `union` by size; two nodes are connected iff their roots match.
@*/
class Core17 {
    static long[] dijkstra(List<List<int[]>> g, int src) {        // g.get(u) = {v, w}
        long[] dist = new long[g.size()];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[src] = 0;
        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[1], b[1]));
        pq.offer(new long[]{src, 0});
        while (!pq.isEmpty()) {
            long[] cur = pq.poll();
            int u = (int) cur[0];
            if (cur[1] > dist[u]) continue;                        // stale
            for (int[] e : g.get(u))
                if (dist[u] + e[1] < dist[e[0]]) {
                    dist[e[0]] = dist[u] + e[1];
                    pq.offer(new long[]{e[0], dist[e[0]]});
                }
        }
        return dist;
    }

    static final class DSU {
        final int[] parent, size;

        DSU(int n) {
            parent = new int[n];
            size = new int[n];
            for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
        }

        int find(int x) {
            while (parent[x] != x) x = parent[x] = parent[parent[x]];   // path halving
            return x;
        }

        boolean union(int a, int b) {                              // false if already together
            a = find(a);
            b = find(b);
            if (a == b) return false;
            if (size[a] < size[b]) { int t = a; a = b; b = t; }
            parent[b] = a;
            size[a] += size[b];
            return true;
        }
    }
}

/*@LC 743
tag: Dijkstra
cue: Time for a signal from k to reach every node (directed, non-negative weights).
hook: Pop the closest, skip if stale, relax the neighbours; the answer is the farthest node.
time: O(E log E) time · O(V + E) space
trap: Nodes are 1..n: size the arrays n + 1 and ignore index 0 when taking the max.
var: LC 1514 Path with Maximum Probability | Max-heap, multiply probabilities, keep the larger.
@*/
class LC743 {
    public int networkDelayTime(int[][] times, int n, int k) {
        List<List<int[]>> g = new ArrayList<>();
        for (int i = 0; i <= n; i++) g.add(new ArrayList<>());
        for (int[] t : times) g.get(t[0]).add(new int[]{t[1], t[2]});
        int[] dist = new int[n + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[k] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[1], b[1]));   //* {node, dist}
        pq.offer(new int[]{k, 0});
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int u = cur[0];
            if (cur[1] > dist[u]) continue;                                  //* stale
            for (int[] e : g.get(u)) {
                int v = e[0], nd = cur[1] + e[1];
                if (nd < dist[v]) { dist[v] = nd; pq.offer(new int[]{v, nd}); }   //* relax
            }
        }
        int ans = 0;
        for (int i = 1; i <= n; i++) {
            if (dist[i] == Integer.MAX_VALUE) return -1;
            ans = Math.max(ans, dist[i]);
        }
        return ans;
    }
}

/*@var LC 1334 · Find the City With the Smallest Number of Neighbors (Floyd–Warshall)
note: All pairs, n ≤ 100: three loops with k OUTSIDE. Use a big value that is safe to add twice.
@*/
class LC743v1 {
    public int findTheCity(int n, int[][] edges, int distanceThreshold) {
        int[][] d = new int[n][n];
        for (int[] row : d) Arrays.fill(row, 1_000_000_000);
        for (int i = 0; i < n; i++) d[i][i] = 0;
        for (int[] e : edges) {
            d[e[0]][e[1]] = Math.min(d[e[0]][e[1]], e[2]);
            d[e[1]][e[0]] = Math.min(d[e[1]][e[0]], e[2]);
        }
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (d[i][k] + d[k][j] < d[i][j]) d[i][j] = d[i][k] + d[k][j];
        int best = -1, bestCount = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            int cnt = 0;
            for (int j = 0; j < n; j++) if (d[i][j] <= distanceThreshold) cnt++;
            if (cnt <= bestCount) { bestCount = cnt; best = i; }     // ties: larger index wins
        }
        return best;
    }
}

/*@LC 1631
tag: Dijkstra with a max-cost path
cue: Path from top-left to bottom-right minimising the biggest height step.
hook: Dijkstra where a path costs its worst step: `max(cur, step)` instead of `cur + step`.
time: O(mn log mn) time · O(mn) space
trap: You may return as soon as the target is POPPED (not when it is pushed).
var: LC 778 Swim in Rising Water | Same code with cost `max(cur, grid[nr][nc])`.
@*/
class LC1631 {
    public int minimumEffortPath(int[][] heights) {
        int m = heights.length, n = heights[0].length;
        int[][] effort = new int[m][n];
        for (int[] row : effort) Arrays.fill(row, Integer.MAX_VALUE);
        effort[0][0] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[2], b[2]));   // {r, c, effort}
        pq.offer(new int[]{0, 0, 0});
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int r = cur[0], c = cur[1];
            if (cur[2] > effort[r][c]) continue;
            if (r == m - 1 && c == n - 1) return cur[2];                               //*
            for (int d = 0; d < 4; d++) {
                int nr = r + dr[d], nc = c + dc[d];
                if (nr < 0 || nc < 0 || nr >= m || nc >= n) continue;
                int e = Math.max(cur[2], Math.abs(heights[nr][nc] - heights[r][c]));   //* worst step so far
                if (e < effort[nr][nc]) { effort[nr][nc] = e; pq.offer(new int[]{nr, nc, e}); }
            }
        }
        return 0;
    }
}

/*@LC 1584
tag: MST · Prim O(n²)
cue: Connect all points with Manhattan-distance edges at minimum total cost.
hook: Grow one tree: add the closest outside point, then let it lower everyone's distance.
time: O(n²) time · O(n) space
trap: With a complete graph, O(n²) Prim beats Kruskal's O(n² log n) sort.
var: LC 1135 Connecting Cities With Minimum Cost (premium) | Kruskal on the given edges; −1 if not all joined.
@*/
class LC1584 {
    public int minCostConnectPoints(int[][] points) {
        int n = points.length, total = 0;
        int[] best = new int[n];                                    // cheapest edge from the tree to i
        boolean[] inTree = new boolean[n];
        Arrays.fill(best, Integer.MAX_VALUE);
        best[0] = 0;
        for (int it = 0; it < n; it++) {
            int u = -1;
            for (int i = 0; i < n; i++)
                if (!inTree[i] && (u == -1 || best[i] < best[u])) u = i;   //* closest outside point
            inTree[u] = true;
            total += best[u];
            for (int v = 0; v < n; v++) {
                if (inTree[v]) continue;
                int d = Math.abs(points[u][0] - points[v][0]) + Math.abs(points[u][1] - points[v][1]);
                if (d < best[v]) best[v] = d;                       //* u may be closer
            }
        }
        return total;
    }
}

/*@var Kruskal version (sort edges + DSU)
note: Sort all edges by cost and take an edge whenever it joins two different components.
@*/
class LC1584v1 {
    public int minCostConnectPoints(int[][] points) {
        int n = points.length;
        List<int[]> edges = new ArrayList<>();
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
                edges.add(new int[]{Math.abs(points[i][0] - points[j][0]) + Math.abs(points[i][1] - points[j][1]), i, j});
        edges.sort((a, b) -> Integer.compare(a[0], b[0]));
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        int total = 0, used = 0;
        for (int[] e : edges) {
            int a = find(parent, e[1]), b = find(parent, e[2]);
            if (a == b) continue;                                  // would close a cycle
            parent[a] = b;
            total += e[0];
            if (++used == n - 1) break;
        }
        return total;
    }

    private int find(int[] p, int x) {
        while (p[x] != x) x = p[x] = p[p[x]];
        return x;
    }
}

/*@LC 1976
tag: Dijkstra + counting shortest paths
cue: Number of shortest paths from 0 to n − 1 (mod 10⁹+7).
hook: Strictly shorter: copy the ways. Equally short: add the ways.
time: O(E log E) time · O(V + E) space
trap: Distances reach 2·10¹¹: use `long` for dist and for the heap entries.
@*/
class LC1976 {
    public int countPaths(int n, int[][] roads) {
        final int MOD = 1_000_000_007;
        List<List<int[]>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] r : roads) { g.get(r[0]).add(new int[]{r[1], r[2]}); g.get(r[1]).add(new int[]{r[0], r[2]}); }
        long[] dist = new long[n], ways = new long[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[0] = 0;
        ways[0] = 1;
        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[1], b[1]));   //* long
        pq.offer(new long[]{0, 0});
        while (!pq.isEmpty()) {
            long[] cur = pq.poll();
            int u = (int) cur[0];
            if (cur[1] > dist[u]) continue;
            for (int[] e : g.get(u)) {
                int v = e[0];
                long nd = dist[u] + e[1];
                if (nd < dist[v]) {                                 //* shorter: reset
                    dist[v] = nd;
                    ways[v] = ways[u];
                    pq.offer(new long[]{v, nd});
                } else if (nd == dist[v]) {                         //* tie: add
                    ways[v] = (ways[v] + ways[u]) % MOD;
                }
            }
        }
        return (int) ways[n - 1];
    }
}

/*@LC 787
tag: Bellman–Ford with a hop limit
cue: Cheapest flight from src to dst with at most k stops.
hook: k stops = k + 1 flights = k + 1 rounds; each round relaxes from LAST round's prices.
time: O(k · E) time · O(n) space
trap: Without the copy (`next = cost.clone()`), one round can chain several flights and break the limit.
var: Dijkstra on (city, stops) | Also works; the state must include the stop count.
@*/
class LC787 {
    public int findCheapestPrice(int n, int[][] flights, int src, int dst, int k) {
        int[] cost = new int[n];
        Arrays.fill(cost, Integer.MAX_VALUE);
        cost[src] = 0;
        for (int round = 0; round <= k; round++) {                           //* k + 1 rounds
            int[] next = cost.clone();                                       //* copy
            for (int[] f : flights) {
                if (cost[f[0]] == Integer.MAX_VALUE) continue;
                next[f[1]] = Math.min(next[f[1]], cost[f[0]] + f[2]);        //* from the old prices
            }
            cost = next;
        }
        return cost[dst] == Integer.MAX_VALUE ? -1 : cost[dst];
    }
}

/*@LC 2290
tag: 0-1 BFS
cue: Fewest obstacles to remove to walk from corner to corner.
hook: Empty cell costs 0 (front of the deque), obstacle costs 1 (back).
time: O(m·n) time · O(m·n) space
trap: The cost belongs to the cell you ENTER.
@*/
class LC2290 {
    public int minimumObstacles(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[][] dist = new int[m][n];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
        dist[0][0] = 0;
        Deque<int[]> dq = new ArrayDeque<>();
        dq.offerFirst(new int[]{0, 0});
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        while (!dq.isEmpty()) {
            int[] c = dq.pollFirst();
            for (int d = 0; d < 4; d++) {
                int r = c[0] + dr[d], col = c[1] + dc[d];
                if (r < 0 || col < 0 || r >= m || col >= n) continue;
                int nd = dist[c[0]][c[1]] + grid[r][col];                       //*
                if (nd < dist[r][col]) {
                    dist[r][col] = nd;
                    if (grid[r][col] == 0) dq.offerFirst(new int[]{r, col});    //*
                    else dq.offerLast(new int[]{r, col});                       //*
                }
            }
        }
        return dist[m - 1][n - 1];
    }
}

/*@LC 2421
tag: DSU in value order
cue: Count paths whose two ends have the same value and no node on the path is bigger.
hook: Add nodes from small to large; join a neighbour only if it is not bigger; count equal values per component.
time: O(n log n) time · O(n) space
trap: Process ALL nodes of one value (all their unions) before counting pairs for that value.
var: LC 684 Redundant Connection | DSU over the edges; the first edge whose ends are already joined is the answer.
@*/
class LC2421 {
    public int numberOfGoodPaths(int[] vals, int[][] edges) {
        int n = vals.length;
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) { g.get(e[0]).add(e[1]); g.get(e[1]).add(e[0]); }
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> Integer.compare(vals[a], vals[b]));           //* small values first
        int ans = n;                                                               // single nodes
        for (int i = 0; i < n; ) {
            int j = i;
            while (j < n && vals[order[j]] == vals[order[i]]) j++;
            for (int t = i; t < j; t++)
                for (int v : g.get(order[t]))
                    if (vals[v] <= vals[order[t]]) union(parent, order[t], v);     //* only not-bigger neighbours
            Map<Integer, Integer> perRoot = new HashMap<>();
            for (int t = i; t < j; t++) perRoot.merge(find(parent, order[t]), 1, Integer::sum);
            for (int c : perRoot.values()) ans += c * (c - 1) / 2;                 //* pairs in one component
            i = j;
        }
        return ans;
    }

    private int find(int[] p, int x) {
        while (p[x] != x) x = p[x] = p[p[x]];
        return x;
    }

    private void union(int[] p, int a, int b) { p[find(p, a)] = find(p, b); }
}

public class Day17 {
    public static void main(String[] args) {
        List<List<int[]>> g = new ArrayList<>();
        for (int i = 0; i < 3; i++) g.add(new ArrayList<>());
        g.get(0).add(new int[]{1, 4}); g.get(0).add(new int[]{2, 1}); g.get(2).add(new int[]{1, 2});
        T.eq("core dij", Core17.dijkstra(g, 0), new long[]{0, 3, 1});
        Core17.DSU dsu = new Core17.DSU(4);
        T.eq("core dsu", new boolean[]{dsu.union(0, 1), dsu.union(1, 0), dsu.union(2, 3), dsu.find(0) == dsu.find(3)}, new boolean[]{true, false, true, false});

        T.eq("743a", new LC743().networkDelayTime(T.grid("[[2,1,1],[2,3,1],[3,4,1]]"), 4, 2), 2);
        T.eq("743b", new LC743().networkDelayTime(T.grid("[[1,2,1]]"), 2, 1), 1);
        T.eq("743c", new LC743().networkDelayTime(T.grid("[[1,2,1]]"), 2, 2), -1);
        T.eq("1334a", new LC743v1().findTheCity(4, T.grid("[[0,1,3],[1,2,1],[1,3,4],[2,3,1]]"), 4), 3);
        T.eq("1334b", new LC743v1().findTheCity(5, T.grid("[[0,1,2],[0,4,8],[1,2,3],[1,4,2],[2,3,1],[3,4,1]]"), 2), 0);

        T.eq("1631a", new LC1631().minimumEffortPath(T.grid("[[1,2,2],[3,8,2],[5,3,5]]")), 2);
        T.eq("1631b", new LC1631().minimumEffortPath(T.grid("[[1,2,3],[3,8,4],[5,3,5]]")), 1);
        T.eq("1631c", new LC1631().minimumEffortPath(T.grid("[[1,2,1,1,1],[1,2,1,2,1],[1,2,1,2,1],[1,2,1,2,1],[1,1,1,2,1]]")), 0);

        T.eq("1584a", new LC1584().minCostConnectPoints(T.grid("[[0,0],[2,2],[3,10],[5,2],[7,0]]")), 20);
        T.eq("1584b", new LC1584().minCostConnectPoints(T.grid("[[3,12],[-2,5],[-4,1]]")), 18);
        for (int t = 0; t < 100; t++) {
            int n = T.rnd(1, 12);
            int[][] p = new int[n][];
            for (int i = 0; i < n; i++) p[i] = new int[]{T.rnd(-20, 20), T.rnd(-20, 20)};
            T.eq("1584 rand", new LC1584().minCostConnectPoints(p), new LC1584v1().minCostConnectPoints(p));
        }

        T.eq("1976a", new LC1976().countPaths(7, T.grid("[[0,6,7],[0,1,2],[1,2,3],[1,3,3],[6,3,3],[3,5,1],[6,5,1],[2,5,1],[0,4,5],[4,6,2]]")), 4);
        T.eq("1976b", new LC1976().countPaths(2, T.grid("[[1,0,10]]")), 1);
        T.eq("1976c", new LC1976().countPaths(1, new int[0][]), 1);

        T.eq("787a", new LC787().findCheapestPrice(4, T.grid("[[0,1,100],[1,2,100],[2,0,100],[1,3,600],[2,3,200]]"), 0, 3, 1), 700);
        T.eq("787b", new LC787().findCheapestPrice(3, T.grid("[[0,1,100],[1,2,100],[0,2,500]]"), 0, 2, 1), 200);
        T.eq("787c", new LC787().findCheapestPrice(3, T.grid("[[0,1,100],[1,2,100],[0,2,500]]"), 0, 2, 0), 500);

        T.eq("2290a", new LC2290().minimumObstacles(T.grid("[[0,1,1],[1,1,0],[1,1,0]]")), 2);
        T.eq("2290b", new LC2290().minimumObstacles(T.grid("[[0,1,0,0,0],[0,1,0,1,0],[0,0,0,1,0]]")), 0);

        T.eq("2421a", new LC2421().numberOfGoodPaths(T.ints("[1,3,2,1,3]"), T.grid("[[0,1],[0,2],[2,3],[2,4]]")), 6);
        T.eq("2421b", new LC2421().numberOfGoodPaths(T.ints("[1,1,2,2,3]"), T.grid("[[0,1],[1,2],[2,3],[2,4]]")), 7);
        T.eq("2421c", new LC2421().numberOfGoodPaths(T.ints("[1]"), new int[0][]), 1);
        for (int t = 0; t < 150; t++) {
            int n = T.rnd(1, 9);
            int[] vals = T.rand(n, 1, 3);
            int[][] e = T.randTreeEdges(n);
            List<List<Integer>> tg = new ArrayList<>();
            for (int i = 0; i < n; i++) tg.add(new ArrayList<>());
            for (int[] x : e) { tg.get(x[0]).add(x[1]); tg.get(x[1]).add(x[0]); }
            int c = n;
            for (int a = 0; a < n; a++) {
                int[] mx = new int[n];
                Arrays.fill(mx, -1);
                Deque<Integer> q = new ArrayDeque<>();
                q.add(a);
                mx[a] = vals[a];
                while (!q.isEmpty()) { int u = q.poll(); for (int v : tg.get(u)) if (mx[v] == -1) { mx[v] = Math.max(mx[u], vals[v]); q.add(v); } }
                for (int b = a + 1; b < n; b++) if (vals[a] == vals[b] && mx[b] == vals[a]) c++;
            }
            T.eq("2421 rand", new LC2421().numberOfGoodPaths(vals, e), c);
        }
        T.summary("Day17");
    }
}
