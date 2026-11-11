import java.util.*;

public class P13OrderedMap {
    //@tpl calendar
    // LC 729: bookings are half-open [s, e) and never overlap, so sorted by start
    // they are also sorted by end. A new booking can only hit its two neighbours.
    static class MyCalendar {
        final TreeMap<Integer, Integer> cal = new TreeMap<>();     // start -> end

        boolean book(int s, int e) {
            Map.Entry<Integer, Integer> prev = cal.floorEntry(s);   // start <= s
            Integer next = cal.ceilingKey(s);                       // start >= s
            if (prev != null && prev.getValue() > s) return false;  // runs into s
            if (next != null && next < e) return false;             // starts before e
            cal.put(s, e);
            return true;
        }
    }

    // LC 220: indexes at most k apart, values at most d apart. The set holds
    // the last k values; the only candidate is the smallest value >= x - d.
    static boolean containsNearbyAlmostDuplicate(int[] nums, int k, int d) {
        TreeSet<Long> window = new TreeSet<>();
        for (int i = 0; i < nums.length; i++) {
            long x = nums[i];
            Long c = window.ceiling(x - d);
            if (c != null && c <= x + d) return true;
            window.add(x);
            if (i >= k) window.remove((long) nums[i - k]);   // keep k values
        }
        return false;
    }
    //@end

    //@tpl intervals
    // Disjoint intervals [l, r] kept merged in a TreeMap start -> end. LC 352
    // calls add(v, v) and reads map.entrySet() in order; LC 2276 asks count().
    static class Intervals {
        final TreeMap<Integer, Integer> map = new TreeMap<>();
        long total = 0;                          // integers covered

        void add(int l, int r) {
            // absorb every stored interval that overlaps or touches [l, r]
            Map.Entry<Integer, Integer> e = map.floorEntry(r + 1);
            while (e != null && e.getValue() >= l - 1) {
                l = Math.min(l, e.getKey());
                r = Math.max(r, e.getValue());
                total -= e.getValue() - e.getKey() + 1;
                map.remove(e.getKey());
                e = map.floorEntry(r + 1);
            }
            map.put(l, r);
            total += r - l + 1;
        }

        long count() { return total; }
    }
    //@end

    // LC 352 getIntervals: the entries in key order
    static int[][] toArray(Intervals iv) {
        int[][] out = new int[iv.map.size()][];
        int i = 0;
        for (Map.Entry<Integer, Integer> e : iv.map.entrySet()) out[i++] = new int[] {e.getKey(), e.getValue()};
        return out;
    }

    // ---------------- tests (not printed) ----------------
    // LC 2034 (variant): TreeMap as a multiset of prices
    static class StockPrice {
        final Map<Integer, Integer> at = new HashMap<>();
        final TreeMap<Integer, Integer> prices = new TreeMap<>();
        int latest = 0;
        void update(int time, int price) {
            Integer old = at.put(time, price);
            if (old != null && prices.merge(old, -1, Integer::sum) == 0) prices.remove(old);
            prices.merge(price, 1, Integer::sum);
            latest = Math.max(latest, time);
        }
        int current() { return at.get(latest); }
        int maximum() { return prices.lastKey(); }
        int minimum() { return prices.firstKey(); }
    }

    // the trap: a TreeSet of prices loses duplicates
    static class StockSet {
        final Map<Integer, Integer> at = new HashMap<>();
        final TreeSet<Integer> prices = new TreeSet<>();
        void update(int time, int price) {
            Integer old = at.put(time, price);
            if (old != null) prices.remove(old);
            prices.add(price);
        }
        int maximum() { return prices.last(); }
    }

    static boolean bruteDup(int[] a, int k, int d) {
        for (int i = 0; i < a.length; i++)
            for (int j = i + 1; j < a.length && j - i <= k; j++)
                if (Math.abs((long) a[i] - a[j]) <= d) return true;
        return false;
    }

