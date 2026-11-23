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
 * Client to server: make the shining neon pillar column at {@code pos} blink with {@code settings}, or stop blinking
 * and stay lit. The server checks everything again ({@code block.ShiningNeonPillarService#applySettings}); the
 * durations are clamped while decoding.
 * 客户端到服务端：让 {@code pos} 处的闪烁霓虹灯柱按 {@code settings} 闪烁，或停止闪烁并保持常亮。服务端会重新校验一切
 * （{@code block.ShiningNeonPillarService#applySettings}）；时长在解码时即被夹到范围内。
 */
public record ApplyNeonPillarSettingsC2SPacket(BlockPos pos, boolean blinking, NeonBlinkSettings settings)
        implements CustomPayload {
    public static final Identifier PAYLOAD_ID = SparkDLC.id("apply_neon_pillar_settings");
    public static final Id<ApplyNeonPillarSettingsC2SPacket> ID = new Id<>(PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, ApplyNeonPillarSettingsC2SPacket> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, ApplyNeonPillarSettingsC2SPacket::pos,
            PacketCodecs.BOOL, ApplyNeonPillarSettingsC2SPacket::blinking,
            NeonBlinkSettings.PACKET_CODEC, ApplyNeonPillarSettingsC2SPacket::settings,
            ApplyNeonPillarSettingsC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
