package dev.caecorthus.sparkdlc.witch.compat;

import dev.caecorthus.sparkdlc.traits.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * SparkTraits seam for the Control Expert. Calls only the public facade
 * {@code SparkTraitsApi.getMarksmanRangeMultiplier(PlayerEntity)} and {@code hasActiveTrait(PlayerEntity, Identifier)}.
 * SparkTraits is fused into SparkDLC, so the calls are direct; a null player means base Taser range and an unknown
 * (null) Impostor answer, which keeps the Judge-style task-pay semantics.
 * 控场专家的 SparkTraits 接缝。仅调用公共门面 {@code getMarksmanRangeMultiplier} 与 {@code hasActiveTrait}。
 * SparkTraits 已融合进 SparkDLC，因此均为直接调用；玩家为空时，电击枪使用基础射程，内鬼查询为未知（null），任务收入沿用
 * 法官语义。
 */
public final class SparkTraitsControlExpertBridge {
    private static final Identifier IMPOSTOR = Identifier.of("sparktraits", "impostor");

    private SparkTraitsControlExpertBridge() {
    }

    /**
     * SparkTraits Marksman range multiplier for the Taser, clamped to [1.0, 1.3]; a null player is 1.0.
     * Shared with USEC through {@link SparkTraitsMarksmanBridge}.
     * 电击枪使用的 SparkTraits 精确枪手射程倍率，限制在 [1.0, 1.3]；玩家为空时为 1.0。经 {@link SparkTraitsMarksmanBridge}
     * 与 USEC 共用。
     */
    public static double marksmanRangeMultiplier(@Nullable PlayerEntity player) {
        return SparkTraitsMarksmanBridge.marksmanRangeMultiplier(player);
    }

    /**
     * Whether the SparkTraits Impostor trait is active: TRUE/FALSE from the facade, null when no player is given
     * (callers treat null as "do not pay", like the Judge).
     * SparkTraits 内鬼词条是否生效：门面给出 TRUE/FALSE；未提供玩家时为 null（调用方与法官一致，视为不发放）。
     */
    public static @Nullable Boolean isImpostor(@Nullable PlayerEntity player) {
        return player == null ? null : Boolean.valueOf(SparkTraitsApi.hasActiveTrait(player, IMPOSTOR));
    }
}
