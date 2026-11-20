# tools/unify — the unification scripts / 统一化脚本

Three passes are scripted here: the stage-1 package move (`move_packages.py`, first part of this file), the stage-2a
namespace rename (`rename_namespace.py`, second part) and the stage-3 class and mixin member rename
(`rename_classes.py`, "One vocabulary" in the third part). Statements of the first part describe the tree as stage 1 left
it: "no id or namespace changed" was true until stage 2a.
这里有三次脚本化改造：第一阶段的包搬迁（`move_packages.py`，本文件第一部分）、第二阶段 a 的命名空间改名
（`rename_namespace.py`，第二部分）以及第三阶段的类与 mixin 成员改名（`rename_classes.py`，第三部分的“统一词汇”）。前半部分描述的是第一阶段结束时的代码树：“id 与命名空间没有变化”在第二阶段 a 之前成立。

# Stage 1 — the package move / 第一阶段：包搬迁

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
| `applied-phases.txt` | the phases that were applied (`src/test/.unify-applied-phases.txt` for the tests) / 已执行的阶段 |

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

Each phase is deterministic and idempotent, and an applied phase is recorded and skipped on the next run
(`--force` overrides). The record matters after the move: `file-moves.tsv` is now the only place that says which
legacy module a file came from, and the verify scripts (`gradle/verify/legacy-modules.gradle`),
`tools/payload_policy.py` and some local tests look files up there by their LEGACY module and package names. Those
names are deliberate and must not be rewritten again. Files are moved with a plain rename (git records it as a rename
because only `package`, `import` and fully-qualified names change), so `git log --follow` keeps working.
`plan` fails on a class-name collision, on a mixin outside the two merged config packages (or an ordinary class
inside one), on a client class outside `client.*`, and when a legacy client mixin plugin names a mixin that its
former config did not list.

每个阶段都是确定且幂等的；已执行的阶段会被记录，再次运行时跳过（`--force` 可强制执行）。搬迁之后这份记录很重要：
`file-moves.tsv` 是唯一还记录“文件来自哪个旧模块”的地方，校验脚本（`gradle/verify/legacy-modules.gradle`）、
`tools/payload_policy.py` 和部分本地测试会按旧模块名、旧包名在其中查找文件。这些旧名字是有意保留的，不能再次改写。
文件用普通重命名移动（只改 `package`、`import` 和全限定名，git 会识别为重命名），
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
- A `package` line inside a stub source kept in a test string follows the class the stub declares.
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

# Stage 2a — one namespace `sparkdlc` / 第二阶段 a：统一命名空间 `sparkdlc`

Owner decision (2026-11-19): everything players, packs and saves can see moves to the namespace `sparkdlc`. The five
legacy namespaces (`sparkfactionapi`, `sparkstrength`, `sparktraits`, `sparkwitch`, `sparkassist`) stop being
namespaces; old names keep working only through an explicit compatibility layer, which is stage 2b and is NOT part
of this stage. This stage adds no alias and no fallback for an old name.

所有者决定（2026-11-19）：玩家、资源包和存档能看到的一切都迁到命名空间 `sparkdlc`。五个旧命名空间不再是命名空间；旧名字只
通过显式的兼容层继续有效，那是第二阶段 b 的工作，不属于本阶段。本阶段不为任何旧名字添加别名或回退。

## Files / 文件

| File | Content |
| --- | --- |
| `rename_namespace.py` | the script / 脚本 |
| `namespace-overrides.tsv` | hand-written: exceptions to "same path under `sparkdlc`" (collisions, kept legacy ids, dropped lang keys) / 手写：“同路径”规则的例外 |
| `namespace-keep.tsv` | hand-written: files and lines that keep a legacy name on purpose / 手写：有意保留旧名字的文件和行 |
| `id-inventory.tsv` | input: every id of the mod with its kind, taken before the rename / 输入：改名前的全部 id 及其种类 |
| `legacy-ids.tsv` | generated: kind, old id, new id, note; the table stage 2b works from / 生成：种类、旧 id、新 id、备注，供第二阶段 b 使用 |
| `resource-moves.tsv` | generated: old path, new path of every moved or merged resource file / 生成：每个被移动或合并的资源文件的新旧路径 |
| `applied-phases.txt` | also records the applied phases of this script (`ns-main`, `ns-tooling`; `ns-tests` in `src/test/.unify-applied-phases.txt`) / 同时记录本脚本已执行的阶段 |

Base commit of the rename: `2b06cf0fe` (main repository), `a11e627` (local tests).
改名的基准提交：主仓库 `2b06cf0fe`，本地测试 `a11e627`。

## Running it / 运行

```bash
python3 tools/unify/rename_namespace.py plan                  # checks + legacy-ids.tsv, changes nothing else
python3 tools/unify/rename_namespace.py apply --phase main     # resources, production and gametest sources
python3 tools/unify/rename_namespace.py apply --phase tooling  # gradle/verify, config, tools/payload_policy.py, docs
python3 tools/unify/rename_namespace.py apply --phase tests    # local-only src/test (JUnit, Strength Java and Python)
python3 tools/payload_policy.py generate --repo .              # after the tooling phase (one class since stage 3)
```

A phase is deterministic, and an applied phase is recorded and skipped on the next run (`--force` overrides; on a
renamed tree a second run finds nothing to rewrite). `--report FILE` lists every token the phase left alone, with
its context. `plan` and every `apply` rewrite `legacy-ids.tsv` from `id-inventory.tsv` and the override table, and
fail when two ids of one kind would become the same new id without a "merged" note.
每个阶段都是确定的；已执行的阶段会被记录并在下次运行时跳过（`--force` 可强制；在已改名的代码树上再次运行不会再改写任何内容）。
`--report FILE` 列出该阶段未改写的每个记号及其上下文。`plan` 与每次 `apply` 都会根据 `id-inventory.tsv` 和例外表重新生成
`legacy-ids.tsv`；同一种类的两个 id 会变成同一个新 id 且没有 “merged” 备注时失败。

