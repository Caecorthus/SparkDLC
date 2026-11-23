package dev.caecorthus.sparkdlc.role.neutral.taotie;

import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadRules;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * Empty "launch your heads" request from the second skill key (one volley, one head per living swallowed player).
 * Everything (role, cooldown, swallowed players, skins, aim) is decided on the server from current state; the id is in
 * the daze deny-list and the other skill deny-lists of the payload policy table.
 * 第二技能键发出的空"发射头颅"请求（一次齐射，体内每名存活玩家一颗头颅）。身份、冷却、体内玩家、皮肤与朝向全部由服务端
 * 按当前状态决定；该 id 已列入眩晕拦截表与数据包策略表中其他技能拦截列表。
 */
public record TaotieHeadFireC2SPacket() implements CustomPayload {
    public static final Id<TaotieHeadFireC2SPacket> ID = new Id<>(TaotieHeadRules.FIRE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, TaotieHeadFireC2SPacket> CODEC =
            PacketCodec.unit(new TaotieHeadFireC2SPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
