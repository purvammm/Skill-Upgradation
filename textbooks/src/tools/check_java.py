#!/usr/bin/env python3
"""Compile every Java block in the book sources with `javac --release 8`.

    python tools/check_java.py                 # all chapters of both books
    python tools/check_java.py theory/ch02.md  # some files

Rules for a ```java block:
  * contains "public class Main" (or any "public class X" with a main method)
        -> compiled on its own as that file;
  * otherwise -> wrapped as members of a class with the usual imports, so it may hold
        static methods, static nested classes, fields, or LeetCode-style "class Solution { ... }";
  * {.fragment} -> wrapped inside a method body (statements only);
  * {.nocompile} -> skipped (use only for deliberately broken or partial code,
        e.g. a "wrong" version shown next to the fix, or code with ____ blanks).
Exit status is 1 if any block fails.
"""

import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

HERE = Path(__file__).resolve().parent.parent
FENCE = re.compile(r"^(\s*)(`{3,})\s*java\b\s*(\{[^}]*\})?\s*(.*)$")
IMPORTS = ("import java.io.*;\nimport java.util.*;\nimport java.util.function.*;\n"
           "import java.util.stream.*;\nimport java.math.*;\n")


def blocks(path):
    lines = path.read_text(encoding="utf8").split("\n")
    i = 0
    while i < len(lines):
        m = FENCE.match(lines[i])
        if m:
            ind, fence, attrs, title = len(m.group(1)), m.group(2), m.group(3) or "", m.group(4)
            j = i + 1
            body = []
            while j < len(lines) and lines[j].strip() != fence:
                body.append(lines[j][ind:] if lines[j][:ind].strip() == "" else lines[j])
                j += 1
            yield i + 1, attrs, title, "\n".join(body)
            i = j + 1
        else:
            i += 1


def compile_block(code, attrs, work):
    if ".nocompile" in attrs:
        return None, "skipped"
    m = re.search(r"public\s+(?:final\s+)?class\s+(\w+)", code)
    if m and "static void main" in code:
        name = m.group(1)
        src = code if "import " in code else IMPORTS + code
    elif ".fragment" in attrs:
        name = "Frag"
        src = IMPORTS + "public class Frag {\n    static void body() throws Exception {\n" + code + "\n    }\n}\n"
    else:
        name = "Snippet"
        body = re.sub(r"^\s*import\s+[\w.*]+;\s*$", "", code, flags=re.M)
        src = IMPORTS + "@SuppressWarnings(\"unchecked\")\npublic class Snippet {\n" + body + "\n}\n"
    d = Path(tempfile.mkdtemp(dir=work))
    (d / f"{name}.java").write_text(src, encoding="utf8")
    r = subprocess.run(["javac", "--release", "8", "-Xlint:-options", "-nowarn", "-d", str(d), str(d / f"{name}.java")],
                       capture_output=True, text=True)
    ok = r.returncode == 0
    return ok, (r.stderr if not ok else "")


def main():
    files = [Path(a) for a in sys.argv[1:]] or sorted(list((HERE / "theory").glob("*.md")) + list((HERE / "workbook").glob("*.md")))
    work = Path(tempfile.mkdtemp(prefix="javacheck-"))
    total = failed = skipped = 0
    try:
        for f in files:
            f = f if f.is_absolute() else (Path.cwd() / f)
            for line, attrs, title, code in blocks(f):
                total += 1
                ok, err = compile_block(code, attrs, work)
                if ok is None:
                    skipped += 1
                elif not ok:
                    failed += 1
                    print(f"FAIL {f.relative_to(HERE)}:{line} {title}\n{err}")
    finally:
        shutil.rmtree(work, ignore_errors=True)
    print(f"{total} Java blocks: {total - failed - skipped} compiled, {failed} failed, {skipped} skipped (.nocompile)")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