## The rule / 规则

**Same path under `sparkdlc`**, for every kind of name. The script does not replace the bare word: it looks at each
occurrence of a namespace word and decides by its form.
**同路径改到 `sparkdlc` 下**，适用于所有种类的名字。脚本不是替换裸词，而是逐个查看命名空间词的出现位置，按其形式判断。

| Form | Example | Result |
| --- | --- | --- |
| id | `"sparkwitch:prophet"`, `sparkwitch:forceAbility` (command) | `sparkdlc:prophet`, `sparkdlc:forceAbility` |
| two-argument id | `Identifier.of("sparkwitch", "prophet")` | `Identifier.of("sparkdlc", "prophet")` |
| helper | `SparkWitch.id("prophet")` | unchanged: the five `MOD_ID` constants are bare literals and become `"sparkdlc"` |
| bare literal | `"sparkwitch"` (namespace argument, logger name, guidebook source, required mod of a Ponder demo) | `"sparkdlc"`; `List.of("sparkdlc", "sparkdlc")` is reduced to one entry |
| key segment | `hud.sparkwitch.x`, `replay.sparkfactionapi.role_changed.cause.sparkwitch.x` | every segment replaced |
| key or file starting with the namespace | `sparkassist.ponder.x`, `sparkwitch.command.setmana`, `sparkassist.json` | `sparkdlc.…` |
| prefixed string id | `sparkstrength_m67`, the bare prefix `"sparkwitch_"` | `sparkdlc_m67`, `"sparkdlc_"` |
| path segment | `assets/sparkwitch/…`, `guidebook_plates/sparktraits/…`, `"sparkwitch/civ_tray_cabin"` | `sparkdlc` |
| config file name | `sparkwitch-client.json` | `sparkdlc-client.json` |

Left alone / 不改写:

| Form | Example | Why |
| --- | --- | --- |
| mixin member prefix | `sparkwitch$tick` | Java member name; a later stage renames them (14 names would clash) / Java 成员名，留给后续阶段 |
| Java code outside strings and comments | `sparkwitch("tarot_reader")` | identifiers are not ids / 标识符不是 id |
| package or class path | `dev.caecorthus.sparkwitch.x`, `annina.sparkstrength`, `…/mixin/compat/sparkstrength/X.class` | pre-fusion and legacy packages are history / 融合前与旧包是历史 |
| legacy mixin config name | `sparkassist.mixins.json` | the nine names are checked as "gone" / 用于检查九个旧配置已不存在 |
| package fragment, system property | `sparktraits.impl`, `-Dsparkassist.decorDebug`, `sparktraits.debugCautiousSound` | not ids; developer switches keep their names / 不是 id；开发开关保留原名 |
| CamelCase data key | `sparktraitsConscienceGas` | private NBT field name / 私有 NBT 字段名 |
| prose | "the sparkwitch module", `` `sparkwitch` `` in backticks | a sentence about a legacy mod stays readable / 关于旧模组的句子保持可读 |

Resources: `assets/<ns>/**` and `data/<ns>/**` move to `sparkdlc` in the same source set (`git mv`); the namespace
folders inside the guidebook and Ponder trees (`guidebook_plates/<ns>/`, `guidebook/*/<ns>/`, `ponder/<ns>/`) and
the post shader programs `assets/minecraft/shaders/program/<ns>_*` follow. The six lang files per locale become
`src/main/resources/assets/sparkdlc/lang/{en_us,zh_cn}.json` (the assist file leaves the client source set: one jar
path may not exist in both), entries grouped by former file in the order sparkdlc, factionapi, strength, traits,
witch, assist; the three `sounds.json` become `assets/sparkdlc/sounds.json`. The merge is done on the text, so
values keep their escapes. `plan` fails when two files would land on one path.
资源：`assets/<ns>/**` 与 `data/<ns>/**` 在原源码集内移到 `sparkdlc`（`git mv`）；指南与 Ponder 目录内按命名空间分的文件夹以及
`assets/minecraft/shaders/program/<ns>_*` 后处理着色器随之改名。每种语言的六个语言文件合并为
`src/main/resources/assets/sparkdlc/lang/{en_us,zh_cn}.json`（assist 的语言文件离开客户端源码集：同一个 jar 路径不能同时存在于
两个源码集），条目按原文件分组，顺序为 sparkdlc、factionapi、strength、traits、witch、assist；三个 `sounds.json` 合并为
`assets/sparkdlc/sounds.json`。合并在文本层面进行，值的转义保持不变。两个文件会落到同一路径时 `plan` 失败。

## Exceptions / 例外

Collisions (`namespace-overrides.tsv`) / 冲突：

| Old | New | Why |
| --- | --- | --- |
| item groups `sparkstrength:items`, `sparkwitch:items` | one tab `sparkdlc:items`, title `itemGroup.sparkdlc.items` = "SparkDLC" | merged by hand before the rename (`SparkDlcItemGroups`): SparkStrength's entries, then SparkWitch's / 改名前手工合并 |
| payloads `sparktraits:server_confirm`, `sparkwitch:server_confirm` | one `sparkdlc:server_confirm` | one handshake since stage 3 ("one wire"); `sparkdlc:trait_server_confirm` and `sparkdlc:witch_server_confirm` in between / 第三阶段起只有一个握手 |
| login channels `*:version_check` | one `sparkdlc:version_check` | one handshake since stage 3; `sparkdlc:faction_version_check`, `trait_version_check`, `witch_version_check` in between; the old channels are retired, nothing listens on them / 旧频道已退役，不再监听 |
| component keys `sparktraits:world`, `sparkwitch:world` | `sparkdlc:trait_world`, `sparkdlc:witch_world` | two component classes / 两个组件类 |
| `key.sparkwitch.secondary_skill` | `key.sparkdlc.role_skill_2` | `key.sparkstrength.secondary_skill` takes `key.sparkdlc.secondary_skill`; texts unchanged / 文本不变 |
| `guidebook.sparkassist.source_name.{sparkwitch,sparktraits,sparkassist}` | one `guidebook.sparkdlc.source_name.sparkdlc` | the assist text ("Spark DLC") stays / 保留 assist 的文本 |
| `modmenu.*Translation.sparkwitch` | dropped | the `sparkdlc` pair exists / 已有 `sparkdlc` 的一对 |
| `gui.{sparktraits,sparkwitch}.inventory.more` | one key | same text / 文本相同 |

