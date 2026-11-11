"""Build the printable DSA patterns book: content/*.yaml + java/*.java -> HTML -> PDF.

    python build.py                    # full book -> ../dsa-patterns-printable.pdf
    python build.py --draft            # allow missing patterns (shown as placeholders)
    python build.py --only 2,5 --out /tmp/p.pdf   # some parts only (implies --draft)
    python build.py --html-only        # just write build/book.html

Code shown in the book is read from the //@tpl regions of java/*.java, the same
files tools/check_java.py compiles and tests, so the printed code is the tested code.
"""

import argparse
import html
import json
import re
import sys
from pathlib import Path

import yaml
from pygments.lexers import JavaLexer
from pygments.token import Token

import fonts

ROOT = Path(__file__).resolve().parent
CONTENT = ROOT / "content"
JAVA = ROOT / "java"
BUILD = ROOT / "build"
DEFAULT_OUT = ROOT.parent / "dsa-patterns-printable.pdf"

COLORS = ["yellow", "peach", "blue", "green", "pink", "purple", "teal", "grey"]
# light, mid, dark for each colour (kept in step with the variables in book.css)
PALETTE = {
    "yellow": ("#fff4c9", "#ffe08a", "#8a5a00"), "peach": ("#ffe7d6", "#ffc089", "#9a4512"),
    "blue": ("#e0edff", "#8bc4ff", "#1f58a3"), "green": ("#d9f3e5", "#7ed5a3", "#166e41"),
    "pink": ("#ffe3ee", "#ff9ec4", "#a3245a"), "purple": ("#ece6ff", "#b8a6ff", "#5b3ab0"),
    "teal": ("#d7f2ef", "#6ed2c7", "#11665e"), "grey": ("#efedf5", "#c4bed6", "#4f4b62"),
}
TAGS = {"memorise": "memorise", "variant": "variant", "example": "example", "helper": "helper",
        "usage": "usage"}


# --------------------------------------------------------------------------- loading
def load():
    manifest = yaml.safe_load((CONTENT / "manifest.yaml").read_text())
    front = yaml.safe_load((CONTENT / "front.yaml").read_text()) if (CONTENT / "front.yaml").exists() else {}
    lc = {}
    for lc_path in sorted((ROOT / "data").glob("leetcode*.json")):
        lc.update(json.loads(lc_path.read_text()))
    jr_path = ROOT / "data" / "java-results.json"
    jr = json.loads(jr_path.read_text()) if jr_path.exists() else {}
    return manifest, front, lc, jr


def load_parts(manifest, only, draft, all_problems):
    """All parts are loaded (so numbers and cross references are right); `only`
    limits which parts report problems. main() decides what gets rendered."""
    parts = []
    for part in manifest["parts"]:
        problems = all_problems if (not only or part["num"] in only) else []
        files = part["file"] if isinstance(part["file"], list) else [part["file"]]
        data = {"intro": "", "patterns": []}
        for fn in files:
            f = CONTENT / fn
            if not f.exists():
                problems.append(f"missing {fn}")
                continue
            d = yaml.safe_load(f.read_text()) or {}
            data["intro"] = data["intro"] or d.get("intro", "")
            data["patterns"].extend(d.get("patterns", []))
        by_id = {p["id"]: p for p in data.get("patterns", [])}
        extra = set(by_id) - set(part["patterns"])
        if extra:
            problems.append(f"part {part['num']}: ids not in manifest: {sorted(extra)}")
        pats = []
        for i, pid in enumerate(part["patterns"], 1):
            p = by_id.get(pid)
            if p is None:
                problems.append(f"part {part['num']}: pattern '{pid}' not written")
                p = {"id": pid, "title": pid.replace("-", " ").capitalize(), "placeholder": True}
            p = dict(p)
            p["num"] = f"{part['num']}.{i}"
            p["part"] = part["num"]
            pats.append(p)
        parts.append({**part, "intro": data.get("intro", ""), "items": pats})
    return parts


# --------------------------------------------------------------------------- text helpers
def esc(s):
    return html.escape(str(s), quote=True)


class Ctx:
    """Pattern lookup for [[id]] cross references."""
    pats = {}
    missing = set()


