import java.util.*;

/*@core Prefix sums: one array, four questions
note: `P[i]` = sum of the first i numbers, so `sum(l..r) = P[r+1] − P[l]`. Counting asks the map "how many earlier prefixes"; longest asks "where was the FIRST one".
@*/
class Core02 {
    static long[] prefix(int[] a) {                      // P[0] = 0, P[i+1] = P[i] + a[i]
        long[] P = new long[a.length + 1];
        for (int i = 0; i < a.length; i++) P[i + 1] = P[i] + a[i];
        return P;
    }

    static int countSumK(int[] a, int k) {               // how many subarrays sum to k
        Map<Integer, Integer> seen = new HashMap<>(Map.of(0, 1));
        int p = 0, count = 0;
        for (int x : a) {
            p += x;
            count += seen.getOrDefault(p - k, 0);
            seen.merge(p, 1, Integer::sum);
        }
        return count;
    }

    static int longestSumK(int[] a, int k) {             // longest subarray that sums to k
        Map<Integer, Integer> first = new HashMap<>(Map.of(0, -1));
        int p = 0, best = 0;
        for (int i = 0; i < a.length; i++) {
            p += a[i];
            Integer j = first.get(p - k);
            if (j != null) best = Math.max(best, i - j);
            first.putIfAbsent(p, i);                     // keep the earliest index
        }
        return best;
    }

    static int[] rangeAdds(int n, int[][] ups) {         // each {l, r, v}: add v to a[l..r]
        int[] d = new int[n + 1];
        for (int[] u : ups) { d[u[0]] += u[2]; d[u[1] + 1] -= u[2]; }
        int[] a = new int[n];
        for (int i = 0, run = 0; i < n; i++) a[i] = run += d[i];
        return a;
    }
}

/*@LC 560
tag: Prefix sum + HashMap
cue: Count subarrays with sum exactly k, and numbers can be negative.
hook: Seed zero, look back k, then count yourself.
time: O(n) time · O(n) space
trap: A sliding window does NOT work with negative numbers. Forgetting `seen.put(0, 1)` misses subarrays that start at index 0.
var: LC 930 Binary Subarrays With Sum | The same code works unchanged.
var: LC 1248 Count Number of Nice Subarrays | Replace each number by `x % 2`, then this code with k.
@*/
class LC560 {
    public int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> seen = new HashMap<>();   // prefix value -> how many times seen
        seen.put(0, 1);                                  //* the empty prefix
        int prefix = 0, count = 0;
        for (int x : nums) {
            prefix += x;
            count += seen.getOrDefault(prefix - k, 0);   //* earlier p with prefix - p == k
            seen.merge(prefix, 1, Integer::sum);         //* count yourself AFTER looking
        }
        return count;
    }
}

/*@var Longest subarray with sum k (LC 325, premium)
note: Store the FIRST index of each prefix instead of a count, and seed `{0: -1}`.
@*/
class LC560v1 {
    public int maxSubArrayLen(int[] nums, int k) {
        Map<Long, Integer> first = new HashMap<>();
        first.put(0L, -1);
        long prefix = 0;
        int best = 0;
        for (int i = 0; i < nums.length; i++) {
            prefix += nums[i];
            Integer j = first.get(prefix - k);
            if (j != null) best = Math.max(best, i - j);
            first.putIfAbsent(prefix, i);                // never overwrite: earliest gives longest
        }
        return best;
    }
}

/*@LC 974
tag: Prefix remainders
cue: Count subarrays whose sum is divisible by k.
hook: Same remainder twice means the part between is divisible.
time: O(n) time · O(k) space
trap: In Java `-7 % 5` is `-2`. Use `Math.floorMod` so remainders are 0..k−1.
@*/
class LC974 {
    public int subarraysDivByK(int[] nums, int k) {
        int[] seen = new int[k];                         // seen[r] = prefixes with remainder r
        seen[0] = 1;                                     //*
        int prefix = 0, count = 0;
        for (int x : nums) {
            prefix = Math.floorMod(prefix + x, k);       //* never negative
            count += seen[prefix];                       //*
            seen[prefix]++;
        }
        return count;
    }
}

