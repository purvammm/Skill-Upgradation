---
letter: D
title: Sources and References
short: Sources
---

The two volumes expand the 29-day plan (`infosys-dsa-29-day-plan.html`) and the daily pattern handbook (`infosys-dsa-daily-handbook.html`) of the Skill-Upgradation repository. Every fact about the Infosys assessment in this book comes from the sources the plan cites, listed in @sec-D-infosys. Problem statements from Infosys and from other sites are paraphrased throughout, and content from every source was rephrased for compliance with licensing restrictions.

## About the Infosys assessment {#sec-D-infosys}

These are the plan's sources, with what each was used for.

- **Infosys, Sample Test SP/DSE** (PDF), rec-test.infosys.com/iwa-web/public/instructions/Sample_Test_SP_DSE.pdf. The official sample paper: the problem format, the input style and the four problems that Chapter 25 solves.
- **PrepInsta**, pages on Infosys SP and DSE coding questions (prepinsta.com/infosys-sp-and-dse/coding-questions/ and the SP and DSE sub-pages). The pattern of the test, the marks and reported problems.
- **placementpreparation.io**, HackWithInfy syllabus and test pattern. Scoring by test cases, cut-offs and the languages offered.
- **takeUforward**, an SP/DSE off-campus interview experience from the 2026 batch. An offline proctored round with about four problems, and the interview task.
- **LeetCode Discuss**, a Specialist Programmer L3 experience from January 2026. Three full solutions and a partial fourth led to an L3 interview.
- **GeeksforGeeks**, an OA experience for Specialist Programmer. The in-person invigilation rules.
- **milinkanu/infosys-qb** (GitHub). The community question bank whose report counts, 2019 to 2026, give the frequencies of the repeat problems in Chapters 21, 24 and 26.
- **thor149/Infosys-Prep** (GitHub), a research report. The summary of the process and the mapping of roles.
- **Great Learning**, Infosys SP and DSE guide 2026. Reported pay (used by the plan, not by this book).

The plan's author checked OneCompiler (onecompiler.com/java) and the Codeforces *Submit* and *Custom test* pages on 7 November 2026, every LeetCode problem through LeetCode's API (all free, none Premium), the Codeforces names and ratings through the Codeforces API, and the GeeksforGeeks pages directly.

## Algorithms and data structures {#sec-D-algorithms}

- Antti Laaksonen, *Competitive Programmer's Handbook*, free at cses.fi/book. Cited by chapter in the "Further reading" of most chapters, and used to check the presentation of standard algorithms.
- Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest and Clifford Stein, *Introduction to Algorithms*, 4th edition, MIT Press, 2022. Cited by chapter title. The convention of exercises inside a chapter and problems at its end follows this book.
- *Algorithms for Competitive Programming*, cp-algorithms.com. The pages cited in the chapters, each checked to exist on 7 November 2026:

Table: {#tbl-D-cpalgo} Pages of cp-algorithms.com cited in this book
| Page (cp-algorithms.com/…) | Topic |
|---|---|
| algebra/binary-exp.html | binary exponentiation |
| algebra/module-inverse.html | modular inverse |
| algebra/sieve-of-eratosthenes.html, algebra/prime-sieve-linear.html | sieves |
| algebra/divisors.html | number and sum of divisors |
| algebra/bit-manipulation.html, algebra/all-submasks.html | bit tricks, enumerating submasks |
| combinatorics/binomial-coefficients.html | binomial coefficients |
| linear_algebra/linear-system-gauss.html | Gaussian elimination, also over GF(2) |
| others/maximum_average_segment.html | best subsegment by prefix sums |
| data_structures/stack_queue_modification.html | minimum stack and minimum queue |
| num_methods/binary_search.html | binary search |
| dynamic_programming/intro-to-dp.html, dynamic_programming/knapsack.html | DP and knapsacks |
| dynamic_programming/divide-and-conquer-dp.html | divide-and-conquer DP |
| sequences/longest_increasing_subsequence.html | longest increasing subsequence |
| game_theory/games_on_graphs.html | games on graphs |
| graph/breadth-first-search.html, graph/depth-first-search.html, graph/01_bfs.html | traversals |
| graph/bipartite-check.html, graph/topological-sort.html | bipartite check, topological order |
| graph/dijkstra.html | Dijkstra's algorithm |
| data_structures/disjoint_set_union.html, graph/mst_kruskal_with_dsu.html | union-find, Kruskal |
| graph/lca_binary_lifting.html | binary lifting |
| data_structures/fenwick.html, data_structures/segment_tree.html | Fenwick and segment trees |

- Oracle, *Java Platform, Standard Edition 8 API Specification*, docs.oracle.com/javase/8/docs/api/. Cited by class name (`java.util.PriorityQueue`, `java.util.TreeMap`, `java.io.BufferedReader` and others).

## How the books were made {#sec-D-making}

- **Pedagogy.** The chapter openers (objectives, an introduction) and closers (summary, key terms, review questions) follow common textbook practice. The workbook moves from complete worked examples to faded examples with gaps and then to independent work, and brings every pattern back for review 1, 3, 7 and 14 days after it is taught, rated with the handbook's Again, Shaky and Solid scale.
- **Code.** Every Java block in both volumes was extracted from the Markdown sources and compiled with `javac --release 8`; blocks marked as fill-in-the-blank or deliberately wrong are the only exceptions. Templates taken from the handbook had been tested against brute force on random inputs; new listings were tested the same way, and every number in a trace, a worked example or an answer key was printed by a program.
- **Typesetting.** Markdown converted to HTML and set with WeasyPrint (weasyprint.org), with figures drawn by the books' own small SVG renderer. Text in Source Serif 4, headings in Source Sans 3, code in JetBrains Mono, with Noto Sans Math and Noto Sans Symbols 2 as fallbacks; all are licensed under the SIL Open Font License and downloaded at build time.
