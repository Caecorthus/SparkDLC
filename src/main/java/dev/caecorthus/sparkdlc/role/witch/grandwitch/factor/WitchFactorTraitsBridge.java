package dev.caecorthus.sparkdlc.role.witch.grandwitch.factor;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;

/** Only confirmed terminal deaths transfer factors. SparkTraits is fused into SparkDLC, so the Last Stand query is a
 * direct call to its public facade.
 * 仅确认终局死亡才传递因子。SparkTraits 已融合进 SparkDLC，因此背水一战查询是对其公共门面的直接调用。 */
public final class WitchFactorTraitsBridge {
    private WitchFactorTraitsBridge() { }

    public static boolean isDeathIntercepted(PlayerEntity player) {
        return SparkTraitsApi.isLastStandDeathIntercepted(player);
    }
}
