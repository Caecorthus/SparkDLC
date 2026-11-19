# History fusion

SparkDLC was made by rewriting the five legacy repositories into this layout and merging their histories.
The tooling is kept so later legacy commits can be brought in the same way.

| File | Purpose |
| --- | --- |
| `modules.tsv` | module name, legacy GitHub repository, legacy Java package, legacy mod id |
| `replace-text.txt` | the literal package/path replacements applied to every text blob |
| `rewrite-history.sh` | rewrites one legacy repository (paths, package names, `#123` references) with `git filter-repo` |
| `merge-module.sh` | merges a rewritten history (branch `legacy/<module>`) into the current branch |
| `merge-json-conflict.py` | concatenates tag/font JSON lists that several legacy mods ship at the same path |
| `import-local-tests.py` | copies a legacy repository's local-only `src/test` tree with the same rewrite |

## What the rewrite does

- `src/{main,client,test}/java/<legacy package>/...` moves to `src/.../java/dev/caecorthus/sparkdlc/<module>/...`.
- Every occurrence of a legacy package name (dotted or slashed) in any text file becomes the new one, in every
  commit, so each historical commit is consistent with its own paths.
- `src/main/resources/fabric.mod.json` and every file outside `src/` move to `legacy/<module>/...`; SparkDLC then
  folds them into the unified build. Jars are dropped from history.
- Resource paths and namespaces are untouched.
- `#123` in commit messages becomes `Caecorthus/<LegacyRepo>#123` (and `SW #12`, `SFA #3`, ... likewise), so GitHub
  links keep pointing at the legacy pull requests.

The rewrite is deterministic: the same legacy commits always give the same rewritten commit ids.

## Bringing in newer legacy commits

Requires `git-filter-repo` on `PATH` (`pip install git-filter-repo`).

```bash
# 1. fetch the legacy repository, then rewrite its current origin/main into a scratch bare repository
git -C ~/Codex-Projects/SparkWitch fetch origin
tools/fusion/rewrite-history.sh witch ~/Codex-Projects/SparkWitch refs/remotes/origin/main /tmp/rw-witch

# 2. update the legacy branch here (fast-forwards, because old commits rewrite to the same ids)
git fetch --no-tags /tmp/rw-witch main:legacy/witch

# 3. merge it; only files changed on both sides can conflict
tools/fusion/merge-module.sh witch <legacy sha>
```

After the merge, files that arrive under `legacy/<module>/` (the legacy `build.gradle`, `gradle.properties`,
`fabric.mod.json`, docs) show what changed upstream: port the change into the unified file by hand and delete the
`legacy/` copy again.

Fusion points so far:

| Module | Legacy repository | Legacy commit |
| --- | --- | --- |
| factionapi | Caecorthus/SparkFactionAPI | 633126a |
| strength | Caecorthus/SparkStrength | edfc93e |
| traits | Caecorthus/SparkTraits | c49a4a9 |
| witch | Caecorthus/SparkWitch | 98064fe9 |
| assist | Caecorthus/SparkAssist | d6a8fcb |
