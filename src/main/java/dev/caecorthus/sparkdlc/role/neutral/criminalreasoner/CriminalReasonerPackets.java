package dev.caecorthus.sparkdlc.role.neutral.criminalreasoner;

import dev.caecorthus.sparkdlc.net.ConfirmCriminalReasonerC2SPacket;
import dev.caecorthus.sparkdlc.net.CriminalReasonerInvisibilityC2SPacket;
import dev.caecorthus.sparkdlc.net.OpenCriminalReasonerS2CPacket;
import dev.caecorthus.sparkdlc.net.RequestCriminalReasonerC2SPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Payload types and server receivers of the Criminal Reasoner. Registered once, from
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}; ids and codecs are those of the payload classes.
 * 犯罪推理学家的数据包类型与服务端接收器。由 {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} 注册一次；
 * id 与编解码器取自各数据包类。
 */
public final class CriminalReasonerPackets {
    private CriminalReasonerPackets() {
    }

    public static void register() {
        // Criminal Reasoner: ability key asks for the roster, menu confirms one pair, secondary key spends a charge.
        // 犯罪推理学家：技能键要名单、菜单提交一对、第二技能键消耗隐身充能。
        PayloadTypeRegistry.playC2S().register(RequestCriminalReasonerC2SPacket.ID,
                RequestCriminalReasonerC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ConfirmCriminalReasonerC2SPacket.ID,
                ConfirmCriminalReasonerC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CriminalReasonerInvisibilityC2SPacket.ID,
                CriminalReasonerInvisibilityC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenCriminalReasonerS2CPacket.ID,
                OpenCriminalReasonerS2CPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RequestCriminalReasonerC2SPacket.ID,
                (payload, context) -> CriminalReasonerRuntime.openSelection(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(ConfirmCriminalReasonerC2SPacket.ID,
                (payload, context) -> CriminalReasonerRuntime.reason(context.player(), payload.sessionId(),
                        payload.victimId(), payload.suspectId()));
        ServerPlayNetworking.registerGlobalReceiver(CriminalReasonerInvisibilityC2SPacket.ID,
                (payload, context) -> CriminalReasonerRuntime.activateInvisibility(context.player()));
    }
}
