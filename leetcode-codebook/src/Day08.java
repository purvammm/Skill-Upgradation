import java.util.*;

/*@core Bit tricks you should be able to write without thinking
note: XOR facts: `a ^ a = 0`, `a ^ 0 = a`, order does not matter. XOR of `a[l..r]` = `P[r+1] ^ P[l]` with prefix XORs. Shift with `1L << b` once b can reach 31.
@*/
class Core08 {
    static int[] tricks(int x, int b) {
        int on = (x >> b) & 1;                         // is bit b set? (1 or 0)
        int set = x | (1 << b);                        // turn bit b on
        int clear = x & ~(1 << b);                     // turn bit b off
        int flip = x ^ (1 << b);                       // flip bit b
        int lowest = x & -x;                           // lowest set bit as a value (x = 12 -> 4)
        int dropLowest = x & (x - 1);                  // x without its lowest set bit; 0 means a power of two
        int ones = Integer.bitCount(x);                // number of set bits
        int highest = 31 - Integer.numberOfLeadingZeros(x);   // index of the highest set bit (x > 0)
        return new int[]{on, set, clear, flip, lowest, dropLowest, ones, highest};
    }

    // Binary trie over the bits of non-negative ints: insert, and best XOR partner.
    static final class XorTrie {
        static final int B = 31;                       // bits 30..0
        final int[][] next;
        int size = 1;                                  // node 0 is the root

        XorTrie(int maxNumbers) { next = new int[maxNumbers * B + 1][2]; }

        void insert(int x) {
            int node = 0;
            for (int b = B - 1; b >= 0; b--) {
                int bit = (x >> b) & 1;
                if (next[node][bit] == 0) next[node][bit] = size++;
                node = next[node][bit];
            }
        }

        int maxXor(int x) {                            // trie must not be empty
            int node = 0, res = 0;
            for (int b = B - 1; b >= 0; b--) {
                int want = ((x >> b) & 1) ^ 1;         // the opposite bit gives a 1 here
                if (next[node][want] != 0) { res |= 1 << b; node = next[node][want]; }
                else node = next[node][want ^ 1];
            }
            return res;
        }
    }
}

/*@core XOR basis: the maximum XOR of any subset
note: Keep at most one number per leading bit (like Gaussian elimination). Insert reduces x by the stored numbers; the max greedily XORs in every number that makes the result bigger. This is the plan's GfG "Maximum Subset XOR".
@*/
class Core08b {
    static long maxSubsetXor(long[] a) {
        long[] basis = new long[63];                   // basis[b] has its highest bit at b, or 0
        for (long x : a) {
            for (int b = 62; b >= 0 && x != 0; b--) {
                if ((x >> b & 1) == 0) continue;
                if (basis[b] == 0) { basis[b] = x; break; }   // new leading bit: store it
                x ^= basis[b];                                // otherwise remove that bit and go on
            }
        }
        long best = 0;
        for (int b = 62; b >= 0; b--) best = Math.max(best, best ^ basis[b]);
        return best;
    }
}

/*@LC 136
tag: XOR cancels pairs
cue: Every number appears twice except one.
hook: Pairs cancel; what is left is the single one.
time: O(n) time · O(1) space
trap: Start from 0, not from `nums[0]` counted twice.
var: LC 268 Missing Number | XOR every index 0..n and every value; the missing one is left.
@*/
class LC136 {
    public int singleNumber(int[] nums) {
        int x = 0;
        for (int v : nums) x ^= v;                     //*
        return x;
    }
}

/*@var LC 137 · Single Number II (others appear three times)
note: Count each bit over all numbers; a count that is not a multiple of 3 belongs to the single number.
@*/
class LC136v1 {
    public int singleNumber(int[] nums) {
        int ans = 0;
        for (int b = 0; b < 32; b++) {
            int ones = 0;
            for (int x : nums) ones += (x >> b) & 1;
            if (ones % 3 != 0) ans |= 1 << b;
        }
        return ans;
    }
}

/*@var LC 260 · Single Number III (two singles)
note: XOR all gives a ^ b. Its lowest set bit is a bit where a and b differ; XOR only the numbers with that bit to get a.
@*/
class LC136v2 {
    public int[] singleNumber(int[] nums) {
        int xor = 0;
        for (int x : nums) xor ^= x;
        int low = xor & -xor;
        int a = 0;
        for (int x : nums) if ((x & low) != 0) a ^= x;
        return new int[]{a, xor ^ a};
    }
}

