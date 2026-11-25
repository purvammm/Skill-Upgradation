import java.util.*;

/*@core BFS on a grid, BFS by layers, adjacency lists
note: Mark a cell visited when you PUSH it, never when you pop it, or the same cell enters the queue many times. Process `q.size()` items per round when you need the layer number (minutes, steps).
@*/
class Core15 {
    static final int[] DR = {1, -1, 0, 0}, DC = {0, 0, 1, -1};

    static int stepsToTarget(char[][] g, int sr, int sc, int tr, int tc) {   // '#' = wall
        int m = g.length, n = g[0].length;
        boolean[][] seen = new boolean[m][n];
        Deque<int[]> q = new ArrayDeque<>();
        q.offer(new int[]{sr, sc});
        seen[sr][sc] = true;
        for (int steps = 0; !q.isEmpty(); steps++) {
            for (int size = q.size(); size > 0; size--) {          // one layer
                int[] c = q.poll();
                if (c[0] == tr && c[1] == tc) return steps;
                for (int d = 0; d < 4; d++) {
                    int r = c[0] + DR[d], col = c[1] + DC[d];
                    if (r < 0 || col < 0 || r >= m || col >= n || seen[r][col] || g[r][col] == '#') continue;
                    seen[r][col] = true;                           // mark on push
                    q.offer(new int[]{r, col});
                }
            }
        }
        return -1;
    }

    static List<List<Integer>> graph(int n, int[][] edges, boolean directed) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) {
            g.get(e[0]).add(e[1]);
            if (!directed) g.get(e[1]).add(e[0]);
        }
        return g;
    }
}

/*@LC 200
tag: Flood fill
cue: Count groups of connected '1' cells.
hook: Every unvisited land cell starts a new island; the BFS sinks the whole island.
time: O(m·n) time · O(m·n) space
trap: Sink (mark) a cell when you push it.
var: LC 547 Number of Provinces | The same count over an adjacency matrix (or use DSU).
@*/
class LC200 {
    public int numIslands(char[][] grid) {
        int m = grid.length, n = grid[0].length, islands = 0;
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) {
                if (grid[i][j] != '1') continue;
                islands++;                                            //* a new island
                grid[i][j] = '0';                                     //* sink on push
                Deque<int[]> q = new ArrayDeque<>();
                q.offer(new int[]{i, j});
                while (!q.isEmpty()) {
                    int[] c = q.poll();
                    for (int d = 0; d < 4; d++) {
                        int r = c[0] + dr[d], col = c[1] + dc[d];
                        if (r < 0 || col < 0 || r >= m || col >= n || grid[r][col] != '1') continue;
                        grid[r][col] = '0';                           //*
                        q.offer(new int[]{r, col});
                    }
                }
            }
        return islands;
    }
}

/*@var LC 695 · Max Area of Island (recursive DFS)
note: DFS returns the size of what it sinks. Fine for 50×50 grids; for big grids prefer the BFS above.
@*/
class LC200v1 {
    public int maxAreaOfIsland(int[][] grid) {
        int best = 0;
        for (int i = 0; i < grid.length; i++)
            for (int j = 0; j < grid[0].length; j++) best = Math.max(best, dfs(grid, i, j));
        return best;
    }

    private int dfs(int[][] g, int r, int c) {
        if (r < 0 || c < 0 || r >= g.length || c >= g[0].length || g[r][c] != 1) return 0;
        g[r][c] = 0;
        return 1 + dfs(g, r + 1, c) + dfs(g, r - 1, c) + dfs(g, r, c + 1) + dfs(g, r, c - 1);
    }
}

