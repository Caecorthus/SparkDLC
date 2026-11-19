#!/usr/bin/env bash
# Merges one rewritten legacy history (branch legacy/<module>) into the current branch.
# 把一条改写后的旧历史（分支 legacy/<module>）合并进当前分支。
#
# usage: merge-module.sh <module> <legacy-sha>
#   legacy-sha  the commit of the legacy repository the rewrite was made from, for the merge message
set -euo pipefail

here="$(cd "$(dirname "$0")" && pwd)"
module="$1"; legacy_sha="$2"

row="$(grep -v '^#' "$here/modules.tsv" | awk -F'\t' -v m="$module" '$1 == m')"
[ -n "$row" ] || { echo "unknown module: $module" >&2; exit 2; }
repo_slug="$(printf '%s' "$row" | cut -f2)"

git merge -q --no-ff --allow-unrelated-histories --no-commit "legacy/$module" || true

# Tag and font JSON shared by several legacy mods conflict as add/add; their lists are concatenated.
# 多个旧 mod 共有的标签与字体 JSON 会产生 add/add 冲突，这里把列表拼接起来。
git diff --name-only --diff-filter=U | while IFS= read -r path; do
  python3 "$here/merge-json-conflict.py" "$path"
done

if [ -n "$(git diff --name-only --diff-filter=U)" ]; then
  echo "unresolved conflicts remain; resolve them and commit by hand" >&2
  exit 1
fi

git commit -q -m "merge: bring in ${repo_slug#*/} as the $module module

Rewritten from $repo_slug main at $legacy_sha by tools/fusion/rewrite-history.sh.
Java sources live under dev.caecorthus.sparkdlc.$module; repository-root files sit in legacy/$module
until they are folded into the unified build.

Co-Authored-By: Claude Fable 5.2 <noreply@anthropic.com>"
echo "merged $module ($repo_slug@$legacy_sha)"
