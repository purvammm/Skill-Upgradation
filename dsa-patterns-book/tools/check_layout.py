"""Scan a built PDF for layout defects that are easy to miss by eye.

- empty window frames: a window border fragment on a page with no text inside it
  (happens when a window's title bar is pushed to the next page)
- text past the right edge of the windows (a code line too long for the page)
- unresolved page references: "(p. )" or "p. " with no number

    python tools/check_layout.py ../dsa-patterns-printable.pdf
"""

import re
import sys

import pymupdf

BORDER = (0.867, 0.843, 0.918)       # #ddd6ea, the window border colour in book.css
RIGHT_LIMIT = 556.0                  # body margin 40pt + window border; anything past this is outside


def close(c, ref, tol=0.02):
    return c is not None and all(abs(a - b) < tol for a, b in zip(c, ref))


def main():
    doc = pymupdf.open(sys.argv[1] if len(sys.argv) > 1 else "../dsa-patterns-printable.pdf")
    empty, wide, refs = [], [], []
    for pno, page in enumerate(doc, 1):
        words = page.get_text("words")
        frames = []
        for d in page.get_drawings():
            if close(d.get("color") or d.get("fill"), BORDER) and d["rect"].width > 400 and d["rect"].height > 4:
                frames.append(d["rect"])
        for r in frames:
            inside = [w for w in words if r.x0 < w[0] < r.x1 and r.y0 < w[1] < r.y1]
            if not inside:
                empty.append((pno, round(r.y0), round(r.y1)))
        for w in words:
            if w[2] > RIGHT_LIMIT and w[1] > 40 and w[3] < 790:
                wide.append((pno, w[4], round(w[2], 1)))
        text = page.get_text()
        if re.search(r"\(p\.\s*\)", text) or re.search(r"p\. *\n *\)", text):
            refs.append(pno)
    print(f"{doc.page_count} pages")
    print(f"empty window frames: {len(empty)}", empty[:30])
    print(f"text past the right edge: {len(wide)}", wide[:30])
    print(f"unresolved page refs on pages: {refs[:30]}")
    sys.exit(1 if empty or wide or refs else 0)


if __name__ == "__main__":
    main()
