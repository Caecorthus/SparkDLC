package dev.caecorthus.sparkdlc.net;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.block.NeonBlinkSettings;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Server to client: open the blink settings of the shining neon pillar column at {@code pos}, which blinks with
 * {@code settings} when {@code blinking} (otherwise {@code settings} are the screen's initial values). Sent only to a
 * creative-mode operator who sneak-right-clicked the pillar.
 * 服务端到客户端：打开 {@code pos} 处闪烁霓虹灯柱的闪烁设置；{@code blinking} 为真时它正以 {@code settings} 闪烁
 * （否则 {@code settings} 只是界面的初值）。只发给潜行右键该灯柱的创造模式管理员。
 */
public record OpenNeonPillarSettingsS2CPacket(BlockPos pos, boolean blinking, NeonBlinkSettings settings)
        implements CustomPayload {
    public static final Identifier PAYLOAD_ID = SparkDLC.id("open_neon_pillar_settings");
    public static final Id<OpenNeonPillarSettingsS2CPacket> ID = new Id<>(PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, OpenNeonPillarSettingsS2CPacket> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, OpenNeonPillarSettingsS2CPacket::pos,
            PacketCodecs.BOOL, OpenNeonPillarSettingsS2CPacket::blinking,
            NeonBlinkSettings.PACKET_CODEC, OpenNeonPillarSettingsS2CPacket::settings,
            OpenNeonPillarSettingsS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
