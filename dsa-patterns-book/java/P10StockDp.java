import java.util.*;

public class P10StockDp {
    //@tpl fee
    // Any number of trades, a fee per sale (fee = 0 is Stock II).
    // hold = best profit so far while owning a share, free = while not.
    static int maxProfitFee(int[] prices, int fee) {
        int hold = -prices[0], free = 0;                 // day 0: buy, or not
        for (int i = 1; i < prices.length; i++) {
            int p = prices[i];
            int newFree = Math.max(free, hold + p - fee);   // rest, or sell today
            int newHold = Math.max(hold, free - p);         // rest, or buy today
            free = newFree;
            hold = newHold;
        }
        return free;                         // still holding at the end never helps
    }
    //@end

    //@tpl cooldown
    // Cooldown: the day after a sale you may not buy.
    // cool = sold today; tomorrow that profit becomes free, but not today.
    static int maxProfitCooldown(int[] prices) {
        int hold = -prices[0], free = 0, cool = 0;
        for (int i = 1; i < prices.length; i++) {
            int p = prices[i];
            int newHold = Math.max(hold, free - p);      // buy from free, never cool
            int newCool = hold + p;                      // sell today
            int newFree = Math.max(free, cool);          // yesterday's sale clears
            hold = newHold;
            cool = newCool;
            free = newFree;
        }
        return Math.max(free, cool);
    }
    //@end

    //@tpl k
    // At most k trades (Stock III is k = 2). hold[t] / free[t]: best profit
    // after t buys. Buy number t starts from free[t-1].
    static int maxProfitK(int k, int[] prices) {
        int n = prices.length;
        if (k >= n / 2) {                     // a trade needs 2 days: no real limit
            int sum = 0;
            for (int i = 1; i < n; i++)
                sum += Math.max(0, prices[i] - prices[i - 1]);  // each rise
            return sum;
        }
        int[] hold = new int[k + 1], free = new int[k + 1];
        Arrays.fill(hold, Integer.MIN_VALUE / 2);       // no share yet
        for (int p : prices)
            for (int t = k; t >= 1; t--) {    // t down: free[t-1] is yesterday's
                free[t] = Math.max(free[t], hold[t] + p);
                hold[t] = Math.max(hold[t], free[t - 1] - p);
            }
        return free[k];
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // Every buy/sell/rest sequence. left = trades still allowed, cd = cooldown days.
    static int brute(int[] p, int i, boolean holding, int left, int fee, int cd) {
        if (i >= p.length) return 0;
        int best = brute(p, i + 1, holding, left, fee, cd);          // rest
        if (holding) best = Math.max(best, p[i] - fee + brute(p, i + 1 + cd, false, left, fee, cd));
        else if (left > 0) best = Math.max(best, -p[i] + brute(p, i + 1, true, left - 1, fee, cd));
        return best;
    }

    static int cooldownSameDay(int[] prices) {   // trap: buy reads today's free
        int hold = -prices[0], free = 0, cool = 0;
        for (int i = 1; i < prices.length; i++) {
            int p = prices[i];
            cool = hold + p;
            free = Math.max(free, cool);
            hold = Math.max(hold, free - p);
        }
        return Math.max(free, cool);
    }

    static int feeHoldZero(int[] prices, int fee) {
        int hold = 0, free = 0;
        for (int p : prices) {
            int nf = Math.max(free, hold + p - fee), nh = Math.max(hold, free - p);
            free = nf;
            hold = nh;
        }
        return free;
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            int[] p = {1, 3, 2, 8, 4, 9};
            int fee = 2, hold = -p[0], free = 0;
            System.out.println("day 0 price " + p[0] + " hold " + hold + " free " + free);
            for (int i = 1; i < p.length; i++) {
                int nf = Math.max(free, hold + p[i] - fee), nh = Math.max(hold, free - p[i]);
                System.out.println("day " + i + " price " + p[i] + " hold " + nh + " free " + nf
                                   + "  (sell " + (hold + p[i] - fee) + ", buy " + (free - p[i]) + ")");
                free = nf;
                hold = nh;
            }
            System.out.println("cooldown same-day bug [1,2,3,0,2]: " + cooldownSameDay(new int[] {1, 2, 3, 0, 2}));
            System.out.println("hold=0 bug [5,4,3]: " + feeHoldZero(new int[] {5, 4, 3}, 0));
            return;
        }
        Random r = new Random(40);
        for (int t = 0; t < 3000; t++) {
            int n = 1 + r.nextInt(10);
            int[] p = Check.ints(r, n, 0, 12);
            int fee = r.nextInt(4), k = r.nextInt(6);
            Check.eq(brute(p, 0, false, 99, 0, 0), maxProfitFee(p, 0), "II " + Arrays.toString(p));
            Check.eq(brute(p, 0, false, 99, fee, 0), maxProfitFee(p, fee), "fee " + Arrays.toString(p) + " " + fee);
            Check.eq(brute(p, 0, false, 99, 0, 1), maxProfitCooldown(p), "cool " + Arrays.toString(p));
            Check.eq(brute(p, 0, false, k, 0, 0), maxProfitK(k, p), "k=" + k + " " + Arrays.toString(p));
        }
        Check.eq(7, maxProfitFee(new int[] {7, 1, 5, 3, 6, 4}, 0), "LC 122 sample 1");
        Check.eq(4, maxProfitFee(new int[] {1, 2, 3, 4, 5}, 0), "LC 122 sample 2");
        Check.eq(8, maxProfitFee(new int[] {1, 3, 2, 8, 4, 9}, 2), "LC 714 sample 1");
        Check.eq(6, maxProfitFee(new int[] {1, 3, 7, 5, 10, 3}, 3), "LC 714 sample 2");
        Check.eq(3, maxProfitCooldown(new int[] {1, 2, 3, 0, 2}), "LC 309 sample 1");
        Check.eq(0, maxProfitCooldown(new int[] {1}), "LC 309 sample 2");
        Check.eq(6, maxProfitK(2, new int[] {3, 3, 5, 0, 0, 3, 1, 4}), "LC 123 sample 1");
        Check.eq(4, maxProfitK(2, new int[] {1, 2, 3, 4, 5}), "LC 123 sample 2");
        Check.eq(0, maxProfitK(2, new int[] {7, 6, 4, 3, 1}), "LC 123 sample 3");
        Check.eq(2, maxProfitK(2, new int[] {2, 4, 1}), "LC 188 sample 1");
        Check.eq(7, maxProfitK(2, new int[] {3, 2, 6, 5, 0, 3}), "LC 188 sample 2");
        Check.done("P10StockDp");
    }
}
