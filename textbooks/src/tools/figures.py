"""Small SVG figure generator for the textbooks.

A figure is written in a chapter as a fenced block whose body is JSON:

    ```fig {#fig-prefix} Prefix sums of a = [1, 2, -1, 2, 1]
    {"type": "array", "rows": [...]}
    ```

Supported types: array, tree, graph, bars, intervals, grid.
See AUTHORING.md for the fields of each type.
"""

import html
import math

FONT = "Source Sans 3, Noto Sans, sans-serif"
MONO = "JetBrains Mono, monospace"
INK = "#1f2937"
LINE = "#475569"
MUTED = "#6b7280"

FILL = {
    "blue": "#dbeafe", "green": "#dcfce7", "red": "#fee2e2", "amber": "#fef3c7",
    "purple": "#ede9fe", "gray": "#e5e7eb", "teal": "#ccfbf1", "pink": "#fce7f3",
    "white": "#ffffff", "none": "none",
}
STROKE = {
    "blue": "#2563eb", "green": "#16a34a", "red": "#dc2626", "amber": "#d97706",
    "purple": "#7c3aed", "gray": "#6b7280", "teal": "#0d9488", "pink": "#db2777",
    "white": LINE, "none": LINE, "ink": INK,
}


def esc(s):
    return html.escape(str(s), quote=True)


def fill_of(c):
    if c is None:
        return "#ffffff"
    return FILL.get(c, c)


def stroke_of(c):
    if c is None:
        return LINE
    return STROKE.get(c, c)


def text(x, y, s, size=11, anchor="middle", weight="normal", color=INK, mono=False, italic=False):
    fam = MONO if mono else FONT
    style = ' font-style="italic"' if italic else ""
    return (f'<text x="{x:.1f}" y="{y:.1f}" font-family="{fam}" font-size="{size}" '
            f'text-anchor="{anchor}" font-weight="{weight}" fill="{color}"{style}>{esc(s)}</text>')


_FIG = [0]


def svg_wrap(w, h, body, max_width=None):
    _FIG[0] += 1
    sfx = f"f{_FIG[0]}"
    body = [b.replace("url(#ah", f"url(#{sfx}ah") for b in body]
    w = int(math.ceil(w))
    h = int(math.ceil(h))
    defs = (
        '<defs>'
        f'<marker id="{sfx}ah" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">'
        f'<path d="M0,0 L10,5 L0,10 z" fill="{LINE}"/></marker>'
        f'<marker id="{sfx}ahb" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">'
        f'<path d="M0,0 L10,5 L0,10 z" fill="{STROKE["blue"]}"/></marker>'
        f'<marker id="{sfx}ahr" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">'
        f'<path d="M0,0 L10,5 L0,10 z" fill="{STROKE["red"]}"/></marker>'
        '</defs>'
    )
    # Width in CSS px at 96 dpi; cap at the text column width (about 640px).
    disp_w = min(w, max_width or 640)
    disp_h = h * disp_w / w
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{disp_w:.0f}" height="{disp_h:.0f}" '
            f'viewBox="0 0 {w} {h}">{defs}{"".join(body)}</svg>')


def marker_for(color):
    if color in ("blue",):
        return "ahb"
    if color in ("red",):
        return "ahr"
    return "ah"


