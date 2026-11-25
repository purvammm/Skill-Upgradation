import java.util.*;

/*@core Intervals: which end do you sort by?
note: Merge or insert → sort by START. Keep the most / fewest arrows / remove the fewest → sort by END and keep greedily. How many overlap at once → sort starts and ends separately (or +1/−1 events).
@*/
class Core04 {
    static int maxOverlap(int[][] iv) {                // closed intervals [s, e]
        List<int[]> ev = new ArrayList<>();
        for (int[] x : iv) { ev.add(new int[]{x[0], +1}); ev.add(new int[]{x[1] + 1, -1}); }
        ev.sort((a, b) -> a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));   // -1 before +1 at a tie
        int cur = 0, best = 0;
        for (int[] e : ev) best = Math.max(best, cur += e[1]);
        return best;
    }
}

/*@LC 56
tag: Sort by start · merge
cue: Merge all overlapping intervals.
hook: Sort by start; overlap stretches the last one, a gap starts a new one.
time: O(n log n) time · O(n) space
trap: Extend with `Math.max(last[1], iv[1])`: an interval can sit completely inside the last one.
@*/
class LC56 {
    public int[][] merge(int[][] intervals) {
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));                //*
        List<int[]> out = new ArrayList<>();
        for (int[] iv : intervals) {
            int[] last = out.isEmpty() ? null : out.get(out.size() - 1);
            if (last != null && iv[0] <= last[1]) last[1] = Math.max(last[1], iv[1]);  //* overlap: stretch
            else out.add(iv);                                                          //* gap: new one
        }
        return out.toArray(new int[0][]);
    }
}

/*@var LC 57 · Insert Interval (input already sorted, no sort needed)
note: Three phases: copy the ones fully left, absorb the overlapping ones, copy the rest.
@*/
class LC56v1 {
    public int[][] insert(int[][] intervals, int[] newInterval) {
        List<int[]> out = new ArrayList<>();
        int i = 0, n = intervals.length, s = newInterval[0], e = newInterval[1];
        while (i < n && intervals[i][1] < s) out.add(intervals[i++]);      // ends before us
        while (i < n && intervals[i][0] <= e) {                           // overlaps us: absorb
            s = Math.min(s, intervals[i][0]);
            e = Math.max(e, intervals[i][1]);
            i++;
        }
        out.add(new int[]{s, e});
        while (i < n) out.add(intervals[i++]);
        return out.toArray(new int[0][]);
    }
}

/*@var LC 986 · Interval List Intersections
note: Two sorted lists, two pointers. The overlap is `[max(starts), min(ends)]`; advance the one that ends first.
@*/
class LC56v2 {
    public int[][] intervalIntersection(int[][] firstList, int[][] secondList) {
        List<int[]> out = new ArrayList<>();
        int i = 0, j = 0;
        while (i < firstList.length && j < secondList.length) {
            int lo = Math.max(firstList[i][0], secondList[j][0]);
            int hi = Math.min(firstList[i][1], secondList[j][1]);
            if (lo <= hi) out.add(new int[]{lo, hi});
            if (firstList[i][1] < secondList[j][1]) i++; else j++;
        }
        return out.toArray(new int[0][]);
    }
}

/*@LC 435
tag: Sort by end · keep greedily
cue: Remove the fewest intervals so the rest do not overlap.
hook: Finish earliest, keep the most; removed = n − kept.
time: O(n log n) time · O(1) space
trap: Sorting by START fails: one long early interval would block many short ones.
var: LC 646 Maximum Length of Pair Chain | Same loop, but chains need `p[0] > end` (strict); return `kept`.
@*/
class LC435 {
    public int eraseOverlapIntervals(int[][] intervals) {
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[1], b[1]));   //* by END
        int kept = 0;
        long end = Long.MIN_VALUE;
        for (int[] iv : intervals) {
            if (iv[0] >= end) { kept++; end = iv[1]; }                 //* fits after the last kept one
        }
        return intervals.length - kept;
    }
}