def xref(pid, with_title=True):
    p = Ctx.pats.get(pid)
    if p is None:
        Ctx.missing.add(pid)
        return f'<span class="xref-missing">[{esc(pid)}]</span>'
    label = f'<span class="xn">{p["num"]}</span>'
    if with_title:
        label += f" {esc(p['title'])}"
        return f'<a class="xref" href="#p-{pid}">{label}</a>'
    return f'<a class="xref short" href="#p-{pid}">{label}</a>'


def inline(s):
    if s is None:
        return ""
    s = str(s)
    out = []
    for piece in re.split(r"(`[^`]+`)", s):
        if len(piece) >= 2 and piece.startswith("`") and piece.endswith("`"):
            out.append(f"<code>{esc(piece[1:-1])}</code>")
            continue
        t = esc(piece)
        t = re.sub(r"\[\[#([a-z0-9-]+)\]\]", lambda m: xref(m.group(1), with_title=False), t)
        t = re.sub(r"\[\[([a-z0-9-]+)\]\]", lambda m: xref(m.group(1)), t)
        t = re.sub(r"\*\*(.+?)\*\*", r"<b>\1</b>", t)
        t = re.sub(r"(?<![\w*])\*([^*\s](?:[^*]*?[^*\s])?)\*(?![\w*])", r"<i>\1</i>", t)
        out.append(t)
    return "".join(out)


def paras(text):
    if not text:
        return ""
    blocks = [b.strip() for b in re.split(r"\n\s*\n", str(text).strip()) if b.strip()]
    out = []
    for b in blocks:
        lines = b.splitlines()
        if all(l.lstrip().startswith("- ") for l in lines):
            out.append("<ul>" + "".join(f"<li>{inline(l.lstrip()[2:])}</li>" for l in lines) + "</ul>")
        else:
            out.append(f"<p>{inline(' '.join(l.strip() for l in lines))}</p>")
    return "\n".join(out)


def ul(items, cls=""):
    if not items:
        return ""
    return f'<ul class="{cls}">' + "".join(f"<li>{inline(i)}</li>" for i in items) + "</ul>"


# --------------------------------------------------------------------------- java code
_region_cache = {}


def java_regions(file):
    if file not in _region_cache:
        found, cur, buf = {}, None, []
        for line in (JAVA / file).read_text().splitlines():
            s = line.strip()
            if s.startswith("//@tpl "):
                cur, buf = s.split(None, 1)[1].strip(), []
            elif s == "//@end" and cur:
                found[cur] = buf
                cur = None
            elif cur is not None:
                buf.append(line)
        _region_cache[file] = found
    return _region_cache[file]


def region_code(file, region):
    lines = java_regions(file)[region]
    ind = min((len(l) - len(l.lstrip()) for l in lines if l.strip()), default=0)
    lines = [l[ind:].rstrip() for l in lines]
    while lines and not lines[0]:
        lines.pop(0)
    while lines and not lines[-1]:
        lines.pop()
    return "\n".join(lines)


def highlight(code):
    out = []
    for ttype, val in JavaLexer().get_tokens(code):
        cls = None
        if ttype in Token.Comment:
            cls = "c"
        elif ttype in Token.Literal.String or ttype in Token.Literal.String.Char:
            cls = "s"
        elif ttype in Token.Literal.Number:
            cls = "n"
        elif ttype in Token.Keyword.Type:
            cls = "kt"
        elif ttype in Token.Keyword:
            cls = "k"
        elif ttype in Token.Name.Decorator:
            cls = "a"
        elif ttype in Token.Name and val[:1].isupper():
            cls = "t"
        v = esc(val)
        out.append(f'<span class="{cls}">{v}</span>' if cls else v)
    text = "".join(out)
    return text[:-1] if text.endswith("\n") else text


def code_block(c, default_title="Template"):
    code = region_code(c["file"], c["region"])
    title = c.get("title", default_title)
    tag = c.get("tag", "memorise")
    tag_html = f'<span class="ctag t-{esc(tag)}">{esc(TAGS.get(tag, tag))}</span>' if tag else ""
    note = f'<div class="cnote">{inline(c["note"])}</div>' if c.get("note") else ""
    return (f'<figure class="code"><div class="cbar"><span class="mini"><i></i><i></i><i></i></span>'
            f'<span class="ctitle">{inline(title)}</span>{tag_html}</div>'
            f"<pre>{highlight(code)}</pre>{note}</figure>")


