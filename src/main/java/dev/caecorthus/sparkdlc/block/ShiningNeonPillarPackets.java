package dev.caecorthus.sparkdlc.block;

import dev.caecorthus.sparkdlc.net.ApplyNeonPillarSettingsC2SPacket;
import dev.caecorthus.sparkdlc.net.OpenNeonPillarSettingsS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * The shining neon pillar's payloads, registered from {@code net.SparkDlcPackets}: the settings screen and the
 * settings it sends back. No listener, so the hub's order is untouched.
 * 闪烁霓虹灯柱的数据包，由 {@code net.SparkDlcPackets} 调用注册：设置界面及其回传的设置。不注册监听器，因此不影响注册中心的顺序。
 */
public final class ShiningNeonPillarPackets {
    private ShiningNeonPillarPackets() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(OpenNeonPillarSettingsS2CPacket.ID, OpenNeonPillarSettingsS2CPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ApplyNeonPillarSettingsC2SPacket.ID, ApplyNeonPillarSettingsC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ApplyNeonPillarSettingsC2SPacket.ID,
                (payload, context) -> ShiningNeonPillarService.applySettings(context.player(), payload));
    }
}
