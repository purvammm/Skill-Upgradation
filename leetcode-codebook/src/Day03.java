import java.util.*;

/*@core Variable sliding window: grow right, shrink left while broken
note: Works when the rule is monotone: if a window breaks the rule, every bigger window around it breaks it too (all numbers ≥ 0, "at most k" counts). Longest: update after shrinking. Shortest: update inside the shrink loop.
@*/
class Core03 {
    static int longestAtMostKDistinct(int[] a, int k) {
        Map<Integer, Integer> cnt = new HashMap<>();
        int best = 0;
        for (int l = 0, r = 0; r < a.length; r++) {
            cnt.merge(a[r], 1, Integer::sum);              // 1. add a[r]
            while (cnt.size() > k) {                       // 2. broken? shrink from the left
                if (cnt.merge(a[l], -1, Integer::sum) == 0) cnt.remove(a[l]);
                l++;
            }
            best = Math.max(best, r - l + 1);              // 3. valid window [l..r]
        }
        return best;
    }

    static int shortestSumAtLeast(int[] a, int target) {  // LC 209, all a[i] > 0
        int best = Integer.MAX_VALUE, sum = 0;
        for (int l = 0, r = 0; r < a.length; r++) {
            sum += a[r];
            while (sum >= target) {                        // valid: record, then try smaller
                best = Math.min(best, r - l + 1);
                sum -= a[l++];
            }
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }
}

/*@LC 3
tag: Sliding window · counts
cue: Longest substring with no repeated character.
hook: Grow right; while the new char is doubled, shrink left.
time: O(n) time · O(128) space
trap: Update the answer AFTER the shrink loop, when the window is valid again.
@*/
class LC3 {
    public int lengthOfLongestSubstring(String s) {
        int[] cnt = new int[128];
        int best = 0;
        for (int l = 0, r = 0; r < s.length(); r++) {
            char c = s.charAt(r);
            cnt[c]++;                                       //* grow right
            while (cnt[c] > 1) cnt[s.charAt(l++)]--;        //* shrink while broken
            best = Math.max(best, r - l + 1);               //* valid here
        }
        return best;
    }
}

/*@var Jump version: remember the last index of each character
note: Instead of shrinking one by one, jump `l` past the previous copy of `c`. `Math.max` stops `l` from moving backwards.
@*/
class LC3v1 {
    public int lengthOfLongestSubstring(String s) {
        int[] last = new int[128];                          // last[c] = 1 + last index of c, 0 = never
        int best = 0;
        for (int l = 0, r = 0; r < s.length(); r++) {
            char c = s.charAt(r);
            l = Math.max(l, last[c]);
            last[c] = r + 1;
            best = Math.max(best, r - l + 1);
        }
        return best;
    }
}

/*@LC 1004
tag: Sliding window · at most k bad
cue: Longest run of 1s if you may flip at most k zeros.
hook: Count the zeros inside; more than k, walk the left end past one.
time: O(n) time · O(1) space
trap: Do not actually flip anything. The window just holds at most k zeros.
var: LC 1493 Longest Subarray of 1's After Deleting One Element | k = 1, return `best − 1` (one element must be deleted).
var: Infosys "Andy's vacation" | Days with obligations are the zeros; cancel at most K of them.
@*/
class LC1004 {
    public int longestOnes(int[] nums, int k) {
        int best = 0, zeros = 0;
        for (int l = 0, r = 0; r < nums.length; r++) {
            if (nums[r] == 0) zeros++;                      //*
            while (zeros > k) if (nums[l++] == 0) zeros--;  //*
            best = Math.max(best, r - l + 1);
        }
        return best;
    }
}

/*@LC 424
tag: Sliding window · most frequent
cue: Longest substring that becomes one letter after at most k replacements.
hook: Window minus its most common letter must fit in k.
time: O(26 · n) time · O(26) space
trap: `maxCount` never has to go down: a smaller one can never give a longer answer.
var: LC 2024 Maximize the Confusion of an Exam | Run LC 1004's window twice: at most k 'F's, and at most k 'T's.
@*/
class LC424 {
    public int characterReplacement(String s, int k) {
        int[] cnt = new int[26];
        int best = 0, maxCount = 0;
        for (int l = 0, r = 0; r < s.length(); r++) {
            maxCount = Math.max(maxCount, ++cnt[s.charAt(r) - 'A']);      //*
            while (r - l + 1 - maxCount > k) cnt[s.charAt(l++) - 'A']--;   //* too many to replace
            best = Math.max(best, r - l + 1);
        }
        return best;
    }
}

/*@LC 15
tag: Sort + two pointers
cue: All unique triples that sum to 0.
hook: Sort, fix one, squeeze the other two, skip the twins.
time: O(n²) time · O(1) extra space
trap: Skip duplicates for i (compare with i − 1) and for l and r after each hit, or you print the same triple twice.
var: LC 259 3Sum Smaller (premium) | When `s < target`, all of l+1..r work with l: `count += r − l; l++`.
@*/
class LC15 {
    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);                                              //*
        List<List<Integer>> res = new ArrayList<>();
        for (int i = 0; i < nums.length - 2; i++) {
            if (i > 0 && nums[i] == nums[i - 1]) continue;              //* skip twin for i
            if (nums[i] > 0) break;                                     // smallest is positive: done
            int l = i + 1, r = nums.length - 1;
            while (l < r) {
                int s = nums[i] + nums[l] + nums[r];
                if (s < 0) l++;                                         //* too small: move l right
                else if (s > 0) r--;                                    //* too big: move r left
                else {
                    res.add(Arrays.asList(nums[i], nums[l], nums[r]));
                    while (l < r && nums[l] == nums[l + 1]) l++;        //* skip twins for l and r
                    while (l < r && nums[r] == nums[r - 1]) r--;
                    l++; r--;
                }
            }
        }
        return res;
    }
}

