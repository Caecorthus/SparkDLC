package dev.caecorthus.sparkdlc.role.civilian.tarotreader;

import dev.caecorthus.sparkdlc.net.OpenTarotDivinationSelectorS2CPacket;
import dev.caecorthus.sparkdlc.net.SubmitTarotDivinationSelectionC2SPacket;
import dev.caecorthus.sparkdlc.net.TarotDivinationReadingS2CPacket;
import dev.caecorthus.sparkdlc.net.TarotDivinationSnapshotS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Payload types and server receivers of the Tarot Reader's divination. Registered once, from
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}; ids and codecs are those of the payload classes.
 * 塔罗牌师占卜的数据包类型与服务端接收器。由 {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} 注册一次；
 * id 与编解码器取自各数据包类。
 */
public final class TarotDivinationPackets {
    private TarotDivinationPackets() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(
                SubmitTarotDivinationSelectionC2SPacket.ID,
                SubmitTarotDivinationSelectionC2SPacket.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                TarotDivinationSnapshotS2CPacket.ID,
                TarotDivinationSnapshotS2CPacket.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                OpenTarotDivinationSelectorS2CPacket.ID,
                OpenTarotDivinationSelectorS2CPacket.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                TarotDivinationReadingS2CPacket.ID,
                TarotDivinationReadingS2CPacket.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(SubmitTarotDivinationSelectionC2SPacket.ID,
                (payload, context) -> TarotReaderDivinationService.submit(context.player(), payload));
    }
}
