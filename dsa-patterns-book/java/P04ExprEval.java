import java.util.*;

public class P04ExprEval {
    //@tpl rpn
    // Reverse Polish notation: operands wait on the stack for their operator.
    static int evalRPN(String[] tokens) {
        Deque<Integer> st = new ArrayDeque<>();
        for (String t : tokens) {
            if (t.length() == 1 && "+-*/".indexOf(t.charAt(0)) >= 0) {
                int b = st.pop(), a = st.pop();          // b was pushed last
                if (t.equals("+")) st.push(a + b);
                else if (t.equals("-")) st.push(a - b);
                else if (t.equals("*")) st.push(a * b);
                else st.push(a / b);                     // truncates toward zero
            } else {
                st.push(Integer.parseInt(t));            // "-11" is a number
            }
        }
        return st.pop();
    }
    //@end

    //@tpl calc2
    // Basic calculator II: + - * / with precedence, no brackets.
    // The stack holds signed terms; * and / fold into the last term at once.
    static int calculate2(String s) {
        Deque<Long> terms = new ArrayDeque<>();
        long num = 0;
        char op = '+';                                   // operator before num
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c)) num = num * 10 + (c - '0');
            boolean last = i == s.length() - 1;
            if (c == '+' || c == '-' || c == '*' || c == '/' || last) {
                if (op == '+') terms.push(num);
                else if (op == '-') terms.push(-num);
                else if (op == '*') terms.push(terms.pop() * num);
                else terms.push(terms.pop() / num);      // -7 / 2 == -3
                op = c;
                num = 0;
            }
        }
        long sum = 0;
        for (long t : terms) sum += t;
        return (int) sum;
    }
    //@end

    //@tpl calc1
    // Basic calculator: + - and brackets; '-' may be unary, as in "-(2 + 3)".
    // A number's sign is the product of the signs of the brackets around it,
    // so a stack of bracket signs is all you need.
    static int calculate(String s) {
        Deque<Integer> signs = new ArrayDeque<>();
        signs.push(1);                                   // sign of the outside
        int result = 0, num = 0, sign = 1;               // sign of the next number
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) {
                num = num * 10 + (c - '0');
            } else if (c == '+' || c == '-') {
                result += sign * num;
                num = 0;
                sign = signs.peek() * (c == '+' ? 1 : -1);
            } else if (c == '(') {
                signs.push(sign);                        // sign of this bracket
            } else if (c == ')') {
                signs.pop();
            }
        }
        return result + sign * num;
    }
    //@end

    // ---------------- tests (not printed) ----------------
    // RPN: random expression tree, evaluated recursively, printed in postfix.
    static int evalTree(Random r, int depth, List<String> out) {
        if (depth == 0 || r.nextInt(3) == 0) {
            int v = r.nextInt(19) - 9;
            out.add(String.valueOf(v));
            return v;
        }
        int a = evalTree(r, depth - 1, out);
        int b = evalTree(r, depth - 1, out);
        char op = "+-*/".charAt(r.nextInt(4));
        if (op == '/' && b == 0) op = '+';
        out.add(String.valueOf(op));
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            default: return a / b;
        }
    }

    // Recursive descent parsers used as the brute force.
    static String src;
    static int pos;

    static void skip() { while (pos < src.length() && src.charAt(pos) == ' ') pos++; }

    static long number() {
        skip();
        long v = 0;
        while (pos < src.length() && Character.isDigit(src.charAt(pos))) v = v * 10 + (src.charAt(pos++) - '0');
        return v;
    }

    // calc2 grammar: expr := term (('+'|'-') term)*, term := num (('*'|'/') num)*
    static long expr2() {
        long v = term2();
        while (true) {
            skip();
            if (pos >= src.length()) return v;
            char c = src.charAt(pos);
            if (c == '+') { pos++; v += term2(); }
            else if (c == '-') { pos++; v -= term2(); }
            else return v;
        }
    }

    static long term2() {
        long v = number();
        while (true) {
            skip();
            if (pos >= src.length()) return v;
            char c = src.charAt(pos);
            if (c == '*') { pos++; v *= number(); }
            else if (c == '/') { pos++; v /= number(); }
            else return v;
        }
    }

    // calc1 grammar: expr := ['-'] atom (('+'|'-') atom)*, atom := num | '(' expr ')'
    static long expr1() {
        skip();
        long v;
        if (src.charAt(pos) == '-') { pos++; v = -atom1(); }
        else v = atom1();
        while (true) {
            skip();
            if (pos >= src.length() || src.charAt(pos) == ')') return v;
            char c = src.charAt(pos++);
            if (c == '+') v += atom1();
            else v -= atom1();
        }
    }

    static long atom1() {
        skip();
        if (src.charAt(pos) == '(') {
            pos++;
            long v = expr1();
            skip();
            pos++;  // ')'
            return v;
        }
        return number();
    }

    static String sp(Random r) { return r.nextInt(4) == 0 ? " " : ""; }

    static String gen2(Random r) {
        StringBuilder sb = new StringBuilder(sp(r));
        int terms = 1 + r.nextInt(6);
        for (int i = 0; i < terms; i++) {
            char op = "+-*/".charAt(r.nextInt(4));
            int v = r.nextInt(8) == 0 ? r.nextInt(100000) : r.nextInt(20);
            if (i > 0) {
                if (op == '/' && v == 0) v = 1 + r.nextInt(9);
                sb.append(sp(r)).append(op).append(sp(r));
            }
            sb.append(v);
        }
        return sb.append(sp(r)).toString();
    }

    static String gen1(Random r, int depth) {
        StringBuilder sb = new StringBuilder();
        if (r.nextInt(4) == 0) sb.append('-').append(sp(r));
        int atoms = 1 + r.nextInt(4);
        for (int i = 0; i < atoms; i++) {
            if (i > 0) sb.append(sp(r)).append(r.nextBoolean() ? '+' : '-').append(sp(r));
            if (depth < 3 && r.nextInt(3) == 0) sb.append('(').append(sp(r)).append(gen1(r, depth + 1)).append(sp(r)).append(')');
            else sb.append(r.nextInt(6) == 0 ? r.nextInt(100000) : r.nextInt(30));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Random r = new Random(42);
        for (int t = 0; t < 4000; t++) {
            List<String> toks = new ArrayList<>();
            int want = evalTree(r, 1 + r.nextInt(4), toks);
            Check.eq(want, evalRPN(toks.toArray(new String[0])), "rpn " + toks);

            String e2 = gen2(r);
            src = e2;
            pos = 0;
            long want2 = expr2();
            if (want2 == (int) want2) Check.eq(want2, calculate2(e2), "calc2 '" + e2 + "'");

            String e1 = (r.nextInt(5) == 0 ? " " : "") + gen1(r, 0);
            src = e1;
            pos = 0;
            Check.eq(expr1(), calculate(e1), "calc1 '" + e1 + "'");
        }
        Check.eq(9, evalRPN(new String[] {"2", "1", "+", "3", "*"}), "LC 150 sample 1");
        Check.eq(6, evalRPN(new String[] {"4", "13", "5", "/", "+"}), "LC 150 sample 2");
        Check.eq(22, evalRPN(new String[] {"10", "6", "9", "3", "+", "-11", "*", "/", "*", "17", "+", "5", "+"}), "LC 150 sample 3");
        Check.eq(7, calculate2("3+2*2"), "LC 227 sample 1");
        Check.eq(1, calculate2(" 3/2 "), "LC 227 sample 2");
        Check.eq(5, calculate2(" 3+5 / 2 "), "LC 227 sample 3");
        Check.eq(10, calculate2("3+5*2-7/2"), "trace example");
        Check.eq(100000, calculate2("100000*100000/100000"), "long terms");
        Check.eq(2, calculate("1 + 1"), "LC 224 sample 1");
        Check.eq(3, calculate(" 2-1 + 2 "), "LC 224 sample 2");
        Check.eq(23, calculate("(1+(4+5+2)-3)+(6+8)"), "LC 224 sample 3");
        Check.eq(-5, calculate("-(2 + 3)"), "unary minus");
        Check.eq(3, calculate("1-(-2)"), "unary minus in bracket");
        Check.done("P04ExprEval");
    }
}
