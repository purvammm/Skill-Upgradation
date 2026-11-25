import java.util.*;
import java.util.function.LongPredicate;

/*@core Binary search: one loop shape for everything
note: Picture the answers as `false false … true true`. Find the first true with `hi = mid` / `lo = mid + 1`. For the last true (`true … false`), round mid UP and use `lo = mid` / `hi = mid − 1`, or the loop never ends.
@*/
class Core06 {
    static int lowerBound(int[] a, int x) {           // first i with a[i] >= x (a.length if none)
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] < x) lo = mid + 1; else hi = mid;
        }
        return lo;
    }

    static int upperBound(int[] a, int x) {           // first i with a[i] > x
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] <= x) lo = mid + 1; else hi = mid;
        }
        return lo;
    }

    static long firstTrue(long lo, long hi, LongPredicate ok) {   // smallest x in [lo, hi] with ok(x)
        while (lo < hi) {
            long mid = lo + (hi - lo) / 2;
            if (ok.test(mid)) hi = mid; else lo = mid + 1;
        }
        return lo;
    }

    static long lastTrue(long lo, long hi, LongPredicate ok) {    // largest x in [lo, hi] with ok(x)
        while (lo < hi) {
            long mid = lo + (hi - lo + 1) / 2;                       // round UP
            if (ok.test(mid)) lo = mid; else hi = mid - 1;
        }
        return lo;
    }
}

/*@LC 34
tag: Lower bound twice
cue: First and last position of a value in a sorted array.
hook: First ≥ x, and first ≥ x + 1, minus one.
time: O(log n) time · O(1) space
trap: `hi` starts at `a.length` (the answer may be "past the end"), and the loop is `lo < hi`.
var: LC 35 Search Insert Position | It IS `lowerBound(nums, target)`.
var: LC 278 First Bad Version | `firstTrue(1, n, isBadVersion)`.
@*/
class LC34 {
    public int[] searchRange(int[] nums, int target) {
        int first = lowerBound(nums, target);
        if (first == nums.length || nums[first] != target) return new int[]{-1, -1};
        return new int[]{first, lowerBound(nums, target + 1) - 1};     //*
    }

    private int lowerBound(int[] a, int x) {           // first i with a[i] >= x
        int lo = 0, hi = a.length;                     //* hi = length, not length - 1
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] < x) lo = mid + 1;              //* too small: answer is right of mid
            else hi = mid;                             //* mid may be the answer
        }
        return lo;
    }
}

/*@LC 875
tag: Binary search on the answer · smallest that works
cue: Smallest eating speed that finishes all piles within h hours.
hook: Guess a speed, count the hours; too slow, go up; fast enough, try lower.
time: O(n log max) time · O(1) space
trap: `(p + speed − 1) / speed` overflows int for p, speed near 10⁹. Use `(p − 1) / speed + 1`, and sum the hours in a `long`.
@*/
class LC875 {
    public int minEatingSpeed(int[] piles, int h) {
        int lo = 1, hi = 1_000_000_000;                //* the range of possible answers
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (hours(piles, mid) <= h) hi = mid;      //* fast enough: try slower
            else lo = mid + 1;
        }
        return lo;
    }

    private long hours(int[] piles, int speed) {
        long total = 0;
        for (int p : piles) total += (p - 1) / speed + 1;   //* ceil(p / speed)
        return total;
    }
}

/*@LC 1011
tag: Binary search on the answer + greedy check
cue: Smallest ship capacity to ship packages in order within D days.
hook: Capacity between the heaviest box and the total; fill each day greedily to check.
time: O(n log sum) time · O(1) space
trap: The low end is the HEAVIEST package, or one box never fits.
var: Book allocation / painter's partition (GfG) | The same code; "pages" or "boards" instead of weights.
@*/
class LC1011 {
    public int shipWithinDays(int[] weights, int days) {
        int lo = 0, hi = 0;
        for (int w : weights) { lo = Math.max(lo, w); hi += w; }   //*
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (daysNeeded(weights, mid) <= days) hi = mid;
            else lo = mid + 1;
        }
        return lo;
    }

    private int daysNeeded(int[] weights, int cap) {
        int d = 1, load = 0;
        for (int w : weights) {
            if (load + w > cap) { d++; load = 0; }      //* full: start the next day
            load += w;
        }
        return d;
    }
}

