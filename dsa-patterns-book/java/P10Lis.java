import java.util.*;

public class P10Lis {
    //@tpl tails
    // tails[k] = smallest last value of a valid subsequence of length k + 1.
    // strict (a[i] < a[j]): x replaces the first tails[k] >= x (lower bound);
    // non-decreasing (a[i] <= a[j]): the first tails[k] > x (upper bound).
    static int lis(int[] a, boolean strict) {
        int[] tails = new int[a.length];
        int len = 0;
        for (int x : a) {
            int lo = 0, hi = len;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (tails[mid] < x || (!strict && tails[mid] == x)) lo = mid + 1;
                else hi = mid;
            }
            tails[lo] = x;                             // smaller end for length lo+1
            if (lo == len) len++;                      // or a new, longer one
        }
        return len;
    }
    //@end

    //@tpl count
    // O(n^2): len[i] = longest strictly increasing subsequence ending at i,
    // cnt[i] = how many subsequences of that length end at i.
    static int findNumberOfLIS(int[] a) {
        int n = a.length, best = 0, total = 0;
        int[] len = new int[n], cnt = new int[n];
        for (int i = 0; i < n; i++) {
            len[i] = cnt[i] = 1;                       // a[i] alone
            for (int j = 0; j < i; j++) {
                if (a[j] >= a[i]) continue;
                if (len[j] + 1 > len[i]) { len[i] = len[j] + 1; cnt[i] = cnt[j]; }
                else if (len[j] + 1 == len[i]) cnt[i] += cnt[j];
            }
            if (len[i] > best) { best = len[i]; total = cnt[i]; }
            else if (len[i] == best) total += cnt[i];
        }
        return total;
    }
    //@end

    //@tpl envelopes
    // Russian doll envelopes: sort width ascending and, for equal widths, height
    // descending; then the answer is the strict LIS of the heights.
    static int maxEnvelopes(int[][] env) {
        Arrays.sort(env, (p, q) -> p[0] != q[0] ? Integer.compare(p[0], q[0])
                                                : Integer.compare(q[1], p[1]));
        int[] h = new int[env.length];
        for (int i = 0; i < env.length; i++) h[i] = env[i][1];
        return lis(h, true);
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Every subset of indices: {length of the best, number of subsets of that length}.
    static int[] brute(int[] a, boolean strict) {
        int best = 0, count = 0;
        for (int mask = 1; mask < (1 << a.length); mask++) {
            int prev = Integer.MIN_VALUE, k = 0;
            boolean ok = true;
            for (int i = 0; i < a.length && ok; i++)
                if ((mask >> i & 1) == 1) {
                    if (k > 0 && (strict ? a[i] <= prev : a[i] < prev)) ok = false;
                    prev = a[i];
                    k++;
                }
            if (!ok) continue;
            if (k > best) { best = k; count = 1; }
            else if (k == best) count++;
        }
        return new int[] {best, count};
    }

    // Every subset of envelopes: does it nest when sorted by width?
    static int bruteEnv(int[][] e) {
        int n = e.length, best = 0;
        for (int mask = 1; mask < (1 << n); mask++) {
            List<int[]> s = new ArrayList<>();
            for (int i = 0; i < n; i++) if ((mask >> i & 1) == 1) s.add(e[i]);
            Collections.sort(s, (p, q) -> p[0] != q[0] ? p[0] - q[0] : p[1] - q[1]);
            boolean ok = true;
            for (int i = 1; i < s.size(); i++)
                if (s.get(i)[0] <= s.get(i - 1)[0] || s.get(i)[1] <= s.get(i - 1)[1]) ok = false;
            if (ok) best = Math.max(best, s.size());
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(1006);
        for (int t = 0; t < 3000; t++) {
            int n = r.nextInt(13);
            int[] a = Check.ints(r, n, -3, t % 2 == 0 ? 4 : 30);
            int[] s = brute(a, true), ns = brute(a, false);
            Check.eq(s[0], lis(a, true), "strict " + Arrays.toString(a));
            Check.eq(ns[0], lis(a, false), "non-decreasing " + Arrays.toString(a));
            if (n > 0) Check.eq(s[1], findNumberOfLIS(a), "count " + Arrays.toString(a));
            int[][] e = new int[r.nextInt(10)][];
            for (int i = 0; i < e.length; i++) e[i] = new int[] {1 + r.nextInt(6), 1 + r.nextInt(6)};
            int want = bruteEnv(e);
            Check.eq(want, maxEnvelopes(e), "envelopes " + Arrays.deepToString(e));
        }
        Check.eq(4, lis(new int[] {10, 9, 2, 5, 3, 7, 101, 18}, true), "LC 300 sample 1");
        Check.eq(4, lis(new int[] {0, 1, 0, 3, 2, 3}, true), "LC 300 sample 2");
        Check.eq(1, lis(new int[] {7, 7, 7, 7, 7, 7, 7}, true), "LC 300 sample 3");
        Check.eq(7, lis(new int[] {7, 7, 7, 7, 7, 7, 7}, false), "non-decreasing sevens");
        Check.eq(2, findNumberOfLIS(new int[] {1, 3, 5, 4, 7}), "LC 673 sample 1");
        Check.eq(5, findNumberOfLIS(new int[] {2, 2, 2, 2, 2}), "LC 673 sample 2");
        Check.eq(3, maxEnvelopes(new int[][] {{5, 4}, {6, 4}, {6, 7}, {2, 3}}), "LC 354 sample 1");
        Check.eq(1, maxEnvelopes(new int[][] {{1, 1}, {1, 1}, {1, 1}}), "LC 354 sample 2");
        Check.done("P10Lis");
    }
}
