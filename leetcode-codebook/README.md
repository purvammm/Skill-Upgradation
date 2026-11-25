# LeetCode Memory Codebook (Infosys 29-day plan)

The printable book is `../infosys-leetcode-codebook.html` (one self-contained file, about 137 A4 pages).

## Printing

1. Open the HTML file in Chrome or Edge.
2. Press the **Print / Save as PDF** button (or Ctrl/Cmd + P).
3. Paper **A4**, portrait, margins **Default**, and turn **Background graphics ON** (the highlighted key lines and tick boxes need it).
4. To save paper, print double-sided. "2 pages per sheet" works too, but the code becomes small.

## What is inside

- Cover with today's date (Wed 25 Nov 2026), study days left, and how to use the book.
- A 10-day spaced-repetition schedule (25 Nov → 4 Dec, final pass 5 Dec, exam 6 Dec). Every card carries its own learn and review dates with tick boxes.
- Pattern triggers: "the problem says … → reach for …".
- Every LeetCode problem from the plan (136), grouped by plan day, plus 21 bonus problems. Each card has the trigger, a one-line memory hook, the Java solution with the key lines highlighted, cost, the common trap, and variations (85 with code, more as one-liners).
- A pattern core box at the start of most days (the template to memorise first).
- A Recall Sheet at the back with every hook, for the last day.

## Rebuilding and checking

```
./run_tests.sh            # compiles src/*.java and runs every day's tests (examples + brute-force checks)
python3 check_printed.py  # compiles each printed solution alone, as you would paste it into LeetCode
python3 build.py          # regenerates ../infosys-leetcode-codebook.html
```

Source format (one file per plan day in `src/`): a `/*@LC n … @*/` header with `tag`, `cue`, `hook`, `time`, `trap`, `var` fields, followed by the tested class. `/*@var …@*/` blocks attach a variation to the card above; `/*@core …@*/` is a day's pattern core. A trailing `//*` marks a key line. `build.py` refuses to build if a plan problem has no card or a class has no test.

Code font: JetBrains Mono (SIL Open Font License 1.1, see `fonts/OFL.txt`), embedded so code stays monospaced on any printer.
