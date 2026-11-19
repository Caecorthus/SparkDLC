#!/usr/bin/env python3
"""Copy a legacy repository's local-only test tree (src/test, gitignored) into SparkDLC's src/test.

Applies the same package/path rewrite as the history fusion (replace-text.txt). Files that SparkDLC already
tracks are left alone. The result stays local-only, like the legacy test trees.
把旧仓库仅本地的测试目录（src/test，不进 git）复制到 SparkDLC 的 src/test，并应用与历史融合相同的包名/路径改写
（replace-text.txt）。SparkDLC 已跟踪的文件不会被覆盖。结果同样只留在本地。

usage: import-local-tests.py <module> <legacy-repo-path> [--dry-run] [--raw]
  --raw  keep the whole tree under src/test/<module>/ instead of the Gradle test source set (SparkStrength's
         suite is a Python harness plus plain `main` classes, not JUnit)
         整棵树放到 src/test/<module>/ 下，不进入 Gradle 的 test 源码集（SparkStrength 的测试是 Python 工具加普通
         `main` 类，不是 JUnit）
"""
import os
import subprocess
import sys

here = os.path.dirname(os.path.abspath(__file__))
root = os.path.abspath(os.path.join(here, "..", ".."))
module, legacy = sys.argv[1], sys.argv[2]
dry = "--dry-run" in sys.argv
raw = "--raw" in sys.argv

rows = [line.rstrip("\n").split("\t") for line in open(os.path.join(here, "modules.tsv")) if not line.startswith("#")]
legacy_pkg = {r[0]: r[2] for r in rows}[module]
old_dir = legacy_pkg.replace(".", "/") + "/"
new_dir = f"dev/caecorthus/sparkdlc/{module}/"
rules = [line.rstrip("\n").split("==>") for line in open(os.path.join(here, "replace-text.txt")) if "==>" in line]
tracked = set(subprocess.check_output(["git", "-C", root, "ls-files", "src/test"]).decode().split("\n"))

copied = skipped = 0
src_root = os.path.join(legacy, "src", "test")
for folder, _, names in os.walk(src_root):
    for name in names:
        if name == ".DS_Store" or "__pycache__" in folder:
            continue
        rel = os.path.relpath(os.path.join(folder, name), src_root)
        # Only JUnit sources go into the Gradle test source set; anything else keeps a per-module home.
        # 只有 JUnit 源码进入 Gradle 的 test 源码集；其他内容按模块单独存放。
        if raw:
            target = f"{module}/{rel}".replace(old_dir, new_dir, 1)
        elif rel.startswith("java/"):
            target = "java/" + rel[5:].replace(old_dir, new_dir, 1)
        elif rel.startswith("resources/"):
            target = rel
        else:
            target = f"{module}/{rel}"
        target = os.path.join("src", "test", target)
        if target in tracked:
            skipped += 1
            continue
        data = open(os.path.join(folder, name), "rb").read()
        if b"\0" not in data[:8192]:
            for old, new in rules:
                data = data.replace(old.encode(), new.encode())
        copied += 1
        if dry:
            continue
        out = os.path.join(root, target)
        os.makedirs(os.path.dirname(out), exist_ok=True)
        with open(out, "wb") as handle:
            handle.write(data)
print(f"{module}: copied {copied}, kept {skipped} tracked")
