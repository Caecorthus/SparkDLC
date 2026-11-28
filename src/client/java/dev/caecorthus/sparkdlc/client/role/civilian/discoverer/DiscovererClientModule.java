package dev.caecorthus.sparkdlc.client.role.civilian.discoverer;

import dev.caecorthus.sparkdlc.net.DiscovererNightVisionC2SPacket;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

/**
 * Client side of the Discoverer's Night Vision key (PR #12): the owner-only cooldown line, from one
 * confirmed-server-gated HUD callback, and the key itself, dispatched here by {@code SparkDLCClient}. Server
 * authority: the client only sends the empty {@code sparkdlc:discoverer_night_vision} request and never checks the
 * light, the cooldown or liveness; the server validates everything and resyncs the cooldown through the owner-only
 * {@code sparkdlc:discoverer_player} component. Role-owned; never the {@code gui.sparkdlc.skills} panel.
 * 探索者夜视键（PR #12）的客户端部分：仅本人可见的冷却行（使用一个仅在已确认服务器上生效的 HUD 回调），以及由
 * {@code SparkDLCClient} 分发到这里的按键本身。服务端权威：客户端只发送空的 {@code sparkdlc:discoverer_night_vision} 请求，
 * 从不检查亮度、冷却或存活状态；全部校验在服务端完成，冷却经由仅本人同步的 {@code sparkdlc:discoverer_player} 组件重新同步。
 * 属于职业自有功能，从不进入 {@code gui.sparkdlc.skills} 面板。
 */
public final class DiscovererClientModule {
    private static boolean initialized;

    private DiscovererClientModule() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (SparkDlcServerConnection.isConfirmedServer()) {
                DiscovererNightVisionHud.render(context);
            }
        });
    }

    /**
     * Called by the shared ability-key dispatcher in {@code SparkDLCClient} when the local player's real role is the
     * Discoverer. Skips the send only when the server has no receiver for the request (fail closed).
     * 当本地玩家的真实职业为探索者时，由 {@code SparkDLCClient} 的共享技能键分发调用。仅在服务端没有该请求的接收器时跳过发送
     * （失败即关闭）。
     */
    public static void onAbilityKey(MinecraftClient client) {
        if (client != null && client.player != null
                && ClientPlayNetworking.canSend(DiscovererNightVisionC2SPacket.ID)) {
            ClientPlayNetworking.send(new DiscovererNightVisionC2SPacket());
        }
    }
}