/*@var LC 523 · Continuous Subarray Sum (length ≥ 2, sum a multiple of k)
note: Remainder → FIRST index, seeded `{0: -1}`; succeed when the same remainder is at least 2 positions back.
@*/
class LC974v1 {
    public boolean checkSubarraySum(int[] nums, int k) {
        Map<Integer, Integer> first = new HashMap<>();
        first.put(0, -1);
        int r = 0;
        for (int i = 0; i < nums.length; i++) {
            r = (r + nums[i]) % k;                       // nums are non-negative here
            Integer j = first.get(r);
            if (j == null) first.put(r, i);
            else if (i - j >= 2) return true;
        }
        return false;
    }
}

/*@LC 1524
tag: Prefix parity counts
cue: Count subarrays with an odd sum (answer mod 10⁹+7).
hook: Odd minus even is odd: count the opposite parity.
time: O(n) time · O(1) space
trap: The empty prefix (sum 0) is even, so start with `even = 1`.
var: Even-sum subarrays | Add the count of the SAME parity instead.
@*/
class LC1524 {
    public int numOfSubarrays(int[] arr) {
        final int MOD = 1_000_000_007;
        long odd = 0, even = 1, count = 0;               //* the empty prefix is even
        int parity = 0;
        for (int x : arr) {
            parity = (parity + x) & 1;                   // only the parity of the prefix matters
            if (parity == 1) { count += even; odd++; }   //* odd now − even before = odd
            else { count += odd; even++; }               //* even now − odd before = odd
        }
        return (int) (count % MOD);
    }
}

/*@LC 525
tag: Balance + first index
cue: Longest subarray with equal numbers of 0s and 1s.
hook: Zero is minus one; the same balance twice means equal counts in between.
time: O(n) time · O(n) space
trap: Store only the FIRST index of each balance, otherwise the length shrinks.
@*/
class LC525 {
    public int findMaxLength(int[] nums) {
        Map<Integer, Integer> first = new HashMap<>();   // balance -> first index seen
        first.put(0, -1);                                //*
        int bal = 0, best = 0;
        for (int i = 0; i < nums.length; i++) {
            bal += nums[i] == 1 ? 1 : -1;                //* 0 counts as -1
            Integer j = first.get(bal);
            if (j != null) best = Math.max(best, i - j); //*
            else first.put(bal, i);                      //* keep the FIRST index
        }
        return best;
    }
}

/*@var LC 1124 · Longest Well-Performing Interval (balance > 0)
note: Hours > 8 count +1, others −1. If the balance is positive the whole prefix works; otherwise look for the first time it was `bal − 1`.
@*/
class LC525v1 {
    public int longestWPI(int[] hours) {
        Map<Integer, Integer> first = new HashMap<>();
        int bal = 0, best = 0;
        for (int i = 0; i < hours.length; i++) {
            bal += hours[i] > 8 ? 1 : -1;
            if (bal > 0) best = i + 1;                   // the whole prefix is good
            else {
                Integer j = first.get(bal - 1);          // first time we were one lower
                if (j != null) best = Math.max(best, i - j);
            }
            first.putIfAbsent(bal, i);
        }
        return best;
    }
}

/*@LC 1109
tag: Difference array
cue: Many "add v to every position from l to r", read every position once at the end.
hook: Add at the start, take it back after the end, then run the sum.
time: O(n + bookings) time · O(n) space
trap: Input is 1-indexed: the start goes to `d[first − 1]` and the stop to `d[last]`.
var: 2-D version (LC 2536) | `d[r1][c1] += v; d[r1][c2+1] −= v; d[r2+1][c1] −= v; d[r2+1][c2+1] += v;` then a 2-D prefix sum.
@*/
class LC1109 {
    public int[] corpFlightBookings(int[][] bookings, int n) {
        int[] d = new int[n + 1];
        for (int[] b : bookings) {
            d[b[0] - 1] += b[2];                         //* start adding at first
            d[b[1]] -= b[2];                             //* stop after last
        }
        int[] ans = new int[n];
        int run = 0;
        for (int i = 0; i < n; i++) {
            run += d[i];                                 //* running sum = the real value
            ans[i] = run;
        }
        return ans;
    }
}

