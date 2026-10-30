package dev.caecorthus.sparkdlc.witch.net;

import dev.caecorthus.sparkdlc.witch.SparkWitch;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Stops the duel track on every client. / 在所有客户端停止决斗音乐。 */
public record KnightDuelStopSoundS2CPayload() implements CustomPayload {
    public static final Id<KnightDuelStopSoundS2CPayload> ID =
            new Id<>(SparkWitch.id("knight_duel_stop_sound"));
    public static final PacketCodec<RegistryByteBuf, KnightDuelStopSoundS2CPayload> CODEC =
            PacketCodec.unit(new KnightDuelStopSoundS2CPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