Legacy names kept on purpose / 有意保留的旧名字：

- `fabric.mod.json` `provides` and `SparkDlcModule`: the five legacy mod ids. `isModLoaded("sparkwitch")` still
  answers; the event volume rows of the settings screen (`EventVolumeRows`) keep them as the mod a row belongs to.
  They are not shown: a row that resolves to the SparkDLC container has no "From ..." line (stage 3, "one name").
  `provides` 与 `SparkDlcModule`：五个旧 mod id；设置界面的事件音量行继续用它们记录该行所属的模组，但不再显示：
  解析到 SparkDLC 容器的行没有「来自……」一行（第三阶段“一个名字”）。
- The legacy filters that predate the rename: `RetiredTraitIds`, `RoundRules.LEGACY_WRAITH`. The former
  `WraithLegacyRoleIds` and `LegacyWraithPlayerComponent` / `LegacyWraithRoundComponent` (the migration of the
  Wraith data saved under `sparktraits:`) were folded into the stage-2b saved-data migration
  (`legacy.LegacySavedData`): they are now the `legacy_role_alias` and `component_fallback` rows of
  `legacy-ids.tsv`. 改名之前就有的旧数据过滤；原先的冤魂旧数据迁移已并入第二阶段 b 的存档迁移，成为对照表中的
  `legacy_role_alias` 与 `component_fallback` 行。
- Private data field names (`private_data_name` rows of `legacy-ids.tsv`): NBT and item custom-data keys, a command
  tag, two scoreboard team names. Renaming one would need its own migration. 私有数据字段名。
- The three misspelt command aliases of SparkStrength (`sparkstength:voteTime`, `sparkStrength:detectiveCaseLimit`,
  `sparkStrength:tabletChatInterval`). SparkStrength 的三个拼写别名命令。
- `tools/fusion/**`, `package-map.tsv`, `file-moves.tsv`, `move_packages.py`, `docs/*/DOWNSTREAM_MIGRATION_NOTES.md`,
  `docs/factionapi/SPARK_MOD_FEATURES.md`, `AGENTS.md`: history, or edited by the owner. 历史文档，或由所有者修改。
- In `gradle/verify/*.gradle` and `tools/payload_policy.py` a quoted bare legacy id is a `provides` check or a module
  label, not a namespace, and stays. The `owner` column of `config/payload-policy/actions.tsv` keeps the legacy
  module id as the label of the registering module; payload ids are `sparkdlc:<path>` and the table is re-sorted.
  校验脚本与数据包策略工具里带引号的旧 id 是 `provides` 检查或模块标签，不是命名空间，保持不变。

Rules that exist only for the local tests (`namespace-keep.tsv`, rows `src/test/*`) / 仅用于本地测试的规则：

- the tests of the existing legacy migrations and of the `provides` list keep their legacy literals;
  已有迁移与 `provides` 列表的测试保留旧字面量；
- a negative assertion about the bare name of a legacy module (`assertFalse(source.contains("sparkstrength"))`,
  `assertNotIn("sparkwitch", ...)`, a `forbidden` / `gone` / `foreign` token list) keeps that name: rewritten, it
  would forbid the mod's own namespace. Such an assertion is weaker than before by nature, because an id can no
  longer contain the word; `--report` lists them (28 lines in 26 test files at the time of the rename).
  针对旧模块裸名的否定断言保留该名字：改写后它会禁止本模组自己的命名空间。这类断言天然比以前弱，因为 id 里不会再出现这个词；
  `--report` 会列出它们（改名时为 26 个测试文件中的 28 行）。
- `path` rows of `namespace-overrides.tsv` name a file that changed its source set (the assist lang file).
  `namespace-overrides.tsv` 的 `path` 行用于改变了源码集的文件（assist 的语言文件）。

A test that told things apart by namespace (`id.startsWith("sparkwitch:")`, "the SparkStrength lang file") cannot be
rewritten by a rule: such tests were restated by hand to select the same set, using `legacy-ids.tsv`,
`resource-moves.tsv` or the `owner` column of `config/payload-policy/actions.tsv`.
按命名空间区分事物的测试（`id.startsWith("sparkwitch:")`、“SparkStrength 的语言文件”）无法用规则改写：这些测试已手工改为借助
`legacy-ids.tsv`、`resource-moves.tsv` 或 `config/payload-policy/actions.tsv` 的 `owner` 列选出同一集合。

## What the script cannot decide / 脚本无法判断的内容

Done by hand, in separate commits / 手工完成，单独提交：

- code that told modules apart by namespace: the creative tab loops, the `"sparktraits:"` shop-entry prefix
  (`TraitShopEntries`), the legacy tables above; 依赖命名空间区分模块的代码；
- `tools/payload_policy.py` (owner semantics) and the three generated policy classes (regenerated, never rewritten);
- a bare literal whose meaning is "the mod id as other mods know it" (the list in `namespace-keep.tsv`);
- tests that assert a legacy id on purpose, or whose intent was a boundary between the former namespaces.

## legacy-ids.tsv

