import java.util.*;

/*@core Interval DP and game DP
note: Interval DP: `dp[l][r]` from shorter intervals, so loop by LENGTH (or l downwards). Try every split k between l and r. Game DP: store (mover − opponent); taking x leaves the opponent leading by `dp[rest]`, so the move is worth `x − dp[rest]`.
@*/
class Core14 {
    static long matrixChain(int[] d) {                 // matrices d[i-1] x d[i], i = 1..n
        int n = d.length - 1;
        long[][] dp = new long[n + 1][n + 1];
        for (int len = 2; len <= n; len++)
            for (int l = 1; l + len - 1 <= n; l++) {
                int r = l + len - 1;
                dp[l][r] = Long.MAX_VALUE;
                for (int k = l; k < r; k++)            // last multiplication splits at k
                    dp[l][r] = Math.min(dp[l][r], dp[l][k] + dp[k + 1][r] + (long) d[l - 1] * d[k] * d[r]);
            }
        return dp[1][n];
    }
}

/*@LC 368
tag: LIS-style chain + parent pointers
cue: Largest subset where every pair divides one way or the other.
hook: Sort; then it is LIS where "smaller" means "divides". Walk back through prev[].
time: O(n²) time · O(n) space
trap: Sort first: after sorting, a chain where each divides the next makes every pair divisible.
@*/
class LC368 {
    public List<Integer> largestDivisibleSubset(int[] nums) {
        Arrays.sort(nums);                                                          //*
        int n = nums.length, end = 0;
        int[] len = new int[n], prev = new int[n];
        for (int i = 0; i < n; i++) {
            len[i] = 1;
            prev[i] = -1;
            for (int j = 0; j < i; j++)
                if (nums[i] % nums[j] == 0 && len[j] + 1 > len[i]) { len[i] = len[j] + 1; prev[i] = j; }   //*
            if (len[i] > len[end]) end = i;
        }
        List<Integer> res = new ArrayList<>();
        for (int i = end; i != -1; i = prev[i]) res.add(nums[i]);                   //* walk back
        Collections.reverse(res);
        return res;
    }
}

/*@var LC 1048 · Longest String Chain
note: Sort by length; each word's best = 1 + best of any word you get by deleting one letter.
@*/
class LC368v1 {
    public int longestStrChain(String[] words) {
        Arrays.sort(words, (a, b) -> Integer.compare(a.length(), b.length()));
        Map<String, Integer> best = new HashMap<>();
        int ans = 0;
        for (String w : words) {
            int cur = 1;
            for (int i = 0; i < w.length(); i++)
                cur = Math.max(cur, best.getOrDefault(w.substring(0, i) + w.substring(i + 1), 0) + 1);
            best.put(w, cur);
            ans = Math.max(ans, cur);
        }
        return ans;
    }
}

/*@LC 486
tag: Game DP (score difference)
cue: Two players take from either end; can player 1 win or tie?
hook: My lead on l..r = what I take minus their lead on what is left.
time: O(n²) time · O(n²) space
trap: Ties count as a win for player 1, so `>= 0`.
var: LC 877 Stone Game | The same DP works (the answer is always true).
@*/
class LC486 {
    public boolean predictTheWinner(int[] nums) {
        int n = nums.length;
        int[][] dp = new int[n][n];                    // mover's score minus the other's on nums[l..r]
        for (int l = n - 1; l >= 0; l--) {
            dp[l][l] = nums[l];
            for (int r = l + 1; r < n; r++)
                dp[l][r] = Math.max(nums[l] - dp[l + 1][r], nums[r] - dp[l][r - 1]);   //*
        }
        return dp[0][n - 1] >= 0;                      //*
    }
}

/*@LC 1547
tag: Interval DP over cut points
cue: Cheapest order of cuts; each cut costs the current stick length.
hook: Add 0 and n, sort; dp over pairs of cut points, choose the FIRST cut inside.
time: O(m³) time · O(m²) space (m = cuts)
trap: Index the DP by cut positions in the sorted array, not by stick length (n is up to 10⁶).
@*/
class LC1547 {
    public int minCost(int n, int[] cuts) {
        int m = cuts.length;
        int[] c = new int[m + 2];
        for (int i = 0; i < m; i++) c[i + 1] = cuts[i];
        c[m + 1] = n;                                            //* add 0 and n
        Arrays.sort(c);
        int[][] dp = new int[m + 2][m + 2];                      // cost to make every cut between c[i] and c[j]
        for (int len = 2; len <= m + 1; len++)
            for (int i = 0; i + len <= m + 1; i++) {
                int j = i + len;
                dp[i][j] = Integer.MAX_VALUE;
                for (int k = i + 1; k < j; k++) dp[i][j] = Math.min(dp[i][j], dp[i][k] + dp[k][j]);   //*
                dp[i][j] += c[j] - c[i];                         //* the first cut costs this length
            }
        return dp[0][m + 1];
    }
}

