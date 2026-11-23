package dev.caecorthus.sparkdlc.round.modevote;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

/** Client -> server: which mode the player wants for the next round. / 客户端 → 服务端：玩家为下一局投的模式。 */
public record ModeVoteC2SPayload(int modeIndex) implements CustomPayload {
    public static final Id<ModeVoteC2SPayload> ID = new Id<>(SparkDLC.id("mode_vote"));
    public static final PacketCodec<RegistryByteBuf, ModeVoteC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, ModeVoteC2SPayload::modeIndex,
            ModeVoteC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static final class Receiver implements ServerPlayNetworking.PlayPayloadHandler<ModeVoteC2SPayload> {
        @Override
        public void receive(ModeVoteC2SPayload payload, ServerPlayNetworking.Context context) {
            ServerPlayerEntity player = context.player();
            ModeVotingComponent component = ModeVotingComponent.KEY.get(player.getServer().getScoreboard());
            component.castVote(player.getUuid(), payload.modeIndex());
        }
    }
}