/*@var LC 16 · 3Sum Closest
note: The same squeeze; keep the sum with the smallest |s − target|.
@*/
class LC15v1 {
    public int threeSumClosest(int[] nums, int target) {
        Arrays.sort(nums);
        int best = nums[0] + nums[1] + nums[2];
        for (int i = 0; i < nums.length - 2; i++) {
            int l = i + 1, r = nums.length - 1;
            while (l < r) {
                int s = nums[i] + nums[l] + nums[r];
                if (Math.abs(s - target) < Math.abs(best - target)) best = s;
                if (s < target) l++; else if (s > target) r--; else return s;
            }
        }
        return best;
    }
}

/*@var LC 18 · 4Sum
note: One more outer loop with its own twin skip. The sum needs `long`: four values of 10⁹ overflow an int.
@*/
class LC15v2 {
    public List<List<Integer>> fourSum(int[] nums, int target) {
        Arrays.sort(nums);
        List<List<Integer>> res = new ArrayList<>();
        int n = nums.length;
        for (int a = 0; a < n - 3; a++) {
            if (a > 0 && nums[a] == nums[a - 1]) continue;
            for (int b = a + 1; b < n - 2; b++) {
                if (b > a + 1 && nums[b] == nums[b - 1]) continue;
                int l = b + 1, r = n - 1;
                while (l < r) {
                    long s = (long) nums[a] + nums[b] + nums[l] + nums[r];
                    if (s < target) l++;
                    else if (s > target) r--;
                    else {
                        res.add(Arrays.asList(nums[a], nums[b], nums[l], nums[r]));
                        while (l < r && nums[l] == nums[l + 1]) l++;
                        while (l < r && nums[r] == nums[r - 1]) r--;
                        l++; r--;
                    }
                }
            }
        }
        return res;
    }
}

/*@LC 992
tag: Exactly K = atMost(K) − atMost(K−1)
cue: Count subarrays with EXACTLY k distinct values.
hook: Exactly is hard, at most is a window: subtract two windows.
time: O(n) time · O(n) space
trap: In `atMost`, add `r − l + 1` (every subarray ending at r), not 1.
@*/
class LC992 {
    public int subarraysWithKDistinct(int[] nums, int k) {
        return atMost(nums, k) - atMost(nums, k - 1);           //*
    }

    private int atMost(int[] a, int k) {                        // subarrays with <= k distinct
        int[] cnt = new int[a.length + 1];                      // values are 1..n
        int distinct = 0, total = 0;
        for (int l = 0, r = 0; r < a.length; r++) {
            if (cnt[a[r]]++ == 0) distinct++;
            while (distinct > k) if (--cnt[a[l++]] == 0) distinct--;
            total += r - l + 1;                                 //* all subarrays ending at r
        }
        return total;
    }
}

/*@var LC 1248 · Count Number of Nice Subarrays (exactly k odd numbers)
note: The same subtraction; the window counts odd numbers instead of distinct values. (LC 930 is the same with sums of 0/1.)
@*/
class LC992v1 {
    public int numberOfSubarrays(int[] nums, int k) {
        return atMost(nums, k) - atMost(nums, k - 1);
    }

