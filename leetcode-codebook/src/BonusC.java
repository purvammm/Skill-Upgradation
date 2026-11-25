import java.util.*;

/*@LC 146 · LRU Cache · Medium
group: C
class: LRUCache
tag: HashMap + doubly linked list
cue: get / put in O(1); when full, throw out the least recently used key.
hook: The map finds the node; the list keeps the order: newest after head, oldest before tail.
time: O(1) per operation · O(capacity) space
trap: On `get`, move the node to the front too. Two dummy nodes (head, tail) remove every null check.
@*/
class LC146 {
    private static final class Node {
        int key, val;
        Node prev, next;
        Node(int k, int v) { key = k; val = v; }
    }

    private final int capacity;
    private final Map<Integer, Node> map = new HashMap<>();
    private final Node head = new Node(0, 0), tail = new Node(0, 0);   // dummies

    public LC146(int capacity) {
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        Node node = map.get(key);
        if (node == null) return -1;
        unlink(node);
        addFront(node);                                      //* used: now the newest
        return node.val;
    }

    public void put(int key, int value) {
        Node node = map.get(key);
        if (node != null) { node.val = value; unlink(node); addFront(node); return; }
        if (map.size() == capacity) {
            Node old = tail.prev;                            //* least recently used
            unlink(old);
            map.remove(old.key);
        }
        node = new Node(key, value);
        map.put(key, node);
        addFront(node);
    }

    private void unlink(Node n) { n.prev.next = n.next; n.next.prev = n.prev; }   //*

    private void addFront(Node n) {                                               //*
        n.next = head.next;
        n.prev = head;
        head.next.prev = n;
        head.next = n;
    }
}

/*@var Short version with LinkedHashMap (say you know both)
class: LRUCache
note: `accessOrder = true` keeps entries in use order; `removeEldestEntry` evicts when the size passes capacity.
@*/
class LC146v1 extends LinkedHashMap<Integer, Integer> {
    private final int capacity;

    public LC146v1(int capacity) {
        super(16, 0.75f, true);
        this.capacity = capacity;
    }

    public int get(int key) { return super.getOrDefault(key, -1); }

    public void put(int key, int value) { super.put(key, value); }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) { return size() > capacity; }
}

/*@LC 208 · Implement Trie (Prefix Tree) · Medium
group: C
class: Trie
tag: Trie
cue: insert, search a whole word, and check whether any word starts with a prefix.
hook: One child per letter; a flag marks where a word ends.
time: O(L) per operation · O(total letters) space
trap: `search` needs the end flag; `startsWith` does not.
@*/
class LC208 {
    private final LC208[] next = new LC208[26];
    private boolean end;

    public LC208() {}

    public void insert(String word) {
        LC208 node = this;
        for (char c : word.toCharArray()) {
            if (node.next[c - 'a'] == null) node.next[c - 'a'] = new LC208();   //*
            node = node.next[c - 'a'];
        }
        node.end = true;                                                        //*
    }

    public boolean search(String word) {
        LC208 node = walk(word);
        return node != null && node.end;                                        //*
    }

    public boolean startsWith(String prefix) {
        return walk(prefix) != null;
    }

    private LC208 walk(String s) {
        LC208 node = this;
        for (char c : s.toCharArray()) {
            node = node.next[c - 'a'];
            if (node == null) return null;
        }
        return node;
    }
}

/*@var LC 211 · Design Add and Search Words ('.' matches any letter)
class: WordDictionary
note: The same trie; on '.', try every child with a small DFS.
@*/
class LC208v1 {
    private final LC208v1[] next = new LC208v1[26];
    private boolean end;

    public LC208v1() {}

    public void addWord(String word) {
        LC208v1 node = this;
        for (char c : word.toCharArray()) {
            if (node.next[c - 'a'] == null) node.next[c - 'a'] = new LC208v1();
            node = node.next[c - 'a'];
        }
        node.end = true;
    }

    public boolean search(String word) { return find(word, 0, this); }

    private boolean find(String w, int i, LC208v1 node) {
        if (i == w.length()) return node.end;
        char c = w.charAt(i);
        if (c != '.') return node.next[c - 'a'] != null && find(w, i + 1, node.next[c - 'a']);
        for (LC208v1 child : node.next) if (child != null && find(w, i + 1, child)) return true;
        return false;
    }
}

