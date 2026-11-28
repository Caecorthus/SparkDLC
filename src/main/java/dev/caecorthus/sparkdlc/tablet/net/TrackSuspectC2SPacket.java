package dev.caecorthus.sparkdlc.tablet.net;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * The Track button of a suspect row on the police channel's Suspects page. It only names the suspect: who may track,
 * whether the suspect can be tracked and the pair's cooldown are all decided by {@code TabletStateService#trackSuspect}.
 * 义警频道嫌疑分区中某一行的“追查”按钮。它只指明嫌疑人：谁可以追查、该嫌疑人能否被追查以及该组合的冷却，全部由
 * TabletStateService#trackSuspect 判定。
 */
public record TrackSuspectC2SPacket(UUID suspectUuid) implements CustomPayload {
    public static final Identifier PAYLOAD_ID = SparkDLC.id("track_suspect");
    public static final Id<TrackSuspectC2SPacket> ID = new Id<>(PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, TrackSuspectC2SPacket> CODEC =
            PacketCodec.of(TrackSuspectC2SPacket::write, TrackSuspectC2SPacket::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(suspectUuid);
    }

    public static TrackSuspectC2SPacket read(PacketByteBuf buf) {
        return new TrackSuspectC2SPacket(buf.readUuid());
    }
}
