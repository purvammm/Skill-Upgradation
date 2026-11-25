import java.util.*;

/*@core Greedy + heap: unlock, then take the best
note: Two shapes. (1) Sort by what unlocks an option, push options as they unlock, pop the best (IPO, refuelling). (2) Take everything, keep the choices in a heap, and undo the worst one when you go over a limit (ladders, courses, potions).
@*/
class Core05 {
    static long unlockThenTake(int[][] items, long start, int picks) {   // items {cost, gain}
        Arrays.sort(items, (a, b) -> Integer.compare(a[0], b[0]));
        PriorityQueue<Integer> gains = new PriorityQueue<>(Collections.reverseOrder());
        long have = start;
        for (int i = 0; picks-- > 0; ) {
            while (i < items.length && items[i][0] <= have) gains.offer(items[i++][1]);
            if (gains.isEmpty()) break;
            have += gains.poll();
        }
        return have;
    }

    static int keepWithUndo(int[] values, long limit) {   // most items with sum <= limit, in order
        PriorityQueue<Integer> taken = new PriorityQueue<>(Collections.reverseOrder());
        long sum = 0;
        for (int v : values) {
            taken.offer(v);
            sum += v;
            if (sum > limit) sum -= taken.poll();         // undo the most expensive choice so far
        }
        return taken.size();
    }
}

/*@LC 502
tag: Sort by capital + max-heap of profits
cue: Pick at most k projects; each needs capital you already have and adds its profit.
hook: Unlock everything you can afford, take the fattest, repeat k times.
time: O(n log n) time · O(n) space
trap: If nothing is affordable, stop early: the heap is empty.
var: Infosys RPG "defeat the most monsters" | Sort by power, walk while experience ≥ power (no heap: every bonus is ≥ 0).
@*/
class LC502 {
    public int findMaximizedCapital(int k, int w, int[] profits, int[] capital) {
        int n = profits.length;
        int[][] proj = new int[n][];
        for (int i = 0; i < n; i++) proj[i] = new int[]{capital[i], profits[i]};
        Arrays.sort(proj, (a, b) -> Integer.compare(a[0], b[0]));                  //* by capital needed
        PriorityQueue<Integer> best = new PriorityQueue<>(Collections.reverseOrder());   //* affordable profits
        int i = 0;
        while (k-- > 0) {
            while (i < n && proj[i][0] <= w) best.offer(proj[i++][1]);              //* unlock
            if (best.isEmpty()) break;
            w += best.poll();                                                       //* take the biggest
        }
        return w;
    }
}

/*@LC 1642
tag: Min-heap of ladder climbs
cue: Bricks pay any climb, ladders pay one climb each; how far can you go?
hook: Give every climb a ladder; when ladders run out, the smallest ladder climb pays bricks.
time: O(n log L) time · O(L) space
trap: Return `i` (the building you are standing on) when bricks go negative, not `i + 1`.
@*/
class LC1642 {
    public int furthestBuilding(int[] heights, int bricks, int ladders) {
        PriorityQueue<Integer> climbs = new PriorityQueue<>();        // climbs currently using ladders
        for (int i = 0; i + 1 < heights.length; i++) {
            int d = heights[i + 1] - heights[i];
            if (d <= 0) continue;
            climbs.offer(d);                                          //* ladder for now
            if (climbs.size() > ladders) bricks -= climbs.poll();     //* smallest one pays bricks
            if (bricks < 0) return i;                                 //*
        }
        return heights.length - 1;
    }
}

/*@LC 1526
tag: Count the rises
cue: Fewest "add 1 to a subarray" operations to build target from zeros.
hook: Every rise starts new operations; a fall just ends some.
time: O(n) time · O(1) space
trap: Start with `target[0]`: climbing from 0 to the first height counts too.
var: Start from an array `nums` instead of zeros | Run it on `d[i] = target[i] − nums[i]` when all `d[i] ≥ 0`.
@*/
class LC1526 {
    public int minNumberOperations(int[] target) {
        int ops = target[0];                                  //*
        for (int i = 1; i < target.length; i++)
            ops += Math.max(0, target[i] - target[i - 1]);    //* only rises cost
        return ops;
    }
}