/*@var LC 1094 · Car Pooling
note: Passengers get on at `from` and off at `to` (not inclusive). Fail as soon as the running count passes capacity.
@*/
class LC1109v1 {
    public boolean carPooling(int[][] trips, int capacity) {
        int[] d = new int[1001];
        for (int[] t : trips) { d[t[1]] += t[0]; d[t[2]] -= t[0]; }
        int run = 0;
        for (int x : d) if ((run += x) > capacity) return false;
        return true;
    }
}

/*@LC 304
class: NumMatrix
tag: 2-D prefix sum
cue: Many rectangle-sum queries on a fixed matrix.
hook: Build: up plus left minus corner plus me. Query: big minus top minus left plus corner.
time: O(mn) build · O(1) per query
trap: Shift everything by one (`P[i+1][j+1]`) so row 0 and column 0 need no special case.
var: LC 303 Range Sum Query (1-D) | `P[r + 1] − P[l]`.
@*/
class LC304 {
    private final int[][] P;   // P[i][j] = sum of rows 0..i-1, cols 0..j-1

    public LC304(int[][] matrix) {
        int m = matrix.length, n = matrix[0].length;
        P = new int[m + 1][n + 1];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                P[i + 1][j + 1] = P[i][j + 1] + P[i + 1][j] - P[i][j] + matrix[i][j];   //*
    }

    public int sumRegion(int row1, int col1, int row2, int col2) {
        return P[row2 + 1][col2 + 1] - P[row1][col2 + 1] - P[row2 + 1][col1] + P[row1][col1];   //*
    }
}

/*@var LC 1314 · Matrix Block Sum
note: The same table; clamp each block's corners to the grid before the query.
@*/
class LC304v1 {
    public int[][] matrixBlockSum(int[][] mat, int k) {
        int m = mat.length, n = mat[0].length;
        int[][] P = new int[m + 1][n + 1];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                P[i + 1][j + 1] = P[i][j + 1] + P[i + 1][j] - P[i][j] + mat[i][j];
        int[][] ans = new int[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) {
                int r1 = Math.max(0, i - k), c1 = Math.max(0, j - k);
                int r2 = Math.min(m - 1, i + k), c2 = Math.min(n - 1, j + k);
                ans[i][j] = P[r2 + 1][c2 + 1] - P[r1][c2 + 1] - P[r2 + 1][c1] + P[r1][c1];
            }
        return ans;
    }
}

public class Day02 {
    static int bruteSumK(int[] a, int k) {
        int c = 0;
        for (int i = 0; i < a.length; i++) { int s = 0; for (int j = i; j < a.length; j++) if ((s += a[j]) == k) c++; }
        return c;
    }

