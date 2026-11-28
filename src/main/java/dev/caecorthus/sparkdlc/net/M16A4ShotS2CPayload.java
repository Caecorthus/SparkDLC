package dev.caecorthus.sparkdlc.net;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * S2C {@code sparkdlc:m16a4_shot}: one accepted round left the barrel, sent to its shooter so the client can kick the
 * view by the revolver's four degrees. The muzzle flash is spawned by the server at the gun, so observers need no
 * packet of their own.
 * S2C {@code sparkdlc:m16a4_shot}：一发子弹已出膛，发给射手，让客户端按左轮的 4 度抬起视角。枪口火光由服务端在枪口生成，
 * 因此观察者不需要单独的数据包。
 */
public record M16A4ShotS2CPayload() implements CustomPayload {
    public static final M16A4ShotS2CPayload INSTANCE = new M16A4ShotS2CPayload();
    public static final Id<M16A4ShotS2CPayload> ID = new Id<>(SparkDLC.id("m16a4_shot"));
    public static final PacketCodec<RegistryByteBuf, M16A4ShotS2CPayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
