package dev.caecorthus.sparkdlc.client.item.m16a4;

import dev.caecorthus.sparkdlc.item.m16a4.M16A4Rules;
import dev.caecorthus.sparkdlc.net.M16A4ShotS2CPayload;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Client half of the M16A4: each accepted round kicks the view by the revolver's four degrees, and the reload input
 * ({@link M16A4ReloadInput}, driven by {@code M16A4InputMixin}) is re-armed at every tick end and reset whenever the
 * connection changes.
 * M16A4 的客户端部分：每发被接受的子弹按左轮的 4 度抬起视角；换弹输入（{@link M16A4ReloadInput}，由
 * {@code M16A4InputMixin} 驱动）在每刻末尾重新上闸，并在连接变化时重置。
 */
public final class M16A4Client {
    private M16A4Client() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(M16A4ShotS2CPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    ClientPlayerEntity player = context.client().player;
                    if (player != null) {
                        player.setPitch(player.getPitch() - M16A4Rules.RECOIL_PITCH);
                    }
                }));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (SparkDlcServerConnection.isConfirmedServer()) {
                M16A4ReloadInput.tick(client);
            } else {
                M16A4ReloadInput.reset();
            }
        });
        // Fabric may fire DISCONNECT on the Netty thread, so the reset is handed to the client thread like every
        // other session reset. / Fabric 可能在 Netty 线程触发 DISCONNECT，因此与其他会话重置一样把重置移交给客户端线程。
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(M16A4ReloadInput::reset));
        // Also on JOIN: a proxy backend switch re-joins without a disconnect. / JOIN 时同样重置：代理切换后端时不会断线而是重新加入。
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> M16A4ReloadInput.reset());
    }
}
