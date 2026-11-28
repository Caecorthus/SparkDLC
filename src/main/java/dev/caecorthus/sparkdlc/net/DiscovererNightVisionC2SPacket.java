package dev.caecorthus.sparkdlc.net;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Stable C2S contract {@code sparkdlc:discoverer_night_vision}: an empty Night Vision request sent by the Discoverer's
 * ability key; the server reads the light level and the cooldown itself and validates everything. Registered by
 * {@code DiscovererNightVisionService}. Classified as a role skill in {@code config/payload-policy/actions.tsv}:
 * dropped in the opening safe time, under the Control Expert stun and the Taotie daze, in a Seeker or Riftwalker
 * session, in a Knight duel and under Grand Witch Fear.
 * 稳定 C2S 契约 {@code sparkdlc:discoverer_night_vision}：探索者按技能键发送的空夜视请求；亮度与冷却都由服务端自己读取，
 * 全部校验在服务端完成。由 {@code DiscovererNightVisionService} 注册。在 {@code config/payload-policy/actions.tsv} 中归类为
 * 职业技能：开局安全时间、控场专家眩晕、饕餮眩晕、搜寻者或隙行者会话、骑士决斗与大魔女恐惧期间都会被丢弃。
 */
public record DiscovererNightVisionC2SPacket() implements CustomPayload {
    public static final Identifier PAYLOAD_ID = SparkDLC.id("discoverer_night_vision");
    public static final Id<DiscovererNightVisionC2SPacket> ID = new Id<>(PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, DiscovererNightVisionC2SPacket> CODEC =
            PacketCodec.unit(new DiscovererNightVisionC2SPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
