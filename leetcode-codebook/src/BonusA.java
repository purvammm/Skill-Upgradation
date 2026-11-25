import java.util.*;

/*@LC 53 · Maximum Subarray · Medium
group: A
tag: Kadane
cue: Largest sum of a non-empty contiguous subarray.
hook: Carry the run only while it helps: restart when the run is worse than the number alone.
time: O(n) time · O(1) space
trap: Start `best` at `nums[0]`, not 0: all numbers may be negative.
var: Return the indices | Remember where the current run started when you restart.
@*/
class LC53 {
    public int maxSubArray(int[] nums) {
        int cur = nums[0], best = nums[0];
        for (int i = 1; i < nums.length; i++) {
            cur = Math.max(nums[i], cur + nums[i]);          //* extend or restart
            best = Math.max(best, cur);                      //*
        }
        return best;
    }
}

/*@var LC 918 · Maximum Sum Circular Subarray
note: A wrapping subarray = total − (a minimum subarray in the middle). Run Kadane for max and for min; if all are negative, return the plain max.
@*/
class LC53v1 {
    public int maxSubarraySumCircular(int[] nums) {
        int total = 0, curMax = 0, curMin = 0, best = nums[0], worst = nums[0];
        for (int x : nums) {
            curMax = Math.max(x, curMax + x);
            best = Math.max(best, curMax);
            curMin = Math.min(x, curMin + x);
            worst = Math.min(worst, curMin);
            total += x;
        }
        return best < 0 ? best : Math.max(best, total - worst);
    }
}

/*@LC 238 · Product of Array Except Self · Medium
group: A
tag: Prefix and suffix products
cue: Each position gets the product of all other numbers, without division.
hook: Left products on the way in, right products on the way back.
time: O(n) time · O(1) extra (the output does not count)
trap: Division breaks on zeros; this two-pass version never divides.
var: Same idea | LC 42 below (left max and right max) and LC 135 Candy (Day 4).
@*/
class LC238 {
    public int[] productExceptSelf(int[] nums) {
        int n = nums.length;
        int[] ans = new int[n];
        ans[0] = 1;
        for (int i = 1; i < n; i++) ans[i] = ans[i - 1] * nums[i - 1];   //* product of everything left
        int right = 1;
        for (int i = n - 1; i >= 0; i--) {
            ans[i] *= right;                                            //* times everything right
            right *= nums[i];
        }
        return ans;
    }
}

/*@LC 128 · Longest Consecutive Sequence · Medium
group: A
tag: HashSet · start only at run beginnings
cue: Longest run of consecutive integers (any order) in O(n).
hook: Only start counting at x when x − 1 is missing; then walk up.
time: O(n) time · O(n) space
trap: Without the `x − 1` check it becomes O(n²) on a long run.
@*/
class LC128 {
    public int longestConsecutive(int[] nums) {
        Set<Integer> set = new HashSet<>();
        for (int x : nums) set.add(x);
        int best = 0;
        for (int x : set) {
            if (set.contains(x - 1)) continue;               //* not a run start
            int len = 1;
            while (set.contains(x + len)) len++;             //* walk up
            best = Math.max(best, len);
        }
        return best;
    }
}

/*@LC 11 · Container With Most Water · Medium
group: A
tag: Two pointers from both ends
cue: Two lines that hold the most water.
hook: Area is limited by the shorter line, so move the shorter one inward.
time: O(n) time · O(1) space
trap: Moving the taller line can never help: the width shrinks and the height cannot grow past the shorter line.
@*/
class LC11 {
    public int maxArea(int[] height) {
        int l = 0, r = height.length - 1, best = 0;
        while (l < r) {
            best = Math.max(best, Math.min(height[l], height[r]) * (r - l));   //*
            if (height[l] < height[r]) l++; else r--;                          //* move the shorter
        }
        return best;
    }
}

