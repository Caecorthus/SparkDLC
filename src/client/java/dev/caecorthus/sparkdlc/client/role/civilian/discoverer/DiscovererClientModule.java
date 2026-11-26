package dev.caecorthus.sparkdlc.client.role.civilian.discoverer;

import dev.caecorthus.sparkdlc.net.DiscovererNightVisionC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

/**
 * Sends the Discoverer's Night Vision request. Everything else — the light test, the cooldown, the effect — is the
 * server's, so a modified client can only ask.
 * 发送探索者的夜视请求。其余（亮度判定、冷却、效果）都在服务端，改过的客户端只能“请求”。
 */
public final class DiscovererClientModule {

    private DiscovererClientModule() {
    }

    /** The ability key for a live, survival Discoverer. / 存活且为生存模式的探索者按下技能键时调用。 */
    public static void onAbilityKey(MinecraftClient client) {
        if (client.player == null || client.player.isSpectator()) {
            return;
        }
        ClientPlayNetworking.send(new DiscovererNightVisionC2SPacket());
    }
}
