"""Download the open-source fonts the books use (once) and emit @font-face rules."""

import io
import urllib.request
import zipfile
from pathlib import Path

DIR = Path(__file__).resolve().parent.parent / ".fonts"

ZIPS = {
    "https://github.com/adobe-fonts/source-serif/releases/download/4.005R/source-serif-4.005_Desktop.zip":
        {f"source-serif-4.005_Desktop/OTF/SourceSerif4-{w}.otf": f"SourceSerif4-{w}.otf"
         for w in ("Regular", "It", "Semibold", "SemiboldIt", "Bold", "BoldIt")},
    "https://github.com/adobe-fonts/source-sans/releases/download/3.052R/OTF-source-sans-3.052R.zip":
        {f"OTF/SourceSans3-{w}.otf": f"SourceSans3-{w}.otf"
         for w in ("Regular", "It", "Medium", "Semibold", "Bold", "Black")},
    "https://github.com/JetBrains/JetBrainsMono/releases/download/v2.304/JetBrainsMono-2.304.zip":
        {f"fonts/ttf/JetBrainsMono-{w}.ttf": f"JetBrainsMono-{w}.ttf" for w in ("Regular", "Italic", "Medium", "Bold")},
}
RAW = {
    "NotoSansMath-Regular.ttf": "https://raw.githubusercontent.com/google/fonts/main/ofl/notosansmath/NotoSansMath-Regular.ttf",
    "NotoSansSymbols2-Regular.ttf": "https://raw.githubusercontent.com/google/fonts/main/ofl/notosanssymbols2/NotoSansSymbols2-Regular.ttf",
}

FACES = [
    ("Source Serif 4", "SourceSerif4-Regular.otf", 400, "normal"),
    ("Source Serif 4", "SourceSerif4-It.otf", 400, "italic"),
    ("Source Serif 4", "SourceSerif4-Semibold.otf", 600, "normal"),
    ("Source Serif 4", "SourceSerif4-SemiboldIt.otf", 600, "italic"),
    ("Source Serif 4", "SourceSerif4-Bold.otf", 700, "normal"),
    ("Source Serif 4", "SourceSerif4-BoldIt.otf", 700, "italic"),
    ("Source Sans 3", "SourceSans3-Regular.otf", 400, "normal"),
    ("Source Sans 3", "SourceSans3-It.otf", 400, "italic"),
    ("Source Sans 3", "SourceSans3-Medium.otf", 500, "normal"),
    ("Source Sans 3", "SourceSans3-Semibold.otf", 600, "normal"),
    ("Source Sans 3", "SourceSans3-Bold.otf", 700, "normal"),
    ("Source Sans 3", "SourceSans3-Black.otf", 900, "normal"),
    ("JetBrains Mono", "JetBrainsMono-Regular.ttf", 400, "normal"),
    ("JetBrains Mono", "JetBrainsMono-Italic.ttf", 400, "italic"),
    ("JetBrains Mono", "JetBrainsMono-Medium.ttf", 500, "normal"),
    ("JetBrains Mono", "JetBrainsMono-Bold.ttf", 700, "normal"),
    ("Noto Sans Math", "NotoSansMath-Regular.ttf", 400, "normal"),
    ("Noto Sans Symbols 2", "NotoSansSymbols2-Regular.ttf", 400, "normal"),
]


def ensure():
    DIR.mkdir(exist_ok=True)
    for url, members in ZIPS.items():
        if all((DIR / dst).exists() for dst in members.values()):
            continue
        print("downloading", url)
        data = urllib.request.urlopen(url, timeout=120).read()
        with zipfile.ZipFile(io.BytesIO(data)) as z:
            for src, dst in members.items():
                (DIR / dst).write_bytes(z.read(src))
    for name, url in RAW.items():
        if not (DIR / name).exists():
            print("downloading", url)
            (DIR / name).write_bytes(urllib.request.urlopen(url, timeout=120).read())


def font_face_css():
    rules = []
    for fam, file, weight, style in FACES:
        rules.append(f'@font-face {{ font-family: "{fam}"; src: url("{(DIR / file).as_uri()}"); '
                     f'font-weight: {weight}; font-style: {style}; }}')
    return "<style>\n" + "\n".join(rules) + "\n</style>"
