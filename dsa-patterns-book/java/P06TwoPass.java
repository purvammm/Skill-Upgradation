import java.util.*;

public class P06TwoPass {
    //@tpl candy
    // Every child gets >= 1; a higher rating than a neighbour => more candy.
    static int candy(int[] rating) {
        int n = rating.length;
        int[] c = new int[n];
        Arrays.fill(c, 1);
        for (int i = 1; i < n; i++)             // pass 1: left neighbour rule
            if (rating[i] > rating[i - 1])
                c[i] = c[i - 1] + 1;
        for (int i = n - 2; i >= 0; i--)        // pass 2: right neighbour rule
            if (rating[i] > rating[i + 1])
                c[i] = Math.max(c[i], c[i + 1] + 1);    // keep pass 1's need too
        int sum = 0;
        for (int x : c) sum += x;
        return sum;
    }
    //@end

    //@tpl trap
    // Water above bar i = min(tallest bar on its left, tallest on its right) - h[i].
    static int trap(int[] h) {
        int n = h.length, water = 0;
        int[] maxL = new int[n], maxR = new int[n];
        for (int i = 0; i < n; i++)             // pass 1: left to right
            maxL[i] = Math.max(i > 0 ? maxL[i - 1] : 0, h[i]);
        for (int i = n - 1; i >= 0; i--)        // pass 2: right to left
            maxR[i] = Math.max(i < n - 1 ? maxR[i + 1] : 0, h[i]);
        for (int i = 0; i < n; i++)
            water += Math.min(maxL[i], maxR[i]) - h[i];
        return water;
    }
    //@end

    //@tpl trap2
    // O(1) space. If lMax < rMax, the true right max of l is >= rMax > lMax,
    // so bar l holds exactly lMax - h[l]. Settle the smaller side and move it.
    static int trapTwoPointers(int[] h) {
        int l = 0, r = h.length - 1, lMax = 0, rMax = 0, water = 0;
        while (l < r) {
            lMax = Math.max(lMax, h[l]);
            rMax = Math.max(rMax, h[r]);
            if (lMax < rMax) water += lMax - h[l++];
            else water += rMax - h[r--];
        }
        return water;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Start everyone at 1 and fix broken rules until nothing changes: the least fixpoint.
    static int bruteCandy(int[] rating) {
        int n = rating.length;
        int[] c = new int[n];
        Arrays.fill(c, 1);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < n; i++) {
                if (i > 0 && rating[i] > rating[i - 1] && c[i] <= c[i - 1]) { c[i] = c[i - 1] + 1; changed = true; }
                if (i < n - 1 && rating[i] > rating[i + 1] && c[i] <= c[i + 1]) { c[i] = c[i + 1] + 1; changed = true; }
            }
        }
        int s = 0;
        for (int x : c) s += x;
        return s;
    }

    static int bruteTrap(int[] h) {
        int w = 0;
        for (int i = 0; i < h.length; i++) {
            int l = 0, r = 0;
            for (int j = 0; j <= i; j++) l = Math.max(l, h[j]);
            for (int j = i; j < h.length; j++) r = Math.max(r, h[j]);
            w += Math.min(l, r) - h[i];
        }
        return w;
    }

    public static void main(String[] args) {
        Random r = new Random(67);
        for (int t = 0; t < 6000; t++) {
            int n = r.nextInt(14);
            int[] a = Check.ints(r, n, 0, 5);
            if (n > 0) Check.eq(bruteCandy(a), candy(a), "candy " + Arrays.toString(a));
            Check.eq(bruteTrap(a), trap(a), "trap " + Arrays.toString(a));
            Check.eq(bruteTrap(a), trapTwoPointers(a), "trap2 " + Arrays.toString(a));
        }
        Check.eq(5, candy(new int[] {1, 0, 2}), "LC 135 sample 1");
        Check.eq(4, candy(new int[] {1, 2, 2}), "LC 135 sample 2");
        int[] h1 = {0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1}, h2 = {4, 2, 0, 3, 2, 5};
        Check.eq(6, trap(h1), "LC 42 sample 1");
        Check.eq(6, trapTwoPointers(h1), "LC 42 sample 1 (two pointers)");
        Check.eq(9, trap(h2), "LC 42 sample 2");
        Check.eq(9, trapTwoPointers(h2), "LC 42 sample 2 (two pointers)");
        Check.done("P06TwoPass");
    }
}
