import java.util.*;

public class P09Scc {
    //@tpl kosaraju
    // Kosaraju, iterative. comp[v] = id of v's strongly connected component.
    // Ids come out in topological order: every edge u -> v has comp[u] <= comp[v].
    static int[] scc(List<Integer>[] g) {
        int n = g.length;
        List<Integer>[] rg = new List[n];               // the reversed graph
        for (int v = 0; v < n; v++) rg[v] = new ArrayList<>();
        for (int u = 0; u < n; u++) for (int v : g[u]) rg[v].add(u);
        // 1. DFS on g; order[] lists vertices by finish time
        int[] order = new int[n], next = new int[n];    // next[u] = next edge to try
        boolean[] seen = new boolean[n];
        int done = 0;
        for (int s = 0; s < n; s++) {
            if (seen[s]) continue;
            Deque<Integer> st = new ArrayDeque<>();
            st.push(s);
            seen[s] = true;
            while (!st.isEmpty()) {
                int u = st.peek();
                if (next[u] < g[u].size()) {
                    int v = g[u].get(next[u]++);
                    if (!seen[v]) { seen[v] = true; st.push(v); }
                } else {
                    order[done++] = st.pop();          // all of u's edges are done
                }
            }
        }
        // 2. Latest finisher first, flood rg; one flood = one component
        int[] comp = new int[n];
        Arrays.fill(comp, -1);
        int c = 0;
        for (int i = n - 1; i >= 0; i--) {
            if (comp[order[i]] != -1) continue;
            Deque<Integer> st = new ArrayDeque<>();
            st.push(order[i]);
            comp[order[i]] = c;
            while (!st.isEmpty())
                for (int v : rg[st.pop()])
                    if (comp[v] == -1) { comp[v] = c; st.push(v); }
            c++;
        }
        return comp;
    }
    //@end

