import java.util.*;

public class P04NextGreater {
    //@tpl next
    // Next greater: for each i, the first j > i with a[j] > a[i], or -1.
    // Invariant: the stack holds the indices still waiting for an answer,
    // and their values never increase from bottom to top.
    static int[] nextGreater(int[] a) {
        int n = a.length;
        int[] next = new int[n];
        Arrays.fill(next, -1);
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            while (!st.isEmpty() && a[st.peek()] < a[i])
                next[st.pop()] = i;              // a[i] answers every smaller top
            st.push(i);
        }
        return next;
    }

    // Daily temperatures: days to wait = next[i] - i, or 0 if never warmer.
    static int[] dailyTemperatures(int[] t) {
        int[] next = nextGreater(t), wait = new int[t.length];
        for (int i = 0; i < t.length; i++) wait[i] = next[i] < 0 ? 0 : next[i] - i;
        return wait;
    }
    //@end

    //@tpl circular
    // Next greater element II (circular): walk the array twice, read a[i % n].
    static int[] nextGreaterCircular(int[] a) {
        int n = a.length;
        int[] ans = new int[n];
        Arrays.fill(ans, -1);
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i < 2 * n; i++) {
            int x = a[i % n];
            while (!st.isEmpty() && a[st.peek()] < x) ans[st.pop()] = x;
            if (i < n) st.push(i);               // the second lap only answers
        }
        return ans;
    }
    //@end

    //@tpl span
    // Online stock span: days in a row, ending today, with price <= today's.
    // Stack of {price, span}; prices strictly decrease from bottom to top.
    static class StockSpanner {
        private final Deque<int[]> st = new ArrayDeque<>();

        int next(int price) {
            int span = 1;                        // today
            while (!st.isEmpty() && st.peek()[0] <= price)
                span += st.pop()[1];             // absorb the days it covered
            st.push(new int[] {price, span});
            return span;
        }
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int[] bruteNext(int[] a) {
        int[] out = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = -1;
            for (int j = i + 1; j < a.length; j++) if (a[j] > a[i]) { out[i] = j; break; }
        }
        return out;
    }

    static int[] bruteCircular(int[] a) {
        int n = a.length;
        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            out[i] = -1;
            for (int d = 1; d < n; d++) if (a[(i + d) % n] > a[i]) { out[i] = a[(i + d) % n]; break; }
        }
        return out;
    }

    public static void main(String[] args) {
        Random r = new Random(43);
        for (int t = 0; t < 4000; t++) {
            int n = r.nextInt(13);
            int[] a = Check.ints(r, n, 0, r.nextBoolean() ? 4 : 30);
            int[] want = bruteNext(a);
            Check.eq(want, nextGreater(a), "next " + Arrays.toString(a));
            int[] wait = dailyTemperatures(a);
            for (int i = 0; i < n; i++) Check.eq(want[i] < 0 ? 0 : want[i] - i, wait[i], "daily " + Arrays.toString(a));
            Check.eq(bruteCircular(a), nextGreaterCircular(a), "circular " + Arrays.toString(a));
            StockSpanner sp = new StockSpanner();
            for (int i = 0; i < n; i++) {
                int span = 0;
                for (int j = i; j >= 0 && a[j] <= a[i]; j--) span++;
                Check.eq(span, sp.next(a[i]), "span " + Arrays.toString(a) + " day " + i);
            }
        }
        Check.eq(new int[] {1, 1, 4, 2, 1, 1, 0, 0}, dailyTemperatures(new int[] {73, 74, 75, 71, 69, 72, 76, 73}), "LC 739 sample 1");
        Check.eq(new int[] {1, 1, 1, 0}, dailyTemperatures(new int[] {30, 40, 50, 60}), "LC 739 sample 2");
        Check.eq(new int[] {1, 1, 0}, dailyTemperatures(new int[] {30, 60, 90}), "LC 739 sample 3");
        Check.eq(new int[] {2, -1, 2}, nextGreaterCircular(new int[] {1, 2, 1}), "LC 503 sample 1");
        Check.eq(new int[] {2, 3, 4, -1, 4}, nextGreaterCircular(new int[] {1, 2, 3, 4, 3}), "LC 503 sample 2");
        StockSpanner sp = new StockSpanner();
        int[] prices = {100, 80, 60, 70, 60, 75, 85}, spans = {1, 1, 1, 2, 1, 4, 6};
        for (int i = 0; i < prices.length; i++) Check.eq(spans[i], sp.next(prices[i]), "LC 901 sample day " + i);
        Check.done("P04NextGreater");
    }
}
