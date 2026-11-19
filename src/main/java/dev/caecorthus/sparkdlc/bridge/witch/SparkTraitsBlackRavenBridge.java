package dev.caecorthus.sparkdlc.bridge.witch;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * SparkTraits seam for the Black Raven disguise economy; calls only the public facade
 * {@code SparkTraitsApi.hasActiveTrait(PlayerEntity, Identifier)} for Conscience and Impostor. SparkTraits is fused
 * into SparkDLC, so the call is direct and the answer is always TRUE or FALSE (FALSE for a null player); the nullable
 * return type is kept for callers, which pay nothing on null.
 * 黑羽鸦伪装经济的 SparkTraits 接缝；仅调用公共门面 hasActiveTrait 查询良知与内鬼词条。SparkTraits 已融合进 SparkDLC，
 * 因此为直接调用，结果恒为 TRUE 或 FALSE（玩家为空时为 FALSE）；为调用方保留可空返回类型（null 时调用方不发放）。
 */
public final class SparkTraitsBlackRavenBridge {
    private static final String MOD_ID = "sparktraits";
    static final Identifier CONSCIENCE = Identifier.of(MOD_ID, "conscience");
    static final Identifier IMPOSTOR = Identifier.of(MOD_ID, "impostor");

    private SparkTraitsBlackRavenBridge() {
    }

    public static @Nullable Boolean isConscienceActive(@Nullable PlayerEntity player) {
        return hasActiveTrait(player, CONSCIENCE);
    }

    public static @Nullable Boolean isImpostor(@Nullable PlayerEntity player) {
        return hasActiveTrait(player, IMPOSTOR);
    }

    private static @Nullable Boolean hasActiveTrait(@Nullable PlayerEntity player, Identifier traitId) {
        return player != null && SparkTraitsApi.hasActiveTrait(player, traitId);
    }
}