# --------------------------------------------------------------------------- array
def fig_array(spec):
    """Rows of cells.

    rows: [{"label": "a", "cells": [3, 1, 4], "index": true, "start": 0}]
    highlight: [{"row": 0, "from": 1, "to": 2, "color": "blue"}]   (inclusive)
    cellcolors: [{"row": 0, "at": 3, "color": "red"}]
    pointers: [{"row": 0, "at": 1, "label": "l", "side": "above", "color": "blue"}]
    brackets: [{"row": 0, "from": 1, "to": 3, "label": "sum 3", "side": "below"}]
    arrows: [{"from": [0, 2], "to": [1, 3], "label": "", "color": "gray"}]  ([row, col])
    cell: width of a cell (default 34)
    """
    rows = spec["rows"]
    cw = spec.get("cell", 34)
    ch = spec.get("cellh", 30)
    label_w = 0
    if any(r.get("label") for r in rows):
        label_w = max(len(str(r.get("label", ""))) for r in rows) * 7.5 + 16
    ncols = max(len(r["cells"]) for r in rows)
    pointers = spec.get("pointers", [])
    brackets = spec.get("brackets", [])

    # vertical layout per row: space above (pointers/brackets), cells, index, below
    row_y = []
    y = 8
    layout = []
    for ri, r in enumerate(rows):
        above = 0
        if any(p["row"] == ri and p.get("side", "above") == "above" for p in pointers):
            above = max(above, 34)
        if any(b["row"] == ri and b.get("side", "below") == "above" for b in brackets):
            above = max(above, 30) + (34 if above else 0) * 0
        below = 0
        if r.get("index", ri == len(rows) - 1 if spec.get("index_last_only") else True):
            below += 16
        if any(p["row"] == ri and p.get("side") == "below" for p in pointers):
            below += 32
        if any(b["row"] == ri and b.get("side", "below") == "below" for b in brackets):
            below += 30
        top = y + above
        layout.append((top, above, below))
        y = top + ch + below + spec.get("rowgap", 14)
    height = y
    width = label_w + ncols * cw + 16

    body = []
    hl = spec.get("highlight", [])
    cellcolors = {(c["row"], c["at"]): c.get("color", "amber") for c in spec.get("cellcolors", [])}
    for ri, r in enumerate(rows):
        top, above, below = layout[ri]
        x0 = label_w + 8
        if r.get("label"):
            body.append(text(label_w - 4, top + ch / 2 + 4, r["label"], 12, "end", "600", INK, italic=r.get("italic", True)))
        for h in hl:
            if h["row"] != ri:
                continue
            hx = x0 + h["from"] * cw
            hw = (h["to"] - h["from"] + 1) * cw
            body.append(f'<rect x="{hx - 2:.1f}" y="{top - 3:.1f}" width="{hw + 4:.1f}" height="{ch + 6:.1f}" rx="5" '
                        f'fill="{fill_of(h.get("color", "blue"))}" stroke="{stroke_of(h.get("color", "blue"))}" stroke-width="1.2"/>')
        for ci, v in enumerate(r["cells"]):
            cx = x0 + ci * cw
            col = cellcolors.get((ri, ci))
            f = fill_of(col) if col else ("#ffffff" if not any(h["row"] == ri and h["from"] <= ci <= h["to"] for h in hl) else fill_of(next(h.get("color", "blue") for h in hl if h["row"] == ri and h["from"] <= ci <= h["to"])))
            if v is None:
                continue
            body.append(f'<rect x="{cx + 1:.1f}" y="{top:.1f}" width="{cw - 2:.1f}" height="{ch:.1f}" rx="3" fill="{f}" stroke="{LINE}" stroke-width="1"/>')
            body.append(text(cx + cw / 2, top + ch / 2 + 4.5, v, 12.5 if len(str(v)) < 4 else 10.5, mono=True))
        show_index = r.get("index", True)
        if show_index:
            start = r.get("start", 0)
            for ci in range(len(r["cells"])):
                body.append(text(x0 + ci * cw + cw / 2, top + ch + 13, ci + start, 9.5, color=MUTED, mono=True))
        # pointers
        for p in pointers:
            if p["row"] != ri:
                continue
            px = x0 + p["at"] * cw + cw / 2
            color = p.get("color", "blue")
            sc = stroke_of(color)
            if p.get("side", "above") == "above":
                body.append(f'<line x1="{px:.1f}" y1="{top - 20:.1f}" x2="{px:.1f}" y2="{top - 4:.1f}" stroke="{sc}" stroke-width="1.6" marker-end="url(#{marker_for(color)})"/>')
                body.append(text(px, top - 23, p["label"], 11, weight="700", color=sc))
            else:
                yb = top + ch + (16 if show_index else 2)
                body.append(f'<line x1="{px:.1f}" y1="{yb + 18:.1f}" x2="{px:.1f}" y2="{yb + 2:.1f}" stroke="{sc}" stroke-width="1.6" marker-end="url(#{marker_for(color)})"/>')
                body.append(text(px, yb + 30, p["label"], 11, weight="700", color=sc))
        for b in brackets:
            if b["row"] != ri:
                continue
            bx1 = x0 + b["from"] * cw + 3
            bx2 = x0 + (b["to"] + 1) * cw - 3
            sc = stroke_of(b.get("color", "gray"))
            if b.get("side", "below") == "below":
                yb = top + ch + (17 if show_index else 4)
                if any(p["row"] == ri and p.get("side") == "below" for p in pointers):
                    yb += 32
                body.append(f'<path d="M{bx1:.1f},{yb:.1f} v6 H{bx2:.1f} v-6" fill="none" stroke="{sc}" stroke-width="1.3"/>')
                body.append(text((bx1 + bx2) / 2, yb + 19, b["label"], 10.5, color=sc))
            else:
                yb = top - 6
                body.append(f'<path d="M{bx1:.1f},{yb:.1f} v-6 H{bx2:.1f} v6" fill="none" stroke="{sc}" stroke-width="1.3"/>')
                body.append(text((bx1 + bx2) / 2, yb - 10, b["label"], 10.5, color=sc))
    for a in spec.get("arrows", []):
        (r1, c1), (r2, c2) = a["from"], a["to"]
        x1 = label_w + 8 + c1 * cw + cw / 2
        x2 = label_w + 8 + c2 * cw + cw / 2
        y1 = layout[r1][0] + ch
        y2 = layout[r2][0]
        if r2 < r1:
            y1 = layout[r1][0]
            y2 = layout[r2][0] + ch
        color = a.get("color", "gray")
        body.append(f'<line x1="{x1:.1f}" y1="{y1 + 2:.1f}" x2="{x2:.1f}" y2="{y2 - 2:.1f}" stroke="{stroke_of(color)}" stroke-width="1.2" marker-end="url(#{marker_for(color)})"/>')
    return svg_wrap(width, height, body, spec.get("maxwidth"))