# --------------------------------------------------------------------------- svg bits
def folder_svg(color, cls="folder"):
    light, mid, dark = PALETTE[color]
    return (f'<svg class="{cls}" viewBox="0 0 64 48" xmlns="http://www.w3.org/2000/svg">'
            f'<path d="M4 10a5 5 0 0 1 5-5h14l6 6h26a5 5 0 0 1 5 5v24a5 5 0 0 1-5 5H9a5 5 0 0 1-5-5z" '
            f'fill="{mid}" stroke="{dark}" stroke-width="2.4" stroke-linejoin="round"/>'
            f'<path d="M4 18a4 4 0 0 1 4-4h48a4 4 0 0 1 4 4v22a5 5 0 0 1-5 5H9a5 5 0 0 1-5-5z" '
            f'fill="{light}" stroke="{dark}" stroke-width="2.4" stroke-linejoin="round"/>'
            "</svg>")


def box():
    return '<span class="box"></span>'


# --------------------------------------------------------------------------- sections
RUN_MAX = 40   # characters that fit in the menu bar's right-hand slot


def check_run(text, problems=None):
    """The running header (menu bar, top right) comes from the heading's data-run attribute."""
    if len(text) > RUN_MAX and problems is not None:
        problems.append(f"running title too long ({len(text)} > {RUN_MAX}), add 'short': {text}")


def window(title, body, color="grey", wid=None, num=None, right="", cls="", run=None, problems=None):
    idattr = f' id="{wid}"' if wid else ""
    numh = f'<span class="num">{num}</span> ' if num else ""
    run = run or (f"{num} " if num else "") + html.unescape(re.sub(r"<[^>]+>", "", title))
    check_run(run, problems)
    return (f'<section class="win c-{color} {cls}"{idattr}><div class="bar">'
            f'<span class="dots"><i></i><i></i><i></i></span><h2 data-run="{esc(run)}">{numh}{title}</h2>{right}</div>'
            f'<div class="wbody">{body}</div></section>')


def practice_html(p, lc, problems):
    rows = []
    for slug in p.get("practice", []):
        q = lc.get(slug)
        if q is None:
            problems.append(f"{p['id']}: practice slug not checked yet: {slug}")
            rows.append(f'<tr><td class="pb">{box()}</td><td class="pn">LC ?</td><td>{esc(slug)}</td><td></td></tr>')
            continue
        d = q["difficulty"]
        rows.append(f'<tr><td class="pb">{box()}</td><td class="pn">LC {q["id"]}</td>'
                    f'<td class="pt">{esc(q["title"])}</td><td class="pd"><span class="diff d-{d.lower()}">{d}</span></td></tr>')
    if not rows:
        return ""
    return ('<div class="practice"><div class="plbl"><span class="pill pill-purple">Practise</span>'
            '<span class="phint">on LeetCode, in this order</span></div>'
            f'<table class="ptable">{"".join(rows)}</table></div>')


def recall_html():
    steps = ["read it", "+1 day", "+3 days", "+7 days", "+21 days"]
    cells = "".join(f'<span class="rc">{box()}<span>{s}</span></span>' for s in steps)
    return f'<div class="recall"><span class="rl">Write the template from memory</span>{cells}</div>'