/*@LC 994
tag: Multi-source BFS by layers
cue: Rot spreads one step per minute from every rotten orange; minutes until none is fresh.
hook: Put every source in the queue first; one layer is one minute.
time: O(m·n) time · O(m·n) space
trap: Count fresh oranges first; if some remain at the end, return −1. No fresh at all → 0.
@*/
class LC994 {
    public int orangesRotting(int[][] grid) {
        int m = grid.length, n = grid[0].length, fresh = 0, minutes = 0;
        Deque<int[]> q = new ArrayDeque<>();
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 2) q.offer(new int[]{i, j});          //* all sources at once
                else if (grid[i][j] == 1) fresh++;
            }
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        while (!q.isEmpty() && fresh > 0) {
            minutes++;
            for (int size = q.size(); size > 0; size--) {               //* one layer = one minute
                int[] c = q.poll();
                for (int d = 0; d < 4; d++) {
                    int r = c[0] + dr[d], col = c[1] + dc[d];
                    if (r < 0 || col < 0 || r >= m || col >= n || grid[r][col] != 1) continue;
                    grid[r][col] = 2;
                    fresh--;
                    q.offer(new int[]{r, col});
                }
            }
        }
        return fresh == 0 ? minutes : -1;                               //*
    }
}

/*@var LC 542 · 01 Matrix (distance to the nearest 0)
note: Multi-source BFS from every 0; `dist = −1` marks "not reached yet".
@*/
class LC994v1 {
    public int[][] updateMatrix(int[][] mat) {
        int m = mat.length, n = mat[0].length;
        int[][] dist = new int[m][n];
        Deque<int[]> q = new ArrayDeque<>();
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) {
                if (mat[i][j] == 0) q.offer(new int[]{i, j});
                else dist[i][j] = -1;
            }
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        while (!q.isEmpty()) {
            int[] c = q.poll();
            for (int d = 0; d < 4; d++) {
                int r = c[0] + dr[d], col = c[1] + dc[d];
                if (r < 0 || col < 0 || r >= m || col >= n || dist[r][col] != -1) continue;
                dist[r][col] = dist[c[0]][c[1]] + 1;
                q.offer(new int[]{r, col});
            }
        }
        return dist;
    }
}

/*@LC 207
tag: Kahn's topological sort
cue: Can all courses be finished given prerequisite pairs?
hook: Start with no-prerequisite courses; finishing one frees the courses waiting on it.
time: O(V + E) time · O(V + E) space
trap: The pair is [a, b] = "b before a", so the edge goes b → a.
var: LC 802 Find Eventual Safe States | Reverse the edges, run Kahn from the terminal nodes, sort what you reach.
@*/
class LC207 {
    public boolean canFinish(int numCourses, int[][] prerequisites) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) g.add(new ArrayList<>());
        int[] indeg = new int[numCourses];
        for (int[] p : prerequisites) { g.get(p[1]).add(p[0]); indeg[p[0]]++; }   //* b -> a
        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < numCourses; i++) if (indeg[i] == 0) q.offer(i);       //*
        int done = 0;
        while (!q.isEmpty()) {
            int u = q.poll();
            done++;
            for (int v : g.get(u)) if (--indeg[v] == 0) q.offer(v);               //*
        }
        return done == numCourses;                                                //* a cycle is left over
    }
}

/*@var LC 210 · Course Schedule II (return an order)
note: The same Kahn loop; write each popped course into the answer.
@*/
class LC207v1 {
    public int[] findOrder(int numCourses, int[][] prerequisites) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) g.add(new ArrayList<>());
        int[] indeg = new int[numCourses];
        for (int[] p : prerequisites) { g.get(p[1]).add(p[0]); indeg[p[0]]++; }
        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < numCourses; i++) if (indeg[i] == 0) q.offer(i);
        int[] order = new int[numCourses];
        int done = 0;
        while (!q.isEmpty()) {
            int u = q.poll();
            order[done++] = u;
            for (int v : g.get(u)) if (--indeg[v] == 0) q.offer(v);
        }
        return done == numCourses ? order : new int[0];
    }
}

