# SparkDLC

Spark Wathe 的 DLC：为 Wathe 加入新的职业与阵营、职业改动、词条、物品与破坏玩法，并附带游戏内指南、成就与设置。它是一个
Fabric mod（Minecraft 1.21.1，需要 Spark 版 Wathe 与 NoellesRoles），由原来的 SparkFactionAPI、SparkStrength、
SparkTraits、SparkWitch、SparkAssist 五个附属融合而成。

Spark Wathe DLC: new roles and factions, reworked roles, traits, items and sabotage for Wathe, with an in-game
guidebook, achievements and settings. One Fabric mod for Minecraft 1.21.1 (needs Spark-era Wathe and NoellesRoles),
made from the five former add-ons SparkFactionAPI, SparkStrength, SparkTraits, SparkWitch and SparkAssist.

## 安装 / Install

- 把 `sparkdlc-<版本>.jar` 放进 `mods`，并**移除**原来的五个 jar（`sparkfactionapi`、`sparkstrength`、`sparktraits`、
  `sparkwitch`、`sparkassist`）。SparkDLC 已经包含它们，不能同时安装。服务端和客户端都要装同一个版本。
- 所有内容都在唯一的命名空间 `sparkdlc` 下（例如 `sparkdlc:m67`、`/sparkdlc:forceAbility`、`config/sparkdlc.json`）。
- 旧名字继续可用：用五个旧 mod 或更早的 SparkDLC 保存的存档、物品、职业与词条数据、成就统计、按键绑定和设置会自动读取；
  旧命令名仍然能执行（只是不再出现在 Tab 补全里）。客户端设置现在只有一个文件 `config/sparkdlc.json`，旧的设置文件在第一次
  启动时并入，原文件保留作备份。
- 针对旧命名空间（`sparkwitch:…`、`sparkstrength:…` 等）制作的资源包和数据包不会被转换，需要改成 `sparkdlc:`。

Put `sparkdlc-<version>.jar` into `mods` on both sides and remove the five legacy jars; SparkDLC contains them and
cannot be installed next to them. Everything is under the single namespace `sparkdlc` (`sparkdlc:m67`,
`/sparkdlc:forceAbility`, `config/sparkdlc.json`). Old names keep working: worlds, items, role and trait data,
achievement statistics, key bindings and settings saved by the five mods or by an earlier SparkDLC are read, and old
command names still run (they are only hidden from Tab completion). Client settings now live in one file,
`config/sparkdlc.json`; older settings files are folded into it on first start and kept as backups. Resource packs
and data packs written for the old namespaces are not translated and have to use `sparkdlc:`.

## 代码结构 / Code layout

代码按功能组织，而不是按原来的五个 mod：职业在 `role.<类别>.<职业>`（类别为 civilian、killer、neutral、jiahao、witch、
special），词条在 `trait`，阵营、回合结算与冷却等共享核心在 `faction`、`round`、`cooldown`，指南、成就、设置与 Ponder 在
`guidebook`、`achievement`、`client.settings`、`ponder`，客户端代码在 `client.*`，全部 mixin 在 `mixin.*` 与
`client.mixin.*`（两个 mixin 配置）。旧名字只在兼容层 `legacy` 包里出现。各部分的说明见 [`CONTEXT.md`](CONTEXT.md)
以及 `docs/core`、`docs/roles`、`docs/client-helpers`；统一化的全过程与脚本见 [`tools/unify/README.md`](tools/unify/README.md)。

Code is organised by function, not by the five former mods: roles in `role.<category>.<role>`, traits in `trait`,
the shared core (factions, round end, cooldowns) in `faction`, `round`, `cooldown`, the guidebook, achievements,
settings and Ponder in `guidebook`, `achievement`, `client.settings`, `ponder`, client code in `client.*`, every
mixin in `mixin.*` / `client.mixin.*` (two mixin configs). Old names appear only in the compatibility layer, the
`legacy` packages. [`CONTEXT.md`](CONTEXT.md) is the map, with notes in `docs/core`, `docs/roles` and
`docs/client-helpers`; [`tools/unify/README.md`](tools/unify/README.md) records how the five mods became one.

启动只有一个入口：通用端 `SparkDLC`、客户端 `client.SparkDLCClient`，各自的入口方法就是按顺序排列的启动步骤清单。步骤顺序
属于行为（同一事件的监听器按注册顺序执行），不要随意调整；`./gradlew runGameTest -PstartupOrderDump=<目录>` 会按顺序写出
启动时注册的内容，便于比较两个构建。

The mod starts through one entry, `SparkDLC` and `client.SparkDLCClient`, whose entry methods are the ordered list
of start-up steps. The order is behaviour (listeners on one event run in registration order), so do not reorder it
casually; `./gradlew runGameTest -PstartupOrderDump=<dir>` writes what the start-up registered, in order, so that
two builds can be compared.

## 构建与验证 / Build and verify

需要 Java 21。Requires Java 21.

```bash
./gradlew build            # 编译并打包，产物在 build/libs/（不运行测试和校验）
./gradlew verifyAll        # 全部校验任务（gradle/verify/*.gradle，按校验内容分组）
./gradlew runGameTest      # 无界面启动真实服务器，审计所有通用端 mixin 并运行兼容性测试
./gradlew runClientAudit   # 启动真实客户端约半分钟，审计所有客户端 mixin 与旧设置迁移后自动退出
./gradlew test             # 单元/契约测试（src/test 仅本地保存，不进 git）
```

## 历史 / History

五个旧仓库的完整提交历史都合并在本仓库中（包名与路径已改写），`git log --follow` 和 `git blame` 可以一直追溯到
旧仓库的每一次提交；提交信息里的 `#123` 已改写成 `Caecorthus/<旧仓库>#123`。之后旧仓库若还有新提交，可按
[`tools/fusion/README.md`](tools/fusion/README.md) 的步骤合并进来。

The full histories of the five legacy repositories are merged in (with packages and paths rewritten), so
`git log --follow` and `git blame` reach every legacy commit; `#123` in their messages became
`Caecorthus/<legacy repo>#123`. [`tools/fusion/README.md`](tools/fusion/README.md) explains how to bring in later
legacy commits.

## 许可 / License

代码为 AGPL-3.0-only，材质见 [`LICENSE-TEXTURES.md`](LICENSE-TEXTURES.md)，第三方声明见
[`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md)。
