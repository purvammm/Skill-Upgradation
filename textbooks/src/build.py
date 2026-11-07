#!/usr/bin/env python3
"""Build the two Infosys DSA textbooks as PDF.

    python build.py                 # both books -> ../*.pdf
    python build.py theory          # one book
    python build.py theory --only 2,3 --out /tmp/preview.pdf   # just some chapters
    python build.py --html          # also keep the intermediate HTML in build/

Requires: weasyprint, markdown, pygments, pyyaml (see README.md).
Fonts are downloaded into src/.fonts on the first run.
"""

import argparse
import datetime as dt
import html
import json
import os
import re
import sys
import unicodedata
from pathlib import Path

import yaml

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE / "tools"))

import bookmd  # noqa: E402
import fonts  # noqa: E402

OUT_DIR = HERE.parent
BUILD = HERE / "build"

THEORY_PDF = "Infosys-DSA-Theory-Textbook.pdf"
WORKBOOK_PDF = "Infosys-DSA-Practice-Workbook.pdf"

PLAN = json.loads((HERE / "data/plan.json").read_text())
MOCKS = json.loads((HERE / "data/mocks.json").read_text())
PATTERNS = json.loads((HERE / "data/patterns.json").read_text())
DAY = {d["d"]: d for d in PLAN}

PHASE_NAME = ["Setup", "Phase 1 · Q1/Q2 toolkit", "Phase 2 · Number theory and DP",
              "Phase 3 · Graphs, trees, segment trees", "Phase 4 · Exam simulation"]

PARTS = [
    {"first": 1, "last": 9, "num": "I", "title": "Foundations",
     "sub": "Days 1–9 · Java toolkit, arrays, greedy, binary search, stacks, bits",
     "intro": "Part I builds the tools that solve the first two problems of a typical Infosys paper. "
              "Each chapter takes one family of techniques (prefix sums, windows, sorting and greedy choice, "
              "heaps, binary search, monotonic stacks and bit tricks) and shows when it applies, why it is correct "
              "and what it costs. The part ends with the first timed mock and the brute-force and stress-testing "
              "habits that turn a wrong answer into a found bug."},
    {"first": 10, "last": 16, "num": "II", "title": "Number Theory, Dynamic Programming and First Graphs",
     "sub": "Days 10–16 · gcd and sieves, five chapters of DP, BFS and topological order",
     "intro": "The third problem of an Infosys paper is usually a counting or optimisation question, and its solution "
              "is usually number theory, dynamic programming or both. Part II starts with modular arithmetic and "
              "factorisation, then develops dynamic programming from first principles: states, transitions, "
              "knapsacks, two-string tables, increasing subsequences, intervals and games. Breadth-first search "
              "and topological order close the part so that graphs are familiar before the second mock."},
    {"first": 17, "last": 23, "num": "III", "title": "Graphs, Trees and Range Queries",
     "sub": "Days 17–23 · Dijkstra, union-find, trees, Fenwick and segment trees, bitmask and digit DP",
     "intro": "The official sample paper's hardest material lives here: rooted trees given as parent arrays, "
              "answers needed for every subtree, and arrays that change between queries. Part III covers weighted "
              "shortest paths, union-find, tree recursion without the call stack, lowest common ancestors, tree DP "
              "and rerooting, small-to-large merging, Fenwick trees, segment trees with and without lazy "
              "propagation, and the exponential techniques (backtracking, bitmask DP, digit DP) that earn partial "
              "marks when n is small."},
    {"first": 24, "last": 29, "num": "IV", "title": "Infosys Problems and the Exam",
     "sub": "Days 24–29 · repeat problems, the official sample paper, the exam playbook",
     "intro": "The last part applies everything to Infosys's own material. It solves the most frequently reported "
              "repeat problems, walks through the official sample paper, and turns the mock experience into a "
              "written playbook for the 180 minutes of the real assessment. It ends with consolidation, the "
              "pattern-recognition map and the day before the exam."},
]


def part_of(n):
    for p in PARTS:
        if p["first"] <= n <= p["last"]:
            return p
    return None


def date_long(d):
    x = dt.date.fromisoformat(d["iso"])
    return x.strftime("%A %-d %B %Y")


def date_short(d):
    x = dt.date.fromisoformat(d["iso"])
    return x.strftime("%a %-d %b")


def esc(s):
    return html.escape(str(s), quote=True)


def read_chapter(path):
    text = path.read_text(encoding="utf8")
    meta = {}
    if text.startswith("---"):
        _, fm, text = text.split("---", 2)
        meta = yaml.safe_load(fm) or {}
    return meta, text.strip("\n") + "\n"


def is_external(url):
    return url.startswith("http://") or url.startswith("https://")


def short_url(u):
    u = re.sub(r"^https?://(www\.)?", "", u)
    return u.rstrip("/")


def fix_plan_html(s):
    """Rewrite the plan's internal links for the printed books."""
    s = s.replace('<a href="#java">Section 03</a>', "Theory Chapter 1 (fast input)")
    s = s.replace('<a href="#sample">walkthroughs</a>', "walkthroughs in Theory Chapter 25")
    # Drop every remaining internal (#...) anchor link, keeping its text; leave external links intact.
    s = re.sub(r'<a href="#[^"]*">(.*?)</a>', r"\1", s)
    return s


def problem_list(d, core=None):
    out = []
    for i, p in enumerate(d["probs"]):
        lvl = "core" if p["l"] == 1 else "bonus"
        if core is True and lvl != "core":
            continue
        if core is False and lvl != "bonus":
            continue
        out.append((i, p, lvl))
    return out


def diff_class(p):
    dl = (p.get("diff") or "").lower()
    if dl in ("easy", "medium", "hard"):
        return dl
    if p["cls"] == "cf":
        try:
            r = int(p["diff"])
            return "easy" if r < 1400 else ("medium" if r < 1800 else "hard")
        except ValueError:
            return "medium"
    return "self"


