import java.util.*;
import java.io.*;

/** The Java kit page: collection idioms every template uses, plus a test of Main.java. */
public class P00JavaKit {

    @SuppressWarnings("unchecked")
    static void idioms() {
        int[] a = {5, 1, 4};
        int[][] iv = {{2, 1}, {1, 5}, {2, 9}};
        String[] words = {"b", "a", "b"};
        int n = 3, rows = 4, cols = 5;
        int[][] dp = new int[2][3];

        //@tpl idioms
        // Heaps: min-heap by default; max-heap and custom orders take a comparator
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        PriorityQueue<int[]> byFirst =
                new PriorityQueue<>((p, q) -> Integer.compare(p[0], q[0]));

        // Sort rows by column 0, ties by column 1 descending (never p[0] - q[0])
        Arrays.sort(iv, (p, q) -> p[0] != q[0] ? Integer.compare(p[0], q[0])
                                               : Integer.compare(q[1], p[1]));
        // iv -> [[1, 5], [2, 9], [2, 1]]

        // Sort int[] descending: sort ascending, then reverse in place
        Arrays.sort(a);
        for (int i = 0, j = a.length - 1; i < j; i++, j--) {
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }                                                     // a -> [5, 4, 1]

        // Count and group
        Map<String, Integer> cnt = new HashMap<>();
        for (String w : words) cnt.merge(w, 1, Integer::sum);  // {a=1, b=2}
        int bs = cnt.getOrDefault("b", 0);                     // 2
        Map<Integer, List<Integer>> groups = new HashMap<>();
        groups.computeIfAbsent(7, k -> new ArrayList<>()).add(1);

        // Adjacency list for nodes 0..n-1
        List<Integer>[] adj = new List[n];
        for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();

        // ArrayDeque is both the stack and the queue
        Deque<Integer> st = new ArrayDeque<>();
        st.push(1); st.push(2);                                // top is 2
        int top = st.pop();                                    // 2
        Deque<Integer> q = new ArrayDeque<>();
        q.offer(1); q.offer(2);
        int front = q.poll();                                  // 1

        // Sorted map: nearest keys in O(log n)
        TreeMap<Integer, String> tm = new TreeMap<>();
        tm.put(10, "x"); tm.put(20, "y");
        Integer fl = tm.floorKey(15), ce = tm.ceilingKey(15);  // 10, 20

        // Fill a 2D table, encode a grid cell as one int
        for (int[] row : dp) Arrays.fill(row, -1);
        int r = 2, c = 3, id = r * cols + c;                   // 13
        int r0 = id / cols, c0 = id % cols;                    // 2, 3
        //@end

        maxHeap.add(3); maxHeap.add(9); maxHeap.add(1);
        Check.eq(9, (long) maxHeap.peek(), "max heap");
        byFirst.add(new int[] {4}); byFirst.add(new int[] {2});
        Check.eq(2, byFirst.peek()[0], "byFirst");
        Check.eq(new int[][] {{1, 5}, {2, 9}, {2, 1}}, iv, "2D sort");
        Check.eq(new int[] {5, 4, 1}, a, "descending");
        Check.eq("{a=1, b=2}", new TreeMap<>(cnt).toString(), "merge");
        Check.eq(2, bs, "getOrDefault");
        Check.eq("[1]", groups.get(7).toString(), "computeIfAbsent");
        Check.eq(3, adj.length, "adj");
        Check.eq(2, top, "stack");
        Check.eq(1, front, "queue");
        Check.eq(10, (long) fl, "floor");
        Check.eq(20, (long) ce, "ceiling");
        Check.eq(new int[][] {{-1, -1, -1}, {-1, -1, -1}}, dp, "fill");
        Check.eq(13, id, "encode");
        Check.eq(2, r0, "decode r");
        Check.eq(3, c0, "decode c");
        Check.eq(4, rows, "rows");
    }

    static String runMain(String input) throws IOException {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes("UTF-8")));
            System.setOut(new PrintStream(bo, true, "UTF-8"));
            Main.main(new String[0]);
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
        return bo.toString("UTF-8").replace("\r\n", "\n");
    }

    public static void main(String[] args) throws IOException {
        idioms();
        Check.eq("6\n-5\n", runMain("2\n3\n1 2 3\n2\n-10   5\n"), "Main sample");
        Random r = new Random(5);
        for (int t = 0; t < 300; t++) {
            int cases = 1 + r.nextInt(4);
            StringBuilder in = new StringBuilder().append(cases).append('\n');
            StringBuilder want = new StringBuilder();
            for (int k = 0; k < cases; k++) {
                int n = r.nextInt(6);
                in.append(n).append(r.nextBoolean() ? "\n" : "  ");
                long s = 0;
                for (int i = 0; i < n; i++) {
                    long x = r.nextInt(2_000_001) - 1_000_000L;
                    if (r.nextInt(5) == 0) x *= 1_000_000L;
                    s += x;
                    in.append(x).append(r.nextBoolean() ? " " : "\n");
                }
                want.append(s).append('\n');
            }
            Check.eq(want.toString(), runMain(in.toString()), "random input " + t);
        }
        Main.FastReader fr = new Main.FastReader(new ByteArrayInputStream("  hello\n world 42".getBytes("UTF-8")));
        Check.eq("hello", fr.next(), "next word 1");
        Check.eq("world", fr.next(), "next word 2");
        Check.eq(42, fr.nextInt(), "int after words");
        Check.done("P00JavaKit");
    }
}
