import java.util.*;

/*@core Java toolkit: the lines you type in every problem
note: LeetCode imports `java.util.*` for you. In the Infosys editor, write `import java.util.*;` and `import java.io.*;` yourself.
@*/
class Core01 {
    static void toolkit(int[] nums, int lo, int hi) {
        // HashMap: count, read with a default, group into lists
        Map<Integer, Integer> cnt = new HashMap<>();
        for (int x : nums) cnt.merge(x, 1, Integer::sum);
        int seen = cnt.getOrDefault(42, 0);
        Map<Integer, List<Integer>> where = new HashMap<>();
        for (int i = 0; i < nums.length; i++) where.computeIfAbsent(nums[i], k -> new ArrayList<>()).add(i);

        // ArrayDeque: stack = push / pop / peek, queue = offer / poll / peek
        Deque<Integer> st = new ArrayDeque<>();
        st.push(1); st.peek(); st.pop();
        Deque<Integer> q = new ArrayDeque<>();
        q.offer(1); q.peek(); q.poll();

        // PriorityQueue: a min-heap unless you pass a comparator
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        PriorityQueue<int[]> byCost = new PriorityQueue<>((a, b) -> Integer.compare(a[1], b[1]));

        // TreeMap: sorted keys + neighbour lookups (each returns null when there is none)
        TreeMap<Integer, Integer> tm = new TreeMap<>(Map.of(1, 10, 7, 70));
        tm.floorKey(5);     // largest key <= 5   -> 1
        tm.ceilingKey(5);   // smallest key >= 5  -> 7
        tm.lowerKey(5);     // largest key < 5
        tm.higherKey(5);    // smallest key > 5
        tm.firstKey(); tm.lastKey(); tm.pollFirstEntry();

        // Sorting
        int[][] iv = {{3, 4}, {1, 2}};
        Arrays.sort(iv, (a, b) -> Integer.compare(a[0], b[0]));   // int[][] by first column
        Integer[] boxed = {3, 1, 2};
        Arrays.sort(boxed, Collections.reverseOrder());          // descending needs Integer[]
        List<Integer> list = new ArrayList<>(List.of(3, 1, 2));
        list.sort(Collections.reverseOrder());

        // Strings and overflow guards
        char[] cs = "listen".toCharArray();
        Arrays.sort(cs);
        String key = new String(cs);                             // "eilnst"
        StringBuilder sb = new StringBuilder("ab").reverse();    // "ba"
        long safe = (long) Integer.MAX_VALUE + 1;                // cast BEFORE adding
        int mid = lo + (hi - lo) / 2;                            // never (lo + hi) / 2
    }
}

/*@LC 1
tag: HashMap · look back
cue: Two indices whose values sum to target, array not sorted.
hook: Ask the map before you tell the map.
time: O(n) time · O(n) space
trap: Check `target - nums[i]` BEFORE storing `nums[i]`, or `[3]` with target 6 pairs 3 with itself.
var: Count pairs with sum k | Same loop: `count += seen.getOrDefault(k - x, 0);` then `seen.merge(x, 1, Integer::sum);`
var: 3Sum / 4Sum | Sort first and use two pointers instead (Day 3, LC 15).
@*/
class LC1 {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> indexOf = new HashMap<>();   // value -> index
        for (int i = 0; i < nums.length; i++) {
            Integer j = indexOf.get(target - nums[i]);      //* partner seen earlier?
            if (j != null) return new int[]{j, i};
            indexOf.put(nums[i], i);                        //* store AFTER the check
        }
        return new int[0];
    }
}

/*@var LC 167 · Two Sum II (input is sorted)
note: Sorted input → two pointers and O(1) space. The answer is 1-indexed.
@*/
class LC1v1 {
    public int[] twoSum(int[] numbers, int target) {
        int l = 0, r = numbers.length - 1;
        while (l < r) {
            int s = numbers[l] + numbers[r];
            if (s == target) return new int[]{l + 1, r + 1};
            if (s < target) l++; else r--;      // too small: move left up; too big: move right down
        }
        return new int[0];
    }
}

