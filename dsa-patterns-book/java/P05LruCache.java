import java.util.*;

public class P05LruCache {
    //@tpl lru
    // LRU cache (LC 146): map key -> node; list order = recency, front = newest.
    static class LRUCache {
        static class Node {
            int key, val;
            Node prev, next;
            Node(int key, int val) { this.key = key; this.val = val; }
        }

        private final int cap;
        private final Map<Integer, Node> map = new HashMap<>();
        private final Node head = new Node(0, 0), tail = new Node(0, 0);  // sentinels

        LRUCache(int capacity) {
            cap = capacity;
            head.next = tail;
            tail.prev = head;
        }

        int get(int key) {
            Node n = map.get(key);
            if (n == null) return -1;
            unlink(n);                        // used now: move to the front
            addFront(n);
            return n.val;
        }

        void put(int key, int value) {
            Node n = map.get(key);
            if (n != null) {                  // update counts as a use
                n.val = value;
                unlink(n);
                addFront(n);
                return;
            }
            if (map.size() == cap) {          // full: evict the oldest, tail.prev
                Node old = tail.prev;
                unlink(old);
                map.remove(old.key);          // this is why a node stores its key
            }
            n = new Node(key, value);
            map.put(key, n);
            addFront(n);
        }

        private void unlink(Node n) { n.prev.next = n.next; n.next.prev = n.prev; }

        private void addFront(Node n) {
            n.prev = head; n.next = head.next;
            head.next.prev = n; head.next = n;
        }
    }
    //@end

    //@tpl linked
    // accessOrder = true moves an entry to the end on get/put; after each put,
    // removeEldestEntry decides whether the first (oldest) entry goes.
    static class LRUShort {
        private final Map<Integer, Integer> map;

        LRUShort(final int capacity) {
            map = new LinkedHashMap<Integer, Integer>(16, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry<Integer, Integer> e) {
                    return size() > capacity;
                }
            };
        }

        int get(int key) { return map.getOrDefault(key, -1); }
        void put(int key, int value) { map.put(key, value); }
    }
    //@end

    // ---------------- tests (not printed) ----------------
    /** Slow reference: a list of {key, value}, most recent first. */
    static class Slow {
        final int cap;
        final List<int[]> items = new ArrayList<>();
        Slow(int cap) { this.cap = cap; }

        int find(int key) {
            for (int i = 0; i < items.size(); i++) if (items.get(i)[0] == key) return i;
            return -1;
        }

        int get(int key) {
            int i = find(key);
            if (i < 0) return -1;
            int[] e = items.remove(i);
            items.add(0, e);
            return e[1];
        }

        void put(int key, int value) {
            int i = find(key);
            if (i >= 0) items.remove(i);
            items.add(0, new int[] {key, value});
            if (items.size() > cap) items.remove(items.size() - 1);
        }

        String keys() {
            List<Integer> k = new ArrayList<>();
            for (int[] e : items) k.add(e[0]);
            return k.toString();
        }
    }

    static String keys(LRUCache c) {
        List<Integer> k = new ArrayList<>();
        for (LRUCache.Node n = c.head.next; n != c.tail; n = n.next) k.add(n.key);
        return k.toString();
    }

    public static void main(String[] args) {
        Random r = new Random(59);
        for (int t = 0; t < 3000; t++) {
            int cap = 1 + r.nextInt(4);
            LRUCache fast = new LRUCache(cap);
            LRUShort shortOne = new LRUShort(cap);
            Slow slow = new Slow(cap);
            int ops = r.nextInt(40);
            for (int i = 0; i < ops; i++) {
                int key = r.nextInt(6);
                if (r.nextBoolean()) {
                    int want = slow.get(key);
                    Check.eq(want, fast.get(key), "get " + key + " t=" + t + " op=" + i);
                    Check.eq(want, shortOne.get(key), "short get " + key + " t=" + t + " op=" + i);
                } else {
                    int val = r.nextInt(100);
                    slow.put(key, val);
                    fast.put(key, val);
                    shortOne.put(key, val);
                }
                Check.eq(slow.keys(), keys(fast), "order t=" + t + " op=" + i);
                Check.eq(slow.items.size(), fast.map.size(), "size t=" + t);
            }
        }
        // LC 146 sample
        int[] want = {1, -1, -1, 3, 4};
        LRUCache c = new LRUCache(2);
        LRUShort s = new LRUShort(2);
        int[] got = new int[5], got2 = new int[5];
        c.put(1, 1); c.put(2, 2); got[0] = c.get(1); c.put(3, 3); got[1] = c.get(2);
        c.put(4, 4); got[2] = c.get(1); got[3] = c.get(3); got[4] = c.get(4);
        s.put(1, 1); s.put(2, 2); got2[0] = s.get(1); s.put(3, 3); got2[1] = s.get(2);
        s.put(4, 4); got2[2] = s.get(1); got2[3] = s.get(3); got2[4] = s.get(4);
        Check.eq(want, got, "LC 146 sample");
        Check.eq(want, got2, "LC 146 sample (LinkedHashMap)");
        Check.done("P05LruCache");
    }
}