/*@LC 1091
tag: BFS · 8 directions
cue: Shortest clear path from top-left to bottom-right moving in 8 directions.
hook: BFS by layers; the path length counts cells, so the start is 1.
time: O(n²) time · O(n²) space
trap: If the start or the end cell is blocked, return −1 immediately.
@*/
class LC1091 {
    public int shortestPathBinaryMatrix(int[][] grid) {
        int n = grid.length;
        if (grid[0][0] == 1 || grid[n - 1][n - 1] == 1) return -1;         //*
        Deque<int[]> q = new ArrayDeque<>();
        q.offer(new int[]{0, 0});
        grid[0][0] = 1;                                                     // visited
        for (int dist = 1; !q.isEmpty(); dist++) {                          //* counts cells
            for (int size = q.size(); size > 0; size--) {
                int[] c = q.poll();
                if (c[0] == n - 1 && c[1] == n - 1) return dist;
                for (int dr = -1; dr <= 1; dr++)
                    for (int dc = -1; dc <= 1; dc++) {                      //* 8 neighbours
                        int r = c[0] + dr, col = c[1] + dc;
                        if (r < 0 || col < 0 || r >= n || col >= n || grid[r][col] != 0) continue;
                        grid[r][col] = 1;
                        q.offer(new int[]{r, col});
                    }
            }
        }
        return -1;
    }
}

/*@var LC 1293 · Shortest Path with Obstacles Elimination (BFS over states)
note: When the rules carry a budget, the state is (row, col, budget left). Same BFS, a 3-D visited array.
@*/
class LC1091v1 {
    public int shortestPath(int[][] grid, int k) {
        int m = grid.length, n = grid[0].length;
        boolean[][][] seen = new boolean[m][n][k + 1];
        Deque<int[]> q = new ArrayDeque<>();
        q.offer(new int[]{0, 0, k});
        seen[0][0][k] = true;
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        for (int steps = 0; !q.isEmpty(); steps++) {
            for (int size = q.size(); size > 0; size--) {
                int[] c = q.poll();
                if (c[0] == m - 1 && c[1] == n - 1) return steps;
                for (int d = 0; d < 4; d++) {
                    int r = c[0] + dr[d], col = c[1] + dc[d];
                    if (r < 0 || col < 0 || r >= m || col >= n) continue;
                    int left = c[2] - grid[r][col];                   // a wall uses one removal
                    if (left < 0 || seen[r][col][left]) continue;
                    seen[r][col][left] = true;
                    q.offer(new int[]{r, col, left});
                }
            }
        }
        return -1;
    }
}

/*@LC 785
tag: BFS two-colouring
cue: Can the nodes be split into two sides with every edge crossing?
hook: Colour the start 1, neighbours −1; a neighbour with my colour means an odd cycle.
time: O(V + E) time · O(V) space
trap: The graph may be disconnected: start a BFS from every uncoloured node.
var: LC 886 Possible Bipartition | Build the graph from the dislike pairs (1-indexed), then this code.
@*/
class LC785 {
    public boolean isBipartite(int[][] graph) {
        int n = graph.length;
        int[] color = new int[n];                                     // 0 = none, 1 / -1 = sides
        for (int s = 0; s < n; s++) {
            if (color[s] != 0) continue;                              //*
            color[s] = 1;
            Deque<Integer> q = new ArrayDeque<>();
            q.offer(s);
            while (!q.isEmpty()) {
                int u = q.poll();
                for (int v : graph[u]) {
                    if (color[v] == color[u]) return false;                      //*
                    if (color[v] == 0) { color[v] = -color[u]; q.offer(v); }     //*
                }
            }
        }
        return true;
    }
}

