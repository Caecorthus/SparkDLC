package dev.caecorthus.sparkdlc.client.role.civilian.seeker;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Cross-module seam (SparkTraits): the Seeker's own read of the public {@code SparkTraitsApi
 * .isInstinctHidden(viewer, target)} facade, deliberately not Black Raven's bridge. SparkTraits is fused into
 * SparkDLC, so the call is direct; a null viewer or target hides nothing. Only the public facade is named, never
 * SparkTraits internals.
 * 跨模块接缝（SparkTraits）：搜寻者自有的一份公共 {@code SparkTraitsApi.isInstinctHidden(viewer, target)}
 * 门面读取，刻意不复用黑鸦的桥。SparkTraits 已融合进 SparkDLC，因此为直接调用；观察者或目标为空时不隐藏任何人。
 * 只引用公共门面，从不触及 SparkTraits 内部实现。
 */
public final class SeekerInstinctVisibilityBridge {
    private SeekerInstinctVisibilityBridge() {
    }

    public static boolean isHidden(PlayerEntity viewer, PlayerEntity target) {
        return viewer != null && target != null && SparkTraitsApi.isInstinctHidden(viewer, target);
    }
}