def ptitle(p):
    if p["plat"] == "Self-judge" or p["label"] == "Infosys":
        return p["title"]
    return f'{p["label"]} · {p["title"]}'


# ============================================================================ common pieces
def css_links(book):
    files = ["base.css", "code.css", f"{book}.css"]
    return "\n".join(f'<link rel="stylesheet" href="{(HERE / "style" / f).as_uri()}">' for f in files)


def toc_html(entries, title="Contents"):
    """entries: list of dict(kind, num, title, id, sections=[(num,title,id)])."""
    rows = [f'<section class="toc" id="toc"><h1 class="front-title">{title}</h1>']
    for e in entries:
        k = e["kind"]
        if k == "part":
            rows.append(f'<div class="toc-part"><a href="#{e["id"]}"><span class="tp-num">Part {e["num"]}</span> {esc(e["title"])}</a></div>')
        elif k in ("chapter", "appendix", "front"):
            label = e.get("label", "")
            rows.append(f'<div class="toc-ch toc-{k}"><a href="#{e["id"]}"><span class="tc-num">{label}</span>'
                        f'<span class="tc-title">{esc(e["title"])}</span></a></div>')
            secs = [s for s in e.get("sections", []) if s[0]]
            if secs and e.get("show_sections", True):
                # section titles arrive as rendered HTML text (e.g. &rsquo;), so unescape before escaping
                items = "".join(f'<a href="#{s[2]}"><span class="ts-num">{s[0]}</span> {esc(html.unescape(s[1]))}</a>' for s in secs)
                rows.append(f'<div class="toc-secs">{items}</div>')
    rows.append("</section>")
    return "\n".join(rows)


def index_html(terms):
    """terms: list of (entry, anchor). Groups by entry, sorted, with page numbers."""
    groups = {}
    for entry, anchor in terms:
        key = entry.strip()
        k2 = key.lower()
        groups.setdefault(k2, [key, []])[1].append(anchor)

    def sort_key(k):
        s = unicodedata.normalize("NFKD", k).lower()
        s = re.sub(r"^[^a-z0-9]+", "", s)
        return s
    keys = sorted(groups, key=sort_key)
    out = ['<section class="index" id="book-index"><h1 class="front-title">Index</h1><div class="index-cols">']
    letter = None
    for k in keys:
        name, anchors = groups[k]
        first = sort_key(k)[:1].upper() or "#"
        if first.isdigit():
            first = "0–9"
        if first != letter:
            letter = first
            out.append(f'<div class="ix-letter">{esc(letter)}</div>')
        refs = ", ".join(f'<a class="ixp" href="#{a}"></a>' for a in anchors)
        out.append(f'<div class="ix-entry">{esc(name)}, {refs}</div>')
    out.append("</div></section>")
    return "\n".join(out)


def checkbox(label="", cls=""):
    return f'<span class="cb {cls}"></span>{label}'


# ============================================================================ theory book
def theory_opener(n, meta, d):
    part = part_of(n)
    objs = meta.get("objectives") or []
    obj_html = "".join(f"<li>{bookmd_inline(o)}</li>" for o in objs)
    prereq = meta.get("prereqs")
    pats = d.get("patterns") or []
    pat_txt = ", ".join(esc(p["title"]) for p in pats) if pats else "—"
    kind = {"learn": "Learning day", "mock": "Mock day", "rest": "Rest day"}.get(d["kind"], d["kind"])
    short = meta.get("short") or meta.get("title")
    return f"""
<div class="ch-opener ph{d['phase']}">
  <div class="ch-num-big">{n}</div>
  <div class="ch-kicker">Chapter {n} · Day {d['d']} of 29 · {esc(date_long(d))} · {kind}</div>
  <h1 class="ch-title" data-bm="{n} · {esc(meta.get('title') or d['title'])}">{esc(meta.get('title') or d['title'])}</h1>
  <span class="rh-ch">{n} · {esc(short)}</span>
  <p class="ch-focus">{esc(meta.get('focus') or d['focus'])}</p>
  <div class="ch-meta">
    <div><b>Part {part['num']}</b> · {esc(part['title'])}</div>
    <div><b>Handbook cards</b> · {pat_txt}</div>
    {f'<div><b>Before this chapter</b> · {bookmd_inline(prereq)}</div>' if prereq else ''}
    <div><b>Practice</b> · Workbook Chapter {n} · {len(d['probs'])} problems · core ≈ {d['coreMin'] / 60:.1f} h</div>
  </div>
  {f'<div class="objectives"><div class="obj-title">Learning objectives</div><p>After this chapter you should be able to:</p><ul>{obj_html}</ul></div>' if objs else ''}
</div>"""


def bookmd_inline(s):
    import markdown as _md
    return _md.markdown(str(s), extensions=["smarty"])[3:-4]


def theory_practice_section(n, d):
    rows = []
    for i, p, lvl in problem_list(d):
        title_cell = f'<a href="{esc(p["url"])}">{esc(ptitle(p))}</a>' if is_external(p["url"]) else esc(ptitle(p))
        rows.append(f'<tr><td class="pid">{n}.P{i + 1}</td><td>{title_cell}</td>'
                    f'<td><span class="tag {diff_class(p)}">{esc(p.get("diffLabel") or p.get("diff") or "")}</span></td>'
                    f'<td><span class="tag {lvl}">{lvl}</span></td></tr>')
    return (f'<h2 class="unnumbered practice-head">Problems</h2>'
            f'<p class="practice-note">These are the problems for Day {d["d"]}. Work them in Workbook Chapter {n}, '
            f'which has a worksheet for each one and the hints in its Appendix A. Core problems come first; the bonus '
            f'problems are the Specialist Programmer level.</p>'
            f'<table class="practice"><thead><tr><th>No.</th><th>Problem</th><th>Level</th><th>Set</th></tr></thead>'
            f'<tbody>{"".join(rows)}</tbody></table>')


