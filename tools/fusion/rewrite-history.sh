#!/usr/bin/env bash
# Rewrites one legacy Spark repository into the SparkDLC layout, keeping its full history.
# 把一个旧 Spark 仓库连同完整历史改写成 SparkDLC 的目录与包结构。
#
# usage: rewrite-history.sh <module> <legacy-repo-path> <ref> <out-dir>
#   module  one of the first column of modules.tsv (factionapi, strength, traits, witch, assist)
#   ref     ref inside the legacy repo, normally refs/remotes/origin/main
#
# The rewrite is deterministic: the same legacy commits always produce the same rewritten commits, so a
# later run on a newer legacy tip can simply be merged into SparkDLC (see README.md).
# 改写是确定性的：同样的旧提交总是得到同样的新提交，所以旧仓库之后的新提交可以直接 merge 进 SparkDLC。
set -euo pipefail

here="$(cd "$(dirname "$0")" && pwd)"
module="$1"; legacy="$2"; ref="$3"; out="$4"

row="$(grep -v '^#' "$here/modules.tsv" | awk -F'\t' -v m="$module" '$1 == m')"
[ -n "$row" ] || { echo "unknown module: $module" >&2; exit 2; }
repo_slug="$(printf '%s' "$row" | cut -f2)"
legacy_pkg="$(printf '%s' "$row" | cut -f3)"
legacy_pkg_path="${legacy_pkg//.//}"

command -v git-filter-repo >/dev/null || { echo "git-filter-repo is not on PATH" >&2; exit 2; }

rm -rf "$out"
git init -q --bare -b main "$out"
git -C "$out" fetch -q --no-tags "$legacy" "$ref:refs/heads/main"

git -C "$out" filter-repo --force --quiet \
  --replace-text "$here/replace-text.txt" \
  --filename-callback "
import re
MODULE = b'$module'
OLD = b'$legacy_pkg_path/'
NEW = b'dev/caecorthus/sparkdlc/' + MODULE + b'/'
# Dependency and wrapper jars are re-added once at the SparkDLC root; their history is only bulk.
# 依赖 jar 与 wrapper jar 在 SparkDLC 根目录统一提供，旧历史里的副本只会增大体积。
if filename.endswith(b'.jar'):
    return None
m = re.match(rb'^(src/(?:main|client|test)/java/)(.*)$', filename)
if m:
    rest = m.group(2)
    if rest.startswith(OLD):
        rest = NEW + rest[len(OLD):]
    return m.group(1) + rest
# One fabric.mod.json per legacy mod cannot share a path; SparkDLC writes a single merged one.
# 五个旧 mod 各有一份 fabric.mod.json，不能共用路径；SparkDLC 只保留一份合并后的。
if filename == b'src/main/resources/fabric.mod.json':
    return b'legacy/' + MODULE + b'/fabric.mod.json'
if filename.startswith(b'src/'):
    return filename
return b'legacy/' + MODULE + b'/' + filename
" \
  --message-callback "
import re
SELF = b'$repo_slug'
ALIASES = {
    b'SFA': b'Caecorthus/SparkFactionAPI', b'SparkFactionAPI': b'Caecorthus/SparkFactionAPI',
    b'SS': b'Caecorthus/SparkStrength', b'SparkStrength': b'Caecorthus/SparkStrength',
    b'ST': b'Caecorthus/SparkTraits', b'SparkTraits': b'Caecorthus/SparkTraits',
    b'SW': b'Caecorthus/SparkWitch', b'SparkWitch': b'Caecorthus/SparkWitch',
    b'SA': b'Caecorthus/SparkAssist', b'SparkAssist': b'Caecorthus/SparkAssist',
}
# Bare '#12' would link to SparkDLC's own issue 12 on GitHub; point it back at the legacy repository.
# 裸写的 '#12' 在 GitHub 上会指向 SparkDLC 自己的 12 号；这里改回指向旧仓库。
def named(m):
    return ALIASES[m.group(1)] + b'#' + m.group(2)
message = re.sub(rb'(?<![\w/])(SFA|SS|ST|SW|SA|SparkFactionAPI|SparkStrength|SparkTraits|SparkWitch|SparkAssist) ?#(\d{1,4})(?![0-9A-Za-z])', named, message)
message = re.sub(rb'(?<![\w/&#])#(\d{1,4})(?![0-9A-Za-z])', lambda m: SELF + b'#' + m.group(1), message)
return message
"

echo "$module: $(git -C "$out" rev-list --count main) commits, tip $(git -C "$out" rev-parse --short main)"
