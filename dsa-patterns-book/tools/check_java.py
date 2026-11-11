"""Compile every Java file with javac --release 8 and run each test main.

A file passes when its main() prints "PASS <name> <n> checks" and exits 0.
Also checks that every code region the book prints exists and fits the page width.
Writes data/java-results.json, which the build uses for the numbers on the cover.

    python tools/check_java.py              # everything
    python tools/check_java.py --part 9     # only the files part 9 prints (isolated build dir)
"""

import json
import re
import subprocess
import sys
import time
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "java"
OUT = ROOT / "build" / "classes"
MAX_COLS = 82          # printable width of a code line at 9 pt on A4
MAX_LINES = 52         # a region must fit on one page
SHARED = ["Check.java", "Nodes.java"]


def part_files(part):
    f = part["file"]
    return f if isinstance(f, list) else [f]


def load_patterns(part_filter=None):
    manifest = yaml.safe_load((ROOT / "content" / "manifest.yaml").read_text())
    out = []
    for part in manifest["parts"]:
        if part_filter and part["num"] not in part_filter:
            continue
        for fn in part_files(part):
            f = ROOT / "content" / fn
            if f.exists():
                out.extend((yaml.safe_load(f.read_text()) or {}).get("patterns", []))
    if not part_filter:
        fy = ROOT / "content" / "front.yaml"
        if fy.exists():
            kit = (yaml.safe_load(fy.read_text()) or {}).get("javakit") or {}
            if kit.get("code"):
                out.append({"id": "javakit", "code": kit["code"]})
    return out


def regions(text):
    """name -> list of lines for every //@tpl name ... //@end block."""
    found, cur, buf = {}, None, []
    for line in text.splitlines():
        s = line.strip()
        if s.startswith("//@tpl "):
            cur, buf = s.split(None, 1)[1].strip(), []
        elif s == "//@end" and cur:
            found[cur] = buf
            cur = None
        elif cur is not None:
            buf.append(line)
    return found


def dedent(lines):
    ind = min((len(l) - len(l.lstrip()) for l in lines if l.strip()), default=0)
    out = [l[ind:].rstrip() for l in lines]
    while out and not out[0]:
        out.pop(0)
    while out and not out[-1]:
        out.pop()
    return out


def check_regions(patterns):
    errors = []
    used = 0
    if True:
        for p in patterns:
            for c in p.get("code", []):
                src = JAVA / c["file"]
                if not src.exists():
                    errors.append(f"{p['id']}: missing {c['file']}")
                    continue
                reg = regions(src.read_text())
                if c["region"] not in reg:
                    errors.append(f"{p['id']}: no region '{c['region']}' in {c['file']}")
                    continue
                lines = dedent(reg[c["region"]])
                used += 1
                for i, l in enumerate(lines, 1):
                    if len(l) > MAX_COLS:
                        errors.append(f"{c['file']}#{c['region']} line {i} has {len(l)} cols (max {MAX_COLS})")
                    if "\t" in l:
                        errors.append(f"{c['file']}#{c['region']} line {i} has a tab")
                if len(lines) > MAX_LINES:
                    errors.append(f"{c['file']}#{c['region']} has {len(lines)} lines (max {MAX_LINES})")
    return used, errors


def main():
    args = sys.argv[1:]
    parts = set()
    if "--part" in args:
        i = args.index("--part")
        parts = {int(x) for x in args[i + 1].split(",")}
        args = args[:i] + args[i + 2:]
    only = set(args)
    patterns = load_patterns(parts)
    used, errors = check_regions(patterns)
    for e in errors:
        print("REGION", e)
    out = OUT
    if parts:
        names = {c["file"] for p in patterns for c in p.get("code", [])} | set(SHARED)
        files = sorted(JAVA / n for n in names if (JAVA / n).exists())
        import os
        out = ROOT / "build" / ("classes-part" + "-".join(str(p) for p in sorted(parts)) + f"-{os.getpid()}")
    else:
        files = sorted(JAVA.glob("*.java"))
    out.mkdir(parents=True, exist_ok=True)
    for old in out.glob("*.class"):
        old.unlink()
    t0 = time.time()
    r = subprocess.run(["javac", "--release", "8", "-nowarn", "-encoding", "UTF-8", "-d", str(out)]
                       + [str(f) for f in files], capture_output=True, text=True)
    if r.returncode != 0:
        print(r.stdout + r.stderr)
        print("COMPILE FAILED")
        sys.exit(1)
    print(f"compiled {len(files)} files with javac --release 8 in {time.time() - t0:.1f}s")

    results, failed = {}, []
    for f in files:
        text = f.read_text()
        if "public static void main" not in text or "@notest" in text:
            continue
        name = re.search(r"public\s+(?:final\s+)?class\s+(\w+)", text)
        if not name:
            continue
        cls = name.group(1)
        if only and cls not in only:
            continue
        t0 = time.time()
        try:
            run = subprocess.run(["java", "-Xss64m", "-cp", str(out), cls], capture_output=True,
                                 text=True, timeout=120)
            log = (run.stdout + run.stderr).strip()
        except subprocess.TimeoutExpired:
            log, run = "TIMEOUT", None
        last = log.splitlines()[-1] if log else ""
        m = re.match(r"PASS (\w+) (\d+) checks", last)
        ok = run is not None and run.returncode == 0 and m is not None
        if ok:
            results[cls] = int(m.group(2))
            print(f"  ok   {cls:<28} {int(m.group(2)):>9,} checks  {time.time() - t0:5.1f}s")
        else:
            failed.append(cls)
            print(f"  FAIL {cls}: {log[-600:]}")

    if not only and not parts:
        (ROOT / "data").mkdir(exist_ok=True)
        (ROOT / "data" / "java-results.json").write_text(json.dumps(
            {"files": len(files), "regions": used, "classes": results,
             "checks": sum(results.values())}, indent=1) + "\n")
    if parts:
        import shutil
        shutil.rmtree(out, ignore_errors=True)
    print(f"{len(results)} test classes passed, {sum(results.values()):,} checks, "
          f"{used} printed code regions, {len(failed)} failed, {len(errors)} region errors")
    sys.exit(1 if failed or errors else 0)


if __name__ == "__main__":
    main()
