#!/usr/bin/env python3
"""Build the Anki deck (.apkg) from the same tested Java sources as the printable codebook.

    pip install genanki          # once
    python3 build_apkg.py        # writes ../infosys-leetcode-codebook.apkg

The deck follows the book's 10-day schedule: one subdeck per study day (Wed 25 Nov to
Fri 4 Dec), and inside each day one subdeck per plan day (or bonus group) learned that day.
Every code block in the book (solutions, variations, pattern cores) is on at least one card.
"""
import hashlib
import html
import re
import shutil
import sys
import tempfile
from pathlib import Path

try:
    import genanki
except ImportError:
    sys.exit("genanki is missing: run  pip install genanki  (or use a virtualenv)")

import build
from build import BONUS_GROUPS, FINAL, TOKEN, TRIGGERS, fdate, hl, inline

OUT = build.ROOT.parent / "infosys-leetcode-codebook.apkg"
ROOT_DECK = "LeetCode Codebook · Infosys"
TAG = "codebook"
NVAR, NQUICK = 3, 3          # variation slots per problem note (code variations, one-line variations)
FONTS = [("Regular", "_jbmono-regular.woff2"), ("Bold", "_jbmono-bold.woff2"), ("Italic", "_jbmono-italic.woff2")]


def stable_id(*parts):
    """Same id on every build, so re-importing updates the existing decks and note types."""
    h = int(hashlib.sha1("|".join(map(str, parts)).encode()).hexdigest(), 16)
    return (1 << 30) + h % (1 << 30)


# ---------------------------------------------------------------- code rendering
def split_comment(text):
    """Split one code line into (code, comment). The comment is the first // outside a string."""
    for m in TOKEN.finditer(text):
        if m.group("com"):
            return text[:m.start()].rstrip(), m.group(0)
    return text.rstrip(), ""


def indent_of(text):
    return len(text) - len(text.lstrip(" "))


def ln_open(text, cls):
    """Open a code line. A wrapped line (narrow screens) continues 4 columns past its own indentation."""
    ind = indent_of(text)
    return '<span class="%s"%s>' % (cls, ' style="--h:%dch"' % (ind + 4) if ind else "")


def line(text, mark=None):
    cls = "ln" + (" " + mark if mark else "")
    return '%s%s</span>' % (ln_open(text, cls), hl(text) if text else "&#8203;")


def blank(n, code_len):
    return '<span class="blank" style="width:%dch">%d</span>' % (max(4, min(code_len, 44)), n)


def gap(text, n):
    """A hidden line: same indentation, a numbered blank, and the line's own comment as the hint."""
    code, comment = split_comment(text.strip())
    hint = ' <i class="c">%s</i>' % html.escape(comment, quote=False) if comment else ""
    return '%s%s%s%s</span>' % (ln_open(text, "ln gap"), " " * indent_of(text), blank(n, len(code)), hint)


def pre(rows):
    # tex2jax_ignore keeps Anki's MathJax away from the code
    return '<pre class="code tex2jax_ignore">%s</pre>' % "".join(rows)


def full_code(lines, focus=()):
    """Book code with key lines highlighted; `focus` lines (by index) get the 'was hidden' highlight."""
    return pre(line(t, "focus" if i in focus else ("key" if k else None)) for i, (t, k) in enumerate(lines))


def key_lines_hidden(lines):
    rows, n = [], 0
    for t, k in lines:
        if k:
            n += 1
            rows.append(gap(t, n))
        else:
            rows.append(line(t))
    return pre(rows) if n else ""


# ---------------------------------------------------------------- pattern cores, split into parts
CONTROL = re.compile(r"^\s*(?:if|else|for|while|do|switch|try|catch|finally|return|new|throw)\b")
SIGNATURE = re.compile(r"^\s*(?:(?:public|private|protected|static|final|abstract)\s+)*"
                       r"(?:(?:class|record|interface|enum)\s+\w+.*|[\w\[\]<>,.? ]+?\s+\w+\s*\([^;]*\))\s*\{$")
ONE_LINER = re.compile(r"^(\s*.+?\)\s*\{)(.+)\}$")
CLOSER = re.compile(r"^\s*[})\];]+$")
MAX_HIDDEN = 12   # most lines hidden on one core card