/*@LC 410
tag: Binary search on the answer (same check as 1011)
cue: Split into k contiguous parts and minimise the largest part sum.
hook: It is the ship problem: days = k, capacity = largest sum.
time: O(n log sum) time · O(1) space
trap: Count parts greedily and compare `parts ≤ k`; fewer parts is fine because you can always cut more.
var: DP version | `dp[j][i]` = best for the first i numbers in j parts (Day 21 shows the k-parts DP).
@*/
class LC410 {
    public int splitArray(int[] nums, int k) {
        int lo = 0, hi = 0;
        for (int x : nums) { lo = Math.max(lo, x); hi += x; }
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (parts(nums, mid) <= k) hi = mid;        //*
            else lo = mid + 1;
        }
        return lo;
    }

    private int parts(int[] a, int limit) {
        int cnt = 1, sum = 0;
        for (int x : a) {
            if (sum + x > limit) { cnt++; sum = 0; }
            sum += x;
        }
        return cnt;
    }
}

/*@LC 1552
tag: Binary search on the answer · largest that works
cue: Place m balls in baskets to maximise the smallest gap.
hook: Guess a gap, place greedily from the left; it fits, go bigger.
time: O(n log n + n log range) time · O(1) space
trap: Searching for the LARGEST, so `mid = lo + (hi − lo + 1) / 2`, or `lo = mid` loops forever.
var: Aggressive Cows (GfG/SPOJ), LC 2517 Maximum Tastiness of Candy Basket | The same code.
@*/
class LC1552 {
    public int maxDistance(int[] position, int m) {
        Arrays.sort(position);
        int lo = 1, hi = position[position.length - 1] - position[0];
        while (lo < hi) {
            int mid = lo + (hi - lo + 1) / 2;                //* round up
            if (canPlace(position, m, mid)) lo = mid;        //*
            else hi = mid - 1;
        }
        return lo;
    }

    private boolean canPlace(int[] pos, int m, int gap) {
        int placed = 1, last = pos[0];
        for (int i = 1; i < pos.length && placed < m; i++)
            if (pos[i] - last >= gap) { placed++; last = pos[i]; }   //* first spot far enough
        return placed >= m;
    }
}

/*@LC 2226
tag: Binary search on the answer · largest that works
cue: Largest equal pile size x so at least k children get one pile of x candies.
hook: Children fed by x is the sum of pile / x; keep the largest x that feeds k.
time: O(n log max) time · O(1) space
trap: The answer can be 0, so `lo = 0`; `k` is a `long`, so count children in a `long`.
@*/
class LC2226 {
    public int maximumCandies(int[] candies, long k) {
        int lo = 0, hi = 0;
        for (int c : candies) hi = Math.max(hi, c);
        while (lo < hi) {
            int mid = lo + (hi - lo + 1) / 2;                //* round up: mid is never 0
            long kids = 0;
            for (int c : candies) kids += c / mid;           //*
            if (kids >= k) lo = mid; else hi = mid - 1;
        }
        return lo;
    }
}

