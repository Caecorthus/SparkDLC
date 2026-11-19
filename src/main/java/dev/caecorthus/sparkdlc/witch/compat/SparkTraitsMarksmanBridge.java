package dev.caecorthus.sparkdlc.witch.compat;

import dev.caecorthus.sparkdlc.traits.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Shared SparkTraits Marksman query behind the Control Expert Taser and the USEC rifle. Calls only the public facade
 * {@code SparkTraitsApi.getMarksmanRangeMultiplier(PlayerEntity)}; SparkTraits is fused into SparkDLC, so the call is
 * direct, and a null player means base range. Each role bridge forwards here and keeps its own, deliberately
 * different, other trait policies. Both sides may call it.
 * 控场专家电击枪与 USEC 狙击枪共用的 SparkTraits 精确枪手查询。仅调用公共门面 {@code getMarksmanRangeMultiplier}；
 * SparkTraits 已融合进 SparkDLC，因此为直接调用，玩家为空时使用基础射程。各职业桥接转发到这里，并各自保留其有意不同的
 * 其他词条策略。两端均可调用。
 */
final class SparkTraitsMarksmanBridge {
    /** Mirrors ControlExpertRules' and UsecRules' Marksman bounds; kept local so this adapter has no role dependency.
     * 与 ControlExpertRules 和 UsecRules 的精确枪手倍率上下限一致；在此本地保存，使适配器不依赖职业模块。 */
    private static final double MIN_MULTIPLIER = 1.0;
    private static final double MAX_MULTIPLIER = 1.3;

    private SparkTraitsMarksmanBridge() {
    }

    /**
     * SparkTraits Marksman range multiplier, clamped to [1.0, 1.3]; a null player is 1.0.
     * SparkTraits 精确枪手射程倍率，限制在 [1.0, 1.3]；玩家为空时为 1.0。
     */
    static double marksmanRangeMultiplier(@Nullable PlayerEntity player) {
        return player == null ? MIN_MULTIPLIER : clampMultiplier(SparkTraitsApi.getMarksmanRangeMultiplier(player));
    }

    /** Non-numeric or non-finite values fail closed to 1.0. / 非数值或非有限值回退为 1.0。 */
    static double clampMultiplier(@Nullable Object raw) {
        if (!(raw instanceof Number number)) {
            return MIN_MULTIPLIER;
        }
        double value = number.doubleValue();
        if (!Double.isFinite(value)) {
            return MIN_MULTIPLIER;
        }
        return Math.min(MAX_MULTIPLIER, Math.max(MIN_MULTIPLIER, value));
    }
}
