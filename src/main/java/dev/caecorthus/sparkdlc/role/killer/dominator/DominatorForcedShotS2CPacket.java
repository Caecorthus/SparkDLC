package dev.caecorthus.sparkdlc.role.killer.dominator;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * Server to the marked player: close an open inventory or container screen and refuse to open one for
 * {@code lockTicks}. Its id differs from the {@code sparkdlc:dominator_forced_shot} CCA component id.
 * 服务端发给被标记者：关闭已打开的物品栏或容器界面，并在 {@code lockTicks} 内拒绝再次打开。其 id 与
 * {@code sparkdlc:dominator_forced_shot} CCA 组件 id 不同。
 */
public record DominatorForcedShotS2CPacket(int lockTicks) implements CustomPayload {
    public static final Id<DominatorForcedShotS2CPacket> ID =
            new Id<>(SparkDLC.id("dominator_forced_shot_lock"));
    public static final PacketCodec<RegistryByteBuf, DominatorForcedShotS2CPacket> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeVarInt(Math.max(0, value.lockTicks)),
            buf -> new DominatorForcedShotS2CPacket(Math.max(0, buf.readVarInt()))
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