/*@var LC 3229 · Minimum Operations to Make Array Equal to Target (+1 and −1 on subarrays)
note: Work on `d = target − nums`. Inside a run of the same sign, only growth in size costs; a sign change starts fresh.
@*/
class LC1526v1 {
    public long minimumOperations(int[] nums, int[] target) {
        long ops = 0;
        int prev = 0;
        for (int i = 0; i < nums.length; i++) {
            int d = target[i] - nums[i];
            if (d > 0 && prev > 0) ops += Math.max(0, d - prev);
            else if (d < 0 && prev < 0) ops += Math.max(0, prev - d);
            else ops += Math.abs(d);
            prev = d;
        }
        return ops;
    }
}

/*@LC 1046
tag: Max-heap simulation
cue: Smash the two heaviest stones until at most one is left.
hook: Poll two, push the difference.
time: O(n log n) time · O(n) space
trap: `PriorityQueue` is a MIN-heap; pass `Collections.reverseOrder()`.
var: LC 1049 Last Stone Weight II | Any pair, not the heaviest: that is subset-sum DP (Day 11).
@*/
class LC1046 {
    public int lastStoneWeight(int[] stones) {
        PriorityQueue<Integer> heap = new PriorityQueue<>(Collections.reverseOrder());   //*
        for (int s : stones) heap.offer(s);
        while (heap.size() > 1) {
            int y = heap.poll(), x = heap.poll();             //* the two heaviest
            if (y != x) heap.offer(y - x);
        }
        return heap.isEmpty() ? 0 : heap.peek();
    }
}

/*@var LC 2558 · Take Gifts From the Richest Pile
note: The same max-heap loop: k times, replace the top with its floor square root.
@*/
class LC1046v1 {
    public long pickGifts(int[] gifts, int k) {
        PriorityQueue<Integer> heap = new PriorityQueue<>(Collections.reverseOrder());
        for (int g : gifts) heap.offer(g);
        while (k-- > 0) heap.offer((int) Math.sqrt(heap.poll()));
        long sum = 0;
        for (int g : heap) sum += g;
        return sum;
    }
}

/*@LC 871
tag: Refuel in hindsight
cue: Fewest refuelling stops to reach target; stations along a line.
hook: Drive as far as you can; when stuck, pretend you stopped at the biggest station you passed.
time: O(n log n) time · O(n) space
trap: `reach` can pass 2³¹ (fuel values up to 10⁹), so keep it in a `long`.
@*/
class LC871 {
    public int minRefuelStops(int target, int startFuel, int[][] stations) {
        PriorityQueue<Integer> passed = new PriorityQueue<>(Collections.reverseOrder());
        long reach = startFuel;
        int stops = 0, i = 0;
        while (reach < target) {
            while (i < stations.length && stations[i][0] <= reach) passed.offer(stations[i++][1]);   //*
            if (passed.isEmpty()) return -1;                                                    //* stuck
            reach += passed.poll();                                                             //* biggest so far
            stops++;
        }
        return stops;
    }
}

/*@var DP version: dp[t] = farthest reach with t stops
note: O(n²) and needs no heap. Loop t downwards so each station is used at most once (0/1 knapsack idea).
@*/
class LC871v1 {
    public int minRefuelStops(int target, int startFuel, int[][] stations) {
        int n = stations.length;
        long[] dp = new long[n + 1];
        dp[0] = startFuel;
        for (int i = 0; i < n; i++)
            for (int t = i; t >= 0; t--)
                if (dp[t] >= stations[i][0]) dp[t + 1] = Math.max(dp[t + 1], dp[t] + stations[i][1]);
        for (int t = 0; t <= n; t++) if (dp[t] >= target) return t;
        return -1;
    }
}

