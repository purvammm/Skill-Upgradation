#!/usr/bin/env python3
"""Build the printable LeetCode memory codebook from the tested Java sources.

    python3 build.py            # writes ../infosys-leetcode-codebook.html

Every code block in the book is cut out of src/*.java, which run_tests.sh compiles
and runs, so the printed code is the tested code.
"""
import datetime as dt
import html
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SRC = ROOT / "src"
OUT = ROOT.parent / "infosys-leetcode-codebook.html"

TODAY = dt.date(2026, 11, 25)      # the date this edition is printed for
EXAM = dt.date(2026, 12, 6)
PLAN_START = dt.date(2026, 11, 7)  # plan Day 1

# ---------------------------------------------------------------- spaced repetition
# Each study date learns some plan days (and a bonus group). Review offsets are in days.
LEARN = [
    ("2026-11-25", [19, 1, 2]),
    ("2026-11-26", [20, 3, 4]),
    ("2026-11-27", [21, 5, 6]),
    ("2026-11-28", [22, 7, 8]),
    ("2026-11-29", [10, 11, "A"]),
    ("2026-11-30", [12, 13, "B"]),
    ("2026-12-01", [14, 15, "C"]),
    ("2026-12-02", [17, 18]),
    ("2026-12-03", [28, 29]),
    ("2026-12-04", []),
]
OFFSETS = [1, 3]                   # fixed reviews
THIRD_WINDOW = [6, 7, 8]           # third review: the lightest of these days (else the final pass)
LAST_STUDY = dt.date(2026, 12, 4)
FINAL = dt.date(2026, 12, 5)       # rest day: recall sheet only
LEARN_MIN, REVIEW_MIN = 6, 1.5     # minutes per card, used only for the load column

BONUS_GROUPS = {
    "A": "Bonus A · arrays and two pointers",
    "B": "Bonus B · dynamic programming",
    "C": "Bonus C · design, trees and graphs",
}

TRIGGERS = [
    ("Pair / complement / \"seen before?\"", "HashMap look-back", "1, 217, 560"),
    ("Count subarrays with sum = k (negatives allowed)", "Prefix sum + HashMap {0:1}", "560, 974, 525, 1524"),
    ("Many range updates, read once at the end", "Difference array", "1109, 1526"),
    ("Rectangle sums, many queries", "2D prefix sum", "304"),
    ("Longest / shortest window with a rule", "Variable sliding window", "3, 1004, 424, 76"),
    ("Count subarrays with EXACTLY k of something", "atMost(k) − atMost(k − 1)", "992"),
    ("Max / min of every window of size k", "Monotonic deque", "239"),
    ("Sorted array, pair or triple with a target", "Two pointers from both ends", "15, 881"),
    ("Intervals: merge, keep most, fewest rooms", "Sort by start or end, then sweep", "56, 435, 452, 2406"),
    ("Pick the best affordable option again and again", "Greedy + heap", "502, 1642, 871, 630"),
    ("\"Minimum X such that it works\" / \"maximise the minimum\"", "Binary search on the answer", "875, 1011, 410, 1552"),
    ("Next greater / smaller, span, histogram", "Monotonic stack", "739, 503, 84, 907"),
    ("Pairs cancel, XOR of ranges, max XOR", "XOR prefix / binary trie", "136, 1442, 421, 1707"),
    ("Primes up to n, gcd, big powers mod p", "Sieve, gcd, fast power", "204, 1922, 2447"),
    ("Take or skip, reach an amount", "1-D DP / knapsack", "198, 322, 518, 416, 494"),
    ("Two strings: match, edit, interleave", "2-D DP table dp[i][j]", "1143, 72, 97, 115"),
    ("Longest increasing chain", "LIS with binary search (tails)", "300, 354, 1964, 1671"),
    ("Best result on a range l..r / two players", "Interval DP / game DP", "486, 312, 1547, 664"),
    ("Fewest steps on a grid or unweighted graph", "BFS (multi-source, 0-1 BFS)", "200, 994, 1091, 1368"),
    ("Order with prerequisites", "Topological sort (Kahn)", "207, 2050"),
    ("Cheapest path, non-negative weights", "Dijkstra", "743, 1631, 1976"),
    ("Connect everything cheaply / merge groups", "DSU + Kruskal", "1584, 2421"),
    ("Tree: answer bends at a node", "Post-order returning one number", "543, 124, 2246"),
    ("Answer for every node as root", "Rerooting (two passes)", "834, 2858"),
    ("k-th ancestor, path queries", "Binary lifting / LCA", "1483, 2846"),
    ("Point update + range query", "Fenwick / segment tree", "307, 315, 2407"),
    ("Range add + range max", "Lazy segment tree", "732, 2916"),
    ("Split the array into k parts", "dp[k][i] over the last cut", "1335, 813"),
    ("n ≤ 20, choose a subset / assignment", "Bitmask DP", "1986, 1879, 847"),
    ("Count numbers ≤ N with a digit rule", "Digit DP (pos, tight, ...)", "902, 2827"),
]

# ---------------------------------------------------------------- parsing
BLOCK = re.compile(
    r"/\*@(?P<head>.*?)@\*/[ \t]*\n(?P<code>class[ \t]+(?P<cls>\w+)[^\n]*\n.*?\n\}\n)", re.S)