/*@LC 1368
tag: 0-1 BFS
cue: Each cell has an arrow; changing an arrow costs 1. Cheapest way to reach the corner.
hook: Following the arrow costs 0 → push to the FRONT; changing it costs 1 → push to the BACK.
time: O(m·n) time · O(m·n) space
trap: Relax with a `dist` array; a cell can be pushed more than once, and the check skips worse copies.
var: Dijkstra | Also correct, just a log factor slower.
@*/
class LC1368 {
    public int minCost(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[] dr = {0, 0, 1, -1}, dc = {1, -1, 0, 0};                // signs 1 right, 2 left, 3 down, 4 up
        int[][] dist = new int[m][n];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
        dist[0][0] = 0;
        Deque<int[]> dq = new ArrayDeque<>();
        dq.offerFirst(new int[]{0, 0});
        while (!dq.isEmpty()) {
            int[] c = dq.pollFirst();
            for (int d = 0; d < 4; d++) {
                int r = c[0] + dr[d], col = c[1] + dc[d];
                if (r < 0 || col < 0 || r >= m || col >= n) continue;
                int w = grid[c[0]][c[1]] == d + 1 ? 0 : 1;                 //* free along the arrow
                if (dist[c[0]][c[1]] + w < dist[r][col]) {
                    dist[r][col] = dist[c[0]][c[1]] + w;
                    if (w == 0) dq.offerFirst(new int[]{r, col});          //*
                    else dq.offerLast(new int[]{r, col});                  //*
                }
            }
        }
        return dist[m - 1][n - 1];
    }
}

/*@LC 2050
tag: Topological order + longest path DP
cue: Courses take time[i] months, unlimited in parallel; earliest finish of everything.
hook: A course finishes at its own time plus the slowest prerequisite.
time: O(V + E) time · O(V + E) space
trap: Input is 1-indexed. Courses with no prerequisites finish at `time[i]`.
@*/
class LC2050 {
    public int minimumTime(int n, int[][] relations, int[] time) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        int[] indeg = new int[n];
        for (int[] r : relations) { g.get(r[0] - 1).add(r[1] - 1); indeg[r[1] - 1]++; }
        int[] finish = new int[n];
        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) { q.offer(i); finish[i] = time[i]; }
        int ans = 0;
        while (!q.isEmpty()) {
            int u = q.poll();
            ans = Math.max(ans, finish[u]);
            for (int v : g.get(u)) {
                finish[v] = Math.max(finish[v], finish[u] + time[v]);       //* wait for the slowest
                if (--indeg[v] == 0) q.offer(v);
            }
        }
        return ans;
    }
}

/*@var LC 1857 · Largest Color Value in a Directed Graph
note: The same Kahn order carrying `cnt[v][26]`: the most nodes of each colour on any path ending at v. A cycle means −1.
@*/
class LC2050v1 {
    public int largestPathValue(String colors, int[][] edges) {
        int n = colors.length();
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        int[] indeg = new int[n];
        for (int[] e : edges) { g.get(e[0]).add(e[1]); indeg[e[1]]++; }
        int[][] cnt = new int[n][26];
        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) q.offer(i);
        int seen = 0, best = 0;
        while (!q.isEmpty()) {
            int u = q.poll();
            seen++;
            best = Math.max(best, ++cnt[u][colors.charAt(u) - 'a']);
            for (int v : g.get(u)) {
                for (int c = 0; c < 26; c++) cnt[v][c] = Math.max(cnt[v][c], cnt[u][c]);
                if (--indeg[v] == 0) q.offer(v);
            }
        }
        return seen == n ? best : -1;
    }
}

