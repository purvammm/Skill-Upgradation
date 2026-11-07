# Infosys DSA textbooks — build notes, reasoning, and status

This directory turns the two single-page study guides from this repo
(`infosys-dsa-29-day-plan.html` and `infosys-dsa-daily-handbook.html`, from the open
PRs on the `Claude-version` branch) into two printable **textbook-style PDFs**:

- **`Infosys-DSA-Theory-Textbook.pdf`** — Volume I, the textbook. One chapter per day
  of the 29-day plan, so **29 chapters**, each teaching the day's ideas properly:
  definitions, theorems with proofs, worked examples with traces, Java listings,
  complexity, figures, exam tips, a summary, review questions with answers, and the
  day's problem list.
- **`Infosys-DSA-Practice-Workbook.pdf`** — Volume II, the workbook with the **same 29
  chapters**. Each has the day's time plan, a spaced-repetition review list, exercises
  you complete by hand (fill-the-gap templates, traces, "find the bug", design tasks),
  a worksheet per problem, and an end-of-day error log. Hints and answers are at the back.

> **This is a work in progress.** It was pushed mid-way (at the user's request) so nothing
> is lost. 17 of 29 theory chapters and 8 of 29 workbook chapters are written. The build
> system is complete and both PDFs generate cleanly from whatever chapters exist.
> See [Status](#status) and [How to resume](#how-to-resume).

---

## What was asked, and how I read it

The request: read the latest comment in the repo, then produce two textbook-style PDFs
(a theory PDF and a practical PDF) with **29 chapters**, matching the structure of the
existing HTML guides, referencing how real textbooks are laid out, and in the end push
the PDFs. Two follow-up steers: *(1)* add more theory/detail for each day to refer to,
done however is best; *(2)* a single combined textbook would also be acceptable — my call.

Decisions I made from that:

1. **Two volumes, not one.** The plan page and the handbook already split "what to do each
   day" from "how each technique works". A theory volume (read first) and a practice
   workbook (worked in by hand, with writing space) map onto that split cleanly, and a
   workbook only makes sense as a separate, consumable book. I kept them as two books with
   **shared chapter numbers**, so Theory Ch 7 and Workbook Ch 7 are the same day. This also
   satisfies the "two PDFs" wording of the original request while honouring the "one
   textbook is fine" steer in spirit (they are one course in two volumes).
2. **29 chapters = the 29 days.** The plan has exactly 29 days; each becomes a chapter with
   the day's topic as its title. Mock days (9, 16, 23, 25, 27), the repair day (26),
   consolidation (28) and the rest day (29) become chapters about exam technique,
   debugging, pattern recognition and logistics, so the count stays at 29 with no filler.
3. **Much more theory than the HTML had.** The HTML handbook is terse ("use it when / how it
   works / template / traps"). The brief said to add depth. So each learning chapter proves
   *why* an algorithm is correct and *what* it costs, not just the template — the level of a
   real algorithms textbook (CLRS / CP-Handbook / cp-algorithms), aimed at one reader.
4. **Real-textbook pedagogy.** I looked at how textbooks are structured (openers:
   objectives, introduction; body; closers: summary, key terms, review questions) and at
   CLRS's convention of *exercises* within and *problems* at the end. The workbook also uses
   the "faded worked examples" idea from cognitive-load research (full example → example with
   gaps → solve alone) and spaced retrieval (the plan's own Again/Shaky/Solid schedule).

---

## Why this toolchain

PDFs are generated from **Markdown → HTML → PDF with [WeasyPrint](https://weasyprint.org/)**
(CSS Paged Media). Reasoning:

- The sandbox has no LaTeX or headless browser, but WeasyPrint installs cleanly via pip and
  does real book typesetting: running headers, footnotes, a table of contents and an index
  with live page numbers, `@page` margins, and controlled page breaks.
- Markdown keeps the 58 chapter files easy to write and review, and let me split the writing
  across parallel sub-agents against one spec.
- Everything is reproducible and self-contained: open-source fonts, a from-scratch figure
  renderer, and the plan/handbook data extracted to JSON.

No binary design tool, no manual page layout: the same source rebuilds both books in a few
seconds each.

---

## How it is built

Everything lives under `src/`. Build with the project venv (has `weasyprint`, `markdown`,
`pygments`, `pyyaml`, `pypdfium2`):

```bash
cd textbooks/src
PY=/projects/sandbox/work/.venv/bin/python     # or any venv with the deps below

$PY build.py                                   # both books -> textbooks/*.pdf
$PY build.py theory                            # one book
$PY build.py theory --only 2,3 --out /tmp/preview.pdf     # just some chapters
$PY tools/preview.py <pdf> 1-20 --out /tmp/prev           # render pages to PNG to eyeball
$PY tools/check_java.py theory/ch07.md                    # javac --release 8 on every listing
```

Dependencies: `pip install weasyprint markdown pygments pyyaml pypdfium2`. On first run,
`tools/fonts.py` downloads the OFL fonts (Source Serif 4, Source Sans 3, JetBrains Mono,
two Noto fallbacks) into `src/.fonts/`, which is git-ignored.

### Layout

```
textbooks/
  Infosys-DSA-Theory-Textbook.pdf        generated (committed)
  Infosys-DSA-Practice-Workbook.pdf      generated (committed)
  PROGRESS.md                            this file
  src/
    build.py          assembles each book: title page, preface, TOC, parts, chapters,
                      generated openers/problem tables/workbook blocks, appendices, index
    AUTHORING.md      the full spec every chapter follows (read this first to write more)
    bookmd? ->        tools/bookmd.py   the Markdown dialect (boxes, figures, cross-refs,
                                        numbered listings, index terms, footnotes)
    tools/
      figures.py      a small SVG renderer: array / tree / graph / bars / intervals / grid
      fonts.py        downloads fonts, emits @font-face
      check_java.py   compiles every ```java block with javac --release 8
      preview.py      PDF pages -> PNG for visual checks
    style/            base.css, code.css, theory.css, workbook.css (CSS Paged Media)
    data/             plan.json, mocks.json, patterns.json (extracted from the two HTML pages)
    theory/           chNN.md  + front-preface.md, app-c-answers.md
    workbook/         chNN.md  + front-howto.md
```

### The Markdown dialect (see `src/AUTHORING.md` for the full spec)

On top of normal Markdown, a chapter can use:

- fenced code with a title/id → numbered **Listing N.k** with line numbers;
  ` ```fig {#id} caption ` + a JSON body → a numbered **Figure N.k** drawn by `figures.py`;
- containers `:::definition / theorem / lemma / proof / example / exercise / keyidea / note /
  tip / pitfall / infosys / java / answers / write / grid / columns ... :::` (numbered boxes
  get automatic numbers);
- `[[term]]` index terms, `^[footnotes]`, and cross-references `@lst-2-1`, `@fig-…`, `@thm-…`
  (and `@T:lst-…` from the workbook into the theory book);
- section numbers (`## Title` → N.k), part/chapter openers, key-terms lists, the per-day
  problem table and all workbook blocks (`{{plan}}`, `{{recall}}`, `{{mock}}`, `{{problems}}`,
  `{{resolve}}`, `{{end}}`) are generated by `build.py` from the JSON, not hand-written.

Labels are global within a book, so every id carries its chapter number to avoid collisions
across chapters written in parallel.

---

## Status

Theory = Volume I chapter present; Workbook = Volume II chapter present.

| Ch | Day topic | Theory | Workbook |
|----|-----------|:------:|:--------:|
| 1 | Set up, Java toolkit, fast input | ✅ | ✅ |
| 2 | Prefix sums, difference arrays, hashing *(reference chapter)* | ✅ | ✅ |
| 3 | Two pointers and sliding window | ✅ | ✅ |
| 4 | Sorting and greedy: intervals | ✅ | ✅ |
| 5 | Greedy with heaps | ⬜ | ⬜ |
| 6 | Binary search, including on the answer | ✅ | ✅ |
| 7 | Monotonic stacks | ✅ | ✅ |
| 8 | Bits and XOR | ✅ | ⬜ |
| 9 | Mock 1 + review | ✅ | ⬜ |
| 10 | Number theory toolkit | ✅ | ✅ |
| 11 | DP I: states, transitions, knapsack | ✅ | ⬜ |
| 12 | DP II: two-string DP | ⬜ | ⬜ |
| 13 | DP III: LIS and extra dimensions | ✅ | ✅ |
| 14 | DP IV: divisor, interval and game DP | ✅ | ⬜ |
| 15 | Graphs I: BFS, DFS, topological order | ✅ | ⬜ |
| 16 | Mock 2 + review (problem analysis) | ✅ | ⬜ |
| 17 | Graphs II: Dijkstra, DSU, MST | ✅ | ⬜ |
| 18 | Trees I: recursion, parent arrays, LCA | ✅ | ⬜ |
| 19 | Trees II: tree DP, rerooting, small-to-large | ✅ | ⬜ |
| 20 | Fenwick trees and segment trees | ⬜ | ⬜ |
| 21 | Lazy segment trees and the K-parts DP | ⬜ | ⬜ |
| 22 | Backtracking, bitmask DP, digit DP | ⬜ | ⬜ |
| 23 | Mock 3 (4 problems) + review | ⬜ | ⬜ |
| 24 | Infosys repeat problems I | ⬜ | ⬜ |
| 25 | Mock 4: the official Infosys sample paper | ⬜ | ⬜ |
| 26 | Repair day + the hard XOR repeat | ⬜ | ⬜ |
| 27 | Mock 5: dress rehearsal | ⬜ | ⬜ |
| 28 | Consolidate | ⬜ | ⬜ |
| 29 | Rest and logistics | ⬜ | ⬜ |

**Totals: Theory 17/29, Workbook 8/29.** Current PDFs: Theory ≈ 311 pages, Workbook ≈ 170
pages. The build is tolerant of missing chapters, so the books compile now and will simply
fill in as the remaining chapters are written.

### What is verified

- **Java compiles.** All 187 `java` blocks in the present chapters pass `javac --release 8`
  (179 compiled, 8 are deliberate fill-in-the-blank templates marked `.nocompile`). Nothing
  fails.
- **Both books build with no WeasyPrint warnings** (no missing fonts, no broken internal
  references, no unresolved cross-references among the chapters that exist).
- Chapter 2 (both volumes) is the fully reviewed **reference chapter**: I rendered every page
  to PNG, checked the figures, traces, tables and page breaks, and ran its listings against a
  brute force. It sets the standard the other chapters follow.
- The writing sub-agents were told to verify their own chapters the same way (brute-force
  tests, rendered-page checks). Chapters other than 2 have **not** yet been re-reviewed by me
  page-by-page — see caveats.

### Caveats / known gaps

- 12 theory chapters (5, 12, 20–29) and 21 workbook chapters are not written yet. The two
  mock chapters that explain the official sample paper (25) and the Infosys repeats (24) are
  among the missing ones, so the hardest exam-specific material is still to come.
- The generated **answers appendix** (Theory App. C) and **hints/answer-key appendices**
  (Workbook App. A/B) only contain entries for the chapters that exist.
- Problem statements from Infosys and other judges are **paraphrased**, and the Infosys facts
  come only from the sources listed on the plan page (kept verbatim to those sources).
- I have not re-run a full page-by-page visual pass over every agent-written chapter; expect
  a few figure or page-break tweaks when the set is complete.

---

## How to resume

1. Read `src/AUTHORING.md` (the complete spec) and the reference chapters `src/theory/ch02.md`
   and `src/workbook/ch02.md`.
2. For a missing chapter N, read its day data `/projects/sandbox/work/sources/days/dayNN.json`
   and the handbook cards it lists (`/projects/sandbox/work/sources/cards/<id>.md`), plus the
   plan sections under `/projects/sandbox/work/sources/plan/*.md` for any Infosys facts.
   *(These `sources/` files are the extracted HTML content; they live outside the repo under
   the working directory. Re-extract with the scripts noted below if they are gone.)*
3. Write `theory/chNN.md` and `workbook/chNN.md`, then verify:
   ```bash
   cd textbooks/src
   $PY tools/check_java.py theory/chNN.md workbook/chNN.md
   $PY build.py theory   --only N --out /tmp/tN.pdf && $PY tools/preview.py /tmp/tN.pdf 1-20 --out /tmp/p
   $PY build.py workbook --only N --out /tmp/wN.pdf && $PY tools/preview.py /tmp/wN.pdf 1-20 --out /tmp/p
   ```
4. When all 29 exist, run `$PY build.py` to rebuild both full PDFs at `textbooks/`.

Remaining order suggested: 20, 21, 22 (range structures and exponential DP), then 24 and 25
(the Infosys-specific repeats and the sample paper — highest value), then 5, 12, 23, 26–29,
then fill the missing workbook chapters for days already written in theory.

---

## Sources

Content was rephrased from these for licensing compliance; problem statements are paraphrased.

- The repo's own `infosys-dsa-29-day-plan.html` and `infosys-dsa-daily-handbook.html` (the
  plan, the 44 pattern cards, the decoded sample paper, the Java exam kit, the repeat
  problems). The plan cites: Infosys' official SP/DSE sample PDF, PrepInsta, takeUforward,
  placementpreparation.io, GeeksforGeeks, LeetCode Discuss, a community question bank, and
  Great Learning. Problem links were checked against the LeetCode and Codeforces APIs on
  7 Nov 2026 by the plan's author.
- Textbook structure and pedagogy: open-pedagogy guidance on chapter openers/closers; the
  CLRS convention of section "exercises" and chapter "problems"; cognitive-load research on
  faded worked examples and retrieval practice (used for the workbook's design only).
- Algorithm references used while writing and for "Further reading": Antti Laaksonen,
  *Competitive Programmer's Handbook* (free at cses.fi/book); *Algorithms for Competitive
  Programming* (cp-algorithms.com, only pages confirmed to exist); Cormen–Leiserson–Rivest–
  Stein, *Introduction to Algorithms* (4th ed.); the Java 8 API docs.

Fonts (all SIL Open Font License, downloaded at build time): Source Serif 4, Source Sans 3,
JetBrains Mono, Noto Sans Math, Noto Sans Symbols 2.
