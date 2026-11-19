package dev.caecorthus.sparkdlc.bridge.strength;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Bridge to SparkTraits' public facade for the sabotage system: is the Master Saboteur trait active. The modules
 * are fused in SparkDLC, so this calls {@link SparkTraitsApi#hasActiveTrait} directly.
 * 破坏系统使用的 SparkTraits 公开门面桥接：是否拥有生效中的「破坏大师」。各模块已融合进 SparkDLC，因此这里直接调用
 * {@link SparkTraitsApi#hasActiveTrait}。
 */
public final class SparkTraitsSabotageCompat {
    public static final Identifier MASTER_SABOTEUR_ID = Identifier.of("sparkdlc", "master_saboteur");

    private SparkTraitsSabotageCompat() {
    }

    public static boolean isMasterSaboteur(PlayerEntity player) {
        return player != null && SparkTraitsApi.hasActiveTrait(player, MASTER_SABOTEUR_ID);
    }
}
