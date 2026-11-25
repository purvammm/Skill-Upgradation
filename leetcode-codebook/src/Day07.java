import java.util.*;

/*@core Monotonic stack: everyone waiting for a bigger (or smaller) neighbour
note: The stack holds indices still waiting for their answer. Pop while the top is beaten by the current element; the current element IS the answer for whatever you pop. Next greater: pop while `a[top] < a[i]`. Next smaller: pop while `a[top] > a[i]`. After popping, the new top is the PREVIOUS greater-or-equal (or smaller-or-equal) element.
@*/
class Core07 {
    static int[] nextGreater(int[] a) {               // index of the next strictly greater element, -1 if none
        int n = a.length;
        int[] nxt = new int[n];
        Arrays.fill(nxt, -1);
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            while (!st.isEmpty() && a[st.peek()] < a[i]) nxt[st.pop()] = i;
            st.push(i);
        }
        return nxt;
    }

    static int[] previousSmaller(int[] a) {           // index of the previous strictly smaller element, -1 if none
        int n = a.length;
        int[] prev = new int[n];
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            while (!st.isEmpty() && a[st.peek()] >= a[i]) st.pop();
            prev[i] = st.isEmpty() ? -1 : st.peek();
            st.push(i);
        }
        return prev;
    }
}

/*@LC 739
tag: Next greater element
cue: For each day, how many days until a warmer one?
hook: Days wait on the stack; a warmer day pops them and answers them.
time: O(n) time · O(n) space
trap: Push INDICES, not temperatures: the answer is a distance.
var: LC 496 Next Greater Element I | Run this on nums2, store `value → next greater value` in a map, then look up nums1.
@*/
class LC739 {
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] ans = new int[n];
        Deque<Integer> st = new ArrayDeque<>();                                   // days still waiting
        for (int i = 0; i < n; i++) {
            while (!st.isEmpty() && temperatures[st.peek()] < temperatures[i]) { //*
                int j = st.pop();
                ans[j] = i - j;                                                   //* day i answers day j
            }
            st.push(i);                                                           //*
        }
        return ans;
    }
}

/*@var LC 901 · Online Stock Span
class: StockSpanner
note: Previous greater element, online. Each stack entry carries the span it already absorbed.
@*/
class LC739v1 {
    private final Deque<int[]> st = new ArrayDeque<>();   // {price, span}, prices decreasing

    public LC739v1() {}

    public int next(int price) {
        int span = 1;
        while (!st.isEmpty() && st.peek()[0] <= price) span += st.pop()[1];
        st.push(new int[]{price, span});
        return span;
    }
}

/*@LC 503
tag: Next greater · circular
cue: Next greater element when the array wraps around.
hook: Walk twice, push only on the first lap.
time: O(n) time · O(n) space
trap: Pushing during the second lap gives wrong answers for duplicates; push only when `i < n`.
@*/
class LC503 {
    public int[] nextGreaterElements(int[] nums) {
        int n = nums.length;
        int[] ans = new int[n];
        Arrays.fill(ans, -1);
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i < 2 * n; i++) {                            //* two laps
            int x = nums[i % n];                                     //*
            while (!st.isEmpty() && nums[st.peek()] < x) ans[st.pop()] = x;
            if (i < n) st.push(i);                                   //* push on lap one only
        }
        return ans;
    }
}

/*@LC 84
tag: Previous / next smaller bar
cue: Largest rectangle in a histogram.
hook: When a bar is popped, the current bar is its right wall and the new top is its left wall.
time: O(n) time · O(n) space
trap: Add a height-0 bar at the end (`i == n`) so every bar gets popped.
@*/
class LC84 {
    public int largestRectangleArea(int[] heights) {
        int n = heights.length, best = 0;
        Deque<Integer> st = new ArrayDeque<>();                      // increasing heights
        for (int i = 0; i <= n; i++) {
            int h = i == n ? 0 : heights[i];                         //* sentinel flushes the stack
            while (!st.isEmpty() && heights[st.peek()] >= h) {
                int height = heights[st.pop()];                      //* shortest bar of its rectangle
                int left = st.isEmpty() ? -1 : st.peek();            //* previous smaller bar
                best = Math.max(best, height * (i - left - 1));      //* i = next smaller bar
            }
            st.push(i);
        }
        return best;
    }
}

