package dev.caecorthus.sparkdlc.client.role.civilian.saint;

import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.caecorthus.sparkdlc.role.civilian.saint.SaintGunDropService;
import dev.doctor4t.wathe.api.event.ShouldAllowSuppressedKey;
import net.minecraft.client.MinecraftClient;

/**
 * Client half of the Saint's gun drop (owner 2026-11-28). Wathe suppresses the drop key of a living player while the
 * train HUD is on and asks {@code ShouldAllowSuppressedKey} first; this listener lets the key through for a Saint whose
 * selected stack is a gun ({@link SaintGunDropService#mayThrowSelectedGun}), with no screen open: inside an inventory
 * screen the key would throw the hovered slot, which Wathe refuses on the server only. The vanilla key handler then
 * predicts the drop and sends the action the server half performs. Any listener answering true allows a key, so the
 * registration order does not matter.
 * 圣徒丢枪的客户端部分（所有者 2026-11-28）。列车 HUD 开启时 Wathe 会压制存活玩家的丢弃键，但会先询问
 * {@code ShouldAllowSuppressedKey}；本监听器在没有打开任何界面时，为选中物品堆是枪械的圣徒放行该键
 * （{@link SaintGunDropService#mayThrowSelectedGun}）：在物品栏界面里该键会丢出鼠标悬停的栏位，而 Wathe 只在服务端拒绝它。
 * 随后原版按键处理预测丢弃并发送动作，由服务端部分执行。任一监听器返回 true 即放行按键，因此注册顺序无关紧要。
 */
public final class SaintGunDropClient {
    private SaintGunDropClient() {
    }

    public static void register() {
        ShouldAllowSuppressedKey.EVENT.register(keyBinding -> {
            MinecraftClient client = MinecraftClient.getInstance();
            return SparkDlcServerConnection.isConfirmedServer()
                    && client.player != null
                    && keyBinding == client.options.dropKey
                    && client.currentScreen == null
                    && SaintGunDropService.mayThrowSelectedGun(client.player);
        });
    }
}