def key_terms_section(ch):
    seen = []
    for entry, anchor, shown in ch.index_terms:
        if entry.lower() not in [s[0].lower() for s in seen]:
            seen.append((entry, anchor))
    if not seen:
        return ""
    items = "".join(f'<li><a href="#{a}">{esc(e)}</a> <a class="pg" href="#{a}"></a></li>' for e, a in seen)
    return f'<h2 class="unnumbered">Key terms</h2><ul class="keyterms">{items}</ul>'


def split_closers(body_html):
    """Return body with key terms inserted after the Summary section (before Review questions)."""
    return body_html


def build_theory(only=None, keep_html=False, out=None, wb_labels=None):
    labels = bookmd.Labels()
    chapters = []
    files = sorted((HERE / "theory").glob("ch[0-9][0-9].md"))
    metas = {}
    for f in files:
        n = int(f.stem[2:])
        try:
            meta, text = read_chapter(f)
        except Exception as e:          # another author's file may be half-written
            if only and n not in only:
                print(f"  [skip] theory ch{n:02d}: {e}")
                continue
            raise
        metas[n] = (meta, text)
    # pass 1: labels for every chapter (so cross-chapter refs work even with --only)
    objs = {}
    for n, (meta, text) in sorted(metas.items()):
        ch = bookmd.Chapter(text, str(n), "theory", labels)
        try:
            ch.number_labels()
        except Exception as e:
            if only and n not in only:
                print(f"  [skip] theory ch{n:02d}: {e}")
                continue
            raise
        objs[n] = ch
    front_meta, preface = read_chapter(HERE / "theory/front-preface.md")
    pre = bookmd.Chapter(preface, "P", "theory", labels)
    pre.number_labels()
    app_files = sorted((HERE / "theory").glob("app-*.md"))
    apps = []
    for f in app_files:
        m, t = read_chapter(f)
        letter = m.get("letter")
        c = bookmd.Chapter(t, letter, "theory", labels, appendix=True)
        c.number_labels()
        apps.append((m, c))

    toc = []
    parts_html = []
    all_terms = []
    answers = []

    pre_html = pre.render()
    all_terms += [(e, a) for e, a, _ in pre.index_terms]

    current_part = None
    for n in sorted(objs):
        if only and n not in only:
            continue
        meta, _ = metas[n]
        d = DAY[n]
        part = part_of(n)
        if part is not current_part:
            current_part = part
            pid = f"part-{part['num']}"
            toc.append({"kind": "part", "num": part["num"], "title": part["title"], "id": pid})
            parts_html.append(
                f'<section class="part-page ph{DAY[part["first"]]["phase"]}" id="{pid}"><div class="part-num">Part {part["num"]}</div>'
                f'<h1 class="part-title" data-bm="Part {part["num"]} · {esc(part["title"])}">{esc(part["title"])}</h1><div class="part-sub">{esc(part["sub"])}</div>'
                f'<p class="part-intro">{esc(part["intro"])}</p><div class="part-chapters">'
                + "".join(f'<div><span>{k}</span> {esc((metas.get(k, ({}, ""))[0].get("title")) or DAY[k]["title"])}</div>'
                          for k in range(part["first"], part["last"] + 1))
                + "</div></section>")
        ch = objs[n]
        body = ch.render()
        all_terms += [(e, a) for e, a, _ in ch.index_terms]
        if ch.answers:
            answers.append((n, meta.get("title") or d["title"], "".join(ch.answers)))
        kt = key_terms_section(ch)
        # insert key terms before "Review questions" if present, else at the end
        m = re.search(r'<h2[^>]*>Review questions', body)
        if m:
            body = body[:m.start()] + kt + body[m.start():]
        else:
            body += kt
        body += theory_practice_section(n, d)
        cid = f"ch-{n}"
        toc.append({"kind": "chapter", "label": str(n), "title": meta.get("title") or d["title"], "id": cid,
                    "sections": ch.sections})
        parts_html.append(f'<section class="chapter ph{d["phase"]}" id="{cid}">{theory_opener(n, meta, d)}'
                          f'<div class="ch-body">{body}</div></section>')

    # appendices
    app_html = []
    first_app = True
    for m, c in apps:
        letter = m.get("letter")
        aid = f"app-{letter}"
        if m.get("generated") == "answers":
            inner = "".join(f'<h2 class="unnumbered ans-ch" id="ans-{n}">Chapter {n} · {esc(t)}</h2>{a}' for n, t, a in answers)
            body = c.render() + f'<div class="answers">{inner}</div>'
        else:
            body = c.render()
            all_terms += [(e, a) for e, a, _ in c.index_terms]
        toc.append({"kind": "appendix", "label": letter, "title": m["title"], "id": aid, "sections": c.sections,
                    "show_sections": m.get("toc_sections", True)})
        app_html.append(f'<section class="chapter appendix{" first-app" if first_app else ""}" id="{aid}">'
                        f'<div class="app-opener"><div class="ch-kicker">Appendix {letter}</div>'
                        f'<h1 class="ch-title" data-bm="Appendix {letter} · {esc(m["title"])}">{esc(m["title"])}</h1><span class="rh-ch">Appendix {letter} · {esc(m.get("short", m["title"]))}</span></div>'
                        f'<div class="ch-body">{body}</div></section>')
        first_app = False

    toc.append({"kind": "appendix", "label": "", "title": "Index", "id": "book-index", "sections": []})
    front = theory_front(pre_html).replace("{TOC}", toc_html(toc))
    doc = f"""<!doctype html><html lang="en"><head><meta charset="utf-8">
<title>Infosys SP/DSE Coding Assessment · Volume I: Theory</title>
<meta name="author" content="Prepared for Purvam Prajapati">
<meta name="description" content="A 29-chapter Java DSA textbook that follows the 29-day Infosys SP/DSE plan.">
<meta name="keywords" content="Infosys, DSA, Java, algorithms, data structures, textbook">
<meta name="generator" content="textbooks/src/build.py">
{fonts.font_face_css()}
{css_links('theory')}</head>
<body class="book theory">
{front}
<div class="mainmatter">
{''.join(parts_html)}
</div>
{''.join(app_html)}
{index_html(all_terms)}
</body></html>"""
    return write_pdf(doc, out or OUT_DIR / THEORY_PDF, "theory", keep_html), labels