def visible_in_part(code):
    """Lines that stay visible when their part is hidden: comments, signatures and closing braces."""
    if not code.strip() or CLOSER.match(code):
        return True
    return bool(SIGNATURE.match(code)) and not CONTROL.match(code)


def core_parts(lines):
    """Split a core at blank lines. Each part becomes one card with that part's lines hidden."""
    parts, cur = [], []
    for i, (t, _) in enumerate(lines):
        if t.strip():
            cur.append(i)
        elif cur:
            parts.append(cur)
            cur = []
    if cur:
        parts.append(cur)

    def hidden(part):
        out = []
        for i in part:
            code, _ = split_comment(lines[i][0])
            one = ONE_LINER.match(code)
            if one and SIGNATURE.match(one.group(1)) and not CONTROL.match(code):
                out.append(i)
            elif not visible_in_part(code):
                out.append(i)
        return out

    merged = []   # a part with fewer than 2 hidden lines joins the next part (or the previous one at the end)
    carry = []
    for part in parts:
        part = carry + part
        carry = []
        if len(hidden(part)) < 2:
            carry = part
        else:
            merged.append(part)
    if carry:
        if merged:
            merged[-1] += carry
        else:
            merged.append(carry)

    out = []
    for part in merged:   # a long method (no blank lines) is cut at statement starts into cards of about 6-10 lines
        hid = hidden(part)
        if len(hid) <= MAX_HIDDEN:
            out.append((part, hid))
            continue
        top = min(indent_of(lines[i][0]) for i in hid)
        chunks = [[]]
        for i in hid:
            if len(chunks[-1]) >= 6 and indent_of(lines[i][0]) <= top + 4:
                chunks.append([])
            chunks[-1].append(i)
        if len(chunks) > 1 and len(chunks[-1]) < 3:
            last = chunks.pop()
            chunks[-1] += last
        out += [(part, ch) for ch in chunks]
    return out


def core_hidden_code(lines, hide):
    rows, n = [], 0
    for i, (t, _) in enumerate(lines):
        if i not in hide:
            rows.append(line(t))
            continue
        n += 1
        code, comment = split_comment(t)
        one = ONE_LINER.match(code)
        if one and SIGNATURE.match(one.group(1)) and not CONTROL.match(code):   # "static long gcd(long a, long b) { ... }"
            hint = ' <i class="c">%s</i>' % html.escape(comment, quote=False) if comment else ""
            rows.append('%s%s %s %s%s</span>' % (
                ln_open(t, "ln gap"), hl(one.group(1)), blank(n, len(one.group(2).strip())), hl("}"), hint))
        else:
            rows.append(gap(t, n))
    return pre(rows)


