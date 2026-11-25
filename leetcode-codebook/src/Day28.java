import java.util.*;

/*@LC 217
tag: HashSet
cue: Does any value appear twice?
hook: `add` returns false when it is already there.
time: O(n) time · O(n) space
trap: None worth a bug; use it as a 2-minute typing drill.
var: LC 219 Contains Duplicate II (within distance k) | Map value → last index; true if `i − last ≤ k`.
var: LC 220 Contains Duplicate III | TreeSet window of size k; check `ceiling(x − t) ≤ x + t`.
@*/
class LC217 {
    public boolean containsDuplicate(int[] nums) {
        Set<Integer> seen = new HashSet<>();
        for (int x : nums) if (!seen.add(x)) return true;      //*
        return false;
    }
}

/*@LC 881
tag: Sort + two pointers
cue: Boats hold at most two people and a weight limit; fewest boats.
hook: The heaviest always leaves; take the lightest with them if they fit.
time: O(n log n) time · O(1) space
trap: Loop while `l <= r` so the last single person also gets a boat.
@*/
class LC881 {
    public int numRescueBoats(int[] people, int limit) {
        Arrays.sort(people);
        int boats = 0;
        for (int l = 0, r = people.length - 1; l <= r; r--) {   //* the heaviest leaves every time
            if (people[l] + people[r] <= limit) l++;             //* the lightest joins if they fit
            boats++;
        }
        return boats;
    }
}

/*@LC 2300
tag: Sort + lower bound
cue: For each spell, how many potions make spell × potion ≥ success?
hook: Sort potions; the first potion that works is a lower bound; everything after it works too.
time: O((n + m) log m) time · O(1) extra
trap: `spell * potion` overflows int: compare in `long`.
@*/
class LC2300 {
    public int[] successfulPairs(int[] spells, int[] potions, long success) {
        Arrays.sort(potions);
        int m = potions.length;
        int[] ans = new int[spells.length];
        for (int i = 0; i < spells.length; i++) {
            int lo = 0, hi = m;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if ((long) spells[i] * potions[mid] < success) lo = mid + 1; else hi = mid;   //*
            }
            ans[i] = m - lo;                                                                  //*
        }
        return ans;
    }
}

public class Day28 {
    public static void main(String[] args) {
        T.eq("217a", new LC217().containsDuplicate(T.ints("[1,2,3,1]")), true);
        T.eq("217b", new LC217().containsDuplicate(T.ints("[1,2,3,4]")), false);
        T.eq("217c", new LC217().containsDuplicate(T.ints("[1,1,1,3,3,4,3,2,4,2]")), true);

        T.eq("881a", new LC881().numRescueBoats(T.ints("[1,2]"), 3), 1);
        T.eq("881b", new LC881().numRescueBoats(T.ints("[3,2,2,1]"), 3), 3);
        T.eq("881c", new LC881().numRescueBoats(T.ints("[3,5,3,4]"), 5), 4);

        T.eq("2300a", new LC2300().successfulPairs(T.ints("[5,1,3]"), T.ints("[1,2,3,4,5]"), 7), T.ints("[4,0,3]"));
        T.eq("2300b", new LC2300().successfulPairs(T.ints("[3,1,2]"), T.ints("[8,5,8]"), 16), T.ints("[2,0,2]"));
        T.eq("2300c", new LC2300().successfulPairs(T.ints("[100000]"), T.ints("[100000]"), 10000000000L), T.ints("[1]"));
        T.summary("Day28");
    }
}
