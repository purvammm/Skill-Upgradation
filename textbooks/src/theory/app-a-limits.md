---
letter: A
title: From the Limits to the Complexity
short: Limits and complexity
---

Every chapter of this book turns the limits of a problem into a target before any code is written. Chapter 1 gave the short version of the table, Chapter 9 added the number of steps at each limit, and Chapter 16 measured what those steps cost in Java. This appendix collects the full version in one place: time (@tbl-A-time), the cases where one number is not enough (@sec-A-several), memory (@tbl-A-bytes and @tbl-A-memory), the depth of the call stack and the bounds of the integer types.

The planning figure throughout is the one the handbook uses: Java performs about 10⁸ to 3 · 10⁸ simple operations per second. A step at the limit is one pass of the innermost loop. Every step count below was computed, not estimated, and every time and byte count is a measurement already printed in Chapter 1 or Chapter 16, made with OpenJDK 25 on the machine used for this book. A judge can be two or three times slower, so treat a count near 3 · 10⁸ as a risk, not a pass.

## Time: the largest n {#sec-A-time}

Read the table from the left. Find the band of the largest *n* in the statement, take the complexity in the second column as the target, and look for a technique in the last two columns. Read it from the right during a mock: a brute force of a given complexity passes exactly the tests whose *n* lies in its band.

