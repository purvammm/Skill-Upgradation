import java.util.*;

public class P08TreeEdges {
    //@tpl adjacency
    // A general tree given as n and an edge list. Each edge goes into both
    // lists, because the input does not say which end is the parent.
    static List<Integer>[] adjacency(int n, int[][] edges) {
        List<Integer>[] g = new List[n];
        for (int v = 0; v < n; v++) g[v] = new ArrayList<>();
        for (int[] e : edges) { g[e[0]].add(e[1]); g[e[1]].add(e[0]); }
        return g;
    }
    //@end

    //@tpl walk
    // One iterative DFS fills parent[], depth[] and order[] (each node after
    // its parent). order[] read backwards puts children first: sizes add up.
    static int[] parent, depth, order, size;

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
    //@end

    //@tpl apples
    // Apples: edge (v, parent) is walked twice iff v's subtree has an apple.
    static int minTime(int n, int[][] edges, List<Boolean> hasApple) {
        walk(adjacency(n, edges), 0);
        boolean[] need = new boolean[n];
        int time = 0;
        for (int i = n - 1; i > 0; i--) {          // i > 0: order[0] is the root
            int v = order[i];
            if (hasApple.get(v)) need[v] = true;
            if (need[v]) {
                time += 2;
                need[parent[v]] = true;
            }
        }
        return time;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Fuel to the capital (a variant in the text): the size[v] people of v's
    // subtree all cross edge (v, parent), in ceil(size[v] / seats) cars.
    static long minimumFuelCost(int[][] roads, int seats) {
        int n = roads.length + 1;
        walk(adjacency(n, roads), 0);
        long fuel = 0;
        for (int v = 1; v < n; v++) fuel += (size[v] + seats - 1) / seats;
        return fuel;
    }

    static int[] rp, rd, rs;

    static void rec(List<Integer>[] g, int v, int p, int d) {
        rp[v] = p;
        rd[v] = d;
        rs[v] = 1;
        for (int u : g[v]) if (u != p) { rec(g, u, v, d + 1); rs[v] += rs[u]; }
    }

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
        Collections.shuffle(Arrays.asList(e), r);
        return e;
    }

    // apples: union of all root paths of apple nodes
    static int bruteApples(int n, int[][] edges, List<Boolean> has) {
        rp = new int[n]; rd = new int[n]; rs = new int[n];
        rec(adjacency(n, edges), 0, -1, 0);
        Set<Integer> used = new HashSet<>();
        for (int v = 0; v < n; v++) if (has.get(v)) for (int x = v; x != 0; x = rp[x]) used.add(x);
        return 2 * used.size();
    }

    static long recFuel(List<Integer>[] g, int v, int p, int seats, long[] fuel) {
        long people = 1;
        for (int u : g[v]) if (u != p) people += recFuel(g, u, v, seats, fuel);
        if (p >= 0) fuel[0] += (people + seats - 1) / seats;
        return people;
    }

    public static void main(String[] args) {
        Random r = new Random(89);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(30);
            int[][] e = shuffledTree(r, n);
            List<Integer>[] g = adjacency(n, e);
            int root = r.nextInt(n);
            walk(g, root);
            rp = new int[n]; rd = new int[n]; rs = new int[n];
            rec(g, root, -1, 0);
            Check.eq(rp, parent, "parent");
            Check.eq(rd, depth, "depth");
            Check.eq(rs, size, "size");
            int[] seen = new int[n];
            int[] at = new int[n];
            for (int i = 0; i < n; i++) { seen[order[i]]++; at[order[i]] = i; }
            for (int v = 0; v < n; v++) {
                Check.eq(1, seen[v], "order is a permutation");
                if (parent[v] >= 0) Check.isTrue(at[parent[v]] < at[v], "parent before child");
            }
            List<Boolean> has = new ArrayList<>();
            for (int v = 0; v < n; v++) has.add(r.nextInt(4) == 0);
            Check.eq(bruteApples(n, e, has), minTime(n, e, has), "apples");
            int seats = 1 + r.nextInt(5);
            long[] fuel = {0};
            recFuel(adjacency(n, e), 0, -1, seats, fuel);
            Check.eq(fuel[0], minimumFuelCost(e, seats), "fuel");
        }
        // n = 100000 path: the iterative walk has no recursion to overflow
        int big = 100000;
        int[][] chain = new int[big - 1][];
        for (int i = 0; i + 1 < big; i++) chain[i] = new int[] {i, i + 1};
        walk(adjacency(big, chain), 0);
        Check.eq(big - 1, depth[big - 1], "deep chain depth");
        Check.eq(big, size[0], "deep chain size");
        int[][] s = {{0, 1}, {0, 2}, {1, 4}, {1, 5}, {2, 3}, {2, 6}};
        Check.eq(8, minTime(7, s, Arrays.asList(false, false, true, false, true, true, false)), "LC 1443 sample 1");
        Check.eq(6, minTime(7, s, Arrays.asList(false, false, true, false, false, true, false)), "LC 1443 sample 2");
        Check.eq(0, minTime(7, s, Arrays.asList(false, false, false, false, false, false, false)), "LC 1443 sample 3");
        Check.eq(3L, minimumFuelCost(new int[][] {{0, 1}, {0, 2}, {0, 3}}, 5), "LC 2477 sample 1");
        Check.eq(7L, minimumFuelCost(new int[][] {{3, 1}, {3, 2}, {1, 0}, {0, 4}, {0, 5}, {4, 6}}, 2), "LC 2477 sample 2");
        Check.eq(0L, minimumFuelCost(new int[][] {}, 1), "LC 2477 sample 3");
        Check.done("P08TreeEdges");
    }
}