def pattern_html(p, color, lc, problems):
    if p.get("placeholder"):
        return window(esc(p["title"]), '<p class="placeholder">Not written yet.</p>', color,
                      f"p-{p['id']}", p["num"])
    b = []
    if p.get("spot"):
        b.append('<div class="call spot"><div class="clbl"><span class="pill pill-yellow">Spot it</span>'
                 f'<span class="ltxt">when the problem looks like this</span></div>{ul(p["spot"])}</div>')
    if p.get("idea"):
        b.append(f'<h3>How it works</h3>{paras(p["idea"])}')
    if p.get("trace"):
        tt = p.get("trace_title", "")
        b.append(f'<div class="trace"><div class="tlbl"><span class="pill pill-grey">Worked example</span>'
                 f'<span class="ttl">{inline(tt)}</span></div><pre>{esc(str(p["trace"]).rstrip())}</pre></div>')
    for c in p.get("code", []):
        b.append(code_block(c))
    if p.get("time") or p.get("space"):
        note = f'<span class="cn">{inline(p.get("cost_note", ""))}</span>' if p.get("cost_note") else ""
        b.append(f'<div class="cost"><span class="pill pill-pink">Cost</span>'
                 f'<span class="cv">Time <b class="o">{esc(p.get("time", ""))}</b></span>'
                 f'<span class="cv">Space <b class="o">{esc(p.get("space", ""))}</b></span>{note}</div>')
    if p.get("variants"):
        b.append('<div class="call variants"><div class="clbl"><span class="pill pill-blue">Variants</span>'
                 f'<span class="ltxt">same idea, small change</span></div>{ul(p["variants"])}</div>')
    if p.get("traps"):
        b.append('<div class="call traps"><div class="clbl"><span class="pill pill-red">Traps</span>'
                 f'<span class="ltxt">bugs people actually write</span></div>{ul(p["traps"])}</div>')
    b.append(practice_html(p, lc, problems))
    b.append(recall_html())
    right = f'<span class="bigo">{esc(p.get("time", ""))}</span>' if p.get("time") else ""
    run = f'{p["num"]} {p.get("short") or p["title"]}'
    return window(esc(p["title"]), "\n".join(b), color, f"p-{p['id']}", p["num"], right, "pattern",
                  run=run, problems=problems)


def part_html(part, lc, problems):
    color = part["color"]
    rows = "".join(
        f'<tr><td class="tn">{p["num"]}</td><td class="tt"><a href="#p-{p["id"]}">{esc(p["title"])}</a>'
        f'{"<div class=sig>" + inline(p["signal"]) + "</div>" if p.get("signal") else ""}</td>'
        f'<td class="to">{esc(p.get("time", ""))}</td>'
        f'<td class="tp">p. <b><a class="pg" href="#p-{p["id"]}"></a></b></td></tr>'
        for p in part["items"])
    run = "Part " + str(part["num"]) + " · " + part["short"]
    head = (f'<section class="part c-{color}" id="part-{part["num"]}"><div class="part-panel">'
            f'<div class="phead">{folder_svg(color)}<div><div class="kicker">Part {part["num"]}</div>'
            f'<h1 data-run="{esc(run)}">{esc(part["title"])}</h1></div></div>'
            f'<p class="pintro">{inline(part.get("intro", ""))}</p>'
            f'<table class="ptoc">{rows}</table></div></section>')
    body = "\n".join(pattern_html(p, color, lc, problems) for p in part["items"])
    return head + body


# --------------------------------------------------------------------------- front matter
def stats(parts, lc, jr):
    pats = [p for part in parts for p in part["items"] if not p.get("placeholder")]
    slugs = {s for p in pats for s in p.get("practice", [])}
    regions = sum(len(p.get("code", [])) for p in pats)
    return {"patterns": len(pats), "regions": regions, "problems": len(slugs),
            "checks": jr.get("checks", 0), "classes": len(jr.get("classes", {}))}


def cover_html(manifest, parts, st, extra_folders):
    folders = []
    for label, href, color in extra_folders:
        folders.append(f'<div class="fold">{folder_svg(color)}<span class="fl">{label}</span>'
                       f'<span class="fp">page <span class="pg-inline" data-h="{href}"></span></span></div>')
    for part in parts:
        folders.append(f'<div class="fold">{folder_svg(part["color"])}'
                       f'<span class="fl">{part["num"]}. {esc(part["short"])}</span>'
                       f'<span class="fp">page <span class="pg-inline" data-h="#part-{part["num"]}"></span></span></div>')
    folders_html = "".join(folders)
    checks = f'{st["checks"]:,}' if st["checks"] else "all"
    dock = "".join(f'<i class="dk c-{c}"></i>' for c in COLORS)
    return f"""
<section class="cover">
  <div class="laptop">
    <div class="screen">
      <div class="menubar-mini"><b>DSA Patterns</b><span>File</span><span>Edit</span><span>View</span><span>Go</span><span>Window</span><span class="mb-r">Daily edition</span></div>
      <div class="spark s1">✦</div><div class="spark s2">★</div><div class="spark s3">♥</div><div class="spark s4">✦</div>
      <div class="cwin">
        <div class="cbar2"><span class="dots"><i></i><i></i><i></i></span><span class="cfile">dsa-patterns-printable.pdf</span></div>
        <div class="cwin-body">
          <div class="ckicker">Java · DSA patterns · printable daily edition</div>
          <div class="ctitle-big">{esc(manifest["title"])}</div>
          <p class="csub">{esc(manifest["subtitle"])}</p>
          <div class="cpills"><span class="pill pill-pink">{st["patterns"]} patterns</span><span class="pill pill-blue">{st["regions"]} Java templates</span><span class="pill pill-green">{checks} test checks passed</span><span class="pill pill-purple">{st["problems"]} LeetCode problems</span></div>
        </div>
      </div>
      <div class="dock">{dock}</div>
    </div>
  </div>
  <div class="laptop-base"></div>
  <div class="folders">{folders_html}</div>
  <div class="startline"><span>Started on <i></i></span><span>Test or goal date <i></i></span><span>Windows done <i></i></span></div>
  <p class="prepared">Prepared for <b>{esc(manifest["for"])}</b> · one window a day, a template from memory, one problem solved</p>
</section>"""