/*@LC 242
tag: Counting array
cue: Same letters, same counts? Only lowercase a–z.
hook: One string adds, the other subtracts, all zero at the end.
time: O(n) time · O(1) space (26 counters)
trap: Different lengths can never be anagrams: return early.
var: LC 383 Ransom Note | Add the magazine, subtract the note; any count below 0 → false.
var: Unicode letters | Use `Map<Character, Integer>` instead of `int[26]`.
@*/
class LC242 {
    public boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) return false;
        int[] cnt = new int[26];
        for (int i = 0; i < s.length(); i++) {
            cnt[s.charAt(i) - 'a']++;                       //*
            cnt[t.charAt(i) - 'a']--;                       //*
        }
        for (int c : cnt) if (c != 0) return false;
        return true;
    }
}

/*@var LC 438 · Find All Anagrams in a String
note: The same counts, slid along s as a fixed window of length |p|.
@*/
class LC242v1 {
    public List<Integer> findAnagrams(String s, String p) {
        List<Integer> res = new ArrayList<>();
        if (p.length() > s.length()) return res;
        int[] need = new int[26], win = new int[26];
        for (char c : p.toCharArray()) need[c - 'a']++;
        for (int i = 0; i < s.length(); i++) {
            win[s.charAt(i) - 'a']++;                                // add the new right char
            if (i >= p.length()) win[s.charAt(i - p.length()) - 'a']--;   // drop the char that left
            if (Arrays.equals(win, need)) res.add(i - p.length() + 1);
        }
        return res;
    }
}

/*@LC 20
tag: Stack
cue: Brackets must close in the reverse order they opened.
hook: Push the closer you expect; pop must match.
time: O(n) time · O(n) space
trap: A closer on an empty stack → false. Leftover openers at the end → false.
var: Only `(` and `)` | No stack needed: keep `open`; it must never drop below 0 and must end at 0.
@*/
class LC20 {
    public boolean isValid(String s) {
        Deque<Character> st = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if (c == '(') st.push(')');                     //* expect its closer
            else if (c == '[') st.push(']');
            else if (c == '{') st.push('}');
            else if (st.isEmpty() || st.pop() != c) return false;   //*
        }
        return st.isEmpty();                                //*
    }
}

/*@var LC 32 · Longest Valid Parentheses
note: Stack of indices with a "base" index: the position just before the current valid run.
@*/
class LC20v1 {
    public int longestValidParentheses(String s) {
        Deque<Integer> st = new ArrayDeque<>();
        st.push(-1);                                   // base before index 0
        int best = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') st.push(i);
            else {
                st.pop();
                if (st.isEmpty()) st.push(i);          // unmatched ')' becomes the new base
                else best = Math.max(best, i - st.peek());
            }
        }
        return best;
    }
}

/*@var LC 921 · Minimum Add to Make Parentheses Valid
note: One bracket type → two counters, no stack.
@*/
class LC20v2 {
    public int minAddToMakeValid(String s) {
        int open = 0, add = 0;
        for (char c : s.toCharArray()) {
            if (c == '(') open++;
            else if (open > 0) open--;
            else add++;                 // a ')' with nothing to match
        }
        return add + open;              // plus the '(' never closed
    }
}

/*@LC 347
tag: HashMap + min-heap of size k
cue: The k most frequent values.
hook: Count, then keep a small heap: push, and pop when it grows past k.
time: O(n log k) time · O(n) space
trap: The heap must be a MIN-heap by count so the weakest is the one thrown away.
var: LC 692 Top K Frequent Words | Comparator `(a, b) -> ca != cb ? ca - cb : b.compareTo(a)` keeps alphabetical order on ties.
@*/
class LC347 {
    public int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> cnt = new HashMap<>();
        for (int x : nums) cnt.merge(x, 1, Integer::sum);                                 //*
        PriorityQueue<int[]> heap = new PriorityQueue<>((a, b) -> Integer.compare(a[1], b[1])); //* min by count
        for (Map.Entry<Integer, Integer> e : cnt.entrySet()) {
            heap.offer(new int[]{e.getKey(), e.getValue()});
            if (heap.size() > k) heap.poll();                                              //*
        }
        int[] ans = new int[k];
        for (int i = k - 1; i >= 0; i--) ans[i] = heap.poll()[0];
        return ans;
    }
}

