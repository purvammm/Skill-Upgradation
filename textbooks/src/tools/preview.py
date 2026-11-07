#!/usr/bin/env python3
"""Render PDF pages to PNG for visual checks.

    python tools/preview.py book.pdf 1-4,9 --out /tmp/prev --scale 1.3
Prints the PNG paths. Also: --info prints the page count and outline.
"""

import argparse
from pathlib import Path

import pypdfium2 as pdfium


def pages_arg(s, n):
    out = []
    for part in s.split(","):
        if "-" in part:
            a, b = part.split("-")
            out += list(range(int(a), min(int(b), n) + 1))
        else:
            out.append(int(part))
    return [p for p in out if 1 <= p <= n]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("pdf")
    ap.add_argument("pages", nargs="?", default="1")
    ap.add_argument("--out", default="/tmp/preview")
    ap.add_argument("--scale", type=float, default=1.25)
    ap.add_argument("--info", action="store_true")
    a = ap.parse_args()
    doc = pdfium.PdfDocument(a.pdf)
    n = len(doc)
    if a.info:
        print("pages:", n)
        for item in doc.get_toc():
            dest = item.get_dest()
            page = dest.get_index() + 1 if dest else 0
            print("  " * item.level + f"{item.get_title()}  -> p{page}")
        return
    out = Path(a.out)
    out.mkdir(parents=True, exist_ok=True)
    stem = Path(a.pdf).stem
    for p in pages_arg(a.pages, n):
        img = doc[p - 1].render(scale=a.scale).to_pil()
        path = out / f"{stem}-p{p:03d}.png"
        img.save(path)
        print(path)


if __name__ == "__main__":
    main()