/*@LC 1442
tag: Prefix XOR
cue: Count triplets i < j ≤ k with xor(i..j−1) == xor(j..k).
hook: a == b means a ^ b == 0: the whole run i..k XORs to zero, and any j inside works.
time: O(n²) time · O(1) space (O(n) version below)
trap: For a zero run i..k there are `k − i` choices of j, not 1.
var: LC 1310 XOR Queries of a Subarray | Prefix XOR, answer `P[r + 1] ^ P[l]`.
@*/
class LC1442 {
    public int countTriplets(int[] arr) {
        int ans = 0;
        for (int i = 0; i < arr.length; i++) {
            int x = 0;
            for (int k = i; k < arr.length; k++) {
                x ^= arr[k];                           //* x = xor of arr[i..k]
                if (x == 0) ans += k - i;              //* any j in (i, k]
            }
        }
        return ans;
    }
}

/*@var O(n) with a map of equal prefix XORs
note: For prefix positions p < q with `P[p] == P[q]`, the run p..q−1 is zero and adds `q − 1 − p`. Keep the count and the sum of positions per prefix value.
@*/
class LC1442v1 {
    public int countTriplets(int[] arr) {
        Map<Integer, Integer> cnt = new HashMap<>(), sumPos = new HashMap<>();
        cnt.put(0, 1);
        sumPos.put(0, 0);
        int prefix = 0, ans = 0;
        for (int k = 0; k < arr.length; k++) {
            prefix ^= arr[k];                          // P[k + 1]
            ans += cnt.getOrDefault(prefix, 0) * k - sumPos.getOrDefault(prefix, 0);
            cnt.merge(prefix, 1, Integer::sum);
            sumPos.merge(prefix, k + 1, Integer::sum);
        }
        return ans;
    }
}

/*@LC 421
tag: Binary trie
cue: Maximum XOR of any two numbers.
hook: From the top bit down, walk to the opposite bit whenever it exists.
time: O(31 · n) time · O(31 · n) space
trap: Size the trie as `n · 31 + 1` nodes, and use `next[node][bit] == 0` as "no child" (0 is the root, never a child).
@*/
class LC421 {
    public int findMaximumXOR(int[] nums) {
        int[][] next = new int[nums.length * 31 + 1][2];         // node 0 = root
        int size = 1, best = 0;
        for (int x : nums) {
            int node = 0;
            for (int b = 30; b >= 0; b--) {                      // insert x
                int bit = (x >> b) & 1;
                if (next[node][bit] == 0) next[node][bit] = size++;
                node = next[node][bit];
            }
            node = 0;
            int cur = 0;
            for (int b = 30; b >= 0; b--) {                      // best partner for x
                int want = ((x >> b) & 1) ^ 1;                   //* the opposite bit
                if (next[node][want] != 0) { cur |= 1 << b; node = next[node][want]; }   //*
                else node = next[node][want ^ 1];
            }
            best = Math.max(best, cur);
        }
        return best;
    }
}

/*@var Maximum XOR of a subarray (GfG)
note: Subarray XOR = `P[r+1] ^ P[l]`, so it is "max XOR of two prefixes". Insert 0 first. Uses `XorTrie` from the pattern core.
@*/
class LC421v1 extends Core08 {
    static int maxSubarrayXor(int[] a) {
        XorTrie trie = new XorTrie(a.length + 1);
        trie.insert(0);                                // the empty prefix
        int prefix = 0, best = 0;
        for (int x : a) {
            prefix ^= x;
            best = Math.max(best, trie.maxXor(prefix));
            trie.insert(prefix);
        }
        return best;
    }
}

/*@LC 1803
tag: Trie with counts · "less than" walk
cue: Count pairs whose XOR lies in [low, high].
hook: Pairs below high + 1 minus pairs below low; where the limit has a 1, everything on the 0 side counts.
time: O(15 · n) time · O(15 · n) space
trap: Each node stores how many numbers pass through it; add `cnt[child]` for the whole smaller side at once.
@*/
class LC1803 {
    private static final int B = 15;                   // values < 2^15
    private int[][] next;
    private int[] cnt;
    private int size;

    public int countPairs(int[] nums, int low, int high) {
        next = new int[nums.length * B + 1][2];
        cnt = new int[nums.length * B + 1];
        size = 1;
        int ans = 0;
        for (int x : nums) {
            ans += countLess(x, high + 1) - countLess(x, low);   //*
            insert(x);
        }
        return ans;
    }

