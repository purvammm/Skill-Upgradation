import java.util.*;

public class P09TopoSort {
    //@tpl kahn
    // Kahn's algorithm, edges u -> v in g[u]. An order shorter than n = a cycle.
    static List<Integer> topoOrder(List<Integer>[] g) {
        int n = g.length;
        int[] indeg = new int[n];
        for (List<Integer> out : g) for (int v : out) indeg[v]++;
        Deque<Integer> q = new ArrayDeque<>();
        for (int v = 0; v < n; v++) if (indeg[v] == 0) q.add(v);   // 1. sources
        List<Integer> order = new ArrayList<>();
        while (!q.isEmpty()) {
            int u = q.poll();
            order.add(u);                     // 2. u's prerequisites are all out
            for (int v : g[u])
                if (--indeg[v] == 0) q.add(v);    // 3. v's last one is out
        }
        return order;
    }

    static List<Integer>[] graph(int n) {
        List<Integer>[] g = new List[n];
        for (int v = 0; v < n; v++) g[v] = new ArrayList<>();
        return g;
    }

    // Course schedule II: {a, b} means take b before a, so the edge is b -> a.
    static int[] findOrder(int n, int[][] prerequisites) {
        List<Integer>[] g = graph(n);
        for (int[] p : prerequisites) g[p[1]].add(p[0]);
        List<Integer> order = topoOrder(g);
        if (order.size() < n) return new int[0];      // a cycle: no order
        return order.stream().mapToInt(Integer::intValue).toArray();
    }
    //@end

    //@tpl matrix
    // Build a matrix with conditions: one order for rows, one for columns.
    static int[][] buildMatrix(int k, int[][] rowConds, int[][] colConds) {
        int[] row = position(k, rowConds), col = position(k, colConds);
        if (row == null || col == null) return new int[0][0];
        int[][] m = new int[k][k];
        for (int v = 0; v < k; v++) m[row[v]][col[v]] = v + 1;
        return m;
    }

    // pos[v] = place of value v + 1 in a topological order; null on a cycle.
    static int[] position(int k, int[][] conds) {
        List<Integer>[] g = graph(k);
        for (int[] e : conds) g[e[0] - 1].add(e[1] - 1);  // values are 1..k
        List<Integer> order = topoOrder(g);
        if (order.size() < k) return null;
        int[] pos = new int[k];
        for (int i = 0; i < k; i++) pos[order.get(i)] = i;
        return pos;
    }
    //@end

