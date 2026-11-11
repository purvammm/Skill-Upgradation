import java.util.*;

public class P01MajorityVote {
    //@tpl majority
    // Boyer-Moore vote: a value seen more than n/2 times survives
    // when every vote for it is cancelled by one vote against it.
    static int majorityElement(int[] a) {
        int cand = 0, votes = 0;
        for (int x : a) {
            if (votes == 0) cand = x;       // nothing left: new candidate
            votes += (x == cand) ? 1 : -1;  // agree +1, cancel -1
        }
        return cand;                        // valid only if a majority is guaranteed
    }
    //@end

    //@tpl two
    // Majority element II: every value seen more than n/3 times (at most 2).
    // A cancel removes three different values at once, so a value with
    // more than n/3 copies cannot be cancelled out completely.
    static List<Integer> majorityElementII(int[] a) {
        int c1 = 0, c2 = 0, v1 = 0, v2 = 0;
        for (int x : a) {         // 1. find two candidates
            if (x == c1) v1++;
            else if (x == c2) v2++;
            else if (v1 == 0) { c1 = x; v1 = 1; }
            else if (v2 == 0) { c2 = x; v2 = 1; }
            else { v1--; v2--; }  // x, c1, c2 cancel
        }
        v1 = v2 = 0;
        for (int x : a) {         // 2. verify: count them again
            if (x == c1) v1++;
            else if (x == c2) v2++;
        }
        List<Integer> out = new ArrayList<>();
        if (v1 > a.length / 3) out.add(c1);
        if (v2 > a.length / 3) out.add(c2);
        return out;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static List<Integer> bruteThird(int[] a) {
        Map<Integer, Integer> cnt = new TreeMap<>();
        for (int x : a) cnt.merge(x, 1, Integer::sum);
        List<Integer> out = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : cnt.entrySet())
            if (e.getValue() > a.length / 3) out.add(e.getKey());
        return out;
    }

    static int[] withMajority(Random r, int n, int lo, int hi) {
        int maj = lo + r.nextInt(hi - lo + 1);
        int copies = n / 2 + 1 + r.nextInt(n - n / 2);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i < copies ? maj : lo + r.nextInt(hi - lo + 1);
        for (int i = n - 1; i > 0; i--) {
            int j = r.nextInt(i + 1), t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
        return a;
    }

    public static void main(String[] args) {
        Random r = new Random(808);
        for (int t = 0; t < 5000; t++) {
            int n = 1 + r.nextInt(15);
            int[] a = withMajority(r, n, -3, 3);
            int expected = Integer.MIN_VALUE;
            Map<Integer, Integer> cnt = new HashMap<>();
            for (int x : a) cnt.merge(x, 1, Integer::sum);
            for (Map.Entry<Integer, Integer> e : cnt.entrySet())
                if (e.getValue() > n / 2) expected = e.getKey();
            Check.isTrue(expected != Integer.MIN_VALUE, "generator made a majority");
            Check.eq(expected, majorityElement(a), "majority " + Arrays.toString(a));

            int[] b = Check.ints(r, r.nextInt(15), -2, 2);
            List<Integer> got = new ArrayList<>(majorityElementII(b));
            Collections.sort(got);
            Check.eq(bruteThird(b), got, "third " + Arrays.toString(b));
        }
        Check.eq(3, majorityElement(new int[] {3, 2, 3}), "LC 169 sample 1");
        Check.eq(2, majorityElement(new int[] {2, 2, 1, 1, 1, 2, 2}), "LC 169 sample 2");
        Check.eq(Arrays.asList(3), majorityElementII(new int[] {3, 2, 3}), "LC 229 sample 1");
        Check.eq(Arrays.asList(1), majorityElementII(new int[] {1}), "LC 229 sample 2");
        List<Integer> s3 = new ArrayList<>(majorityElementII(new int[] {1, 2}));
        Collections.sort(s3);
        Check.eq(Arrays.asList(1, 2), s3, "LC 229 sample 3");
        Check.done("P01MajorityVote");
    }
}
