package dev.caecorthus.sparkdlc.client.trait.killer;

import dev.caecorthus.sparkdlc.client.role.killer.blackraven.BlackRavenPerceptionScreenEffects;
import net.minecraft.entity.player.PlayerEntity;

/**
 * The single composed Last Escape pass: hands it to the perception compositor
 * ({@link BlackRavenPerceptionScreenEffects#renderLastEscape}); a pass that was not rendered, or that failed, keeps the
 * trait feature's own fallback grayscale ({@code DepressionScreenEffects}). The compositor is called directly: there
 * is no installed renderer slot and no protocol version to confirm.
 * 唯一的脱险合成后处理：交给感知合成器（{@link BlackRavenPerceptionScreenEffects#renderLastEscape}）；未执行或失败时保留
 * 特质功能自己的回退灰阶（{@code DepressionScreenEffects}）。合成器被直接调用：没有安装式渲染器槽位，也无需确认协议版本。
 */
public final class LastEscapeComposedPass {
    // Latched by the first failure inside the composed pass: from then on the trait feature keeps rendering its own
    // fallback. / 合成后处理第一次失败后锁存：此后特质功能一直使用自己的回退后处理。
    private static boolean failed;

    private LastEscapeComposedPass() {
    }

    /**
     * Render thread only. True means the composed pass was actually rendered, not merely supported; false (not the
     * local player, no escape composition, shader load failure, or a failed pass) lets the trait feature render its
     * fallback.
     * 仅在渲染线程调用；成功执行合成后处理才返回 true。返回 false（非本地玩家、无脱险参数、着色器加载失败或后处理失败）
     * 时由特质功能回退渲染。
     */
    public static boolean tryRender(PlayerEntity player, float delta) {
        if (failed) {
            return false;
        }
        try {
            return player != null && BlackRavenPerceptionScreenEffects.renderLastEscape(player, delta);
        } catch (RuntimeException | LinkageError ignored) {
            // Render fault isolation: a broken composed pass must not take the escape grayscale away.
            // 渲染故障隔离：合成后处理出错时不能让脱险灰阶消失。
            failed = true;
            return false;
        }
    }
}