def parse_head(head):
    lines = head.strip("\n").split("\n")
    first = lines[0].strip()
    fields = {}
    last = None
    for ln in lines[1:]:
        if not ln.strip():
            continue
        m = re.match(r"^(\w+):\s?(.*)$", ln)
        if m and not ln.startswith(" "):
            key, val = m.group(1), m.group(2).strip()
            fields.setdefault(key, []).append(val)
            last = key
        elif last:
            fields[last][-1] += " " + ln.strip()
        else:
            raise ValueError("bad header line: " + ln)
    return first, fields


def display_code(code, cls, kind, fields):
    """Turn a tested Java class into the text printed in the book."""
    shown = fields.get("class", [None])[0]
    lines = code.rstrip("\n").split("\n")
    if kind == "problem" or shown:
        name = shown or "Solution"
        lines = [re.sub(r"\b%s\b" % re.escape(cls), name, ln) for ln in lines]
    else:  # body only, dedented
        body = lines[1:-1]
        lines = [ln[4:] if ln.startswith("    ") else ln for ln in body]
        while lines and not lines[0].strip():
            lines.pop(0)
    out = []
    for ln in lines:
        key = False
        m = re.search(r"//\*", ln)
        if m:  # "code; //* note" -> highlighted "code; // note"; "code; //*" -> highlighted "code;"
            key = True
            before, note = ln[:m.start()], ln[m.end():].strip()
            ln = before + "// " + note if note else before
        out.append((ln.rstrip(), key))
    return out


def load_sources():
    problems, cores = [], {}
    tests_ok = True
    for path in sorted(SRC.glob("*.java")):
        if path.name == "Common.java":
            continue
        text = path.read_text()
        test_part = text[text.find("public class "):]
        owner = None
        for m in BLOCK.finditer(text):
            first, fields = parse_head(m.group("head"))
            cls = m.group("cls")
            if not re.search(r"\b%s\b" % re.escape(cls), test_part):
                print("NOT TESTED:", path.name, cls)
                tests_ok = False
            if first.startswith("core"):
                kind = "core"
                item = {"kind": kind, "title": first[4:].strip(), "fields": fields, "cls": cls, "file": path.stem}
                item["code"] = display_code(m.group("code"), cls, kind, fields)
                cores.setdefault(path.stem, []).append(item)
            elif first.startswith("var"):
                kind = "var"
                if owner is None:
                    raise ValueError("variation before any problem in " + path.name)
                item = {"kind": kind, "title": first[3:].strip(), "fields": fields, "cls": cls}
                item["code"] = display_code(m.group("code"), cls, kind, fields)
                owner["vars"].append(item)
            elif first.startswith("LC"):
                kind = "problem"
                parts = [p.strip() for p in first.split("·")]
                num = int(parts[0][2:].strip())
                item = {"kind": kind, "num": num, "fields": fields, "cls": cls, "file": path.stem,
                        "title": parts[1] if len(parts) > 1 else None,
                        "diff": parts[2] if len(parts) > 2 else None, "vars": []}
                item["code"] = display_code(m.group("code"), cls, kind, fields)
                problems.append(item)
                owner = item
            else:
                raise ValueError("unknown block head: " + first)
    return problems, cores, tests_ok


# ---------------------------------------------------------------- HTML helpers
JAVA_KW = set("""abstract boolean break byte case catch char class continue default do double else enum
extends final finally float for if implements import instanceof int interface long new null private protected
public record return short static super switch this throw throws true false try var void while""".split())
TOKEN = re.compile(r'(?P<com>//.*$)|(?P<str>"(?:\\.|[^"\\])*")|(?P<chr>\'(?:\\.|[^\'\\])\')'
                   r'|(?P<num>\b\d[\d_]*(?:\.\d+)?[LlDdFf]?\b)|(?P<id>[A-Za-z_]\w*)|(?P<other>.)')


def hl(line):
    out = []
    for m in TOKEN.finditer(line):
        t = html.escape(m.group(0), quote=False)
        if m.group("com"):
            out.append('<i class="c">%s</i>' % t)
        elif m.group("str") or m.group("chr"):
            out.append('<span class="s">%s</span>' % t)
        elif m.group("num"):
            out.append('<span class="n">%s</span>' % t)
        elif m.group("id"):
            w = m.group("id")
            if w in JAVA_KW:
                out.append('<b class="k">%s</b>' % t)
            elif w[0].isupper():
                out.append('<span class="t">%s</span>' % t)
            else:
                out.append(t)
        else:
            out.append(t)
    return "".join(out)


def code_html(lines, small=False):
    rows = []
    for text, key in lines:
        cls = "ln key" if key else "ln"
        rows.append('<span class="%s">%s</span>' % (cls, hl(text) if text else "&#8203;"))
    return '<pre class="code%s">%s</pre>' % (" small" if small else "", "".join(rows))


def inline(s):
    s = html.escape(s, quote=False)
    return re.sub(r"`([^`]+)`", r"<code>\1</code>", s)


def fdate(d, year=False):
    s = d.strftime("%a %-d %b")
    return s + (d.strftime(" %Y") if year else "")


def sdate(d):
    return d.strftime("%a %-d")


DIFF_CLASS = {"Easy": "easy", "Medium": "med", "Hard": "hard"}


