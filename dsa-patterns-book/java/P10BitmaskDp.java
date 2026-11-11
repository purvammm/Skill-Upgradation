import java.util.*;

public class P10BitmaskDp {
    //@tpl sessions
    // Fewest work sessions of length T that finish every task (n <= 16).
    // For the tasks in mask: cnt = sessions opened, fill = time used in the
    // last one. Keep the smallest (cnt, fill) pair: fewer sessions first.
    static int minSessions(int[] tasks, int T) {
        int n = tasks.length, full = (1 << n) - 1;
        int[] cnt = new int[1 << n], fill = new int[1 << n];
        Arrays.fill(cnt, Integer.MAX_VALUE);
        cnt[0] = 1;                                      // one empty session open
        for (int mask = 0; mask < full; mask++)          // masks only grow
            for (int i = 0; i < n; i++) {
                if ((mask >> i & 1) == 1) continue;      // task i is done
                int c = cnt[mask], f = fill[mask] + tasks[i];
                if (f > T) { c++; f = tasks[i]; }        // open a new session
                int nm = mask | 1 << i;
                if (c < cnt[nm] || (c == cnt[nm] && f < fill[nm])) {
                    cnt[nm] = c;
                    fill[nm] = f;
                }
            }
        return cnt[full];
    }

    // k equal-sum subsets: k sessions of length sum / k must hold everything.
    static boolean canPartitionKSubsets(int[] nums, int k) {
        int sum = 0, max = 0;
        for (int x : nums) { sum += x; max = Math.max(max, x); }
        if (sum % k != 0 || max > sum / k) return false;
        return minSessions(nums, sum / k) == k;
    }
    //@end

