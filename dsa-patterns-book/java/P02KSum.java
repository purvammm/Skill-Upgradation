import java.util.*;

public class P02KSum {
    //@tpl three
    // All unique triples with a[i] + a[l] + a[r] == 0 (LC 15).
    static List<List<Integer>> threeSum(int[] a) {
        Arrays.sort(a);
        List<List<Integer>> out = new ArrayList<>();
        int n = a.length;
        for (int i = 0; i < n - 2; i++) {
            if (i > 0 && a[i] == a[i - 1]) continue;   // 1. same first value: skip
            if (a[i] > 0) break;                        // all three would be > 0
            int l = i + 1, r = n - 1;
            while (l < r) {                             // 2. pair search on a[i+1..]
                int s = a[i] + a[l] + a[r];
                if (s < 0) l++;
                else if (s > 0) r--;
                else {
                    out.add(Arrays.asList(a[i], a[l], a[r]));
                    l++;
                    r--;
                    while (l < r && a[l] == a[l - 1]) l++;  // 3. same second value
                }
            }
        }
        return out;
    }
    //@end

    //@tpl closest
    // Sum of three elements closest to target (LC 16).
    static int threeSumClosest(int[] a, int target) {
        Arrays.sort(a);
        int best = a[0] + a[1] + a[2];
        for (int i = 0; i < a.length - 2; i++) {
            int l = i + 1, r = a.length - 1;
            while (l < r) {
                int s = a[i] + a[l] + a[r];
                if (Math.abs(s - target) < Math.abs(best - target)) best = s;
                if (s < target) l++;
                else if (s > target) r--;
                else return s;                          // exact: cannot do better
            }
        }
        return best;
    }
    //@end

    //@tpl four
    // All unique quadruples with sum == target (LC 18). Values reach 1e9: use long.
    static List<List<Integer>> fourSum(int[] a, int target) {
        Arrays.sort(a);
        List<List<Integer>> out = new ArrayList<>();
        int n = a.length;
        for (int i = 0; i < n - 3; i++) {
            if (i > 0 && a[i] == a[i - 1]) continue;
            for (int j = i + 1; j < n - 2; j++) {
                if (j > i + 1 && a[j] == a[j - 1]) continue;
                int l = j + 1, r = n - 1;
                while (l < r) {
                    long s = (long) a[i] + a[j] + a[l] + a[r];
                    if (s < target) l++;
                    else if (s > target) r--;
                    else {
                        out.add(Arrays.asList(a[i], a[j], a[l], a[r]));
                        l++;
                        r--;
                        while (l < r && a[l] == a[l - 1]) l++;
                    }
                }
            }
        }
        return out;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static List<List<Integer>> bruteK(int[] a0, int k, long target) {
        int[] a = a0.clone();
        Arrays.sort(a);
        Set<List<Integer>> seen = new LinkedHashSet<>();
        int n = a.length;
        int[] idx = new int[k];
        bruteRec(a, k, 0, 0, 0L, target, idx, seen);
        return new ArrayList<>(seen);
    }

    static void bruteRec(int[] a, int k, int depth, int from, long sum, long target, int[] idx,
                         Set<List<Integer>> seen) {
        if (depth == k) {
            if (sum == target) {
                List<Integer> t = new ArrayList<>();
                for (int i : idx) t.add(a[i]);
                seen.add(t);
            }
            return;
        }
        for (int i = from; i < a.length; i++) {
            idx[depth] = i;
            bruteRec(a, k, depth + 1, i + 1, sum + a[i], target, idx, seen);
        }
    }

    static int bruteClosestDist(int[] a, int target) {
        int best = Integer.MAX_VALUE;
        for (int i = 0; i < a.length; i++)
            for (int j = i + 1; j < a.length; j++)
                for (int k = j + 1; k < a.length; k++)
                    best = Math.min(best, Math.abs(a[i] + a[j] + a[k] - target));
        return best;
    }

    static boolean isTripleSum(int[] a, int s) {
        for (int i = 0; i < a.length; i++)
            for (int j = i + 1; j < a.length; j++)
                for (int k = j + 1; k < a.length; k++)
                    if (a[i] + a[j] + a[k] == s) return true;
        return false;
    }

    public static void main(String[] args) {
        Random r = new Random(21);
        for (int t = 0; t < 3000; t++) {
            int n = r.nextInt(11);
            int[] a = Check.ints(r, n, -6, 6);
            Check.eq(Check.canon(bruteK(a, 3, 0), false), Check.canon(threeSum(a.clone()), false),
                    "3sum " + Arrays.toString(a));
            if (n >= 3) {
                int target = r.nextInt(25) - 12;
                int got = threeSumClosest(a.clone(), target);
                Check.eq(bruteClosestDist(a, target), Math.abs(got - target), "closest dist " + Arrays.toString(a));
                Check.isTrue(isTripleSum(a, got), "closest achievable " + Arrays.toString(a));
            }
            int[] b = Check.ints(r, n, -5, 5);
            int tg = r.nextInt(15) - 7;
            Check.eq(Check.canon(bruteK(b, 4, tg), false), Check.canon(fourSum(b.clone(), tg), false),
                    "4sum " + Arrays.toString(b) + " t=" + tg);
            // large values: int sums would overflow
            int[] c = new int[n];
            for (int i = 0; i < n; i++) c[i] = (r.nextInt(5) - 2) * 500_000_000;
            int tc = (r.nextInt(5) - 2) * 500_000_000;
            Check.eq(Check.canon(bruteK(c, 4, tc), false), Check.canon(fourSum(c.clone(), tc), false),
                    "4sum big " + Arrays.toString(c) + " t=" + tc);
        }
        Check.eq("[[-1, -1, 2], [-1, 0, 1]]", Check.canon(threeSum(new int[] {-1, 0, 1, 2, -1, -4}), false),
                "LC 15 sample 1");
        Check.eq(0, threeSum(new int[] {0, 1, 1}).size(), "LC 15 sample 2");
        Check.eq("[[0, 0, 0]]", Check.canon(threeSum(new int[] {0, 0, 0}), false), "LC 15 sample 3");
        Check.eq(2, threeSumClosest(new int[] {-1, 2, 1, -4}, 1), "LC 16 sample 1");
        Check.eq(0, threeSumClosest(new int[] {0, 0, 0}, 1), "LC 16 sample 2");
        Check.eq("[[-1, 0, 0, 1], [-2, -1, 1, 2], [-2, 0, 0, 2]]",
                Check.canon(fourSum(new int[] {1, 0, -1, 0, -2, 2}, 0), false), "LC 18 sample 1");
        Check.eq("[[2, 2, 2, 2]]", Check.canon(fourSum(new int[] {2, 2, 2, 2, 2}, 8), false), "LC 18 sample 2");
        Check.eq(0, fourSum(new int[] {1000000000, 1000000000, 1000000000, 1000000000}, -294967296).size(),
                "LC 18 overflow case");
        Check.done("P02KSum");
    }
}