/*@var LC 85 · Maximal Rectangle (binary matrix)
note: Each row turns into a histogram of "1s stacked above". Reuse `largestRectangleArea` from the card above for every row.
@*/
class LC84v1 extends LC84 {
    public int maximalRectangle(char[][] matrix) {
        int n = matrix[0].length, best = 0;
        int[] h = new int[n];
        for (char[] row : matrix) {
            for (int j = 0; j < n; j++) h[j] = row[j] == '1' ? h[j] + 1 : 0;
            best = Math.max(best, largestRectangleArea(h));
        }
        return best;
    }
}

/*@LC 907
tag: Contribution counting
cue: Sum of the minimum of every subarray (mod 10⁹+7).
hook: Each element is the minimum of (choices on the left) × (choices on the right) subarrays.
time: O(n) time · O(n) space
trap: With equal values, use `>` on one side and `>=` on the other, so each subarray is counted exactly once.
var: Sum of subarray MAXIMUMS | Flip both comparisons (`<` and `<=`).
@*/
class LC907 {
    public int sumSubarrayMins(int[] arr) {
        final long MOD = 1_000_000_007L;
        int n = arr.length;
        int[] left = new int[n], right = new int[n];
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {                                    // left: back to the previous <= element
            while (!st.isEmpty() && arr[st.peek()] > arr[i]) st.pop();   //* strict here
            left[i] = st.isEmpty() ? i + 1 : i - st.peek();
            st.push(i);
        }
        st.clear();
        for (int i = n - 1; i >= 0; i--) {                               // right: up to the next < element
            while (!st.isEmpty() && arr[st.peek()] >= arr[i]) st.pop();  //* not strict here
            right[i] = st.isEmpty() ? n - i : st.peek() - i;
            st.push(i);
        }
        long sum = 0;
        for (int i = 0; i < n; i++)
            sum = (sum + (long) arr[i] * left[i] % MOD * right[i]) % MOD;   //* value × count
        return (int) sum;
    }
}

/*@LC 402
tag: Greedy stack · smallest number
cue: Remove k digits to make the smallest number.
hook: A bigger digit in front of a smaller one must go.
time: O(n) time · O(n) space
trap: Three clean-ups: k left over → cut from the end; strip leading zeros; empty → "0".
var: Largest number after removing k digits | Pop while the top is SMALLER than the current digit.
@*/
class LC402 {
    public String removeKdigits(String num, int k) {
        StringBuilder st = new StringBuilder();                               // the stack
        for (char c : num.toCharArray()) {
            while (k > 0 && st.length() > 0 && st.charAt(st.length() - 1) > c) {   //*
                st.deleteCharAt(st.length() - 1);
                k--;
            }
            st.append(c);
        }
        st.setLength(st.length() - k);                                        //* leftover k: cut the tail
        int i = 0;
        while (i < st.length() - 1 && st.charAt(i) == '0') i++;               //* leading zeros
        return st.length() == 0 ? "0" : st.substring(i);
    }
}

/*@var LC 1673 · Find the Most Competitive Subsequence (keep exactly k)
note: The same pop rule, but only while enough numbers remain to still fill k places.
@*/
class LC402v1 {
    public int[] mostCompetitive(int[] nums, int k) {
        int[] st = new int[k];
        int top = 0;
        for (int i = 0; i < nums.length; i++) {
            while (top > 0 && st[top - 1] > nums[i] && top - 1 + nums.length - i >= k) top--;
            if (top < k) st[top++] = nums[i];
        }
        return st;
    }
}