    //@tpl tsp
    // Shortest superstring (TSP shape). dp[mask][last] = most chars saved by
    // overlaps, over every order of the words in mask that ends with last.
    static String shortestSuperstring(String[] w) {
        int n = w.length, full = (1 << n) - 1;
        int[][] ov = new int[n][n];               // end of w[i] = start of w[j]
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                for (int k = Math.min(w[i].length(), w[j].length()); k > 0; k--)
                    if (i != j && w[i].endsWith(w[j].substring(0, k))) {
                        ov[i][j] = k;
                        break;
                    }
        int[][] dp = new int[1 << n][n], par = new int[1 << n][n];
        for (int[] row : dp) Arrays.fill(row, -1);
        for (int i = 0; i < n; i++) dp[1 << i][i] = 0;
        for (int mask = 1; mask <= full; mask++)
            for (int last = 0; last < n; last++) {
                if (dp[mask][last] < 0) continue;        // last is not in mask
                for (int j = 0; j < n; j++) {
                    int nm = mask | 1 << j, v = dp[mask][last] + ov[last][j];
                    if (nm != mask && v > dp[nm][j]) {   // j is new: append it
                        dp[nm][j] = v;
                        par[nm][j] = last;
                    }
                }
            }
        int last = 0;
        for (int i = 1; i < n; i++) if (dp[full][i] > dp[full][last]) last = i;
        int[] order = new int[n];                 // walk the parents back
        for (int pos = n - 1, mask = full; pos >= 0; pos--) {
            order[pos] = last;
            int prev = par[mask][last];
            mask ^= 1 << last;
            last = prev;
        }
        StringBuilder sb = new StringBuilder(w[order[0]]);
        for (int t = 1; t < n; t++)
            sb.append(w[order[t]].substring(ov[order[t - 1]][order[t]]));
        return sb.toString();
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // assign each task to session 0..used (canonical numbering), all ways
    static int bruteSessions(int[] a, int T, int i, int[] load, int used) {
        if (i == a.length) return used;
        int best = Integer.MAX_VALUE;
        for (int s = 0; s <= used && s < a.length; s++) {
            if (load[s] + a[i] > T) continue;
            load[s] += a[i];
            best = Math.min(best, bruteSessions(a, T, i + 1, load, Math.max(used, s + 1)));
            load[s] -= a[i];
        }
        return best;
    }

    static boolean bruteK(int[] a, int k) {
        int sum = 0;
        for (int x : a) sum += x;
        if (sum % k != 0) return false;
        int total = 1;
        for (int i = 0; i < a.length; i++) total *= k;
        for (int code = 0; code < total; code++) {
            int[] s = new int[k];
            int c = code;
            for (int x : a) { s[c % k] += x; c /= k; }
            boolean ok = true;
            for (int v : s) ok &= v == sum / k;
            if (ok) return true;
        }
        return false;
    }

    static int bruteSuper(String[] w) {
        int n = w.length;
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;
        int best = Integer.MAX_VALUE;
        do {
            String s = w[perm[0]];
            for (int t = 1; t < n; t++) {
                String x = w[perm[t]];
                int k = Math.min(s.length(), x.length());
                while (k > 0 && !s.endsWith(x.substring(0, k))) k--;
                s = s + x.substring(k);
            }
            best = Math.min(best, s.length());
        } while (nextPerm(perm));
        return best;
    }

    static boolean nextPerm(int[] a) {
        int i = a.length - 2;
        while (i >= 0 && a[i] >= a[i + 1]) i--;
        if (i < 0) return false;
        int j = a.length - 1;
        while (a[j] <= a[i]) j--;
        int t = a[i]; a[i] = a[j]; a[j] = t;
        for (int l = i + 1, r = a.length - 1; l < r; l++, r--) { t = a[l]; a[l] = a[r]; a[r] = t; }
        return true;
    }

    static String[] randomWords(Random r, int n) {
        List<String> out = new ArrayList<>();
        int guard = 0;
        while (out.size() < n && guard++ < 1000) {
            String x = Check.str(r, 1 + r.nextInt(4), "ab");
            boolean ok = true;
            for (String y : out) if (y.contains(x) || x.contains(y)) ok = false;
            if (ok) out.add(x);
        }
        return out.toArray(new String[0]);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            int[] tasks = {1, 2, 3};
            int n = 3, T = 3;
            int[] cnt = new int[1 << n], fill = new int[1 << n];
            Arrays.fill(cnt, Integer.MAX_VALUE);
            cnt[0] = 1;
            for (int mask = 0; mask < (1 << n) - 1; mask++)
                for (int i = 0; i < n; i++) {
                    if ((mask >> i & 1) == 1) continue;
                    int c = cnt[mask], f = fill[mask] + tasks[i];
                    if (f > T) { c++; f = tasks[i]; }
                    int nm = mask | 1 << i;
                    System.out.println("  " + mask + " +" + tasks[i] + " -> " + nm + " (" + c + "," + f + ")");
                    if (c < cnt[nm] || (c == cnt[nm] && f < fill[nm])) { cnt[nm] = c; fill[nm] = f; }
                }
            for (int m = 0; m < 1 << n; m++)
                System.out.println(String.format("%3s", Integer.toBinaryString(m)).replace(' ', '0') + " (" + cnt[m] + ", " + fill[m] + ")");
            System.out.println(shortestSuperstring(new String[] {"catg", "ctaagt", "gcta", "ttca", "atgcatc"}));
            return;
        }
        Random r = new Random(50);
        for (int t = 0; t < 1500; t++) {
            int n = 1 + r.nextInt(8);
            int T = 1 + r.nextInt(10);
            int[] a = Check.ints(r, n, 1, T);
            Check.eq(bruteSessions(a, T, 0, new int[n], 0), minSessions(a, T), "sessions " + Arrays.toString(a) + " T=" + T);
            int k = 1 + r.nextInt(4);
            int[] b = Check.ints(r, 1 + r.nextInt(7), 1, 6);
            Check.eq(bruteK(b, k), canPartitionKSubsets(b, k), "k-part " + Arrays.toString(b) + " k=" + k);
        }
        for (int t = 0; t < 800; t++) {
            String[] w = randomWords(r, 1 + r.nextInt(6));
            String got = shortestSuperstring(w);
            for (String x : w) Check.isTrue(got.contains(x), "super contains " + x + " in " + got);
            Check.eq(bruteSuper(w), got.length(), "super len " + Arrays.toString(w));
        }
        Check.eq(2, minSessions(new int[] {1, 2, 3}, 3), "LC 1986 sample 1");
        Check.eq(2, minSessions(new int[] {3, 1, 3, 1, 1}, 8), "LC 1986 sample 2");
        Check.eq(1, minSessions(new int[] {1, 2, 3, 4, 5}, 15), "LC 1986 sample 3");
        Check.eq(true, canPartitionKSubsets(new int[] {4, 3, 2, 3, 5, 2, 1}, 4), "LC 698 sample 1");
        Check.eq(false, canPartitionKSubsets(new int[] {1, 2, 3, 4}, 3), "LC 698 sample 2");
        String s1 = shortestSuperstring(new String[] {"alex", "loves", "leetcode"});
        Check.eq(17, s1.length(), "LC 943 sample 1");
        String s2 = shortestSuperstring(new String[] {"catg", "ctaagt", "gcta", "ttca", "atgcatc"});
        Check.eq("gctaagttcatgcatc".length(), s2.length(), "LC 943 sample 2");
        for (String x : new String[] {"catg", "ctaagt", "gcta", "ttca", "atgcatc"})
            Check.isTrue(s2.contains(x), "LC 943 sample 2 contains " + x);
        Check.done("P10BitmaskDp");
    }
}
