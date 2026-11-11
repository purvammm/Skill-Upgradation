import java.util.*;

public class P09EulerPath {
    //@tpl main
    // Hierholzer: a path in directed graph g that uses every edge once, or null.
    // Start at the vertex with out - in = 1 if there is one (else any with edges).
    static List<Integer> eulerPath(List<Integer>[] g) {
        int n = g.length, edges = 0, start = -1, plus = 0, minus = 0;
        int[] in = new int[n];
        for (int u = 0; u < n; u++) for (int v : g[u]) { in[v]++; edges++; }
        for (int u = 0; u < n; u++) {
            int d = g[u].size() - in[u];
            if (d == 1) { plus++; start = u; }
            else if (d == -1) minus++;
            else if (d != 0) return null;
        }
        if (plus != minus || plus > 1) return null;     // degrees rule it out
        for (int u = 0; u < n && start == -1; u++) if (!g[u].isEmpty()) start = u;
        if (start == -1) return new ArrayList<>();      // no edges at all
        int[] next = new int[n];                        // next unused edge of u
        Deque<Integer> st = new ArrayDeque<>();
        List<Integer> path = new ArrayList<>();
        st.push(start);
        while (!st.isEmpty()) {
            int u = st.peek();
            if (next[u] < g[u].size()) st.push(g[u].get(next[u]++));   // walk on
            else path.add(st.pop());                    // stuck: u goes last
        }
        Collections.reverse(path);
        return path.size() == edges + 1 ? path : null;  // edges left: not connected
    }
    //@end

    //@tpl itinerary
    // Reconstruct itinerary (LC 332): each airport hands out tickets smallest first.
    static List<String> findItinerary(List<List<String>> tickets) {
        Map<String, PriorityQueue<String>> g = new HashMap<>();
        for (List<String> t : tickets)
            g.computeIfAbsent(t.get(0), k -> new PriorityQueue<>()).add(t.get(1));
        LinkedList<String> route = new LinkedList<>();
        Deque<String> st = new ArrayDeque<>();
        st.push("JFK");
        while (!st.isEmpty()) {
            PriorityQueue<String> out = g.get(st.peek());
            if (out != null && !out.isEmpty()) st.push(out.poll());
            else route.addFirst(st.pop());              // stuck: fix it at the front
        }
        return route;
    }
    //@end