/*@LC 42 · Trapping Rain Water · Hard
group: A
tag: Two pointers with running maxima
cue: Water trapped between bars after rain.
hook: Water above a bar = min(tallest left, tallest right) − bar. The smaller side's max is already final.
time: O(n) time · O(1) space
trap: Move the side whose max is SMALLER; that side's water is decided.
@*/
class LC42 {
    public int trap(int[] height) {
        int l = 0, r = height.length - 1, leftMax = 0, rightMax = 0, water = 0;
        while (l < r) {
            leftMax = Math.max(leftMax, height[l]);
            rightMax = Math.max(rightMax, height[r]);
            if (leftMax < rightMax) water += leftMax - height[l++];      //* left side is decided
            else water += rightMax - height[r--];                        //* right side is decided
        }
        return water;
    }
}

/*@var Prefix-max arrays version (easier to get right under pressure)
note: Two arrays: tallest bar up to i from the left, and from the right. Water at i is the smaller one minus the bar.
@*/
class LC42v1 {
    public int trap(int[] height) {
        int n = height.length, water = 0;
        int[] left = new int[n], right = new int[n];
        for (int i = 0; i < n; i++) left[i] = Math.max(i > 0 ? left[i - 1] : 0, height[i]);
        for (int i = n - 1; i >= 0; i--) right[i] = Math.max(i + 1 < n ? right[i + 1] : 0, height[i]);
        for (int i = 0; i < n; i++) water += Math.min(left[i], right[i]) - height[i];
        return water;
    }
}

/*@LC 33 · Search in Rotated Sorted Array · Medium
group: A
tag: Binary search · one half is always sorted
cue: Find a target in a sorted array that was rotated (distinct values).
hook: One half of [lo, hi] is sorted; if the target is inside that half go there, otherwise go to the other half.
time: O(log n) time · O(1) space
trap: Use `nums[lo] <= nums[mid]` (with `=`) to detect a sorted left half when lo == mid.
@*/
class LC33 {
    public int search(int[] nums, int target) {
        int lo = 0, hi = nums.length - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (nums[mid] == target) return mid;
            if (nums[lo] <= nums[mid]) {                                       //* left half sorted
                if (nums[lo] <= target && target < nums[mid]) hi = mid - 1;
                else lo = mid + 1;
            } else {                                                           //* right half sorted
                if (nums[mid] < target && target <= nums[hi]) lo = mid + 1;
                else hi = mid - 1;
            }
        }
        return -1;
    }
}

/*@var LC 153 · Find Minimum in Rotated Sorted Array
note: Compare with the RIGHT end: if `nums[mid] > nums[hi]` the minimum is to the right of mid.
@*/
class LC33v1 {
    public int findMin(int[] nums) {
        int lo = 0, hi = nums.length - 1;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (nums[mid] > nums[hi]) lo = mid + 1; else hi = mid;
        }
        return nums[lo];
    }
}

/*@LC 55 · Jump Game · Medium
group: A
tag: Greedy reach
cue: From each index you may jump up to nums[i]; can you reach the end?
hook: Keep the farthest reachable index; if you ever stand beyond it, you are stuck.
time: O(n) time · O(1) space
trap: Check `i > reach` BEFORE using nums[i].
@*/
class LC55 {
    public boolean canJump(int[] nums) {
        int reach = 0;
        for (int i = 0; i < nums.length; i++) {
            if (i > reach) return false;                     //*
            reach = Math.max(reach, i + nums[i]);            //*
        }
        return true;
    }
}

/*@var LC 45 · Jump Game II (fewest jumps)
note: BFS by levels without a queue: `end` is the edge of the current level, `far` the edge of the next one.
@*/
class LC55v1 {
    public int jump(int[] nums) {
        int jumps = 0, end = 0, far = 0;
        for (int i = 0; i < nums.length - 1; i++) {
            far = Math.max(far, i + nums[i]);
            if (i == end) { jumps++; end = far; }            // leave the current level
        }
        return jumps;
    }
}

