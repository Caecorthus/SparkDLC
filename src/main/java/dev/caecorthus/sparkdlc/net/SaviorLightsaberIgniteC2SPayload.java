package dev.caecorthus.sparkdlc.net;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * C2S intent {@code sparkdlc:savior_lightsaber_ignite}: the client's render-time ignition has started. The server
 * re-validates that the player still holds the lightsaber and then broadcasts the ignition sound at the player's
 * position, so nearby players hear it with normal distance attenuation instead of it being a client-local sound.
 * C2S 意图 {@code sparkdlc:savior_lightsaber_ignite}：客户端渲染计时的点火已开始。服务端重新确认玩家仍手持光剑后，
 * 在玩家位置广播点火音，附近玩家可按距离衰减听到，而不是只在客户端本地播放。
 */
public record SaviorLightsaberIgniteC2SPayload() implements CustomPayload {
    public static final Id<SaviorLightsaberIgniteC2SPayload> ID =
            new Id<>(SparkDLC.id("savior_lightsaber_ignite"));
    public static final PacketCodec<RegistryByteBuf, SaviorLightsaberIgniteC2SPayload> CODEC =
            PacketCodec.unit(new SaviorLightsaberIgniteC2SPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
