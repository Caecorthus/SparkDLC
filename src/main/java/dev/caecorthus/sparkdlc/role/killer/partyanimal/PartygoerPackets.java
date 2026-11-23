package dev.caecorthus.sparkdlc.role.killer.partyanimal;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Payload types and server receivers of the Party Animal's Partygoer. Registered once, from
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}; ids and codecs are those of the payload classes.
 * 派对狂的派对客的数据包类型与服务端接收器。由 {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} 注册一次；
 * id 与编解码器取自各数据包类。
 */
public final class PartygoerPackets {
    private PartygoerPackets() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(PartygoerMenuC2SPacket.ID, PartygoerMenuC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(PartygoerSummonC2SPacket.ID, PartygoerSummonC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(PartygoerSwallowC2SPacket.ID, PartygoerSwallowC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(PartygoerMenuS2CPacket.ID, PartygoerMenuS2CPacket.CODEC);
        // Party Animal's Partygoer: picker request and summon; the service re-checks every gate.
        // 派对狂的派对客：选房请求与召唤；服务层重新校验所有门槛。
        ServerPlayNetworking.registerGlobalReceiver(PartygoerMenuC2SPacket.ID,
                (payload, context) -> PartygoerSkillService.openMenu(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(PartygoerSummonC2SPacket.ID,
                (payload, context) -> PartygoerSkillService.summon(context.player(), payload.spawnRoom(),
                        payload.destinationRoom()));
        // Taotie swallowing a Partygoer (NoellesRoles ability key); the service re-checks every gate.
        // 饕餮吞派对客（NoellesRoles 技能键）；服务层重新校验所有门槛。
        ServerPlayNetworking.registerGlobalReceiver(PartygoerSwallowC2SPacket.ID,
                (payload, context) -> PartygoerTaotieService.handleSwallow(context.player(), payload));
    }
}
