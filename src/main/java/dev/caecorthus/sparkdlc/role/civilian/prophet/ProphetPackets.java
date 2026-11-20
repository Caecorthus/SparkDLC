package dev.caecorthus.sparkdlc.role.civilian.prophet;

import dev.caecorthus.sparkdlc.net.ConfirmProphecyC2SPacket;
import dev.caecorthus.sparkdlc.net.OpenProphecyS2CPacket;
import dev.caecorthus.sparkdlc.net.OpenProphetNecrologyS2CPacket;
import dev.caecorthus.sparkdlc.net.RequestProphecyC2SPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Payload types and server receivers of the Prophet's prophecy session and Necrology. Registered once, from
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}; ids and codecs are those of the payload classes.
 * 先知预言会话与亡者名录的数据包类型与服务端接收器。由 {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} 注册一次；
 * id 与编解码器取自各数据包类。
 */
public final class ProphetPackets {
    private ProphetPackets() {
    }

    public static void register() {
        // Prophecy uses its own session packets; the generic skill packet is refused for it.
        // 预言使用自有会话数据包；通用技能包对其一律拒绝。
        PayloadTypeRegistry.playC2S().register(RequestProphecyC2SPacket.ID, RequestProphecyC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ConfirmProphecyC2SPacket.ID, ConfirmProphecyC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenProphecyS2CPacket.ID, OpenProphecyS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(
                OpenProphetNecrologyS2CPacket.ID,
                OpenProphetNecrologyS2CPacket.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(RequestProphecyC2SPacket.ID,
                (payload, context) -> ProphetProphecyService.requestSession(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(ConfirmProphecyC2SPacket.ID,
                (payload, context) -> ProphetProphecyService.confirmGuess(
                        context.player(), payload.sessionId(), payload.victim(), payload.groupId()));
    }
}