    public static void main(String[] args) {
        T.eq("core prefix", Core02.prefix(T.ints("[1,2,3]")), new long[]{0, 1, 3, 6});
        T.eq("core range", Core02.rangeAdds(5, new int[][]{{1, 3, 3}, {2, 4, 2}}), T.ints("[0,3,5,5,2]"));

        T.eq("560a", new LC560().subarraySum(T.ints("[1,1,1]"), 2), 2);
        T.eq("560b", new LC560().subarraySum(T.ints("[1,2,3]"), 3), 2);
        for (int t = 0; t < 300; t++) {
            int[] a = T.rand(T.rnd(1, 12), -3, 3);
            int k = T.rnd(-4, 4);
            T.eq("560 rand", new LC560().subarraySum(a, k), bruteSumK(a, k));
            T.eq("core count", Core02.countSumK(a, k), bruteSumK(a, k));
            int best = 0;
            for (int i = 0; i < a.length; i++) { int s = 0; for (int j = i; j < a.length; j++) if ((s += a[j]) == k) best = Math.max(best, j - i + 1); }
            T.eq("325 rand", new LC560v1().maxSubArrayLen(a, k), best);
            T.eq("core longest", Core02.longestSumK(a, k), best);
        }

        T.eq("974a", new LC974().subarraysDivByK(T.ints("[4,5,0,-2,-3,1]"), 5), 7);
        T.eq("974b", new LC974().subarraysDivByK(T.ints("[5]"), 9), 0);
        for (int t = 0; t < 300; t++) {
            int[] a = T.rand(T.rnd(1, 12), -9, 9);
            int k = T.rnd(1, 6), c = 0;
            for (int i = 0; i < a.length; i++) { int s = 0; for (int j = i; j < a.length; j++) if ((s += a[j]) % k == 0) c++; }
            T.eq("974 rand", new LC974().subarraysDivByK(a, k), c);
        }
        T.eq("523a", new LC974v1().checkSubarraySum(T.ints("[23,2,4,6,7]"), 6), true);
        T.eq("523b", new LC974v1().checkSubarraySum(T.ints("[23,2,6,4,7]"), 6), true);
        T.eq("523c", new LC974v1().checkSubarraySum(T.ints("[23,2,6,4,7]"), 13), false);
        T.eq("523d", new LC974v1().checkSubarraySum(T.ints("[5,0,0,0]"), 3), true);

        T.eq("1524a", new LC1524().numOfSubarrays(T.ints("[1,3,5]")), 4);
        T.eq("1524b", new LC1524().numOfSubarrays(T.ints("[2,4,6]")), 0);
        T.eq("1524c", new LC1524().numOfSubarrays(T.ints("[1,2,3,4,5,6,7]")), 16);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 12), 1, 9);
            int c = 0;
            for (int i = 0; i < a.length; i++) { int s = 0; for (int j = i; j < a.length; j++) if ((s += a[j]) % 2 == 1) c++; }
            T.eq("1524 rand", new LC1524().numOfSubarrays(a), c);
        }

        T.eq("525a", new LC525().findMaxLength(T.ints("[0,1]")), 2);
        T.eq("525b", new LC525().findMaxLength(T.ints("[0,1,0]")), 2);
        T.eq("525c", new LC525().findMaxLength(T.ints("[0,1,1,1,1,1,0,0,0]")), 6);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 14), 0, 1);
            int best = 0;
            for (int i = 0; i < a.length; i++) { int b = 0; for (int j = i; j < a.length; j++) if ((b += a[j] == 1 ? 1 : -1) == 0) best = Math.max(best, j - i + 1); }
            T.eq("525 rand", new LC525().findMaxLength(a), best);
        }
        T.eq("1124a", new LC525v1().longestWPI(T.ints("[9,9,6,0,6,6,9]")), 3);
        T.eq("1124b", new LC525v1().longestWPI(T.ints("[6,6,6]")), 0);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 14), 6, 10);
            int best = 0;
            for (int i = 0; i < a.length; i++) { int b = 0; for (int j = i; j < a.length; j++) if ((b += a[j] > 8 ? 1 : -1) > 0) best = Math.max(best, j - i + 1); }
            T.eq("1124 rand", new LC525v1().longestWPI(a), best);
        }

        T.eq("1109a", new LC1109().corpFlightBookings(T.grid("[[1,2,10],[2,3,20],[2,5,25]]"), 5), T.ints("[10,55,45,25,25]"));
        T.eq("1109b", new LC1109().corpFlightBookings(T.grid("[[1,2,10],[2,2,15]]"), 2), T.ints("[10,25]"));
        T.eq("1094a", new LC1109v1().carPooling(T.grid("[[2,1,5],[3,3,7]]"), 4), false);
        T.eq("1094b", new LC1109v1().carPooling(T.grid("[[2,1,5],[3,3,7]]"), 5), true);
        T.eq("1094c", new LC1109v1().carPooling(T.grid("[[2,1,5],[3,5,7]]"), 3), true);

        LC304 nm = new LC304(T.grid("[[3,0,1,4,2],[5,6,3,2,1],[1,2,0,1,5],[4,1,0,1,7],[1,0,3,0,5]]"));
        T.eq("304", new int[]{nm.sumRegion(2, 1, 4, 3), nm.sumRegion(1, 1, 2, 2), nm.sumRegion(1, 2, 2, 4)}, T.ints("[8,11,12]"));
        T.eq("1314a", new LC304v1().matrixBlockSum(T.grid("[[1,2,3],[4,5,6],[7,8,9]]"), 1), T.grid("[[12,21,16],[27,45,33],[24,39,28]]"));
        T.eq("1314b", new LC304v1().matrixBlockSum(T.grid("[[1,2,3],[4,5,6],[7,8,9]]"), 2), T.grid("[[45,45,45],[45,45,45],[45,45,45]]"));
        T.summary("Day02");
    }
}