/*@LC 316
tag: Greedy stack + last index
cue: Remove duplicate letters; the result must be the smallest in dictionary order.
hook: Pop a bigger letter only if it comes again later.
time: O(n) time · O(26) space
trap: Skip a letter that is already in the stack, otherwise it appears twice.
var: LC 1081 Smallest Subsequence of Distinct Characters | Identical problem and code.
@*/
class LC316 {
    public String removeDuplicateLetters(String s) {
        int[] last = new int[26];
        for (int i = 0; i < s.length(); i++) last[s.charAt(i) - 'a'] = i;      //*
        boolean[] inStack = new boolean[26];
        StringBuilder st = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inStack[c - 'a']) continue;                                     //*
            while (st.length() > 0) {
                char top = st.charAt(st.length() - 1);
                if (top > c && last[top - 'a'] > i) {                           //* bigger AND comes again
                    st.deleteCharAt(st.length() - 1);
                    inStack[top - 'a'] = false;
                } else break;
            }
            st.append(c);
            inStack[c - 'a'] = true;
        }
        return st.toString();
    }
}

/*@LC 2104
tag: Contribution counting · max − min
cue: Sum over all subarrays of (max − min).
hook: Sum of maximums minus sum of minimums, each by contribution.
time: O(n) time · O(n) space
trap: The products reach 10⁹ · 10⁶; use `long`.
@*/
class LC2104 {
    public long subArrayRanges(int[] nums) {
        return sumOf(nums, true) - sumOf(nums, false);                  //*
    }

    private long sumOf(int[] a, boolean wantMax) {                      // Σ max (or Σ min) of all subarrays
        int n = a.length;
        long total = 0;
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i <= n; i++) {
            while (!st.isEmpty() && (i == n || (wantMax ? a[st.peek()] < a[i] : a[st.peek()] > a[i]))) {   //*
                int j = st.pop();
                int left = st.isEmpty() ? -1 : st.peek();
                total += (long) a[j] * (j - left) * (i - j);            //* a[j] wins (j−left)·(i−j) subarrays
            }
            st.push(i);
        }
        return total;
    }
}

