"""Download the open-source fonts the book uses (once) and emit @font-face rules.

Atkinson Hyperlegible Next (body), Nunito (headings), JetBrains Mono (code) and
Noto Sans / Math / Symbols 2 (fallback for arrows, sub- and superscripts).
Nunito and Noto Sans ship as variable fonts; fontTools cuts static weights from them.
"""

import io
import urllib.request
import zipfile
from pathlib import Path

DIR = Path(__file__).resolve().parent / ".fonts"

ATK = "https://raw.githubusercontent.com/googlefonts/atkinson-hyperlegible-next/main/fonts/ttf/"
GF = "https://raw.githubusercontent.com/google/fonts/main/ofl/"
RAW = {f"AtkinsonHyperlegibleNext-{w}.ttf": ATK + f"AtkinsonHyperlegibleNext-{w}.ttf"
       for w in ("Regular", "Italic", "SemiBold", "Bold", "BoldItalic")}
RAW.update({
    "Nunito-VF.ttf": GF + "nunito/Nunito%5Bwght%5D.ttf",
    "NotoSans-VF.ttf": GF + "notosans/NotoSans%5Bwdth,wght%5D.ttf",
    "NotoSansMath-Regular.ttf": GF + "notosansmath/NotoSansMath-Regular.ttf",
    "NotoSansSymbols2-Regular.ttf": GF + "notosanssymbols2/NotoSansSymbols2-Regular.ttf",
})
JBM = "https://github.com/JetBrains/JetBrainsMono/releases/download/v2.304/JetBrainsMono-2.304.zip"
JBM_FILES = [f"JetBrainsMono-{w}.ttf" for w in ("Regular", "Italic", "SemiBold", "Bold")]

FACES = [
    ("Atkinson", "AtkinsonHyperlegibleNext-Regular.ttf", 400, "normal"),
    ("Atkinson", "AtkinsonHyperlegibleNext-Italic.ttf", 400, "italic"),
    ("Atkinson", "AtkinsonHyperlegibleNext-SemiBold.ttf", 600, "normal"),
    ("Atkinson", "AtkinsonHyperlegibleNext-Bold.ttf", 700, "normal"),
    ("Atkinson", "AtkinsonHyperlegibleNext-BoldItalic.ttf", 700, "italic"),
    ("Nunito", "Nunito-600.ttf", 600, "normal"),
    ("Nunito", "Nunito-700.ttf", 700, "normal"),
    ("Nunito", "Nunito-800.ttf", 800, "normal"),
    ("Nunito", "Nunito-900.ttf", 900, "normal"),
    ("JB Mono", "JetBrainsMono-Regular.ttf", 400, "normal"),
    ("JB Mono", "JetBrainsMono-Italic.ttf", 400, "italic"),
    ("JB Mono", "JetBrainsMono-SemiBold.ttf", 600, "normal"),
    ("JB Mono", "JetBrainsMono-Bold.ttf", 700, "normal"),
    ("Noto Sans", "NotoSans-Regular.ttf", 400, "normal"),
    ("Noto Sans", "NotoSans-Bold.ttf", 700, "normal"),
    ("Noto Sans Math", "NotoSansMath-Regular.ttf", 400, "normal"),
    ("Noto Sans Symbols 2", "NotoSansSymbols2-Regular.ttf", 400, "normal"),
]


def ensure():
    DIR.mkdir(exist_ok=True)
    for name, url in RAW.items():
        if not (DIR / name).exists():
            print("downloading", url)
            (DIR / name).write_bytes(urllib.request.urlopen(url, timeout=120).read())
    if not all((DIR / f).exists() for f in JBM_FILES):
        print("downloading", JBM)
        with zipfile.ZipFile(io.BytesIO(urllib.request.urlopen(JBM, timeout=120).read())) as z:
            for f in JBM_FILES:
                (DIR / f).write_bytes(z.read("fonts/ttf/" + f))
    from fontTools.ttLib import TTFont
    from fontTools.varLib.instancer import instantiateVariableFont
    for w in (600, 700, 800, 900):
        out = DIR / f"Nunito-{w}.ttf"
        if not out.exists():
            instantiateVariableFont(TTFont(DIR / "Nunito-VF.ttf"), {"wght": w}, static=True).save(out)
    for w, name in ((400, "Regular"), (700, "Bold")):
        out = DIR / f"NotoSans-{name}.ttf"
        if not out.exists():
            instantiateVariableFont(TTFont(DIR / "NotoSans-VF.ttf"), {"wght": w, "wdth": 100},
                                    static=True).save(out)


def font_face_css():
    return "\n".join(
        f'@font-face {{ font-family: "{fam}"; src: url("{(DIR / file).as_uri()}"); '
        f"font-weight: {weight}; font-style: {style}; }}"
        for fam, file, weight, style in FACES)
