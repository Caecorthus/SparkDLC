package dev.caecorthus.sparkdlc.traits.client.compat;

import dev.caecorthus.sparkdlc.witch.client.blackraven.BlackRavenInstinctClientHooks;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Bridge to SparkWitch's Black Raven sensed-instinct resolver. The Black Raven is a civilian police role
 * (2026-10-21); SparkWitch answers its 8-block raven-feather x-ray through
 * {@code resolvePrioritySensedHighlight(Entity)}. The modules are fused in SparkDLC, so this calls
 * {@link BlackRavenInstinctClientHooks} directly; the instinct hook asks for every visible entity each frame.
 * 到 SparkWitch 黑羽鸦感知本能裁决器的桥接。黑羽鸦现为好人警察身份（2026-10-21）；SparkWitch 通过
 * {@code resolvePrioritySensedHighlight(Entity)} 给出 8 格鸦羽色透视结果。各模块已融合进 SparkDLC，因此这里直接调用
 * {@link BlackRavenInstinctClientHooks}；本能钩子每帧为每个可见实体调用此处。
 */
public final class SparkWitchBlackRavenBridge {
    private SparkWitchBlackRavenBridge() {
    }

    public static @Nullable Integer resolveSensedInstinctHighlight(Entity target) {
        if (target == null) {
            return null;
        }
        return BlackRavenInstinctClientHooks.resolvePrioritySensedHighlight(target);
    }
}
