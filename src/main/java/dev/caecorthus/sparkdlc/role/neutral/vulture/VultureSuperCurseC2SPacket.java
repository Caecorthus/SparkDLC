package dev.caecorthus.sparkdlc.role.neutral.vulture;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * The Vulture pressed the Role Skill 2 key: a request to Super Curse. Empty on purpose; role, liveness, skill locks and
 * the cooldown are all re-checked on the server. The payload id {@code sparkdlc:vulture_super_curse} is deny-listed by
 * id (stuns, remote views, rift sessions), so never rename it.
 * 秃鹫按下“职业技能 2”键发出的超级骂请求。故意不带字段：职业、存活、技能封锁与冷却全部由服务端重新校验。payload id
 * {@code sparkdlc:vulture_super_curse} 按 id 列入拦截名单（定身、远程视角、裂隙会话），不得改名。
 */
public record VultureSuperCurseC2SPacket() implements CustomPayload {
    public static final Identifier PAYLOAD_ID = SparkDLC.id("vulture_super_curse");
    public static final Id<VultureSuperCurseC2SPacket> ID = new Id<>(PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, VultureSuperCurseC2SPacket> CODEC =
            PacketCodec.of(VultureSuperCurseC2SPacket::write, VultureSuperCurseC2SPacket::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
    }

    public static VultureSuperCurseC2SPacket read(PacketByteBuf buf) {
        return new VultureSuperCurseC2SPacket();
    }
}
