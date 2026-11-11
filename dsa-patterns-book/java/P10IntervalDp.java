import java.util.*;

public class P10IntervalDp {
    //@tpl balloons
    // Burst balloons. Pad with a 1 at both ends. dp[l][r] = most coins from
    // bursting every balloon strictly between l and r. k is the LAST one to
    // burst, so at that moment its neighbours are l and r.
    static int maxCoins(int[] nums) {
        int n = nums.length + 2;
        int[] a = new int[n];
        a[0] = a[n - 1] = 1;
        for (int i = 0; i < nums.length; i++) a[i + 1] = nums[i];
        int[][] dp = new int[n][n];
        for (int len = 2; len < n; len++)                // len = r - l
            for (int l = 0; l + len < n; l++) {
                int r = l + len;
                for (int k = l + 1; k < r; k++)
                    dp[l][r] = Math.max(dp[l][r],
                                        dp[l][k] + a[l] * a[k] * a[r] + dp[k][r]);
            }
        return dp[0][n - 1];
    }
    //@end

    //@tpl stick
    // Minimum cost to cut a stick; a cut costs the length of the piece it cuts.
    // p = 0, the sorted cuts, n. dp[i][j] = cheapest way to make every cut
    // strictly between p[i] and p[j]; the first one costs p[j] - p[i].
    static int minCost(int n, int[] cuts) {
        int m = cuts.length + 2;
        int[] p = new int[m];
        for (int i = 0; i < cuts.length; i++) p[i + 1] = cuts[i];
        p[m - 1] = n;
        Arrays.sort(p);
        int[][] dp = new int[m][m];
        for (int len = 2; len < m; len++)
            for (int i = 0; i + len < m; i++) {
                int j = i + len;
                dp[i][j] = Integer.MAX_VALUE;
                for (int k = i + 1; k < j; k++)          // k = the first cut
                    dp[i][j] = Math.min(dp[i][j], dp[i][k] + dp[k][j]);
                dp[i][j] += p[j] - p[i];
            }
        return dp[0][m - 1];
    }
    //@end

