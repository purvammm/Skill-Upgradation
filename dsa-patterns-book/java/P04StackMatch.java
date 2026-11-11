import java.util.*;

public class P04StackMatch {
    //@tpl valid
    // Valid parentheses: each closer must match the most recent unclosed opener.
    static boolean isValid(String s) {
        Deque<Character> st = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if (c == '(') st.push(')');              // push the closer we expect
            else if (c == '[') st.push(']');
            else if (c == '{') st.push('}');
            else if (st.isEmpty() || st.pop() != c) return false;
        }
        return st.isEmpty();                         // nothing left open
    }
    //@end

    //@tpl decode
    // Decode string: "3[a2[c]]" -> "accaccacc".
    // '[' saves the outer level (its count and its text); ']' restores it.
    static String decodeString(String s) {
        Deque<Integer> counts = new ArrayDeque<>();
        Deque<StringBuilder> outer = new ArrayDeque<>();
        StringBuilder cur = new StringBuilder();
        int k = 0;
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) {
                k = k * 10 + (c - '0');              // counts can have 2+ digits
            } else if (c == '[') {
                counts.push(k);
                outer.push(cur);
                cur = new StringBuilder();
                k = 0;
            } else if (c == ']') {
                StringBuilder prev = outer.pop();
                for (int r = counts.pop(); r > 0; r--) prev.append(cur);
                cur = prev;
            } else {
                cur.append(c);
            }
        }
        return cur.toString();
    }
    //@end

    //@tpl cancel
    // Simplify path: a stack of folder names; ".." pops one.
    static String simplifyPath(String path) {
        Deque<String> st = new ArrayDeque<>();
        for (String part : path.split("/")) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (!part.equals("..")) st.push(part);
            else if (!st.isEmpty()) st.pop();            // ".." at the root: no-op
        }
        StringBuilder sb = new StringBuilder();
        while (!st.isEmpty()) sb.append('/').append(st.pollLast());  // bottom first
        return sb.length() == 0 ? "/" : sb.toString();
    }

    // Remove all adjacent duplicates: the StringBuilder is the stack.
    static String removeDuplicates(String s) {
        StringBuilder st = new StringBuilder();
        for (char c : s.toCharArray()) {
            int n = st.length();
            if (n > 0 && st.charAt(n - 1) == c) st.setLength(n - 1);  // pop
            else st.append(c);                                        // push
        }
        return st.toString();
    }
    //@end

    // ---------------- tests (not printed) ----------------
    static boolean bruteValid(String s) {
        String prev;
        do {
            prev = s;
            s = s.replace("()", "").replace("[]", "").replace("{}", "");
        } while (!s.equals(prev));
        return s.isEmpty();
    }

    static String randomBalanced(Random r, int pairs) {
        if (pairs == 0) return "";
        int inside = r.nextInt(pairs);
        int t = r.nextInt(3);
        char open = "([{".charAt(t), close = ")]}".charAt(t);
        return open + randomBalanced(r, inside) + close + randomBalanced(r, pairs - 1 - inside);
    }

    static String bruteDecode(String s) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+)\\[([a-z]*)\\]");
        while (true) {
            java.util.regex.Matcher m = p.matcher(s);
            if (!m.find()) return s;
            StringBuilder rep = new StringBuilder();
            int times = Integer.parseInt(m.group(1));
            for (int i = 0; i < times; i++) rep.append(m.group(2));
            s = s.substring(0, m.start()) + rep + s.substring(m.end());
        }
    }

    static String randomEncoded(Random r, int depth) {
        StringBuilder sb = new StringBuilder();
        int items = 1 + r.nextInt(3);
        for (int i = 0; i < items; i++) {
            if (depth < 3 && r.nextInt(2) == 0) {
                int k = r.nextInt(4) == 0 ? 10 + r.nextInt(3) : 1 + r.nextInt(4);
                sb.append(k).append('[').append(randomEncoded(r, depth + 1)).append(']');
            } else {
                sb.append(Check.str(r, 1 + r.nextInt(2), "abc"));
            }
        }
        return sb.toString();
    }

    static String bruteSimplify(String path) {
        List<String> parts = new ArrayList<>();
        for (String p : path.split("/")) if (!p.isEmpty() && !p.equals(".")) parts.add(p);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < parts.size(); i++) {
                if (parts.get(i).equals("..")) {
                    parts.remove(i);
                    if (i > 0) parts.remove(i - 1);
                    changed = true;
                    break;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String p : parts) sb.append('/').append(p);
        return sb.length() == 0 ? "/" : sb.toString();
    }

    static String bruteRemove(String s) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i + 1 < s.length(); i++) {
                if (s.charAt(i) == s.charAt(i + 1)) {
                    s = s.substring(0, i) + s.substring(i + 2);
                    changed = true;
                    break;
                }
            }
        }
        return s;
    }

    public static void main(String[] args) {
        Random r = new Random(41);
        String[] comps = {"", ".", "..", "...", "a", "b", "c.d", "..e", "_f"};
        for (int t = 0; t < 4000; t++) {
            String s = Check.str(r, r.nextInt(11), "()[]{}");
            Check.eq(bruteValid(s), isValid(s), "valid " + s);
            String b = randomBalanced(r, r.nextInt(6));
            Check.isTrue(isValid(b), "balanced " + b);
            if (b.length() > 0) {
                int i = r.nextInt(b.length());
                String mutated = b.substring(0, i) + "([{)]}".charAt(r.nextInt(6)) + b.substring(i + 1);
                Check.eq(bruteValid(mutated), isValid(mutated), "mutated " + mutated);
            }

            String enc = randomEncoded(r, 0);
            Check.eq(bruteDecode(enc), decodeString(enc), "decode " + enc);

            StringBuilder path = new StringBuilder();
            int parts = r.nextInt(7);
            for (int i = 0; i < parts; i++) path.append('/').append(comps[r.nextInt(comps.length)]);
            if (path.length() == 0 || r.nextInt(3) == 0) path.append('/');
            String p = path.toString();
            Check.eq(bruteSimplify(p), simplifyPath(p), "simplify " + p);

            String d = Check.str(r, r.nextInt(13), r.nextBoolean() ? "ab" : "abc");
            Check.eq(bruteRemove(d), removeDuplicates(d), "remove " + d);
        }
        Check.isTrue(isValid("()"), "LC 20 sample 1");
        Check.isTrue(isValid("()[]{}"), "LC 20 sample 2");
        Check.isTrue(!isValid("(]"), "LC 20 sample 3");
        Check.isTrue(isValid("([])"), "LC 20 sample 4");
        Check.isTrue(!isValid("([)]"), "LC 20 sample 5");
        Check.eq("aaabcbc", decodeString("3[a]2[bc]"), "LC 394 sample 1");
        Check.eq("accaccacc", decodeString("3[a2[c]]"), "LC 394 sample 2");
        Check.eq("abcabccdcdcdef", decodeString("2[abc]3[cd]ef"), "LC 394 sample 3");
        Check.eq("aaaaaaaaaaaa", decodeString("12[a]"), "two-digit count");
        Check.eq("/home", simplifyPath("/home/"), "LC 71 sample 1");
        Check.eq("/home/foo", simplifyPath("/home//foo/"), "LC 71 sample 2");
        Check.eq("/home/user/Pictures", simplifyPath("/home/user/Documents/../Pictures"), "LC 71 sample 3");
        Check.eq("/", simplifyPath("/../"), "LC 71 sample 4");
        Check.eq("/.../b/d", simplifyPath("/.../a/../b/c/../d/./"), "LC 71 sample 5");
        Check.eq("ca", removeDuplicates("abbaca"), "LC 1047 sample 1");
        Check.eq("ay", removeDuplicates("azxxzy"), "LC 1047 sample 2");
        Check.done("P04StackMatch");
    }
}
