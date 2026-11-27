package dev.caecorthus.sparkdlc.client.item.m16a4;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.item.m16a4.M16A4Rules;
import dev.caecorthus.sparkdlc.net.M16A4ReloadC2SPayload;
import dev.caecorthus.sparkdlc.net.M16A4ShotS2CPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Client half of the M16A4: each accepted round kicks the view by the revolver's four degrees, and a fresh attack-key
 * press while the rifle is in the main hand asks the server for a reload exactly once (holding the button does not
 * repeat it).
 * M16A4 的客户端部分：每发被接受的子弹按左轮的 4 度抬起视角；主手持步枪时攻击键的一次新按下只向服务端请求一次换弹
 * （按住不会重复）。
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
        ClientTickEvents.START_CLIENT_TICK.register(M16A4Client::tick);
    }

    /**
     * One reload intent per attack-key press. {@code wasPressed} counts the key-down events instead of sampling the key
     * at the tick boundary, so a click that goes down and up inside one tick is not lost.
     * 每次按下攻击键只发一次换弹意图。{@code wasPressed} 统计按下事件而不是在刻边界采样按键状态，因此在一个刻内按下又松开的
     * 快速点击不会被漏掉。
     */
    private static void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }
        boolean pressed = client.options.attackKey.wasPressed();
        if (pressed && client.currentScreen == null && player.getMainHandStack().isOf(SparkDlcItems.m16a4())) {
            dev.caecorthus.sparkdlc.SparkDLC.LOGGER.info("[m16a4] reload request sent");
            ClientPlayNetworking.send(M16A4ReloadC2SPayload.INSTANCE);
        }
    }
}