def theory_front(pre_html):
    return f"""
<section class="titlepage">
  <div class="tp-band"></div>
  <div class="tp-series">Infosys SP / DSE coding assessment · 6 December 2026</div>
  <h1 class="tp-title">Data Structures and Algorithms in Java</h1>
  <div class="tp-sub">A 29-chapter textbook for the Infosys SP/DSE coding assessment</div>
  <div class="tp-vol">Volume I · Theory</div>
  <div class="tp-desc">One chapter for each day of the 29-day plan, from the Java toolkit on 7 November to the day before the exam.
  Each chapter teaches the ideas, proves why they work, measures what they cost, and gives the Java you will type.</div>
  <div class="tp-companion">Companion volume: <b>Volume II · Practice Workbook</b> (same chapter numbers)</div>
  <div class="tp-for">Prepared for Purvam Prajapati · Edition of 7 November 2026</div>
</section>
<section class="front colophon">
  <p><b>Data Structures and Algorithms in Java · Volume I: Theory.</b> Edition of 7 November 2026.</p>
  <p>This book expands the 29-day plan (<i>infosys-dsa-29-day-plan.html</i>) and the daily pattern handbook
  (<i>infosys-dsa-daily-handbook.html</i>) of the Skill-Upgradation repository into a textbook. The chapter numbers are the day numbers of the plan.</p>
  <p>Every Java listing was extracted from the book's sources and compiled with <code>javac --release 8</code>; listings that are complete programs were run on their sample inputs.
  Problem statements from Infosys and other sites are paraphrased. Facts about the assessment come from the sources listed in Appendix D.</p>
  <p>Typeset from Markdown with WeasyPrint. Text in Source Serif 4, headings in Source Sans 3, code in JetBrains Mono (all SIL Open Font License).</p>
</section>
{{TOC}}
<section class="front preface" id="preface"><h1 class="front-title">Preface</h1><span class="rh-ch">Preface</span>{pre_html}</section>
"""


# ============================================================================ workbook
def recall_due(n):
    """Patterns first taught on days n-1, n-3, n-7 and n-14 (expanding review intervals)."""
    due = []
    for gap in (1, 3, 7, 14):
        for p in PATTERNS:
            if p["day"] == n - gap:
                due.append((gap, p))
    return due


def wb_plan_block(n, d):
    probs = problem_list(d, core=True)
    rows = []
    rows.append(('Handbook review: recall the patterns listed below from memory', 15))
    if d["kind"] == "learn":
        rows.append((f"Read Theory Chapter {n}; type each new listing once and run it", 40))
    for i, p, _ in probs:
        rows.append((f'{n}.P{i + 1} {esc(ptitle(p))}', p["min"]))
    rows.append(("Review: error log, re-solve one problem from 3 days ago", 20))
    trs = "".join(f'<tr><td class="cbcell"><span class="cb"></span></td><td>{t}</td><td class="min">{m} min</td></tr>' for t, m in rows)
    bonus = problem_list(d, core=False)
    btxt = ""
    if bonus:
        bl = ", ".join(f"{n}.P{i + 1}" for i, _, _ in bonus)
        btxt = f'<tr class="bonus"><td class="cbcell"><span class="cb"></span></td><td>Bonus hour (optional, only if the core is done and you feel sharp): {bl}</td><td class="min">{d["bonusMin"]} min</td></tr>'
    learn = "".join(f"<li>{fix_plan_html(x)}</li>" for x in d["learn"])
    return f"""
<h2 class="unnumbered">Today's plan</h2>
<table class="dayplan"><thead><tr><th></th><th>Block</th><th>Time</th></tr></thead><tbody>{trs}{btxt}</tbody>
<tfoot><tr><td></td><td>Core total (estimate)</td><td class="min">{d['coreMin'] / 60:.1f} h</td></tr></tfoot></table>
<div class="box note"><div class="box-title"><span class="box-label">Notes for the day</span></div><div class="box-body"><ul class="learn">{learn}</ul>
<p class="why"><b>Why it matters for Infosys.</b> {fix_plan_html(d['infy'])}</p></div></div>
<div class="timebar">Start time <span class="blank s"></span> End time <span class="blank s"></span> Energy now (1–5) <span class="blank xs"></span> Hours slept <span class="blank xs"></span></div>
"""


def wb_recall_block(n):
    due = recall_due(n)
    if not due:
        return ""
    rows = "".join(
        f'<tr><td>{esc(p["title"])}</td><td class="c">Ch. {p["day"]}</td><td class="c">{gap} day{"s" if gap > 1 else ""} ago</td>'
        f'<td class="c rate"><span class="cb"></span>Again <span class="cb"></span>Shaky <span class="cb"></span>Solid</td></tr>'
        for gap, p in due)
    return f"""
<h2 class="unnumbered">Spaced review: write these from memory</h2>
<p class="instr">Cover the theory book. For each pattern, write the template on the next blank page or in an editor, then compare
it with the listing in the theory chapter shown and rate yourself honestly. "Again" means you looked; "Solid" means it was right first time.</p>
<table class="recall"><thead><tr><th>Pattern</th><th>Taught</th><th>Interval</th><th>Rating</th></tr></thead><tbody>{rows}</tbody></table>
"""


