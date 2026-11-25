#!/usr/bin/env python3
"""Compile every main solution exactly as it is printed in the book.

Each card's code goes into its own package with only `java.util.*` and LeetCode's
ListNode / TreeNode available, which is what the LeetCode editor gives you.
"""
import subprocess
import sys
import tempfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import build  # noqa: E402

NODES = {
    "ListNode": "public class ListNode { public int val; public ListNode next; public ListNode() {} "
                "public ListNode(int val) { this.val = val; } "
                "public ListNode(int val, ListNode next) { this.val = val; this.next = next; } }",
    "TreeNode": "public class TreeNode { public int val; public TreeNode left, right; public TreeNode() {} "
                "public TreeNode(int val) { this.val = val; } "
                "public TreeNode(int val, TreeNode left, TreeNode right) { this.val = val; this.left = left; this.right = right; } }",
}


def main():
    problems, _, _ = build.load_sources()
    with tempfile.TemporaryDirectory() as tmp:
        root = Path(tmp)
        (root / "lc").mkdir()
        for name, body in NODES.items():
            (root / "lc" / (name + ".java")).write_text("package lc;\n" + body + "\n")
        files = [str(root / "lc" / (n + ".java")) for n in NODES]
        for p in problems:
            pkg = "p%d" % p["num"]
            (root / pkg).mkdir()
            code = "\n".join(text for text, _ in p["code"])
            cls = p["fields"].get("class", ["Solution"])[0]
            f = root / pkg / (cls + ".java")
            f.write_text("package %s;\nimport java.util.*;\nimport lc.*;\n\n%s\n" % (pkg, code))
            files.append(str(f))
        out = subprocess.run(["javac", "-nowarn", "-Xlint:none", "-d", str(root / "out")] + files,
                             capture_output=True, text=True)
        errors = [ln for ln in out.stderr.splitlines() if "error" in ln]
        if out.returncode != 0:
            print(out.stderr)
            sys.exit("printed code does not compile (%d errors)" % len(errors))
        print("all %d printed solutions compile as standalone LeetCode classes" % len(problems))


if __name__ == "__main__":
    main()
