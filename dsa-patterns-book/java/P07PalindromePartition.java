import java.util.*;

public class P07PalindromePartition {
    //@tpl partition
    static List<List<String>> partition(String s) {
        int n = s.length();
        boolean[][] isPal = new boolean[n][n];      // s[i..j] is a palindrome
        for (int i = n - 1; i >= 0; i--)
            for (int j = i; j < n; j++)
                isPal[i][j] = s.charAt(i) == s.charAt(j)
                              && (j - i < 2 || isPal[i + 1][j - 1]);
        List<List<String>> out = new ArrayList<>();
        cut(s, 0, isPal, new ArrayList<>(), out);
        return out;
    }

    // Choose the first piece s[start..end], then partition the rest the same way.
    static void cut(String s, int start, boolean[][] isPal, List<String> path,
                    List<List<String>> out) {
        if (start == s.length()) { out.add(new ArrayList<>(path)); return; }
        for (int end = start; end < s.length(); end++) {
            if (!isPal[start][end]) continue;
            path.add(s.substring(start, end + 1));
            cut(s, end + 1, isPal, path, out);
            path.remove(path.size() - 1);
        }
    }
    //@end

    //@tpl ip
    // Restore IP addresses: 4 pieces, each 0..255, no leading zeros.
    static List<String> restoreIpAddresses(String s) {
        List<String> out = new ArrayList<>();
        dot(s, 0, new ArrayList<>(), out);
        return out;
    }

    static void dot(String s, int start, List<String> parts, List<String> out) {
        int left = 4 - parts.size(), rest = s.length() - start;
        if (rest < left || rest > 3 * left) return;          // prune: cannot fit
        if (left == 0) { out.add(String.join(".", parts)); return; }
        for (int len = 1; len <= 3 && start + len <= s.length(); len++) {
            String p = s.substring(start, start + len);
            if (len > 1 && p.charAt(0) == '0') break;         // "01" is not allowed
            if (Integer.parseInt(p) > 255) break;
            parts.add(p);
            dot(s, start + len, parts, out);
            parts.remove(parts.size() - 1);
        }
    }
    //@end

    //@tpl parens
    // Generate parentheses: '(' while open < n, ')' while close < open.
    static List<String> generateParenthesis(int n) {
        List<String> out = new ArrayList<>();
        brackets(n, 0, 0, new StringBuilder(), out);
        return out;
    }

    static void brackets(int n, int open, int close, StringBuilder sb,
                         List<String> out) {
        if (sb.length() == 2 * n) { out.add(sb.toString()); return; }
        if (open < n) {
            sb.append('(');
            brackets(n, open + 1, close, sb, out);
            sb.setLength(sb.length() - 1);
        }
        if (close < open) {
            sb.append(')');
            brackets(n, open, close + 1, sb, out);
            sb.setLength(sb.length() - 1);
        }
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean pal(String t) {
        return new StringBuilder(t).reverse().toString().equals(t);
    }

    // Every set of cut positions (2^(n-1) of them); keep those with all pieces palindromes.
    static List<String> brutePartition(String s) {
        int n = s.length();
        List<String> out = new ArrayList<>();
        if (n == 0) { out.add("[]"); return out; }
        for (int mask = 0; mask < (1 << (n - 1)); mask++) {
            List<String> pieces = new ArrayList<>();
            int from = 0;
            boolean ok = true;
            for (int i = 0; i < n; i++) {
                if (i == n - 1 || (mask >> i & 1) == 1) {
                    String p = s.substring(from, i + 1);
                    if (!pal(p)) ok = false;
                    pieces.add(p);
                    from = i + 1;
                }
            }
            if (ok) out.add(pieces.toString());
        }
        Collections.sort(out);
        return out;
    }

    static boolean okPart(String p) {
        if (p.isEmpty() || p.length() > 3) return false;
        if (p.length() > 1 && p.charAt(0) == '0') return false;
        return Integer.parseInt(p) <= 255;
    }

    static List<String> bruteIp(String s) {
        List<String> out = new ArrayList<>();
        int n = s.length();
        for (int a = 1; a < n; a++)
            for (int b = a + 1; b < n; b++)
                for (int c = b + 1; c < n; c++) {
                    String[] p = {s.substring(0, a), s.substring(a, b), s.substring(b, c), s.substring(c)};
                    boolean ok = true;
                    for (String x : p) ok &= okPart(x);
                    if (ok) out.add(String.join(".", p));
                }
        return out;
    }

    static List<String> bruteParens(int n) {
        List<String> out = new ArrayList<>();
        for (int mask = 0; mask < (1 << (2 * n)); mask++) {
            StringBuilder sb = new StringBuilder();
            int bal = 0;
            boolean ok = true;
            for (int i = 0; i < 2 * n; i++) {
                boolean open = (mask >> i & 1) == 1;
                sb.append(open ? '(' : ')');
                bal += open ? 1 : -1;
                if (bal < 0) ok = false;
            }
            if (ok && bal == 0) out.add(sb.toString());
        }
        return out;
    }

    static List<String> asStrings(List<List<String>> parts) {
        List<String> s = new ArrayList<>();
        for (List<String> p : parts) s.add(p.toString());
        Collections.sort(s);
        return s;
    }

    public static void main(String[] args) {
        Random r = new Random(77);
        for (int t = 0; t < 2000; t++) {
            String s = Check.str(r, r.nextInt(11), "aab");
            Check.eq(brutePartition(s), asStrings(partition(s)), "partition " + s);
            String d = Check.str(r, 4 + r.nextInt(9), "0012559");
            Check.eq(Check.sortedCopy(bruteIp(d)), Check.sortedCopy(restoreIpAddresses(d)), "ip " + d);
        }
        for (int n = 1; n <= 8; n++) {
            List<String> got = generateParenthesis(n);
            Check.eq(Check.sortedCopy(bruteParens(n)), Check.sortedCopy(got), "parens n=" + n);
        }
        Check.eq("[[a, a, b], [aa, b]]", asStrings(partition("aab")).toString(), "LC 131 sample 1");
        Check.eq("[[a]]", asStrings(partition("a")).toString(), "LC 131 sample 2");
        Check.eq("[255.255.11.135, 255.255.111.35]", Check.sortedCopy(restoreIpAddresses("25525511135")),
                 "LC 93 sample 1");
        Check.eq("[0.0.0.0]", Check.sortedCopy(restoreIpAddresses("0000")), "LC 93 sample 2");
        Check.eq("[1.0.10.23, 1.0.102.3, 10.1.0.23, 10.10.2.3, 101.0.2.3]",
                 Check.sortedCopy(restoreIpAddresses("101023")), "LC 93 sample 3");
        Check.eq("[((())), (()()), (())(), ()(()), ()()()]", Check.sortedCopy(generateParenthesis(3)),
                 "LC 22 sample 1");
        Check.eq("[()]", Check.sortedCopy(generateParenthesis(1)), "LC 22 sample 2");
        Check.done("P07PalindromePartition");
    }
}
