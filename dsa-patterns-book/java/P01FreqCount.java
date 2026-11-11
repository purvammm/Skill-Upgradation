import java.util.*;

public class P01FreqCount {
    //@tpl count
    // Count each value. A missing key counts as 0.
    static Map<Integer, Integer> countValues(int[] a) {
        Map<Integer, Integer> cnt = new HashMap<>();
        for (int x : a) {
            cnt.merge(x, 1, Integer::sum);
            // same as: cnt.put(x, cnt.getOrDefault(x, 0) + 1);
        }
        return cnt;
    }

    // Valid anagram: same letters with the same counts.
    // For 'a'..'z' an int[26] is faster and shorter than a map.
    static boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) return false;
        int[] cnt = new int[26];
        for (int i = 0; i < s.length(); i++) {
            cnt[s.charAt(i) - 'a']++;  // s adds one
            cnt[t.charAt(i) - 'a']--;  // t takes one away
        }
        for (int c : cnt) if (c != 0) return false;
        return true;
    }
    //@end

    //@tpl group
    // Group anagrams: words with the same letter counts get the same key.
    static List<List<String>> groupAnagrams(String[] words) {
        Map<String, List<String>> groups = new HashMap<>();
        for (String w : words) {
            int[] cnt = new int[26];
            for (char c : w.toCharArray()) cnt[c - 'a']++;
            String key = Arrays.toString(cnt);     // "[1, 0, 0, 0, 1, ...]"
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(w);
        }
        return new ArrayList<>(groups.values());
    }
    //@end

    //@tpl unique
    // First unique character: count everything, then scan in order.
    static int firstUniqChar(String s) {
        int[] cnt = new int[26];
        for (char c : s.toCharArray()) cnt[c - 'a']++;
        for (int i = 0; i < s.length(); i++)
            if (cnt[s.charAt(i) - 'a'] == 1) return i;
        return -1;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean bruteAnagram(String s, String t) {
        char[] a = s.toCharArray(), b = t.toCharArray();
        Arrays.sort(a);
        Arrays.sort(b);
        return Arrays.equals(a, b);
    }

    static String canonGroups(List<List<String>> groups) {
        List<String> out = new ArrayList<>();
        for (List<String> g : groups) {
            List<String> c = new ArrayList<>(g);
            Collections.sort(c);
            out.add(c.toString());
        }
        Collections.sort(out);
        return out.toString();
    }

    static List<List<String>> bruteGroups(String[] words) {
        List<List<String>> out = new ArrayList<>();
        boolean[] used = new boolean[words.length];
        for (int i = 0; i < words.length; i++) {
            if (used[i]) continue;
            List<String> g = new ArrayList<>();
            for (int j = i; j < words.length; j++) {
                if (!used[j] && bruteAnagram(words[i], words[j])) {
                    used[j] = true;
                    g.add(words[j]);
                }
            }
            out.add(g);
        }
        return out;
    }

    static int bruteUnique(String s) {
        for (int i = 0; i < s.length(); i++) {
            int c = 0;
            for (int j = 0; j < s.length(); j++) if (s.charAt(j) == s.charAt(i)) c++;
            if (c == 1) return i;
        }
        return -1;
    }

    public static void main(String[] args) {
        Random r = new Random(101);
        for (int t = 0; t < 4000; t++) {
            int n = r.nextInt(9);
            String s = Check.str(r, n, "abc");
            String u = r.nextBoolean() ? Check.str(r, n, "abc") : Check.str(r, r.nextInt(9), "abcz");
            Check.eq(bruteAnagram(s, u), isAnagram(s, u), "anagram " + s + " " + u);
            Check.eq(bruteUnique(s), firstUniqChar(s), "unique " + s);

            String[] words = new String[r.nextInt(8)];
            for (int i = 0; i < words.length; i++) words[i] = Check.str(r, r.nextInt(4), "abt");
            Check.eq(canonGroups(bruteGroups(words)), canonGroups(groupAnagrams(words)),
                    "group " + Arrays.toString(words));

            int[] a = Check.ints(r, n, -3, 3);
            Map<Integer, Integer> cnt = countValues(a);
            int total = 0;
            for (int v = -3; v <= 3; v++) {
                int c = 0;
                for (int x : a) if (x == v) c++;
                Check.eq(c, (int) cnt.getOrDefault(v, 0), "count " + v + " in " + Arrays.toString(a));
                total += c;
            }
            Check.eq(total, a.length, "count total");
            Check.eq((long) cnt.size(), (long) new HashSet<Integer>(cnt.keySet()).size(), "keys");
        }
        Check.eq(true, isAnagram("anagram", "nagaram"), "LC 242 sample 1");
        Check.eq(false, isAnagram("rat", "car"), "LC 242 sample 2");
        Check.eq("[[ate, eat, tea], [bat], [nat, tan]]",
                canonGroups(groupAnagrams(new String[] {"eat", "tea", "tan", "ate", "nat", "bat"})),
                "LC 49 sample");
        Check.eq(0, firstUniqChar("leetcode"), "LC 387 sample 1");
        Check.eq(2, firstUniqChar("loveleetcode"), "LC 387 sample 2");
        Check.eq(-1, firstUniqChar("aabb"), "LC 387 sample 3");
        Check.done("P01FreqCount");
    }
}