# --------------------------------------------------------------------------- tree
def fig_tree(spec):
    """Rooted tree with automatic layout.

    nodes: [{"id": 1, "label": "1", "parent": null, "note": "dp=3", "color": "blue"}]
    or edges: [[1, 2], [1, 3]] with root: 1 and labels: {"1": "a"}
    side: {"5": "right"} for binary trees where a lone child must sit right
    highlight_edges: [[1, 2]]; dashed_edges: [[..]]; edge_labels: {"1-2": "w=3"}
    radius (default 15), hgap (default 46), vgap (default 62)
    """
    r = spec.get("radius", 15)
    hgap = spec.get("hgap", 46)
    vgap = spec.get("vgap", 62)
    nodes = {}
    children = {}
    root = spec.get("root")
    if "nodes" in spec:
        for n in spec["nodes"]:
            nid = str(n["id"])
            nodes[nid] = n
            p = n.get("parent")
            if p is None or p == 0 and spec.get("zero_is_root", True) and "0" not in [str(m["id"]) for m in spec["nodes"]]:
                root = nid if root is None else str(root)
            else:
                children.setdefault(str(p), []).append(nid)
    else:
        labels = spec.get("labels", {})
        adj = {}
        for u, v in spec["edges"]:
            adj.setdefault(str(u), []).append(str(v))
            adj.setdefault(str(v), []).append(str(u))
        root = str(spec.get("root", spec["edges"][0][0]))
        seen = {root}
        order = [root]
        for u in order:
            for v in adj.get(u, []):
                if v not in seen:
                    seen.add(v)
                    order.append(v)
                    children.setdefault(u, []).append(v)
        for u in order:
            nodes[u] = {"id": u, "label": labels.get(u, u)}
        for k, v in spec.get("notes", {}).items():
            if str(k) in nodes:
                nodes[str(k)]["note"] = v
        for k, v in spec.get("colors", {}).items():
            if str(k) in nodes:
                nodes[str(k)]["color"] = v
    root = str(root)
    side = {str(k): v for k, v in spec.get("side", {}).items()}

    pos = {}
    counter = [0]

    def layout(u, depth):
        ch = children.get(u, [])
        if not ch:
            pos[u] = [counter[0], depth]
            counter[0] += 1
            return
        if len(ch) == 1 and side.get(ch[0]) in ("left", "right"):
            # binary tree: leave a gap on the missing side
            if side[ch[0]] == "right":
                start = counter[0]
                layout(ch[0], depth + 1)
                pos[u] = [pos[ch[0]][0] - 0.5, depth]
                if pos[u][0] < start:
                    shift(ch[0], start - pos[u][0])
                    pos[u][0] = start
                    counter[0] = max(counter[0], pos[ch[0]][0] + 1)
            else:
                layout(ch[0], depth + 1)
                pos[u] = [pos[ch[0]][0] + 0.5, depth]
                counter[0] = max(counter[0], pos[u][0] + 1)
            return
        for c in ch:
            layout(c, depth + 1)
        pos[u] = [(pos[ch[0]][0] + pos[ch[-1]][0]) / 2, depth]

    def shift(u, dx):
        pos[u][0] += dx
        for c in children.get(u, []):
            shift(c, dx)

    layout(root, 0)
    maxx = max(p[0] for p in pos.values())
    maxd = max(p[1] for p in pos.values())
    has_notes = any(n.get("note") for n in nodes.values())
    note_h = 16 if has_notes else 0
    lab_extra = 0
    if any(len(str(n.get("label", ""))) > 3 for n in nodes.values()):
        lab_extra = 1
    rx = r + (10 * lab_extra if spec.get("wide") else 0)
    pad = 14 + rx
    width = pad * 2 + maxx * hgap
    height = pad * 2 + maxd * vgap + note_h

    def xy(u):
        return pad + pos[u][0] * hgap, pad + pos[u][1] * vgap

    body = []
    hi_edges = {tuple(sorted((str(a), str(b)))) for a, b in spec.get("highlight_edges", [])}
    dashed = {tuple(sorted((str(a), str(b)))) for a, b in spec.get("dashed_edges", [])}
    elabels = spec.get("edge_labels", {})
    for u, ch in children.items():
        for c in ch:
            x1, y1 = xy(u)
            x2, y2 = xy(c)
            key = tuple(sorted((u, c)))
            col = STROKE["blue"] if key in hi_edges else LINE
            wdt = 2.6 if key in hi_edges else 1.3
            dash = ' stroke-dasharray="4 3"' if key in dashed else ""
            body.append(f'<line x1="{x1:.1f}" y1="{y1:.1f}" x2="{x2:.1f}" y2="{y2:.1f}" stroke="{col}" stroke-width="{wdt}"{dash}/>')
            lab = elabels.get(f"{u}-{c}") or elabels.get(f"{c}-{u}")
            if lab:
                mx, my = (x1 + x2) / 2, (y1 + y2) / 2
                dx = -8 if x2 < x1 else 8
                body.append(text(mx + dx, my, lab, 10, "end" if dx < 0 else "start", color=MUTED))
    for u, n in nodes.items():
        if u not in pos:
            continue
        x, y = xy(u)
        col = n.get("color")
        lab = str(n.get("label", u))
        if spec.get("wide") and len(lab) > 2:
            w = max(2 * r, len(lab) * 7.2 + 12)
            body.append(f'<rect x="{x - w / 2:.1f}" y="{y - r:.1f}" width="{w:.1f}" height="{2 * r:.1f}" rx="{r * 0.6:.1f}" '
                        f'fill="{fill_of(col)}" stroke="{stroke_of(col)}" stroke-width="1.3"/>')
        else:
            body.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{r}" fill="{fill_of(col)}" stroke="{stroke_of(col)}" stroke-width="1.3"/>')
        body.append(text(x, y + 4.2, lab, 11.5 if len(lab) < 4 else 10, mono=True))
        if n.get("note"):
            body.append(text(x, y + r + 13, n["note"], 9.5, color=STROKE.get(n.get("notecolor", "gray"), MUTED)))
    return svg_wrap(width, height, body, spec.get("maxwidth"))