/*@LC 719
tag: Binary search the value + count with two pointers
cue: The k-th smallest distance among all pairs.
hook: Guess d, count pairs ≤ d with a sliding window; the first d with count ≥ k.
time: O(n log n + n log W) time · O(1) space
trap: Count pairs in a `long`: n = 10⁴ gives 5·10⁷ pairs, fine, but the habit saves you on bigger n.
var: LC 668 Kth Smallest Number in Multiplication Table | Count = Σ min(n, mid / i) over rows i.
@*/
class LC719 {
    public int smallestDistancePair(int[] nums, int k) {
        Arrays.sort(nums);
        int lo = 0, hi = nums[nums.length - 1] - nums[0];
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (pairsAtMost(nums, mid) >= k) hi = mid;      //*
            else lo = mid + 1;
        }
        return lo;
    }

    private long pairsAtMost(int[] a, int d) {
        long cnt = 0;
        for (int l = 0, r = 0; r < a.length; r++) {
            while (a[r] - a[l] > d) l++;                    //* window where every pair is <= d
            cnt += r - l;                                   //* pairs (l..r-1, r)
        }
        return cnt;
    }
}

/*@var LC 378 · Kth Smallest Element in a Sorted Matrix
note: Binary search the value; count cells ≤ mid with a staircase walk from the bottom-left corner.
@*/
class LC719v1 {
    public int kthSmallest(int[][] matrix, int k) {
        int n = matrix.length, lo = matrix[0][0], hi = matrix[n - 1][n - 1];
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2, cnt = 0;
            for (int r = n - 1, c = 0; r >= 0 && c < n; ) {
                if (matrix[r][c] <= mid) { cnt += r + 1; c++; }   // whole column part counts
                else r--;
            }
            if (cnt >= k) hi = mid; else lo = mid + 1;
        }
        return lo;
    }
}