def wb_problem_sheets(n, d):
    sheets = []
    for i, p, lvl in problem_list(d):
        pid = f"{n}.P{i + 1}"
        dc = diff_class(p)
        url = p["url"]
        hint = f'Appendix A, hint {pid}' if p.get("h") else "no hint: use the theory chapter"
        sheets.append(f"""
<div class="prob {lvl}" id="prob-{n}-{i + 1}">
  <div class="prob-head"><span class="prob-id">{pid}</span><span class="prob-title">{esc(ptitle(p))}</span>
    <span class="tag {dc}">{esc(p.get('diffLabel') or p.get('diff') or '')}</span><span class="tag {lvl}">{lvl}</span>
    <span class="prob-time">{p['min']} min</span></div>
  <div class="prob-url">{f'<a href="{esc(url)}">{esc(short_url(url))}</a> · ' if is_external(url) else 'No online judge · solve in OneCompiler · '}{esc(p['plat'])}</div>
  <table class="prob-fields">
    <tr><th>Pattern</th><td class="w1"></td><th>Target O( )</th><td class="w1"></td></tr>
    <tr><th>Key idea</th><td colspan="3" class="tall"></td></tr>
    <tr><th>Edge cases tested</th><td colspan="3"></td></tr>
  </table>
  <div class="prob-result"><span>Time used <span class="blank xs"></span> min</span>
    <span><span class="cb"></span>Solved alone</span><span><span class="cb"></span>After hint</span>
    <span><span class="cb"></span>After editorial</span><span><span class="cb"></span>Not yet</span>
    <span class="hintref">{hint}</span></div>
</div>""")
    core_n = sum(1 for _, _, l in problem_list(d) if l == "core")
    head = (f'<h2 class="unnumbered">Problem worksheets</h2><p class="instr">Fill the top rows <i>before</i> you type code: name the pattern, '
            f'turn the largest limit into a target complexity (Theory Appendix A), and write the key idea in one sentence. '
            f'Time limits: Easy 20, Medium 40, Hard 60 minutes. When the time is up, open the hint; if still stuck after 10 more minutes, '
            f'read the editorial, close it, and write the solution yourself. {core_n} core problems, '
            f'{len(d["probs"]) - core_n} bonus.</p>')
    return head + "".join(sheets)


def wb_mock_block(n, d):
    m = next((x for x in MOCKS if x["day"] == n), None)
    if not m:
        return ""
    weights = [20, 30, 50] if len(m["probs"]) == 3 else [20, 30, 50, 0]
    rows = []
    for i, p in enumerate(m["probs"]):
        w = weights[i] if i < len(weights) else 0
        wtxt = f"{w}" if w else "partial"
        url = p["url"]
        url_cell = f'<div class="prob-url"><a href="{esc(url)}">{esc(short_url(url))}</a></div>' if is_external(url) else '<div class="prob-url">no online judge</div>'
        rows.append(f'<tr><td class="c">Q{i + 1}</td><td><b>{esc(p["label"])}</b> {esc(p["title"])}{url_cell}</td>'
                    f'<td class="c">{esc(p.get("diff") or "—")}</td><td class="c">{wtxt}</td><td></td><td></td><td></td><td></td></tr>')
    checkpoint = {
        9: "Q1 fully solved; Q2 at least half the tests.",
        16: "Q1 + Q2 full, weighted score ≥ 50/100.",
        23: "Weighted score ≥ 60 and a brute force submitted on every problem.",
        25: "Compare with the walkthroughs in Theory Chapter 25 only after grading yourself.",
        27: "Weighted score ≥ 65.",
    }.get(n, "")
    return f"""
<h2 class="unnumbered">Mock exam: {esc(m['title'])}</h2>
<p class="instr">{esc(m['desc'])} One sitting of 180 minutes, phone in another room, the same rules as the real assessment.</p>
<table class="mock-rules"><tbody>
<tr><td class="cbcell"><span class="cb"></span></td><td><b>Minutes 0–15.</b> Read every problem. For each, write the limits, the idea, its complexity and a brute-force fallback in the planning grid below.</td></tr>
<tr><td class="cbcell"><span class="cb"></span></td><td><b>Solve the problem you are surest of first</b>, which is not always Q1. Test on the samples (Codeforces Custom test or OneCompiler) before submitting.</td></tr>
<tr><td class="cbcell"><span class="cb"></span></td><td><b>By minute 150</b> every problem has a submission, even if it is only a brute force.</td></tr>
<tr><td class="cbcell"><span class="cb"></span></td><td><b>Minutes 165–180.</b> Make sure every submission compiles. Do not touch code that already passes.</td></tr>
</tbody></table>
<h3>Planning grid (first 15 minutes)</h3>
<table class="mock-plan"><thead><tr><th>Q</th><th>Limits</th><th>Idea</th><th>Complexity</th><th>Brute-force fallback</th><th>Order</th></tr></thead>
<tbody>{''.join(f'<tr><td class="c">Q{i + 1}</td><td></td><td></td><td></td><td></td><td></td></tr>' for i in range(len(m['probs'])))}</tbody></table>
<h3>Score sheet</h3>
<table class="mock-score"><thead><tr><th>Q</th><th>Problem</th><th>Rating</th><th>Weight</th><th>Start</th><th>End</th><th>% tests</th><th>Marks</th></tr></thead>
<tbody>{''.join(rows)}</tbody><tfoot><tr><td></td><td colspan="6">Weighted score = Σ weight × % tests passed</td><td></td></tr></tfoot></table>
<p class="checkpoint"><b>SP checkpoint.</b> {esc(checkpoint)}</p>
<h3>Post-mortem (about 60 minutes after the mock)</h3>
<table class="postmortem"><thead><tr><th>Q</th><th>What went wrong</th><th>Cause</th><th>Rule that would have prevented it</th></tr></thead>
<tbody>{''.join(f'<tr><td class="c">Q{i + 1}</td><td></td><td class="causes">misread · idea · overflow · off-by-one · TLE · input · edge · stack · time</td><td></td></tr>' for i in range(len(m['probs'])))}</tbody></table>
"""