# --------------------------------------------------------------------------- graph
def fig_graph(spec):
    """Graph with explicit coordinates (grid units).

    nodes: [{"id": "A", "x": 0, "y": 0, "label": "A", "note": "d=0", "color": "green"}]
    edges: [["A", "B", "4"], ["B", "C"]]   third item = weight label (optional)
    directed: false; unit: 70 (pixels per grid unit)
    highlight_edges: [["A", "B"]]  (drawn thick blue)
    """
    unit = spec.get("unit", 70)
    r = spec.get("radius", 15)
    pad = r + 18
    nodes = {str(n["id"]): n for n in spec["nodes"]}
    xs = [n["x"] for n in nodes.values()]
    ys = [n["y"] for n in nodes.values()]
    minx, miny = min(xs), min(ys)
    width = pad * 2 + (max(xs) - minx) * unit
    height = pad * 2 + (max(ys) - miny) * unit + (14 if any(n.get("note") for n in nodes.values()) else 0)

    def xy(n):
        return pad + (n["x"] - minx) * unit, pad + (n["y"] - miny) * unit

    directed = spec.get("directed", False)
    hi = {(str(a), str(b)) for a, b in spec.get("highlight_edges", [])}
    body = []
    for e in spec.get("edges", []):
        a, b = nodes[str(e[0])], nodes[str(e[1])]
        x1, y1 = xy(a)
        x2, y2 = xy(b)
        d = math.hypot(x2 - x1, y2 - y1) or 1
        ux, uy = (x2 - x1) / d, (y2 - y1) / d
        sx, sy = x1 + ux * r, y1 + uy * r
        ex, ey = x2 - ux * (r + (2 if directed else 0)), y2 - uy * (r + (2 if directed else 0))
        is_hi = (str(e[0]), str(e[1])) in hi or (not directed and (str(e[1]), str(e[0])) in hi)
        col = STROKE["blue"] if is_hi else LINE
        wdt = 2.6 if is_hi else 1.3
        mk = f' marker-end="url(#{"ahb" if is_hi else "ah"})"' if directed else ""
        bend = e[3] if len(e) > 3 else 0
        if bend:
            mx, my = (sx + ex) / 2 - uy * bend, (sy + ey) / 2 + ux * bend
            body.append(f'<path d="M{sx:.1f},{sy:.1f} Q{mx:.1f},{my:.1f} {ex:.1f},{ey:.1f}" fill="none" stroke="{col}" stroke-width="{wdt}"{mk}/>')
            lx, ly = (sx + 2 * mx + ex) / 4, (sy + 2 * my + ey) / 4
        else:
            body.append(f'<line x1="{sx:.1f}" y1="{sy:.1f}" x2="{ex:.1f}" y2="{ey:.1f}" stroke="{col}" stroke-width="{wdt}"{mk}/>')
            lx, ly = (sx + ex) / 2, (sy + ey) / 2
        if len(e) > 2 and e[2] not in (None, ""):
            ox, oy = -uy * 9, ux * 9
            body.append(f'<rect x="{lx + ox - 9:.1f}" y="{ly + oy - 8:.1f}" width="18" height="14" rx="3" fill="#ffffff" opacity="0.85"/>')
            body.append(text(lx + ox, ly + oy + 3.5, e[2], 10.5, weight="600", color=STROKE["amber"] if not is_hi else STROKE["blue"]))
    for nid, n in nodes.items():
        x, y = xy(n)
        col = n.get("color")
        body.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{r}" fill="{fill_of(col)}" stroke="{stroke_of(col)}" stroke-width="1.3"/>')
        body.append(text(x, y + 4.2, n.get("label", nid), 11.5, mono=True))
        if n.get("note"):
            body.append(text(x, y + r + 13, n["note"], 9.5, color=MUTED))
    return svg_wrap(width, height, body, spec.get("maxwidth"))