# ---------------------------------------------------------------- build
def main():
    plan = json.loads((ROOT / "plan.json").read_text())
    problems, cores, tests_ok = load_sources()
    by_num = {}
    for p in problems:
        if p["num"] in by_num:
            sys.exit("duplicate card LC %d" % p["num"])
        by_num[p["num"]] = p

    # attach plan info; check every plan problem has a card in the right file
    missing = []
    day_of = {}
    for d in plan:
        for q in d["problems"]:
            p = by_num.get(q["n"])
            if p is None:
                missing.append("Day %d LC %d %s" % (d["day"], q["n"], q["title"]))
                continue
            if p["file"] != "Day%02d" % d["day"]:
                missing.append("LC %d is in %s, plan says Day %d" % (q["n"], p["file"], d["day"]))
            p.update(title=q["title"], diff=q["diff"], slug=q["slug"], day=d["day"])
            day_of[q["n"]] = d["day"]
    bonus = [p for p in problems if p["file"].startswith("Bonus")]
    for p in bonus:
        p["day"] = None
        p["group"] = p["fields"].get("group", ["?"])[0]
        if p["group"] not in BONUS_GROUPS:
            sys.exit("bonus LC %d has no valid group" % p["num"])
        if p["num"] in day_of:
            sys.exit("bonus LC %d is already in the plan" % p["num"])
        if not p["title"] or not p["diff"]:
            sys.exit("bonus LC %d needs '· title · difficulty' in its header" % p["num"])
    extra = [p for p in problems if p.get("day") is None and not p["file"].startswith("Bonus")]
    if extra:
        sys.exit("cards not in the plan: " + ", ".join("LC %d" % p["num"] for p in extra))
    for p in problems:
        for f in ("cue", "hook", "time"):
            if f not in p["fields"]:
                missing.append("LC %d has no %s" % (p["num"], f))
    if missing or not tests_ok:
        print("\n".join(missing))
        if "--draft" not in sys.argv:
            sys.exit("build stopped: fix the problems above (or pass --draft)")

    # ---- schedule
    unit_cards = {}
    for d in plan:
        unit_cards[d["day"]] = [by_num[q["n"]] for q in d["problems"] if q["n"] in by_num]
    for g in BONUS_GROUPS:
        unit_cards[g] = [p for p in bonus if p["group"] == g]
    sched = []  # rows
    review_on = {}
    for iso, units in LEARN:
        day = dt.date.fromisoformat(iso)
        cards = [c for u in units for c in unit_cards.get(u, [])]
        sched.append({"date": day, "units": units, "cards": cards})
    learn_count = {row["date"]: len(row["cards"]) for row in sched}

    def load(d):
        return learn_count.get(d, 0) * LEARN_MIN + len(review_on.get(d, [])) * REVIEW_MIN

    for row in sched:  # fixed reviews first, then place the flexible third review on the lightest day
        day, cards = row["date"], row["cards"]
        for c in cards:
            c["learn"] = day
            c["reviews"] = []
        for off in OFFSETS:
            r = day + dt.timedelta(days=off)
            r = r if r <= LAST_STUDY else FINAL
            for c in cards:
                if r not in c["reviews"]:
                    c["reviews"].append(r)
                    review_on.setdefault(r, []).append(c)
    for row in sched:
        day, cards = row["date"], row["cards"]
        if not cards:
            continue
        options = [day + dt.timedelta(days=o) for o in THIRD_WINDOW]
        options = [d for d in options if d <= LAST_STUDY]
        r = min(options, key=lambda d: (load(d), d)) if options else FINAL
        for c in cards:
            if r not in c["reviews"]:
                c["reviews"].append(r)
                review_on.setdefault(r, []).append(c)
    unscheduled = [p for p in problems if "learn" not in p]
    if unscheduled and "--draft" not in sys.argv:
        sys.exit("not scheduled: " + ", ".join("LC %d" % p["num"] for p in unscheduled))

    html_out = render(plan, problems, cores, sched, review_on, bonus)
    OUT.write_text(html_out)
    nvar = sum(len(p["vars"]) for p in problems)
    print("wrote %s: %d cards (%d plan + %d bonus), %d variations, %d cores" % (
        OUT.name, len(problems), len(problems) - len(bonus), len(bonus), nvar,
        sum(len(v) for v in cores.values())))
    for row in sched:
        d = row["date"]
        print("  %s learn %2d  review %2d" % (d, len(row["cards"]), len(review_on.get(d, []))))
    print("  %s final pass (recall sheet) %d" % (FINAL, len(review_on.get(FINAL, []))))


def plan_day_of(date):
    return (date - PLAN_START).days + 1


def unit_label(u):
    return BONUS_GROUPS[u].split(" · ")[0] if isinstance(u, str) else "Day %d" % u


