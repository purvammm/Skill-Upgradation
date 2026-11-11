import java.util.*;

public class P12BitTricks {
    //@tpl ops
    // Bit i counts from 0 at the right (the 1s place).
    static boolean has(int x, int i)  { return ((x >> i) & 1) == 1; }
    static int set(int x, int i)      { return x | (1 << i); }
    static int clear(int x, int i)    { return x & ~(1 << i); }
    static int toggle(int x, int i)   { return x ^ (1 << i); }
    static int dropLowest(int x)      { return x & (x - 1); }   // 101100 -> 101000
    static int lowestBit(int x)       { return x & -x; }        // 101100 -> 000100

    // Number of 1 bits: each round removes the lowest one. Same as Integer.bitCount.
    static int popcount(int x) {
        int c = 0;
        for (; x != 0; x &= x - 1) c++;
        return c;
    }

    static boolean isPowerOfTwo(int n) { return n > 0 && (n & (n - 1)) == 0; }

    // bits[i] = number of 1 bits in i; i >> 1 is i without its last bit.
    static int[] countBits(int n) {
        int[] bits = new int[n + 1];
        for (int i = 1; i <= n; i++) bits[i] = bits[i >> 1] + (i & 1);
        return bits;
    }
    //@end

    //@tpl submasks
    // Every submask of mask, largest first, 0 last.
    // (s - 1) & mask is the largest submask below s.
    static List<Integer> submasks(int mask) {
        List<Integer> out = new ArrayList<>();
        for (int s = mask; ; s = (s - 1) & mask) {
            out.add(s);
            if (s == 0) break;          // 0 is a submask too; stop after it
        }
        return out;
    }
    //@end

    //@tpl apps
    // Reverse bits: move n's lowest bit onto r, 32 times.
    static int reverseBits(int n) {
        int r = 0;
        for (int i = 0; i < 32; i++) {
            r = (r << 1) | (n & 1);
            n >>>= 1;                   // >>> shifts in 0s, also when n < 0
        }
        return r;
    }

    // Minimum flips so that (a | b) == c, one bit position at a time.
    static int minFlips(int a, int b, int c) {
        int flips = 0;
        for (int i = 0; i < 31; i++) {
            int x = (a >> i) & 1, y = (b >> i) & 1, z = (c >> i) & 1;
            if (z == 1) flips += (x | y) == 0 ? 1 : 0;   // turn one of them on
            else flips += x + y;                          // turn both off
        }
        return flips;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int bruteLowestIndex(int x) {
        for (int i = 0; i < 32; i++) if (((x >>> i) & 1) == 1) return i;
        return -1;
    }

    static int bruteMinFlips(int a, int b, int c, int bits) {
        int best = Integer.MAX_VALUE;
        for (int a2 = 0; a2 < (1 << bits); a2++)
            for (int b2 = 0; b2 < (1 << bits); b2++)
                if ((a2 | b2) == c)
                    best = Math.min(best, Integer.bitCount(a ^ a2) + Integer.bitCount(b ^ b2));
        return best;
    }

    static int bruteReverse(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 31; i >= 0; i--) sb.append((n >>> i) & 1);
        return (int) Long.parseLong(sb.reverse().toString(), 2);
    }