def howto_html(front, st, n_days):
    h = front.get("howto", {})
    fill = {"checks": f"{st['checks']:,}", "days": str(n_days), "patterns": str(st["patterns"])}

    def sub(t):
        return re.sub(r"\{(checks|days|patterns)\}", lambda m: fill[m.group(1)], str(t))
    blocks = []
    blocks.append(f'<div class="lead">{paras(h.get("lead", ""))}</div>')
    if h.get("anatomy"):
        cells = "".join(f'<div class="an"><span class="ann">{i}</span><div><b>{inline(a["name"])}</b> {inline(a["text"])}</div></div>'
                        for i, a in enumerate(h["anatomy"], 1))
        blocks.append(f'<h3>What each window contains</h3><div class="anatomy">{cells}</div>')
    if h.get("routine"):
        steps = "".join(f'<li><b>{inline(r["step"])}</b> {inline(r["text"])}</li>' for r in h["routine"])
        blocks.append(f'<div class="call routine"><div class="clbl"><span class="pill pill-green">Daily routine</span>'
                      f'<span class="ltxt">about 60–90 minutes</span></div><ol>{steps}</ol></div>')
    notes = []
    for n in h.get("notes", []):
        notes.append(f'<div class="note c-{n["color"]}"><div class="clbl"><span class="pill pill-{n["pill"]}">{esc(n["label"])}</span>'
                     f'<b class="ntitle">{inline(n["title"])}</b></div>{paras(sub(n["text"]))}</div>')
    if notes:
        blocks.append(f'<div class="notes">{"".join(notes)}</div>')
    return window("How to use this book", "\n".join(blocks), "grey", "howto", cls="front")


def contents_html(parts, front_items):
    top = "".join(f'<span class="cchip">{esc(label)} <b><span class="pg-inline" data-h="{href}"></span></b></span>'
                  for label, href in front_items)
    cols = []
    for part in parts:
        rows = "".join(f'<tr><td class="tn">{p["num"]}</td><td class="tt"><a href="#p-{p["id"]}">{esc(p["title"])}</a></td>'
                       f'<td class="tp"><a class="pg" href="#p-{p["id"]}"></a></td></tr>' for p in part["items"])
        cols.append(f'<div class="cpart c-{part["color"]}"><div class="cph"><span>Part {part["num"]} · {esc(part["short"])}</span>'
                    f'<a class="pg" href="#part-{part["num"]}"></a></div><table>{rows}</table></div>')
    body = f'<div class="cchips">{top}</div><div class="ccols">{"".join(cols)}</div>'
    return window("Contents", body, "grey", "contents", cls="front contents")


def plan_days(parts, n_days):
    pats = [p for part in parts for p in part["items"]]
    learn_days = max(1, n_days - 2)

    def weight(p):
        w = 1.0
        for c in p.get("code", []):
            try:
                w += region_code(c["file"], c["region"]).count("\n") / 28.0
            except Exception:
                pass
        return w

    ws = [weight(p) for p in pats]
    total = sum(ws)
    days, cur, acc = [], [], 0.0
    for p, w in zip(pats, ws):
        cur.append(p)
        acc += w
        if acc >= total * (len(days) + 1) / learn_days - 1e-9 and len(days) < learn_days - 1:
            days.append(cur)
            cur = []
    if cur:
        days.append(cur)
    while len(days) < learn_days:
        days.append([])
    return days


