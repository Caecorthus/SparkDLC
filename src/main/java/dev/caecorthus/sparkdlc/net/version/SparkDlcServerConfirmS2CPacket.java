package dev.caecorthus.sparkdlc.net.version;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Play-phase half of the SparkDLC handshake: the server names its version once the player has joined. It is the only
 * confirmation a client gets behind a proxy, which answers the backend's login query itself. It replaced the two
 * packets the former SparkTraits and SparkWitch handshakes each sent ({@code sparkdlc:trait_server_confirm} and
 * {@code sparkdlc:witch_server_confirm}).
 * SparkDLC 握手的 play 阶段部分：玩家加入后服务端报出自己的版本。代理会自行应答后端的登录查询，因此代理之后的客户端只能靠它
 * 得到确认。它取代了原 SparkTraits 与 SparkWitch 握手各自发送的两个数据包（{@code sparkdlc:trait_server_confirm} 与
 * {@code sparkdlc:witch_server_confirm}）。
 */
public record SparkDlcServerConfirmS2CPacket(String serverVersion) implements CustomPayload {
    public static final Identifier PAYLOAD_ID = SparkDLC.id("server_confirm");
    public static final Id<SparkDlcServerConfirmS2CPacket> ID = new Id<>(PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, SparkDlcServerConfirmS2CPacket> CODEC =
            PacketCodec.of(SparkDlcServerConfirmS2CPacket::write, SparkDlcServerConfirmS2CPacket::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(serverVersion);
    }

    public static SparkDlcServerConfirmS2CPacket read(PacketByteBuf buf) {
        return new SparkDlcServerConfirmS2CPacket(buf.readString(SparkDlcVersionCheck.MAX_VERSION_LENGTH));
    }
}