# --------------------------------------------------------------------------- bars
def fig_bars(spec):
    """Histogram. values: [2, 1, 5, 6, 2, 3]; colors: {"2": "blue"}; rect: {"from": 2, "to": 3, "height": 5}"""
    vals = spec["values"]
    bw = spec.get("bar", 34)
    unit = spec.get("unit", 18)
    top = max(vals)
    pad = 14
    width = pad * 2 + len(vals) * bw
    height = pad * 2 + top * unit + 30
    base = pad + top * unit + 4
    body = []
    colors = {int(k): v for k, v in spec.get("colors", {}).items()}
    for i, v in enumerate(vals):
        x = pad + i * bw
        col = colors.get(i, "gray")
        body.append(f'<rect x="{x + 2:.1f}" y="{base - v * unit:.1f}" width="{bw - 4:.1f}" height="{v * unit:.1f}" '
                    f'fill="{fill_of(col)}" stroke="{stroke_of(col)}" stroke-width="1"/>')
        body.append(text(x + bw / 2, base - v * unit - 4, v, 10.5, mono=True))
        body.append(text(x + bw / 2, base + 14, i, 9.5, color=MUTED, mono=True))
    if "rect" in spec:
        rc = spec["rect"]
        x = pad + rc["from"] * bw
        w = (rc["to"] - rc["from"] + 1) * bw
        h = rc["height"] * unit
        body.append(f'<rect x="{x:.1f}" y="{base - h:.1f}" width="{w:.1f}" height="{h:.1f}" fill="none" '
                    f'stroke="{STROKE["red"]}" stroke-width="2.2" stroke-dasharray="5 3"/>')
        if rc.get("label"):
            body.append(text(x + w / 2, base - h / 2 + 4, rc["label"], 11, weight="700", color=STROKE["red"]))
    body.append(f'<line x1="{pad}" y1="{base:.1f}" x2="{width - pad}" y2="{base:.1f}" stroke="{LINE}" stroke-width="1.2"/>')
    return svg_wrap(width, height, body, spec.get("maxwidth"))


