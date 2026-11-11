// @notest: this program reads stdin, so P00JavaKit runs it on sample inputs instead.
//@tpl main
import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        FastReader in = new FastReader(System.in);
        PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));
        int t = in.nextInt();                    // number of test cases
        while (t-- > 0) {
            int n = in.nextInt();
            long[] a = new long[n];
            for (int i = 0; i < n; i++) a[i] = in.nextLong();
            out.println(solve(n, a));
        }
        out.flush();                             // prints nothing without this
    }

    static long solve(int n, long[] a) {         // your algorithm goes here
        long sum = 0;
        for (long x : a) sum += x;
        return sum;
    }

    // static class FastReader { ... }  is the next block; paste it here
//@end

    //@tpl reader
    // Reads numbers and words from a byte buffer: much faster than Scanner.
    static class FastReader {
        private final InputStream is;
        private final byte[] buf = new byte[1 << 16];
        private int len = 0, ptr = 0;

        FastReader(InputStream is) { this.is = is; }

        private int read() throws IOException {
            if (ptr == len) {
                len = is.read(buf, 0, buf.length);
                ptr = 0;
                if (len <= 0) return -1;
            }
            return buf[ptr++];
        }

        long nextLong() throws IOException {
            int c = read();
            while (c != '-' && (c < '0' || c > '9')) {
                if (c == -1) throw new EOFException();
                c = read();
            }
            boolean neg = c == '-';
            if (neg) c = read();
            long x = 0;
            while (c >= '0' && c <= '9') { x = x * 10 + (c - '0'); c = read(); }
            return neg ? -x : x;
        }

        int nextInt() throws IOException { return (int) nextLong(); }

        String next() throws IOException {        // one word, skipping spaces
            int c = read();
            while (c != -1 && c <= ' ') c = read();
            StringBuilder sb = new StringBuilder();
            while (c != -1 && c > ' ') { sb.append((char) c); c = read(); }
            return sb.toString();
        }
    }
    //@end
}
