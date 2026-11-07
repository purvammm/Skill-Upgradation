---
title: Preface
---

This book is written for one reader and one date. Purvam Prajapati sits the Infosys Specialist Programmer (SP) and Digital Specialist Engineer (DSE) coding assessment on 6 December 2026, in Java, and has 29 days to prepare. The 29-day plan of the Skill-Upgradation repository fixes what to do on each of those days; its daily pattern handbook gives a short card for each of the 44 techniques the plan uses. This book is the textbook behind both. It takes one day of the plan per chapter and teaches that day's ideas properly: what each technique is for, why it is correct, what it costs, and the Java you will type.

## Two volumes, one course {.unnumbered}

The course comes in two volumes with the same 29 chapter numbers, and each chapter number is a day number.

- **Volume I, Theory** (this book) is read first. Chapter *N* teaches the ideas for Day *N* with definitions, theorems and proofs, worked examples with traces, Java listings, figures and the costs of every method. It ends with a summary, the key terms, review questions and the list of the day's problems.
- **Volume II, Practice Workbook** is worked in by hand. Its Chapter *N* has the day's time plan, a list of earlier patterns to write from memory, exercises with spaces to fill in, a worksheet for each problem and an error log. Hints and answers are at the back of the workbook.

A day therefore runs: the spaced review in the workbook, the theory chapter, the workbook exercises, the problems on LeetCode, Codeforces or OneCompiler, and the error log. Chapter 1 describes this routine in detail, with the time for each block.

## How the chapters are arranged {.unnumbered}

The 29 chapters fall into four parts, which follow the phases of the plan.

- **Part I, Foundations (Chapters 1–9)**: the Java toolkit, prefix sums, windows, sorting and greedy choice, heaps, binary search, monotonic stacks and bits, ending with the first mock and the habits of brute force and stress testing.
- **Part II, Number Theory, Dynamic Programming and First Graphs (Chapters 10–16)**: modular arithmetic and sieves, four chapters of dynamic programming, breadth-first and depth-first search, and the second mock with a chapter on analysing a problem before you code.
- **Part III, Graphs, Trees and Range Queries (Chapters 17–23)**: shortest paths and union-find, rooted trees without recursion, tree DP and rerooting, Fenwick and segment trees, lazy propagation and the *K*-parts DP, exponential techniques for small *n*, and the third mock.
- **Part IV, Infosys Problems and the Exam (Chapters 24–29)**: the most reported Infosys problems as full programs, the official sample paper as a mock with full walkthroughs afterwards, a repair day, the exam playbook and the last two days.

The mock chapters (9, 16, 23, 25 and 27) each teach a piece of exam technique and keep their problems' ideas behind a box marked "Read only after the mock". Sit the mock first.

Four appendices close the book. Appendix A turns the limits of a problem into a target complexity, a memory budget and a choice of integer type. Appendix B is a one-page Java 8 reference. Appendix C holds the answers to the review questions, and Appendix D lists the sources.

## How to read a chapter {.unnumbered}

Each learning chapter follows the same order: why the problem family matters, the definitions, the method and the reason it is correct, worked examples, the code and its cost. Read the proofs. They are short, they use only school mathematics, and they are what lets you change a template safely when a problem does not quite fit it. Then type every new listing once and run it.

Boxes mark material of one kind. **Key idea** boxes give the one sentence to remember. **Java** boxes explain a detail of the language. **Pitfall** boxes record a mistake that has cost marks, most of them from the handbook's list of traps. **Tip** boxes are exam technique, and **Infosys** boxes say why the material matters for this assessment, using only what the plan's sources report.

The review questions at the end of each chapter are conceptual; their answers are in Appendix C. The problems at the end are the day's practice, and the workbook has a worksheet for each.

## Conventions {.unnumbered}

Arrays are 0-indexed, *a*[0..*n* − 1], and *a*[*l*..*r*] is inclusive at both ends; a half-open range is written [*l*, *r*). The logarithm log means log₂. Variables are in italics in the text and code is in `monospace`.

All code is Java 8: no `var`, records, `List.of`, switch expressions or text blocks, because you cannot assume a newer compiler in the exam. Every Java block in both volumes was extracted from the book's sources and compiled with `javac --release 8`; only fill-in-the-blank exercises and deliberately wrong code are excluded. Templates taken from the handbook had been tested against brute force on random inputs, and every new listing was tested the same way. Every number in a trace, a worked example or an answer was printed by a program, not worked out by hand.

Problem statements from Infosys and other sites are paraphrased, and problems are cited with the labels the plan uses, such as LeetCode 502 or Codeforces 1526C2. Facts about the assessment come only from the sources the plan cites (Appendix D). Where those sources are silent, this book says so and tells you where to look.

## What this book does not do {.unnumbered}

It does not replace the problems. Reading a proof does not make the template fast under a clock; only the workbook's exercises and the day's problems do that. It does not promise a cut-off or a result, because Infosys publishes none. And it does not try to cover all of competitive programming: topics the plan does not use, such as flows, string hashing and geometry, are left to the references in Appendix D.