# --------------------------------------------------------------------------- intervals
def fig_intervals(spec):
    """Intervals on a number line.

    intervals: [{"s": 1, "e": 3, "label": "A", "color": "blue", "row": 0}]
    axis: [0, 10]; sweep: [4]; unit: 34
    Rows are assigned greedily when not given.
    """
    lo, hi = spec.get("axis", [0, 10])
    unit = spec.get("unit", 34)
    pad = 20
    ivs = spec["intervals"]
    rows_end = []
    placed = []
    for iv in ivs:
        if "row" in iv:
            row = iv["row"]
        else:
            row = None
            for k, end in enumerate(rows_end):
                if end < iv["s"]:
                    row = k
                    break
            if row is None:
                row = len(rows_end)
                rows_end.append(iv["e"])
            rows_end[row] = iv["e"]
        placed.append((row, iv))
    nrows = max(p[0] for p in placed) + 1
    rh = 26
    width = pad * 2 + (hi - lo) * unit
    height = pad + nrows * rh + 40
    axis_y = pad + nrows * rh + 8
    body = []
    for x in spec.get("sweep", []):
        px = pad + (x - lo) * unit
        body.append(f'<line x1="{px:.1f}" y1="{pad - 10}" x2="{px:.1f}" y2="{axis_y:.1f}" stroke="{STROKE["red"]}" stroke-width="1.5" stroke-dasharray="4 3"/>')
    for row, iv in placed:
        x1 = pad + (iv["s"] - lo) * unit
        x2 = pad + (iv["e"] - lo) * unit
        y = pad + row * rh
        col = iv.get("color", "blue")
        body.append(f'<rect x="{x1:.1f}" y="{y:.1f}" width="{max(x2 - x1, 4):.1f}" height="18" rx="4" fill="{fill_of(col)}" stroke="{stroke_of(col)}" stroke-width="1.2"/>')
        if iv.get("label"):
            body.append(text((x1 + x2) / 2, y + 13, iv["label"], 10.5, weight="600"))
    body.append(f'<line x1="{pad - 6}" y1="{axis_y:.1f}" x2="{width - pad + 6}" y2="{axis_y:.1f}" stroke="{LINE}" stroke-width="1.2"/>')
    step = spec.get("tick", 1)
    t = lo
    while t <= hi:
        px = pad + (t - lo) * unit
        body.append(f'<line x1="{px:.1f}" y1="{axis_y - 3:.1f}" x2="{px:.1f}" y2="{axis_y + 3:.1f}" stroke="{LINE}"/>')
        body.append(text(px, axis_y + 16, t, 9.5, color=MUTED, mono=True))
        t += step
    return svg_wrap(width, height, body, spec.get("maxwidth"))


