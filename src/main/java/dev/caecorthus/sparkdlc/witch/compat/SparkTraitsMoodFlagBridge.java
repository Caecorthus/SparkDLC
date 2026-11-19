package dev.caecorthus.sparkdlc.witch.compat;

import dev.caecorthus.sparkdlc.traits.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * SparkTraits seam for the top-left mood flags ({@code client/moodflag/MoodFlags}): whether the local player holds
 * Conscience or Impostor. Calls only the public facade
 * {@code SparkTraitsApi.hasActiveTrait(PlayerEntity, Identifier)}; never {@code sparktraits.impl} or
 * {@code sparktraits.component}. SparkTraits is fused into SparkDLC, so the call is direct. Display only: a null player
 * reads as false, so Wathe's own flag stays.
 * 左上角心情旗帜（{@code client/moodflag/MoodFlags}）的 SparkTraits 接缝：本地玩家是否持有善良或内鬼词条。仅调用
 * 公共门面 {@code SparkTraitsApi.hasActiveTrait(PlayerEntity, Identifier)}，从不触及 {@code impl} 或 {@code component}
 * 包。SparkTraits 已融合进 SparkDLC，因此为直接调用。仅用于展示：玩家为空时视为 false，保留 Wathe 自己的旗帜。
 */
public final class SparkTraitsMoodFlagBridge {
    private static final String MOD_ID = "sparktraits";
    static final Identifier CONSCIENCE = Identifier.of(MOD_ID, "conscience");
    static final Identifier IMPOSTOR = Identifier.of(MOD_ID, "impostor");

    private SparkTraitsMoodFlagBridge() {
    }

    /** Conscience (善良): false unless the facade confirms it. / 善良词条：门面确认前一律为 false。 */
    public static boolean isConscienceActive(@Nullable PlayerEntity player) {
        return hasActiveTrait(player, CONSCIENCE);
    }

    /** Impostor (内鬼): false unless the facade confirms it. / 内鬼词条：门面确认前一律为 false。 */
    public static boolean isImpostorActive(@Nullable PlayerEntity player) {
        return hasActiveTrait(player, IMPOSTOR);
    }

    private static boolean hasActiveTrait(@Nullable PlayerEntity player, Identifier traitId) {
        return player != null && SparkTraitsApi.hasActiveTrait(player, traitId);
    }
}
