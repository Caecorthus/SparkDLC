package dev.caecorthus.sparkdlc.role.civilian.detective;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Payload types and server receivers of the Detective's case folder and interrogation. Registered once, from
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}; ids and codecs are those of the payload classes.
 * 侦探案卷与审讯的数据包类型与服务端接收器。由 {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} 注册一次；
 * id 与编解码器取自各数据包类。
 */
public final class DetectivePackets {
    private DetectivePackets() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(SelectDetectiveCaseC2SPacket.ID, SelectDetectiveCaseC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateDetectiveCaseNotesC2SPacket.ID, UpdateDetectiveCaseNotesC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SetDetectiveKillerGuessC2SPacket.ID, SetDetectiveKillerGuessC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SetDetectivePresumedKillerC2SPacket.ID, SetDetectivePresumedKillerC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(InterrogateDetectiveSuspectC2SPacket.ID, InterrogateDetectiveSuspectC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenDetectiveFolderS2CPacket.ID, OpenDetectiveFolderS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenDetectiveInterrogationS2CPacket.ID, OpenDetectiveInterrogationS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(
                DetectiveInterrogationResultS2CPacket.ID,
                DetectiveInterrogationResultS2CPacket.CODEC
        );
        // Detective folder edits: the service re-validates role, liveness and case ownership (client input is untrusted).
        // 侦探文件夹编辑：服务端重新校验身份、存活状态与案件归属，客户端输入不可信。
        ServerPlayNetworking.registerGlobalReceiver(SelectDetectiveCaseC2SPacket.ID,
                (payload, context) -> DetectiveCaseService.handleSelectCase(context.player(), payload.caseId()));
        ServerPlayNetworking.registerGlobalReceiver(UpdateDetectiveCaseNotesC2SPacket.ID,
                (payload, context) -> DetectiveCaseService.handleUpdateNotes(
                        context.player(),
                        payload.caseId(),
                        payload.notes()
                ));
        ServerPlayNetworking.registerGlobalReceiver(SetDetectiveKillerGuessC2SPacket.ID,
                (payload, context) -> DetectiveCaseService.handleSetKillerGuess(
                        context.player(),
                        payload.caseId(),
                        payload.roleId()
                ));
        ServerPlayNetworking.registerGlobalReceiver(SetDetectivePresumedKillerC2SPacket.ID,
                (payload, context) -> DetectiveCaseService.handleSetPresumedKiller(
                        context.player(),
                        payload.caseId(),
                        payload.playerUuid()
                ));
        ServerPlayNetworking.registerGlobalReceiver(InterrogateDetectiveSuspectC2SPacket.ID,
                (payload, context) -> DetectiveCaseService.handleInterrogate(context.player(), payload.playerUuid()));
    }
}