def card_html(p, idx):
    f = p["fields"]
    diff = p["diff"] or ""
    dcls = DIFF_CLASS.get(diff, "cf")
    url = "https://leetcode.com/problems/%s/" % p["slug"] if p.get("slug") else None
    title = html.escape(p["title"] or "")
    boxes = []
    if "learn" in p:
        boxes.append('<span class="box">%s</span><span class="bl">learn %s</span>' % ("", sdate(p["learn"])))
        for r in p["reviews"]:
            lab = "final" if r == FINAL else sdate(r)
            boxes.append('<span class="box"></span><span class="bl">%s</span>' % lab)
    where = ("Day %d" % p["day"]) if p.get("day") else BONUS_GROUPS.get(p.get("group"), "Bonus").split(" · ")[0]
    head = (
        '<div class="chead">'
        '<span class="num">LC %d</span>'
        '<span class="ttl">%s</span>'
        '<span class="diff %s">%s</span>'
        '<span class="where">%s · #%d</span>'
        '</div>' % (p["num"], title, dcls, html.escape(diff), where, idx))
    meta = '<div class="srs">%s<span class="tag">%s</span></div>' % (
        "".join(boxes), inline(f.get("tag", [""])[0]))
    cue = '<div class="row"><span class="lab">Spot it</span><span>%s</span></div>' % inline(f["cue"][0]) if "cue" in f else ""
    hook = '<div class="hook">“%s”</div>' % inline(f["hook"][0]) if "hook" in f else ""
    parts = ['<article class="card" id="lc%d">' % p["num"],
             '<div class="keep">', head, meta, cue, hook, '</div>',
             code_html(p["code"])]
    facts = []
    if "time" in f:
        facts.append('<div class="row"><span class="lab">Cost</span><span>%s</span></div>' % inline(f["time"][0]))
    for t in f.get("trap", []):
        facts.append('<div class="row trap"><span class="lab">Trap</span><span>%s</span></div>' % inline(t))
    parts.append('<div class="facts">%s</div>' % "".join(facts))
    tv = f.get("var", [])
    if p["vars"] or tv:
        parts.append('<div class="vars">')
        vhead = '<div class="vhead">Variations</div>'   # rides inside the first item so it never sits alone
        for v in p["vars"]:
            vf = v["fields"]
            note = '<div class="vnote">%s</div>' % inline(vf["note"][0]) if "note" in vf else ""
            parts.append('<div class="var">%s<div class="vt">%s</div>%s%s</div>' % (
                vhead, inline(v["title"]), note, code_html(v["code"], small=True)))
            vhead = ""
        if tv:
            items = []
            for t in tv:
                name, _, delta = t.partition("|")
                items.append('<li><b>%s</b> — %s</li>' % (inline(name.strip()), inline(delta.strip())))
            parts.append('<div class="var">%s<ul class="vlist">%s</ul></div>' % (vhead, "".join(items)))
        parts.append('</div>')
    parts.append('</article>')
    return "\n".join(parts)


def font_faces():
    """Embed JetBrains Mono (SIL OFL 1.1, see fonts/OFL.txt) so code is monospaced on every printer."""
    import base64
    rules = []
    for fname, weight, style in (("Regular", 400, "normal"), ("Bold", 700, "normal"), ("Italic", 400, "italic")):
        data = base64.b64encode((ROOT / "fonts" / ("JetBrainsMono-%s.woff2" % fname)).read_bytes()).decode()
        rules.append('@font-face { font-family: "JB Mono"; font-weight: %d; font-style: %s; '
                     'src: url(data:font/woff2;base64,%s) format("woff2"); }' % (weight, style, data))
    return "\n".join(rules)


def core_html(c):
    f = c["fields"]
    note = '<div class="cnote">%s</div>' % inline(f["note"][0]) if "note" in f else ""
    return ('<section class="core"><div class="corehead"><span class="corelab">Pattern core</span>%s</div>%s%s</section>'
            % (inline(c["title"]), note, code_html(c["code"])))


