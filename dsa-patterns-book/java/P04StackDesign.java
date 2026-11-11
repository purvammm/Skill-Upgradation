import java.util.*;

public class P04StackDesign {
    //@tpl minstack
    // Min stack: each entry stores {value, minimum of it and everything below}.
    static class MinStack {
        private final Deque<int[]> st = new ArrayDeque<>();

        void push(int x) {
            int min = st.isEmpty() ? x : Math.min(x, st.peek()[1]);
            st.push(new int[] {x, min});
        }
        void pop() { st.pop(); }                 // the entry below kept its own min
        int top() { return st.peek()[0]; }
        int getMin() { return st.peek()[1]; }
    }
    //@end

    //@tpl queue
    // Queue from two stacks. New items go on stack in; stack out holds the
    // older items in pop order. Refill out from in only when out is empty.
    static class MyQueue {
        private final Deque<Integer> in = new ArrayDeque<>();
        private final Deque<Integer> out = new ArrayDeque<>();

        void push(int x) { in.push(x); }
        int pop() { shift(); return out.pop(); }
        int peek() { shift(); return out.peek(); }
        boolean empty() { return in.isEmpty() && out.isEmpty(); }

        private void shift() {                   // each item moves once, ever
            if (out.isEmpty())
                while (!in.isEmpty()) out.push(in.pop());   // reverses the order
        }
    }
    //@end

    //@tpl freqstack
    // Maximum frequency stack: pop the most frequent value, the most recent
    // one on ties. The c-th copy of x is pushed on stack group[c].
    static class FreqStack {
        private final Map<Integer, Integer> freq = new HashMap<>();
        private final Map<Integer, Deque<Integer>> group = new HashMap<>();
        private int maxFreq = 0;

        void push(int x) {
            int f = freq.merge(x, 1, Integer::sum);
            maxFreq = Math.max(maxFreq, f);
            group.computeIfAbsent(f, k -> new ArrayDeque<>()).push(x);
        }

        int pop() {
            Deque<Integer> top = group.get(maxFreq);
            int x = top.pop();
            freq.merge(x, -1, Integer::sum);     // its earlier copy is still below
            if (top.isEmpty()) maxFreq--;        // group[maxFreq - 1] is not empty
            return x;
        }
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Slow reference for FreqStack: scan the whole history on each pop.
    static int brutePop(List<Integer> items) {
        Map<Integer, Integer> cnt = new HashMap<>();
        int best = 0;
        for (int x : items) best = Math.max(best, cnt.merge(x, 1, Integer::sum));
        for (int i = items.size() - 1; i >= 0; i--) {
            if (cnt.get(items.get(i)) == best) return items.remove(i);
        }
        throw new IllegalStateException();
    }

    public static void main(String[] args) {
        Random r = new Random(46);
        for (int t = 0; t < 1000; t++) {
            MinStack ms = new MinStack();
            List<Integer> ref = new ArrayList<>();
            MyQueue q = new MyQueue();
            ArrayDeque<Integer> refQ = new ArrayDeque<>();
            FreqStack fs = new FreqStack();
            List<Integer> refF = new ArrayList<>();
            int range = r.nextBoolean() ? 3 : 1000;
            for (int op = 0; op < 60; op++) {
                int x = r.nextInt(2 * range + 1) - range;
                if (ref.isEmpty() || r.nextInt(5) < 3) {
                    ms.push(x);
                    ref.add(x);
                } else {
                    int k = r.nextInt(3);
                    if (k == 0) { ms.pop(); ref.remove(ref.size() - 1); }
                    else if (k == 1) Check.eq(ref.get(ref.size() - 1), (Object) ms.top(), "top");
                    else Check.eq(Collections.min(ref), (Object) ms.getMin(), "getMin " + ref);
                }

                int k = refQ.isEmpty() ? 0 : r.nextInt(4);
                if (k <= 1) { q.push(x); refQ.addLast(x); }
                else if (k == 2) Check.eq(refQ.pollFirst(), (Object) q.pop(), "queue pop");
                else Check.eq(refQ.peekFirst(), (Object) q.peek(), "queue peek");
                Check.eq(refQ.isEmpty(), q.empty(), "queue empty");

                int y = r.nextInt(range == 3 ? 4 : 6);
                if (refF.isEmpty() || r.nextInt(5) < 3) { fs.push(y); refF.add(y); }
                else Check.eq(brutePop(refF), fs.pop(), "freq pop");
            }
        }
        // amortised O(1): 10^6 mixed operations finish quickly
        MyQueue big = new MyQueue();
        long sum = 0;
        for (int i = 0; i < 1000000; i++) {
            big.push(i);
            if (i % 3 == 2) { sum += big.pop(); sum += big.pop(); }
        }
        Check.eq(666666L * 666665 / 2, sum, "big queue order");

        MinStack ms = new MinStack();
        ms.push(-2);
        ms.push(0);
        ms.push(-3);
        Check.eq(-3, ms.getMin(), "LC 155 getMin");
        ms.pop();
        Check.eq(0, ms.top(), "LC 155 top");
        Check.eq(-2, ms.getMin(), "LC 155 getMin 2");
        MyQueue q = new MyQueue();
        q.push(1);
        q.push(2);
        Check.eq(1, q.peek(), "LC 232 peek");
        Check.eq(1, q.pop(), "LC 232 pop");
        Check.eq(false, q.empty(), "LC 232 empty");
        FreqStack fs = new FreqStack();
        for (int x : new int[] {5, 7, 5, 7, 4, 5}) fs.push(x);
        int[] want = {5, 7, 5, 4};
        for (int i = 0; i < 4; i++) Check.eq(want[i], fs.pop(), "LC 895 pop " + i);
        Check.done("P04StackDesign");
    }
}
