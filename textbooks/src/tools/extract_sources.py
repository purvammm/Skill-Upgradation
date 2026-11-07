#!/usr/bin/env python3
"""Re-extract the writing sources from the two HTML guides into a scratch folder.

    git show origin/feature/infosys-dsa-responsive-layout:infosys-dsa-29-day-plan.html  > WORK/html/plan.html
    git show origin/feature/infosys-dsa-responsive-layout:infosys-dsa-daily-handbook.html > WORK/html/handbook.html
    python tools/extract_sources.py WORK          # needs beautifulsoup4

Writes, under WORK/sources/:
    days/dayNN.json   one entry of data/plan.json per day
    cards/<id>.md     the 44 handbook pattern cards (use-when, how it works, template, traps, practice links)
    plan/<name>.md    the plan page sections: how, test, target, java-kit, mocks, repeats, sample, exam,
                      plan-b, log, sources
These files are inputs for writing chapters (see AUTHORING.md); they are not part of the build.
"""
import json
import sys
from pathlib import Path

from bs4 import BeautifulSoup, NavigableString, Tag

HERE = Path(__file__).resolve().parent.parent
SECTIONS = ("how", "test", "target", "java", "mocks", "repeats", "sample", "exam", "backup", "log", "sources")
RENAME = {"java": "java-kit", "backup": "plan-b"}


def inline(node):
    out = []
    for c in node.children:
        if isinstance(c, NavigableString):
            out.append(str(c))
        elif isinstance(c, Tag) and c.name not in ("button", "script", "style"):
            if c.name == "code":
                out.append("`" + c.get_text() + "`")
            elif c.name in ("b", "strong"):
                out.append("**" + inline(c) + "**")
            elif c.name in ("i", "em"):
                out.append("*" + inline(c) + "*")
            elif c.name == "a":
                href = c.get("href", "")
                out.append(f"[{inline(c)}]({href})" if href.startswith("http") else inline(c))
            elif c.name == "br":
                out.append("\n")
            else:
                out.append(inline(c))
    return "".join(out)


def block(node):
    out = []
    for c in node.children:
        if isinstance(c, NavigableString):
            if str(c).strip():
                out.append(str(c).strip())
            continue
        if not isinstance(c, Tag) or c.name in ("button", "script", "style", "svg"):
            continue
        cls = c.get("class") or []
        if "rate" in cls or "filebar" in cls:
            continue
        if c.name in ("h1", "h2", "h3", "h4", "h5"):
            out.append("#" * min(int(c.name[1]) + 1, 6) + " " + c.get_text(" ", strip=True))
        elif c.name == "pre":
            out.append("```java\n" + c.get_text().rstrip("\n") + "\n```")
        elif c.name in ("ul", "ol"):
            items = []
            for i, li in enumerate(c.find_all("li", recursive=False)):
                mark = "-" if c.name == "ul" else f"{i + 1}."
                items.append(f"{mark} " + " ".join(inline(li).split()))
            out.append("\n".join(items))
        elif c.name == "table":
            rows = ["| " + " | ".join(" ".join(inline(td).split()) for td in tr.find_all(["td", "th"])) + " |"
                    for tr in c.find_all("tr")]
            if rows:
                rows.insert(1, "|" + "---|" * (rows[0].count("|") - 1))
            out.append("\n".join(rows))
        elif c.name == "p":
            out.append(" ".join(inline(c).split()))
        elif c.name == "span" and "lbl" in cls:
            out.append("**" + c.get_text(" ", strip=True) + "**")
        elif c.name == "div" and "practice" in cls:
            links = [f"- [{' '.join(a.get_text(' ', strip=True).split())}]({a.get('href')})" for a in c.find_all("a")]
            out.append("**Practise it**\n" + "\n".join(links))
        elif c.name in ("div", "section", "article", "details", "summary", "span", "header", "footer", "aside"):
            inner = block(c)
            if inner.strip():
                out.append(inner)
        else:
            t = " ".join(inline(c).split())
            if t:
                out.append(t)
    return "\n\n".join(x for x in out if x.strip())


def main():
    work = Path(sys.argv[1] if len(sys.argv) > 1 else "/projects/sandbox/work")
    src = work / "sources"
    for d in ("days", "cards", "plan"):
        (src / d).mkdir(parents=True, exist_ok=True)
    for d in json.loads((HERE / "data/plan.json").read_text()):
        (src / "days" / f"day{d['d']:02d}.json").write_text(json.dumps(d, indent=1, ensure_ascii=False))
    hb = BeautifulSoup((work / "html/handbook.html").read_text(), "html.parser")
    for a in hb.find_all("article", class_="pat"):
        (src / "cards" / f"{a.get('data-id')}.md").write_text(block(a) + "\n")
    # On the plan page a section id sits on a heading band; its content is every sibling up to the next band.
    pl = BeautifulSoup((work / "html/plan.html").read_text(), "html.parser")
    for sid in SECTIONS:
        node = pl.find(id=sid)
        if node is None:
            print("missing section", sid)
            continue
        wrap = BeautifulSoup("<div></div>", "html.parser").div
        sib = node.find_next_sibling()
        while sib is not None and "band" not in (sib.get("class") or []):
            wrap.append(BeautifulSoup(str(sib), "html.parser"))
            sib = sib.find_next_sibling()
        text = "# " + node.get_text(" ", strip=True) + "\n\n" + block(wrap)
        (src / "plan" / f"{RENAME.get(sid, sid)}.md").write_text(text + "\n")
    print("wrote", src)


if __name__ == "__main__":
    main()