def render(plan, problems, cores, sched, review_on, bonus):
    idx_of = {}
    order = []
    for d in plan:
        for q in d["problems"]:
            p = next((x for x in problems if x["num"] == q["n"]), None)
            if p:
                order.append(p)
    for g in BONUS_GROUPS:
        order += [p for p in bonus if p["group"] == g]
    for i, p in enumerate(order, 1):
        idx_of[p["num"]] = i
    nvar = sum(len(p["vars"]) for p in problems)
    ntext = sum(len(p["fields"].get("var", [])) for p in problems)
    days_left = (LAST_STUDY - TODAY).days + 1

    S = []
    S.append(HEAD.replace("/*FONTS*/", font_faces()))
    S.append('<div class="toolbar"><button onclick="window.print()">Print / Save as PDF</button>'
             '<span>A4 · portrait · margins: default · turn ON “Background graphics”</span></div>')

    # ---- cover
    S.append('<section class="page cover">')
    S.append('<div class="kicker">Infosys SP / DSE · 29-day plan · Java</div>')
    S.append('<h1>LeetCode Memory Codebook</h1>')
    S.append('<p class="sub">Every LeetCode problem from the plan as a tested Java solution, with the pattern it teaches, '
             'a one-line memory hook, the lines that matter highlighted, and the variations you should be able to write next.</p>')
    S.append('<div class="today"><div><span class="tl">Printed for</span><b>%s</b></div>'
             '<div><span class="tl">Plan day</span><b>Day %d of 29</b></div>'
             '<div><span class="tl">Study days left</span><b>%d</b> <small>(%s → %s)</small></div>'
             '<div><span class="tl">Exam</span><b>%s</b></div></div>' % (
                 fdate(TODAY, True), plan_day_of(TODAY), days_left, sdate(TODAY), fdate(LAST_STUDY),
                 fdate(EXAM, True)))
    S.append('<div class="stats"><div><b>%d</b><span>plan problems</span></div><div><b>%d</b><span>bonus problems</span></div>'
             '<div><b>%d</b><span>variations with code</span></div><div><b>%d</b><span>more one-line variations</span></div></div>' % (
                 len(problems) - len(bonus), len(bonus), nvar, ntext))
    S.append(HOWTO)
    S.append(LEGEND)
    S.append('</section>')

    # ---- schedule
    S.append('<section class="page"><h2 class="sec">10-day spaced repetition: %s → %s</h2>' % (fdate(TODAY), fdate(FINAL)))
    S.append('<p class="lead">Each card is learned once, reviewed 1 day and 3 days later, and a third time 6–8 days later (whichever of those days is lightest). '
             'Reviews that would land after %s move to the final pass on %s. The dates are printed on every card: tick the box when you do it. '
             'Minutes assume about %d min to learn a card and %.1f min to review one.</p>'
             % (fdate(LAST_STUDY), fdate(FINAL), LEARN_MIN, REVIEW_MIN))
    S.append('<table class="sched"><colgroup><col style="width:21mm"><col style="width:11mm"><col style="width:66mm"><col><col style="width:20mm"><col style="width:13mm"></colgroup><thead><tr><th>Date</th><th>Plan</th><th>Learn (new cards)</th><th>Review</th><th>Load</th><th>Done</th></tr></thead><tbody>')
    for row in sched:
        d = row["date"]
        revs = review_on.get(d, [])
        learn_txt = []
        for u in row["units"]:
            nums = [c["num"] for c in row["cards"] if (c.get("day") == u if not isinstance(u, str) else c.get("group") == u)]
            if nums:
                learn_txt.append('<div><b>%s</b> %s</div>' % (unit_label(u), ", ".join(str(n) for n in nums)))
        rev_units = []
        seen = set()
        for c in revs:
            u = c.get("day") or c.get("group")
            if u not in seen:
                seen.add(u)
                rev_units.append(u)
        rev_txt = ", ".join(unit_label(u) for u in rev_units) or "—"
        mins = len(row["cards"]) * LEARN_MIN + len(revs) * REVIEW_MIN
        today = ' class="today-row"' if d == TODAY else ""
        flag = '<br><span class="todayflag">TODAY</span>' if d == TODAY else ""
        S.append('<tr%s><td><b>%s</b>%s</td><td>Day %d</td><td>%s</td><td>%s <small>(%d cards)</small></td><td>%d new<br>%d review<br><small>about %d min</small></td><td><span class="box big"></span></td></tr>' % (
            today, fdate(d), flag, plan_day_of(d), "".join(learn_txt) or "— no new cards: reviews + plan work", rev_txt, len(revs),
            len(row["cards"]), len(revs), round(mins)))
    S.append('<tr class="final-row"><td><b>%s</b></td><td>Day %d</td><td>— rest day</td><td>Final pass: read the Recall Sheet (last pages) top to bottom, %d cards due. No new code.</td><td>about 45 min</td><td><span class="box big"></span></td></tr>' % (
        fdate(FINAL), plan_day_of(FINAL), len(review_on.get(FINAL, []))))
    S.append('<tr class="exam-row"><td><b>%s</b></td><td>Exam</td><td colspan="4">Infosys assessment. Morning: the Day 1 toolkit core and five hooks you like, nothing else.</td></tr>' % fdate(EXAM))
    S.append('</tbody></table>')
    S.append('<div class="rules"><div><b>Missed a day?</b> Do its learn list the next morning and push its reviews one day later. Never skip a review to learn something new.</div>'
             '<div><b>Got a key line wrong?</b> Circle the card number. Circled cards are reviewed again the next day, whatever the schedule says.</div></div>')
    S.append('</section>')

    # ---- triggers + contents
    S.append('<section class="page"><h2 class="sec">Pattern triggers: read the problem, name the pattern</h2>')
    S.append('<table class="trig"><thead><tr><th>The problem says …</th><th>Reach for</th><th>Cards (LC)</th></tr></thead><tbody>')
    for a, b, c in TRIGGERS:
        S.append('<tr><td>%s</td><td><b>%s</b></td><td>%s</td></tr>' % (html.escape(a), html.escape(b), c))
    S.append('</tbody></table>')
    S.append('</section>')
    S.append('<section class="page"><h2 class="sec">Contents</h2><div class="toc">')
    for d in plan:
        nums = ", ".join(str(q["n"]) for q in d["problems"])
        S.append('<a href="#day%d"><b>Day %d</b> %s <small>LC %s</small></a>' % (d["day"], d["day"], html.escape(d["title"]), nums))
    for g, label in BONUS_GROUPS.items():
        nums = ", ".join(str(p["num"]) for p in bonus if p["group"] == g)
        S.append('<a href="#bonus%s"><b>%s</b> %s <small>LC %s</small></a>' % (g, label.split(" · ")[0], label.split(" · ")[1], nums))
    S.append('<a href="#recall"><b>Recall Sheet</b> every hook on a few pages, for the final pass</a>')
    S.append('</div></section>')

    # ---- day sections
    for d in plan:
        day_date = PLAN_START + dt.timedelta(days=d["day"] - 1)
        cards = [p for p in order if p.get("day") == d["day"]]
        learn = cards[0]["learn"] if cards and "learn" in cards[0] else None
        S.append('<section class="day" id="day%d">' % d["day"])
        S.append('<header class="dayhead"><div class="dnum">Day %d</div><div class="dtxt"><h2>%s</h2><p>%s</p></div>'
                 '<div class="dmeta">plan date %s%s</div></header>' % (
                     d["day"], html.escape(d["title"]), html.escape(d["focus"]), fdate(day_date),
                     ("<br><b>learn these cards %s</b>" % fdate(learn)) if learn else ""))
        for c in cores.get("Day%02d" % d["day"], []):
            S.append(core_html(c))
        for p in cards:
            S.append(card_html(p, idx_of[p["num"]]))
        S.append('</section>')

    for g, label in BONUS_GROUPS.items():
        cards = [p for p in order if p.get("group") == g]
        if not cards:
            continue
        learn = cards[0].get("learn")
        S.append('<section class="day bonus" id="bonus%s">' % g)
        S.append('<header class="dayhead"><div class="dnum">%s</div><div class="dtxt"><h2>%s</h2><p>Extra problems that are not in the plan '
                 'but use patterns Infosys and LeetCode test often. Same card format, same review schedule.</p></div>'
                 '<div class="dmeta">%s</div></header>' % (
                     label.split(" · ")[0], html.escape(label.split(" · ")[1].capitalize()),
                     ("<b>learn these cards %s</b>" % fdate(learn)) if learn else ""))
        for c in cores.get("Bonus%s" % g, []):
            S.append(core_html(c))
        for p in cards:
            S.append(card_html(p, idx_of[p["num"]]))
        S.append('</section>')

    # ---- recall sheet
    S.append('<section class="recall" id="recall"><h2 class="sec">Recall Sheet: say the code from the hook</h2>')
    S.append('<p class="lead">For the final pass (%s) and any spare ten minutes. Cover the right column, read the problem, say the pattern and hook; then check. '
             'Tick when you could also picture the highlighted lines.</p>' % fdate(FINAL))
    S.append('<table class="rec"><thead><tr><th>#</th><th>Problem</th><th>Pattern · hook</th><th></th></tr></thead><tbody>')
    cur = None
    for p in order:
        grp = ("Day %d" % p["day"]) if p.get("day") else BONUS_GROUPS[p["group"]].split(" · ")[0]
        if grp != cur:
            cur = grp
            S.append('<tr class="grp"><td colspan="4">%s</td></tr>' % grp)
        S.append('<tr><td>%d</td><td><b>LC %d</b> %s</td><td><span class="rt">%s</span> “%s”</td><td><span class="box"></span></td></tr>' % (
            idx_of[p["num"]], p["num"], html.escape(p["title"] or ""), inline(p["fields"].get("tag", [""])[0]),
            inline(p["fields"].get("hook", [""])[0])))
    S.append('</tbody></table></section>')
    S.append('<footer class="endnote">Generated from leetcode-codebook/src/*.java by build.py. Every printed solution and variation is compiled and run against '
             'LeetCode\'s examples (and brute-force checks where possible) by run_tests.sh.</footer>')
    S.append("</body></html>")
    return "\n".join(S)


