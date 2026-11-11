import java.util.*;

public class P11Trie {
    //@tpl trie
    static class Node {
        Node[] next = new Node[26];      // child for each letter, or null
        boolean end;                     // some word ends at this node
    }

    static class Trie {                  // LC 208
        final Node root = new Node();

        void insert(String w) {
            Node cur = root;
            for (char c : w.toCharArray()) {
                if (cur.next[c - 'a'] == null) cur.next[c - 'a'] = new Node();
                cur = cur.next[c - 'a'];
            }
            cur.end = true;
        }

        // The node reached by spelling p from the root, or null.
        Node walk(String p) {
            Node cur = root;
            for (int i = 0; i < p.length() && cur != null; i++)
                cur = cur.next[p.charAt(i) - 'a'];
            return cur;
        }

        boolean search(String w) { Node x = walk(w); return x != null && x.end; }
        boolean startsWith(String p) { return walk(p) != null; }
    }
    //@end

    //@tpl dots
    // LC 211: a letter follows one child; '.' tries all 26 children.
    // Call matches(trie.root, word, 0).
    static boolean matches(Node x, String w, int i) {
        if (x == null) return false;
        if (i == w.length()) return x.end;
        char c = w.charAt(i);
        if (c != '.') return matches(x.next[c - 'a'], w, i + 1);
        for (Node child : x.next)
            if (matches(child, w, i + 1)) return true;
        return false;
    }
    //@end

    //@tpl board
    // LC 212: one backtracking walk of the board moves through the trie at
    // the same time. An end node stores its word; clear it once found.
    static class WNode { WNode[] next = new WNode[26]; String word; }

    static List<String> findWords(char[][] board, String[] words) {
        WNode root = new WNode();
        for (String w : words) {
            WNode cur = root;
            for (char c : w.toCharArray()) {
                if (cur.next[c - 'a'] == null) cur.next[c - 'a'] = new WNode();
                cur = cur.next[c - 'a'];
            }
            cur.word = w;
        }
        List<String> found = new ArrayList<>();
        for (int r = 0; r < board.length; r++)
            for (int c = 0; c < board[0].length; c++) dfs(board, r, c, root, found);
        return found;
    }

    static void dfs(char[][] b, int r, int c, WNode parent, List<String> found) {
        if (r < 0 || c < 0 || r >= b.length || c >= b[0].length) return;
        char ch = b[r][c];
        if (ch == '#' || parent.next[ch - 'a'] == null) return;  // used, or no word
        WNode x = parent.next[ch - 'a'];
        if (x.word != null) { found.add(x.word); x.word = null; }  // report once
        b[r][c] = '#';                                    // mark the cell used
        dfs(b, r + 1, c, x, found); dfs(b, r - 1, c, x, found);
        dfs(b, r, c + 1, x, found); dfs(b, r, c - 1, x, found);
        b[r][c] = ch;                                     // undo
        if (x.word == null && isLeaf(x)) parent.next[ch - 'a'] = null;  // prune
    }