    private void insert(int x) {
        int node = 0;
        for (int b = B - 1; b >= 0; b--) {
            int bit = (x >> b) & 1;
            if (next[node][bit] == 0) next[node][bit] = size++;
            node = next[node][bit];
            cnt[node]++;
        }
    }

    private int countLess(int x, int limit) {          // inserted y with (x ^ y) < limit
        int node = 0, res = 0;
        for (int b = B - 1; b >= 0; b--) {
            int xb = (x >> b) & 1;
            if (((limit >> b) & 1) == 1) {
                if (next[node][xb] != 0) res += cnt[next[node][xb]];   //* xor bit 0 < limit bit 1: all count
                node = next[node][xb ^ 1];                             //* xor bit 1: still equal, go on
            } else {
                node = next[node][xb];                                 //* xor bit must be 0
            }
            if (node == 0) break;                                      // no such path
        }
        return res;
    }
}

/*@LC 2429
tag: Greedy on bits
cue: x with the same number of set bits as num2, minimising x XOR num1.
hook: Cancel num1's high bits first, then fill the lowest free bits.
time: O(31) time · O(1) space
trap: The second loop must skip bits already set in x.
@*/
class LC2429 {
    public int minimizeXor(int num1, int num2) {
        int need = Integer.bitCount(num2), x = 0;
        for (int b = 30; b >= 0 && need > 0; b--)
            if (((num1 >> b) & 1) == 1) { x |= 1 << b; need--; }     //* copy num1's top bits
        for (int b = 0; b <= 30 && need > 0; b++)
            if (((x >> b) & 1) == 0) { x |= 1 << b; need--; }        //* leftovers go low
        return x;
    }
}

/*@LC 1707
tag: Offline queries + trie
cue: For each query (x, m), max x XOR v over v ≤ m.
hook: Sort queries by m, sort numbers, insert numbers as they become allowed.
time: O((n + q) · 30 + sorting) time · O(30 · n) space
trap: Answers must go back in the ORIGINAL query order: sort an index array, not the queries.
@*/
class LC1707 {
    public int[] maximizeXor(int[] nums, int[][] queries) {
        Arrays.sort(nums);
        int q = queries.length;
        Integer[] order = new Integer[q];
        for (int i = 0; i < q; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> Integer.compare(queries[a][1], queries[b][1]));   //* by limit m
        int[][] next = new int[nums.length * 30 + 1][2];
        int size = 1, p = 0;
        int[] ans = new int[q];
        for (int qi : order) {
            int x = queries[qi][0], m = queries[qi][1];
            while (p < nums.length && nums[p] <= m) {                                  //* insert all <= m
                int node = 0;
                for (int b = 29; b >= 0; b--) {
                    int bit = (nums[p] >> b) & 1;
                    if (next[node][bit] == 0) next[node][bit] = size++;
                    node = next[node][bit];
                }
                p++;
            }
            if (p == 0) { ans[qi] = -1; continue; }                                    //* nothing allowed
            int node = 0, cur = 0;
            for (int b = 29; b >= 0; b--) {
                int want = ((x >> b) & 1) ^ 1;
                if (next[node][want] != 0) { cur |= 1 << b; node = next[node][want]; }
                else node = next[node][want ^ 1];
            }
            ans[qi] = cur;
        }
        return ans;
    }
}

