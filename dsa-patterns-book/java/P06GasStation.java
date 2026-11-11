import java.util.*;

public class P06GasStation {
    //@tpl gas
    // Index to start one full circle from, or -1. tank = fuel since `start`.
    static int canCompleteCircuit(int[] gas, int[] cost) {
        long total = 0, tank = 0;
        int start = 0;
        for (int i = 0; i < gas.length; i++) {
            int d = gas[i] - cost[i];
            total += d;
            tank += d;
            if (tank < 0) {          // no start in start..i can pass station i
                start = i + 1;
                tank = 0;
            }
        }
        return total >= 0 ? start : -1;   // total >= 0: the last start works
    }
    //@end

    //@tpl parens
    // Fewest '(' or ')' to add so s is valid (LC 921). Same reset at balance < 0.
    static int minAddToMakeValid(String s) {
        int balance = 0, added = 0;
        for (int i = 0; i < s.length(); i++) {
            balance += s.charAt(i) == '(' ? 1 : -1;
            if (balance < 0) {       // this ')' has no partner: add a '(' for it
                added++;
                balance = 0;
            }
        }
        return added + balance;      // every '(' still open needs a ')'
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean worksFrom(int[] gas, int[] cost, int s) {
        long tank = 0;
        int n = gas.length;
        for (int k = 0; k < n; k++) {
            int i = (s + k) % n;
            tank += gas[i] - cost[i];
            if (tank < 0) return false;
        }
        return true;
    }

    // Delete "()" until none is left; each leftover bracket needs one partner.
    static int bruteParens(String s) {
        String prev;
        do {
            prev = s;
            s = s.replace("()", "");
        } while (!s.equals(prev));
        return s.length();
    }

    public static void main(String[] args) {
        Random r = new Random(66);
        int solvable = 0;
        for (int t = 0; t < 6000; t++) {
            int n = 1 + r.nextInt(10);
            int[] gas = Check.ints(r, n, 0, 6), cost = Check.ints(r, n, 0, 6);
            boolean any = false;
            for (int s = 0; s < n; s++) any |= worksFrom(gas, cost, s);
            int got = canCompleteCircuit(gas, cost);
            if (any) {
                solvable++;
                Check.isTrue(got >= 0 && worksFrom(gas, cost, got),
                             "gas " + Arrays.toString(gas) + " " + Arrays.toString(cost) + " got " + got);
            } else {
                Check.eq(-1, got, "gas none " + Arrays.toString(gas) + " " + Arrays.toString(cost));
            }
            String p = Check.str(r, r.nextInt(13), "()");
            Check.eq(bruteParens(p), minAddToMakeValid(p), "parens " + p);
        }
        Check.isTrue(solvable > 1000, "enough solvable cases");
        Check.eq(3, canCompleteCircuit(new int[] {1, 2, 3, 4, 5}, new int[] {3, 4, 5, 1, 2}), "LC 134 sample 1");
        Check.eq(-1, canCompleteCircuit(new int[] {2, 3, 4}, new int[] {3, 4, 3}), "LC 134 sample 2");
        Check.eq(1, minAddToMakeValid("())"), "LC 921 sample 1");
        Check.eq(3, minAddToMakeValid("((("), "LC 921 sample 2");
        Check.done("P06GasStation");
    }
}
