package dev.caecorthus.sparkdlc.role.civilian.judge;

import dev.caecorthus.sparkdlc.net.ConfirmJudgeSelectionC2SPacket;
import dev.caecorthus.sparkdlc.net.OpenJudgeSelectionC2SPacket;
import dev.caecorthus.sparkdlc.net.OpenJudgeSelectionS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Payload types and server receivers of the Judge's selection. Registered once, from
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}; ids and codecs are those of the payload classes.
 * 法官选择界面的数据包类型与服务端接收器。由 {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} 注册一次；
 * id 与编解码器取自各数据包类。
 */
public final class JudgePackets {
    private JudgePackets() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(OpenJudgeSelectionC2SPacket.ID, OpenJudgeSelectionC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ConfirmJudgeSelectionC2SPacket.ID, ConfirmJudgeSelectionC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenJudgeSelectionS2CPacket.ID, OpenJudgeSelectionS2CPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(OpenJudgeSelectionC2SPacket.ID,
                (payload, context) -> JudgeRuntime.openSelection(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(ConfirmJudgeSelectionC2SPacket.ID,
                (payload, context) -> JudgeRuntime.confirmSelection(context.player(), payload.sessionId(), payload.targetId()));
    }
}
