package dev.caecorthus.sparkdlc.bridge.traits;

import dev.caecorthus.sparkdlc.sabotage.SabotageAccess;
import net.minecraft.entity.player.PlayerEntity;

/**
 * SparkStrength's tablet sabotage replaced Wathe's shop blackout. Master Saboteur is offered to players who may
 * sabotage (SparkStrength applies the trait's effects itself); the shop rule still covers Wathe's blackout. The
 * modules are fused in SparkDLC, so the provider is always present and {@link SabotageAccess} is called directly.
 * SparkStrength 的平板破坏取代了 Wathe 的商店关灯。破坏大师会提供给可以破坏的玩家（词条效果由 SparkStrength 自己实现）；
 * Wathe 关灯仍由商店规则覆盖。各模块已融合进 SparkDLC，因此该提供方始终存在，这里直接调用 {@link SabotageAccess}。
 */
public final class SparkStrengthSabotageBridge {
    private SparkStrengthSabotageBridge() {
    }

    public static boolean canSabotage(PlayerEntity player) {
        return player != null && SabotageAccess.hasSabotageIdentity(player);
    }

    /**
     * Whether SparkStrength's tablet sabotage is present (it then owns the trait's effects); always true in SparkDLC.
     * SparkStrength 的平板破坏是否存在（存在时由它实现词条效果）；在 SparkDLC 中恒为 true。
     */
    public static boolean isAvailable() {
        return true;
    }
}
