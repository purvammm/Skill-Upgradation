"""Look up every practice problem on LeetCode and cache number, title, difficulty.

Fails if a slug does not exist or the problem is Premium-only, so every printed
problem is real and free to open. Results go to data/leetcode.json (the build reads it).

    python tools/check_leetcode.py            # fetch missing slugs only
    python tools/check_leetcode.py --refresh  # re-check every slug
    python tools/check_leetcode.py --part 9   # only part 9, cached in data/leetcode-part9.json
"""

import json
import sys
import time
import urllib.request
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parent.parent
CACHE = ROOT / "data" / "leetcode.json"
QUERY = ("query q($s: String!) { question(titleSlug: $s) "
         "{ questionFrontendId title difficulty isPaidOnly } }")


def all_slugs(part_filter=None):
    manifest = yaml.safe_load((ROOT / "content" / "manifest.yaml").read_text())
    seen = {}
    for part in manifest["parts"]:
        if part_filter and part["num"] not in part_filter:
            continue
        files = part["file"] if isinstance(part["file"], list) else [part["file"]]
        for fn in files:
            f = ROOT / "content" / fn
            if not f.exists():
                continue
            for p in (yaml.safe_load(f.read_text()) or {}).get("patterns", []):
                for s in p.get("practice", []):
                    seen.setdefault(s, p["id"])
    return seen


def fetch(slug):
    body = json.dumps({"query": QUERY, "variables": {"s": slug}}).encode()
    req = urllib.request.Request("https://leetcode.com/graphql", data=body, headers={
        "Content-Type": "application/json", "Referer": "https://leetcode.com",
        "User-Agent": "Mozilla/5.0"})
    for attempt in range(4):
        try:
            with urllib.request.urlopen(req, timeout=30) as r:
                return json.loads(r.read())["data"]["question"]
        except Exception as e:  # network hiccup: back off and retry
            if attempt == 3:
                raise
            time.sleep(2 + 3 * attempt)


def main():
    refresh = "--refresh" in sys.argv
    parts = set()
    if "--part" in sys.argv:
        parts = {int(x) for x in sys.argv[sys.argv.index("--part") + 1].split(",")}
    cache_file = CACHE.with_name("leetcode-part" + "-".join(map(str, sorted(parts))) + ".json") if parts else CACHE
    cache = {}
    for f in sorted(CACHE.parent.glob("leetcode*.json")):   # reuse anything already looked up
        cache.update(json.loads(f.read_text()))
    slugs = all_slugs(parts)
    errors = []
    for slug, pid in sorted(slugs.items()):
        if slug in cache and not refresh:
            continue
        q = fetch(slug)
        if q is None:
            errors.append(f"{pid}: unknown slug {slug}")
            continue
        cache[slug] = {"id": int(q["questionFrontendId"]), "title": q["title"],
                       "difficulty": q["difficulty"], "paid": q["isPaidOnly"]}
        time.sleep(0.25)
    for slug, pid in sorted(slugs.items()):
        if slug in cache and cache[slug]["paid"]:
            errors.append(f"{pid}: {slug} is Premium-only")
    CACHE.parent.mkdir(exist_ok=True)
    keep = {s: cache[s] for s in slugs if s in cache}    # only the slugs the book uses
    cache_file.write_text(json.dumps(dict(sorted(keep.items())), indent=1, ensure_ascii=False) + "\n")
    used = [s for s in slugs if s in cache]
    print(f"{len(slugs)} practice slugs, {len(used)} resolved, "
          f"{sum(1 for s in used if not cache[s]['paid'])} free")
    for e in errors:
        print("ERROR", e)
    sys.exit(1 if errors else 0)


if __name__ == "__main__":
    main()