One row per id that existed before the rename: `kind`, `old_id`, `new_id`, `note`. The registry kinds are named after
the vanilla registry (`item`, `block`, `entity_type`, `sound_event`, `data_component_type`, `particle_type`,
`mob_effect`, `armor_material`, `creative_mode_tab`), tags are `tag/<registry>`. Other kinds: `payload_c2s`,
`payload_s2c`, `login_channel`, `component`, `role`, `faction`, `trait`, `skill`, `secondary_ability`,
`death_reason`, the replay kinds, `command`, `command_alias`, `permission`, `shop_entry`, `key_binding`,
`key_category`, `settings_key`, `settings_store`, `file` (config and data files the mod writes), `mod_metadata_key`,
`attribute_modifier`, `event_phase`, `action_id`, `role_change_cause`, `reload_listener`, `identifier` (other id
constants), `lang_key` (only the keys that do not follow the plain rule), and the rows whose name did NOT change:
`private_data_name`, `retired_trait`, `command_alias`. `legacy_role_alias` rows are the six older
`sparktraits:<role>` ids of the Wraith family with the role's new id, and `component_fallback` rows are the two older
`sparktraits:` keys of the Wraith components: both are a SECOND old name of something that also has a `role` /
`component` row, and are read only after that row (`legacy.LegacySavedData`). Asset paths and the 4,100 lang keys that
only change their namespace segment are not listed; `resource-moves.tsv` lists the files.

The registry, tag, payload, component, role, faction, trait, skill, replay-formatter and command rows come from a
dump of a running headless server taken before the rename; the same dump taken after the rename equals the old one
mapped through this table (16,229 rows, no difference).

改名前存在的每个 id 一行：`kind`、`old_id`、`new_id`、`note`。注册表种类以原版注册表命名，标签为 `tag/<注册表>`。名字没有变化的行
（`private_data_name`、`retired_trait`、`command_alias`）也在表中。`legacy_role_alias` 与 `component_fallback` 行是同一职业、
同一组件更早的第二个旧名字（冤魂系的 `sparktraits:` id 与组件键），读取时排在对应的 `role` / `component` 行之后。资源路径和仅替换命名空间段的约 4100 个语言键
不逐条列出；文件见 `resource-moves.tsv`。注册表、标签、数据包、组件、职业、阵营、天赋、技能、回放格式化器和命令的行来自改名前
无界面服务器的运行时导出；改名后的同一导出与经本表映射的旧导出完全一致（16,229 行，无差异）。

# Stage 3: one mod inside / 第三阶段：内部也是一个模组

## One entry / 单一入口

The five initializers, four Cardinal Components entrypoints, three voice chat plugins and three client mixin plugins
became one class each: `SparkDLC`, `client.SparkDLCClient`, `SparkDlcComponents`, `voice.SparkDlcVoiceChatPlugin`,
`client.SparkDlcClientMixinPlugin`. Nothing was reordered: the bodies of the former classes are the named steps of
the new ones, in the sequence the fused entrypoints used to call them.

- **Proof of order.** `./gradlew runGameTest -PstartupOrderDump=<dir>` (and `runClientAudit` with the same property)
  writes `startup-order-<side>.txt`: the mod's entries of every vanilla registry in raw-id order, the handlers of
  every Fabric event that has one of ours in dispatch order (other mods' handlers included, each named by its host
  class), the component factories in registration order, the payload ids and global receivers, and on a client the
  modded key bindings (gametest class `StartupOrderDump`, reflection only, so the same file runs on any revision).
  A dump run switches off Fabric Loader's development-only mod shuffle, which otherwise moves other mods' listeners
  around ours from run to run. For this change the server dump before and after differs only in the host class of
  the three listeners the common initializer registers itself (`SparkWitch` became `SparkDLC`).
- **`file-moves.tsv` keeps the rows of the classes that were merged away** (it is the record of which former module
  a file came from). A consumer takes only the rows whose file still exists, as `legacyModules.sources` in
  `gradle/verify/legacy-modules.gradle` does. The merged classes belong to no former module and are not listed, so a
  per-module rule no longer scans the start-up code; a rule that must keep covering it names the merged class.
- **Transitional holders.** `SparkFactionApiMod`, `SparkStrength`, `SparkTraits`, `SparkWitch`, `SparkAssist` keep
  `MOD_ID`, `LOGGER` and `id(...)`, and `client.SparkAssistClient`, `client.SparkTraitsClient`,
  `client.SparkWitchClient` forward `configManager()`, `runSessionCleanup(...)` / `shouldRunSessionCleanup(...)` and
  `abilityKeyText()` to `SparkDLCClient`, for the roughly one thousand call sites that still name them. A scripted
  pass points those at `SparkDLC` / `SparkDLCClient` and deletes the holders (done: "One vocabulary" below).

五个初始化器、四个 Cardinal Components 入口、三个语音插件和三个客户端 mixin 插件各自合并为一个类：`SparkDLC`、
`client.SparkDLCClient`、`SparkDlcComponents`、`voice.SparkDlcVoiceChatPlugin`、`client.SparkDlcClientMixinPlugin`。
没有调整任何顺序：原有各类的方法体成为新类中具名的步骤，顺序与融合入口原先调用它们的顺序一致。

- **顺序的证明。**`./gradlew runGameTest -PstartupOrderDump=<目录>`（`runClientAudit` 同样支持该属性）会写出
  `startup-order-<side>.txt`：每个原版注册表中本模组条目的 raw id 顺序、每个含有本模组处理器的 Fabric 事件的处理器分发顺序
  （包含其他模组的处理器，以所在类命名）、组件工厂的注册顺序、数据包 id 与全局接收器，以及客户端上的模组按键绑定
  （gametest 类 `StartupOrderDump`，只用反射，因此同一个文件可在任意版本上运行）。导出时会关闭 Fabric Loader 仅在开发环境启用的
  模组顺序打乱，否则其他模组的监听器每次运行都会出现在我们监听器的不同位置。本次改动前后的服务端导出只有一处差异：
  通用初始化器自己注册的三个监听器所在的类（`SparkWitch` 变为 `SparkDLC`）。
- **`file-moves.tsv` 保留被合并掉的类所在的行**（它记录的是文件来自哪个旧模块）。使用方只取文件仍然存在的行，与
  `gradle/verify/legacy-modules.gradle` 中 `legacyModules.sources` 的做法一致。合并后的类不属于任何旧模块，也不在清单中，
  因此按模块的规则不再扫描启动代码；需要继续覆盖它的规则要直接点名合并后的类。