def wb_resolve_block(n):
    src = DAY.get(n - 3)
    if not src or n - 3 < 1 or DAY[n]["kind"] == "rest":   # the rest day has no re-solve block
        return ""
    core = problem_list(src, core=True)
    items = "".join(f'<span class="resolve-item"><span class="cb"></span>{n - 3}.P{i + 1} {esc(ptitle(p))}</span>' for i, p, _ in core)
    return f"""
<h2 class="unnumbered">Re-solve from three days ago</h2>
<p class="instr">Pick one problem from Day {n - 3} and solve it again without looking at your old code. If it takes longer than the first time, add it to the error log.</p>
<div class="resolve">{items}</div>"""


def wb_end_block(n, d):
    rows = "".join('<tr><td></td><td></td><td></td><td></td><td></td></tr>' for _ in range(3))
    tomorrow = DAY.get(n + 1)
    nxt = f"Tomorrow: Chapter {n + 1}, {esc(tomorrow['title'])}." if tomorrow else "Tomorrow is the exam."
    return f"""
<h2 class="unnumbered">End of day</h2>
<table class="errlog"><thead><tr><th>Problem</th><th>What went wrong</th><th>Cause</th><th>Rule for next time</th><th>Re-solve on</th></tr></thead><tbody>{rows}</tbody></table>
<div class="write-wrap"><div class="write-prompt">One thing I understand today that I did not understand yesterday:</div><div class="write" style="height:15mm"></div></div>
<div class="write-wrap"><div class="write-prompt">One thing that is still shaky (re-read it in the theory book tomorrow):</div><div class="write" style="height:15mm"></div></div>
<p class="tomorrow">{nxt}</p>
"""


def wb_opener(n, meta, d):
    kind = {"learn": "Learning day", "mock": "Mock day", "rest": "Rest day"}.get(d["kind"], d["kind"])
    return f"""
<div class="ch-opener wb ph{d['phase']}">
  <div class="ch-num-big">{n}</div>
  <div class="ch-kicker">Day {n} of 29 · {esc(date_long(d))} · {kind} · {PHASE_NAME[d['phase']]}</div>
  <h1 class="ch-title" data-bm="{n} · {esc(meta.get('title') or d['title'])}">{esc(meta.get('title') or d['title'])}</h1>
  <span class="rh-ch">Day {n} · {esc(date_short(d))} · {esc(meta.get('short') or d['title'])}</span>
  <p class="ch-focus">{esc(d['focus'])}</p>
  <div class="ch-meta"><div><b>Read first</b> · Theory Chapter {n}</div><div><b>Patterns</b> · {', '.join(esc(p['title']) for p in d['patterns']) or '—'}</div></div>
</div>"""


def build_workbook(theory_labels, only=None, keep_html=False, out=None):
    labels = bookmd.Labels()
    toc = []
    chapters_html = []
    answers = []
    hints = []
    for n in range(1, 30):
        f = HERE / "workbook" / f"ch{n:02d}.md"
        d = DAY[n]
        for i, p, lvl in problem_list(d):
            if p.get("h"):
                hints.append((n, i, p))
        if only and n not in only:
            continue
        meta, text = read_chapter(f) if f.exists() else ({}, "")
        ch = bookmd.Chapter(text, str(n), "workbook", labels, other_labels=theory_labels)
        ch.number_labels()
        body = ch.render() if text.strip() else ""
        if ch.answers:
            answers.append((n, meta.get("title") or d["title"], "".join(ch.answers)))
        parts = {
            "plan": wb_plan_block(n, d),
            "recall": wb_recall_block(n),
            "mock": wb_mock_block(n, d),
            "problems": wb_problem_sheets(n, d) if d["kind"] != "mock" else wb_problem_sheets_mock_note(n, d),
            "resolve": wb_resolve_block(n),
            "end": wb_end_block(n, d),
        }
        # authored body may position blocks with <p>{{name}}</p>; otherwise default order
        used = set()

        def place(m):
            used.add(m.group(1))
            return parts.get(m.group(1), "")
        body = re.sub(r"<p>\{\{(\w+)\}\}</p>", place, body)
        pre = "".join(parts[k] for k in ("plan", "recall", "mock") if k not in used)
        post = "".join(parts[k] for k in ("problems", "resolve", "end") if k not in used)
        if body:
            body = f'<h2 class="unnumbered">Exercises</h2>{body}' if not body.lstrip().startswith("<h2") else body
        cid = f"wch-{n}"
        toc.append({"kind": "chapter", "label": str(n), "title": meta.get("title") or d["title"], "id": cid,
                    "sections": [], "show_sections": False})
        chapters_html.append(f'<section class="chapter ph{d["phase"]}" id="{cid}">{wb_opener(n, meta, d)}'
                             f'<div class="ch-body">{pre}{body}{post}</div></section>')

    app = workbook_appendices(hints, answers)
    for a in app["toc"]:
        toc.append(a)
    front = workbook_front()
    doc = f"""<!doctype html><html lang="en"><head><meta charset="utf-8">
<title>Infosys SP/DSE Coding Assessment · Volume II: Practice Workbook</title>
<meta name="author" content="Prepared for Purvam Prajapati">
<meta name="description" content="Daily worksheets, exercises and mock exams for the 29-day Infosys SP/DSE plan.">
<meta name="generator" content="textbooks/src/build.py">
{fonts.font_face_css()}
{css_links('workbook')}</head>
<body class="book workbook">
{front.replace("{TOC}", toc_html(toc))}
<div class="mainmatter">{''.join(chapters_html)}</div>
{app['html']}
</body></html>"""
    return write_pdf(doc, out or OUT_DIR / WORKBOOK_PDF, "workbook", keep_html)