# ---------------------------------------------------------------- note types
CSS = r"""
@font-face { font-family: "JB Mono"; src: url("_jbmono-regular.woff2") format("woff2"); font-weight: 400; font-style: normal; }
@font-face { font-family: "JB Mono"; src: url("_jbmono-bold.woff2") format("woff2"); font-weight: 700; font-style: normal; }
@font-face { font-family: "JB Mono"; src: url("_jbmono-italic.woff2") format("woff2"); font-weight: 400; font-style: italic; }
.card {
  --bg: #ffffff; --ink: #16181d; --muted: #5b6270; --line: #c9ced6; --soft: #f3f5f8; --accent: #1f4fa3; --accentbg: #eaf0fb;
  --codebg: #fafbfc; --codeline: #dfe3e8; --key: #fff1b8; --keybar: #d39b00; --focus: #e1ecff; --focusbar: #1f4fa3;
  --gapbg: #e8ebf0; --gapline: #9aa3b2; --chip: #16181d; --chipink: #ffffff;
  --k: #0b3d91; --t: #6a2c91; --s: #a1260d; --n: #0d6e6e; --c: #5e6b5e; --easy: #1b7a3a; --med: #9a6300; --hard: #b3261e;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif;
  font-size: 17px; line-height: 1.45; text-align: left; color: var(--ink); background: var(--bg); margin: 0;
}
.card.nightMode, .card.night_mode, .nightMode .card, .night_mode .card {
  --bg: #1b1d22; --ink: #e6e8ec; --muted: #a3aab6; --line: #3a3f48; --soft: #2a2e35; --accent: #8fb4ff; --accentbg: #25324a;
  --codebg: #22252b; --codeline: #353a43; --key: #4a3f12; --keybar: #e0b13a; --focus: #1f3150; --focusbar: #8fb4ff;
  --gapbg: #30353e; --gapline: #6b7382; --chip: #e6e8ec; --chipink: #16181d;
  --k: #8fb4ff; --t: #d7a6f5; --s: #f2a68f; --n: #6fd6cf; --c: #95a395; --easy: #6fcf8a; --med: #e8b960; --hard: #f08a82;
}
.lcb { max-width: 880px; margin: 0 auto; padding: 14px 16px 24px; }
.top { display: flex; flex-wrap: wrap; align-items: center; gap: 6px 8px; font-size: 13px; }
.lc { background: var(--chip); color: var(--chipink); font-weight: 800; padding: 2px 8px; border-radius: 4px; white-space: nowrap; }
.lc.core { background: var(--accent); color: var(--bg); }
.lc.trig { background: var(--t); color: var(--bg); }
.diff { font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: .05em; padding: 1px 6px; border: 1px solid; border-radius: 4px; }
.d-Easy { color: var(--easy); } .d-Medium { color: var(--med); } .d-Hard { color: var(--hard); }
.where { color: var(--muted); }
.title { font-size: 22px; font-weight: 700; line-height: 1.2; margin: 8px 0 2px; }
.mode { font-size: 11.5px; font-weight: 800; letter-spacing: .09em; text-transform: uppercase; color: var(--accent); margin: 4px 0 10px; }
.row { display: grid; grid-template-columns: 84px 1fr; gap: 8px; margin: 7px 0; }
.lab { font-size: 11px; font-weight: 800; letter-spacing: .07em; text-transform: uppercase; color: var(--muted); padding-top: 3px; }
.trap .lab { color: var(--hard); }
.pattern { display: inline-block; font-weight: 700; color: var(--accent); background: var(--accentbg); border-radius: 4px; padding: 2px 9px; margin: 2px 0 4px; }
.pattern.big { font-size: 20px; padding: 4px 12px; }
.hook { font-family: Georgia, "Times New Roman", serif; font-style: italic; font-size: 19px; border-left: 3px solid var(--ink); padding: 2px 0 2px 11px; margin: 10px 0; }
.vtitle { font-size: 19px; font-weight: 700; margin: 4px 0 6px; }
.says { font-size: 20px; font-weight: 600; margin: 6px 0; }
.ctx { color: var(--muted); font-size: 15px; }
.idea { margin: 6px 0 8px; }
.ask { color: var(--muted); font-size: 14px; margin-top: 14px; }
.back .ask { display: none; }
hr#answer { border: 0; border-top: 1px dashed var(--line); margin: 14px 0; }
.foot { margin-top: 14px; font-size: 12.5px; color: var(--muted); }
.foot a, .lcb a { color: var(--accent); }
code, pre.code { font-family: "JB Mono", "JetBrains Mono", "SF Mono", Menlo, Consolas, "DejaVu Sans Mono", monospace;
  font-variant-ligatures: none; font-feature-settings: "liga" 0, "calt" 0; }
code { font-size: .88em; background: var(--soft); border: 1px solid var(--codeline); border-radius: 3px; padding: 0 3px; }
pre.code { font-size: 14px; line-height: 1.45; background: var(--codebg); border: 1px solid var(--codeline); border-radius: 6px;
  padding: 8px 0; margin: 10px 0; white-space: pre-wrap; overflow-wrap: anywhere; tab-size: 4; }
.ln { --pad: 16px; display: block; position: relative; padding: 0 10px 0 calc(var(--pad) + var(--h, 4ch));
  text-indent: calc(0px - var(--h, 4ch)); }
.ln.key { background: var(--key); }
.ln.focus { background: var(--focus); }
.ln.key::before, .ln.focus::before { content: ""; position: absolute; left: 4px; top: 0; bottom: 0; width: 5px; background: var(--keybar); }
.ln.focus::before { background: var(--focusbar); }
.blank { display: inline-block; max-width: 78%; text-indent: 0; vertical-align: -1px; text-align: center; font-size: 11px; line-height: 1.35;
  color: var(--muted); background: var(--gapbg); border: 1px dashed var(--gapline); border-radius: 3px; }
pre .k { color: var(--k); font-weight: 700; }
pre .t { color: var(--t); }
pre .s { color: var(--s); }
pre .n { color: var(--n); }
pre .c { color: var(--c); }
@media (max-width: 600px) {
  .card { font-size: 16px; }
  .lcb { padding: 10px 10px 20px; }
  .title { font-size: 19px; }
  .hook, .vtitle, .says { font-size: 17px; }
  .row { grid-template-columns: 1fr; gap: 0; }
  pre.code { font-size: 12.5px; }
  .ln { --pad: 12px; padding-right: 6px; }
}
"""