    //@tpl safe
    // Cracking the safe (LC 753): node = last n-1 digits as a number; typing d
    // moves u to (u * k + d) % size. Each of the k^n passwords is one edge.
    static String crackSafe(int n, int k) {
        int size = 1;
        for (int i = 1; i < n; i++) size *= k;          // k^(n-1) nodes
        int[] nextDigit = new int[size];
        Deque<int[]> st = new ArrayDeque<>();           // {node, digit typed}
        StringBuilder digits = new StringBuilder();
        st.push(new int[] {0, -1});
        while (!st.isEmpty()) {
            int u = st.peek()[0];
            if (nextDigit[u] < k) {
                int d = nextDigit[u]++;
                st.push(new int[] {(u * k + d) % size, d});
            } else {
                int d = st.pop()[1];
                if (d >= 0) digits.append((char) ('0' + d));
            }
        }
        for (int i = 1; i < n; i++) digits.append('0'); // start node 00..0
        return digits.reverse().toString();
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // LC 2097 through eulerPath: compress the values, then read pairs off the path.
    static int[][] validArrangement(int[][] pairs) {
        int[] vals = new int[2 * pairs.length];
        for (int i = 0; i < pairs.length; i++) { vals[2 * i] = pairs[i][0]; vals[2 * i + 1] = pairs[i][1]; }
        int[] sorted = vals.clone();
        Arrays.sort(sorted);
        int m = 0;
        for (int i = 0; i < sorted.length; i++) if (i == 0 || sorted[i] != sorted[i - 1]) sorted[m++] = sorted[i];
        List<Integer>[] g = new List[m];
        for (int i = 0; i < m; i++) g[i] = new ArrayList<>();
        for (int[] p : pairs) g[Arrays.binarySearch(sorted, 0, m, p[0])].add(Arrays.binarySearch(sorted, 0, m, p[1]));
        List<Integer> path = eulerPath(g);
        int[][] ans = new int[pairs.length][];
        for (int i = 0; i < pairs.length; i++) ans[i] = new int[] {sorted[path.get(i)], sorted[path.get(i + 1)]};
        return ans;
    }

    static boolean usesEveryEdgeOnce(List<Integer>[] g, List<Integer> path) {
        Map<Long, Integer> cnt = new HashMap<>();
        int m = 0;
        for (int u = 0; u < g.length; u++) for (int v : g[u]) { cnt.merge((long) u * 1000 + v, 1, Integer::sum); m++; }
        if (path.size() != m + 1) return false;
        for (int i = 0; i + 1 < path.size(); i++) {
            Integer c = cnt.get((long) path.get(i) * 1000 + path.get(i + 1));
            if (c == null || c == 0) return false;
            cnt.put((long) path.get(i) * 1000 + path.get(i + 1), c - 1);
        }
        return true;
    }

    // backtracking: does any Euler path exist?
    static boolean bruteExists(int[][] edges) {
        if (edges.length == 0) return true;
        boolean[] used = new boolean[edges.length];
        for (int[] e : edges) if (extend(edges, used, e[0], 0)) return true;
        return false;
    }

    static boolean extend(int[][] edges, boolean[] used, int u, int cnt) {
        if (cnt == edges.length) return true;
        for (int i = 0; i < edges.length; i++) {
            if (used[i] || edges[i][0] != u) continue;
            used[i] = true;
            if (extend(edges, used, edges[i][1], cnt + 1)) { used[i] = false; return true; }
            used[i] = false;
        }
        return false;
    }

    // LC 332 brute: DFS over destinations in sorted order; the first full route wins.
    static List<String> bruteItinerary(List<List<String>> tickets) {
        List<List<String>> ts = new ArrayList<>(tickets);
        ts.sort((a, b) -> a.get(1).compareTo(b.get(1)));
        List<String> route = new ArrayList<>();
        route.add("JFK");
        boolean[] used = new boolean[ts.size()];
        return go(ts, used, route) ? route : null;
    }

    static boolean go(List<List<String>> ts, boolean[] used, List<String> route) {
        if (route.size() == ts.size() + 1) return true;
        String u = route.get(route.size() - 1);
        for (int i = 0; i < ts.size(); i++) {
            if (used[i] || !ts.get(i).get(0).equals(u)) continue;
            used[i] = true;
            route.add(ts.get(i).get(1));
            if (go(ts, used, route)) return true;
            route.remove(route.size() - 1);
            used[i] = false;
        }
        return false;
    }

    public static void main(String[] args) {
        Random r = new Random(37);
        int found = 0;
        for (int t = 0; t < 5000; t++) {
            int n = 1 + r.nextInt(5), m = r.nextInt(8);
            List<Integer>[] g = new List[n];
            for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
            int[][] edges = new int[m][];
            if (t % 2 == 0) {                                // a random walk: a path exists
                int u = r.nextInt(n);
                for (int i = 0; i < m; i++) { int v = r.nextInt(n); edges[i] = new int[] {u, v}; u = v; }
            } else {
                for (int i = 0; i < m; i++) edges[i] = new int[] {r.nextInt(n), r.nextInt(n)};
            }
            for (int[] e : edges) g[e[0]].add(e[1]);
            for (List<Integer> l : g) Collections.shuffle(l, r);
            List<Integer> p = eulerPath(g);
            boolean exists = bruteExists(edges);
            Check.eq(exists, p != null, "exists " + Arrays.deepToString(edges));
            if (p != null && m == 0) Check.isTrue(p.isEmpty(), "no edges: empty path");
            else if (p != null) { found++; Check.isTrue(usesEveryEdgeOnce(g, p), "valid path " + p); }
        }
        Check.isTrue(found > 2000, "enough graphs with a path");

        String[] airports = {"JFK", "AAA", "ATL", "SFO", "MUC"};
        for (int t = 0; t < 3000; t++) {
            int m = 1 + r.nextInt(7);
            List<List<String>> tickets = new ArrayList<>();
            int u = 0;
            for (int i = 0; i < m; i++) {
                int v;
                do v = r.nextInt(airports.length); while (v == u);
                tickets.add(Arrays.asList(airports[u], airports[v]));
                u = v;
            }
            Collections.shuffle(tickets, r);
            Check.eq(bruteItinerary(tickets), findItinerary(tickets), "itinerary " + tickets);
        }

        for (int t = 0; t < 2000; t++) {
            int m = 1 + r.nextInt(8);
            int[][] pairs = new int[m][];
            int u = r.nextInt(1_000_000_000);
            for (int i = 0; i < m; i++) {
                int v = r.nextInt(5) == 0 ? r.nextInt(1_000_000_000) : r.nextInt(6);
                pairs[i] = new int[] {u, v};
                u = v;
            }
            Collections.shuffle(Arrays.asList(pairs), r);
            int[][] got = validArrangement(pairs);
            Check.eq(m, got.length, "pairs length");
            for (int i = 0; i + 1 < m; i++) Check.eq(got[i][1], got[i + 1][0], "pairs chain");
            List<String> a = new ArrayList<>(), b = new ArrayList<>();
            for (int[] p : pairs) a.add(p[0] + "," + p[1]);
            for (int[] p : got) b.add(p[0] + "," + p[1]);
            Check.eq(Check.sortedCopy(a), Check.sortedCopy(b), "pairs used once");
        }

        for (int n = 1; n <= 4; n++)
            for (int k = 1; k <= 10; k++) {
                int total = 1;
                for (int i = 0; i < n; i++) total *= k;
                if (total > 4096) continue;
                String s = crackSafe(n, k);
                Check.eq(total + n - 1, s.length(), "safe length n=" + n + " k=" + k);
                Set<String> seen = new HashSet<>();
                for (int i = 0; i + n <= s.length(); i++) seen.add(s.substring(i, i + n));
                Check.eq(total, seen.size(), "every password n=" + n + " k=" + k);
            }

        Check.eq(Arrays.asList("JFK", "MUC", "LHR", "SFO", "SJC"), findItinerary(Arrays.asList(
            Arrays.asList("MUC", "LHR"), Arrays.asList("JFK", "MUC"), Arrays.asList("SFO", "SJC"),
            Arrays.asList("LHR", "SFO"))), "LC 332 sample 1");
        Check.eq(Arrays.asList("JFK", "ATL", "JFK", "SFO", "ATL", "SFO"), findItinerary(Arrays.asList(
            Arrays.asList("JFK", "SFO"), Arrays.asList("JFK", "ATL"), Arrays.asList("SFO", "ATL"),
            Arrays.asList("ATL", "JFK"), Arrays.asList("ATL", "SFO"))), "LC 332 sample 2");
        Check.eq(Arrays.asList("JFK", "NRT", "JFK", "KUL"), findItinerary(Arrays.asList(
            Arrays.asList("JFK", "KUL"), Arrays.asList("JFK", "NRT"), Arrays.asList("NRT", "JFK"))), "dead end first");
        Check.eq(new int[][] {{11, 9}, {9, 4}, {4, 5}, {5, 1}},
            validArrangement(new int[][] {{5, 1}, {4, 5}, {11, 9}, {9, 4}}), "LC 2097 sample 1");
        String s1 = crackSafe(1, 2), s2 = crackSafe(2, 2);
        Check.isTrue(s1.equals("01") || s1.equals("10"), "LC 753 sample 1: " + s1);
        Check.isTrue(s2.length() == 5 && s2.contains("00") && s2.contains("01") && s2.contains("10")
            && s2.contains("11"), "LC 753 sample 2: " + s2);
        // long path: no recursion, so 10^5 edges are fine on the default stack
        int big = 100_000;
        List<Integer>[] g = new List[big];
        for (int i = 0; i < big; i++) { g[i] = new ArrayList<>(); g[i].add((i + 1) % big); }
        Check.eq(big + 1, eulerPath(g).size(), "big circuit");
        Check.done("P09EulerPath");
    }
}