    //@tpl groups
    // Sort items by groups: order the groups, order the items, then output each
    // group's items in item order. An item with group -1 gets a group of its own.
    static int[] sortItems(int n, int m, int[] group, List<List<Integer>> before) {
        for (int i = 0; i < n; i++) if (group[i] == -1) group[i] = m++;
        List<Integer>[] items = graph(n), groups = graph(m);
        for (int i = 0; i < n; i++)
            for (int b : before.get(i)) {
                items[b].add(i);
                if (group[b] != group[i]) groups[group[b]].add(group[i]);
            }
        List<Integer> itemOrder = topoOrder(items), groupOrder = topoOrder(groups);
        if (itemOrder.size() < n || groupOrder.size() < m) return new int[0];
        List<Integer>[] members = graph(m);
        for (int i : itemOrder) members[group[i]].add(i);
        int[] out = new int[n];
        int k = 0;
        for (int gr : groupOrder) for (int i : members[gr]) out[k++] = i;
        return out;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean acyclic(int n, int[][] edges) {          // Floyd-Warshall closure
        boolean[][] reach = new boolean[n][n];
        for (int[] e : edges) reach[e[0]][e[1]] = true;
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                if (reach[i][k])
                    for (int j = 0; j < n; j++) if (reach[k][j]) reach[i][j] = true;
        for (int i = 0; i < n; i++) if (reach[i][i]) return false;
        return true;
    }

    static int[][] randomEdges(Random r, int n, int m, int base) {
        boolean dag = r.nextBoolean();
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;
        for (int i = n - 1; i > 0; i--) { int j = r.nextInt(i + 1), t = perm[i]; perm[i] = perm[j]; perm[j] = t; }
        int[][] e = new int[m][];
        for (int i = 0; i < m; i++) {
            int a = r.nextInt(n), b = r.nextInt(n);
            if (dag) {
                if (a == b) b = (a + 1) % n;
                if (a > b) { int t = a; a = b; b = t; }
                if (n == 1) { a = 0; b = 0; }
                a = perm[a]; b = perm[b];
            }
            e[i] = new int[] {a + base, b + base};
        }
        if (dag && n == 1) e = new int[0][];
        return e;
    }

    // Is there any valid order of items? Try every permutation (n <= 6).
    static boolean bruteItemsExist(int n, int[] group, List<List<Integer>> before) {
        int[] p = new int[n];
        for (int i = 0; i < n; i++) p[i] = i;
        return anyPerm(p, 0, group, before);
    }

    static boolean anyPerm(int[] p, int i, int[] group, List<List<Integer>> before) {
        if (i == p.length) return validItems(p, group, before);
        for (int j = i; j < p.length; j++) {
            int t = p[i]; p[i] = p[j]; p[j] = t;
            boolean ok = anyPerm(p, i + 1, group, before);
            t = p[i]; p[i] = p[j]; p[j] = t;
            if (ok) return true;
        }
        return false;
    }

    // group is the original array (with -1 for "no group").
    static boolean validItems(int[] out, int[] group, List<List<Integer>> before) {
        int n = group.length;
        if (out.length != n) return false;
        int[] pos = new int[n];
        Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) { if (out[i] < 0 || out[i] >= n || pos[out[i]] != -1) return false; pos[out[i]] = i; }
        for (int i = 0; i < n; i++) for (int b : before.get(i)) if (pos[b] > pos[i]) return false;
        // items of one group are next to each other
        Set<Integer> closed = new HashSet<>();
        for (int i = 0; i < n; i++) {
            int gr = group[out[i]];
            if (gr == -1) continue;
            if (closed.contains(gr)) return false;
            if (i + 1 == n || group[out[i + 1]] != gr) closed.add(gr);
        }
        return true;
    }

