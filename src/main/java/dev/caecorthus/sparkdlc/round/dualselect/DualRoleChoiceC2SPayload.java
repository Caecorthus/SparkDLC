package dev.caecorthus.sparkdlc.round.dualselect;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Client -> server: which of the two offered roles the player wants. / 客户端 → 服务端：两个身份里选了哪个。 */
public record DualRoleChoiceC2SPayload(int index) implements CustomPayload {
    public static final Id<DualRoleChoiceC2SPayload> ID = new Id<>(SparkDLC.id("dual_role_choice"));
    public static final PacketCodec<RegistryByteBuf, DualRoleChoiceC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, DualRoleChoiceC2SPayload::index,
            DualRoleChoiceC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static final class Receiver implements ServerPlayNetworking.PlayPayloadHandler<DualRoleChoiceC2SPayload> {
        @Override
        public void receive(DualRoleChoiceC2SPayload payload, ServerPlayNetworking.Context context) {
            DualRoleSelectionComponent.KEY.get(context.player().getServer().getScoreboard())
                    .choose(context.player().getUuid(), payload.index());
        }
    }
}
