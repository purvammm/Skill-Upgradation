#!/usr/bin/env bash
# Compile every source file and run each day's tests. Exit code is non-zero if anything fails.
set -euo pipefail
cd "$(dirname "$0")"
OUT="${OUT_DIR:-$(mktemp -d)}"
javac -nowarn -Xlint:none -d "$OUT" src/*.java 2>&1 | grep -v '^Note:' || true
status=0
for f in src/Day*.java src/Bonus*.java; do
  [ -e "$f" ] || continue
  cls="$(basename "$f" .java)"
  java -Xss64m -cp "$OUT" "$cls" || status=1
done
exit $status