def row(label, value, extra=""):
    return '<div class="row%s"><span class="lab">%s</span><span>%s</span></div>' % (extra, label, value)


def p_head(mode):
    return ('<div class="top"><span class="lc">LC {{LC}}</span>'
            '<span class="diff d-{{Difficulty}}">{{Difficulty}}</span>'
            '<span class="where">{{Where}} · book card {{Book}}</span></div>'
            '<div class="title">{{Title}}</div><div class="mode">%s</div>' % mode)


P_FOOT = ('<div class="foot">{{#Slug}}<a href="https://leetcode.com/problems/{{Slug}}/">Open on LeetCode</a> · {{/Slug}}'
          'Book schedule: {{Schedule}}</div>')
HOOK = '<div class="hook">“{{Hook}}”</div>'
CTX = '<div class="ctx">Main solution: <b>{{Pattern}}</b> · “{{Hook}}”</div>'


def ask(text):
    return '<div class="ask">%s</div>' % text


def card(q, a):
    return '<div class="lcb">%s</div>' % q, '<div class="lcb back">%s</div>' % a


def problem_model():
    fields = ["ID", "LC", "Title", "Difficulty", "Where", "Book", "Pattern", "SpotIt", "Hook", "PlanHint",
              "Code", "KeyLinesHidden", "Cost", "Trap", "Slug", "Schedule"]
    for k in range(1, NVAR + 1):
        fields += ["Var%dTitle" % k, "Var%dIdea" % k, "Var%dCode" % k]
    for k in range(1, NQUICK + 1):
        fields += ["Quick%dName" % k, "Quick%dChange" % k]

    spot = row("Spot it", "{{SpotIt}}")
    templates = []

    head = p_head("Spot the pattern")
    q, a = card(head + spot + ask("Name the pattern and say the hook out loud, then flip."),
                head + spot + '<hr id="answer"><div class="pattern">{{Pattern}}</div>' + HOOK
                + "{{#PlanHint}}" + row("Plan hint", "{{PlanHint}}") + "{{/PlanHint}}"
                + row("Cost", "{{Cost}}") + P_FOOT)
    templates.append({"name": "1 Spot the pattern", "qfmt": q, "afmt": a})

    head = p_head("Write the code")
    front = head + '<div class="pattern">{{Pattern}}</div>' + spot + HOOK
    q, a = card(front + ask("Write the whole solution from memory (on paper or in LeetCode), then flip to check."),
                front + '<hr id="answer">{{Code}}' + row("Cost", "{{Cost}}") + row("Trap", "{{Trap}}", " trap") + P_FOOT)
    templates.append({"name": "2 Write the code", "qfmt": q, "afmt": a})

    head = p_head("Fill the highlighted lines")
    q, a = card(head + HOOK + "{{KeyLinesHidden}}" + ask("Say or write each missing line. A grey comment after a blank is the book's hint."),
                head + HOOK + '<hr id="answer">{{Code}}' + row("Trap", "{{Trap}}", " trap") + P_FOOT)
    templates.append({"name": "3 Fill the key lines",
                      "qfmt": "{{#KeyLinesHidden}}%s{{/KeyLinesHidden}}" % q, "afmt": a})

    for k in range(1, NVAR + 1):
        head = p_head("Variation")
        front = head + '<div class="vtitle">{{Var%dTitle}}</div>' % k + CTX
        q, a = card(front + ask("What changes from the main solution? Write the new version, then flip."),
                    front + '<hr id="answer"><div class="idea">{{Var%dIdea}}</div>{{Var%dCode}}' % (k, k) + P_FOOT)
        templates.append({"name": "%d Variation %d" % (3 + k, k),
                          "qfmt": "{{#Var%dCode}}%s{{/Var%dCode}}" % (k, q, k), "afmt": a})

    for k in range(1, NQUICK + 1):
        head = p_head("Quick variation")
        front = head + '<div class="vtitle">{{Quick%dName}}</div>' % k + CTX
        q, a = card(front + ask("What changes from the main solution? Say it in one line, then flip."),
                    front + '<hr id="answer"><div class="idea">{{Quick%dChange}}</div>' % k + P_FOOT)
        templates.append({"name": "%d Quick variation %d" % (3 + NVAR + k, k),
                          "qfmt": "{{#Quick%dName}}%s{{/Quick%dName}}" % (k, q, k), "afmt": a})

    return genanki.Model(stable_id("model", "problem"), "LeetCode Codebook · Problem",
                         fields=[{"name": f} for f in fields], templates=templates, css=CSS)


