package dev.caecorthus.sparkdlc.item.m16a4;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.doctor4t.wathe.record.GameRecordEvent;
import dev.doctor4t.wathe.record.GameRecordManager;
import dev.doctor4t.wathe.record.replay.ReplayGenerator;
import dev.doctor4t.wathe.record.replay.ReplayRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/**
 * Replay line of the M16A4, registered by item id so every other item's formatter is untouched: a round that hit a
 * player ("shot %s", recorded before the kill; Wathe records the death itself, so a shielded or vetoed hit never reads
 * as a kill). A round that hit no one is recorded too (the record is what marks the rifle as really used) but prints no
 * line: at fifteen rounds a second a miss line per round would bury the replay.
 * M16A4 的回放文本，按物品 id 注册，不影响其他物品的格式器：命中玩家的子弹（“射中 %s”，在击杀之前记录；死亡由 Wathe 自行
 * 记录，因此被护盾或否决挡下的命中不会显示为击杀）。没有打中人的子弹同样会被记录（该记录标志着步枪确实被使用过），但不输出
 * 文本：每秒 15 发，若每发都输出一行未命中会淹没回放。
 */
public final class M16A4Replay {
    static final String SHOT_KEY = "replay.item_use.sparkdlc.m16a4.shot";
    private static boolean registered;

    private M16A4Replay() {
    }

    static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ReplayRegistry.registerItemUseFormatter(SparkDlcItems.M16A4_ID, M16A4Replay::format);
    }

    @Nullable
    private static Text format(GameRecordEvent event, GameRecordManager.MatchRecord match, ServerWorld world) {
        NbtCompound data = event.data();
        // The shared rifle shot names a target exactly when the round hit a player. / 共用步枪射击只在命中玩家时写入目标。
        if (!data.containsUuid("actor") || !data.containsUuid("target")) {
            return null;
        }
        var players = ReplayGenerator.getPlayerInfoCache(match);
        return Text.translatable(SHOT_KEY, ReplayGenerator.formatPlayerName(data.getUuid("actor"), players),
                ReplayGenerator.formatPlayerName(data.getUuid("target"), players));
    }
}
