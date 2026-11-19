# tools/unify — the unification scripts / 统一化脚本

Two stages are scripted here: the stage-1 package move (`move_packages.py`, first part of this file) and the stage-2a
namespace rename (`rename_namespace.py`, second part). Statements of the first part describe the tree as stage 1 left
it: "no id or namespace changed" was true until stage 2a.
这里有两个阶段的脚本：第一阶段的包搬迁（`move_packages.py`，本文件前半部分）与第二阶段 a 的命名空间改名
（`rename_namespace.py`，后半部分）。前半部分描述的是第一阶段结束时的代码树：“id 与命名空间没有变化”在第二阶段 a 之前成立。

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
python3 tools/payload_policy.py generate --repo . --mod <factionapi|witch|strength>   # after the tooling phase
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
| payloads `sparktraits:server_confirm`, `sparkwitch:server_confirm` | `sparkdlc:trait_server_confirm`, `sparkdlc:witch_server_confirm` | two handshakes until they are merged / 握手合并之前仍是两个 |
| login channels `*:version_check` | `sparkdlc:faction_version_check`, `trait_version_check`, `witch_version_check` | three handshakes / 三个握手 |
| component keys `sparktraits:world`, `sparkwitch:world` | `sparkdlc:trait_world`, `sparkdlc:witch_world` | two component classes / 两个组件类 |
| `key.sparkwitch.secondary_skill` | `key.sparkdlc.role_skill_2` | `key.sparkstrength.secondary_skill` takes `key.sparkdlc.secondary_skill`; texts unchanged / 文本不变 |
| `guidebook.sparkassist.source_name.{sparkwitch,sparktraits,sparkassist}` | one `guidebook.sparkdlc.source_name.sparkdlc` | the assist text ("Spark DLC") stays / 保留 assist 的文本 |
| `modmenu.*Translation.sparkwitch` | dropped | the `sparkdlc` pair exists / 已有 `sparkdlc` 的一对 |
| `gui.{sparktraits,sparkwitch}.inventory.more` | one key | same text / 文本相同 |

Legacy names kept on purpose / 有意保留的旧名字：

- `fabric.mod.json` `provides` and `SparkDlcModule`: the five legacy mod ids. `isModLoaded("sparkwitch")` still
  answers; the event volume rows of the settings screen (`EventVolumeRows`) keep them as module labels.
  `provides` 与 `SparkDlcModule`：五个旧 mod id；设置界面的事件音量行继续用它们标注模块。
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
