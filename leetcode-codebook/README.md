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

## Anki deck

`../infosys-leetcode-codebook.apkg` is the same book as Anki flashcards (732 cards). Import it with **File → Import** (desktop) or by opening the file in AnkiDroid / AnkiMobile.

Decks follow the 10-day schedule:

```
LeetCode Codebook · Infosys
  Day 01 · Wed 25 Nov
    1 · Plan Day 19 · Trees II: tree DP, rerooting, small-to-large
    2 · Plan Day 1 · Set up, Java toolkit, fast input
    3 · Plan Day 2 · Prefix sums, difference arrays, hashing
  Day 02 · Thu 26 Nov
  …
  Day 09 · Thu 3 Dec
  Day 10 · Fri 4 Dec
    1 · Pattern triggers
```

| Card | Front | Back |
|---|---|---|
| Spot the pattern | LC number, title, "Spot it" | pattern, hook, plan hint, cost |
| Write the code | pattern, "Spot it", hook | full Java solution (key lines highlighted), cost, trap |
| Fill the key lines | the solution with the highlighted lines blanked (the book's hint stays) | full solution, trap |
| Variation | a variation's title + the main hook | the idea and its Java code |
| Quick variation | a one-line variation | what changes |
| Pattern core | the day's template with one part hidden | the template with the hidden part in blue |
| Pattern trigger | "the problem says …" | the pattern to reach for and its cards |

Every code block in the book (157 solutions, 85 variations, 21 pattern cores) is on a card; long cores are split into parts of at most 12 hidden lines.

New cards per day: 104, 89, 79, 92, 85, 85, 87, 66, 15, 30.

Settings to change after importing (gear icon on the deck → Options, then **Save to all subdecks**):

- **New cards/day: 200** and **Maximum reviews/day: 9999**. The default of 20 new cards would hold most of each day back.
- Leave **Bury new siblings** off, so a problem's cards all appear on its day.

Anki schedules the reviews itself (with default settings, gaps of about 1, 3 and 7–8 days). To follow the book's dates exactly, every note is tagged `codebook::review::YYYY-MM-DD`; make a filtered deck with that tag on the day. For the final pass on Sat 5 Dec, make a filtered deck with the search `"deck:LeetCode Codebook · Infosys" "card:1 Spot the pattern"` and limit 200. That is the Recall Sheet.

Re-importing a rebuilt `.apkg` updates the existing notes (stable note IDs) and keeps your review history.

## Rebuilding and checking

```
./run_tests.sh            # compiles src/*.java and runs every day's tests (examples + brute-force checks)
python3 check_printed.py  # compiles each printed solution alone, as you would paste it into LeetCode
python3 build.py          # regenerates ../infosys-leetcode-codebook.html
pip install genanki       # once (use a virtualenv if site-packages is read-only)
python3 build_apkg.py     # regenerates ../infosys-leetcode-codebook.apkg from the same sources and schedule
```

Source format (one file per plan day in `src/`): a `/*@LC n … @*/` header with `tag`, `cue`, `hook`, `time`, `trap`, `var` fields, followed by the tested class. `/*@var …@*/` blocks attach a variation to the card above; `/*@core …@*/` is a day's pattern core. A trailing `//*` marks a key line. `build.py` refuses to build if a plan problem has no card or a class has no test.

Code font: JetBrains Mono (SIL Open Font License 1.1, see `fonts/OFL.txt`), embedded so code stays monospaced on any printer.