def core_model():
    head = ('<div class="top"><span class="lc core">Pattern core</span><span class="where">{{Where}}</span></div>'
            '<div class="title">{{Core}}</div><div class="mode">Fill the hidden lines · {{Part}}</div>')
    q, a = card(head + "{{Hidden}}" + ask("Write the hidden lines (the rest of the template is shown), then flip."),
                head + '<hr id="answer"><div class="idea">{{Idea}}</div>{{Code}}'
                '<div class="foot">Blue lines were hidden. Book schedule: {{Schedule}}</div>')
    return genanki.Model(stable_id("model", "core"), "LeetCode Codebook · Pattern core",
                         fields=[{"name": f} for f in ["ID", "Core", "Where", "Part", "Hidden", "Code", "Idea", "Schedule"]],
                         templates=[{"name": "Fill the core", "qfmt": q, "afmt": a}], css=CSS)


def trigger_model():
    head = ('<div class="top"><span class="lc trig">Pattern trigger</span><span class="where">{{Where}}</span></div>'
            '<div class="mode">The problem says …</div><div class="says">{{Says}}</div>')
    q, a = card(head + ask("Which pattern do you reach for?"),
                head + '<hr id="answer"><div class="pattern big">{{ReachFor}}</div>' + row("Cards", "{{Cards}}"))
    return genanki.Model(stable_id("model", "trigger"), "LeetCode Codebook · Pattern trigger",
                         fields=[{"name": f} for f in ["ID", "Says", "ReachFor", "Cards", "Where"]],
                         templates=[{"name": "Name the pattern", "qfmt": q, "afmt": a}], css=CSS)


# ---------------------------------------------------------------- build
def schedule_text(c):
    revs = ["final pass %s" % fdate(r) if r == FINAL else fdate(r) for r in c["reviews"]]
    return "learn %s · review %s" % (fdate(c["learn"]), ", ".join(revs))


def schedule_tags(c):
    return (["%s::learn::%s" % (TAG, c["learn"].isoformat())]
            + ["%s::review::%s%s" % (TAG, "final_" if r == FINAL else "", r.isoformat()) for r in c["reviews"]])


def unit_title(u, plan_by_day):
    if isinstance(u, str):
        return "Bonus %s · %s" % (u, BONUS_GROUPS[u].split(" · ")[1])
    return "Plan Day %d · %s" % (u, plan_by_day[u]["title"])