/*@LC 295 · Find Median from Data Stream · Hard
group: C
class: MedianFinder
tag: Two heaps
cue: Add numbers one by one; report the median at any time.
hook: Max-heap holds the small half, min-heap the large half; keep sizes equal or the small half one bigger.
time: O(log n) add · O(1) median
trap: Always pass the new number through one heap into the other, then rebalance; this keeps every small ≤ every large.
@*/
class LC295 {
    private final PriorityQueue<Integer> small = new PriorityQueue<>(Collections.reverseOrder());   // max-heap
    private final PriorityQueue<Integer> large = new PriorityQueue<>();                            // min-heap

    public LC295() {}

    public void addNum(int num) {
        small.offer(num);
        large.offer(small.poll());                                    //* biggest of the small half moves up
        if (large.size() > small.size()) small.offer(large.poll());   //* rebalance
    }

    public double findMedian() {
        return small.size() > large.size() ? small.peek() : (small.peek() + (double) large.peek()) / 2;   //*
    }
}

/*@LC 23 · Merge k Sorted Lists · Hard
group: C
tag: Min-heap of list heads
cue: Merge k sorted linked lists into one.
hook: The heap holds the current head of every list; pop the smallest, push its next.
time: O(N log k) time · O(k) space
trap: Skip null lists when filling the heap.
@*/
class LC23 {
    public ListNode mergeKLists(ListNode[] lists) {
        PriorityQueue<ListNode> heap = new PriorityQueue<>((a, b) -> Integer.compare(a.val, b.val));
        for (ListNode l : lists) if (l != null) heap.offer(l);          //*
        ListNode dummy = new ListNode(0), tail = dummy;
        while (!heap.isEmpty()) {
            ListNode node = heap.poll();                                //* smallest head
            tail.next = node;
            tail = node;
            if (node.next != null) heap.offer(node.next);               //*
        }
        return dummy.next;
    }
}

/*@var LC 21 · Merge Two Sorted Lists
note: Dummy head and a tail pointer; attach the smaller node each time, then the rest.
@*/
class LC23v1 {
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        ListNode dummy = new ListNode(0), tail = dummy;
        while (list1 != null && list2 != null) {
            if (list1.val <= list2.val) { tail.next = list1; list1 = list1.next; }
            else { tail.next = list2; list2 = list2.next; }
            tail = tail.next;
        }
        tail.next = list1 != null ? list1 : list2;
        return dummy.next;
    }
}

