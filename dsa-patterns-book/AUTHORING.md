# Writing a pattern window

The book is built from two kinds of files:

- `content/partNN.yaml`: the words for each pattern (one YAML entry per pattern).
- `java/PNNName.java`: the Java the book prints, plus a test `main()` that proves it works.

The printed code is cut out of the Java files between `//@tpl name` and `//@end`, so what
the reader sees is exactly what was compiled and tested. `content/part02.yaml` with
`java/P02OppositePointers.java` and `java/P02SlidingWindow.java` is the reference example.
Match its depth, tone and layout.

## The YAML entry

```yaml
intro: >-               # once per part file (only the first file of a split part)
  Two or three sentences: what the part's patterns have in common and when to reach for them.

patterns:
  - id: sliding-window              # must match content/manifest.yaml exactly, same order
    title: Sliding window (variable size)       # ≤ 36 characters, or add `short:`
    short: Sliding window           # optional; used in the page header when the title is long
    signal: Longest or shortest contiguous subarray or substring that satisfies a condition.
    spot:                           # 2–4 bullets: what the problem statement looks like
      - ...
    idea: |                         # "How it works": 70–170 words, 1–3 paragraphs
      ...
    trace_title: "a = [1, 2, 1, 3, 3, 2], at most 2 distinct"   # optional but wanted
    trace: |                        # ≤ 12 lines, ≤ 80 columns, plain ASCII table
      ...
    code:                           # 1–3 regions; the first is the one to memorise
      - file: P02SlidingWindow.java
        region: longest
        title: Longest valid window
        tag: memorise               # memorise | variant | example | helper | usage
        note: optional one-line note printed under the code
    time: O(n)                      # short: shown in a pill in the title bar (≤ 16 chars)
    space: O(k)
    cost_note: One sentence on why.
    variants:                       # 2–4 bullets: the same idea with a small change
      - ...
    traps:                          # 2–4 bullets: real bugs people write
      - ...
    practice: [slug-one, slug-two]  # 3–6 LeetCode slugs, easiest first
```

Inline markup in any text field: `` `code` ``, `**bold**`, `*italic*`, and `[[pattern-id]]` for
a cross reference (prints as "2.8 Exactly k (p. 40)"). Cross references may point to any id in
`manifest.yaml`, including parts other people are writing. Quote a YAML string if it contains
`: ` or starts with a backtick, `*`, `[` or `"`.

## Writing style

- Write for one reader preparing for coding interviews and online assessments in Java. Use "you".
- Plain words, short sentences, one idea per sentence. Name the mechanism and the number.
- No filler, no hype, no "simply"/"just"/"robust"/"leverage"/"crucial". No em-dash chains.
- "How it works" must explain *why* the method is correct and *why* it has its cost, not only
  what the code does. Name the invariant when there is one.
- The "Spot it" bullets are about the wording and limits of a problem, so the reader learns to
  recognise the pattern from a problem statement.
- Traps are concrete: the exact wrong line or condition, and what happens.
- Use British spelling to match the rest of the book (memorise, practise as a verb).
- Unicode is fine in prose (≤ ≥ → × 10⁵ ·). Keep Java code ASCII.

## The Java file

```java
import java.util.*;

public class P09Dijkstra {          // P + two-digit part number + name; file name = class name
    //@tpl main
    static long[] dijkstra(List<int[]>[] g, int src) { ... }
    //@end

    // ---------------- tests (not printed) ----------------
    static long[] brute(...) { ... }

    public static void main(String[] args) {
        Random r = new Random(1);
        for (int t = 0; t < 2000; t++) { ... Check.eq(expected, got, "context"); }
        Check.eq(..., ..., "LeetCode sample 1");
        Check.done("P09Dijkstra");
    }
}
```

Rules:

1. **Java 8.** Everything must compile with `javac --release 8`: no `var`, `List.of`, `Map.of`,
   records, text blocks, switch expressions, `String.repeat`, `isBlank`, `strip`. Assume
   `import java.util.*;` (and `java.io.*` for I/O) and nothing else.
2. **One top-level class per file**, `public`, named `PNN<Name>`. Helper types (DSU, Edge,
   Node, Trie...) are `static` nested classes *inside* it, so names never clash between files.
   The only shared classes are `ListNode` and `TreeNode` in `java/Nodes.java` (LeetCode's
   definitions) and the test helper `java/Check.java`. Do not edit those two files or any file
   you did not create.
3. **Printed regions**: `//@tpl name` … `//@end`, each ≤ 52 lines and ≤ 82 columns after the
   common indentation is removed, 4-space indents, no tabs. Total printed code per pattern
   about 15–70 lines. Write it the way you'd want to memorise it: clear names, the standard
   shape of the algorithm, short comments that number the steps or state the invariant.
   Use `long` where sums or products can pass 2·10⁹. Mention recursion depth limits in a trap
   if the code recurses on input-sized depth.
4. **Tests**: `main()` exercises every printed method. Compare against a brute force on
   hundreds or thousands of small random inputs whenever a brute force exists, and check at
   least one LeetCode sample. Use fixed seeds. It must finish in under 10 seconds and end with
   `Check.done("<ClassName>")`. Use `Check.eq`, `Check.isTrue`, `Check.ints`, `Check.sortedInts`,
   `Check.str`, `Check.randomTree`, `Check.canon`, `Nodes.list`, `Nodes.tree`, `Nodes.randomTree`.
5. **Traces** must be true. Produce every number in a trace by running the code (a throwaway
   `main` or a few prints), not by hand.

## Practice problems

LeetCode only, free (not Premium), real slugs (the part of the URL after `/problems/`). Each
must actually be solved by the pattern, easiest first. Avoid repeating a problem that another
pattern in your part already lists.

## Checking your work

```bash
cd dsa-patterns-book
python tools/check_java.py --part 9          # compile the part's files with javac --release 8, run the tests
python tools/check_leetcode.py --part 9      # every slug exists and is free; caches titles
python build.py --only 9 --out preview-9.pdf # render just that part
python tools/check_layout.py preview-9.pdf   # empty window frames, text past the edge
python tools/preview.py preview-9.pdf 1-6 --out png-9   # PNGs to look at
```

`build.py --only` prints `PROBLEM` lines for anything missing in that part (unwritten patterns,
unknown slugs, long titles). In a part-only preview, cross references into other parts have
no page number; `check_layout.py` reports those as unresolved, which is expected there.
