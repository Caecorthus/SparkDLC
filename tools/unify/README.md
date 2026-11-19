# tools/unify — the stage-1 package move / 第一阶段包搬迁

Stage 1 of the "one real mod" restructure: every Java class left the five legacy module packages
(`dev.caecorthus.sparkdlc.{factionapi,strength,traits,witch,assist}`) for a layout organised by function, and the
nine mixin configs became two. It is a pure move: no class was renamed or merged, and no registry id, packet id,
lang key, asset or data namespace, config file name or command name changed.

“真正融合成一个 mod”重构的第一阶段：所有 Java 类离开五个旧模块包，搬到按功能组织的布局；九个 mixin 配置合并为两个。
这是纯搬迁：没有类被重命名或合并，注册 id、数据包 id、语言键、资源与数据命名空间、配置文件名和命令名都没有变化。

## Files / 文件

| File | Content |
| --- | --- |
| `move_packages.py` | the script / 脚本 |
| `package-map.tsv` | the map: old package prefix (+ class-name regex) -> new package prefix / 包映射 |
| `file-moves.tsv` | generated manifest: old path, new path, old and new class name, source root, phase / 生成的清单 |
| `src/test/.unify-file-moves.tsv` | the same for the local-only tests (not in this repository) / 本地测试的清单（不在本仓库） |

Base commit of the move: `0f7f6db71` (main repository), `0b83061` (local tests).
搬迁的基准提交：主仓库 `0f7f6db71`，本地测试 `0b83061`。

## Running it / 运行

```bash
python3 tools/unify/move_packages.py plan                   # manifest + checks, changes nothing else
python3 tools/unify/move_packages.py apply --phase code     # non-mixin classes, fabric.mod.json entrypoints
python3 tools/unify/move_packages.py apply --phase mixins   # mixin classes, the two merged configs
python3 tools/unify/move_packages.py apply --phase tooling  # gradle/verify, build.gradle, tools, config, docs
python3 tools/unify/move_packages.py apply --phase tests    # local-only src/test (JUnit, Strength Java and Python)
```

Each phase is deterministic and idempotent. Files are moved with a plain rename (git records it as a rename
because only `package`, `import` and fully-qualified names change), so `git log --follow` keeps working.
`plan` fails on a class-name collision, on a mixin outside the two merged config packages (or an ordinary class
inside one), on a client class outside `client.*`, and when a legacy client mixin plugin names a mixin that its
former config did not list.

每个阶段都是确定且幂等的。文件用普通重命名移动（只改 `package`、`import` 和全限定名，git 会识别为重命名），
`git log --follow` 仍然可用。出现类名冲突、mixin 落在两个合并配置包之外（或普通类落在其中）、客户端类落在 `client.*`
之外，或旧客户端 mixin 插件点名了不属于其原配置的 mixin 时，`plan` 会失败。

## The map / 映射

`package-map.tsv` started as the survey's `3-package-map.tsv` (548 rules). The longest `old_prefix` wins; a rule
with a `class_filter` applies to that exact package only and beats the plain rule of the same package. A test in a
package that the map splits follows the production classes it uses without an import.

`package-map.tsv` 以调研得到的 `3-package-map.tsv`（548 条规则）为起点。最长的 `old_prefix` 优先；带 `class_filter`
的规则只作用于该包本身，并优先于同包的普通规则。测试所在的包被拆分时，测试跟随它不经 import 直接使用的生产类。

### Corrections to the survey map / 对调研映射的修正

1. **Team Jiahao is its own role category** (owner decision). `corruptcop`, `insider`, `sphinxhao` and `sniper`
   land in `role.jiahao.<role>` instead of `role.neutral.<role>`, and so do their mirrors under `client.role`,
   `mixin.role` and `client.mixin.role` (11 rules changed, 3 rules added for `witch.roles.neutral.{insider,
   sphinxhao,sniper}`). The map has no class-name rule for these roles, and nothing else about them changes: the
   faction id is still `sparkwitch:jiahao`.
   **嘉豪阵营是独立的职业类别**（所有者决定）。`corruptcop`、`insider`、`sphinxhao`、`sniper` 落在 `role.jiahao.<职业>`
   而不是 `role.neutral.<职业>`，`client.role`、`mixin.role`、`client.mixin.role` 下的镜像包同样处理（修改 11 条规则，
   为 `witch.roles.neutral.{insider,sphinxhao,sniper}` 新增 3 条）。映射里没有针对这些职业的类名规则，其余一切不变：
   阵营 id 仍是 `sparkwitch:jiahao`。
2. **Three test-only packages got rules**: `witch.client.wraith` -> `client.role.special.wraith`, `witch.lang` ->
   `lang`, `witch.testsupport` -> `testsupport` (the survey map only covered production packages).
   **三个只有测试的包补了规则**（调研映射只覆盖生产代码的包）。
3. The columns `order`, `files` and `collisions` were dropped; the script computes them.
   去掉了 `order`、`files`、`collisions` 三列，由脚本计算。

No rule had to change for package-private access, and no visibility was widened.
没有规则因包私有访问而调整，也没有放宽任何可见性。

## What the script rewrites / 脚本改写的内容

- `package` lines; `import` lines and every other fully-qualified name in code, comments, Javadoc and strings, in
  dotted form, slashed form (`dev/caecorthus/sparkdlc/...`, with or without `src/<set>/java/`, `.java`, `.class`,
  `$Inner`) and regex-escaped form.
- Imports that a split package makes necessary: a class that shared a package with another one and no longer
  does gets an `import` for it.
- `fabric.mod.json` (entrypoints, the mixin config list), the two merged mixin configs (lists concatenated in the
  former `fabric.mod.json` order; the common config is listed first), text resources that name a class.
- Old directories: a directory maps to the common ancestor of everything that was below it (a module root maps to
  the source-set package root), so a root constant plus relative pieces keeps resolving.
- Module-relative pieces in strings, comments and backticked doc text: `roles/civilian/bear/X.java`,
  `mixin/seeker/`, `compat/SparkTraitsSeekerBridge`, `bear.RicochetKillPlayerMixin`, `"X.java"` next to a
  directory constant, `.resolve("seeker")` below a root the same file names. A piece is rewritten only when it
  matches exactly one production file or directory (after narrowing by the modules and source sets the file
  names); ambiguous pieces are left alone and logged.
- Config-relative mixin names: dotted entries, entries that carry JSON quotes (`\"FooMixin\"`), package-prefix
  literals (`"bear."`), and in `gradle/verify/*.gradle` every whole-literal mixin name. The nine config file names.

## What it cannot rewrite / 脚本无法改写的内容

These stay as they were and fail loudly in the tests that use them / 这些保持原样，使用它们的测试会明确失败：

- names built at run time (`MAIN + "mixin/" + name + ".java"`, `"dev/.../mixin/" + name + ".class"`);
- a bare `"FooMixin"` that one test uses both as a config entry and as a file name;
- order, adjacency and "tail of the list" assertions on a merged mixin list;
- scans of a former module root (the scan now covers the whole tree) and rules about boundaries between the
  former modules: their intent has to be restated for the new layout or retired.

It leaves alone on purpose: `tools/fusion/**` (documents the legacy history rewrite), `AGENTS.md` (the project's
instruction file, edited by its owner), `docs/local/**`, lines that name a path at an old commit (`git show`),
legacy namespaces and ids everywhere.
有意不动的内容：`tools/fusion/**`（记录旧历史的改写方式）、`AGENTS.md`（项目指令文件，由所有者修改）、`docs/local/**`、
引用旧提交路径的行（`git show`），以及所有旧命名空间和 id。
