package dev.caecorthus.sparkdlc.bridge.witch;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * SparkTraits seam for 城崎诺亚. Calls only the public facade
 * {@code SparkTraitsApi.hasActiveTrait(PlayerEntity, Identifier)}; never {@code sparktraits.impl} or
 * {@code sparktraits.component}. SparkTraits itself never gives Noa the Impostor trait (owner 2026-11-04); this stays
 * as a guard. SparkTraits is fused into SparkDLC, so the call is direct. Callers pick their own fail-closed direction
 * for an unknown (null player) answer: task pay needs a confirmed non-Impostor, the knife innocent-kill death is
 * skipped only for a confirmed Impostor.
 * 城崎诺亚的 SparkTraits 接缝。仅调用公共门面 {@code SparkTraitsApi.hasActiveTrait}，从不触及 {@code impl} 或
 * {@code component} 包。SparkTraits 自身不会给诺亚内鬼词条（所有者 2026-11-04）；此处保留为防护。SparkTraits 已融合进
 * SparkDLC，因此为直接调用。对未知答案（玩家为空），调用方各自决定失败关闭的方向：任务收入要求确认不是内鬼，用刀误杀处死
 * 只在确认是内鬼时跳过。
 */
public final class SparkTraitsNoaBridge {
    private static final String MOD_ID = "sparkdlc";
    private static final Identifier IMPOSTOR = Identifier.of(MOD_ID, "impostor");

    private SparkTraitsNoaBridge() {
    }

    /**
     * TRUE/FALSE from the facade; null when no player is given.
     * 门面给出 TRUE/FALSE；未提供玩家时为 null。
     */
    public static @Nullable Boolean isImpostor(@Nullable PlayerEntity player) {
        if (player == null) {
            return null;
        }
        return SparkTraitsApi.hasActiveTrait(player, IMPOSTOR);
    }
}