def num_range(ps):
    if not ps:
        return ""
    if len(ps) == 1:
        return ps[0]["num"]
    # split into runs inside one part
    runs, cur = [], [ps[0]]
    for p in ps[1:]:
        if p["part"] == cur[-1]["part"]:
            cur.append(p)
        else:
            runs.append(cur)
            cur = [p]
    runs.append(cur)
    return ", ".join(r[0]["num"] if len(r) == 1 else f'{r[0]["num"]}–{r[-1]["num"]}' for r in runs)


def plan_html(parts, front, n_days):
    days = plan_days(parts, n_days)
    rows = []
    for d, ps in enumerate(days, 1):
        learn = "".join(f'<span class="pl"><a href="#p-{p["id"]}"><b>{p["num"]}</b> {esc(p["title"])}</a></span>' for p in ps)
        rev = []
        for gap in (1, 3, 7, 14):
            if d - gap >= 1 and days[d - gap - 1]:
                rev.append(f'<span class="rv"><span class="rvd">d{d - gap}</span>&#160;{num_range(days[d - gap - 1])}</span>')
        first = ps[0] if ps else None
        page = f'<a class="pg" href="#p-{first["id"]}"></a>' if first else ""
        rows.append(f'<tr><td class="dn">{d}</td><td class="dd"></td><td class="dl">{learn}</td>'
                    f'<td class="dp">{page}</td><td class="dr">{" · ".join(rev) or "—"}</td><td class="dc">{box()}</td></tr>')
    for extra in front.get("plan_extra", []):
        d = len(rows) + 1
        rows.append(f'<tr class="xtra"><td class="dn">{d}</td><td class="dd"></td><td class="dl" colspan="3">{inline(extra)}</td>'
                    f'<td class="dc">{box()}</td></tr>')
    intro = paras(front.get("plan_intro", ""))
    table = ('<table class="plan"><thead><tr><th>Day</th><th>Date</th><th>Learn (new windows)</th><th>Page</th>'
             f'<th>Rewrite from memory</th><th>Done</th></tr></thead><tbody>{"".join(rows)}</tbody></table>')
    return window(f"The {n_days}-day plan", intro + table, "green", "plan", cls="front")


def which_html(front):
    groups = []
    for g in front.get("which", []):
        rows = []
        for r in g["rows"]:
            tries = " ".join(f'<span class="try">{xref(i)}</span>' for i in r["try"])
            rows.append(f'<tr><td class="wc">{inline(r["clue"])}</td><td class="wt">{tries}</td></tr>')
        groups.append(f'<div class="wg"><div class="wgh">{inline(g["group"])}</div><table class="which">{"".join(rows)}</table></div>')
    intro = paras(front.get("which_intro", ""))
    return window("Which pattern? Read the problem, then this table", intro + "".join(groups), "yellow", "which",
                  cls="front", run="Which pattern?")


def sizes_html(front):
    rows = "".join(f'<tr><td class="sn">{inline(r["n"])}</td><td class="so"><b class="o">{inline(r["o"])}</b></td>'
                   f'<td class="se">{inline(r["eg"])}</td></tr>' for r in front.get("sizes", []))
    intro = paras(front.get("sizes_intro", ""))
    after = paras(front.get("sizes_after", ""))
    table = f'<table class="sizes"><thead><tr><th>n up to</th><th>Aim for</th><th>Typical patterns</th></tr></thead><tbody>{rows}</tbody></table>'
    return window("Read the limits: input size → target complexity", intro + table + after, "pink", "sizes",
                  cls="front", run="Input size → complexity")


def javakit_html(front, problems):
    k = front.get("javakit")
    if not k:
        return ""
    b = [paras(k.get("intro", ""))]
    for c in k.get("code", []):
        try:
            b.append(code_block(c, "Java"))
        except KeyError as e:
            problems.append(f"javakit: {e}")
    return window("The Java kit every template assumes", "\n".join(b), "purple", "javakit", cls="front",
                  run="Java kit")