/*@LC 630
tag: Sort by deadline + max-heap undo
cue: Most courses you can finish, each with a duration and a last day.
hook: Take courses by deadline; when late, throw out the longest one taken.
time: O(n log n) time · O(n) space
trap: Drop the LONGEST course so far (it may be the current one), not the last one.
var: CF 1526C2 Potions | Drink every potion; when health < 0, undo the most negative one so far (min-heap).
@*/
class LC630 {
    public int scheduleCourse(int[][] courses) {
        Arrays.sort(courses, (a, b) -> Integer.compare(a[1], b[1]));                 //* by deadline
        PriorityQueue<Integer> taken = new PriorityQueue<>(Collections.reverseOrder());   // durations
        int time = 0;
        for (int[] c : courses) {
            taken.offer(c[0]);
            time += c[0];
            if (time > c[1]) time -= taken.poll();                                   //* late: drop the longest
        }
        return taken.size();
    }
}

/*@var LC 1353 · Maximum Number of Events That Can Be Attended
note: Walk the days; a min-heap holds end days of events that have started. Each day attend the one that ends first.
@*/
class LC630v1 {
    public int maxEvents(int[][] events) {
        Arrays.sort(events, (a, b) -> Integer.compare(a[0], b[0]));
        PriorityQueue<Integer> ends = new PriorityQueue<>();
        int i = 0, n = events.length, attended = 0;
        for (int day = 1; i < n || !ends.isEmpty(); day++) {
            if (ends.isEmpty() && i < n && events[i][0] > day) day = events[i][0];   // skip empty days
            while (i < n && events[i][0] == day) ends.offer(events[i++][1]);
            while (!ends.isEmpty() && ends.peek() < day) ends.poll();                // expired
            if (!ends.isEmpty()) { ends.poll(); attended++; }
        }
        return attended;
    }
}

