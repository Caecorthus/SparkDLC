# SparkDLC

Spark Wathe 的 DLC 合集：SparkFactionAPI、SparkStrength、SparkTraits、SparkWitch、SparkAssist 五个 mod 融合成的一个
Fabric mod（Minecraft 1.21.1，需要 Spark 版 Wathe 与 NoellesRoles）。

One Fabric mod for Minecraft 1.21.1 that fuses the five Spark add-ons for Spark-era Wathe and NoellesRoles.

## 安装 / Install

- 把 `sparkdlc-<版本>.jar` 放进 `mods`，并**移除**原来的五个 jar（`sparkfactionapi`、`sparkstrength`、`sparktraits`、
  `sparkwitch`、`sparkassist`）。SparkDLC 已经包含这五个 mod，不能与它们同时安装。
- 服务端和客户端都要装同一个版本。
- 所有物品、职业、数据包、组件、语言键、命令和配置文件现在都在唯一的命名空间 `sparkdlc` 下（例如
  `sparkdlc:m67`、`/sparkdlc:forceAbility`、`config/sparkdlc.json`）。针对旧命名空间（`sparkwitch:…`、
  `sparkstrength:…` 等）制作的资源包和数据包需要改名。
- 旧存档、旧配置和旧命令名的兼容层尚未完成（统一化第二阶段 b）；在它完成之前，用旧版本保存的存档、成就和设置不会被读取，
  升级前请先备份。

Put `sparkdlc-<version>.jar` into `mods` on both sides and remove the five legacy jars. Every item, role, packet,
component, lang key, command and config file is now under the single namespace `sparkdlc` (`sparkdlc:m67`,
`/sparkdlc:forceAbility`, `config/sparkdlc.json`). Resource packs and data packs made for the old namespaces
(`sparkwitch:…`, `sparkstrength:…`) have to be renamed. The compatibility layer for old worlds, old config files
and old command names is not finished yet (unification stage 2b): until it is, worlds, achievements and settings
saved with an older build are not read. Back up before upgrading.

## 模块 / Modules

| 原模块 | 现在的主要包（`dev.caecorthus.sparkdlc` 下） | 旧 mod id（仅作为 `provides` 别名） | 内容 |
| --- | --- | --- | --- |
| factionapi | `faction`, `round`, `cooldown`, `replay`, `record`, `team`, `net.version` | `sparkfactionapi` | 阵营 API、回合结算、数据包策略、回放等公共服务 |
| strength | `role.<category>.<role>`, `item`, `entity`, `sabotage`, `tablet`, `economy`, `viewpoint` | `sparkstrength` | 职业增强、物品（M67、平板、无人机…）、破坏系统 |
| traits | `trait` | `sparktraits` | 被动特质 |
| witch | `role.<category>.<role>`, `skill`, `faction.witch`, `item`, `net` | `sparkwitch` | 魔女/嘉豪阵营、职业、主动技能框架 |
| assist | `guidebook`, `achievement`, `ponder`, `config`, `sound`, `client.settings` | `sparkassist` | 客户端辅助：指南、成就、统一设置、Ponder 演示 |

代码按功能而不是按原模块组织：职业在 `role.<类别>.<职业>`（类别为 civilian、killer、neutral、jiahao、witch、special），客户端代码在 `client.*`，全部 mixin 在 `mixin.*` 与 `client.mixin.*`（两个 mixin 配置）。命名空间保持不变；搬迁脚本与包映射见 [`tools/unify/README.md`](tools/unify/README.md)。

Code is organised by function, not by legacy module: roles live in `role.<category>.<role>`, client code in
`client.*`, every mixin in `mixin.*` / `client.mixin.*` (two mixin configs). Namespaces are unchanged; the move
script and the package map are in [`tools/unify/README.md`](tools/unify/README.md).

更完整的说明见 [`CONTEXT.md`](CONTEXT.md)，各模块的细节在 `docs/<module>/CONTEXT.md`。

## 构建与验证 / Build and verify

需要 Java 21。Requires Java 21.

```bash
./gradlew build            # 编译并打包，产物在 build/libs/（不运行测试和校验）
./gradlew verifyAll        # 各模块的校验任务（gradle/verify/<module>.gradle）
./gradlew runGameTest      # 无界面启动真实服务器，加载全部模块并审计所有通用端 mixin
./gradlew runClientAudit   # 启动真实客户端约半分钟，审计所有客户端 mixin 后自动退出
./gradlew test             # 单元/契约测试（src/test 仅本地保存，不进 git）
```

## 历史 / History

五个旧仓库的完整提交历史都合并在本仓库中（包名与路径已改写），`git log --follow` 和 `git blame` 可以一直追溯到
旧仓库的每一次提交；提交信息里的 `#123` 已改写成 `Caecorthus/<旧仓库>#123`。之后旧仓库若还有新提交，可按
[`tools/fusion/README.md`](tools/fusion/README.md) 的步骤合并进来。

## 许可 / License

代码为 AGPL-3.0-only，材质见 [`LICENSE-TEXTURES.md`](LICENSE-TEXTURES.md)，第三方声明见
[`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md)。