/*@var Bucket sort version · O(n)
note: A value can appear at most n times, so put values into buckets by count and read from the top.
@*/
class LC347v1 {
    public int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> cnt = new HashMap<>();
        for (int x : nums) cnt.merge(x, 1, Integer::sum);
        List<List<Integer>> bucket = new ArrayList<>();          // bucket.get(f) = values seen f times
        for (int i = 0; i <= nums.length; i++) bucket.add(new ArrayList<>());
        for (Map.Entry<Integer, Integer> e : cnt.entrySet()) bucket.get(e.getValue()).add(e.getKey());
        int[] ans = new int[k];
        int idx = 0;
        for (int f = nums.length; f >= 1 && idx < k; f--)
            for (int v : bucket.get(f)) if (idx < k) ans[idx++] = v;
        return ans;
    }
}

/*@var LC 215 · Kth Largest Element in an Array
note: The same "heap of size k" trick: a min-heap of the k largest; its top is the answer.
@*/
class LC347v2 {
    public int findKthLargest(int[] nums, int k) {
        PriorityQueue<Integer> heap = new PriorityQueue<>();
        for (int x : nums) {
            heap.offer(x);
            if (heap.size() > k) heap.poll();
        }
        return heap.peek();
    }
}

/*@LC 729
class: MyCalendar
tag: TreeMap neighbours
cue: Accept a booking only if it overlaps nothing booked so far.
hook: Look at the neighbour on the left and on the right; nobody else can touch you.
time: O(log n) per booking
trap: Intervals are half-open [start, end): end == next start is allowed, so compare with `>` and `<`, not `>=`.
@*/
class LC729 {
    private final TreeMap<Integer, Integer> cal = new TreeMap<>();   // start -> end

    public LC729() {}

    public boolean book(int startTime, int endTime) {
        Integer prev = cal.floorKey(startTime);                   //* starts at or before me
        Integer next = cal.ceilingKey(startTime);                 //* starts at or after me
        if (prev != null && cal.get(prev) > startTime) return false;   //*
        if (next != null && next < endTime) return false;              //*
        cal.put(startTime, endTime);
        return true;
    }
}

/*@var LC 731 · My Calendar II (no triple booking)
class: MyCalendarTwo
note: Sweep line on a TreeMap of +1 / −1. Add the booking, walk the running count, undo if it ever reaches 3. (My Calendar III is Day 21.)
@*/
class LC729v1 {
    private final TreeMap<Integer, Integer> diff = new TreeMap<>();

    public LC729v1() {}

    public boolean book(int startTime, int endTime) {
        diff.merge(startTime, 1, Integer::sum);
        diff.merge(endTime, -1, Integer::sum);
        int active = 0;
        for (int v : diff.values()) {
            active += v;
            if (active >= 3) {                          // triple booking: undo and refuse
                diff.merge(startTime, -1, Integer::sum);
                diff.merge(endTime, 1, Integer::sum);
                return false;
            }
        }
        return true;
    }
}

/*@LC 206
tag: Linked list · three pointers
cue: Reverse a singly linked list in place.
hook: Save, flip, step, step.
time: O(n) time · O(1) space
trap: Return `prev`, not `head`: at the end `cur` is null and `prev` is the new head.
@*/
class LC206 {
    public ListNode reverseList(ListNode head) {
        ListNode prev = null, cur = head;
        while (cur != null) {
            ListNode next = cur.next;     //* save
            cur.next = prev;              //* flip
            prev = cur;                   //* step prev
            cur = next;                   //* step cur
        }
        return prev;
    }
}

/*@var Recursive reverse
note: Same result, O(n) stack. Good to know; the loop is safer for long lists.
@*/
class LC206v1 {
    public ListNode reverseList(ListNode head) {
        if (head == null || head.next == null) return head;
        ListNode newHead = reverseList(head.next);
        head.next.next = head;            // the node after head points back to head
        head.next = null;
        return newHead;
    }
}