HOWTO = """
<div class="howto">
  <h3>How to memorise with this book</h3>
  <ol>
    <li><b>Learn (once).</b> Read <i>Spot it</i>, say the <i>hook</i> out loud, read the code with the highlighted lines,
        then type it on LeetCode from memory and submit. Tick <span class="box"></span> learn.</li>
    <li><b>Review (on the printed dates).</b> Cover the code with a sheet of paper. From the title and the hook, write only the
        highlighted lines on scrap paper. Uncover and compare. Tick the date box.</li>
    <li><b>Variations.</b> After the main code, read each variation and say what changes. The code for each one is
        printed, so you can check yourself.</li>
    <li><b>Final pass (5 Dec).</b> Only the Recall Sheet at the back. No new problems the day before the exam.</li>
  </ol>
</div>"""

LEGEND = """
<div class="legend">
  <h3>How to read a card</h3>
  <div class="legend-grid">
    <div><span class="num">LC 560</span> LeetCode number, then title, difficulty, plan day and card number (#).</div>
    <div><span class="box"></span><span class="bl">learn Wed 25</span><span class="box"></span><span class="bl">Thu 26</span> your learn and review dates.</div>
    <div><span class="lab">Spot it</span> the words in a problem that should make you think of this pattern.</div>
    <div><span class="hook inline">“The hook”</span> one line to say out loud; it is the whole idea.</div>
    <div><span class="legend-key">highlighted</span> lines are the pattern. Everything else is plumbing you will write anyway.</div>
    <div><span class="lab">Trap</span> the bug most people write the first time.</div>
  </div>
</div>"""

