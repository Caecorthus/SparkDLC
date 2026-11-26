package dev.caecorthus.sparkdlc.net;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Asks the server for the Discoverer's Night Vision. Empty: the server reads the light level and the cooldown itself.
 * 请求服务端的探索者夜视。空包：亮度与冷却都由服务端自己读取。
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