public class Day07 {
    public static void main(String[] args) {
        T.eq("core ng", Core07.nextGreater(T.ints("[2,1,2,4,3]")), T.ints("[3,2,3,-1,-1]"));
        T.eq("core ps", Core07.previousSmaller(T.ints("[3,1,4,1,5]")), T.ints("[-1,-1,1,-1,3]"));

        T.eq("739a", new LC739().dailyTemperatures(T.ints("[73,74,75,71,69,72,76,73]")), T.ints("[1,1,4,2,1,1,0,0]"));
        T.eq("739b", new LC739().dailyTemperatures(T.ints("[30,40,50,60]")), T.ints("[1,1,1,0]"));
        T.eq("739c", new LC739().dailyTemperatures(T.ints("[30,60,90]")), T.ints("[1,1,0]"));
        LC739v1 sp = new LC739v1();
        int[] spans = new int[7], prices = {100, 80, 60, 70, 60, 75, 85};
        for (int i = 0; i < 7; i++) spans[i] = sp.next(prices[i]);
        T.eq("901", spans, T.ints("[1,1,1,2,1,4,6]"));

        T.eq("503a", new LC503().nextGreaterElements(T.ints("[1,2,1]")), T.ints("[2,-1,2]"));
        T.eq("503b", new LC503().nextGreaterElements(T.ints("[1,2,3,4,3]")), T.ints("[2,3,4,-1,4]"));
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 9);
            int[] a = T.rand(n, 0, 4), want = new int[n];
            for (int i = 0; i < n; i++) { want[i] = -1; for (int d = 1; d < n; d++) if (a[(i + d) % n] > a[i]) { want[i] = a[(i + d) % n]; break; } }
            T.eq("503 rand", new LC503().nextGreaterElements(a), want);
        }

        T.eq("84a", new LC84().largestRectangleArea(T.ints("[2,1,5,6,2,3]")), 10);
        T.eq("84b", new LC84().largestRectangleArea(T.ints("[2,4]")), 4);
        for (int t = 0; t < 200; t++) {
            int[] h = T.rand(T.rnd(1, 9), 0, 6);
            int best = 0;
            for (int i = 0; i < h.length; i++) { int m = Integer.MAX_VALUE; for (int j = i; j < h.length; j++) { m = Math.min(m, h[j]); best = Math.max(best, m * (j - i + 1)); } }
            T.eq("84 rand", new LC84().largestRectangleArea(h), best);
        }
        T.eq("85a", new LC84v1().maximalRectangle(T.chars("10100", "10111", "11111", "10010")), 6);
        T.eq("85b", new LC84v1().maximalRectangle(T.chars("0")), 0);
        T.eq("85c", new LC84v1().maximalRectangle(T.chars("1")), 1);

        T.eq("907a", new LC907().sumSubarrayMins(T.ints("[3,1,2,4]")), 17);
        T.eq("907b", new LC907().sumSubarrayMins(T.ints("[11,81,94,43,3]")), 444);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 9), 1, 4);
            long s = 0;
            for (int i = 0; i < a.length; i++) { int m = Integer.MAX_VALUE; for (int j = i; j < a.length; j++) { m = Math.min(m, a[j]); s += m; } }
            T.eq("907 rand", new LC907().sumSubarrayMins(a), (int) s);
        }

        T.eq("402a", new LC402().removeKdigits("1432219", 3), "1219");
        T.eq("402b", new LC402().removeKdigits("10200", 1), "200");
        T.eq("402c", new LC402().removeKdigits("10", 2), "0");
        T.eq("402d", new LC402().removeKdigits("112", 1), "11");
        T.eq("402e", new LC402().removeKdigits("9", 1), "0");
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 8), k = T.rnd(0, n);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) sb.append((char) ('0' + T.rnd(0, 9)));
            String num = sb.toString(), best = null;
            for (int mask = 0; mask < 1 << n; mask++) {
                if (Integer.bitCount(mask) != n - k) continue;
                StringBuilder c = new StringBuilder();
                for (int i = 0; i < n; i++) if ((mask >> i & 1) == 1) c.append(num.charAt(i));
                String v = c.toString().replaceFirst("^0+(?=.)", "");
                if (v.isEmpty()) v = "0";
                if (best == null || v.length() < best.length() || (v.length() == best.length() && v.compareTo(best) < 0)) best = v;
            }
            T.eq("402 rand " + num + " " + k, new LC402().removeKdigits(num, k), best);
        }
        T.eq("1673a", new LC402v1().mostCompetitive(T.ints("[3,5,2,6]"), 2), T.ints("[2,6]"));
        T.eq("1673b", new LC402v1().mostCompetitive(T.ints("[2,4,3,3,5,4,9,6]"), 4), T.ints("[2,3,3,4]"));

        T.eq("316a", new LC316().removeDuplicateLetters("bcabc"), "abc");
        T.eq("316b", new LC316().removeDuplicateLetters("cbacdcbc"), "acdb");
        for (int t = 0; t < 200; t++) {
            int n = T.rnd(1, 10);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) sb.append((char) ('a' + T.rnd(0, 3)));
            String s = sb.toString();
            long distinct = s.chars().distinct().count();
            String best = null;
            for (int mask = 0; mask < 1 << n; mask++) {
                if (Integer.bitCount(mask) != distinct) continue;
                StringBuilder c = new StringBuilder();
                for (int i = 0; i < n; i++) if ((mask >> i & 1) == 1) c.append(s.charAt(i));
                String v = c.toString();
                if (v.chars().distinct().count() == distinct && (best == null || v.compareTo(best) < 0)) best = v;
            }
            T.eq("316 rand " + s, new LC316().removeDuplicateLetters(s), best);
        }

        T.eq("2104a", new LC2104().subArrayRanges(T.ints("[1,2,3]")), 4L);
        T.eq("2104b", new LC2104().subArrayRanges(T.ints("[1,3,3]")), 4L);
        T.eq("2104c", new LC2104().subArrayRanges(T.ints("[4,-2,-3,4,1]")), 59L);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 9), -3, 3);
            long s = 0;
            for (int i = 0; i < a.length; i++) { int mx = a[i], mn = a[i]; for (int j = i; j < a.length; j++) { mx = Math.max(mx, a[j]); mn = Math.min(mn, a[j]); s += mx - mn; } }
            T.eq("2104 rand", new LC2104().subArrayRanges(a), s);
        }
        T.summary("Day07");
    }
}
