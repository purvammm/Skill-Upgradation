import java.util.*;

public class P09Dsu {
    //@tpl dsu
    static class DSU {
        int[] parent, size;
        int count;                                // number of sets
        DSU(int n) {
            parent = new int[n];
            size = new int[n];
            count = n;
            for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
        }

        // Root of x's set. Path compression: point every node passed at the root.
        int find(int x) {
            return parent[x] == x ? x : (parent[x] = find(parent[x]));
        }

        // Join the sets of a and b; false if they were already one set.
        boolean union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) return false;
            if (size[ra] < size[rb]) { int t = ra; ra = rb; rb = t; }
            parent[rb] = ra;                      // smaller tree under the bigger
            size[ra] += size[rb];
            count--;
            return true;
        }
    }
    //@end

    //@tpl uses
    // Redundant connection: the first edge whose two ends are already joined.
    static int[] findRedundantConnection(int[][] edges) {
        DSU d = new DSU(edges.length + 1);        // nodes 1..n and n = edges
        for (int[] e : edges)
            if (!d.union(e[0], e[1])) return e;
        return new int[0];
    }

    // Equality equations: join every "a==b" first, then test every "a!=b".
    static boolean equationsPossible(String[] equations) {
        DSU d = new DSU(26);
        for (String e : equations)
            if (e.charAt(1) == '=') d.union(e.charAt(0) - 'a', e.charAt(3) - 'a');
        for (String e : equations) {
            int a = e.charAt(0) - 'a', b = e.charAt(3) - 'a';
            if (e.charAt(1) == '!' && d.find(a) == d.find(b)) return false;
        }
        return true;
    }
    //@end

    //@tpl accounts
    // Accounts merge: join two accounts when they share an email.
    static List<List<String>> accountsMerge(List<List<String>> acc) {
        DSU d = new DSU(acc.size());
        Map<String, Integer> firstOwner = new HashMap<>();   // email -> account
        for (int i = 0; i < acc.size(); i++)
            for (String email : acc.get(i).subList(1, acc.get(i).size())) {
                Integer j = firstOwner.putIfAbsent(email, i);
                if (j != null) d.union(i, j);
            }
        Map<Integer, TreeSet<String>> byRoot = new HashMap<>();
        for (Map.Entry<String, Integer> e : firstOwner.entrySet())
            byRoot.computeIfAbsent(d.find(e.getValue()), k -> new TreeSet<>())
                  .add(e.getKey());
        List<List<String>> out = new ArrayList<>();
        for (Map.Entry<Integer, TreeSet<String>> e : byRoot.entrySet()) {
            List<String> row = new ArrayList<>();
            row.add(acc.get(e.getKey()).get(0));         // the name
            row.addAll(e.getValue());                    // sorted emails
            out.add(row);
        }
        return out;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Brute force connectivity: label propagation over the edges so far.
    static int[] labels(int n, List<int[]> edges) {
        int[] lab = new int[n];
        for (int i = 0; i < n; i++) lab[i] = i;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int[] e : edges) {
                int m = Math.min(lab[e[0]], lab[e[1]]);
                if (lab[e[0]] != m || lab[e[1]] != m) { lab[e[0]] = m; lab[e[1]] = m; changed = true; }
            }
        }
        return lab;
    }

    static int[] bruteRedundant(int n, int[][] edges) {
        for (int skip = edges.length - 1; skip >= 0; skip--) {
            List<int[]> rest = new ArrayList<>();
            for (int i = 0; i < edges.length; i++) if (i != skip) rest.add(edges[i]);
            int[] lab = labels(n + 1, rest);
            boolean connected = true;
            for (int v = 1; v <= n; v++) if (lab[v] != lab[1]) connected = false;
            if (connected) return edges[skip];
        }
        return new int[0];
    }

    static boolean bruteEquations(String[] eqs) {
        boolean[][] same = new boolean[26][26];
        for (int i = 0; i < 26; i++) same[i][i] = true;
        for (String e : eqs)
            if (e.charAt(1) == '=') { same[e.charAt(0) - 'a'][e.charAt(3) - 'a'] = true; same[e.charAt(3) - 'a'][e.charAt(0) - 'a'] = true; }
        for (int k = 0; k < 26; k++)
            for (int i = 0; i < 26; i++)
                for (int j = 0; j < 26; j++) if (same[i][k] && same[k][j]) same[i][j] = true;
        for (String e : eqs)
            if (e.charAt(1) == '!' && same[e.charAt(0) - 'a'][e.charAt(3) - 'a']) return false;
        return true;
    }

    static String canonAccounts(List<List<String>> accs) {
        List<String> rows = new ArrayList<>();
        for (List<String> a : accs) {
            List<String> emails = new ArrayList<>(a.subList(1, a.size()));
            List<String> sorted = new ArrayList<>(emails);
            Collections.sort(sorted);
            if (!sorted.equals(emails)) return "UNSORTED " + a;
            rows.add(a.get(0) + ":" + emails);
        }
        Collections.sort(rows);
        return rows.toString();
    }

    public static void main(String[] args) {
        Random r = new Random(99);
        for (int t = 0; t < 2000; t++) {
            int n = 1 + r.nextInt(12);
            DSU d = new DSU(n);
            List<int[]> edges = new ArrayList<>();
            int ops = r.nextInt(2 * n + 1);
            for (int i = 0; i < ops; i++) {
                int a = r.nextInt(n), b = r.nextInt(n);
                int[] before = labels(n, edges);
                Check.eq(before[a] != before[b], d.union(a, b), "union result");
                edges.add(new int[] {a, b});
                int[] lab = labels(n, edges);
                Set<Integer> comps = new HashSet<>();
                Map<Integer, Integer> sizes = new HashMap<>();
                for (int x : lab) { comps.add(x); sizes.merge(x, 1, Integer::sum); }
                Check.eq(comps.size(), d.count, "count");
                for (int x = 0; x < n; x++) {
                    Check.eq((int) sizes.get(lab[x]), d.size[d.find(x)], "size of set");
                    int y = r.nextInt(n);
                    Check.eq(lab[x] == lab[y], d.find(x) == d.find(y), "same set");
                }
            }
            // redundant connection: a tree on 1..n plus one extra edge
            if (n >= 3) {
                List<int[]> es = new ArrayList<>();
                Set<Long> have = new HashSet<>();
                for (int[] e : Check.randomTree(r, n)) {
                    es.add(new int[] {e[0] + 1, e[1] + 1});
                    have.add((long) Math.min(e[0], e[1]) * 100 + Math.max(e[0], e[1]));
                }
                int a, b;
                do { a = r.nextInt(n); b = r.nextInt(n); }
                while (a == b || have.contains((long) Math.min(a, b) * 100 + Math.max(a, b)));
                es.add(new int[] {a + 1, b + 1});
                Collections.shuffle(es, r);
                for (int[] e : es) if (r.nextBoolean()) { int x = e[0]; e[0] = e[1]; e[1] = x; }
                int[][] ea = es.toArray(new int[0][]);
                Check.eq(bruteRedundant(n, ea), findRedundantConnection(ea), "redundant " + Check.str(ea));
            }
            // equations
            String[] eqs = new String[1 + r.nextInt(8)];
            for (int i = 0; i < eqs.length; i++)
                eqs[i] = "" + (char) ('a' + r.nextInt(5)) + (r.nextInt(3) == 0 ? "!=" : "==") + (char) ('a' + r.nextInt(5));
            Check.eq(bruteEquations(eqs), equationsPossible(eqs), "equations " + Arrays.toString(eqs));
            // accounts: random emails, names consistent per brute-force group
            int na = 1 + r.nextInt(6);
            List<List<String>> raw = new ArrayList<>();
            for (int i = 0; i < na; i++) {
                List<String> a = new ArrayList<>();
                a.add("?");
                int k = 1 + r.nextInt(3);
                for (int j = 0; j < k; j++) a.add("e" + r.nextInt(10) + "@x.com");
                raw.add(a);
            }
            List<int[]> share = new ArrayList<>();
            for (int i = 0; i < na; i++)
                for (int j = i + 1; j < na; j++)
                    for (String e : raw.get(i).subList(1, raw.get(i).size()))
                        if (raw.get(j).contains(e)) share.add(new int[] {i, j});
            int[] grp = labels(na, share);
            String[] names = {"Ann", "Bob", "Cy"};
            Map<Integer, String> nameOf = new HashMap<>();
            for (int i = 0; i < na; i++) raw.get(i).set(0, nameOf.computeIfAbsent(grp[i], x -> names[r.nextInt(3)]));
            List<List<String>> want = new ArrayList<>();
            for (int g = 0; g < na; g++) {
                TreeSet<String> em = new TreeSet<>();
                String name = null;
                for (int i = 0; i < na; i++)
                    if (grp[i] == g) { name = raw.get(i).get(0); em.addAll(raw.get(i).subList(1, raw.get(i).size())); }
                if (name == null) continue;
                List<String> row = new ArrayList<>();
                row.add(name);
                row.addAll(em);
                want.add(row);
            }
            Check.eq(canonAccounts(want), canonAccounts(accountsMerge(raw)), "accounts " + raw);
        }
        Check.eq(new int[] {2, 3}, findRedundantConnection(new int[][] {{1, 2}, {1, 3}, {2, 3}}), "LC 684 sample 1");
        Check.eq(new int[] {1, 4},
                 findRedundantConnection(new int[][] {{1, 2}, {2, 3}, {3, 4}, {1, 4}, {1, 5}}), "LC 684 sample 2");
        Check.eq(false, equationsPossible(new String[] {"a==b", "b!=a"}), "LC 990 sample 1");
        Check.eq(true, equationsPossible(new String[] {"b==a", "a==b"}), "LC 990 sample 2");
        List<List<String>> acc = Arrays.asList(
            Arrays.asList("John", "johnsmith@mail.com", "john_newyork@mail.com"),
            Arrays.asList("John", "johnsmith@mail.com", "john00@mail.com"),
            Arrays.asList("Mary", "mary@mail.com"),
            Arrays.asList("John", "johnnybravo@mail.com"));
        List<List<String>> exp = Arrays.asList(
            Arrays.asList("John", "john00@mail.com", "john_newyork@mail.com", "johnsmith@mail.com"),
            Arrays.asList("Mary", "mary@mail.com"),
            Arrays.asList("John", "johnnybravo@mail.com"));
        Check.eq(canonAccounts(exp), canonAccounts(accountsMerge(acc)), "LC 721 sample 1");
        // LC 547 with the DSU count
        int[][] isConnected = {{1, 1, 0}, {1, 1, 0}, {0, 0, 1}};
        DSU p = new DSU(3);
        for (int i = 0; i < 3; i++) for (int j = i + 1; j < 3; j++) if (isConnected[i][j] == 1) p.union(i, j);
        Check.eq(2, p.count, "LC 547 sample 1 via count");
        Check.done("P09Dsu");
    }
}