- **过渡期的持有类。**`SparkFactionApiMod`、`SparkStrength`、`SparkTraits`、`SparkWitch`、`SparkAssist` 保留 `MOD_ID`、
  `LOGGER` 与 `id(...)`；`client.SparkAssistClient`、`client.SparkTraitsClient`、`client.SparkWitchClient` 把
  `configManager()`、`runSessionCleanup(...)` / `shouldRunSessionCleanup(...)` 与 `abilityKeyText()` 转发给
  `SparkDLCClient`，供仍然引用它们的约一千处调用使用。之后的脚本化改名会把这些调用点指向 `SparkDLC` / `SparkDLCClient`
  并删除这些持有类（已完成，见下文“统一词汇”）。

## One wire / 一条线

The mod spoke to its own client as three mods: three version handshakes, three generated payload-policy classes,
three packet hubs. Each is one now.

- **Handshake.** `net.version.SparkDlcVersionHandshake` asks one login query (`sparkdlc:version_check`) and sends one
  play confirmation (`SparkDlcServerConfirmS2CPacket`, `sparkdlc:server_confirm`); `SparkDlcVersionCheck` is the
  decision as pure logic with the one pair of kick messages, which name SparkDLC; `SparkDlcServerConnection` is the one
  "the server runs SparkDLC and is confirmed" flag; `client.net.version.SparkDlcClientVersionHandshake` answers. The
  three old login channels and the two old confirmation payloads are retired, nothing listens on them (notes of the
  `login_channel` and `server_confirm` rows of `legacy-ids.tsv`, set in `id-inventory.tsv` and
  `namespace-overrides.tsv`). The flag keeps the lifecycle of the former SparkTraits flag, the stricter one: a JOIN
  keeps only a confirmation that this connection's own login earned, so after a proxy backend switch the readers of the
  former SparkWitch flag are unconfirmed until the new backend's confirmation arrives (that flag used to survive every
  JOIN). The confirmation is sent from a player-join listener and stands where the first of the two former ones stood:
  after the join listeners of the role kits, before those of the traits and the mod's own roles
  (`SparkDlcVersionHandshake.registerServer()` in `SparkDLC#onInitialize`). Game test `SparkDlcHandshakeGameTest` logs
  three scripted clients in over a loopback socket: same version, other version, no answer.
- **Payload policy.** One generated class, `net.policy.GeneratedPayloadPolicy`, with every emitted profile.
  `tools/payload_policy.py` has no per-module target (`generate --repo .`, `check --repo .`) and no longer reads
  `file-moves.tsv`. In `config/payload-policy/actions.tsv` the `owner` column is the registering mod again and equals
  the namespace of the id (`sparkdlc`, `wathe`, `noellesroles`); what the former labels `sparkstrength` / `sparkwitch`
  still carried for a rule is the new `group` column: `role_kit` (kits of the Wathe and NoellesRoles roles, sabotage,
  tablet, killer team economy) or `own_role`. `check` also fails on a `sparkdlc` row that no source registers. The
  `tools/payload_policy.py` row of `namespace-keep.tsv` no longer matches anything.
