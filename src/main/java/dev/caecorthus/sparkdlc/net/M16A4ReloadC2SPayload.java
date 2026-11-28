package dev.caecorthus.sparkdlc.net;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * C2S intent {@code sparkdlc:m16a4_reload}: the local player pressed attack while holding the rifle. The server
 * revalidates the main hand, the magazine and an already running reload.
 * C2S 意图 {@code sparkdlc:m16a4_reload}：本地玩家手持步枪按下攻击键。服务端重新校验主手、弹匣与是否已在换弹。
 */
public record M16A4ReloadC2SPayload() implements CustomPayload {
    public static final M16A4ReloadC2SPayload INSTANCE = new M16A4ReloadC2SPayload();
    public static final Id<M16A4ReloadC2SPayload> ID = new Id<>(SparkDLC.id("m16a4_reload"));
    public static final PacketCodec<RegistryByteBuf, M16A4ReloadC2SPayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
