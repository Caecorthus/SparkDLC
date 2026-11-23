package dev.caecorthus.sparkdlc.client.role.neutral.jester;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.client.render.ManagedPostEffect;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Lifecycle for the Jester Moment grayscale: tick the fade, and release the processor on resource reload, disconnect
 * and shutdown. All GL work runs on the render thread.
 * 小丑时刻灰度的生命周期：每 tick 推进渐变；资源重载、断开连接与关闭客户端时释放处理器。所有 GL 操作都在渲染线程执行。
 * Not the inventory lock of the same moment ({@code client.trait.jester.JesterMomentInventoryLock}): another job.
 * 不同于同一时刻的背包锁定（{@code client.trait.jester.JesterMomentInventoryLock}）：两者职责不同。
 */
public final class JesterMomentGrayscaleLifecycle {
    private JesterMomentGrayscaleLifecycle() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(JesterMomentGrayscale::tick);
        ManagedPostEffect.registerLifecycle(SparkDLC.id("jester_moment_grayscale"),
                JesterMomentGrayscale::reset, JesterMomentGrayscale::reset, JesterMomentGrayscale::close);
    }
}