def checklist_html(front):
    c = front.get("checklist")
    if not c:
        return ""
    steps = "".join(f'<li><b>{inline(r["step"])}</b> {inline(r["text"])}</li>' for r in c["steps"])
    body = (f'{paras(c.get("intro", ""))}<div class="call routine"><div class="clbl"><span class="pill pill-green">Every problem</span>'
            f'<span class="ltxt">in this order</span></div><ol>{steps}</ol></div>'
            '<div class="call variants"><div class="clbl"><span class="pill pill-blue">Stuck?</span>'
            f'<span class="ltxt">questions that unlock most problems</span></div>{ul(c["stuck"])}</div>')
    return window("Before you code: a checklist for any problem", body, "green", "checklist", cls="front",
                  run="Checklist")


def javatraps_html(front):
    k = front.get("javakit") or {}
    if not k.get("traps"):
        return ""
    body = ('<div class="call traps"><div class="clbl"><span class="pill pill-red">Java traps</span>'
            f'<span class="ltxt">each of these has cost people an accepted solution</span></div>{ul(k["traps"])}</div>')
    rows = []
    for g in front.get("collection_costs", []):
        ops = "".join(f'<tr><td class="jo">{inline(o)}</td><td class="jc"><b class="o">{inline(c)}</b></td></tr>'
                      for o, c in g["ops"])
        rows.append(f'<div class="jg"><div class="jgh">{inline(g["name"])}</div><table class="jcost">{ops}</table></div>')
    if rows:
        body += ('<h3>What Java\'s collections cost</h3><div class="jcols">' + "".join(rows) + "</div>"
                 + paras(front.get("collection_costs_after", "")))
    return window("Java traps and collection costs", body, "pink", "javatraps", cls="front", run="Java traps")


# --------------------------------------------------------------------------- back matter
def costs_html(parts):
    rows = ['<thead><tr><th></th><th>Pattern</th><th>Time</th><th>Space</th><th>Page</th></tr></thead><tbody>']
    for part in parts:
        rows.append(f'<tr class="cphrow c-{part["color"]}"><td colspan="5">Part {part["num"]} · {esc(part["title"])}</td></tr>')
        rows.extend(f'<tr class="c-{part["color"]}"><td class="tn">{p["num"]}</td>'
                    f'<td class="tt"><a href="#p-{p["id"]}">{esc(p["title"])}</a></td>'
                    f'<td class="ct">{esc(p.get("time", ""))}</td><td class="cs">{esc(p.get("space", ""))}</td>'
                    f'<td class="tp"><a class="pg" href="#p-{p["id"]}"></a></td></tr>' for p in part["items"])
    rows.append("</tbody>")
    intro = "<p>Time and space of every template, as stated in its window. n is the input size; the window names the other letters.</p>"
    cols = ('<colgroup><col style="width:30pt"><col><col style="width:118pt"><col style="width:84pt">'
            '<col style="width:30pt"></colgroup>')
    return window("Costs at a glance", intro + f'<table class="costs">{cols}{"".join(rows)}</table>', "pink",
                  "costs", cls="back")


def lc_index_html(parts, lc):
    entries = {}
    for part in parts:
        for p in part["items"]:
            for s in p.get("practice", []):
                if s in lc:
                    entries.setdefault(lc[s]["id"], (lc[s], []))[1].append(p)
    items = []
    for num in sorted(entries):
        q, ps = entries[num]
        refs = ", ".join(f'<a href="#p-{p["id"]}">{p["num"]}</a>' for p in ps)
        items.append(f'<div class="lci"><b class="lcn">{num}</b> {esc(q["title"])} <span class="lcr">→ {refs}</span></div>')
    intro = ("<p>Every practice problem in the book, by LeetCode number. Met a problem and "
             "don't know where to start? Look it up here and read that window first.</p>")
    return window("Problem index: LeetCode number → pattern", intro + f'<div class="lccols">{"".join(items)}</div>',
                  "blue", "lcindex", cls="back", run="Problem index")


