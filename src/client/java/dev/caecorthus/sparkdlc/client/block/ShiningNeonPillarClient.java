package dev.caecorthus.sparkdlc.client.block;

import dev.caecorthus.sparkdlc.net.OpenNeonPillarSettingsS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Client half of the shining neon pillar: the receiver that opens its blink settings screen. Receivers are kept per
 * payload id, so where the start-up calls this has no order to keep.
 * 闪烁霓虹灯柱的客户端部分：打开其闪烁设置界面的接收器。接收器按数据包 id 保存，因此启动流程在何处调用它没有需要保持的顺序。
 */
public final class ShiningNeonPillarClient {
    private ShiningNeonPillarClient() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(OpenNeonPillarSettingsS2CPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        context.client().setScreen(new ShiningNeonPillarScreen(payload))));
    }
}