/*@LC 2406
tag: Minimum platforms
cue: Fewest groups so that no two intervals in a group overlap (closed intervals).
hook: Sort starts and ends; a start after the earliest end reuses a group, otherwise open one.
time: O(n log n) time · O(n) space
trap: Intervals are inclusive, so reuse needs `start > end` (strictly). For half-open meetings (LC 253) use `>=`.
@*/
class LC2406 {
    public int minGroups(int[][] intervals) {
        int n = intervals.length;
        int[] starts = new int[n], ends = new int[n];
        for (int i = 0; i < n; i++) { starts[i] = intervals[i][0]; ends[i] = intervals[i][1]; }
        Arrays.sort(starts);                                   //*
        Arrays.sort(ends);                                     //*
        int groups = 0, j = 0;
        for (int i = 0; i < n; i++) {
            if (starts[i] > ends[j]) j++;                      //* a group finished: reuse it
            else groups++;                                     //* all busy: open a new group
        }
        return groups;
    }
}

/*@var Heap version (also gives which group to reuse)
note: Sort by start; a min-heap holds the end of each open group. Its size at the end is the answer.
@*/
class LC2406v1 {
    public int minGroups(int[][] intervals) {
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));
        PriorityQueue<Integer> ends = new PriorityQueue<>();
        for (int[] iv : intervals) {
            if (!ends.isEmpty() && ends.peek() < iv[0]) ends.poll();   // the group that ends first is free
            ends.offer(iv[1]);
        }
        return ends.size();
    }
}

/*@LC 179
tag: Custom comparator
cue: Arrange numbers to form the largest number.
hook: Put a before b when a+b beats b+a.
time: O(n log n · L) time · O(n · L) space
trap: All zeros give "000"; return "0".
var: Smallest number | Use `(a + b).compareTo(b + a)` and strip leading zeros.
@*/
class LC179 {
    public String largestNumber(int[] nums) {
        String[] s = new String[nums.length];
        for (int i = 0; i < nums.length; i++) s[i] = String.valueOf(nums[i]);
        Arrays.sort(s, (a, b) -> (b + a).compareTo(a + b));    //* a first if a+b > b+a
        if (s[0].equals("0")) return "0";                      //*
        return String.join("", s);
    }
}

/*@LC 1798
tag: Greedy reach
cue: How many consecutive values 0, 1, 2 ... can the coins make?
hook: Sort; if the next coin fits inside reach + 1, reach grows by it; otherwise there is a hole.
time: O(n log n) time · O(1) space
trap: The answer counts 0 too, so return `reach + 1`.
@*/
class LC1798 {
    public int getMaximumConsecutive(int[] coins) {
        Arrays.sort(coins);
        int reach = 0;                               //* every value 0..reach can be made
        for (int c : coins) {
            if (c > reach + 1) break;                //* reach + 1 can never be made
            reach += c;                              //*
        }
        return reach + 1;
    }
}

/*@var LC 330 · Patching Array
note: The same reach; when the next number does not fit, the best patch is `reach + 1`, which doubles the reach.
@*/
class LC1798v1 {
    public int minPatches(int[] nums, int n) {
        long reach = 0;                              // can make 1..reach
        int i = 0, patches = 0;
        while (reach < n) {
            if (i < nums.length && nums[i] <= reach + 1) reach += nums[i++];
            else { reach += reach + 1; patches++; }
        }
        return patches;
    }
}

/*@LC 452
tag: Sort by end · arrows
cue: Fewest arrows to burst all balloons (touching counts).
hook: Shoot at the end of the first balloon; everything starting before it is gone.
time: O(n log n) time · O(1) space
trap: Values reach ±2³¹: compare with `Integer.compare`, never `a[1] − b[1]`.
@*/
class LC452 {
    public int findMinArrowShots(int[][] points) {
        Arrays.sort(points, (a, b) -> Integer.compare(a[1], b[1]));   //* by end, no subtraction
        int arrows = 0;
        long arrowAt = Long.MIN_VALUE;
        for (int[] p : points) {
            if (p[0] > arrowAt) { arrows++; arrowAt = p[1]; }          //* not hit: new arrow at its end
        }
        return arrows;
    }
}

