import java.util.*;

public class P03BucketSort {
    //@tpl topk
    // LC 347: the k most frequent values. A count is at most n, so bucket[f]
    // lists the values seen exactly f times; read the buckets from f = n down.
    static int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int x : nums) freq.merge(x, 1, Integer::sum);
        List<List<Integer>> bucket = new ArrayList<>();
        for (int f = 0; f <= nums.length; f++) bucket.add(new ArrayList<>());
        for (Map.Entry<Integer, Integer> e : freq.entrySet())
            bucket.get(e.getValue()).add(e.getKey());
        int[] out = new int[k];
        int n = 0;
        for (int f = nums.length; f >= 1 && n < k; f--)
            for (int x : bucket.get(f))
                if (n < k) out[n++] = x;
        return out;
    }
    //@end

    //@tpl chars
    // LC 451: characters by frequency, most frequent first.
    static String frequencySort(String s) {
        int[] cnt = new int[128];                       // 1. counting sort's count
        for (int i = 0; i < s.length(); i++) cnt[s.charAt(i)]++;
        StringBuilder[] bucket = new StringBuilder[s.length() + 1];
        for (char c = 0; c < 128; c++) {                // 2. bucket by count
            if (cnt[c] == 0) continue;
            if (bucket[cnt[c]] == null) bucket[cnt[c]] = new StringBuilder();
            for (int i = 0; i < cnt[c]; i++) bucket[cnt[c]].append(c);
        }
        StringBuilder out = new StringBuilder();
        for (int f = s.length(); f >= 1; f--)           // 3. read high to low
            if (bucket[f] != null) out.append(bucket[f]);
        return out.toString();
    }
    //@end

    //@tpl gap
    // LC 164: largest gap between neighbours in sorted order, in O(n).
    // Pigeonhole: n values in [min, max] leave a gap >= (max - min) / (n - 1).
    // Buckets narrower than that cannot hold the answer inside them, so only
    // each bucket's min and max matter.
    static int maximumGap(int[] a) {
        int n = a.length, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int x : a) { min = Math.min(min, x); max = Math.max(max, x); }
        if (n < 2 || min == max) return 0;
        int width = Math.max(1, (max - min) / (n - 1));
        int m = (max - min) / width + 1;                // number of buckets
        int[] lo = new int[m], hi = new int[m];
        Arrays.fill(lo, Integer.MAX_VALUE);
        Arrays.fill(hi, Integer.MIN_VALUE);
        for (int x : a) {
            int b = (x - min) / width;
            lo[b] = Math.min(lo[b], x);
            hi[b] = Math.max(hi[b], x);
        }
        int best = 0, prev = hi[0];                     // bucket 0 holds min
        for (int b = 1; b < m; b++) {
            if (lo[b] == Integer.MAX_VALUE) continue;   // empty bucket
            best = Math.max(best, lo[b] - prev);
            prev = hi[b];
        }
        return best;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int bruteGap(int[] a) {
        int[] s = a.clone();
        Arrays.sort(s);
        int best = 0;
        for (int i = 1; i < s.length; i++) best = Math.max(best, s[i] - s[i - 1]);
        return best;
    }

    public static void main(String[] args) {
        Random r = new Random(59);
        for (int t = 0; t < 5000; t++) {
            int n = 1 + r.nextInt(15);
            int[] a = Check.ints(r, n, -4, 4);
            Map<Integer, Integer> freq = new HashMap<>();
            for (int x : a) freq.merge(x, 1, Integer::sum);
            List<Integer> fs = new ArrayList<>(freq.values());
            Collections.sort(fs, Collections.reverseOrder());
            int k = 1 + r.nextInt(freq.size());
            int[] got = topKFrequent(a, k);
            List<Integer> gf = new ArrayList<>();
            Set<Integer> distinct = new HashSet<>();
            for (int x : got) { gf.add(freq.getOrDefault(x, 0)); distinct.add(x); }
            Collections.sort(gf, Collections.reverseOrder());
            Check.eq(fs.subList(0, k), gf, "topk freqs " + Arrays.toString(a) + " k=" + k);
            Check.eq(k, distinct.size(), "topk distinct");
            String s = Check.str(r, n, "aabbcZ09");
            String o = frequencySort(s);
            char[] x1 = s.toCharArray(), x2 = o.toCharArray();
            Arrays.sort(x1);
            Arrays.sort(x2);
            Check.eq(x1, x2, "chars permutation " + s);
            int prevCount = Integer.MAX_VALUE;
            Set<Character> done = new HashSet<>();
            for (int i = 0; i < o.length(); ) {
                int j = i;
                while (j < o.length() && o.charAt(j) == o.charAt(i)) j++;
                Check.isTrue(done.add(o.charAt(i)) && j - i <= prevCount, "chars grouped, counts fall: " + o);
                prevCount = j - i;
                i = j;
            }
            int[] g = r.nextBoolean() ? Check.ints(r, n, 0, 30) : Check.ints(r, n, 0, 1000000000);
            Check.eq(bruteGap(g), maximumGap(g), "gap " + Arrays.toString(g));
        }
        int[] wide = {0, 1000000000, 1, 999999999};
        Check.eq(999999998, maximumGap(wide), "gap wide");
        // LeetCode samples
        int[] t1 = topKFrequent(new int[] {1, 1, 1, 2, 2, 3}, 2);
        Arrays.sort(t1);
        Check.eq(new int[] {1, 2}, t1, "LC 347 sample 1");
        Check.eq(new int[] {1}, topKFrequent(new int[] {1}, 1), "LC 347 sample 2");
        String f1 = frequencySort("tree");
        Check.isTrue(f1.equals("eert") || f1.equals("eetr"), "LC 451 sample 1");
        String f2 = frequencySort("cccaaa");
        Check.isTrue(f2.equals("cccaaa") || f2.equals("aaaccc"), "LC 451 sample 2");
        String f3 = frequencySort("Aabb");
        Check.isTrue(f3.equals("bbAa") || f3.equals("bbaA"), "LC 451 sample 3");
        Check.eq(3, maximumGap(new int[] {3, 6, 9, 1}), "LC 164 sample 1");
        Check.eq(0, maximumGap(new int[] {10}), "LC 164 sample 2");
        Check.done("P03BucketSort");
    }
}