    //@tpl functional
    // Longest cycle (LC 2360). Each node has at most one out-edge, so every
    // SCC with 2+ nodes is one cycle, found by walking. stamp[v] = global
    // step when v was first visited (0 = never).
    static int longestCycle(int[] edges) {
        int n = edges.length, best = -1, clock = 1;
        int[] stamp = new int[n];
        for (int s = 0; s < n; s++) {
            int start = clock, u = s;                   // this walk: stamps >= start
            while (u != -1 && stamp[u] == 0) {
                stamp[u] = clock++;
                u = edges[u];
            }
            if (u != -1 && stamp[u] >= start)           // ran into its own path
                best = Math.max(best, clock - stamp[u]);
        }
        return best;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // LC 2876 with the condensation: answer(SCC) = size + answer(the SCC it points to)
    static int[] countVisitedNodes(List<Integer> edges) {
        int n = edges.size();
        List<Integer>[] g = new List[n];
        for (int v = 0; v < n; v++) { g[v] = new ArrayList<>(); g[v].add(edges.get(v)); }
        int[] comp = scc(g);
        int c = 0;
        for (int x : comp) c = Math.max(c, x + 1);
        int[] size = new int[c], next = new int[c], ans = new int[c];
        Arrays.fill(next, -1);
        for (int v = 0; v < n; v++) {
            size[comp[v]]++;
            int w = comp[edges.get(v)];
            if (w != comp[v]) next[comp[v]] = w;
        }
        for (int i = c - 1; i >= 0; i--) ans[i] = size[i] + (next[i] == -1 ? 0 : ans[next[i]]);
        int[] res = new int[n];
        for (int v = 0; v < n; v++) res[v] = ans[comp[v]];
        return res;
    }

    // LC 2360 straight from scc(): the largest SCC with at least 2 nodes
    static int longestCycleScc(int[] edges) {
        int n = edges.length;
        List<Integer>[] g = new List[n];
        for (int v = 0; v < n; v++) { g[v] = new ArrayList<>(); if (edges[v] != -1) g[v].add(edges[v]); }
        int[] comp = scc(g), size = new int[n];
        int best = -1;
        for (int v = 0; v < n; v++) size[comp[v]]++;
        for (int s : size) if (s >= 2) best = Math.max(best, s);
        return best;
    }

    static boolean[][] reach(List<Integer>[] g) {
        int n = g.length;
        boolean[][] r = new boolean[n][n];
        for (int s = 0; s < n; s++) {
            Deque<Integer> st = new ArrayDeque<>();
            st.push(s);
            r[s][s] = true;
            while (!st.isEmpty()) for (int v : g[st.pop()]) if (!r[s][v]) { r[s][v] = true; st.push(v); }
        }
        return r;
    }

    public static void main(String[] args) {
        Random rnd = new Random(31);
        for (int t = 0; t < 4000; t++) {
            int n = 1 + rnd.nextInt(9), m = rnd.nextInt(3 * n);
            List<Integer>[] g = new List[n];
            for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
            for (int i = 0; i < m; i++) g[rnd.nextInt(n)].add(rnd.nextInt(n));
            int[] comp = scc(g);
            boolean[][] r = reach(g);
            for (int u = 0; u < n; u++)
                for (int v = 0; v < n; v++)
                    Check.eq(r[u][v] && r[v][u], comp[u] == comp[v], "same scc " + u + "," + v);
            for (int u = 0; u < n; u++) for (int v : g[u]) Check.isTrue(comp[u] <= comp[v], "topological ids");
            int c = 0;
            for (int x : comp) c = Math.max(c, x + 1);
            boolean[] used = new boolean[c];
            for (int x : comp) used[x] = true;
            for (boolean b : used) Check.isTrue(b, "ids are 0..c-1");
        }
        for (int t = 0; t < 4000; t++) {
            int n = 2 + rnd.nextInt(9);
            int[] edges = new int[n];
            List<Integer> el = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                int v;
                do v = rnd.nextInt(n); while (v == i);
                edges[i] = rnd.nextInt(4) == 0 ? -1 : v;
                el.add(v);
            }
            // brute: walk n steps from each node; a cycle returns to its start
            int want = -1;
            for (int s = 0; s < n; s++) {
                int u = edges[s];
                for (int k = 1; k <= n && u != -1; k++, u = edges[u])
                    if (u == s) { want = Math.max(want, k); break; }
            }
            Check.eq(want, longestCycle(edges), "longest cycle " + Arrays.toString(edges));
            Check.eq(want, longestCycleScc(edges), "longest cycle via scc");
            int[] vis = new int[n];
            for (int s = 0; s < n; s++) {
                Set<Integer> seen = new HashSet<>();
                for (int u = s; seen.add(u); u = el.get(u)) { }
                vis[s] = seen.size();
            }
            Check.eq(vis, countVisitedNodes(el), "visited " + el);
        }
        // deep graph: one cycle through 200000 nodes (no recursion, so no stack issue)
        int big = 200_000;
        List<Integer>[] g = new List[big];
        for (int i = 0; i < big; i++) { g[i] = new ArrayList<>(); g[i].add((i + 1) % big); }
        int[] comp = scc(g);
        boolean one = true;
        for (int x : comp) one &= x == 0;
        Check.isTrue(one, "one big scc");
        Check.eq(3, longestCycle(new int[] {3, 3, 4, 2, 3}), "LC 2360 sample 1");
        Check.eq(-1, longestCycle(new int[] {2, -1, 3, 1}), "LC 2360 sample 2");
        Check.eq(new int[] {3, 3, 3, 4}, countVisitedNodes(Arrays.asList(1, 2, 0, 0)), "LC 2876 sample 1");
        Check.eq(new int[] {5, 5, 5, 5, 5}, countVisitedNodes(Arrays.asList(1, 2, 3, 4, 0)), "LC 2876 sample 2");
        Check.done("P09Scc");
    }
}