/*@var LC 1039 · Minimum Score Triangulation of Polygon
note: The same shape: the edge (i, j) forms a triangle with some k between them.
@*/
class LC1547v1 {
    public int minScoreTriangulation(int[] values) {
        int n = values.length;
        int[][] dp = new int[n][n];
        for (int len = 2; len < n; len++)
            for (int i = 0; i + len < n; i++) {
                int j = i + len;
                dp[i][j] = Integer.MAX_VALUE;
                for (int k = i + 1; k < j; k++)
                    dp[i][j] = Math.min(dp[i][j], dp[i][k] + dp[k][j] + values[i] * values[k] * values[j]);
            }
        return dp[0][n - 1];
    }
}

/*@LC 1690
tag: Game DP + prefix sums
cue: Remove an end stone and score the sum of what is left; maximise the score difference.
hook: Same game DP; the points of a move are the remaining sum, from prefix sums.
time: O(n²) time · O(n²) space
trap: Removing stones[l] scores `sum(l+1..r)`, removing stones[r] scores `sum(l..r−1)`.
@*/
class LC1690 {
    public int stoneGameVII(int[] stones) {
        int n = stones.length;
        int[] P = new int[n + 1];
        for (int i = 0; i < n; i++) P[i + 1] = P[i] + stones[i];
        int[][] dp = new int[n][n];
        for (int l = n - 2; l >= 0; l--)
            for (int r = l + 1; r < n; r++) {
                int takeLeft = P[r + 1] - P[l + 1];                  //*
                int takeRight = P[r] - P[l];                         //*
                dp[l][r] = Math.max(takeLeft - dp[l + 1][r], takeRight - dp[l][r - 1]);   //*
            }
        return dp[0][n - 1];
    }
}

/*@LC 2338
tag: Divisor chains × combinations
cue: Count arrays of length n with values ≤ maxValue where each value divides the next.
hook: Count chains of DISTINCT values (at most 14 long), then choose where the value changes: C(n−1, s−1).
time: O(14 · V log V + 14 · n) time · O(14 · (V + n)) space
trap: Chains are short because each step at least doubles: 2¹³ < 10⁴ ≤ 2¹⁴.
var: Infosys version with mod 10000 | 10000 is not prime: build C with Pascal's triangle (as here), never with inverses.
@*/
class LC2338 {
    public int idealArrays(int n, int maxValue) {
        final long MOD = 1_000_000_007L;
        final int S = 15;
        long[][] chains = new long[S][maxValue + 1];             // chains[s][v] = s distinct values ending at v
        for (int v = 1; v <= maxValue; v++) chains[1][v] = 1;
        for (int s = 1; s + 1 < S; s++)
            for (int v = 1; v <= maxValue; v++) {
                if (chains[s][v] == 0) continue;
                for (int m = 2 * v; m <= maxValue; m += v)       //* extend by a proper multiple
                    chains[s + 1][m] = (chains[s + 1][m] + chains[s][v]) % MOD;
            }
        long[][] C = new long[n][S];                             // Pascal: C[i][j] = C(i, j)
        for (int i = 0; i < n; i++) {
            C[i][0] = 1;
            for (int j = 1; j < S && j <= i; j++) C[i][j] = (C[i - 1][j - 1] + C[i - 1][j]) % MOD;
        }
        long ans = 0;
        for (int s = 1; s < S && s <= n; s++) {
            long sum = 0;
            for (int v = 1; v <= maxValue; v++) sum += chains[s][v];
            ans = (ans + sum % MOD * C[n - 1][s - 1]) % MOD;     //* place s − 1 change points
        }
        return (int) ans;
    }
}

/*@LC 312
tag: Interval DP · last one wins
cue: Burst balloons; each burst earns left × it × right; maximise.
hook: Pick the balloon burst LAST in (l, r): its neighbours are then l and r.
time: O(n³) time · O(n²) space
trap: Choosing the FIRST burst does not split into independent halves; choosing the last does.
var: LC 1000 Minimum Cost to Merge Stones | Interval DP with an extra "how many piles" condition (hard).
@*/
class LC312 {
    public int maxCoins(int[] nums) {
        int n = nums.length;
        int[] a = new int[n + 2];
        a[0] = a[n + 1] = 1;                                       //* virtual 1s
        for (int i = 0; i < n; i++) a[i + 1] = nums[i];
        int[][] dp = new int[n + 2][n + 2];                        // best for balloons strictly inside (l, r)
        for (int len = 2; len <= n + 1; len++)
            for (int l = 0; l + len <= n + 1; l++) {
                int r = l + len;
                for (int k = l + 1; k < r; k++)                    //* k bursts LAST
                    dp[l][r] = Math.max(dp[l][r], dp[l][k] + a[l] * a[k] * a[r] + dp[k][r]);   //*
            }
        return dp[0][n + 1];
    }
}

