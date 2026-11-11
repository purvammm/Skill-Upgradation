import java.util.*;

/**
 * Tiny test helper shared by every pattern file. Each pattern's main() compares
 * the template against a brute force (or known answers) and finishes with done().
 * Run through tools/check_java.py; a run passes only if it prints "PASS <name> <n> checks".
 */
final class Check {
    static long checks = 0;

    private Check() {}

    static void fail(String msg) {
        System.out.println("FAIL " + msg);
        System.exit(1);
    }

    static void eq(long expected, long got, String ctx) {
        checks++;
        if (expected != got) fail(ctx + ": expected " + expected + " but got " + got);
    }

    static void eq(double expected, double got, double eps, String ctx) {
        checks++;
        if (Math.abs(expected - got) > eps) fail(ctx + ": expected " + expected + " but got " + got);
    }

    static void eq(Object expected, Object got, String ctx) {
        checks++;
        if (!Objects.deepEquals(expected, got)) {
            fail(ctx + ": expected " + str(expected) + " but got " + str(got));
        }
    }

    static void isTrue(boolean cond, String ctx) {
        checks++;
        if (!cond) fail(ctx);
    }

    static String str(Object o) {
        if (o instanceof int[]) return Arrays.toString((int[]) o);
        if (o instanceof long[]) return Arrays.toString((long[]) o);
        if (o instanceof char[]) return new String((char[]) o);
        if (o instanceof boolean[]) return Arrays.toString((boolean[]) o);
        if (o instanceof Object[]) return Arrays.deepToString((Object[]) o);
        return String.valueOf(o);
    }

    static void done(String name) {
        if (checks == 0) fail(name + ": no checks ran");
        System.out.println("PASS " + name + " " + checks + " checks");
    }

    // ---- random input helpers ----
    static int[] ints(Random r, int n, int lo, int hi) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = lo + r.nextInt(hi - lo + 1);
        return a;
    }

    static int[] sortedInts(Random r, int n, int lo, int hi) {
        int[] a = ints(r, n, lo, hi);
        Arrays.sort(a);
        return a;
    }

    static String str(Random r, int n, String alphabet) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return sb.toString();
    }

    static String sortedCopy(List<? extends Object> list) {
        List<String> s = new ArrayList<>();
        for (Object o : list) s.add(String.valueOf(o));
        Collections.sort(s);
        return s.toString();
    }

    /** Random undirected tree on n nodes as an edge list {u, v}. */
    static int[][] randomTree(Random r, int n) {
        int[][] e = new int[Math.max(0, n - 1)][];
        for (int v = 1; v < n; v++) e[v - 1] = new int[] {r.nextInt(v), v};
        return e;
    }

    /** List<List<Integer>> as a canonical sorted string, ignoring inner order too if asked. */
    static String canon(List<List<Integer>> lists, boolean sortInner) {
        List<String> out = new ArrayList<>();
        for (List<Integer> l : lists) {
            List<Integer> c = new ArrayList<>(l);
            if (sortInner) Collections.sort(c);
            out.add(c.toString());
        }
        Collections.sort(out);
        return out.toString();
    }
}