- **Packets.** `net.SparkDlcPackets.register()` is the one call for payload types and server receivers, a list of
  features by role category. A feature with several payloads has a registrar in its own package (`*Packets`,
  `*Networking`; fourteen were split out of the former hubs), a feature with one or two is registered in the hub under
  its name. Payload types and global receivers are kept per payload id, so nothing in the hub depends on order; what
  does is an event listener, and the hub registers none: the services that register listeners together with their
  payloads (`CrossSwordStabService`, the Savior's three services, `HunterShotgunAimClaims`) are called from
  `registerOwnRoleGameplay`, in the place and order they had inside the former hub. On the client the fifteen
  receivers that the start-up wrote out itself are `client.net.SparkDlcClientPackets.register()`; client feature
  modules keep their own receivers.
- **Proof.** The server start-up dump before and after differs only in what the handshake merge changes on purpose:
  one `QueryStart` listener instead of three, one join listener of `SparkDlcVersionHandshake` in the place of the
  one of `SparkTraitsPackets` and none in the place of the one of `SparkWitchPackets`, and `sparkdlc:server_confirm`
  instead of the two old payload ids. The packet hub merge changes nothing in the dump. The 12 profile sets of the
  one policy class equal the sets of the three classes that carried them (109 payload ids x 12 profiles).

本模组原先以三个模组的身份与自己的客户端通信：三个版本握手、三个生成的数据包策略类、三个数据包注册中心。现在各只有一个。

- **握手。**`net.version.SparkDlcVersionHandshake` 只提出一个登录查询（`sparkdlc:version_check`），只发送一个 play 确认
  （`SparkDlcServerConfirmS2CPacket`，`sparkdlc:server_confirm`）；`SparkDlcVersionCheck` 以纯逻辑给出判定及唯一一对踢出提示
  （提示中的名字是 SparkDLC）；`SparkDlcServerConnection` 是唯一的“服务端运行 SparkDLC 且已确认”标记；
  `client.net.version.SparkDlcClientVersionHandshake` 负责应答。三个旧登录频道与两个旧确认数据包已退役，不再有任何监听
  （见 `legacy-ids.tsv` 中 `login_channel` 与 `server_confirm` 行的备注，在 `id-inventory.tsv` 与 `namespace-overrides.tsv` 中设置）。
  该标记沿用原 SparkTraits 标记的生命周期（两者中更严格的一个）：JOIN 只保留由本连接自己的登录取得的确认，因此代理切换后端之后，
  原 SparkWitch 标记的读取方在新后端的确认到达之前处于未确认状态（该标记原先会保留到每次 JOIN 之后）。确认由玩家加入监听器发送，
  位置与原先两个确认中的第一个相同：晚于职业套件的加入监听器，早于词条与本模组自有职业的加入监听器
  （`SparkDLC#onInitialize` 中的 `SparkDlcVersionHandshake.registerServer()`）。game test `SparkDlcHandshakeGameTest` 通过回环 socket
  让三个脚本客户端登录：版本相同、版本不同、不应答。
- **数据包策略。**只有一个生成类 `net.policy.GeneratedPayloadPolicy`，包含全部生成的配置。`tools/payload_policy.py` 不再有按模块的
  目标（`generate --repo .`、`check --repo .`），也不再读取 `file-moves.tsv`。`config/payload-policy/actions.tsv` 的 `owner` 列重新表示
  注册数据包的模组，并与 id 的命名空间相同（`sparkdlc`、`wathe`、`noellesroles`）；旧标签 `sparkstrength` / `sparkwitch` 仍为某条规则
  携带的信息成为新的 `group` 列：`role_kit`（Wathe 与 NoellesRoles 职业的套件、破坏、平板电脑、杀手团队经济）或 `own_role`。
  `check` 还会在某个 `sparkdlc` 行没有任何源码注册时失败。`namespace-keep.tsv` 中 `tools/payload_policy.py` 那一行已不再匹配任何内容。
- **数据包。**`net.SparkDlcPackets.register()` 是注册数据包类型与服务端接收器的唯一调用，按职业类别列出各功能。数据包较多的功能在
  自己的包里有注册类（`*Packets`、`*Networking`；其中十四个从原注册中心拆出），只有一两个数据包的功能在注册中心内按功能名注册。
  数据包类型与全局接收器按数据包 id 保存，因此注册中心内没有任何内容依赖顺序；依赖顺序的是事件监听器，而注册中心不注册监听器：
  连同数据包一起注册监听器的服务（`CrossSwordStabService`、救世主的三个服务、`HunterShotgunAimClaims`）由 `registerOwnRoleGameplay`
  调用，位置与顺序同它们在原注册中心内时一致。客户端方面，启动流程原先自己写出的十五个接收器现在是
  `client.net.SparkDlcClientPackets.register()`；各客户端功能模块仍保留自己的接收器。
- **证明。**改动前后的服务端启动导出只有握手合并有意造成的差异：`QueryStart` 监听器由三个变为一个；`SparkDlcVersionHandshake` 的
  一个加入监听器取代了 `SparkTraitsPackets` 的那个，`SparkWitchPackets` 的那个不再存在；两个旧数据包 id 变为
  `sparkdlc:server_confirm`。数据包注册中心的合并在导出中没有任何变化。合并后的策略类的 12 个配置集合与原先三个类中的对应集合
  完全相同（109 个数据包 id × 12 个配置）。

## One vocabulary / 统一词汇 (`rename_classes.py`)

The third scripted pass. After the move and the namespace rename the code still named the five former mods in three
places: 153 class names (`SparkWitchItems`, `SparkStrengthEvents`, `SparkTraitsReplayEvents`, ...), 1744 mixin member
names with a former prefix (`sparkwitch$`, `sparkstrength$`, `sparktraits$`, `sparkfactionapi$`, `sparkassist$`) and
the registries of the root package, which were split by former mod. This pass renames the classes, gives every mixin
member the prefix `sparkdlc$`, merges the registries into one class per kind and deletes the transitional holder
classes of the "one entry" slice (130 classes renamed, 15 registry classes merged into 6, 8 holders deleted). No id,
key, packet, saved-data name, lang key or asset changes; the registration
order does not change either (see "Proof of order" below).

第三次脚本化改造。包搬迁与命名空间改名之后，代码中仍有三处以五个旧模组命名：153 个类名、1744 个带旧前缀的 mixin 成员名，
以及根包中按旧模组拆分的注册类。其中 130 个类改名，15 个注册类合并为 6 个，8 个持有类被删除。本次改造为这些类改名，把所有 mixin 成员的前缀统一为 `sparkdlc$`，把注册类合并为每种一个，
并删除“单一入口”阶段留下的过渡持有类。id、键、数据包、存档数据名、语言键与资源均不变，注册顺序也不变（见下文“顺序的证明”）。

### Files / 文件

| File | Content |
| --- | --- |
| `rename_classes.py` | the script / 脚本 |
| `class-renames.tsv` | hand-written: old class, new class, kind, section title (merges), reason / 手写：旧类、新类、种类、部分标题、理由 |
| `class-keeps.tsv` | hand-written: classes that match a former-mod name pattern and keep their name, with the reason / 手写：有意保留名字的类及理由 |
| `member-renames.tsv` | hand-written: members of the merged classes that are renamed, dropped or moved / 手写：合并类中被改名、去掉或搬走的成员 |
| `mixin-member-renames.tsv` | hand-written: the mixin members that would clash under one prefix, with their new names / 手写：统一前缀后会冲突的 mixin 成员及其新名字 |
| `source-edits.tsv` | hand-written: one-line edits made before the rename (two start-up calls that the merged data component registry makes redundant) / 手写：改名前的单行修改 |
| `merged/<Class>.header.txt` | hand-written class comment of a merged class / 合并类的类注释 |
| `merged/SparkDlcDataComponents.java`, `merged/sources.tsv` | the one merged class written by hand, and the hashes of the classes it was made from / 唯一手写的合并类及其来源类的哈希 |
| `applied-phases.txt` | also records `names-main`, `names-tooling` (`names-tests` in `src/test/.unify-applied-phases.txt`) / 同时记录本脚本已执行的阶段 |

Base commit of the rename: `e2ba3ce16` (main repository).
改名的基准提交：主仓库 `e2ba3ce16`。

### Running it / 运行

```bash
python3 tools/unify/rename_classes.py plan                   # checks, changes nothing
python3 tools/unify/rename_classes.py apply --phase main     # production and gametest sources, mixin configs, fabric.mod.json
python3 tools/unify/rename_classes.py apply --phase tooling  # gradle/verify, tools/payload_policy.py, config, docs, file-moves.tsv
python3 tools/unify/rename_classes.py apply --phase tests    # local-only src/test (JUnit, Strength Java and Python)
python3 tools/unify/rename_classes.py translate OLD.txt --out NEW.txt   # a text of before, in the names of after
```

Every phase is deterministic; an applied phase is recorded and skipped on the next run (`--force` runs it again, and
on a renamed tree finds nothing to rewrite). `--report FILE` lists what a phase left alone on purpose. The script is
written to be run again on a tree that has changed since the tables were written:

- a row whose class no longer exists is skipped with a note (`skipped ... the class no longer exists`); a merge whose
  parts are partly gone merges the rest;
- `plan` (and every `apply`, which runs the same checks first) **fails** on
  - a class whose name matches a former-mod pattern (`Spark(Witch|Strength|Traits|Faction|Assist)` anywhere, a
    leading `Witch`, `Witches`, `Strength`, `Traits` or `Assist`, or one of those words before `Bridge` / `Compat`)
    and that has neither a row in `class-renames.tsv` nor a keep row: `unlisted class named after a former mod: <name>`;
  - two classes that would have one name in one package, a new name that a local test class already has, and one old
    simple name that belongs to several classes with different new names;
  - a file that would import two classes with one simple name;
  - two parts of a merged class that declare one member name (`member collision in <class>`), and a holder class
    with a member its absorbing class lacks;
  - a changed source of the hand-merged class (`changed since the class was merged by hand`, with the new hash);
  - **a mixin member collision**: two member names that are different today and would become one name
    (`mixin member collision on the same target: sparkdlc$x would be declared 2 times`, followed by the mixins that
    declare them and their targets). The answer is a row in `mixin-member-renames.tsv` with an explicit new name.

每个阶段都是确定的；已执行的阶段会被记录并在下次运行时跳过（`--force` 可强制再次执行，在已改名的代码树上不会再改写任何内容）。
`--report FILE` 列出该阶段有意未改写的内容。脚本可以在表格写成之后发生了变化的代码树上重新运行：类已不存在的行会被跳过并给出
说明；部分来源已不存在的合并会合并其余部分。出现以下情况时 `plan`（以及先执行同样检查的每次 `apply`）会**失败**：名字符合旧模组模式
而两张表中都没有的类；同一个包内的重名、与本地测试类重名、同一个旧简单名对应多个新名字；同一文件会导入两个同名类；合并类的两个部分
声明了同名成员、或持有类的成员在吸收它的类中不存在；手写合并类的来源发生了变化；**mixin 成员冲突**（今天不同的两个成员名会变成
同一个名字），解决办法是在 `mixin-member-renames.tsv` 中加一行并给出明确的新名字。

### What it rewrites / 改写的内容

- **Class names** (`kind` = `rename`). The file is renamed in place (a plain rename, through `git mv` for a tracked
  file, so the history follows; the package never changes, moving is `move_packages.py`'s job) and every whole-word
  occurrence of the old simple name becomes the new one: declarations, imports, fully-qualified names, comments and
  Javadoc, strings (the source-text tests), the two mixin configs, `fabric.mod.json`, access wideners, verify
  scripts, `tools/payload_policy.py`, docs. A name behind a package or directory path that is not the class's own
  package is left alone when it names a file or a whole name of the past
  (`dev/caecorthus/sparkdlc/factionapi/util/SparkFactionPermissions.class` in a "stale class must not be in the jar"
  check). `file-moves.tsv` keeps saying where a file came from: its `new_path` / `new_fqcn` columns follow a plain
  rename.
- **Merged registries** (`kind` = `merge`). The class bodies are concatenated in row order, one titled section per
  former class; the former class comment becomes the comment of its section; the imports are united; members named
  in `member-renames.tsv` are renamed (also where the part uses them without a qualifier), dropped or moved. Every
  part keeps its own registration method and `SparkDLC` calls it where it called the old class, so nothing registers
  earlier or later than before. `SparkDlcDataComponents` is `kind` = `stored`: two of its three parts registered in
  their static initialiser and one in `register()`, which a concatenation cannot keep in order, so the class was
  written by hand (everything in the static initialiser, in the raw-id order of before; `TIMEKEEPER_WATCH_MODE`
  moved in from the item registry, where its place in that order depended on class loading).
- **Holder classes** (`kind` = `absorb`). `SparkFactionApiMod`, `SparkStrength`, `SparkTraits`, `SparkWitch`,
  `SparkAssist` are deleted and their uses become `SparkDLC`; `client.SparkAssistClient`, `client.SparkTraitsClient`,
  `client.SparkWitchClient` become `client.SparkDLCClient`. These names are also words of prose ("SparkWitch adds two
  beds"), so they are rewritten only where they are code: in Java code, in front of a member of the absorbing class
  (`SparkWitch.id(` in a comment or a test string), in a fully-qualified name, an import or a `{@link}`.
- **Mixin member prefixes.** `sparkwitch$x`, `sparkstrength$x`, `sparktraits$x`, `sparkfactionapi$x`, `sparkassist$x`
  become `sparkdlc$x` everywhere (also in strings such as `@Group(name = ...)` and in regex-escaped form). The 30
  names of `mixin-member-renames.tsv` get an explicit new name instead: without it, 14 names would be declared two
  to four times on one target class (three duplicated cooldown accessors, the duplicated ray-shape and corpse
  equipment handlers, two stun guards).
- **Sub-logger names.** `"SparkAssist/Config"`, `"SparkWitch/MiniWitherSkin"`, `"SparkFactionAPI/ForcedCooldowns"`
  and the like become `"SparkDLC/<Name>"` (the string-literal form only). The main logger was `sparkdlc` already.

类名（`rename`）：文件原地改名（普通重命名，已跟踪文件走 `git mv`；包不变），旧简单名的每个整词出现都改为新名字，包括声明、import、
全限定名、注释、字符串、两个 mixin 配置、`fabric.mod.json`、access widener、校验脚本、`tools/payload_policy.py` 与文档；带有并非
该类所在包的路径、且指向文件或过去的全名时保持不变。`file-moves.tsv` 的 `new_path` / `new_fqcn` 两列随改名更新。合并的注册类
（`merge`）：类体按行序拼接，每个旧类一个带标题的部分，旧的类注释成为该部分的注释，import 合并，`member-renames.tsv` 中的成员被改名、
去掉或搬走；每个部分保留自己的注册方法，`SparkDLC` 在原来调用旧类的位置调用它，因此注册时机不变。`SparkDlcDataComponents` 是手写的
（`stored`）：三个部分中有两个在静态初始化器中注册、一个在 `register()` 中注册，拼接无法保持顺序，因此全部改为在静态初始化器中按原有
raw id 顺序注册，`TIMEKEEPER_WATCH_MODE` 也从物品注册类搬入。持有类（`absorb`）：删除并把使用处改为 `SparkDLC` /
`SparkDLCClient`；这些名字同时是普通词语，因此只在确实是代码的位置改写。mixin 成员前缀统一为 `sparkdlc$`，
`mixin-member-renames.tsv` 中的 30 个名字改为明确的新名字。子日志器名字 `"SparkAssist/…"`、`"SparkWitch/…"`、
`"SparkFactionAPI/…"` 改为 `"SparkDLC/…"`，主日志器本来就是 `sparkdlc`。

### Naming decisions / 命名决定

The words of the former mods are replaced by what the thing is in one mod / 旧模组的名字换成它在一个模组里的含义：

| Former mod word | Now | Why |
| --- | --- | --- |
| `SparkStrength…` | `RoleKit…` | the kits (items, entities, rules) of the Wathe and NoellesRoles roles, as `SparkDLC#registerRoleKitContent` names them / Wathe 与 NoellesRoles 职业的套件 |
| `SparkWitch…`, and `Witch…` where it meant the mod | `Role…`, or `Spark…` next to a `Noelles…` / `Wathe…` sibling, or no prefix | the mod's own roles; `Witch` stays where it is the Witch faction, a witch skill, mana, the witch factor, the Witch Maiden or the Witches' Sabbath / 本模组自有职业；`Witch` 指魔女阵营、魔女技能等游戏概念时保留 |
| `SparkTraits…` | `Trait…` | the trait mechanic / 词条机制 |
| `SparkFaction…`, `SparkFactionApi…` | `Faction…`, or what the class holds | the faction concept; `SparkFactionApi` itself stays / 阵营概念；`SparkFactionApi` 本身保留 |
| `SparkAssist…` | `SparkDlc…` (config and settings of the whole mod) | they are the client config of the mod / 它们就是本模组的客户端配置 |
| root registries | `SparkDlcItems`, `SparkDlcBlocks`, `SparkDlcEntities`, `SparkDlcSounds`, `SparkDlcDataComponents`, `SparkDlcItemGroups`, `SparkDlcParticles`, `SparkDlcRoles`, `SparkDlcFactions`, `SparkDlcDeathReasons` | one class per kind / 每种一个类 |

- **The registries stay in the root package.** After the merge the root package is the two entry classes'
  neighbours and nothing else: `SparkDLC`, `SparkDlcComponents`, `SparkDlcModule` and one registry per kind, the
  index of what the mod adds (Wathe keeps its own in `index`). The kind packages are no better home: `item`, `block`,
  `entity` and `sound` hold the role kits' implementation classes only, while the own roles' items and entities live
  in their role packages, and `component` is the Cardinal Components package, where a data component registry would
  be misread. Staying also keeps this pass a pure rename (no import changes), which the two parallel slices merge
  with more easily. 注册类留在根包：合并后根包只剩入口类及每种一个的注册类，相当于本模组新增内容的索引；`item`、`block`、
  `entity`、`sound` 等包只放职业套件的实现类，自有职业的物品与实体在各自的职业包中，而 `component` 是 Cardinal Components 的包。
- **Kept on purpose** (`class-keeps.tsv`): `faction.api.SparkFactionApi` and `trait.api.SparkTraitsApi` (public
  facades whose names read "Spark faction API" and "Spark traits API"; with `SparkReplayApi` and the renamed
  `SparkRoleApi` they are the family of public APIs), and every `Witch…` class that is about the Witch faction or
  the witch skill system. 有意保留：两个公开门面，以及所有确实与魔女阵营或魔女技能系统相关的 `Witch…` 类。
- **Local tests.** `apply --phase tests` rewrites the names inside the local tests. Tests that pinned a shape this
  pass changed (a merged registry, a deleted holder, a list sorted by file name) were restated by hand; the edits are
  kept next to the tests as `src/test/.unify-names-hand-edits.py` (local-only, like the tests), to be run once after
  the tests phase. 本地测试：`apply --phase tests` 改写本地测试中的名字；固定了本次改造所改变的形状的测试经手工重述，
  这些修改以 `src/test/.unify-names-hand-edits.py` 的形式与测试放在一起（同样只在本地），在 tests 阶段之后运行一次。
- **Test classes keep their names** (42 local test classes are still called `SparkWitch…Test` and the like): they
  are local-only, several pairs would collide after a merge, and their subject is rewritten inside them.
  测试类保留原名：它们只在本地存在，合并后会有成对重名，其内部引用的被测类已随之改写。
- **Prose is not rewritten.** Comments, exception messages and log messages that say "SparkWitch" or "SparkStrength"
  as the name of a former mod stay (`--report` lists them); so do the legacy mod ids, `SparkDlcModule` and everything
  in the `legacy` packages. 普通文字不改写：把旧模组名当作名字来叙述的注释、异常信息与日志信息保持不变。

### Proof of order / 顺序的证明

`runGameTest -PstartupOrderDump=<dir>` before and after: the registry part of the dump (the mod's entries of every
vanilla registry in raw-id order, 295 lines) is byte-identical, and the whole dump of before, run through
`rename_classes.py translate`, equals the dump of after (2056 lines: listener order of every event, component
factories, payload ids and receivers). 改名前后各导出一次：注册表部分（各原版注册表中本模组条目的 raw id 顺序，295 行）逐字节相同；
改名前的完整导出经 `translate` 转换后与改名后的导出完全一致（2056 行）。
