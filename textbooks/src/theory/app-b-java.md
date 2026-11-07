---
letter: B
title: Java 8 Reference for the Exam
short: Java reference
---

This appendix is a lookup sheet for the Java you type in the assessment. It repeats nothing that needs an explanation: the reasons are in Chapter 1 (collections, overflow, input), Chapter 16 (costs and memory) and Chapter 27 (the exam file). Every snippet below compiles with `javac --release 8`.

## The exam file {#sec-B-file}

Type the exam file of Chapter 27 (@lst-27-skeleton) from memory at the start of every problem that has no function stub: FastScanner, one `PrintWriter`, a `solve` and a `brute` with the same parameters, and a `main` that calls one of them. The five lines that must be right:

```java {.nocompile}
public static void main(String[] args) throws IOException {          // readLine can throw
PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
while (st == null || !st.hasMoreTokens()) { ... }                      // while, not if
if (line == null) return null;                                         // end of input
out.flush();                                                           // or nothing is printed
```

With a function stub such as `get_ans(int n, int[] a)`, keep its name, parameters and return type exactly, and fill in the body.

## Collections by question {#sec-B-collections}

Choose the structure by the question you need it to answer. The costs are those of Chapter 1's table of costs.

Table: {#tbl-B-collections} Which class answers which question
| Question | Class | Calls | Cost per call |
|---|---|---|---|
| how many of each? have I seen *x*? | `HashMap<K, V>`, `HashSet<K>` | `getOrDefault`, `merge(k, 1, Integer::sum)`, `put`, `add`, `contains` | O(1) expected |
| keys in a small range 0..10⁷ | `int[]` indexed by the key | `cnt[x]++` | O(1), no boxing |
| last in, first out; first in, first out | `ArrayDeque<E>` | `push`, `pop`, `peek`; `offer`, `poll`, `peekFirst`, `pollLast` | O(1) amortised |
| the smallest (or largest) so far | `PriorityQueue<E>` | `add`, `poll`, `peek`, `size` | O(log *n*); `peek` O(1) |
| the nearest key below or above *x*; keys in order | `TreeMap<K, V>`, `TreeSet<K>` | `floorKey`, `ceilingKey`, `lowerKey`, `higherKey`, `firstKey`, `lastKey`, `pollFirstEntry` | O(log *n*) |
| a list that grows at the end | `ArrayList<E>` | `add`, `get`, `set`, `size` | O(1), `add` amortised |
| adjacency lists for *n* vertices | `List<Integer>[]` or head/next/to arrays | `g[u].add(v)` | O(1) amortised |

Three calls are O(*n*) and are easy to use by accident: `PriorityQueue.remove(x)`, `ArrayList.remove(i)` near the front, and `ArrayList.contains(x)`.

## Idioms that compile in Java 8 {#sec-B-idioms}

```java {.fragment}
// Sorting
int[] a = {5, 3, 9, 1};
Arrays.sort(a);                                                // primitives, ascending
Integer[] boxed = {5, 3, 9, 1};
Arrays.sort(boxed, Collections.reverseOrder());                // objects, descending
int[][] pairs = {{3, 1}, {1, 2}, {3, 0}};
Arrays.sort(pairs, (x, y) -> x[0] != y[0] ? Integer.compare(x[0], y[0])
                                          : Integer.compare(x[1], y[1]));
List<Long> list = new ArrayList<>(Arrays.asList(4L, 2L, 7L));
Collections.sort(list);

// Heaps and deques
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
PriorityQueue<long[]> byFirst = new PriorityQueue<>((x, y) -> Long.compare(x[0], y[0]));
ArrayDeque<Integer> stack = new ArrayDeque<>();
stack.push(1);
int top = stack.pop();

// Maps
Map<Long, Integer> cnt = new HashMap<>();
cnt.merge(42L, 1, Integer::sum);
int c = cnt.getOrDefault(7L, 0);
TreeMap<Integer, Integer> tm = new TreeMap<>();
tm.put(10, 1);
Integer below = tm.floorKey(12);                               // null if there is none

// Arrays
long[] d = new long[5];
Arrays.fill(d, Long.MAX_VALUE / 4);                            // "infinity" that survives + 1
int[] copy = Arrays.copyOf(a, a.length);
int pos = Arrays.binarySearch(a, 9);                           // any matching index, or negative

// Strings and characters
StringBuilder sb = new StringBuilder();
sb.append("ab").append(3).reverse();
String s = sb.toString();
int[] letters = new int[26];
for (char ch : "hello".toCharArray()) letters[ch - 'a']++;
String[] words = "a b  c".trim().split("\\s+");

// Arithmetic
long prod = (long) a[0] * a[1];                                // cast an operand, not the result
int r = Math.floorMod(-7, 5);                                  // 3, while -7 % 5 is -2
int mid = (a[0] + a[3]) >>> 1;                                 // overflow-free midpoint
long big = 1L << 40;                                           // 1 << 40 would wrap

// Bits
int ones = Integer.bitCount(13);                               // 3
int low = 12 & -12;                                            // lowest set bit: 4
int high = 31 - Integer.numberOfLeadingZeros(40);              // index of the top bit: 5
boolean set = ((13 >> 2) & 1) == 1;                            // is bit 2 set?
```

`Arrays.binarySearch` returns *some* index of a matching key when there are duplicates, not the first one; write your own lower bound (Chapter 6) when the first index matters.

## What Java 8 does not have {#sec-B-java8}

You cannot assume a newer compiler, so the book writes Java 8 throughout.

Table: {#tbl-B-java8} Newer syntax and its Java 8 replacement
| Newer Java | Java 8 |
|---|---|
| `var x = 5;` | `int x = 5;` |
| `List.of(1, 2, 3)` | `Arrays.asList(1, 2, 3)` (fixed size) or `new ArrayList<>(Arrays.asList(...))` |
| `Map.of("a", 1)` | `Map<String, Integer> m = new HashMap<>(); m.put("a", 1);` |
| `record Pair(int a, int b)` | `static final class Pair { final int a, b; Pair(int a, int b) { ... } }`, or a `long` or `int[]` |
| `switch` expressions with `->` | an `if`–`else` chain or a classic `switch` with `break` |
| text blocks `"""` | string concatenation |
| `String.repeat(k)` | a loop with `StringBuilder` |

## Traps, in one line each {#sec-B-traps}

- `int` sums and products overflow silently: use `long`, and cast before multiplying.
- `%` keeps the sign of the left operand: use `Math.floorMod`.
- A comparator that subtracts can overflow: use `Integer.compare` and `Long.compare`.
- `==` on `Integer` or `Long` objects compares references: it fails outside −128..127.
- `list.remove(3)` removes index 3; `list.remove(Integer.valueOf(3))` removes the value 3.
- `s += c` in a loop is quadratic: use `StringBuilder`.
- `System.out.println` in a loop of 10⁶ lines takes about 2 s: use one `PrintWriter` and `flush()` once.
- Recursion deeper than about 10⁴ overflows the stack: use a loop or an explicit stack.
- `1 << k` with *k* ≥ 32 wraps: use `1L << k`.
- `Stack` and `Vector` are synchronised and slower: use `ArrayDeque`.