    public static void main(String[] args) {
        Random r = new Random(97);
        int ok = 0;
        for (int t = 0; t < 4000; t++) {
            int n = 1 + r.nextInt(9);
            int[][] edges = randomEdges(r, n, r.nextInt(2 * n + 1), 0);
            boolean dag = acyclic(n, edges);
            // topoOrder on the raw edges
            List<Integer>[] g = graph(n);
            for (int[] e : edges) g[e[0]].add(e[1]);
            List<Integer> order = topoOrder(g);
            Check.eq(dag, order.size() == n, "cycle iff fewer than n " + Check.str(edges));
            // findOrder: {a, b} means b first
            int[][] pre = new int[edges.length][];
            for (int i = 0; i < edges.length; i++) pre[i] = new int[] {edges[i][1], edges[i][0]};
            int[] fo = findOrder(n, pre);
            if (!dag) Check.eq(0, fo.length, "findOrder cycle");
            else {
                ok++;
                Check.eq(n, fo.length, "findOrder length");
                int[] pos = new int[n];
                Arrays.fill(pos, -1);
                for (int i = 0; i < n; i++) { Check.isTrue(pos[fo[i]] == -1, "permutation"); pos[fo[i]] = i; }
                for (int[] e : edges) Check.isTrue(pos[e[0]] < pos[e[1]], "edge respected " + Arrays.toString(e));
            }
            // buildMatrix, values 1..k
            int k = n;
            int[][] rc = randomEdges(r, k, r.nextInt(2 * k + 1), 1), cc = randomEdges(r, k, r.nextInt(2 * k + 1), 1);
            int[][] rc0 = new int[rc.length][], cc0 = new int[cc.length][];
            for (int i = 0; i < rc.length; i++) rc0[i] = new int[] {rc[i][0] - 1, rc[i][1] - 1};
            for (int i = 0; i < cc.length; i++) cc0[i] = new int[] {cc[i][0] - 1, cc[i][1] - 1};
            int[][] mat = buildMatrix(k, rc, cc);
            if (!acyclic(k, rc0) || !acyclic(k, cc0)) Check.eq(0, mat.length, "matrix impossible");
            else {
                Check.eq(k, mat.length, "matrix size");
                int[] rowOf = new int[k + 1], colOf = new int[k + 1];
                Arrays.fill(rowOf, -1);
                int cnt = 0;
                for (int i = 0; i < k; i++)
                    for (int j = 0; j < k; j++)
                        if (mat[i][j] != 0) {
                            Check.isTrue(rowOf[mat[i][j]] == -1, "value once");
                            rowOf[mat[i][j]] = i; colOf[mat[i][j]] = j; cnt++;
                        }
                Check.eq(k, cnt, "all values placed");
                for (int[] e : rc) Check.isTrue(rowOf[e[0]] < rowOf[e[1]], "row condition");
                for (int[] e : cc) Check.isTrue(colOf[e[0]] < colOf[e[1]], "col condition");
            }
            // sortItems, n <= 6
            int ni = 1 + r.nextInt(6), m = 1 + r.nextInt(3);
            int[] group = new int[ni];
            for (int i = 0; i < ni; i++) group[i] = r.nextInt(m + 1) - 1;
            List<List<Integer>> before = new ArrayList<>();
            int dens = r.nextInt(30);
            for (int i = 0; i < ni; i++) {
                before.add(new ArrayList<>());
                for (int b = 0; b < ni; b++) if (b != i && r.nextInt(100) < dens) before.get(i).add(b);
            }
            boolean exists = bruteItemsExist(ni, group, before);
            int[] got = sortItems(ni, m, group.clone(), before);
            if (!exists) Check.eq(0, got.length, "items impossible");
            else Check.isTrue(validItems(got, group, before), "items valid " + Arrays.toString(got));
        }
        Check.isTrue(ok > 1000 && ok < 3000, "both answers are common: " + ok);
        int[] s2 = findOrder(4, new int[][] {{1, 0}, {2, 0}, {3, 1}, {3, 2}});
        Check.isTrue(Arrays.equals(s2, new int[] {0, 1, 2, 3}) || Arrays.equals(s2, new int[] {0, 2, 1, 3}), "LC 210 sample 2");
        Check.eq(new int[] {0, 1}, findOrder(2, new int[][] {{1, 0}}), "LC 210 sample 1");
        Check.eq(new int[] {0}, findOrder(1, new int[0][]), "LC 210 sample 3");
        int[][] bm = buildMatrix(3, new int[][] {{1, 2}, {3, 2}}, new int[][] {{2, 1}, {3, 2}});
        Check.eq(3, bm.length, "LC 2392 sample 1");
        Check.eq(0, buildMatrix(3, new int[][] {{1, 2}, {2, 3}, {3, 1}, {2, 3}}, new int[][] {{2, 1}}).length,
                 "LC 2392 sample 2");
        int[] g1 = {-1, -1, 1, 0, 0, 1, 0, -1};
        List<List<Integer>> b1 = Arrays.asList(Arrays.<Integer>asList(), Arrays.asList(6), Arrays.asList(5),
                Arrays.asList(6), Arrays.asList(3, 6), Arrays.<Integer>asList(), Arrays.<Integer>asList(),
                Arrays.<Integer>asList());
        int[] o1 = sortItems(8, 2, g1.clone(), b1);
        Check.isTrue(validItems(o1, g1, b1), "LC 1203 sample 1 " + Arrays.toString(o1));
        List<List<Integer>> b2 = Arrays.asList(Arrays.<Integer>asList(), Arrays.asList(6), Arrays.asList(5),
                Arrays.asList(6), Arrays.asList(3), Arrays.<Integer>asList(), Arrays.asList(4),
                Arrays.<Integer>asList());
        Check.eq(0, sortItems(8, 2, g1.clone(), b2).length, "LC 1203 sample 2");
        Check.done("P09TopoSort");
    }
}