HEAD = r"""<!DOCTYPE html>
<html lang="en"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>LeetCode Memory Codebook · Infosys 29-day plan (Java)</title>
<style>
/*FONTS*/
@page { size: A4 portrait; margin: 11mm 11mm 13mm 11mm;
  @bottom-left { content: "LeetCode Memory Codebook · Infosys 29-day plan"; font: 7pt/1 var(--sans); color: #777; }
  @bottom-right { content: counter(page); font: 8pt/1 var(--sans); color: #555; } }
:root {
  --sans: "Inter", "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", "Segoe UI Symbol", "DejaVu Sans", "JB Mono", sans-serif;
  --mono: "JB Mono", "JetBrains Mono", "Cascadia Mono", "SF Mono", Menlo, Consolas, "DejaVu Sans Mono", "Liberation Mono", monospace;
  --ink: #16181d; --muted: #5b6270; --line: #c9ced6; --soft: #f3f5f8; --accent: #1f4fa3;
  --key: #fff1b8; --keybar: #d39b00;
}
* { box-sizing: border-box; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
pre, code { font-variant-ligatures: none; font-feature-settings: "liga" 0, "calt" 0; }   /* show != and -> as typed */
html { font-size: 9.4pt; }
body { margin: 0; font-family: var(--sans); color: var(--ink); line-height: 1.38; background: #e9ebef; }
h1, h2, h3 { line-height: 1.15; margin: 0; }
code { font-family: var(--mono); font-size: 0.9em; background: var(--soft); border: 0.5pt solid #dde1e7; border-radius: 2px; padding: 0 2px; }
small { color: var(--muted); }

/* screen: look like sheets of paper */
@media screen {
  body { padding: 14px 0 40px; }
  .page, .day, .recall, .endnote { width: 210mm; margin: 0 auto 14px; background: #fff; padding: 11mm; box-shadow: 0 2px 10px rgba(0,0,0,.15); }
  .toolbar { position: sticky; top: 0; z-index: 5; width: 210mm; margin: 0 auto 10px; display: flex; gap: 12px; align-items: center;
    background: #fff; padding: 8px 12px; border-radius: 6px; box-shadow: 0 1px 6px rgba(0,0,0,.15); font-size: 9pt; color: var(--muted); }
  .toolbar button { font: inherit; font-weight: 600; color: #fff; background: var(--accent); border: 0; border-radius: 4px; padding: 6px 12px; cursor: pointer; }
}
@media screen and (max-width: 820px) {
  .page, .day, .recall, .endnote, .toolbar { width: auto; margin-left: 6px; margin-right: 6px; padding: 5mm; }
  pre.code { overflow-x: auto; }
}
@media print {
  body { background: #fff; }
  .toolbar { display: none; }
  .page, .day, .recall { break-before: page; }
  .cover { break-before: auto; }
  a { color: inherit; text-decoration: none; }
}

/* cover */
.cover .kicker { font-size: 9pt; letter-spacing: .12em; text-transform: uppercase; color: var(--accent); font-weight: 700; margin-top: 6mm; }
.cover h1 { font-size: 30pt; letter-spacing: -.01em; margin: 3mm 0 3mm; }
.cover .sub { font-size: 11pt; color: #333; max-width: 165mm; margin: 0 0 6mm; }
.today { display: grid; grid-template-columns: repeat(4, 1fr); border: 1pt solid var(--ink); border-radius: 3mm; overflow: hidden; margin-bottom: 5mm; }
.today > div { padding: 3mm 3.5mm; border-right: 0.6pt solid var(--line); }
.today > div:last-child { border-right: 0; }
.today .tl { display: block; font-size: 7.5pt; text-transform: uppercase; letter-spacing: .08em; color: var(--muted); }
.today b { font-size: 12pt; }
.today > div:first-child { background: #16181d; color: #fff; }
.today > div:first-child .tl { color: #c9ced6; }
.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 3mm; margin-bottom: 6mm; }
.stats > div { background: var(--soft); border-radius: 2mm; padding: 3mm; text-align: center; }
.stats b { display: block; font-size: 18pt; }
.stats span { font-size: 8pt; color: var(--muted); }
.howto, .legend { border: 0.6pt solid var(--line); border-radius: 2mm; padding: 3.5mm 4.5mm; margin-bottom: 4mm; }
.howto h3, .legend h3 { font-size: 11pt; margin-bottom: 2mm; }
.howto ol { margin: 0; padding-left: 5mm; }
.howto li { margin-bottom: 1.4mm; }
.legend-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 2mm 6mm; align-items: center; }
.legend-key { background: var(--key); border-left: 2pt solid var(--keybar); padding: 0 4px; font-family: var(--mono); font-size: 8.5pt; }
.hook.inline { display: inline; margin: 0 1mm 0 0; padding-left: 1.5mm; }

/* sections */
h2.sec { font-size: 15pt; margin: 0 0 3mm; padding-bottom: 1.5mm; border-bottom: 1.2pt solid var(--ink); }
h2.sec.small { font-size: 12pt; margin-top: 6mm; }
.lead { margin: 0 0 3mm; color: #333; }
table { border-collapse: collapse; width: 100%; }
th { text-align: left; font-size: 7.8pt; text-transform: uppercase; letter-spacing: .06em; color: var(--muted); border-bottom: 1pt solid var(--ink); padding: 1.5mm 2mm; }
td { border-bottom: 0.5pt solid var(--line); padding: 1.6mm 2mm; vertical-align: top; }
table.sched { table-layout: fixed; }
table.sched td { font-size: 8.2pt; padding: 1.2mm 1.6mm; line-height: 1.3; }
table.sched th { padding: 1.2mm 1.6mm; }
table.sched td:first-child { white-space: nowrap; }
.today-row td { background: #fff6d6; }
.todayflag { display: inline-block; margin-top: 0.8mm; background: #c62828; color: #fff; font-size: 7pt; font-weight: 800; padding: 0.3mm 1.6mm; border-radius: 1mm; letter-spacing: .06em; }
.final-row td { background: #eef4ff; }
.exam-row td { background: #16181d; color: #fff; }
.rules { display: grid; grid-template-columns: 1fr 1fr; gap: 4mm; margin-top: 4mm; font-size: 8.8pt; }
.rules > div { background: var(--soft); border-radius: 2mm; padding: 3mm; }
table.trig td { font-size: 8.7pt; padding: 1.25mm 2mm; }
.toc { columns: 2; column-gap: 6mm; font-size: 9.4pt; }
.toc a { display: block; break-inside: avoid; color: inherit; text-decoration: none; padding: 1.6mm 0; border-bottom: 0.4pt dotted var(--line); }
.toc small { display: block; }

/* day header */
.dayhead { display: grid; grid-template-columns: auto 1fr auto; gap: 4mm; align-items: center; border: 1.2pt solid var(--ink); border-radius: 2.5mm; padding: 2.5mm 3.5mm; margin-bottom: 3.5mm; }
.dnum { background: var(--ink); color: #fff; font-weight: 800; font-size: 13pt; padding: 1.5mm 3mm; border-radius: 1.5mm; white-space: nowrap; }
.dtxt h2 { font-size: 13pt; }
.dtxt p { margin: 0.8mm 0 0; color: #333; font-size: 8.8pt; }
.dmeta { font-size: 8pt; color: var(--muted); text-align: right; }
.dmeta b { color: var(--ink); }
.bonus .dnum { background: var(--accent); }

/* pattern core */
.core { border: 1pt dashed var(--accent); border-radius: 2mm; padding: 2.5mm 3mm; margin-bottom: 3.5mm; break-inside: avoid; }
.corehead { font-weight: 700; font-size: 10pt; margin-bottom: 1mm; }
.corelab { display: inline-block; font-size: 7pt; text-transform: uppercase; letter-spacing: .08em; color: #fff; background: var(--accent); border-radius: 1mm; padding: 0.3mm 1.5mm; margin-right: 2mm; vertical-align: 1px; }
.cnote { font-size: 8.6pt; color: #333; margin-bottom: 1.5mm; }

/* card */
.card { border: 0.7pt solid #aab1bc; border-radius: 2mm; padding: 2.4mm 3mm 2mm; margin-bottom: 3.5mm; }
.keep { break-inside: avoid; break-after: avoid; }
.chead { display: flex; align-items: baseline; gap: 2.2mm; }
.num { background: var(--ink); color: #fff; font-weight: 800; font-size: 8.6pt; padding: 0.4mm 1.8mm; border-radius: 1mm; white-space: nowrap; }
.ttl { font-weight: 700; font-size: 11pt; flex: 1; }
.diff { font-size: 7.4pt; font-weight: 700; text-transform: uppercase; letter-spacing: .05em; padding: 0.3mm 1.6mm; border-radius: 1mm; border: 0.6pt solid; }
.diff.easy { color: #1b7a3a; } .diff.med { color: #9a6300; } .diff.hard { color: #b3261e; }
.where { font-size: 7.6pt; color: var(--muted); white-space: nowrap; }
.srs { display: flex; flex-wrap: wrap; align-items: center; gap: 0.6mm; margin: 1.2mm 0 1.2mm; font-size: 7.6pt; color: var(--muted); }
.box { display: inline-block; width: 3mm; height: 3mm; border: 0.8pt solid var(--ink); border-radius: 0.5mm; vertical-align: -0.5mm; background: #fff; }
.box.big { width: 4.5mm; height: 4.5mm; }
.bl { margin: 0 2.4mm 0 0.8mm; }
.tag { margin-left: auto; font-weight: 600; color: var(--accent); }
.row { display: grid; grid-template-columns: 13mm 1fr; gap: 1.5mm; margin: 0.6mm 0; }
.lab { font-size: 6.9pt; font-weight: 800; text-transform: uppercase; letter-spacing: .07em; color: var(--muted); padding-top: 0.5mm; }
.trap .lab { color: #b3261e; }
.hook { font-family: Georgia, "Times New Roman", serif; font-style: italic; font-size: 10.6pt; border-left: 2.2pt solid var(--ink); padding: 0.3mm 0 0.3mm 2.5mm; margin: 1.4mm 0 1.6mm; }
.facts { margin-top: 1mm; }

/* code */
pre.code { font-family: var(--mono); font-size: 7.9pt; line-height: 1.32; margin: 1.2mm 0; padding: 1.4mm 0; background: #fafbfc;
  border: 0.5pt solid #dfe3e8; border-radius: 1.2mm; white-space: pre-wrap; break-inside: avoid; }
pre.code.small { font-size: 7.4pt; }
.ln { display: block; padding: 0 2mm 0 4.2mm; position: relative; }
.ln.key { background: var(--key); }
.ln.key::before { content: ""; position: absolute; left: 0.9mm; top: 0; bottom: 0; width: 1.4mm; background: var(--keybar); }
pre .k { color: #0b3d91; font-weight: 700; }
pre .t { color: #6a2c91; }
pre .s { color: #a1260d; }
pre .n { color: #0d6e6e; }
pre .c { color: #5e6b5e; }

/* variations */
.vars { margin-top: 1.6mm; border-top: 0.6pt dashed var(--line); padding-top: 1.2mm; }
.vhead { font-size: 7.2pt; font-weight: 800; text-transform: uppercase; letter-spacing: .08em; color: var(--accent); margin-bottom: 0.8mm; }
.var { break-inside: avoid; margin-bottom: 1.2mm; }
.vt { font-weight: 700; font-size: 9pt; }
.vnote { font-size: 8.4pt; color: #333; }
.vlist { margin: 0.6mm 0 0; padding-left: 4.5mm; font-size: 8.5pt; }
.vlist li { margin-bottom: 0.6mm; break-inside: avoid; }

/* recall sheet */
table.rec td { font-size: 7.9pt; padding: 0.8mm 1.5mm; }
table.rec td:first-child { color: var(--muted); width: 7mm; }
table.rec td:nth-child(2) { width: 62mm; }
table.rec tr { break-inside: avoid; }
table.rec .grp td { background: var(--soft); font-weight: 800; font-size: 8pt; padding-top: 1.2mm; }
.rt { font-weight: 700; color: var(--accent); }
.endnote { font-size: 7.5pt; color: var(--muted); padding-top: 3mm; }
@media screen { .endnote { box-shadow: none; background: transparent; } }
</style></head><body>
"""

if __name__ == "__main__":
    main()
