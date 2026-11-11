import java.util.*;

public class P01SeenMap {
    //@tpl twosum
    // Two sum: for each a[i], ask whether its partner was seen earlier.
    static int[] twoSum(int[] a, int target) {
        Map<Integer, Integer> indexOf = new HashMap<>();  // value -> index
        for (int i = 0; i < a.length; i++) {
            Integer j = indexOf.get(target - a[i]);       // 1. look back first
            if (j != null) return new int[] {j, i};
            indexOf.put(a[i], i);                         // 2. then remember a[i]
        }
        return new int[] {-1, -1};
    }
    //@end

    //@tpl nearby
    // Contains duplicate II: two equal values at most k indexes apart.
    // Only the last index of each value matters: it is the closest one.
    static boolean containsNearbyDuplicate(int[] a, int k) {
        Map<Integer, Integer> last = new HashMap<>();  // value -> last index
        for (int i = 0; i < a.length; i++) {
            Integer j = last.put(a[i], i);             // put returns the old index
            if (j != null && i - j <= k) return true;
        }
        return false;
    }
    //@end

    //@tpl consecutive
    // Longest run of consecutive values (any order) in O(n).
    // Count only from x where x - 1 is missing, so each run is walked once.
    static int longestConsecutive(int[] a) {
        Set<Integer> set = new HashSet<>();
        for (int x : a) set.add(x);
        int best = 0;
        for (int x : set) {                     // the set, not a: no repeat walks
            if (set.contains(x - 1)) continue;  // x is not the start of a run
            int len = 1;
            while (set.contains(x + len)) len++;
            best = Math.max(best, len);
        }
        return best;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean brutePair(int[] a, int target) {
        for (int i = 0; i < a.length; i++)
            for (int j = i + 1; j < a.length; j++)
                if (a[i] + a[j] == target) return true;
        return false;
    }

    static boolean bruteNearby(int[] a, int k) {
        for (int i = 0; i < a.length; i++)
            for (int j = i + 1; j < a.length && j - i <= k; j++)
                if (a[i] == a[j]) return true;
        return false;
    }

    static int bruteConsecutive(int[] a) {
        int best = 0;
        for (int x : a) {
            int len = 0;
            boolean found = true;
            while (found) {
                found = false;
                for (int y : a) if (y == x + len) { found = true; break; }
                if (found) len++;
            }
            best = Math.max(best, len);
        }
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(202);
        for (int t = 0; t < 5000; t++) {
            int n = r.nextInt(12);
            int[] a = Check.ints(r, n, -8, 8);
            int target = r.nextInt(33) - 16;
            int[] got = twoSum(a, target);
            boolean exists = brutePair(a, target);
            Check.eq(exists, got[0] >= 0, "two sum exists " + Arrays.toString(a) + " t=" + target);
            if (exists) Check.isTrue(got[0] < got[1] && a[got[0]] + a[got[1]] == target,
                    "two sum valid " + Arrays.toString(a));
            int k = r.nextInt(6);
            Check.eq(bruteNearby(a, k), containsNearbyDuplicate(a, k), "nearby " + Arrays.toString(a));
            Check.eq(bruteConsecutive(a), longestConsecutive(a), "consecutive " + Arrays.toString(a));
        }
        Check.eq(new int[] {0, 1}, twoSum(new int[] {2, 7, 11, 15}, 9), "LC 1 sample 1");
        Check.eq(new int[] {1, 2}, twoSum(new int[] {3, 2, 4}, 6), "LC 1 sample 2");
        Check.eq(new int[] {0, 1}, twoSum(new int[] {3, 3}, 6), "LC 1 sample 3");
        Check.eq(true, containsNearbyDuplicate(new int[] {1, 2, 3, 1}, 3), "LC 219 sample 1");
        Check.eq(true, containsNearbyDuplicate(new int[] {1, 0, 1, 1}, 1), "LC 219 sample 2");
        Check.eq(false, containsNearbyDuplicate(new int[] {1, 2, 3, 1, 2, 3}, 2), "LC 219 sample 3");
        Check.eq(4, longestConsecutive(new int[] {100, 4, 200, 1, 3, 2}), "LC 128 sample 1");
        Check.eq(9, longestConsecutive(new int[] {0, 3, 7, 2, 5, 8, 4, 6, 0, 1}), "LC 128 sample 2");
        // many copies of a run start: must stay fast because we loop over the set
        int[] big = new int[200000];
        for (int i = 0; i < big.length; i++) big[i] = i < 100000 ? 1 : i - 99999;
        Check.eq(100000, longestConsecutive(big), "big run with duplicates");
        Check.done("P01SeenMap");
    }
}