/*@LC 664
tag: Interval DP · merge equal ends
cue: A printer prints runs of one letter over anything; fewest turns to print s.
hook: Print s[l] alone, or let it share a turn with a later equal letter s[k].
time: O(n³) time · O(n²) space
trap: When s[k] == s[l], the left part becomes `dp[l+1][k−1]` (s[l] rides along with s[k]).
var: LC 546 Remove Boxes | Interval DP with a third index: how many boxes equal to s[l] are glued on (hard).
@*/
class LC664 {
    public int strangePrinter(String s) {
        int n = s.length();
        int[][] dp = new int[n][n];
        for (int l = n - 1; l >= 0; l--) {
            dp[l][l] = 1;
            for (int r = l + 1; r < n; r++) {
                dp[l][r] = dp[l + 1][r] + 1;                                   //* s[l] on its own
                for (int k = l + 1; k <= r; k++)
                    if (s.charAt(k) == s.charAt(l))
                        dp[l][r] = Math.min(dp[l][r], dp[l + 1][k - 1] + dp[k][r]);   //* share with s[k]
            }
        }
        return dp[0][n - 1];
    }
}

public class Day14 {
    static int game(int[] a, int l, int r) { return l > r ? 0 : Math.max(a[l] - game(a, l + 1, r), a[r] - game(a, l, r - 1)); }

    static int game7(int[] a, int l, int r) {
        if (l >= r) return 0;
        int s = 0;
        for (int i = l; i <= r; i++) s += a[i];
        return Math.max(s - a[l] - game7(a, l + 1, r), s - a[r] - game7(a, l, r - 1));
    }

    static int cutBrute(int lo, int hi, List<Integer> cuts) {
        List<Integer> inside = new ArrayList<>();
        for (int c : cuts) if (c > lo && c < hi) inside.add(c);
        if (inside.isEmpty()) return 0;
        int best = Integer.MAX_VALUE;
        for (int c : inside) best = Math.min(best, hi - lo + cutBrute(lo, c, inside) + cutBrute(c, hi, inside));
        return best;
    }

    static int burst(List<Integer> b) {
        if (b.isEmpty()) return 0;
        int best = 0;
        for (int i = 0; i < b.size(); i++) {
            int l = i > 0 ? b.get(i - 1) : 1, r = i + 1 < b.size() ? b.get(i + 1) : 1;
            List<Integer> rest = new ArrayList<>(b);
            rest.remove(i);
            best = Math.max(best, l * b.get(i) * r + burst(rest));
        }
        return best;
    }