public class Day08 {
    public static void main(String[] args) {
        T.eq("core tricks", Core08.tricks(12, 1), T.ints("[0,14,12,14,4,8,2,3]"));
        for (int t = 0; t < 200; t++) {
            long[] a = new long[T.rnd(1, 8)];
            for (int i = 0; i < a.length; i++) a[i] = T.rnd(0, 63);
            long best = 0;
            for (int mask = 0; mask < 1 << a.length; mask++) { long x = 0; for (int i = 0; i < a.length; i++) if ((mask >> i & 1) == 1) x ^= a[i]; best = Math.max(best, x); }
            T.eq("core basis", Core08b.maxSubsetXor(a), best);
        }

        T.eq("136a", new LC136().singleNumber(T.ints("[2,2,1]")), 1);
        T.eq("136b", new LC136().singleNumber(T.ints("[4,1,2,1,2]")), 4);
        T.eq("137a", new LC136v1().singleNumber(T.ints("[2,2,3,2]")), 3);
        T.eq("137b", new LC136v1().singleNumber(T.ints("[0,1,0,1,0,1,99]")), 99);
        T.eq("137c", new LC136v1().singleNumber(T.ints("[-2,-2,1,1,4,1,4,4,-4,-2]")), -4);
        T.eq("260a", T.sorted(new LC136v2().singleNumber(T.ints("[1,2,1,3,2,5]"))), "[3, 5]");
        T.eq("260b", T.sorted(new LC136v2().singleNumber(T.ints("[-1,0]"))), "[-1, 0]");

        T.eq("1442a", new LC1442().countTriplets(T.ints("[2,3,1,6,7]")), 4);
        T.eq("1442b", new LC1442().countTriplets(T.ints("[1,1,1,1,1]")), 10);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 9), 1, 4);
            int c = 0;
            for (int i = 0; i < a.length; i++) for (int j = i + 1; j < a.length; j++) for (int k = j; k < a.length; k++) {
                int x = 0, y = 0;
                for (int p = i; p < j; p++) x ^= a[p];
                for (int p = j; p <= k; p++) y ^= a[p];
                if (x == y) c++;
            }
            T.eq("1442 rand", new LC1442().countTriplets(a), c);
            T.eq("1442v rand", new LC1442v1().countTriplets(a), c);
        }

        T.eq("421a", new LC421().findMaximumXOR(T.ints("[3,10,5,25,2,8]")), 28);
        T.eq("421b", new LC421().findMaximumXOR(T.ints("[14,70,53,83,49,91,36,80,92,51,66,70]")), 127);
        T.eq("421c", new LC421().findMaximumXOR(new int[]{Integer.MAX_VALUE, 0}), Integer.MAX_VALUE);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 8), 0, 1000);
            int best = 0, sub = 0;
            for (int i = 0; i < a.length; i++) for (int j = i; j < a.length; j++) best = Math.max(best, a[i] ^ a[j]);
            for (int i = 0; i < a.length; i++) { int x = 0; for (int j = i; j < a.length; j++) sub = Math.max(sub, x ^= a[j]); }
            T.eq("421 rand", new LC421().findMaximumXOR(a), best);
            T.eq("subxor rand", LC421v1.maxSubarrayXor(a), sub);
        }

        T.eq("1803a", new LC1803().countPairs(T.ints("[1,4,2,7]"), 2, 6), 6);
        T.eq("1803b", new LC1803().countPairs(T.ints("[9,8,4,2,1]"), 5, 14), 8);
        for (int t = 0; t < 200; t++) {
            int[] a = T.rand(T.rnd(1, 9), 1, 40);
            int lo = T.rnd(1, 30), hi = lo + T.rnd(0, 30), c = 0;
            for (int i = 0; i < a.length; i++) for (int j = i + 1; j < a.length; j++) { int x = a[i] ^ a[j]; if (x >= lo && x <= hi) c++; }
            T.eq("1803 rand", new LC1803().countPairs(a, lo, hi), c);
        }

        T.eq("2429a", new LC2429().minimizeXor(3, 5), 3);
        T.eq("2429b", new LC2429().minimizeXor(1, 12), 3);
        for (int t = 0; t < 200; t++) {
            int a = T.rnd(1, 200), b = T.rnd(1, 200), best = -1;
            for (int x = 0; x < 1024; x++) if (Integer.bitCount(x) == Integer.bitCount(b) && (best == -1 || (x ^ a) < (best ^ a))) best = x;
            T.eq("2429 rand", new LC2429().minimizeXor(a, b), best);
        }

        T.eq("1707a", new LC1707().maximizeXor(T.ints("[0,1,2,3,4]"), T.grid("[[3,1],[1,3],[5,6]]")), T.ints("[3,3,7]"));
        T.eq("1707b", new LC1707().maximizeXor(T.ints("[5,2,4,6,6,3]"), T.grid("[[12,4],[8,1],[6,3]]")), T.ints("[15,-1,5]"));
        for (int t = 0; t < 100; t++) {
            int[] a = T.rand(T.rnd(1, 8), 0, 50);
            int[][] qs = new int[T.rnd(1, 6)][];
            int[] want = new int[qs.length];
            for (int i = 0; i < qs.length; i++) {
                qs[i] = new int[]{T.rnd(0, 60), T.rnd(0, 60)};
                want[i] = -1;
                for (int v : a) if (v <= qs[i][1]) want[i] = Math.max(want[i], v ^ qs[i][0]);
            }
            T.eq("1707 rand", new LC1707().maximizeXor(a.clone(), qs), want);
        }
        T.summary("Day08");
    }
}