/*@LC 135
tag: Two passes
cue: Each child with a higher rating than a neighbour gets more candy; minimise the total.
hook: Left pass fixes left neighbours, right pass fixes right neighbours, keep the max.
time: O(n) time · O(n) space
trap: In the right pass use `Math.max`, or you break what the left pass fixed.
var: Same left/right idea | LC 42 Trapping Rain Water and LC 238 Product Except Self (Bonus A).
@*/
class LC135 {
    public int candy(int[] ratings) {
        int n = ratings.length;
        int[] c = new int[n];
        Arrays.fill(c, 1);
        for (int i = 1; i < n; i++)
            if (ratings[i] > ratings[i - 1]) c[i] = c[i - 1] + 1;                 //*
        for (int i = n - 2; i >= 0; i--)
            if (ratings[i] > ratings[i + 1]) c[i] = Math.max(c[i], c[i + 1] + 1); //*
        int sum = 0;
        for (int x : c) sum += x;
        return sum;
    }
}

public class Day04 {
    public static void main(String[] args) {
        T.eq("core overlap", Core04.maxOverlap(T.grid("[[1,5],[2,3],[5,6],[7,8]]")), 2);
        T.eq("core overlap2", Core04.maxOverlap(T.grid("[[1,5],[2,5],[5,6]]")), 3);

        T.eq("56a", new LC56().merge(T.grid("[[1,3],[2,6],[8,10],[15,18]]")), T.grid("[[1,6],[8,10],[15,18]]"));
        T.eq("56b", new LC56().merge(T.grid("[[1,4],[4,5]]")), T.grid("[[1,5]]"));
        T.eq("56c", new LC56().merge(T.grid("[[4,7],[1,4]]")), T.grid("[[1,7]]"));
        T.eq("56d", new LC56().merge(T.grid("[[1,10],[2,3]]")), T.grid("[[1,10]]"));
        T.eq("57a", new LC56v1().insert(T.grid("[[1,3],[6,9]]"), T.ints("[2,5]")), T.grid("[[1,5],[6,9]]"));
        T.eq("57b", new LC56v1().insert(T.grid("[[1,2],[3,5],[6,7],[8,10],[12,16]]"), T.ints("[4,8]")), T.grid("[[1,2],[3,10],[12,16]]"));
        T.eq("57c", new LC56v1().insert(new int[0][], T.ints("[5,7]")), T.grid("[[5,7]]"));
        T.eq("986", new LC56v2().intervalIntersection(T.grid("[[0,2],[5,10],[13,23],[24,25]]"), T.grid("[[1,5],[8,12],[15,24],[25,26]]")),
             T.grid("[[1,2],[5,5],[8,10],[15,23],[24,24],[25,25]]"));

        T.eq("435a", new LC435().eraseOverlapIntervals(T.grid("[[1,2],[2,3],[3,4],[1,3]]")), 1);
        T.eq("435b", new LC435().eraseOverlapIntervals(T.grid("[[1,2],[1,2],[1,2]]")), 2);
        T.eq("435c", new LC435().eraseOverlapIntervals(T.grid("[[1,2],[2,3]]")), 0);
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 8);
            int[][] iv = new int[n][];
            for (int i = 0; i < n; i++) { int s = T.rnd(0, 10); iv[i] = new int[]{s, s + T.rnd(1, 5)}; }
            int best = 0;
            for (int mask = 0; mask < 1 << n; mask++) {
                List<int[]> ch = new ArrayList<>();
                for (int i = 0; i < n; i++) if ((mask >> i & 1) == 1) ch.add(iv[i]);
                ch.sort((a, b) -> Integer.compare(a[0], b[0]));
                boolean ok = true;
                for (int i = 1; i < ch.size(); i++) if (ch.get(i)[0] < ch.get(i - 1)[1]) ok = false;
                if (ok) best = Math.max(best, ch.size());
            }
            int[][] copy = new int[n][];
            for (int i = 0; i < n; i++) copy[i] = iv[i].clone();
            T.eq("435 rand", new LC435().eraseOverlapIntervals(copy), n - best);
        }

        T.eq("2406a", new LC2406().minGroups(T.grid("[[5,10],[6,8],[1,5],[2,3],[1,10]]")), 3);
        T.eq("2406b", new LC2406().minGroups(T.grid("[[1,3],[5,6],[8,10],[11,13]]")), 1);
        for (int t = 0; t < 300; t++) {
            int n = T.rnd(1, 10);
            int[][] iv = new int[n][];
            for (int i = 0; i < n; i++) { int s = T.rnd(1, 12); iv[i] = new int[]{s, s + T.rnd(0, 5)}; }
            int want = Core04.maxOverlap(iv);
            int[][] c1 = new int[n][], c2 = new int[n][];
            for (int i = 0; i < n; i++) { c1[i] = iv[i].clone(); c2[i] = iv[i].clone(); }
            T.eq("2406 rand", new LC2406().minGroups(c1), want);
            T.eq("2406v rand", new LC2406v1().minGroups(c2), want);
        }

        T.eq("179a", new LC179().largestNumber(T.ints("[10,2]")), "210");
        T.eq("179b", new LC179().largestNumber(T.ints("[3,30,34,5,9]")), "9534330");
        T.eq("179c", new LC179().largestNumber(T.ints("[0,0]")), "0");
        T.eq("179d", new LC179().largestNumber(T.ints("[432,43243]")), "43243432");

        T.eq("1798a", new LC1798().getMaximumConsecutive(T.ints("[1,3]")), 2);
        T.eq("1798b", new LC1798().getMaximumConsecutive(T.ints("[1,1,1,4]")), 8);
        T.eq("1798c", new LC1798().getMaximumConsecutive(T.ints("[1,4,10,3,1]")), 20);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 8), 1, 6);
            Set<Integer> sums = new HashSet<>();
            for (int mask = 0; mask < 1 << a.length; mask++) { int s = 0; for (int i = 0; i < a.length; i++) if ((mask >> i & 1) == 1) s += a[i]; sums.add(s); }
            int x = 0;
            while (sums.contains(x)) x++;
            T.eq("1798 rand", new LC1798().getMaximumConsecutive(a.clone()), x);
        }
        T.eq("330a", new LC1798v1().minPatches(T.ints("[1,3]"), 6), 1);
        T.eq("330b", new LC1798v1().minPatches(T.ints("[1,5,10]"), 20), 2);
        T.eq("330c", new LC1798v1().minPatches(T.ints("[1,2,2]"), 5), 0);

        T.eq("452a", new LC452().findMinArrowShots(T.grid("[[10,16],[2,8],[1,6],[7,12]]")), 2);
        T.eq("452b", new LC452().findMinArrowShots(T.grid("[[1,2],[3,4],[5,6],[7,8]]")), 4);
        T.eq("452c", new LC452().findMinArrowShots(T.grid("[[1,2],[2,3],[3,4],[4,5]]")), 2);
        T.eq("452d", new LC452().findMinArrowShots(new int[][]{{-2147483646, -2147483645}, {2147483646, 2147483647}}), 2);

        T.eq("135a", new LC135().candy(T.ints("[1,0,2]")), 5);
        T.eq("135b", new LC135().candy(T.ints("[1,2,2]")), 4);
        T.eq("135c", new LC135().candy(T.ints("[1,3,2,2,1]")), 7);
        T.eq("135d", new LC135().candy(T.ints("[1,2,87,87,87,2,1]")), 13);
        T.summary("Day04");
    }
}