    //@tpl printer
    // Strange printer: dp[l][r] = fewest turns to print s[l..r]. Either s[r]
    // gets its own turn, or the turn that prints some s[k] == s[r] runs on to r.
    static int strangePrinter(String s) {
        int n = s.length();
        int[][] dp = new int[n][n];                      // dp[r][r-1] = 0: empty
        for (int l = n - 1; l >= 0; l--) {
            dp[l][l] = 1;
            for (int r = l + 1; r < n; r++) {
                dp[l][r] = dp[l][r - 1] + 1;
                for (int k = l; k < r; k++)
                    if (s.charAt(k) == s.charAt(r))
                        dp[l][r] = Math.min(dp[l][r], dp[l][k] + dp[k + 1][r - 1]);
            }
        }
        return dp[0][n - 1];
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int bruteCoins(List<Integer> a) {
        int best = 0;
        for (int i = 0; i < a.size(); i++) {
            int left = i > 0 ? a.get(i - 1) : 1, right = i + 1 < a.size() ? a.get(i + 1) : 1;
            List<Integer> b = new ArrayList<>(a);
            b.remove(i);
            best = Math.max(best, left * a.get(i) * right + bruteCoins(b));
        }
        return best;
    }

    // try every order of cuts; each costs the length of the piece holding it
    static int bruteStick(int n, int[] cuts, boolean[] used, TreeSet<Integer> done) {
        int best = Integer.MAX_VALUE;
        boolean any = false;
        for (int i = 0; i < cuts.length; i++) {
            if (used[i]) continue;
            any = true;
            int lo = done.lower(cuts[i]), hi = done.higher(cuts[i]);
            used[i] = true;
            done.add(cuts[i]);
            best = Math.min(best, hi - lo + bruteStick(n, cuts, used, done));
            done.remove(cuts[i]);
            used[i] = false;
        }
        return any ? best : 0;
    }

    // BFS over every paper state; '.' is blank
    static int brutePrinter(String s) {
        int n = s.length();
        char[] alpha = "abc".toCharArray();
        StringBuilder blank = new StringBuilder();
        for (int i = 0; i < n; i++) blank.append('.');
        Map<String, Integer> dist = new HashMap<>();
        Deque<String> q = new ArrayDeque<>();
        dist.put(blank.toString(), 0);
        q.add(blank.toString());
        while (!q.isEmpty()) {
            String x = q.poll();
            int d = dist.get(x);
            if (x.equals(s)) return d;
            for (int i = 0; i < n; i++)
                for (int j = i; j < n; j++)
                    for (char c : alpha) {
                        char[] y = x.toCharArray();
                        for (int t = i; t <= j; t++) y[t] = c;
                        String ys = new String(y);
                        if (!dist.containsKey(ys)) { dist.put(ys, d + 1); q.add(ys); }
                    }
        }
        return -1;
    }

    // trap: l ascending instead of by length
    static int coinsWrongOrder(int[] nums) {
        int n = nums.length + 2;
        int[] a = new int[n];
        a[0] = a[n - 1] = 1;
        for (int i = 0; i < nums.length; i++) a[i + 1] = nums[i];
        int[][] dp = new int[n][n];
        for (int l = 0; l < n; l++)
            for (int r = l + 2; r < n; r++)
                for (int k = l + 1; k < r; k++)
                    dp[l][r] = Math.max(dp[l][r], dp[l][k] + a[l] * a[k] * a[r] + dp[k][r]);
        return dp[0][n - 1];
    }

    // trap: k as the FIRST balloon, neighbours a[k-1], a[k+1]
    static int coinsFirst(int[] nums) {
        int n = nums.length + 2;
        int[] a = new int[n];
        a[0] = a[n - 1] = 1;
        for (int i = 0; i < nums.length; i++) a[i + 1] = nums[i];
        int[][] dp = new int[n][n];
        for (int len = 2; len < n; len++)
            for (int l = 0; l + len < n; l++) {
                int r = l + len;
                for (int k = l + 1; k < r; k++)
                    dp[l][r] = Math.max(dp[l][r], dp[l][k] + a[k - 1] * a[k] * a[k + 1] + dp[k][r]);
            }
        return dp[0][n - 1];
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            int[] nums = {3, 1, 5, 8};
            int n = nums.length + 2;
            int[] a = new int[n];
            a[0] = a[n - 1] = 1;
            for (int i = 0; i < nums.length; i++) a[i + 1] = nums[i];
            System.out.println("pad " + Arrays.toString(a));
            for (int l = 0; l < n; l++) {
                StringBuilder sb = new StringBuilder(String.format("%d  ", l));
                for (int r = 2; r < n; r++) {
                    if (r - l < 2) { sb.append("     "); continue; }
                    sb.append(String.format("%5d", sub(a, l, r)));
                }
                System.out.println(sb);
            }
            for (int k = 1; k < n - 1; k++)
                System.out.println("k=" + k + " a[k]=" + a[k] + ": " + sub(a, 0, k) + " + " + (a[0] * a[k] * a[n - 1]) + " + " + sub(a, k, n - 1)
                                   + " = " + (sub(a, 0, k) + a[0] * a[k] * a[n - 1] + sub(a, k, n - 1)));
            System.out.println("wrong order: " + coinsWrongOrder(nums) + "  first: " + coinsFirst(nums));
            return;
        }
        Random r = new Random(30);
        for (int t = 0; t < 1500; t++) {
            int n = r.nextInt(7);
            int[] nums = Check.ints(r, n, 0, 9);
            List<Integer> lst = new ArrayList<>();
            for (int x : nums) lst.add(x);
            Check.eq(bruteCoins(lst), maxCoins(nums), "coins " + Arrays.toString(nums));
            int len = 2 + r.nextInt(12);
            Set<Integer> cs = new HashSet<>();
            int want = r.nextInt(Math.min(6, len - 1) + 1);
            while (cs.size() < want) cs.add(1 + r.nextInt(len - 1));
            int[] cuts = new int[cs.size()];
            int ci = 0;
            for (int c : cs) cuts[ci++] = c;
            TreeSet<Integer> done = new TreeSet<>();
            done.add(0);
            done.add(len);
            Check.eq(bruteStick(len, cuts, new boolean[cuts.length], done), minCost(len, cuts.clone()),
                     "stick " + len + " " + Arrays.toString(cuts));
        }
        for (int t = 0; t < 300; t++) {
            String s = Check.str(r, 1 + r.nextInt(5), r.nextBoolean() ? "ab" : "abc");
            Check.eq(brutePrinter(s), strangePrinter(s), "printer " + s);
        }
        Check.eq(167, maxCoins(new int[] {3, 1, 5, 8}), "LC 312 sample 1");
        Check.eq(10, maxCoins(new int[] {1, 5}), "LC 312 sample 2");
        Check.eq(16, minCost(7, new int[] {1, 3, 4, 5}), "LC 1547 sample 1");
        Check.eq(22, minCost(9, new int[] {5, 6, 1, 4, 2}), "LC 1547 sample 2");
        Check.eq(2, strangePrinter("aaabbb"), "LC 664 sample 1");
        Check.eq(2, strangePrinter("aba"), "LC 664 sample 2");
        Check.done("P10IntervalDp");
    }

    static int sub(int[] a, int l, int r) {
        // dp[l][r] by running maxCoins on the inside, with a[l], a[r] as borders
        int n = r - l + 1;
        int[][] dp = new int[n][n];
        for (int len = 2; len < n; len++)
            for (int x = 0; x + len < n; x++) {
                int y = x + len;
                for (int k = x + 1; k < y; k++)
                    dp[x][y] = Math.max(dp[x][y], dp[x][k] + a[l + x] * a[l + k] * a[l + y] + dp[k][y]);
            }
        return dp[0][n - 1];
    }
}
