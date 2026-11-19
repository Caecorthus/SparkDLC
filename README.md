# SparkDLC

Spark Wathe 的 DLC 合集：SparkFactionAPI、SparkStrength、SparkTraits、SparkWitch、SparkAssist 五个 mod 融合成的一个
Fabric mod（Minecraft 1.21.1，需要 Spark 版 Wathe 与 NoellesRoles）。

One Fabric mod for Minecraft 1.21.1 that fuses the five Spark add-ons for Spark-era Wathe and NoellesRoles.

## 安装 / Install

- 把 `sparkdlc-<版本>.jar` 放进 `mods`，并**移除**原来的五个 jar（`sparkfactionapi`、`sparkstrength`、`sparktraits`、
  `sparkwitch`、`sparkassist`）。SparkDLC 已经包含这五个 mod，不能与它们同时安装。
- 服务端和客户端都要装同一个版本。
- 存档、地图、资源包、配置文件都不用改：所有物品、职业、数据包、组件、语言键和配置文件仍然使用原来的命名空间
  （`sparkwitch:…`、`sparkstrength:…` 等）。

Put `sparkdlc-<version>.jar` into `mods` on both sides and remove the five legacy jars. Worlds, maps, resource packs
and config files keep working: every id keeps its legacy namespace.

## 模块 / Modules

| 模块 | 包 | 命名空间 | 内容 |
| --- | --- | --- | --- |
| factionapi | `dev.caecorthus.sparkdlc.factionapi` | `sparkfactionapi` | 阵营 API、回合结算、数据包策略、回放等公共服务 |
| strength | `dev.caecorthus.sparkdlc.strength` | `sparkstrength` | 职业增强、物品（M67、平板、无人机…）、破坏系统 |
| traits | `dev.caecorthus.sparkdlc.traits` | `sparktraits` | 被动特质 |
| witch | `dev.caecorthus.sparkdlc.witch` | `sparkwitch` | 魔女/嘉豪阵营、职业、主动技能框架 |
| assist | `dev.caecorthus.sparkdlc.assist` | `sparkassist` | 客户端辅助：指南、成就、统一设置、Ponder 演示 |

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
