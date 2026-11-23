package dev.caecorthus.sparkdlc.role.civilian.sherry.net;

import dev.caecorthus.sparkdlc.role.civilian.sherry.SherryDeductionService;
import dev.caecorthus.sparkdlc.role.civilian.sherry.SherryMightyForceService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Payload types and server receivers of 橘雪莉 (Sherry). Registered once, from
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}; ids and codecs are those of the payload classes.
 * 橘雪莉的数据包类型与服务端接收器。由 {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} 注册一次；
 * id 与编解码器取自各数据包类。
 */
public final class SherryPackets {
    private SherryPackets() {
    }

    public static void register() {
        // 橘雪莉 (PR #206): the 推演 panel request (shared key), its paid confirmation, her own 巨力魔法 (Role Skill 2),
        // and the candidate panel the server answers with.
        // 橘雪莉（PR #206）：推演面板请求（共享技能键）、付费确认、她自己的巨力魔法（职业技能 2），以及服务端回应的候选面板。
        PayloadTypeRegistry.playC2S().register(
                RequestSherryDeductionC2SPacket.ID,
                RequestSherryDeductionC2SPacket.CODEC
        );
        PayloadTypeRegistry.playC2S().register(
                ConfirmSherryDeductionC2SPacket.ID,
                ConfirmSherryDeductionC2SPacket.CODEC
        );
        PayloadTypeRegistry.playC2S().register(
                UseSherryMightyForceC2SPacket.ID,
                UseSherryMightyForceC2SPacket.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                OpenSherryDeductionS2CPacket.ID,
                OpenSherryDeductionS2CPacket.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(RequestSherryDeductionC2SPacket.ID,
                (payload, context) -> context.server().execute(() ->
                        SherryDeductionService.requestOpen(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ConfirmSherryDeductionC2SPacket.ID,
                (payload, context) -> context.server().execute(() ->
                        SherryDeductionService.confirm(context.player(), payload.target())));
        ServerPlayNetworking.registerGlobalReceiver(UseSherryMightyForceC2SPacket.ID,
                (payload, context) -> context.server().execute(() ->
                        SherryMightyForceService.use(context.player())));
    }
}