public class Day05 {
    public static void main(String[] args) {
        T.eq("core unlock", Core05.unlockThenTake(new int[][]{{0, 1}, {1, 2}, {1, 3}}, 0, 2), 4L);
        T.eq("core undo", Core05.keepWithUndo(T.ints("[5,1,1,5,1]"), 4), 3);

        T.eq("502a", new LC502().findMaximizedCapital(2, 0, T.ints("[1,2,3]"), T.ints("[0,1,1]")), 4);
        T.eq("502b", new LC502().findMaximizedCapital(3, 0, T.ints("[1,2,3]"), T.ints("[0,1,2]")), 6);
        T.eq("502c", new LC502().findMaximizedCapital(1, 0, T.ints("[1,2,3]"), T.ints("[1,1,2]")), 0);

        T.eq("1642a", new LC1642().furthestBuilding(T.ints("[4,2,7,6,9,14,12]"), 5, 1), 4);
        T.eq("1642b", new LC1642().furthestBuilding(T.ints("[4,12,2,7,3,18,20,3,19]"), 10, 2), 7);
        T.eq("1642c", new LC1642().furthestBuilding(T.ints("[14,3,19,3]"), 17, 0), 3);
        for (int t = 0; t < 300; t++) {
            int[] h = T.rand(T.rnd(1, 9), 1, 10);
            int b = T.rnd(0, 10), lad = T.rnd(0, 2), want = 0;
            for (int i = 1; i < h.length; i++) {
                List<Integer> c = new ArrayList<>();
                for (int j = 1; j <= i; j++) if (h[j] > h[j - 1]) c.add(h[j] - h[j - 1]);
                Collections.sort(c);
                int s = 0;
                for (int j = 0; j < c.size() - lad; j++) s += c.get(j);
                if (s <= b) want = i; else break;
            }
            T.eq("1642 rand", new LC1642().furthestBuilding(h, b, lad), want);
        }

        T.eq("1526a", new LC1526().minNumberOperations(T.ints("[1,2,3,2,1]")), 3);
        T.eq("1526b", new LC1526().minNumberOperations(T.ints("[3,1,1,2]")), 4);
        T.eq("1526c", new LC1526().minNumberOperations(T.ints("[3,1,5,4,2]")), 7);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 8), 0, 5);
            if (a[0] == 0) a[0] = 1;
            T.eq("1526 rand", new LC1526().minNumberOperations(a), bruteOps(a, 0, a.length - 1, 0));
        }
        T.eq("3229a", new LC1526v1().minimumOperations(T.ints("[3,5,1,2]"), T.ints("[4,6,2,4]")), 2L);
        T.eq("3229b", new LC1526v1().minimumOperations(T.ints("[1,3,2]"), T.ints("[2,1,4]")), 5L);

        T.eq("1046a", new LC1046().lastStoneWeight(T.ints("[2,7,4,1,8,1]")), 1);
        T.eq("1046b", new LC1046().lastStoneWeight(T.ints("[1]")), 1);
        T.eq("1046c", new LC1046().lastStoneWeight(T.ints("[2,2]")), 0);
        T.eq("2558a", new LC1046v1().pickGifts(T.ints("[25,64,9,4,100]"), 4), 29L);
        T.eq("2558b", new LC1046v1().pickGifts(T.ints("[1,1,1,1]"), 4), 4L);

        T.eq("871a", new LC871().minRefuelStops(1, 1, new int[0][]), 0);
        T.eq("871b", new LC871().minRefuelStops(100, 1, T.grid("[[10,100]]")), -1);
        T.eq("871c", new LC871().minRefuelStops(100, 10, T.grid("[[10,60],[20,30],[30,30],[60,40]]")), 2);
        for (int t = 0; t < 300; t++) {
            int n = T.rnd(0, 6), target = T.rnd(1, 40), fuel = T.rnd(1, 15);
            int[] pos = T.rand(n, 1, Math.max(1, target - 1));
            Arrays.sort(pos);
            int[][] st = new int[n][];
            for (int i = 0; i < n; i++) st[i] = new int[]{pos[i], T.rnd(1, 15)};
            T.eq("871 rand", new LC871().minRefuelStops(target, fuel, st), new LC871v1().minRefuelStops(target, fuel, st));
        }

        T.eq("630a", new LC630().scheduleCourse(T.grid("[[100,200],[200,1300],[1000,1250],[2000,3200]]")), 3);
        T.eq("630b", new LC630().scheduleCourse(T.grid("[[1,2]]")), 1);
        T.eq("630c", new LC630().scheduleCourse(T.grid("[[3,2],[4,3]]")), 0);
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 7);
            int[][] c = new int[n][];
            for (int i = 0; i < n; i++) c[i] = new int[]{T.rnd(1, 5), T.rnd(1, 12)};
            int best = 0;
            for (int mask = 0; mask < 1 << n; mask++) {
                List<int[]> ch = new ArrayList<>();
                for (int i = 0; i < n; i++) if ((mask >> i & 1) == 1) ch.add(c[i]);
                ch.sort((a, b) -> Integer.compare(a[1], b[1]));
                int time = 0; boolean ok = true;
                for (int[] x : ch) if ((time += x[0]) > x[1]) ok = false;
                if (ok) best = Math.max(best, ch.size());
            }
            int[][] copy = new int[n][];
            for (int i = 0; i < n; i++) copy[i] = c[i].clone();
            T.eq("630 rand", new LC630().scheduleCourse(copy), best);
        }
        T.eq("1353a", new LC630v1().maxEvents(T.grid("[[1,2],[2,3],[3,4]]")), 3);
        T.eq("1353b", new LC630v1().maxEvents(T.grid("[[1,2],[2,3],[3,4],[1,2]]")), 4);
        T.eq("1353c", new LC630v1().maxEvents(T.grid("[[1,5],[1,5],[1,5],[2,3],[2,3]]")), 5);
        T.eq("1353d", new LC630v1().maxEvents(T.grid("[[100000,100000]]")), 1);
        T.summary("Day05");
    }

    // fewest +1 subarray operations, by splitting at the minimum (slow but obviously right)
    static int bruteOps(int[] a, int l, int r, int base) {
        if (l > r) return 0;
        int m = l;
        for (int i = l; i <= r; i++) if (a[i] < a[m]) m = i;
        return a[m] - base + bruteOps(a, l, m - 1, a[m]) + bruteOps(a, m + 1, r, a[m]);
    }
}