/*@var LC 92 · Reverse Linked List II (positions left..right)
note: Dummy node, walk to the node before `left`, then move each next node to the front of the section.
@*/
class LC206v2 {
    public ListNode reverseBetween(ListNode head, int left, int right) {
        ListNode dummy = new ListNode(0, head), before = dummy;
        for (int i = 1; i < left; i++) before = before.next;
        ListNode cur = before.next;
        for (int i = 0; i < right - left; i++) {
            ListNode move = cur.next;     // take the node after cur ...
            cur.next = move.next;
            move.next = before.next;      // ... and put it right after before
            before.next = move;
        }
        return dummy.next;
    }
}

/*@LC 141
tag: Fast and slow pointers
cue: Does the list loop back on itself? O(1) memory.
hook: The hare laps the tortoise only on a track.
time: O(n) time · O(1) space
trap: Loop condition is `fast != null && fast.next != null`, in that order.
var: LC 876 Middle of the Linked List | Same loop without the meet check; return `slow` (the second middle for even length).
@*/
class LC141 {
    public boolean hasCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {   //*
            slow = slow.next;
            fast = fast.next.next;                    //*
            if (slow == fast) return true;            //*
        }
        return false;
    }
}

/*@var LC 142 · Linked List Cycle II (where does the cycle start?)
note: After they meet, restart one pointer at head; move both one step; they meet at the cycle start.
@*/
class LC141v1 {
    public ListNode detectCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                ListNode p = head;
                while (p != slow) { p = p.next; slow = slow.next; }
                return p;
            }
        }
        return null;
    }
}

/*@LC 49
tag: HashMap of lists · canonical key
cue: Group words that are anagrams of each other.
hook: Same sorted letters, same bucket.
time: O(n · L log L) time · O(n · L) space
trap: `new String(chars)` for the key; `chars.toString()` gives an address, not the letters.
var: Count key, O(L) per word | Key = `Arrays.toString(int[26] counts)` instead of sorting.
@*/
class LC49 {
    public List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> groups = new HashMap<>();
        for (String s : strs) {
            char[] c = s.toCharArray();
            Arrays.sort(c);                                                       //*
            groups.computeIfAbsent(new String(c), k -> new ArrayList<>()).add(s); //*
        }
        return new ArrayList<>(groups.values());
    }
}

