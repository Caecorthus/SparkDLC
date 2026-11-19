package dev.caecorthus.sparkdlc.client.role.killer.blackraven;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;

/** Bridge to SparkTraits' stable visibility facade only; SparkTraits is fused into SparkDLC, so the call is direct.
 * 仅连接 SparkTraits 稳定可见性门面的桥；SparkTraits 已融合进 SparkDLC，因此为直接调用。 */
public final class SparkTraitsInstinctVisibilityBridge {
    private SparkTraitsInstinctVisibilityBridge() {
    }

    public static boolean isHidden(PlayerEntity viewer, PlayerEntity target) {
        return viewer != null && target != null && SparkTraitsApi.isInstinctHidden(viewer, target);
    }
}
