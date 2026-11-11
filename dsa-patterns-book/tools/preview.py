"""Render PDF pages to PNG so a layout can be checked by eye, or print the outline.

    python tools/preview.py book.pdf 1-6 --out /tmp/png      # pages 1..6 (1-based)
    python tools/preview.py book.pdf --info                  # page count, fonts and outline
"""

import argparse
from pathlib import Path

import pymupdf


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("pdf")
    ap.add_argument("pages", nargs="?", default="1-4")
    ap.add_argument("--out", default="preview-png")
    ap.add_argument("--dpi", type=int, default=80)
    ap.add_argument("--info", action="store_true")
    a = ap.parse_args()
    doc = pymupdf.open(a.pdf)
    if a.info:
        fonts = sorted({f[3] for page in doc for f in page.get_fonts()})
        print(f"{doc.page_count} pages; fonts: {', '.join(fonts)}")
        for level, title, page in doc.get_toc():
            print(f"{'  ' * (level - 1)}{title} .... {page}")
        return
    lo, _, hi = a.pages.partition("-")
    lo, hi = int(lo), int(hi or lo)
    out = Path(a.out)
    out.mkdir(parents=True, exist_ok=True)
    for i in range(lo - 1, min(hi, doc.page_count)):
        path = out / f"page-{i + 1:03d}.png"
        doc[i].get_pixmap(dpi=a.dpi).save(path)
        print(path)


if __name__ == "__main__":
    main()
