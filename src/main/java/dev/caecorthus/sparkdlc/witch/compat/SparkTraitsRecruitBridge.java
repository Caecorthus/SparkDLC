package dev.caecorthus.sparkdlc.witch.compat;

import dev.caecorthus.sparkdlc.traits.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * SparkTraits seam for the Recruit. Calls only the public facade
 * {@code SparkTraitsApi.hasActiveTrait(PlayerEntity, Identifier)}; never {@code sparktraits.impl} or
 * {@code sparktraits.component}. SparkTraits is fused into SparkDLC, so the call is direct; a null player reads as
 * "not Impostor", so the Recruit keeps its knife sanity cost and innocent-kill death.
 * 新兵的 SparkTraits 接缝。仅调用公共门面 {@code SparkTraitsApi.hasActiveTrait}，从不触及 {@code impl} 或
 * {@code component} 包。SparkTraits 已融合进 SparkDLC，因此为直接调用；玩家为空时视为“非内鬼”，新兵保留用刀理智消耗与
 * 误杀处死。
 */
public final class SparkTraitsRecruitBridge {
    private static final String MOD_ID = "sparktraits";
    private static final Identifier IMPOSTOR = Identifier.of(MOD_ID, "impostor");

    private SparkTraitsRecruitBridge() {
    }

    /** Whether the SparkTraits Impostor trait is active; false for a null player.
     * SparkTraits 内鬼词条是否生效；玩家为空时为 false。 */
    public static boolean isImpostor(@Nullable PlayerEntity player) {
        return player != null && SparkTraitsApi.hasActiveTrait(player, IMPOSTOR);
    }
}
