package dev.caecorthus.sparkdlc.role.civilian.usec;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.trait.api.TraitIds;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Cross-module seam for USEC task pay: calls only the public facade
 * {@code SparkTraitsApi.hasActiveTrait(PlayerEntity, Identifier)} for {@code TraitIds.IMPOSTOR}, never its
 * {@code impl} or {@code component} packages. SparkTraits is fused into SparkDLC, so the call is direct: TRUE/FALSE
 * from the facade, null ("unknown") only for a null player; the caller then denies the pay (fail closed), as the
 * Control Expert's bridge does. Server use only (task completion).
 * USEC 任务收入的跨模块接缝：只调用公共门面 {@code SparkTraitsApi.hasActiveTrait} 查询 {@code TraitIds.IMPOSTOR}，
 * 从不触及 {@code impl} 或 {@code component} 包。SparkTraits 已融合进 SparkDLC，因此为直接调用：门面给出 TRUE/FALSE，
 * 仅在玩家为空时为 null（“未知”），调用方随即拒绝发放（失败即关闭），与控场专家桥接一致。仅服务端（任务完成）使用。
 */
final class UsecEconomyTraitsProbe {
    private UsecEconomyTraitsProbe() {
    }

    /**
     * TRUE/FALSE from the facade; null when no player is given.
     * 门面给出 TRUE/FALSE；未提供玩家时为 null。
     */
    static @Nullable Boolean isImpostor(@Nullable PlayerEntity player) {
        if (player == null) {
            return null;
        }
        return SparkTraitsApi.hasActiveTrait(player, TraitIds.IMPOSTOR);
    }
}
