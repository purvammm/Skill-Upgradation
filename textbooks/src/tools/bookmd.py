"""Markdown dialect for the two textbooks.

On top of Python-Markdown (tables, attr_list, smarty, sane_lists) it adds:

* fenced code     ```java {#lst-id} Title``` -> numbered "Listing N.k" with line numbers
                  ```java``` (no title)      -> plain highlighted snippet
                  ```text``` / ```trace```   -> monospace block, no highlighting
* figures         ```fig {#fig-id} Caption``` with a JSON body (see figures.py)
* containers      :::type{#id key=value} Optional title  ...  :::
* index terms     [[term]] or [[shown text|Index entry]]
* footnotes       ^[footnote text]  (rendered as real page footnotes)
* cross-refs      @lst-x @fig-x @def-x @thm-x @lem-x @ex-x @sec-x @tbl-x
                  @T:lst-x refers to a label in the theory book from the workbook
* section numbers "## Title" becomes "N.k Title" unless it is one of the closing
                  sections (Summary, Key terms, Review questions, ...) or has {.unnumbered}
"""

import html
import json
import re

import markdown
from pygments import highlight
from pygments.formatters import HtmlFormatter
from pygments.lexers import JavaLexer, get_lexer_by_name

import figures

FENCE_RE = re.compile(r"^(`{3,})\s*([\w+-]*)\s*(\{[^}]*\})?\s*(.*?)\s*$")
CONT_OPEN_RE = re.compile(r"^:::\s*([A-Za-z][\w-]*)\s*(\{[^}]*\})?\s*(.*?)\s*$")
CONT_CLOSE_RE = re.compile(r"^:::\s*$")
ATTR_ID_RE = re.compile(r"#([\w:-]+)")
ATTR_KV_RE = re.compile(r"([\w-]+)=(\"[^\"]*\"|[^\s}]+)")
REF_RE = re.compile(r"(?<![\w@/.])@(T:)?((?:lst|fig|def|thm|lem|ex|exr|sec|tbl|alg|ch)-[\w-]+)")
INDEX_RE = re.compile(r"\[\[([^\]|]+?)(?:\|([^\]]+?))?\]\]")
FOOTNOTE_RE = re.compile(r"\^\[((?:[^\[\]]|\[[^\]]*\])+)\]")

UNNUMBERED = ("summary", "key terms", "review questions", "further reading", "chapter notes",
              "exercises", "problems", "answers", "looking ahead", "what's next", "bibliography")

# container type -> (label, numbered?, counter group)
BOX_TYPES = {
    "definition": ("Definition", True, "def"),
    "theorem": ("Theorem", True, "thm"),
    "lemma": ("Lemma", True, "thm"),
    "claim": ("Claim", True, "thm"),
    "example": ("Example", True, "ex"),
    "exercise": ("Exercise", True, "exr"),
    "proof": ("Proof", False, None),
    "keyidea": ("Key idea", False, None),
    "note": ("Note", False, None),
    "tip": ("Exam tip", False, None),
    "pitfall": ("Pitfall", False, None),
    "infosys": ("Infosys lens", False, None),
    "java": ("Java note", False, None),
    "history": ("Background", False, None),
    "recall": ("Recall", False, None),
    "solution": ("Solution", False, None),
    "hint": ("Hint", False, None),
    "task": ("Task", False, None),
    "checklist": ("Checklist", False, None),
    "plain": ("", False, None),
}

PREFIX_LABEL = {
    "lst": "Listing", "fig": "Figure", "def": "Definition", "thm": "Theorem", "lem": "Lemma",
    "ex": "Example", "exr": "Exercise", "sec": "Section", "tbl": "Table", "alg": "Algorithm", "ch": "Chapter",
}

_formatter = HtmlFormatter(nowrap=True)


