#!/usr/bin/env python3
"""Resolve an add/add conflict on a tag or font JSON by concatenating the two lists.

Separate mods could each ship `data/<ns>/tags/...json` or `assets/minecraft/font/default.json` and the game
merged them across resource packs. Inside one jar only one file per path survives, so the lists are merged here.
独立 mod 可以各自提供同一路径的标签或字体 JSON，游戏会跨资源包合并；合成一个 jar 后同一路径只能留一份，
因此在这里把两边的列表合并。

usage: merge-json-conflict.py <path>...   (run inside a conflicted merge)
"""
import json
import subprocess
import sys


def stage(n, path):
    return json.loads(subprocess.check_output(["git", "show", f":{n}:{path}"]).decode("utf-8"))


for path in sys.argv[1:]:
    ours, theirs = stage(2, path), stage(3, path)
    keys = [k for k in ("values", "providers") if k in ours or k in theirs]
    if len(keys) != 1:
        sys.exit(f"{path}: expected exactly one of values/providers")
    key = keys[0]
    merged = dict(ours)
    merged[key] = list(ours.get(key, []))
    for item in theirs.get(key, []):
        if item not in merged[key]:
            merged[key].append(item)
    for other in theirs:
        if other != key and theirs[other] != ours.get(other):
            sys.exit(f"{path}: differing '{other}' values need a manual decision")
    with open(path, "w", encoding="utf-8", newline="\n") as out:
        # ensure_ascii keeps private-use glyphs as \uXXXX escapes, readable in diffs.
        # ensure_ascii 让私有区字形保持 \uXXXX 转义，diff 里可读。
        json.dump(merged, out, indent=2, ensure_ascii=True)
        out.write("\n")
    subprocess.check_call(["git", "add", path])
    print(f"merged {path}: {len(merged[key])} {key}")
