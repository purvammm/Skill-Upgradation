# Infosys DSA textbooks: build notes, reasoning and status

This directory turns the two single-page study guides from this repo
(`infosys-dsa-29-day-plan.html` and `infosys-dsa-daily-handbook.html`, from the open
PRs on the `Claude-version` branch) into two printable **textbook-style PDFs**:

- **`Infosys-DSA-Theory-Textbook.pdf`**, Volume I, the textbook (513 pages). One chapter per day
  of the 29-day plan, so **29 chapters**, each teaching the day's ideas properly:
  definitions, theorems with proofs, worked examples with traces, Java listings,
  complexity, figures, exam tips, a summary, review questions with answers, and the
  day's problem list. Four appendices: limits and complexity, a Java 8 reference,
  answers to the review questions, and sources.
- **`Infosys-DSA-Practice-Workbook.pdf`**, Volume II, the workbook (316 pages), with the **same 29
  chapters**. Each has the day's time plan, a spaced-repetition review list, exercises
  you complete by hand (fill-the-gap templates, traces, "find the bug", design tasks),
  a worksheet per problem (or a mock sheet on mock days), and an end-of-day error log.
  Hints, the answer key, error-log pages, a mock-score page and scratch paper are at the back.

> **Status: complete.** All 29 theory chapters and all 29 workbook chapters are written, with the
> preface, the workbook's "How to use" pages and all appendices. Both PDFs build with no warnings.
> See [Status](#status) and [What is verified](#what-is-verified).

---

## What was asked, and how I read it

The request: read the latest comment in the repo, then produce two textbook-style PDFs
(a theory PDF and a practical PDF) with **29 chapters**, matching the structure of the
existing HTML guides, referencing how real textbooks are laid out, and in the end push
the PDFs. Two follow-up steers: *(1)* add more theory/detail for each day to refer to,
done however is best; *(2)* a single combined textbook would also be acceptable, my call.

Decisions made from that:

1. **Two volumes, not one.** The plan page and the handbook already split "what to do each
   day" from "how each technique works". A theory volume (read first) and a practice
   workbook (worked in by hand, with writing space) map onto that split cleanly, and a
   workbook only makes sense as a separate, consumable book. They share **chapter numbers**,
   so Theory Ch 7 and Workbook Ch 7 are the same day.
2. **29 chapters = the 29 days.** Mock days (9, 16, 23, 25, 27), the repair day (26),
   consolidation (28) and the rest day (29) are chapters about exam technique,
   debugging, pattern recognition and logistics, so the count stays at 29 with no filler.
3. **Much more theory than the HTML had.** Each learning chapter proves *why* an algorithm is
   correct and *what* it costs, not just the template: the level of CLRS / CP-Handbook /
   cp-algorithms, aimed at one reader.
4. **Real-textbook pedagogy.** Openers (objectives, introduction), body, closers (summary, key
   terms, review questions), CLRS-style exercises and problems. The workbook uses faded worked
   examples (full example, then gaps, then solve alone) and spaced retrieval (reviews 1, 3, 7 and
   14 days after a pattern is taught).

---

## Why this toolchain

PDFs are generated from **Markdown → HTML → PDF with [WeasyPrint](https://weasyprint.org/)**
(CSS Paged Media). The sandbox has no LaTeX or headless browser, but WeasyPrint installs via pip
and does real book typesetting: running headers, footnotes, a table of contents and an index
with live page numbers, `@page` margins and controlled page breaks. Markdown keeps the 58 chapter
files easy to write and review, and let the writing be split across parallel writers against one
spec. Everything is reproducible: open-source fonts, a from-scratch figure renderer, and the
plan/handbook data extracted to JSON.

---

## How it is built

Everything lives under `src/`. Build with any venv that has the dependencies:

```bash
pip install weasyprint markdown pygments pyyaml pypdfium2      # + beautifulsoup4 for extract_sources.py
cd textbooks/src
PY=python                                                      # the venv's python

$PY build.py                                   # both books -> textbooks/*.pdf (about 3 minutes)
$PY build.py theory                            # one book
$PY build.py theory --only 2,3 --out /tmp/preview.pdf     # just some chapters
$PY tools/preview.py <pdf> 1-20 --out /tmp/prev           # render pages to PNG to eyeball
$PY tools/preview.py <pdf> --info                         # page count and outline
$PY tools/check_java.py                                   # javac --release 8 on every listing
```

On first run, `tools/fonts.py` downloads the OFL fonts (Source Serif 4, Source Sans 3,
JetBrains Mono, two Noto fallbacks) into `src/.fonts/`, which is git-ignored.

### Layout

```
textbooks/
  Infosys-DSA-Theory-Textbook.pdf        generated (committed)
  Infosys-DSA-Practice-Workbook.pdf      generated (committed)
  PROGRESS.md                            this file
  src/
    build.py          assembles each book: title page, preface, TOC, parts, chapters,
                      generated openers/problem tables/workbook blocks, appendices, index
    AUTHORING.md      the full spec every chapter follows (read this first to change a chapter)
    tools/
      bookmd.py       the Markdown dialect (boxes, figures, cross-refs, numbered listings, index terms)
      figures.py      a small SVG renderer: array / tree / graph / bars / intervals / grid
      fonts.py        downloads fonts, emits @font-face
      check_java.py   compiles every ```java block with javac --release 8
      preview.py      PDF pages -> PNG for visual checks
      extract_sources.py  re-extracts the writing sources (days, cards, plan sections) from the HTML
    style/            base.css, code.css, theory.css, workbook.css (CSS Paged Media)
    data/             plan.json, mocks.json, patterns.json (extracted from the two HTML pages)
    theory/           ch01..ch29.md, front-preface.md, app-a-limits.md, app-b-java.md,
                      app-c-answers.md (generated from the chapters), app-d-sources.md
    workbook/         ch01..ch29.md, front-howto.md
```

### The Markdown dialect (see `src/AUTHORING.md` for the full spec)

On top of normal Markdown, a chapter can use fenced code with a title/id (numbered **Listing N.k**),
` ```fig ` blocks with a JSON body (numbered **Figure N.k**, drawn by `figures.py`), containers
`:::definition / theorem / lemma / proof / example / exercise / keyidea / pitfall / infosys / java /
answers / write / grid ... :::`, `[[term]]` index terms, `^[footnotes]`, and cross-references
`@lst-…`, `@fig-…`, `@thm-…` (and `@T:lst-…` from the workbook into the theory book). Section
numbers, chapter openers, key-terms lists, the per-day problem table and all workbook blocks
(`{{plan}}`, `{{recall}}`, `{{mock}}`, `{{problems}}`, `{{resolve}}`, `{{end}}`) are generated by
`build.py` from the JSON. Labels are global within a book, so every id carries its chapter number.

---

## Status

| Ch | Day topic | Theory | Workbook |
|----|-----------|:------:|:--------:|
| 1 | Set up, Java toolkit, fast input | ✅ | ✅ |
| 2 | Prefix sums, difference arrays, hashing *(reference chapter)* | ✅ | ✅ |
| 3 | Two pointers and sliding window | ✅ | ✅ |
| 4 | Sorting and greedy: intervals | ✅ | ✅ |
| 5 | Greedy with heaps | ✅ | ✅ |
| 6 | Binary search, including on the answer | ✅ | ✅ |
| 7 | Monotonic stacks | ✅ | ✅ |
| 8 | Bits and XOR | ✅ | ✅ |
| 9 | Mock 1 + review | ✅ | ✅ |
| 10 | Number theory toolkit | ✅ | ✅ |
| 11 | DP I: states, transitions, knapsack | ✅ | ✅ |
| 12 | DP II: two-string DP | ✅ | ✅ |
| 13 | DP III: LIS and extra dimensions | ✅ | ✅ |
| 14 | DP IV: divisor, interval and game DP | ✅ | ✅ |
| 15 | Graphs I: BFS, DFS, topological order | ✅ | ✅ |
| 16 | Mock 2 + review (problem analysis) | ✅ | ✅ |
| 17 | Graphs II: Dijkstra, DSU, MST | ✅ | ✅ |
| 18 | Trees I: recursion, parent arrays, LCA | ✅ | ✅ |
| 19 | Trees II: tree DP, rerooting, small-to-large | ✅ | ✅ |
| 20 | Fenwick trees and segment trees | ✅ | ✅ |
| 21 | Lazy segment trees and the K-parts DP | ✅ | ✅ |
| 22 | Backtracking, bitmask DP, digit DP | ✅ | ✅ |
| 23 | Mock 3 (4 problems) + review | ✅ | ✅ |
| 24 | Infosys repeat problems I | ✅ | ✅ |
| 25 | Mock 4: the official Infosys sample paper | ✅ | ✅ |
| 26 | Repair day + the hard XOR repeat | ✅ | ✅ |
| 27 | Mock 5: dress rehearsal (the exam playbook) | ✅ | ✅ |
| 28 | Consolidate (the pattern-recognition map) | ✅ | ✅ |
| 29 | Rest and logistics | ✅ | ✅ |

Front matter and back matter: Theory preface ✅, Theory Appendices A (limits → complexity, memory,
stack, types), B (Java 8 reference), C (answers, generated), D (sources) ✅; Workbook "How to use"
pages, progress and recall trackers, Appendices A–E ✅.

Size: about 253,000 words of theory source and 74,000 of workbook source (both counts include code
and figure data), 205 numbered listings, 252 theorems/lemmas/claims with proofs, 123 figures and
232 workbook exercises with full answers.

### What is verified

- **Java compiles.** All 289 `java` blocks pass `javac --release 8` (266 compiled; 23 are deliberate
  fill-in-the-blank templates or broken code marked `.nocompile`). Nothing fails.
- **Both books build with no WeasyPrint warnings**, no unknown cross-references, no leftover
  placeholders and no stray HTML entities (checked by extracting the PDF text and scanning it).
- **Problem citations.** Every LeetCode and Codeforces number in either book appears in
  `data/plan.json`, `data/mocks.json` or the handbook cards' practice links.
- **Listings tested.** Each chapter writer tested every non-trivial listing against a brute force
  on thousands of random inputs (harnesses kept outside the repo), ran every full program on its
  samples, and produced every number in traces, worked examples and answer keys by running code.
  The sample-paper programs of Chapter 25 pass the official PDF's samples and were stress-tested.
- **Pages inspected.** Each writer rendered and inspected every page of their chapters. Chapter 2 was
  the fully reviewed reference chapter; the final pass checked the preface, the appendices and a
  sample of pages from every new chapter.

### Known limitations

- Several later theory chapters run above the word targets in `AUTHORING.md` (for example Chapters
  20, 21, 22 and 24 have 7,000–7,700 words of prose); they were kept to preserve complete proofs.
- Problem statements from Infosys and other judges are **paraphrased**. For the Infosys repeats that
  the plan gives without an input format (Chapter 24, and "pairs with XOR < K and a prime sum" in
  Chapter 26), the input order is the book's own choice and the text says so.
- Facts about Infosys come only from the sources listed on the plan page (Theory Appendix D).
  Logistics the sources do not state (reporting time, what to bring) are explicitly left to the
  invite or admit card in Chapter 29.
- A few workbook pages leave part of a page blank where a listing or a grid is kept together on the
  next page (`:::nobreak`), which is deliberate.

### Tooling changes made while finishing

- `build.py`: the plan's "why it matters" line now renders its links instead of printing raw HTML
  (Day 9); the rest day (29) no longer gets a "re-solve" block after "Tomorrow is the exam"; TOC
  section titles are unescaped before escaping, so apostrophes no longer print as `&rsquo;`.
- `style/theory.css`: the note under the generated "Problems" heading is kept with its table.
- `tools/extract_sources.py`: new, re-creates the writing sources from the two HTML guides.

---

## How to change a chapter

1. Read `src/AUTHORING.md` and the reference chapters `src/theory/ch02.md` and `src/workbook/ch02.md`.
2. If you need the writing sources (day data, handbook cards, plan sections), extract them:
   ```bash
   W=/path/to/scratch; mkdir -p $W/html
   git show origin/feature/infosys-dsa-responsive-layout:infosys-dsa-29-day-plan.html  > $W/html/plan.html
   git show origin/feature/infosys-dsa-responsive-layout:infosys-dsa-daily-handbook.html > $W/html/handbook.html
   python tools/extract_sources.py $W
   ```
3. Edit `theory/chNN.md` or `workbook/chNN.md`, then verify:
   ```bash
   python tools/check_java.py theory/chNN.md workbook/chNN.md
   python build.py theory   --only N --out /tmp/tN.pdf && python tools/preview.py /tmp/tN.pdf 1-20 --out /tmp/p
   python build.py workbook --only N --out /tmp/wN.pdf && python tools/preview.py /tmp/wN.pdf 1-20 --out /tmp/p
   ```
4. Run `python build.py` to rebuild both full PDFs at `textbooks/`.

---

## Sources

Content was rephrased from these for licensing compliance; problem statements are paraphrased.
Theory Appendix D has the full list.

- The repo's own `infosys-dsa-29-day-plan.html` and `infosys-dsa-daily-handbook.html` (the
  plan, the 44 pattern cards, the decoded sample paper, the Java exam kit, the repeat
  problems). The plan cites: Infosys' official SP/DSE sample PDF, PrepInsta, takeUforward,
  placementpreparation.io, GeeksforGeeks, LeetCode Discuss, a community question bank, and
  Great Learning. Problem links were checked against the LeetCode and Codeforces APIs on
  7 Nov 2026 by the plan's author.
- Textbook structure and pedagogy: chapter openers and closers; the CLRS convention of section
  exercises and chapter problems; faded worked examples and retrieval practice (workbook design).
- Algorithm references: Antti Laaksonen, *Competitive Programmer's Handbook* (cses.fi/book);
  *Algorithms for Competitive Programming* (cp-algorithms.com, only pages confirmed to exist);
  Cormen–Leiserson–Rivest–Stein, *Introduction to Algorithms* (4th ed.); the Java 8 API docs.

Fonts (all SIL Open Font License, downloaded at build time): Source Serif 4, Source Sans 3,
JetBrains Mono, Noto Sans Math, Noto Sans Symbols 2.
