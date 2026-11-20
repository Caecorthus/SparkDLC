package dev.caecorthus.sparkdlc.role.killer.blackraven;

import dev.caecorthus.sparkdlc.net.OpenBlackRavenDisguiseS2CPacket;
import dev.caecorthus.sparkdlc.net.OpenBlackRavenLedgerS2CPacket;
import dev.caecorthus.sparkdlc.net.SelectBlackRavenDisguiseC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.blackraven.disguise.BlackRavenDisguiseService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Payload types and server receivers of the Black Raven's ledger and disguise. Registered once, from
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}; ids and codecs are those of the payload classes.
 * 黑羽鸦账本与伪装的数据包类型与服务端接收器。由 {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} 注册一次；
 * id 与编解码器取自各数据包类。
 */
public final class BlackRavenPackets {
    private BlackRavenPackets() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(
                OpenBlackRavenLedgerS2CPacket.ID,
                OpenBlackRavenLedgerS2CPacket.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                OpenBlackRavenDisguiseS2CPacket.ID,
                OpenBlackRavenDisguiseS2CPacket.CODEC
        );
        PayloadTypeRegistry.playC2S().register(
                SelectBlackRavenDisguiseC2SPacket.ID,
                SelectBlackRavenDisguiseC2SPacket.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(SelectBlackRavenDisguiseC2SPacket.ID,
                (payload, context) -> BlackRavenDisguiseService.requestSwitch(
                        context.player(), payload.session(), payload.target()));
    }
}