def wb_problem_sheets_mock_note(n, d):
    return ""


def workbook_front():
    meta, text = read_chapter(HERE / "workbook/front-howto.md")
    labels = bookmd.Labels()
    ch = bookmd.Chapter(text, "H", "workbook", labels)
    ch.number_labels()
    howto = ch.render()
    # progress tracker
    rows = []
    for d in PLAN:
        core = len(problem_list(d, core=True))
        bonus = len(problem_list(d, core=False))
        boxes = "".join('<span class="cb"></span>' for _ in range(core))
        bboxes = "".join('<span class="cb bonus"></span>' for _ in range(bonus))
        rows.append(f'<tr class="ph{d["phase"]}"><td class="c">{d["d"]}</td><td>{esc(date_short(d))}</td><td>{esc(d["title"])}</td>'
                    f'<td class="boxes">{boxes}</td><td class="boxes">{bboxes}</td><td></td></tr>')
    tracker = (f'<section class="front tracker" id="tracker"><h1 class="front-title">Progress tracker</h1><span class="rh-ch">Progress tracker</span>'
               f'<p class="instr">Tick a box for each problem solved (alone or after a hint). Shaded boxes are bonus problems.</p>'
               f'<table class="tracker-t"><thead><tr><th>Day</th><th>Date</th><th>Topic</th><th>Core</th><th>Bonus</th><th>Hours</th></tr></thead>'
               f'<tbody>{"".join(rows)}</tbody></table></section>')
    # pattern recall tracker
    prow = []
    for p in sorted(PATTERNS, key=lambda x: x["day"]):
        cells = "".join(f'<td class="c small">{p["day"] + g if p["day"] + g <= 29 else ""}</td>' for g in (1, 3, 7, 14))
        prow.append(f'<tr><td>{esc(p["title"])}</td><td class="c">{p["day"]}</td>{cells}<td></td></tr>')
    ptrack = (f'<section class="front tracker" id="recall-tracker"><h1 class="front-title">Pattern recall tracker</h1><span class="rh-ch">Pattern recall tracker</span>'
              f'<p class="instr">Each pattern comes back for review 1, 3, 7 and 14 days after it is taught; the day numbers are filled in. '
              f'Write A (again), S (shaky) or G (solid, good) in each cell after the review. Any pattern still at A on Day 26 goes into the repair session.</p>'
              f'<table class="tracker-t recall-t"><thead><tr><th>Pattern</th><th>Taught</th><th>+1</th><th>+3</th><th>+7</th><th>+14</th><th>Notes</th></tr></thead>'
              f'<tbody>{"".join(prow)}</tbody></table></section>')
    return f"""
<section class="titlepage wbtp">
  <div class="tp-band"></div>
  <div class="tp-series">Infosys SP / DSE coding assessment · 6 December 2026</div>
  <h1 class="tp-title">Data Structures and Algorithms in Java</h1>
  <div class="tp-sub">Daily worksheets, exercises and mock exams for the 29-day plan</div>
  <div class="tp-vol">Volume II · Practice Workbook</div>
  <div class="tp-desc">Twenty-nine chapters, one per day. Each has the day's plan, a spaced-review list, exercises that you complete by hand,
  a worksheet for every problem, and an end-of-day error log. Hints and answers are at the back.</div>
  <div class="tp-companion">Read with: <b>Volume I · Theory</b> (same chapter numbers)</div>
  <div class="tp-for">Prepared for Purvam Prajapati · Edition of 7 November 2026</div>
  <div class="tp-name">Name <span class="blank l"></span></div>
</section>
{{TOC}}
<section class="front preface" id="howto"><h1 class="front-title">How to use this workbook</h1><span class="rh-ch">How to use this workbook</span>{howto}</section>
{tracker}
{ptrack}
"""


