package dev.caecorthus.sparkdlc.role.killer.natsume;

import dev.caecorthus.sparkdlc.net.ConfirmNatsumeTargetC2SPacket;
import dev.caecorthus.sparkdlc.net.OpenNatsumeMenuS2CPacket;
import dev.caecorthus.sparkdlc.net.RequestNatsumeMenuC2SPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Payload types and server receivers of 夏目安安 (Natsume). Registered once, from
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}; ids and codecs are those of the payload classes.
 * 夏目安安的数据包类型与服务端接收器。由 {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} 注册一次；
 * id 与编解码器取自各数据包类。
 */
public final class NatsumePackets {
    private NatsumePackets() {
    }

    public static void register() {
        // 夏目安安 (PR #194): the target-menu request, the menu itself, and the confirmed pick.
        // 夏目安安（PR #194）：目标菜单请求、菜单本身与确认选择。
        PayloadTypeRegistry.playC2S().register(RequestNatsumeMenuC2SPacket.ID, RequestNatsumeMenuC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ConfirmNatsumeTargetC2SPacket.ID, ConfirmNatsumeTargetC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenNatsumeMenuS2CPacket.ID, OpenNatsumeMenuS2CPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RequestNatsumeMenuC2SPacket.ID,
                (payload, context) -> context.server().execute(() ->
                        NatsumeService
                                .requestMenu(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ConfirmNatsumeTargetC2SPacket.ID,
                (payload, context) -> context.server().execute(() ->
                        NatsumeService
                                .cast(context.player(), payload.target())));
    }
}
