import java.util.*;

public class P02MergeSorted {
    //@tpl merge
    // nums1 has length m + n; its last n slots are free (LC 88).
    // Fill from the back: k = i + j + 1 > i, so no unread nums1 value is overwritten.
    static void merge(int[] nums1, int m, int[] nums2, int n) {
        int i = m - 1, j = n - 1, k = m + n - 1;
        while (j >= 0) {                 // once nums2 is used up, nums1 is in place
            if (i >= 0 && nums1[i] > nums2[j]) nums1[k--] = nums1[i--];
            else nums1[k--] = nums2[j--];
        }
    }
    //@end

    //@tpl walk
    // Common values of two sorted arrays, with repeats (LC 350 on sorted input).
    static int[] intersect(int[] a, int[] b) {
        int[] out = new int[Math.min(a.length, b.length)];
        int i = 0, j = 0, k = 0;
        while (i < a.length && j < b.length) {
            if (a[i] < b[j]) i++;            // a[i] is smaller than all of b[j..]
            else if (a[i] > b[j]) j++;
            else { out[k++] = a[i]; i++; j++; }
        }
        return Arrays.copyOf(out, k);
    }

    // Is s a subsequence of t (LC 392)? Match each char of s as early as possible.
    static boolean isSubsequence(String s, String t) {
        int i = 0;
        for (int j = 0; j < t.length() && i < s.length(); j++)
            if (s.charAt(i) == t.charAt(j)) i++;
        return i == s.length();
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static int[] bruteIntersect(int[] a, int[] b) {
        TreeMap<Integer, Integer> ca = new TreeMap<>(), cb = new TreeMap<>();
        for (int x : a) ca.merge(x, 1, Integer::sum);
        for (int x : b) cb.merge(x, 1, Integer::sum);
        List<Integer> out = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : ca.entrySet()) {
            int c = Math.min(e.getValue(), cb.getOrDefault(e.getKey(), 0));
            for (int i = 0; i < c; i++) out.add(e.getKey());
        }
        int[] res = new int[out.size()];
        for (int i = 0; i < res.length; i++) res[i] = out.get(i);
        return res;
    }

    static boolean bruteSubseq(String s, String t) {   // LCS length == |s|
        int[][] dp = new int[s.length() + 1][t.length() + 1];
        for (int i = 1; i <= s.length(); i++)
            for (int j = 1; j <= t.length(); j++)
                dp[i][j] = s.charAt(i - 1) == t.charAt(j - 1) ? dp[i - 1][j - 1] + 1
                        : Math.max(dp[i - 1][j], dp[i][j - 1]);
        return dp[s.length()][t.length()] == s.length();
    }

    public static void main(String[] args) {
        Random r = new Random(27);
        for (int t = 0; t < 5000; t++) {
            int m = r.nextInt(9), n = r.nextInt(9);
            int[] a = Check.sortedInts(r, m, -5, 5);
            int[] b = Check.sortedInts(r, n, -5, 5);
            int[] nums1 = Arrays.copyOf(a, m + n);
            merge(nums1, m, b, n);
            int[] want = Arrays.copyOf(a, m + n);
            System.arraycopy(b, 0, want, m, n);
            Arrays.sort(want);
            Check.eq(want, nums1, "merge " + Arrays.toString(a) + " " + Arrays.toString(b));
            Check.eq(bruteIntersect(a, b), intersect(a, b), "intersect " + Arrays.toString(a) + " " + Arrays.toString(b));
            String s = Check.str(r, r.nextInt(5), "abc"), tt = Check.str(r, r.nextInt(9), "abc");
            Check.eq(bruteSubseq(s, tt), isSubsequence(s, tt), "subseq " + s + " in " + tt);
        }
        int[] l1 = {1, 2, 3, 0, 0, 0};
        merge(l1, 3, new int[] {2, 5, 6}, 3);
        Check.eq(new int[] {1, 2, 2, 3, 5, 6}, l1, "LC 88 sample 1");
        int[] l2 = {1};
        merge(l2, 1, new int[] {}, 0);
        Check.eq(new int[] {1}, l2, "LC 88 sample 2");
        int[] l3 = {0};
        merge(l3, 0, new int[] {1}, 1);
        Check.eq(new int[] {1}, l3, "LC 88 sample 3");
        int[] x1 = {1, 2, 2, 1}, y1 = {2, 2};
        Arrays.sort(x1);
        Arrays.sort(y1);
        Check.eq(new int[] {2, 2}, intersect(x1, y1), "LC 350 sample 1");
        int[] x2 = {4, 9, 5}, y2 = {9, 4, 9, 8, 4};
        Arrays.sort(x2);
        Arrays.sort(y2);
        Check.eq(new int[] {4, 9}, intersect(x2, y2), "LC 350 sample 2");
        Check.eq(true, isSubsequence("abc", "ahbgdc"), "LC 392 sample 1");
        Check.eq(false, isSubsequence("axc", "ahbgdc"), "LC 392 sample 2");
        Check.done("P02MergeSorted");
    }
}