public class BonusA {
    public static void main(String[] args) {
        T.eq("53a", new LC53().maxSubArray(T.ints("[-2,1,-3,4,-1,2,1,-5,4]")), 6);
        T.eq("53b", new LC53().maxSubArray(T.ints("[1]")), 1);
        T.eq("53c", new LC53().maxSubArray(T.ints("[5,4,-1,7,8]")), 23);
        T.eq("53d", new LC53().maxSubArray(T.ints("[-3,-2]")), -2);
        T.eq("918a", new LC53v1().maxSubarraySumCircular(T.ints("[1,-2,3,-2]")), 3);
        T.eq("918b", new LC53v1().maxSubarraySumCircular(T.ints("[5,-3,5]")), 10);
        T.eq("918c", new LC53v1().maxSubarraySumCircular(T.ints("[-3,-2,-3]")), -2);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 9), -5, 5);
            int n = a.length, best = Integer.MIN_VALUE, circ = Integer.MIN_VALUE;
            for (int i = 0; i < n; i++) { int s = 0; for (int j = i; j < n; j++) best = Math.max(best, s += a[j]); }
            for (int i = 0; i < n; i++) { int s = 0; for (int len = 1; len <= n; len++) circ = Math.max(circ, s += a[(i + len - 1) % n]); }
            T.eq("53 rand", new LC53().maxSubArray(a), best);
            T.eq("918 rand", new LC53v1().maxSubarraySumCircular(a), circ);
        }

        T.eq("238a", new LC238().productExceptSelf(T.ints("[1,2,3,4]")), T.ints("[24,12,8,6]"));
        T.eq("238b", new LC238().productExceptSelf(T.ints("[-1,1,0,-3,3]")), T.ints("[0,0,9,0,0]"));

        T.eq("128a", new LC128().longestConsecutive(T.ints("[100,4,200,1,3,2]")), 4);
        T.eq("128b", new LC128().longestConsecutive(T.ints("[0,3,7,2,5,8,4,6,0,1]")), 9);
        T.eq("128c", new LC128().longestConsecutive(new int[0]), 0);

        T.eq("11a", new LC11().maxArea(T.ints("[1,8,6,2,5,4,8,3,7]")), 49);
        T.eq("11b", new LC11().maxArea(T.ints("[1,1]")), 1);
        for (int t = 0; t < 200; t++) {
            int[] h = T.rand(T.rnd(2, 10), 0, 9);
            int best = 0, water = 0;
            for (int i = 0; i < h.length; i++) for (int j = i + 1; j < h.length; j++) best = Math.max(best, Math.min(h[i], h[j]) * (j - i));
            for (int i = 0; i < h.length; i++) { int l = 0, r = 0; for (int j = 0; j <= i; j++) l = Math.max(l, h[j]); for (int j = i; j < h.length; j++) r = Math.max(r, h[j]); water += Math.min(l, r) - h[i]; }
            T.eq("11 rand", new LC11().maxArea(h), best);
            T.eq("42 rand", new LC42().trap(h), water);
            T.eq("42v rand", new LC42v1().trap(h), water);
        }
        T.eq("42a", new LC42().trap(T.ints("[0,1,0,2,1,0,1,3,2,1,2,1]")), 6);
        T.eq("42b", new LC42().trap(T.ints("[4,2,0,3,2,5]")), 9);

        T.eq("33a", new LC33().search(T.ints("[4,5,6,7,0,1,2]"), 0), 4);
        T.eq("33b", new LC33().search(T.ints("[4,5,6,7,0,1,2]"), 3), -1);
        T.eq("33c", new LC33().search(T.ints("[1]"), 0), -1);
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 9), rot = T.rnd(0, n - 1);
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = 2 * ((i + rot) % n);
            int target = T.rnd(-1, 2 * n), want = -1;
            for (int i = 0; i < n; i++) if (a[i] == target) want = i;
            T.eq("33 rand", new LC33().search(a, target), want);
            T.eq("153 rand", new LC33v1().findMin(a), 0);
        }

        T.eq("55a", new LC55().canJump(T.ints("[2,3,1,1,4]")), true);
        T.eq("55b", new LC55().canJump(T.ints("[3,2,1,0,4]")), false);
        T.eq("45a", new LC55v1().jump(T.ints("[2,3,1,1,4]")), 2);
        T.eq("45b", new LC55v1().jump(T.ints("[2,3,0,1,4]")), 2);
        T.eq("45c", new LC55v1().jump(T.ints("[0]")), 0);
        T.summary("BonusA");
    }
}