# --------------------------------------------------------------------------- page assembly
def build_html(manifest, front, parts, lc, jr, problems, preview=False):
    st = stats(parts, lc, jr)
    n_days = manifest.get("plan_days", 30)
    front_items = [("How to use this book", "#howto"), (f"The {n_days}-day plan", "#plan"),
                   ("Which pattern?", "#which"), ("Input size → complexity", "#sizes"),
                   ("Checklist", "#checklist"), ("Java traps", "#javatraps"), ("Java kit", "#javakit"), ("Costs at a glance", "#costs"),
                   ("Problem index", "#lcindex")]
    sections = [
        cover_html(manifest, parts, st, [("Daily plan", "#plan", "grey"), ("Which pattern?", "#which", "yellow")]),
        f'<div class="frontpage">{howto_html(front, st, n_days)}</div>',
        f'<div class="frontpage">{contents_html(parts, front_items)}</div>',
        f'<div class="frontpage">{plan_html(parts, front, n_days)}</div>',
        f'<div class="frontpage">{which_html(front)}</div>',
        f'<div class="frontpage">{sizes_html(front)}</div>',
        f'<div class="frontpage">{checklist_html(front)}</div>',
        f'<div class="frontpage">{javatraps_html(front)}</div>',
        f'<div class="frontpage">{javakit_html(front, problems)}</div>',
    ]
    if preview:          # part previews: just the parts, no front or back matter
        sections = []
    for part in parts:
        sections.append(part_html(part, lc, problems))
    if preview:
        sections.append("")
    sections.append(f'<div class="frontpage">{costs_html(parts)}</div>')
    sections.append(f'<div class="frontpage">{lc_index_html(parts, lc)}</div>')
    sections.append(f'<p class="endnote">♥ Made for {esc(manifest["for"])} · {st["patterns"]} patterns · '
                    f'{st["regions"]} Java templates compiled with javac --release 8 and tested · '
                    f'one window a day ♥</p>')
    css = (ROOT / "book.css").read_text()
    title = manifest["title"]
    return f"""<!doctype html>
<html lang="en"><head><meta charset="utf-8">
<title>{esc(title)}: printable daily edition</title>
<meta name="author" content="Prepared for {esc(manifest['for'])}">
<meta name="description" content="{esc(manifest['subtitle'])} Every template compiled and tested in Java.">
<meta name="keywords" content="DSA, algorithms, patterns, Java, LeetCode, interview">
<style>{fonts.font_face_css()}
{css}</style></head>
<body>
{chr(10).join(sections)}
</body></html>"""


def fix_page_refs(doc_html):
    # <span class="pg-inline" data-h="#x"> -> an <a> whose ::after prints the page number
    return re.sub(r'<span class="pg-inline" data-h="([^"]+)"></span>', r'<a class="pg" href="\1"></a>', doc_html)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--draft", action="store_true")
    ap.add_argument("--only", default="")
    ap.add_argument("--out", default=str(DEFAULT_OUT))
    ap.add_argument("--html-only", action="store_true")
    a = ap.parse_args()
    only = {int(x) for x in a.only.split(",") if x.strip()}
    draft = a.draft or bool(only)

    fonts.ensure()
    manifest, front, lc, jr = load()
    problems = []
    parts = load_parts(manifest, only, draft, problems)
    Ctx.pats = {p["id"]: p for part in parts for p in part["items"]}
    shown = [p for p in parts if not only or p["num"] in only]
    doc = fix_page_refs(build_html(manifest, front, shown, lc, jr, problems, preview=bool(only)))
    for m in sorted(Ctx.missing):
        problems.append(f"unknown cross reference [[{m}]]")
    BUILD.mkdir(exist_ok=True)
    tag = ("-part" + "-".join(str(n) for n in sorted(only))) if only else ""
    (BUILD / f"book{tag}.html").write_text(doc)
    for pr in problems:
        print("PROBLEM", pr)
    if problems and not draft:
        print("Fix the problems above, or pass --draft.")
        sys.exit(1)
    if a.html_only:
        return
    import logging
    from weasyprint import HTML
    logging.getLogger("weasyprint").setLevel(logging.ERROR)
    logging.getLogger("fontTools").setLevel(logging.ERROR)
    pdf = HTML(string=doc, base_url=str(ROOT)).write_pdf(pdf_variant=None)
    Path(a.out).write_bytes(pdf)
    print(f"wrote {a.out} ({len(pdf) / 1e6:.1f} MB)")


if __name__ == "__main__":
    main()