    static boolean isLeaf(WNode x) {
        for (WNode y : x.next) if (y != null) return false;
        return true;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // LC 648 (variant): walk each word down the trie, stop at the first end node
    static String replaceWords(List<String> roots, String sentence) {
        Trie t = new Trie();
        for (String r : roots) t.insert(r);
        StringBuilder out = new StringBuilder();
        for (String w : sentence.split(" ")) {
            Node cur = t.root;
            int i = 0;
            while (i < w.length() && !cur.end && cur.next[w.charAt(i) - 'a'] != null)
                cur = cur.next[w.charAt(i++) - 'a'];
            if (out.length() > 0) out.append(' ');
            out.append(cur.end ? w.substring(0, i) : w);
        }
        return out.toString();
    }

    static String bruteReplace(List<String> roots, String sentence) {
        StringBuilder out = new StringBuilder();
        for (String w : sentence.split(" ")) {
            String best = w;
            for (String r : roots) if (w.startsWith(r) && r.length() < best.length()) best = r;
            if (out.length() > 0) out.append(' ');
            out.append(best);
        }
        return out.toString();
    }

    static boolean dotMatch(String word, String pat) {
        if (word.length() != pat.length()) return false;
        for (int i = 0; i < word.length(); i++)
            if (pat.charAt(i) != '.' && pat.charAt(i) != word.charAt(i)) return false;
        return true;
    }

    // brute word search: one plain DFS per word
    static boolean exist(char[][] b, String w) {
        for (int r = 0; r < b.length; r++)
            for (int c = 0; c < b[0].length; c++) if (go(b, r, c, w, 0)) return true;
        return false;
    }

    static boolean go(char[][] b, int r, int c, String w, int i) {
        if (i == w.length()) return true;
        if (r < 0 || c < 0 || r >= b.length || c >= b[0].length || b[r][c] != w.charAt(i)) return false;
        char ch = b[r][c];
        b[r][c] = '#';
        boolean ok = go(b, r + 1, c, w, i + 1) || go(b, r - 1, c, w, i + 1)
                || go(b, r, c + 1, w, i + 1) || go(b, r, c - 1, w, i + 1);
        b[r][c] = ch;
        return ok;
    }

    // trap: not clearing the word reports it once per path
    static List<String> findWordsNoClear(char[][] board, String[] words) {
        WNode root = new WNode();
        for (String w : words) {
            WNode cur = root;
            for (char c : w.toCharArray()) {
                if (cur.next[c - 'a'] == null) cur.next[c - 'a'] = new WNode();
                cur = cur.next[c - 'a'];
            }
            cur.word = w;
        }
        List<String> found = new ArrayList<>();
        for (int r = 0; r < board.length; r++)
            for (int c = 0; c < board[0].length; c++) dfsNoClear(board, r, c, root, found);
        return found;
    }

    static void dfsNoClear(char[][] b, int r, int c, WNode parent, List<String> found) {
        if (r < 0 || c < 0 || r >= b.length || c >= b[0].length) return;
        char ch = b[r][c];
        if (ch == '#' || parent.next[ch - 'a'] == null) return;
        WNode x = parent.next[ch - 'a'];
        if (x.word != null) found.add(x.word);
        b[r][c] = '#';
        dfsNoClear(b, r + 1, c, x, found); dfsNoClear(b, r - 1, c, x, found);
        dfsNoClear(b, r, c + 1, x, found); dfsNoClear(b, r, c - 1, x, found);
        b[r][c] = ch;
    }

    static char[][] grid(String... rows) {
        char[][] g = new char[rows.length][];
        for (int i = 0; i < rows.length; i++) g[i] = rows[i].toCharArray();
        return g;
    }

    // LC 2416 (variant): cnt per node
    static class CNode { CNode[] next = new CNode[26]; int cnt; }

    static int[] sumPrefixScores(String[] words) {
        CNode root = new CNode();
        for (String w : words) {
            CNode cur = root;
            for (char c : w.toCharArray()) {
                if (cur.next[c - 'a'] == null) cur.next[c - 'a'] = new CNode();
                cur = cur.next[c - 'a'];
                cur.cnt++;
            }
        }
        int[] out = new int[words.length];
        for (int i = 0; i < words.length; i++) {
            CNode cur = root;
            for (char c : words[i].toCharArray()) { cur = cur.next[c - 'a']; out[i] += cur.cnt; }
        }
        return out;
    }

    public static void main(String[] args) {
        Random r = new Random(1105);
        for (int t = 0; t < 1500; t++) {
            String alpha = t % 2 == 0 ? "ab" : "abc";
            Trie trie = new Trie();
            Set<String> words = new HashSet<>();
            for (int op = 0; op < 40; op++) {
                String w = Check.str(r, 1 + r.nextInt(5), alpha);
                int kind = r.nextInt(4);
                if (kind == 0) { trie.insert(w); words.add(w); }
                else if (kind == 1) Check.eq(words.contains(w), trie.search(w), "search " + w);
                else if (kind == 2) {
                    String p = w.substring(0, 1 + r.nextInt(w.length()));
                    boolean any = false;
                    for (String x : words) if (x.startsWith(p)) any = true;
                    Check.eq(any, trie.startsWith(p), "startsWith " + p);
                } else {
                    StringBuilder pat = new StringBuilder(w);
                    for (int i = 0; i < pat.length(); i++) if (r.nextInt(3) == 0) pat.setCharAt(i, '.');
                    boolean any = false;
                    for (String x : words) if (dotMatch(x, pat.toString())) any = true;
                    Check.eq(any, matches(trie.root, pat.toString(), 0), "dots " + pat);
                }
            }
            List<String> roots = new ArrayList<>();
            for (int i = 0; i < 1 + r.nextInt(4); i++) roots.add(Check.str(r, 1 + r.nextInt(3), alpha));
            StringBuilder sen = new StringBuilder();
            for (int i = 0; i < 1 + r.nextInt(5); i++) {
                if (i > 0) sen.append(' ');
                sen.append(Check.str(r, 1 + r.nextInt(5), alpha));
            }
            Check.eq(bruteReplace(roots, sen.toString()), replaceWords(roots, sen.toString()), "replace " + sen);
            String[] ww = new String[1 + r.nextInt(5)];
            for (int i = 0; i < ww.length; i++) ww[i] = Check.str(r, 1 + r.nextInt(4), alpha);
            int[] want0 = new int[ww.length];
            for (int i = 0; i < ww.length; i++)
                for (int len = 1; len <= ww[i].length(); len++)
                    for (String x : ww) if (x.startsWith(ww[i].substring(0, len))) want0[i]++;
            Check.eq(want0, sumPrefixScores(ww), "LC 2416 " + Arrays.toString(ww));
            // word search II
            int R = 1 + r.nextInt(4), C = 1 + r.nextInt(4);
            char[][] b = new char[R][C];
            for (char[] row : b) for (int j = 0; j < C; j++) row[j] = alpha.charAt(r.nextInt(alpha.length()));
            Set<String> ws = new LinkedHashSet<>();
            for (int i = 0; i < 1 + r.nextInt(8); i++) ws.add(Check.str(r, 1 + r.nextInt(6), alpha));
            String[] wa = ws.toArray(new String[0]);
            List<String> want = new ArrayList<>();
            for (String w : wa) if (exist(b, w)) want.add(w);
            Check.eq(Check.sortedCopy(want), Check.sortedCopy(findWords(b, wa)), "board " + Arrays.deepToString(b) + " " + ws);
        }
        // LeetCode samples
        Trie t = new Trie();
        t.insert("apple");
        Check.isTrue(t.search("apple"), "LC 208 search apple");
        Check.isTrue(!t.search("app"), "LC 208 search app");
        Check.isTrue(t.startsWith("app"), "LC 208 startsWith app");
        t.insert("app");
        Check.isTrue(t.search("app"), "LC 208 search app after insert");
        Trie d = new Trie();
        d.insert("bad"); d.insert("dad"); d.insert("mad");
        Check.isTrue(!matches(d.root, "pad", 0), "LC 211 pad");
        Check.isTrue(matches(d.root, "bad", 0), "LC 211 bad");
        Check.isTrue(matches(d.root, ".ad", 0), "LC 211 .ad");
        Check.isTrue(matches(d.root, "b..", 0), "LC 211 b..");
        Check.eq("the cat was rat by the bat",
                replaceWords(Arrays.asList("cat", "bat", "rat"), "the cattle was rattled by the battery"), "LC 648 sample 1");
        Check.eq("a a b c", replaceWords(Arrays.asList("a", "b", "c"), "aadsfasf absbs bbab cadsfafs"), "LC 648 sample 2");
        Check.eq(new int[] {5, 4, 3, 2}, sumPrefixScores(new String[] {"abc", "ab", "bc", "b"}), "LC 2416 sample 1");
        char[][] b1 = grid("oaan", "etae", "ihkr", "iflv");
        Check.eq("[eat, oath]", Check.sortedCopy(findWords(b1, new String[] {"oath", "pea", "eat", "rain"})), "LC 212 sample 1");
        Check.eq("[]", Check.sortedCopy(findWords(grid("ab", "cd"), new String[] {"abcb"})), "LC 212 sample 2");
        // big board: 12 x 12 of 'a', words a, aa, ..., up to length 10, plus one that is missing
        char[][] big = new char[12][12];
        for (char[] row : big) Arrays.fill(row, 'a');
        List<String> bw = new ArrayList<>();
        for (int len = 1; len <= 10; len++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) sb.append('a');
            bw.add(sb.toString());
        }
        Check.eq(10, findWords(big, bw.toArray(new String[0])).size(), "big board");
        // traps
        Trie tr = new Trie();
        tr.insert("apple");
        Check.isTrue(tr.walk("app") != null && !tr.search("app"), "prefix is not a word");
        Check.eq("[a, a]", findWordsNoClear(grid("aa"), new String[] {"a"}).toString(), "no clear: duplicates");
        Check.eq("[a]", findWords(grid("aa"), new String[] {"a"}).toString(), "clear: once");
        Check.done("P11Trie");
    }
}