    private int atMost(int[] a, int k) {
        int odd = 0, total = 0;
        for (int l = 0, r = 0; r < a.length; r++) {
            odd += a[r] & 1;
            while (odd > k) odd -= a[l++] & 1;
            total += r - l + 1;
        }
        return total;
    }
}

/*@LC 239
tag: Monotonic deque
cue: The maximum of every window of size k.
hook: Kick out the old from the front, kick out the small from the back; the front is the max.
time: O(n) time · O(k) space
trap: Store INDICES in the deque, so you can tell when the front has left the window.
var: Window minimum | Flip the second comparison to `nums[dq.peekLast()] >= nums[i]`.
@*/
class LC239 {
    public int[] maxSlidingWindow(int[] nums, int k) {
        int n = nums.length;
        int[] ans = new int[n - k + 1];
        Deque<Integer> dq = new ArrayDeque<>();                 // indices; their values decrease front -> back
        for (int i = 0; i < n; i++) {
            while (!dq.isEmpty() && dq.peekFirst() <= i - k) dq.pollFirst();          //* left the window
            while (!dq.isEmpty() && nums[dq.peekLast()] <= nums[i]) dq.pollLast();    //* smaller: never the max again
            dq.offerLast(i);
            if (i >= k - 1) ans[i - k + 1] = nums[dq.peekFirst()];                    //* front = max
        }
        return ans;
    }
}

/*@var LC 1438 · Longest Subarray with |max − min| ≤ limit
note: A variable window plus TWO deques (one for max, one for min). Shrink while `max − min > limit`.
@*/
class LC239v1 {
    public int longestSubarray(int[] nums, int limit) {
        Deque<Integer> mx = new ArrayDeque<>(), mn = new ArrayDeque<>();   // values
        int best = 0;
        for (int l = 0, r = 0; r < nums.length; r++) {
            while (!mx.isEmpty() && mx.peekLast() < nums[r]) mx.pollLast();
            while (!mn.isEmpty() && mn.peekLast() > nums[r]) mn.pollLast();
            mx.offerLast(nums[r]);
            mn.offerLast(nums[r]);
            while (mx.peekFirst() - mn.peekFirst() > limit) {
                if (mx.peekFirst() == nums[l]) mx.pollFirst();   // nums[l] leaves: drop it if it is a front
                if (mn.peekFirst() == nums[l]) mn.pollFirst();
                l++;
            }
            best = Math.max(best, r - l + 1);
        }
        return best;
    }
}

/*@var LC 862 · Shortest Subarray with Sum at Least K (negatives allowed)
note: Negatives break the plain window. Use prefix sums and a deque of indices with increasing prefix values.
@*/
class LC239v2 {
    public int shortestSubarray(int[] nums, int k) {
        int n = nums.length;
        long[] P = new long[n + 1];
        for (int i = 0; i < n; i++) P[i + 1] = P[i] + nums[i];
        Deque<Integer> dq = new ArrayDeque<>();
        int best = n + 1;
        for (int i = 0; i <= n; i++) {
            while (!dq.isEmpty() && P[i] - P[dq.peekFirst()] >= k) best = Math.min(best, i - dq.pollFirst());
            while (!dq.isEmpty() && P[dq.peekLast()] >= P[i]) dq.pollLast();   // a later, lower start is better
            dq.offerLast(i);
        }
        return best <= n ? best : -1;
    }
}

/*@LC 76
tag: Sliding window · need vs have
cue: Smallest window of s that contains every character of t (with counts).
hook: Count what is missing; at zero, shrink and record.
time: O(|s| + |t|) time · O(128) space
trap: Record the answer INSIDE the shrink loop (before removing s[l]), because that is when the window is valid.
@*/
class LC76 {
    public String minWindow(String s, String t) {
        int[] need = new int[128];
        for (char c : t.toCharArray()) need[c]++;
        int missing = t.length(), bestL = 0, bestLen = Integer.MAX_VALUE;
        for (int l = 0, r = 0; r < s.length(); r++) {
            if (need[s.charAt(r)]-- > 0) missing--;             //* we still needed this char
            while (missing == 0) {                              //* valid: record, then shrink
                if (r - l + 1 < bestLen) { bestLen = r - l + 1; bestL = l; }
                if (++need[s.charAt(l++)] > 0) missing++;       //* dropped a char we need
            }
        }
        return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestL, bestL + bestLen);
    }
}

