package dev.caecorthus.sparkdlc.bridge.witch;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Cross-module seam: the Seeker's own SparkTraits reads through the public {@code SparkTraitsApi} facade only
 * ({@code isLastStandPending(PlayerEntity)} and {@code hasActiveTrait(PlayerEntity, Identifier)}); never
 * {@code sparktraits.impl} or {@code sparktraits.component}. SparkTraits is fused into SparkDLC, so the calls are
 * direct. A null player: not pending; the impostor query reads as unknown (true), which denies devices and pay (Q5-b,
 * fail closed).
 * 跨模块接缝：搜寻者自有的 SparkTraits 读取，仅经由公共门面 {@code SparkTraitsApi}
 * （{@code isLastStandPending} 与 {@code hasActiveTrait}），从不触及 {@code impl} 或 {@code component} 包。SparkTraits
 * 已融合进 SparkDLC，因此均为直接调用。玩家为空时：非待定；内鬼查询视为未知（true），从而拒绝设备与报酬（Q5-b，失败即关闭）。
 */
public final class SparkTraitsSeekerBridge {
    private static final Identifier IMPOSTOR = Identifier.of("sparktraits", "impostor");

    private SparkTraitsSeekerBridge() {
    }

    /**
     * Whether SparkTraits Last Stand currently holds this player's pending death transition. A null player reads as
     * false, so the final-death fallback then treats a dead Seeker as finally dead. The query is shared with the Abyss
     * Listener through {@link SparkTraitsLastStandBridge#isLastStandPending}.
     * SparkTraits 背水一战当前是否持有该玩家的待决死亡转换。玩家为空时为 false，
     * 此时最终死亡兜底会把已死亡的搜寻者视为最终死亡。查询经 {@link SparkTraitsLastStandBridge#isLastStandPending}
     * 与聆渊者共用。
     */
    public static boolean isLastStandPending(@Nullable PlayerEntity player) {
        return SparkTraitsLastStandBridge.isLastStandPending(player);
    }

    /**
     * Whether the Impostor trait is active or cannot be verified: true when the facade reports it or when no player is
     * given.
     * 内鬼词条是否生效或无法确认：门面报告生效，或未提供玩家时为 true。
     */
    public static boolean isImpostorOrUnknown(@Nullable PlayerEntity player) {
        // Unknown (no player) counts as Impostor. / 未知（未提供玩家）按内鬼处理。
        return player == null || SparkTraitsApi.hasActiveTrait(player, IMPOSTOR);
    }
}