def parse_attrs(s):
    s = (s or "").strip()
    if not s:
        return None, {}
    m = ATTR_ID_RE.search(s)
    ident = m.group(1) if m else None
    kv = {}
    for k, v in ATTR_KV_RE.findall(s):
        kv[k] = v.strip('"')
    classes = re.findall(r"\.([\w-]+)", s)
    if classes:
        kv["class"] = " ".join(classes)
    return ident, kv


def slug(s):
    s = re.sub(r"<[^>]+>", "", s)
    s = re.sub(r"[^\w\s-]", "", s.lower())
    return re.sub(r"[\s_]+", "-", s).strip("-")[:60]


class Labels:
    """Global label table shared by every chapter of one book (and readable by the other)."""

    def __init__(self):
        self.labels = {}      # id -> (kind label, number string, chapter)

    def add(self, ident, kind, number, chapter):
        if ident in self.labels:
            raise ValueError(f"duplicate label #{ident} (chapter {chapter})")
        self.labels[ident] = (kind, number, chapter)


class Chapter:
    """Renders one chapter's Markdown into HTML. Two passes: number labels, then render."""

    def __init__(self, text, number, book, labels, other_labels=None, appendix=False):
        self.text = text
        self.number = number          # "2" or "A"
        self.book = book              # "theory" | "workbook"
        self.labels = labels
        self.other = other_labels
        self.appendix = appendix
        self.counters = {}
        self.index_terms = []         # (entry, anchor id, shown)
        self.answers = []             # html blocks
        self.sections = []            # (number, title, id)
        self.blocks = {}
        self._n = 0
        self.ix = 0

    # ---------------------------------------------------------------- pass 1
    def number_labels(self):
        sec = 0
        counters = {}
        in_code = None
        for line in self.text.split("\n"):
            if in_code:
                if line.strip().startswith(in_code) and line.strip().strip("`") == "":
                    in_code = None
                continue
            m = FENCE_RE.match(line)
            if m and m.group(1):
                in_code = m.group(1)
                lang = m.group(2)
                ident, _ = parse_attrs(m.group(3))
                if lang == "fig":
                    counters["fig"] = counters.get("fig", 0) + 1
                    if ident:
                        self.labels.add(ident, "Figure", f"{self.number}.{counters['fig']}", self.number)
                elif m.group(4) or ident:
                    counters["lst"] = counters.get("lst", 0) + 1
                    if ident:
                        self.labels.add(ident, "Listing", f"{self.number}.{counters['lst']}", self.number)
                continue
            m = CONT_OPEN_RE.match(line)
            if m:
                kind = m.group(1)
                ident, _ = parse_attrs(m.group(2))
                info = BOX_TYPES.get(kind)
                if info and info[1]:
                    grp = info[2]
                    counters[grp] = counters.get(grp, 0) + 1
                    if ident:
                        self.labels.add(ident, info[0], f"{self.number}.{counters[grp]}", self.number)
                continue
            if line.startswith("## "):
                title = line[3:]
                ident, kv = parse_attrs((re.search(r"\{[^}]*\}\s*$", title) or [""])[0] if "{" in title else "")
                clean = re.sub(r"\s*\{[^}]*\}\s*$", "", title).strip()
                unnumbered = clean.lower().startswith(UNNUMBERED) or "unnumbered" in kv.get("class", "")
                if not unnumbered:
                    sec += 1
                    if ident:
                        self.labels.add(ident, "Section", f"{self.number}.{sec}", self.number)
            elif line.startswith("| ") and False:
                pass
        # tables with ids are written as a line "Table: {#tbl-x} caption" before the table
        tcount = 0
        for m in re.finditer(r"^Table:\s*(\{[^}]*\})?\s*(.*)$", self.text, re.M):
            tcount += 1
            ident, _ = parse_attrs(m.group(1))
            if ident:
                self.labels.add(ident, "Table", f"{self.number}.{tcount}", self.number)

    # ---------------------------------------------------------------- helpers
    def next_count(self, grp):
        self.counters[grp] = self.counters.get(grp, 0) + 1
        return f"{self.number}.{self.counters[grp]}"

    def stash(self, html_text):
        self._n += 1
        key = f"QXBLOCK{self._n}QX"
        self.blocks[key] = html_text
        return f"\n\n{key}\n\n"

    def ref(self, m):
        other = bool(m.group(1))
        ident = m.group(2)
        table = self.other.labels if other and self.other else self.labels.labels
        if ident not in table:
            raise ValueError(f"chapter {self.number}: unknown reference @{'T:' if other else ''}{ident}")
        kind, num, _ = table[ident]
        if other:
            return f'<span class="xref ext">Theory {kind} {num}</span>'
        return f'<a class="xref" href="#{ident}">{kind}&#160;{num}</a>'

    def inline(self, s):
        """Inline extensions applied to Markdown text (never to code)."""
        def ix(m):
            shown = m.group(1).strip()
            entry = (m.group(2) or shown).strip()
            self.ix += 1
            anchor = f"ix-{self.book[0]}{self.number}-{self.ix}"
            self.index_terms.append((entry, anchor, shown))
            return f'<dfn class="kt" id="{anchor}">{shown}</dfn>'
        s = INDEX_RE.sub(ix, s)
        s = FOOTNOTE_RE.sub(lambda m: f'<span class="footnote">{m.group(1)}</span>', s)
        s = REF_RE.sub(self.ref, s)
        return s

    def md_inline(self, s):
        """Inline Markdown (code, emphasis, refs, index terms) for one-line titles and captions."""
        if not s:
            return ""
        t, saved = self.protect_inline_code(s)
        t = self.inline(t)
        for k, v in enumerate(saved):
            t = t.replace(f"QXCODE{k}QX", v)
        out = markdown.markdown(t, extensions=["smarty"]).strip()
        if out.startswith("<p>") and out.endswith("</p>"):
            out = out[3:-4]
        return out

    # ---------------------------------------------------------------- code & figures
    def render_code(self, lang, attrs, title, body):
        ident, kv = parse_attrs(attrs)
        code = body
        if lang in ("java", "Java"):
            lexer = JavaLexer()
        elif lang in ("text", "trace", "", "output", "plain"):
            lexer = None
        else:
            try:
                lexer = get_lexer_by_name(lang)
            except Exception:
                lexer = None
        hl = highlight(code, lexer, _formatter) if lexer else html.escape(code)
        hl = hl.rstrip("\n")
        numbered = bool(title or ident) and lang not in ("text", "trace", "output")
        nolines = "nolines" in kv.get("class", "")
        lines = hl.split("\n")
        if numbered and not nolines:
            hl = "\n".join(f'<span class="ln"></span>{ln}' for ln in lines)
        cls = "code"
        if lang in ("text", "trace", "output"):
            cls += " trace"
        if kv.get("class"):
            cls += " " + kv["class"]
        small = " small" if len(max(lines, key=len, default="")) > 92 else ""
        out = []
        if numbered:
            num = self.next_count("lst")
            idattr = f' id="{ident}"' if ident else ""
            cap = self.md_inline(title) if title else ""
            out.append(f'<div class="listing"{idattr}><div class="lst-cap"><span class="lst-label">Listing {num}</span> {cap}</div>')
            out.append(f'<pre class="{cls}{small}"><code>{hl}</code></pre></div>')
        else:
            if title:
                out.append(f'<div class="snippet-cap">{self.md_inline(title)}</div>')
            out.append(f'<pre class="{cls}{small}"><code>{hl}</code></pre>')
        return "".join(out)

    def render_figure(self, attrs, caption, body):
        ident, kv = parse_attrs(attrs)
        try:
            spec = json.loads(body)
        except json.JSONDecodeError as e:
            raise ValueError(f"chapter {self.number}: bad JSON in figure '{caption[:40]}': {e}")
        svg = figures.render(spec)
        num = self.next_count("fig")
        idattr = f' id="{ident}"' if ident else ""
        cap = self.md_inline(caption)
        return (f'<figure class="fig"{idattr}>{svg}<figcaption><span class="fig-label">Figure {num}</span> '
                f'{cap}</figcaption></figure>')

    # ---------------------------------------------------------------- containers
    def render_container(self, kind, attrs, title, body):
        ident, kv = parse_attrs(attrs)
        if kind == "answers":
            inner = self.render_md(body)
            self.answers.append(inner)
            return ""
        if kind == "write":
            lines = int(kv.get("lines", 6))
            prompt = self.md_inline(title) if title else ""
            inner = self.render_md(body) if body.strip() else ""
            ptxt = f'<div class="write-prompt">{prompt}</div>' if prompt else ""
            wl = '<div class="wl"></div>' * lines
            return f'<div class="write-wrap">{ptxt}{inner}<div class="write">{wl}</div></div>'
        if kind == "grid":
            rows = int(kv.get("rows", 8))
            prompt = self.md_inline(title) if title else ""
            inner = self.render_md(body) if body.strip() else ""
            ptxt = f'<div class="write-prompt">{prompt}</div>' if prompt else ""
            return f'<div class="write-wrap">{ptxt}{inner}<div class="gridpaper" style="height:{rows * 5:.1f}mm"></div></div>'
        if kind == "columns":
            inner = self.render_md(body)
            return f'<div class="columns">{inner}</div>'
        if kind == "nobreak":
            inner = self.render_md(body)
            return f'<div class="nobreak">{inner}</div>'
        if kind == "pagebreak":
            return '<div class="pagebreak"></div>'
        if kind not in BOX_TYPES:
            raise ValueError(f"chapter {self.number}: unknown container :::{kind}")
        label, numbered, grp = BOX_TYPES[kind]
        if numbered:
            label = f"{label} {self.next_count(grp)}"
        idattr = f' id="{ident}"' if ident else ""
        extra = (" " + kv["class"]) if kv.get("class") else ""
        inner = self.render_md(body)
        if kind == "proof":
            inner = inner.strip()
            lab = f'<span class="proof-label">{html.escape(title) if title else "Proof."}</span>'
            if inner.startswith("<p>"):
                inner = "<p>" + lab + inner[3:]
            else:
                inner = f"<p>{lab}</p>" + inner
            if inner.endswith("</p>"):
                inner = inner[:-4] + ' <span class="qed">∎</span></p>'
            else:
                inner += '<p class="qed-line"><span class="qed">∎</span></p>'
        t = self.md_inline(title) if title else ""
        head = ""
        if label or t:
            sep = " · " if (label and t and kind not in BOX_TYPES or False) else " "
            head = f'<div class="box-title"><span class="box-label">{label}</span>{sep if label and t else ""}<span class="box-name">{t}</span></div>'
        return f'<div class="box {kind}{extra}"{idattr}>{head}<div class="box-body">{inner}</div></div>'

    # ---------------------------------------------------------------- block scanner
    def scan(self, text):
        """Replace fenced code, figures and containers with stash keys; return Markdown text."""
        lines = text.split("\n")
        out = []
        i = 0
        while i < len(lines):
            line = lines[i]
            m = FENCE_RE.match(line)
            if m and m.group(1):
                fence = m.group(1)
                lang, attrs, title = m.group(2), m.group(3), m.group(4)
                j = i + 1
                body = []
                while j < len(lines) and not (lines[j].strip().startswith(fence) and lines[j].strip().strip("`") == ""):
                    body.append(lines[j])
                    j += 1
                if j >= len(lines):
                    raise ValueError(f"chapter {self.number}: unclosed code fence starting '{line[:50]}'")
                body = "\n".join(body)
                # strip common indentation (fences inside list items)
                ind = len(line) - len(line.lstrip())
                if ind:
                    body = "\n".join(b[ind:] if b[:ind].strip() == "" else b for b in body.split("\n"))
                if lang == "fig":
                    out.append(self.stash(self.render_figure(attrs, title, body)))
                else:
                    out.append(self.stash(self.render_code(lang, attrs, title, body)))
                i = j + 1
                continue
            m = CONT_OPEN_RE.match(line)
            if m:
                kind, attrs, title = m.group(1), m.group(2), m.group(3)
                depth = 1
                j = i + 1
                body = []
                in_code = None
                while j < len(lines):
                    l2 = lines[j]
                    fm = FENCE_RE.match(l2)
                    if in_code:
                        if l2.strip().startswith(in_code) and l2.strip().strip("`") == "":
                            in_code = None
                    elif fm and fm.group(1):
                        in_code = fm.group(1)
                    elif CONT_OPEN_RE.match(l2):
                        depth += 1
                    elif CONT_CLOSE_RE.match(l2):
                        depth -= 1
                        if depth == 0:
                            break
                    body.append(l2)
                    j += 1
                if kind in ("pagebreak",):
                    out.append(self.stash(self.render_container(kind, attrs, title, "")))
                    i += 1
                    continue
                if j >= len(lines):
                    raise ValueError(f"chapter {self.number}: unclosed :::{kind} '{title[:40]}'")
                out.append(self.stash(self.render_container(kind, attrs, title, "\n".join(body))))
                i = j + 1
                continue
            out.append(line)
            i += 1
        return "\n".join(out)

    def protect_inline_code(self, s):
        """Hide `code` spans from the inline extensions."""
        saved = []

        def keep(m):
            saved.append(m.group(0))
            return f"QXCODE{len(saved) - 1}QX"
        s = re.sub(r"(`+)(.+?)\1", keep, s)
        return s, saved

    def render_md(self, text):
        md_text = self.scan(text)
        md_text, saved = self.protect_inline_code(md_text)
        # "Table: {#id} caption" lines -> caption paragraph before the table
        def tcap(m):
            ident, _ = parse_attrs(m.group(1))
            num = self.next_count("tbl")
            idattr = f' id="{ident}"' if ident else ""
            return f'<p class="tbl-cap"{idattr}><span class="tbl-label">Table {num}</span> {m.group(2)}</p>\n'
        md_text = re.sub(r"^Table:\s*(\{[^}]*\})?\s*(.*)$", tcap, md_text, flags=re.M)
        md_text = self.inline(md_text)
        for k, v in enumerate(saved):
            md_text = md_text.replace(f"QXCODE{k}QX", v)
        html_out = markdown.markdown(
            md_text,
            extensions=["tables", "attr_list", "sane_lists", "smarty", "md_in_html"],
            output_format="html5",
        )
        # put stashed blocks back
        def unstash(m):
            return self.blocks[m.group(1)]
        for _ in range(3):
            html_out = re.sub(r"<p>(QXBLOCK\d+QX)</p>", unstash, html_out)
            html_out = re.sub(r"(QXBLOCK\d+QX)", unstash, html_out)
        return html_out

    def number_sections(self, html_text, prefix_ok=True):
        sec = [0]

        def h2(m):
            attrs, inner = m.group(1) or "", m.group(2)
            plain = re.sub(r"<[^>]+>", "", inner).strip()
            unnumbered = plain.lower().startswith(UNNUMBERED) or "unnumbered" in attrs
            idm = re.search(r'id="([^"]+)"', attrs)
            ident = idm.group(1) if idm else f"s{self.book[0]}{self.number}-{slug(plain)}"
            if not idm:
                attrs += f' id="{ident}"'
            if unnumbered:
                cls = ' class="unnumbered"' if "class=" not in attrs else ""
                self.sections.append((None, plain, ident))
                return f"<h2{attrs}{cls}>{inner}</h2>"
            sec[0] += 1
            num = f"{self.number}.{sec[0]}"
            self.sections.append((num, plain, ident))
            return f'<h2{attrs} data-num="{num}"><span class="secnum">{num}</span> {inner}</h2>'
        return re.sub(r"<h2([^>]*)>(.*?)</h2>", h2, html_text, flags=re.S)

    def render(self):
        body = self.render_md(self.text)
        body = self.number_sections(body)
        return body