public class Day06 {
    public static void main(String[] args) {
        int[] s = T.ints("[1,2,2,2,5]");
        T.eq("core lb", new int[]{Core06.lowerBound(s, 2), Core06.upperBound(s, 2), Core06.lowerBound(s, 9)}, T.ints("[1,4,5]"));
        T.eq("core ft", Core06.firstTrue(0, 100, x -> x * x >= 50), 8L);
        T.eq("core lt", Core06.lastTrue(0, 100, x -> x * x <= 50), 7L);

        T.eq("34a", new LC34().searchRange(T.ints("[5,7,7,8,8,10]"), 8), T.ints("[3,4]"));
        T.eq("34b", new LC34().searchRange(T.ints("[5,7,7,8,8,10]"), 6), T.ints("[-1,-1]"));
        T.eq("34c", new LC34().searchRange(new int[0], 0), T.ints("[-1,-1]"));
        T.eq("34d", new LC34().searchRange(T.ints("[1]"), 1), T.ints("[0,0]"));

        T.eq("875a", new LC875().minEatingSpeed(T.ints("[3,6,7,11]"), 8), 4);
        T.eq("875b", new LC875().minEatingSpeed(T.ints("[30,11,23,4,20]"), 5), 30);
        T.eq("875c", new LC875().minEatingSpeed(T.ints("[30,11,23,4,20]"), 6), 23);
        T.eq("875d", new LC875().minEatingSpeed(T.ints("[1000000000,1000000000]"), 3), 1000000000);
        for (int t = 0; t < 200; t++) {
            int[] p = T.rand(T.rnd(1, 6), 1, 20);
            int h = p.length + T.rnd(0, 20), v = 1;
            while (true) { long hrs = 0; for (int x : p) hrs += (x + v - 1) / v; if (hrs <= h) break; v++; }
            T.eq("875 rand", new LC875().minEatingSpeed(p, h), v);
        }

        T.eq("1011a", new LC1011().shipWithinDays(T.ints("[1,2,3,4,5,6,7,8,9,10]"), 5), 15);
        T.eq("1011b", new LC1011().shipWithinDays(T.ints("[3,2,2,4,1,4]"), 3), 6);
        T.eq("1011c", new LC1011().shipWithinDays(T.ints("[1,2,3,1,1]"), 4), 3);

        T.eq("410a", new LC410().splitArray(T.ints("[7,2,5,10,8]"), 2), 18);
        T.eq("410b", new LC410().splitArray(T.ints("[1,2,3,4,5]"), 2), 9);
        T.eq("410c", new LC410().splitArray(T.ints("[1,4,4]"), 3), 4);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 8), 0, 9);
            int k = T.rnd(1, a.length);
            T.eq("410 rand", new LC410().splitArray(a, k), bruteSplit(a, 0, k));
            T.eq("1011 rand", new LC1011().shipWithinDays(a, k), bruteSplit(a, 0, k));
        }

        T.eq("1552a", new LC1552().maxDistance(T.ints("[1,2,3,4,7]"), 3), 3);
        T.eq("1552b", new LC1552().maxDistance(T.ints("[5,4,3,2,1,1000000000]"), 2), 999999999);
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(2, 7), m = T.rnd(2, n);
            int[] p = new int[n];
            Set<Integer> used = new HashSet<>();
            for (int i = 0; i < n; i++) { int x; do x = T.rnd(1, 30); while (!used.add(x)); p[i] = x; }
            int best = 0;
            for (int mask = 0; mask < 1 << n; mask++) {
                if (Integer.bitCount(mask) != m) continue;
                List<Integer> ch = new ArrayList<>();
                for (int i = 0; i < n; i++) if ((mask >> i & 1) == 1) ch.add(p[i]);
                Collections.sort(ch);
                int g = Integer.MAX_VALUE;
                for (int i = 1; i < ch.size(); i++) g = Math.min(g, ch.get(i) - ch.get(i - 1));
                best = Math.max(best, g);
            }
            T.eq("1552 rand", new LC1552().maxDistance(p, m), best);
        }

        T.eq("2226a", new LC2226().maximumCandies(T.ints("[5,8,6]"), 3), 5);
        T.eq("2226b", new LC2226().maximumCandies(T.ints("[2,5]"), 11), 0);
        for (int t = 0; t < 200; t++) {
            int[] c = T.rand(T.rnd(1, 6), 1, 20);
            long k = T.rnd(1, 15);
            int best = 0;
            for (int x = 1; x <= 20; x++) { long kids = 0; for (int v : c) kids += v / x; if (kids >= k) best = x; }
            T.eq("2226 rand", new LC2226().maximumCandies(c, k), best);
        }

        T.eq("719a", new LC719().smallestDistancePair(T.ints("[1,3,1]"), 1), 0);
        T.eq("719b", new LC719().smallestDistancePair(T.ints("[1,1,1]"), 2), 0);
        T.eq("719c", new LC719().smallestDistancePair(T.ints("[1,6,1]"), 3), 5);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(2, 8), 0, 15);
            List<Integer> d = new ArrayList<>();
            for (int i = 0; i < a.length; i++) for (int j = i + 1; j < a.length; j++) d.add(Math.abs(a[i] - a[j]));
            Collections.sort(d);
            int k = T.rnd(1, d.size());
            T.eq("719 rand", new LC719().smallestDistancePair(a.clone(), k), d.get(k - 1));
        }
        T.eq("378a", new LC719v1().kthSmallest(T.grid("[[1,5,9],[10,11,13],[12,13,15]]"), 8), 13);
        T.eq("378b", new LC719v1().kthSmallest(T.grid("[[-5]]"), 1), -5);
        T.summary("Day06");
    }

    static int bruteSplit(int[] a, int from, int k) {   // min over splits of the max part sum
        if (k == 1) { int s = 0; for (int i = from; i < a.length; i++) s += a[i]; return s; }
        int best = Integer.MAX_VALUE, s = 0;
        for (int i = from; i < a.length - 1; i++) {
            s += a[i];
            best = Math.min(best, Math.max(s, bruteSplit(a, i + 1, k - 1)));
        }
        if (best == Integer.MAX_VALUE) { s = 0; for (int i = from; i < a.length; i++) s += a[i]; best = s; }
        return best;
    }
}