# --------------------------------------------------------------------------- grid
def fig_grid(spec):
    """2-D table/grid (DP tables, BFS grids).

    cells: [["0", "1"], ["1", "#"]]   (strings; "" for empty)
    rowhead: ["", "a", "b"]; colhead: ["", "x", "y"]   (optional)
    colors: [{"r": 1, "c": 1, "color": "blue"}]
    arrows: [{"from": [0, 0], "to": [1, 1], "color": "blue"}]
    cell: 32
    """
    cells = spec["cells"]
    nr = len(cells)
    nc = max(len(r) for r in cells)
    cw = spec.get("cell", 32)
    chh = spec.get("cellh", cw)
    rowhead = spec.get("rowhead")
    colhead = spec.get("colhead")
    ox = 10 + (cw if rowhead else 0)
    oy = 10 + (22 if colhead else 0)
    width = ox + nc * cw + 10
    height = oy + nr * chh + 10
    colors = {(c["r"], c["c"]): c.get("color", "blue") for c in spec.get("colors", [])}
    body = []
    if colhead:
        for j, h in enumerate(colhead):
            body.append(text(ox + j * cw + cw / 2, oy - 7, h, 10.5, weight="600", color=MUTED, mono=True))
    if rowhead:
        for i, h in enumerate(rowhead):
            body.append(text(ox - 8, oy + i * chh + chh / 2 + 4, h, 10.5, "end", weight="600", color=MUTED, mono=True))
    dark = spec.get("dark", "#")
    for i, row in enumerate(cells):
        for j, v in enumerate(row):
            x = ox + j * cw
            y = oy + i * chh
            col = colors.get((i, j))
            v = "" if v is None else str(v)
            f = "#374151" if v == dark else fill_of(col)
            body.append(f'<rect x="{x:.1f}" y="{y:.1f}" width="{cw:.1f}" height="{chh:.1f}" fill="{f}" stroke="{LINE}" stroke-width="0.9"/>')
            if v and v != dark:
                body.append(text(x + cw / 2, y + chh / 2 + 4, v, 11 if len(v) < 4 else 9.5, mono=True))
    for a in spec.get("arrows", []):
        (r1, c1), (r2, c2) = a["from"], a["to"]
        x1, y1 = ox + c1 * cw + cw / 2, oy + r1 * chh + chh / 2
        x2, y2 = ox + c2 * cw + cw / 2, oy + r2 * chh + chh / 2
        d = math.hypot(x2 - x1, y2 - y1) or 1
        shrink = min(cw, chh) * 0.32
        x1 += (x2 - x1) / d * shrink
        y1 += (y2 - y1) / d * shrink
        x2 -= (x2 - x1) / d * shrink
        y2 -= (y2 - y1) / d * shrink
        color = a.get("color", "blue")
        body.append(f'<line x1="{x1:.1f}" y1="{y1:.1f}" x2="{x2:.1f}" y2="{y2:.1f}" stroke="{stroke_of(color)}" stroke-width="1.6" marker-end="url(#{marker_for(color)})"/>')
    return svg_wrap(width, height, body, spec.get("maxwidth"))


RENDERERS = {
    "array": fig_array, "tree": fig_tree, "graph": fig_graph,
    "bars": fig_bars, "intervals": fig_intervals, "grid": fig_grid,
}


def render(spec):
    kind = spec.get("type")
    if kind not in RENDERERS:
        raise ValueError(f"unknown figure type {kind!r}; use one of {sorted(RENDERERS)}")
    return RENDERERS[kind](spec)
