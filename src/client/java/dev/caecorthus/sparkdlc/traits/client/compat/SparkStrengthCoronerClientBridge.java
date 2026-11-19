package dev.caecorthus.sparkdlc.traits.client.compat;

import dev.caecorthus.sparkdlc.strength.client.role.coroner.CoronerClientHooks;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Bridge to SparkStrength's Coroner instinct resolver.
 *
 * <p>SparkTraits owns the early WatheClient instinct mixin, so the normal Wathe
 * {@code GetInstinctHighlight} event can be bypassed before SparkStrength gets a
 * chance to report a killer disguise. This bridge lets SparkTraits ask SparkStrength
 * for only that one presentation result. The modules are fused in SparkDLC, so it
 * calls {@link CoronerClientHooks} directly.</p>
 *
 * <p>SparkStrength 验尸官本能桥接。SparkTraits 掌握 WatheClient
 * 的早期本能 Mixin，可能在 SparkStrength 收到普通本能事件之前就直接返回结果。
 * 因此这里只查询这一个客户端显示结果。各模块已融合进 SparkDLC，
 * 因此直接调用 {@link CoronerClientHooks}。</p>
 */
public final class SparkStrengthCoronerClientBridge {
    private SparkStrengthCoronerClientBridge() {
    }

    /**
     * Returns SparkStrength's killer-disguise color, or {@code null} when the
     * target is not such a disguise.
     */
    public static @Nullable Integer resolveKillerDisguiseInstinctColor(Entity target) {
        if (target == null) {
            return null;
        }
        return CoronerClientHooks.resolveKillerDisguiseInstinctColor(target);
    }
}