    public static void main(String[] args) {
        T.eq("core mcm", Core14.matrixChain(T.ints("[10,30,5,60]")), 4500L);

        List<Integer> d = new LC368().largestDivisibleSubset(T.ints("[1,2,3]"));
        T.ok("368a", d.size() == 2 && d.get(1) % d.get(0) == 0);
        T.eq("368b", new LC368().largestDivisibleSubset(T.ints("[1,2,4,8]")), List.of(1, 2, 4, 8));
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 8);
            Set<Integer> vs = new LinkedHashSet<>();
            while (vs.size() < n) vs.add(T.rnd(1, 40));
            int[] a = vs.stream().mapToInt(Integer::intValue).toArray();
            int best = 0;
            for (int m = 1; m < 1 << n; m++) {
                boolean ok = true;
                for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) if ((m >> i & 1) == 1 && (m >> j & 1) == 1 && a[i] % a[j] != 0 && a[j] % a[i] != 0) ok = false;
                if (ok) best = Math.max(best, Integer.bitCount(m));
            }
            List<Integer> got = new LC368().largestDivisibleSubset(a.clone());
            boolean ok = got.size() == best;
            for (int i = 0; i + 1 < got.size(); i++) if (got.get(i + 1) % got.get(i) != 0) ok = false;
            T.ok("368 rand", ok);
        }
        T.eq("1048a", new LC368v1().longestStrChain(new String[]{"a", "b", "ba", "bca", "bda", "bdca"}), 4);
        T.eq("1048b", new LC368v1().longestStrChain(new String[]{"xbc", "pcxbcf", "xb", "cxbc", "pcxbc"}), 5);
        T.eq("1048c", new LC368v1().longestStrChain(new String[]{"abcd", "dbqca"}), 1);

        T.eq("486a", new LC486().predictTheWinner(T.ints("[1,5,2]")), false);
        T.eq("486b", new LC486().predictTheWinner(T.ints("[1,5,233,7]")), true);
        T.eq("1690a", new LC1690().stoneGameVII(T.ints("[5,3,1,4,2]")), 6);
        T.eq("1690b", new LC1690().stoneGameVII(T.ints("[7,90,5,1,100,10,10,2]")), 122);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 9), 0, 20);
            T.eq("486 rand", new LC486().predictTheWinner(a), game(a, 0, a.length - 1) >= 0);
            if (a.length >= 2) T.eq("1690 rand", new LC1690().stoneGameVII(a), game7(a, 0, a.length - 1));
        }

        T.eq("1547a", new LC1547().minCost(7, T.ints("[1,3,4,5]")), 16);
        T.eq("1547b", new LC1547().minCost(9, T.ints("[5,6,1,4,2]")), 22);
        for (int t = 0; t < 100; t++) {
            int n = T.rnd(2, 15);
            Set<Integer> cs = new TreeSet<>();
            int m = T.rnd(1, Math.min(6, n - 1));
            while (cs.size() < m) cs.add(T.rnd(1, n - 1));
            int[] cuts = cs.stream().mapToInt(Integer::intValue).toArray();
            T.eq("1547 rand", new LC1547().minCost(n, cuts.clone()), cutBrute(0, n, new ArrayList<>(cs)));
        }
        T.eq("1039a", new LC1547v1().minScoreTriangulation(T.ints("[1,2,3]")), 6);
        T.eq("1039b", new LC1547v1().minScoreTriangulation(T.ints("[3,7,4,5]")), 144);
        T.eq("1039c", new LC1547v1().minScoreTriangulation(T.ints("[1,3,1,4,1,5]")), 13);

        T.eq("2338a", new LC2338().idealArrays(2, 5), 10);
        T.eq("2338b", new LC2338().idealArrays(5, 3), 11);
        for (int t = 0; t < 60; t++) {
            int n = T.rnd(1, 12), mv = T.rnd(1, 40);
            long[] cnt = new long[mv + 1];
            for (int v = 1; v <= mv; v++) cnt[v] = 1;
            for (int len = 2; len <= n; len++) {
                long[] nx = new long[mv + 1];
                for (int v = 1; v <= mv; v++) for (int m2 = v; m2 <= mv; m2 += v) nx[m2] += cnt[v];
                cnt = nx;
            }
            long total = 0;
            for (long c : cnt) total += c;
            T.eq("2338 rand", new LC2338().idealArrays(n, mv), (int) (total % 1_000_000_007L));
        }
        T.ok("2338 big", new LC2338().idealArrays(10000, 10000) >= 0);

        T.eq("312a", new LC312().maxCoins(T.ints("[3,1,5,8]")), 167);
        T.eq("312b", new LC312().maxCoins(T.ints("[1,5]")), 10);
        for (int t = 0; t < 100; t++) {
            int[] a = T.rand(T.rnd(1, 6), 0, 9);
            List<Integer> b = new ArrayList<>();
            for (int x : a) b.add(x);
            T.eq("312 rand", new LC312().maxCoins(a), burst(b));
        }

        T.eq("664a", new LC664().strangePrinter("aaabbb"), 2);
        T.eq("664b", new LC664().strangePrinter("aba"), 2);
        T.eq("664c", new LC664().strangePrinter("abcabc"), 5);
        T.eq("664d", new LC664().strangePrinter("tbgtgb"), 4);
        T.eq("664e", new LC664().strangePrinter("a"), 1);
        for (int t = 0; t < 60; t++) {
            int n = T.rnd(1, 6);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) sb.append((char) ('a' + T.rnd(0, 2)));
            String target = sb.toString();
            Map<String, Integer> dist = new HashMap<>();
            Deque<String> q = new ArrayDeque<>();
            String start = ".".repeat(n);
            dist.put(start, 0);
            q.add(start);
            while (!q.isEmpty() && !dist.containsKey(target)) {
                String cur = q.poll();
                for (int i = 0; i < n; i++) for (int j = i; j < n; j++) for (char c = 'a'; c <= 'c'; c++) {
                    char[] x = cur.toCharArray();
                    for (int p = i; p <= j; p++) x[p] = c;
                    String nx = new String(x);
                    if (!dist.containsKey(nx)) { dist.put(nx, dist.get(cur) + 1); q.add(nx); }
                }
            }
            T.eq("664 rand " + target, new LC664().strangePrinter(target), dist.get(target));
        }
        T.summary("Day14");
    }
}
