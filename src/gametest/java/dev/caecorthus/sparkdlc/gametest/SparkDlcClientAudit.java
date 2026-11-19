package dev.caecorthus.sparkdlc.gametest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

/**
 * Client half of the boot check (`./gradlew runClientAudit`): starts a real client, waits for the first resource
 * reload to finish, force-applies every client mixin of all five modules, then closes the game. Minecraft 1.21.1 has
 * no client game-test API, so this runs as a plain client entrypoint that does nothing unless the run sets
 * {@code -Dsparkdlc.clientAudit=true}.
 * 启动检查的客户端部分（`./gradlew runClientAudit`）：启动真实客户端，等首次资源重载完成，强制应用五个模块的全部客户端
 * mixin，然后关闭游戏。Minecraft 1.21.1 没有客户端 game-test API，因此它是一个普通的客户端入口，只有运行配置设置了
 * {@code -Dsparkdlc.clientAudit=true} 时才会动作。
 */
public final class SparkDlcClientAudit implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("sparkdlc_gametest");
    private boolean done;

    @Override
    public void onInitializeClient() {
        if (!Boolean.getBoolean("sparkdlc.clientAudit")) {
            return;
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // The loading overlay stays up until the resource reload is over; models, fonts, shaders and lang files
            // have been read by then, so their errors are already in the log.
            // 资源重载结束前加载遮罩一直存在；此时模型、字体、着色器和语言文件都已读完，相关错误已在日志里。
            if (this.done || client.getOverlay() != null) {
                return;
            }
            this.done = true;
            MixinEnvironment.getCurrentEnvironment().audit();
            LOGGER.info("[SparkDLC client audit] resource reload finished and every client mixin applied");
            client.scheduleStop();
        });
    }
}