Table: {#tbl-A-time} From the largest n to a target complexity, a technique and its chapter
| Largest *n* | Target | Steps at the limit | Typical techniques | Chapters |
|---|---|---|---|---|
| ≤ 10 | O(*n*! · *n*) | 10 · 10! = 36,288,000 | every order, backtracking | 9, 22 |
| ≤ 11 or 12 | O(*n*! · *n*) | 11 · 11! = 439,084,800 | the same, only with heavy pruning | 22 |
| ≤ 15 | O(3ⁿ) | 3¹⁵ = 14,348,907 | every subset of every subset | 22 |
| ≤ 20 | O(2ⁿ · *n*) | 20 · 2²⁰ = 20,971,520 | every subset, bitmask DP | 9, 22 |
| ≤ 20, with a last element | O(2ⁿ · *n*²) | 2²⁰ · 20² = 419,430,400 | dp[*mask*][*last*]: too slow at 20, fine at 18 | 22 |
| ≤ 500 | O(*n*³) | 500³ = 125,000,000 | three nested loops, interval DP | 14 |
| ≤ 5,000 | O(*n*²) | 5000² = 25,000,000 | all pairs, two-dimensional DP | 11, 12, 21 |
| ≤ 10⁵ | O(*n* √*n*) | 10⁵ · √10⁵ ≈ 31,622,777 | square-root blocks (not taught here) | — |
| ≤ 2 · 10⁵ | O(*n* log² *n*) | about 62,019,888 | binary search over a log-time check | 6, 20 |
| ≤ 2 · 10⁵ | O(*n* log *n*) | about 3,521,928 | sorting, heaps, binary search, Fenwick and segment trees, Dijkstra, tree methods | 4–6, 13, 17–21 |
| ≤ 10⁶ | O(*n* log *n*) | about 19,931,569 | the same, with primitive arrays only | 4, 20 |
| ≤ 10⁷ | O(*n*) | 10,000,000 | one pass: prefix sums, two pointers, stacks, a sieve | 2, 3, 7, 10 |
| a value ≤ 10¹² | O(√*V*) | √10¹² = 1,000,000 | divisors and trial division | 10 |
| a value ≤ 10¹⁸ | O(log *V*) | log₂ 10¹⁸ ≈ 60 | binary search on the answer, fast power, digit DP | 6, 10, 22 |

Three rows deserve a comment. The 3ⁿ row comes from enumerating, for every mask, all of its submasks (Chapter 22); at *n* = 16 it is already 3¹⁶ = 43,046,721 steps. The dp[*mask*][*last*] row shows why a bitmask DP with an extra index of size *n* is comfortable at *n* = 18 but not at 20. And the two O(*n* log *n*) rows differ by a factor of about 5.7 between 2 · 10⁵ and 10⁶: at 10⁶ the constant factor decides, which is why Chapter 16 measured `Arrays.sort(Integer[])` at 0.71 s for 10⁶ values against 0.13 s for `int[]`.

:::tip
Write the target on paper before you code: the workbook's problem worksheets have a box for it. If your idea is slower than the target, it is the partial-marks fallback, not the solution.
:::

## Several numbers, test cases and values {#sec-A-several}

A statement with more than one limit needs a little arithmetic before the table applies.

Table: {#tbl-A-several} Limits that need more than one number
| Situation | Budget | Example at the limit |
|---|---|---|
| *n* elements and *q* queries | (*n* + *q*) · log *n* for structures; *n* · *q* for the brute force | *n* = *q* = 10⁵: about 3.3 · 10⁶ against 10¹⁰ |
| a grid of *R* × *C* cells | the table for *n* = *R* · *C* | 1000 × 1000 = 10⁶ cells: O(*RC*) with 4 neighbours, about 4 · 10⁶ checks |
| a graph with *n* vertices and *m* edges | O(*n* + *m*) for BFS and DFS, O((*n* + *m*) log *n*) for Dijkstra | *n* = 10⁵, *m* = 2 · 10⁵: (3 · 10⁵) · log₂ 10⁵ ≈ 5.0 · 10⁶ for Dijkstra |
| *t* test cases with Σ *n* bounded | apply the table to Σ *n*; each test may only do work proportional to its own *n* | *t* = 10⁴, Σ *n* = 2 · 10⁵: clearing a 2 · 10⁵ array per test is 2 · 10⁹ writes |
| two strings of lengths *n* and *m* | O(*nm*) for a two-string DP | *n* = *m* = 5000: 2.5 · 10⁷ cells |
| a value *V* rather than a length | O(log *V*) to search over it, O(√*V*) to factor it | *V* = 10⁹: 30 halvings, or 31,623 trial divisors |
| values up to 10⁶ | an array indexed by the value | counting, sieves and DP over values |
| values up to 10⁹ | sorting, compression or a hash map | an array of 10⁹ cells does not fit |

The test-case row is the one most often missed: the work must be proportional to Σ *n*, so global arrays are allocated once at the largest size and only their first *n* + 1 entries are cleared in each test case (Chapter 9).

## Memory {#sec-A-memory}

Memory is estimated the same way as time: cells times bytes per cell. Chapter 16 measured the cost of one item in the common structures.

Table: {#tbl-A-bytes} Bytes per item, measured on a 64-bit JVM with compressed references (from Chapter 16)
| Structure | Bytes per item |
|---|---|
| `boolean[]`, `byte[]` | 1 |
| `int[]` | 4 |
| `long[]`, `double[]` | 8 |
| `int[2][n]` (two long rows) | about 8 per pair |
| `Integer[]` filled with distinct objects | about 20 |
| `ArrayList<Integer>` | about 23 |
| `int[n][2]` (*n* short rows) | about 29 per row |
| `HashSet<Long>` | about 69 |
| `HashMap<Integer, Integer>` | about 77 |

With a typical limit of 256 MB, that is 256 · 10⁶ bytes, the JVM itself needs part of the budget. Plan to use at most about half for your own arrays.

Table: {#tbl-A-memory} What fits in memory, computed as cells × bytes (decimal megabytes)
| Allocation | Bytes | Verdict for 256 MB |
|---|---|---|
| 256 MB as `int`s | 64,000,000 cells | the upper bound, never reached in practice |
| 256 MB as `long`s | 32,000,000 cells | the upper bound |
| `int[5000][5000]` | 100,000,000 (100 MB) | fits, but uses most of the limit |
| `long[5000][5000]` | 200,000,000 (200 MB) | too much once the JVM is counted; use two rows |
| `long[20][1 << 20]` | 167,772,160 (about 168 MB) | too much; use `int` if the values fit |
| `int[20][1 << 20]` | 83,886,080 (about 84 MB) | fits |
| two `long[2 · 10⁵]` arrays | 3,200,000 (3.2 MB) | trivial |
| a lazy segment tree, two `long[4n]` arrays, *n* = 10⁵ | 6,400,000 (6.4 MB) | trivial |
| 10⁶ `HashMap` entries | about 77,000,000 (77 MB) | fits, but an `int[]` indexed by value is 4 MB |

Two habits keep memory low. If row *i* of a DP table reads only row *i* − 1, keep two rows (Chapter 12). And store pairs as two parallel arrays or as `int[2][n]`, never as `int[n][2]`, because every Java array is an object with a header of about 16 bytes.

## The call stack {#sec-A-stack}

Recursion depth is the third budget. On the machine used for this book the default stack held about 24,700 nested calls of a method with one `int` parameter and about 11,400 of a method with three parameters, and only about 9,800 and 7,400 when the same methods ran before the just-in-time compiler had optimised them (Chapter 16). The safe rule is the one used from Chapter 15 on: any recursion that can be deeper than about 10⁴ becomes a loop.

| Situation | Depth | Verdict |
|---|---|---|
| backtracking or bitmask search, *n* ≤ 20 | *n* + 1 | recursion is fine (Chapter 22) |
| LeetCode `TreeNode` problems, up to 10⁴ nodes | up to 10⁴ | accepted by LeetCode's tests; do not rely on it elsewhere (Chapter 18) |
| a parent array or edge list with 10⁵ nodes | up to 10⁵ on a path | `StackOverflowError`: use a BFS order or an explicit stack (Chapters 15 and 18) |
| top-down DP over 10⁶ states in a line | 10⁶ | `StackOverflowError`: fill the table bottom-up (Chapter 11) |

## Integer types and bounds {#sec-A-types}

Java integer arithmetic wraps around silently (Chapter 1). Choose the type of every quantity from the limits before you write the code.

Table: {#tbl-A-types} Bounds to compare against
| Quantity | Value | Type |
|---|---|---|
| `Integer.MAX_VALUE` | 2,147,483,647 ≈ 2.1 · 10⁹ | |
| `Long.MAX_VALUE` | 9,223,372,036,854,775,807 ≈ 9.2 · 10¹⁸ | |
| a sum of 10⁵ values up to 10⁹ | 10¹⁴ | `long` |
| a product of two values up to 10⁹ | 10¹⁸ | `long`, just; cast an operand first: `(long) a * b` |
| a product of two residues below 10⁹ + 7 | (10⁹ + 7)² ≈ 1.0 · 10¹⁸ | `long`; reduce after every multiplication |
| the number of pairs among 10⁵ items | 10⁵ · (10⁵ − 1)/2 = 4,999,950,000 | `long` |
| the number of pairs among 2 · 10⁵ items | 19,999,900,000 | `long` |
| a midpoint of two `int`s near 2 · 10⁹ | the sum overflows | `lo + (hi - lo) / 2` or `(lo + hi) >>> 1` |

When a product of three values can appear, reduce after each multiplication: `u * v % MOD * w % MOD`. When the answer is a remainder, use `Math.floorMod` for anything that can be negative.

## Using the appendix: one example {#sec-A-example}

A statement gives a rooted tree with *n* ≤ 2 · 10⁵ nodes as a parent array, a value up to 10⁹ on each node, and *q* ≤ 2 · 10⁵ queries, each asking for the sum of the values in a subtree after some values have changed.

1. **Time.** Two limits of 2 · 10⁵: the brute force walks a subtree per query, up to *n* · *q* = 4 · 10¹⁰ steps. The target is (*n* + *q*) · log *n* ≈ 4 · 10⁵ · 18 = 7.2 · 10⁶ (@tbl-A-several), which points to a Fenwick tree over an Euler tour (Chapter 20).
2. **Memory.** A few `int[n]` and `long[n]` arrays, a few megabytes in all (@tbl-A-memory).
3. **Stack.** A parent array of 2 · 10⁵ nodes can be a path, so the Euler tour must be iterative (@sec-A-stack).
4. **Types.** A subtree sum can reach 2 · 10⁵ · 10⁹ = 2 · 10¹⁴, so the Fenwick tree stores `long` (@tbl-A-types).

Four lines of arithmetic, before any code, decide the structure, the memory, the traversal and the type.