    public static void main(String[] args) {
        Random r = new Random(12);
        for (int t = 0; t < 20000; t++) {
            int x = t % 4 == 0 ? r.nextInt(64) : r.nextInt();
            int i = r.nextInt(32);
            long u = x & 0xFFFFFFFFL;
            Check.eq(((u >> i) & 1) == 1, has(x, i), "has " + x + " " + i);
            Check.eq((int) (u | (1L << i)), set(x, i), "set");
            Check.eq((int) (u & ~(1L << i)), clear(x, i), "clear");
            Check.eq((int) (u ^ (1L << i)), toggle(x, i), "toggle");
            Check.isTrue(has(set(x, i), i) && !has(clear(x, i), i), "set/clear");
            Check.eq(has(x, i), !has(toggle(x, i), i), "toggle flips");
            int lo = bruteLowestIndex(x);
            Check.eq(lo < 0 ? 0 : (int) (u & ~(1L << lo)), dropLowest(x), "dropLowest " + x);
            Check.eq(lo < 0 ? 0 : (int) (1L << lo), lowestBit(x), "lowestBit " + x);
            if (x != 0) Check.eq(lo, Integer.numberOfTrailingZeros(x), "ntz");
            Check.eq(Long.bitCount(u), popcount(x), "popcount " + x);
            boolean pow = false;
            for (int k = 0; k < 31; k++) if (x == (1 << k)) pow = true;
            Check.eq(pow, isPowerOfTwo(x), "pow2 " + x);
            Check.eq(bruteReverse(x), reverseBits(x), "reverse " + x);
            Check.eq(Integer.reverse(x), reverseBits(x), "reverse lib " + x);
        }
        for (int k = 0; k < 31; k++) Check.isTrue(isPowerOfTwo(1 << k), "pow2 " + k);
        Check.isTrue(!isPowerOfTwo(0) && !isPowerOfTwo(Integer.MIN_VALUE) && !isPowerOfTwo(-8), "pow2 edge");
        int[] bits = countBits(100000);
        for (int i = 0; i <= 100000; i++) Check.eq(Integer.bitCount(i), bits[i], "countBits " + i);
        Check.eq(new int[] {0, 1, 1, 2, 1, 2}, countBits(5), "LC 338 sample");
        // submasks: compare with a scan; total over all masks of n bits is 3^n
        for (int n = 0; n <= 10; n++) {
            long total = 0;
            for (int mask = 0; mask < (1 << n); mask++) {
                List<Integer> exp = new ArrayList<>();
                for (int s = mask; s >= 0; s--) if ((s & ~mask) == 0) exp.add(s);
                List<Integer> got = submasks(mask);
                Check.eq(exp.toString(), got.toString(), "submasks " + mask);
                total += got.size();
            }
            long p3 = 1;
            for (int k = 0; k < n; k++) p3 *= 3;
            Check.eq(p3, total, "3^n " + n);
        }
        for (int t = 0; t < 3000; t++) {
            int a = r.nextInt(16), b = r.nextInt(16), c = r.nextInt(16);
            int exp = bruteMinFlips(a, b, c, 4);
            Check.eq(exp, minFlips(a, b, c), "minFlips " + a + " " + b + " " + c);
            Check.eq(exp, Integer.bitCount((a | b) ^ c) + Integer.bitCount(a & b & ~c), "one-liner");
        }
        for (int t = 0; t < 3000; t++) {
            int a = r.nextInt(Integer.MAX_VALUE), b = r.nextInt(Integer.MAX_VALUE), c = r.nextInt(Integer.MAX_VALUE);
            Check.eq(Integer.bitCount((a | b) ^ c) + Integer.bitCount(a & b & ~c), minFlips(a, b, c), "big flips");
        }
        // Gray code variant: neighbours differ in exactly one bit, all values distinct
        for (int n = 1; n <= 12; n++) {
            boolean[] seen = new boolean[1 << n];
            for (int i = 0; i < (1 << n); i++) {
                int g = i ^ (i >> 1);
                Check.isTrue(!seen[g], "gray distinct");
                seen[g] = true;
                int next = ((i + 1) % (1 << n)) ^ (((i + 1) % (1 << n)) >> 1);
                Check.eq(1, Integer.bitCount(g ^ next), "gray step");
            }
        }
        // traps quoted in the book
        Check.eq(Integer.MIN_VALUE, 1 << 31, "1 << 31");
        Check.eq(1, 1 << 32, "1 << 32 wraps");
        Check.eq(256L, 0L | (1 << 40), "int shift in long mask");
        Check.eq(-1, -1 >> 1, ">> keeps sign");
        int l = 10, rr = 20;
        Check.eq(10, l + (rr - l) >> 1, "precedence");
        Check.isTrue((0 & (0 - 1)) == 0 && (Integer.MIN_VALUE & (Integer.MIN_VALUE - 1)) == 0, "pow2 trap");
        // LeetCode samples
        Check.eq(3, popcount(11), "LC 191 sample 1");
        Check.eq(1, popcount(128), "LC 191 sample 2");
        Check.eq(30, popcount(2147483645), "LC 191 sample 3");
        Check.eq(964176192, reverseBits(43261596), "LC 190 sample 1");
        Check.eq(1073741822, reverseBits(2147483644), "LC 190 sample 2");
        Check.eq(3, minFlips(2, 6, 5), "LC 1318 sample 1");
        Check.eq(1, minFlips(4, 2, 7), "LC 1318 sample 2");
        Check.eq(0, minFlips(1, 2, 3), "LC 1318 sample 3");
        Check.isTrue(isPowerOfTwo(1) && isPowerOfTwo(16) && !isPowerOfTwo(3), "LC 231 samples");
        Check.done("P12BitTricks");
    }
}
