# DSA Patterns in Java: printable daily edition

Source for [`../dsa-patterns-printable.pdf`](../dsa-patterns-printable.pdf): an A4 book with one
"window" per DSA pattern, meant to be printed and read a window or two a day.

## What is in the PDF (387 pages)

- **Front:** cover, how to use the book, contents, a 60-day plan with spaced review
  (rewrite each template 1, 3, 7 and 14 days later), a *Which pattern?* table from problem
  clues to windows, input size → target complexity, a problem-solving checklist, Java traps
  and collection costs, and a Java kit (LeetCode's node classes, collection idioms, a full
  stdin program with a fast reader).
- **125 pattern windows in 13 parts:** arrays and hashing, two pointers and windows, binary
  search and sorting, stacks/queues/heaps, linked lists, intervals and greedy, backtracking,
  trees, graphs, dynamic programming, strings, math and bits, range queries.
  Each window has *Spot it*, *How it works*, a worked example, 1–3 Java templates (one tagged
  **memorise**), cost, variants, traps, 2–6 free LeetCode problems with tick boxes, and a
  "write it from memory" tracker.
- **Back:** costs of every template, and an index from LeetCode number to window.

The look matches `java-dsa-methods-printable.pdf` (PR #10): pastel macOS windows, menu bar
and dock, Atkinson Hyperlegible Next 10.5 pt body, JetBrains Mono 9 pt code, fonts embedded.

## How it was checked

- The printed code is cut from `java/*.java` between `//@tpl name` and `//@end`, so the book
  shows exactly the code that was tested. All 129 Java files compile with `javac --release 8`.
  Each pattern's test `main()` compares the templates with a brute force on random inputs and
  checks LeetCode samples: 126 test classes, 7.2 million checks, all passing.
- Every practice problem was looked up through LeetCode's API: 580 problems, all exist and
  none is Premium-only. Titles and difficulty come from the API (`data/leetcode.json`).
- Trace numbers and the numbers quoted in traps were produced by running code.
- `tools/check_layout.py` scans the PDF: no empty window frames, no text past the right edge,
  no unresolved page references.

## Rebuild

Needs Python 3 with `weasyprint pymupdf pyyaml pygments`, and a JDK with `javac`.

```bash
cd dsa-patterns-book
python -m venv .venv && .venv/bin/pip install weasyprint pymupdf pyyaml pygments
.venv/bin/python tools/check_java.py        # compile and run every test, about 1 minute
.venv/bin/python tools/check_leetcode.py    # look up new practice slugs (network)
.venv/bin/python build.py                   # -> ../dsa-patterns-printable.pdf, about 2 minutes
.venv/bin/python tools/check_layout.py ../dsa-patterns-printable.pdf
```

The first build downloads the fonts into `.fonts/` (ignored by git). To change a window, edit
its entry in `content/partNN.yaml` and its file in `java/`; `AUTHORING.md` has the format and
the writing rules. `content/manifest.yaml` fixes the order and numbering of parts and patterns,
and `content/front.yaml` holds the front pages.
