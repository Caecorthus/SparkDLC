package dev.caecorthus.sparkdlc.client.legacy;

import dev.caecorthus.sparkdlc.legacy.LegacyFiles;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Everything the five former mods left on a player's disk under an old name, brought to the current names before
 * the client modules start:
 * <ul>
 *     <li>the config and data files ({@link LegacyFiles}): client settings, the achievement saves of every
 *     account, the scope settings, the folder of Mini Wither skins, the cached team list;</li>
 *     <li>the custom keys in {@code options.txt} ({@link LegacyKeyBindings}).</li>
 * </ul>
 * Nothing else is stored under an old name: the settings files keep their keys (enum names, {@code page/group}
 * strings, {@code scopeMode} ...), the per-event volumes are keyed by sound group and not by sound id, there is no
 * sound category, voice chat volume category, Mod Menu entry or Ponder progress of ours, and the ids inside the
 * achievement save are converted where it is read ({@code LegacyAchievementSave}).
 *
 * 五个旧模组以旧名字留在玩家磁盘上的全部内容，在客户端各模块启动之前带到现在的名字下：配置与数据文件（{@link LegacyFiles}：
 * 客户端设置、每个账号的成就存档、瞄准镜设置、小凋灵皮肤文件夹、缓存的团队名单），以及 {@code options.txt} 里的自定义按键
 * （{@link LegacyKeyBindings}）。除此之外没有其他内容以旧名字保存：设置文件内部的键没有变化（枚举名、{@code 页/分组} 字符串、
 * {@code scopeMode} 等），事件音量按声音分组而非声音 id 保存，本模组没有自己的声音分类、语音音量分类、Mod Menu 入口或 Ponder 进度，
 * 成就存档里的 id 则在读取时转换（{@code LegacyAchievementSave}）。
 */
public final class LegacyClientNames {
    private LegacyClientNames() {
    }

    /**
     * Call first in the client entrypoint: the first reader of these files ({@code SparkAssistConfigManager}) is the
     * next statement, and vanilla reads {@code options.txt} right after the entrypoints return.
     * 须在客户端入口最开头调用：这些文件的第一个读取方（{@code SparkAssistConfigManager}）就是下一条语句，而原版在各入口返回后
     * 立即读取 {@code options.txt}。
     */
    public static void carryOver() {
        FabricLoader loader = FabricLoader.getInstance();
        LegacyFiles.carryOverAll(loader.getConfigDir());
        LegacyKeyBindings.carryOver(loader.getGameDir().resolve("options.txt"));
    }
}