def workbook_appendices(hints, answers):
    toc = []
    out = []
    # A: hints
    items = []
    cur = None
    for n, i, p in hints:
        if n != cur:
            cur = n
            items.append(f'<h3 class="hint-day">Day {n} · {esc(DAY[n]["title"])}</h3>')
        items.append(f'<div class="hint-item"><span class="hint-id">{n}.P{i + 1}</span> <b>{esc(ptitle(p))}.</b> {esc(p["h"])}</div>')
    out.append(app_section("A", "Hints", "Hints", '<p class="instr">One hint per problem, taken from the 29-day plan. Open it only when the time limit on the worksheet has run out.</p>'
                           + f'<div class="hints">{"".join(items)}</div>'))
    toc.append({"kind": "appendix", "label": "A", "title": "Hints", "id": "app-A", "sections": []})
    # B: answers
    inner = "".join(f'<h2 class="unnumbered ans-ch" id="wans-{n}">Chapter {n} · {esc(t)}</h2>{a}' for n, t, a in answers)
    out.append(app_section("B", "Answer key", "Answer key", '<p class="instr">Answers to the exercises. Check your work after you finish a section, not during it.</p>'
                           + f'<div class="answers">{inner}</div>'))
    toc.append({"kind": "appendix", "label": "B", "title": "Answer key", "id": "app-B", "sections": []})
    # C: error log pages
    rows = "".join('<tr><td></td><td></td><td></td><td></td><td></td><td></td></tr>' for _ in range(22))
    pages = "".join(f'<table class="errlog big"><thead><tr><th>Date</th><th>Problem</th><th>Topic</th><th>Cause</th><th>Rule that prevents it</th><th>Re-solve</th></tr></thead><tbody>{rows}</tbody></table>' for _ in range(3))
    out.append(app_section("C", "Error log", "Error log",
                           '<p class="instr">Every mistake goes here with the rule that would have prevented it. Causes: misread statement, wrong idea, '
                           'overflow / mod, off-by-one, TLE / complexity, input reading, edge case, stack overflow, ran out of time. '
                           'Re-solve each entry three days later.</p>' + pages))
    toc.append({"kind": "appendix", "label": "C", "title": "Error log", "id": "app-C", "sections": []})
    # D: mock summary
    mrows = []
    target = {9: "Q1 full; Q2 ≥ half", 16: "Q1+Q2 full; ≥ 50", 23: "≥ 60; brute force on all", 25: "grade, then walkthroughs", 27: "≥ 65"}
    for m in MOCKS:
        mrows.append(f'<tr><td>{esc(m["title"])}</td><td class="c">{m["day"]}</td><td>{esc(target.get(m["day"], ""))}</td>'
                     + "".join("<td></td>" for _ in range(len(m["probs"])))
                     + ("<td></td>" if len(m["probs"]) == 3 else "")
                     + "<td></td><td></td></tr>")
    out.append(app_section("D", "Mock scores and SP checkpoints", "Mock scores",
                           '<p class="instr">Copy each mock\'s result here so the trend is visible on one page. The checkpoints are estimates of what is on track for the '
                           'Specialist Programmer role, not Infosys\'s published cut-offs (it publishes none).</p>'
                           '<table class="mocksum"><thead><tr><th>Mock</th><th>Day</th><th>On track if</th><th>Q1 %</th><th>Q2 %</th><th>Q3 %</th><th>Q4 %</th><th>Score</th><th>Weakest topic</th></tr></thead>'
                           f'<tbody>{"".join(mrows)}</tbody></table>'
                           '<table class="checkpoints"><thead><tr><th>If you are below the checkpoint after…</th><th>Do this</th></tr></thead><tbody>'
                           '<tr><td>Mock 1 (Day 9)</td><td>Skip bonus problems in week 2 and redo the core problems of your weakest day.</td></tr>'
                           '<tr><td>Mock 2 (Day 16)</td><td>Same, and spend the review block on DP problems from Days 11–14.</td></tr>'
                           '<tr><td>Mock 3 (Day 23)</td><td>Days 24–26: repeats and error-log repair only, no new topics.</td></tr>'
                           '<tr><td>Mock 5 (Day 27)</td><td>On exam day, lock in Q1 and Q2 first and use partial marks on Q3.</td></tr></tbody></table>'))
    toc.append({"kind": "appendix", "label": "D", "title": "Mock scores and SP checkpoints", "id": "app-D", "sections": []})
    # E: notes
    out.append(app_section("E", "Notes and scratch paper", "Notes",
                           '<div class="gridpaper full"></div><div class="pagebreak"></div><div class="gridpaper full"></div>'))
    toc.append({"kind": "appendix", "label": "E", "title": "Notes and scratch paper", "id": "app-E", "sections": []})
    return {"html": "".join(out), "toc": toc}


def app_section(letter, title, short, inner):
    return (f'<section class="chapter appendix" id="app-{letter}"><div class="app-opener"><div class="ch-kicker">Appendix {letter}</div>'
            f'<h1 class="ch-title" data-bm="Appendix {letter} · {esc(title)}">{esc(title)}</h1><span class="rh-ch">Appendix {letter} · {esc(short)}</span></div><div class="ch-body">{inner}</div></section>')


# ============================================================================ output
def write_pdf(doc, out_path, book, keep_html):
    import logging
    from weasyprint import HTML

    class Collect(logging.Handler):
        def __init__(self):
            super().__init__(logging.WARNING)
            self.msgs = []

        def emit(self, record):
            self.msgs.append(record.getMessage())
    col = Collect()
    logging.getLogger("weasyprint").addHandler(col)
    if keep_html:
        BUILD.mkdir(exist_ok=True)
        (BUILD / f"{book}.html").write_text(doc, encoding="utf8")
    pdf = HTML(string=doc, base_url=str(HERE)).write_pdf()
    Path(out_path).write_bytes(pdf)
    logging.getLogger("weasyprint").removeHandler(col)
    seen = set()
    for m in col.msgs:
        if m not in seen:
            seen.add(m)
            print(f"  [weasyprint] {m}")
    return out_path


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("books", nargs="*", default=["theory", "workbook"])
    ap.add_argument("--only", help="comma-separated chapter numbers")
    ap.add_argument("--out", help="output PDF path (single book only)")
    ap.add_argument("--html", action="store_true")
    a = ap.parse_args()
    only = {int(x) for x in a.only.split(",")} if a.only else None
    fonts.ensure()
    theory_labels = None
    if "theory" in a.books:
        path, theory_labels = build_theory(only, a.html, a.out if len(a.books) == 1 else None)
        print("wrote", path)
    if "workbook" in a.books:
        if theory_labels is None:
            theory_labels = collect_theory_labels()
        path = build_workbook(theory_labels, only, a.html, a.out if len(a.books) == 1 else None)
        print("wrote", path)


def collect_theory_labels():
    labels = bookmd.Labels()
    for f in sorted((HERE / "theory").glob("ch[0-9][0-9].md")):
        try:
            meta, text = read_chapter(f)
            bookmd.Chapter(text, str(int(f.stem[2:])), "theory", labels).number_labels()
        except Exception as e:
            print(f"  [warn] theory {f.name}: {e}")
    return labels


if __name__ == "__main__":
    main()