public class Day15 {
    public static void main(String[] args) {
        T.eq("core bfs", Core15.stepsToTarget(T.chars("..#", ".#.", "..."), 0, 0, 1, 2), 5);
        T.eq("core graph", Core15.graph(3, T.grid("[[0,1],[1,2]]"), false).get(1).size(), 2);

        T.eq("200a", new LC200().numIslands(T.chars("11110", "11010", "11000", "00000")), 1);
        T.eq("200b", new LC200().numIslands(T.chars("11000", "11000", "00100", "00011")), 3);
        T.eq("695a", new LC200v1().maxAreaOfIsland(T.grid("[[0,0,1,0,0,0,0,1,0,0,0,0,0],[0,0,0,0,0,0,0,1,1,1,0,0,0],[0,1,1,0,1,0,0,0,0,0,0,0,0],[0,1,0,0,1,1,0,0,1,0,1,0,0],[0,1,0,0,1,1,0,0,1,1,1,0,0],[0,0,0,0,0,0,0,0,0,0,1,0,0],[0,0,0,0,0,0,0,1,1,1,0,0,0],[0,0,0,0,0,0,0,1,1,0,0,0,0]]")), 6);
        T.eq("695b", new LC200v1().maxAreaOfIsland(T.grid("[[0,0,0,0,0,0,0,0]]")), 0);

        T.eq("994a", new LC994().orangesRotting(T.grid("[[2,1,1],[1,1,0],[0,1,1]]")), 4);
        T.eq("994b", new LC994().orangesRotting(T.grid("[[2,1,1],[0,1,1],[1,0,1]]")), -1);
        T.eq("994c", new LC994().orangesRotting(T.grid("[[0,2]]")), 0);
        T.eq("542", new LC994v1().updateMatrix(T.grid("[[0,0,0],[0,1,0],[1,1,1]]")), T.grid("[[0,0,0],[0,1,0],[1,2,1]]"));

        T.eq("207a", new LC207().canFinish(2, T.grid("[[1,0]]")), true);
        T.eq("207b", new LC207().canFinish(2, T.grid("[[1,0],[0,1]]")), false);
        T.eq("210a", new LC207v1().findOrder(2, T.grid("[[1,0]]")), T.ints("[0,1]"));
        T.eq("210b", new LC207v1().findOrder(2, T.grid("[[1,0],[0,1]]")), new int[0]);
        int[] ord = new LC207v1().findOrder(4, T.grid("[[1,0],[2,0],[3,1],[3,2]]"));
        int[] pos = new int[4];
        for (int i = 0; i < 4; i++) pos[ord[i]] = i;
        T.ok("210c", pos[0] < pos[1] && pos[0] < pos[2] && pos[1] < pos[3] && pos[2] < pos[3]);

        T.eq("1091a", new LC1091().shortestPathBinaryMatrix(T.grid("[[0,1],[1,0]]")), 2);
        T.eq("1091b", new LC1091().shortestPathBinaryMatrix(T.grid("[[0,0,0],[1,1,0],[1,1,0]]")), 4);
        T.eq("1091c", new LC1091().shortestPathBinaryMatrix(T.grid("[[1,0,0],[1,1,0],[1,1,0]]")), -1);
        T.eq("1091d", new LC1091().shortestPathBinaryMatrix(T.grid("[[0]]")), 1);
        T.eq("1293a", new LC1091v1().shortestPath(T.grid("[[0,0,0],[1,1,0],[0,0,0],[0,1,1],[0,0,0]]"), 1), 6);
        T.eq("1293b", new LC1091v1().shortestPath(T.grid("[[0,1,1],[1,1,1],[1,0,0]]"), 1), -1);

        T.eq("785a", new LC785().isBipartite(new int[][]{{1, 2, 3}, {0, 2}, {0, 1, 3}, {0, 2}}), false);
        T.eq("785b", new LC785().isBipartite(new int[][]{{1, 3}, {0, 2}, {1, 3}, {0, 2}}), true);
        T.eq("785c", new LC785().isBipartite(new int[][]{{}, {2}, {1}}), true);

        T.eq("1368a", new LC1368().minCost(T.grid("[[1,1,1,1],[2,2,2,2],[1,1,1,1],[2,2,2,2]]")), 3);
        T.eq("1368b", new LC1368().minCost(T.grid("[[1,1,3],[3,2,2],[1,1,4]]")), 0);
        T.eq("1368c", new LC1368().minCost(T.grid("[[1,2],[4,3]]")), 1);

        T.eq("2050a", new LC2050().minimumTime(3, T.grid("[[1,3],[2,3]]"), T.ints("[3,2,5]")), 8);
        T.eq("2050b", new LC2050().minimumTime(5, T.grid("[[1,5],[2,5],[3,5],[3,4],[4,5]]"), T.ints("[1,2,3,4,5]")), 12);
        T.eq("1857a", new LC2050v1().largestPathValue("abaca", T.grid("[[0,1],[0,2],[2,3],[3,4]]")), 3);
        T.eq("1857b", new LC2050v1().largestPathValue("a", T.grid("[[0,0]]")), -1);
        T.summary("Day15");
    }
}