    public static void main(String[] args) {
        //@tpl api
        TreeMap<Integer, String> m = new TreeMap<>();
        m.put(10, "a"); m.put(20, "b"); m.put(30, "c");
        Integer fl = m.floorKey(25);           // 20: largest key <= 25, or null
        Integer ce = m.ceilingKey(25);         // 30: smallest key >= 25
        Integer lo = m.lowerKey(20);           // 10: largest key <  20
        Integer hi = m.higherKey(30);          // null: no key > 30
        String v = m.floorEntry(25).getValue();                // "b"
        SortedMap<Integer, String> head = m.headMap(20);       // keys < 20: {10}
        NavigableMap<Integer, String> tail = m.tailMap(20, true);  // >= 20: {20, 30}
        Map.Entry<Integer, String> first = m.pollFirstEntry(); // removes 10=a
        // head and tail are live views of m: head is now {}
        // TreeSet<Integer> has the same calls without "Key": floor, ceiling,
        // lower, higher, first, last, pollFirst, headSet, tailSet.

        // A multiset: count per value; drop the key when its count reaches 0.
        TreeMap<Integer, Integer> bag = new TreeMap<>();
        for (int p : new int[] {5, 3, 5}) bag.merge(p, 1, Integer::sum);
        if (bag.merge(5, -1, Integer::sum) == 0) bag.remove(5);   // one 5 left
        int max = bag.lastKey(), min = bag.firstKey();            // 5 and 3
        //@end
        Check.eq(20, (int) fl, "floorKey");
        Check.eq(30, (int) ce, "ceilingKey");
        Check.eq(10, (int) lo, "lowerKey");
        Check.isTrue(hi == null, "higherKey null");
        Check.eq("b", v, "floorEntry value");
        Check.eq("{}", head.toString(), "headMap is a view");
        TreeMap<Integer, String> m2 = new TreeMap<>();
        m2.put(10, "a"); m2.put(20, "b"); m2.put(30, "c");
        Check.eq("{10=a}", m2.headMap(20).toString(), "headMap before the poll");
        Check.eq("{10=a, 20=b}", m2.headMap(20, true).toString(), "headMap inclusive");
        Check.eq("{20=b, 30=c}", m2.tailMap(20).toString(), "tailMap includes the key");
        Check.eq("{20=b, 30=c}", tail.toString(), "tailMap");
        Check.eq("10=a", first.toString(), "pollFirstEntry");
        Check.eq("{20=b, 30=c}", m.toString(), "after poll");
        Check.eq(5, max, "multiset max");
        Check.eq(3, min, "multiset min");
        Check.eq("{3=1, 5=1}", bag.toString(), "multiset");

        Random r = new Random(1305);
        for (int t = 0; t < 2000; t++) {
            // calendar against a plain list
            MyCalendar cal = new MyCalendar();
            List<int[]> booked = new ArrayList<>();
            for (int op = 0; op < 20; op++) {
                int s = r.nextInt(30), e = s + 1 + r.nextInt(6);
                boolean ok = true;
                for (int[] b : booked) if (Math.max(b[0], s) < Math.min(b[1], e)) ok = false;
                if (ok) booked.add(new int[] {s, e});
                Check.eq(ok, cal.book(s, e), "calendar");
            }
            // contains duplicate III
            int n = 1 + r.nextInt(15);
            int[] a = t % 3 == 0 ? Check.ints(r, n, -1000000000, 1000000000) : Check.ints(r, n, -20, 20);
            int k = 1 + r.nextInt(n), d = t % 3 == 0 ? r.nextInt(1000000001) : r.nextInt(5);
            Check.eq(bruteDup(a, k, d), containsNearbyAlmostDuplicate(a, k, d), "LC 220 " + Arrays.toString(a));
            // intervals against a boolean array
            Intervals iv = new Intervals();
            boolean[] cover = new boolean[42];
            for (int op = 0; op < 20; op++) {
                int l = r.nextInt(40), rr = l + r.nextInt(Math.min(6, 40 - l));
                iv.add(l, rr);
                for (int i = l; i <= rr; i++) cover[i] = true;
                long cnt = 0;
                List<int[]> runs = new ArrayList<>();
                for (int i = 0; i < 42; i++) {
                    if (cover[i]) cnt++;
                    if (cover[i] && (i == 0 || !cover[i - 1])) runs.add(new int[] {i, i});
                    if (cover[i]) runs.get(runs.size() - 1)[1] = i;
                }
                Check.eq(cnt, iv.count(), "count");
                Check.eq(runs.toArray(new int[0][]), toArray(iv), "runs");
            }
            // stock price multiset
            StockPrice sp = new StockPrice();
            Map<Integer, Integer> plain = new HashMap<>();
            for (int op = 0; op < 20; op++) {
                int time = 1 + r.nextInt(8), price = 1 + r.nextInt(6);
                sp.update(time, price);
                plain.put(time, price);
                int mx = Integer.MIN_VALUE, mn = Integer.MAX_VALUE, latest = 0;
                for (Map.Entry<Integer, Integer> e : plain.entrySet()) {
                    mx = Math.max(mx, e.getValue());
                    mn = Math.min(mn, e.getValue());
                    latest = Math.max(latest, e.getKey());
                }
                Check.eq(mx, sp.maximum(), "stock max");
                Check.eq(mn, sp.minimum(), "stock min");
                Check.eq((int) plain.get(latest), sp.current(), "stock current");
            }
        }
        // big: 1e5 single points with values up to 1e9, count fits a long
        Intervals big = new Intervals();
        for (int i = 0; i < 100000; i++) big.add(i * 10000, i * 10000 + 9999);
        Check.eq(1, toArray(big).length, "big merge");
        Check.eq(1000000000L, big.count(), "big count");
        int[] bigA = new int[100000];
        for (int i = 0; i < bigA.length; i++) bigA[i] = i * 3;
        Check.isTrue(!containsNearbyAlmostDuplicate(bigA, 10000, 2), "big LC 220");
        // LeetCode samples
        MyCalendar c = new MyCalendar();
        Check.isTrue(c.book(10, 20), "LC 729 book 1");
        Check.isTrue(!c.book(15, 25), "LC 729 book 2");
        Check.isTrue(c.book(20, 30), "LC 729 book 3");
        Check.isTrue(!c.book(5, 12), "trace book [5, 12)");
        Check.isTrue(c.book(5, 10), "trace book [5, 10)");
        Check.isTrue(containsNearbyAlmostDuplicate(new int[] {1, 2, 3, 1}, 3, 0), "LC 220 sample 1");
        Check.isTrue(!containsNearbyAlmostDuplicate(new int[] {1, 5, 9, 1, 5, 9}, 2, 3), "LC 220 sample 2");
        Intervals sr = new Intervals();
        int[] vals = {1, 3, 7, 2, 6};
        String[] want = {"[[1, 1]]", "[[1, 1], [3, 3]]", "[[1, 1], [3, 3], [7, 7]]", "[[1, 3], [7, 7]]", "[[1, 3], [6, 7]]"};
        for (int i = 0; i < vals.length; i++) {
            sr.add(vals[i], vals[i]);
            Check.eq(want[i], Arrays.deepToString(toArray(sr)), "LC 352 step " + i);
        }
        Intervals ci = new Intervals();
        ci.add(2, 3);
        ci.add(7, 10);
        Check.eq(6, ci.count(), "LC 2276 count 1");
        ci.add(5, 8);
        Check.eq(8, ci.count(), "LC 2276 count 2");
        StockPrice st = new StockPrice();
        st.update(1, 10); st.update(2, 5);
        Check.eq(5, st.current(), "LC 2034 current");
        Check.eq(10, st.maximum(), "LC 2034 max");
        st.update(1, 3);
        Check.eq(5, st.maximum(), "LC 2034 max 2");
        st.update(4, 2);
        Check.eq(2, st.minimum(), "LC 2034 min");
        // traps
        StockSet ss = new StockSet();
        ss.update(1, 10); ss.update(2, 10); ss.update(1, 5);
        Check.eq(5, ss.maximum(), "set loses the second 10");
        StockPrice sp2 = new StockPrice();
        sp2.update(1, 10); sp2.update(2, 10); sp2.update(1, 5);
        Check.eq(10, sp2.maximum(), "multiset keeps it");
        boolean npe = false;
        try {
            TreeMap<Integer, Integer> one = new TreeMap<>();
            one.put(10, 1);
            int key = one.floorKey(5);
            Check.isTrue(key < 0, "unreachable");
        } catch (NullPointerException e) {
            npe = true;
        }
        Check.isTrue(npe, "unboxing a null floorKey");
        TreeSet<int[]> byFirst = new TreeSet<>((x, y) -> Integer.compare(x[0], y[0]));
        byFirst.add(new int[] {1, 2});
        byFirst.add(new int[] {1, 3});
        Check.eq(1, byFirst.size(), "comparator ties drop elements");
        boolean cme = false;
        try {
            TreeMap<Integer, Integer> tm = new TreeMap<>();
            tm.put(1, 2); tm.put(3, 4); tm.put(5, 6);
            for (Integer key : tm.tailMap(2).keySet()) tm.remove(key);
        } catch (ConcurrentModificationException e) {
            cme = true;
        }
        Check.isTrue(cme, "remove while iterating a view");
        Check.done("P13OrderedMap");
    }
}