/*@LC 98 · Validate Binary Search Tree · Medium
group: C
tag: Recursion with bounds
cue: Is this binary tree a valid BST (strict)?
hook: Every node must lie strictly between the bounds its ancestors set.
time: O(n) time · O(h) space
trap: Checking only parent vs child is wrong; and values can be `Integer.MIN/MAX`, so pass the bounds as `long`.
@*/
class LC98 {
    public boolean isValidBST(TreeNode root) {
        return ok(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    private boolean ok(TreeNode n, long lo, long hi) {
        if (n == null) return true;
        if (n.val <= lo || n.val >= hi) return false;                  //*
        return ok(n.left, lo, n.val) && ok(n.right, n.val, hi);        //* left caps hi, right raises lo
    }
}

/*@var LC 230 · Kth Smallest Element in a BST
note: An in-order walk of a BST visits values in sorted order. Do it with a stack and stop at the k-th pop.
@*/
class LC98v1 {
    public int kthSmallest(TreeNode root, int k) {
        Deque<TreeNode> st = new ArrayDeque<>();
        TreeNode cur = root;
        while (true) {
            while (cur != null) { st.push(cur); cur = cur.left; }    // go as far left as possible
            cur = st.pop();
            if (--k == 0) return cur.val;
            cur = cur.right;
        }
    }
}

/*@LC 1192 · Critical Connections in a Network · Hard
group: C
tag: Bridges (Tarjan low-link)
cue: Edges whose removal disconnects the graph.
hook: `low[v]` = earliest discovery time v's subtree can reach with one back edge. Edge (u, v) is a bridge if `low[v] > tin[u]`.
time: O(V + E) time · O(V + E) space
trap: Skip only the edge back to the PARENT, and update `low[u]` with `tin[w]` for back edges.
var: Articulation points | A non-root u is a cut vertex if some child v has `low[v] >= tin[u]`; the root if it has 2+ children.
@*/
class LC1192 {
    private List<List<Integer>> g;
    private int[] tin, low;
    private int timer;
    private List<List<Integer>> bridges;

    public List<List<Integer>> criticalConnections(int n, List<List<Integer>> connections) {
        g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (List<Integer> e : connections) { g.get(e.get(0)).add(e.get(1)); g.get(e.get(1)).add(e.get(0)); }
        tin = new int[n];
        low = new int[n];
        Arrays.fill(tin, -1);
        timer = 0;
        bridges = new ArrayList<>();
        for (int i = 0; i < n; i++) if (tin[i] == -1) dfs(i, -1);
        return bridges;
    }

    private void dfs(int u, int parent) {
        tin[u] = low[u] = timer++;
        for (int w : g.get(u)) {
            if (w == parent) continue;                               //*
            if (tin[w] != -1) low[u] = Math.min(low[u], tin[w]);     //* back edge
            else {
                dfs(w, u);
                low[u] = Math.min(low[u], low[w]);                   //*
                if (low[w] > tin[u]) bridges.add(List.of(u, w));     //* w cannot climb past u
            }
        }
    }
}

/*@LC 79 · Word Search · Medium
group: C
tag: Grid backtracking
cue: Does the word appear as a path of adjacent cells, each cell used once?
hook: Try every start; mark the cell, explore 4 ways, unmark on the way back.
time: O(m·n·3^L) time · O(L) space
trap: Restore the cell after exploring, or other start points see a damaged board.
@*/
class LC79 {
    public boolean exist(char[][] board, String word) {
        for (int i = 0; i < board.length; i++)
            for (int j = 0; j < board[0].length; j++)
                if (dfs(board, word, 0, i, j)) return true;
        return false;
    }

    private boolean dfs(char[][] b, String w, int k, int r, int c) {
        if (k == w.length()) return true;
        if (r < 0 || c < 0 || r >= b.length || c >= b[0].length || b[r][c] != w.charAt(k)) return false;
        char saved = b[r][c];
        b[r][c] = '#';                                                               //* mark
        boolean found = dfs(b, w, k + 1, r + 1, c) || dfs(b, w, k + 1, r - 1, c)
                     || dfs(b, w, k + 1, r, c + 1) || dfs(b, w, k + 1, r, c - 1);
        b[r][c] = saved;                                                             //* unmark
        return found;
    }
}

public class BonusC {
    public static void main(String[] args) {
        for (int variant = 0; variant < 2; variant++) {
            int[] out = new int[5];
            if (variant == 0) {
                LC146 c = new LC146(2);
                c.put(1, 1); c.put(2, 2); out[0] = c.get(1); c.put(3, 3); out[1] = c.get(2); c.put(4, 4); out[2] = c.get(1); out[3] = c.get(3); out[4] = c.get(4);
            } else {
                LC146v1 c = new LC146v1(2);
                c.put(1, 1); c.put(2, 2); out[0] = c.get(1); c.put(3, 3); out[1] = c.get(2); c.put(4, 4); out[2] = c.get(1); out[3] = c.get(3); out[4] = c.get(4);
            }
            T.eq("146 " + variant, out, T.ints("[1,-1,-1,3,4]"));
        }
        for (int t = 0; t < 50; t++) {
            int cap = T.rnd(1, 4);
            LC146 a = new LC146(cap);
            LC146v1 b = new LC146v1(cap);
            for (int op = 0; op < 30; op++) {
                int k = T.rnd(0, 5);
                if (T.rnd(0, 1) == 0) { int v = T.rnd(0, 9); a.put(k, v); b.put(k, v); }
                else T.eq("146 rand", a.get(k), b.get(k));
            }
        }

        LC208 trie = new LC208();
        trie.insert("apple");
        boolean[] tr = {trie.search("apple"), trie.search("app"), trie.startsWith("app"), false};
        trie.insert("app");
        tr[3] = trie.search("app");
        T.eq("208", tr, new boolean[]{true, false, true, true});
        LC208v1 wd = new LC208v1();
        wd.addWord("bad"); wd.addWord("dad"); wd.addWord("mad");
        T.eq("211", new boolean[]{wd.search("pad"), wd.search("bad"), wd.search(".ad"), wd.search("b..")}, new boolean[]{false, true, true, true});

        LC295 mf = new LC295();
        mf.addNum(1); mf.addNum(2);
        double m1 = mf.findMedian();
        mf.addNum(3);
        T.close("295a", m1, 1.5);
        T.close("295b", mf.findMedian(), 2.0);
        for (int t = 0; t < 50; t++) {
            LC295 f = new LC295();
            List<Integer> all = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                int x = T.rnd(-20, 20);
                f.addNum(x);
                all.add(x);
                Collections.sort(all);
                int n = all.size();
                double want = n % 2 == 1 ? all.get(n / 2) : (all.get(n / 2 - 1) + all.get(n / 2)) / 2.0;
                T.close("295 rand", f.findMedian(), want);
            }
        }

        T.eq("23a", T.arr(new LC23().mergeKLists(new ListNode[]{T.list(1, 4, 5), T.list(1, 3, 4), T.list(2, 6)})), T.ints("[1,1,2,3,4,4,5,6]"));
        T.eq("23b", T.arr(new LC23().mergeKLists(new ListNode[0])), new int[0]);
        T.eq("23c", T.arr(new LC23().mergeKLists(new ListNode[]{null})), new int[0]);
        T.eq("21a", T.arr(new LC23v1().mergeTwoLists(T.list(1, 2, 4), T.list(1, 3, 4))), T.ints("[1,1,2,3,4,4]"));
        T.eq("21b", T.arr(new LC23v1().mergeTwoLists(null, T.list(0))), T.ints("[0]"));

        T.eq("98a", new LC98().isValidBST(T.tree(2, 1, 3)), true);
        T.eq("98b", new LC98().isValidBST(T.tree(5, 1, 4, null, null, 3, 6)), false);
        T.eq("98c", new LC98().isValidBST(T.tree(5, 4, 6, null, null, 3, 7)), false);
        T.eq("98d", new LC98().isValidBST(T.tree(2147483647)), true);
        T.eq("98e", new LC98().isValidBST(T.tree(1, 1)), false);
        T.eq("230a", new LC98v1().kthSmallest(T.tree(3, 1, 4, null, 2), 1), 1);
        T.eq("230b", new LC98v1().kthSmallest(T.tree(5, 3, 6, 2, 4, null, null, 1), 3), 3);

        T.eq("1192a", T.canon(new LC1192().criticalConnections(4, List.of(List.of(0, 1), List.of(1, 2), List.of(2, 0), List.of(1, 3)))), "[[1, 3]]");
        T.eq("1192b", T.canon(new LC1192().criticalConnections(2, List.of(List.of(0, 1)))), "[[0, 1]]");
        for (int t = 0; t < 150; t++) {
            int n = T.rnd(2, 8);
            List<List<Integer>> edges = new ArrayList<>();
            for (int[] e : T.randTreeEdges(n)) edges.add(List.of(e[0], e[1]));
            Set<String> have = new HashSet<>();
            for (List<Integer> e : edges) have.add(Math.min(e.get(0), e.get(1)) + "," + Math.max(e.get(0), e.get(1)));
            for (int extra = T.rnd(0, 4); extra > 0; extra--) {
                int a = T.rnd(0, n - 1), b = T.rnd(0, n - 1);
                if (a == b || !have.add(Math.min(a, b) + "," + Math.max(a, b))) continue;
                edges.add(List.of(a, b));
            }
            List<List<Integer>> want = new ArrayList<>();
            for (int skip = 0; skip < edges.size(); skip++) {
                int[] p = new int[n];
                for (int i = 0; i < n; i++) p[i] = i;
                int comps = n;
                for (int i = 0; i < edges.size(); i++) {
                    if (i == skip) continue;
                    int a = edges.get(i).get(0), b = edges.get(i).get(1);
                    while (p[a] != a) a = p[a];
                    while (p[b] != b) b = p[b];
                    if (a != b) { p[a] = b; comps--; }
                }
                if (comps > 1) want.add(edges.get(skip));
            }
            T.eq("1192 rand", T.canon(new LC1192().criticalConnections(n, edges)), T.canon(want));
        }

        char[][] board = T.chars("ABCE", "SFCS", "ADEE");
        T.eq("79", new boolean[]{new LC79().exist(board, "ABCCED"), new LC79().exist(board, "SEE"), new LC79().exist(board, "ABCB")}, new boolean[]{true, true, false});
        T.summary("BonusC");
    }
}