/*@var LC 567 · Permutation in String
note: The same `missing` counter on a FIXED window of length |s1|.
@*/
class LC76v1 {
    public boolean checkInclusion(String s1, String s2) {
        int[] need = new int[26];
        for (char c : s1.toCharArray()) need[c - 'a']++;
        int missing = s1.length(), m = s1.length();
        for (int r = 0; r < s2.length(); r++) {
            if (need[s2.charAt(r) - 'a']-- > 0) missing--;
            if (r >= m && ++need[s2.charAt(r - m) - 'a'] > 0) missing++;   // char leaving the window
            if (missing == 0) return true;
        }
        return false;
    }
}

public class Day03 {
    public static void main(String[] args) {
        T.eq("core distinct", Core03.longestAtMostKDistinct(T.ints("[1,2,1,2,3,3]"), 2), 4);
        T.eq("core 209a", Core03.shortestSumAtLeast(T.ints("[2,3,1,2,4,3]"), 7), 2);
        T.eq("core 209b", Core03.shortestSumAtLeast(T.ints("[1,1,1,1]"), 11), 0);

        String[] ss = {"abcabcbb", "bbbbb", "pwwkew", "", " ", "dvdf", "abba", "tmmzuxt"};
        int[] sw = {3, 1, 3, 0, 1, 3, 2, 5};
        for (int i = 0; i < ss.length; i++) {
            T.eq("3 " + ss[i], new LC3().lengthOfLongestSubstring(ss[i]), sw[i]);
            T.eq("3v " + ss[i], new LC3v1().lengthOfLongestSubstring(ss[i]), sw[i]);
        }

        T.eq("1004a", new LC1004().longestOnes(T.ints("[1,1,1,0,0,0,1,1,1,1,0]"), 2), 6);
        T.eq("1004b", new LC1004().longestOnes(T.ints("[0,0,1,1,0,0,1,1,1,0,1,1,0,0,0,1,1,1,1]"), 3), 10);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 12), 0, 1);
            int k = T.rnd(0, 3), best = 0;
            for (int i = 0; i < a.length; i++) { int z = 0; for (int j = i; j < a.length; j++) { if (a[j] == 0) z++; if (z <= k) best = Math.max(best, j - i + 1); } }
            T.eq("1004 rand", new LC1004().longestOnes(a, k), best);
        }

        T.eq("424a", new LC424().characterReplacement("ABAB", 2), 4);
        T.eq("424b", new LC424().characterReplacement("AABABBA", 1), 4);
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 10), k = T.rnd(0, 3), best = 0;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) sb.append((char) ('A' + T.rnd(0, 2)));
            String s = sb.toString();
            for (int i = 0; i < n; i++) { int[] c = new int[26]; int mx = 0; for (int j = i; j < n; j++) { mx = Math.max(mx, ++c[s.charAt(j) - 'A']); if (j - i + 1 - mx <= k) best = Math.max(best, j - i + 1); } }
            T.eq("424 rand", new LC424().characterReplacement(s, k), best);
        }

        T.eq("15a", T.canon(new LC15().threeSum(T.ints("[-1,0,1,2,-1,-4]"))), "[[-1, -1, 2], [-1, 0, 1]]");
        T.eq("15b", new LC15().threeSum(T.ints("[0,1,1]")).size(), 0);
        T.eq("15c", T.canon(new LC15().threeSum(T.ints("[0,0,0]"))), "[[0, 0, 0]]");
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(3, 9), -4, 4);
            Set<List<Integer>> want = new HashSet<>();
            for (int i = 0; i < a.length; i++) for (int j = i + 1; j < a.length; j++) for (int k = j + 1; k < a.length; k++)
                if (a[i] + a[j] + a[k] == 0) { int[] x = {a[i], a[j], a[k]}; Arrays.sort(x); want.add(List.of(x[0], x[1], x[2])); }
            List<List<Integer>> got = new LC15().threeSum(a.clone());
            T.eq("15 rand", T.canon(got) + got.size(), T.canon(want) + want.size());
        }
        T.eq("16a", new LC15v1().threeSumClosest(T.ints("[-1,2,1,-4]"), 1), 2);
        T.eq("16b", new LC15v1().threeSumClosest(T.ints("[0,0,0]"), 1), 0);
        T.eq("18a", T.canon(new LC15v2().fourSum(T.ints("[1,0,-1,0,-2,2]"), 0)),
             T.canon(List.of(List.of(-2, -1, 1, 2), List.of(-2, 0, 0, 2), List.of(-1, 0, 0, 1))));
        T.eq("18b", T.canon(new LC15v2().fourSum(T.ints("[2,2,2,2,2]"), 8)), "[[2, 2, 2, 2]]");
        T.eq("18c", new LC15v2().fourSum(T.ints("[1000000000,1000000000,1000000000,1000000000]"), -294967296).size(), 0);

        T.eq("992a", new LC992().subarraysWithKDistinct(T.ints("[1,2,1,2,3]"), 2), 7);
        T.eq("992b", new LC992().subarraysWithKDistinct(T.ints("[1,2,1,3,4]"), 3), 3);
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 10);
            int[] a = T.rand(n, 1, Math.min(n, 4));
            int k = T.rnd(1, 4), c = 0;
            for (int i = 0; i < n; i++) { Set<Integer> s = new HashSet<>(); for (int j = i; j < n; j++) { s.add(a[j]); if (s.size() == k) c++; } }
            T.eq("992 rand", new LC992().subarraysWithKDistinct(a, k), c);
        }
        T.eq("1248a", new LC992v1().numberOfSubarrays(T.ints("[1,1,2,1,1]"), 3), 2);
        T.eq("1248b", new LC992v1().numberOfSubarrays(T.ints("[2,4,6]"), 1), 0);
        T.eq("1248c", new LC992v1().numberOfSubarrays(T.ints("[2,2,2,1,2,2,1,2,2,2]"), 2), 16);

        T.eq("239a", new LC239().maxSlidingWindow(T.ints("[1,3,-1,-3,5,3,6,7]"), 3), T.ints("[3,3,5,5,6,7]"));
        T.eq("239b", new LC239().maxSlidingWindow(T.ints("[1]"), 1), T.ints("[1]"));
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 12), k = T.rnd(1, n);
            int[] a = T.rand(n, -5, 5), want = new int[n - k + 1];
            for (int i = 0; i + k <= n; i++) { int m = Integer.MIN_VALUE; for (int j = i; j < i + k; j++) m = Math.max(m, a[j]); want[i] = m; }
            T.eq("239 rand", new LC239().maxSlidingWindow(a, k), want);
        }
        T.eq("1438a", new LC239v1().longestSubarray(T.ints("[8,2,4,7]"), 4), 2);
        T.eq("1438b", new LC239v1().longestSubarray(T.ints("[10,1,2,4,7,2]"), 5), 4);
        T.eq("1438c", new LC239v1().longestSubarray(T.ints("[4,2,2,2,4,4,2,2]"), 0), 3);
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 12), lim = T.rnd(0, 4), best = 0;
            int[] a = T.rand(n, 0, 6);
            for (int i = 0; i < n; i++) { int mx = a[i], mn = a[i]; for (int j = i; j < n; j++) { mx = Math.max(mx, a[j]); mn = Math.min(mn, a[j]); if (mx - mn <= lim) best = Math.max(best, j - i + 1); } }
            T.eq("1438 rand", new LC239v1().longestSubarray(a, lim), best);
        }
        T.eq("862a", new LC239v2().shortestSubarray(T.ints("[1]"), 1), 1);
        T.eq("862b", new LC239v2().shortestSubarray(T.ints("[1,2]"), 4), -1);
        T.eq("862c", new LC239v2().shortestSubarray(T.ints("[2,-1,2]"), 3), 3);
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 12), k = T.rnd(1, 10), best = Integer.MAX_VALUE;
            int[] a = T.rand(n, -4, 6);
            for (int i = 0; i < n; i++) { int s = 0; for (int j = i; j < n; j++) if ((s += a[j]) >= k) best = Math.min(best, j - i + 1); }
            T.eq("862 rand", new LC239v2().shortestSubarray(a, k), best == Integer.MAX_VALUE ? -1 : best);
        }

        T.eq("76a", new LC76().minWindow("ADOBECODEBANC", "ABC"), "BANC");
        T.eq("76b", new LC76().minWindow("a", "a"), "a");
        T.eq("76c", new LC76().minWindow("a", "aa"), "");
        T.eq("567a", new LC76v1().checkInclusion("ab", "eidbaooo"), true);
        T.eq("567b", new LC76v1().checkInclusion("ab", "eidboaoo"), false);
        T.eq("567c", new LC76v1().checkInclusion("adc", "dcda"), true);
        T.summary("Day03");
    }
}