public class Day01 {
    public static void main(String[] args) {
        Core01.toolkit(new int[]{1, 2, 2}, 0, 10);

        T.eq("1a", new LC1().twoSum(T.ints("[2,7,11,15]"), 9), T.ints("[0,1]"));
        T.eq("1b", new LC1().twoSum(T.ints("[3,2,4]"), 6), T.ints("[1,2]"));
        T.eq("1c", new LC1().twoSum(T.ints("[3,3]"), 6), T.ints("[0,1]"));
        T.eq("167a", new LC1v1().twoSum(T.ints("[2,7,11,15]"), 9), T.ints("[1,2]"));
        T.eq("167b", new LC1v1().twoSum(T.ints("[2,3,4]"), 6), T.ints("[1,3]"));
        T.eq("167c", new LC1v1().twoSum(T.ints("[-1,0]"), -1), T.ints("[1,2]"));

        T.eq("242a", new LC242().isAnagram("anagram", "nagaram"), true);
        T.eq("242b", new LC242().isAnagram("rat", "car"), false);
        T.eq("242c", new LC242().isAnagram("a", "ab"), false);
        T.eq("438a", new LC242v1().findAnagrams("cbaebabacd", "abc"), List.of(0, 6));
        T.eq("438b", new LC242v1().findAnagrams("abab", "ab"), List.of(0, 1, 2));

        String[] ps = {"()", "()[]{}", "(]", "([])", "([)]", "]", "(", "{[]}"};
        boolean[] pw = {true, true, false, true, false, false, false, true};
        for (int i = 0; i < ps.length; i++) T.eq("20 " + ps[i], new LC20().isValid(ps[i]), pw[i]);
        T.eq("32a", new LC20v1().longestValidParentheses("(()"), 2);
        T.eq("32b", new LC20v1().longestValidParentheses(")()())"), 4);
        T.eq("32c", new LC20v1().longestValidParentheses(""), 0);
        T.eq("32d", new LC20v1().longestValidParentheses("()(())"), 6);
        T.eq("921a", new LC20v2().minAddToMakeValid("())"), 1);
        T.eq("921b", new LC20v2().minAddToMakeValid("((("), 3);
        T.eq("921c", new LC20v2().minAddToMakeValid(")("), 2);

        T.eq("347a", T.sorted(new LC347().topKFrequent(T.ints("[1,1,1,2,2,3]"), 2)), "[1, 2]");
        T.eq("347b", new LC347().topKFrequent(T.ints("[1]"), 1), T.ints("[1]"));
        T.eq("347c", T.sorted(new LC347().topKFrequent(T.ints("[1,2,1,2,1,2,3,1,3,2]"), 2)), "[1, 2]");
        T.eq("347v1a", T.sorted(new LC347v1().topKFrequent(T.ints("[1,1,1,2,2,3]"), 2)), "[1, 2]");
        T.eq("347v1b", new LC347v1().topKFrequent(T.ints("[1]"), 1), T.ints("[1]"));
        T.eq("215a", new LC347v2().findKthLargest(T.ints("[3,2,1,5,6,4]"), 2), 5);
        T.eq("215b", new LC347v2().findKthLargest(T.ints("[3,2,3,1,2,4,5,5,6]"), 4), 4);

        LC729 cal = new LC729();
        T.eq("729", new boolean[]{cal.book(10, 20), cal.book(15, 25), cal.book(20, 30), cal.book(5, 10), cal.book(5, 11)},
             new boolean[]{true, false, true, true, false});
        LC729v1 c2 = new LC729v1();
        T.eq("731", new boolean[]{c2.book(10, 20), c2.book(50, 60), c2.book(10, 40), c2.book(5, 15), c2.book(5, 10), c2.book(25, 55)},
             new boolean[]{true, true, true, false, true, true});

        T.eq("206a", T.arr(new LC206().reverseList(T.list(1, 2, 3, 4, 5))), T.ints("[5,4,3,2,1]"));
        T.eq("206b", T.arr(new LC206().reverseList(T.list(1, 2))), T.ints("[2,1]"));
        T.eq("206c", T.arr(new LC206().reverseList(null)), new int[0]);
        T.eq("206r", T.arr(new LC206v1().reverseList(T.list(1, 2, 3, 4, 5))), T.ints("[5,4,3,2,1]"));
        T.eq("92a", T.arr(new LC206v2().reverseBetween(T.list(1, 2, 3, 4, 5), 2, 4)), T.ints("[1,4,3,2,5]"));
        T.eq("92b", T.arr(new LC206v2().reverseBetween(T.list(5), 1, 1)), T.ints("[5]"));
        T.eq("92c", T.arr(new LC206v2().reverseBetween(T.list(3, 5), 1, 2)), T.ints("[5,3]"));

        T.eq("141a", new LC141().hasCycle(T.cycle(T.ints("[3,2,0,-4]"), 1)), true);
        T.eq("141b", new LC141().hasCycle(T.cycle(T.ints("[1,2]"), 0)), true);
        T.eq("141c", new LC141().hasCycle(T.cycle(T.ints("[1]"), -1)), false);
        ListNode cyc = T.cycle(T.ints("[3,2,0,-4]"), 1);
        T.ok("142a", new LC141v1().detectCycle(cyc) == cyc.next);
        T.ok("142b", new LC141v1().detectCycle(T.list(1)) == null);

        List<List<String>> g = new LC49().groupAnagrams(new String[]{"eat", "tea", "tan", "ate", "nat", "bat"});
        T.eq("49a", T.canon(g), T.canon(List.of(List.of("bat"), List.of("nat", "tan"), List.of("ate", "eat", "tea"))));
        T.eq("49b", T.canon(new LC49().groupAnagrams(new String[]{""})), "[[]]");
        T.summary("Day01");
    }
}