def main():
    plan, problems, cores, sched, review_on, bonus = build.load_book()
    plan_by_day = {d["day"]: d for d in plan}
    hint_of = {q["n"]: q.get("hint", "") for d in plan for q in d["problems"]}
    order = build.book_order(plan, problems, bonus)
    book_no = {p["num"]: i for i, p in enumerate(order, 1)}
    by_num = {p["num"]: p for p in problems}

    too_many = [p["num"] for p in problems if len(p["vars"]) > NVAR or len(p["fields"].get("var", [])) > NQUICK]
    if too_many:
        sys.exit("raise NVAR / NQUICK: too many variations on LC " + ", ".join(map(str, too_many)))

    pm, cm, tm = problem_model(), core_model(), trigger_model()
    decks = [genanki.Deck(stable_id("deck", ROOT_DECK), ROOT_DECK)]
    due = 0
    counts = {"problem": 0, "var": 0, "quick": 0, "core_part": 0, "trigger": 0}
    day_rows = []
    hidden_once = {}   # core id -> set of line indexes hidden on some card (coverage check)

    for di, srow in enumerate(sched, 1):
        day_name = "%s::Day %02d · %s" % (ROOT_DECK, di, fdate(srow["date"]))
        day_deck = genanki.Deck(stable_id("deck", day_name), day_name)
        decks.append(day_deck)
        day_cards, unit_lines = 0, []
        for ui, u in enumerate(srow["units"], 1):
            label = unit_title(u, plan_by_day)
            deck = genanki.Deck(stable_id("deck", day_name, label), "%s::%d · %s" % (day_name, ui, label))
            decks.append(deck)
            stem = "Bonus%s" % u if isinstance(u, str) else "Day%02d" % u
            unit_cards = [p for p in order if (p.get("group") == u if isinstance(u, str) else p.get("day") == u)]
            where = label
            n_cards = 0

            for ci, c in enumerate(cores.get(stem, []), 1):
                parts = core_parts(c["code"])
                key = (stem, ci)
                hidden_once[key] = set()
                sched_from = unit_cards[0]
                for pi, (part, hide) in enumerate(parts, 1):
                    hidden_once[key].update(hide)
                    due += 1
                    deck.add_note(genanki.Note(
                        model=cm, due=due,
                        guid=genanki.guid_for(TAG, "core", stem, ci, pi),
                        fields=["Core %s.%d · part %d of %d" % (stem, ci, pi, len(parts)), inline(c["title"]), where,
                                "part %d of %d" % (pi, len(parts)), core_hidden_code(c["code"], set(hide)),
                                full_code(c["code"], focus=set(hide)), inline(c["fields"].get("note", [""])[0]),
                                schedule_text(sched_from)],
                        tags=["%s::type::core" % TAG, "%s::day::%02d" % (TAG, di), "%s::plan::%s" % (TAG, stem.lower())]
                        + schedule_tags(sched_from)))
                    counts["core_part"] += 1
                    n_cards += 1

            for p in unit_cards:
                f = p["fields"]
                code_vars = [(inline(v["title"]), inline(v["fields"].get("note", [""])[0]), full_code(v["code"]))
                             for v in p["vars"]]
                quick = [(inline(t.partition("|")[0].strip()), inline(t.partition("|")[2].strip())) for t in f.get("var", [])]
                fields = ["LC %d · %s" % (p["num"], p["title"]), str(p["num"]), html.escape(p["title"]), p["diff"],
                          where, "#%d" % book_no[p["num"]], inline(f.get("tag", [""])[0]), inline(f["cue"][0]),
                          inline(f["hook"][0]), inline(hint_of.get(p["num"], "")), full_code(p["code"]),
                          key_lines_hidden(p["code"]), inline(f["time"][0]),
                          "<br>".join(inline(t) for t in f.get("trap", [])), p.get("slug") or "", schedule_text(p)]
                for k in range(NVAR):
                    fields += list(code_vars[k]) if k < len(code_vars) else ["", "", ""]
                for k in range(NQUICK):
                    fields += list(quick[k]) if k < len(quick) else ["", ""]
                due += 1
                deck.add_note(genanki.Note(
                    model=pm, fields=fields, due=due, guid=genanki.guid_for(TAG, "LC", p["num"]),
                    tags=["%s::type::problem" % TAG, "%s::day::%02d" % (TAG, di), "%s::lc::%d" % (TAG, p["num"]),
                          "%s::plan::%s" % (TAG, stem.lower()), "%s::difficulty::%s" % (TAG, p["diff"].lower())]
                    + schedule_tags(p)))
                counts["problem"] += 1
                counts["var"] += len(code_vars)
                counts["quick"] += len(quick)
                n_cards += 3 + len(code_vars) + len(quick)
            unit_lines.append("<li>%s: %d problems, %d cards</li>" % (html.escape(label), len(unit_cards), n_cards))
            day_cards += n_cards

        if di == len(sched):   # the book's last study day has no new problems: learn the pattern triggers
            label = "Pattern triggers"
            deck = genanki.Deck(stable_id("deck", day_name, label), "%s::1 · %s" % (day_name, label))
            decks.append(deck)
            for ti, (says, reach, nums) in enumerate(TRIGGERS, 1):
                refs = []
                for n in (int(x) for x in nums.split(",")):
                    p = by_num.get(n)
                    if p:
                        refs.append("LC %d %s <small>(%s)</small>" % (n, html.escape(p["title"]), unit_title(
                            p["day"] if p.get("day") else p["group"], plan_by_day).split(" · ")[0]))
                    else:
                        parent = next((q for q in problems if re.search(r"\bLC\s*%d\b" % n, " ".join(
                            [v["title"] for v in q["vars"]] + q["fields"].get("var", [])))), None)
                        refs.append("LC %d <small>(variation on LC %d)</small>" % (n, parent["num"]) if parent else "LC %d" % n)
                due += 1
                deck.add_note(genanki.Note(
                    model=tm, due=due, guid=genanki.guid_for(TAG, "trigger", says),
                    fields=["Trigger %02d" % ti, html.escape(says), html.escape(reach), "<br>".join(refs),
                            "%d of %d" % (ti, len(TRIGGERS))],
                    tags=["%s::type::trigger" % TAG, "%s::day::%02d" % (TAG, di)]))
                counts["trigger"] += 1
            unit_lines.append("<li>Pattern triggers: %d cards (read the problem, name the pattern)</li>" % len(TRIGGERS))
            day_cards += len(TRIGGERS)

        revs = len(review_on.get(srow["date"], []))
        day_deck.description = (
            "<b>%s 2026</b> · plan day %d<br>New today: %d cards<ul>%s</ul>"
            "The book reviews %d earlier problems today; Anki shows its own reviews from earlier days automatically." % (
                fdate(srow["date"]), build.plan_day_of(srow["date"]), day_cards, "".join(unit_lines), revs))
        day_rows.append((di, srow["date"], day_cards, revs))

    # every core line that is not a comment, signature or brace is hidden on at least one card
    for (stem, ci), hid in hidden_once.items():
        c = cores[stem][ci - 1]
        must = {i for i, (t, _) in enumerate(c["code"]) if not visible_in_part(split_comment(t)[0])}
        if must - hid:
            sys.exit("core %s.%d: lines never hidden: %s" % (stem, ci, sorted(must - hid)))
    nvars = sum(len(p["vars"]) for p in problems)
    nquick = sum(len(p["fields"].get("var", [])) for p in problems)
    assert counts["problem"] == len(problems) and counts["var"] == nvars and counts["quick"] == nquick, counts
    total = counts["problem"] * 3 + counts["var"] + counts["quick"] + counts["core_part"] + counts["trigger"]

    decks[0].description = (
        "<b>LeetCode Memory Codebook · Infosys 29-day plan (Java)</b><br>"
        "%d cards: %d problems × 3 (spot the pattern, write the code, fill the highlighted lines), "
        "%d variations with code, %d quick variations, %d pattern-core parts, %d pattern triggers.<br><br>"
        "<b>Use:</b> study <i>Day 01</i> on Wed 25 Nov, <i>Day 02</i> on Thu 26 Nov, and so on to <i>Day 10</i> on Fri 4 Dec. "
        "Each day deck holds that day's new cards; Anki brings back earlier cards as reviews.<br>"
        "<b>Settings:</b> Options → New cards/day 200 and Maximum reviews/day 9999, saved to all subdecks "
        "(the default 20/day would hold most cards back).<br>"
        "<b>Missed a day?</b> Do that day deck the next morning. Never skip reviews to learn new cards.<br>"
        "<b>Final pass, Sat 5 Dec:</b> Tools → Create Filtered Deck, search "
        "<code>\"deck:%s\" \"card:1 Spot the pattern\"</code>, limit 200. That is the book's Recall Sheet."
        % (total, counts["problem"], counts["var"], counts["quick"], counts["core_part"], counts["trigger"], ROOT_DECK))

    with tempfile.TemporaryDirectory() as tmp:
        media = []
        for src, name in FONTS:
            dst = Path(tmp) / name
            shutil.copy(build.ROOT / "fonts" / ("JetBrainsMono-%s.woff2" % src), dst)
            media.append(str(dst))
        lic = Path(tmp) / "_jbmono-OFL.txt"
        shutil.copy(build.ROOT / "fonts" / "OFL.txt", lic)
        media.append(str(lic))
        genanki.Package(decks, media_files=media).write_to_file(str(OUT))

    print("wrote %s: %d cards from %d notes" % (OUT.name, total, counts["problem"] + counts["core_part"] + counts["trigger"]))
    print("  %d problems × 3, %d variations, %d quick variations, %d core parts, %d triggers" % (
        counts["problem"], counts["var"], counts["quick"], counts["core_part"], counts["trigger"]))
    for di, d, n, r in day_rows:
        print("  Day %02d %s  new %3d  (book reviews %2d)" % (di, d, n, r))


if __name__ == "__main__":
    main()
