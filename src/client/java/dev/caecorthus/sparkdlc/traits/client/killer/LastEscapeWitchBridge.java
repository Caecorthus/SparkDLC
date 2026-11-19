package dev.caecorthus.sparkdlc.traits.client.killer;

import dev.caecorthus.sparkdlc.witch.client.blackraven.BlackRavenPerceptionScreenEffects;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Delegates the single Last Escape pass to SparkWitch's compositor; a pass that was not rendered, or that failed,
 * retains the Traits fallback. The modules are fused in SparkDLC, so the compositor is always present and is called
 * directly: there is no installed renderer slot and no protocol version to confirm any more.
 * 将唯一的脱险后处理委托给 SparkWitch 合成器；未执行或失败时保留 Traits 的回退后处理。各模块已融合进 SparkDLC，
 * 合成器始终存在并被直接调用：不再有安装式渲染器槽位，也无需确认协议版本。
 */
public final class LastEscapeWitchBridge {
    // Latched by the first failure inside the Witch pass: from then on Traits keeps rendering its own fallback.
    // Witch 后处理第一次失败后锁存：此后 Traits 一直使用自己的回退后处理。
    private static boolean failed;

    private LastEscapeWitchBridge() {
    }

    /**
     * Render thread only. True means the composed pass was actually rendered, not merely supported; false (not the
     * local player, no escape composition, shader load failure, or a failed pass) lets Traits render its fallback.
     * 仅在渲染线程调用；成功执行合成后处理才返回 true。返回 false（非本地玩家、无脱险参数、着色器加载失败或后处理失败）
     * 时由 Traits 回退渲染。
     */
    public static boolean tryRender(PlayerEntity player, float delta) {
        if (failed) {
            return false;
        }
        try {
            return player != null && BlackRavenPerceptionScreenEffects.renderLastEscape(player, delta);
        } catch (RuntimeException | LinkageError ignored) {
            // Render fault isolation: a broken Witch pass must not take the escape grayscale away.
            // 渲染故障隔离：Witch 后处理出错时不能让脱险灰阶消失。
            failed = true;
            return false;
        }
    }
}
