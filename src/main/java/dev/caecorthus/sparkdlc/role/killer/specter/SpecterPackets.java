package dev.caecorthus.sparkdlc.role.killer.specter;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Payload types and server receivers of the Specter's two skills. Registered once, from
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}; ids and codecs are those of the payload classes.
 * 幽魂的两个技能的数据包类型与服务端接收器。由 {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} 注册一次；
 * id 与编解码器取自各数据包类。
 */
public final class SpecterPackets {
    private SpecterPackets() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(UseSpecterSkillC2SPacket.ID, UseSpecterSkillC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(
                UseSpecterSecondSkillC2SPacket.ID,
                UseSpecterSecondSkillC2SPacket.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(UseSpecterSkillC2SPacket.ID,
                (payload, context) -> SpecterSkillService.use(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(UseSpecterSecondSkillC2SPacket.ID,
                (payload, context) -> SpecterSecondSkillService.use(context.player()));
    }
}
